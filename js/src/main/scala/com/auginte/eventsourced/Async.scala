package com.auginte.eventsourced

import org.scalajs.dom._

object Async {
  def timer(interval: Int)(f: => Boolean): Unit = {
    val continue = f
    if (continue) {
      window.setTimeout(() => timer(interval)(f), interval)
    }
  }
}
