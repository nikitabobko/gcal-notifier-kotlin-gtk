package ru.nikitabobko.gcalnotifier

import junit.framework.TestCase
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.support.*
import ru.nikitabobko.gcalnotifier.view.View

class ControllerTests : TestCase() {
  fun `test removeAllData when logoutButtonClicked`() {
    val localDataManager = mock(UserDataManager::class.java)
    val controller = ControllerImpl(
      mock(View::class.java),
      localDataManager,
      mock(GoogleCalendarManager::class.java),
      mock(EventReminderTracker::class.java),
      FakeUtils)
    controller.logoutButtonClicked()
    verify(localDataManager).removeAllData()
  }

  fun `test getUpcomingEventsAsync when refreshButtonClicked`() {
    val googleCalendarManager = mock(GoogleCalendarManager::class.java)
    val controller = ControllerImpl(
      mock(View::class.java),
      mock(UserDataManager::class.java),
      googleCalendarManager,
      mock(EventReminderTracker::class.java),
      FakeUtils)
    controller.refreshButtonClicked()
    verify(googleCalendarManager).getUpcomingEventsAsync(any())
  }

  fun `test openUrlInDefaultBrowser when eventPopupItemClicked`() {
    val view = mock(View::class.java)
    val controller = ControllerImpl(
      view,
      mock(UserDataManager::class.java),
      mock(GoogleCalendarManager::class.java),
      mock(EventReminderTracker::class.java),
      FakeUtils)
    val someHtmlLink = "http://some-html-link"
    val event = MyEvent("title", 0L, 10.seconds, null, htmlLink = someHtmlLink)
    controller.eventPopupItemClicked(event)
    verify(view).openUrlInDefaultBrowser(someHtmlLink)
  }

  fun `test View_quit when controller_quitClicked`() {
    val view = mock(View::class.java)
    val controller = ControllerImpl(
      view,
      mock(UserDataManager::class.java),
      mock(GoogleCalendarManager::class.java),
      mock(EventReminderTracker::class.java),
      FakeUtils)
    controller.quitClicked()
    verify(view).quit()
  }
}
