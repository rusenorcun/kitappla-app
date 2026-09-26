package com.kitappla.app.core.cache

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * "Bağlantı sorunu yaşıyoruz" uyarısının tek kaynağı. Ön bellek eski veri gösterdiğinde [report] çağrılır; arayüz
 * [events]'i dinleyip küçük bir uyarı gösterir. Aynı anda birkaç ekran yüklenirse uyarı yığılmasın diye
 * [minGapMillis] içindeki tekrarlar yutulur.
 */
@Singleton
class ConnectionNotice(
    private val clock: () -> Long,
    private val minGapMillis: Long,
) {
    @Inject constructor() : this(System::currentTimeMillis, DEFAULT_MIN_GAP_MILLIS)

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    private var lastReportAt: Long? = null

    @Synchronized
    fun report() {
        val now = clock()
        val last = lastReportAt
        if (last != null && now - last < minGapMillis) return
        lastReportAt = now
        _events.tryEmit(Unit)
    }

    private companion object {
        const val DEFAULT_MIN_GAP_MILLIS = 15_000L
    }
}
