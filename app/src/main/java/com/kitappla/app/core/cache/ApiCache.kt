package com.kitappla.app.core.cache

import com.kitappla.app.core.net.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Basit bellek içi ön bellek: okuma uçlarının başarılı yanıtlarını süreli tutar.
 *
 * - Süresi dolmamış giriş ağa gitmeden döner (`forceRefresh` ile atlanır).
 * - Ağ ya da sunucu (5xx) hatasında, süresi dolmuş de olsa elde giriş varsa o gösterilir ve [onStaleServed] çağrılır.
 *   4xx yanıtları (yetki, bulunamadı, doğrulama) hiçbir zaman ön bellekle maskelenmez.
 * - Süreç ölünce silinir; diske yazılmaz. Oturuma özel veri taşıyan yanıtlar bu yüzden oturum değişince
 *   [clear] ile temizlenir (bkz. SessionManager).
 */
@Singleton
class ApiCache(
    private val onStaleServed: () -> Unit,
    private val clock: () -> Long,
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
) {
    @Inject constructor(notice: ConnectionNotice) : this(notice::report, System::currentTimeMillis)

    private class Entry(val value: Any, val storedAt: Long)

    private val lock = Any()
    private val entries = object : LinkedHashMap<String, Entry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>): Boolean = size > maxEntries
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any> cached(
        key: String,
        ttlMillis: Long,
        forceRefresh: Boolean = false,
        fetch: suspend () -> ApiResult<T>,
    ): ApiResult<T> {
        val known = synchronized(lock) { entries[key] }
        if (!forceRefresh && known != null && clock() - known.storedAt < ttlMillis) {
            return ApiResult.Success(known.value as T)
        }
        return when (val result = fetch()) {
            is ApiResult.Success -> {
                synchronized(lock) { entries[key] = Entry(result.value, clock()) }
                result
            }
            is ApiResult.Failure -> {
                val stale = synchronized(lock) { entries[key] }
                if (stale != null && result.isConnectionProblem()) {
                    onStaleServed()
                    ApiResult.Success(stale.value as T)
                } else {
                    result
                }
            }
        }
    }

    fun remove(key: String) {
        synchronized(lock) { entries.remove(key) }
    }

    fun removePrefix(prefix: String) {
        synchronized(lock) { entries.keys.removeAll { it.startsWith(prefix) } }
    }

    fun clear() {
        synchronized(lock) { entries.clear() }
    }

    private fun ApiResult.Failure.isConnectionProblem(): Boolean = isNetwork || (code ?: 0) >= 500

    private companion object {
        const val DEFAULT_MAX_ENTRIES = 128
    }
}
