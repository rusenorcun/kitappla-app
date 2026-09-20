package com.kitap.app.ui.screens.profile

import org.junit.Assert.assertEquals
import org.junit.Test

/** E-postadaki bağlantının tamamı yapıştırılsa da yalnızca `token=` değeri sunucuya gitmeli. */
class VerificationTokenTest {
    private val token = "Ab3_x-9QzLmN0pRsTuVwXyZ1a2B3c4D5e6F7g8H9iJk"

    @Test
    fun aPlainCodeIsKeptAsIs() {
        assertEquals(token, extractVerificationToken(token))
    }

    @Test
    fun surroundingWhitespaceIsTrimmed() {
        assertEquals(token, extractVerificationToken("  $token \n"))
    }

    @Test
    fun theTokenIsTakenFromTheEmailLink() {
        assertEquals(token, extractVerificationToken("https://www.kitappla.com/profil/ogrenci/eposta/onay?token=$token"))
    }

    @Test
    fun otherQueryParametersAndFragmentsDoNotLeakIn() {
        assertEquals(token, extractVerificationToken("https://www.kitappla.com/x?a=1&token=$token&b=2#bolum"))
        assertEquals(token, extractVerificationToken("https://www.kitappla.com/x?token=$token#bolum"))
    }

    @Test
    fun aLinkPastedTogetherWithOtherTextStillWorks() {
        assertEquals(token, extractVerificationToken("Okul e-postamı doğrula:\nhttp://kitappla.com/onay?token=$token\nTeşekkürler"))
    }

    @Test
    fun theParameterNameIsCaseInsensitive() {
        assertEquals(token, extractVerificationToken("https://www.kitappla.com/x?TOKEN=$token"))
    }

    @Test
    fun aLinkWithoutATokenIsPassedThroughSoTheServerCanRejectIt() {
        assertEquals("https://www.kitappla.com/profil", extractVerificationToken(" https://www.kitappla.com/profil "))
    }

    @Test
    fun blankStaysBlank() {
        assertEquals("", extractVerificationToken("   "))
    }
}
