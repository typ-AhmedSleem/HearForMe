package com.typ.hearforme.domain.model

import com.typ.hearforme.designsystem.R

sealed class SoundType(
    val nameRes: Int,
    val label: String,
    val priority: Priority,
    val color: Long, // ARGB color code
    val emoji: String,
) {
    enum class Priority { LOW, NORMAL, HIGH, CRITICAL }

    // Explicit high-priority types for deaf safety
    data object BabyCrying : SoundType(
        nameRes = R.string.sound_type_baby_crying,
        label = "cry",
        priority = Priority.HIGH,
        color = 0xFFE91E63,
        emoji = "👶"
    )

    data object Doorbell : SoundType(
        nameRes = R.string.sound_type_doorbell,
        label = "bell",
        priority = Priority.HIGH,
        color = 0xFFFF9800,
        emoji = "🔔"
    )

    data object AlarmSiren : SoundType(
        nameRes = R.string.sound_type_siren,
        label = "siren",
        priority = Priority.CRITICAL,
        color = 0xFFF44336,
        emoji = "🚨"
    )

    data object SomeoneSpeaking : SoundType(
        nameRes = R.string.sound_type_someone_speaking,
        label = "speech",
        priority = Priority.LOW,
        color = 0xFFF44336,
        emoji = "🗣️"
    )

    data object DogBarking : SoundType(
        nameRes = R.string.sound_type_dog_barking,
        label = "dog",
        priority = Priority.HIGH,
        color = 0xFF795548,
        emoji = "🐕"
    )

    data object RunningWater : SoundType(
        nameRes = R.string.sound_type_microwave_done,
        label = "Beep",
        priority = Priority.NORMAL,
        color = 0xFF2196F3,
        emoji = "⏲️"
    )

    data object Silence : SoundType(
        nameRes = R.string.sound_type_silence,
        label = "Silence",
        priority = Priority.LOW,
        color = 0xFF9E9E9E,
        emoji = "🤫"
    )

    // Catch-all for any of the 521 YAMNet classes
    data class Generic(val yamnetClassName: String) : SoundType(
        emoji = "🔊",
        color = 0xFF9E9E9E,
        label = yamnetClassName,
        priority = Priority.LOW,
        nameRes = 0, // Not used for Generic
    ) {
        val displayName: String = yamnetClassName
            .replace("_", " ")
            .split(" ")
            .joinToString(" ")
            { it.replaceFirstChar { c -> c.uppercase() } }
    }

    companion object {
        fun fromLabel(label: String): SoundType {
            val lowerLabel = label.lowercase()
            // * Check direct labels first
            val directType = explicitTypes.firstOrNull {
                lowerLabel.containsIgnoringCase(it.label.lowercase())
            }
            if (directType != null) return directType

            // * Handle silence explicitly if not in direct types (though it is now)
            if (lowerLabel.containsIgnoringCase("silence")) return Silence

            // * Check alternative labels
            return when {
                // Baby sounds
                (lowerLabel.containsIgnoringCase("baby")
                        || lowerLabel.containsIgnoringCase("infant"))
                        && (lowerLabel.containsIgnoringCase("cry")
                        || lowerLabel.containsIgnoringCase("wail")) -> BabyCrying

                // Doorbell
                lowerLabel.containsIgnoringCase("doorbell")
                        || lowerLabel.containsIgnoringCase("Doorbell")
                        || lowerLabel.containsIgnoringCase("fork")
                        || lowerLabel.containsIgnoringCase("door bell") -> Doorbell

                // Alarms and sirens
                lowerLabel.containsIgnoringCase("siren")
                        || (lowerLabel.containsIgnoringCase("alarm")
                        && !lowerLabel.containsIgnoringCase("police"))
                        || lowerLabel.containsIgnoringCase("emergency") -> AlarmSiren

                // Smoke alarm
                lowerLabel.containsIgnoringCase("fire")
                        && lowerLabel.containsIgnoringCase("alarm") -> SomeoneSpeaking

                // Dog barking
                lowerLabel.containsIgnoringCase("dog")
                        && (lowerLabel.containsIgnoringCase("bark")
                        || lowerLabel.containsIgnoringCase("animal")
                        || lowerLabel.containsIgnoringCase("dog barking")) -> DogBarking

                // Water sounds
                lowerLabel.containsIgnoringCase("beep")
                        || lowerLabel.containsIgnoringCase("boop")
                        || lowerLabel.containsIgnoringCase("microwave")
                        || lowerLabel.containsIgnoringCase("bleep") -> RunningWater

                // Everything else
                else -> Generic(label)
            }
        }

        // Get all explicit (non-Generic) types for settings UI
        val explicitTypes: List<SoundType> = listOf(
            BabyCrying,
            Doorbell,
            AlarmSiren,
            SomeoneSpeaking,
            DogBarking,
            RunningWater,
        )

        private fun String.containsIgnoringCase(text: String): Boolean {
            return this.contains(text, ignoreCase = true)
        }
    }

}
