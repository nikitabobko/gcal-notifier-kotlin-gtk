package ru.nikitabobko.gcalnotifier

import junit.framework.TestCase
import org.mockito.Mockito.mock
import org.mockito.invocation.InvocationOnMock
import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.support.*
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread

class EventReminderTrackerTests : TestCase() {
    override fun setUp() {
        super.setUp()
        FakeUtils.resetTime()
    }

    fun `test simple`() {
        doTest(events = listOf(
                createEvent("10", 10.seconds, createReminder(0)),
                createEvent("10", 10.seconds, createReminder(0)),
                createEvent("10", 10.seconds, createReminder(0)),
                createEvent("20", 20.seconds, createReminder(0))
        ), numberOfTriggers = 4) { event: MyEvent, count: Int ->
            when (count) {
                in 0..2 -> assertEquals("10", event.title)
                3 -> assertEquals("20", event.title)
                else -> fail()
            }
        }
    }

    fun `test simple 2`() {
        doTest(events = listOf(
                createEvent("10", 10.seconds, createReminder(0)),
                createEvent("-20", 10.seconds, createReminder(30.seconds)),
                createEvent("10, 8", 10.seconds, createReminder(0), createReminder(2.seconds))
        ), numberOfTriggers = 3) { event: MyEvent, count: Int ->
            when (count) {
                0 -> assertEquals("10, 8", event.title)
                in 1..2 -> assertTrue("10" == event.title || "10, 8" == event.title)
                else -> fail()
            }
        }
    }

    fun `test assert trigger when notify in 29 seconds`() {
        doTest(events = listOf(
                createEvent("29", 30.minutes, createReminder(30.minutes - 29.seconds))
        ), numberOfTriggers = 1) { event: MyEvent, count: Int ->
            if (count == 0) {
                assertEquals("29", event.title)
            } else {
                fail()
            }
        }
    }

    fun `test EventReminderTracker daemon is sleeping and waiting for upcoming 30 seconds event`(): Unit = repeat(4) {
        val trackerWrapper: EventReminderTrackerWrapper = doTest(events = listOf(
                createEvent("title", 30.seconds, createReminder(0.minutes))
        ), numberOfTriggers = 0)
        val eventTrackerDaemon = trackerWrapper.tracker.javaClass.declaredFields
                .find { it.name == "eventTrackerDaemon" }!!
                .apply { isAccessible = true }
                .get(trackerWrapper.tracker) as Thread?
        assertNotNull(eventTrackerDaemon)
        assertEquals(Thread.State.TIMED_WAITING, eventTrackerDaemon!!.state)
    }

    fun `test last notified is considered`() {
        val lastNotifiedUNIXTime = 3.seconds
        val trackerWrapper: EventReminderTrackerWrapper = doTest(events = listOf(
                createEvent("title0", lastNotifiedUNIXTime, createReminder(0))
        ), numberOfTriggers = 1) { event: MyEvent, count: Int ->
            if (count == 0) {
                assertEquals("title0", event.title)
            } else {
                println(event)
                fail()
            }
        }
        FakeUtils.currentTimeMillis += 10.seconds
        val actualLastNotifiedEventUNIXTime = trackerWrapper.tracker.javaClass.declaredFields
                .find { it.name == "lastNotifiedEventUNIXTime" }!!
                .apply { isAccessible = true }
                .get(trackerWrapper.tracker) as Long?
        assertEquals(lastNotifiedUNIXTime, actualLastNotifiedEventUNIXTime)
        val start = FakeUtils.currentTimeMillis
        doTest(events = listOf(
                createEvent("title", start - 2.seconds, createReminder(0)),
                createEvent("title", start, createReminder(2.seconds)),
                createEvent("title2", start - 1.seconds, createReminder(0)),
                createEvent("title2", start, createReminder(1.seconds)),
                createEvent("ignore", start - 10.seconds, createReminder(0)),
                createEvent("ignore", start, createReminder(10.seconds))
        ), numberOfTriggers = 4, initTrackerWrapper = trackerWrapper) { event: MyEvent, count: Int ->
            when {
                count <= 1 -> assertEquals("title", event.title)
                count in 2..3 -> assertEquals("title2", event.title)
                else -> fail()
            }
        }
    }

    fun `test newDataCame race condition`() = repeat(10) {
        val events = listOf(
                createEvent("0", 1.seconds, createReminder(0)),
                createEvent("1", 10.seconds, createReminder(0))
        )
        var count = 0

        val tracker = createEventReminderTrackerImpl(mock(Controller::class.java).apply {
            whenCalled(this.eventReminderTriggered(any())).thenAnswer { invocation: InvocationOnMock ->
                val event = invocation.arguments[0] as MyEvent
                when (count) {
                    0 -> assertEquals("0", event.title)
                    1 -> assertEquals("1", event.title)
                    else -> fail()
                }
                count++
                return@thenAnswer Unit
            }
        }, events.toTypedArray(), emptyArray())

        val numOfThreads = 5_000
        val barrier = CyclicBarrier(numOfThreads + 1)
        val threads = Array(numOfThreads) {
            thread {
                barrier.await()
                tracker.newDataCame(events, emptyList())
            }
        }
        barrier.await()
        threads.forEach { it.join() }

        waitFor { count == 2 }
        assertEquals(2, count)
    }

    fun `test calendar reminder simple`() {
        val calendarId = "calendarId"
        doTest(events = listOf(
                createEvent("10", 20.seconds, createCalendarReminder(), calendarId)
        ), calendars = listOf(
                MyCalendarListEntry(calendarId, listOf(createReminder(10.seconds)))
        ), numberOfTriggers = 1) { event: MyEvent, count: Int ->
            when (count) {
                0 -> assertEquals("10", event.title)
                else -> fail()
            }
        }
    }

    fun `test calendar multiple reminders`() {
        val calendarId = "calendarId"
        doTest(events = listOf(
                createEvent("10, 15", 20.seconds, createCalendarReminder(), calendarId)
        ), calendars = listOf(
                MyCalendarListEntry(calendarId, listOf(createReminder(10.seconds), createReminder(5.seconds)))
        ), numberOfTriggers = 2) { event: MyEvent, count: Int ->
            when (count) {
                0 -> assertEquals("10, 15", event.title)
                1 -> assertEquals("10, 15", event.title)
                else -> fail()
            }
        }
    }

    fun `test mix events reminders and calendar reminders`() {
        val calendarId1 = "calendarId1"
        val calendarId2 = "calendarId2"
        doTest(events = listOf(
                createEvent("5", 10.seconds, createReminder(5.seconds)),
                createEvent("10, -15", 20.seconds, createCalendarReminder(), calendarId1),
                createEvent("10, 25", 35.seconds, createCalendarReminder(), calendarId1),
                createEvent("15", 30.seconds, createCalendarReminder(), calendarId2),
                createEvent("-5", 10.seconds, createCalendarReminder(), calendarId2)
        ), calendars = listOf(
                MyCalendarListEntry(calendarId1, listOf(createReminder(10.seconds), createReminder(25.seconds))),
                MyCalendarListEntry(calendarId2, listOf(createReminder(15.seconds)))
        ), numberOfTriggers = 5) { event: MyEvent, count: Int ->
            when (count) {
                0 -> assertEquals("5", event.title)
                in 1..2 -> assertTrue("10, -15" == event.title || "10, 25" == event.title)
                3 -> assertEquals("15", event.title)
                4 -> assertEquals("10, 25", event.title)
                else -> fail()
            }
        }
    }

    private class MutableProvider<T>(override var value: T) : Provider<T>

    private fun <T> T.asMutableProvider(): MutableProvider<T> = MutableProvider(this)

    private data class EventReminderTrackerWrapper(val tracker: EventReminderTracker,
                                                   val controllerProvider: Provider<Controller>)

    private fun doTest(events: List<MyEvent>, calendars: List<MyCalendarListEntry> = listOf(),
                       numberOfTriggers: Int, initTrackerWrapper: EventReminderTrackerWrapper? = null,
                       eventTriggered: ((event: MyEvent, count: Int) -> Unit)? = null): EventReminderTrackerWrapper {
        var count = 0

        val fakeController = mock(Controller::class.java).apply {
            whenCalled(this.eventReminderTriggered(any())).thenAnswer { invocation: InvocationOnMock ->
                eventTriggered?.invoke(invocation.arguments[0] as MyEvent, count++)
            }
        }

        val controllerProvider = (initTrackerWrapper?.controllerProvider as? MutableProvider)?.apply {
            value = fakeController
        } ?: fakeController.asMutableProvider()

        val tracker = initTrackerWrapper?.tracker
                ?: createEventReminderTrackerImpl(controllerProvider, events.toTypedArray(), calendars.toTypedArray())

        tracker.newDataCame(events, calendars)
        waitFor { count == numberOfTriggers }
        assertEquals(numberOfTriggers, count)
        return EventReminderTrackerWrapper(tracker, controllerProvider)
    }

    private fun createEventReminderTrackerImpl(controllerProvider: Provider<Controller>,
                                               events: Array<MyEvent>,
                                               calendars: Array<MyCalendarListEntry>): EventReminderTrackerImpl {
        return EventReminderTrackerImpl(
                controllerProvider,
                mock(LocalDataManager::class.java).apply {
                    whenCalled(this.restoreEventsList()).thenReturn(events)
                    whenCalled(this.restoreUsersCalendarList()).thenReturn(calendars)
                }.asProvider(),
                FakeUtils)
    }

    private fun createEventReminderTrackerImpl(controller: Controller, events: Array<MyEvent>,
                                               calendars: Array<MyCalendarListEntry>): EventReminderTrackerImpl {
        return createEventReminderTrackerImpl(controller.asProvider(), events, calendars)
    }

    private inline fun waitFor(maxTime: Long = 10.seconds, callback: () -> Boolean) {
        var count = 0
        do {
            Thread.yield()
            Thread.sleep(1.seconds)
            count++
        } while (!callback() && count <= maxTime / 1.seconds)
    }
}
