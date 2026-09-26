package com.kitappla.app.ui.screens.profile

import com.kitappla.app.data.api.ProfileApi
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Backend `StudentStatus` enum'u NONE/PENDING/APPROVED/REJECTED'tır. E-posta bağlantısı (bilgisayar dahil) başka bir
 * yerde onaylandığında mobil uygulama bunu "onaylı öğrenci" olarak göstermeli.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OgrenciDogrulamaViewModelTest {
    private lateinit var server: MockWebServer

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

    private fun me(status: String) = MockResponse().setBody(
        """{"user":{"id":1,"name":"Ayşe","admin":false,"studentStatus":"$status"}}"""
    )

    private fun newVm() =
        OgrenciDogrulamaViewModel(
            ProfileRepository(
                android.content.ContextWrapper(null),
                testRetrofit(server).create(ProfileApi::class.java),
            )
        )

    private fun awaitState(vm: OgrenciDogrulamaViewModel, predicate: (OgrenciDogrulamaState) -> Boolean) =
        runBlocking { withTimeout(5_000) { vm.state.first(predicate) } }

    @Test
    fun approvedStatusFromTheBackendIsShownAsConfirmed() {
        server.enqueue(me("APPROVED"))
        val s = awaitState(newVm()) { it.user != null }
        assertEquals("APPROVED", s.user?.studentStatus)
        assertTrue("APPROVED onaylı öğrenci sayılmalı", s.isConfirmed)
    }

    @Test
    fun pendingStatusIsNotConfirmed() {
        server.enqueue(me("PENDING"))
        val s = awaitState(newVm()) { it.user != null }
        assertFalse(s.isConfirmed)
    }

    @Test
    fun aPastedEmailLinkIsReducedToItsTokenBeforeItIsSent() {
        server.enqueue(me("PENDING"))
        val vm = newVm()
        awaitState(vm) { it.user != null }

        server.enqueue(MockResponse().setBody("""{"id":1,"name":"Ayşe","admin":false,"studentStatus":"APPROVED"}"""))
        vm.updateToken("https://www.kitappla.com/profil/ogrenci/eposta/onay?token=abc_DEF-123")
        vm.confirmToken()
        assertTrue(awaitState(vm) { it.isConfirmed }.isConfirmed)

        server.takeRequest()   // GET /me
        val confirm = server.takeRequest()
        assertEquals("/api/v1/me/student/confirm", confirm.path)
        assertEquals("""{"token":"abc_DEF-123"}""", confirm.body.readUtf8())
    }

    @Test
    fun loadWhileAnotherLoadIsRunningDoesNotSendASecondRequest() {
        server.enqueue(me("PENDING").setBodyDelay(300, java.util.concurrent.TimeUnit.MILLISECONDS))
        val vm = newVm()                      // init yüklemesi sürüyor
        runBlocking { vm.load().join() }      // açılıştaki ilk ON_RESUME
        awaitState(vm) { it.user != null }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun reloadPicksUpAConfirmationDoneOnAnotherDevice() {
        server.enqueue(me("PENDING"))
        val vm = newVm()
        assertFalse(awaitState(vm) { it.user != null }.isConfirmed)

        server.enqueue(me("APPROVED"))   // kullanıcı bilgisayardaki bağlantıyı onayladı
        vm.load()
        assertTrue(awaitState(vm) { it.isConfirmed }.isConfirmed)
    }

    @Test
    fun sendVerificationShowsSentMessageWhenTheBackendActuallySendsTheEmail() {
        server.enqueue(me("PENDING"))
        val vm = newVm()
        awaitState(vm) { it.user != null }

        server.enqueue(
            MockResponse()
                .setHeader("X-Student-Verification-Sent", "true")
                .setBody("""{"id":1,"name":"Ayşe","admin":false,"studentStatus":"PENDING"}""")
        )
        vm.updateEmail("ogrenci@okul.edu.tr")
        vm.sendVerification()

        val s = awaitState(vm) { it.actionMessage != null }
        assertEquals("Doğrulama bağlantısı e-posta adresinize gönderildi.", s.actionMessage)
        assertTrue(s.verificationSent)
        assertEquals(null, s.error)
    }

    /**
     * Backend mail servisi kapalıyken ya da art arda çok deneme yapıldığında 200 döner ama e-postayı
     * sessizce atlar; bunu `X-Student-Verification-Sent: false` başlığıyla işaretler. Uygulama bu
     * durumda "gönderildi" yalanını söylememeli, kullanıcıyı gerçek durumdan haberdar etmeli.
     */
    @Test
    fun sendVerificationWarnsWhenTheBackendSilentlySkipsSendingTheEmail() {
        server.enqueue(me("PENDING"))
        val vm = newVm()
        awaitState(vm) { it.user != null }

        server.enqueue(
            MockResponse()
                .setHeader("X-Student-Verification-Sent", "false")
                .setBody("""{"id":1,"name":"Ayşe","admin":false,"studentStatus":"PENDING"}""")
        )
        vm.updateEmail("ogrenci@okul.edu.tr")
        vm.sendVerification()

        val s = awaitState(vm) { it.error != null }
        assertEquals(
            "Öğrenci doğrulaman beklemede; e-posta gönderilmedi. Daha sonra tekrar dene.",
            s.error,
        )
        assertFalse(s.verificationSent)
        assertEquals(null, s.actionMessage)
    }

    /** Yol bazlı yanıt: bağlantıdan açılışta onay ve profil istekleri aynı anda gider, sıraları belirsizdir. */
    private fun route(confirm: MockResponse, vararg profiles: MockResponse) {
        val queue = java.util.ArrayDeque(profiles.toList())
        server.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
            override fun dispatch(request: okhttp3.mockwebserver.RecordedRequest): MockResponse = when (request.path) {
                "/api/v1/me/student/confirm" -> confirm
                else -> synchronized(queue) { queue.pollFirst() ?: profiles.last() }
            }
        }
    }

    private fun newVmFromLink(token: String) =
        OgrenciDogrulamaViewModel(
            ProfileRepository(
                android.content.ContextWrapper(null),
                testRetrofit(server).create(ProfileApi::class.java),
            ),
            androidx.lifecycle.SavedStateHandle(mapOf("token" to token)),
        )

    @Test
    fun openingTheSchoolEmailLinkConfirmsAndCelebrates() {
        route(
            confirm = MockResponse().setBody("""{"id":1,"name":"Ayşe","admin":false,"studentStatus":"APPROVED"}"""),
            me("PENDING"), me("APPROVED"),
        )
        val vm = newVmFromLink("t0k")

        val s = awaitState(vm) { it.justConfirmed }
        assertTrue(s.openedFromLink)
        assertTrue(s.isConfirmed)
        assertEquals(null, s.linkError)
        assertEquals("sonuç ekranda büyük gösterilir, ayrıca uyarı çıkmaz", null, s.actionMessage)
    }

    @Test
    fun aFailedLinkIsExplainedAboveTheFormNotBelowIt() {
        route(
            confirm = MockResponse().setResponseCode(400)
                .setBody("""{"error":"Bağlantı geçersiz ya da süresi dolmuş. Yeniden doğrulama iste."}"""),
            me("PENDING"),
        )
        val vm = newVmFromLink("eski")

        val s = awaitState(vm) { it.linkError != null }
        assertEquals("Bağlantı geçersiz ya da süresi dolmuş. Yeniden doğrulama iste.", s.linkError)
        assertEquals(null, s.error)
        assertFalse(s.isConfirmed)
    }

    @Test
    fun tappingAnAlreadyUsedLinkWhenAlreadyApprovedIsNotAnError() {
        route(
            confirm = MockResponse().setResponseCode(400).setBody("""{"error":"Bekleyen e-posta doğrulaması bulunamadı."}"""),
            me("APPROVED"),
        )
        val vm = newVmFromLink("harcanmis")

        val s = awaitState(vm) { it.isConfirmed && !it.confirmingToken }
        assertEquals(null, s.linkError)
        assertEquals(null, s.error)
        assertFalse(s.justConfirmed)
    }

    /** Backend'in e-posta çakışması hatası ("... başka bir hesapta kullanılıyor.") olduğu gibi gösterilmeli. */
    @Test
    fun sendVerificationShowsTheBackendsMessageWhenTheEmailIsAlreadyRegistered() {
        server.enqueue(me("PENDING"))
        val vm = newVm()
        awaitState(vm) { it.user != null }

        server.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error":"Bu okul adresi başka bir hesapta kullanılıyor."}""")
        )
        vm.updateEmail("ogrenci@okul.edu.tr")
        vm.sendVerification()

        val s = awaitState(vm) { it.error != null }
        assertEquals("Bu okul adresi başka bir hesapta kullanılıyor.", s.error)
        assertEquals(null, s.actionMessage)
    }
}
