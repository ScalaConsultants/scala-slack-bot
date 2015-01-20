package io.scalac.slack

import org.slf4j.LoggerFactory

/**
 * Created on 20.01.15 23:19
 */
trait Logger {
  def logger = LoggerFactory.getLogger(getClass)
}
