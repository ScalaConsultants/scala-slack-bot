package io.scalac.slack.api.methods

import io.scalac.slack.Config

/**
 * Created on 21.01.15 12:38
 */
trait MethodsBase {

  def url(endpoint: String) = Config.baseUrl(endpoint)

}
