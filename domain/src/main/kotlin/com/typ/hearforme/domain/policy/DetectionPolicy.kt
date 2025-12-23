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
        // 1. Check confidence (This might be redundant if Repository filters it, but good to have)
        if (event.confidence < 0.5f) return false
        
        // 2. Check cooldown
        if (lastAlertTime != null) {
            val timeSinceLast = event.timestamp - lastAlertTime
            if (timeSinceLast < cooldownMs) {
                return false
            }
        }
        
        return true
    }
}
