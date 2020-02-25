package ru.nikitabobko.gcalnotifier.support

import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.model.MyEventReminderMethod
import ru.nikitabobko.kotlin.refdelegation.weakRef
import kotlin.concurrent.thread

/**
 * Tracks upcoming reminders for notifying user about them
 */
interface EventReminderTracker {
  /**
   * Notify [EventReminderTracker] that new data came.
   * Usually called by [Controller] after data has been refreshed
   */
  fun newDataCame(upcomingEvents: List<MyEvent>, calendars: List<MyCalendarListEntry>)
}

class EventReminderTrackerImpl(private val controller: Controller,
                               private val localDataManager: LocalDataManager,
                               private val utils: Utils) : EventReminderTracker {
  private var lastNotifiedEventUNIXTime: Long = utils.currentTimeMillis
  private var nextEventsToNotify: List<MyEvent> = listOf()
  private var nextEventsToNotifyUNIXTime: Long? = null
  private val upcomingEventsAndUserCalendarsLock = Any()
  private var upcomingEvents: List<MyEvent> by weakRef {
    localDataManager.restoreEventsList()?.toList() ?: emptyList()
  }
  private var userCalendarList: List<MyCalendarListEntry> by weakRef {
    localDataManager.restoreUsersCalendarList()?.toList() ?: emptyList()
  }
  private val eventTrackerDaemonLock = Any()
  @Volatile
  private var eventTrackerDaemon: Thread? = null

  @Synchronized
  override fun newDataCame(upcomingEvents: List<MyEvent>, calendars: List<MyCalendarListEntry>) {
    synchronized(upcomingEventsAndUserCalendarsLock) {
      this.upcomingEvents = upcomingEvents
      this.userCalendarList = calendars
    }
    synchronized(eventTrackerDaemonLock) {
      if (eventTrackerDaemon?.isAlive == true) {
        eventTrackerDaemon!!.interrupt()
      } else {
        eventTrackerDaemon = buildEventTrackerThread().also { it.start() }
      }
    }
  }

  private fun buildEventTrackerThread(): Thread = thread(isDaemon = true, start = false, priority = Thread.MIN_PRIORITY) {
    while (true) {
      var doContinue = false
      synchronized(eventTrackerDaemonLock) {
        val currentTimeMillis = utils.currentTimeMillis
        initNextEventsToNotify()
        if (nextEventsToNotify.isEmpty() || nextEventsToNotifyUNIXTime == null) {
          eventTrackerDaemon = null
          return@thread
        }
        val epsilon: Long = minOf(
          30.seconds,
          PERCENT_ACCURACY.percentOf(nextEventsToNotify.map { it.startUNIXTime }.min()!! - nextEventsToNotifyUNIXTime!!)
            .takeIf { it > 0 }
            ?: Long.MAX_VALUE
        )
        if (nextEventsToNotifyUNIXTime!! - epsilon < currentTimeMillis) {
          for (event in nextEventsToNotify) {
            controller.eventReminderTriggered(event)
          }
          lastNotifiedEventUNIXTime = nextEventsToNotifyUNIXTime!!

          nextEventsToNotify = listOf()
          nextEventsToNotifyUNIXTime = null
          doContinue = true
        }
      }
      if (doContinue) continue
      try {
        val maxOf = maxOf(nextEventsToNotifyUNIXTime!! - utils.currentTimeMillis, 0L)
        Thread.sleep(maxOf)
      } catch (ignored: InterruptedException) {
      }
    }
  }

  private fun MyEvent.calculateNextToNotifyTime(): Long? = this.getReminders(userCalendarList)
    ?.filter { it.method == MyEventReminderMethod.POPUP }
    ?.mapNotNull { if (it.milliseconds != null) this.startUNIXTime - it.milliseconds else null }
    ?.filter { it > lastNotifiedEventUNIXTime }
    ?.min()

  private fun initNextEventsToNotify() = synchronized(upcomingEventsAndUserCalendarsLock) {
    val candidates = upcomingEvents.mapNotNull { event ->
      event.calculateNextToNotifyTime()?.let { Pair(event, it) }
    }

    nextEventsToNotifyUNIXTime = candidates.minBy { it.second }?.second
    nextEventsToNotify = candidates.filter { it.second == nextEventsToNotifyUNIXTime }.map { it.first }
  }

  companion object {
    const val PERCENT_ACCURACY = 10
  }
}
