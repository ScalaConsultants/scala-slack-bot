package io.scalac.slack.api

import akka.actor.ActorSystem
import akka.event.Logging
import io.scalac.slack.Config
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import spray.json._

import scala.concurrent.Future

/**
 * Created on 29.01.15 22:43
 */
object ApiClient {

  val log = Logging

  implicit val system = ActorSystem("SlackApiClient")

  import io.scalac.slack.api.ApiClient.system.dispatcher

  //function from HttpRequest to HttpResponse
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  def request[T <: ResponseObject](request: HttpRequest)(implicit reader: JsonReader[T]): Future[T] = {
    val futureResponse = pipeline(request).map(_.entity.asString)

    //    var result: Either[T, SlackError] = Right(UnspecifiedError(""))

    for {
      responseJson <- futureResponse
      response = JsonParser(responseJson).convertTo[T]
    } yield response

  }

  def apiUrl(endpoint: String) = Config.baseUrl(endpoint)
}
