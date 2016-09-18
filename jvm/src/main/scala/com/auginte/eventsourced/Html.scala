package com.auginte.eventsourced

import java.io.File

import scala.io.Source
import scala.xml.Utility

object Html {
  val index = resource("index.html")

  val commonJs = resource("common.js")

  def project(project: Project, uuid: UUID, compiledJsName: String, compiledJsLauncherName: String, compiledJsDepsName: String) = resource(
    "project.html",
    Map(
      "{{project}}" -> urlEncode(project),
      "{{project|urlencoded}}" -> java.net.URLEncoder.encode(project, "utf-8"),
      "{{uuid}}" -> uuid,
      "{{compiledJsName}}" -> compiledJsName,
      "{{compiledJsLauncherName}}" -> compiledJsLauncherName,
      "{{compiledJsDepsName}}" -> compiledJsDepsName
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

  def readJs(name: String) = {
    val file = new File(name)
    Source.fromFile(file)
  }
}
