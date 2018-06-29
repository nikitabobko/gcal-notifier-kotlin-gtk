package ru.nikitabobko.gcalnotifier

import com.google.common.io.Resources
import org.gnome.gdk.Pixbuf
import org.gnome.gtk.*
import org.gnome.notify.Notification
import ru.nikitabobko.gcalnotifier.model.MyEvent
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

val view: View = ViewImpl()

interface View {
    fun showStatusIcon()
    fun showSettingsWindow()
    fun update(events: List<MyEvent>)
    fun quit()
    var refreshButtonState: RefreshButtonState
    fun showPopupMenu()
    fun showNotification(summary: String, body: String?,
                         actionLabel: String? = null, action: ((Notification, String) -> Unit)? = null)
    fun showInfiniteNotification(summary: String, body: String?,
                                 actionLabel: String? = null,
                                 action: ((Notification, String) -> Unit)? = null)
}

enum class RefreshButtonState {
    REFRESHING, NORMAL
}

class ViewImpl : View {
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
            Resources.toByteArray(Class::class.java.getResource("/gcal-icon.png"))
    )
    override var refreshButtonState: RefreshButtonState = RefreshButtonState.NORMAL
        set(value) {
            field = value
            if (field == RefreshButtonState.NORMAL) {
                refreshMenuItem.sensitive = true
                refreshMenuItem.name = "Refreshing..."
            } else {
                refreshMenuItem.sensitive = false
                refreshMenuItem.name = "Refresh"
            }
            popupMenu.showAll()
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
                                  action: ((Notification, String) -> Unit)?) {
        showNotification(summary, body, false, actionLabel, action)
    }

    override fun showInfiniteNotification(summary: String,
                                          body: String?,
                                          actionLabel: String?,
                                          action: ((Notification, String) -> Unit)?) {
        showNotification(summary, body, true, actionLabel, action)
    }

    override fun showPopupMenu() {
        popupMenu.popup(statusIcon ?: return)
    }

    @Synchronized
    override fun update(events: List<MyEvent>) {
        val events = events.subList(0, min(settings.maxNumberOfEventsToShowInPopupMenu, events.size))
        removeAllEventsFromPopupMenu()
        if (events.isEmpty()) {
            val item = MenuItem("No upcoming events")
            item.sensitive = false
            popupMenu.insert(item, firstEventItemIndexInPopupMenu)
            popupMenu.showAll()
        } else insertEventsInPopupMenu(events)
    }

    private fun insertEventsInPopupMenu(events: List<MyEvent>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        for (event: MyEvent in events.reversed()) {
            val date = Date(event.startUNIXTime)
            val item = MenuItem(
                    dateFormat.format(date) + "    " + event.title,
                    MenuItem.Activate { menuItem ->
                        val indexOf = popupMenu.children.indexOf(menuItem) -
                                firstEventItemIndexInPopupMenu
                        controller.eventPopupItemClicked(indexOf)
                    }
            )
            popupMenu.insert(item, firstEventItemIndexInPopupMenu)
        }
        popupMenu.showAll()
    }

    private fun removeAllEventsFromPopupMenu() {
        while (popupMenu.children[firstEventItemIndexInPopupMenu] !is SeparatorMenuItem) {
            popupMenu.remove(popupMenu.children[firstEventItemIndexInPopupMenu])
        }
    }

    override fun quit() {
        Gtk.mainQuit()
    }

    override fun showSettingsWindow() {
        TODO(reason = "not implemented")
    }

    override fun showStatusIcon() {
        statusIcon = StatusIcon(appIcon)
        // left mouse button click
        statusIcon!!.connect(StatusIcon.Activate { controller.statusIconClicked() })
        // right mouse button click
        statusIcon!!.connect { _: StatusIcon, _, _ -> controller.statusIconClicked() }
    }

    /**
     * Builds empty popup menu. Empty means without events included
     */
    private fun buildEmptySystemTrayPopupMenu(): Menu {
        val menu = Menu()

        menu.add(MenuItem(
            "Open Google Calendar on web",
            MenuItem.Activate { controller.openGoogleCalendarOnWebButtonClicked() }
        ))

        refreshMenuItem = MenuItem(
                "Refresh",
                MenuItem.Activate { controller.refreshButtonClicked() }
        )
        menu.add(refreshMenuItem)

        menu.add(SeparatorMenuItem())

        firstEventItemIndexInPopupMenu = menu.children.size

        /////////////////////////////////////////////
        // Space between two separators for events //
        /////////////////////////////////////////////

        menu.add(SeparatorMenuItem())

        // todo upcoming feature
//        menu.add(MenuItem(
//            "Settings",
//            MenuItem.Activate { controller.settingsButtonClicked() }
//        ))

        menu.add(MenuItem(
            "Log out",
            MenuItem.Activate { controller.logoutButtonClicked() }
        ))

        menu.add(MenuItem(
            "Quit",
            MenuItem.Activate { controller.quitClicked() }
        ))

        menu.showAll()
        return menu
    }
}