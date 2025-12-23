package com.typ.hearforme.domain.repository

import com.typ.hearforme.domain.model.SoundType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    // Onboarding
    val hasCompletedOnboarding: Flow<Boolean>
    
    // Global toggle
    val isDetectionEnabled: Flow<Boolean>
    
    // Per-sound settings
    fun isSoundTypeEnabled(type: SoundType): Flow<Boolean>
    
    // 0.0 (Low) to 1.0 (High) - used to filter confidence
    fun getSensitivity(type: SoundType): Flow<Float>
    
    // Alert settings
    val isFlashlightEnabled: Flow<Boolean>
    val isVibrationEnabled: Flow<Boolean>

    // Update functions
    suspend fun setOnboardingCompleted()
    suspend fun setDetectionEnabled(enabled: Boolean)
    suspend fun setSoundTypeEnabled(type: SoundType, enabled: Boolean)
    suspend fun setSensitivity(type: SoundType, sensitivity: Float)
    suspend fun setFlashlightEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
}
