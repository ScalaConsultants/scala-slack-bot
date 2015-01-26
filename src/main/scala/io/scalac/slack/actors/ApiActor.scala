package io.scalac.slack.actors

import io.scalac.slack.actors.messages.{AuthTest, ApiTest, Ok}
import io.scalac.slack.api.{AuthTestResponse, ApiTestResponse, Unmarshallers}
import io.scalac.slack.exceptions.SlackError
import spray.http.Uri
import spray.httpx.RequestBuilding._
import spray.json._

import scala.util.{Failure, Success}

/**
 * Created on 21.01.15 20:32
 */
class ApiActor extends ClientActor {

  import context.dispatcher
  import io.scalac.slack.api.Unmarshallers._

  override def receive: Receive = {
    case at: ApiTest =>
      log.debug("api.test requested")
      val params = Map("param" -> at.param, "error" -> at.error).collect { case (key, Some(value)) => key -> value}
      val uri = Uri(url("api.test")).withQuery(params)

      val futureResponse = request(Get(uri))

      futureResponse onComplete {
        case Success(response) => println(response)
          val res = response.parseJson.convertTo[ApiTestResponse]
          if (res.ok)
            sender ! Ok(res.args.getOrElse(Map.empty[String, String]))
          else

            sender ! new UnknownError("ApiTestError")

        case Failure(err) =>
          log.error(err, "api.test error")
          println("An error has occured: " + err.getMessage)
      }
    case AuthTest(token) =>
      log.debug("auth.test requested")
      val uri = Uri(url("auth.test")).withQuery("token" -> token.key + "ld")

      val futureResponse = request(Get(uri))

      futureResponse onComplete {
        case Success(response) =>
          log.debug(response)
          val res = response.parseJson.convertTo[AuthTestResponse]
          if(res.ok)
            sender ! res
          else
            sender ! SlackError(res.error.get)

        case Failure(err) =>
          log.error(err, "auth.test error")
          sender ! SlackError("AuthTestError")
    }
  }
}
