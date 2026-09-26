package com.kitappla.app.core.net

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Backend `EpostaDogrulamaFiltresi` reddinin gövdesi: `{"error":"...","code":"EMAIL_NOT_VERIFIED"}`. */
class EmailVerificationNoticeTest {
    @Test
    fun recognisesTheEmailNotVerifiedRejection() {
        assertTrue(isEmailNotVerified("""{"error":"Bu işlem için önce e-posta adresini doğrulamalısın.","code":"EMAIL_NOT_VERIFIED"}"""))
    }

    @Test
    fun otherForbiddenBodiesAreIgnored() {
        assertFalse(isEmailNotVerified("""{"error":"Bu işlem için yetkiniz bulunmuyor."}"""))
        assertFalse(isEmailNotVerified("""{"error":"x","code":"OTHER"}"""))
        assertFalse(isEmailNotVerified("<html>403</html>"))
        assertFalse(isEmailNotVerified(""))
        assertFalse(isEmailNotVerified(null))
    }
}
