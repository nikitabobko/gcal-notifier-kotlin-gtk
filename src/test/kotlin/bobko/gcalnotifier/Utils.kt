package bobko.gcalnotifier

import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import bobko.gcalnotifier.model.MyEvent
import bobko.gcalnotifier.model.MyEventReminder
import bobko.gcalnotifier.model.MyEventReminderMethod
import bobko.gcalnotifier.support.Utils
import bobko.gcalnotifier.support.minutes

fun createEvent(title: String, start: Long, reminders: MyEvent.MyReminders, calendarId: String? = null): MyEvent {
  return MyEvent(title, start, start + 60.minutes, reminders, calendarId = calendarId)
}

fun createEvent(title: String, start: Long, vararg reminders: MyEventReminder, calendarId: String? = null): MyEvent {
  return createEvent(title, start, MyEvent.MyReminders(useDefault = false, overrides = reminders.toList()), calendarId)
}

fun createReminder(milliseconds: Long): MyEventReminder {
  return MyEventReminder(MyEventReminderMethod.POPUP, milliseconds)
}

fun createCalendarReminder(): MyEvent.MyReminders {
  return MyEvent.MyReminders(useDefault = true, overrides = null)
}

object FakeUtils : Utils() {
  @Volatile
  override var currentTimeMillis: Long = 0L

  fun resetTime() {
    currentTimeMillis = 0L
  }
}

fun <T> whenCalled(methodCall: T): OngoingStubbing<T> = Mockito.`when`(methodCall)

/**
 * @see [stackoverflow](https://stackoverflow.com/a/30308199/4359679)
 */
@Suppress("UNCHECKED_CAST")
fun <T> any(): T {
  Mockito.any<T>()
  return null as T
}
