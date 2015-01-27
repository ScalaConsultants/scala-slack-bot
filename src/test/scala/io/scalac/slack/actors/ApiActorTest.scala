package io.scalac.slack.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import io.scalac.slack.actors.messages.{ApiTest, Ok}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created on 24.01.15 12:45
 */
class ApiActorTest extends TestKit(ActorSystem("ApiActorSystem")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }


  "An ApiActor" when {
    "call 'api.test' " should {
      "response OK " in {
        val api = TestActorRef[ApiActor]

        api ! ApiTest(None, None)
        expectMsg(Ok(None))

      }
    }

  }
}
