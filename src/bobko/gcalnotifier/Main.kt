package bobko.gcalnotifier

import org.gnome.gtk.Gtk
import org.gnome.notify.Notify
import bobko.gcalnotifier.injector.Injector
import bobko.gcalnotifier.util.APPLICATION_NAME
import bobko.gcalnotifier.util.APPLICATION_VERSION
import bobko.gcalnotifier.util.UI_THREAD_ID
import kotlin.system.exitProcess

/**
 * Application entry point
 */
fun main(args: Array<String>) {
  check(Thread.currentThread().id == UI_THREAD_ID)

  parseArgs(args)

  Gtk.init(arrayOf())
  Notify.init(APPLICATION_NAME)
  object : Injector() {}.controller.applicationStarted()
  Gtk.main()
}

fun parseArgs(args: Array<out String>) {
  if (args.contains("--help") || args.contains("-h")) printHelpAndExit()
  if (args.contains("--version") || args.contains("-v")) printVersionAndExit()
  if (args.isNotEmpty()) printHelpAndExit()
}

fun printVersionAndExit(): Nothing {
  println("$APPLICATION_NAME version: $APPLICATION_VERSION")
  exitProcess(0)
}

fun printHelpAndExit(): Nothing {
  println("""
    usage: $APPLICATION_NAME [options]
    where possible [options] include:
      -h, --help          show this help message and exit
      -v, --version       show version and exit
  """.trimIndent())
  exitProcess(0)
}
