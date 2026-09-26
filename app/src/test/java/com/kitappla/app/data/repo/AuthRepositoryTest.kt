package com.kitappla.app.data.repo

import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.InMemoryCookiePersistence
import com.kitappla.app.core.net.PersistentCookieJar
import com.kitappla.app.core.session.SessionManager
import com.kitappla.app.core.session.SessionState
import com.kitappla.app.data.api.AuthApi
import com.kitappla.app.data.dto.EmailVerificationDto
import com.kitappla.app.data.dto.ForgotPasswordBody
import com.kitappla.app.data.dto.LoginRequest
import com.kitappla.app.data.dto.MeDto
import com.kitappla.app.data.dto.MessageDto
import com.kitappla.app.data.dto.RegisterRequest
import com.kitappla.app.data.dto.ResetPasswordBody
import com.kitappla.app.data.dto.UserDto
import com.kitappla.app.data.dto.VerifyEmailBody
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

private class FakeAuthApi : AuthApi {
    var loginResponse: Response<MeDto> = ok(admin = false)
    var meResponse: Response<MeDto> = ok(admin = false)
    var meThrows: IOException? = null
    var logoutCalls = 0
    var meCalls = 0

    /** Doluysa me() bu kapı açılana dek askıda kalır (yanıt vermeyen sunucu senaryosu). */
    var meGate: CompletableDeferred<Unit>? = null

    /** Doluysa login/register bu kapı açılana dek askıda kalır (iptal senaryoları için). */
    var loginGate: CompletableDeferred<Unit>? = null
    var logoutGate: CompletableDeferred<Unit>? = null
    var logoutThrows: IOException? = null

    override suspend fun login(body: LoginRequest): Response<MeDto> {
        loginGate?.await()
        return loginResponse
    }

    var lastRegisterRequest: RegisterRequest? = null

    override suspend fun register(body: RegisterRequest): Response<MeDto> {
        lastRegisterRequest = body
        loginGate?.await()
        return loginResponse
    }
    override suspend fun me(): Response<MeDto> {
        meCalls++
        meGate?.await()
        meThrows?.let { throw it }
        return meResponse
    }

    override suspend fun logout(): Response<Unit> {
        logoutCalls++
        logoutGate?.await()
        logoutThrows?.let { throw it }
        return Response.success(Unit)
    }

    override suspend fun forgotPassword(body: ForgotPasswordBody): Response<MessageDto> {
        return Response.success(MessageDto(message = "OK"))
    }

    override suspend fun resetPassword(body: ResetPasswordBody): Response<Unit> {
        return Response.success(Unit)
    }

    override suspend fun verifyEmail(body: VerifyEmailBody): Response<EmailVerificationDto> {
        return Response.success(EmailVerificationDto("DOGRULANDI"))
    }

    companion object {
        fun ok(admin: Boolean): Response<MeDto> =
            Response.success(MeDto(UserDto(id = if (admin) 2 else 1, name = "X", admin = admin)))

        fun error(code: Int, body: String): Response<MeDto> =
            Response.error(code, body.toResponseBody("application/json".toMediaType()))
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {
    private val api = FakeAuthApi()
    private val store = InMemoryCookiePersistence()
    private val jar = PersistentCookieJar(store)
    private val session = SessionManager(jar, com.kitappla.app.core.cache.ApiCache(onStaleServed = {}, clock = { 0L }))
    private val repo = AuthRepository(api, session)

    /** Sunucunun Set-Cookie ile verdiği oturum çerezini taklit eder (çerez, yanıt okunur okunmaz kalıcıdır). */
    private fun seedCookie() = jar.saveFromResponse(
        "http://10.0.2.2:8080/".toHttpUrl(),
        listOf(Cookie.Builder().name("KITAPPLA_SESSION").value("v").hostOnlyDomain("10.0.2.2").path("/").build()),
    )

    @Test
    fun restoreGivesUpOnAnUnresponsiveServerInsteadOfSpinningForever() = runTest {
        seedCookie()
        api.meGate = CompletableDeferred()   // hiç tamamlanmaz
        repo.restore(timeoutMillis = 5_000)
        assertEquals(SessionState.Guest, session.state.value)
        assertEquals(1, store.stored.size)   // yalnızca zaman aşımı: çerezler korunur
    }

    @Test
    fun memberModeAcceptsNormalUser() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = false)
        val r = repo.login("a@b.com", "sifre", LoginMode.MEMBER)
        assertTrue(r is ApiResult.Success)
        assertTrue(session.state.value is SessionState.Member)
        assertEquals(0, api.logoutCalls)
    }

    @Test
    fun memberModeRejectsAdminAccountAndLogsOut() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = true)
        val r = repo.login("admin@kitappla.app", "admin123", LoginMode.MEMBER)
        assertEquals(ApiResult.Failure(LOGIN_FAILED_MESSAGE, 400), r)
        assertEquals(SessionState.Guest, session.state.value)
        assertEquals(1, api.logoutCalls)
    }

    @Test
    fun adminModeAcceptsAdminAccount() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = true)
        val r = repo.login("admin@kitappla.app", "admin123", LoginMode.ADMIN)
        assertTrue(r is ApiResult.Success)
        assertTrue(session.state.value is SessionState.Admin)
    }

    @Test
    fun adminModeRejectsNormalUserAndLogsOut() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = false)
        val r = repo.login("ayse@ornek.com", "sifre123", LoginMode.ADMIN)
        assertEquals(ApiResult.Failure(LOGIN_FAILED_MESSAGE, 400), r)
        assertEquals(SessionState.Guest, session.state.value)
        assertEquals(1, api.logoutCalls)
    }

    @Test
    fun serverErrorIsPassedThroughWithoutLogout() = runTest {
        api.loginResponse = FakeAuthApi.error(400, """{"error":"Hesabın askıya alınmış."}""")
        val r = repo.login("a@b.com", "x", LoginMode.MEMBER)
        assertEquals(ApiResult.Failure("Hesabın askıya alınmış.", 400), r)
        assertEquals(0, api.logoutCalls)
    }

    @Test
    fun registerSignsInAsMember() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = false)
        val r = repo.register("Ayşe", "a@b.com", "sifre")
        assertTrue(r is ApiResult.Success)
        assertTrue(session.state.value is SessionState.Member)
    }

    @Test
    fun registerReportsWhetherTheVerificationEmailWasSent() = runTest {
        api.loginResponse = Response.success(
            MeDto(UserDto(id = 1, name = "X", emailVerified = false)),
            okhttp3.Headers.headersOf("X-Email-Verification-Sent", "false"),
        )
        val notSent = repo.register("Ayşe", "a@b.com", "sifre")
        assertEquals(false, (notSent as ApiResult.Success).value.verificationEmailSent)

        api.loginResponse = FakeAuthApi.ok(admin = false)   // başlık yok: eski sunucu
        val legacy = repo.register("Ayşe", "a@b.com", "sifre")
        assertEquals(true, (legacy as ApiResult.Success).value.verificationEmailSent)
    }

    @Test
    fun registerPassesSchoolAndPhone() = runTest {
        api.loginResponse = FakeAuthApi.ok(admin = false)
        val r = repo.register(
            name = "Ahmet",
            email = "ahmet@atauni.edu.tr",
            password = "sifre",
            school = "ATATURK_UNIVERSITESI",
            phone = "5551234567",
        )
        assertTrue(r is ApiResult.Success)
        val req = api.lastRegisterRequest
        assertEquals("Ahmet", req?.name)
        assertEquals("ahmet@atauni.edu.tr", req?.email)
        assertEquals("ATATURK_UNIVERSITESI", req?.school)
        assertEquals("5551234567", req?.phone)
    }

    @Test
    fun restoreWithValidSessionSignsIn() = runTest {
        api.meResponse = FakeAuthApi.ok(admin = true)
        repo.restore()
        assertTrue(session.state.value is SessionState.Admin)
    }

    @Test
    fun restoreWithUnauthorizedBecomesGuest() = runTest {
        api.meResponse = FakeAuthApi.error(401, "")
        repo.restore()
        assertEquals(SessionState.Guest, session.state.value)
    }

    @Test
    fun restoreWithNetworkErrorBecomesGuest() = runTest {
        api.meThrows = IOException("offline")
        repo.restore()
        assertEquals(SessionState.Guest, session.state.value)
    }

    @Test
    fun restoreIsNoOpWhenSessionAlreadyResolved() = runTest {
        // Süreç canlıyken Activity yeniden açılırsa yeni AppViewModel restore() çağırır; çözülmüş oturum bozulmamalı.
        session.signedIn(UserDto(id = 1))
        api.meThrows = IOException("offline")
        repo.restore()
        assertTrue(session.state.value is SessionState.Member)
        assertEquals(0, api.meCalls)
    }

    @Test
    fun logoutCallsApiAndBecomesGuest() = runTest {
        session.signedIn(UserDto(id = 1))
        repo.logout()
        assertEquals(1, api.logoutCalls)
        assertEquals(SessionState.Guest, session.state.value)
    }

    @Test
    fun cancelledLoginRejectedAsAdminStillEndsGuestWithEmptyJar() = runTest {
        seedCookie()
        api.loginResponse = FakeAuthApi.ok(admin = true)
        val gate = CompletableDeferred<Unit>().also { api.loginGate = it }
        val job = launch { repo.login("admin@kitappla.app", "admin123", LoginMode.MEMBER) }
        runCurrent()
        job.cancel()
        gate.complete(Unit)
        job.join()
        assertEquals(SessionState.Guest, session.state.value)
        assertTrue(store.stored.isEmpty())
        assertEquals(1, api.logoutCalls)
    }

    @Test
    fun cancelledRegisterRejectedAsAdminStillEndsGuestWithEmptyJar() = runTest {
        seedCookie()
        api.loginResponse = FakeAuthApi.ok(admin = true)
        val gate = CompletableDeferred<Unit>().also { api.loginGate = it }
        val job = launch { repo.register("Ayşe", "a@b.com", "sifre") }
        runCurrent()
        job.cancel()
        gate.complete(Unit)
        job.join()
        assertEquals(SessionState.Guest, session.state.value)
        assertTrue(store.stored.isEmpty())
        assertEquals(1, api.logoutCalls)
    }

    @Test
    fun cancelDuringRejectLogoutStillSignsOut() = runTest {
        seedCookie()
        api.loginResponse = FakeAuthApi.ok(admin = true)
        val gate = CompletableDeferred<Unit>().also { api.logoutGate = it }
        val job = launch { repo.login("admin@kitappla.app", "admin123", LoginMode.MEMBER) }
        runCurrent()   // giriş döndü, ret yolunun logout çağrısı askıda
        assertEquals(1, api.logoutCalls)
        job.cancel()
        gate.complete(Unit)
        job.join()
        assertEquals(SessionState.Guest, session.state.value)
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun cancelDuringLogoutStillSignsOut() = runTest {
        seedCookie()
        session.signedIn(UserDto(id = 1))
        val gate = CompletableDeferred<Unit>().also { api.logoutGate = it }
        val job = launch { repo.logout() }
        runCurrent()
        job.cancel()
        gate.complete(Unit)
        job.join()
        assertEquals(1, api.logoutCalls)
        assertEquals(SessionState.Guest, session.state.value)
        assertTrue(store.stored.isEmpty())
    }

    @Test
    fun logoutApiFailureStillEndsGuestWithClearedCookies() = runTest {
        seedCookie()
        session.signedIn(UserDto(id = 1))
        api.logoutThrows = IOException("offline")
        repo.logout()
        assertEquals(SessionState.Guest, session.state.value)
        assertTrue(store.stored.isEmpty())
    }
}
