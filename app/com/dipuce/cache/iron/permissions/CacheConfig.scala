package com.dipuce.cache.iron.permissions

/**
 * Cache Config
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
case class CacheConfig(hostName: String,
                       cacheName: String,
                       oAuthToken: String,
                       projectId: String,
                       apiTimeoutSeconds: Int)
