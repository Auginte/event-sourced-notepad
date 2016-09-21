package com.auginte.eventsourced

import diode._
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.html.Input

import scalatags.JsDom.all._

class ControlsView(dispatch: Dispatcher) {
  val newElementId = "newElement"

  private def newElement = dom.document.getElementById(newElementId).asInstanceOf[Input]

  def value() = newElement.value

  def render = {
    form(
      onsubmit := { (e: Event) => dispatch(AddNewElement(value(), e)); false },
      input(
        id := newElementId
      ),
      button(
        `type` := "submit",
        "Add element"
      )
    )
  }
}
