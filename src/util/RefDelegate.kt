package bobko.gcalnotifier.util

import java.lang.ref.Reference
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private class RefDelegate<R: Reference<T>, T>(
  private val refInit: (T) -> R,
  private val init: () -> T
) : ReadWriteProperty<Any?, T> {
  @Volatile
  private var ref: R? = null

  @Synchronized
  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    ref = refInit(value)
  }

  override fun getValue(thisRef: Any?, property: KProperty<*>): T =
    ref?.get() ?: synchronized(this) { ref?.get() ?: init().also { ref = refInit(it) } }
}

fun <T> weakRef(init: () -> T): ReadWriteProperty<Any?, T> = RefDelegate(::WeakReference, init)
