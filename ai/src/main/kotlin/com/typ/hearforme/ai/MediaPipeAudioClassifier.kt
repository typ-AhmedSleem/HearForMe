package com.typ.hearforme.ai

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifierResult
import com.google.mediapipe.tasks.components.containers.AudioData.*
import com.google.mediapipe.tasks.core.BaseOptions
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import com.typ.hearforme.domain.classifier.AudioClassifier as DomainAudioClassifier

class MediaPipeAudioClassifier(
    private val context: Context,
    private val modelPath: String = "yamnet.tflite",
    private val threshold: Float = 0.3f,
) : DomainAudioClassifier {

    private var classifier: AudioClassifier? = null
    private var audioRecord: AudioRecord? = null
    private val executor = ScheduledThreadPoolExecutor(1)

    // Emissions should be hot
    private val _events = MutableSharedFlow<SoundEvent>(
        replay = 0,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val events = _events.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun start() {
        if (classifier != null) return

        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(modelPath)
            .build()

        val options = AudioClassifier.AudioClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setScoreThreshold(threshold)
            .build()

        try {
            AudioDataFormat
                .builder()
                .setNumOfChannels()
                .setSampleRate()
                .build()
            classifier = AudioClassifier.createFromOptions(context, options)
            audioRecord = classifier?.createAudioRecord(
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioClassifierConstants.SAMPLING_RATE_IN_HZ,
                AudioClassifierConstants.BUFFER_SIZE_IN_BYTES.toInt()
            ) // Defaults are usually fine: 44.1kHz, mono

            // Start polling
            startPolling()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPolling() {
        audioRecord?.startRecording()

        executor.scheduleWithFixedDelay({
            val audioClassifier = classifier ?: return@scheduleWithFixedDelay
            val record = audioRecord ?: return@scheduleWithFixedDelay

            // Capture audio
            // record.read(buffer) ? 
            // MediaPipe Java API helper: classify(tensorAudio)

            val tensorAudio = audioClassifier.createInputTensorAudio()
            tensorAudio.load(record)

            val results: AudioClassifierResult = audioClassifier.classify(tensorAudio)
            processResults(results)

        }, 0, 500, TimeUnit.MILLISECONDS) // Run every 500ms
    }

    private fun processResults(results: AudioClassifierResult) {
        val classificationResults = results.classificationResults()
        if (classificationResults.isEmpty()) return

        // head index 0 is usually the main head
        val classifications = classificationResults[0].classifications()

        val topResult = classifications.maxByOrNull { it.score() } ?: return

        if (topResult.score() > threshold) {
            val soundType = SoundType.fromLabel(topResult.categoryName())
            if (soundType != SoundType.UNKNOWN) {
                val event = SoundEvent(
                    type = soundType,
                    timestamp = System.currentTimeMillis(),
                    confidence = topResult.score()
                )
                scope.launch {
                    _events.emit(event)
                }
            }
        }
    }

    override fun stop() {
        executor.shutdown()
        audioRecord?.stop()
        classifier?.close()
        classifier = null
        audioRecord = null
    }
}
