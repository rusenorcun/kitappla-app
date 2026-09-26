package com.kitappla.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.kitappla.app.ui.screens.auth.AuthFormState
import com.kitappla.app.ui.screens.auth.HesapDogrulamaContent
import com.kitappla.app.ui.screens.auth.HesapDogrulamaState
import com.kitappla.app.ui.screens.auth.HesapDogrulamaView
import com.kitappla.app.ui.screens.auth.SifreSifirlaScreen
import com.kitappla.app.ui.screens.auth.SifremiUnuttumScreen
import androidx.compose.runtime.mutableStateOf
import com.kitappla.app.ui.theme.KitapplaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** E-posta bağlantısından sonra açılan ekranların hâlleri ve düğmeleri. */
class EmailFlowScreensTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun shown(text: String) = rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    private fun assertShown(vararg texts: String) = texts.forEach { assertTrue("'$it' görünmeli", shown(it)) }
    private fun assertHidden(vararg texts: String) = texts.forEach { assertFalse("'$it' görünmemeli", shown(it)) }

    private val clicks = mutableListOf<String>()

    private fun verification(state: HesapDogrulamaState) = rule.setContent {
        KitapplaTheme {
            Surface {
                HesapDogrulamaContent(
                    state = state,
                    onBack = { clicks += "geri" },
                    onOpenMail = { clicks += "posta" },
                    onCheck = { clicks += "kontrol" },
                    onResend = { clicks += "yeniden" },
                    onContinue = { clicks += "kesfet" },
                    onOpenDashboard = { clicks += "panom" },
                    onLogin = { clicks += "giris" },
                    onLoginThenVerify = { clicks += "giris-dogrula" },
                )
            }
        }
    }

    @Test
    fun pendingViewGuidesToTheInboxAndOffersResend() {
        verification(HesapDogrulamaState(view = HesapDogrulamaView.PENDING, email = "ayse@ornek.com"))
        assertShown("E-postanı doğrula", "E-posta uygulamasını aç", "Doğruladım, kontrol et")
        rule.onNodeWithText("E-posta uygulamasını aç").performClick()
        rule.onNodeWithText("Doğruladım, kontrol et").performClick()
        rule.onNodeWithText("Doğrulama e-postasını yeniden gönder").performScrollTo().performClick()
        assertEquals(listOf("posta", "kontrol", "yeniden"), clicks)
    }

    @Test
    fun resendIsDisabledDuringCooldown() {
        verification(HesapDogrulamaState(view = HesapDogrulamaView.PENDING, email = "a@b.c", resendCooldown = 12))
        assertShown("Yeniden gönder (12 sn)")
        rule.onNodeWithText("Yeniden gönder (12 sn)").performScrollTo().performClick()
        assertTrue("bekleme süresinde istek atılmaz", clicks.isEmpty())
    }

    @Test
    fun verifiedViewCelebratesAndLeadsToExplore() {
        verification(HesapDogrulamaState(view = HesapDogrulamaView.VERIFIED, email = "a@b.c", justVerified = true))
        assertShown("E-postan doğrulandı", "Bağış yap", "Kitap iste", "Takas et", "Mesajlaş")
        assertHidden("Hesabın zaten doğrulanmış")
        rule.onNodeWithText("Keşfetmeye başla").performScrollTo().performClick()
        assertEquals(listOf("kesfet"), clicks)
    }

    @Test
    fun invalidLinkOffersANewOne() {
        verification(HesapDogrulamaState(view = HesapDogrulamaView.INVALID_LINK, email = "a@b.c"))
        assertShown("Bu bağlantı artık çalışmıyor")
        rule.onNodeWithText("Yeni bağlantı gönder").performClick()
        assertEquals(listOf("yeniden"), clicks)
    }

    @Test
    fun guestsAreSentToLogin() {
        verification(HesapDogrulamaState(view = HesapDogrulamaView.GUEST_INVALID))
        rule.onNodeWithText("Giriş yap ve yeni bağlantı iste").performClick()
        assertEquals(listOf("giris-dogrula"), clicks)
    }

    private fun reset(token: String?) = rule.setContent {
        KitapplaTheme {
            Surface {
                SifreSifirlaScreen(
                    form = AuthFormState(),
                    initialToken = token,
                    onSubmit = { t, _, _, onSuccess -> clicks += "gonder:$t"; onSuccess() },
                    onDone = { clicks += "giris" },
                    onRequestNewLink = {},
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun resetFromTheLinkHidesTheCodeFieldAndShowsSuccessAfterward() {
        reset("jeton123")
        assertHidden("Bağlantı veya sıfırlama kodu")
        rule.onNodeWithText("Şifreyi güncelle").performScrollTo().performClick()
        assertEquals("gonder:jeton123", clicks.first())
        assertShown("Şifren güncellendi")
        rule.onNodeWithText("Giriş yap").performClick()
        assertEquals("giris", clicks.last())
    }

    /** Hâl değişimi (erken `return` içeren inline lambda'lar bu geçişte Compose'u çökertiyordu). */
    @Test
    fun checkingSwitchesToPendingWithoutCrashing() {
        val state = mutableStateOf(HesapDogrulamaState())
        rule.setContent {
            KitapplaTheme {
                Surface {
                    HesapDogrulamaContent(
                        state = state.value, onBack = {}, onOpenMail = {}, onCheck = {}, onResend = {},
                        onContinue = {}, onOpenDashboard = {}, onLogin = {}, onLoginThenVerify = {},
                    )
                }
            }
        }
        assertShown("Hesabın kontrol ediliyor…")
        rule.runOnIdle { state.value = HesapDogrulamaState(view = HesapDogrulamaView.PENDING, email = "a@b.c") }
        assertShown("E-postanı doğrula")
        rule.runOnIdle { state.value = state.value.copy(view = HesapDogrulamaView.VERIFIED, justVerified = true) }
        assertShown("E-postan doğrulandı")
    }

    @Test
    fun forgotPasswordSwitchesToTheInboxViewAfterSending() {
        rule.setContent {
            KitapplaTheme {
                Surface {
                    SifremiUnuttumScreen(
                        form = AuthFormState(),
                        onSubmit = { _, onSent -> onSent("OK") },
                        onNavigateToReset = {},
                        onBack = {},
                    )
                }
            }
        }
        rule.onNodeWithText("Sıfırlama bağlantısı gönder").performScrollTo().performClick()
        assertShown("Gelen kutunu kontrol et", "Bağlantı yerine kodu gir")
        rule.onNodeWithText("Farklı bir adres dene ya da yeniden gönder").performScrollTo().performClick()
        assertShown("Şifreni mi unuttun?")
    }

    @Test
    fun manualResetAsksForTheLinkOrCode() {
        reset(null)
        assertShown("Bağlantı veya sıfırlama kodu")
    }
}
