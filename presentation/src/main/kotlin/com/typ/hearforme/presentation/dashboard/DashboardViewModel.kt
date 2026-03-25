package com.typ.hearforme.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.hearforme.designsystem.R
import com.typ.hearforme.domain.classifier.AudioClassifier
import com.typ.hearforme.domain.manager.AlertManager
import com.typ.hearforme.domain.manager.HapticEngine
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import com.typ.hearforme.domain.repository.SettingsRepository
import com.typ.islamictkt.datetime.Timestamp
import com.typ.islamictkt.location.PopularLocations
import com.typ.islamictkt.prays.enums.AsrMethod
import com.typ.islamictkt.prays.enums.CalculationMethod
import com.typ.islamictkt.prays.enums.HigherLatitudeMethod
import com.typ.islamictkt.prays.enums.PrayType.ASR
import com.typ.islamictkt.prays.enums.PrayType.DHUHR
import com.typ.islamictkt.prays.enums.PrayType.FAJR
import com.typ.islamictkt.prays.enums.PrayType.ISHA
import com.typ.islamictkt.prays.enums.PrayType.MAGHRIB
import com.typ.islamictkt.prays.enums.PrayType.SUNRISE
import com.typ.islamictkt.prays.lib.PrayerTimesCalculator
import com.typ.islamictkt.prays.models.Pray
import com.typ.islamictkt.prays.models.PrayerTimes
import com.typ.islamictkt.prays.utils.prayerTimesCalcConfig
import kotlinx.coroutines.delay
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

    private val praysCalculator = PrayerTimesCalculator(
        location = PopularLocations.Egypt.CAIRO,
        config = prayerTimesCalcConfig {
            calcMethod = CalculationMethod.EGYPT
            asrMethod = AsrMethod.SHAFII
            higherLatMethod = HigherLatitudeMethod.ONESEVENTH
        }
    )

    private val _nextPray = MutableStateFlow<Pray?>(null)
    val nextPray = _nextPray.asStateFlow()

    private val _isMicPermissionGranted = MutableStateFlow(true)
    val isMicPermissionGranted = _isMicPermissionGranted.asStateFlow()

    private val _requestPermissionTrigger = MutableSharedFlow<Unit>()
    val requestPermissionTrigger: SharedFlow<Unit> = _requestPermissionTrigger.asSharedFlow()

    init {
        trackNextPrayTime()
    }

    private fun trackNextPrayTime() {
        viewModelScope.launch {
            while (true) {
                val todayPrays = PrayerTimes.getTodayPrays(praysCalculator)
                val next = PrayerTimes.getNextPray(todayPrays) ?: PrayerTimes.getPrays(praysCalculator, Timestamp.tomorrow()).fajr

                _nextPray.value = next

                val delayMillis = next.time.toMillis() - System.currentTimeMillis()
                if (delayMillis > 0) {
                    delay(delayMillis)
                    // Trigger alert
                    val eventType = when (next.type) {
                        FAJR -> SoundType.PrayTime(prayNameRes = R.string.fajr)
                        SUNRISE -> SoundType.PrayTime(prayNameRes = R.string.sunrise)
                        DHUHR -> SoundType.PrayTime(prayNameRes = R.string.dhuhr)
                        ASR -> SoundType.PrayTime(prayNameRes = R.string.asr)
                        MAGHRIB -> SoundType.PrayTime(prayNameRes = R.string.maghrib)
                        ISHA -> SoundType.PrayTime(prayNameRes = R.string.isha)
                    }
                    triggerAlertFeedback(
                        SoundEvent(
                            type = eventType,
                            confidence = 1.0f,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                delay(1000) // Small buffer
            }
        }
    }

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
