package com.auginte.eventsourced.vdom

import org.scalajs.dom

trait MouseEvents {
  val onClick = new GenericEvent[dom.MouseEvent]("click")
  val onMouseDown = new GenericEvent[dom.MouseEvent]("mousedown")
  val onMouseUp = new GenericEvent[dom.MouseEvent]("mouseup")
  val onMouseMove = new GenericEvent[dom.MouseEvent]("mousemove")
}
