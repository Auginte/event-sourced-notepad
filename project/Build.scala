import sbt._
import Keys._
import xerial.sbt.Pack.{pack => sbtPack, _}

object Build extends sbt.Build {
  val buildName = "auginte-event-sourced"
  val buildVersion = "0.0.1"
  val buildScalaVersion = "2.11.8"
  val buildOptions = Seq("-unchecked", "-deprecation", "-encoding", "utf8")

  val akkaVersion = "2.4.9"
  val buildDependencies = Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion
  )

  lazy val auginteEventSourced = Project(id = buildName, base = file(".")) settings
    (
      name := buildName,
      version := buildVersion,
      organization := "com.auginte",
      scalaVersion := buildScalaVersion,
      scalacOptions := buildOptions,
      libraryDependencies ++= buildDependencies
    ) settings packAutoSettings
}
