package bobko.gcalnotifier.util

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver
import java.util.*

val USER_HOME_FOLDER: String = System.getProperty("user.home")!!

const val APPLICATION_NAME = "gcal-notifier-kotlin-gtk"

val APPLICATION_VERSION: String =
  object {}::class.java.getResourceAsStream("/bobko/gcalnotifier/util/res/version.txt")!!
    .bufferedReader()
    .use {
      it.readLine()!!
    }

val UI_THREAD_ID = Thread.currentThread().id

private fun Date.plusDays(days: Int): Date {
  val cal = Calendar.getInstance()
  cal.time = this
  cal.add(Calendar.DAY_OF_YEAR, days)
  return cal.time
}

fun Int.percentOf(value: Long): Long {
  check(this in 0..100)
  return value * this / 100L
}

class AuthorizationCodeInstalledAppHack(flow: AuthorizationCodeFlow,
                                        receiver: VerificationCodeReceiver,
                                        private val openURLInDefaultBrowser: (url: String) -> Unit
) : AuthorizationCodeInstalledApp(flow, receiver) {
  override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl?) {
    // HACK: real onAuthorization method calls some AWT methods which
    // will lead to program crash as long as we use GTK
    openURLInDefaultBrowser(authorizationUrl?.build() ?: return)
  }
}

val Int.seconds: Long
  get() = this * 1000L

val Int.minutes: Long
  get() = this * 1000L * 60L

infix fun Date.until(exclusive: Date): ClosedRange<Date> {
  return this..Date(exclusive.time - 1.seconds)
}

abstract class TimeProvider {
  abstract val currentTimeMillis: Long

  val today: Date
    get() {
      val cal = Calendar.getInstance()
      cal.time = Date(currentTimeMillis)
      cal.set(Calendar.HOUR_OF_DAY, 0)
      cal.set(Calendar.MINUTE, 0)
      cal.set(Calendar.SECOND, 0)
      cal.set(Calendar.MILLISECOND, 0)
      return cal.time
    }

  val tomorrow: Date
    get() = today.plusDays(1)

  val theDayAfterTomorrow: Date
    get() = today.plusDays(2)
}

object TimeProviderImpl : TimeProvider() {
  override val currentTimeMillis: Long
    get() = System.currentTimeMillis()
}
