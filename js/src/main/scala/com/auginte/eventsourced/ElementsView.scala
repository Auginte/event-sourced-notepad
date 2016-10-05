package com.auginte.eventsourced

import com.auginte.eventsourced.Generic.Text
import diode._
import org.scalajs.dom._

import scalatags.JsDom.all._

class ElementsView(elements: ModelR[_, Map[Generic.AggregateId, Generic.Data]], dispatch: Dispatcher) {
  def render = {
    div(
      style := "border: 2px solid green; width: 100%; height: 80%; min-height: 80%;",
      onmouseup := { (e: MouseEvent) => dispatch(MouseUp(e.screenX.toInt, e.screenY.toInt)) },
      " ",
      elements.value.map {
        case (id: Generic.AggregateId, t@Text(text, x, y, scale)) =>
          div(
            style := s"""position: absolute; left: $x; top: $y; display: inline; border: 1px solid red;""",
            onmousedown := { (e: MouseEvent) => dispatch(ElementMouseDown(id, e.screenX.toInt, e.screenY.toInt)) },
            onmouseup := { (e: MouseEvent) => dispatch(ElementMouseUp(id, e.screenX.toInt, e.screenY.toInt)) },
            text
          )
        case other =>
          div(other._1, other._2.toString)
      }.toSeq
    )
  }
}
