package com.kitappla.app.core.document

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File

/** Belgenin nasıl gösterileceği (backend `DocumentService.resolveContentType`: pdf, jpeg, png, düz metin). */
enum class DocumentKind {
    PDF, IMAGE, TEXT, UNSUPPORTED;

    companion object {
        fun of(contentType: String?): DocumentKind {
            val type = contentType?.substringBefore(';')?.trim()?.lowercase().orEmpty()
            return when {
                type == "application/pdf" -> PDF
                type.startsWith("image/") -> IMAGE
                type.startsWith("text/") -> TEXT
                else -> UNSUPPORTED
            }
        }
    }
}

/** Ekranda gösterilecek hâl: sayfa görselleri ya da metin. */
sealed interface RenderedDocument {
    class Pages(val pages: List<Bitmap>, val truncated: Boolean) : RenderedDocument
    class Text(val text: String) : RenderedDocument
    data object Unsupported : RenderedDocument
}

/**
 * Öğrenci belgesini uygulama içinde gösterilecek biçime çevirir. Hassas bir belge olduğu için başka bir uygulamaya
 * verilmez: PDF sayfaları görsele çevrilir (geçici dosya [cacheDir]'da açılır ve hemen silinir), görseller büyükse
 * küçültülerek çözülür. Ağ ya da disk erişimi olduğundan ana iş parçacığında çağrılmamalı.
 */
object DocumentRenderer {
    /** Çok sayfalı belgede bellek sınırı: öğrenci belgesi genelde 1-2 sayfadır. */
    const val MAX_PAGES = 10

    /** Sayfa görsellerinin genişliği (piksel): küçük yazılar yakınlaştırınca okunabilsin. */
    const val PAGE_WIDTH_PX = 1600

    private const val MAX_IMAGE_EDGE = 2560

    fun render(bytes: ByteArray, contentType: String?, cacheDir: File): RenderedDocument =
        when (DocumentKind.of(contentType)) {
            DocumentKind.PDF -> renderPdf(bytes, cacheDir)
            DocumentKind.IMAGE -> decodeImage(bytes)?.let { RenderedDocument.Pages(listOf(it), truncated = false) }
                ?: RenderedDocument.Unsupported
            DocumentKind.TEXT -> RenderedDocument.Text(bytes.toString(Charsets.UTF_8))
            DocumentKind.UNSUPPORTED -> RenderedDocument.Unsupported
        }

    private fun renderPdf(bytes: ByteArray, cacheDir: File): RenderedDocument {
        val temp = File.createTempFile("belge", ".pdf", cacheDir)
        try {
            temp.writeBytes(bytes)
            ParcelFileDescriptor.open(temp, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                PdfRenderer(fd).use { renderer ->
                    val count = minOf(renderer.pageCount, MAX_PAGES)
                    val pages = (0 until count).map { index ->
                        renderer.openPage(index).use { page ->
                            val height = (PAGE_WIDTH_PX.toFloat() * page.height / page.width).toInt().coerceAtLeast(1)
                            Bitmap.createBitmap(PAGE_WIDTH_PX, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                                bitmap.eraseColor(Color.WHITE)   // saydam PDF arka planı koyu temada okunmaz olmasın
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            }
                        }
                    }
                    return RenderedDocument.Pages(pages, truncated = renderer.pageCount > MAX_PAGES)
                }
            }
        } finally {
            temp.delete()
        }
    }

    private fun decodeImage(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_IMAGE_EDGE) sample *= 2
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}
