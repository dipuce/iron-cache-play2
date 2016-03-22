package com.dipuce.cache.iron

import play.api.{Configuration, Environment, Plugin, Application}
import com.dipuce.cache.iron.permissions.ConfigResolverFactory
import com.dipuce.cache.iron.provider.{DefaultIronCacheProvider, IronCacheProvider}
import play.api.inject.{Binding, Module}
import javax.inject.Singleton
import play.api.cache._
import play.api.inject.Binding
import com.dipuce.cache.iron.provider.DefaultIronCacheProvider
import play.cache.NamedCacheImpl
import com.dipuce.cache.iron.rest.actions.HttpIronAPI

/**
 * Iron Cache Plugin main file
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.1
 * Dipuce, LLC
 */
@Singleton
class IronCacheModule(override val app: Application) extends Module with ConfigResolverFactory {

    implicit def current: Application = app

    val provider: IronCacheProvider = new DefaultIronCacheProvider(app)
    val api: HttpIronAPI = provider.api

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    import scala.collection.JavaConversions._

    val defaultCacheName = configuration.underlying.getString("play.cache.defaultCache")
    val bindCaches = configuration.underlying.getStringList("play.cache.bindCaches").toSeq

    def named(name: String): NamedCache = {
      new NamedCacheImpl(name)
    }

    Seq(
      bind[CacheApi].to(bind[HttpIronAPI].qualifiedWith(named(defaultCacheName)))
    )
  }
}