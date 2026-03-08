package com.typ.hearforme.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.policy.DetectionPolicy
import com.typ.hearforme.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepository: SettingsRepository,
    private val classifier: AudioClassifier,
    private val policy: DetectionPolicy,
    private val alertManager: AlertManager,
) : ViewModel() {

    private var lastAlertTime = 0L

    val hasCompletedOnboarding: StateFlow<Boolean> = settingsRepository.hasCompletedOnboarding
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isDetectionEnabled: StateFlow<Boolean> = settingsRepository.isDetectionEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
        }
    }

    fun startDetection() {
        /*viewModelScope.launch {
//            if (classifier.isRunning.lastOrNull() == true) return@launch
            classifier.start()
            classifier.events.collect { event ->
                if (policy.shouldAlert(event, lastAlertTime)) {
                    alertManager.onSoundDetected(event)
                    lastAlertTime = event.timestamp
                }
            }
        }*/
    }

    fun stopDetection() {
        classifier.stop()
    }
}
