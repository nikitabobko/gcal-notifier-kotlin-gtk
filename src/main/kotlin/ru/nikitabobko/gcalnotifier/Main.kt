package ru.nikitabobko.gcalnotifier

import org.gnome.gtk.Gtk
import org.gnome.notify.Notify
import ru.nikitabobko.gcalnotifier.controller.ControllerImpl
import kotlin.system.exitProcess

const val APPLICATION_NAME = "gcal-notifier-kotlin-gtk"
const val APPLICATION_VERSION = "v1.0.6-beta"

/**
 * Application entry point
 */
fun main(args: Array<String>) {
    parseArgs(args)

    Gtk.init(arrayOf())
    Notify.init(APPLICATION_NAME)
    ControllerImpl().applicationStarted()
    Gtk.main()
}

fun parseArgs(args: Array<out String>) {
    if (args.contains("--help") || args.contains("-h")) printHelpAndExit()
    if (args.contains("--version") || args.contains("-v")) printVersionAndExit()
    if (args.isNotEmpty()) printHelpAndExit()
}

fun printVersionAndExit() {
    println("$APPLICATION_NAME version: $APPLICATION_VERSION")
    exitProcess(0)
}

fun printHelpAndExit() {
    println("""
        usage: $APPLICATION_NAME <options>
        where possible options include:
          -h, --help          show this help message and exit
          -v, --version       show version and exit
    """.trimIndent())
    exitProcess(0)
}
