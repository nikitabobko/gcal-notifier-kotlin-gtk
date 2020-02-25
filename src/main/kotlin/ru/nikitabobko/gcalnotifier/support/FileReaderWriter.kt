package ru.nikitabobko.gcalnotifier.support

import java.io.File
import java.io.PrintWriter

interface FileReaderWriter {
  fun readFromFile(path: String): String?
  fun writeToFile(path: String, content: String)
}

object FileReaderWriterImpl : FileReaderWriter {
  override fun readFromFile(path: String): String? {
    return File(path).takeIf { it.exists() }?.bufferedReader()?.use { it.readText() }
  }

  override fun writeToFile(path: String, content: String) {
    PrintWriter(path).use { it.print(content) }
  }
}
