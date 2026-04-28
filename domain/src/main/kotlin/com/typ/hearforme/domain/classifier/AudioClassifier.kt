package com.typ.hearforme.domain.classifier

import com.typ.hearforme.domain.model.SoundEvent
import kotlinx.coroutines.flow.Flow

interface AudioClassifier {
    /** Stream of detected sound events */
    val events: Flow<SoundEvent>

    /** Stream of top 4 possible sound events in realtime */
    val possibleSounds: Flow<List<SoundEvent>>

    /** Stream of root-mean-square (loudness) values for visualizations */
    val rms: Flow<Float>

    /** Stream of classifier running status */
    val isRunning: Flow<Boolean>
    
    fun start()
    fun stop()
}
