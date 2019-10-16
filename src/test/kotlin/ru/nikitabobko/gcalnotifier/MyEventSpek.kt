package ru.nikitabobko.gcalnotifier

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.support.UtilsImpl
import kotlin.test.assertEquals

object Foo : Spek({
  describe("foo") {
    it("test") {

    }
  }
})

object MyEventSpek : Spek({
  describe("foo") {
    it("test") {

    }
  }

  describe("${MyEvent::dateTimeString.name} return value") {
    val timeProviders = listOf(System::currentTimeMillis, UtilsImpl.today::getTime, UtilsImpl.tomorrow::getTime)
    val answers = listOf("Today", "Today", "Tomorrow")

    for ((actualTimeInMillisProvider, dateTimeStringExpected) in timeProviders.zip(answers)) {
      val actualDateTimeReturnValue by memoized {
        createEvent("title", actualTimeInMillisProvider.call()).dateTimeString(UtilsImpl).split("•")[1].trim()
      }

      it("equals $dateTimeStringExpected for \"$actualTimeInMillisProvider\"") {
        assertEquals(dateTimeStringExpected, actualDateTimeReturnValue)
      }
    }
  }
})
