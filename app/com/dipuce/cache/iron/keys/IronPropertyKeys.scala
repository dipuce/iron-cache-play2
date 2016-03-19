package com.dipuce.cache.iron.keys

/**
 * Configuration Keys
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
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
