package ru.nikitabobko.gcalnotifier.support

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.nikitabobko.gcalnotifier.APPLICATION_NAME
import ru.nikitabobko.gcalnotifier.model.EventsCalendarsPair
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.model.toInternal
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
private const val CLIENT_SECRET_DIR = "/client_secret.json"
private val SCOPES: List<String> = Collections.singletonList(CalendarScopes.CALENDAR_READONLY)

/**
 * Performs Google Calendar API calls
 */
interface GoogleCalendarManager {
  suspend fun fetchUpcomingEventsAsync(): EventsCalendarsPair?
}

class GoogleCalendarManagerImpl(private val openURLInDefaultBrowser: (url: String) -> Unit,
                                private val utils: Utils,
                                localDataManager: Provider<LocalDataManager>) : GoogleCalendarManager {
  private val localDataManager: LocalDataManager by localDataManager
  @Volatile
  private var _service: Calendar? = null
  private val service: Calendar
    get(): Calendar {
      return _service ?: buildService().also { _service = it }
    }

  private fun fetchUpcomingEventsForCalendar(calendarId: String): List<MyEvent>? = try {
    val cal: java.util.Calendar = java.util.Calendar.getInstance()
    cal.time = Date(utils.currentTimeMillis)
    cal.add(java.util.Calendar.MINUTE, -30)
    val start = DateTime(cal.time)
    cal.add(java.util.Calendar.MINUTE, 30)
    // Look 8 weeks ahead
    cal.add(java.util.Calendar.WEEK_OF_YEAR, 8)
    val end = DateTime(cal.time)

    val events: Events = service.events().list(calendarId)
      .setTimeMin(start)
      .setTimeMax(end)
      .setOrderBy("startTime")
      .setSingleEvents(true)
      .execute()
    events.items.map { it.toInternal(calendarId) }
  } catch (ex: IOException) {
    _service = null
    null
  }

  override suspend fun fetchUpcomingEventsAsync(): EventsCalendarsPair? = withContext(Dispatchers.IO) {
    val calendars = fetchUserCalendars()
    val events = calendars
      ?.mapNotNull { fetchUpcomingEventsForCalendar(it.id) }
      ?.flatten()
      ?.sortedWith(compareBy({ it.startUNIXTime }, { it.title }))
    return@withContext EventsCalendarsPair(events ?: return@withContext null, calendars)
  }

  private fun fetchUserCalendars(): List<MyCalendarListEntry>? = try {
    service.calendarList().list().execute().items.map { it.toInternal() }
  } catch (ex: IOException) {
    _service = null
    null
  }

  private fun buildService(): Calendar = GoogleNetHttpTransport.newTrustedTransport().let {
    Calendar.Builder(it, JSON_FACTORY, getCredentials(it)).setApplicationName(APPLICATION_NAME).build()
  }

  @Throws(IOException::class)
  private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
    // Load client secrets.
    val `in`: InputStream = GoogleCalendarManager::class.java.getResourceAsStream(CLIENT_SECRET_DIR)
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

    // Build flow and trigger user authorization request.
    val flow = GoogleAuthorizationCodeFlow.Builder(
      HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory(FileDataStoreFactory(
        File(localDataManager.googleCalendarCredentialsDirPath)))
      .setAccessType("offline")
      .build()
    val authorizationCodeInstalledApp = AuthorizationCodeInstalledAppHack(
      flow,
      LocalServerReceiver(),
      openURLInDefaultBrowser
    )
    return authorizationCodeInstalledApp.authorize("user")
  }
}
