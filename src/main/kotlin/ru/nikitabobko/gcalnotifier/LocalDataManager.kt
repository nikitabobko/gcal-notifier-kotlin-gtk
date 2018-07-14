package ru.nikitabobko.gcalnotifier

import com.google.gson.Gson
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.support.APPLICATION_NAME
import ru.nikitabobko.gcalnotifier.support.USER_HOME_FOLDER
import java.io.*

interface LocalDataManager {
    fun safeEventsList(events: Array<MyEvent>)
    /**
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    fun restoreEventsList(): Array<MyEvent>
    fun safeUsersCalendarList(calendarList: Array<MyCalendarListEntry>)
    /**
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    fun restoreUsersCalendarList(): Array<MyCalendarListEntry>
    fun safe(events: Array<MyEvent>, calendarList: Array<MyCalendarListEntry>)
}

/**
 * Implementation based on JSON
 */
class LocalDataManagerJSON : LocalDataManager {
    private val gson = Gson()
    private val lock = Any()
    companion object {
        private val EVENTS_LIST_FILE_LOCATION =
                "$USER_HOME_FOLDER/.config/$APPLICATION_NAME/events.json"

        private val USER_CALENDAR_LIST_FILE_LOCATION =
                "$USER_HOME_FOLDER/.config/$APPLICATION_NAME/calendars.json"
    }

    private fun safe(any: Any, fileName: String) {
        synchronized(lock) {
            try {
                PrintWriter(fileName).use {
                    it.println(gson.toJson(any))
                }
            } catch (ex: Exception) { }
        }
    }

    private inline fun <reified T : Any> restore(fileName: String) : T? {
        synchronized(lock) {
            val file = File(fileName)
            if (!file.exists()) return null

            return try {
                BufferedReader(FileReader(fileName)).use {
                    gson.fromJson(it.readLine(), T::class.java)
                }
            } catch (ex: Exception) {
                try {
                    File(fileName).delete()
                } catch (ex: Exception) { }
                null
            }
        }
    }

    override fun safe(events: Array<MyEvent>, calendarList: Array<MyCalendarListEntry>) {
        safeEventsList(events)
        safeUsersCalendarList(calendarList)
    }

    override fun safeEventsList(events: Array<MyEvent>) = safe(events, EVENTS_LIST_FILE_LOCATION)

    override fun safeUsersCalendarList(calendarList: Array<MyCalendarListEntry>) =
            safe(calendarList, USER_CALENDAR_LIST_FILE_LOCATION)


    override fun restoreEventsList(): Array<MyEvent> =
            restore(EVENTS_LIST_FILE_LOCATION) ?: throw FileNotFoundException()

    override fun restoreUsersCalendarList(): Array<MyCalendarListEntry> =
            restore(USER_CALENDAR_LIST_FILE_LOCATION) ?: throw FileNotFoundException()
}