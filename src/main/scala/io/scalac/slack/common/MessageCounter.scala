package io.scalac.slack.common

import java.util.concurrent.atomic.AtomicInteger

class MessageCounter() {
  private val cc = new AtomicInteger(0)
  def get() = cc.incrementAndGet()
}
