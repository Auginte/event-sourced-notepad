package com.auginte.eventsourced

import scala.scalajs.js.Dynamic.{global => g}

/**
  * Created by aurelijus on 16.9.21.
  */
object Config {
  val uuid = g.uuid.asInstanceOf[String]
  val url = g.url.asInstanceOf[String]
}
