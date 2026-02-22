package com.typ.hearforme.ai.engine

/**
 * Deterministic output from the SoundDecisionEngine.
 */
data class SoundEvent(
    val group: String,
    val confidence: Float,
    val isActive: Boolean,
)
