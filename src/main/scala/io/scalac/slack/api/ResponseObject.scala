package io.scalac.slack.api

import spray.json.DefaultJsonProtocol

/**
 * Created on 21.01.15 12:28
 */
sealed trait ResponseObject

case class ApiTestResponse(ok: Boolean, error: Option[String], args: Option[Map[String, String]]) extends ResponseObject


object Unmarshallers extends DefaultJsonProtocol {
  implicit val ApiTestResponseFormat = jsonFormat3(ApiTestResponse)
}