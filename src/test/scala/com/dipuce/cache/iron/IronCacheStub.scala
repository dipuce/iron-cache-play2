package com.dipuce.cache.iron

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._

import play.api.BuiltInComponents
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{ Handler, RequestHeader }
import play.api.routing.sird._
import play.api.test.WsTestClient
import play.core.server.Server

/**
 * An in-memory stand-in for the Iron Cache REST API, served by an embedded Play server, so
 * the module can be tested offline. Mirrors https://dev.iron.io/cache/reference/api/.
 */
object IronCacheStub {

  // Kept literal below because sird's `$var` matches a single path segment; they must stay in sync.

  val Token     = "secret-token"
  val ProjectId = "proj123"
  val CacheName = "unit-test"

  final class State {
    val items: TrieMap[String, JsValue] = TrieMap.empty
    val expiries: TrieMap[String, Long] = TrieMap.empty
    @volatile var clears: Int           = 0
  }

  /** Runs `block` with an [[IronCacheApi]] wired to a fresh stub server, and returns its result. */
  def withApi[T](timeout: FiniteDuration = 5.seconds)(block: (IronCacheApi, State) => T): T = {
    val state = new State
    Server.withRouterFromComponents()(routes(state)) { implicit port =>
      WsTestClient.withClient { (ws: WSClient) =>
        val config = IronCacheConfig(
          host = s"http://localhost:${port.value}",
          cacheName = CacheName,
          token = Token,
          projectId = ProjectId,
          timeout = timeout
        )
        block(new WsIronCacheApi(ws, config)(scala.concurrent.ExecutionContext.global), state)
      }
    }
  }

  private def routes(state: State)(components: BuiltInComponents): PartialFunction[RequestHeader, Handler] = {
    import play.api.mvc.Results._

    val Action = components.defaultActionBuilder
    val json   = components.playBodyParsers.json

    def authorised(request: RequestHeader): Boolean =
      request.headers.get("Authorization").contains(s"OAuth $Token")

    def item(key: String, value: JsValue): JsObject =
      Json.obj("cache" -> CacheName, "key" -> key, "value" -> value)

    {
      case request if !authorised(request) =>
        Action(Unauthorized(Json.obj("msg" -> "Invalid authentication token.")))

      case GET(p"/1/projects/proj123/caches/unit-test/items/$key") =>
        Action {
          state.items.get(key) match {
            case Some(value) => Ok(item(key, value))
            case None        => NotFound(Json.obj("msg" -> "Key not found."))
          }
        }

      case PUT(p"/1/projects/proj123/caches/unit-test/items/$key") =>
        Action(json) { request =>
          (request.body \ "value").toOption match {
            case Some(value) =>
              state.items.put(key, value)
              (request.body \ "expires_in").asOpt[Long].foreach(state.expiries.put(key, _))
              Ok(Json.obj("msg" -> "Stored."))
            case None => BadRequest(Json.obj("msg" -> "value is required"))
          }
        }

      case DELETE(p"/1/projects/proj123/caches/unit-test/items/$key") =>
        Action {
          if (state.items.remove(key).isDefined) Ok(Json.obj("msg" -> "Deleted."))
          else NotFound(Json.obj("msg" -> "Key not found."))
        }

      case POST(p"/1/projects/proj123/caches/unit-test/items/$key/increment") =>
        Action(json) { request =>
          val amount = (request.body \ "amount").as[Long]
          state.items.get(key) match {
            case Some(JsNumber(current)) =>
              val updated = current + amount
              state.items.put(key, JsNumber(updated))
              Ok(Json.obj("msg" -> "Added", "value" -> updated))
            case Some(_) => BadRequest(Json.obj("msg" -> "Cannot increment a non-numeric value."))
            case None    => NotFound(Json.obj("msg" -> "Key not found."))
          }
        }

      case POST(p"/1/projects/proj123/caches/unit-test/clear") =>
        Action {
          state.items.clear()
          state.clears += 1
          Ok(Json.obj("msg" -> "Cleared."))
        }

      case GET(p"/1/projects/proj123/caches") =>
        Action {
          Ok(Json.arr(Json.obj("project_id" -> ProjectId, "name" -> CacheName)))
        }
    }
  }
}
