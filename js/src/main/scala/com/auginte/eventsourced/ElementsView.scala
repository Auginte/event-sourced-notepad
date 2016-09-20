package com.auginte.eventsourced

import diode._
import scalatags.JsDom.all._

class ElementsView(elements: ModelR[_, List[String]], dispatch: Dispatcher) {
  def render = {
    elements.value.map(div(_))
  }
}
