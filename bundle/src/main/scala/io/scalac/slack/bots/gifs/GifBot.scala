package io.scalac.slack.bots.gifs

import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common._
import io.scalac.slack.{MessageEventBus, SlackBot}

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.util.Random

class GifsBot(gifsRepo: GifsRepository) extends AbstractBot {

  override def help(channel: String): OutboundMessage = OutboundMessage(channel, s"*$name* is used for storing useful gifs \\n " +
    s"Add image to repository for your future uses \\n" +
    s"`gif store {url} {tags}` command store image's url with tags \\n" +
    s"`gif {tag}` displays image from stored url \\n" +
    s"`gif list` displays all available tags \\n"

  )

  override def act: Receive = {

    case Command("gif", "list" :: params, message) =>
      val tags = gifsRepo.tagList
      publish(OutboundMessage(message.channel, "Images stored under tags: " + tags.mkString(", ")))
    case Command("gif", "store" :: params, message) =>
      val gifUrl = params.head
      params.tail.distinct.foreach { tag =>
        println(s"GIF TAG ADDED: url[$gifUrl], tag:[$tag]")
        gifsRepo.insert(tag, gifUrl)
        publish(OutboundMessage(message.channel, s"Image stored under tag: [$tag]"))
      }
    case Command("gif", "help" :: params, message) =>
      publish(help(message.channel))

    case Command(command, tag :: params, message) if command.matches("gif|show") =>
      val gifList = gifsRepo.find(tag)
      if (gifList.nonEmpty) {
        val tagged = gifList(Random.nextInt(gifList.size))
        publish(
          RichOutboundMessage(message.channel, List(Attachment(ImageUrl(tagged), Text(tag))))
        )
      }
  }

  override val bus: MessageEventBus = SlackBot.eventBus
}

class GifsRepository() extends AbstractRepository {
  override val bucket: String = "GifsBot"

  private class GifTag(tag: Tag) extends Table[(Long, String, String)](tag, s"${bucket}_GifsTag") {
    def id = column[Long]("DataTagId", O.PrimaryKey, O.AutoInc)

    def name = column[String]("Name")

    def url = column[String]("Url")

    def * = (id, name, url)
  }

  private val gifTags = TableQuery[GifTag]

  db.withDynSession {
    if (migrationNeeded())
      gifTags.ddl.create
  }

  //public methods
  def insert(gifTag: String, gifUrl: String) = {
    db.withDynSession {
      gifTags.map(gt => (gt.name, gt.url)) +=(gifTag, strip(gifUrl))
    }
  }

  def find(tagName: String) = db.withDynSession(gifTags.filter(_.name === tagName).list.map(c => strip(c._3)))

  def tagList: List[String] = db.withDynSession(gifTags.map(_.name).list.distinct)

  def strip(url: String): String = url.trim.replaceAll("[<>]", "")
}
