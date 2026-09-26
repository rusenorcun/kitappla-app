package com.kitappla.app.core.device

import kotlin.math.sqrt

object ShakeConfig {
    /** Toplam ivme büyüklüğü (g); telefon dururken ≈1 g. */
    const val THRESHOLD_G = 2.7f
    const val REQUIRED_SPIKES = 3
    const val WINDOW_MS = 1_500L
    const val MIN_SPIKE_GAP_MS = 150L
    const val COOLDOWN_MS = 2_000L
    const val GRAVITY = 9.80665f
}

/** Saf mantık: örnekleri alır, sallama tamamlandığında `true` döndürür. */
class ShakeLogic(
    private val thresholdG: Float = ShakeConfig.THRESHOLD_G,
    private val requiredSpikes: Int = ShakeConfig.REQUIRED_SPIKES,
    private val windowMs: Long = ShakeConfig.WINDOW_MS,
    private val minSpikeGapMs: Long = ShakeConfig.MIN_SPIKE_GAP_MS,
    private val cooldownMs: Long = ShakeConfig.COOLDOWN_MS,
) {
    private val spikes = ArrayDeque<Long>()
    private var cooldownUntil = 0L

    fun onSample(x: Float, y: Float, z: Float, nowMs: Long): Boolean {
        if (nowMs < cooldownUntil) return false
        val g = sqrt(x * x + y * y + z * z) / ShakeConfig.GRAVITY
        if (g < thresholdG) return false
        if (spikes.isNotEmpty() && nowMs - spikes.last() < minSpikeGapMs) return false

        spikes.addLast(nowMs)
        while (spikes.isNotEmpty() && nowMs - spikes.first() > windowMs) spikes.removeFirst()
        if (spikes.size >= requiredSpikes) {
            spikes.clear()
            cooldownUntil = nowMs + cooldownMs
            return true
        }
        return false
    }

    fun reset() {
        spikes.clear()
        cooldownUntil = 0L
    }
}
