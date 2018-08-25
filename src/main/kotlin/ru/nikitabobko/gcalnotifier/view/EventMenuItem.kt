package ru.nikitabobko.gcalnotifier.view

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

        box(Orientation.HORIZONTAL, 4) {
            label(dateTime) {
                setAlignment(0f, 0f)
                setWidthChars(dateTimeLabelCharWidth)
                setMaxWidthChars(dateTimeLabelCharWidth)
            }
            label(eventTitle)
        }
    }
}