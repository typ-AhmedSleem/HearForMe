package com.typ.hearforme.manager

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
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
        // Handle Silence: Reset UI state to identifying
        if (event.type is SoundType.Silence) {
            _activeAlert.value = null
            Log.d("HearForMe", "Silence detected, resetting UI.")
            return
        }

        val activeAlertLabel = _activeAlert.value?.type?.label ?: ""
        if (event.type.label.equals(activeAlertLabel, true)) return

        // * Set as active alert
        _activeAlert.value = event
        triggerFeedback(event.type.priority)

        // * Save to persistent history
        scope.launch {
            historyRepository.saveEvent(event)
        }

        Log.d("HearForMe", "AlertManager::onSoundDetected '$event'.")
    }

    override suspend fun clearHistory() {
        historyRepository.clearHistory()
    }

    override fun dismissAlert() {
        _activeAlert.value = null
    }

    override fun triggerAlertFeedback(event: SoundEvent) {
        triggerFeedback(event.type.priority)
    }

    /**
     * Triggers a manual SOS alert.
     *
     * This function is designed to be invoked directly by the user in an emergency situation.
     * It performs the following actions to maximize attention:
     * 1. Creates a `SoundEvent` of type `AlarmSiren` with maximum confidence and sets it as the
     *    `activeAlert`, which will typically display a full-screen alert UI.
     * 2. Initiates an intense, rapid flashlight strobe effect (10 flashes) to provide a strong
     *    visual cue. This is more aggressive than the standard alert strobe.
     * 3. Triggers a "CRITICAL" priority haptic feedback pattern via the `hapticEngine`,
     *    providing a powerful tactile alert.
     *
     * This function bypasses normal sound detection and user settings for feedback, ensuring that
     * maximum visual and haptic feedback is always provided when the user requests SOS.
     */
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

    /**
     * Triggers feedback mechanisms based on user settings and the priority of a detected sound.
     *
     * This function launches a coroutine to perform the feedback actions asynchronously. It checks
     * the user's settings to determine whether to activate haptic feedback (vibration) and/or
     * visual feedback (flashlight strobe).
     *
     * @param priority The priority level of the sound event, which determines the intensity
     * and pattern of the feedback.
     * @see HapticEngine.vibrateForPriority
     * @see strobe
     */
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

    /**
     * Triggers a strobe effect using the device's flashlight.
     *
     * This function accesses the camera service to control the torch (flashlight). It rapidly
     * turns the flashlight on and off five times with a 100ms delay between each state change,
     * creating a flashing or strobing effect. This is intended to serve as a visual alert.
     * The operation is performed within a coroutine on a background thread. Any exceptions,
     * such as issues accessing the camera, are caught and printed to the stack trace.
     */
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
