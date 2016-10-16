package com.auginte.eventsourced

import com.auginte.eventsourced.Generic.{AggregateId, Text}
import diode.{Action, ActionHandler, ActionResult, Circuit}
import org.scalajs.dom
import org.scalajs.dom.Event
import org.scalajs.dom.ext.Ajax.InputData
import org.scalajs.dom.ext.{Ajax, AjaxException}
import prickle._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success}

case object Empty extends Action

case class AddNewElement(text: String, e: Event) extends Action

case class LoadElement(text: String) extends Action

case class ElementMouseDown(aggregateId: AggregateId, x: Int, y: Int) extends Action

case class MouseMove(x: Int, y: Int) extends Action

case class ElementMouseUp(aggregateId: AggregateId, x: Int, y: Int) extends Action

case class MouseUp(x: Int, y: Int) extends Action

object ElementsApp extends Circuit[ElementsModel]{
  import Generic.Implicits._

  override protected def initialModel: ElementsModel = ElementsModel(Map())

  val initialTop = 60
  val initialLeft = 20
  val textSize = 20

  implicit val defaultUser: Generic.User = Config.uuid

  val handler = new ActionHandler(zoomRW(identity)((m, whole) => whole)) {
    override protected def handle: PartialFunction[Any, ActionResult[ElementsModel]] = {
      case Empty => updated(ElementsModel())
      case AddNewElement(newText, e) =>
        e.preventDefault()
        val text = Text(newText, initialLeft, initialTop + value.elements.size * textSize, 1)
        val event = Generic.Event.created(text)
        val data = Pickle.intoString(event) + "\n"
        sendAjax(Config.url, data)
        noChange // Depending on backend, while inner cache not implemented

      case LoadElement(raw) =>
        Unpickle[Generic.Event].fromString(raw) match {
          case Success(e) =>
            updated(value.copy(elements = value.elements.updated(e.aggregateId, e.data)))
          case Failure(err) =>
            dom.console.warn("Unable to unmarshal event", err.toString, raw)
            noChange
        }

      case ElementMouseDown(id, x, y) =>
        updated(value.copy(lastMousePosition = MousePosition(x, y), selectedElementId = Some(id)))

      case MouseMove(x, y) =>
        val mousePosition = MousePosition(x, y)
        value.selectedElementId match {
          case Some(id) => value.elements.get(id) match {
            case Some(textElement: Text) =>
              val updatedElement = moveElement(textElement, value.lastMousePosition, mousePosition)
              movedElement(id, updatedElement, mousePosition, value.selectedElementId)
            case Some(element) =>
              dom.console.warn(s"Element to drag not supported $id: $element")
              noChange
            case None =>
              dom.console.warn("Element to drag not found", id)
              noChange
          }
          case None => noChange // No element selected
        }

      case ElementMouseUp(id, x, y) =>
        val mousePosition = MousePosition(x, y)
        value.elements.get(id) match {
          case Some(textElement: Text) =>
            val updatedElement = moveElement(textElement, value.lastMousePosition, mousePosition)
            storedElement(id, updatedElement, mousePosition, selected = None)
          case other =>
            dom.console.warn(s"Trying to mouse up not stored element: $id: $other")
            noChange
        }

      case MouseUp(x, y) =>
        val mousePosition = MousePosition(x, y)
        value.selectedElementId match {
          case Some(id) => value.elements.get(id) match {
            case Some(textElement: Text) =>
              val updatedElement = moveElement(textElement, value.lastMousePosition, mousePosition)
              storedElement(id, updatedElement, mousePosition, selected = None)
            case other =>
              dom.console.warn(s"Trying to move not stored selected element: $id: $other")
              noChange
          }
          case None => noChange
        }
    }

    def storeUpdated(id: AggregateId, element: Generic.Data): Unit ={
      val event = Generic.Event.updated(id, element)
      val data = Pickle.intoString(event) + "\n"
      sendAjax(Config.url, data)
    }

    private def moveElement(element: Text, oldPos: MousePosition, newPos: MousePosition): Generic.Data = {
      element.copy(x = element.x + (newPos.x - oldPos.x), y = element.y + (newPos.y - oldPos.y))
    }

    private def movedElement(id: AggregateId, element: Generic.Data, mousePosition: MousePosition, selected: Option[AggregateId]) =
      updated(value.copy(elements = value.elements.updated(id, element), lastMousePosition = mousePosition, selectedElementId = selected))

    private def storedElement(id: AggregateId, element: Generic.Data, mousePosition: MousePosition, selected: Option[AggregateId]) = {
      storeUpdated(id, element)
      movedElement(id, element, mousePosition, selected)
    }

  }

  private def handleAjax(url: String, input: String): Future[String] =
    Ajax.post(url, InputData.str2ajax(input)).map(_.responseText)

  private def sendAjax(url: String, data: String): Unit = {
    val response = handleAjax(url, data)
    response.onFailure{
      case err: AjaxException => dom.console.warn("Failed to store data to backend", err.xhr, data)
    }
  }

  override protected def actionHandler: HandlerFunction = composeHandlers(handler)
}
