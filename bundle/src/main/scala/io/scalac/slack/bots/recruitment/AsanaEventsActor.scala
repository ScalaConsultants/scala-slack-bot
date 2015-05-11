package io.scalac.slack.bots.recruitment

import akka.actor.{ActorRef, ActorSystem, Actor}
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

trait Measurable {
  val level: Double // 0 - junior, 1 - medior, 3 - senior
  val domain: Double // 0 - mobile, 1 - backend, 3 - frontend
}

case class TaskData(
  name: String,
  url: String,
  level: Double,
  domain: Double
) extends Measurable
case class Events(data: Seq[TaskData]) extends ResponseObject


class AsanaEventsActor(asanaKey: String, client: AbstractHttpClient, master: ActorRef) extends Actor {

  private case object Ask

  val defaultHeaders = Map("Authorization" -> asanaKey)

  import context.dispatcher

  implicit val taskFormat = jsonFormat4(TaskData)
  implicit val eventsFormat = jsonFormat1(Events)

  override def receive: Receive = {
    case StartObserving(interval) =>
      println(s"Registering AsanaEvents Actor with interval $interval")
      context.system.scheduler.schedule(0 milliseconds, interval milliseconds, self, Ask)

    case Ask =>
      println(s"Asking Asana")
      client.get[Events]("/users/me", defaultHeaders).map(events => {
        println(s"Received $events")
        master ! events
      })
  }
}

class AbstractHttpClient(baseUrl: String)(implicit val system: ActorSystem) {

  println(s"    START!!!!!")

  import system.dispatcher

//  val pipeline: HttpRequest => Future[HttpResponse] = (
//    addHeader("Authorization", "MlYxSzNydVEuamxIQWh0V09TZzV0dFc3WnA5NHJIelg=")
//      ~> sendReceive
//  )

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

//    val futureResponse = pipeline(HttpRequest(method, url)).map(_.entity.asString)
//    (for {
//      responseJson <- futureResponse
//      response = {println(s"=== Reposne $responseJson"); JsonParser(responseJson).convertTo[T]}
//    } yield response) recover {
//      case cause => throw new Exception("Something went wrong", cause)
//    }
    
    Future { Events(List(TaskData("John Doe", "Asana url", 3, 0.5))).asInstanceOf[T] }
  }
}