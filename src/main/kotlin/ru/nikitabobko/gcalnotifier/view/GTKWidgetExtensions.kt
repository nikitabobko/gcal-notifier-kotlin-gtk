package ru.nikitabobko.gcalnotifier.view

import org.gnome.gtk.*

inline fun <T : Widget> Container.add(child: T, block: T.() -> Unit): T {
    add(child.apply(block))
    return child
}

inline fun <T : Widget> Container.addAndGetChild(child: T): T {
    add(child)
    return child
}

inline fun Container.box(orientation: Orientation, spacing: Int,
                         block: Box.() -> Unit) = add(Box(orientation, spacing), block)
inline fun Container.box(orientation: Orientation, spacing: Int) = addAndGetChild(Box(orientation, spacing))

inline fun Container.label(text: String, block: Label.() -> Unit) = add(Label(text), block)
inline fun Container.label(text: String) = addAndGetChild(Label(text))
