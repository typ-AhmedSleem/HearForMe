package com.typ.hearforme.ai

object AudioClassifierConstants {
    internal const val DISPLAY_THRESHOLD = 0.6f
    internal const val DEFAULT_NUM_OF_RESULTS = 1
    internal const val DEFAULT_OVERLAP = 0.5f
    internal const val GENERAL_MODEL = "yamnet.tflite"

    internal const val BUFFER_SIZE_FACTOR = 2
    internal const val SAMPLING_RATE_IN_HZ = 16000
    internal const val EXPECTED_INPUT_LENGTH = 0.975F
    internal const val REQUIRE_INPUT_BUFFER_SIZE = SAMPLING_RATE_IN_HZ * EXPECTED_INPUT_LENGTH

    /**
     * Size of the buffer where the audio data is stored by Android
     */
    internal const val BUFFER_SIZE_IN_BYTES = REQUIRE_INPUT_BUFFER_SIZE * Float.SIZE_BYTES * BUFFER_SIZE_FACTOR
}