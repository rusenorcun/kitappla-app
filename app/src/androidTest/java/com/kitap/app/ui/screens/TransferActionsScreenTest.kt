package com.kitap.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kitap.app.data.dto.BookDto
import com.kitap.app.data.dto.ClaimDto
import com.kitap.app.data.dto.ClaimStatus
import com.kitap.app.data.dto.DonationStatus
import com.kitap.app.data.dto.MyClaimDto
import com.kitap.app.data.dto.MyDonationDto
import com.kitap.app.data.dto.RequestDto
import com.kitap.app.data.dto.RequestStatus
import com.kitap.app.ui.screens.donation.AldiklarimContent
import com.kitap.app.ui.screens.donation.AldiklarimState
import com.kitap.app.ui.screens.donation.BagislarimContent
import com.kitap.app.ui.screens.donation.BagislarimState
import com.kitap.app.ui.screens.request.IsteklerimContent
import com.kitap.app.ui.screens.request.IsteklerimState
import com.kitap.app.ui.screens.request.KarsiladiklarimContent
import com.kitap.app.ui.screens.request.KarsiladiklarimState
import com.kitap.app.ui.theme.KitapTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Backend enum adlarıyla gelen durumların ekranlarda doğru rozet ve butonlara dönüştüğünü doğrular
 * (eskiden uydurma Türkçe adlar karşılaştırıldığı için hiçbiri eşleşmiyordu).
 */
class TransferActionsScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun shown(text: String) = rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    private fun assertShown(vararg texts: String) = texts.forEach { assertTrue("'$it' görünmeli", shown(it)) }
    private fun assertHidden(vararg texts: String) = texts.forEach { assertFalse("'$it' görünmemeli", shown(it)) }

    private val book = BookDto(title = "Kitap", author = "Yazar")

    // ---------- Aldıklarım: kitabı alan taraf ----------
    private fun aldiklarim(status: String, onDeliver: (Long) -> Unit = {}) = rule.setContent {
        KitapTheme {
            Surface {
                AldiklarimContent(
                    state = AldiklarimState(
                        loading = false,
                        items = listOf(MyClaimDto(id = 7L, status = status, book = book, donorName = "Ali")),
                    ),
                    onBack = {}, onExplore = {}, onDeliver = onDeliver, onThank = { _, _ -> },
                    onCancel = {}, onChat = {}, onRetry = {},
                )
            }
        }
    }

    @Test
    fun receiverOfAMatchedClaimCanOnlyCancel() {
        aldiklarim(ClaimStatus.MATCHED)
        assertShown("Bekliyor", "İptal")
        assertHidden("Teslim Aldım", "Teşekkür Et")
    }

    @Test
    fun receiverOfAnArrangedClaimCanConfirmReceipt() {
        aldiklarim(ClaimStatus.ARRANGED)
        assertShown("Buluşma Ayarlandı", "Teslim Aldım")
        assertHidden("Teşekkür Et", "İptal")
    }

    @Test
    fun confirmingReceiptReportsTheClaimId() {
        var delivered: Long? = null
        aldiklarim(ClaimStatus.SHIPPED) { delivered = it }
        rule.onNodeWithText("Teslim Aldım").performClick()
        assertEquals(7L, delivered)
    }

    @Test
    fun receiverOfADeliveredClaimCanThank() {
        aldiklarim(ClaimStatus.DELIVERED)
        assertShown("Teslim Alındı", "Teşekkür Et")
        assertHidden("Teslim Aldım", "İptal")
    }

    @Test
    fun aCancelledClaimOffersNoActions() {
        aldiklarim(ClaimStatus.CANCELLED)
        assertShown("İptal Edildi")
        assertHidden("Teslim Aldım", "Teşekkür Et", "İptal")
    }

    // ---------- Bağışlarım: bağışçı ----------
    private fun bagislarim(donation: String, claim: String) = rule.setContent {
        KitapTheme {
            Surface {
                BagislarimContent(
                    state = BagislarimState(
                        loading = false,
                        items = listOf(
                            MyDonationDto(
                                id = 1L, book = book, quantity = 1, claimed = 1, remaining = 0, status = donation,
                                claims = listOf(ClaimDto(id = 10L, status = claim, requesterName = "Zeynep")),
                            )
                        ),
                    ),
                    onBack = {}, onNewDonation = {}, onArrangeMeeting = { _, _, _, _ -> }, onNoShowClaim = {},
                    onCloseDonation = {}, onReopenDonation = {}, onDeleteDonation = {}, onChat = {}, onRetry = {},
                )
            }
        }
    }

    @Test
    fun donorSeesMeetingAndNoShowButNeverADeliverButton() {
        bagislarim(DonationStatus.OPEN, ClaimStatus.ARRANGED)
        assertShown("Buluşma Ayarlandı", "Buluşma", "Gelmedi", "Kapat")
        assertHidden("Teslim", "Teslim Aldım", "Yeniden Aç")
    }

    @Test
    fun aClosedDonationIsShownClosedAndCanBeReopened() {
        bagislarim(DonationStatus.CLOSED, ClaimStatus.DELIVERED)
        assertShown("Kapandı", "Teslim Edildi", "Yeniden Aç")
        assertHidden("Kapat", "Buluşma", "Gelmedi")
    }

    // ---------- İsteklerim: isteyen (kitabı alan) ----------
    private fun isteklerim(status: String) = rule.setContent {
        KitapTheme {
            Surface {
                IsteklerimContent(
                    state = IsteklerimState(
                        loading = false,
                        items = listOf(RequestDto(id = 3L, book = book, status = status, fulfilledByName = "Kemal")),
                    ),
                    onBack = {}, onNewRequest = {}, onArrangeMeeting = { _, _, _, _ -> }, onDeliver = {},
                    onThank = { _, _ -> }, onDelete = {}, onChat = {}, onRetry = {},
                )
            }
        }
    }

    @Test
    fun anOpenRequestCanBeRemovedButNothingElse() {
        isteklerim(RequestStatus.OPEN)
        assertShown("Açık", "Sil")
        assertHidden("Buluşma", "Teslim Aldım", "Teşekkür Et")
    }

    @Test
    fun aFulfilledRequestCanArrangeAMeetingButNotBeRemoved() {
        isteklerim(RequestStatus.FULFILLED)
        assertShown("Karşılandı", "Buluşma")
        assertHidden("Sil", "Teslim Aldım", "Teşekkür Et")
    }

    @Test
    fun anArrangedRequestCanConfirmReceipt() {
        isteklerim(RequestStatus.ARRANGED)
        assertShown("Buluşma Ayarlandı", "Teslim Aldım")
        assertHidden("Sil", "Teşekkür Et")
    }

    @Test
    fun aDeliveredRequestCanThank() {
        isteklerim(RequestStatus.DELIVERED)
        assertShown("Teslim Alındı", "Teşekkür Et")
        assertHidden("Sil", "Teslim Aldım", "Buluşma")
    }

    // ---------- Karşıladıklarım: karşılayan (veren) ----------
    private fun karsiladiklarim(status: String) = rule.setContent {
        KitapTheme {
            Surface {
                KarsiladiklarimContent(
                    state = KarsiladiklarimState(
                        loading = false,
                        items = listOf(RequestDto(id = 4L, book = book, status = status, requesterName = "Canan")),
                    ),
                    onBack = {}, onBrowseRequests = {}, onArrangeMeeting = { _, _, _, _ -> },
                    onNoShow = {}, onChat = {}, onRetry = {},
                )
            }
        }
    }

    @Test
    fun fulfillerOfAnArrangedRequestSeesMeetingAndNoShowNeverADeliverButton() {
        karsiladiklarim(RequestStatus.ARRANGED)
        assertShown("Buluşma Ayarlandı", "Buluşma", "Gelmedi")
        assertHidden("Teslim Ettim")
    }

    @Test
    fun aDeliveredRequestOffersTheFulfillerNoActions() {
        karsiladiklarim(RequestStatus.DELIVERED)
        assertShown("Teslim Edildi")
        assertHidden("Buluşma", "Gelmedi", "Teslim Ettim")
    }
}
