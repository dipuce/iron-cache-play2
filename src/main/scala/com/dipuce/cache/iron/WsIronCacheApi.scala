package com.dipuce.cache.iron

import javax.inject.{ Inject, Singleton }

import scala.concurrent.duration.Duration
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

import org.apache.pekko.Done
import play.api.Logging
import play.api.libs.json._
import play.api.libs.ws.{ WSClient, WSRequest, WSResponse }
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue

/**
 * [[IronCacheApi]] implemented over Play's `WSClient`.
 *
 * Iron Cache stores JSON, so cached values must be JSON-representable: `String`, `Boolean`,
 * any numeric primitive, `BigDecimal`, or a `JsValue`. Other values fail the returned future
 * with an `IllegalArgumentException`.
 */
@Singleton
class WsIronCacheApi @Inject() (ws: WSClient, config: IronCacheConfig)(implicit ec: ExecutionContext)
    extends IronCacheApi
    with Logging {

  import WsIronCacheApi._

  private val endpoints = config.endpoints

  // --- AsyncCacheApi -------------------------------------------------------------------------

  override def set(key: String, value: Any, expiration: Duration): Future[Done] =
    toJson(value) match {
      case Left(err) => Future.failed(new IllegalArgumentException(err))
      case Right(json) =>
        val expires =
          if (expiration.isFinite) Seq("expires_in" -> JsNumber(math.max(1L, expiration.toSeconds)))
          else Seq.empty
        val body = JsObject(("value" -> json) +: expires)
        request(endpoints.item(key)).put(body).flatMap(done(_))
    }

  override def get[T: ClassTag](key: String): Future[Option[T]] =
    request(endpoints.item(key)).get().flatMap { response =>
      response.status match {
        case 200 =>
          val stored = (response.json \ "value").toOption.getOrElse(JsNull)
          Future.successful(fromJson[T](stored))
        case 404 => Future.successful(None)
        case _   => failed(response)
      }
    }

  override def getOrElseUpdate[A: ClassTag](key: String, expiration: Duration)(orElse: => Future[A]): Future[A] =
    get[A](key).flatMap {
      case Some(value) => Future.successful(value)
      case None        => orElse.flatMap(value => set(key, value, expiration).map(_ => value))
    }

  override def remove(key: String): Future[Done] =
    request(endpoints.item(key)).delete().flatMap { response =>
      // Deleting a missing key is not an error for a cache.
      if (response.status == 404) Future.successful(Done) else done(response)
    }

  override def removeAll(): Future[Done] =
    request(endpoints.clear).post(EmptyBody).flatMap(done(_))

  // --- Iron extras ---------------------------------------------------------------------------

  override def increment(key: String, amount: Long): Future[Option[Long]] =
    request(endpoints.increment(key)).post(Json.obj("amount" -> amount)).flatMap { response =>
      response.status match {
        case 200 | 201 => Future.successful((response.json \ "value").asOpt[Long])
        case 404       => Future.successful(None)
        case _         => failed(response)
      }
    }

  override def listCaches(page: Int): Future[Map[String, String]] =
    request(endpoints.listCaches).withQueryStringParameters("page" -> page.toString).get().flatMap { response =>
      response.status match {
        case 200 =>
          val caches = response.json.asOpt[Seq[JsObject]].getOrElse(Seq.empty)
          Future.successful(caches.flatMap { c =>
            for {
              name <- (c \ "name").asOpt[String]
              id   <- (c \ "project_id").asOpt[String]
            } yield name -> id
          }.toMap)
        case _ => failed(response)
      }
    }

  // --- helpers -------------------------------------------------------------------------------

  private def request(url: String): WSRequest =
    ws.url(url)
      .withHttpHeaders("Authorization" -> s"OAuth ${config.token}", "Content-Type" -> "application/json")
      .withRequestTimeout(config.timeout)

  private def done(response: WSResponse): Future[Done] =
    if (response.status == 200 || response.status == 201) Future.successful(Done) else failed(response)

  private def failed[T](response: WSResponse): Future[T] = {
    val msg = errorMessage(response)
    logger.warn(s"Iron Cache ${response.status} for ${response.uri}: $msg")
    Future.failed(IronCacheException(response.status, msg))
  }

  private def errorMessage(response: WSResponse): String =
    scala.util.Try((response.json \ "msg").asOpt[String]).toOption.flatten.getOrElse(response.statusText)
}

object WsIronCacheApi {

  private val EmptyBody: JsValue = Json.obj()

  private[iron] def toJson(value: Any): Either[String, JsValue] = value match {
    case s: String     => Right(JsString(s))
    case b: Boolean    => Right(JsBoolean(b))
    case i: Int        => Right(JsNumber(BigDecimal(i)))
    case l: Long       => Right(JsNumber(BigDecimal(l)))
    case s: Short      => Right(JsNumber(BigDecimal(s.toInt)))
    case b: Byte       => Right(JsNumber(BigDecimal(b.toInt)))
    case d: Double     => Right(JsNumber(BigDecimal(d)))
    case f: Float      => Right(JsNumber(BigDecimal(f.toDouble)))
    case d: BigDecimal => Right(JsNumber(d))
    case j: JsValue    => Right(j)
    case null          => Right(JsNull)
    case other =>
      Left(s"Iron Cache can only store JSON-representable values, not ${other.getClass.getName}")
  }

  private[iron] def fromJson[T](json: JsValue)(implicit ct: ClassTag[T]): Option[T] = {
    val cls = ct.runtimeClass
    def num: Option[BigDecimal] = json.asOpt[BigDecimal]
    val converted: Option[Any] =
      if (cls == classOf[JsValue]) Some(json)
      else if (classOf[JsValue].isAssignableFrom(cls)) Option(json).filter(cls.isInstance)
      else if (cls == classOf[String]) json match {
        case JsString(s) => Some(s)
        case JsNull      => None
        case other       => Some(Json.stringify(other))
      }
      else if (cls == classOf[Boolean] || cls == classOf[java.lang.Boolean]) json.asOpt[Boolean]
      else if (cls == classOf[Int] || cls == classOf[java.lang.Integer]) num.filter(_.isValidInt).map(_.toInt)
      else if (cls == classOf[Long] || cls == classOf[java.lang.Long]) num.filter(_.isValidLong).map(_.toLong)
      else if (cls == classOf[Short] || cls == classOf[java.lang.Short]) num.filter(_.isValidShort).map(_.toShort)
      else if (cls == classOf[Byte] || cls == classOf[java.lang.Byte]) num.filter(_.isValidByte).map(_.toByte)
      else if (cls == classOf[Double] || cls == classOf[java.lang.Double]) num.map(_.toDouble)
      else if (cls == classOf[Float] || cls == classOf[java.lang.Float]) num.map(_.toFloat)
      else if (cls == classOf[BigDecimal]) num
      else if (cls == classOf[Any] || cls == classOf[AnyRef] || cls == classOf[java.lang.Object]) json match {
        case JsString(s)  => Some(s)
        case JsBoolean(b) => Some(b)
        case JsNumber(n) if n.isValidInt  => Some(n.toInt)
        case JsNumber(n) if n.isValidLong => Some(n.toLong)
        case JsNumber(n)                  => Some(n)
        case JsNull                       => None
        case other                        => Some(other)
      }
      else None
    converted.map(_.asInstanceOf[T])
  }
}
