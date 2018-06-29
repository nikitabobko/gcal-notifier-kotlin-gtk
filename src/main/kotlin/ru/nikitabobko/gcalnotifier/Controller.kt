package ru.nikitabobko.gcalnotifier

import org.gnome.notify.Notification
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.support.openURLInDefaultBrowser
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.util.*

val controller: Controller = ControllerImpl()

interface Controller {
    fun applicationStarted()
    fun openGoogleCalendarOnWebButtonClicked()
    fun statusIconClicked()
    fun quitClicked()
    fun refreshButtonClicked()
    fun settingsButtonClicked()
    fun logoutButtonClicked()
    fun eventPopupItemClicked(indexOf: Int)
    fun eventReminderTriggered(event: MyEvent)
}

class ControllerImpl : Controller {
    private val googleCalendarManager = GoogleCalendarManagerImpl()
    private val localDataManager: LocalDataManager = LocalDataManagerJSON()
    private val eventReminderTracker: EventReminderTracker = EventReminderTrackerImpl(this)
    /**
     * Use only in [synchronized(eventsLock)] block
     */
    @Volatile
    private var events: List<MyEvent> = listOf()
    private val eventsLock = Any()
    private var lastRefreshWasSucceeded = true

    override fun eventReminderTriggered(event: MyEvent) {
        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        var body = simpleDateFormat.format(Date(event.startUNIXTime))
        simpleDateFormat = SimpleDateFormat(" - hh:mm")
        body += simpleDateFormat.format(Date(event.endUNIXTime))

        view.showInfiniteNotification(
                event.title,
                body,
                "Open on web"
        ) { _: Notification, _: String ->
            openURLInDefaultBrowser(event.htmlLink)
        }
    }

    override fun statusIconClicked() {
        view.showPopupMenu()
    }

    override fun eventPopupItemClicked(indexOf: Int) {
        synchronized(eventsLock) {
            openURLInDefaultBrowser(events[indexOf].htmlLink)
        }
    }

    override fun logoutButtonClicked() {
        googleCalendarManager.removeCredentialsFolder()
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
        openURLInDefaultBrowser("https://calendar.google.com/calendar/r")
    }

    @Synchronized
    private fun refresh(byExplicitRefreshButtonClick: Boolean = false) {
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
            } else if (byExplicitRefreshButtonClick || lastRefreshWasSucceeded) {
                view.showNotification("Error", "Unable to connect to Google Calendar")
            }
            if (byExplicitRefreshButtonClick) {
                view.refreshButtonState = RefreshButtonState.NORMAL
            }
            lastRefreshWasSucceeded = events != null
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
        val refreshDaemon = Thread {
            while (true) {
                refresh()
                Thread.sleep(settings.refreshFrequencyInMinutes * 60 * 1000)
            }
        }
        refreshDaemon.isDaemon = true
        refreshDaemon.start()
    }
}