package com.kitappla.app.ui.screens.admin

import com.kitappla.app.data.api.AdminApi
import com.kitappla.app.data.repo.AdminRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Backend `AdminMonitorDto` (GET /api/v1/admin/monitor) yanıtı ve izleç ekranının durumu. */
@OptIn(ExperimentalCoroutinesApi::class)
class AdminIzlecViewModelTest {
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

    /** Backend kaydının alan adlarıyla birebir örnek (dbActiveConnections null olabilir). */
    private val monitorJson = """
        {"summary":{"activeSessions":3,"onlineUsers":2,"onlineAdmins":1,"onlineStudents":1,"persistentLogins":4,
         "uptimeFormatted":"2 sa 5 dk","uptimeMillis":7500000,"startTime":"24.09.2026 00:10:00","usedMemoryMb":300,
         "totalMemoryMb":512,"maxMemoryMb":1024,"memoryPercent":29,"availableProcessors":4,"threadCount":40,
         "peakThreadCount":55,"dbConnected":true,"dbProduct":"PostgreSQL 16.4","dbActiveConnections":null,
         "dbIdleConnections":8,"loginFailuresTracked":0},
         "sessions":[{"id":"6f1c2b9e-opak","maskedSessionId":"A1B2C...9Z8Y7","userId":7,"userName":"Ayşe","userEmail":"a@b.c",
         "initials":"AY","admin":false,"student":true,"blocked":false,"lastRequestTime":"02:10:01",
         "lastRequestRelative":"Az önce","currentSession":false}]}
    """.trimIndent()

    private fun newVm() = AdminIzlecViewModel(AdminRepository(testRetrofit(server).create(AdminApi::class.java)))

    private fun awaitState(vm: AdminIzlecViewModel, predicate: (AdminIzlecState) -> Boolean) =
        runBlocking { withTimeout(5_000) { vm.state.first(predicate) } }

    @Test
    fun parsesTheBackendMonitorPayload() {
        server.enqueue(MockResponse().setBody(monitorJson))
        val s = awaitState(newVm()) { it.data != null }

        val data = s.data!!
        assertEquals(3L, data.summary.activeSessions)
        assertEquals(null, data.summary.dbActiveConnections)
        assertEquals("Ayşe", data.sessions.single().userName)
        assertEquals("opak tanıtıcı okunur (ham oturum kimliği değil)", "6f1c2b9e-opak", data.sessions.single().id)
        assertTrue(data.sessions.single().student)
        assertNotNull(s.lastUpdated)
        assertFalse(s.isLoading)
        assertEquals("/api/v1/admin/monitor", server.takeRequest().path)
    }

    @Test
    fun aFailedSilentRefreshKeepsTheLastDataAndShowsTheError() {
        server.enqueue(MockResponse().setBody(monitorJson))
        val vm = newVm()
        awaitState(vm) { it.data != null }

        server.enqueue(MockResponse().setResponseCode(500).setBody("""{"error":"Sunucu hatası"}"""))
        runBlocking { withTimeout(5_000) { while (vm.state.value.error == null) vm.load(silent = true).join() } }

        val s = vm.state.value
        assertEquals("Sunucu hatası", s.error)
        assertEquals(3L, s.data?.summary?.activeSessions)
    }

    @Test
    fun expiringASessionCallsTheOpaqueTokenEndpointAndRefreshes() {
        server.enqueue(MockResponse().setBody(monitorJson))
        val vm = newVm()
        awaitState(vm) { it.data != null }
        server.takeRequest()   // açılış GET

        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setBody(monitorJson.replace(Regex("\"sessions\":\\[.*\\]}", RegexOption.DOT_MATCHES_ALL), "\"sessions\":[]}")))
        vm.expireSession("6f1c2b9e-opak", "Ayşe")

        val s = awaitState(vm) { it.actionMessage != null && it.data?.sessions?.isEmpty() == true }
        assertEquals("Ayşe adlı kullanıcının oturumu sonlandırıldı.", s.actionMessage)
        assertEquals(null, s.expiringId)
        val expire = server.takeRequest()
        assertEquals("POST /api/v1/admin/monitor/sessions/6f1c2b9e-opak/expire", "${expire.method} ${expire.path}")
    }

    @Test
    fun refusingToExpireOwnSessionShowsTheServersReason() {
        server.enqueue(MockResponse().setBody(monitorJson))
        val vm = newVm()
        awaitState(vm) { it.data != null }

        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"Kendi geçerli oturumunu sonlandıramazsın."}"""))
        server.enqueue(MockResponse().setBody(monitorJson))
        vm.expireSession("kendi", "Yönetici")

        val s = awaitState(vm) { it.actionMessage != null }
        assertEquals("Kendi geçerli oturumunu sonlandıramazsın.", s.actionMessage)
        assertEquals(null, s.expiringId)
    }

    @Test
    fun liveCanBeSwitchedOff() {
        server.enqueue(MockResponse().setBody(monitorJson))
        val vm = newVm()
        awaitState(vm) { it.data != null }
        vm.setLive(false)
        assertFalse(vm.state.value.live)
    }
}
