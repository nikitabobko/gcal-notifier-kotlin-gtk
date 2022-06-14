package bobko.gcalnotifier.util

import junit.framework.TestCase

class ApplicationVersionTest : TestCase() {
  fun `test fetching APPLICATION_VERSION doesn't throw exception and version is not empty`() {
    assertTrue(APPLICATION_VERSION.isNotEmpty())
  }
}
