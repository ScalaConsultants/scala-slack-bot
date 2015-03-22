package io.scalac.slack

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import io.scalac.slack.models.{Active, SlackUser}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created on 22.03.15 13:44
 */
class UsersStorageTest(_system: ActorSystem) extends TestKit(_system) with DefaultTimeout with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("UsersStorageTestActorSystem"))

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "UserStorage" must {
    "save incoming users" in {
      val mario = SlackUser("1234", "mario", deleted = false, Some(false), None, None, None, None, None, None, Active)
      val stefek = SlackUser("12345", "stefek", deleted = false, Some(false), None, None, None, None, None, None, Active)
      val us = system.actorOf(Props[UsersStorage])

      within(2 seconds) {
        us ! RegisterUsers(mario)
        expectMsg("ok")
      }

    }

    "find user from storage" in {
      val mario = SlackUser("1234", "mario", deleted = false, Some(false), None, None, None, None, None, None, Active)
      val stefek = SlackUser("12345", "stefek", deleted = false, Some(false), None, None, None, None, None, None, Active)
      val us = system.actorOf(Props[UsersStorage])

      within(1 second){
        us ! RegisterUsers(mario, stefek)
        expectMsg("ok")
        us ! FindUser("mario")
        expectMsg(Some(UserInfo("1234", "mario", Active)))
        us ! FindUser("12345")
        expectMsg(Some(UserInfo("12345", "stefek", Active)))
        us ! FindUser("gitara")
        expectMsg(None)

      }
    }
  }
}
