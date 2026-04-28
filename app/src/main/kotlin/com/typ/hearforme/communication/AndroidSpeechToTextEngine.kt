package com.typ.hearforme.communication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.ERROR_SERVER_DISCONNECTED
import android.util.Log
import com.typ.hearforme.designsystem.R
import com.typ.hearforme.domain.communication.SpeechToTextEngine
import com.typ.hearforme.domain.model.EngineError
import com.typ.hearforme.presentation.Event
import com.typ.hearforme.presentation.EventsPipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidSpeechToTextEngine(private val context: Context) : SpeechToTextEngine {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    private val _results = MutableStateFlow("")
    override val results = _results.asStateFlow()

    private val _partialResults = MutableStateFlow("")
    override val partialResults = _partialResults.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    override val isListening = _isListening.asStateFlow()

    private val _error = MutableStateFlow<EngineError?>(null)
    override val error = _error.asStateFlow()

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("HearForMe", "onReadyForSpeech")
                _isListening.value = true
                _error.value = null
            }

            override fun onBeginningOfSpeech() {
                Log.d("HearForMe", "onBeginningOfSpeech")
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("HearForMe", "onEndOfSpeech")
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false

                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> context.getString(R.string.error_audio)
                    SpeechRecognizer.ERROR_CLIENT -> context.getString(R.string.error_client)
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> context.getString(R.string.error_insufficient_permissions)
                    SpeechRecognizer.ERROR_NETWORK, ERROR_SERVER_DISCONNECTED -> context.getString(R.string.error_network)
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> context.getString(R.string.error_network_timeout)
                    SpeechRecognizer.ERROR_NO_MATCH -> context.getString(R.string.error_no_match)
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> context.getString(R.string.error_recognizer_busy)
                    SpeechRecognizer.ERROR_SERVER -> context.getString(R.string.error_server)
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> context.getString(R.string.error_speech_timeout)
                    else -> context.getString(R.string.error_unknown, error)
                }

                _error.value = EngineError(
                    msg = errorMessage,
                    code = error,
                )

                EventsPipe.sendEvent(Event.Error(title = context.getString(R.string.speech_engine_error), message = errorMessage))
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _results.value = matches[0]
                }
                _isListening.value = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches.isNullOrEmpty()) return
                _partialResults.value = matches[0]
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("HearForMe", "onEvent: $eventType")
            }
        })
    }

    override fun startListening(language: String) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
        }
        speechRecognizer.startListening(intent)
    }

    override fun stopListening() {
        speechRecognizer.stopListening()
        _isListening.value = false
    }

    override fun release() {
        speechRecognizer.destroy()
    }
}
