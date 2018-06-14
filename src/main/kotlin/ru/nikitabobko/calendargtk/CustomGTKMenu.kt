package ru.nikitabobko.calendargtk

import org.gnome.gtk.Menu
import org.gnome.gtk.MenuItem
import org.gnome.gtk.StatusIcon

// Not used yet
class CustomGTKMenu(val statusIcon: StatusIcon) : Menu() {
    init {
        connect(Hide {
            if (suppressHiding) {
                activate()
            }
        })
    }

    override fun popup() {
        super.popup(statusIcon)
    }

    var suppressHiding: Boolean = false
        get() {
            val ret = field
            if (field) {
                field = false
            }
            return ret
        }

    /**
     * Adds MenuItem which has one feature: when user click this MenuItem menu won't hide after this click
     */
    fun addSuppressingHidingWhenClicked(child: MenuItem) {
        child.connect(MenuItem.Activate {
            suppressHiding = true
            print("here")
        })
        super.add(child)
    }
}