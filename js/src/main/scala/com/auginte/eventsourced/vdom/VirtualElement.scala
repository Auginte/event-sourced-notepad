package com.auginte.eventsourced.vdom

import org.scalajs.dom
import org.scalajs.dom.html._

case class VirtualElement(tag: String, properties: Property*) extends GenericProperty {
  def createDomElement(implicit document: dom.html.Document): Element ={
    val element = dom.document.createElement(tag).asInstanceOf[dom.html.Element]
    properties.foreach(_.update(element))
    element
  }

  override def update(parent: Element)(implicit document: dom.html.Document): Unit = parent.appendChild(this.createDomElement)
}

trait GenericProperty extends DivProperty with InputProperty

case class Elements(elements: Iterable[VirtualElement]) extends GenericProperty {
  override def update(parent: Element)(implicit document: Document): Unit = {
    elements.foreach(element => parent.appendChild(element.createDomElement))
  }
}
