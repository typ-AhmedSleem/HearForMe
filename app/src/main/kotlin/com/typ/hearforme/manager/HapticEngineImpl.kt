package com.typ.hearforme.manager

import android.content.Context
import android.media.AudioAttributes
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

        vibratePattern(pattern)
    }

    override fun performInteractionFeedback() {
        if (!hasVibrator) return
        vibratePattern(HapticVibrationPattern.InteractionPriority)
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
            ).also { effect ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attributes = VibrationAttributes.Builder().apply {
                        when (pattern) {
                            is HapticVibrationPattern.InteractionPriority -> {
                                setUsage(VibrationAttributes.USAGE_TOUCH)
                            }

                            is HapticVibrationPattern.CriticalPriority -> {
                                setUsage(VibrationAttributes.USAGE_ALARM)
                                setFlags(
                                    VibrationAttributes.FLAG_BYPASS_INTERRUPTION_POLICY,
                                    VibrationAttributes.FLAG_BYPASS_INTERRUPTION_POLICY
                                )
                            }

                            else -> {
                                // Sound detection alerts (Low, Normal, High)
                                setUsage(VibrationAttributes.USAGE_NOTIFICATION)
                                setFlags(
                                    VibrationAttributes.FLAG_BYPASS_INTERRUPTION_POLICY,
                                    VibrationAttributes.FLAG_BYPASS_INTERRUPTION_POLICY
                                )
                            }
                        }
                    }.build()
                    vibrator.vibrate(effect, attributes)
                } else {
                    @Suppress("DEPRECATION")
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .apply {
                            when (pattern) {
                                is HapticVibrationPattern.InteractionPriority -> {
                                    setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                }

                                is HapticVibrationPattern.CriticalPriority -> {
                                    setUsage(AudioAttributes.USAGE_ALARM)
                                }

                                else -> {
                                    setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                }
                            }
                        }.build()
                    vibrator.vibrate(effect, audioAttributes)
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
