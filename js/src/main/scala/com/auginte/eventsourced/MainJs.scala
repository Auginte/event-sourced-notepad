package com.auginte.eventsourced

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.html.{Element => _, _}

import scala.scalajs.js.{Date, JSApp}
import scalatags.JsDom.all._

object MainJs extends JSApp {

  val elements = new ElementsView(ElementsApp.zoom(_.elements), ElementsApp)
  val controls = new ControlsView(ElementsApp)

  object DEBUG {
    var guiRepainted = 0

    var frames = 0

    var started: scala.Option[Double] = None

    def repainted() = {
      guiRepainted += 1
      val debug = document.getElementById("debugUpdates").asInstanceOf[dom.html.Span]
      debug.innerHTML = s"R(${DEBUG.guiRepainted})"
    }

    def framesPerSecond() = {
      Async.timer(1000) {
        val fpsElement = document.getElementById("debugFps")
        val now = new Date().getTime()
        started match {
          case Some(s: Double) =>
            val fps = frames / (now - s) * 1000
            if (fps > 1) {
              fpsElement.innerHTML = Math.floor(fps) + " fps"
            } else {
              fpsElement.innerHTML = (Math.floor(fps * 10) / 10) + " fps"
            }

          case None =>
            started = Some(now)
            fpsElement.innerHTML = "Starting"
        }
        started = Some(now)
        frames = 0
        true
      }
    }
  }

  var needUpdate = false

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

    onReady {
      val updatesPerSecond = 60
      Async.timer(1000 / updatesPerSecond) {
        DEBUG.frames += 1
        if (needUpdate) {
          needUpdate = false
        }
        true
      }
      DEBUG.framesPerSecond()

      // Data model
      val root = dom.document.getElementById("root")
      ElementsApp.subscribe(ElementsApp.zoom(identity))(_ => render(root))
      ElementsApp(Empty)

      // Updates from server
      val source = new dom.EventSource(Config.url)
      val messageHandler = (e: MessageEvent) => {
        ElementsApp(LoadElement(e.data.asInstanceOf[String]))
      }
      source.addEventListener[MessageEvent]("message", messageHandler, useCapture = false)
    }
  }

  def render(root: Element) = {
    def clearChilds(root: Element): Unit = {
      root.innerHTML = ""
    }

    clearChilds(root)
    root.appendChild(div(controls.render).render)
    root.appendChild(elements.domElements.createDomElement(dom.document))
    DEBUG.repainted()
  }
}
