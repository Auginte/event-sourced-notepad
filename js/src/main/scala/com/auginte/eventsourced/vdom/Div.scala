package com.auginte.eventsourced.vdom

import org.scalajs.dom

object Div extends CommonAttributes with DivElement {
  def apply(divProperties: DivProperty*) = VirtualElement("div", divProperties: _*)
}

case class DivAttribute(name: String) {
  def :=(value: String): DivProperty = new PropertyValue(name, value) with DivProperty
}

case class DivEvent[A <: dom.Event](`type`: String) {
  def := (action: (A) => Unit): DivProperty = new FunctionValue[A](`type`, action) with DivProperty
}

trait DivElement {
}

trait DivProperty extends Property