package io.scalac.slack.api.methods

import spray.http.Uri
import spray.httpx.RequestBuilding._

/**
 * Created on 21.01.15 12:36
 */
object Api extends MethodsBase {


  def test(name: Option[String] = None, error: Option[String] = None) = {

    Get(Uri(url("api.test")).withQuery(Map("name" -> name, "error" -> error).collect { case (key, Some(value)) => key -> value}))

  }


}
