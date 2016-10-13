package com.auginte.eventsourced.vdom

trait CommonAttributes extends CommonElements with StyleAttributes with MouseEvents with TouchEvents

object Implicits extends CommonAttributes with InputElement with DivElement
