package bobko.gcalnotifier

import junit.framework.TestCase
import org.mockito.Mockito.mock
import bobko.gcalnotifier.settings.Settings
import bobko.gcalnotifier.support.TimeProviderImpl

class MyEventTest : TestCase() {

  fun `test MyEvent_dateTimeString`() {
    doTest("Today", System.currentTimeMillis())
    doTest("Today", TimeProviderImpl.today.time)
    doTest("Tomorrow", TimeProviderImpl.tomorrow.time)
  }

  private fun doTest(expected: String, actualTimeInMillis: Long) {
    assertEquals(expected, createEvent("title", actualTimeInMillis).dateString(TimeProviderImpl, mock(Settings::class.java)))
  }
}
