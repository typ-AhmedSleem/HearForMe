package com.typ.hearforme.domain.interceptors.prays

import android.util.Log
import com.typ.hearforme.designsystem.R
import com.typ.hearforme.domain.interceptors.ClassifierInterceptor
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import com.typ.islamictkt.datetime.Timestamp
import com.typ.islamictkt.location.PopularLocations
import com.typ.islamictkt.prays.enums.AsrMethod
import com.typ.islamictkt.prays.enums.CalculationMethod
import com.typ.islamictkt.prays.enums.HigherLatitudeMethod
import com.typ.islamictkt.prays.enums.PrayType.ASR
import com.typ.islamictkt.prays.enums.PrayType.DHUHR
import com.typ.islamictkt.prays.enums.PrayType.FAJR
import com.typ.islamictkt.prays.enums.PrayType.ISHA
import com.typ.islamictkt.prays.enums.PrayType.MAGHRIB
import com.typ.islamictkt.prays.enums.PrayType.SUNRISE
import com.typ.islamictkt.prays.lib.PrayerTimesCalculator
import com.typ.islamictkt.prays.models.PrayerTimes
import com.typ.islamictkt.prays.utils.prayerTimesCalcConfig

/**
 * Interceptor responsible for modifying or observing requests and responses
 * related to prayer times data within the domain layer.
 */
class PrayTimesInterceptor : ClassifierInterceptor {

    private val praysCalculator = PrayerTimesCalculator(
        location = PopularLocations.Egypt.CAIRO,
        config = prayerTimesCalcConfig {
            calcMethod = CalculationMethod.EGYPT
            asrMethod = AsrMethod.SHAFII
            higherLatMethod = HigherLatitudeMethod.ONESEVENTH
        }
    )
    private var todayPrays = PrayerTimes.getTodayPrays(praysCalculator)

    override fun intercept(event: SoundEvent): SoundEvent {
        if (event.type !is SoundType.SomeoneSpeaking) {
            return event
        }

        val currentTime = System.currentTimeMillis()
        val prays = todayPrays.toArrayNoSunrise()

        // Find if any prayer time is within 10 seconds of now
        val nearbyPray = prays.find { pray ->
            val prayTime = pray.time.toMillis()
            kotlin.math.abs(currentTime - prayTime) <= 5_000 // 5 seconds
        }

        if (nearbyPray != null) {
            val eventType = when (nearbyPray.type) {
                FAJR -> SoundType.PrayTime(prayNameRes = R.string.fajr)
                SUNRISE -> SoundType.PrayTime(prayNameRes = R.string.sunrise)
                DHUHR -> SoundType.PrayTime(prayNameRes = R.string.dhuhr)
                ASR -> SoundType.PrayTime(prayNameRes = R.string.asr)
                MAGHRIB -> SoundType.PrayTime(prayNameRes = R.string.maghrib)
                ISHA -> SoundType.PrayTime(prayNameRes = R.string.isha)
            }

            return SoundEvent(
                type = eventType,
                timestamp = nearbyPray.time.toMillis(),
                confidence = event.confidence
            )
        }

        // Check if we need to load tomorrow's prays (if all today's prays are past by more than 10s)
        val lastPrayTime = prays.lastOrNull()?.time?.toMillis() ?: 0L
        if (currentTime > lastPrayTime + 5_000) {
            todayPrays = PrayerTimes.getPrays(
                calculator = praysCalculator,
                timestamp = Timestamp.tomorrow()
            )
            // Re-run interception with new data
            Log.w("PrayTimesInterceptor", "Tomorrow's prays loaded !!")
            return intercept(event)
        }

        // If not near any prayer time and today's prays are still relevant, don't transform
        return event
    }

}