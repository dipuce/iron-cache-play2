package com.dipuce.cache.iron

import play.api.test.FakeApplication
import play.api.cache.{CacheApi, Cache}
import play.api.Application
import com.dipuce.cache.iron.permissions.ConfigResolver
import scala.concurrent.duration.Duration
import javax.inject.Inject


/**
 * A Test!
 */
class IronCacheModuleSpec @Inject() (cache: CacheApi) extends IronSpec {

  val mySettings = Map("iron.project.id" -> "52ae817a45d8040009000032",
  "iron.token" -> "S7hwr550H0oh-4-7w_5mGzBz6sQ")
  val fakeApp = FakeApplication(
    additionalConfiguration = Map("ehcacheplugin" -> "disabled",
    "play.modules.enabled" -> Seq("com.dipuce.cache.iron.IronCacheModule")) ++ mySettings
  )

  implicit val app: Application = fakeApp

  val fiveSecs = Duration("5s")

  override def afterAll() {
    // Remove everything at the end
    //cache.clear()
  }

  describe("Basic Reads") {
    it("Can connect to Iron Cache") {
       val cacheValue = "val"
       Cache.set("test", cacheValue, 3600)
       val result = Cache.getAs[String]("test")
       result.get shouldEqual cacheValue
    }
  }

  describe("Basic Remove") {
    it("Can remove a set key") {
      val cacheValue = "val2"
      Cache.set("test", cacheValue, 3600)
      Cache.remove("test")

      val maybeResult = Cache.get("test")
      maybeResult.isEmpty shouldEqual true
    }
  }

  describe("Extended Features") {
    val ironPlugin = fakeApp.plugin[IronCacheModule].get
    val cacheAPI = ironPlugin.provider.api
    it("Can increment a key") {
       val num = 5
       val incAmount = 2
       cacheAPI.set("nums", num, fiveSecs)
       cacheAPI.increment("nums", incAmount)

       val result = Cache.get("nums")
       result.isDefined shouldEqual true
       result.get.toString.toInt shouldEqual 7
    }

    it("Can decrement a key") {
      val num = 5
      val decAmount = 2
      cacheAPI.set("nums", num, fiveSecs)
      cacheAPI.increment("nums", decAmount)

      val result = Cache.get("nums")
      result.isDefined shouldEqual true
      result.get.toString.toInt shouldEqual 3
    }

    it("Can list current caches") {
      val caches = cacheAPI.listCaches()
      val defCache = caches.get(ConfigResolver.defaultCache.get)
      defCache.isDefined shouldEqual true
    }
  }
}
