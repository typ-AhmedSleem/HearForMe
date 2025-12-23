package com.typ.hearforme.domain.policy

import com.typ.hearforme.domain.model.SoundEvent

interface DetectionPolicy {
    fun shouldAlert(
        event: SoundEvent,
        lastAlertTime: Long?
    ): Boolean
}

class DefaultDetectionPolicy : DetectionPolicy {

    private val cooldownMs = 2000L // 2 seconds default cooldown

    override fun shouldAlert(event: SoundEvent, lastAlertTime: Long?): Boolean {
        if (event.confidence < 0.5f) return false
        
        if (lastAlertTime != null) {
            val timeSinceLast = event.timestamp - lastAlertTime
            if (timeSinceLast < cooldownMs) {
                return false
            }
        }
        
        return true
    }
}

class DebouncedDetectionPolicy : DetectionPolicy {

    private val cooldownMs = 2000L
    private var lastObservedLabel: String? = null
    private var observationCount = 0

    override fun shouldAlert(event: SoundEvent, lastAlertTime: Long?): Boolean {
        // 1. Confidence check
        if (event.confidence < 0.5f) {
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
