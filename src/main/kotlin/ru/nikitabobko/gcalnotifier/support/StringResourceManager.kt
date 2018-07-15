package ru.nikitabobko.gcalnotifier.support

// Not used yet
interface StringResourceManager {
    val openGoogleCalendarOnWeb: String
}

class StringResourceManagerEn : StringResourceManager {
    override val openGoogleCalendarOnWeb: String = "Open Google Calendar on web"
}

class StringResourceManagerRu : StringResourceManager {
    override val openGoogleCalendarOnWeb: String = "Открыть Google Календарь в web"
}