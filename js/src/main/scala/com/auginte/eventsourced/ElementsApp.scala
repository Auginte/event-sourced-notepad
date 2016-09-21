package com.auginte.eventsourced

import com.auginte.eventsourced.Generic.Text
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

object ElementsApp extends Circuit[ElementsModel]{
  import Generic.Implicits._

  override protected def initialModel: ElementsModel = ElementsModel(List())

  val textSize = 10

  val handler = new ActionHandler(zoomRW(_.elements)((m, e) => m.copy(elements = e))) {
    override protected def handle: PartialFunction[Any, ActionResult[ElementsModel]] = {
      case Empty => updated(List())
      case AddNewElement(newText, e) =>
        e.preventDefault()
        val text = Text(newText, 0, value.size * textSize, 1)
        val event = Generic.Event.inferred(text, Config.uuid)
        val data = Pickle.intoString(event) + "\n"
        sendAjax(Config.url, data)
        updated(value)

      case LoadElement(raw) =>
        Unpickle[Generic.Event].fromString(raw) match {
          case Success(e) => updated(e.data :: value)
          case Failure(err) =>
            dom.console.warn("Unable to unmarshal event", err.toString, raw)
            updated(value)
        }


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
