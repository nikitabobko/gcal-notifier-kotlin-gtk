package ru.nikitabobko.gcalnotifier.injected

import kotlin.reflect.KProperty

interface Injected<out T> {
  val value: T
}

operator fun <T> Injected<T>.getValue(thisRef: Any?, property: KProperty<*>): T = value
