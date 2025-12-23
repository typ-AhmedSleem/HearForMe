package com.typ.hearforme.domain.communication

import kotlinx.coroutines.flow.Flow

interface SpeechToTextEngine {
    val results: Flow<String>
    val partialResults: Flow<String>
    val isListening: Flow<Boolean>
    val error: Flow<String?>

    fun startListening(language: String = "ar-SA")
    fun stopListening()
    fun release()
}
