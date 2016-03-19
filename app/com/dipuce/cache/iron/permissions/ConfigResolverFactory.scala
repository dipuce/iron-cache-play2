package com.dipuce.cache.iron.permissions

import play.api.Application
import com.dipuce.cache.iron.keys.IronPropertyKeys
import play.Logger
import com.dipuce.cache.iron.messaging.UserMessages

/**
 * Produces a properly validated configuration. In the cases where the
 * user does not supply all the parameters, this class will attempt to
 * fill in the missing values with sensible defaults.
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
trait ConfigResolverFactory extends IronPropertyKeys with UserMessages {

  def app: Application

  def makeCacheConfig: CacheConfig = {
    val configResolver = ConfigResolver(
        hostName = getProp(hostNameKey),
        oAuthToken = getProp(oAuthTokenKey),
        cacheName = getProp(cacheNameKey),
        projectId = getProp(projectIdKey),
        apiTimeoutSeconds = getProp(timeoutSecondsKey)
    )

    val cacheConfigFromUser = configResolver.isValid match {
      case true => configResolver.toConfig.get
      case _ =>
        Logger.warn(notAllFieldsProvided)
        val defaultConfig = configResolver.copy(
          hostName = configResolver.hostName.getOrElse(ConfigResolver.defaultHost),
          cacheName = configResolver.cacheName.getOrElse(ConfigResolver.defaultCache),
          apiTimeoutSeconds = configResolver.apiTimeoutSeconds.getOrElse(ConfigResolver.defaultTimeout))
        defaultConfig.toConfig match {
          case None =>
            val ise = new IllegalStateException(defaultConfig.getMissingFieldErrorMsg)
            Logger.error(cannotContinue, ise)
            throw ise
          case Some(config) => config
        }
    }

    cacheConfigFromUser
  }

  private def getProp(name: String): Option[String] = {
    app.configuration.getString(name)
  }

}
