package com.dipuce.cache.iron.api

/**
 * Maps the different `APIResponse`s to the appropriate HTTP
 * status codes, according to Iron.io's documentation.
 *
 * @author Mike Garrett
 * @since 03/18/2016
 * @version 2.0.0
 * Dipuce, LLC
 */
object APIResponseProvider {

  def getResponse(statusCode: Int): APIResponse = {
    statusCode match {
      case 200 | 201 => SuccessfulResponse()
      case 405 | 406 | 503 | 404 => UnsuccessfulResponse()
      case _ => FatalResponse()
    }
  }

}
