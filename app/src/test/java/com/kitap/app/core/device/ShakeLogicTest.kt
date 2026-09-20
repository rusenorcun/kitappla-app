package com.kitap.app.core.device

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeLogicTest {
    private val logic = ShakeLogic()

    /** ~3,06 g: eşiğin üstünde. */
    private fun spike(t: Long) = logic.onSample(0f, 30f, 0f, t)

    /** ~1 g: telefon dururken. */
    private fun rest(t: Long) = logic.onSample(0f, 0f, 9.81f, t)

    @Test
    fun restingNeverTriggers() {
        for (t in 0L..5_000L step 20L) assertFalse(rest(t))
    }

    @Test
    fun belowThresholdSamplesAreIgnored() {
        // ~2,04 g
        for (t in listOf(0L, 300L, 600L, 900L)) assertFalse(logic.onSample(0f, 20f, 0f, t))
    }

    @Test
    fun threeSpikesTriggerOnTheThird() {
        assertFalse(spike(0))
        assertFalse(spike(300))
        assertTrue(spike(600))
    }

    @Test
    fun twoSpikesDoNotTrigger() {
        assertFalse(spike(0))
        assertFalse(spike(300))
        assertFalse(rest(700))
    }

    @Test
    fun spikesSpreadWiderThanWindowDoNotTrigger() {
        assertFalse(spike(0))
        assertFalse(spike(800))
        assertFalse(spike(1_600))
    }

    @Test
    fun sustainedJoltIsCountedOnce() {
        for (t in 0L..140L step 20L) assertFalse(spike(t))
    }

    @Test
    fun cooldownIgnoresNewShakeThenAllowsAgain() {
        spike(0); spike(300)
        assertTrue(spike(600))
        assertFalse(spike(900))
        assertFalse(spike(1_200))
        assertFalse(spike(1_500))
        assertFalse(spike(3_000))
        assertFalse(spike(3_300))
        assertTrue(spike(3_600))
    }

    @Test
    fun resetClearsPartialShake() {
        spike(0); spike(300)
        logic.reset()
        assertFalse(spike(600))
    }
}
