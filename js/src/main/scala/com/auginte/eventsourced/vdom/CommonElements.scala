package com.auginte.eventsourced.vdom

import org.scalajs.dom
import org.scalajs.dom.html._

case object InnerHtml {
  def :=(value: String): DivProperty = new GenericProperty {
    override def update(e: Element)(implicit document: dom.html.Document): Unit = e.innerHTML = value

    override def toString: String = s"innerHTML = $value"
  }
}

trait CommonElements {
  val innerHtml = InnerHtml
  val id = GenericAttribute("id")
  val className = GenericAttribute("class")
}