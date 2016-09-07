package com.auginte.eventsourced

import java.io.BufferedInputStream

import scala.io.Source

object Html {
  val consumer = Source.fromInputStream(getClass.getResourceAsStream("index.html")).mkString("")
}
