package com.auginte.eventsourced.vdom

import org.scalajs.dom
import org.scalajs.dom.html._

trait Property {
  def update(e: Element)(implicit document: dom.html.Document)
}

case class GenericAttribute(name: String) {
  def :=(value: String) = new GenericProperty {
    override def update(e: Element)(implicit document: dom.html.Document): Unit = e.setAttribute(name, value)

    override def toString: String = s"$name = $value"
  }
}

case class GenericEvent[A <: dom.Event](`type`: String) {
  def := (action: (A) => Unit): DivProperty = new FunctionValue[A](`type`, action)
}

class PropertyValue(name: String, value: String) extends Property {
  override def update(e: Element)(implicit document: dom.html.Document): Unit = e.setAttribute(name, value)

  override def toString: String = s"$name = $value"
}

class FunctionValue[A <: dom.Event](`type`: String, action: (A) => Unit) extends GenericProperty {
  override def update(e: Element)(implicit document: dom.html.Document): Unit = e.addEventListener(`type`, action)

  override def toString: String = s"${`type`} = [FUNCTION]()"
}