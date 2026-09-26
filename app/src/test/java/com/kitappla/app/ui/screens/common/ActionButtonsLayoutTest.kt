package com.kitappla.app.ui.screens.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionButtonsLayoutTest {

    @Test
    fun buttonsThatDoNotFitMoveToTheNextLine() {
        // 300 px satır, 8 px ara: 100 + 120 sığar, 90 sığmaz.
        assertEquals(listOf(listOf(0, 1), listOf(2)), packLines(listOf(100, 120, 90), gap = 8, maxWidth = 300))
    }

    @Test
    fun everythingOnOneLineWhenItFits() {
        assertEquals(listOf(listOf(0, 1, 2)), packLines(listOf(80, 80, 80), gap = 8, maxWidth = 300))
    }

    @Test
    fun equalWeightsShareTheLineEvenly() {
        assertEquals(listOf(150, 150), shareLine(listOf(60, 90), listOf(1f, 1f), width = 300))
    }

    @Test
    fun aLabelNeverGetsLessThanItsFullWidth() {
        // Eski düzende "Bağışa Taşı" payı (1/2.7 × 300 ≈ 111) etiketinden (150) darlaşıp bölünüyordu.
        val widths = shareLine(listOf(80, 150, 50), listOf(1f, 1f, 0.7f), width = 300)
        assertEquals(150, widths[1])
        assertTrue(widths[0] >= 80 && widths[2] >= 50)
        assertTrue(widths.sum() <= 300)
    }

    @Test
    fun unweightedButtonsKeepTheirLabelWidth() {
        assertEquals(listOf(200, 60), shareLine(listOf(120, 60), listOf(1f, 0f), width = 260))
    }
}
