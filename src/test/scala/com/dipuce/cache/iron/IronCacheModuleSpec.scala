package com.dipuce.cache.iron

import scala.concurrent.duration._

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import play.api.cache.{ AsyncCacheApi, SyncCacheApi }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{ Application, Configuration, PlayException }

class IronCacheModuleSpec extends AnyFunSpec with Matchers {

  private val validConfig: Map[String, Any] = Map(
    "iron.token"      -> "tok",
    "iron.project.id" -> "proj"
  )

  private def withApp[T](config: Map[String, Any])(block: Application => T): T = {
    val app = new GuiceApplicationBuilder().configure(config).build()
    try block(app)
    finally app.stop()
  }

  describe("IronCacheModule") {
    it("is enabled by reference.conf and binds Play's Scala and Java cache APIs to Iron Cache") {
      withApp(validConfig) { app =>
        val injector = app.injector
        injector.instanceOf[AsyncCacheApi] shouldBe a[WsIronCacheApi]
        injector.instanceOf[IronCacheApi] should be theSameInstanceAs injector.instanceOf[AsyncCacheApi]
        injector.instanceOf[SyncCacheApi] shouldBe an[IronSyncCacheApi]
        injector.instanceOf[play.cache.AsyncCacheApi] shouldBe a[play.cache.DefaultAsyncCacheApi]
        injector.instanceOf[play.cache.SyncCacheApi] shouldBe a[play.cache.DefaultSyncCacheApi]
      }
    }

    it("applies the reference.conf defaults") {
      withApp(validConfig) { app =>
        val config = app.injector.instanceOf[IronCacheConfig]
        config.host shouldBe "https://cache-aws-us-east-1.iron.io"
        config.cacheName shouldBe "cache"
        config.timeout shouldBe 5.seconds
        config.endpoints.items shouldBe "https://cache-aws-us-east-1.iron.io/1/projects/proj/caches/cache/items"
      }
    }

    it("fails fast when the token or project id is missing") {
      withApp(validConfig - "iron.token") { app =>
        val ex = intercept[Exception](app.injector.instanceOf[IronCacheApi])
        ex.getMessage should include("iron.token")
      }
    }
  }

  describe("IronCacheConfig.fromConfiguration") {
    it("reads overrides and strips a trailing slash from the host") {
      val config = IronCacheConfig.fromConfiguration(
        Configuration.from(
          Map(
            "iron.token"         -> "t",
            "iron.project.id"    -> "p",
            "iron.cache.host"    -> "https://cache-aws-eu-west-1.iron.io/",
            "iron.cache.name"    -> "sessions",
            "iron.cache.timeout" -> "2 seconds"
          )
        )
      )
      config shouldBe IronCacheConfig("https://cache-aws-eu-west-1.iron.io", "sessions", "t", "p", 2.seconds)
    }

    it("reports a missing required key") {
      val ex = intercept[PlayException](
        IronCacheConfig.fromConfiguration(Configuration.from(Map("iron.token" -> "t", "iron.cache.host" -> "h",
          "iron.cache.name" -> "n", "iron.cache.timeout" -> "1 second")))
      )
      ex.getMessage should include("iron.project.id")
    }
  }
}
