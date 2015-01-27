package io.scalac.slack.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import io.scalac.slack.actors.messages.{ApiTest, Ok}
import io.scalac.slack.exceptions.ApiTestError
import io.scalac.slack.mock.Api
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

        val api = TestActorRef(Props(new ApiActor with Api.Test.Empty))

        api ! ApiTest()
        expectMsg(Ok(None))

      }

      "response OK and echo param" in {
        val api = TestActorRef(Props(new ApiActor with Api.Test.WithParam))
        api ! ApiTest()
        expectMsg(Ok(Some(Map("name" -> "mario"))))
      }

      "response error when error was sent" in {
        val api = TestActorRef(Props(new ApiActor with Api.Test.WithError))
        api ! ApiTest()
        expectMsg(ApiTestError)

      }

      "response error when error was sent, params dropped" in {
        val api = TestActorRef(Props(new ApiActor with Api.Test.WithErrorAndParam))
        api ! ApiTest()
        expectMsg(ApiTestError)
      }
    }


  }
}
