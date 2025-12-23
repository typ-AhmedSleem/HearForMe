package com.typ.hearforme.ai

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifier
import com.google.mediapipe.tasks.audio.audioclassifier.AudioClassifierResult
import com.google.mediapipe.tasks.components.containers.AudioData
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
import kotlin.math.sqrt
import com.typ.hearforme.domain.classifier.AudioClassifier as DomainAudioClassifier

class MediaPipeAudioClassifier(
    private val context: Context,
    private val modelPath: String = "yamnet.tflite",
    private val threshold: Float = 0.3f,
) : DomainAudioClassifier {

    private val textUnknownSound = context.getString(R.string.unknown_sound)

    private var classifier: AudioClassifier? = null
    private var audioRecord: AudioRecord? = null
    private val executor = ScheduledThreadPoolExecutor(1)

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

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun start() {
        if (classifier != null) return
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPolling() {
        Log.d("HearForMe", "Starting polling")
        audioRecord?.startRecording() ?: return
        Log.d("HearForMe", "Recording started")

        val audioClassifier = classifier ?: return
        Log.d("HearForMe", "AudioClassifier created")
        val record = audioRecord ?: return
        Log.d("HearForMe", "AudioRecord created")

        // Create TensorAudio manually
        // YAMNet input is ~15600 samples (0.975s)
        AudioFormat.Builder()
            .setSampleRate(AudioClassifierConstants.SAMPLING_RATE_IN_HZ)
            .build()
        Log.d("HearForMe", "AudioFormat created")
        val tensorAudio = AudioData.create(
            AudioData
                .AudioDataFormat
                .builder()
                .setNumOfChannels(1)
                .setSampleRate(AudioClassifierConstants.SAMPLING_RATE_IN_HZ.toFloat())
                .build(),
            15600
        )
        Log.d("HearForMe", "TensorAudio created")

        executor.scheduleWithFixedDelay(
            {
                tensorAudio.load(record)

                // Calculate RMS for visualizer
                val buffer = tensorAudio.buffer
                var sum = 0.0
                val limit = 15600 // YAMNet buffer size
                for (i in 0 until limit) {
                    val sample = buffer[i]
                    sum += (sample.toDouble() * sample.toDouble())
                }
                val rmsValue = sqrt(sum / limit.toDouble()).toFloat()
                scope.launch {
                    _rms.emit(rmsValue)
                }

                val results: AudioClassifierResult = audioClassifier.classify(tensorAudio)
                processResults(results)
            },
            0,
            100, // Faster polling for smoother visualizer
            TimeUnit.MILLISECONDS
        )
    }

    private fun processResults(results: AudioClassifierResult) {
        val classificationResults = results.classificationResults()
        if (classificationResults.isEmpty()) return

        val classifications = classificationResults.first().classifications()

        val topResult = classifications.map {
            it.categories().maxByOrNull { category -> category.score() }
        }.firstOrNull()

        val topResultScore = topResult?.score() ?: 0f
        val topResultLabel = topResult?.categoryName() ?: textUnknownSound

        if (topResultScore > threshold) {
            val soundType = SoundType.fromLabel(topResultLabel)
            if (soundType !is SoundType.Generic) {
                val event = SoundEvent(
                    type = soundType,
                    confidence = topResultScore,
                    timestamp = System.currentTimeMillis(),
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
