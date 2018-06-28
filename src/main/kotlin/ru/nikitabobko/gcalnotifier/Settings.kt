package ru.nikitabobko.gcalnotifier

val settings = Settings()

class Settings {
    var refreshFrequencyInMinutes: Long = 5L
    var maxNumberOfEventsToShowInPopupMenu: Int = 10
}
