package bobko.gcalnotifier.controller

import bobko.gcalnotifier.model.MyCalendarListEntry
import bobko.gcalnotifier.model.MyEvent
import bobko.gcalnotifier.model.MyEventReminderMethod
import bobko.gcalnotifier.support.UserDataManager
import bobko.gcalnotifier.util.TimeProvider
import bobko.gcalnotifier.util.percentOf
import bobko.gcalnotifier.util.seconds
import bobko.gcalnotifier.util.weakRef
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

  fun registerEventReminderTriggeredHandler(handler: (MyEvent) -> Unit)
}

class EventReminderTrackerImpl(private val userDataManager: UserDataManager,
                               private val timeProvider: TimeProvider) : EventReminderTracker {
  private var lastNotifiedEventUNIXTime: Long = timeProvider.currentTimeMillis
  private var nextEventsToNotify: List<MyEvent> = listOf()
  private var nextEventsToNotifyUNIXTime: Long? = null
  private val upcomingEventsAndUserCalendarsLock = Any()
  private var upcomingEvents: List<MyEvent> by weakRef {
    userDataManager.restoreEventsList()?.toList() ?: emptyList()
  }
  private var userCalendarList: List<MyCalendarListEntry> by weakRef {
    userDataManager.restoreUsersCalendarList()?.toList() ?: emptyList()
  }
  private val eventTrackerDaemonLock = Any()
  @Volatile
  private var eventTrackerDaemon: Thread? = null
  private var eventReminderHandler: ((MyEvent) -> Unit)? = null

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

  override fun registerEventReminderTriggeredHandler(handler: (MyEvent) -> Unit) {
    eventReminderHandler = handler
  }

  private fun buildEventTrackerThread(): Thread = thread(isDaemon = true, start = false) {
    while (true) {
      var doContinue = false
      synchronized(eventTrackerDaemonLock) {
        val currentTimeMillis = timeProvider.currentTimeMillis
        initNextEventsToNotify()
        if (nextEventsToNotify.isEmpty() || nextEventsToNotifyUNIXTime == null) {
          eventTrackerDaemon = null
          return@thread
        }
        val epsilon: Long = minOf(
          30.seconds,
          PERCENT_ACCURACY.percentOf(nextEventsToNotify.minOf { it.startUNIXTime } - nextEventsToNotifyUNIXTime!!)
            .takeIf { it > 0 }
            ?: Long.MAX_VALUE
        )
        if (nextEventsToNotifyUNIXTime!! - epsilon < currentTimeMillis) {
          for (event in nextEventsToNotify) {
            eventReminderHandler?.invoke(event)
          }
          lastNotifiedEventUNIXTime = nextEventsToNotifyUNIXTime!!

          nextEventsToNotify = listOf()
          nextEventsToNotifyUNIXTime = null
          doContinue = true
        }
      }
      if (doContinue) continue
      try {
        val maxOf = maxOf(nextEventsToNotifyUNIXTime!! - timeProvider.currentTimeMillis, 0L)
        Thread.sleep(maxOf)
      } catch (ignored: InterruptedException) {
      }
    }
  }

  private fun MyEvent.calculateNextToNotifyTime(): Long? = this.getReminders(userCalendarList)
    ?.filter { it.method == MyEventReminderMethod.POPUP }
    ?.mapNotNull { eventReminder -> eventReminder.milliseconds?.let { this.startUNIXTime - it } }
    ?.filter { it > lastNotifiedEventUNIXTime }
    ?.minOrNull()

  private fun initNextEventsToNotify() = synchronized(upcomingEventsAndUserCalendarsLock) {
    val candidates = upcomingEvents.mapNotNull { event ->
      event.calculateNextToNotifyTime()?.let { Pair(event, it) }
    }

    nextEventsToNotifyUNIXTime = candidates.minByOrNull { it.second }?.second
    nextEventsToNotify = candidates.filter { it.second == nextEventsToNotifyUNIXTime }.map { it.first }
  }

  companion object {
    const val PERCENT_ACCURACY = 10
  }
}
