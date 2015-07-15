package io.scalac.slack.bots.recruitment

import scala.util.Random
import scala.math._

class KMeansMatcherEngine(people: List[Scalac]) extends MatcherEngine {

  val threshold = 1.0D

  val rand = new Random()

  override def matchCandidate(task: TaskData): Option[Scalac] = {
    val withDistnace = people.map(sc => {
      val distance = sqrt(pow(abs(sc.focus - task.focus), 2) + pow(abs(sc.level - task.level), 2))
      (sc, distance)
    })
    val filtered = withDistnace.filter(_._2 <= threshold).map(_._1)
    rand.shuffle(filtered).headOption
  }
}
