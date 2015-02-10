package io.scalac.slack.common

import spray.json.DefaultJsonProtocol

/**
 * Created on 10.02.15 17:33
 */
object MessageJsonProtocol extends DefaultJsonProtocol {

  implicit val messageTypeFormat = jsonFormat(MessageType, "type", "subtype")

}
