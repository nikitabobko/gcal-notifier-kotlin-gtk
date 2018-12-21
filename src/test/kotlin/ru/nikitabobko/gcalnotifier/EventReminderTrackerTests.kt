import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.createEvent
import ru.nikitabobko.gcalnotifier.createReminder
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.support.*
import java.util.concurrent.CyclicBarrier
import kotlin.concurrent.thread
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.test.*

open class EmptyFakeController : Controller {
    override fun applicationStarted(): Unit = TODO("not implemented")

    override fun openGoogleCalendarOnWebButtonClicked(): Unit = TODO("not implemented")

    override fun statusIconClicked(): Unit = TODO("not implemented")

    override fun quitClicked(): Unit = TODO("not implemented")

    override fun refreshButtonClicked(): Unit = TODO("not implemented")

    override fun settingsButtonClicked(): Unit = TODO("not implemented")

    override fun logoutButtonClicked(): Unit = TODO("not implemented")

    override fun eventPopupItemClicked(event: MyEvent): Unit = TODO("not implemented")

    override fun eventReminderTriggered(event: MyEvent): Unit = TODO("not implemented")
}

open class EmptyLocalDataManager : LocalDataManager {
    override val googleCalendarCredentialsDirPath: String
        get() = TODO("not implemented")

    override fun safeEventsList(events: Array<MyEvent>): Unit = TODO("not implemented")

    override fun restoreEventsList(): Array<MyEvent>? = TODO("not implemented")

    override fun safeUsersCalendarList(calendarList: Array<MyCalendarListEntry>) = TODO("not implemented")

    override fun restoreUsersCalendarList(): Array<MyCalendarListEntry>? = TODO("not implemented")

    override fun safe(events: Array<MyEvent>, calendarList: Array<MyCalendarListEntry>): Unit = TODO("not implemented")

    override fun removeAllData(): Unit = TODO("not implemented")

}

class EventReminderTrackerTests {
    @Test
    fun simpleTest() {
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
    fun simpleTest2() {
        val startUNIXTime = System.currentTimeMillis() + 10.seconds
        doTest(events = listOf(
                createEvent("title", startUNIXTime, listOf(createReminder(0))),
                createEvent("not-valid", startUNIXTime, listOf(createReminder(30.seconds))),
                createEvent("title2", startUNIXTime, listOf(createReminder(0), createReminder(2.seconds)))
        ), numberOfTriggers = 3) { event: MyEvent, count: Int ->
            when (count) {
                0       -> assertEquals("title2", event.title)
                in 1..2 -> assertTrue("title" == event.title || "title2" == event.title)
                else    -> fail()
            }
        }
    }

    @Test
    fun simpleTest3() {
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
    fun eventTrackerDaemonIsSleepingTest(): Unit = repeat(4) {
        val trackerWrapper: EventReminderTrackerWrapper = doTest(events = listOf(
                createEvent("title", System.currentTimeMillis() + 60.seconds, listOf(createReminder(0.minutes)))
        ), numberOfTriggers = 0)
        val eventTrackerDaemon = trackerWrapper.tracker.javaClass.declaredFields
                .find { it.name == "eventTrackerDaemon" }!!
                .apply { isAccessible = true }
                .get(trackerWrapper.tracker) as Thread?
        assertNotEquals(null, eventTrackerDaemon)
        Thread.yield()
        assertEquals(Thread.State.TIMED_WAITING, eventTrackerDaemon!!.state)
    }

    @Test
    fun lastNotifiedIsConsideredTest() {
        val lastNotifiedUNIXTime = System.currentTimeMillis() + 3.seconds
        val trackerWrapper: EventReminderTrackerWrapper = doTest(events = listOf(
                createEvent("title", lastNotifiedUNIXTime, listOf(createReminder(0)))
        ), numberOfTriggers = 1) { event: MyEvent, count: Int ->
            if (count == 0) {
                assertEquals("title", event.title)
            } else {
                println(event)
                fail()
            }
        }
        Thread.sleep(10.seconds)
        val actualLastNotifiedEventUNIXTime = trackerWrapper.tracker.javaClass.declaredFields
                .find { it.name == "lastNotifiedEventUNIXTime" }!!
                .apply { isAccessible = true }
                .get(trackerWrapper.tracker) as Long?
        assertEquals(lastNotifiedUNIXTime, actualLastNotifiedEventUNIXTime)
        val start = System.currentTimeMillis()
        doTest(events = listOf(
                createEvent("title", start - 2.seconds, listOf(createReminder(0))),
                createEvent("title", start, listOf(createReminder(2.seconds))),
                createEvent("title2", start - 1.seconds, listOf(createReminder(0))),
                createEvent("title2", start, listOf(createReminder(1.seconds))),
                createEvent("ignore", start - 10.seconds, listOf(createReminder(0))),
                createEvent("ignore", start, listOf(createReminder(10.seconds)))
        ), numberOfTriggers = 4, initTrackerWrapper = trackerWrapper) { event: MyEvent, count: Int ->
            when {
                count <= 1    -> assertEquals("title", event.title)
                count in 2..3 -> assertEquals("title2", event.title)
                else          -> fail()
            }
        }
    }

    @Test
    fun newDataCameRaceConditionTest() {
        val events = listOf(
                createEvent("0", System.currentTimeMillis() + 1.seconds, listOf(createReminder(0))),
                createEvent("1", System.currentTimeMillis() + 10.seconds, listOf(createReminder(0)))
        )
        var count = 0
        val tracker = createEventReminderTrackerImpl(object : EmptyFakeController() {
            override fun eventReminderTriggered(event: MyEvent) {
                when (count) {
                    0    -> assertEquals("0", event.title)
                    1    -> assertEquals("1", event.title)
                    else -> fail()
                }
                count++
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
        Thread.yield()
        barrier.await()
        threads.forEach { it.join() }

        waitFor { count == 2 }
        assertEquals(2, count)
    }

    private class ControllerProvider(var controller: Controller) : ReadOnlyProperty<Any?, Controller> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Controller = controller
    }

    private data class EventReminderTrackerWrapper(val tracker: EventReminderTracker, val controllerProvider: ControllerProvider)

    private fun doTest(events: List<MyEvent>, calendars: List<MyCalendarListEntry> = listOf(),
                       numberOfTriggers: Int, initTrackerWrapper: EventReminderTrackerWrapper? = null,
                       eventTriggered: ((event: MyEvent, count: Int) -> Unit)? = null): EventReminderTrackerWrapper {
        var count = 0
        val fakeController = object : EmptyFakeController() {
            override fun eventReminderTriggered(event: MyEvent) {
                eventTriggered?.invoke(event, count++)
            }
        }

        val controllerProvider = initTrackerWrapper?.controllerProvider
                ?.also { it.controller = fakeController } ?: ControllerProvider(fakeController)

        val trackerWrapper = initTrackerWrapper?.tracker
                ?: createEventReminderTrackerImpl(controllerProvider, events.toTypedArray(), calendars.toTypedArray())

        trackerWrapper.newDataCame(events, calendars)
        waitFor { count == numberOfTriggers }
        assertEquals(numberOfTriggers, count)
        return EventReminderTrackerWrapper(trackerWrapper, controllerProvider)
    }

    private fun createEventReminderTrackerImpl(controllerProvider: ReadOnlyProperty<Any?, Controller>,
                                               events: Array<MyEvent>, calendars: Array<MyCalendarListEntry>): EventReminderTrackerImpl {
        return EventReminderTrackerImpl(object : FactoryForEventReminderTracker {
            override val localDataManager: ReadOnlyProperty<Any?, LocalDataManager> = object : ReadOnlyProperty<Any?, LocalDataManager> {
                override fun getValue(thisRef: Any?, property: KProperty<*>): LocalDataManager = object : EmptyLocalDataManager() {
                    override fun restoreEventsList(): Array<MyEvent>? = events

                    override fun restoreUsersCalendarList(): Array<MyCalendarListEntry>? = calendars
                }
            }
            override val controller: ReadOnlyProperty<Any?, Controller> = controllerProvider
        })
    }

    private fun createEventReminderTrackerImpl(controller: Controller, events: Array<MyEvent>,
                                               calendars: Array<MyCalendarListEntry>): EventReminderTrackerImpl {
        return createEventReminderTrackerImpl(
                object : ReadOnlyProperty<Any?, Controller> {
                    override fun getValue(thisRef: Any?, property: KProperty<*>) = controller
                },
                events,
                calendars)
    }

    private inline fun waitFor(maxTime: Long = 10.seconds,callback: () -> Boolean) {
        var count = 0
        do {
            Thread.yield()
            Thread.sleep(1.seconds)
            count++
        } while (!callback() && count <= maxTime/1.seconds)
    }
}
