package com.kitappla.app.ui.screens.donation

import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.data.api.DonationApi
import com.kitappla.app.data.api.PickupPointApi
import com.kitappla.app.data.repo.DonationRepository
import com.kitappla.app.data.repo.PickupPointRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** Kitabı alan taraf "Teslim aldım" ile teslimi onaylar; ardından "Teşekkür et" açılır (backend: deliver → thank). */
@OptIn(ExperimentalCoroutinesApi::class)
class AldiklarimViewModelTest {

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

    private fun claims(status: String) =
        MockResponse().setBody("""[{"id":5,"status":"$status","book":{"id":1,"title":"Kitap"}}]""")

    private fun newVm(): AldiklarimViewModel {
        val retrofit = testRetrofit(server)
        val cache = ApiCache(onStaleServed = {}, clock = { 0L })
        runBlocking {
            cache.cached("pickup-points:active", Long.MAX_VALUE) {
                com.kitappla.app.core.net.ApiResult.Success(emptyList<com.kitappla.app.data.dto.PickupPointDto>())
            }
        }
        return AldiklarimViewModel(
            DonationRepository(
                retrofit.create(DonationApi::class.java),
                cache,
            ),
            PickupPointRepository(
                retrofit.create(PickupPointApi::class.java),
                cache,
            ),
        )
    }

    private fun awaitState(vm: AldiklarimViewModel, predicate: (AldiklarimState) -> Boolean) =
        runBlocking { withTimeout(5_000) { vm.state.first(predicate) } }

    @Test
    fun deliverConfirmsReceiptThenReloadsTheList() {
        server.enqueue(claims("ARRANGED"))
        val vm = newVm()
        awaitState(vm) { it.items.isNotEmpty() }

        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(claims("DELIVERED"))
        vm.deliver(5)
        val s = awaitState(vm) { it.items.firstOrNull()?.status == "DELIVERED" }

        assertEquals("Kitabı teslim aldığınız kaydedildi.", s.actionMessage)
        assertEquals("GET /api/v1/my/claims", server.takeRequest().let { "${it.method} ${it.path}" })
        assertEquals("POST /api/v1/claims/5/deliver", server.takeRequest().let { "${it.method} ${it.path}" })
    }

    @Test
    fun aRejectedDeliverShowsTheServerMessageAndKeepsTheList() {
        server.enqueue(claims("MATCHED"))
        val vm = newVm()
        awaitState(vm) { it.items.isNotEmpty() }

        server.enqueue(
            MockResponse().setResponseCode(400).setBody("""{"error":"Önce buluşma ayarlayın, sonra teslimi onaylayın."}""")
        )
        vm.deliver(5)
        val s = awaitState(vm) { it.actionMessage != null }

        assertEquals("Önce buluşma ayarlayın, sonra teslimi onaylayın.", s.actionMessage)
        assertEquals("MATCHED", s.items.single().status)
        assertNull(s.error)
    }
}
