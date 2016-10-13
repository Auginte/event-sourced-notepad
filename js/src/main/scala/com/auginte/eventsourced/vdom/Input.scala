package com.auginte.eventsourced.vdom

import org.scalajs.dom
import org.scalajs.dom.html._

object Input extends CommonAttributes with InputElement {
  def apply(properties: InputProperty*) = VirtualElement("input", properties: _*)
}

case class InputAttribute(name: String) {
  def :=(value: String): InputProperty = new PropertyValue(name, value) with InputProperty
}

case class InputEvent[A <: dom.Event](`type`: String) {
  def := (action: (A) => Unit): InputProperty = new FunctionValue[A](`type`, action) with InputProperty
}

trait InputElement {
  val value = InputAttribute("value")
}

trait InputProperty extends Property


