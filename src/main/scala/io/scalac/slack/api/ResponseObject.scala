package io.scalac.slack.api

import spray.json.DefaultJsonProtocol

/**
 * Created on 21.01.15 12:28
 */
sealed trait ResponseObject

case class ApiTestResponse(ok: Boolean, error: Option[String], args: Option[Map[String, String]]) extends ResponseObject

case class AuthTestResponse(ok: Boolean, error: Option[String], url: Option[String], team: Option[String], user: Option[String], teamId: Option[String], userId: Option[String])

object Unmarshallers extends DefaultJsonProtocol {
  implicit val ApiTestResponseFormat = jsonFormat3(ApiTestResponse)
  implicit val AuthTestResponseFormat = jsonFormat7(AuthTestResponse)
}