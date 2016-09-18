import sbt._
import Keys._
import xerial.sbt.Pack.{pack => sbtPack, _}

object Build extends sbt.Build {
  val buildName = "auginte-event-sourced"
  val buildVersion = "0.0.4"
  val buildScalaVersion = "2.11.8"
  val buildOptions = Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

  val akkaVersion = "2.4.9"
  val buildDependencies = Seq(
    "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
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
      libraryDependencies ++= buildDependencies,
      spray.revolver.RevolverPlugin.Revolver.settings
    ) settings packAutoSettings
}
