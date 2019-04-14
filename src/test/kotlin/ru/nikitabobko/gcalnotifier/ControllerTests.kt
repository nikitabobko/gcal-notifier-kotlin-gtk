package ru.nikitabobko.gcalnotifier

import junit.framework.TestCase
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import ru.nikitabobko.gcalnotifier.support.EventReminderTracker
import ru.nikitabobko.gcalnotifier.support.GoogleCalendarManager
import ru.nikitabobko.gcalnotifier.support.LocalDataManager
import ru.nikitabobko.gcalnotifier.support.asProvider
import ru.nikitabobko.gcalnotifier.view.View

class ControllerTests : TestCase() {
    fun testRemoveAllDataAtLogout() {
        val localDataManager = mock(LocalDataManager::class.java)
        val controller = ControllerImpl(
                mock(View::class.java).asProvider(),
                localDataManager.asProvider(),
                mock(GoogleCalendarManager::class.java).asProvider(),
                mock(EventReminderTracker::class.java).asProvider(),
                FakeUtils)
        controller.logoutButtonClicked()
        verify(localDataManager).removeAllData()
    }
}
