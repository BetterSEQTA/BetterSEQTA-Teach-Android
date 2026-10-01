package org.betterseqta.betterseqtateachandroid.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.betterseqta.betterseqtateachandroid.util.wrapMessageBodyHtml
import org.betterseqta.betterseqtateachandroid.util.wrapNoticeHtml

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NoticeHtmlWebView(
    html: String,
    modifier: Modifier = Modifier,
    minHeightDp: Int = 120,
) {
    val dark = isSystemInDarkTheme()
    AutoHeightHtmlWebView(
        documentHtml = wrapNoticeHtml(html, dark),
        modifier = modifier,
        minHeightDp = minHeightDp,
        forceLightDocument = false,
        webViewBackgroundArgb = if (dark) android.graphics.Color.parseColor("#121212") else android.graphics.Color.TRANSPARENT,
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MessageHtmlWebView(
    html: String,
    modifier: Modifier = Modifier,
    minHeightDp: Int = 120,
) {
    val dark = isSystemInDarkTheme()
    AutoHeightHtmlWebView(
        documentHtml = wrapMessageBodyHtml(html, dark),
        modifier = modifier,
        minHeightDp = minHeightDp,
        forceLightDocument = false,
        webViewBackgroundArgb = if (dark) android.graphics.Color.parseColor("#121212") else android.graphics.Color.TRANSPARENT,
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun AutoHeightHtmlWebView(
    documentHtml: String,
    modifier: Modifier = Modifier,
    minHeightDp: Int = 120,
    forceLightDocument: Boolean,
    webViewBackgroundArgb: Int = android.graphics.Color.TRANSPARENT,
) {
    var contentHeightPx by remember(documentHtml) { mutableIntStateOf(0) }
    val heightDp = if (contentHeightPx > 0) {
        (contentHeightPx / android.content.res.Resources.getSystem().displayMetrics.density).dp
    } else {
        minHeightDp.dp
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(maxOf(heightDp, minHeightDp.dp)),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                if (forceLightDocument) {
                    setBackgroundColor(android.graphics.Color.WHITE)
                } else {
                    setBackgroundColor(webViewBackgroundArgb)
                }
                isVerticalScrollBarEnabled = false
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        val uri = request?.url ?: return false
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        return true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        view?.evaluateJavascript(
                            "(function() { return document.body.scrollHeight; })();",
                        ) { result ->
                            result?.trim()?.toDoubleOrNull()?.toInt()?.let { px ->
                                contentHeightPx = px
                            }
                        }
                    }
                }
                loadDataWithBaseURL(null, documentHtml, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, documentHtml, "text/html", "UTF-8", null)
        },
    )
}
