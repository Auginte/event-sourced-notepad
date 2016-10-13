package com.auginte.eventsourced.vdom

import org.scalajs.dom

trait TouchEvents {
  val onTouchStart = new GenericEvent[dom.TouchEvent]("touchstart")
  val onTouchEnd = new GenericEvent[dom.TouchEvent]("touchend")
  val onTouchCancel = new GenericEvent[dom.TouchEvent]("touchcancel")
  val onTouchMove = new GenericEvent[dom.TouchEvent]("touchmove")
}
