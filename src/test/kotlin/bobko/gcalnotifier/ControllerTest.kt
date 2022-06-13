package bobko.gcalnotifier

import junit.framework.TestCase
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import bobko.gcalnotifier.controller.impl.ControllerImpl
import bobko.gcalnotifier.model.MyEvent
import bobko.gcalnotifier.settings.Settings
import bobko.gcalnotifier.support.*
import bobko.gcalnotifier.view.View

class ControllerTest : TestCase() {
  fun `test removeAllData when logoutButtonClicked`() {
    val localDataManager = mock(UserDataManager::class.java)
    val controller = ControllerImpl.create(
      mock(View::class.java),
      localDataManager,
      mock(GoogleCalendarManager::class.java),
      mock(EventReminderTracker::class.java),
      FakeUtils,
      mock(Settings::class.java))
    controller.logoutButtonClicked()
    verify(localDataManager).removeAllData()
  }

  fun `test getUpcomingEventsAsync when refreshButtonClicked`() {
    val googleCalendarManager = mock(GoogleCalendarManager::class.java)
    val controller = ControllerImpl.create(
      mock(View::class.java),
      mock(UserDataManager::class.java),
      googleCalendarManager,
      mock(EventReminderTracker::class.java),
      FakeUtils,
      mock(Settings::class.java))
    controller.refreshButtonClicked()
    verify(googleCalendarManager).getUpcomingEventsAsync(any())
  }

  fun `test openUrlInDefaultBrowser when eventPopupItemClicked`() {
    val view = mock(View::class.java)
    val controller = ControllerImpl.create(
      view,
      mock(UserDataManager::class.java),
      mock(GoogleCalendarManager::class.java),
      mock(EventReminderTracker::class.java),
      FakeUtils,
      mock(Settings::class.java))
    val someHtmlLink = "http://some-html-link"
    val event = MyEvent("title", 0L, 10.seconds, null, htmlLink = someHtmlLink)
    controller.eventPopupItemClicked(event)
    verify(view).openUrlInDefaultBrowser(someHtmlLink)
  }

  fun `test View_quit when controller_quitClicked`() {
    val view = mock(View::class.java)
    val controller = ControllerImpl.create(
      view,
      mock(UserDataManager::class.java),
      mock(GoogleCalendarManager::class.java),
      mock(EventReminderTracker::class.java),
      FakeUtils,
      mock(Settings::class.java))
    controller.quitClicked()
    verify(view).quit()
  }
}
