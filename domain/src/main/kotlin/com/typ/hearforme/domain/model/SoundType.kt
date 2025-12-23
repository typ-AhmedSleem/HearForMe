package com.typ.hearforme.domain.model

sealed class SoundType(
    val displayName: String,
    val label: String,
    val priority: Priority,
    val color: Long, // ARGB color code
    val emoji: String,
) {
    enum class Priority { LOW, NORMAL, HIGH, CRITICAL }

    // Explicit high-priority types for deaf safety
    data object BabyCrying : SoundType(
        displayName = "Baby Crying",
        label = "baby_crying",
        priority = Priority.HIGH,
        color = 0xFFE91E63,
        emoji = "👶"
    )

    data object Doorbell : SoundType(
        displayName = "Doorbell",
        label = "door_bell",
        priority = Priority.NORMAL,
        color = 0xFFFF9800,
        emoji = "🔔"
    )

    data object AlarmSiren : SoundType(
        displayName = "Siren",
        label = "siren",
        priority = Priority.CRITICAL,
        color = 0xFFF44336,
        emoji = "🚨"
    )

    data object SmokeAlarm : SoundType(
        displayName = "Smoke Alarm",
        label = "smoke",
        priority = Priority.CRITICAL,
        color = 0xFFF44336,
        emoji = "🔥"
    )

    data object DogBarking : SoundType(
        displayName = "Dog Barking",
        label = "dog",
        priority = Priority.NORMAL,
        color = 0xFF795548,
        emoji = "🐕"
    )

    data object RunningWater : SoundType(
        displayName = "Running Water",
        label = "water",
        priority = Priority.LOW,
        color = 0xFF2196F3,
        emoji = "🚿"
    )

    // Catch-all for any of the 521 YAMNet classes
    data class Generic(val yamnetClassName: String) : SoundType(
        emoji = "🔊",
        color = 0xFF9E9E9E,
        label = yamnetClassName,
        priority = Priority.LOW,
        displayName = yamnetClassName
            .replace("_", " ")
            .split(" ")
            .joinToString(" ")
            { it.replaceFirstChar { c -> c.uppercase() } },
    )

    companion object {
        fun fromLabel(label: String): SoundType {
            val lowerLabel = label.lowercase()
            return when {
                // Baby sounds
                (lowerLabel.containsIgnoringCase("baby")
                        || lowerLabel.containsIgnoringCase("infant"))
                        && (lowerLabel.containsIgnoringCase("cry")
                        || lowerLabel.containsIgnoringCase("wail")) -> BabyCrying

                // Doorbell
                lowerLabel.containsIgnoringCase("doorbell")
                        || lowerLabel.containsIgnoringCase("door bell") -> Doorbell

                // Alarms and sirens
                lowerLabel.containsIgnoringCase("siren")
                        || (lowerLabel.containsIgnoringCase("alarm")
                        && !lowerLabel.containsIgnoringCase("smoke"))
                        || lowerLabel.containsIgnoringCase("emergency") -> AlarmSiren

                // Smoke alarm
                lowerLabel.containsIgnoringCase("smoke")
                        && lowerLabel.containsIgnoringCase("alarm") -> SmokeAlarm

                // Dog barking
                lowerLabel.containsIgnoringCase("dog")
                        && (lowerLabel.containsIgnoringCase("bark")
                        || lowerLabel.containsIgnoringCase("yap")
                        || lowerLabel.containsIgnoringCase("howl")) -> DogBarking

                // Water sounds
                (lowerLabel.containsIgnoringCase("water") || lowerLabel.containsIgnoringCase("tap")) &&
                        (lowerLabel.containsIgnoringCase("run") || lowerLabel.containsIgnoringCase("flow")) -> RunningWater

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

        private fun String.containsIgnoringCase(text: String): Boolean {
            return this.contains(text, ignoreCase = true)
        }
    }

}
