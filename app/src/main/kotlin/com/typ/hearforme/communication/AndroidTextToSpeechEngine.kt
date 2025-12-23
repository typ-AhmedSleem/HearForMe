package com.typ.hearforme.communication

import android.content.Context
import android.speech.tts.TextToSpeech
import com.typ.hearforme.domain.communication.TextToSpeechEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AndroidTextToSpeechEngine(private val context: Context) : TextToSpeechEngine {

    private var tts: TextToSpeech? = null

    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking = _isSpeaking.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error = _error.asStateFlow()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.ERROR) {
                _error.value = "Failed to initialize TTS"
            }
        }
    }

    override fun speak(text: String, language: String) {
        tts?.apply {
            val locale = Locale(language)
            val result = setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                _error.value = "Language $language not supported"
                return
            }

            _isSpeaking.value = true
            speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")

            // Note: In a real app we'd use setOnUtteranceProgressListener to update _isSpeaking accurately
            // For now, simple implementation
        }
    }

    override fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    override fun release() {
        tts?.shutdown()
        tts = null
    }
}
