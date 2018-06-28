package ru.nikitabobko.calendargtk

import com.google.api.services.calendar.model.Event
import org.gnome.notify.Notification
import ru.nikitabobko.calendargtk.support.openURLInDefaultBrowser
import ru.nikitabobko.calendargtk.support.timeIfAvaliableOrDate
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
    private val eventTracker: EventTracker = EventTrackerImpl(this)
    @Volatile
    private var events: List<Event> = listOf()
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
        var htmlLink = ""
        synchronized(eventsLock) {
            htmlLink = events[indexOf].htmlLink
        }
        openURLInDefaultBrowser(htmlLink)
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
                eventTracker.upcomingEvents = events
                view.update(events)
                synchronized(eventsLock) {
                    this.events = events
                }
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
        val refreshThread = Thread {
            while (true) {
                refresh()
                Thread.sleep(settings.refreshFrequencyInMinutes * 60 * 1000)
            }
        }
        refreshThread.isDaemon = true
        refreshThread.start()
    }
}