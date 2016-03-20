package com.dipuce.cache.iron

import play.api.cache.{CacheAPI, CachePlugin}
import play.api.Application
import com.dipuce.cache.iron.permissions.ConfigResolverFactory
import com.dipuce.cache.iron.provider.{DefaultIronCacheProvider, IronCacheProvider}

/**
 * Iron Cache Plugin main file
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.1
 * Dipuce, LLC
 */
class IronCachePlugin(override val app: Application) extends CachePlugin with ConfigResolverFactory {

    implicit def current: Application = app

    val provider: IronCacheProvider = new DefaultIronCacheProvider(app)

    override def api: CacheAPI = provider.api

    override def onStart() {
      play.Logger.info(s"Iron Cache plugin started.")
      super.onStart()
    }

    override def onStop() {
      super.onStop()
    }

}