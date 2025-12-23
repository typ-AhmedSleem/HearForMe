package com.typ.hearforme.domain.classifier

import com.typ.hearforme.domain.model.SoundEvent
import kotlinx.coroutines.flow.Flow

interface AudioClassifier {
    
    val events: Flow<SoundEvent>
    
    fun start()
    fun stop()
    
    // Optional: Pass raw data if we manage mic separately
    // fun classify(audioData: ByteArray)
}
