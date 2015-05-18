package io.scalac.slack.common

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigFactory, Config}

object HelperActorSystem {
  private val config = ConfigFactory.load()
  val system: ActorSystem = ActorSystem("HelperSystem", config.getConfig("helper-system").withFallback(config))
}
