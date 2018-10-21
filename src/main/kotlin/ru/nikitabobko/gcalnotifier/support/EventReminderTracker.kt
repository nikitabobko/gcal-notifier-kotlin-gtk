package ru.nikitabobko.gcalnotifier.support

import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.model.MyEventReminderMethod
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

class EventReminderTrackerImpl(factory: EventReminderTrackerFactory) : EventReminderTracker {
    private var lastNotifiedEventUNIXTime: Long? = null
    private var nextEventsToNotify: List<MyEvent> = listOf()
    private var nextEventsToNotifyUNIXTime: Long? = null
    companion object {
        /**
         * 30 seconds in milliseconds
         */
        private const val EPS: Long = 30*1000
    }

    private val upcomingEventsAndUserCalendarsLock = Any()
    @Volatile
    private var upcomingEvents: List<MyEvent> = listOf()
    @Volatile
    private var userCalendarList: List<MyCalendarListEntry> = listOf()

    private val controller: Controller by lazy { factory.controller }

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
            var currentTimeMillis = System.currentTimeMillis()
            synchronized(eventTrackerDaemonLock) {
                currentTimeMillis = System.currentTimeMillis()
                initNextEventsToNotify(currentTimeMillis)
                if (nextEventsToNotify.isEmpty() || nextEventsToNotifyUNIXTime == null) {
                    eventTrackerDaemon = null
                    return@thread
                }
                if (nextEventsToNotifyUNIXTime!! - EPS < currentTimeMillis) {
                    for (event in nextEventsToNotify) {
                        controller.eventReminderTriggered(event)
                    }
                    lastNotifiedEventUNIXTime = nextEventsToNotifyUNIXTime

                    nextEventsToNotify = listOf()
                    nextEventsToNotifyUNIXTime = null
                    doContinue = true
                }
            }
            if (doContinue) continue
            try {
                Thread.sleep(minOf(nextEventsToNotifyUNIXTime!! - currentTimeMillis, 0L))
            } catch (ignored: InterruptedException) { }
        }
    }

    private fun MyEvent.getNextToNotifyTime(currentTimeMillis: Long): Long? {
        return this.getReminders(userCalendarList)
                ?.filter { it.method == MyEventReminderMethod.POPUP }
                ?.mapNotNull { if (it.minutes != null) this.startUNIXTime - it.minutes else null }
                ?.filter { it > (lastNotifiedEventUNIXTime ?: currentTimeMillis) }
                ?.min()
    }

    private fun initNextEventsToNotify(currentTimeMillis: Long) = synchronized(upcomingEventsAndUserCalendarsLock) {
        nextEventsToNotify = upcomingEvents.allMinBy { it.getNextToNotifyTime(currentTimeMillis) ?: Long.MAX_VALUE }
        nextEventsToNotifyUNIXTime = nextEventsToNotify.firstOrNull()?.getNextToNotifyTime(currentTimeMillis)
    }
}
