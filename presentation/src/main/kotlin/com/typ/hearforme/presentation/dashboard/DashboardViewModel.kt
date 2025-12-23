package com.typ.hearforme.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface DashboardUiState {
    object MicAccessRequired : DashboardUiState
    object ServiceOffline : DashboardUiState
    object Identifying : DashboardUiState
    data class Identified(val event: SoundEvent) : DashboardUiState
}

class DashboardViewModel(
    private val alertManager: AlertManager,
    private val classifier: AudioClassifier,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _isMicPermissionGranted = MutableStateFlow(true) // Default to true, updated from UI
    val isMicPermissionGranted = _isMicPermissionGranted.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        isMicPermissionGranted,
        settingsRepository.isDetectionEnabled,
        classifier.isRunning,
        alertManager.activeAlert
    ) { hasMic, isEnabled, isRunning, activeAlert ->
        when {
            !hasMic -> DashboardUiState.MicAccessRequired
            !isEnabled || !isRunning -> DashboardUiState.ServiceOffline
            activeAlert != null -> DashboardUiState.Identified(activeAlert)
            else -> DashboardUiState.Identifying
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState.ServiceOffline)

    val history: StateFlow<List<SoundEvent>> = alertManager.history
    val rms: StateFlow<Float> = classifier.rms.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    fun onPermissionResult(granted: Boolean) {
        _isMicPermissionGranted.value = granted
    }

    fun dismissAlert() {
        alertManager.dismissAlert()
    }

    fun triggerAlertFeedback(event: SoundEvent) {
        alertManager.triggerAlertFeedback(event)
    }

    fun triggerSOS() {
        alertManager.triggerSOS()
    }
}
