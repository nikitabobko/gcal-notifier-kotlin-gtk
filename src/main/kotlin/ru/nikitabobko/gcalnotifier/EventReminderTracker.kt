package ru.nikitabobko.gcalnotifier

import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.model.MyEventReminder
import sun.awt.Mutex
import java.util.*

/**
 * Tracks upcoming reminders for notifying user about them
 */
interface EventReminderTracker {
    fun newDataCame(upcomingEvents: List<MyEvent>, calendars: List<MyCalendarListEntry>)
}

class EventReminderTrackerImpl(private val controller: Controller) : EventReminderTracker {
    private var lastNotifiedEventUNIXTime: Long? = null
    private var nextEventsToNotify: List<MyEvent> = listOf()
    private var nextEventsToNotifyUNIXTime: Long? = null
    companion object {
        /**
         * 30 seconds in milliseconds
         */
        private const val EPS: Long = 30*1000
    }

    private val upcomingEventsAndUserCalendarsMutex = Mutex()
    @Volatile
    private var upcomingEvents: List<MyEvent> = listOf()
    @Volatile
    private var userCalendarList: List<MyCalendarListEntry> = listOf()

    @Synchronized
    override fun newDataCame(upcomingEvents: List<MyEvent>, calendars: List<MyCalendarListEntry>) {
        upcomingEventsAndUserCalendarsMutex.lock()
        this.upcomingEvents = upcomingEvents
        this.userCalendarList = calendars
        upcomingEventsAndUserCalendarsMutex.unlock()
        if (eventTrackerDaemon.isAlive) {
            eventTrackerDaemon.interrupt()
        } else {
            eventTrackerDaemon = buildEventTrackerThread()
            eventTrackerDaemon.start()
        }
    }

    private var eventTrackerDaemon: Thread = buildEventTrackerThread()

    init {
        eventTrackerDaemon.start()
    }

    private fun buildEventTrackerThread(): Thread {
        val thread = Thread {
            while(true) {
                val currentTimeMillis = System.currentTimeMillis()
                if (nextEventsToNotify.isEmpty() || nextEventsToNotifyUNIXTime == null) {
                    initNextEventsToNotify(currentTimeMillis)
                    if (nextEventsToNotify.isEmpty() || nextEventsToNotifyUNIXTime == null) {
                        break
                    }
                }
                if (nextEventsToNotifyUNIXTime!! - EPS < currentTimeMillis) {
                    for (event in nextEventsToNotify) {
                        controller.eventReminderTriggered(event)
                    }
                    lastNotifiedEventUNIXTime = nextEventsToNotifyUNIXTime

                    nextEventsToNotify = listOf()
                    nextEventsToNotifyUNIXTime = null
                    continue
                }
                try {
                    Thread.sleep(nextEventsToNotifyUNIXTime!! - currentTimeMillis)
                } catch (ex: InterruptedException) {
                    // Thread was interrupted, let's check whether nextEventToNotify changed
                    initNextEventsToNotify(System.currentTimeMillis())
                }
            }
        }
        thread.isDaemon = true
        return thread
    }

    private fun initNextEventsToNotify(currentTimeMillis: Long) {
        upcomingEventsAndUserCalendarsMutex.lock()
        var curTime = Date(Long.MAX_VALUE)
        val curEvents: MutableList<MyEvent> = mutableListOf()
        val cal = java.util.Calendar.getInstance()

        for (event: MyEvent in upcomingEvents) {
            val reminders = event.reminders ?: continue
            val remindersList: List<MyEventReminder> = when {

                reminders.useDefault -> userCalendarList.find { calendarListEntry ->
                    calendarListEntry.id == event.calendarId
                }?.defaultReminders ?: listOf()

                reminders.overrides != null -> reminders.overrides

                else -> listOf()

            }.filter { eventReminder -> eventReminder.method == "popup" }

            for (eventReminder in remindersList) {
                cal.time = Date(event.startUNIXTime)
                cal.add(java.util.Calendar.MINUTE, -eventReminder.minutes)
                val reminderTime: Date = cal.time

                val condition = reminderTime <= curTime

                val firstSubCondition = lastNotifiedEventUNIXTime == null &&
                        reminderTime.time >= currentTimeMillis

                val secondSubCondition = lastNotifiedEventUNIXTime != null &&
                        reminderTime.time > lastNotifiedEventUNIXTime!!

                if (condition && (firstSubCondition || secondSubCondition)) {
                    if (reminderTime < curTime) {
                        curEvents.clear()
                    }
                    curEvents.add(event)
                    curTime = reminderTime
                }
            }
        }
        nextEventsToNotify = curEvents
        if (!curEvents.isEmpty()) {
            nextEventsToNotifyUNIXTime = curTime.time
        }
        upcomingEventsAndUserCalendarsMutex.unlock()
    }
}
