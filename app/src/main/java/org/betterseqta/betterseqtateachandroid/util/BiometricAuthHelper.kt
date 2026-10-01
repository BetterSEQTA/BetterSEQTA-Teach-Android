package org.betterseqta.betterseqtateachandroid.util

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object BiometricAuthHelper {

    fun isAvailable(activity: FragmentActivity): Boolean {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        return BiometricManager.from(activity)
            .canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun biometricTypeName(activity: FragmentActivity): String {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        val result = BiometricManager.from(activity).canAuthenticate(authenticators)
        return when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometrics"
            else -> "Biometrics"
        }
    }

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock BetterSEQTA Teach",
        subtitle: String = "Confirm your identity to continue",
    ): Boolean = suspendCancellableCoroutine { continuation ->
        if (!isAvailable(activity)) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (continuation.isActive) continuation.resume(true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (continuation.isActive) continuation.resume(false)
            }

            override fun onAuthenticationFailed() {
                // Keep prompt open; user can retry.
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK,
            )
            .build()

        prompt.authenticate(promptInfo)
        continuation.invokeOnCancellation {
            prompt.cancelAuthentication()
        }
    }
}
