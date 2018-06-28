package ru.nikitabobko.calendargtk.support

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver
import org.gnome.gtk.Gtk
import java.net.URI

const val APPLICATION_NAME = "gcal-notifier-kotlin-gtk"

fun openURLInDefaultBrowser(url: String) {
    Gtk.showURI(URI(url))
}

class AuthorizationCodeInstalledAppWorkaround(
        flow: AuthorizationCodeFlow, receiver: VerificationCodeReceiver
) : AuthorizationCodeInstalledApp(flow, receiver) {
    override fun onAuthorization(authorizationUrl: AuthorizationCodeRequestUrl?) {
        // Workaround: real onAuthorization method calls some AWT methods which
        // will lead to program crash as long as we use GTK
        openURLInDefaultBrowser(authorizationUrl?.build() ?: return)
    }
}