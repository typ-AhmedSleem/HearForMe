package com.typ.hearforme.manager

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlertManagerImpl(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
) : AlertManager {

    private val _activeAlert = MutableStateFlow<SoundEvent?>(null)
    override val activeAlert = _activeAlert.asStateFlow()

    private val _history = MutableStateFlow<List<SoundEvent>>(emptyList())
    override val history = _history.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onSoundDetected(event: SoundEvent) {
        // Update history
        val currentHistory = _history.value.toMutableList()
        currentHistory.add(0, event)
        if (currentHistory.size > 50) currentHistory.removeAt(50)
        _history.value = currentHistory

        // Check if we should show overlay (High/Critical)
        // For now, any event with confidence > 0.7 triggers it
        if (event.confidence > 0.7f) {
            _activeAlert.value = event

            // Trigger feedback
            triggerFeedback()
        }
    }

    override fun dismissAlert() {
        _activeAlert.value = null
    }

    private fun triggerFeedback() {
        scope.launch {
            if (settingsRepository.isVibrationEnabled.first()) {
                vibrate()
            }
            // Flashlight implementation would go here (needs CameraManager)
        }
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
