package bobko.gcalnotifier

import junit.framework.TestCase
import org.mockito.Mockito.mock
import bobko.gcalnotifier.settings.Settings
import bobko.gcalnotifier.support.UtilsImpl

class MyEventTest : TestCase() {

  fun `test MyEvent_dateTimeString`() {
    doTest("Today", System.currentTimeMillis())
    doTest("Today", UtilsImpl.today.time)
    doTest("Tomorrow", UtilsImpl.tomorrow.time)
  }

  private fun doTest(expected: String, actualTimeInMillis: Long) {
    assertEquals(expected, createEvent("title", actualTimeInMillis).dateString(UtilsImpl, mock(Settings::class.java)))
  }
}
