package ru.nikitabobko.gcalnotifier.controller

import org.gnome.notify.Notification
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.support.*
import ru.nikitabobko.gcalnotifier.view.RefreshButtonState
import ru.nikitabobko.gcalnotifier.view.View
import ru.nikitabobko.gcalnotifier.view.ViewJavaGnome

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
     * @param event which event was clicked
     */
    fun eventPopupItemClicked(event: MyEvent)

    /**
     * Notify [Controller] that it's about time to remind user about event he/she setted to be reminded.
     * Should be called by [EventReminderTracker]
     * @param event Event to remind user about it
     */
    fun eventReminderTriggered(event: MyEvent)
}

class ControllerImpl(private val view: View) : Controller {
    private val localDataManager: LocalDataManager = LocalDataManagerJSON()
    private val googleCalendarManager = GoogleCalendarManagerImpl(
            view::openURLInDefaultBrowser, localDataManager)
    private val eventReminderTracker: EventReminderTracker = EventReminderTrackerImpl(this)
    private var notifyUserAboutRefreshFailures = true

    override fun eventReminderTriggered(event: MyEvent) {
        view.showInfiniteNotification(
                event.title ?: "",
                event.dateTimeString(),
                "Open on web"
        ) { _: Notification, _: String ->
            view.openURLInDefaultBrowser(event.htmlLink)
        }
    }

    override fun statusIconClicked() {
        view.showPopupMenu()
    }

    override fun eventPopupItemClicked(event: MyEvent) {
        view.openURLInDefaultBrowser(event.htmlLink)
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
        view.refreshButtonState = RefreshButtonState.REFRESHING
        googleCalendarManager
                .getUpcomingEventsAsync { events: List<MyEvent>?, calendarList: List<MyCalendarListEntry>? ->
            if (events != null && calendarList != null) {
                localDataManager.safe(events.toTypedArray(), calendarList.toTypedArray())
                eventReminderTracker.newDataCame(events, calendarList)
                view.update(events)
                notifyUserAboutRefreshFailures = true
            } else if (!doNotNotifyAboutRefreshFailureForce &&
                    (byExplicitRefreshButtonClick || notifyUserAboutRefreshFailures)) {
                view.showNotification("Error", "Unable to connect to Google Calendar")
                notifyUserAboutRefreshFailures = false
            }
            view.refreshButtonState = RefreshButtonState.NORMAL
        }
    }

    override fun applicationStarted() {
        view.showStatusIcon()
        // Trying to load saved events
        val events: List<MyEvent> = localDataManager.restoreEventsList()?.toList() ?: listOf()
        val calendars: List<MyCalendarListEntry> = localDataManager.restoreUsersCalendarList()?.toList() ?: listOf()
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
