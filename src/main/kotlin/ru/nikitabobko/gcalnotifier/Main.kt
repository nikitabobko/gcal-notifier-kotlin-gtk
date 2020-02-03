package ru.nikitabobko.gcalnotifier

import org.gnome.gtk.Gtk
import org.gnome.notify.Notify
import ru.nikitabobko.gcalnotifier.support.FactoryImpl
import kotlin.system.exitProcess

const val APPLICATION_NAME = "gcal-notifier-kotlin-gtk"

val APPLICATION_VERSION: String = object {}::class.java.getResourceAsStream("/version.txt").bufferedReader().use {
  it.readLine()!!
}

val UI_THREAD_ID = Thread.currentThread().id

/**
 * Application entry point
 */
fun main(args: Array<String>) {
  Thread.currentThread().let {
    assert(it.id == UI_THREAD_ID)
    it.priority = Thread.MIN_PRIORITY
  }

  parseArgs(args)

  Gtk.init(arrayOf())
  Notify.init(APPLICATION_NAME)
  FactoryImpl.controller.value.applicationStarted()
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
