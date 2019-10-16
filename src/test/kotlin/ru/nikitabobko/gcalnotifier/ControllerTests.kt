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
    val localDataManager = mock(LocalDataManager::class.java)
    val controller = ControllerImpl(
      mock(View::class.java).asProvider(),
      localDataManager.asProvider(),
      mock(GoogleCalendarManager::class.java).asProvider(),
      mock(EventReminderTracker::class.java).asProvider(),
      FakeUtils)
    controller.logoutButtonClicked()
    verify(localDataManager).removeAllData()
  }

  fun `test getUpcomingEventsAsync when refreshButtonClicked`() {
    val googleCalendarManager = mock(GoogleCalendarManager::class.java)
    val controller = ControllerImpl(
      mock(View::class.java).asProvider(),
      mock(LocalDataManager::class.java).asProvider(),
      googleCalendarManager.asProvider(),
      mock(EventReminderTracker::class.java).asProvider(),
      FakeUtils)
    controller.refreshButtonClicked()
    verify(googleCalendarManager).getUpcomingEventsAsync(any())
  }

  fun `test openUrlInDefaultBrowser when eventPopupItemClicked`() {
    val view = mock(View::class.java)
    val controller = ControllerImpl(
      view.asProvider(),
      mock(LocalDataManager::class.java).asProvider(),
      mock(GoogleCalendarManager::class.java).asProvider(),
      mock(EventReminderTracker::class.java).asProvider(),
      FakeUtils)
    val someHtmlLink = "http://some-html-link"
    val event = MyEvent("title", 0L, 10.seconds, null, htmlLink = someHtmlLink)
    controller.eventPopupItemClicked(event)
    verify(view).openURLInDefaultBrowser(someHtmlLink)
  }

  fun `test View_quit when controller_quitClicked`() {
    val view = mock(View::class.java)
    val controller = ControllerImpl(
      view.asProvider(),
      mock(LocalDataManager::class.java).asProvider(),
      mock(GoogleCalendarManager::class.java).asProvider(),
      mock(EventReminderTracker::class.java).asProvider(),
      FakeUtils)
    controller.quitClicked()
    verify(view).quit()
  }
}
