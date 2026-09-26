package com.kitappla.app.ui.screens.profile

import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.core.net.InMemoryCookiePersistence
import com.kitappla.app.core.net.PersistentCookieJar
import com.kitappla.app.core.session.SessionManager
import com.kitappla.app.data.api.AuthApi
import com.kitappla.app.data.api.ProfileApi
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** Uygulamaya dönünce (ör. doğrulama bağlantısı bilgisayarda onaylandıktan sonra) profil sessizce yenilenmeli. */
@OptIn(ExperimentalCoroutinesApi::class)
class PanomViewModelTest {
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

    private fun newVm(): PanomViewModel {
        val retrofit = testRetrofit(server)
        val session = SessionManager(
            PersistentCookieJar(InMemoryCookiePersistence()),
            ApiCache(onStaleServed = {}, clock = { 0L }),
        )
        return PanomViewModel(
            ProfileRepository(
                android.content.ContextWrapper(null),
                retrofit.create(ProfileApi::class.java),
            ),
            AuthRepository(retrofit.create(AuthApi::class.java), session),
        )
    }

    private fun awaitLoaded(vm: PanomViewModel) =
        runBlocking { withTimeout(5_000) { vm.state.first { !it.loading && it.me != null } } }

    @Test
    fun refreshPicksUpAnApprovalDoneElsewhereWithoutShowingTheSpinner() {
        server.enqueue(me("PENDING"))
        val vm = newVm()
        assertEquals("PENDING", awaitLoaded(vm).me?.user?.studentStatus)

        server.enqueue(me("APPROVED"))
        val job = vm.refresh()
        assertFalse("sessiz yenileme içeriği spinner ile değiştirmemeli", vm.state.value.loading)
        runBlocking { job.join() }
        assertEquals("APPROVED", vm.state.value.me?.user?.studentStatus)
    }

    @Test
    fun failedRefreshKeepsWhatWasShownAndRaisesNoError() {
        server.enqueue(me("PENDING"))
        val vm = newVm()
        awaitLoaded(vm)

        server.enqueue(MockResponse().setResponseCode(500))
        runBlocking { vm.refresh().join() }
        assertEquals("PENDING", vm.state.value.me?.user?.studentStatus)
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun refreshWhileTheFirstLoadIsStillRunningDoesNotSendASecondRequest() {
        server.enqueue(me("PENDING").setBodyDelay(300, java.util.concurrent.TimeUnit.MILLISECONDS))
        val vm = newVm()          // init yüklemesi sürüyor
        runBlocking { vm.refresh().join() }   // açılıştaki ilk ON_RESUME: mevcut yüklemeye katılır
        awaitLoaded(vm)
        assertEquals(1, server.requestCount)
    }
}
