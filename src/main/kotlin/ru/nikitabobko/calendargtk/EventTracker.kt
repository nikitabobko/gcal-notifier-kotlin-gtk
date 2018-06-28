package ru.nikitabobko.calendargtk

import com.google.api.services.calendar.model.Event

/**
 * Tracks upcoming events for notifying user about them
 */
interface EventTracker {
    var upcomingEvents: List<Event>
}

class EventTrackerImpl(val view: View) : EventTracker{
    override var upcomingEvents: List<Event> = listOf()
        set(value) {
            field = value
            eventTrackerThread.interrupt()
        }
    private val eventTrackerThread: Thread = Thread(Runnable {
        while(true) {
            for (event: Event in upcomingEvents) {
                val reminders = event.reminders
                val a = 4
            }
            try {
                Thread.sleep(10*60*1000) // 10 minutes
            } catch (ex: InterruptedException) {

            }
        }
    })

    init {
        eventTrackerThread.start()
    }
}