package ru.nikitabobko.gcalnotifier

import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.model.MyEventReminder
import ru.nikitabobko.gcalnotifier.model.MyEventReminderMethod
import ru.nikitabobko.gcalnotifier.support.minutes

fun createEvent(title: String, start: Long, reminders: List<MyEventReminder>): MyEvent {
    return MyEvent(title, start, start + 60.minutes, false, MyEvent.MyReminders(false, reminders))
}

fun createReminder(milliseconds: Long): MyEventReminder {
    return MyEventReminder(MyEventReminderMethod.POPUP, milliseconds)
}
