package com.typ.hearforme.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.model.SoundEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val alertManager: AlertManager,
    private val classifier: AudioClassifier,
) : ViewModel() {

    val activeAlert: StateFlow<SoundEvent?> = alertManager.activeAlert
    val history: StateFlow<List<SoundEvent>> = alertManager.history
    val rms: StateFlow<Float> = classifier.rms.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    fun dismissAlert() {
        alertManager.dismissAlert()
    }

    fun triggerSOS() {
        alertManager.triggerSOS()
    }
}
