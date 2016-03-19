package com.dipuce.cache.iron.keys

/**
 * Created by Mike on 3/18/2016.
 */
trait IronPropertyKeys {

    def iron = "iron"

    def cache = "cache"

    def projectIdKey = s"$iron.project.id"

    def oAuthTokenKey = s"$iron.token"

    def cacheNameKey = s"$iron.$cache.name"

    def hostNameKey = s"$iron.$cache.host"

    def timeoutSecondsKey = s"$iron.$cache.timeout"

}
