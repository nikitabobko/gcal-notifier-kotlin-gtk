package bobko.gcalnotifier.controller

import bobko.gcalnotifier.model.MyEvent
import bobko.gcalnotifier.controller.EventReminderTracker

/**
 * Controls app flow. All decisions are made by it.
 */
interface Controller {
  /**
   * Notify [Controller] that application started. Should be called by [bobko.gcalnotifier.view.View].
   */
  fun applicationStarted()

  /**
   * Notify [Controller] that user clicked button "Open Google Calendar on web".
   * Should be called by [bobko.gcalnotifier.view.View].
   */
  fun openGoogleCalendarOnWebButtonClicked()

  /**
   * Notify [Controller] that user clicked status icon on system tray.
   * Should be called by [bobko.gcalnotifier.view.View].
   */
  fun statusIconClicked()

  /**
   * Notify [Controller] that user clicked "Quit" button. Should be called by [bobko.gcalnotifier.view.View].
   */
  fun quitClicked()

  /**
   * Notify [Controller] that user clicked "Refresh" button. Should be called by [bobko.gcalnotifier.view.View].
   */
  fun refreshButtonClicked()

  /**
   * Notify [Controller] that user clicked "Settings" button. Should be called by [bobko.gcalnotifier.view.View].
   */
  fun settingsButtonClicked()

  /**
   * Notify [Controller] that user clicked "Log out" button. Should be called by [bobko.gcalnotifier.view.View].
   */
  fun logoutButtonClicked()

  /**
   * Notify [Controller] that user clicked on of the events item in popup menu.
   * Should be called by [bobko.gcalnotifier.view.View].
   * @param event which event was clicked
   */
  fun eventPopupItemClicked(event: MyEvent)

  /**
   * Notify [Controller] that it's about time to remind user about event he/she set to be reminded.
   * Should be called by [EventReminderTracker]
   * @param event Event to remind user about it
   */
  fun eventReminderTriggered(event: MyEvent)
}
