package com.auginte.eventsourced

import scala.io.Source
import scala.xml.Utility

object Html {
  val index = resource("index.html")

  val commonJs = resource("common.js")

  def project(project: Project, uuid: UUID) = resource(
    "project.html",
    Map(
      "{{project}}" -> urlEncode(project),
      "{{project|urlencoded}}" -> java.net.URLEncoder.encode(project, "utf-8"),
      "{{uuid}}" -> uuid
    )
  )

  private def resource(file: String, parameters: Map[String, String] = Map()): String = {
    val resource = Html.getClass.getResourceAsStream(file)
    if (resource == null) {
      throw new RuntimeException(s"Cannot load resource: $file")
    }
    replace(Source.fromInputStream(resource).mkString(""), parameters)
  }

  private def replace(initialData: String, parameters: Map[String, String]): String =
    parameters.foldLeft(initialData)((commulative, pair) => commulative.replaceAllLiterally(pair._1, pair._2))

  private def urlEncode(data: String) = Utility.escape(data.replaceAll(" ", "-")).replaceAll("'", "%27").replaceAll("\"", "%22")
}
