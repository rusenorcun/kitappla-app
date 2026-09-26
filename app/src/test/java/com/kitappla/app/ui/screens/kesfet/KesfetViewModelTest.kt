package com.kitappla.app.ui.screens.kesfet

import com.kitappla.app.core.net.ApiMessages
import com.kitappla.app.data.repo.donationJson
import com.kitappla.app.data.repo.donationRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KesfetViewModelTest {
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

    private fun enqueuePage(range: IntRange, total: Int) = server.enqueue(
        MockResponse().setHeader("X-Total-Count", total.toString())
            .setBody(range.joinToString(prefix = "[", postfix = "]") { donationJson(it) })
    )

    private fun newVm() = KesfetViewModel(donationRepository(server))

    private fun awaitIdle(vm: KesfetViewModel) = runBlocking {
        withTimeout(5_000) { vm.state.first { !it.loading && !it.loadingMore } }
    }

    @Test
    fun firstPageLoadsAndIsNotTheEnd() {
        enqueuePage(1..24, total = 30)
        val s = awaitIdle(newVm())
        assertEquals(24, s.items.size)
        assertEquals(30L, s.total)
        assertFalse(s.endReached)
        assertNull(s.error)
    }

    @Test
    fun loadMoreAppendsThenStopsAtTheEnd() {
        enqueuePage(1..24, total = 30)
        val vm = newVm()
        awaitIdle(vm)

        enqueuePage(25..30, total = 30)
        vm.loadMore()
        val s = awaitIdle(vm)
        assertEquals(30, s.items.size)
        assertTrue(s.endReached)

        vm.loadMore()   // bitti: yeni istek atılmamalı
        assertEquals(2, server.requestCount)
    }

    @Test
    fun pagesAreRequestedInOrder() {
        enqueuePage(1..24, total = 30)
        val vm = newVm()
        awaitIdle(vm)
        enqueuePage(25..30, total = 30)
        vm.loadMore()
        awaitIdle(vm)
        assertTrue(server.takeRequest().path!!.contains("page=0"))
        assertTrue(server.takeRequest().path!!.contains("page=1"))
    }

    @Test
    fun duplicateIdAcrossPagesIsDroppedKeepingFirstOccurrence() {
        enqueuePage(1..24, total = 25)
        val vm = newVm()
        awaitIdle(vm)

        // Sayfalar arasında yeni bağış oluşunca 24 numaralı kayıt ikinci sayfada yeniden gelir.
        enqueuePage(24..25, total = 25)
        vm.loadMore()
        val s = awaitIdle(vm)
        assertEquals((1..25).map { it.toLong() }, s.items.map { it.id })
        assertEquals(s.items.size, s.items.map { it.id }.toSet().size)
        assertTrue(s.endReached)
    }

    @Test
    fun retryAfterLaterPageFailureResumesAtTheFailedPage() {
        enqueuePage(1..24, total = 30)
        val vm = newVm()
        awaitIdle(vm)

        server.enqueue(MockResponse().setResponseCode(500))
        vm.loadMore()
        val failed = awaitIdle(vm)
        assertEquals(24, failed.items.size)
        assertEquals(ApiMessages.SERVER_ERROR, failed.error)

        enqueuePage(25..30, total = 30)
        vm.retry()
        val ok = awaitIdle(vm)
        assertEquals(30, ok.items.size)
        assertNull(ok.error)
        assertTrue(ok.endReached)

        assertTrue(server.takeRequest().path!!.contains("page=0"))
        assertTrue(server.takeRequest().path!!.contains("page=1"))
        assertTrue(server.takeRequest().path!!.contains("page=1"))
        assertEquals(3, server.requestCount)
    }

    @Test
    fun retryWithoutItemsBehavesLikeRefresh() {
        server.enqueue(MockResponse().setResponseCode(500))
        val vm = newVm()
        assertEquals(ApiMessages.SERVER_ERROR, awaitIdle(vm).error)

        enqueuePage(1..3, total = 3)
        vm.retry()
        val ok = awaitIdle(vm)
        assertNull(ok.error)
        assertEquals(3, ok.items.size)
        assertTrue(server.takeRequest().path!!.contains("page=0"))
        assertTrue(server.takeRequest().path!!.contains("page=0"))
    }

    @Test
    fun errorOnFirstPageIsShownAndRefreshRecovers() {
        server.enqueue(MockResponse().setResponseCode(500))
        val vm = newVm()
        val failed = awaitIdle(vm)
        assertEquals(ApiMessages.SERVER_ERROR, failed.error)
        assertTrue(failed.items.isEmpty())

        vm.loadMore()   // hata varken sessizce yeni sayfa istenmez
        assertEquals(1, server.requestCount)

        enqueuePage(1..3, total = 3)
        vm.refresh()
        val ok = awaitIdle(vm)
        assertNull(ok.error)
        assertEquals(3, ok.items.size)
        assertTrue(ok.endReached)
    }
}
