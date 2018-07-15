package ru.nikitabobko.gcalnotifier.support

import com.google.gson.Gson
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import java.io.*

/**
 * Manages user's local data (Such as list of events and calendars to be able work in offline)
 */
interface LocalDataManager {
    /**
     * Path to directory where Google Calendar API stores credentials
     * @see removeGoogleCalendarCredentialsDir
     */
    val googleCalendarCredentialsDirPath: String

    /**
     * Remove directory where Google Calendar API stores credentials
     * @see googleCalendarCredentialsDirPath
     */
    fun removeGoogleCalendarCredentialsDir()
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
    override val googleCalendarCredentialsDirPath: String
        get() = "$USER_HOME_FOLDER/.config/$APPLICATION_NAME/credentials"
    private val gson = Gson()
    private val lock = Any()
    companion object {
        private val EVENTS_LIST_FILE_LOCATION =
                "$USER_HOME_FOLDER/.config/$APPLICATION_NAME/events.json"

        private val USER_CALENDAR_LIST_FILE_LOCATION =
                "$USER_HOME_FOLDER/.config/$APPLICATION_NAME/calendars.json"
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

    override fun removeGoogleCalendarCredentialsDir() {
        File(googleCalendarCredentialsDirPath).deleteRecursively()
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
}