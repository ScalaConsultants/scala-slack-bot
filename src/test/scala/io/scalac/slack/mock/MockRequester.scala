package io.scalac.slack.mock

import io.scalac.slack.Requester
import spray.http.HttpRequest
import spray.httpx.RequestBuilding.Get

import scala.concurrent.Future

/**
 * Created on 25.01.15 22:58
 */
trait MockRequester extends Requester {

  val KEY = "xoxb-3466662038-mFVCBbog4HgW2CHe89bd87J5"

}

object Api {

  object Test {

    trait Empty extends MockRequester {
      override def request(request: HttpRequest): Future[String] = {
        val response = /*language=JSON*/ """{"ok":true}"""

        Future.successful(response)
      }
    }

    trait WithParam extends MockRequester {
      override def request(request: HttpRequest): Future[String] = {
        val response = /*language=JSON*/ """{"ok":true,"args":{"name":"mario"}}"""

        Future.successful(response)
      }
    }

    trait WithError extends MockRequester {
      override def request(request: HttpRequest): Future[String] = {
        val res = """{"ok":false,"error":"auth_error","args":{"error":"auth_error"}}"""
        Future.successful(res)
      }
    }

    trait WithErrorAndParam extends MockRequester {
      override def request(request: HttpRequest): Future[String] = {
        val res = """{"ok":false,"error":"auth_error","args":{"error":"auth_error","name":"mario"}}"""
        Future.successful(res)
      }
    }

  }

}

object Auth {
  object Test {
    trait Successful extends MockRequester with MockSlackData {
      override def request(request: HttpRequest): Future[String] = {
        Future.successful(s"""{"ok":true,"url":"$url","team":"$team","user":"$username","team_id":"$teamId","user_id":"$userId"}""")
      }
    }

    trait FailedNoKey extends MockRequester {
      override def request(request: HttpRequest): Future[String] = Future.successful("""{"ok":false,"error":"not_authed"}""")
    }

    trait FailedWrongKey extends MockRequester {
      override def request(request: HttpRequest): Future[String] = Future.successful("""{"ok":false,"error":"invalid_auth"}""")
    }

    trait FailedKeyInactive extends MockRequester {
      override def request(request: HttpRequest): Future[String] = Future.successful("""{"ok":false,"error":"account_inactive"}""")
    }
  }
}
