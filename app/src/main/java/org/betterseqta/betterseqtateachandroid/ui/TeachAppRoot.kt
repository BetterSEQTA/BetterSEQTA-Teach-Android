package org.betterseqta.betterseqtateachandroid.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.betterseqta.betterseqtateachandroid.domain.model.LoginStatus
import org.betterseqta.betterseqtateachandroid.domain.model.SessionState
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository
import org.betterseqta.betterseqtateachandroid.navigation.DeepLinkNavigator
import org.betterseqta.betterseqtateachandroid.navigation.LocalDeepLinkNavigator
import androidx.compose.runtime.CompositionLocalProvider
import org.betterseqta.betterseqtateachandroid.ui.auth.BiometricLockOverlay
import org.betterseqta.betterseqtateachandroid.ui.auth.SetupOnboardingScreen
import org.betterseqta.betterseqtateachandroid.ui.auth.TeachLoginWebViewScreen
import org.betterseqta.betterseqtateachandroid.ui.auth.UrlEntryScreen
import org.betterseqta.betterseqtateachandroid.ui.shell.AppShellViewModel
import org.betterseqta.betterseqtateachandroid.ui.shell.TabRootScreen
import org.betterseqta.betterseqtateachandroid.ui.motion.AppSharedTransitions

/**
 * Root composable after [MainActivity] theme setup. Shows auth until [SessionRepository]
 * reports a logged-in session, then hosts [TabRootScreen].
 */
@Composable
fun TeachAppRoot(
    activity: FragmentActivity,
    deepLinkNavigator: DeepLinkNavigator,
    modifier: Modifier = Modifier,
    shellViewModel: AppShellViewModel = hiltViewModel(),
) {
    val sessionState by shellViewModel.sessionState.collectAsStateWithLifecycle()
    val biometricRequired by shellViewModel.biometricRequired.collectAsStateWithLifecycle()
    val session = sessionState.session
    val isAuthenticated = session?.isAuthenticated == true &&
        sessionState.loginStatus == LoginStatus.LoggedIn

    var setupCompleteForThisLogin by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }

    LaunchedEffect(shellViewModel) {
        shellViewModel.logoutEvents.collect {
            setupCompleteForThisLogin = false
        }
    }

    LaunchedEffect(isAuthenticated, biometricRequired) {
        if (isAuthenticated && biometricRequired) {
            isLocked = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isAuthenticated, biometricRequired) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP &&
                biometricRequired &&
                isAuthenticated
            ) {
                isLocked = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val shouldShowLockGate = biometricRequired && isAuthenticated && isLocked

    CompositionLocalProvider(LocalDeepLinkNavigator provides deepLinkNavigator) {
        AnimatedContent(
            targetState = isAuthenticated,
            transitionSpec = {
                AppSharedTransitions.fadeScaleIn() togetherWith AppSharedTransitions.fadeScaleOut()
            },
            label = "authToShell",
        ) { authed ->
            Box(modifier = modifier.fillMaxSize()) {
                if (authed) {
                    TabRootScreen(
                        deepLinkNavigator = deepLinkNavigator,
                        session = session!!,
                        staffId = sessionState.staffId,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (shouldShowLockGate) {
                        BiometricLockOverlay(
                            activity = activity,
                            onDismiss = { isLocked = false },
                        )
                    }
                } else {
                    AuthFlow(
                        sessionState = sessionState,
                        setupCompleteForThisLogin = setupCompleteForThisLogin,
                        onSetupComplete = { setupCompleteForThisLogin = true },
                        onStartLogin = shellViewModel::startLogin,
                        onCompleteLogin = shellViewModel::completeLogin,
                        onCancelLogin = shellViewModel::cancelLogin,
                        onLoginError = shellViewModel::setLoginError,
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthFlow(
    sessionState: SessionState,
    setupCompleteForThisLogin: Boolean,
    onSetupComplete: () -> Unit,
    onStartLogin: (String) -> Unit,
    onCompleteLogin: (TeachSession) -> Unit,
    onCancelLogin: () -> Unit,
    onLoginError: (String) -> Unit,
) {
    val session = sessionState.session
    val loginStatus = sessionState.loginStatus

    AnimatedContent(
        targetState = when {
            loginStatus is LoginStatus.LoggingIn -> AuthStep.LoginWebView
            setupCompleteForThisLogin -> AuthStep.UrlEntry
            else -> AuthStep.Onboarding
        },
        transitionSpec = {
            AppSharedTransitions.fadeScaleIn() togetherWith AppSharedTransitions.fadeScaleOut()
        },
        label = "authFlow",
    ) { step ->
        when (step) {
            AuthStep.Onboarding -> {
                SetupOnboardingScreen(onComplete = onSetupComplete)
            }
            AuthStep.UrlEntry -> {
                UrlEntryScreen(onContinue = onStartLogin)
            }
            AuthStep.LoginWebView -> {
                val baseUrl = session?.baseUrl ?: return@AnimatedContent
                TeachLoginWebViewScreen(
                    baseUrl = baseUrl,
                    loginStatus = loginStatus,
                    onLoginSuccess = onCompleteLogin,
                    onCancel = onCancelLogin,
                    onLoginError = onLoginError,
                )
            }
        }
    }
}

private enum class AuthStep {
    Onboarding,
    UrlEntry,
    LoginWebView,
}
