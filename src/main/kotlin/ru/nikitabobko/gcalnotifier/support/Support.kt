package ru.nikitabobko.gcalnotifier.support

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver
import java.util.*

val USER_HOME_FOLDER = System.getProperty("user.home")!!

private fun Date.plusDays(days: Int): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.add(Calendar.DAY_OF_YEAR, days)
    return cal.time
}

val today: Date
    get() {
        val cal = Calendar.getInstance()
        cal.time = Date(System.currentTimeMillis())
        cal.set(Calendar.HOUR_OF_DAY, 0)
        return cal.time
    }

val tomorrow: Date
    get() = today.plusDays(1)

val theDayAfterTomorrow: Date
    get() = today.plusDays(2)

class AuthorizationCodeInstalledAppHack(
        flow: AuthorizationCodeFlow,
        receiver: VerificationCodeReceiver,
        private val openURLInDefaultBrowser: (url: String) -> Unit
) : AuthorizationCodeInstalledApp(flow, receiver) {
    override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl?) {
        // HACK: real onAuthorization method calls some AWT methods which
        // will lead to program crash as long as we use GTK
        openURLInDefaultBrowser(authorizationUrl?.build() ?: return)
    }
}

infix fun Date.until(tomorrow: Date): ExclusiveDateRange {
    return ExclusiveDateRange(this, tomorrow)
}

class ExclusiveDateRange(private val fromInclusively: Date, private val toExclusively: Date) {
    operator fun contains(a: Date): Boolean {
        return a >= fromInclusively && a < toExclusively
    }
}