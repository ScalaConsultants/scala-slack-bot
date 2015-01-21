package io.scalac.slack.actors

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.Config
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}

import scala.concurrent.Future

/**
 * Created on 21.01.15 20:26
 */
trait ClientActor extends Actor with ActorLogging {

  import context.dispatcher

  //function from HttpRequest to HttpResponse
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  def request(request: HttpRequest): Future[String] = {
    val futureResponse = pipeline(request)
    futureResponse.map(_.entity.asString)
  }

  def url(endpoint: String) = Config.baseUrl(endpoint)
}
