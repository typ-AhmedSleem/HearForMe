package com.typ.hearforme.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.hearforme.domain.model.SoundType
import com.typ.hearforme.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SoundSetting(
    val type: SoundType,
    val isEnabled: Boolean,
    val sensitivity: Float,
)

data class SettingsUiState(
    val isGlobalDetectionEnabled: Boolean = true,
    val soundSettings: List<SoundSetting> = emptyList(),
    val isFlashlightEnabled: Boolean = false,
    val isVibrationEnabled: Boolean = true,
)

class SettingsViewModel(
    private val repository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        combine(
            repository.isDetectionEnabled,
            repository.isFlashlightEnabled,
            repository.isVibrationEnabled
        ) { detection, flash, vibration ->
            _uiState.update {
                it.copy(
                    isGlobalDetectionEnabled = detection,
                    isFlashlightEnabled = flash,
                    isVibrationEnabled = vibration
                )
            }
        }.launchIn(viewModelScope)

        // For simplicity, we create settings for all known types
        // In a real app, this would be more dynamic
        val types = SoundType.explicitTypes

        combine(
            types.map { type ->
                repository.isSoundTypeEnabled(type).combine(repository.getSensitivity(type)) { enabled, sens ->
                    SoundSetting(type, enabled, sens)
                }
            }
        ) { settingsArray ->
            _uiState.update { it.copy(soundSettings = settingsArray.toList()) }
        }.launchIn(viewModelScope)
    }

    fun toggleGlobalDetection(enabled: Boolean) {
        viewModelScope.launch { repository.setDetectionEnabled(enabled) }
    }

    fun toggleSoundType(type: SoundType, enabled: Boolean) {
        viewModelScope.launch { repository.setSoundTypeEnabled(type, enabled) }
    }

    fun updateSensitivity(type: SoundType, sensitivity: Float) {
        viewModelScope.launch { repository.setSensitivity(type, sensitivity) }
    }

    fun toggleFlashlight(enabled: Boolean) {
        viewModelScope.launch { repository.setFlashlightEnabled(enabled) }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch { repository.setVibrationEnabled(enabled) }
    }
}
