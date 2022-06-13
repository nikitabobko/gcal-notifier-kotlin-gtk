package bobko.gcalnotifier.view.gtk

import org.gnome.gtk.MenuItem
import org.gnome.gtk.Orientation

class EventMenuItem(date: String,
                    time: String,
                    eventTitle: String,
                    dateLabelCharWidth: Int,
                    timeLabelCharWidth: Int,
                    handler: (MenuItem) -> Unit) : MenuItem() {
  init {
    box(Orientation.HORIZONTAL, 4) {
      label(date) {
        setAlignment(0f, 0f)
        setWidthChars(dateLabelCharWidth)
      }
      label(time) {
        setAlignment(0f, 0f)
        setWidthChars(timeLabelCharWidth)
      }
      label(eventTitle)
    }

    connect(handler)
  }
}
