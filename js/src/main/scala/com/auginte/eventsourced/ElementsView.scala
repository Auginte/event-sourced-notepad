package com.auginte.eventsourced

import com.auginte.eventsourced.Generic.Text
import com.auginte.eventsourced.vdom.Implicits._
import com.auginte.eventsourced.vdom.{Div, Elements}
import diode._
import org.scalajs.dom.{MouseEvent, Touch, TouchEvent}

import scalatags.JsDom.all.{style => _}

class ElementsView(elements: ModelR[_, Map[Generic.AggregateId, Generic.Data]], dispatch: Dispatcher) {
  def domElements = {
    Div(
      className := "elements",
      Div(
        className := "main",
        style := "border: 2px solid green; width: 100%; height: 80%; min-height: 80%;",
        className := "main",
        onMouseUp := { (e: MouseEvent) => dispatch(MouseUp(e.screenX.toInt, e.screenY.toInt)) },
        onTouchEnd := { (e: TouchEvent) => dispatch(MouseUp(firstTouch(e).screenX.toInt, firstTouch(e).screenY.toInt)) },
        onTouchCancel := { (e: TouchEvent) => dispatch(MouseUp(firstTouch(e).screenX.toInt, firstTouch(e).screenY.toInt)) },
        onMouseMove := { (e: MouseEvent) => dispatch(MouseMove(e.screenX.toInt, e.screenY.toInt)) },
        onTouchMove := { (e: TouchEvent) => dispatch(MouseMove(firstTouch(e).screenX.toInt, firstTouch(e).screenY.toInt)) },
        Elements(innerElements)
      )
    )
  }

  private def innerElements = elements.value.map {
    case (id: Generic.AggregateId, t@Text(text, x, y, scale)) =>
      Div(
        className := "element",
        style := s"""position: absolute; left: $x; top: $y;""",
        onMouseDown := { (e: MouseEvent) => dispatch(ElementMouseDown(id, e.screenX.toInt, e.screenY.toInt)) },
        onMouseUp := { (e: MouseEvent) => dispatch(ElementMouseUp(id, e.screenX.toInt, e.screenY.toInt)) },
        onTouchStart := { (e: TouchEvent) => dispatch(ElementMouseDown(id, firstTouch(e).screenX.toInt, firstTouch(e).screenY.toInt)) },
        onTouchEnd := { (e: TouchEvent) => dispatch(ElementMouseUp(id, firstTouch(e).screenX.toInt, firstTouch(e).screenY.toInt)) },
        onMouseMove := { (e: MouseEvent) => dispatch(MouseMove(e.screenX.toInt, e.screenY.toInt)) },
        onTouchMove := { (e: TouchEvent) => dispatch(MouseMove(firstTouch(e).screenX.toInt, firstTouch(e).screenY.toInt)) },
        innerHtml := text
      )
    case other =>
      Div(
        innerHtml := other._1 + other._2.toString
      )
  }

  def firstTouch(e: TouchEvent): Touch = {
    if (e.targetTouches.length > 0) {
      e.targetTouches.item(0)
    } else if (e.touches.length > 0) {
      e.touches.item(0)
    } else if (e.changedTouches.length > 0) {
      e.changedTouches.item(0)
    } else {
      new Touch
    }
  }
}
