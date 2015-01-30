package io.scalac.slack.api

import io.scalac.slack.api.ResponseObject._
import io.scalac.slack.{ApiTestError, SlackError}
import spray.http.Uri
import spray.httpx.RequestBuilding._
import spray.json._

import scala.util.{Success, Failure}

/**
 * Created on 21.01.15 20:32
 */
class ApiActor extends ClientActor {

  import context.dispatcher
  import io.scalac.slack.api.Unmarshallers._
  import ApiClient._

  override def receive = {
    case ApiTest(param, error) =>
      log.debug("api.test requested")
      val params = Map("param" -> param, "error" -> error).collect { case (key, Some(value)) => key -> value}
      val url = Uri(apiUrl("api.test")).withQuery(params)

      val send = sender()

      ApiClient.request[ApiTestResponse](Get(url)) onComplete {
        case Success(res) =>
          if (res.ok)
            send ! Ok(res.args)
          else
            send ! ApiTestError
        case Failure(e) =>
          send ! e

      }

    case AuthTest(token) =>
      log.debug("auth.test requested")
      val uri = Uri(url("auth.test")).withQuery("token" -> token.key)

      val futureResponse = request(Get(uri))
      val send = sender()
      futureResponse onSuccess {
        case response =>
          val res = response.parseJson.convertTo[AuthTestResponse]
          if (res.ok)
            send ! AuthData(res)
          else
            send ! SlackError(res.error.get)
      }
    case RtmStart(token) =>
      log.debug("rtm.start requested")
      val uri = Uri(url("rtm.start")).withQuery("token" -> token.key)
      val futureResponse = request(Get(uri))

      val send = sender()

      futureResponse onSuccess {
        case response =>
          val res = response.parseJson.convertTo[RtmStartResponse]
          if(res.ok)
            send ! RtmData(res.url)

      }
  }
}
