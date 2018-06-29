package ru.nikitabobko.gcalnotifier.support

import com.google.api.client.json.GenericJson
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import ru.nikitabobko.gcalnotifier.model.toInternal

val EventDateTime.timeIfAvaliableOrDate: DateTime
    get() {
        return dateTime ?: date
    }

/**
 * Convert to internal representation
 */
fun <T : GenericJson, R> List<T>.toInternal() : List<R> {
    val ret = mutableListOf<R>()
    for (entry in this) {
        ret.add(entry.toInternal())
    }
    return ret
}

/**
 * Convert to internal representation
 */
fun <T : GenericJson, R> T.toInternal() : R {
    return when(this) {
        is Event -> toInternal() as R
        is CalendarListEntry -> toInternal() as R
        is EventReminder -> toInternal() as R
        else -> throw UnsupportedOperationException()
    }
}