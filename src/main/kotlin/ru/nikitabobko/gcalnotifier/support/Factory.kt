package ru.nikitabobko.gcalnotifier.support

import ru.nikitabobko.gcalnotifier.UI_THREAD_ID
import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import ru.nikitabobko.gcalnotifier.view.View
import ru.nikitabobko.gcalnotifier.view.ViewJavaGnome

interface Factory {
    val view: View
    val controller: Controller
}

object MyFactory : Factory {
    override val view: View = ViewJavaGnome(UI_THREAD_ID)
    override val controller: Controller = ControllerImpl(view)
        get() {
            view.controller = field
            return field
        }
}