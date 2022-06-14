package bobko.gcalnotifier.test

import bobko.gcalnotifier.model.MyEvent
import bobko.gcalnotifier.model.MyEvent.MyReminders
import bobko.gcalnotifier.model.MyEventReminder
import bobko.gcalnotifier.model.MyEventReminderMethod
import bobko.gcalnotifier.util.TimeProvider
import bobko.gcalnotifier.util.minutes
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing

fun createOneHourEvent(title: String, start: Long, reminders: MyReminders, calendarId: String? = null) =
  MyEvent(title, start, start + 60.minutes, reminders, calendarId = calendarId)

fun createOneHourEvent(title: String, start: Long, vararg reminders: MyEventReminder, calendarId: String? = null) =
  createOneHourEvent(title, start, MyReminders(useDefault = false, overrides = reminders.toList()), calendarId)

fun createReminder(milliseconds: Long): MyEventReminder {
  return MyEventReminder(MyEventReminderMethod.POPUP, milliseconds)
}

fun createCalendarReminder(): MyReminders {
  return MyReminders(useDefault = true, overrides = null)
}

object FakeTimeProvider : TimeProvider() {
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
