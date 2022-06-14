package bobko.gcalnotifier.view.gtk

import junit.framework.TestCase

class IconResourceTest : TestCase() {
  fun `test icon resource is available`() {
    assertNotNull(IconResourceTest::class.java.getResource(ICON_RESOURCE))
  }
}
