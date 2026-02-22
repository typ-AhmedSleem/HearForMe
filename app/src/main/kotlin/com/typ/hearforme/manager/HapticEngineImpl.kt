package com.typ.hearforme.manager

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.typ.hearforme.domain.manager.HapticEngine
import com.typ.hearforme.domain.model.SoundType

class HapticEngineImpl(context: Context) : HapticEngine {

    companion object {
        private const val TAG = "HapticEngine"
        private const val AMP_MAX = 255
        private const val AMP_STRONG = 220
        private const val AMP_MEDIUM = 180
        private const val AMP_OFF = 0
    }

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    init {
        Log.d(TAG, "Vibrator available: ${vibrator.hasVibrator()}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Amplitude control: ${vibrator.hasAmplitudeControl()}")
        }
    }

    override fun vibrateForPriority(priority: SoundType.Priority) {
        if (!vibrator.hasVibrator()) return

        val (timings, amplitudes) = when (priority) {
            // Intense staccato bursts followed by a sustained long vibration
            SoundType.Priority.CRITICAL -> longArrayOf(
                0, 150, 50, 150, 50, 150, 50, 600
            ) to intArrayOf(
                AMP_OFF, AMP_MAX, AMP_OFF, AMP_MAX, AMP_OFF, AMP_MAX, AMP_OFF, AMP_MAX
            )

            // Strong double-pulse pattern
            SoundType.Priority.HIGH -> longArrayOf(
                0, 300, 80, 300, 80, 400
            ) to intArrayOf(
                AMP_OFF, AMP_MAX, AMP_OFF, AMP_STRONG, AMP_OFF, AMP_MAX
            )

            // Single powerful pulse
            SoundType.Priority.NORMAL -> longArrayOf(
                0, 500, 100, 300
            ) to intArrayOf(
                AMP_OFF, AMP_STRONG, AMP_OFF, AMP_MEDIUM
            )

            // Short subtle tap
            SoundType.Priority.LOW -> longArrayOf(
                0, 250
            ) to intArrayOf(
                AMP_OFF, AMP_MEDIUM
            )
        }

        vibrateWithAmplitudes(timings, amplitudes)
    }

    override fun performInteractionFeedback() {
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
    }

    override fun vibrate(pattern: LongArray, repeat: Int) {
        if (!vibrator.hasVibrator()) return
        // Build max-amplitude array for the simple pattern API
        val amplitudes = IntArray(pattern.size) { i ->
            if (i % 2 == 0) AMP_OFF else AMP_MAX
        }
        vibrateWithAmplitudes(pattern, amplitudes, repeat)
    }

    private fun vibrateWithAmplitudes(
        timings: LongArray,
        amplitudes: IntArray,
        repeat: Int = -1,
    ) {
        val effect = VibrationEffect.createWaveform(timings, amplitudes, repeat)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attributes = VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_ALARM)
                .build()
            vibrator.vibrate(effect, attributes)
        } else {
            vibrator.vibrate(effect)
        }
    }
}
