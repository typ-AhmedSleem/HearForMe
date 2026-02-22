package com.typ.hearforme.ai.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SoundDecisionEngineTest {

    private lateinit var engine: SoundDecisionEngine

    @Before
    fun setup() {
        // Default: window=5, start=0.6, stop=0.3
        engine = SoundDecisionEngine()
    }

    @Test
    fun `test initial state is inactive`() {
        val results = engine.processFrame(emptyMap())
        val dogEvent = results.find { it.group == "DOG" }
        assertNotNull(dogEvent)
        assertFalse(dogEvent!!.isActive)
    }

    @Test
    fun `test activation needs smoothed score above start threshold`() {
        // startThreshold is 0.6. With window 5:
        // Frame 1: 0.7 -> avg 0.14 (0.7/5)
        // Frame 2: 0.7 -> avg 0.28
        // Frame 3: 0.7 -> avg 0.42
        // Frame 4: 0.7 -> avg 0.56
        // Frame 5: 0.7 -> avg 0.70 -> ACTIVE!

        val frames = listOf(
            mapOf("Bark" to 0.7f),
            mapOf("Bark" to 0.7f),
            mapOf("Bark" to 0.7f),
            mapOf("Bark" to 0.7f),
            mapOf("Bark" to 0.7f)
        )

        var lastEvent: SoundEvent? = null
        frames.forEachIndexed { index, frame ->
            val results = engine.processFrame(frame)
            lastEvent = results.find { it.group == "DOG" }
            if (index < 4) {
                assertFalse("Frame $index should not be active yet", lastEvent!!.isActive)
            }
        }

        assertTrue("Frame 4 should be active", lastEvent!!.isActive)
        assertEquals(0.7f, lastEvent!!.confidence, 0.01f)
    }

    @Test
    fun `test flicker prevention (short spikes do not activate)`() {
        // One high spike in a window of 5 shouldn't trigger 0.6
        // 1.0 / 5 = 0.2 < 0.6
        val results = engine.processFrame(mapOf("Bark" to 1.0f))
        val dogEvent = results.find { it.group == "DOG" }
        assertFalse("Single spike should not trigger activation", dogEvent!!.isActive)
    }

    @Test
    fun `test gap prevention (short drops do not deactivate)`() {
        // 1. Activate first
        repeat(5) { engine.processFrame(mapOf("Bark" to 0.8f)) }
        var dogEvent = engine.processFrame(mapOf("Bark" to 0.8f)).find { it.group == "DOG" }
        assertTrue(dogEvent!!.isActive)

        // 2. Single drop to 0
        // New avg = (0.8 + 0.8 + 0.8 + 0.8 + 0.0) / 5 = 0.64
        // 0.64 > stopThreshold (0.3), so should stay active
        dogEvent = engine.processFrame(mapOf("Bark" to 0.0f)).find { it.group == "DOG" }
        assertTrue("Single frame drop should NOT deactivate due to smoothing", dogEvent!!.isActive)
        assertEquals(0.64f, dogEvent!!.confidence, 0.01f)
    }

    @Test
    fun `test hysteresis deactivation`() {
        // 1. Activate
        repeat(5) { engine.processFrame(mapOf("Bark" to 0.8f)) }
        assertTrue(engine.processFrame(mapOf("Bark" to 0.8f)).find { it.group == "DOG" }!!.isActive)

        // 2. Drop gradually
        // Average must fall below 0.3
        // After 4 frames of 0.0: (0.8 + 0 + 0 + 0 + 0) / 5 = 0.16 < 0.3 -> DEACTIVE
        repeat(3) {
            val ev = engine.processFrame(mapOf("Bark" to 0.0f)).find { it.group == "DOG" }
            assertTrue("Still active after ${it + 1} frames of silence", ev!!.isActive)
        }

        val finalEvent = engine.processFrame(mapOf("Bark" to 0.0f)).find { it.group == "DOG" }
        assertFalse("Should be inactive after 4 frames of silence", finalEvent!!.isActive)
    }

    @Test
    fun `test semantic aggregation`() {
        // Summing multiple labels in the same group
        // SIREN -> ["Siren", "Police car (siren)", "Ambulance (siren)"]
        val results = engine.processFrame(
            mapOf(
                "Siren" to 0.3f,
                "Police car (siren)" to 0.4f
            )
        )

        val sirenEvent = results.find { it.group == "SIREN" }
        // First frame avg = (0.3 + 0.4) / 5 = 0.14
        assertEquals(0.14f, sirenEvent!!.confidence, 0.01f)
    }
}
