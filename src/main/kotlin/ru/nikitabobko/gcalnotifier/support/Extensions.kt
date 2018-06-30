package ru.nikitabobko.gcalnotifier.support

import com.google.api.client.json.GenericJson
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.EventReminder
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.model.toInternal

val EventDateTime.timeIfAvaliableOrDate: DateTime
    get() {
        return dateTime ?: date
    }

/**
 * Convert to internal representation
 */
inline fun <T : GenericJson, reified R> List<T>.toInternal() : List<R> {
    val ret = mutableListOf<R>()
    for (entry in this) {
        ret.add(entry.toInternal())
    }
    return ret
}

/**
 * Convert to internal representation
 */
inline fun <T : GenericJson, reified R> T.toInternal() : R {
    return (when(this) {
        is Event -> toInternal()
        is CalendarListEntry -> toInternal()
        is EventReminder -> toInternal()
        else -> Any()
    } as? R) ?: throw UnsupportedOperationException()
}