package com.typ.hearforme.domain.model

data class SoundEvent(
    val type: SoundType,
    val timestamp: Long,
    val confidence: Float
)
