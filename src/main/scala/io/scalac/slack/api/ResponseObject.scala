package io.scalac.slack.api

/**
 * Created on 21.01.15 12:28
 */
sealed trait ResponseObject

object OK extends ResponseObject
case class ApiTestOk(ok: Boolean, error: Option[String], args: Map[String, String]) extends ResponseObject

