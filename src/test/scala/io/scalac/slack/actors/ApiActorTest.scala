package io.scalac.slack.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import io.scalac.slack.actors.messages.{ApiTest, AuthData, AuthTest, Ok}
import io.scalac.slack.api.APIKey
import io.scalac.slack.errors.{AccountInactive, ApiTestError, InvalidAuth, NotAuthenticated}
import io.scalac.slack.mock.{Api, Auth, MockSlackData}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created on 24.01.15 12:45
 */
class ApiActorTest extends TestKit(ActorSystem("ApiActorSystem")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with MockSlackData {

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

    "call auth.test" should {
      val authTest = AuthTest(APIKey("FAKETOKEN"))

      "response OK" in {
        val api = TestActorRef(Props(new ApiActor with Auth.Test.Successful))
        api ! authTest
        expectMsg(AuthData(url, team, username, teamId, userId))
      }

      "response AccountInactive when token's owner is deleted" in {
        val api = TestActorRef(Props(new ApiActor with Auth.Test.FailedKeyInactive))
        api ! authTest
        expectMsg(AccountInactive)
      }
      "response InvalidAuth when if token is wrong" in {
        val api = TestActorRef(Props(new ApiActor with Auth.Test.FailedWrongKey))
        api ! authTest
        expectMsg(InvalidAuth)
      }
      "response NotAuthenticated when no token provided" in {
        val api = TestActorRef(Props(new ApiActor with Auth.Test.FailedNoKey))
        api ! authTest
        expectMsg(NotAuthenticated)
      }

    }
  }
}
