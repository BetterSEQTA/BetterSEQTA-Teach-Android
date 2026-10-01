package org.betterseqta.betterseqtateachandroid.ui.auth

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import org.betterseqta.betterseqtateachandroid.domain.model.LoginStatus
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.util.JSessionCookieHelper
import org.betterseqta.betterseqtateachandroid.util.SeqtaUrlHelper

private const val LOGIN_POLL_INTERVAL_MS = 1_000L
private const val LOGIN_TIMEOUT_MS = 180_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachLoginWebViewScreen(
    baseUrl: String,
    loginStatus: LoginStatus,
    onLoginSuccess: (TeachSession) -> Unit,
    onCancel: () -> Unit,
    onLoginError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var captureTrigger by remember { mutableIntStateOf(0) }
    var currentWebUrl by remember { mutableStateOf<String?>(null) }
    var pollingActive by remember { mutableStateOf(true) }
    val loginError = (loginStatus as? LoginStatus.Error)?.message

    val teachUrl = remember(baseUrl) { SeqtaUrlHelper.buildTeachLoginUrl(baseUrl) }
    val title = remember(baseUrl) { SeqtaUrlHelper.hostFromUrl(baseUrl) }

    LaunchedEffect(captureTrigger, baseUrl) {
        if (captureTrigger <= 0) return@LaunchedEffect
        pollingActive = false
        val jsessionId = JSessionCookieHelper.findJSessionId(baseUrl)
        if (!jsessionId.isNullOrEmpty()) {
            onLoginSuccess(TeachSession(baseUrl = baseUrl, jsessionId = jsessionId))
        } else {
            onLoginError(
                "Could not find a session cookie. Finish signing in, then tap Done again.",
            )
            pollingActive = true
        }
    }

    LaunchedEffect(baseUrl, pollingActive, captureTrigger) {
        if (!pollingActive || captureTrigger > 0) return@LaunchedEffect
        val start = System.currentTimeMillis()
        while (pollingActive && System.currentTimeMillis() - start < LOGIN_TIMEOUT_MS) {
            delay(LOGIN_POLL_INTERVAL_MS)
            val jsessionId = JSessionCookieHelper.findJSessionId(baseUrl)
            if (!jsessionId.isNullOrEmpty() && JSessionCookieHelper.isWelcomeUrl(currentWebUrl)) {
                pollingActive = false
                onLoginSuccess(TeachSession(baseUrl = baseUrl, jsessionId = jsessionId))
                return@LaunchedEffect
            }
        }
        if (pollingActive) {
            onLoginError("Login timed out. Please try again.")
            pollingActive = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { captureTrigger += 1 },
                    ) {
                        Text("Done", fontWeight = FontWeight.SemiBold)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TeachLoginWebView(
                    teachUrl = teachUrl,
                    onUrlChanged = { currentWebUrl = it },
                    onPageFinished = {
                        currentWebUrl = it
                        val jsessionId = JSessionCookieHelper.findJSessionId(baseUrl)
                        if (!jsessionId.isNullOrEmpty() && JSessionCookieHelper.isWelcomeUrl(it)) {
                            pollingActive = false
                            onLoginSuccess(TeachSession(baseUrl = baseUrl, jsessionId = jsessionId))
                        }
                    },
                )

                if (loginError != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        tonalElevation = 4.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = loginError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Tap Done when you're signed in",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Your session stays on this device only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun TeachLoginWebView(
    teachUrl: String,
    onUrlChanged: (String) -> Unit,
    onPageFinished: (String) -> Unit,
) {
    val cookieManager = remember { CookieManager.getInstance() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            cookieManager.setAcceptCookie(true)
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                cookieManager.setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean = false

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val finishedUrl = url ?: view?.url
                        if (finishedUrl != null) {
                            onUrlChanged(finishedUrl)
                            onPageFinished(finishedUrl)
                        }
                    }
                }
                loadUrl(teachUrl)
            }
        },
        update = { webView ->
            cookieManager.flush()
        },
    )
}
