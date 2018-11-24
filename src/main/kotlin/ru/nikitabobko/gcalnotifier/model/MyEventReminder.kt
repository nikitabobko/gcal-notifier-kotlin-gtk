package ru.nikitabobko.gcalnotifier.model

import com.google.api.services.calendar.model.EventReminder
import ru.nikitabobko.gcalnotifier.support.minutes

enum class MyEventReminderMethod {
    EMAIL, SMS, POPUP
}

/**
 * Internal representation of [EventReminder]
 */
data class MyEventReminder(
        /**
         * The method used by this reminder. Possible values are:
         * * [MyEventReminderMethod.EMAIL] - Reminders are sent via email.
         * * [MyEventReminderMethod.SMS] - Reminders are sent via SMS. These are only available for G Suite customers. Requests to set
         *   SMS reminders for other account types are ignored.
         * * [MyEventReminderMethod.POPUP] - Reminders are sent via a UI popup.
         * * `null` - Google Calendar API returned `null` for some reason
         */
        val method: MyEventReminderMethod?,
        /**
         * Number of milliseconds before the start of the event when the reminder should trigger.
         * Valid values are between 0 and 2 419 200 000 (4 weeks in milliseconds).
         */
        val milliseconds: Long?)

/**
 * Convert to internal representation
 */
fun EventReminder.toInternal(): MyEventReminder {
    return MyEventReminder(
            method = when(method) {
                "email" -> MyEventReminderMethod.EMAIL
                "sms" -> MyEventReminderMethod.SMS
                "popup" -> MyEventReminderMethod.POPUP
                else -> null
            },
            milliseconds = minutes.minutes
    )
}