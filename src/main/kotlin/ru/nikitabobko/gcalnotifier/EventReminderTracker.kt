package ru.nikitabobko.gcalnotifier

import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventReminder
import ru.nikitabobko.gcalnotifier.support.timeIfAvaliableOrDate
import java.util.*

/**
 * Tracks upcoming reminders for notifying user about them
 */
interface EventReminderTracker {
    var upcomingEvents: List<Event>
}

class EventReminderTrackerImpl(private val controller: Controller, private val googleCalendarManager: GoogleCalendarManager) : EventReminderTracker{
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

    private val upcomingEventsLock = Any()
    @Volatile
    override var upcomingEvents: List<Event> = listOf()
        get() {
            synchronized(upcomingEventsLock) {
                return field
            }
        }
        set(value) {
            synchronized(upcomingEventsLock) {
                field = value
                if (eventTrackerDaemon.isAlive) {
                    eventTrackerDaemon.interrupt()
                } else {
                    eventTrackerDaemon = buildEventTrackerThread()
                    eventTrackerDaemon.start()
                }
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
        val userCalendarList = googleCalendarManager.userCalendarList
        val upcomingEvents = this.upcomingEvents

        for (event: Event in upcomingEvents) {
            val reminders = event.reminders
            val remindersList: List<EventReminder> = when {
                reminders.useDefault -> userCalendarList?.find { calendarListEntry ->
                            calendarListEntry.id == event.organizer?.email
                        }?.defaultReminders ?: listOf()
                reminders.overrides != null -> reminders.overrides ?: listOf()
                else -> listOf()
            }.filter { eventReminder -> eventReminder.method == "popup" }
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
