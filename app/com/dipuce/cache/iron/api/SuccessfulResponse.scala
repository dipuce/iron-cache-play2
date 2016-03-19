package com.dipuce.cache.iron.api

/**
 * Created by Mike on 3/18/2016.
 */
case class SuccessfulResponse() extends APIResponse {
  override def isError: Boolean = false
  override def isRecoverable: Boolean = true
}