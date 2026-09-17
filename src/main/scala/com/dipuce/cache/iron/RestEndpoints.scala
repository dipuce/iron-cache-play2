package com.dipuce.cache.iron

/**
 * Iron Cache REST endpoints, per https://dev.iron.io/cache/reference/api/.
 *
 * @param projectAddress `<host>/1/projects/<projectId>`
 * @param cacheName      the cache all operations target
 */
final case class RestEndpoints(projectAddress: String, cacheName: String) {

  /** Lists the caches of the project. */
  val listCaches: String = s"$projectAddress/caches"

  /** Base of every operation on the configured cache. */
  val cache: String = s"$listCaches/$cacheName"

  /** Removes every item from the cache. */
  val clear: String = s"$cache/clear"

  /** Item collection; individual items live at `items/<key>`. */
  val items: String = s"$cache/items"

  def item(key: String): String = s"$items/${RestEndpoints.encode(key)}"

  def increment(key: String): String = s"${item(key)}/increment"
}

object RestEndpoints {
  private[iron] def encode(segment: String): String =
    java.net.URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
}
