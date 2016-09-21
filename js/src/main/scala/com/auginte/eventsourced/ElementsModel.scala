package com.auginte.eventsourced


case class ElementsModel(elements: List[Generic.Data] = List(), lastMousePosition: MousePosition = MousePosition(), selectedElement: Option[Generic.Data] = None)

case class MousePosition(x: Int = 0, y: Int = 0)
