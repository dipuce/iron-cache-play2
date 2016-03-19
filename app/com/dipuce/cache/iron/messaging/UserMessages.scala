package com.dipuce.cache.iron.messaging

/**
 * Created by Mike on 3/18/2016.
 */
trait UserMessages {

  val successApi = "Successfully retrieved response for "

  val notAllFieldsProvided = "Not all config fields provided by user. Falling back to defaults for host name and cache name."


  // Error Msgs

  val cannotClear = "Clear request did not complete as expected."

  val genericCannot = "Request did not complete as expected."

  val cannotContinue = "Cannot continue due to initialization issues. Resolve and try again."

  val fatalMsg = "An unrecoverable error has occurred. Please fix. "

  val myError = "An error has occurred that may be recoverable. Error: "
}
