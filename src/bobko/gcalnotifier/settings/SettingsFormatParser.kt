package bobko.gcalnotifier.settings

import java.lang.IllegalStateException

/**
 * Settings format parser
 */
interface SettingsFormatParser {
  fun parse(text: String): List<KeyValuePairWithOptionalComment>

  fun toFormattedString(data: List<KeyValuePairWithOptionalComment>): String

  data class KeyValuePairWithOptionalComment(val key: String, val value: String, val comment: String?)
}

/**
 * It would be great if it was some generally known format such as YAML or JSON. But I've not found any
 * YAML library in Java which would allow parsing comments.
 */
class YamlLikeSettingsFormatParser : SettingsFormatParser {
  sealed class LexerNode {
    class Comment(val comment: String) : LexerNode() {
      override fun toString(): String {
        return "# $comment"
      }
    }

    class KeyValuePair(val key: String, val value: String) : LexerNode() {
      init {
        if (key.contains(":")) {
          throw IllegalStateException("Key cannot contain semicolon character. But it was: $key")
        }
      }

      override fun toString(): String {
        return "$key: $value"
      }
    }
  }

  override fun parse(text: String): List<SettingsFormatParser.KeyValuePairWithOptionalComment> {
    val linesParsed = text.split("\n").mapNotNull { line ->
      if (line.isBlank()) {
        return@mapNotNull null
      }
      if (line.startsWith("#")) {
        LexerNode.Comment(line.substring(1).trim())
      } else {
        val indexOfSemicolon = line.indexOf(":")
        if (indexOfSemicolon == -1) {
          return emptyList()
        }
        LexerNode.KeyValuePair(line.substring(0, indexOfSemicolon).trim(), line.substring(indexOfSemicolon + 1).trim())
      }
    }

    if (linesParsed.isEmpty()) {
      return emptyList()
    }

    val ans = ArrayList<SettingsFormatParser.KeyValuePairWithOptionalComment>()
    val prevComments = ArrayList<LexerNode.Comment>()

    for (cur in linesParsed) {
      when (cur) {
        is LexerNode.KeyValuePair -> {
          ans.add(SettingsFormatParser.KeyValuePairWithOptionalComment(
            cur.key, cur.value, prevComments.takeIf { it.isNotEmpty() }?.joinToString("\n") { it.comment }
          ))
          prevComments.clear()
        }
        is LexerNode.Comment -> {
          prevComments.add(cur)
        }
      }
    }
    return ans
  }

  private fun SettingsFormatParser.KeyValuePairWithOptionalComment.splitToLexerNodes(): List<LexerNode> {
    return (this.comment?.let { comment -> comment.split("\n").map { LexerNode.Comment(it) } } ?: emptyList()) +
      listOf(LexerNode.KeyValuePair(this.key, this.value))
  }

  override fun toFormattedString(data: List<SettingsFormatParser.KeyValuePairWithOptionalComment>): String {
    return data.flatMap { it.splitToLexerNodes() }.joinToString("\n")
  }
}
