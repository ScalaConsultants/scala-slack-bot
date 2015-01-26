package io.scalac.slack.mock

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSuite, Matchers}
import spray.http.HttpRequest
import spray.httpx.RequestBuilding._

import scala.concurrent.Future
import scala.util.Success

/**
 * Created on 25.01.15 23:15
 */
class MockRequesterTest extends FunSuite with Matchers with ScalaFutures with MockSlackData{

  def request: HttpRequest = Get("/")

  test("Api.Test.Empty") {
    val req = new MockRequester with Api.Test.Empty
    val result = req.request(request)

    whenReady(result) { res =>
      res should equal(/*language=JSON*/ """{"ok":true}""")
    }
  }

  test("Api.Test.WithParam") {
    val req = new MockRequester with Api.Test.WithParam
    val result = req.request(request)

    whenReady(result) { res =>
      res should equal(/*language=JSON*/ """{"ok":true,"args":{"name":"mario"}}""")
    }
  }

  test("Api.Test.WithError") {
    val req = new MockRequester with Api.Test.WithError
    val result = req.request(request)

    whenReady(result) { res =>
      res should equal(/*language=JSON*/ """{"ok":false,"error":"auth_error","args":{"error":"auth_error"}}""")
    }
  }


  test("Api.Test.WithErrorAndParam") {
    val req = new MockRequester with Api.Test.WithErrorAndParam
    val result = req.request(request)

    whenReady(result) { res =>
      res should equal(/*language=JSON*/ """{"ok":false,"error":"auth_error","args":{"error":"auth_error","name":"mario"}}""")
    }
  }

  test("Auth.Test.Successful") {
    val req = new MockRequester with Auth.Test.Successful
    val result = req.request(request)

    whenReady(result) { res =>
      res should equal(/*language=JSON*/ s"""{"ok":true,"url":"$url","team":"$team","user":"$username","team_id":"$teamId","user_id":"$userId"}""")
    }
  }

  test("Auth.Test.FailedNoKey") {
    val req = new MockRequester with Auth.Test.FailedNoKey
    val result = req.request(request)

    whenReady(result) { res =>
      res should equal(/*language=JSON*/ """{"ok":false,"error":"not_authed"}""")
    }
  }

  test("Auth.Test.FailedKeyInactive") {
    val req = new MockRequester with Auth.Test.FailedKeyInactive
    val result = req.request(request)

    whenReady(result) { res =>
      res should equal(/*language=JSON*/ """{"ok":false,"error":"account_inactive"}""")
    }
  }

  test("Auth.Test.FailedWrongKey") {
    val req = new MockRequester with Auth.Test.FailedWrongKey
    val result = req.request(request)

    whenReady(result) { res =>
      res should equal(/*language=JSON*/ """{"ok":false,"error":"invalid_auth"}""")
    }
  }

  test("for compehension with futures"){

    val f: Future[Option[String]] = Future.successful(Some("testData"))

    f onComplete{
      case Success(result) =>

    }

  }



}
