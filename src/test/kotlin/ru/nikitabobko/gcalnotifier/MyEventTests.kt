package ru.nikitabobko.gcalnotifier

import ru.nikitabobko.gcalnotifier.support.tomorrow
import ru.nikitabobko.gcalnotifier.support.today
import kotlin.test.Test
import kotlin.test.assertEquals

class MyEventTests {

    @Test
    fun dateTimeStringTest() {
        doTest("Today", System.currentTimeMillis())
        doTest("Today", today.time)
        doTest("Tomorrow", tomorrow.time)
    }

    private fun doTest(expected: String, actualTimeInMillis: Long) {
        assertEquals(expected, createEvent("title", actualTimeInMillis, emptyList()).dateTimeString().split(" ")[0])
    }
}
