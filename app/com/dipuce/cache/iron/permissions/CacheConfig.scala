package com.dipuce.cache.iron.permissions

/**
 * Created by Mike on 3/18/2016.
 */
case class CacheConfig(hostName: String,
                       cacheName: String,
                       oAuthToken: String,
                       projectId: String,
                       apiTimeoutSeconds: Int)
