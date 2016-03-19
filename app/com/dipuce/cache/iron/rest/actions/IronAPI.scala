package com.dipuce.cache.iron.rest.actions

import com.dipuce.cache.iron.rest.RestEndpoints

/**
 * Created by Mike on 3/18/2016.
 */
trait IronAPI {

   def endpoints: RestEndpoints

   def get(key: String): Option[String]

   def set(key: String, value: Any, expiration: Int)

   def delete(key: String)

   def increment(key: String, amount: Int): Option[Int]

   def decrement(key: String, amount: Int): Option[Int]

   def clear()
}
