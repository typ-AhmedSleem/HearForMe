package com.typ.hearforme.ai

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifierResult
import com.google.mediapipe.tasks.components.containers.AudioData
import com.google.mediapipe.tasks.core.BaseOptions
import com.typ.hearforme.ai.engine.SoundDecisionEngine
import com.typ.hearforme.domain.interceptors.ClassifierInterceptor
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.typ.hearforme.domain.classifier.AudioClassifier as DomainAudioClassifier

class MediaPipeAudioClassifier(
    private val context: Context,
    private val modelPath: String = "yamnet.tflite",
    private val threshold: Float = 0.3f,
    private val interceptors: List<ClassifierInterceptor> = emptyList(),
) : DomainAudioClassifier {

    private val engine = SoundDecisionEngine(
        windowSize = 5,
        startThreshold = 0.6f,
        stopThreshold = 0.3f
    )

    private val textUnknownSound = context.getString(R.string.unknown_sound)

    private var classifier: AudioClassifier? = null
    private var audioRecord: AudioRecord? = null
    private var pollingJob: Job? = null

    var lastLabel = ""
    private val _events = MutableSharedFlow<SoundEvent>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    private val _rms = MutableSharedFlow<Float>(
        replay = 1,
        extraBufferCapacity = 5,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val rms = _rms.asSharedFlow()

    private val _isRunning = MutableStateFlow(false)
    override val isRunning = _isRunning.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun start() {
        if (_isRunning.value) return
        _isRunning.value = true
        Log.d("HearForMe", "Starting classifier")

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(modelPath)
            .build()

        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setScoreThreshold(threshold)
            .build()

        try {
            classifier = AudioClassifier.createFromOptions(context, options)

            // YAMNet requires 16kHz
            audioRecord = classifier?.createAudioRecord(
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioClassifierConstants.SAMPLING_RATE_IN_HZ,
                AudioClassifierConstants.BUFFER_SIZE_IN_BYTES.toInt()
            ) // Defaults are usually fine: 44.1kHz, mono

            // Start polling
            startPolling()
        } catch (e: Throwable) {
            e.printStackTrace()
            _isRunning.value = false
            Log.e("HearForMe", "Error with classifier. Reason: ${e.message}", e)
        }
    }

    private fun startPolling() {
        Log.d("HearForMe", "Starting polling (coroutines)")
        val record = audioRecord ?: return
        val audioClassifier = classifier ?: return

        record.startRecording()

        val tensorAudio = AudioData.create(
            AudioData.AudioDataFormat.builder()
                .setNumOfChannels(1)
                .setSampleRate(AudioClassifierConstants.SAMPLING_RATE_IN_HZ.toFloat())
                .build(),
            15600
        )

        pollingJob = scope.launch {
            while (isActive && _isRunning.value) {
                try {
                    tensorAudio.load(record)
                    val results = audioClassifier.classify(tensorAudio)
                    processResults(results)
                } catch (t: Throwable) {
                    Log.e("HearForMe", "Polling error", t)
                    break
                }
                delay(100)
            }
        }
    }

    private fun processResults(results: AudioClassifierResult) {
        val classificationResults = results.classificationResults()
        if (classificationResults.isEmpty()) return

        val classifications = classificationResults.first().classifications()

        // Prepare raw results for the engine (label -> score)
        val rawResults = mutableMapOf<String, Float>()
        classifications.forEach { classification ->
            classification.categories().forEach { category ->
                rawResults[category.categoryName()] = category.score()
                Log.i("HearForMe", "Raw Result: ${category.categoryName()} (${category.score()})")
            }
        }

        // --- PRODUCTION-SAFE SOUND DECISION ENGINE (Deterministic Smoothing & Hysteresis) ---
        val engineEvents = engine.processFrame(rawResults)
        val activeEvents = engineEvents.filter { it.isActive }

        if (activeEvents.isEmpty()) {
            // Signal Silence/Reset state
            scope.launch {
                val event = SoundEvent(
                    type = SoundType.Silence,
                    confidence = 1.0f,
                    timestamp = System.currentTimeMillis()
                )
                _events.emit(event)
            }
        } else {
            // Emit all active sound events
            activeEvents.forEach { engineEvent ->
//                Log.d("HearForMe", "Engine Event: ${engineEvent.group} (${engineEvent.confidence})")
                val soundType = SoundType.fromLabel(engineEvent.group)
                if (soundType !is SoundType.Generic) {
                    val event = SoundEvent(
                        type = soundType,
                        confidence = engineEvent.confidence,
                        timestamp = System.currentTimeMillis(),
                    )
                    scope.launch {
                        emitIntercepted(event)
                    }
                }
            }
        }
    }

    private suspend fun emitIntercepted(event: SoundEvent) {
        /*var currentEvent: SoundEvent = event
        for (interceptor in interceptors) {
            currentEvent = currentEvent?.let { interceptor.intercept(it) }
        }*/

        val interceptedEvent = interceptors.first().intercept(event)
            .also {
                Log.i("HearForMe", "Intercepted event '${it.type.label}' => '${it.type.label}' (${it.confidence})")
            }
        _events.emit(interceptedEvent)
    }

    override fun stop() {
        if (!_isRunning.value) return
        _isRunning.value = false

        pollingJob?.cancel()
        pollingJob = null

        try {
            audioRecord?.stop()
        } catch (_: Throwable) {
        }

        try {
            classifier?.close()
        } catch (_: Throwable) {
        }

        audioRecord = null
        classifier = null
    }
}
