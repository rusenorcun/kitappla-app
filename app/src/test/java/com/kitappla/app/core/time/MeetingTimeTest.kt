package com.kitappla.app.core.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.TimeZone

/** Sunucu `Instant.parse` yalnızca `…Z` biçimini kabul eder; kullanıcı ise İstanbul saatini görür/seçer. */
class MeetingTimeTest {
    private val istanbul = TimeZone.getTimeZone("Europe/Istanbul")

    @Test
    fun localSelectionIsSentAsUtcInstant() {
        val day = MeetingTime.split(MeetingTime.parse("2026-10-15T00:00:00Z")!!, TimeZone.getTimeZone("UTC")).first
        val at = MeetingTime.combine(day, hour = 14, minute = 30, zone = istanbul)
        assertEquals("2026-10-15T11:30:00Z", MeetingTime.toIso(at))
    }

    @Test
    fun serverInstantIsShownInLocalTime() {
        assertEquals("15 Eki 2026, 14:30", MeetingTime.display("2026-10-15T11:30:00Z", istanbul))
        assertEquals("15 Eki 2026, 14:30", MeetingTime.display("2026-10-15T11:30:00.123456Z", istanbul))
    }

    @Test
    fun splitRoundTripsThroughThePickers() {
        val at = MeetingTime.parse("2026-10-15T21:45:00Z")!!   // İstanbul'da 16 Ekim 00:45
        val (day, hour, minute) = MeetingTime.split(at, istanbul)
        assertEquals(0, hour)
        assertEquals(45, minute)
        assertEquals("16 Ekim 2026", MeetingTime.displayDate(day))
        assertEquals("2026-10-15T21:45:00Z", MeetingTime.toIso(MeetingTime.combine(day, hour, minute, istanbul)))
    }

    @Test
    fun unparsableValuesDoNotCrash() {
        assertNull(MeetingTime.parse("15.10.2026 14:30"))
        assertNull(MeetingTime.parse(null))
        assertEquals("yarın öğlen", MeetingTime.display("yarın öğlen", istanbul))
        assertNull(MeetingTime.display("", istanbul))
    }
}
