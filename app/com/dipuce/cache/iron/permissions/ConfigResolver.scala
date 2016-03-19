package com.dipuce.cache.iron.permissions

/**
 * Gets and validates config parameters
 *
 * @author mikegarrett
 * @since 03/18/2016
 */
case class ConfigResolver(hostName: Option[String],
                          cacheName: Option[String],
                          oAuthToken: Option[String],
                          projectId: Option[String],
                          apiTimeoutSeconds: Option[Int]) {

  def isValid: Boolean = {
    List(hostName, cacheName, oAuthToken, projectId, apiTimeoutSeconds).forall(_.isDefined)
  }

  def toConfig: Option[CacheConfig] = {
    for {
      host <- hostName
      cache <- cacheName
      token <- oAuthToken
      id <- projectId
      timeout <- apiTimeoutSeconds
    } yield CacheConfig(host, cache, token, id, timeout)
  }

  def getMissingFieldErrorMsg: String = {
    val notDefined = " was not defined."
    val builder = new StringBuilder()
    if(hostName.isEmpty) { builder.append(s"Host name$notDefined") }
    if(cacheName.isEmpty) { builder.append(s"Cache name$notDefined") }
    if(oAuthToken.isEmpty) { builder.append(s"oAuth Token$notDefined") }
    if(projectId.isEmpty) { builder.append(s"Project ID$notDefined") }
    builder.toString()
  }

}

object ConfigResolver {
  val defaultHost = Option("cache-aws-us-east-1")
  val defaultCache = Option("cache")
  val defaultTimeout = Option(5)
}
