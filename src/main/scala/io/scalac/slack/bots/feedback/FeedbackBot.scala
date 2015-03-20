package io.scalac.slack.bots.feedback

import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common.{AbstractRepository, OutboundMessage, Command}
import org.joda.time.{DateTimeZone, DateTime}

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

class FeedbackBot(repo: FeedbackRepository) extends IncomingMessageListener {
  log.debug(s"Starting $this")

  def receive = {
    case Command("improve", "list" :: _, message) =>
      log.debug(s"Got x= feedback-list from Slack")
      publish(OutboundMessage(message.channel, s"Feedback list: \\n ${repo.read().mkString("\\n")}"))

    case Command("improve", text, message) =>
      log.debug(s"Got x= feedback from Slack")
      repo.create(text.mkString(" "), message.user)
      publish(OutboundMessage(message.channel, s"Thank you for your feedback :) Let's make Scalac a better place"))
  }
}

class FeedbackRepository() extends AbstractRepository {
  /// definitions
  override val bucket = "FeedbackBot"

  private class Feedback(tag: Tag) extends Table[(Long, String, Long)](tag, s"${bucket}_Feedback") {
    def id = column[Long]("DigestLinkId", O.PrimaryKey, O.AutoInc)
    def text = column[String]("Text")
    def added = column[Long]("Added")
    def * = (id, text, added)
  }
  private val links = TableQuery[Feedback]

  db.withDynSession {
    if(migrationNeeded())
      links.ddl.create
  }

  //public methods

  def create(linkToStore: String, user: String) = {
    db.withDynSession{
      val when = new DateTime(DateTimeZone.UTC).getMillis
      links.insert((-1L, linkToStore, when))
    }
  }

  def read() = {
    db.withDynSession{
      links.list.map(_._2)
    }
  }
}
