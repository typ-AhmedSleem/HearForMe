package com.typ.hearforme.domain.model

enum class SoundType(val displayName: String) {
    BABY_CRYING("Baby Crying"),
    DOORBELL("Doorbell"),
    KNOCK("Door Knock"),
    ALARM_SIREN("Alarm / Siren"),
    DOG_BARK("Dog Barking"),
    CAT_MEOW("Cat Meowing"),
    UNKNOWN("Unknown");

    companion object {
        fun fromLabel(label: String): SoundType {
            // Simple mapping - can be improved later
            return entries.find { it.name.equals(label, ignoreCase = true) } 
                ?: entries.find { it.displayName.equals(label, ignoreCase = true) }
                ?: UNKNOWN
        }
    }
}
