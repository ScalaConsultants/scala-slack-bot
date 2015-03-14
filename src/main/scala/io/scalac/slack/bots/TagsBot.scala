package io.scalac.slack.bots

import io.scalac.slack.common._
import org.joda.time.{DateTimeZone, DateTime}
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.meta._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

class TagsBot(tagsRepo: TagsRepository) extends IncomingMessageListener  {

  log.debug(s"Starting $this")

  def receive = {
    case BaseMessage(fullMsg, channel, user, _, _) if fullMsg.contains("[") && fullMsg.contains("]") && !fullMsg.contains("$") =>
      val wordList = fullMsg.split(" ")
      val tags = wordList.filter(x => x.startsWith("[") && x.endsWith("]")).map(_.toLowerCase().replaceAll("\\[", "").replaceAll("\\]", ""))

      log.debug(s"Got x= tag $fullMsg from Slack with tags ${tags.mkString(" ")}")

      tags.map(tagsRepo.insert(_, fullMsg, user))
      publish(OutboundMessage(channel, s"$fullMsg has been tagged with ${tags.mkString(" ")}"))

    case Command("tag", tag :: _, message) =>
      log.debug(s"Got x= tag-list $tag from Slack")
      val data = tagsRepo.find(tag).getOrElse(List())
      publish(OutboundMessage(message.channel, s"$tag contains ${data.mkString(" ; ")}"))
  }
}

class TagsRepository() extends AbstractRepository {

  /// definitions
  override val bucket = "TagsBot"

  private class DataTag(tag: Tag) extends Table[(Long, String)](tag, s"${bucket}_DataTag") {
    def id = column[Long]("DataTagId", O.PrimaryKey, O.AutoInc)
    def name = column[String]("Name")
    def * = (id, name)
  }
  private val dataTags = TableQuery[DataTag]
  private val dataTagsInsert = dataTags.map(_.name).returning(dataTags.map(_.id))

  private class TaggedEntry(tag: Tag) extends Table[(Long, Long, String, String, Long)](tag, s"${bucket}_TaggedEntry") {
    def id = column[Long]("TaggedEntryId", O.PrimaryKey, O.AutoInc)
    def tagId = column[Long]("TagId")
    def text = column[String]("Text")
    def author = column[String]("Author")
    def added = column[Long]("Added")
    def * = (id, tagId, text, author, added)
    // A reified foreign key relation that can be navigated to create a join
    def tagFK = foreignKey("Tag_FK", tagId, dataTags)(_.id)
  }
  private val entries = TableQuery[TaggedEntry]

  db.withDynSession {
    if(migrationNeeded())
      (dataTags.ddl ++ entries.ddl).create
  }

  //public methods

  def getTagByName(_name: String) = dataTags.filter(_.name === _name).map(_.id)

  def insert(tag: String, fullMSg: String, user: String) = {
    db.withDynSession{
      val tagId: Long = getTagByName(tag).firstOption.getOrElse( dataTagsInsert.insert((tag)) )
      val when = new DateTime(DateTimeZone.UTC).getMillis
      entries.insert((-1L, tagId, fullMSg, user, when))
    }
  }

  def find(tag: String) = {
    db.withDynSession{
      getTagByName(tag).firstOption.map(id => {
        entries.filter(_.tagId === id).list.map(_._3)
      })
    }
  }
}
