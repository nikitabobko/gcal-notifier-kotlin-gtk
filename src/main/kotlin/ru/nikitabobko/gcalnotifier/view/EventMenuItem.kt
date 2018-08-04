package ru.nikitabobko.gcalnotifier.view

import org.gnome.gtk.Box
import org.gnome.gtk.Label
import org.gnome.gtk.MenuItem
import org.gnome.gtk.Orientation

class EventMenuItem(
        private val dateTime: String,
        private val eventTitle: String,
        private val dateTimeLabelCharWidth: Int = dateTime.length,
        handler: (MenuItem) -> Unit
) : MenuItem() {
    init {
        connect(handler)
        add(Box(Orientation.HORIZONTAL, 4).let {
            it.add(Label(dateTime).let {
                it.setAlignment(0f, 0f)
                it.setWidthChars(dateTimeLabelCharWidth)
                it.setMaxWidthChars(dateTimeLabelCharWidth)
                it
            })
            it.add(Label(eventTitle))
            return@let it
        })
    }
}