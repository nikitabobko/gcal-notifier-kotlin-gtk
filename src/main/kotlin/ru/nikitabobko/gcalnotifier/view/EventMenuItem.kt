package ru.nikitabobko.gcalnotifier.view

import org.gnome.gtk.MenuItem
import org.gnome.gtk.Orientation

class EventMenuItem(dateTime: String, eventTitle: String,
                    dateTimeLabelCharWidth: Int = dateTime.length,
                    handler: (MenuItem) -> Unit) : MenuItem() {
    init {
        box(Orientation.HORIZONTAL, 4) {
            label(dateTime) {
                setAlignment(0f, 0f)
                setWidthChars(dateTimeLabelCharWidth)
            }
            label(eventTitle)
        }

        connect(handler)
    }
}