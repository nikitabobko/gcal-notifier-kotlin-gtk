package ru.nikitabobko.calendargtk.support

import org.gnome.gtk.Gtk
import java.net.URI

const val APPLICATION_NAME = "gcal-notifier-kotlin-gtk"

fun openURLInDefaultBrowser(url: String) {
    Gtk.showURI(URI(url))
}
