package com.kitappla.app.ui.screens.swap

import android.content.ContextWrapper
import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.data.api.BookApi
import com.kitappla.app.data.api.RequestApi
import com.kitappla.app.data.api.SwapApi
import com.kitappla.app.data.api.UploadApi
import com.kitappla.app.data.repo.BookRepository
import com.kitappla.app.data.repo.RequestRepository
import com.kitappla.app.data.repo.SwapRepository
import com.kitappla.app.data.repo.UploadRepository
import com.kitappla.app.data.repo.testRetrofit
import com.kitappla.app.ui.screens.common.BookFields
import com.kitappla.app.ui.screens.request.IstekYeniViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/** Takasa kitap ekleme ve istek formları: linkten getirme, kapak ve zorunlu yazar. */
@OptIn(ExperimentalCoroutinesApi::class)
class TakasKitapEkleViewModelTest {
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

    private val cache = ApiCache(onStaleServed = {}, clock = System::currentTimeMillis)
    private fun books() = BookRepository(testRetrofit(server).create(BookApi::class.java))

    // Yükleme bu testlerde kullanılmaz; bağlam yalnızca kurucunun gereği.
    private fun uploads() = UploadRepository(ContextWrapper(null), testRetrofit(server).create(UploadApi::class.java))

    private fun swapVm() = TakasKitapEkleViewModel(
        SwapRepository(testRetrofit(server).create(SwapApi::class.java), cache), books(), uploads(),
    )

    private fun requestVm() = IstekYeniViewModel(
        RequestRepository(testRetrofit(server).create(RequestApi::class.java), cache), books(), uploads(),
    )

    @Test
    fun swapBookWithoutAuthorIsNotSentAndAsksForTheAuthor() {
        val vm = swapVm()
        vm.updateTitle("Simyacı")

        var added = false
        vm.submit { added = true }

        assertEquals(0, server.requestCount)
        assertEquals(false, added)
        assertEquals(BookFields.AUTHOR_REQUIRED, vm.book.value.validationMessage)
        assertEquals(BookFields.AUTHOR_REQUIRED, vm.book.value.authorError)
    }

    @Test
    fun requestWithoutAuthorIsNotSent() {
        // Sunucu yazarsız kaydı "beklenmeyen hata" ile reddediyordu; istek hiç gönderilmemeli.
        val vm = requestVm()
        vm.updateTitle("Simyacı")

        vm.submit { }

        assertEquals(0, server.requestCount)
        assertEquals(BookFields.AUTHOR_REQUIRED, vm.book.value.validationMessage)
    }

    @Test
    fun linkPreviewFillsTheFormAndTheCoverIsSentWithTheSwapBook() {
        server.enqueue(
            MockResponse().setBody(
                """{"found":true,"title":"Simyacı","author":"Paulo Coelho","coverUrl":"https://img.magaza.com/k.jpg"}""",
            ),
        )
        server.enqueue(MockResponse().setResponseCode(201).setBody("""{"id":7,"status":"OPEN"}"""))
        val vm = swapVm()

        vm.updatePurchaseLink("https://www.magaza.com/simyaci")
        vm.fetchPreview()
        val filled = runBlocking { withTimeout(5_000) { vm.book.first { !it.fetchingPreview && it.title.isNotEmpty() } } }
        assertEquals("Paulo Coelho", filled.author)
        assertEquals("https://img.magaza.com/k.jpg", filled.coverUrl)
        server.takeRequest(5, TimeUnit.SECONDS)

        vm.updateNote("Roman olur")
        var added = false
        vm.submit { added = true }

        val sent = server.takeRequest(5, TimeUnit.SECONDS)!!
        runBlocking { withTimeout(5_000) { while (!added) delay(10) } }
        assertEquals("/api/v1/swap/books", sent.path)
        val body = sent.body.readUtf8()
        assertTrue(body, body.contains(""""coverUrl":"https://img.magaza.com/k.jpg""""))
        assertTrue(body, body.contains(""""author":"Paulo Coelho""""))
        assertTrue(body, body.contains(""""purchaseLink":"https://www.magaza.com/simyaci""""))
        assertTrue(body, body.contains(""""note":"Roman olur""""))
    }

    @Test
    fun unreadableLinkIsReported() {
        server.enqueue(MockResponse().setBody("""{"found":false}"""))
        val vm = swapVm()

        vm.updatePurchaseLink("https://www.magaza.com/yok")
        vm.fetchPreview()
        val s = runBlocking { withTimeout(5_000) { vm.book.first { !it.fetchingPreview && it.notice != null } } }

        assertEquals(BookFields.LINK_NOT_FOUND, s.notice)
    }
}
