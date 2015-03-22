package io.scalac.slack

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.api.Ok
import io.scalac.slack.models.{Presence, SlackUser}

/**
 * Created on 21.03.15 23:13
 */
class UsersStorage extends Actor with ActorLogging {

  var userCatalog = List.empty[UserInfo]

  implicit def convertUsers(su: SlackUser): UserInfo = UserInfo(su.id.trim, su.name.trim, su.presence)

  override def receive: Receive = {
    case RegisterUsers(users@_*) =>
      users.filterNot(u => u.deleted).foreach(addUser(_))
      sender ! Ok
    case FindUser(matcher) => sender ! userCatalog.find { u =>
      val m = matcher.trim
      m == u.id || m == u.name
    }
  }

  def addUser(user: UserInfo) = {
    userCatalog = user :: userCatalog.filterNot(p => p.id == user.id)
  }
}

case class UserInfo(id: String, name: String, presence: Presence){
  def userLink() = s"""<@$id|name>"""
}

case class RegisterUsers(slackUsers: SlackUser*)

case class FindUser(key: String)

