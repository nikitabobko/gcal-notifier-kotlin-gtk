package ru.nikitabobko.calendargtk

import com.google.api.services.calendar.model.Event
import ru.nikitabobko.calendargtk.support.timeIfAvaliableOrDate
import java.util.*

/**
 * Tracks upcoming events for notifying user about them
 */
interface EventTracker {
    var upcomingEvents: List<Event>
}

class EventTrackerImpl(private val controller: Controller) : EventTracker{
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
                if (nextEventToNotify == null || nextEventToNotifyUNIXTime == null) {
                    initNextEventToNotify()
                    if (nextEventToNotify == null) {
                        break
                    }
                } else if (nextEventToNotifyUNIXTime!! - EPS < System.currentTimeMillis()) {
                    controller.eventReminderTriggered(nextEventToNotify!!)
                    lastNotifiedEvent = nextEventToNotify
                    lastNotifiedEventUNIXTime = nextEventToNotifyUNIXTime

                    nextEventToNotify = null
                    nextEventToNotifyUNIXTime = null
                    continue
                }
                try {
                    Thread.sleep(nextEventToNotifyUNIXTime!! - System.currentTimeMillis())
                } catch (ex: InterruptedException) {
                    // Thread was interrupted, let's check whether nextEventToNotify changed
                    initNextEventToNotify()
                }
            }
        }
        thread.isDaemon = true
        return thread
    }

    private fun initNextEventToNotify() {
        var curTime = Date(Long.MAX_VALUE)
        var curEvent: Event? = null
        val cal = java.util.Calendar.getInstance()

        for (event: Event in upcomingEvents) {
            val reminders = event.reminders
            if (reminders.useDefault) {

            } else if (reminders.overrides != null) {
                for (eventReminder in reminders.overrides.filter {
                    eventReminder -> eventReminder.method == "popup"
                }) {
                    cal.time = Date(event.start.timeIfAvaliableOrDate.value)
                    cal.add(java.util.Calendar.MINUTE, -eventReminder.minutes)
                    if (
                            cal.time <= curTime &&
                            (lastNotifiedEvent == null || lastNotifiedEventUNIXTime == null
                                    || (cal.time.time > lastNotifiedEventUNIXTime!! ||
                                    cal.time.time == lastNotifiedEventUNIXTime!! &&
                                    event != lastNotifiedEvent))
                    ) {
                        curTime = cal.time
                        curEvent = event
                    }
                }
            }
        }
        nextEventToNotify = curEvent
        if (curEvent != null) {
            nextEventToNotifyUNIXTime = curTime.time
        }
    }
}
