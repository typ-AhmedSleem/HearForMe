package com.typ.hearforme.ai.engine

/**
 * Semantic groups define which YAMNet labels belong to the same logical sound event.
 */
object SemanticGroups {
    val MAPPING: Map<String, Set<String>> = mapOf(
        "CAT" to setOf("Cat", "Caterwaul", "Purr", "Meow"),
        "DOG" to setOf("Dog", "Bark", "Howl", "Growling"),
        "SIREN" to setOf(
            "Siren",
            "Police car (siren)",
            "Ambulance (siren)",
            "Fire engine (siren)",
            "Emergency vehicle"
        ),
        "ALARM" to setOf("Alarm", "Smoke detector", "Fire alarm", "Burglar alarm"),
        "DOORBELL" to setOf("Doorbell", "Ding-dong"),
        "BABY_CRY" to setOf("Baby cry", "Crying, sobbing", "Whimper"),
        "KNOCK" to setOf("Knock", "Tap"),
        "SPEECH" to setOf("Speech", "Child speech, kid speaking", "Conversation", "Chatter"),
        "WHISTLE" to setOf("Whistle"),
        "TELEPHONE" to setOf("Telephone", "Telephone bell", "Ringtone")
    )
}
