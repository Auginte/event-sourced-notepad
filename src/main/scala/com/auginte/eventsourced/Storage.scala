package com.auginte.eventsourced

import java.io.{BufferedReader, FileReader, FileWriter}

import akka.NotUsed
import akka.stream.scaladsl.Source
import collection.JavaConverters._
import scala.language.implicitConversions

class Storage(private val path: String) {
  def append(cluster: String, data: String): Boolean = {
    val fw = new FileWriter(fileName(cluster), true)
    try {
      fw.write(data.replaceAll("\n", "\\n") + "\n")
      true
    } catch {
      case e: Exception => false
    } finally {
      fw.close()
    }
  }

  def readAll(cluster: String): Source[String, NotUsed] = {
    val fr = new BufferedReader(new FileReader(fileName(cluster)))
    Source.fromIterator[String](() => fr.lines().iterator().asScala)
  }

  private def fileName(cluster: String) = path + "/" + cluster + ".jsonl"
}
