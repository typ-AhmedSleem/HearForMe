package com.typ.hearforme.presentation.dashboard

import android.util.Log
import androidx.compose.runtime.Immutable
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
import kotlinx.coroutines.Job
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Immutable
sealed interface DashboardUiState {
    object MicAccessRequired : DashboardUiState
    object ServiceOffline : DashboardUiState
    data class Identifying(val nextPray: Pray?, val possibleSounds: List<SoundEvent> = emptyList()) : DashboardUiState
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
//            timezone = java.util.TimeZone.getDefault().id
        }
    )

    private val _nextPrayFlow = MutableStateFlow<Pray?>(null)

    private val _isMicPermissionGranted = MutableStateFlow(true)
    val isMicPermissionGranted = _isMicPermissionGranted.asStateFlow()

    private val _requestPermissionTrigger = MutableSharedFlow<Unit>()
    val requestPermissionTrigger: SharedFlow<Unit> = _requestPermissionTrigger.asSharedFlow()

    private var jobTrackNextPrayTime: Job? = null

    init {
        trackNextPrayTime()
    }

    internal fun trackNextPrayTime() {
        jobTrackNextPrayTime?.cancel()
        jobTrackNextPrayTime = viewModelScope.launch {
            Log.i("DashboardViewModel", "trackNextPrayTime is called.")
            while (isActive) {
                val todayPrays = PrayerTimes.getTodayPrays(praysCalculator)
                val next = PrayerTimes.getNextPray(todayPrays) ?: PrayerTimes.getPrays(praysCalculator, Timestamp.tomorrow()).fajr

                _nextPrayFlow.value = next

                var delayMillis = next.time.toMillis() - System.currentTimeMillis()
                while (delayMillis > 0) {
                    delayMillis -= 1000
                    delay(1000)
                }
                triggerPrayTimeAlert(next)
            }
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        isMicPermissionGranted,
        settingsRepository.isDetectionEnabled,
        classifier.isRunning,
        alertManager.activeAlert,
        _nextPrayFlow,
        classifier.possibleSounds,
    ) { combined ->
        val hasMic = combined[0] as Boolean
        val isEnabled = combined[1] as Boolean
        val isRunning = combined[2] as Boolean
        val activeAlert = combined[3] as SoundEvent?
        val nextPray = combined[4] as Pray?
        val possibleSounds = combined[5] as List<SoundEvent>
        when {
            !hasMic -> DashboardUiState.MicAccessRequired
            !isEnabled || !isRunning -> DashboardUiState.ServiceOffline
            activeAlert != null -> DashboardUiState.Identified(activeAlert)
            else -> DashboardUiState.Identifying(nextPray, possibleSounds)
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

    fun triggerPrayTimeAlert(pray: Pray) {
        Log.i("DashboardViewModel", "triggerPrayTimeAlert is called.")
        // Trigger alert
        val eventType = when (pray.type) {
            FAJR -> SoundType.PrayTime(prayNameRes = R.string.fajr)
            SUNRISE -> SoundType.PrayTime(prayNameRes = R.string.sunrise)
            DHUHR -> SoundType.PrayTime(prayNameRes = R.string.dhuhr)
            ASR -> SoundType.PrayTime(prayNameRes = R.string.asr)
            MAGHRIB -> SoundType.PrayTime(prayNameRes = R.string.maghrib)
            ISHA -> SoundType.PrayTime(prayNameRes = R.string.isha)
        }
        alertManager.onSoundDetected(
            SoundEvent(
                type = eventType,
                confidence = 1.0f,
                timestamp = System.currentTimeMillis()
            )
        )
        Log.i("DashboardViewModel", "triggerPrayTimeAlert finished..")
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
