package org.betterseqta.betterseqtateachandroid.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.betterseqta.betterseqtateachandroid.util.ComposeEditorHtml

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ComposeHtmlEditorWebView(
    html: String,
    onHtmlChanged: (String) -> Unit,
    formatCommand: String?,
    onFormatCommandConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var ready by remember { mutableStateOf(false) }
    var lastLoadedDark by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(formatCommand, ready) {
        val command = formatCommand ?: return@LaunchedEffect
        val webView = webViewRef ?: return@LaunchedEffect
        webView.evaluateJavascript(ComposeEditorHtml.execFormatCommand(command), null)
        onFormatCommandConsumed()
    }

    LaunchedEffect(isDark) {
        val webView = webViewRef ?: return@LaunchedEffect
        if (lastLoadedDark == isDark) return@LaunchedEffect
        ready = false
        lastLoadedDark = isDark
        webView.loadDataWithBaseURL(
            null,
            ComposeEditorHtml.shell(html, isDark),
            "text/html",
            "UTF-8",
            null,
        )
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                isVerticalScrollBarEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                addJavascriptInterface(
                    HtmlBridge(onHtmlChanged),
                    "HtmlBridge",
                )
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        ready = true
                    }
                }
                lastLoadedDark = isDark
                loadDataWithBaseURL(
                    null,
                    ComposeEditorHtml.shell(html, isDark),
                    "text/html",
                    "UTF-8",
                    null,
                )
                webViewRef = this
            }
        },
        update = { webView ->
            webViewRef = webView
        },
    )
}

private class HtmlBridge(
    private val onHtmlChanged: (String) -> Unit,
) {
    @JavascriptInterface
    fun onHtmlChanged(html: String) {
        onHtmlChanged(html)
    }
}
