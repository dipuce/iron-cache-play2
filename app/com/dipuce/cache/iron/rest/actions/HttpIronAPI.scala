package com.dipuce.cache.iron.rest.actions
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import com.dipuce.cache.iron.messaging.UserMessages
import org.apache.http.protocol.HTTP
import org.apache.http.client.methods._
import play.api.libs.json.JsArray
import play.api.libs.json.JsUndefined
import com.dipuce.cache.iron.rest.helpers.HttpHelpers
import scala.concurrent.Future

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

  val auth = ("Authorization", s"OAuth $authToken")
  val jsonCT = (HTTP.CONTENT_TYPE,"application/json")

  def fGet(key: String): Future[Option[String]] = {
     val api = s"${endpoints.cacheUri}/$key"
     val holder = makeRequest(api)
     val result = executeRequestWithResponse(holder, HttpGet.METHOD_NAME)

     result.map {
       case JsNull | JsUndefined() => None
       case jvalue@JsObject(_) => Some( (jvalue \ "value").toString() )
     }

  }

  def fSet(key: String, value: Any, expiration: Int): Future[Boolean] = {
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
    val result = executeRequestWithNoResponse(holder, HttpPut.METHOD_NAME, Option(body.toString()))
    result
  }

  def fClear(): Future[Boolean] = {
    val holder = makeRequest(endpoints.clearCacheUri)
    val result = executeRequestWithNoResponse(holder, HttpPost.METHOD_NAME)
    result
  }

  def fIncrement(key: String, amount: Int): Future[Option[Int]] = {
    val holder = makeRequest(endpoints.incrementUri(key))
    val body = JsObject(Seq( ("amount", JsNumber(amount)) )).toString()
    val result = executeRequestWithResponse(holder, HttpPost.METHOD_NAME, Option(body))
    result.map {
      case jvalue@JsObject(_) => Some((jvalue \ "value").toString().toInt)
      case _ => None
    }
  }

  def fDecrement(key: String, amount: Int): Future[Option[Int]] = fIncrement(key, -amount)

  def fDelete(key: String): Future[Boolean] = {
    val holder = makeRequest(s"${endpoints.cacheUri}/$key")
    val result = executeRequestWithNoResponse(holder, HttpDelete.METHOD_NAME)
    result
  }


  def fListCaches(page: Int = 0): Future[Map[String, String]] = {
    val pageParam = ("page", page.toString)
    val holder = makeRequest(endpoints.listCacheUri).withQueryString(pageParam)

    val result = executeRequestWithResponse(holder, HttpGet.METHOD_NAME)

    result.map {
      case JsUndefined() | JsNull => Map.empty
      case JsArray(values) =>
        values.map(j => (j \ "project_id").toString() -> (j \ "name").toString()).toMap
    }
  }

  // Interface Implementations
  override def remove(key: String): Unit = getResult(fDelete(key))

  override def get(key: String): Option[String] = getResult(fGet(key))

  override def set(key: String, value: Any, expiration: Int): Unit = getResult(fSet(key, value, expiration))

  override def delete(key: String): Unit = getResult(fDelete(key))

  override def increment(key: String, amount: Int): Option[Int] = getResult(fIncrement(key, amount))

  override def decrement(key: String, amount: Int): Option[Int] = getResult(fDecrement(key, amount))

  override def clear(): Unit = getResult(fClear(), cannotClear)

  override def listCaches(page: Int): Map[String, String] = getResult(fListCaches(page))
}
