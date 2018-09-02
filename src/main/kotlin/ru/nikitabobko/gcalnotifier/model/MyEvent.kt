package ru.nikitabobko.gcalnotifier.model

import com.google.api.services.calendar.model.Colors
import com.google.api.services.calendar.model.Event
import ru.nikitabobko.gcalnotifier.support.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Internal representation of [Event]
 */
data class MyEvent(val title: String?, val startUNIXTime: Long, val endUNIXTime: Long,
                   val reminders: MyReminders?, val calendarId: String?, val htmlLink: String,
                   val isAllDayEvent: Boolean) {
    /**
     * Internal representation of [Event.Reminders]
     */
    data class MyReminders(val useDefault: Boolean, val overrides: List<MyEventReminder>?)

    fun dateTimeString(): String {
        val eventStart = Date(startUNIXTime)
        var dateTime = when(eventStart) {
            in today until tomorrow -> "Today"
            in tomorrow until theDayAfterTomorrow -> "Tomorrow"
            else -> SimpleDateFormat("yyyy/MM/dd").format(eventStart)
        }
        if (!isAllDayEvent) {
            dateTime += SimpleDateFormat(" HH:mm").format(eventStart)
            dateTime += SimpleDateFormat(" - HH:mm").format(Date(endUNIXTime))
        }
        return dateTime
    }
}

/**
 * Convert to internal representation
 */
fun Event.toInternal(): MyEvent {
    return MyEvent(
            title = summary,
            startUNIXTime = (start.dateTime ?: start.date).value,
            endUNIXTime = (end.dateTime ?: end.date).value,
            reminders = reminders?.let { reminders: Event.Reminders ->
                MyEvent.MyReminders(
                        useDefault = reminders.useDefault,
                        overrides = reminders.overrides?.map { it.toInternal() }
                )
            },
            calendarId = organizer?.email,
            htmlLink = htmlLink,
            isAllDayEvent = start.date != null
    )
}
