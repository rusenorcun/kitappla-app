package com.kitappla.app.core.net

import com.kitappla.app.data.dto.ApiErrorDto
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownServiceException
import javax.net.ssl.SSLException

object ApiMessages {
    const val NETWORK = "Bağlantı kurulamadı. İnternet bağlantınızı kontrol edip tekrar deneyin."
    const val TIMEOUT = "Sunucu zamanında yanıt vermedi. Bağlantınız yavaş olabilir; biraz sonra tekrar deneyin."
    const val SERVER_UNREACHABLE = "Sunucuya bağlanılamadı. Sunucu şu an kapalı olabilir; biraz sonra tekrar deneyin."
    const val SECURE_CONNECTION = "Güvenli bağlantı kurulamadı. Telefonunuzun tarih ve saatinin doğru olduğundan emin olun."
    const val CLEARTEXT_BLOCKED = "Güvenli olmayan (HTTP) bağlantıya izin verilmiyor."
    const val SESSION_EXPIRED = "Oturum süresi doldu. Lütfen tekrar giriş yapın."
    const val FORBIDDEN = "Bu işlem için yetkiniz bulunmuyor."
    const val NOT_FOUND = "İstenen kayıt bulunamadı."
    const val SERVER_ERROR = "Sunucuda bir sorun oluştu. Lütfen tekrar deneyin."
    const val UNEXPECTED = "Beklenmeyen bir hata oluştu."
    const val INVALID_RESPONSE = "Sunucudan geçersiz bir yanıt alındı."
    const val EMPTY_RESPONSE = "Sunucudan boş yanıt alındı."
}

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(
        val message: String,
        val code: Int? = null,
        val isNetwork: Boolean = false,
    ) : ApiResult<Nothing>
}

internal fun errorMessage(raw: String?, code: Int): String {
    val fromServer = raw?.let {
        runCatching { KitapplaJson.instance.decodeFromString(ApiErrorDto.serializer(), it).error }.getOrNull()
    }
    if (!fromServer.isNullOrBlank()) return fromServer
    return when {
        code == 401 -> ApiMessages.SESSION_EXPIRED
        code == 403 -> ApiMessages.FORBIDDEN
        code == 404 -> ApiMessages.NOT_FOUND
        code >= 500 -> ApiMessages.SERVER_ERROR
        else -> ApiMessages.UNEXPECTED
    }
}

/**
 * Bağlantı hatasını kullanıcının işine yarayacak biçimde ayırır. Hepsi yine "ağ sorunu" (`isNetwork`) sayılır;
 * yalnızca mesaj değişir. Çevrimdışıyken gelen `UnknownHostException` genel mesajda kalır.
 */
internal fun networkMessage(e: IOException): String = when (e) {
    is SSLException -> ApiMessages.SECURE_CONNECTION
    is SocketTimeoutException, is InterruptedIOException -> ApiMessages.TIMEOUT
    is ConnectException -> ApiMessages.SERVER_UNREACHABLE
    is UnknownServiceException -> ApiMessages.CLEARTEXT_BLOCKED
    else -> ApiMessages.NETWORK
}

/** Yanıtın kendisini (başlıklar dahil) döndürür; hata gövdesini Türkçe mesaja çevirir. */
suspend fun <T> safeResponse(block: suspend () -> Response<T>): ApiResult<Response<T>> = try {
    val response = block()
    if (response.isSuccessful) {
        ApiResult.Success(response)
    } else {
        ApiResult.Failure(errorMessage(response.errorBody()?.string(), response.code()), response.code())
    }
} catch (e: IOException) {
    ApiResult.Failure(networkMessage(e), null, true)
} catch (e: SerializationException) {
    ApiResult.Failure(ApiMessages.INVALID_RESPONSE)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    // Beklenmeyen bir istisna ekranı "yükleniyor"da bırakmasın; ViewModel her zaman bir sonuç görür.
    ApiResult.Failure(ApiMessages.UNEXPECTED)
}

/** Yalnızca gövdeyi döndürür; gövde yoksa hata sayılır. */
suspend fun <T> safeApiCall(block: suspend () -> Response<T>): ApiResult<T> =
    when (val result = safeResponse(block)) {
        is ApiResult.Failure -> result
        is ApiResult.Success -> {
            val body = result.value.body()
            if (body != null) ApiResult.Success(body)
            else ApiResult.Failure(ApiMessages.EMPTY_RESPONSE, result.value.code())
        }
    }

/** Gövdesiz başarılı yanıtlar (204 No Content gibi) için. */
suspend fun safeUnitCall(block: suspend () -> Response<Unit>): ApiResult<Unit> =
    when (val result = safeResponse(block)) {
        is ApiResult.Failure -> result
        is ApiResult.Success -> ApiResult.Success(Unit)
    }

