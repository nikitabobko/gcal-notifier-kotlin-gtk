package ru.nikitabobko.gcalnotifier

import com.google.api.services.calendar.model.Event
import org.gnome.notify.Notification
import ru.nikitabobko.gcalnotifier.support.openURLInDefaultBrowser
import ru.nikitabobko.gcalnotifier.support.timeIfAvaliableOrDate
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
    fun eventReminderTriggered(event: Event)
}

class ControllerImpl : Controller {
    private val googleCalendarManager = GoogleCalendarManagerImpl()
    private val eventReminderTracker: EventReminderTracker = EventReminderTrackerImpl(this, googleCalendarManager)
    /**
     * Thread safety
     */
    @Volatile
    private var events: List<Event> = listOf()
        get() {
            synchronized(eventsLock) {
                return field
            }
        }
        set(value) {
            synchronized(eventsLock) {
                field = value
            }
        }
    private val eventsLock = Any()
    private var lastRefreshWasSucceeded = true

    override fun eventReminderTriggered(event: Event) {
        var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        var body = simpleDateFormat.format(Date(event.start.timeIfAvaliableOrDate.value))
        simpleDateFormat = SimpleDateFormat(" - hh:mm")
        body += simpleDateFormat.format(Date(event.end.timeIfAvaliableOrDate.value))

        view.showNotification(
                event.summary,
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
        openURLInDefaultBrowser(events[indexOf].htmlLink)
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
        googleCalendarManager.getUpcomingEventsAsync { events: List<Event>? ->
            if (events != null) {
                eventReminderTracker.upcomingEvents = events
                view.update(events)
                this.events = events
            } else if (byExplicitRefreshButtonClick || lastRefreshWasSucceeded){
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