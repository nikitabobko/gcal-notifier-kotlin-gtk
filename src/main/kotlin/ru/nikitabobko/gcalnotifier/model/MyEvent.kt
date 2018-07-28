package ru.nikitabobko.gcalnotifier.model

import com.google.api.services.calendar.model.Event
import ru.nikitabobko.gcalnotifier.support.toInternal

/**
 * Internal representation of [Event]
 */
data class MyEvent(val title: String?, val startUNIXTime: Long, val endUNIXTime: Long,
              val reminders: MyReminders?, val calendarId: String?, val htmlLink: String, val isAllDayEvent: Boolean) {
    /**
     * Internal representation of [Event.Reminders]
     */
    data class MyReminders(val useDefault: Boolean, val overrides: List<MyEventReminder>?)
}

/**
 * Convert to internal representation
 */
fun Event.toInternal(): MyEvent {
    return MyEvent(
            title = summary,
            startUNIXTime = (start.dateTime ?: start.date).value,
            endUNIXTime = (end.dateTime ?: end.date).value,
            reminders = reminders?.let {
                MyEvent.MyReminders(
                        useDefault = reminders.useDefault,
                        overrides = reminders.overrides?.toInternal()
                )
            },
            calendarId = organizer?.email,
            htmlLink = htmlLink,
            isAllDayEvent = start.date != null
    )
}
