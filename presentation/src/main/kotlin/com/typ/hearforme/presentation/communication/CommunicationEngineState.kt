package com.typ.hearforme.presentation.communication

/**
 * Represents the different states of the Speech-To-Text engine for the Communication UI.
 */
sealed class CommunicationEngineState {
    /**
     * The engine is ready to start listening.
     */
    object Ready : CommunicationEngineState()

    /**
     * The engine is actively listening and transcribing speech.
     */
    object Listening : CommunicationEngineState()

    /**
     * The microphone is currently in use by another application.
     */
    object Busy : CommunicationEngineState()

    /**
     * Communication is offline due to connectivity issues.
     */
    object Offline : CommunicationEngineState()
}
