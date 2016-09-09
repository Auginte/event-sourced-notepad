package com.auginte.eventsourced

import akka.actor.ActorRef
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.scaladsl.Source

class RealTimeMessages extends ActorPublisher[String] {
  private var storage:List[String] = List()

  def receive = {
    case Request(cnt) =>
      sendStored()
    case Cancel =>
      context.stop(self)
    case data:String =>
      storage ::= data
      sendStored()
    case _ =>
  }

  private def sendStored() = if (isActive && totalDemand > 0) {
    val messages = takeFromStorage(totalDemand.toInt)
    messages.foreach(message => if (isActive && totalDemand > 0) onNext(message))
  }

  private def takeFromStorage(demand: Int): Iterable[String] = this.synchronized {
    val(consumeNow, consumeLater) = storage.splitAt(totalDemand.toInt)
    storage = consumeLater
    consumeNow
  }
}

object RealTimeMessages {
  def source(publisher: ActorRef) = Source.fromPublisher(ActorPublisher[String](publisher))
}