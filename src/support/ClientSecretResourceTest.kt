package bobko.gcalnotifier.support

import junit.framework.TestCase

class ClientSecretResourceTest : TestCase() {
  fun `test client secret resource is available`() {
    assertNotNull(ClientSecretResourceTest::class.java.getResource(CLIENT_SECRET_JSON))
  }
}
