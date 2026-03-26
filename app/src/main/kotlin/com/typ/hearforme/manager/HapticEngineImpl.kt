package com.typ.hearforme.manager

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.VibratorManager
import android.util.Log
import com.typ.hearforme.domain.manager.HapticEngine
import com.typ.hearforme.domain.manager.HapticVibrationPattern
import com.typ.hearforme.domain.model.SoundType
import com.typ.hearforme.domain.utils.shouldMatchLengthOf

class HapticEngineImpl(context: Context) : HapticEngine {

    companion object {
        private const val TAG = "HapticEngine"
        private const val AMP_MAX = 255
        private const val AMP_STRONG = 220
        private const val AMP_MEDIUM = 180
        private const val AMP_OFF = 0
    }

    private val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    private val vibrator = vibratorManager.defaultVibrator
    private val hasVibrator = vibrator.hasVibrator()

    init {
        Log.d(TAG, "Vibrator available: $hasVibrator")
        Log.d(TAG, "Amplitude control: ${vibrator.hasAmplitudeControl()}")
    }

    override fun vibrateForPriority(priority: SoundType.Priority) {
        if (!hasVibrator) return

        val pattern = when (priority) {
            SoundType.Priority.LOW -> {
                HapticVibrationPattern.LowPriority
            }

            SoundType.Priority.NORMAL -> {
                HapticVibrationPattern.NormalPriority
            }

            SoundType.Priority.HIGH -> {
                HapticVibrationPattern.HighPriority
            }

            SoundType.Priority.CRITICAL -> {
                HapticVibrationPattern.CriticalPriority
            }
        }

        repeat(pattern.repeatCount) {
            vibratePattern(pattern)
        }

        /*val (timings, amplitudes) = when (priority) {
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

        vibrateWithAmplitudes(timings, amplitudes)*/
    }

    override fun performInteractionFeedback() {
        if (!hasVibrator) return
        vibratePattern(HapticVibrationPattern.InteractionPriority)
//        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
    }

    override fun vibrate(pattern: LongArray, repeat: Int) {
        if (!hasVibrator) return
        // Build max-amplitude array for the simple pattern API
        val amplitudes = IntArray(pattern.size) { i ->
            if (i % 2 == 0) AMP_OFF else AMP_MAX
        }
        vibrateWithAmplitudes(pattern, amplitudes, repeat)
    }

    override fun vibratePattern(pattern: HapticVibrationPattern) {
        if (hasVibrator) {
            VibrationEffect.createWaveform(
                pattern.pattern,
                pattern.amplitude shouldMatchLengthOf pattern.pattern,
                pattern.repeatCount
            ).also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    vibrator.vibrate(
                        it,
                        VibrationAttributes.Builder()
                            .setUsage(VibrationAttributes.USAGE_CLASS_FEEDBACK)
                            .build()
                    )
                } else {
                    vibrator.vibrate(it)
                }
            }
        }
    }

    @Deprecated(
        message = "Will be replaced with vibratePattern",
        level = DeprecationLevel.WARNING
    )
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
