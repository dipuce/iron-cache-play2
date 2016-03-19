package com.dipuce.cache.iron.api

/**
 * Created by Mike on 3/18/2016.
 */
case class FatalResponse() extends APIResponse {
  override def isError: Boolean = true
  override def isRecoverable: Boolean = false
}
