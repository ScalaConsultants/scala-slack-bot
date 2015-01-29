package io.scalac.slack.api

import spray.http.HttpRequest

import scala.concurrent.Future

/**
 * Created on 25.01.15 22:22
 */
trait Requester {
  def request(request: HttpRequest): Future[String]
}
