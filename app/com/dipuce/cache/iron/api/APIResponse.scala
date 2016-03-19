package com.dipuce.cache.iron.api

/**
 * Any response retrieved from Iron.io will be categorized as an
 * API Response.
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
abstract class APIResponse {
   def isError: Boolean
   def isRecoverable: Boolean
}
