package ru.nikitabobko.calendargtk

import com.google.api.services.calendar.model.Event
import ru.nikitabobko.calendargtk.support.openURLInDefaultBrowser

val controller: Controller = ControllerImpl()

interface Controller {
    fun applicationStarted()
    fun openGoogleCalendarOnWebButtonClicked()
    fun quitClicked()
    fun refreshButtonClicked()
    fun settingsButtonClicked()
    fun logoutButtonClicked()
    fun eventPopupItemClicked(indexOf: Int)
}

class ControllerImpl : Controller {
    private val eventTracker: EventTracker = EventTrackerImpl(view)
    @Volatile
    private var events: List<Event> = listOf()
    private val eventsLock = Any()
    private var lastRefreshWasSucceeded = true

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
        view.refreshButtonState = RefreshButtonState.REFRESHING
        refresh(afterRefreshPerformed = {
            view.refreshButtonState = RefreshButtonState.NORMAL
        })
    }

    override fun quitClicked() {
        view.quit()
    }

    override fun openGoogleCalendarOnWebButtonClicked() {
        openURLInDefaultBrowser("https://calendar.google.com/calendar/r")
    }

    private fun refresh(afterRefreshPerformed: (() -> Unit)? = null) {
        googleCalendarManager.getUpcomingEventsAsync { events: List<Event>? ->
            if (events != null) {
                eventTracker.upcomingEvents = events
                view.update(events)
                synchronized(eventsLock) {
                    this.events = events
                }
            } else if (lastRefreshWasSucceeded) {
                view.showNotification("Error", "Unable to connect to Google Calendar")
            }
            lastRefreshWasSucceeded = events != null

            if (afterRefreshPerformed != null) afterRefreshPerformed()
        }
    }

    override fun applicationStarted() {
        view.showStatusIcon()
        refresh()
        // refresh thread
        Thread(Runnable {
            Thread.sleep(settings.refreshFrequencyInMinutes*60*1000)
            refresh()
        }).start()
    }
}