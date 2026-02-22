package com.typ.hearforme.ai.engine

import java.util.ArrayDeque

/**
 * Deterministic engine that processes raw YAMNet frames.
 * Reduces flickering, eliminates gaps, and provides stable sound event states.
 */
class SoundDecisionEngine(
    private val windowSize: Int = 5,
    private val startThreshold: Float = 0.6f,
    private val stopThreshold: Float = 0.3f,
) {
    // Sliding windows for each group
    private val histories = mutableMapOf<String, ArrayDeque<Float>>()

    // Previous states for hysteresis
    private val states = mutableMapOf<String, Boolean>()

    /**
     * Processes a single frame of classifier output.
     * @param rawResults Map of YAMNet label to its confidence score.
     */
    fun processFrame(rawResults: Map<String, Float>): List<SoundEvent> {
        val currentEvents = mutableListOf<SoundEvent>()

        // 1. Semantic Group Aggregation
        val groupScores = aggregateGroups(rawResults)

        // Process each group defined in SemanticGroups
        for (group in SemanticGroups.MAPPING.keys) {
            val rawScore = groupScores[group] ?: 0f

            // 2. Sliding Window Temporal Smoothing
            val smoothedScore = updateHistoryAndGetAverage(group, rawScore)

            // 3. Hysteresis Decision Layer
            val wasActive = states[group] ?: false
            val isActive = decideState(wasActive, smoothedScore)
            states[group] = isActive

            // 4. Output Model
            currentEvents.add(
                SoundEvent(
                    group = group,
                    confidence = smoothedScore,
                    isActive = isActive
                )
            )
        }

        return currentEvents
    }

    private fun aggregateGroups(rawResults: Map<String, Float>): Map<String, Float> {
        val groupScores = mutableMapOf<String, Float>()

        for ((group, labels) in SemanticGroups.MAPPING) {
            var sum = 0f
            for (label in labels) {
                sum += rawResults[label] ?: 0f
            }
            groupScores[group] = sum
        }

        return groupScores
    }

    private fun updateHistoryAndGetAverage(group: String, score: Float): Float {
        val history = histories.getOrPut(group) { ArrayDeque(windowSize) }

        // Push new groupScore
        history.addLast(score)

        // Remove oldest if exceeding window size
        if (history.size > windowSize) {
            history.removeFirst()
        }

        // Compute smoothedScore = average(history)
        // We divide by windowSize even if not full yet to prevent "warm-up" spikes (flicker prevention)
        return history.sum() / windowSize
    }

    private fun decideState(wasActive: Boolean, smoothedScore: Float): Boolean {
        return when {
            !wasActive && smoothedScore >= startThreshold -> true
            wasActive && smoothedScore <= stopThreshold -> false
            else -> wasActive
        }
    }

    /**
     * Resets the engine state. Useful between sessions.
     */
    fun reset() {
        histories.clear()
        states.clear()
    }
}
