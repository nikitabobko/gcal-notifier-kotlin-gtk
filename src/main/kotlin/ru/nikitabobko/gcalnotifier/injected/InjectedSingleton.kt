package ru.nikitabobko.gcalnotifier.injected

fun <T> injectedSingleton(init: () -> T): Injected<T> {
  return InjectedSingleton(init)
}

private class InjectedSingleton<T>(init: () -> T) : Injected<T> {
  override val value: T by lazy(init)
}
