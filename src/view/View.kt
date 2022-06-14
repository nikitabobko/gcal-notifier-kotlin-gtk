package bobko.gcalnotifier.view

import bobko.gcalnotifier.controller.Controller
import bobko.gcalnotifier.model.MyEvent
import org.gnome.notify.Notification

/**
 * Just receives requests by [Controller] and performs them.
 */
interface View {
  fun registerController(controller: Controller)
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

  fun openUrlInDefaultBrowser(url: String)

  fun openFileInDefaultFileEditor(filePath: String)
}

enum class RefreshButtonState {
  REFRESHING, NORMAL
}
