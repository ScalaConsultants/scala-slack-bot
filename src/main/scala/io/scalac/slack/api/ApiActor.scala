package io.scalac.slack.api

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.api.ResponseObject._
import io.scalac.slack.{ApiTestError, SlackError}

import scala.util.{Failure, Success}

/**
 * Created on 21.01.15 20:32
 */
class ApiActor extends Actor with ActorLogging {

  import context.dispatcher
  import io.scalac.slack.api.Unmarshallers._

  override def receive = {

    case ApiTest(param, error) =>
      log.debug("api.test requested")
      val send = sender()
      val params = Map("param" -> param, "error" -> error).collect { case (key, Some(value)) => key -> value}

      SlackApiClient.get[ApiTestResponse]("api.test", params) onComplete {
        case Success(res) =>
          if (res.ok) {
            send ! Ok(res.args)
          }
          else {
            send ! ApiTestError
          }
        case Failure(ex) =>
          send ! ex

      }

    case AuthTest(token) =>
      log.debug("auth.test requested")
      val send = sender()

      SlackApiClient.get[AuthTestResponse]("auth.test", Map("token" -> token.key)) onComplete {
        case Success(res) =>

          if (res.ok)
            send ! AuthData(res)
          else
            send ! SlackError(res.error.get)
        case Failure(ex) =>
          send ! ex
      }

    case RtmStart(token) =>
      log.debug("rtm.start requested")
      val send = sender()

      SlackApiClient.get[RtmStartResponse]("rtm.start", Map("token" -> token.key)) onComplete {

        case Success(res) =>
          if (res.ok)
            send ! RtmData(res.url)
            send ! res.self
        case Failure(ex) =>
          send ! ex
      }
  }
}
