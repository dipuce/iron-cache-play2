package com.dipuce.cache.iron.rest.actions
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import com.dipuce.cache.iron.messaging.UserMessages
import org.apache.http.protocol.HTTP
import org.apache.http.client.methods._
import play.api.libs.json.JsArray
import play.api.libs.json.JsUndefined
import com.dipuce.cache.iron.rest.helpers.HttpHelpers

/**
 * An implementation of `IronAPI` centered around actual HTTP calls
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
trait HttpIronAPI extends IronAPI with UserMessages with HttpHelpers {

  def authToken: String

  def timeout: Int

  val auth = ("Authorization", s"OAuth $authToken")
  val jsonCT = (HTTP.CONTENT_TYPE,"application/json")

  override def get(key: String): Option[String] = {
     val api = s"${endpoints.cacheUri}/$key"
     val holder = makeRequest(api)
     val result = executeRequestWithResponse(holder, HttpGet.METHOD_NAME)

     result match {
       case JsNull | JsUndefined => None
       case JsObject => Some( (result \ "value").toString() )
     }

  }

  override def set(key: String, value: Any, expiration: Int): Unit = {
    val api = s"${endpoints.cacheUri}/$key"
    val holder = makeRequest(api)
    val typedValue: JsValue = value match {
              case i: String => JsString(value.asInstanceOf[String])
              case i: Int =>    JsNumber(value.asInstanceOf[Int])
              case i: Double => JsNumber(value.asInstanceOf[Double])
              case i: Boolean => JsBoolean(value.asInstanceOf[Boolean])
              case _ => JsNull
            }
    val body = JsObject(Seq(
      "value" -> typedValue,
      "expires_in" -> JsNumber(expiration)
    ))
    val result = executeRequestWithNoResponse(holder, HttpPut.METHOD_NAME, Option(body))
    logOnResultError(result)
  }

  override def clear(): Unit = {
    val holder = makeRequest(endpoints.clearCacheUri)
    val result = executeRequestWithNoResponse(holder, HttpPost.METHOD_NAME)
    logOnResultError(result, cannotClear)
  }

  override def increment(key: String, amount: Int): Option[Int] = {
    val holder = makeRequest(endpoints.incrementUri(key))
    val body = JsObject(Seq( ("amount", JsNumber(amount)) )).toString()
    val result = executeRequestWithResponse(holder, HttpPost.METHOD_NAME, Option(body))
    result match {
      case JsObject => Some( (result \ "value").toString().toInt )
      case _ => None
    }
  }

  override def decrement(key: String, amount: Int): Option[Int] = increment(key, -amount)

  override def delete(key: String): Unit = {
    val holder = makeRequest(s"${endpoints.cacheUri}/$key")
    val result = executeRequestWithNoResponse(holder, HttpDelete.METHOD_NAME)
    logOnResultError(result)
  }

  def listCaches(page: Int = 0) = {
    val page = ("page", page.toString)
    val holder = makeRequest(endpoints.listCacheUri).withQueryString(page)

    val result = executeRequestWithResponse(holder, HttpGet.METHOD_NAME)

    result match {
      case JsUndefined | JsNull => Map.empty
      case JsArray(values) =>
        values.map(j => (j \ "project_id").toString() -> (j \ "name").toString()).toMap
    }
  }
}
