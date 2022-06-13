package bobko.gcalnotifier.settings

import bobko.gcalnotifier.APPLICATION_NAME
import bobko.gcalnotifier.support.FileReaderWriter
import bobko.gcalnotifier.support.USER_HOME_FOLDER
import java.nio.file.Paths
import kotlin.reflect.KProperty
import kotlin.reflect.KType

interface Settings {
  val refreshFrequencyInMinutes: Int
  val maxNumberOfEventsToShowInPopupMenu: Int
  val timeFormat: String
  val dateFormat: String

  val settingsFilePath: String
}

/**
 * Implementation which uses custom format to keep settings (see: [SettingsFormatParser])
 *
 * Thread safe implementation.
 */
class SettingsImpl(
  private val fileReaderWriter: FileReaderWriter,
  private val parser: SettingsFormatParser
) : Settings {
  private val localDataFolderPath = "$USER_HOME_FOLDER/.config/$APPLICATION_NAME"
  private val registeredSettingsItems = HashMap<String, SettingItem<*>>()
  private val memoizedSettings: Map<String, Any> by lazy {
    val userSettingsString = fileReaderWriter.readFromFile(settingsFilePath)
    val userSettings = userSettingsString?.let { parser.parse(it) }

    val validatedUserSettings = userSettings
      ?.mapNotNull {
        val manipulator = registeredSettingsItems[it.key] ?: return@mapNotNull null
        SettingsFormatParser.KeyValuePairWithOptionalComment(
          it.key, manipulator.validateString(it.value).toString(), manipulator.comment)
      }
      ?.distinctBy { it.key }
      ?: emptyList()

    val inUserConfig = validatedUserSettings.map { it.key }

    val settingsWhichAreMissedInUserConfig = registeredSettingsItems
      .filter { it.key !in inUserConfig }
      .map {
        SettingsFormatParser.KeyValuePairWithOptionalComment(
          it.key, it.value.defaultSetting().toString(), it.value.comment)
      }
    val toSave = validatedUserSettings + settingsWhichAreMissedInUserConfig

    fileReaderWriter.writeToFile(settingsFilePath, parser.toFormattedString(toSave))

    return@lazy toSave.asSequence()
      .map {
        val manipulator = registeredSettingsItems[it.key]
          ?: throw IllegalStateException("It's completely correct settings!")
        return@map it.key to manipulator.validateString(it.value)
      }.toMap()
  }

  override val settingsFilePath = Paths.get(localDataFolderPath, "settings.yml").toString()

  override val refreshFrequencyInMinutes: Int by SettingRegistrar { maxOf(it ?: 5, 1) }

  override val maxNumberOfEventsToShowInPopupMenu: Int by SettingRegistrar { it ?: 10 }

  override val timeFormat: String by SettingRegistrar("""
    For time format documentation refer to:
    https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/text/SimpleDateFormat.html
  """.trimIndent()) { it ?: "HH:mm" }

  override val dateFormat: String by SettingRegistrar("""
    For date format documentation refer to:
    https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/text/SimpleDateFormat.html
  """.trimIndent()) { it ?: "dd MMM yyyy" }

  private inner class SettingRegistrar<T : Any>(
    val comment: String? = null,
    private val validateItem: (T?) -> T
  ) {
    operator fun provideDelegate(thisRef: SettingsImpl, prop: KProperty<*>): SettingItem<T> {
      return SettingItem(comment, validateItem, prop.returnType).also { registeredSettingsItems[prop.name] = it }
    }
  }

  private inner class SettingItem<T : Any>(
    val comment: String?,
    private val validateItem: (T?) -> T,
    private val type: KType
  ) {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(settings: SettingsImpl, property: KProperty<*>): T {
      return memoizedSettings[property.name] as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun String.toT(): T? {
      return when (type.classifier) {
        Int::class -> this.toIntOrNull()
        String::class -> this
        else -> throw UnsupportedOperationException("$type isn't supported yet")
      } as T?
    }

    fun validateString(string: String): T {
      return validateItem(string.toT())
    }

    fun defaultSetting(): T {
      return validateItem(null)
    }
  }
}
