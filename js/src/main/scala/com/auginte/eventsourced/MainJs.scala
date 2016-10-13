package com.auginte.eventsourced
import com.auginte.eventsourced.vdom.Input
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.html.{Element => _, _}

import scala.scalajs.js
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

    clearChilds(root)
    root.appendChild(div(controls.render).render)
    root.appendChild(elements.domElements.createDomElement(dom.document))

    focus()
  }

  private def clearChilds(root: Element): Unit ={
    root.innerHTML = ""
  }
}
