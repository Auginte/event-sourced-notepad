import com.typesafe.sbt.less.Import.LessKeys
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import xerial.sbt.Pack.{pack => sbtPack, _}

object Build extends sbt.Build {
  val buildName = "auginte-event-sourced"
  val buildVersion = "0.0.6"
  val buildScalaVersion = "2.11.8"
  val buildOptions = Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")
  val akkaVersion = "2.4.9"


  lazy val auginteEventSourcedAggregated = project.in(file(".")).
    aggregate(auginteEventSourcedJS, auginteEventSourcedJVM).
    settings(
      publish := {},
      publishLocal := {}
    )

  lazy val auginteEventSourced = crossProject.in(file(".")).
    settings(
      name := buildName,
      version := buildVersion,
      organization := "com.auginte",
      scalaVersion := buildScalaVersion,
      scalacOptions := buildOptions
    ).
    enablePlugins(SbtWeb).
    jvmSettings(
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion
      ),
      includeFilter in(Assets, LessKeys.less) := "*.less",
      LessKeys.compress in Assets := true,
      LessKeys.verbose := true,
      mainClass in Compile := Some("com.auginte.eventsourced.Main")
    ).
    jvmSettings(spray.revolver.RevolverPlugin.Revolver.settings: _*).
    jvmSettings(packAutoSettings: _*).
    jsSettings(
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.1",
        "com.lihaoyi" %%% "scalatags" % "0.6.0",
        "com.github.benhutchison" %%% "prickle" % "1.1.11",
        "me.chrons" %%% "diode" % "1.0.0"
      ),
      persistLauncher := true
    )

  lazy val auginteEventSourcedJVM = auginteEventSourced.jvm
  lazy val auginteEventSourcedJS = auginteEventSourced.js
}
