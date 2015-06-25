package io.scalac.slack

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.models.{Presence, SlackUser}

/**
 * Maintainer: @marioosh
 */
class UsersStorage extends Actor with ActorLogging {

  var userCatalog = List.empty[UserInfo]

  implicit def convertUsers(su: SlackUser): UserInfo = UserInfo(su.id, su.name, su.presence)

  override def receive: Receive = {
    case RegisterUsers(users @ _*) =>
      users.filterNot(u=> u.deleted).foreach(addUser(_))

    case FindUser(key) => sender ! userCatalog.find { user =>
      val matcher = key.trim.toLowerCase
      matcher == user.id.trim.toLowerCase || matcher == user.name.trim.toLowerCase
    }

  }

  def addUser(user: UserInfo) = {
    userCatalog = user :: userCatalog.filterNot(p => p.id == user.id)
    log.info("USER CATALOG SIZE: " + userCatalog.size)
  }
}

case class UserInfo(id: String, name: String, presence: Presence) {
  def userLink() = s"""<@$id|name>"""
}


case class RegisterUsers(slackUsers: SlackUser*)

case class FindUser(key: String)