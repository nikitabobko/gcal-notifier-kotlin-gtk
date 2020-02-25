package ru.nikitabobko.gcalnotifier.injector

import ru.nikitabobko.gcalnotifier.UI_THREAD_ID
import ru.nikitabobko.gcalnotifier.controller.Controller
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import ru.nikitabobko.gcalnotifier.injected.getValue
import ru.nikitabobko.gcalnotifier.injected.injectedSingleton
import ru.nikitabobko.gcalnotifier.support.*
import ru.nikitabobko.gcalnotifier.view.View
import ru.nikitabobko.gcalnotifier.view.ViewJavaGnome

/**
 * Makes all members of [Injector] open by default
 */
annotation class InjectorAllOpen

/**
 * Dependency Injection mechanism
 */
@InjectorAllOpen
abstract class Injector {
  val eventReminderTracker: EventReminderTracker by injectedSingleton {
    EventReminderTrackerImpl(controller, userDataManager, utils)
  }

  val userDataManager: UserDataManager by injectedSingleton { JsonUserDataManager() }

  val googleCalendarManager: GoogleCalendarManager by injectedSingleton {
    GoogleCalendarManagerImpl(view::openUrlInDefaultBrowser, utils, userDataManager)
  }

  val view: View by injectedSingleton { ViewJavaGnome(UI_THREAD_ID, controller, utils) }

  val controller: Controller by injectedSingleton {
    ControllerImpl(view, userDataManager, googleCalendarManager, eventReminderTracker, utils)
  }

  val utils: Utils by injectedSingleton {
    UtilsImpl
  }
}
