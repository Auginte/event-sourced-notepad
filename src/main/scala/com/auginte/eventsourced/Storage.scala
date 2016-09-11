package com.auginte.eventsourced

import java.io.{BufferedReader, File, FileReader, FileWriter}

import akka.NotUsed
import akka.stream.scaladsl.Source

import collection.JavaConverters._
import scala.language.implicitConversions

class Storage(private val path: String) {
  def append(project: Project, data: String): Boolean = {
    val fw = new FileWriter(fileName(project), true)
    try {
      fw.write(data.replaceAll("\n", "\\n") + "\n")
      true
    } catch {
      case e: Exception => false
    } finally {
      fw.close()
    }
  }

  def readAll(project: Project): Source[String, NotUsed] = {
    val file = new File(fileName(project))
    if (file.exists()) {
      val fr = new BufferedReader(new FileReader(fileName(project)))
      Source.fromIterator[String](() => fr.lines().iterator().asScala)
    } else {
      Source.empty[String]
    }
  }

  private def fileName(project: Project) =  path + "/" + md5Hash(project) + "-" + sanitise(project) + ".jsonl"

  private def sanitise(name: String) = {
    name.map(_.toString).filter(_.matches("[a-zA-Z0-9-_]")).mkString("")
  }

  private def md5Hash(s: String) = {
    val m = java.security.MessageDigest.getInstance("MD5")
    val b = s.getBytes("UTF-8")
    m.update(b, 0, b.length)
    new java.math.BigInteger(1, m.digest()).toString(16)
  }
}
