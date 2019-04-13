package ru.nikitabobko.gcalnotifier.model

import com.google.api.services.calendar.model.Event
import ru.nikitabobko.gcalnotifier.support.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Internal representation of [Event]
 */
data class MyEvent(val title: String?, val startUNIXTime: Long, val endUNIXTime: Long,
                   val reminders: MyReminders?, val isAllDayEvent: Boolean = false,
                   val calendarId: String? = null, val htmlLink: String? = null) {
    /**
     * Internal representation of [Event.Reminders]
     */
    data class MyReminders(
            /**
             * Whether the default reminders of the calendar apply to the event.
             */
            val useDefault: Boolean,
            /**
             * If the event doesn't use the default reminders, this lists the reminders specific to the event, or,
             * if not set, indicates that no reminders are set for this event. The maximum number of override
             * reminders is 5.
             */
            val overrides: List<MyEventReminder>?)

    fun dateTimeString(utils: Utils): String {
        val eventStart = Date(startUNIXTime)
        var dateTime = when(eventStart) {
            in utils.today until utils.tomorrow -> "Today"
            in utils.tomorrow until utils.theDayAfterTomorrow -> "Tomorrow"
            else -> SimpleDateFormat("yyyy/MM/dd").format(eventStart)
        }
        if (!isAllDayEvent) {
            dateTime += SimpleDateFormat(" HH:mm").format(eventStart)
            dateTime += SimpleDateFormat(" - HH:mm").format(Date(endUNIXTime))
        }
        return dateTime
    }

    fun getReminders(calendars: List<MyCalendarListEntry>): List<MyEventReminder>? = when {
        reminders?.useDefault == true -> calendars.find { it.id == calendarId }?.defaultReminders
        reminders?.overrides != null -> reminders.overrides
        else -> null
    }
}

/**
 * Convert to internal representation
 */
fun Event.toInternal(calendarId: String): MyEvent {
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
            calendarId = calendarId,
            htmlLink = htmlLink,
            isAllDayEvent = start.date != null
    )
}
