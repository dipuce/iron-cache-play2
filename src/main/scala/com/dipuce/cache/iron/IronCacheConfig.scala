package com.dipuce.cache.iron

import scala.concurrent.duration.FiniteDuration

import play.api.Configuration

/**
 * Resolved configuration for the Iron Cache module.
 *
 * @param host      base URL of the Iron Cache region, e.g. `https://cache-aws-us-east-1.iron.io`
 * @param cacheName name of the cache all keys are stored in
 * @param token     Iron.io OAuth token
 * @param projectId Iron.io project id
 * @param timeout   per-request timeout; also the await timeout of the synchronous API
 */
final case class IronCacheConfig(
    host: String,
    cacheName: String,
    token: String,
    projectId: String,
    timeout: FiniteDuration
) {
  lazy val endpoints: RestEndpoints = RestEndpoints(s"$host/1/projects/$projectId", cacheName)
}

object IronCacheConfig {

  /** Configuration keys, unchanged from the Play 2.x plugin so existing `application.conf`s keep working. */
  object Keys {
    val token: String     = "iron.token"
    val projectId: String = "iron.project.id"
    val host: String      = "iron.cache.host"
    val cacheName: String = "iron.cache.name"
    val timeout: String   = "iron.cache.timeout"
  }

  /**
   * Reads the module configuration. `host`, `cacheName` and `timeout` fall back to the
   * defaults in `reference.conf`; `token` and `projectId` have no sensible default and
   * raise a configuration error when absent.
   */
  def fromConfiguration(configuration: Configuration): IronCacheConfig = {
    def required(key: String): String =
      configuration.getOptional[String](key).map(_.trim).filter(_.nonEmpty).getOrElse {
        throw configuration.reportError(key, s"Iron Cache requires `$key` to be set")
      }

    IronCacheConfig(
      host = required(Keys.host).stripSuffix("/"),
      cacheName = required(Keys.cacheName),
      token = required(Keys.token),
      projectId = required(Keys.projectId),
      timeout = configuration.get[FiniteDuration](Keys.timeout)
    )
  }
}
