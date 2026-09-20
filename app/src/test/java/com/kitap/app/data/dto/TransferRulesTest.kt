package com.kitap.app.data.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kurallar backend servisleri (DonationService / RequestService) ve web şablonlarındaki (aldiklarim.html,
 * isteklerim.html, bagislarim.html, karsiladiklarim.html) koşullarla aynıdır.
 */
class TransferRulesTest {
    private val all = listOf(
        null, "OPEN", "MATCHED", "FULFILLED", "ARRANGED", "SHIPPED", "DELIVERED", "NO_SHOW", "CANCELLED",
    )

    private fun only(expected: Set<String?>, rule: (String?) -> Boolean) {
        all.forEach { assertEquals("durum=$it", it in expected, rule(it)) }
    }

    @Test
    fun receiptCanBeConfirmedOnlyAfterAMeetingIsArrangedOrShipped() =
        only(setOf("ARRANGED", "SHIPPED")) { TransferRules.canConfirmReceipt(it) }

    @Test
    fun thanksAreOnlyPossibleAfterDelivery() = only(setOf("DELIVERED")) { TransferRules.canThank(it) }

    @Test
    fun aClaimCanBeCancelledOnlyWhileJustMatched() = only(setOf("MATCHED")) { TransferRules.canCancelClaim(it) }

    @Test
    fun onlyAnOpenRequestCanBeRemoved() = only(setOf("OPEN")) { TransferRules.canRemoveRequest(it) }

    @Test
    fun meetingsAndNoShowAreOfferedWhileTheTransferIsInProgress() =
        only(setOf("MATCHED", "FULFILLED", "ARRANGED", "SHIPPED")) { TransferRules.isInProgress(it) }

    @Test
    fun labelsUseTheBackendNames() {
        assertEquals("Karşılandı", TransferRules.label("FULFILLED", receiving = true))
        assertEquals("Buluşma Ayarlandı", TransferRules.label("ARRANGED", receiving = false))
        assertEquals("Kargoda", TransferRules.label("SHIPPED", receiving = true))
        assertEquals("Gelinmedi", TransferRules.label("NO_SHOW", receiving = true))
        assertEquals("İptal Edildi", TransferRules.label("CANCELLED", receiving = false))
    }

    @Test
    fun deliveredReadsFromTheReceiversAndTheGiversPointOfView() {
        assertEquals("Teslim Alındı", TransferRules.label("DELIVERED", receiving = true))
        assertEquals("Teslim Edildi", TransferRules.label("DELIVERED", receiving = false))
    }

    @Test
    fun startingStatesHaveNoLabelSoTheScreenShowsItsOwnDefault() {
        assertNull(TransferRules.label("OPEN", receiving = true))
        assertNull(TransferRules.label("MATCHED", receiving = true))
        assertNull(TransferRules.label(null, receiving = true))
    }

    /** Eskiden uygulamanın uydurduğu adlar backend'de yok; artık hiçbir işlem/etiket açmamalı. */
    @Test
    fun namesTheBackendNeverSendsAreIgnored() {
        listOf("TESLIM_EDILDI", "BULUSMA_AYARLANDI", "GELINMEDI", "KARGODA", "KARSILANDI", "ACIK").forEach {
            assertNull(it, TransferRules.label(it, receiving = true))
            assertFalse(it, TransferRules.canConfirmReceipt(it))
            assertFalse(it, TransferRules.canThank(it))
            assertFalse(it, TransferRules.isInProgress(it))
        }
    }

    @Test
    fun donationIsClosedOnlyWhenTheBackendSaysClosed() {
        assertTrue(isDonationClosed("CLOSED"))
        listOf("OPEN", null, "KAPALI", "DAGITILDI").forEach { assertFalse(it.toString(), isDonationClosed(it)) }
    }

    @Test
    fun constantsMatchTheBackendEnumNames() {
        assertEquals(listOf("MATCHED", "ARRANGED", "SHIPPED", "DELIVERED", "NO_SHOW", "CANCELLED"),
            listOf(ClaimStatus.MATCHED, ClaimStatus.ARRANGED, ClaimStatus.SHIPPED, ClaimStatus.DELIVERED,
                ClaimStatus.NO_SHOW, ClaimStatus.CANCELLED))
        assertEquals(listOf("OPEN", "FULFILLED", "ARRANGED", "SHIPPED", "DELIVERED", "NO_SHOW", "CANCELLED"),
            listOf(RequestStatus.OPEN, RequestStatus.FULFILLED, RequestStatus.ARRANGED, RequestStatus.SHIPPED,
                RequestStatus.DELIVERED, RequestStatus.NO_SHOW, RequestStatus.CANCELLED))
        assertEquals(listOf("PENDING", "ACCEPTED", "REJECTED", "CANCELLED", "COMPLETED"),
            listOf(OfferStatus.PENDING, OfferStatus.ACCEPTED, OfferStatus.REJECTED, OfferStatus.CANCELLED, OfferStatus.COMPLETED))
        assertEquals(listOf("OPEN", "CLOSED"), listOf(DonationStatus.OPEN, DonationStatus.CLOSED))
        assertEquals(listOf("OPEN", "CLOSED"), listOf(SwapBookStatus.OPEN, SwapBookStatus.CLOSED))
    }

    @Test
    fun offerLabelsCoverEveryBackendState() {
        assertEquals("Yanıt Bekliyor", OfferStatus.label("PENDING"))
        assertEquals("Kabul Edildi", OfferStatus.label("ACCEPTED"))
        assertEquals("Reddedildi", OfferStatus.label("REJECTED"))
        assertEquals("İptal Edildi", OfferStatus.label("CANCELLED"))
        assertEquals("Tamamlandı", OfferStatus.label("COMPLETED"))
        assertEquals("Bilinmiyor", OfferStatus.label("WAITING"))
    }
}
