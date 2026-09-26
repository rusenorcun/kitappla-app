package com.kitappla.app.core.net

import com.kitappla.app.data.dto.ApiErrorDto
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Backend `EpostaDogrulamaFiltresi.KOD`: e-postası doğrulanmamış üyenin durum değiştiren isteği 403 ile reddedilir. */
const val EMAIL_NOT_VERIFIED_CODE = "EMAIL_NOT_VERIFIED"

/**
 * "Önce e-postanı doğrula" uyarısının tek kaynağı. Ağ katmanı reddi gördüğünde [report] çağrılır; kök arayüz
 * [events]'i dinleyip doğrulama ekranına götüren bir uyarı gösterir. İşlemi yapan ekran sunucunun hata metnini
 * zaten gösterir; bu uyarı yalnızca oraya giden yolu ekler.
 */
@Singleton
class EmailVerificationNotice @Inject constructor() {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun report() {
        _events.tryEmit(Unit)
    }
}

/** 403 yanıtının gövdesine (tüketmeden) bakar; kod [EMAIL_NOT_VERIFIED_CODE] ise [onBlocked] çağrılır. */
class EmailNotVerifiedInterceptor(private val onBlocked: () -> Unit) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 403 && isEmailNotVerified(runCatching { response.peekBody(PEEK_BYTES).string() }.getOrNull())) {
            onBlocked()
        }
        return response
    }

    private companion object {
        const val PEEK_BYTES = 4_096L
    }
}

internal fun isEmailNotVerified(rawErrorBody: String?): Boolean {
    if (rawErrorBody.isNullOrBlank()) return false
    val code = runCatching { KitapplaJson.instance.decodeFromString(ApiErrorDto.serializer(), rawErrorBody).code }.getOrNull()
    return code == EMAIL_NOT_VERIFIED_CODE
}
