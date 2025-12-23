package com.typ.hearforme.domain.communication

import kotlinx.coroutines.flow.Flow

interface TextToSpeechEngine {
    val isSpeaking: Flow<Boolean>
    val error: Flow<String?>

    fun speak(text: String, language: String = "ar-SA")
    fun stop()
    fun release()
}
