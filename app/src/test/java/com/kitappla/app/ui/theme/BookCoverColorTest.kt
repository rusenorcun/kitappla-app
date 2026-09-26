package com.kitappla.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookCoverColorTest {

    @Test
    fun getBookCoverColorIsDeterministic() {
        val c1 = getBookCoverColor("Suç ve Ceza")
        val c2 = getBookCoverColor("Suç ve Ceza")
        assertEquals(c1, c2)
    }

    @Test
    fun getBookCoverColorReturnsPaletteColor() {
        val testTitles = listOf(
            "Suç ve Ceza",
            "Kürk Mantolu Madonna",
            "Beyaz Diş",
            "1984",
            "Simyacı",
            "",
            null,
        )

        for (title in testTitles) {
            val color = getBookCoverColor(title)
            assertTrue("Renk palet içinde bulunmalı: $color", KapakPaleti.contains(color))
        }
    }

    @Test
    fun getBookCoverColorMatchesJavaFloorMod() {
        // Java Book.getCoverColor() eşleştirmesi:
        // key = "" -> h = 0 -> floorMod(0, 8) = 0 -> #7A2E2A
        assertEquals(Color(0xFF7A2E2A), getBookCoverColor(""))
        assertEquals(Color(0xFF7A2E2A), getBookCoverColor(null))
    }
}
