package com.auginte.eventsourced

import akka.actor.ActorRef
import akka.stream.SourceShape
import akka.stream.scaladsl.{GraphDSL, Merge}

/**
  * Assuming `realTimeMessages` is [[com.auginte.eventsourced.RealTimeMessages]]
  */
case class SessionStream(storage: Storage, project: Project, realTimeMessages: ActorRef, uuid: UUID = SessionStream.newUuid) {
  lazy val stream = GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._
      val merge = b.add(Merge[String](2))

      storage.readAll(project) ~> merge
      RealTimeMessages.source(realTimeMessages) ~> merge

      SourceShape(merge.out)
    }
}

object SessionStream {
  def newUuid: UUID = java.util.UUID.randomUUID.toString
}