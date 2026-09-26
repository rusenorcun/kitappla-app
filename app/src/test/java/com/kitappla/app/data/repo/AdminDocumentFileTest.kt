package com.kitappla.app.data.repo

import com.kitappla.app.core.document.DocumentKind
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.api.AdminApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Yöneticinin öğrenci belgesini indirmesi: backend `GET /api/v1/admin/docs/{userId}/file`. */
class AdminDocumentFileTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: AdminRepository

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        repo = AdminRepository(testRetrofit(server).create(AdminApi::class.java))
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun downloadsBytesWithTheirContentType() = runBlocking {
        val pdf = "%PDF-1.4 belge".toByteArray()
        server.enqueue(MockResponse().setHeader("Content-Type", "application/pdf").setBody(Buffer().write(pdf)))

        val r = repo.getDocumentFile(42)

        assertTrue(r is ApiResult.Success)
        val file = (r as ApiResult.Success).value
        assertArrayEquals(pdf, file.bytes)
        assertEquals("application/pdf", file.contentType)
        assertEquals("/api/v1/admin/docs/42/file", server.takeRequest().path)
    }

    @Test
    fun missingDocumentShowsTheServersMessage() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":"Belge bulunamadı."}"""))
        assertEquals(ApiResult.Failure("Belge bulunamadı.", 404), repo.getDocumentFile(7))
    }

    @Test
    fun documentKindFollowsTheBackendContentTypes() {
        assertEquals(DocumentKind.PDF, DocumentKind.of("application/pdf"))
        assertEquals(DocumentKind.IMAGE, DocumentKind.of("image/jpeg"))
        assertEquals(DocumentKind.IMAGE, DocumentKind.of("image/png"))
        assertEquals(DocumentKind.TEXT, DocumentKind.of("text/plain; charset=utf-8"))
        assertEquals(DocumentKind.UNSUPPORTED, DocumentKind.of("application/octet-stream"))
        assertEquals(DocumentKind.UNSUPPORTED, DocumentKind.of(null))
    }
}
