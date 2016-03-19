package com.dipuce.cache.iron.api

/**
 * Errors caused by some user input that will not resolve themselves
 * on their own.
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
case class FatalResponse() extends APIResponse {
  override def isError: Boolean = true
  override def isRecoverable: Boolean = false
}
