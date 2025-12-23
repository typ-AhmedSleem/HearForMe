package com.typ.hearforme.domain.classifier

import com.typ.hearforme.domain.model.SoundEvent
import kotlinx.coroutines.flow.Flow

interface AudioClassifier {
    
    val events: Flow<SoundEvent>
    val rms: Flow<Float>
    
    fun start()
    fun stop()
}
