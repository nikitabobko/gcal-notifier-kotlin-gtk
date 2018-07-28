package ru.nikitabobko.gcalnotifier.controller

import org.gnome.notify.Notification
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.support.*
import ru.nikitabobko.gcalnotifier.view.RefreshButtonState
import ru.nikitabobko.gcalnotifier.view.View
import ru.nikitabobko.gcalnotifier.view.ViewJavaGnome
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Controls app flow. All decisions are made by it.
 */
interface Controller {
    /**
     * Notify [Controller] that application started. Should be called by [View].
     */
    fun applicationStarted()

    /**
     * Notify [Controller] that user clicked button "Open Google Calendar on web".
     * Should be called by [View].
     */
    fun openGoogleCalendarOnWebButtonClicked()

    /**
     * Notify [Controller] that user clicked status icon on system tray.
     * Should be called by [View].
     */
    fun statusIconClicked()

    /**
     * Notify [Controller] that user clicked "Quit" button. Should be called by [View].
     */
    fun quitClicked()

    /**
     * Notify [Controller] that user clicked "Refresh" button. Should be called by [View].
     */
    fun refreshButtonClicked()

    /**
     * Notify [Controller] that user clicked "Settings" button. Should be called by [View].
     */
    fun settingsButtonClicked()

    /**
     * Notify [Controller] that user clicked "Log out" button. Should be called by [View].
     */
    fun logoutButtonClicked()

    /**
     * Notify [Controller] that user clicked on of the events item in popup menu.
     * Should be called by [View].
     * @param indexOf Index of event item in popup menu.
     */
    fun eventPopupItemClicked(indexOf: Int)

    /**
     * Notify [Controller] that it's about time to remind user about event he/she setted to be reminded.
     * Should be called by [EventReminderTracker]
     * @param event Event to remind user about it
     */
    fun eventReminderTriggered(event: MyEvent)
}

class ControllerImpl : Controller {
    private val view: View = ViewJavaGnome(this)

    private val localDataManager: LocalDataManager = LocalDataManagerJSON()
    private val googleCalendarManager = GoogleCalendarManagerImpl(
            view::openURLInDefaultBrowser, localDataManager)
    private val eventReminderTracker: EventReminderTracker = EventReminderTrackerImpl(this)
    /**
     * Use only in [synchronized(eventsLock)] block
     */
    @Volatile
    private var events: List<MyEvent> = listOf()
    private val eventsLock = Any()
    private var notifyUserAboutRefreshFailures = true

    override fun eventReminderTriggered(event: MyEvent) {
        val eventStart = Date(event.startUNIXTime)
        var body = when(eventStart) {
            in today until tomorrow -> "Today"
            in tomorrow until theDayAfterTomorrow -> "Tomorrow"
            else -> SimpleDateFormat("yyyy-MM-dd").format(eventStart)
        }
        if (!event.isAllDayEvent) {
            body += SimpleDateFormat(" HH:mm").format(eventStart)
            body += SimpleDateFormat(" - HH:mm").format(Date(event.endUNIXTime))
        }

        view.showInfiniteNotification(
                event.title ?: "",
                body,
                "Open on web"
        ) { _: Notification, _: String ->
            view.openURLInDefaultBrowser(event.htmlLink)
        }
    }

    override fun statusIconClicked() {
        view.showPopupMenu()
    }

    override fun eventPopupItemClicked(indexOf: Int) {
        synchronized(eventsLock) {
            view.openURLInDefaultBrowser(events[indexOf].htmlLink)
        }
    }

    override fun logoutButtonClicked() {
        localDataManager.removeAllData()
        view.quit()
    }

    override fun settingsButtonClicked() {
        TODO("not implemented")
    }

    override fun refreshButtonClicked() {
        refresh(byExplicitRefreshButtonClick = true)
    }

    override fun quitClicked() {
        view.quit()
    }

    override fun openGoogleCalendarOnWebButtonClicked() {
        view.openURLInDefaultBrowser("https://calendar.google.com/calendar/r")
    }

    @Synchronized
    private fun refresh(byExplicitRefreshButtonClick: Boolean = false,
                        doNotNotifyAboutRefreshFailureForce: Boolean = false) {
        if (byExplicitRefreshButtonClick) {
            view.refreshButtonState = RefreshButtonState.REFRESHING
        }
        googleCalendarManager
                .getUpcomingEventsAsync { events: List<MyEvent>?, calendarList: List<MyCalendarListEntry>? ->
            if (events != null && calendarList != null) {
                localDataManager.safe(events.toTypedArray(), calendarList.toTypedArray())
                eventReminderTracker.newDataCame(events, calendarList)
                view.update(events)
                synchronized(eventsLock) {
                    this.events = events
                }
                notifyUserAboutRefreshFailures = true
            } else if (!doNotNotifyAboutRefreshFailureForce &&
                    (byExplicitRefreshButtonClick || notifyUserAboutRefreshFailures)) {
                view.showNotification("Error", "Unable to connect to Google Calendar")
                notifyUserAboutRefreshFailures = false
            }
            if (byExplicitRefreshButtonClick) {
                view.refreshButtonState = RefreshButtonState.NORMAL
            }
        }
    }

    override fun applicationStarted() {
        view.showStatusIcon()
        // Trying to load saved events
        var events: List<MyEvent>
        var calendars: List<MyCalendarListEntry>
        try {
            events = localDataManager.restoreEventsList().toList()
            calendars = localDataManager.restoreUsersCalendarList().toList()
        } catch (ex: FileNotFoundException) {
            events = listOf()
            calendars = listOf()
        }
        synchronized(eventsLock) {
            this.events = events
        }
        view.update(events)
        eventReminderTracker.newDataCame(events, calendars)
        // refresh thread
        Thread {
            // If user setted up our app to autostart then it would annoy user
            // that "Unable to connect to Google Calendar" if gcal-notifier launches
            // faster than connected to wifi network. So we don't notify user about
            // failed initial refresh.
            refresh(doNotNotifyAboutRefreshFailureForce = true)
            while (true) {
                Thread.sleep(Settings.refreshFrequencyInMinutes * 60 * 1000)
                refresh()
            }
        }.run { isDaemon = true; start() }
    }
}
