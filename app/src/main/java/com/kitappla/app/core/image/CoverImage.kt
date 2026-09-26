package com.kitappla.app.core.image

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream

/** Yüklemeye hazır görsel. */
class PreparedImage(val bytes: ByteArray, val mimeType: String, val fileName: String)

/**
 * Kapak görselini sunucu sınırlarına sığdırır (backend `CoverService`: en fazla 3 MB; JPG/PNG/WEBP/GIF; aşırı yüksek
 * çözünürlük reddedilir). Telefon kamerası 4-8 MB ve 4000 px üstü (bazen HEIC) fotoğraf üretir; bunlar olduğu gibi
 * gönderilince "en fazla 3 MB" ya da "çözünürlüğü çok yüksek" hatası alınıyordu. Zaten küçük ve desteklenen biçimdeki
 * görsel (saydam PNG, GIF) dokunulmadan gönderilir; gerisi en uzun kenarı [MAX_EDGE] olacak şekilde küçültülüp,
 * EXIF yönü uygulanarak JPEG'e çevrilir.
 */
object CoverImage {
    const val MAX_EDGE = 1280
    const val PASS_THROUGH_BYTES = 1_500_000
    private const val JPEG_QUALITY = 85
    private val SUPPORTED = setOf("image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif")

    /** Kod çözme için 2'nin kuvveti örnekleme: sonuç en uzun kenarda hedefin iki katından küçük kalmaz. */
    fun sampleSize(width: Int, height: Int, maxEdge: Int = MAX_EDGE): Int {
        var sample = 1
        while (maxOf(width, height) / (sample * 2) >= maxEdge) sample *= 2
        return sample
    }

    /** En uzun kenar [maxEdge]'i aşmayacak şekilde oranı koruyarak ölçekler. */
    fun targetSize(width: Int, height: Int, maxEdge: Int = MAX_EDGE): Pair<Int, Int> {
        val longest = maxOf(width, height)
        if (longest <= maxEdge) return width to height
        val scale = maxEdge.toDouble() / longest
        return maxOf(1, (width * scale).toInt()) to maxOf(1, (height * scale).toInt())
    }

    fun needsShrinking(sizeBytes: Int, mimeType: String?, width: Int, height: Int): Boolean =
        sizeBytes > PASS_THROUGH_BYTES || mimeType?.lowercase() !in SUPPORTED || maxOf(width, height) > MAX_EDGE * 2

    /** [uri]'deki görseli okur ve gerekiyorsa küçültür. Okunamazsa ya da görsel değilse `null`. */
    fun prepare(resolver: ContentResolver, uri: Uri): PreparedImage? {
        val original = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val mimeType = resolver.getType(uri)?.lowercase()

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(original, 0, original.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        if (!needsShrinking(original.size, mimeType, bounds.outWidth, bounds.outHeight)) {
            val type = if (mimeType == "image/jpg") "image/jpeg" else mimeType!!
            return PreparedImage(original, type, "cover" + extensionOf(type))
        }

        val decoded = BitmapFactory.decodeByteArray(
            original, 0, original.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight) },
        ) ?: return null
        val (w, h) = targetSize(decoded.width, decoded.height)
        val scaled = if (w != decoded.width || h != decoded.height) Bitmap.createScaledBitmap(decoded, w, h, true) else decoded
        val oriented = rotateByExif(scaled, original)

        val out = ByteArrayOutputStream()
        oriented.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        listOf(decoded, scaled, oriented).distinct().forEach { it.recycle() }
        return PreparedImage(out.toByteArray(), "image/jpeg", "cover.jpg")
    }

    private fun rotateByExif(bitmap: Bitmap, original: ByteArray): Bitmap {
        val orientation = runCatching {
            ExifInterface(original.inputStream()).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun extensionOf(mimeType: String) = when (mimeType) {
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        "image/gif" -> ".gif"
        else -> ".jpg"
    }
}
