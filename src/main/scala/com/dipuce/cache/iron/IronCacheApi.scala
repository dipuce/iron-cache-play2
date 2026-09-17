package com.dipuce.cache.iron

import scala.concurrent.Future

import org.apache.pekko.Done
import play.api.cache.AsyncCacheApi

/**
 * Play's [[play.api.cache.AsyncCacheApi]] plus the operations Iron Cache offers on top of it.
 * Inject this type instead of `AsyncCacheApi` when you need the extras.
 */
trait IronCacheApi extends AsyncCacheApi {

  /** Atomically adds `amount` (which may be negative) to the numeric value stored at `key`. */
  def increment(key: String, amount: Long = 1): Future[Option[Long]]

  /** Atomically subtracts `amount` from the numeric value stored at `key`. */
  def decrement(key: String, amount: Long = 1): Future[Option[Long]] = increment(key, -amount)

  /** Removes every item from the cache. Alias of [[removeAll]]. */
  def clear(): Future[Done] = removeAll()

  /** Lists the caches of the configured project as `name -> projectId`, 100 per page. */
  def listCaches(page: Int = 0): Future[Map[String, String]]
}
