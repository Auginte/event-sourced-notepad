package com.auginte.eventsourced

import akka.actor.{ActorRefFactory, Props}
import com.auginte.eventsourced.RealTimeMessages.Publish

case class SessionFactory(storage: Storage, context: ActorRefFactory) {
  private type Sessions = Map[UUID, SessionStream]

  private var projects: Map[Project, Sessions] = Map()

  def newSession(project: Project): SessionStream = {
    val uuid = SessionStream.newUuid
    val actor = context.actorOf(Props[RealTimeMessages], RealTimeMessages.getClass.getName + "-" + uuid)
    val session = SessionStream(storage, project, actor, uuid)
    appendToProject(session)
    session
  }

  def get(project: Project, uuid: UUID): Option[SessionStream] = projects.get(project) match {
    case Some(sessions) => sessions.get(uuid)
    case None => None
  }

  private def appendToProject(session: SessionStream): Unit = this.synchronized {
    val project = session.project
    projects.get(project) match {
      case Some(oldSessions) => projects = projects.updated(project, oldSessions + (session.uuid -> session))
      case None => projects = projects.updated(project, Map(session.uuid -> session))
    }
  }

  def publish(data: Data, project: Project): Boolean = projects.get(project) match {
    case Some(sessions) =>
      sessions.values.foreach { m =>
        m.realTimeMessages ! Publish(data)
      }
      true
    case None =>
      false
  }

  def detach(project: Project, uuid: UUID): Boolean = this.synchronized {
    projects.get(project) match {
      case Some(sessions) =>
        val oldCount = sessions.size
        val cleanedSessions = sessions - uuid
        if (cleanedSessions.isEmpty) {
          projects -= project
          true
        } else if (oldCount > cleanedSessions.size) {
          projects = projects.updated(project, cleanedSessions)
          true
        } else {
          false
        }
      case None => false
    }
  }
}
