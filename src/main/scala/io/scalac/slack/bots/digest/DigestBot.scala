package io.scalac.slack.bots.digest

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{AbstractRepository, Command, OutboundMessage}
import org.joda.time.{DateTimeZone, DateTime}

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

/**
 * Maintainer: Patryk
 */
class DigestBot(linkRepo: DigestRepository, override val bus: MessageEventBus) extends AbstractBot {

  def put(linkToStore: String, user: String) = linkRepo.create(linkToStore, user)
  def get() = linkRepo.read()
  def clear() = linkRepo.archive()

  def act = {
    case Command("digest-put", links, message) =>
      log.debug(s"Got x= digest-put $links from Slack")
      links.foreach(put(_, message.user))
      publish(OutboundMessage(message.channel, s"Link ${links.mkString(" ")} stored for Digest!"))

    case Command("digest-list", _, message) =>
      log.debug(s"Got x= digest-list from Slack")
      publishContent(message.channel)

    case Command("digest-clear", _, message) =>
      log.debug(s"Got x= digest-clear from Slack")
      publishContent(message.channel)
      publish(OutboundMessage(message.channel, s"Now Clearing"))
      clear()
  }
  
  def publishContent(channel: String): Unit = {
    publish(OutboundMessage(channel, s"Digest contained: ${get().mkString(" ")}"))
  }

  override def help(channel: String): OutboundMessage = OutboundMessage(channel, s"*${name}* is a helper for Scalac Digest \\n " +
      s"`tag-put {link}` - adds the link to storage \\n" +
      s"`digest-list` - prints all the un-archived links \\n" +
      s"`tag-clear` - archives all links in the storage ")
}

class DigestRepository() extends AbstractRepository {
  /// definitions
  override val bucket = "DigestBot"

  private class DigestLink(tag: Tag) extends Table[(Long, String, String, Long, Boolean)](tag, s"${bucket}_DigestLink") {
    def id = column[Long]("DigestLinkId", O.PrimaryKey, O.AutoInc)
    def text = column[String]("Text")
    def author = column[String]("Author")
    def added = column[Long]("Added")
    def archived = column[Boolean]("Archived")
    def * = (id, text, author, added, archived)
  }
  private val links = TableQuery[DigestLink]

  db.withDynSession {
    if(migrationNeeded())
      links.ddl.create
  }

  //public methods

  def create(linkToStore: String, user: String) = {
    db.withDynSession{
      val when = new DateTime(DateTimeZone.UTC).getMillis
      links.insert((-1L, linkToStore, user, when, false))
    }
  }

  def read() = {
    db.withDynSession{
      links.filter(_.archived === false).list.map(_._2)
    }
  }

  def archive() = {
    db.withDynSession {
      val when = new DateTime(DateTimeZone.UTC).getMillis - 10000L
      links.filter(_.added < when).map(_.archived).update(true)
    }
  }
}
