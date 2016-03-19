package com.dipuce.cache.iron.rest.actions

import com.dipuce.cache.iron.rest.RestEndpoints
import play.api.cache.CacheAPI

/**
 * Iron Cache API descriptor
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
trait IronAPI extends CacheAPI {

   def endpoints: RestEndpoints

   def get(key: String): Option[String]

   def set(key: String, value: Any, expiration: Int)

   def delete(key: String)

   def increment(key: String, amount: Int): Option[Int]

   def decrement(key: String, amount: Int): Option[Int]

   def clear()
}
