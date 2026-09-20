package com.kitap.app.core.net

import com.kitap.app.data.dto.MeDto
import com.kitap.app.data.dto.UserDto
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiResultTest {
    private fun errorResponse(code: Int, body: String) =
        Response.error<MeDto>(code, body.toResponseBody("application/json".toMediaType()))

    private val me = MeDto(UserDto(id = 1))

    @Test
    fun serverErrorMessageIsUsed() = runTest {
        val r = safeApiCall { errorResponse(400, """{"error":"E-posta ya da şifre hatalı."}""") }
        assertEquals(ApiResult.Failure("E-posta ya da şifre hatalı.", 400), r)
    }

    @Test
    fun nonJsonServerErrorFallsBackToGenericMessage() = runTest {
        val r = safeApiCall { errorResponse(502, "<html>Bad Gateway</html>") }
        assertEquals(ApiResult.Failure(ApiMessages.SERVER_ERROR, 502), r)
    }

    @Test
    fun emptyUnauthorizedBodyMapsToSessionExpired() = runTest {
        val r = safeApiCall { errorResponse(401, "") }
        assertEquals(ApiResult.Failure(ApiMessages.SESSION_EXPIRED, 401), r)
    }

    @Test
    fun ioExceptionMapsToNetworkFailure() = runTest {
        val r = safeApiCall<MeDto> { throw IOException("boom") }
        assertEquals(ApiResult.Failure(ApiMessages.NETWORK, null, true), r)
    }

    private suspend fun failureOf(e: Exception) = safeApiCall<MeDto> { throw e } as ApiResult.Failure

    @Test
    fun offlineAndUnknownIoProblemsKeepTheGenericMessage() = runTest {
        assertEquals(ApiMessages.NETWORK, failureOf(java.net.UnknownHostException("kitappla.com")).message)
        assertEquals(ApiMessages.NETWORK, failureOf(java.net.SocketException("Network is unreachable")).message)
    }

    @Test
    fun timeoutsGetTheirOwnMessage() = runTest {
        assertEquals(ApiMessages.TIMEOUT, failureOf(java.net.SocketTimeoutException("timeout")).message)
        // OkHttp'nin callTimeout'u InterruptedIOException fırlatır.
        assertEquals(ApiMessages.TIMEOUT, failureOf(java.io.InterruptedIOException("timeout")).message)
    }

    @Test
    fun refusedConnectionSaysTheServerMayBeDown() = runTest {
        assertEquals(ApiMessages.SERVER_UNREACHABLE, failureOf(java.net.ConnectException("Connection refused")).message)
    }

    @Test
    fun tlsProblemsPointAtTheDeviceClock() = runTest {
        assertEquals(ApiMessages.SECURE_CONNECTION, failureOf(javax.net.ssl.SSLHandshakeException("Chain validation failed")).message)
        assertEquals(ApiMessages.SECURE_CONNECTION, failureOf(javax.net.ssl.SSLPeerUnverifiedException("Hostname mismatch")).message)
    }

    @Test
    fun blockedCleartextIsExplained() = runTest {
        val e = java.net.UnknownServiceException("CLEARTEXT communication to 10.0.2.2 not permitted by network security policy")
        assertEquals(ApiMessages.CLEARTEXT_BLOCKED, failureOf(e).message)
    }

    /** Hepsi hâlâ "bağlantı sorunu"dur: ön bellek eski veriyi bu bayrakla sunar. */
    @Test
    fun everyConnectionProblemStaysFlaggedAsNetwork() = runTest {
        listOf(
            java.net.UnknownHostException("x"), java.net.SocketTimeoutException("x"), java.net.ConnectException("x"),
            javax.net.ssl.SSLHandshakeException("x"), java.net.UnknownServiceException("x"),
        ).forEach { assertTrue(it.javaClass.simpleName, failureOf(it).isNetwork) }
    }

    @Test
    fun serializationExceptionMapsToInvalidResponse() = runTest {
        val r = safeApiCall<MeDto> { throw SerializationException("bozuk") }
        assertEquals(ApiResult.Failure(ApiMessages.INVALID_RESPONSE), r)
    }

    @Test
    fun unexpectedExceptionMapsToUnexpectedInsteadOfEscaping() = runTest {
        val r = safeApiCall<MeDto> { throw IllegalStateException("beklenmedik") }
        assertEquals(ApiResult.Failure(ApiMessages.UNEXPECTED), r)
    }

    @Test
    fun cancellationIsNeverSwallowed() = runTest {
        var caught: Throwable? = null
        try {
            safeApiCall<MeDto> { throw kotlinx.coroutines.CancellationException("iptal") }
        } catch (e: kotlinx.coroutines.CancellationException) {
            caught = e
        }
        assertTrue(caught != null)
    }

    @Test
    fun successReturnsBody() = runTest {
        val r = safeApiCall { Response.success(me) }
        assertEquals(ApiResult.Success(me), r)
    }

    @Test
    fun successWithNullBodyIsFailure() = runTest {
        val r = safeApiCall<MeDto> { Response.success<MeDto>(null) }
        assertEquals(ApiResult.Failure(ApiMessages.EMPTY_RESPONSE, 200), r)
    }

    @Test
    fun unitResponseIsSuccess() = runTest {
        val r = safeApiCall { Response.success(Unit) }
        assertTrue(r is ApiResult.Success)
    }

    @Test
    fun safeResponseKeepsHeaders() = runTest {
        val raw = Response.success(me, okhttp3.Headers.headersOf("X-Total-Count", "42"))
        val r = safeResponse { raw } as ApiResult.Success
        assertEquals("42", r.value.headers()["X-Total-Count"])
    }
}
