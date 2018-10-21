package ru.nikitabobko.gcalnotifier.support


import ru.nikitabobko.gcalnotifier.UI_THREAD_ID
import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import ru.nikitabobko.gcalnotifier.view.View
import ru.nikitabobko.gcalnotifier.view.ViewJavaGnome
import ru.nikitabobko.kotlin.refdelegation.softRef

interface Factory : ControllerFactory, ViewFactory, EventReminderTrackerFactory

interface ControllerFactory {
    val eventReminderTracker: EventReminderTracker
    val localDataManager: LocalDataManager
    val googleCalendarManager: GoogleCalendarManager
    val view: View
}

interface ViewFactory {
    val controller: Controller
}

interface EventReminderTrackerFactory {
    val controller: Controller
}

object MyFactory : Factory {
    override val view: ViewJavaGnome = ViewJavaGnome(UI_THREAD_ID, this)

    override val controller: Controller = ControllerImpl(this)

    override val eventReminderTracker: EventReminderTracker = EventReminderTrackerImpl(this)

    override val localDataManager: LocalDataManager by softRef(::LocalDataManagerJSON)

    override val googleCalendarManager: GoogleCalendarManager by softRef {
        GoogleCalendarManagerImpl(view::openURLInDefaultBrowser, localDataManager)
    }
}
