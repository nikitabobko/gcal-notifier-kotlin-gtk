package ru.nikitabobko.gcalnotifier

import junit.framework.TestCase

class ApplicationIdentificationTests : TestCase() {
  fun `test fetching APPLICATION_VERSION doesn't throw exception and version is not empty`() {
    assert(APPLICATION_VERSION.isNotEmpty())
  }
}
