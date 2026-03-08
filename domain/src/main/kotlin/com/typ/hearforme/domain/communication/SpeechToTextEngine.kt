package com.typ.hearforme.domain.communication

import com.typ.hearforme.domain.model.EngineError
import kotlinx.coroutines.flow.Flow

interface SpeechToTextEngine {
    val results: Flow<String>
    val partialResults: Flow<String>
    val isListening: Flow<Boolean>
    val error: Flow<EngineError?>

    fun startListening(language: String = "ar")
    fun stopListening()
    fun release()
}
