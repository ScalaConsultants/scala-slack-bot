package io.scalac.slack.bots

import io.scalac.slack.Config
import io.scalac.slack.bots.repl.Repl
import org.scalatest.{Matchers, FunSuite}

class ReplTest  extends FunSuite with Matchers {
  test("repl"){
    new Repl(Config.scalaLibraryPath).run("List(1,2,3).map(_*2)")
  }
}
