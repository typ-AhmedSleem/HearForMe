package com.typ.hearforme.domain.policy

import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

interface DetectionPolicy {
    suspend fun shouldAlert(
        event: SoundEvent,
        lastAlertTime: Long?,
    ): Boolean
}

class DefaultDetectionPolicy(private val settingsRepository: SettingsRepository) : DetectionPolicy {

    private val cooldownMs = 2000L // 2 seconds default cooldown

    override suspend fun shouldAlert(event: SoundEvent, lastAlertTime: Long?): Boolean {
        val isEnabled = settingsRepository.isSoundTypeEnabled(event.type).first()
        if (!isEnabled) return false

        val sensitivity = settingsRepository.getSensitivity(event.type).first()
        // Threshold: High sensitivity (1.0) -> Low threshold (0.3), Low sensitivity (0.0) -> High threshold (0.8)
        val threshold = 0.8f - (sensitivity * 0.5f)

        if (event.confidence < threshold) return false
        
        if (lastAlertTime != null) {
            val timeSinceLast = event.timestamp - lastAlertTime
            if (timeSinceLast < cooldownMs) {
                return false
            }
        }
        
        return true
    }
}

class DebouncedDetectionPolicy(private val settingsRepository: SettingsRepository) : DetectionPolicy {

    private val cooldownMs = 2000L
    private var lastObservedLabel: String? = null
    private var observationCount = 0

    override suspend fun shouldAlert(event: SoundEvent, lastAlertTime: Long?): Boolean {
        val isEnabled = settingsRepository.isSoundTypeEnabled(event.type).first()
        if (!isEnabled) return false

        val sensitivity = settingsRepository.getSensitivity(event.type).first()
        val threshold = 0.8f - (sensitivity * 0.5f)

        // 1. Confidence check
        if (event.confidence < threshold) {
            reset()
            return false
        }

        // 2. Cooldown check
        if (lastAlertTime != null) {
            val timeSinceLast = event.timestamp - lastAlertTime
            if (timeSinceLast < cooldownMs) return false
        }

        // 3. Debounce logic: check if same as last observed
        val currentLabel = event.type.label
        if (currentLabel == lastObservedLabel) {
            observationCount++
        } else {
            lastObservedLabel = currentLabel
            observationCount = 1
        }

        // 4. Require 2 consecutive observations
        return if (observationCount >= 2) {
            reset() // Reset after alert to require another 2 for next one (or rely on cooldown)
            true
        } else {
            false
        }
    }

    private fun reset() {
        lastObservedLabel = null
        observationCount = 0
    }
}
