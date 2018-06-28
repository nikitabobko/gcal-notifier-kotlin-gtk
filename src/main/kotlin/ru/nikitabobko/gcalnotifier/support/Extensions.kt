package ru.nikitabobko.gcalnotifier.support

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.EventDateTime

val EventDateTime.timeIfAvaliableOrDate: DateTime
    get() {
        return dateTime ?: date
    }
