package ru.nikitabobko.calendargtk.support

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.EventDateTime

val EventDateTime.timeIfAvaliableOrDate: DateTime
    get() {
        return dateTime ?: date
    }

fun AuthorizationCodeInstalledApp.authorizeWorkaround(userId: String): Credential {
    // Workaround: this code copied from authorize(userId) method with
    // onAuthorization(authorizationUrl); replaced with
    // openURLInDefaultBrowser(authorizationUrl.build())
    // If we allow real onAuthorization(authorizationUrl) perform it'd lead program to crash
    // as this method calls some AWT functions which cannot be intersected with GTK
    try {
        val credential = flow.loadCredential(userId)
        if (credential != null && (credential.refreshToken != null ||
                        credential.expiresInSeconds == null ||
                        credential.expiresInSeconds > 60)) {
            return credential
        }
        // open in browser
        val redirectUri = receiver.redirectUri
        val authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri)
        openURLInDefaultBrowser(authorizationUrl.build())
        // receive authorization code and exchange it for an access token
        val code = receiver.waitForCode()
        val response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute()
        // store credential and return it
        return flow.createAndStoreCredential(response, userId)
    } finally {
        receiver.stop()
    }
}
