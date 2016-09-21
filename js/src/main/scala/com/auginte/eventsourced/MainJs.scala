package com.auginte.eventsourced
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.html.{Element => _, _}

import scala.scalajs.js.JSApp
import scalatags.JsDom.all._

object MainJs extends JSApp {

  val elements = new ElementsView(ElementsApp.zoom(_.elements), ElementsApp)
  val controls = new ControlsView(ElementsApp)


  def main(): Unit = {
    var loaded = false

    def onReady(action: => Unit): Unit = {
      dom.document.onreadystatechange = { e: Event =>
        if (!loaded) {
          loaded = true
          action
        }
      }
    }

    onReady{
      val root = dom.document.getElementById("root")
      ElementsApp.subscribe(ElementsApp.zoom(identity))(_ => render(root))
      ElementsApp(Empty)

      val source = new dom.EventSource(Config.url)
      val messageHandler = (e: MessageEvent) => {
        ElementsApp(LoadElement(e.data.asInstanceOf[String]))
      }
      source.addEventListener[MessageEvent]("message", messageHandler, useCapture = false)

    }
  }

  def render(root: Element) = {
    def focus(): Unit = {
      val element = dom.document.getElementById("newElement").asInstanceOf[Input]
      element.focus()
    }

    root.innerHTML = ""
    val e = div(
      controls.render,
      div(
        `class` := "elements",
        elements.render
      )
    )
    root.appendChild(e.render)
    focus()
  }
}
