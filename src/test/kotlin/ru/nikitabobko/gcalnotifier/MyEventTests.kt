package ru.nikitabobko.gcalnotifier

import junit.framework.TestCase
import ru.nikitabobko.gcalnotifier.support.UtilsImpl

class MyEventTests : TestCase() {

    fun testDateTimeString() {
        doTest("Today", System.currentTimeMillis())
        doTest("Today", UtilsImpl.today.time)
        doTest("Tomorrow", UtilsImpl.tomorrow.time)
    }

    private fun doTest(expected: String, actualTimeInMillis: Long) {
        assertEquals(expected, createEvent("title", actualTimeInMillis).dateTimeString(UtilsImpl).split(" ")[0])
    }
}
