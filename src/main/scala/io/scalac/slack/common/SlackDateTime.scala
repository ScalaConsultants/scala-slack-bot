package io.scalac.slack.common

import org.joda.time.DateTime

/**
 * Created on 08.02.15 01:07
 * Slack use two formats of timestamp
 * 1. "1234567890" - time in seconds
 * 2. "1234567890.000012" - time in seconds and unique ID
 */
object SlackDateTime {

  /**
   *
   * @return 13-digist long timestamp (miliseconds)
   */
  def timeStamp: String = {
    DateTime.now.getMillis.toString //timestamp in milis
  }

  /**
   *
   * @return 10-digits long timestamp (seconds)
   */
  def seconds: String = {
    (DateTime.now.getMillis / 1000).toString
  }

  /**
   *
   * @return 10-digits long timestamp with unique connection ID
   */
  def uniqueTimeStamp: String = {
    seconds + "." + f"${MessageCounter.next}%06d"
  }
}
