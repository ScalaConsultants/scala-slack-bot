package io.scalac.slack.actors

import io.scalac.slack.actors.messages.ApiTest
import io.scalac.slack.api.{OK, ApiTestResponse, Unmarshallers}
import spray.http.Uri
import spray.httpx.RequestBuilding._
import spray.json._

import scala.util.{Success, Failure}

/**
 * Created on 21.01.15 20:32
 */
class ApiActor extends ClientActor {
  import context.dispatcher
  import DefaultJsonProtocol._
  import Unmarshallers._

  override def receive: Receive = {
    case at: ApiTest =>
      log.debug("Api test requested")
      val params = Map("param" -> at.param, "error" -> at.error).collect { case (key, Some(value)) => key -> value}
      val uri = Uri(url("api.test")).withQuery(params)

      val futureResponse = request(Get(uri))

      futureResponse onComplete {
        case Success(response) => println(response)
          val res = response.parseJson.convertTo[ApiTestResponse]
          if(res.ok)
            sender ! OK
          else
            sender ! new UnknownError("ApiTestError")

        case Failure(err) => println("An error has occured: " + err.getMessage)
      }
  }
}
