package org.betterseqta.betterseqtateachandroid.util

object ComposeEditorHtml {

    fun shell(initialBody: String, isDark: Boolean): String {
        val css = editorCss(isDark)
        val darkClass = if (isDark) " class=\"dark\"" else ""
        val escapedBody = initialBody
        val contrastScript = if (isDark) darkContrastScript else "function fixContrast() {}"

        return """
            <!doctype html>
            <html>
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>$css</style>
            </head>
            <body contenteditable="true" id="editor"$darkClass>$escapedBody</body>
            <script>
              $contrastScript
              const editor = document.getElementById('editor');
              let ignoreNext = false;
              function setContent(html) {
                ignoreNext = true;
                editor.innerHTML = html;
                fixContrast();
                ignoreNext = false;
              }
              function notifyChange() {
                if (ignoreNext) return;
                HtmlBridge.onHtmlChanged(editor.innerHTML);
              }
              editor.addEventListener('input', notifyChange);
              editor.addEventListener('blur', notifyChange);
              fixContrast();
            </script>
            </html>
        """.trimIndent()
    }

    fun execFormatCommand(command: String): String {
        return if (command in listOf("h1", "h2", "h3")) {
            """
            (function(){
              var e = document.getElementById('editor');
              e.focus();
              document.execCommand('formatBlock', false, '$command');
              HtmlBridge.onHtmlChanged(e.innerHTML);
            })();
            """.trimIndent()
        } else {
            """
            (function(){
              var e = document.getElementById('editor');
              e.focus();
              document.execCommand('$command', false, null);
              HtmlBridge.onHtmlChanged(e.innerHTML);
            })();
            """.trimIndent()
        }
    }

    fun setContentJs(html: String): String {
        val escaped = escapeForJs(html)
        return "setContent('$escaped');"
    }

    fun escapeForJs(value: String): String =
        value
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\r", "")
            .replace("\n", "\\n")

    private fun editorCss(isDark: Boolean): String {
        val textColor = if (isDark) "#ffffff" else "#000000"
        val placeholderColor = if (isDark) "#666666" else "#999999"
        val borderColor = if (isDark) "#555555" else "#d1d5db"
        val quoteColor = if (isDark) "#aaaaaa" else "#6b7280"
        return """
          body {
            font-family: -apple-system, Roboto, Helvetica, Arial, sans-serif;
            font-size: 16px;
            line-height: 1.5;
            padding: 12px;
            margin: 0;
            min-height: 240px;
            outline: none;
            color: $textColor;
            background: transparent;
          }
          body:empty:before {
            content: "Write your message…";
            color: $placeholderColor;
          }
          blockquote.forward {
            border-left: 3px solid $borderColor;
            padding-left: 12px;
            margin: 12px 0;
            color: $quoteColor;
          }
          h1 { font-size: 1.5em; font-weight: 700; margin: 0.5em 0; }
          h2 { font-size: 1.25em; font-weight: 600; margin: 0.5em 0; }
          h3 { font-size: 1.1em; font-weight: 600; margin: 0.5em 0; }
        """.trimIndent()
    }

    private val darkContrastScript = """
        function fixContrast() {
          function luminance(r, g, b) {
            var a = [r, g, b].map(function(v) {
              v /= 255;
              return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
            });
            return 0.2126 * a[0] + 0.7152 * a[1] + 0.0722 * a[2];
          }
          function parseColor(str) {
            var m = str.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/);
            if (m) return { r: +m[1], g: +m[2], b: +m[3] };
            return null;
          }
          function hasExplicitBg(el) {
            var inline = el.style.backgroundColor;
            if (inline && inline !== '' && inline !== 'transparent') return true;
            if (el.getAttribute && el.getAttribute('bgcolor')) return true;
            return false;
          }
          var els = document.body.querySelectorAll('*');
          for (var i = 0; i < els.length; i++) {
            var el = els[i];
            if (!hasExplicitBg(el)) continue;
            var bg = getComputedStyle(el).backgroundColor;
            var c = parseColor(bg);
            if (!c) continue;
            var lum = luminance(c.r, c.g, c.b);
            if (lum > 0.4) {
              el.style.color = '';
            }
          }
        }
    """.trimIndent()
}
