package ru.nikitabobko.gcalnotifier

import org.gnome.gtk.Gtk
import org.gnome.notify.Notify
import ru.nikitabobko.gcalnotifier.support.APPLICATION_NAME

fun main(args: Array<String>) {
    Gtk.init(arrayOf())
    Notify.init(APPLICATION_NAME)
    controller.applicationStarted()
    Gtk.main()
}
