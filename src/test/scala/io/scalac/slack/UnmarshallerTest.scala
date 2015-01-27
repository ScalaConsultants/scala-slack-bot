package io.scalac.slack

import io.scalac.slack.api.{AuthTestResponse, ApiTestResponse}
import io.scalac.slack.mock.MockSlackData
import org.scalatest.{Matchers, FunSuite}
import spray.json._

import scala.concurrent.Future

/**
 * Created on 27.01.15 22:39
 */
class UnmarshallerTest extends FunSuite with Matchers with MockSlackData{
  import io.scalac.slack.api.Unmarshallers._

  test("api.test empty response"){
    val response = /*language=JSON*/ """{"ok":true}"""

    val apiTestResponse = response.parseJson.convertTo[ApiTestResponse]

    apiTestResponse shouldBe 'ok
    apiTestResponse.args should be (None)
    apiTestResponse.error should be (None)

  }

  test("api.test with param"){
    val response = /*language=JSON*/ """{"ok":true,"args":{"name":"mario"}}"""
    val apiTestResponse = response.parseJson.convertTo[ApiTestResponse]
    apiTestResponse shouldBe 'ok
    apiTestResponse.args should be (Some(Map("name"-> "mario")))
    apiTestResponse.error should be (None)

  }
  test("api.test with error"){
    val response = """{"ok":false,"error":"auth_error","args":{"error":"auth_error"}}"""

    val apiTestResponse = response.parseJson.convertTo[ApiTestResponse]

    apiTestResponse should not be 'ok
    apiTestResponse.args should be (Some(Map("error"-> "auth_error")))
    apiTestResponse.error should be (Some("auth_error"))

  }

  test("api.test with error and param"){
    val response = """{"ok":false,"error":"auth_error","args":{"error":"auth_error","name":"mario"}}"""
    val apiTestResponse = response.parseJson.convertTo[ApiTestResponse]

    apiTestResponse should not be 'ok
    apiTestResponse.args should be (Some(Map("error"-> "auth_error", "name"-> "mario")))
    apiTestResponse.error should be (Some("auth_error"))

  }

  test("auth.test successful"){
    val response = s"""{"ok":true,"url":"$url","team":"$team","user":"$username","team_id":"$teamId","user_id":"$userId"}"""
    val authTestResponse = response.parseJson.convertTo[AuthTestResponse]

    authTestResponse shouldBe 'ok

    authTestResponse.error should be (None)
    authTestResponse.url should equal (Some(url))
    authTestResponse.team should equal (Some(team))
    authTestResponse.user should equal (Some(username))
    authTestResponse.user_id should equal (Some(userId))
    authTestResponse.team_id should equal (Some(teamId))
  }

  test("auth.test failed"){
    val response = """{"ok":false,"error":"not_authed"}"""

    val authTestResponse = response.parseJson.convertTo[AuthTestResponse]

    authTestResponse should not be 'ok
    authTestResponse.error should be (Some("not_authed"))
    authTestResponse.url should be (None)
    authTestResponse.team should equal (None)
    authTestResponse.user should equal (None)
    authTestResponse.user_id should equal (None)
    authTestResponse.team_id should equal (None)
  }
}
