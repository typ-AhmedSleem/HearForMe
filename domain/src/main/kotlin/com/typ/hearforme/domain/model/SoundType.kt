package com.typ.hearforme.domain.model

sealed class SoundType(
    val displayName: String,
    val priority: Priority,
    val color: Long, // ARGB color code
    val emoji: String,
) {
    enum class Priority { LOW, NORMAL, HIGH, CRITICAL }

    // Explicit high-priority types for deaf safety
    data object BabyCrying : SoundType(
        displayName = "Baby Crying",
        priority = Priority.HIGH,
        color = 0xFFE91E63,
        emoji = "👶"
    )

    data object Doorbell : SoundType(
        displayName = "Doorbell",
        priority = Priority.NORMAL,
        color = 0xFFFF9800,
        emoji = "🔔"
    )

    data object AlarmSiren : SoundType(
        displayName = "Alarm/Siren",
        priority = Priority.CRITICAL,
        color = 0xFFF44336,
        emoji = "🚨"
    )

    data object SmokeAlarm : SoundType(
        displayName = "Smoke Alarm",
        priority = Priority.CRITICAL,
        color = 0xFFF44336,
        emoji = "🔥"
    )

    data object DogBarking : SoundType(
        displayName = "Dog Barking",
        priority = Priority.NORMAL,
        color = 0xFF795548,
        emoji = "🐕"
    )

    data object RunningWater : SoundType(
        displayName = "Running Water",
        priority = Priority.LOW,
        color = 0xFF2196F3,
        emoji = "🚿"
    )

    // Catch-all for any of the 521 YAMNet classes
    data class Generic(val yamnetClassName: String) : SoundType(
        displayName = yamnetClassName.replace("_", " ").split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
        priority = Priority.LOW,
        color = 0xFF9E9E9E, // Gray
        emoji = "🔊"
    )

    companion object {
        fun fromLabel(label: String): SoundType {
            val lowerLabel = label.lowercase()
            return when {
                // Baby sounds
                (lowerLabel.contains("baby") || lowerLabel.contains("infant")) &&
                        (lowerLabel.contains("cry") || lowerLabel.contains("wail")) -> BabyCrying

                // Doorbell
                lowerLabel.contains("doorbell") || lowerLabel.contains("door bell") -> Doorbell

                // Alarms and sirens
                lowerLabel.contains("siren") ||
                        (lowerLabel.contains("alarm") && !lowerLabel.contains("smoke")) ||
                        lowerLabel.contains("emergency") -> AlarmSiren

                // Smoke alarm
                lowerLabel.contains("smoke") && lowerLabel.contains("alarm") -> SmokeAlarm

                // Dog barking
                lowerLabel.contains("dog") &&
                        (lowerLabel.contains("bark") || lowerLabel.contains("yap") || lowerLabel.contains("howl")) -> DogBarking

                // Water sounds
                (lowerLabel.contains("water") || lowerLabel.contains("tap")) &&
                        (lowerLabel.contains("run") || lowerLabel.contains("flow")) -> RunningWater

                // Everything else
                else -> Generic(label)
            }
        }

        // Get all explicit (non-Generic) types for settings UI
        val explicitTypes: List<SoundType> = listOf(
            BabyCrying,
            Doorbell,
            AlarmSiren,
            SmokeAlarm,
            DogBarking,
            RunningWater
        )
    }
}
