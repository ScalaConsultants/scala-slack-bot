package io.scalac.slack

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.models.{Presence, SlackUser}

/**
 * Created on 21.03.15 23:13
 */
class UsersStorage extends Actor with ActorLogging {

  var userCatalog = List.empty[UserInfo]

  implicit def convertUsers(su: SlackUser): UserInfo = UserInfo(su.id, su.name, su.presence)

  override def receive: Receive = {
    case RegisterUsers(users @ _*) => users.filterNot(u=> u.deleted).foreach(addUser(_))
  }

  def addUser(user: UserInfo) = {
    log.info("ADD USER: " + user)
    userCatalog = user :: userCatalog.filterNot(p => p.id == user.id)
    log.info("USER CATALOG SIZE: " + userCatalog.size)
  }
}

case class UserInfo(id: String, name: String, presence: Presence)

case class RegisterUsers(slackUsers: SlackUser*)

