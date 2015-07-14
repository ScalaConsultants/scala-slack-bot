package io.scalac.slack.bots.recruitment

trait MatcherEngine {
  def findClosest(task: TaskData): Option[Scalac]
}
