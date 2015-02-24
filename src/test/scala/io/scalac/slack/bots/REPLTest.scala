package io.scalac.slack.bots

import io.scalac.slack.bots.repl.REPL
import org.scalatest.{Matchers, FunSuite}

class REPLTest  extends FunSuite with Matchers {
  test("foo"){
    REPL.run("List(1,2,3).map(_*2)")
  }
}
