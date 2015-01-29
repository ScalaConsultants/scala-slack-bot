package io.scalac.slack.api

import akka.actor.ActorSystem
import akka.event.Logging
import io.scalac.slack.{SlackError, UnspecifiedError}
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import spray.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created on 29.01.15 22:43
 */
object ApiClient {

  val log = Logging

  implicit val system = ActorSystem("Slack API Client")

  import io.scalac.slack.api.ApiClient.system.dispatcher

  //function from HttpRequest to HttpResponse
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  def request[T <: ResponseObject](request: HttpRequest)(implicit reader: JsonReader[T]): Either[T, SlackError] = {
    val futureResponse = pipeline(request).map(_.entity.asString)

    var result: Either[T, SlackError] = Right(UnspecifiedError(""))

    futureResponse onComplete {
      case Success(responseString) =>
        try {
          val response = JsonParser(responseString).convertTo[T]
          result = Left(response)

        } catch {
          case e: Exception =>
            result = Right(UnspecifiedError("JSON mapping error"))
        }
      case Failure(reason) =>
        result = Right(UnspecifiedError("Response failure"))
    }
    result
  }
}
