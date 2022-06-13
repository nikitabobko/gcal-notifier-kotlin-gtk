package bobko.gcalnotifier.controller.impl

import bobko.gcalnotifier.controller.Controller
import bobko.gcalnotifier.model.MyCalendarListEntry
import bobko.gcalnotifier.model.MyEvent
import bobko.gcalnotifier.settings.Settings
import bobko.gcalnotifier.support.EventReminderTracker
import bobko.gcalnotifier.support.GoogleCalendarManager
import bobko.gcalnotifier.support.UserDataManager
import bobko.gcalnotifier.support.TimeProvider
import bobko.gcalnotifier.view.RefreshButtonState
import bobko.gcalnotifier.view.View
import org.gnome.notify.Notification
import kotlin.concurrent.thread

class ControllerImpl private constructor(
  private val view: View,
  private val userDataManager: UserDataManager,
  private val googleCalendarManager: GoogleCalendarManager,
  private val eventReminderTracker: EventReminderTracker,
  private val timeProvider: TimeProvider,
  private val settings: Settings
) : Controller {
  private var notifyAboutRefreshFailures = true

  companion object {
    fun create(view: View,
               userDataManager: UserDataManager,
               googleCalendarManager: GoogleCalendarManager,
               eventReminderTracker: EventReminderTracker,
               timeProvider: TimeProvider,
               settings: Settings
    ): Controller {
      return ControllerImpl(view, userDataManager, googleCalendarManager, eventReminderTracker, timeProvider, settings).also { controller ->
        view.registerController(controller)
        eventReminderTracker.registerEventReminderTriggeredHandler(controller::eventReminderTriggered)
      }
    }
  }

  private fun concatTimeAndDate(time: String?, date: String): String {
    if (time == null) {
      return date
    }
    return "$time • $date"
  }

  override fun eventReminderTriggered(event: MyEvent) {
    view.showInfiniteNotification(
      event.title ?: "",
      concatTimeAndDate(event.timeString(settings), event.dateString(timeProvider, settings)),
      "Open in web"
    ) { _: Notification, _: String ->
      view.openUrlInDefaultBrowser(event.htmlLink ?: return@showInfiniteNotification)
    }
  }

  override fun statusIconClicked() {
    view.showPopupMenu()
  }

  override fun eventPopupItemClicked(event: MyEvent) {
    view.openUrlInDefaultBrowser(event.htmlLink ?: return)
  }

  override fun logoutButtonClicked() {
    userDataManager.removeAllData()
    view.quit()
  }

  override fun settingsButtonClicked() {
    view.openFileInDefaultFileEditor(settings.settingsFilePath)
  }

  override fun refreshButtonClicked() {
    refresh()
  }

  override fun quitClicked() {
    view.quit()
  }

  override fun openGoogleCalendarOnWebButtonClicked() {
    view.openUrlInDefaultBrowser("https://calendar.google.com")
  }

  @Synchronized
  private fun refresh(notifyAboutRefreshFailuresForce: Boolean = true) {
    view.refreshButtonState = RefreshButtonState.REFRESHING
    googleCalendarManager.getUpcomingEventsAsync { events, calendarList ->
      if (events != null && calendarList != null) {
        userDataManager.save(events.toTypedArray(), calendarList.toTypedArray())
        eventReminderTracker.newDataCame(events, calendarList)
        view.update(events)
        notifyAboutRefreshFailures = true
      } else if (notifyAboutRefreshFailuresForce && notifyAboutRefreshFailures) {
        view.showNotification("Error", "Unable to connect to Google Calendar")
        notifyAboutRefreshFailures = false
      }
      view.refreshButtonState = RefreshButtonState.NORMAL
    }
  }

  override fun applicationStarted() {
    view.showStatusIcon()

    // Trying to load saved events
    val events: List<MyEvent> = userDataManager.restoreEventsList()?.toList() ?: listOf()

    val calendars: List<MyCalendarListEntry> = userDataManager.restoreUsersCalendarList()
      ?.toList() ?: listOf()

    view.update(events)
    eventReminderTracker.newDataCame(events, calendars)
    // refresh thread
    thread(isDaemon = true) {
      // If user set up our app to autostart then it would annoy user
      // that "Unable to connect to Google Calendar" if gcal-notifier launches
      // faster than connected to wifi network. So we don't notify user about
      // failed initial refresh.
      refresh(notifyAboutRefreshFailuresForce = false)
      while (true) {
        Thread.sleep((settings.refreshFrequencyInMinutes * 60 * 1000).toLong())
        refresh()
      }
    }
  }
}
