package bobko.gcalnotifier.model

import com.google.api.services.calendar.model.CalendarListEntry

/**
 * Internal representation of [CalendarListEntry]
 */
data class MyCalendarListEntry(val id: String, val defaultReminders: List<MyEventReminder>?)

/**
 * Convert to internal representation
 */
fun CalendarListEntry.toInternal(): MyCalendarListEntry {
  return MyCalendarListEntry(
    id = id,
    defaultReminders = defaultReminders?.map { it.toInternal() }
  )
}
