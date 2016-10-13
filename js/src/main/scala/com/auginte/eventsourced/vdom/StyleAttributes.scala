package com.auginte.eventsourced.vdom

import org.scalajs.dom
import org.scalajs.dom.html._

import scala.scalajs.js

trait StyleAttributes {
  val style = GenericAttribute("style")
  val position = StyleAttribute("position")
  val left = StyleAttribute("left")
  val right = StyleAttribute("right")
  val top = StyleAttribute("top")
  val bottom = StyleAttribute("bottom")
}

@js.native
trait WithAttributeSet extends js.Object {
  def setAttribute(name: String, value: String): Unit = js.native
}

case class StyleAttribute(name: String) {
  def :=(value: String): DivProperty = new DivProperty {
    override def update(e: Element)(implicit document: dom.html.Document): Unit = e.style.asInstanceOf[js.Dynamic].updateDynamic(name)(value)

    override def toString: String = s"$name = $value"
  }
}

