package ru.nikitabobko.gcalnotifier

import ru.nikitabobko.gcalnotifier.support.tomorrow
import kotlin.test.Test
import kotlin.test.assertEquals

class MyEventTests {

    @Test
    fun dateTimeStringTest() {
        assertEquals("Today", createEvent("title", System.currentTimeMillis(), listOf()).dateTimeString().split(" ")[0])
        assertEquals("Tomorrow", createEvent("title", tomorrow.time, listOf()).dateTimeString().split(" ")[0])
    }
}