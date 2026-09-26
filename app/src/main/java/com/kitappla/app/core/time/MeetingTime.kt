package com.kitappla.app.core.time

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Buluşma zamanı dönüşümleri. Sunucu anı ISO-8601 UTC olarak alır ve verir (`2026-10-15T11:30:00Z`; backend
 * `Instant.parse` / `Instant.toString`); kullanıcı ise yerel saatle seçer ve görür. minSdk 24'te `java.time`
 * olmadığından `java.util` kullanılır.
 */
object MeetingTime {
    private val TR = Locale("tr", "TR")
    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

    private fun isoFormat() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = UTC }

    /** `…Z` biçimindeki anı çözer; kesirli saniye (`.123456Z`) varsa atılır. Çözülemezse `null`. */
    fun parse(iso: String?): Date? {
        val text = iso?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val normalized = text.replace(Regex("\\.\\d+Z$"), "Z")
        return try {
            isoFormat().apply { isLenient = false }.parse(normalized)
        } catch (e: ParseException) {
            null
        }
    }

    /** Sunucuya gönderilecek biçim: saniyesiz yerel seçim → UTC `yyyy-MM-ddTHH:mm:00Z`. */
    fun toIso(date: Date): String = isoFormat().format(date)

    /** Ekranda: "15 Eki 2026, 14:30" (cihazın saat diliminde). Çözülemeyen değer olduğu gibi gösterilir. */
    fun display(iso: String?, zone: TimeZone = TimeZone.getDefault()): String? {
        val date = parse(iso) ?: return iso?.takeIf { it.isNotBlank() }
        return SimpleDateFormat("d MMM yyyy, HH:mm", TR).apply { timeZone = zone }.format(date)
    }

    fun displayDate(utcDayMillis: Long): String =
        SimpleDateFormat("d MMMM yyyy", TR).apply { timeZone = UTC }.format(Date(utcDayMillis))

    /**
     * Tarih seçicinin günü (UTC gece yarısı, Material DatePicker böyle verir) + yerel saat/dakika → an.
     */
    fun combine(utcDayMillis: Long, hour: Int, minute: Int, zone: TimeZone = TimeZone.getDefault()): Date {
        val day = Calendar.getInstance(UTC).apply { timeInMillis = utcDayMillis }
        return Calendar.getInstance(zone).apply {
            clear()
            set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
        }.time
    }

    /** Bir anın yerel gününü tarih seçicinin beklediği UTC gece yarısına, saatini de (saat, dakika) çiftine ayırır. */
    fun split(date: Date, zone: TimeZone = TimeZone.getDefault()): Triple<Long, Int, Int> {
        val local = Calendar.getInstance(zone).apply { time = date }
        val day = Calendar.getInstance(UTC).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }
        return Triple(day.timeInMillis, local.get(Calendar.HOUR_OF_DAY), local.get(Calendar.MINUTE))
    }
}
