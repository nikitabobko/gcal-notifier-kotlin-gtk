package bobko.gcalnotifier

import junit.framework.TestCase

class ApplicationIdentificationTest : TestCase() {
  fun `test fetching APPLICATION_VERSION doesn't throw exception and version is not empty`() {
    assertTrue(APPLICATION_VERSION.isNotEmpty())
  }
}
