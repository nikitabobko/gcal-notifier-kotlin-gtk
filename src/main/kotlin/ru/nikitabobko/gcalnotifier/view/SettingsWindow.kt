package ru.nikitabobko.gcalnotifier.view

import org.gnome.gtk.*

class SettingsWindow(view: ViewJavaGnome, onClose: (() -> Unit)? = null) : Window(){
    init {
        setTitle("Settings")
        setIcon(view.appIcon)

        add(Box(Orientation.VERTICAL, 4).apply {
            add(Box(Orientation.HORIZONTAL, 4).apply {
                add(Label("Refresh frequency in minutes:"))
                add(Entry("5"))
            })
            add(Box(Orientation.HORIZONTAL, 4).apply {
                add(Label("Max number of elements in popup"))
                add(Entry("5"))
            })
        })

        connect(Widget.Hide { if (onClose != null) onClose() })

        showAll()
    }

    fun focusWindow() {
        hide()
        showAll()
    }
}