package com.auginte.eventsourced
import scalatags.JsDom.all._
import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js.JSApp

object MainJs extends JSApp {

  val elements = new ElementsView(ElementsApp.zoom(_.elements), ElementsApp)
  val controls = new ControlsView(ElementsApp)

  def main(): Unit = {
    dom.document.onreadystatechange = { e: Event =>
      val root = dom.document.getElementById("root")
      ElementsApp.subscribe(ElementsApp.zoom(identity))(_ => render(root))
      ElementsApp(Empty)
    }
  }

  def render(root: Element) = {
    root.innerHTML = ""
    val e = div(
      controls.render,
      elements.render
    )
    root.appendChild(e.render)
  }
}
