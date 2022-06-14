package bobko.gcalnotifier.model

import junit.framework.TestCase
import org.mockito.Mockito.mock
import bobko.gcalnotifier.settings.Settings
import bobko.gcalnotifier.test.createOneHourEvent
import bobko.gcalnotifier.util.TimeProviderImpl

class MyEventTest : TestCase() {

  fun `test MyEvent_dateTimeString`() {
    doTest("Today", System.currentTimeMillis())
    doTest("Today", TimeProviderImpl.today.time)
    doTest("Tomorrow", TimeProviderImpl.tomorrow.time)
  }

  private fun doTest(expected: String, actualTimeInMillis: Long) {
    assertEquals(expected, createOneHourEvent("title", actualTimeInMillis).dateString(TimeProviderImpl, mock(Settings::class.java)))
  }
}
