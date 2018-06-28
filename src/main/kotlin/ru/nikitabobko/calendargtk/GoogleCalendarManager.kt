package ru.nikitabobko.calendargtk

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import ru.nikitabobko.calendargtk.support.authorizeWorkaround
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.Events
import ru.nikitabobko.calendargtk.support.APPLICATION_NAME
import ru.nikitabobko.calendargtk.support.timeIfAvaliableOrDate
import sun.awt.Mutex
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.Comparator

val googleCalendarManager: GoogleCalendarManager = GoogleCalendarManagerImpl()
private val USER_HOME_FOLDER = System.getProperty("user.home")
private val CREDENTIALS_FOLDER: String = "$USER_HOME_FOLDER/.config/$APPLICATION_NAME/credentials"
private val JSON_FACTORY = JacksonFactory.getDefaultInstance()
private const val CLIENT_SECRET_DIR = "/client_secret.json"
private val SCOPES: List<String> = Collections.singletonList(CalendarScopes.CALENDAR_READONLY)

interface GoogleCalendarManager {
    fun getCredentials(): Credential
    fun getUpcomingEventsAsync(onRefreshedListener: (events: List<Event>?) -> Unit)
    fun getUpcomingEventsAsync(calendarId: String, onRefreshedListener: (events: List<Event>?) -> Unit)
    fun getUserCalendarListAsync(
            onReceivedUserCalendarListListener: (calendarList: List<CalendarListEntry>?) -> Unit
    )
    fun removeCredentialsFolder()
}

class GoogleCalendarManagerImpl : GoogleCalendarManager {
    override fun removeCredentialsFolder() {
        File(CREDENTIALS_FOLDER).deleteRecursively()
    }

    @Volatile
    private var _service: Calendar? = null
    private val service: Calendar
        get() {
            if (_service == null) {
                _service = buildService()
            }
            return _service!!
        }
    private val mutex: Mutex = Mutex()

    private fun getUpcomingEvents(calendarId: String): List<Event>? {
        return try {
            val now = DateTime(System.currentTimeMillis())
            val tomorrowTemp = Date.from(
                    LocalDate.now().plusDays(settings.daysAhead)
                            .atStartOfDay(ZoneId.systemDefault()).toInstant()
            ) // fuck you api designer
            val tomorrow = DateTime(tomorrowTemp)

            val events: Events = service.events().list(calendarId)
                    .setTimeMin(now)
                    .setTimeMax(tomorrow)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
            events.items
        } catch (ex: Exception) {
            _service = null
            null
        }
    }

    override fun getUpcomingEventsAsync(
            calendarId: String,
            onRefreshedListener: (events: List<Event>?) -> Unit
    ) {
        Thread(Runnable {
            mutex.lock()
            onRefreshedListener(getUpcomingEvents(calendarId))
            mutex.unlock()
        }).start()
    }

    override fun getUpcomingEventsAsync(onRefreshedListener: (List<Event>?) -> Unit) {
        Thread(Runnable {
            var retList: List<Event>? = null
            try {
                mutex.lock()
                val list = mutableListOf<Event>()
                val calendars: List<CalendarListEntry> = getUserCalendarList() ?: return@Runnable
                for (calendar: CalendarListEntry in calendars) {
                    val events: List<Event> = getUpcomingEvents(calendar.id) ?: return@Runnable
                    list.addAll(events)
                }
                list.sortWith(Comparator { a: Event, b: Event ->
                    val x: Long = a.start.timeIfAvaliableOrDate.value
                    val y: Long = b.start.timeIfAvaliableOrDate.value
                    if (x != y) return@Comparator (x - y).toInt()
                    return@Comparator a.summary.compareTo(b.summary)
                })
                retList = list
            } finally {
                onRefreshedListener(retList)
                mutex.unlock()
            }
        }).start()
    }

    override fun getUserCalendarListAsync(
            onReceivedUserCalendarListListener: (calendarList: List<CalendarListEntry>?) -> Unit
    ) {
        Thread(Runnable {
            mutex.lock()
            onReceivedUserCalendarListListener(getUserCalendarList())
            mutex.unlock()
        }).start()
    }

    private fun getUserCalendarList(): List<CalendarListEntry>? {
        return try {
            service.calendarList().list().execute().items
        } catch (ex: Exception) {
            _service = null
            null
        }
    }

    private fun buildService(): Calendar {
        // Build a new authorized API client service.
        val netHttpTransport: NetHttpTransport =
                GoogleNetHttpTransport.newTrustedTransport()
        return Calendar.Builder(
                netHttpTransport,
                JSON_FACTORY,
                getCredentials(netHttpTransport)
        ).setApplicationName(APPLICATION_NAME).build()
    }

    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        // Load client secrets.
        val `in`: InputStream = GoogleCalendarManager::class.java.getResourceAsStream(CLIENT_SECRET_DIR)
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(FileDataStoreFactory(File(CREDENTIALS_FOLDER)))
                .setAccessType("offline")
                .build()
        val authorizationCodeInstalledApp = AuthorizationCodeInstalledApp(flow, LocalServerReceiver())
        return authorizationCodeInstalledApp.authorizeWorkaround("user")
    }

    override fun getCredentials(): Credential {
        return getCredentials(GoogleNetHttpTransport.newTrustedTransport())
    }
}
