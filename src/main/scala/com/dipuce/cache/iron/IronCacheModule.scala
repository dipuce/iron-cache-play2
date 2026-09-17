package com.dipuce.cache.iron

import javax.inject.{ Inject, Provider, Singleton }

import scala.concurrent.duration.Duration

import play.api.cache.{ AsyncCacheApi, DefaultSyncCacheApi, SyncCacheApi }
import play.api.inject.{ Binding, Module }
import play.api.{ Configuration, Environment }

/**
 * Binds Play's cache APIs (Scala and Java, async and sync) to Iron Cache.
 *
 * Enabled automatically through `reference.conf`. If another cache module is on the classpath,
 * disable it in `application.conf`:
 * {{{
 * play.modules.disabled += "play.api.cache.ehcache.EhCacheModule"
 * }}}
 */
class IronCacheModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[IronCacheConfig].toProvider[IronCacheConfigProvider],
    bind[IronCacheApi].to[WsIronCacheApi],
    bind[AsyncCacheApi].to(bind[IronCacheApi]),
    bind[SyncCacheApi].to[IronSyncCacheApi],
    bind[play.cache.AsyncCacheApi].to[play.cache.DefaultAsyncCacheApi],
    bind[play.cache.SyncCacheApi].to[play.cache.DefaultSyncCacheApi]
  )
}

@Singleton
class IronCacheConfigProvider @Inject() (configuration: Configuration) extends Provider[IronCacheConfig] {
  lazy val get: IronCacheConfig = IronCacheConfig.fromConfiguration(configuration)
}

/** Blocking facade whose await timeout follows `iron.cache.timeout`. */
@Singleton
class IronSyncCacheApi @Inject() (async: IronCacheApi, config: IronCacheConfig) extends DefaultSyncCacheApi(async) {
  override protected val awaitTimeout: Duration = config.timeout
}
