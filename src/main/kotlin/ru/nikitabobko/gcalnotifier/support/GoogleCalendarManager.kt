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
import ru.nikitabobko.gcalnotifier.model.MyCalendarListEntry
import ru.nikitabobko.gcalnotifier.model.MyEvent
import ru.nikitabobko.gcalnotifier.model.toInternal
import sun.awt.Mutex
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.Comparator

private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
private const val CLIENT_SECRET_DIR = "/client_secret.json"
private val SCOPES: List<String> = Collections.singletonList(CalendarScopes.CALENDAR_READONLY)

/**
 * Performs Google Calendar API calls
 */
interface GoogleCalendarManager {
    fun getUpcomingEventsAsync(
            onRefreshedListener: (events: List<MyEvent>?, calendarList: List<MyCalendarListEntry>?) -> Unit
    )
    fun getUserCalendarListAsync(
            onReceivedUserCalendarListListener: (calendarList: List<MyCalendarListEntry>?) -> Unit
    )
}

class GoogleCalendarManagerImpl(
        private val openURLInDefaultBrowser: (url: String) -> Unit,
        private val localDataManager: LocalDataManager) : GoogleCalendarManager {
    @Volatile
    private var _service: Calendar? = null
    private val service: Calendar
        get(): Calendar {
            return _service ?: buildService().also { _service = it }
        }
    private val mutex: Mutex = Mutex()

    private fun getUpcomingEvents(calendarId: String): List<MyEvent>? = try {
        val cal: java.util.Calendar = java.util.Calendar.getInstance()
        cal.time = Date(System.currentTimeMillis())
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
        events.items.map { it.toInternal() }
    } catch (ex: Throwable) {
        _service = null
        null
    }

    override fun getUpcomingEventsAsync(
            onRefreshedListener: (events: List<MyEvent>?, calendarList: List<MyCalendarListEntry>?) -> Unit) {
        Thread {
            var eventList: List<MyEvent>? = null
            var calendarList: List<MyCalendarListEntry>? = null
            try {
                mutex.lock()
                val list = mutableListOf<MyEvent>()
                val calendars: List<MyCalendarListEntry> = getUserCalendarList() ?: return@Thread
                for (calendar: MyCalendarListEntry in calendars) {
                    val events: List<MyEvent> = getUpcomingEvents(calendar.id) ?: return@Thread
                    list.addAll(events)
                }
                list.sortWith(Comparator { a: MyEvent, b: MyEvent ->
                    val x: Long = a.startUNIXTime
                    val y: Long = b.startUNIXTime
                    if (x != y) return@Comparator if (x > y) 1 else -1
                    if (a.title != null && b.title != null) {
                        return@Comparator a.title.compareTo(b.title)
                    }
                    return@Comparator 0
                })
                eventList = list
                calendarList = calendars
            } finally {
                onRefreshedListener(eventList, calendarList)
                mutex.unlock()
            }
        }.start()
    }

    override fun getUserCalendarListAsync(
            onReceivedUserCalendarListListener: (calendarList: List<MyCalendarListEntry>?) -> Unit
    ) {
        Thread {
            mutex.lock()
            onReceivedUserCalendarListListener(getUserCalendarList())
            mutex.unlock()
        }.start()
    }

    private fun getUserCalendarList(): List<MyCalendarListEntry>? = try {
        service.calendarList().list().execute().items.map { it.toInternal() }
    } catch (ex: Throwable) {
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
