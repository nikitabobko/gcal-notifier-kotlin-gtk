package bobko.gcalnotifier.util

import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Represents read only delegate for Java's [Reference]
 */
interface ReadOnlyRefDelegate<T> : ReadOnlyProperty<Any?, T> {
  val value: T
}

interface ReadWriteRefDelegate<T> : ReadOnlyRefDelegate<T>, ReadWriteProperty<Any?, T>

private class SynchronizedRefDelegateUsingFun<R: Reference<T>, T>(private val refInit: (T) -> R,
                                                                  private val init: () -> T) : ReadWriteRefDelegate<T> {
  @Volatile
  private var ref: R? = null

  @Synchronized
  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    ref = refInit(value)
  }

  override val value: T
    get() = ref?.get() ?: synchronized(this) { ref?.get() ?: init().also { ref = refInit(it) } }

  override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}

private class SynchronizedRefDelegateUsingObj<R: Reference<T?>, T>(private val refInit: (T?) -> R,
                                                                   obj: T) : ReadWriteRefDelegate<T?> {
  override val value: T?
    get() = ref.get()

  @Volatile
  private var ref: R = refInit(obj)
    @Synchronized get() = field
    @Synchronized private set

  override fun getValue(thisRef: Any?, property: KProperty<*>): T? = value

  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
    ref = refInit(value)
  }
}

fun <T> weakRef(init: () -> T): ReadWriteRefDelegate<T> = SynchronizedRefDelegateUsingFun(::WeakReference, init)
fun <T> softRef(init: () -> T): ReadWriteRefDelegate<T> = SynchronizedRefDelegateUsingFun(::SoftReference, init)
fun <T> weakRefOf(obj: T): ReadWriteRefDelegate<T?> = SynchronizedRefDelegateUsingObj(::WeakReference, obj)
fun <T> softRefOf(obj: T): ReadWriteRefDelegate<T?> = SynchronizedRefDelegateUsingObj(::SoftReference, obj)
