package io.scalac.slack

import com.typesafe.config.ConfigFactory
import io.scalac.slack.api.APIKey

/**
 * Created on 20.01.15 22:17
 */
object Config {

  private val config = ConfigFactory.load()

  def apiKey :APIKey = APIKey(config.getString("api.key"))
}
