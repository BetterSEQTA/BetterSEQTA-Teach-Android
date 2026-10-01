package org.betterseqta.betterseqtateachandroid.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Haptic and light audio feedback (iOS FeedbackManager). */
@Singleton
class FeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val vibrator: Vibrator?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService<VibratorManager>()?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService()
        }

    fun selection(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            ?: vibrateOneShot(12, 40)
    }

    fun light(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            ?: vibrateOneShot(18, 60)
    }

    fun medium(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            ?: vibrateOneShot(28, 80)
    }

    fun rigidSnap(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            ?: vibrateOneShot(35, 120)
    }

    fun heavy(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            ?: vibrateOneShot(45, 140)
    }

    fun success(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            ?: vibratePattern(longArrayOf(0, 30, 60, 40), intArrayOf(0, 80, 0, 120))
    }

    fun error(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.REJECT)
            ?: vibratePattern(longArrayOf(0, 40, 40, 40), intArrayOf(0, 150, 0, 150))
    }

    fun warning(view: View? = null) {
        view?.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
            ?: vibrateOneShot(30, 100)
    }

    fun doubleTap() {
        vibratePattern(
            longArrayOf(0, 40, 80, 40),
            intArrayOf(0, 70, 0, 70),
        )
    }

    fun tripleTap() {
        vibratePattern(
            longArrayOf(0, 35, 70, 35, 70, 35),
            intArrayOf(0, 65, 0, 65, 0, 65),
        )
    }

    fun longVibration() {
        vibrateOneShot(250, 160)
    }

    fun longThenShort() {
        vibratePattern(
            longArrayOf(0, 150, 50, 35),
            intArrayOf(0, 120, 0, 90),
        )
    }

    fun playTick() = playTone(ToneGenerator.TONE_PROP_BEEP)
    fun playTap() = playTone(ToneGenerator.TONE_PROP_BEEP)
    fun playMark() = playTone(ToneGenerator.TONE_PROP_ACK)
    fun playSuccess() = playTone(ToneGenerator.TONE_PROP_ACK)

    private fun vibrateOneShot(durationMs: Long, amplitude: Int) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }

    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(timings, -1)
        }
    }

    private fun playTone(tone: Int) {
        runCatching {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 40).startTone(tone, 40)
        }
    }
}
