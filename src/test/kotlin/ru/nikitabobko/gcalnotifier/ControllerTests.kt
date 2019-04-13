package ru.nikitabobko.gcalnotifier

import junit.framework.TestCase
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import ru.nikitabobko.gcalnotifier.support.asProvider

class ControllerTests : TestCase() {
    fun testRemoveAllDataAtLogout() {
        var removeAllDataCalled = false
        val controller = ControllerImpl(
                EmptyView().asProvider(),
                object : EmptyLocalDataManager() {
                    override fun removeAllData() {
                        removeAllDataCalled = true
                    }
                }.asProvider(),
                EmptyGoogleCalendarManager().asProvider(),
                EmptyEventReminderTracker().asProvider(),
                FakeUtils)
        controller.logoutButtonClicked()
        assertTrue(removeAllDataCalled)
    }
}
