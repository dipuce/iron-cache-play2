package com.dipuce.cache.iron.api

/**
 * Created by Mike on 3/18/2016.
 */
object APIResponseProvider {

  def getResponse(statusCode: Int): APIResponse = {
    statusCode match {
      case 200 | 201 => SuccessfulResponse
      case 405 | 406 | 503 => UnsuccessfulResponse
      case _ => FatalResponse
    }
  }

}
