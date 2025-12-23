package com.typ.hearforme.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.hearforme.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val hasCompletedOnboarding: StateFlow<Boolean> = settingsRepository.hasCompletedOnboarding
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isDetectionEnabled: StateFlow<Boolean> = settingsRepository.isDetectionEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
        }
    }
}
