package ru.nikitabobko.gcalnotifier

import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventReminder
import ru.nikitabobko.gcalnotifier.support.timeIfAvaliableOrDate
import java.util.*

/**
 * Tracks upcoming events for notifying user about them
 */
interface EventTracker {
    var upcomingEvents: List<Event>
}

class EventTrackerImpl(private val controller: Controller, private val googleCalendarManager: GoogleCalendarManager) : EventTracker{
    private var lastNotifiedEvent: Event? = null
    private var lastNotifiedEventUNIXTime: Long? = null
    private var nextEventToNotify: Event? = null
    private var nextEventToNotifyUNIXTime: Long? = null
    companion object {
        /**
         * 30 seconds in milliseconds
         */
        private const val EPS: Long = 30*1000
    }

    override var upcomingEvents: List<Event> = listOf()
        set(value) {
            field = value
            if (eventTrackerThread.isAlive) {
                eventTrackerThread.interrupt()
            } else {
                eventTrackerThread = buildEventTrackerThread()
                eventTrackerThread.start()
            }
        }
    private var eventTrackerThread: Thread = buildEventTrackerThread()

    init {
        eventTrackerThread.start()
    }

    private fun buildEventTrackerThread(): Thread {
        val thread = Thread {
            while(true) {
                val currentTimeMillis = System.currentTimeMillis()
                if (nextEventToNotify == null || nextEventToNotifyUNIXTime == null) {
                    initNextEventToNotify(currentTimeMillis)
                    if (nextEventToNotify == null || nextEventToNotifyUNIXTime == null) {
                        break
                    }
                }
                if (nextEventToNotifyUNIXTime!! - EPS < currentTimeMillis) {
                    controller.eventReminderTriggered(nextEventToNotify!!)
                    lastNotifiedEvent = nextEventToNotify
                    lastNotifiedEventUNIXTime = nextEventToNotifyUNIXTime

                    nextEventToNotify = null
                    nextEventToNotifyUNIXTime = null
                    continue
                }
                try {
                    Thread.sleep(nextEventToNotifyUNIXTime!! - currentTimeMillis)
                } catch (ex: InterruptedException) {
                    // Thread was interrupted, let's check whether nextEventToNotify changed
                    initNextEventToNotify(System.currentTimeMillis())
                }
            }
        }
        thread.isDaemon = true
        return thread
    }

    private fun initNextEventToNotify(currentTimeMillis: Long) {
        var curTime = Date(Long.MAX_VALUE)
        var curEvent: Event? = null
        val cal = java.util.Calendar.getInstance()

        for (event: Event in upcomingEvents) {
            val reminders = event.reminders
            val remindersList: List<EventReminder> = when {
                reminders.useDefault -> googleCalendarManager.userCalendarList
                        ?.find { calendarListEntry ->
                            calendarListEntry.id == event.organizer?.email
                        }?.defaultReminders ?: listOf()
                reminders.overrides != null -> reminders.overrides
                        .filter { eventReminder -> eventReminder.method == "popup" }
                else -> listOf()
            }
            for (eventReminder in remindersList) {
                cal.time = Date(event.start.timeIfAvaliableOrDate.value)
                cal.add(java.util.Calendar.MINUTE, -eventReminder.minutes)

                val condition = cal.time <= curTime
                val firstSubCondition = lastNotifiedEvent == null && cal.time.time >= currentTimeMillis
                val secondSubCondition = lastNotifiedEvent != null &&
                        (cal.time.time > lastNotifiedEventUNIXTime!! ||
                                cal.time.time == lastNotifiedEventUNIXTime!! &&
                                event != lastNotifiedEvent)

                if (condition && (firstSubCondition || secondSubCondition)) {
                    curTime = cal.time
                    curEvent = event
                }
            }
        }
        nextEventToNotify = curEvent
        if (curEvent != null) {
            nextEventToNotifyUNIXTime = curTime.time
        }
    }
}
