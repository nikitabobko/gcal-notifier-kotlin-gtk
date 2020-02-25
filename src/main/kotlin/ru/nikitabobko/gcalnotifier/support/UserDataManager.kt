package ru.nikitabobko.gcalnotifier.support

import com.google.gson.Gson
import ru.nikitabobko.gcalnotifier.APPLICATION_NAME
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import java.io.*

/**
 * Manages user's local data (Such as list of events and calendars to be able work in offline)
 */
interface UserDataManager {
  /**
   * Path to directory where Google Calendar API stores credentials
   */
  val googleCalendarCredentialsDirPath: String

  fun safeEventsList(events: Array<MyEvent>)

  fun restoreEventsList(): Array<MyEvent>?

  fun safeUsersCalendarList(calendarList: Array<MyCalendarListEntry>)

  fun restoreUsersCalendarList(): Array<MyCalendarListEntry>?

  fun safe(events: Array<MyEvent>, calendarList: Array<MyCalendarListEntry>)

  fun removeAllData()
}

/**
 * Implementation based on JSON
 */
class JsonUserDataManager : UserDataManager {
  private val localDataFolderPath = "$USER_HOME_FOLDER/.config/$APPLICATION_NAME"
  private val eventsListFileLocation = "$localDataFolderPath/events.json"
  private val userCalendarFileLocation = "$localDataFolderPath/calendars.json"
  override val googleCalendarCredentialsDirPath = "$localDataFolderPath/credentials"
  private val gson = Gson()
  private val lock = Any()

  override fun safe(events: Array<MyEvent>, calendarList: Array<MyCalendarListEntry>) {
    safeEventsList(events)
    safeUsersCalendarList(calendarList)
  }

  override fun safeEventsList(events: Array<MyEvent>) = safe(events, eventsListFileLocation)

  override fun safeUsersCalendarList(calendarList: Array<MyCalendarListEntry>) {
    return safe(calendarList, userCalendarFileLocation)
  }

  override fun restoreEventsList(): Array<MyEvent>? {
    return restore(eventsListFileLocation)
  }

  override fun restoreUsersCalendarList(): Array<MyCalendarListEntry>? {
    return restore(userCalendarFileLocation)
  }

  override fun removeAllData() {
    File(localDataFolderPath).deleteRecursively()
  }

  private fun safe(any: Any, fileName: String) = synchronized(lock) {
    try {
      PrintWriter(fileName).use {
        it.println(gson.toJson(any))
      }
    } catch (ex: FileNotFoundException) {
    }
  }

  private inline fun <reified T : Any> restore(fileName: String): T? = synchronized(lock) {
    val file = File(fileName)
    if (!file.exists()) return null

    return try {
      BufferedReader(FileReader(fileName)).use {
        gson.fromJson(it.readLine(), T::class.java)
      }
    } catch (ex: IOException) {
      File(fileName).delete()
      null
    }
  }
}
