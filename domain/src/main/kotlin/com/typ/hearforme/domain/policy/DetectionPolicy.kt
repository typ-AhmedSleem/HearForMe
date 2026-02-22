package com.typ.hearforme.domain.policy

import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
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

    private val cooldownMs = 250L
    private var lastObservedLabel: String? = null
    private var lastReportedLabel: String? = null
    private var observationCount = 0

    override suspend fun shouldAlert(event: SoundEvent, lastAlertTime: Long?): Boolean {
        val isEnabled = settingsRepository.isSoundTypeEnabled(event.type).first()
        if (!isEnabled) return false

        val sensitivity = settingsRepository.getSensitivity(event.type).first()
        //        val threshold = 0.8f - (sensitivity * 0.5f)

        // 1. Confidence check
        if (event.confidence < sensitivity) {
//            reset()
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
            if (lastReportedLabel != currentLabel) {
                observationCount++
            }
        } else {
            lastObservedLabel = currentLabel
            observationCount = 1
        }

        // 3.1 Require 5 consecutive observations (only for silence)
        if (currentLabel == SoundType.Silence.label) {
            return if (observationCount >= 5 && lastReportedLabel != currentLabel) {
                lastReportedLabel = currentLabel
                reset() // Reset after alert to require another 5 for next one (or rely on cooldown)
                return true
            } else {
                false
            }
        }

        // 4. Require 2 consecutive observations (for all labels except silence)
        return if (observationCount >= 1) {
            lastReportedLabel = currentLabel
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
