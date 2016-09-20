package com.auginte.eventsourced

import diode.{Action, ActionHandler, ActionResult, Circuit}
import org.scalajs.dom.Event

case object Empty extends Action

case class AddNewElement(text: String, e: Event) extends Action

object ElementsApp extends Circuit[ElementsModel]{
  override protected def initialModel: ElementsModel = ElementsModel(List())

  val handler = new ActionHandler(zoomRW(_.elements)((m, e) => m.copy(elements = e))) {
    override protected def handle: PartialFunction[Any, ActionResult[ElementsModel]] = {
      case Empty => updated(List())
      case AddNewElement(newText, e) =>
        e.preventDefault()
        updated(newText :: value)
    }
  }

  override protected def actionHandler: HandlerFunction = composeHandlers(handler)
}
