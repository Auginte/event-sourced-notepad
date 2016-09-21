package com.auginte.eventsourced

import com.auginte.eventsourced.Generic.{Data, Text}
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

case class ElementMouseDown(element: Generic.Data, x: Int, y: Int) extends Action

case class ElementMouseUp(element: Generic.Data, x: Int, y: Int) extends Action

case class MouseUp(x: Int, y: Int) extends Action

object ElementsApp extends Circuit[ElementsModel]{
  import Generic.Implicits._

  override protected def initialModel: ElementsModel = ElementsModel(List())

  val textSize = 10

  val handler = new ActionHandler(zoomRW(identity)((m, whole) => whole)) {
    override protected def handle: PartialFunction[Any, ActionResult[ElementsModel]] = {
      case Empty => updated(ElementsModel())
      case AddNewElement(newText, e) =>
        e.preventDefault()
        val text = Text(newText, 0, value.elements.size * textSize, 1)
        val event = Generic.Event.inferred(text, Config.uuid)
        val data = Pickle.intoString(event) + "\n"
        sendAjax(Config.url, data)
        updated(value)

      case LoadElement(raw) =>
        Unpickle[Generic.Event].fromString(raw) match {
          case Success(e) =>
            dom.console.log(e.data.toString)
            updated(value.copy(elements = e.data :: value.elements))
          case Failure(err) =>
            dom.console.warn("Unable to unmarshal event", err.toString, raw)
            updated(value)
        }

      case ElementMouseDown(element, x, y) =>
        updated(value.copy(lastMousePosition = MousePosition(x, y), selectedElement = Some(element)))

      case ElementMouseUp(element, x, y) =>
        val oldElements = value.elements.diff(List(element))
        val mousePosition = MousePosition(x, y)
        val textElement = element.asInstanceOf[Text] //TODO: not casting, better copy
        val newElement = moveElement(textElement, value.lastMousePosition, mousePosition)
        updated(value.copy(elements = newElement :: oldElements, lastMousePosition = mousePosition, selectedElement = None))

      case MouseUp(x, y) =>
        value.selectedElement match {
          case Some(element) =>
            val oldElements = value.elements.diff(List(element))
            val textElement = element.asInstanceOf[Text] //TODO: not casting, better copy
            val mousePosition = MousePosition(x, y)
            val newElement = moveElement(textElement, value.lastMousePosition, mousePosition)
            updated(value.copy(elements = newElement :: oldElements, lastMousePosition = mousePosition, selectedElement = None))
          case None =>
            updated(value)
        }
    }

    private def moveElement(element: Text, oldPos: MousePosition, newPos: MousePosition): Generic.Data = {
      element.copy(x = element.x + (newPos.x - oldPos.x), y = element.y + (newPos.y - oldPos.y))
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
