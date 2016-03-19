package com.dipuce.cache.iron

import play.api.test.FakeApplication
import play.api.cache.Cache
import scala.reflect.ClassTag


/**
 * A Test!
 */
class IronCachePluginSpec extends IronSpec {

  val fakeApp = FakeApplication(
    additionalPlugins = Seq("com.dipuce.cache.iron.IronCachePlugin"),
    additionalConfiguration = Map("ehcacheplugin" -> "disabled")
  )

  describe("Basic Reads") {
    it("Can connect to Iron Cache") {
       val cacheValue = "val"
       Cache.set("test", cacheValue, 3600)(fakeApp)
       val result = Cache.getAs[String]("test")(fakeApp, ClassTag(cacheValue.getClass))
       result shouldEqual cacheValue
    }
  }
}
