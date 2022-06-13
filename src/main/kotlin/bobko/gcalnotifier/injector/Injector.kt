package bobko.gcalnotifier.injector

import bobko.gcalnotifier.UI_THREAD_ID
import bobko.gcalnotifier.controller.Controller
import bobko.gcalnotifier.controller.impl.ControllerImpl
import bobko.gcalnotifier.injected.injectedSingleton
import bobko.gcalnotifier.injected.getValue
import bobko.gcalnotifier.settings.Settings
import bobko.gcalnotifier.settings.SettingsFormatParser
import bobko.gcalnotifier.settings.SettingsImpl
import bobko.gcalnotifier.settings.YamlLikeSettingsFormatParser
import bobko.gcalnotifier.support.*
import bobko.gcalnotifier.view.View
import bobko.gcalnotifier.view.gtk.GtkView

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
    EventReminderTrackerImpl(userDataManager, timeProvider)
  }

  val userDataManager: UserDataManager by injectedSingleton { JsonUserDataManager() }

  val googleCalendarManager: GoogleCalendarManager by injectedSingleton {
    GoogleCalendarManagerImpl(view::openUrlInDefaultBrowser, timeProvider, userDataManager)
  }

  val view: View by injectedSingleton { GtkView(UI_THREAD_ID, timeProvider, settings) }

  val controller: Controller by injectedSingleton {
    ControllerImpl.create(view, userDataManager, googleCalendarManager, eventReminderTracker, timeProvider, settings)
  }

  val timeProvider: TimeProvider by injectedSingleton { TimeProviderImpl }

  val settings: Settings by injectedSingleton { SettingsImpl(fileReaderWriter, settingsFormatParser) }

  val settingsFormatParser: SettingsFormatParser by injectedSingleton { YamlLikeSettingsFormatParser() }

  val fileReaderWriter: FileReaderWriter by injectedSingleton { FileReaderWriterImpl }
}
