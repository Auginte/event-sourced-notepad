package com.auginte.eventsourced

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import akka.stream.scaladsl.Source
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.collection.immutable.Queue

/**
  * Similar to [[akka.stream.actor.ActorPublisher]],
  * but have multiple subscribers and should be used for infinite streams.
  *
  * So it could fit for `text/event-stream` protocol, that includes reconnecting (or browser refresh)
  */
class RealTimeMessages extends Actor with ActorLogging {

  import RealTimeMessages._

  private var subscribers = Map[SubscriberData, ActorRef]()
  private var buffer: Queue[Data] = Queue()

  override def receive: Receive = {
    case Subscribe(subscriber) =>
      subscribe(subscriber)
    case Cancel(subscriber) =>
      cancel(subscriber)
    case Request(subscriber, n) =>
      request(subscriber, n)
    case Publish(data) =>
      publish(data)
    case other =>
      log.debug("Unknown message", other)
  }

  private def subscribe(subscriber: SubscriberData): Unit = {
    val innerPublisher = context.actorOf(Props[InnerPublisher])
    subscribers = subscribers.updated(subscriber, innerPublisher)
  }

  private def cancel(subscriber: SubscriberData): Unit = {
    subscribers.get(subscriber) match {
      case Some(actor) =>
        actor ! PoisonPill
      case None => log.warning("Trying to stop not existing subscriber", subscriber, self, sender())
    }
    subscribers -= subscriber
  }

  private def publish(data: Data): Unit = {
    if (subscribers.isEmpty) {
      buffer = buffer.enqueue(data)
    } else {
      if (buffer.isEmpty) {
        subscribers.values.foreach { actor =>
          actor ! Publish(data)
        }
      } else {
        val allData = buffer.enqueue(data)
        buffer = Queue()
        subscribers.values.foreach { actor =>
          actor ! PublishMultiple(allData)
        }
      }
    }
  }

  private def request(subscriber: SubscriberData, n: Long): Unit = {
    subscribers.get(subscriber) match {
      case Some(actor) => actor ! Request(subscriber, n)
      case None => log.warning("No inner publisher to respond to requested data", subscriber, n)
    }
  }
}

class InnerPublisher extends Actor with ActorLogging {

  import RealTimeMessages._

  private var subscriberOption: Option[SubscriberData] = None
  private var queue: Queue[Data] = Queue()
  private var demand: Long = 0

  override def receive: Receive = {
    case Request(subscriber, n) =>
      request(subscriber, n)
      publishData()
    case Publish(data) =>
      store(data)
      publishData()
    case PublishMultiple(multiple) =>
      for (data <- multiple) {
        store(data)
      }
      publishData()
    case other =>
      log.warning("Received unknown message", other)
  }

  private def request(subscriber: SubscriberData, n: Long): Unit = {
    subscriberOption = Some(subscriber)
    demand += n
  }

  private def store(data: Data): Unit = {
    queue = queue.enqueue(data)
  }

  private def publishData(): Unit = {
    for {
      subscriber <- subscriberOption
      data: Data <- takeFromStorage()
    } {
      subscriber.onNext(data)
    }
  }

  private def takeFromStorage(): Iterable[Data] = this.synchronized {
    val (consumeNow, consumeLater) = queue.splitAt(demand.toInt)
    queue = consumeLater
    demand -= consumeNow.size
    consumeNow
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    super.postStop()
    for (
      subscriber <- subscriberOption
    ) {
      subscriber.onComplete()
    }
  }
}

object RealTimeMessages {
  type Data = String
  type SubscriberData = Subscriber[_ >: Data]

  private case class Subscribe(s: SubscriberData)

  case class Publish(data: Data)

  private[eventsourced] case class PublishMultiple(data: Seq[Data])

  private case class Cancel(s: SubscriberData)

  private[eventsourced] case class Request(s: SubscriberData, n: Long)

  private def wrapActor(actor: ActorRef) = new Publisher[Data] {
    override def subscribe(s: Subscriber[_ >: Data]): Unit = {
      actor ! Subscribe(s)
      s.onSubscribe(new Subscription() {
        override def cancel(): Unit = {
          actor ! Cancel
        }

        override def request(n: Long): Unit = {
          actor ! Request(s, n)
        }
      })
    }

  }

  def source(actor: ActorRef) = Source.fromPublisher[Data](wrapActor(actor))
}