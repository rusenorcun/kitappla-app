package com.kitap.app.ui.screens.profile

import com.kitap.app.data.api.ProfileApi
import com.kitap.app.data.repo.ProfileRepository
import com.kitap.app.data.repo.testRetrofit
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
        OgrenciDogrulamaViewModel(ProfileRepository(testRetrofit(server).create(ProfileApi::class.java)))

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
}
