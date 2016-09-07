package com.auginte.eventsourced

import scala.io.Source

object Html {
  val consumer = resource("index.html")

  def page(project: String) = resource("project.html", Map("{{project}}" -> project))

  private def resource(file: String, parameters: Map[String, String] = Map()): String = {
    val resource = Html.getClass.getResourceAsStream(file)
    if (resource == null) {
      throw new RuntimeException(s"Cannot load resource: $file")
    }
    replace(Source.fromInputStream(resource).mkString(""), parameters)
  }

  private def replace(initialData: String, parameters: Map[String, String]): String =
    parameters.foldLeft(initialData)((commulative, pair) => commulative.replaceAllLiterally(pair._1, pair._2))
}
