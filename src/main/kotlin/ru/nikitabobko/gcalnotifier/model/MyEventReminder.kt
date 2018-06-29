package ru.nikitabobko.gcalnotifier.model

import com.google.api.services.calendar.model.EventReminder

/**
 * Internal representation of [EventReminder]
 */
data class MyEventReminder(val method: String, val minutes: Int)

/**
 * Convert to internal representation
 */
fun EventReminder.toInternal(): MyEventReminder {
    return MyEventReminder(
            method = method,
            minutes = minutes
    )
}