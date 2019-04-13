package ru.nikitabobko.gcalnotifier

import org.gnome.notify.Notification
import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.model.MyEventReminder
import ru.nikitabobko.gcalnotifier.model.MyEventReminderMethod
import ru.nikitabobko.gcalnotifier.support.*
import ru.nikitabobko.gcalnotifier.view.RefreshButtonState
import ru.nikitabobko.gcalnotifier.view.View
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

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

open class EmptyFakeController : Controller {
    override fun applicationStarted(): Unit = TODO("not implemented")

    override fun openGoogleCalendarOnWebButtonClicked(): Unit = TODO("not implemented")

    override fun statusIconClicked(): Unit = TODO("not implemented")

    override fun quitClicked(): Unit = TODO("not implemented")

    override fun refreshButtonClicked(): Unit = TODO("not implemented")

    override fun settingsButtonClicked(): Unit = TODO("not implemented")

    override fun logoutButtonClicked(): Unit = TODO("not implemented")

    override fun eventPopupItemClicked(event: MyEvent): Unit = TODO("not implemented")

    override fun eventReminderTriggered(event: MyEvent): Unit = TODO("not implemented")
}

open class EmptyLocalDataManager : LocalDataManager {
    override val googleCalendarCredentialsDirPath: String
        get() = TODO("not implemented")

    override fun safeEventsList(events: Array<MyEvent>): Unit = TODO("not implemented")

    override fun restoreEventsList(): Array<MyEvent>? = TODO("not implemented")

    override fun safeUsersCalendarList(calendarList: Array<MyCalendarListEntry>) = TODO("not implemented")

    override fun restoreUsersCalendarList(): Array<MyCalendarListEntry>? = TODO("not implemented")

    override fun safe(events: Array<MyEvent>, calendarList: Array<MyCalendarListEntry>): Unit = TODO("not implemented")

    override fun removeAllData(): Unit = TODO("not implemented")
}

open class EmptyView : View {

    override fun showStatusIcon() = TODO("not implemented")

    override fun showSettingsWindow() = TODO("not implemented")

    override fun update(events: List<MyEvent>) = TODO("not implemented")

    override fun quit() { }

    override var refreshButtonState: RefreshButtonState
        get() = TODO("not implemented")
        set(value) {}

    override fun showPopupMenu() = TODO("not implemented")

    override fun showNotification(summary: String, body: String?, actionLabel: String?,
                                  action: ((Notification, String) -> Unit)?) = TODO("not implemented")

    override fun showInfiniteNotification(summary: String, body: String?, actionLabel: String?,
                                          action: ((Notification, String) -> Unit)?) = TODO("not implemented")

    override fun openURLInDefaultBrowser(url: String) = TODO("not implemented")
}

open class EmptyGoogleCalendarManager : GoogleCalendarManager {
    override fun getUpcomingEventsAsync(onRefreshedListener: (events: List<MyEvent>?,
                                                              calendarList: List<MyCalendarListEntry>?) -> Unit)
            = TODO("not implemented")

    override fun getUserCalendarListAsync(
            onReceivedUserCalendarListListener: (calendarList: List<MyCalendarListEntry>?) -> Unit)
            = TODO("not implemented")

}

open class EmptyEventReminderTracker : EventReminderTracker {
    override fun newDataCame(upcomingEvents: List<MyEvent>, calendars: List<MyCalendarListEntry>)
            = TODO("not implemented")
}

object FakeUtils : Utils() {
    override var currentTimeMillis: Long = 0L

    fun resetTime() {
        currentTimeMillis = 0L
    }
}
