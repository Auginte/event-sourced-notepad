package com.auginte.eventsourced

import akka.stream.scaladsl.Source
import org.reactivestreams.{Publisher, Subscriber, Subscription}

class RealTimeMessages {

}

object RealTimeMessages {
  private val canceled = -1

  private var storage:List[String] = List()

  private type DataSubscriber = Subscriber[_ >: String]

  private var subscribers = Map[DataSubscriber, Long]()

  def publishData(data: String): Unit = {
    println("# Queuing new data")
    storage ::= data
    sendStored()
  }

  private def takeFromStorage(demand: Long): Iterable[String] = this.synchronized {
    val(consumeNow, consumeLater) = storage.splitAt(demand.toInt)
    storage = consumeLater
    consumeNow
  }

  private def sendStored() = subscribers.foreach{ case (subscriber: DataSubscriber, demand: Long) =>
    val data = takeFromStorage(demand)
    println(s"# Sending data: ${data.size} -> $subscriber")
    data.foreach (subscriber.onNext)
    addValue(subscriber, -data.size)
  }

  private def replaceValue(subscriber: DataSubscriber, newValue: Long): Unit = {
    subscribers.updated(subscriber, newValue)
  }

  private def addValue(subscriber: DataSubscriber, diffValue: Long): Long = {
    subscribers.get(subscriber) match {
      case Some(oldValue) =>
        if (oldValue != canceled) {
          subscribers = subscribers.updated(subscriber, oldValue + diffValue)
          oldValue + diffValue
        } else {
          canceled
        }
      case None =>
        subscribers = subscribers.updated(subscriber, diffValue)
        diffValue
    }
  }


  def debugPublisher = new Publisher[String] {


    private def notifyPublishing(requested: Long): Unit =
    {
      println("Publishing")

    }

    override def subscribe(s: Subscriber[_ >: String]): Unit = {
      subscribers = subscribers.updated(s, 0)
      s.onSubscribe(new Subscription() {
        override def cancel(): Unit = {
          println("# Cancel")
          replaceValue(s, canceled)
        }

        override def request(n: Long): Unit = {
          println(s"# Requested: $n")
          addValue(s, n)
          sendStored()
        }
      })
    }

  }

  def source() = Source.fromPublisher(debugPublisher)
}