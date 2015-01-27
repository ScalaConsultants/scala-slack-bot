package io.scalac.slack.actors

import io.scalac.slack.actors.messages.{ApiTest, AuthTest, Ok}
import io.scalac.slack.api.{ApiTestResponse, AuthTestResponse}
import io.scalac.slack.exceptions.{ApiTestError, SlackError}
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
    case ApiTest(param, error) =>
      log.debug("api.test requested")
      val params = Map("param" -> param, "error" -> error).collect { case (key, Some(value)) => key -> value}
      val uri = Uri(url("api.test")).withQuery(params)

      val futureResponse = request(Get(uri))
        val send = sender()
      futureResponse onSuccess {
        case result =>
          val res = result.parseJson.convertTo[ApiTestResponse]
          if (res.ok)
            send ! Ok(res.args)
          else

            send ! ApiTestError

      }

    case AuthTest(token) =>
      log.debug("auth.test requested")
      val uri = Uri(url("auth.test")).withQuery("token" -> token.key + "ld")

      val futureResponse = request(Get(uri))

      futureResponse onComplete {
        case Success(response) =>
          log.debug(response)
          val res = response.parseJson.convertTo[AuthTestResponse]
          if (res.ok)
            sender ! res
          else
            sender ! SlackError(res.error.get)

        case Failure(err) =>
          log.error(err, "auth.test error")
          sender ! SlackError("AuthTestError")
      }
    case theRest =>
      println("DUPA ZIMNA: " + theRest)

  }
}
