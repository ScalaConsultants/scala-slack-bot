package io.scalac.slack.api

import io.scalac.slack.actors.messages.AuthData
import spray.json.DefaultJsonProtocol

/**
 * Created on 21.01.15 12:28
 */
sealed trait ResponseObject

case class ApiTestResponse(ok: Boolean, error: Option[String], args: Option[Map[String, String]]) extends ResponseObject

case class AuthTestResponse(ok: Boolean, error: Option[String], url: Option[String], team: Option[String], user: Option[String], team_id: Option[String], user_id: Option[String])

object Unmarshallers extends DefaultJsonProtocol {
  implicit val ApiTestResponseFormat = jsonFormat3(ApiTestResponse)
  implicit val AuthTestResponseFormat = jsonFormat7(AuthTestResponse)
}

object ResponseObject {
  implicit def authTestResponseToAuthData(atr: AuthTestResponse): AuthData =
    AuthData(atr.url.getOrElse("url"), atr.team.getOrElse("team"), atr.user.getOrElse("user"), atr.team_id.getOrElse("teamID"), atr.user_id.getOrElse("userID"))
}