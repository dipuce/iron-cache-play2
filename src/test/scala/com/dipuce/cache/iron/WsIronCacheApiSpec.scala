package com.dipuce.cache.iron

import scala.concurrent.Future
import scala.concurrent.duration._

import org.apache.pekko.Done
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{ Seconds, Span }
import play.api.libs.json.{ JsNumber, JsString, Json }

class WsIronCacheApiSpec extends AnyFunSpec with Matchers with ScalaFutures {

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(10, Seconds))

  import IronCacheStub.withApi

  describe("set / get") {
    it("round-trips strings") {
      withApi() { (api, _) =>
        api.set("greeting", "hello", 1.hour).futureValue shouldBe Done
        api.get[String]("greeting").futureValue shouldBe Some("hello")
      }
    }

    it("round-trips numbers and booleans") {
      withApi() { (api, _) =>
        api.set("int", 42).futureValue
        api.set("long", 1L << 40).futureValue
        api.set("double", 1.5).futureValue
        api.set("bool", true).futureValue

        api.get[Int]("int").futureValue shouldBe Some(42)
        api.get[Long]("long").futureValue shouldBe Some(1L << 40)
        api.get[Double]("double").futureValue shouldBe Some(1.5)
        api.get[Boolean]("bool").futureValue shouldBe Some(true)
        api.get[Any]("int").futureValue shouldBe Some(42)
        api.get[Any]("bool").futureValue shouldBe Some(true)
      }
    }

    it("round-trips raw JSON values") {
      withApi() { (api, _) =>
        val payload = Json.obj("a" -> 1, "b" -> Json.arr("x"))
        api.set("json", payload).futureValue
        api.get[play.api.libs.json.JsValue]("json").futureValue shouldBe Some(payload)
      }
    }

    it("returns None for a missing key") {
      withApi() { (api, _) =>
        api.get[String]("nope").futureValue shouldBe None
      }
    }

    it("returns None when the stored value has a different type") {
      withApi() { (api, _) =>
        api.set("s", "not a number").futureValue
        api.get[Int]("s").futureValue shouldBe None
      }
    }

    it("sends the expiration in seconds and omits it for Duration.Inf") {
      withApi() { (api, state) =>
        api.set("ttl", "v", 90.seconds).futureValue
        api.set("forever", "v").futureValue
        state.expiries.get("ttl") shouldBe Some(90L)
        state.expiries.get("forever") shouldBe None
      }
    }

    it("URL-encodes keys") {
      withApi() { (api, state) =>
        api.set("a key/with:odd chars", "v").futureValue
        state.items.keySet should contain("a key/with:odd chars")
        api.get[String]("a key/with:odd chars").futureValue shouldBe Some("v")
      }
    }

    it("rejects values Iron Cache cannot store") {
      withApi() { (api, _) =>
        api.set("obj", new Object).failed.futureValue shouldBe an[IllegalArgumentException]
      }
    }
  }

  describe("getOrElseUpdate") {
    it("computes and stores a missing value, then serves the cached one") {
      withApi() { (api, _) =>
        var calls = 0
        def compute: Future[String] = { calls += 1; Future.successful("computed") }

        api.getOrElseUpdate[String]("lazy", 1.minute)(compute).futureValue shouldBe "computed"
        api.getOrElseUpdate[String]("lazy", 1.minute)(compute).futureValue shouldBe "computed"
        calls shouldBe 1
      }
    }
  }

  describe("remove / removeAll") {
    it("removes a key and tolerates removing a missing one") {
      withApi() { (api, _) =>
        api.set("k", "v").futureValue
        api.remove("k").futureValue shouldBe Done
        api.get[String]("k").futureValue shouldBe None
        api.remove("k").futureValue shouldBe Done
      }
    }

    it("clears the whole cache") {
      withApi() { (api, state) =>
        api.set("a", 1).futureValue
        api.set("b", 2).futureValue
        api.removeAll().futureValue shouldBe Done
        state.items shouldBe empty
        api.clear().futureValue shouldBe Done
        state.clears shouldBe 2
      }
    }
  }

  describe("Iron extras") {
    it("increments and decrements") {
      withApi() { (api, _) =>
        api.set("n", 5).futureValue
        api.increment("n", 2).futureValue shouldBe Some(7L)
        api.decrement("n", 4).futureValue shouldBe Some(3L)
        api.increment("n").futureValue shouldBe Some(4L)
        api.get[Int]("n").futureValue shouldBe Some(4)
      }
    }

    it("returns None when incrementing a missing key") {
      withApi() { (api, _) =>
        api.increment("missing").futureValue shouldBe None
      }
    }

    it("lists caches") {
      withApi() { (api, _) =>
        api.listCaches().futureValue shouldBe Map(IronCacheStub.CacheName -> IronCacheStub.ProjectId)
      }
    }
  }

  describe("errors") {
    it("fails with IronCacheException on an unexpected status") {
      withApi() { (api, _) =>
        api.set("bad", JsString("x")).futureValue
        api.increment("bad").failed.futureValue should matchPattern { case IronCacheException(400, _) => }
      }
    }
  }

  describe("value conversion") {
    import WsIronCacheApi.{ fromJson, toJson }

    it("maps Scala values to JSON") {
      toJson("s") shouldBe Right(JsString("s"))
      toJson(3) shouldBe Right(JsNumber(3))
      toJson(2.5f) shouldBe Right(JsNumber(2.5))
      toJson(null) shouldBe Right(play.api.libs.json.JsNull)
      toJson(List(1)) shouldBe a[Left[_, _]]
    }

    it("refuses lossy numeric narrowing") {
      fromJson[Int](JsNumber(BigDecimal(Long.MaxValue))) shouldBe None
      fromJson[Long](JsNumber(BigDecimal(Long.MaxValue))) shouldBe Some(Long.MaxValue)
      fromJson[String](JsNumber(7)) shouldBe Some("7")
    }
  }
}
