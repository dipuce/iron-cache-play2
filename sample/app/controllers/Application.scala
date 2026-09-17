package controllers

import javax.inject.{ Inject, Singleton }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import com.dipuce.cache.iron.IronCacheApi
import play.api.mvc._

/**
 * Exercises both Play's standard cache API (`set`, `get`, `remove`) and the Iron.io extras
 * (`increment`, `clear`) exposed by [[IronCacheApi]].
 */
@Singleton
class Application @Inject() (cache: IronCacheApi, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def index: Action[AnyContent] = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def cacheSetExpiration(value: String): Action[AnyContent] = Action.async {
    cache.set("test", value, 1.hour).map(_ => Ok(s"$value set."))
  }

  def cacheSetString(key: String, value: String): Action[AnyContent] = Action.async {
    cache.set(key, value).map(_ => Ok(s"Set $key to $value"))
  }

  def cacheSetInt(key: String, value: Int): Action[AnyContent] = Action.async {
    cache.set(key, value).map(_ => Ok(s"Set $key to $value"))
  }

  def cacheGet: Action[AnyContent] = Action.async {
    cache.get[String]("test").map {
      case Some(value) => Ok(s"Cache value found: $value")
      case None        => Ok("Cache value not found")
    }
  }

  def cacheIncrement(key: String, incVal: Long): Action[AnyContent] = Action.async {
    cache.increment(key, incVal).map {
      case Some(amount) => Ok(s"Cache value increased by $incVal to $amount")
      case None         => Ok(s"Key $key not found or not numeric.")
    }
  }

  def clearCache: Action[AnyContent] = Action.async {
    cache.clear().map(_ => Ok("Cache has been cleared."))
  }

  def cacheDelete: Action[AnyContent] = Action.async {
    cache.remove("test").map(_ => Ok("Removed."))
  }
}
