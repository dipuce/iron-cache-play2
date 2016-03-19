package com.dipuce.cache.iron.provider

import com.dipuce.cache.iron.rest.actions.{HttpIronAPI, IronAPI}
import com.dipuce.cache.iron.permissions.ConfigResolverFactory
import play.api.Application
import com.dipuce.cache.iron.rest.RestEndpoints
import play.api.cache.CacheAPI

/**
 * Created by Mike on 3/18/2016.
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
