package com.kitappla.app.ui.screens.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoverTitleTest {
    @Test
    fun breakBetweenWordsIsFine() {
        // "Kürk Mantolu" → "Kürk " | "Mantolu"
        assertFalse(breaksInsideWord("Kürk Mantolu", listOf(5)))
    }

    @Test
    fun breakInsideAWordIsDetected() {
        // "Simyacı" → "Simy" | "acı"
        assertTrue(breaksInsideWord("Simyacı", listOf(4)))
        assertTrue(breaksInsideWord("Kürk Mantolu Madonna", listOf(5, 8)))
    }

    @Test
    fun singleLineAndHyphenBreaksAreFine() {
        assertFalse(breaksInsideWord("Simyacı", emptyList()))
        assertFalse(breaksInsideWord("Bilim-kurgu", listOf(6)))
    }
}
