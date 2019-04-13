package ru.nikitabobko.gcalnotifier.support

import ru.nikitabobko.gcalnotifier.UI_THREAD_ID
import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import ru.nikitabobko.gcalnotifier.view.View
import ru.nikitabobko.gcalnotifier.view.ViewJavaGnome
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Provider<out T> : ReadOnlyProperty<Any?, T> {
    val value: T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

interface Factory {
    val eventReminderTracker: Provider<EventReminderTracker>
    val localDataManager: Provider<LocalDataManager>
    val googleCalendarManager: Provider<GoogleCalendarManager>
    val view: Provider<View>
    val controller: Provider<Controller>
    val utils: Utils
}

object FactoryImpl : Factory {
    override val utils: Utils = UtilsImpl

    override val view = lazyProvider {
        ViewJavaGnome(UI_THREAD_ID, controller, utils)
    }

    override val controller: Provider<Controller> = lazyProvider {//
        ControllerImpl(view, localDataManager, googleCalendarManager, eventReminderTracker, utils)
    }

    override val eventReminderTracker = lazyProvider { EventReminderTrackerImpl(controller, localDataManager, utils) }

    override val localDataManager = weakProvider(::LocalDataManagerJSON)

    override val googleCalendarManager = weakProvider {
        GoogleCalendarManagerImpl(view.value::openURLInDefaultBrowser, utils, localDataManager)
    }
}
