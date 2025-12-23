package com.typ.hearforme.manager

import android.content.Context
import android.hardware.camera2.CameraManager
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.manager.HapticEngine
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import com.typ.hearforme.domain.repository.HistoryRepository
import com.typ.hearforme.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlertManagerImpl(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val historyRepository: HistoryRepository,
    private val hapticEngine: HapticEngine,
) : AlertManager {

    private val _activeAlert = MutableStateFlow<SoundEvent?>(null)
    override val activeAlert = _activeAlert.asStateFlow()

    override val history: StateFlow<List<SoundEvent>> = historyRepository.getHistory()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onSoundDetected(event: SoundEvent) {
        // Save to persistent history
        scope.launch {
            historyRepository.saveEvent(event)
        }

        // Check if we should show overlay (High/Critical)
        if (event.confidence > 0.7f) {
            _activeAlert.value = event
            triggerFeedback(event.type.priority)
        }
    }

    override fun dismissAlert() {
        _activeAlert.value = null
    }

    override fun triggerAlertFeedback(event: SoundEvent) {
        triggerFeedback(event.type.priority)
    }

    override fun triggerSOS() {
        val sosEvent = SoundEvent(
            type = SoundType.AlarmSiren,
            confidence = 1.0f,
            timestamp = System.currentTimeMillis()
        )
        _activeAlert.value = sosEvent

        // Intense strobe
        scope.launch {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return@launch
            repeat(10) {
                cameraManager.setTorchMode(cameraId, true)
                delay(50)
                cameraManager.setTorchMode(cameraId, false)
                delay(50)
            }
        }

        // Critical vibration via engine
        hapticEngine.vibrateForPriority(SoundType.Priority.CRITICAL)
    }

    private fun triggerFeedback(priority: SoundType.Priority) {
        scope.launch {
            if (settingsRepository.isVibrationEnabled.first()) {
                hapticEngine.vibrateForPriority(priority)
            }
            if (settingsRepository.isFlashlightEnabled.first()) {
                strobe()
            }
        }
    }

    private fun strobe() {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        scope.launch {
            try {
                val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return@launch
                repeat(5) {
                    cameraManager.setTorchMode(cameraId, true)
                    delay(100)
                    cameraManager.setTorchMode(cameraId, false)
                    delay(100)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
