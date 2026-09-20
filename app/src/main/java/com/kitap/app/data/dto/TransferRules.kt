package com.kitap.app.data.dto

// API durumları backend enum adlarıyla ham gelir (`ApiDtoMapper` → `name()`). Uygulamada bu metinleri yeniden
// uydurma; aşağıdaki sabitleri kullan. Kaynak: backend `domain/` altındaki enum'lar.

object DonationStatus {
    const val OPEN = "OPEN"
    const val CLOSED = "CLOSED"
}

/** Bağış talebi (Claim). */
object ClaimStatus {
    const val MATCHED = "MATCHED"
    const val ARRANGED = "ARRANGED"
    const val SHIPPED = "SHIPPED"
    const val DELIVERED = "DELIVERED"
    const val NO_SHOW = "NO_SHOW"
    const val CANCELLED = "CANCELLED"
}

/** Kitap isteği (Request); aşamaları talebinkilerle aynı adları paylaşır. */
object RequestStatus {
    const val OPEN = "OPEN"
    const val FULFILLED = "FULFILLED"
    const val ARRANGED = "ARRANGED"
    const val SHIPPED = "SHIPPED"
    const val DELIVERED = "DELIVERED"
    const val NO_SHOW = "NO_SHOW"
    const val CANCELLED = "CANCELLED"
}

object SwapBookStatus {
    const val OPEN = "OPEN"
    const val CLOSED = "CLOSED"
}

object OfferStatus {
    const val PENDING = "PENDING"
    const val ACCEPTED = "ACCEPTED"
    const val REJECTED = "REJECTED"
    const val CANCELLED = "CANCELLED"
    const val COMPLETED = "COMPLETED"

    fun label(status: String?): String = when (status) {
        PENDING -> "Yanıt Bekliyor"
        ACCEPTED -> "Kabul Edildi"
        REJECTED -> "Reddedildi"
        CANCELLED -> "İptal Edildi"
        COMPLETED -> "Tamamlandı"
        else -> "Bilinmiyor"
    }
}

fun isDonationClosed(status: String?): Boolean = status == DonationStatus.CLOSED

/**
 * Kitap devri (talep ya da istek) aşamalarına göre hangi işlemin sunulacağı. Kurallar backend servislerinin ve web
 * şablonlarının koşullarıyla aynıdır: teslim aldım/teşekkür/iptal yalnızca **alan** tarafa açıktır (backend
 * `claimOfReceiver` / `ownRequest`); buluşma ve "gelmedi" iki tarafa da açıktır.
 */
object TransferRules {
    /** Alan taraf teslim aldığını, buluşma ayarlandıktan (ya da kargolandıktan) sonra onaylar. */
    fun canConfirmReceipt(status: String?): Boolean =
        status == ClaimStatus.ARRANGED || status == ClaimStatus.SHIPPED

    /** Teşekkür notu yalnızca teslim edilmiş kitap için gönderilir. */
    fun canThank(status: String?): Boolean = status == ClaimStatus.DELIVERED

    /** Talep yalnızca henüz ilerlememişken (MATCHED) iptal edilebilir. */
    fun canCancelClaim(status: String?): Boolean = status == ClaimStatus.MATCHED

    /** Yalnızca karşılanmamış (OPEN) istek kaldırılabilir. */
    fun canRemoveRequest(status: String?): Boolean = status == RequestStatus.OPEN

    /** Buluşma ayarlama ve "gelmedi" bildirimi: süreç başlamış ve bitmemişken. */
    fun isInProgress(status: String?): Boolean = when (status) {
        ClaimStatus.MATCHED, RequestStatus.FULFILLED, ClaimStatus.ARRANGED, ClaimStatus.SHIPPED -> true
        else -> false
    }

    /**
     * Rozet metni; başlangıç durumları (OPEN/MATCHED) için `null` döner ve ekran kendi varsayılanını gösterir.
     * [receiving]: kitabı alan tarafın gözünden mi (Teslim Alındı) verenin gözünden mi (Teslim Edildi).
     */
    fun label(status: String?, receiving: Boolean): String? = when (status) {
        RequestStatus.FULFILLED -> "Karşılandı"
        ClaimStatus.ARRANGED -> "Buluşma Ayarlandı"
        ClaimStatus.SHIPPED -> "Kargoda"
        ClaimStatus.DELIVERED -> if (receiving) "Teslim Alındı" else "Teslim Edildi"
        ClaimStatus.NO_SHOW -> "Gelinmedi"
        ClaimStatus.CANCELLED -> "İptal Edildi"
        else -> null
    }
}
