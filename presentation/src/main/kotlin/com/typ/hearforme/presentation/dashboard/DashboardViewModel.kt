package com.typ.hearforme.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.manager.HapticEngine
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    object MicAccessRequired : DashboardUiState
    object ServiceOffline : DashboardUiState
    object Identifying : DashboardUiState
    data class Identified(val event: SoundEvent) : DashboardUiState
}

class DashboardViewModel(
    private val alertManager: AlertManager,
    private val classifier: AudioClassifier,
    private val settingsRepository: SettingsRepository,
    private val hapticEngine: HapticEngine,
) : ViewModel() {

    private val _isMicPermissionGranted = MutableStateFlow(true)
    val isMicPermissionGranted = _isMicPermissionGranted.asStateFlow()

    private val _requestPermissionTrigger = MutableSharedFlow<Unit>()
    val requestPermissionTrigger: SharedFlow<Unit> = _requestPermissionTrigger.asSharedFlow()

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
    val activeAlert: StateFlow<SoundEvent?> = alertManager.activeAlert
    val rms: StateFlow<Float> = classifier.rms.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    fun onPermissionResult(granted: Boolean) {
        _isMicPermissionGranted.value = granted
        if (granted) performHapticFeedback()
    }

    fun enableDetection() {
        viewModelScope.launch {
            settingsRepository.setDetectionEnabled(true)
            performHapticFeedback()
        }
    }

    fun disableDetection() {
        viewModelScope.launch {
            settingsRepository.setDetectionEnabled(false)
            performHapticFeedback()
        }
    }

    fun triggerPermissionRequest() {
        viewModelScope.launch {
            _requestPermissionTrigger.emit(Unit)
            performHapticFeedback()
        }
    }

    fun dismissAlert() {
        alertManager.dismissAlert()
        performHapticFeedback()
    }

    fun triggerAlertFeedback(event: SoundEvent) {
        alertManager.triggerAlertFeedback(event)
    }

    fun triggerSOS() {
        alertManager.triggerSOS()
        performHapticFeedback()
    }

    fun performHapticFeedback() {
        hapticEngine.performInteractionFeedback()
    }

    fun clearHistory() {
        viewModelScope.launch { alertManager.clearHistory() }
    }
}
