package com.dipuce.cache.iron.provider

import com.dipuce.cache.iron.rest.actions.{HttpIronAPI, IronAPI}
import com.dipuce.cache.iron.permissions.ConfigResolverFactory
import play.api.Application
import com.dipuce.cache.iron.rest.RestEndpoints
import play.api.cache.CacheAPI

/**
 * Constructs an API instance with config properties taken from the user's application
 * configuration file.
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
case class IronCacheProvider(api: IronAPI, app: Application) extends ConfigResolverFactory {

  val cacheConfig = makeCacheConfig

}

case class DefaultIronCacheProvider(app: Application) extends IronCacheProvider {
   override val api: IronAPI = new DefaultHttpIronAPI()

   class DefaultHttpIronAPI extends HttpIronAPI with CacheAPI {
     override def authToken: String = cacheConfig.oAuthToken

     override def timeout: Int = cacheConfig.apiTimeoutSeconds

     override def endpoints: RestEndpoints = new RestEndpoints(cacheConfig.hostName, cacheConfig.cacheName)
   }
}
