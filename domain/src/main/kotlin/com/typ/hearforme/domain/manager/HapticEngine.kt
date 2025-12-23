package com.typ.hearforme.domain.manager

import com.typ.hearforme.domain.model.SoundType

interface HapticEngine {
    /**
     * Performs a vibration based on sound priority.
     */
    fun vibrateForPriority(priority: SoundType.Priority)

    /**
     * Performs subtle haptic feedback for UI interactions.
     */
    fun performInteractionFeedback()

    /**
     * Performs a custom vibration pattern.
     */
    fun vibrate(pattern: LongArray, repeat: Int = -1)
}