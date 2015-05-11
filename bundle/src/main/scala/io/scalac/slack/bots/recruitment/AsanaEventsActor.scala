package io.scalac.slack.bots.recruitment

import akka.actor.{ActorSystem, Actor}
import akka.actor.Actor.Receive
import io.scalac.slack.api.ResponseObject
import io.scalac.slack.api.Unmarshallers._
import spray.http.{HttpRequest, Uri, HttpMethod, HttpMethods}
import spray.json.{JsonParser, JsonReader}
import scala.concurrent.Future
import scala.concurrent.duration._

import spray.client.pipelining._
import spray.http._
import spray.json._

case class StartObserving(interval: Double) // in seconds

case class Events(data: JsObject) extends ResponseObject


class AsanaEventsActor(asanaKey: String, client: AbstractHttpClient) extends Actor {

  private case object Ask

  val defaultHeaders = Map("Authorization" -> asanaKey)

  import context.dispatcher

  import spray.client.pipelining

  implicit val eventsFormat = jsonFormat1(Events)

  override def receive: Receive = {
    case StartObserving(interval) =>
      println(s"Registering AsanaEvents Actor with interval $interval")
      context.system.scheduler.schedule(0 milliseconds, interval milliseconds, self, Ask)

    case Ask =>
      println(s"Asking Asana")
      val events = client.get[Events]("/users/me", defaultHeaders)
      println(s"Received $events")
  }
}

class AbstractHttpClient(baseUrl: String)(implicit val system: ActorSystem) {

  println(s"    START!!!!!")

  import system.dispatcher

  val pipeline: HttpRequest => Future[HttpResponse] = (
    addHeader("Authorization", "MlYxSzNydVEuamxIQWh0V09TZzV0dFc3WnA5NHJIelg=")
      ~> sendReceive
  )

  def get[T <: ResponseObject](
    endpoint: String,
    params: Map[String, String] = Map.empty[String, String]
  )(implicit reader: JsonReader[T]): Future[T] =
    request[T](HttpMethods.GET, endpoint, params)

  def post[T <: ResponseObject](
    endpoint: String,
    params: Map[String, String] = Map.empty[String, String]
  )(implicit reader: JsonReader[T]): Future[T] =
    request[T](HttpMethods.POST, endpoint, params)

  def request[T <: ResponseObject](
    method: HttpMethod,
    endpoint: String,
    params: Map[String, String] = Map.empty[String,String]
  )(implicit reader: JsonReader[T]): Future[T] = {

    val url = Uri(baseUrl + endpoint).withQuery(params)

    println(s"Querying for $url")

    val futureResponse = pipeline(HttpRequest(method, url)).map(_.entity.asString)
    (for {
      responseJson <- futureResponse
      response = {println(s"=== Reposne $responseJson"); JsonParser(responseJson).convertTo[T]}
    } yield response) recover {
      case cause => throw new Exception("Something went wrong", cause)
    }
  }
}