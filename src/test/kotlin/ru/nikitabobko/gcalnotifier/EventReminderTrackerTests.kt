import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.model.MyEventReminder
import ru.nikitabobko.gcalnotifier.model.MyEventReminderMethod
import ru.nikitabobko.gcalnotifier.support.EventReminderTracker
import ru.nikitabobko.gcalnotifier.support.EventReminderTrackerFactory
import ru.nikitabobko.gcalnotifier.support.EventReminderTrackerImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

open class EmptyFakeController : Controller {
    override fun applicationStarted() {}

    override fun openGoogleCalendarOnWebButtonClicked() {}

    override fun statusIconClicked() {}

    override fun quitClicked() {}

    override fun refreshButtonClicked() {}

    override fun settingsButtonClicked() {}

    override fun logoutButtonClicked() {}

    override fun eventPopupItemClicked(event: MyEvent) {}

    override fun eventReminderTriggered(event: MyEvent) {}
}

class EventReminderTrackerTests {
    @Test
    fun test1() {
        val startUNIXTime = System.currentTimeMillis() + 10.seconds
        doTest(events = listOf(
                createEvent("title", startUNIXTime, listOf(createReminder(0))),
                createEvent("title", startUNIXTime, listOf(createReminder(0))),
                createEvent("title", startUNIXTime, listOf(createReminder(0))),
                createEvent("title2", startUNIXTime + 1.seconds, listOf(createReminder(0)))
        ), numberOfTriggers = 4) { event: MyEvent, count: Int ->
            when {
                count <= 2 -> assertEquals("title", event.title)
                count == 3 -> assertEquals("title2", event.title)
                else       -> fail()
            }
        }
    }

    @Test
    fun test2() {
        val startUNIXTime = System.currentTimeMillis() + 10.seconds
        doTest(events = listOf(
                createEvent("title", startUNIXTime, listOf(createReminder(0))),
                createEvent("not-valid", startUNIXTime, listOf(createReminder(30.seconds))),
                createEvent("title2", startUNIXTime, listOf(createReminder(0), createReminder(2.seconds)))
        ), numberOfTriggers = 3) { event: MyEvent, count: Int ->
            when {
                count == 0    -> assertEquals("title2", event.title)
                count in 1..2 -> assertTrue("title" == event.title || "title2" == event.title)
                else          -> fail()
            }
        }
    }

    @Test
    fun test4() {
        doTest(events = listOf(
                createEvent("title", System.currentTimeMillis() + 30.minutes, listOf(createReminder(30.minutes - 30.seconds)))
        ), numberOfTriggers = 1) { event: MyEvent, count: Int ->
            if (count == 0) {
                assertEquals("title", event.title)
            } else {
                fail()
            }
        }
    }

    @Test
    fun test3() {
        val tracker = doTest(events = listOf(
                createEvent("title", System.currentTimeMillis() + 3.seconds, listOf(createReminder(0)))
        ), numberOfTriggers = 1) { event: MyEvent, count: Int ->
            if (count == 0) {
                assertEquals("title", event.title)
            } else {
                println(event)
                fail()
            }
            println("part 1 $count")
        }
        Thread.sleep(10.seconds)
        println("hey")
        val start = System.currentTimeMillis()
        doTest(events = listOf(
                createEvent("title2", start - 2.seconds, listOf(createReminder(0))),
                createEvent("title2", start, listOf(createReminder(2.seconds))),
                createEvent("title", start - 1, listOf(createReminder(0))),
                createEvent("title", start, listOf(createReminder(1.seconds))),
                createEvent("ignore", start - 10.seconds, listOf(createReminder(0))),
                createEvent("ignore", start, listOf(createReminder(10.seconds)))
        ), numberOfTriggers = 4, initTracker = tracker) { event: MyEvent, count: Int ->
            when {
                count <= 1    -> assertEquals("title", event.title)
                count in 2..3 -> assertEquals("title2", event.title)
                else          -> fail()
            }
            println("catch")
        }
    }

    private fun doTest(events: List<MyEvent>, calendars: List<MyCalendarListEntry> = listOf(),
                       numberOfTriggers: Int, initTracker: EventReminderTracker? = null,
                       eventTriggered: (event: MyEvent, count: Int) -> Unit): EventReminderTracker {
        var count = 0
        val tracker = initTracker ?: EventReminderTrackerImpl(object : EventReminderTrackerFactory {
            override val controller: Controller = object : EmptyFakeController() {
                override fun eventReminderTriggered(event: MyEvent) {
                    eventTriggered(event, count)
                    count++
                }
            }
        })
        tracker.newDataCame(events, calendars)
        println(count)
        waitFor(message = { "Expected: $numberOfTriggers, Actual: $count" }) { count == numberOfTriggers }
        return tracker
    }

    private inline fun waitFor(noinline message: (() -> String)? = null, callback: () -> Boolean) {
        var count = 0
        do {
            Thread.yield()
            Thread.sleep(1.seconds)
            count++
        } while (!callback() && count <= 10)
        assertTrue(callback(), message = message?.invoke())
    }

    private fun createEvent(title: String, start: Long, reminders: List<MyEventReminder>): MyEvent {
        return MyEvent(title, start, start + 60.minutes, false, MyEvent.MyReminders(false, reminders))
    }

    private fun createReminder(minutes: Long): MyEventReminder {
        return MyEventReminder(MyEventReminderMethod.POPUP, minutes.toInt())
    }
}

private val Int.seconds: Long
    get() = this * 1000L

private val Int.minutes: Long
    get() = this.seconds * 60
