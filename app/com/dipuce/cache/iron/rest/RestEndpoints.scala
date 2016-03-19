package com.dipuce.cache.iron.rest

/**
 * Endpoint Repository
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
class RestEndpoints(projectAddress: String, cacheName: String) {

  val listCacheUri = s"$projectAddress/caches"
  /**
   * URL all other calls are based on
   */
  val baseUri = s"$listCacheUri/$cacheName"

  /**
   * Endpoint to call when the user needs to clear everything from cache
   */
  val clearCacheUri = s"$baseUri/clear"

  /**
   * Main endpoint that all cache operations (add, delete, etc) need to interact
   * with
   */
  val cacheUri = s"$baseUri/items"

  def incrementUri(key: String) = s"$cacheUri/$key/increment"

}
