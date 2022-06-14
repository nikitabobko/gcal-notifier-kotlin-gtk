package bobko.gcalnotifier.settings

import junit.framework.TestCase
import bobko.gcalnotifier.settings.SettingsFormatParser.*

class SettingsFormatParserTest : TestCase() {
  fun `test first line is empty`() = doParseTest("""

    foo: 5
  """.trimIndent(), listOf(KeyValuePairWithOptionalComment("foo", "5", null)))

  fun `test several key values`() = doParseTest("""
    foo: bar
    baz: quix
  """.trimIndent(), listOf(
    KeyValuePairWithOptionalComment("foo", "bar", null),
    KeyValuePairWithOptionalComment("baz", "quix", null)
  ))

  fun `test several key values second line is blank`() = doParseTest("""
    foo: bar

    baz: quix
  """.trimIndent(), listOf(
    KeyValuePairWithOptionalComment("foo", "bar", null),
    KeyValuePairWithOptionalComment("baz", "quix", null)
  ))

  fun `test one line comment`() = doParseTest("""
    # This is comment
    foo: bar
  """.trimIndent(), listOf(
    KeyValuePairWithOptionalComment("foo", "bar", "This is comment")
  ))

  fun `test multiline comment`() = doParseTest("""
    # First comment line
    # Second comment line
    foo: bar
  """.trimIndent(), listOf(
    KeyValuePairWithOptionalComment("foo", "bar", """
      First comment line
      Second comment line
    """.trimIndent())
  ))

  fun `test comment without anchor`() = doParseTest("""
    # Foo
  """.trimIndent(), listOf())

  fun `test several key pair values each with own comment`() = doParseTest("""
    # Comment
    foo: bar
    # Multi-line
    # comment
    baz: quix
  """.trimIndent(), listOf(
    KeyValuePairWithOptionalComment("foo", "bar", "Comment"),
    KeyValuePairWithOptionalComment("baz", "quix", """
      Multi-line
      comment
    """.trimIndent())
  ))

  fun `test whitespaces around semicolon are trimmed`() = doParseTest("""
    foo      :          bar
  """.trimIndent(), listOf(KeyValuePairWithOptionalComment("foo", "bar", null)))

  fun `test semicolon in value is parsed correctly`() = doParseTest("""
    foo: ba:r
  """.trimIndent(), listOf(KeyValuePairWithOptionalComment("foo", "ba:r", null)))

  fun `test completely broken format does not yield crash`() = doParseTest("""
    fjlasjfl;sajf;# l sf;lsj flsj;lfws
    fjlasjfl;sa  :    jf;# l sf;lsj flsj;lfws
  """.trimIndent(), listOf())

  private fun doParseTest(text: String, expected: List<KeyValuePairWithOptionalComment>) {
    val parser = YamlLikeSettingsFormatParser()
    assertEquals(expected, parser.parse(text))
  }
}
