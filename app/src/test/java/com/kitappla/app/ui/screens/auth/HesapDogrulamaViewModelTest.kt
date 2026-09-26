package com.kitappla.app.ui.screens.auth

import androidx.lifecycle.SavedStateHandle
import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.core.net.InMemoryCookiePersistence
import com.kitappla.app.core.net.PersistentCookieJar
import com.kitappla.app.core.session.SessionManager
import com.kitappla.app.core.session.SessionState
import com.kitappla.app.data.api.AuthApi
import com.kitappla.app.data.api.ProfileApi
import com.kitappla.app.data.dto.UserDto
import com.kitappla.app.data.repo.AuthRepository
import com.kitappla.app.data.repo.ProfileRepository
import com.kitappla.app.data.repo.testRetrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Hesap doğrulama ekranının hâlleri: web'de onaylanan bağlantıdan dönüş (`kitappla://eposta-dogrulandi`), geçersiz
 * bağlantı, uygulama içinden açılan "gelen kutunu kontrol et" ve oturum yokken gösterilenler.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HesapDogrulamaViewModelTest {
    private lateinit var server: MockWebServer
    private val session = SessionManager(
        PersistentCookieJar(InMemoryCookiePersistence()),
        ApiCache(onStaleServed = {}, clock = { 0L }),
    )
    private val member = UserDto(id = 1, name = "Ayşe", email = "ayse@ornek.com", emailVerified = false)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun me(verified: Boolean, email: String = "ayse@ornek.com", student: String = "NONE") = MockResponse().setBody(
        """{"user":{"id":1,"name":"Ayşe","email":"$email","admin":false,"studentStatus":"$student","emailVerified":$verified}}"""
    )

    /**
     * `durum` null: uygulama içinden (Routes.HESAP_DOGRULAMA); "" ya da "gecersiz": bağlantıdan dönüş.
     * `kayit`: kayıttan hemen sonra açılış ("gonderildi" / "gonderilmedi").
     */
    private fun newVm(durum: String?, kayit: String? = null, token: String? = null): HesapDogrulamaViewModel {
        val retrofit = testRetrofit(server)
        val args = buildMap<String, Any?> {
            if (durum != null) put(HesapDogrulamaViewModel.DURUM_ARG, durum)
            if (kayit != null) put(HesapDogrulamaViewModel.KAYIT_ARG, kayit)
            if (token != null) put(HesapDogrulamaViewModel.TOKEN_ARG, token)
        }
        return HesapDogrulamaViewModel(
            authRepository = AuthRepository(retrofit.create(AuthApi::class.java), session),
            profileRepository = ProfileRepository(android.content.ContextWrapper(null), retrofit.create(ProfileApi::class.java)),
            sessionManager = session,
            savedStateHandle = SavedStateHandle(args),
        )
    }

    private fun awaitState(vm: HesapDogrulamaViewModel, predicate: (HesapDogrulamaState) -> Boolean) =
        runBlocking { withTimeout(5_000) { vm.state.first(predicate) } }

    private fun awaitView(vm: HesapDogrulamaViewModel) = awaitState(vm) { it.view != HesapDogrulamaView.CHECKING }

    /**
     * Süren bir kontrol varken `check` ona katılır (yeni istek atmaz). Açılış kontrolü durum yazıldıktan hemen sonra
     * biter ama test iş parçacığı araya girebilir; bu yüzden koşul sağlanana dek kontrol yeniden istenir.
     */
    private fun checkUntil(vm: HesapDogrulamaViewModel, silent: Boolean, predicate: (HesapDogrulamaState) -> Boolean) =
        runBlocking {
            withTimeout(5_000) {
                while (!predicate(vm.state.value)) vm.check(silent).join()
            }
            vm.state.value
        }

    @Test
    fun returningFromTheVerifiedLinkShowsSuccessAndRefreshesTheSession() {
        session.signedIn(member)
        server.enqueue(me(verified = true))

        val s = awaitView(newVm(durum = ""))

        assertEquals(HesapDogrulamaView.VERIFIED, s.view)
        assertTrue("bağlantıdan az önce doğrulandı", s.justVerified)
        assertFalse(s.studentUnlocked)
        val now = session.state.value as SessionState.Member
        assertTrue("açık oturum doğrulanmış kullanıcıyı görmeli", now.user.emailVerified)
    }

    @Test
    fun verifyingWithASchoolAddressAlsoAnnouncesStudentPriority() {
        session.signedIn(member.copy(email = "ayse@ogr.uni.edu.tr"))
        server.enqueue(me(verified = true, email = "ayse@ogr.uni.edu.tr", student = "APPROVED"))

        val s = awaitView(newVm(durum = ""))

        assertEquals(HesapDogrulamaView.VERIFIED, s.view)
        assertTrue(s.studentUnlocked)
    }

    @Test
    fun whenTheServerIsUnreachableTheWebsVerdictIsTrusted() {
        session.signedIn(member)
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val s = awaitView(newVm(durum = ""))

        assertEquals(HesapDogrulamaView.VERIFIED, s.view)
        assertEquals("ayse@ornek.com", s.email)
    }

    @Test
    fun aLinkConfirmedForAnotherAccountLeavesThisOnePending() {
        session.signedIn(member)
        server.enqueue(me(verified = false))

        val s = awaitView(newVm(durum = ""))

        assertEquals(HesapDogrulamaView.PENDING, s.view)
        assertTrue(s.otherAccountOpen)
    }

    @Test
    fun invalidLinkOffersANewOneAndSendingItSwitchesToTheInboxView() {
        session.signedIn(member)
        server.enqueue(me(verified = false))
        val vm = newVm(durum = "gecersiz")
        assertEquals(HesapDogrulamaView.INVALID_LINK, awaitView(vm).view)

        server.enqueue(MockResponse().setResponseCode(202).setBody("""{"message":"ayse@ornek.com adresine doğrulama bağlantısı gönderildi."}"""))
        vm.resend()

        val s = awaitState(vm) { it.sentMessage != null && it.resendCooldown > 0 }
        assertEquals(HesapDogrulamaView.PENDING, s.view)
        assertEquals("ayse@ornek.com adresine doğrulama bağlantısı gönderildi.", s.sentMessage)
        assertTrue("art arda göndermeyi önlemek için bekleme süresi başlar", s.resendCooldown > 0)
        server.takeRequest() // GET /me
        assertEquals("/api/v1/me/email-verification", server.takeRequest().path)
    }

    @Test
    fun resendErrorsFromTheServerAreShown() {
        session.signedIn(member)
        server.enqueue(me(verified = false))
        val vm = newVm(durum = null)
        awaitView(vm)

        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"Çok fazla istek gönderildi. Lütfen bir süre sonra tekrar dene."}"""))
        vm.resend()

        val s = awaitState(vm) { it.error != null }
        assertEquals("Çok fazla istek gönderildi. Lütfen bir süre sonra tekrar dene.", s.error)
        assertNull(s.sentMessage)
        assertEquals(0, s.resendCooldown)
    }

    @Test
    fun openedFromTheAppAnUnverifiedMemberSeesTheInboxView() {
        session.signedIn(member)
        server.enqueue(me(verified = false))

        val s = awaitView(newVm(durum = null))

        assertEquals(HesapDogrulamaView.PENDING, s.view)
        assertFalse(s.otherAccountOpen)
        assertNull("ilk açılışta uyarı gösterilmez", s.error)
    }

    @Test
    fun checkingAgainSwitchesToVerifiedOnceTheLinkWasTappedElsewhere() {
        session.signedIn(member)
        server.enqueue(me(verified = false))
        val vm = newVm(durum = null)
        awaitView(vm)

        server.enqueue(me(verified = true))   // kullanıcı bağlantıya bilgisayarda dokundu
        val s = checkUntil(vm, silent = true) { it.view == HesapDogrulamaView.VERIFIED }
        assertTrue("bekleyen hâlden geçildi: \"doğrulandı\" denir", s.justVerified)
    }

    @Test
    fun anExplicitCheckThatIsStillUnverifiedTellsTheUser() {
        session.signedIn(member)
        server.enqueue(me(verified = false))
        val vm = newVm(durum = null)
        awaitView(vm)

        server.enqueue(me(verified = false))
        val s = checkUntil(vm, silent = false) { it.error != null }
        assertEquals(HesapDogrulamaViewModel.NOT_YET_VERIFIED, s.error)
        assertFalse(s.checking)
    }

    @Test
    fun alreadyVerifiedMemberOpeningTheScreenIsToldSo() {
        session.signedIn(member.copy(emailVerified = true))
        server.enqueue(me(verified = true))

        val s = awaitView(newVm(durum = null))

        assertEquals(HesapDogrulamaView.VERIFIED, s.view)
        assertFalse("\"zaten doğrulanmış\" denir", s.justVerified)
    }

    @Test
    fun rightAfterRegisteringTheAddressTheLinkWentToIsConfirmed() {
        session.signedIn(member)
        server.enqueue(me(verified = false))

        val s = awaitView(newVm(durum = null, kayit = "gonderildi"))

        assertEquals(HesapDogrulamaView.PENDING, s.view)
        assertEquals("ayse@ornek.com adresine doğrulama bağlantısı gönderdik.", s.sentMessage)
        assertNull(s.error)
    }

    @Test
    fun rightAfterRegisteringAnUnsentEmailIsAdmittedAndResendIsOffered() {
        session.signedIn(member)
        server.enqueue(me(verified = false))

        val s = awaitView(newVm(durum = null, kayit = "gonderilmedi"))

        assertEquals(HesapDogrulamaView.PENDING, s.view)
        assertEquals(HesapDogrulamaViewModel.REGISTER_EMAIL_NOT_SENT, s.error)
        assertNull("\"gönderdik\" denmez", s.sentMessage)
    }

    private fun verifyEmail(status: String) = MockResponse().setBody("""{"status":"$status"}""")

    @Test
    fun aLinkOpenedInTheAppIsConfirmedByTheScreen() {
        session.signedIn(member)
        server.enqueue(verifyEmail("DOGRULANDI"))
        server.enqueue(me(verified = true))

        val s = awaitView(newVm(durum = null, token = "t0k"))

        assertEquals(HesapDogrulamaView.VERIFIED, s.view)
        assertTrue(s.justVerified)
        val confirm = server.takeRequest()
        assertEquals("/api/v1/auth/verify-email", confirm.path)
        assertEquals("""{"token":"t0k"}""", confirm.body.readUtf8())
        assertTrue("açık oturum doğrulanmış kullanıcıyı görmeli", (session.state.value as SessionState.Member).user.emailVerified)
    }

    @Test
    fun anInvalidLinkOpenedInTheAppOffersANewOne() {
        session.signedIn(member)
        server.enqueue(verifyEmail("GECERSIZ"))
        server.enqueue(me(verified = false))

        assertEquals(HesapDogrulamaView.INVALID_LINK, awaitView(newVm(durum = null, token = "eski")).view)
    }

    @Test
    fun guestsOpeningTheLinkInTheAppAreVerifiedWithoutSigningIn() {
        session.markGuest()
        server.enqueue(verifyEmail("ZATEN_DOGRULANMIS"))
        assertEquals(HesapDogrulamaView.GUEST_VERIFIED, awaitView(newVm(durum = null, token = "t0k")).view)

        server.enqueue(verifyEmail("GECERSIZ"))
        assertEquals(HesapDogrulamaView.GUEST_INVALID, awaitView(newVm(durum = null, token = "eski")).view)
        assertEquals("yalnızca onay istekleri", 2, server.requestCount)
    }

    @Test
    fun whenTheLinkCannotBeConfirmedTheScreenOffersToRetry() {
        session.signedIn(member)
        server.enqueue(MockResponse().setResponseCode(503).setBody("""{"error":"Sunucu şu an yanıt veremiyor."}"""))
        val vm = newVm(durum = null, token = "t0k")

        val failed = awaitView(vm)
        assertEquals(HesapDogrulamaView.LINK_FAILED, failed.view)
        assertEquals("Sunucu şu an yanıt veremiyor.", failed.error)

        server.enqueue(verifyEmail("DOGRULANDI"))
        server.enqueue(me(verified = true))
        val s = checkUntil(vm, silent = false) { it.view == HesapDogrulamaView.VERIFIED }
        assertTrue(s.justVerified)
        assertNull(s.error)
        assertFalse(s.checking)
    }

    @Test
    fun guestsAreAskedToSignIn() {
        session.markGuest()
        assertEquals(HesapDogrulamaView.GUEST_VERIFIED, awaitView(newVm(durum = "")).view)
        assertEquals(HesapDogrulamaView.GUEST_INVALID, awaitView(newVm(durum = "gecersiz")).view)
        assertEquals("misafirde sunucuya sorulmaz", 0, server.requestCount)
    }
}
