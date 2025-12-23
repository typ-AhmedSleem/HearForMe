package com.typ.hearforme.presentation.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typ.hearforme.domain.communication.SpeechToTextEngine
import com.typ.hearforme.domain.communication.TextToSpeechEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class CommunicationUiState(
    val transcribedText: String = "",
    val partialTranscription: String = "",
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val error: String? = null,
)

class CommunicationViewModel(
    private val sttEngine: SpeechToTextEngine,
    private val ttsEngine: TextToSpeechEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunicationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeEngines()
    }

    private fun observeEngines() {
        sttEngine.results.onEach { text ->
            _uiState.update { it.copy(transcribedText = it.transcribedText + " " + text, partialTranscription = "") }
        }.launchIn(viewModelScope)

        sttEngine.partialResults.onEach { text ->
            _uiState.update { it.copy(partialTranscription = text) }
        }.launchIn(viewModelScope)

        sttEngine.isListening.onEach { listening ->
            _uiState.update { it.copy(isListening = listening) }
        }.launchIn(viewModelScope)

        sttEngine.error.onEach { error ->
            _uiState.update { it.copy(error = error) }
        }.launchIn(viewModelScope)

        ttsEngine.isSpeaking.onEach { speaking ->
            _uiState.update { it.copy(isSpeaking = speaking) }
        }.launchIn(viewModelScope)
    }

    fun startListening() {
        sttEngine.startListening()
    }

    fun stopListening() {
        sttEngine.stopListening()
    }

    fun speak(text: String) {
        ttsEngine.speak(text)
    }

    fun clearTranscription() {
        _uiState.update { it.copy(transcribedText = "", partialTranscription = "") }
    }

    override fun onCleared() {
        super.onCleared()
        sttEngine.release()
        ttsEngine.release()
    }
}
