package io.scalac.slack.common

abstract class AbstractRepository(val bucket: String) {



  def create(q: String) = ???
  def find(q: String) = ???
}
