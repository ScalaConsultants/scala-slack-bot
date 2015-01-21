package io.scalac.slack

import akka.actor.ActorSystem
import spray.http.{HttpRequest, HttpResponse}
import spray.client.pipelining._
import scala.concurrent.Future

/**
 * Created on 21.01.15 11:49
 */
class WebClient(implicit system: ActorSystem) {

  import system.dispatcher

  //function from HttpRequest to HttpResponse
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  def request(request: HttpRequest) : Future[String] = {
    val futureResponse = pipeline(request)
    futureResponse.map(_.entity.asString)
  }

}

