package ru.nikitabobko.gcalnotifier

import junit.framework.TestCase
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.invocation.InvocationOnMock
import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.support.*
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger
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
      createEvent("10, 0.5", 10.seconds, createReminder(0), createReminder(9.seconds + 500))
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
    val eventTrackerDaemon = trackerWrapper.tracker.daemonThread
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
    val count = AtomicInteger(0)

    val tracker = createEventReminderTrackerImpl(mockController { event: MyEvent ->
      when (count.get()) {
        0 -> assertEquals("0", event.title)
        1 -> assertEquals("1", event.title)
        else -> fail()
      }
      count.getAndIncrement()
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

    assertEquals(2, count.get())
  }

  fun `test calendar reminder simple`() {
    val calendarId = "calendarId"
    doTest(events = listOf(
      createEvent("10", 20.minutes, createCalendarReminder(), calendarId)
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
      createEvent("10, 15", 20.minutes, createCalendarReminder(), calendarId)
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
      createEvent("5", 10.minutes, createReminder(10.minutes - 5.seconds)),
      createEvent("10, -15", 20.minutes, createCalendarReminder(), calendarId1),
      createEvent("10, 25", 20.minutes + 15.seconds, createCalendarReminder(), calendarId1),
      createEvent("15", 10.minutes + 20.seconds, createCalendarReminder(), calendarId2),
      createEvent("-5", 10.minutes, createCalendarReminder(), calendarId2)
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
      createEvent("doesn't matter title", eventTime, createReminder(eventNotification))
    )
    val events2 = arrayOf(
      createEvent("doesn't matter title 1", eventTime, createReminder(eventNotification)),
      createEvent("doesn't matter title 2", eventTime + 30.minutes, createReminder(eventNotification + 30.minutes))
    )

    for (events in listOf(events1, events2)) {
      // setup
      val controller = ControllerWithSwapableEventReminderTriggeredHandler.create {
        fail("You shouldn't notify too early for small notifications!")
      }

      val tracker: EventReminderTrackerImpl = createEventReminderTrackerImpl(
        controller,
        events,
        emptyArray())
      FakeUtils.currentTimeMillis = eventTime - eventNotification -
        EventReminderTrackerImpl.PERCENT_ACCURACY.percentOf(eventNotification) - 1.seconds
      // action
      tracker.newDataCame(events.toList(), emptyList())
      // assert
      checkAsyncAssertion { assertEquals(Thread.State.TIMED_WAITING, tracker.daemonThread!!.state) }

      // setup
      controller.eventReminderTriggeredHandler = {}
      // action
      FakeUtils.currentTimeMillis += 2.seconds
      tracker.daemonThread!!.interrupt()
      Thread.sleep(2.seconds)
      // assert
      checkAsyncAssertion { Mockito.verify(controller).eventReminderTriggered(events.first()) }
    }
  }

  private data class EventReminderTrackerWrapper(val tracker: EventReminderTracker,
                                                 val controller: ControllerWithSwapableEventReminderTriggeredHandler)

  private fun doTest(events: List<MyEvent>,
                     calendars: List<MyCalendarListEntry> = listOf(),
                     numberOfTriggers: Int,
                     initTrackerWrapper: EventReminderTrackerWrapper? = null,
                     eventTriggered: ((event: MyEvent, count: Int) -> Unit)? = null): EventReminderTrackerWrapper {
    val count = AtomicInteger(0)

    val controller = (initTrackerWrapper?.controller ?: ControllerWithSwapableEventReminderTriggeredHandler.create()).apply {
      eventReminderTriggeredHandler = { event: MyEvent ->
        eventTriggered?.invoke(event, count.getAndIncrement())
      }
    }

    val tracker = initTrackerWrapper?.tracker
      ?: createEventReminderTrackerImpl(controller, events.toTypedArray(), calendars.toTypedArray())

    tracker.newDataCame(events, calendars)
    checkAsyncAssertion { assertEquals(numberOfTriggers, count.get()) }
    return EventReminderTrackerWrapper(tracker, controller)
  }

  private fun createEventReminderTrackerImpl(controller: Controller,
                                             events: Array<MyEvent>,
                                             calendars: Array<MyCalendarListEntry>): EventReminderTrackerImpl {
    return EventReminderTrackerImpl(
      controller,
      mock(UserDataManager::class.java).apply {
        whenCalled(this.restoreEventsList()).thenReturn(events)
        whenCalled(this.restoreUsersCalendarList()).thenReturn(calendars)
      },
      FakeUtils)
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

abstract class ControllerWithSwapableEventReminderTriggeredHandler : Controller {
  var eventReminderTriggeredHandler: (event: MyEvent) -> Unit = {}

  companion object {
    fun create(handler: (event: MyEvent) -> Unit = {}): ControllerWithSwapableEventReminderTriggeredHandler {
      return mock(ControllerWithSwapableEventReminderTriggeredHandler::class.java).apply {
        eventReminderTriggeredHandler = handler
        whenCalled(this.eventReminderTriggered(any())).thenAnswer { invocation: InvocationOnMock ->
          eventReminderTriggeredHandler(invocation.arguments[0] as MyEvent)
        }
      }
    }
  }
}

private fun mockController(eventReminderTriggered: (event: MyEvent) -> Unit): Controller {
  return mock(Controller::class.java).apply {
    whenCalled(this.eventReminderTriggered(any())).thenAnswer { invocation: InvocationOnMock ->
      eventReminderTriggered(invocation.arguments[0] as MyEvent)
    }
  }
}
