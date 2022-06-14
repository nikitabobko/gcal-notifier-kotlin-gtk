package bobko.gcalnotifier.controller

import junit.framework.TestCase
import org.mockito.Mockito.mock
import bobko.gcalnotifier.model.MyCalendarListEntry
import bobko.gcalnotifier.model.MyEvent
import bobko.gcalnotifier.support.*
import bobko.gcalnotifier.test.*
import bobko.gcalnotifier.util.minutes
import bobko.gcalnotifier.util.percentOf
import bobko.gcalnotifier.util.seconds
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class EventReminderTrackerTest : TestCase() {
  override fun setUp() {
    super.setUp()
    FakeTimeProvider.resetTime()
  }

  fun `test simple`() {
    doTest(events = listOf(
      createOneHourEvent("10", 10.seconds, createReminder(0)),
      createOneHourEvent("10", 10.seconds, createReminder(0)),
      createOneHourEvent("10", 10.seconds, createReminder(0)),
      createOneHourEvent("20", 20.seconds, createReminder(0))
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
      createOneHourEvent("10", 10.seconds, createReminder(0)),
      createOneHourEvent("-20", 10.seconds, createReminder(30.seconds)),
      createOneHourEvent("10, 0.5", 10.seconds, createReminder(0), createReminder(9.seconds + 500))
    ), numberOfTriggers = 3) { event: MyEvent, count: Int ->
      when (count) {
        0 -> assertEquals("10, 0.5", event.title)
        in 1..2 -> assertTrue("10" == event.title || "10, 0.5" == event.title)
        else -> fail()
      }
    }
  }

  fun `test assert trigger when notify in 29 seconds`() {
    doTest(events = listOf(
      createOneHourEvent("29", 30.minutes, createReminder(30.minutes - 29.seconds))
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
      createOneHourEvent("title", 30.seconds, createReminder(0.minutes))
    ), numberOfTriggers = 0)
    val eventTrackerDaemon = trackerWrapper.tracker.daemonThread
    assertNotNull(eventTrackerDaemon)
    assertEquals(Thread.State.TIMED_WAITING, eventTrackerDaemon!!.state)
  }

  fun `test last notified is considered`() {
    val lastNotifiedUNIXTime = 3.seconds
    val trackerWrapper: EventReminderTrackerWrapper = doTest(events = listOf(
      createOneHourEvent("title0", lastNotifiedUNIXTime, createReminder(0))
    ), numberOfTriggers = 1) { event: MyEvent, count: Int ->
      if (count == 0) {
        assertEquals("title0", event.title)
      } else {
        println(event)
        fail()
      }
    }
    FakeTimeProvider.currentTimeMillis += 10.seconds
    val actualLastNotifiedEventUNIXTime = trackerWrapper.tracker.javaClass.declaredFields
      .find { it.name == "lastNotifiedEventUNIXTime" }!!
      .apply { isAccessible = true }
      .get(trackerWrapper.tracker) as Long?
    assertEquals(lastNotifiedUNIXTime, actualLastNotifiedEventUNIXTime)
    val start = FakeTimeProvider.currentTimeMillis
    doTest(events = listOf(
      createOneHourEvent("title", start - 2.seconds, createReminder(0)),
      createOneHourEvent("title", start, createReminder(2.seconds)),
      createOneHourEvent("title2", start - 1.seconds, createReminder(0)),
      createOneHourEvent("title2", start, createReminder(1.seconds)),
      createOneHourEvent("ignore", start - 10.seconds, createReminder(0)),
      createOneHourEvent("ignore", start, createReminder(10.seconds))
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
      createOneHourEvent("0", 1.seconds, createReminder(0)),
      createOneHourEvent("1", 10.seconds, createReminder(0))
    )
    val count = AtomicInteger(0)

    val tracker = EventReminderTrackerWrapper({ event: MyEvent ->
      when (count.get()) {
        0 -> assertEquals("0", event.title)
        1 -> assertEquals("1", event.title)
        else -> fail()
      }
      count.getAndIncrement()
    }, events.toTypedArray(), emptyArray()).tracker

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

    assertEquals(2, count.get())
  }

  fun `test calendar reminder simple`() {
    val calendarId = "calendarId"
    doTest(events = listOf(
      createOneHourEvent("10", 20.minutes, createCalendarReminder(), calendarId)
    ), calendars = listOf(
      MyCalendarListEntry(calendarId, listOf(createReminder(20.minutes - 10.seconds)))
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
      createOneHourEvent("10, 15", 20.minutes, createCalendarReminder(), calendarId)
    ), calendars = listOf(
      MyCalendarListEntry(calendarId,
        listOf(createReminder(20.minutes - 10.seconds), createReminder(20.minutes - 5.seconds)))
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
      createOneHourEvent("5", 10.minutes, createReminder(10.minutes - 5.seconds)),
      createOneHourEvent("10, -15", 20.minutes, createCalendarReminder(), calendarId1),
      createOneHourEvent("10, 25", 20.minutes + 15.seconds, createCalendarReminder(), calendarId1),
      createOneHourEvent("15", 10.minutes + 20.seconds, createCalendarReminder(), calendarId2),
      createOneHourEvent("-5", 10.minutes, createCalendarReminder(), calendarId2)
    ), calendars = listOf(
      MyCalendarListEntry(calendarId1,
        listOf(createReminder(20.minutes - 10.seconds), createReminder(20.minutes + 5.seconds))),
      MyCalendarListEntry(calendarId2, listOf(createReminder(10.minutes + 5.seconds)))
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

  fun `test don't notify sooner than EventReminderTrackerImpl#PERCENT_ACCURACY% of notification "pretime"`() {
    val eventTime = 1.minutes
    val eventNotification = 40.seconds

    val events1 = arrayOf(
      createOneHourEvent("doesn't matter title", eventTime, createReminder(eventNotification))
    )
    val events2 = arrayOf(
      createOneHourEvent("doesn't matter title 1", eventTime, createReminder(eventNotification)),
      createOneHourEvent("doesn't matter title 2", eventTime + 30.minutes, createReminder(eventNotification + 30.minutes))
    )

    for (events in listOf(events1, events2)) {
      // setup
      val trackerWrapper = EventReminderTrackerWrapper({
        fail("You shouldn't notify too early for small notifications!")
      }, events, emptyArray())
      FakeTimeProvider.currentTimeMillis = eventTime - eventNotification -
        EventReminderTrackerImpl.PERCENT_ACCURACY.percentOf(eventNotification) - 1.seconds
      // action
      trackerWrapper.tracker.newDataCame(events.toList(), emptyList())
      // assert
      checkAsyncAssertion { assertEquals(Thread.State.TIMED_WAITING, trackerWrapper.tracker.daemonThread!!.state) }

      val counter = AtomicInteger(0)
      // setup
      trackerWrapper.eventReminderTriggeredHandler = { counter.incrementAndGet() }
      // action
      FakeTimeProvider.currentTimeMillis += 2.seconds
      trackerWrapper.tracker.daemonThread!!.interrupt()
      Thread.sleep(2.seconds)
      // assert
      checkAsyncAssertion { assertEquals(events.size, counter.get()) }
    }
  }

  private class EventReminderTrackerWrapper(var eventReminderTriggeredHandler: (MyEvent) -> Unit,
                                            events: Array<MyEvent>,
                                            calendars: Array<MyCalendarListEntry>) {
    var tracker: EventReminderTracker
      private set

    init {
      tracker = EventReminderTrackerImpl(
        mock(UserDataManager::class.java).apply {
          whenCalled(this.restoreEventsList()).thenReturn(events)
          whenCalled(this.restoreUsersCalendarList()).thenReturn(calendars)
        },
        FakeTimeProvider).apply {
        registerEventReminderTriggeredHandler { eventReminderTriggeredHandler(it) }
      }
    }
  }

  private fun doTest(events: List<MyEvent>,
                     calendars: List<MyCalendarListEntry> = listOf(),
                     numberOfTriggers: Int,
                     initTrackerWrapper: EventReminderTrackerWrapper? = null,
                     eventTriggered: ((event: MyEvent, count: Int) -> Unit)? = null): EventReminderTrackerWrapper {
    val count = AtomicInteger(0)

    val trackerWrapper =
      (initTrackerWrapper ?: EventReminderTrackerWrapper({}, events.toTypedArray(), calendars.toTypedArray())).apply {
        eventReminderTriggeredHandler = {
          eventTriggered?.invoke(it, count.getAndIncrement())
        }
      }

    trackerWrapper.tracker.newDataCame(events, calendars)
    checkAsyncAssertion { assertEquals(numberOfTriggers, count.get()) }
    return trackerWrapper
  }

  private fun checkAsyncAssertion(maxTime: Long = 10.seconds, assertion: () -> Unit) {
    var count = 0
    val sleepTimeout = 100L

    // Wait for assertion to become true
    do {
      Thread.yield()
      Thread.sleep(sleepTimeout)
      count++
    } while (!assertion.assertionToPredicate() && count <= maxTime / 1.seconds)
    assertion()

    // Check that assertion is still valid for a while
    do {
      Thread.yield()
      Thread.sleep(sleepTimeout)
      count++
    } while (assertion.assertionToPredicate() && count <= maxTime / 1.seconds)

    assertion()
  }

  private fun (() -> Unit).assertionToPredicate(): Boolean {
    try {
      this()
      return true
    } catch (ex: AssertionError) {
      return false
    }
  }

  private val EventReminderTracker.daemonThread: Thread?
    get() = this.javaClass.getDeclaredField("eventTrackerDaemon")
      .apply { isAccessible = true }
      .get(this) as Thread?
}
