package com.auginte.eventsourced


case class ElementsModel(elements: Map[Generic.AggregateId, Generic.Data] = Map(), lastMousePosition: MousePosition = MousePosition(), selectedElementId: Option[Generic.AggregateId] = None)

case class MousePosition(x: Int = 0, y: Int = 0)
