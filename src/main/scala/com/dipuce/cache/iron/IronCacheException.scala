package com.dipuce.cache.iron

/** Raised when Iron.io answers with a status the module cannot recover from. */
final case class IronCacheException(status: Int, message: String)
    extends RuntimeException(s"Iron Cache request failed with HTTP $status: $message")
