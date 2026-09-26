package com.kitappla.app.core.document

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

/** Öğrenci belgesi uygulama içinde gösterilir: PDF sayfaları görsele çevrilir, geçici dosya bırakılmaz. */
class DocumentRendererTest {
    private val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir

    private fun pdf(pages: Int): ByteArray {
        val doc = PdfDocument()
        repeat(pages) { i ->
            val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, i + 1).create())
            page.canvas.drawText("Öğrenci belgesi sayfa ${i + 1}", 72f, 72f, Paint().apply { textSize = 18f })
            doc.finishPage(page)
        }
        return ByteArrayOutputStream().also { doc.writeTo(it); doc.close() }.toByteArray()
    }

    @Test
    fun pdfPagesAreRenderedAtFixedWidthAndTheTempFileIsRemoved() {
        val before = cacheDir.listFiles()?.count { it.name.startsWith("belge") } ?: 0
        val r = DocumentRenderer.render(pdf(2), "application/pdf", cacheDir) as RenderedDocument.Pages

        assertEquals(2, r.pages.size)
        assertFalse(r.truncated)
        assertEquals(DocumentRenderer.PAGE_WIDTH_PX, r.pages.first().width)
        assertTrue("A4 oranı korunur", r.pages.first().height > r.pages.first().width)
        assertEquals(before, cacheDir.listFiles()?.count { it.name.startsWith("belge") } ?: 0)
    }

    @Test
    fun longPdfsAreCappedAtMaxPages() {
        val r = DocumentRenderer.render(pdf(DocumentRenderer.MAX_PAGES + 2), "application/pdf", cacheDir) as RenderedDocument.Pages
        assertEquals(DocumentRenderer.MAX_PAGES, r.pages.size)
        assertTrue(r.truncated)
    }

    @Test
    fun imagesAndTextAreShownAsIs() {
        val png = ByteArrayOutputStream().also {
            Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }.toByteArray()
        val image = DocumentRenderer.render(png, "image/png", cacheDir) as RenderedDocument.Pages
        assertEquals(40, image.pages.single().width)

        val text = DocumentRenderer.render("Belge no: 123".toByteArray(), "text/plain; charset=utf-8", cacheDir)
        assertEquals("Belge no: 123", (text as RenderedDocument.Text).text)

        assertEquals(RenderedDocument.Unsupported, DocumentRenderer.render(byteArrayOf(1, 2), "application/octet-stream", cacheDir))
        assertEquals(RenderedDocument.Unsupported, DocumentRenderer.render(byteArrayOf(1, 2), "image/png", cacheDir))
    }
}
