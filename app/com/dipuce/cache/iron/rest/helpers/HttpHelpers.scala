package com.dipuce.cache.iron.rest.helpers

import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.json.{JsUndefined, JsValue}
import com.dipuce.cache.iron.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.Logger
import play.api.libs.ws.WS
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import org.apache.http.client.methods.{HttpPut, HttpPost}
import com.dipuce.cache.iron.messaging.UserMessages

/**
 * Methods to create and manipulate HTTP reponses in a
 * consistent way for all actions.
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
trait HttpHelpers {
  this: UserMessages =>

  def timeout: Int

  def auth: (String, String)
  def jsonCT: (String, String)

  protected def executeRequestWithResponse(holder: WSRequestHolder,
                                           method: String,
                                           body: Option[String] = None): JsValue = {

    val fResponse = produceFutureResponseWithBody(holder, method, body)

    val result: Future[JsValue] = fResponse.map { response =>
      APIResponseProvider.getResponse(response.status) match {
        case SuccessfulResponse() =>
          Logger.debug(holder.url)
          response.json

        case FatalResponse() =>  // user error
          val iae = new IllegalArgumentException( (response.json \ "msg").toString() )
          Logger.error(fatalMsg, iae)
          throw iae

        case UnsuccessfulResponse() =>
          val error = (response.json \ "msg").toString()
          Logger.warn(myError + error)
          JsUndefined(error)
      }
    }

    getResult(result)
  }

  protected def executeRequestWithNoResponse(holder: WSRequestHolder,
                                             method: String,
                                             body: Option[String] = None): Boolean = {
    val fResponse = produceFutureResponseWithBody(holder, method, body)

    val result: Future[Boolean] = fResponse.map { response =>
      APIResponseProvider.getResponse(response.status) match {
        case SuccessfulResponse() =>
          Logger.debug(holder.url)
          true

        case FatalResponse() =>  // user error
          val iae = new IllegalArgumentException( (response.json \ "msg").toString() )
          Logger.error(fatalMsg, iae)
          throw iae

        case UnsuccessfulResponse() =>
          val error = (response.json \ "msg").toString()
          Logger.warn(myError + error)
          false
      }
    }

    getResult(result)
  }

  protected def makeRequest(endpoint: String) = {
    WS.url(endpoint).withHeaders(auth, jsonCT)
  }

  protected def getResult[T](f: Future[T]): T = {
    val duration = Duration(timeout, "s")
    Await.result(f, duration)
  }

  protected def produceFutureResponseWithBody(holder: WSRequestHolder, method: String, body: Option[String]) = {
    body match {
      case None => holder.execute(method)
      case Some(jsBody) => method match {
        case HttpPost.METHOD_NAME => holder.post(jsBody)
        case HttpPut.METHOD_NAME => holder.put(jsBody)
        case _ => holder.execute(method)
      }
    }
  }

  protected def logOnResultError(result: Boolean, msg: String = genericCannot) {
    if(!result) {
      Logger.warn(msg)
    }
  }
}
