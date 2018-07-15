package ru.nikitabobko.gcalnotifier

import org.gnome.gtk.Gtk
import org.gnome.notify.Notify
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import ru.nikitabobko.gcalnotifier.support.APPLICATION_NAME

/**
 * Application entry point
 */
fun main(args: Array<String>) {
    Gtk.init(arrayOf())
    Notify.init(APPLICATION_NAME)
    ControllerImpl().applicationStarted()
    Gtk.main()
}
