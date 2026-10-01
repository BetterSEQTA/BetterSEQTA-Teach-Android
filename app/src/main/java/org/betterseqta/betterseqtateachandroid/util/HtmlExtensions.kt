package org.betterseqta.betterseqtateachandroid.util

import android.text.Html
import android.os.Build

/**
 * iOS String+HTML parity for plain-text extraction and paragraph wrapping.
 */
fun String.plainTextFromHtml(): String {
    if (isBlank()) return ""
    val stripped = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(this).toString()
    }
    return stripped.trim()
}

fun String.wrappedInHtmlParagraphs(): String {
    val escaped = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
    val paragraphs = escaped.split("\n\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (paragraphs.isEmpty()) return "<p></p>"
    return paragraphs.joinToString(separator = "") { paragraph ->
        val withBreaks = paragraph.replace("\n", "<br>")
        "<p>$withBreaks</p>"
    }
}

fun wrapNoticeHtml(fragment: String, darkMode: Boolean): String {
    val textColor = if (darkMode) "#e6e1e5" else "#1c1b1f"
    val bg = if (darkMode) "#121212" else "#ffffff"
    val border = if (darkMode) "#49454f" else "#d1d5db"
    val colorScheme = if (darkMode) "dark" else "light"
    return """
<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
    <meta name="color-scheme" content="$colorScheme">
    <style>
      html, body { margin: 0; padding: 0; background: $bg; color: $textColor; font-family: sans-serif; font-size: 16px; line-height: 1.6; }
      a { color: #7cacf8; }
      img { max-width: 100%; height: auto; border-radius: 8px; }
      table { border-collapse: collapse; width: 100%; }
      th, td { border: 1px solid $border; padding: 8px; text-align: left; }
      p { margin: 0 0 0.75em 0; }
    </style>
  </head>
  <body>
    $fragment
  </body>
</html>
""".trimIndent()
}

fun wrapMessageBodyHtml(fragment: String, darkMode: Boolean): String {
    val textColor = if (darkMode) "#ffffff" else "#000000"
    val bg = if (darkMode) "#121212" else "#ffffff"
    return """
<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
    <style>
      html, body { margin: 0; padding: 12px; background: $bg; color: $textColor; font-family: sans-serif; font-size: 16px; line-height: 1.5; }
      a { color: #0a84ff; }
      img { max-width: 100%; height: auto; }
    </style>
  </head>
  <body>
    $fragment
  </body>
</html>
""".trimIndent()
}
