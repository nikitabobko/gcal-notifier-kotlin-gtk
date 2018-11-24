package ru.nikitabobko.gcalnotifier.support

import ru.nikitabobko.gcalnotifier.UI_THREAD_ID
import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import ru.nikitabobko.gcalnotifier.view.View
import ru.nikitabobko.gcalnotifier.view.ViewJavaGnome
import kotlin.properties.ReadOnlyProperty

interface Factory : FactoryForController, FactoryForView, FactoryForEventReminderTracker

interface FactoryForController {
    val eventReminderTracker: ReadOnlyProperty<Any?, EventReminderTracker>
    val localDataManager: ReadOnlyProperty<Any?, LocalDataManager>
    val googleCalendarManager: ReadOnlyProperty<Any?, GoogleCalendarManager>
    val view: ReadOnlyProperty<Any?, View>
}

interface FactoryForView {
    val controller: ReadOnlyProperty<Any?, Controller>
}

interface FactoryForEventReminderTracker {
    val controller: ReadOnlyProperty<Any?, Controller>
    val localDataManager: ReadOnlyProperty<Any?, LocalDataManager>
}

object MyFactory : Factory {
    override val view = lazyProp { ViewJavaGnome(UI_THREAD_ID, this) }

    override val controller = lazyProp { ControllerImpl(this) }

    override val eventReminderTracker = lazyProp { EventReminderTrackerImpl(this) }

    // todo use softRef
    override val localDataManager = lazyProp(::LocalDataManagerJSON)

    // todo use softRef
    override val googleCalendarManager = lazyProp {
        GoogleCalendarManagerImpl(view.value::openURLInDefaultBrowser, localDataManager.value)
    }
}
