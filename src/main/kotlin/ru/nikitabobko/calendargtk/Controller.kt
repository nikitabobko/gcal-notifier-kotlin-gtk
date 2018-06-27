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
}

class ControllerImpl : Controller {
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
            if (events == null) {
                view.showNotification("Error", "Unable to connect to Google Calendar")
            } else {
                view.update(events)
            }

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