package com.dipuce.cache.iron.api

/**
 * Created by Mike on 3/18/2016.
 */
abstract class APIResponse {
   def isError: Boolean
   def isRecoverable: Boolean
}
