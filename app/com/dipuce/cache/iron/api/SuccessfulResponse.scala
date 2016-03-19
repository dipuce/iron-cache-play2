package com.dipuce.cache.iron.api

/**
 * A normal response from the server
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
case class SuccessfulResponse() extends APIResponse {
  override def isError: Boolean = false
  override def isRecoverable: Boolean = true
}