package io.scalac.slack.bots.recruitment

trait MatcherEngine {
  def matchCandidate(task: TaskData): Option[Scalac]
}
