package com.auginte.eventsourced

import prickle._

import scala.collection.mutable
import scala.scalajs.js.Date
import scala.util.{Failure, Success}

object Generic {

  def newId = java.util.UUID.randomUUID().toString

  object Implicits {
    implicit object EventPicklerPickler extends Pickler[EventName] {
      def pickle[P](x: EventName, state: PickleState)(implicit config: PConfig[P]): P =
        config.makeString(x.getClass.getName)
    }

    implicit object EventNameUnpickler extends Unpickler[EventName] {
      def unpickle[P](pickle: P, state: mutable.Map[String, Any])(implicit config: PConfig[P]) = {
        config.readString(pickle) match {
          case Success(name) if name == TextCreated.getClass.getName => Success(TextCreated)
          case Success(name) if name == TextUpdated.getClass.getName => Success(TextUpdated)
          case other => Failure(new Throwable(s"Unsupported event name: $other"))
        }
      }
    }

    implicit val dataPickler = CompositePickler[Data].concreteType[Text]
    implicit val eventPricler: Pickler[Event] = Pickler.materializePickler[Event]
    implicit val eventUnpicler = Unpickler.materializeUnpickler[Event]
  }

  type AggregateId = String

  type User = String

  sealed trait EventName

  case object TextCreated extends EventName

  case object TextUpdated extends EventName

  case class Event(eventName: EventName, aggregateId: AggregateId, data: Data, time: String, user: String, version: String = "0.0.5")

  object Event {
    private def inferred(eventName: EventName, data: Data, aggregateId: AggregateId, user: String) = Event(eventName, aggregateId, data, new Date().toISOString(), user)

    def created(data: Data)(implicit user: User) = inferred(TextCreated, data, newId, user)

    def updated(aggregateId: AggregateId, data: Data)(implicit user: User) = inferred(TextUpdated, data, aggregateId, user)
  }

  trait Movable {
    val x: Double
    val y: Double
  }

  trait Zoomable {
    val scale: Double
  }

  sealed trait Data {}

  case class Text(value: String, x: Double, y: Double, scale: Double) extends Data with Movable with Zoomable
}
