package ru.nikitabobko.gcalnotifier

import junit.framework.TestCase
import ru.nikitabobko.gcalnotifier.support.today
import ru.nikitabobko.gcalnotifier.support.tomorrow

class MyEventTests : TestCase() {

    fun testDateTimeString() {
        doTest("Today", System.currentTimeMillis())
        doTest("Today", today.time)
        doTest("Tomorrow", tomorrow.time)
    }

    private fun doTest(expected: String, actualTimeInMillis: Long) {
        assertEquals(expected, createEvent("title", actualTimeInMillis, emptyList()).dateTimeString().split(" ")[0])
    }
}
