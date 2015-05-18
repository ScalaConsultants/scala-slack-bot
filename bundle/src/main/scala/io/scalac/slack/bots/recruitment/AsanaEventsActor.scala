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

trait AsanaReponse extends ResponseObject
case class AsanaTasks(data: Seq[TaskData]) extends AsanaReponse
case class AsanaError(val sync: String, error: String) extends AsanaReponse

class AsanaEventsActor(asanaKey: String, client: AbstractHttpClient, master: ActorRef) extends Actor {

  private case object Ask

  import context.dispatcher

  implicit val asanaReader = new JsonReader[AsanaReponse] {

    def parseTasks(jsObject: JsObject): AsanaTasks = {
      println(s"Read data is ${ jsObject.fields.get("data") } ")
      val tasks = jsObject.fields("data") match {
        case array: JsArray => array.elements.map{
          case item: JsObject =>
            println(s"Single item $item")
            val res = item.getFields("resource").head
            println(s"Resources $res")
            1
        }
      }
      println(s" ___ Parsed tasks are $tasks ")
      AsanaTasks(Nil)
    }

    def parseErrors(jsObject: JsObject): AsanaError = {
      val fields = jsObject.fields
      val sync = fields.getOrElse("sync", JsString("")).convertTo[String]
      val errors = fields.get("errors").get.toString() //TODO
      AsanaError(sync, errors)
    }

    override def read(json: JsValue): AsanaReponse = {
      println(s"Reading ${json.prettyPrint}")
      val obj = json.asJsObject
      val isTask = !obj.fields.contains("errors")
      if(isTask) {
        parseTasks(obj)
      } else {
        parseErrors(obj)
      }
    }
  }

  var token: String = ""
  val recruitmentProject = 12176168760658L
  val defaultHeaders = Map("Authorization" -> ("Basic " + asanaKey))

  override def receive: Receive = {
    case StartObserving(interval) =>
      println(s"Registering AsanaEvents Actor with interval $interval")
      context.system.scheduler.schedule(0 milliseconds, interval milliseconds, self, Ask)

    case Ask =>
      println(s"Asking Asana")
      client.get[AsanaReponse](s"/projects/$recruitmentProject/events", Map("sync" -> token)).map(events => {
        println(s"Received $events")
        events match {
          case AsanaTasks(tasks) => master ! tasks
          case AsanaError(sync, _) => token = sync
        }
      })
  }
}

class AbstractHttpClient(baseUrl: String)(implicit val system: ActorSystem) {

  import system.dispatcher

  val pipeline: HttpRequest => Future[HttpResponse] = (
    addHeader("Authorization", "Basic MlYxSzNydVEuamxIQWh0V09TZzV0dFc3WnA5NHJIelg=")
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
      case cause =>
        println(s"Error $cause")
        throw new Exception("Something went wrong", cause)
    }
  }
}