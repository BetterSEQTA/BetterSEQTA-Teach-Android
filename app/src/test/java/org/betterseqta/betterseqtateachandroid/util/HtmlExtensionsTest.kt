package org.betterseqta.betterseqtateachandroid.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlExtensionsTest {

    @Test
    fun plainTextFromHtml_stripsTags() {
        val result = "<p>Hello <b>world</b></p>".plainTextFromHtml()
        assertTrue(result.contains("Hello"))
        assertTrue(result.contains("world"))
    }

    @Test
    fun wrappedInHtmlParagraphs_splitsBlankLines() {
        val result = "Line one\n\nLine two".wrappedInHtmlParagraphs()
        assertEquals("<p>Line one</p><p>Line two</p>", result)
    }

    @Test
    fun wrapNoticeHtml_includesBodyFragment() {
        val html = wrapNoticeHtml("<strong>Notice</strong>", darkMode = true)
        assertTrue(html.contains("<strong>Notice</strong>"))
        assertTrue(html.contains("color-scheme"))
        assertTrue(html.contains("#121212"))
    }
}
