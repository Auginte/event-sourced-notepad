package com.auginte.eventsourced

import prickle._

import scala.scalajs.js.Date

object Generic {

  object Implicits {
    implicit val dataPickler = CompositePickler[Data].concreteType[Text]
    implicit val eventPricler: Pickler[Event] = Pickler.materializePickler[Event]
    implicit val eventUnpicler = Unpickler.materializeUnpickler[Event]
  }


  sealed trait Data {}

  case class Event(eventName: String, data: Data, time: String, user: String, version: String = "0.0.5")

  object Event {
    def inferred(data: Data, user: String): Event = Event(data.getClass.getName, data, new Date().toISOString(), user)
  }

  trait Movable {
    val x: Double
    val y: Double
  }

  trait Zoomable {
    val scale: Double
  }

  case class Text(value: String, x: Double, y: Double, scale: Double) extends Data with Movable with Zoomable
}
