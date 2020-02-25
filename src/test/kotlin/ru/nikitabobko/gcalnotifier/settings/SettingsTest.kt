package ru.nikitabobko.gcalnotifier.settings

import junit.framework.TestCase
import ru.nikitabobko.gcalnotifier.support.FileReaderWriter
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class SettingsTest : TestCase() {

  fun `test simple default config`() = doTestAndReadFromAnyProperty("""
    refreshFrequencyInMinutes: 5
    maxNumberOfEventsToShowInPopupMenu: 10
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
  """.trimIndent(), """
    refreshFrequencyInMinutes: 5
    maxNumberOfEventsToShowInPopupMenu: 10
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
  """.trimIndent())

  fun `test completely custom settings are preserved`() = doTestAndReadFromAnyProperty("""
    refreshFrequencyInMinutes: 100
    maxNumberOfEventsToShowInPopupMenu: 65
    dateFormat: dd yyyy
    timeFormat: foo
  """.trimIndent(), """
    refreshFrequencyInMinutes: 100
    maxNumberOfEventsToShowInPopupMenu: 65
    dateFormat: dd yyyy
    timeFormat: foo
  """.trimIndent())

  fun `test missing setting item is restored after validation`() = doTestAndReadFromAnyProperty("""
    maxNumberOfEventsToShowInPopupMenu: 10
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
  """.trimIndent(), """
    maxNumberOfEventsToShowInPopupMenu: 10
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
    refreshFrequencyInMinutes: 5
  """.trimIndent())

  fun `test refreshFrequencyInMinutes is validated to be at minimum 1`() = doTestAndReadFromAnyProperty("""
    refreshFrequencyInMinutes: 0
    maxNumberOfEventsToShowInPopupMenu: 10
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
  """.trimIndent(), """
    refreshFrequencyInMinutes: 1
    maxNumberOfEventsToShowInPopupMenu: 10
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
  """.trimIndent())

  fun `test duplicated key value pair is reduced`() = doTestAndReadFromAnyProperty("""
    refreshFrequencyInMinutes: 5
    refreshFrequencyInMinutes: 15
    maxNumberOfEventsToShowInPopupMenu: 10
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
  """.trimIndent(), """
    refreshFrequencyInMinutes: 5
    maxNumberOfEventsToShowInPopupMenu: 10
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
  """.trimIndent())

  fun `test remove key value pairs which are not exist`() = doTestAndReadFromAnyProperty("""
    refreshFrequencyInMinutes: 5
    maxNumberOfEventsToShowInPopupMenu: 10
    foo: 1
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
  """.trimIndent(), """
    refreshFrequencyInMinutes: 5
    maxNumberOfEventsToShowInPopupMenu: 10
    dateFormat: dd MMM yyyy
    timeFormat: HH:mm
  """.trimIndent())

  /**
   * Reading from any property is required for triggering [Settings] object to read for file do validation
   */
  private fun doTestAndReadFromAnyProperty(
    readFromSettingsFile: String?,
    expectedWriteToSettingsFile: String
  ) = doTest(readFromSettingsFile, expectedWriteToSettingsFile) {
    it.maxNumberOfEventsToShowInPopupMenu
  }

  private fun doTest(
    readFromSettingsFile: String?,
    expectedWriteToSettingsFile: String,
    testCases: (Settings) -> Unit
  ) {
    // Atomic because we don't know whether Settings implementation uses threads or not
    val readCounter = AtomicInteger(0)
    val writeCounter = AtomicInteger(0)

    val settings = SettingsImpl(object : FileReaderWriter {
      override fun readFromFile(path: String): String? {
        assertEquals("settings.yml", File(path).name)
        readCounter.incrementAndGet()
        return readFromSettingsFile
      }

      override fun writeToFile(path: String, content: String) {
        assertEquals("settings.yml", File(path).name)
        writeCounter.incrementAndGet()
        assertEquals(expectedWriteToSettingsFile, content)
      }
    }, YamlLikeSettingsFormatParser())
    testCases(settings)
    assertEquals(1, readCounter.get())
    assertEquals(1, writeCounter.get())
  }
}
