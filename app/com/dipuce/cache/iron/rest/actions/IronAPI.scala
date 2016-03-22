package com.dipuce.cache.iron.rest.actions

import com.dipuce.cache.iron.rest.RestEndpoints
import play.api.cache.CacheApi
import scala.concurrent.duration.Duration

/**
 * Iron Cache API descriptor
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
trait IronAPI extends CacheApi {

   def endpoints: RestEndpoints

   def get(key: String): Option[String]

   def set(key: String, value: Any, expiration: Duration)

   def delete(key: String): Unit

   def increment(key: String, amount: Int): Option[Int]

   def decrement(key: String, amount: Int): Option[Int]

   def clear(): Unit

   def listCaches(page: Int = 0): Map[String, String]
}
