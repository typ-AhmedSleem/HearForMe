package com.typ.hearforme.domain.manager

import com.typ.hearforme.domain.model.SoundEvent
import kotlinx.coroutines.flow.StateFlow

interface AlertManager {
    val activeAlert: StateFlow<SoundEvent?>
    val history: StateFlow<List<SoundEvent>>

    fun onSoundDetected(event: SoundEvent)
    fun dismissAlert()
}
