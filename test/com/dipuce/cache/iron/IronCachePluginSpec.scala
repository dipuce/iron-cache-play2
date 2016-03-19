package com.dipuce.cache.iron

import play.api.test.FakeApplication
import play.api.cache.Cache
import play.api.Application
import com.dipuce.cache.iron.permissions.ConfigResolver


/**
 * A Test!
 */
class IronCachePluginSpec extends IronSpec {

  val fakeApp = FakeApplication(
    additionalPlugins = Seq("com.dipuce.cache.iron.IronCachePlugin"),
    additionalConfiguration = Map("ehcacheplugin" -> "disabled")
  )

  implicit val app: Application = fakeApp

  override def afterAll() {
    // Remove everything at the end
    fakeApp.plugin[IronCachePlugin].get.provider.api.clear()
  }

  describe("Basic Reads") {
    it("Can connect to Iron Cache") {
       val cacheValue = "val"
       Cache.set("test", cacheValue, 3600)
       val result = Cache.getAs[String]("test")
       result shouldEqual cacheValue
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
    val ironPlugin = fakeApp.plugin[IronCachePlugin].get
    val cacheAPI = ironPlugin.provider.api
    it("Can increment a key") {
       val num = 5
       val incAmount = 2
       cacheAPI.set("nums", num, 3600)
       cacheAPI.increment("nums", incAmount)

       val result = Cache.get("nums")
       result.isDefined shouldEqual true
       result.get.toString.toInt shouldEqual 7
    }

    it("Can decrement a key") {
      val num = 5
      val decAmount = 2
      cacheAPI.set("nums", num, 3600)
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
