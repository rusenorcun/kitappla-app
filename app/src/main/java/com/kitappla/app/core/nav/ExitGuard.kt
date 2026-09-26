package com.kitappla.app.core.nav

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * Kök hedefte geri tuşu kuralı: ilk basış uyarır, [windowMs] içindeki ikinci basış çıkar.
 * Tek bir basış hiçbir koşulda [Decision.EXIT] döndürmez.
 */
class ExitGuard(
    private val windowMs: Long = 2_000L,
    // Monoton saat: duvar saati geri/ileri atlasa bile bayat bir uyarı ikinci basış sayılmaz.
    private val clock: () -> Long = SystemClock::elapsedRealtime,
) {
    enum class Decision { WARN, EXIT }

    private var lastWarnAt: Long? = null

    fun onBackAtRoot(): Decision {
        val now = clock()
        val last = lastWarnAt
        return if (last != null && now - last <= windowMs) {
            lastWarnAt = null
            Decision.EXIT
        } else {
            lastWarnAt = now
            Decision.WARN
        }
    }

    /** Bekleyen uyarıyı unutur: kökten ayrılıp dönüldüğünde eski uyarı çıkış sayılmasın. */
    fun reset() {
        lastWarnAt = null
    }
}

/** [enabled] iken geri tuşunu yakalar (kök hedef). Kök değilken kapalı tutulmalı; navigasyon kendi geri davranışını sürdürür. */
@Composable
fun ExitGuardHandler(enabled: Boolean, onWarn: () -> Unit, onExit: () -> Unit) {
    val guard = remember { ExitGuard() }
    LaunchedEffect(enabled) { if (!enabled) guard.reset() }
    BackHandler(enabled = enabled) {
        when (guard.onBackAtRoot()) {
            ExitGuard.Decision.WARN -> onWarn()
            ExitGuard.Decision.EXIT -> onExit()
        }
    }
}
