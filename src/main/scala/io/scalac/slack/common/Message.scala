package io.scalac.slack.common

/**
 * Created on 08.02.15 01:03
 */
sealed trait Message

object Ping extends Message {
  override def toString =

    //format: off
    s"""
       |{
       | id: ${MessageCounter.next},
       | type: "ping",
       | time: ${SlackDateTime.timeStamp}
       |}
     """.stripMargin
  //format: on
}
case class ChannelMessage(id: Int, text: String, channelId: String, time: String) extends Message


