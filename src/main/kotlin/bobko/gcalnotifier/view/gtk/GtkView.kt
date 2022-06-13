package bobko.gcalnotifier.view.gtk

import bobko.gcalnotifier.controller.Controller
import bobko.gcalnotifier.model.MyEvent
import bobko.gcalnotifier.settings.Settings
import bobko.gcalnotifier.support.Utils
import bobko.gcalnotifier.view.gtk.EventMenuItem
import bobko.gcalnotifier.view.RefreshButtonState
import bobko.gcalnotifier.view.View
import com.google.common.io.Resources
import org.gnome.gdk.Pixbuf
import org.gnome.glib.Glib
import org.gnome.gtk.*
import org.gnome.notify.Notification
import java.io.File
import java.net.URI
import kotlin.math.min

/**
 * Implementation based on java-gnome lib
 */
class GtkView(
  private val uiThreadId: Long,
  private val utils: Utils,
  private val settings: Settings
) : View {
  private var controller: Controller? = null
  private var popupMenu: Menu = buildEmptySystemTrayPopupMenu()
  /**
   * Initialized in [buildEmptySystemTrayPopupMenu]
   * @see buildEmptySystemTrayPopupMenu
   */
  private lateinit var refreshMenuItem: MenuItem
  /**
   * Initialized in [buildEmptySystemTrayPopupMenu]
   * @see buildEmptySystemTrayPopupMenu
   */
  private var firstEventItemIndexInPopupMenu = 0
  private var statusIcon: StatusIcon? = null
  private val appIcon: Pixbuf = Pixbuf(
    Resources.toByteArray(View::class.java.getResource("/icon.png")))
  override var refreshButtonState: RefreshButtonState = RefreshButtonState.NORMAL
    set(value) = runOnUiThread {
      field = value
      (refreshMenuItem.child as? Label)?.label = if (field == RefreshButtonState.NORMAL) "Refresh"
      else "Refreshing..."
      refreshMenuItem.sensitive = field == RefreshButtonState.NORMAL
      popupMenu.showAll()
    }

  override fun registerController(controller: Controller) {
    this.controller = controller
  }

  override fun openUrlInDefaultBrowser(url: String) = runOnUiThread {
    Gtk.showURI(URI(url))
  }

  override fun openFileInDefaultFileEditor(filePath: String) {
    Gtk.showURI(File(filePath).toURI())
  }

  private fun showNotification(summary: String,
                               body: String?,
                               infinite: Boolean,
                               actionLabel: String?,
                               action: ((Notification, String) -> Unit)?) {
    val notification = Notification(summary, body, null)
    notification.setIcon(appIcon)
    if (action != null && actionLabel != null) {
      notification.addAction("default", actionLabel, action)
    }
    if (infinite) notification.setTimeout(Notification.NOTIFY_EXPIRES_NEVER)
    notification.show()
  }

  override fun showNotification(summary: String,
                                body: String?,
                                actionLabel: String?,
                                action: ((Notification, String) -> Unit)?) = runOnUiThread {
    showNotification(summary, body, false, actionLabel, action)
  }

  override fun showInfiniteNotification(summary: String,
                                        body: String?,
                                        actionLabel: String?,
                                        action: ((Notification, String) -> Unit)?) = runOnUiThread {
    showNotification(summary, body, true, actionLabel, action)
  }

  override fun showPopupMenu() = runOnUiThread {
    popupMenu.popup(statusIcon ?: return@runOnUiThread)
  }

  @Synchronized
  override fun update(events: List<MyEvent>) = runOnUiThread {
    val eventsList = events.subList(0, min(settings.maxNumberOfEventsToShowInPopupMenu, events.size))
    removeAllEventsFromPopupMenu()
    if (eventsList.isEmpty()) {
      val item = MenuItem("No upcoming events")
      item.sensitive = false
      popupMenu.insert(item, firstEventItemIndexInPopupMenu)
      popupMenu.showAll()
    } else insertEventsInPopupMenu(eventsList)
  }

  private fun insertEventsInPopupMenu(events: List<MyEvent>) {
    val eventsDates = events.map { it.dateString(utils, settings) }
    val eventsTimes = events.map { it.timeString(settings) }

    val dateCharWidth: Int = eventsDates.map { it.length }.maxOrNull() ?: 0
    val timeCharWidth: Int = eventsTimes.map { it?.length ?: 0 }.maxOrNull() ?: 0

    events.mapIndexed { index, myEvent ->
      EventMenuItem(
        eventsDates[index],
        eventsTimes[index] ?: "",
        myEvent.title ?: "",
        dateCharWidth,
        timeCharWidth
      ) { controller?.eventPopupItemClicked(myEvent) }
    }.reversed().forEach { popupMenu.insert(it, firstEventItemIndexInPopupMenu) }

    popupMenu.showAll()
  }

  private fun removeAllEventsFromPopupMenu() {
    while (popupMenu.children[firstEventItemIndexInPopupMenu] !is SeparatorMenuItem) {
      popupMenu.remove(popupMenu.children[firstEventItemIndexInPopupMenu])
    }
  }

  override fun quit() = runOnUiThread {
    Gtk.mainQuit()
  }

  override fun showSettingsWindow() = runOnUiThread {
    TODO(reason = "not implemented")
  }

  override fun showStatusIcon() = runOnUiThread {
    statusIcon = StatusIcon(appIcon).apply {
      // left mouse button click
      connect(StatusIcon.Activate { controller?.statusIconClicked() })
      // right mouse button click
      connect { _, _, _ -> controller?.statusIconClicked() }
    }
  }

  /**
   * Builds empty popup menu. Empty means without events included
   */
  private fun buildEmptySystemTrayPopupMenu(): Menu {
    val menu = Menu()

    menu.add(MenuItem(
      "Open Google Calendar on web",
      MenuItem.Activate { controller?.openGoogleCalendarOnWebButtonClicked() }
    ))

    refreshMenuItem = MenuItem(
      "Refresh",
      MenuItem.Activate { controller?.refreshButtonClicked() }
    )
    menu.add(refreshMenuItem)

    menu.add(SeparatorMenuItem())

    firstEventItemIndexInPopupMenu = menu.children.size

    /////////////////////////////////////////////
    // Space between two separators for events //
    /////////////////////////////////////////////

    menu.add(SeparatorMenuItem())

    menu.add(MenuItem(
      "Settings",
      MenuItem.Activate { controller?.settingsButtonClicked() }
    ))

    menu.add(MenuItem(
      "Log out",
      MenuItem.Activate { controller?.logoutButtonClicked() }
    ))

    menu.add(MenuItem(
      "Quit",
      MenuItem.Activate { controller?.quitClicked() }
    ))

    menu.showAll()
    return menu
  }

  private fun runOnUiThread(callback: () -> Unit) {
    if (Thread.currentThread().id != uiThreadId) {
      Glib.idleAdd {
        callback()
        return@idleAdd false
      }
    } else {
      callback()
    }
  }
}
