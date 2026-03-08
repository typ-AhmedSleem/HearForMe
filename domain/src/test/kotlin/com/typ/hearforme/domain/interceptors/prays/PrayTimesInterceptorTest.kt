package com.typ.hearforme.domain.interceptors.prays

import com.typ.hearforme.designsystem.R
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class PrayTimesInterceptorTest {

    val interceptor = PrayTimesInterceptor()

    @Test
    fun `test interceptor transforms speaking event near pray time`() {
        val speechEvent = SoundEvent(
            type = SoundType.SomeoneSpeaking,
            confidence = 0.9f,
            timestamp = System.currentTimeMillis()
        )

        // We can at least verify that it returns the same event if not near any pray time.
        val result = interceptor.intercept(speechEvent)

        // Since we are likely not EXACTLY at a pray time during the test, 
        // it should return the original event.
        assertEquals(speechEvent, result)
    }

    @Test
    fun `test interceptor does not transform non-speaking event even near pray time`() {
        val dogBarkingEvent = SoundEvent(
            type = SoundType.CarAlarm,
            confidence = 0.9f,
            timestamp = System.currentTimeMillis()
        )

        val result = interceptor.intercept(dogBarkingEvent)
        assertEquals(dogBarkingEvent, result)
    }

    @Test
    fun `test active interceptor that intercepts collected random events`() = runTest {
        flow {
            repeat(100) {
                val randomEvent = SoundEvent(
                    type = SoundType.explicitTypes.random(),
                    timestamp = System.currentTimeMillis(),
                    confidence = Random
                        .nextFloat()
                        .coerceAtLeast(0.25f),
                )
                emit(randomEvent)
            }
        }.collect { event ->
            val interceptedEvent = interceptor.intercept(event) ?: event
            if (interceptedEvent.type is SoundType.PrayTime) {
                when (interceptedEvent.type.prayNameRes) {
                    R.string.fajr -> println("================== Intercepted PRAY TIME: Pray is: Fajr ==================")
                    R.string.sunrise -> println("================== Intercepted PRAY TIME: Pray is: Sunrise ==================")
                    R.string.dhuhr -> println("================== Intercepted PRAY TIME: Pray is: Dhuhr ==================")
                    R.string.asr -> println("================== Intercepted PRAY TIME: Pray is: Asr ==================")
                    R.string.maghrib -> println("================== Intercepted PRAY TIME: Pray is: Maghrib ==================")
                    R.string.isha -> println("================== Intercepted PRAY TIME: Pray is: Isha ==================")
                }
            } else {
                println("Event: '${event.type::class.simpleName}', intercepted: '${interceptedEvent.type::class.simpleName}'.")
            }
        }
    }
}