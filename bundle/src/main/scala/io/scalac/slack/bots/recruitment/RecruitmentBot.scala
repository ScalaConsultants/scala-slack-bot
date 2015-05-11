package io.scalac.slack.bots.recruitment

import akka.actor.Actor.Receive
import akka.actor.Props
import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.OutboundMessage

/**
 * Maintainer: Patryk
 */
class RecruitmentBot(asanaKey: String, httpClient: AbstractHttpClient, override val bus: MessageEventBus) extends AbstractBot {

  val asana = context.actorOf(
    Props(
      classOf[AsanaEventsActor],
      asanaKey, httpClient, self
    ), "asana-client"
  )

  asana ! StartObserving(1000)

  val channel = "" //TODO: find recruitment channel id

  def findReviewer(task: TaskData): OutboundMessage = {
    val chosenOne = findClosest(task)
    OutboundMessage(channel, s"Hello ${chosenOne.name} please review our candidate ${task.name}. You can find all the details under ${task.url}. Thanks")
  }

  override def act: Receive = {
    case Events(e) =>
      println(s"Master received $e")
      println(s"Mapped is ${e.map( findReviewer )}")
      e.map( findReviewer ).map( publish(_) )
  }

  override def help(channel: String): OutboundMessage =
    OutboundMessage(channel, s"Round Robin for Asana Recruitment tasks")

  case class Scalac(
    name: String,
    level: Double,
    domain: Double) extends Measurable
  val people = List(
    Scalac("Jan", 3, 1),
    Scalac("Piotr", 3, 0),
    Scalac("Marek T", 3, 3),
    Scalac("Note", 2, 1)
  )

  def findClosest(task: TaskData): Scalac = {
    val withDistnace = people.map(sc => {
      val distance = Math.abs(sc.domain - task.domain) + Math.abs(sc.level - task.level)
      (sc, distance)
    })
    val smallestKey = withDistnace.sortBy(_._2).head._2
    val shuffled = util.Random.shuffle( withDistnace.groupBy(_._2)(smallestKey) )
    shuffled.head._1
  }
}
