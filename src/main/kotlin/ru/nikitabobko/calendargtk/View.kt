package ru.nikitabobko.calendargtk

import com.google.api.services.calendar.model.Event
import com.google.common.io.Resources
import org.gnome.gdk.Pixbuf
import org.gnome.gtk.*
import org.gnome.notify.Notification
import ru.nikitabobko.calendargtk.support.timeIfAvaliableOrDate
import java.text.SimpleDateFormat
import java.util.*

val view: View = ViewImpl()

interface View {
    fun showStatusIcon()
    fun showSettingsWindow()
    fun update(events: List<Event>)
    fun quit()
    var refreshButtonState: RefreshButtonState
    fun showNotification(summary: String, body: String)
}

enum class RefreshButtonState {
    REFRESHING, NORMAL
}

class ViewImpl : View {
    private var popupMenu: Menu = buildEmptySystemTrayPopupMenu()
    private var refreshMenuItem: MenuItem? = null
    /**
     * Initialized in [buildEmptySystemTrayPopupMenu]
     * @see buildEmptySystemTrayPopupMenu
     */
    private var firstEventItemIndexInPopupMenu = 0
    private var statusIcon: StatusIcon? = null
    private val appIcon: Pixbuf = Pixbuf(Resources.toByteArray(Class::class.java.getResource("/gcal-icon.png")))
    override var refreshButtonState: RefreshButtonState = RefreshButtonState.NORMAL
        set(value) {
            field = value
            if (refreshMenuItem == null) return
            if (field == RefreshButtonState.NORMAL) {
                refreshMenuItem!!.sensitive = true
                refreshMenuItem!!.setTooltipText("Refresh")
            } else {
                refreshMenuItem!!.sensitive = false
                refreshMenuItem!!.setTooltipText("Refreshing...")
            }
            refreshMenuItem!!.show()
        }

    override fun showNotification(summary: String, body: String) {
        val notification = Notification(summary, body, null)
        notification.setIcon(appIcon)
        notification.show()
    }

    override fun update(events: List<Event>) {
        removeAllEventsFromPopupMenu(popupMenu)
        if (events.isEmpty()) {
            val item = MenuItem("No upcoming events")
            item.sensitive = false
            popupMenu.insert(item, firstEventItemIndexInPopupMenu)
            popupMenu.showAll()
        } else insertEventsInPopupMenu(popupMenu, events)
    }

    private fun insertEventsInPopupMenu(popupMenu: Menu, events: List<Event>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        for (event: Event in events.reversed()) {
            val date = Date(event.start.timeIfAvaliableOrDate.value)
            popupMenu.insert(
                    MenuItem(dateFormat.format(date) + "    " + event.summary),
                    firstEventItemIndexInPopupMenu
            )
        }
        popupMenu.showAll()
    }

    private fun removeAllEventsFromPopupMenu(popupMenu: Menu) {
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
        statusIcon!!.connect(::onStatusIconClick)
        // right mouse button click
        statusIcon!!.connect { a: StatusIcon, _, _ -> onStatusIconClick(a) }
    }

    private fun onStatusIconClick(statusIcon: StatusIcon) {
        popupMenu.popup(statusIcon)
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

        // upcoming feature
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