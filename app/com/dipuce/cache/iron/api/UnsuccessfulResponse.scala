package com.dipuce.cache.iron.api

/**
 * These responses are meant to signify that more action is needed by the library
 * but not by the user.
 *
 */
case class UnsuccessfulResponse() extends APIResponse {
  override def isError: Boolean = true
  override def isRecoverable: Boolean = true
}
