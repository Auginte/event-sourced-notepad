package com.auginte.eventsourced

import diode._

import scalatags.JsDom.all._

class ElementsView(elements: ModelR[_, List[Generic.Data]], dispatch: Dispatcher) {
  def render = {
    elements.value.map(e => div(e.toString))
  }
}
