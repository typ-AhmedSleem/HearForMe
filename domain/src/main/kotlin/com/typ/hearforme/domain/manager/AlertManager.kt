package com.typ.hearforme.domain.manager

import com.typ.hearforme.domain.model.SoundEvent
import kotlinx.coroutines.flow.StateFlow

interface AlertManager {
    val activeAlert: StateFlow<SoundEvent?>
    val history: StateFlow<List<SoundEvent>>

    /** Handle a new sound detection event (saves to history and triggers feedback) */
    fun onSoundDetected(event: SoundEvent)

    /** Re-triggers feedback for an existing event (haptics, flashlight) */
    fun triggerAlertFeedback(event: SoundEvent)

    /** Dismisses the current active alert */
    fun dismissAlert()

    /** Triggers the emergency SOS flow */
    fun triggerSOS()
}
