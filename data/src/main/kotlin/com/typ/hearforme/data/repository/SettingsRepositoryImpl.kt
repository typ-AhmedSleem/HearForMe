package com.typ.hearforme.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.typ.hearforme.domain.model.SoundType
import com.typ.hearforme.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    private val KEY_DETECTION_ENABLED = booleanPreferencesKey("detection_enabled")
    private val KEY_FLASHLIGHT_ENABLED = booleanPreferencesKey("flashlight_enabled")
    private val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")

    override val isDetectionEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_DETECTION_ENABLED] ?: true }

    override fun isSoundTypeEnabled(type: SoundType): Flow<Boolean> {
        val key = booleanPreferencesKey("sound_enabled_${type.name}")
        return context.dataStore.data.map { it[key] ?: true }
    }

    override fun getSensitivity(type: SoundType): Flow<Float> {
        val key = floatPreferencesKey("sound_sensitivity_${type.name}")
        return context.dataStore.data.map { it[key] ?: 0.5f }
    }

    override val isFlashlightEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_FLASHLIGHT_ENABLED] ?: true }

    override val isVibrationEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_VIBRATION_ENABLED] ?: true }

    override suspend fun setDetectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DETECTION_ENABLED] = enabled }
    }

    override suspend fun setSoundTypeEnabled(type: SoundType, enabled: Boolean) {
        val key = booleanPreferencesKey("sound_enabled_${type.name}")
        context.dataStore.edit { it[key] = enabled }
    }

    override suspend fun setSensitivity(type: SoundType, sensitivity: Float) {
        val key = floatPreferencesKey("sound_sensitivity_${type.name}")
        context.dataStore.edit { it[key] = sensitivity }
    }

    override suspend fun setFlashlightEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_FLASHLIGHT_ENABLED] = enabled }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VIBRATION_ENABLED] = enabled }
    }
}
