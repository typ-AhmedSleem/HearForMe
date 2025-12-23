package com.typ.hearforme.domain.model

enum class SoundType(val displayName: String) {
    BABY_CRYING("Baby Crying"),
    DOORBELL("Doorbell"),
    ALARM_SIREN("Siren"),
    DOG_BARKING("Dog Barking"),
    SMOKE_ALARM("Smoke Alarm"),
    RUNNING_WATER("Running Water"),
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
