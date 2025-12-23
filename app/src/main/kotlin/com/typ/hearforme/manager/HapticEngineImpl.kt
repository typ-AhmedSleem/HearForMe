package com.typ.hearforme.manager

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.typ.hearforme.domain.manager.HapticEngine
import com.typ.hearforme.domain.model.SoundType

class HapticEngineImpl(context: Context) : HapticEngine {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun vibrateForPriority(priority: SoundType.Priority) {
        val pattern = when (priority) {
            SoundType.Priority.CRITICAL -> longArrayOf(0, 200, 100, 200, 100, 500, 100, 200)
            SoundType.Priority.HIGH -> longArrayOf(0, 300, 100, 300, 100, 300)
            SoundType.Priority.NORMAL -> longArrayOf(0, 400, 200, 400)
            SoundType.Priority.LOW -> longArrayOf(0, 300)
        }
        vibrate(pattern)
    }

    override fun performInteractionFeedback() {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    }

    override fun vibrate(pattern: LongArray, repeat: Int) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
    }
}
