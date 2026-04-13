package com.typ.hearforme.domain.model

import com.typ.hearforme.designsystem.R

abstract class SoundType(
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
        label = "BABY_CRY",
        priority = Priority.HIGH,
        color = 0xFFE91E63,
        emoji = "👶"
    )

    data object Doorbell : SoundType(
        nameRes = R.string.sound_type_doorbell,
        label = "DOORBELL",
        priority = Priority.HIGH,
        color = 0xFFFF9800,
        emoji = "🔔"
    )

    data object AlarmSiren : SoundType(
        nameRes = R.string.sound_type_siren,
        label = "SIREN",
        priority = Priority.CRITICAL,
        color = 0xFFF44336,
        emoji = "🚨"
    )

    data object Explosion : SoundType(
        nameRes = R.string.sound_type_explosion,
        label = "explosion",
        priority = Priority.CRITICAL,
        color = 0xFFF44336,
        emoji = "💥"
    )

    data object SomeoneSpeaking : SoundType(
        nameRes = R.string.sound_type_someone_speaking,
        label = "SPEECH",
        priority = Priority.LOW,
        color = 0xFF4CAF50,
        emoji = "🗣️"
    )

    data object Screaming : SoundType(
        nameRes = R.string.sound_type_screaming,
        label = "screaming",
        priority = Priority.HIGH,
        color = 0xFFE91E63,
        emoji = "😱"
    )

    data object DogBarking : SoundType(
        nameRes = R.string.sound_type_dog_barking,
        label = "DOG",
        priority = Priority.HIGH,
        color = 0xFF795548,
        emoji = "🐕"
    )

    data object CarAlarm : SoundType(
        nameRes = R.string.sound_type_car_alarm,
        label = "car_alarm",
        priority = Priority.HIGH,
        color = 0xFFFF9800,
        emoji = "🚗"
    )

    data object CatMeowing : SoundType(
        nameRes = R.string.sound_type_cat_meowing,
        label = "CAT",
        priority = Priority.NORMAL,
        color = 0xFFFFC107,
        emoji = "🐈"
    )

    data object MicrowaveDone : SoundType(
        nameRes = R.string.sound_type_microwave_done,
        label = "ALARM",
        priority = Priority.NORMAL,
        color = 0xFF2196F3,
        emoji = "⏲️"
    )

    data object Laughter : SoundType(
        nameRes = R.string.sound_type_laughter,
        label = "laughter",
        priority = Priority.NORMAL,
        color = 0xFF4CAF50,
        emoji = "😂"
    )

    data object PowerTool : SoundType(
        nameRes = R.string.sound_type_power_tool,
        label = "power_tool",
        priority = Priority.NORMAL,
        color = 0xFF607D8B,
        emoji = "🛠️"
    )

    data object WaterRunning : SoundType(
        nameRes = R.string.sound_type_water_running,
        label = "water",
        priority = Priority.LOW,
        color = 0xFF00BCD4,
        emoji = "💧"
    )

    data object GlassShattering : SoundType(
        nameRes = R.string.sound_type_glass_shattering,
        label = "glass",
        priority = Priority.HIGH,
        color = 0xFF607D8B,
        emoji = "💎"
    )

    data object DoorKnocking : SoundType(
        nameRes = R.string.sound_type_door_knocking,
        label = "knock",
        priority = Priority.NORMAL,
        color = 0xFF8D6E63,
        emoji = "✊"
    )

    data object VehicleHorn : SoundType(
        nameRes = R.string.sound_type_vehicle_horn,
        label = "horn",
        priority = Priority.HIGH,
        color = 0xFFFF5722,
        emoji = "📢"
    )

    data object TelephoneRinging : SoundType(
        nameRes = R.string.sound_type_telephone_ringing,
        label = "TELEPHONE",
        priority = Priority.NORMAL,
        color = 0xFF3F51B5,
        emoji = "📞"
    )

    data object Thunder : SoundType(
        nameRes = R.string.sound_type_thunder,
        label = "thunder",
        priority = Priority.LOW,
        color = 0xFF673AB7,
        emoji = "⚡"
    )

    data object BirdChirping : SoundType(
        nameRes = R.string.sound_type_bird_chirping,
        label = "bird",
        priority = Priority.LOW,
        color = 0xFF8BC34A,
        emoji = "🐦"
    )

    data object Music : SoundType(
        nameRes = R.string.sound_type_music,
        label = "music",
        priority = Priority.LOW,
        color = 0xFFE91E63,
        emoji = "🎵"
    )

    data object Wind : SoundType(
        nameRes = R.string.sound_type_wind,
        label = "wind",
        priority = Priority.LOW,
        color = 0xFFB0BEC5,
        emoji = "🌬️"
    )

    data object Silence : SoundType(
        nameRes = R.string.sound_type_silence,
        label = "Silence",
        priority = Priority.LOW,
        color = 0xFF9E9E9E,
        emoji = "🤫"
    )

    data class PrayTime(
        val prayNameRes: Int = R.string.sound_type_pray_times,
    ) : SoundType(
        nameRes = prayNameRes,
        label = "PRAY_TIME",
        priority = Priority.HIGH,
        color = 0xFF009688,
        emoji = "🕌"
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
        // Label Lists for Grouping
        private val babyLabels = listOf("baby", "infant", "cry", "wail", "sobbing", "babbling", "whimper")
        private val doorbellLabels = listOf("doorbell", "ding-dong", "chime")
        private val sirenLabels = listOf(
            "siren",
            "alarm",
            "emergency",
            "police",
            "ambulance",
            "fire engine",
            "smoke detector",
            "buzzer",
            "civil defense siren",
            "fire alarm"
        )
        private val explosionLabels = listOf("explosion", "gunshot", "boom", "artillery fire", "firecracker")
        private val speakingLabels = listOf("speech", "speaking", "conversation", "narration", "whispering")
        private val screamingLabels = listOf("screaming", "yell", "shout", "bellow", "whoop")
        private val dogLabels = listOf("dog", "bark", "yip", "howl", "bow-wow", "canidae", "canine")
        private val catLabels = listOf("cat", "meow", "purr", "hiss", "caterwaul")
        private val carAlarmLabels = listOf("car alarm")
        private val microwaveLabels = listOf("microwave", "beep", "bleep")
        private val laughterLabels = listOf("laughter", "giggle", "snicker", "chuckle")
        private val powerToolLabels = listOf("power tool", "drill", "sawing", "hammer", "chainsaw", "jackhammer")
        private val waterLabels = listOf("water", "stream", "waterfall", "gurgling", "rain", "drip", "pour", "trickle", "gush")
        private val glassLabels = listOf("glass", "shatter", "chink", "clink")
        private val knockLabels = listOf("knock", "door", "tap", "slam")
        private val hornLabels = listOf("horn", "honk", "toot", "vehicle horn", "honking", "car horn", "Vehicle horn, car horn, honking".lowercase())
        private val telephoneLabels = listOf("telephone", "ringtone", "ring")
        private val thunderLabels = listOf("thunder", "thunderstorm")
        private val birdLabels = listOf("bird", "chirp", "tweet", "squawk")
        private val musicLabels = listOf("music", "musical instrument", "orchestra", "piano", "guitar")
        private val windLabels = listOf("wind", "rustling leaves")

        fun fromLabel(label: String): SoundType {
            val lowerLabel = label.lowercase()

            // * Check direct labels first
            val directType = explicitTypes.firstOrNull {
                lowerLabel.containsIgnoringCase(it.label.lowercase())
            }
            if (directType != null) return directType

            // * Handle silence explicitly
            if (lowerLabel.containsIgnoringCase("silence")) return Silence
            if (lowerLabel.containsIgnoringCase("Vehicle horn, car horn, honking")) {
                return VehicleHorn
            }
            if (lowerLabel.containsIgnoringCase("car")) {
                return VehicleHorn
            }
            if (lowerLabel.containsIgnoringCase("vehicle")) {
                return VehicleHorn
            }

            // * Check alternative and grouped YAMNet labels
            return when {
                lowerLabel.containsAny(babyLabels) -> BabyCrying
                lowerLabel.containsAny(doorbellLabels) -> Doorbell
                lowerLabel.containsAny(sirenLabels) -> AlarmSiren
                lowerLabel.containsAny(explosionLabels) -> Explosion
                lowerLabel.containsAny(screamingLabels) -> Screaming
                lowerLabel.containsAny(speakingLabels) -> SomeoneSpeaking
                lowerLabel.containsAny(dogLabels) -> DogBarking
                lowerLabel.containsAny(catLabels) -> CatMeowing
                lowerLabel.containsAny(carAlarmLabels) -> CarAlarm
                lowerLabel.containsAny(microwaveLabels) -> MicrowaveDone
                lowerLabel.containsAny(laughterLabels) -> Laughter
                lowerLabel.containsAny(powerToolLabels) -> PowerTool
                lowerLabel.containsAny(waterLabels) -> WaterRunning
                lowerLabel.containsAny(glassLabels) -> GlassShattering
                lowerLabel.containsAny(knockLabels) -> DoorKnocking
                lowerLabel.containsAny(hornLabels) -> VehicleHorn
                lowerLabel.containsAny(telephoneLabels) -> TelephoneRinging
                lowerLabel.containsAny(thunderLabels) -> Thunder
                lowerLabel.containsAny(birdLabels) -> BirdChirping
                lowerLabel.containsAny(musicLabels) -> Music
                lowerLabel.containsAny(windLabels) -> Wind
                lowerLabel.containsIgnoringCase("PRAY_TIME") -> PrayTime()
                else -> Generic(label)
            }
        }

        private fun String.containsAny(keywords: List<String>): Boolean {
            return keywords.any { this.contains(it, ignoreCase = true) }
        }

        private fun String.containsIgnoringCase(text: String): Boolean {
            return this.contains(text, ignoreCase = true)
        }

        // Get all explicit (non-Generic) types for settings UI
        val explicitTypes: List<SoundType> = listOf(
            BabyCrying,
            Doorbell,
            AlarmSiren,
//            Explosion,
//            Screaming,
            SomeoneSpeaking,
            DogBarking,
//            CarAlarm,
            CatMeowing,
            MicrowaveDone,
//            Laughter,
//            PowerTool,
//            WaterRunning,
//            GlassShattering,
            DoorKnocking,
            VehicleHorn,
            TelephoneRinging,
//            Thunder,
//            BirdChirping,
//            Music,
//            Wind,
        )
    }

}
