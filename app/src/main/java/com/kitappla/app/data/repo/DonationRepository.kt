package com.kitappla.app.data.repo

import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.core.net.safeResponse
import com.kitappla.app.core.net.safeUnitCall
import com.kitappla.app.data.api.DonationApi
import com.kitappla.app.data.dto.ArrangeMeetingBody
import com.kitappla.app.data.dto.ClaimDto
import com.kitappla.app.data.dto.CreateDonationBody
import com.kitappla.app.data.dto.CreateSwapBookBody
import com.kitappla.app.data.dto.DonationDto
import com.kitappla.app.data.dto.IdStatusDto
import com.kitappla.app.data.dto.MyClaimDto
import com.kitappla.app.data.dto.MyDonationDto
import com.kitappla.app.data.dto.ThankBody
import javax.inject.Inject
import javax.inject.Singleton

const val DEFAULT_PAGE_SIZE = 24

data class PagedResult<T>(val items: List<T>, val total: Long)

/** Bağış ön bellek girdileri bu önekle tutulur; bağış durumunu değiştiren her akış hepsini birden düşürür. */
const val DONATION_CACHE_PREFIX = "donation:"
private const val DONATION_TTL_MILLIS = 60_000L

@Singleton
class DonationRepository @Inject constructor(
    private val api: DonationApi,
    private val cache: ApiCache,
) {
    /**
     * Her zaman `page` ve `size` gönderir (sunucu `page` yoksa tüm listeyi döndürür). Toplam `X-Total-Count`'tan okunur.
     * Sonuç 1 dk ön bellekte tutulur; `forceRefresh` ağa gitmeye zorlar (ağ yoksa yine eski veri gösterilir).
     */
    suspend fun page(
        page: Int,
        size: Int = DEFAULT_PAGE_SIZE,
        query: String? = null,
        level: String? = null,
        forceRefresh: Boolean = false,
    ): ApiResult<PagedResult<DonationDto>> =
        cache.cached("${DONATION_CACHE_PREFIX}list:$page:$size:${level.orEmpty()}:${query.orEmpty()}", DONATION_TTL_MILLIS, forceRefresh) {
            when (val r = safeResponse { api.donations(page, size, level, query, true) }) {
                is ApiResult.Failure -> r
                is ApiResult.Success -> {
                    val items = r.value.body().orEmpty()
                    val total = r.value.headers()["X-Total-Count"]?.toLongOrNull() ?: items.size.toLong()
                    ApiResult.Success(PagedResult(items, total))
                }
            }
        }

    suspend fun getDonation(id: Long): ApiResult<DonationDto> =
        cache.cached("${DONATION_CACHE_PREFIX}detail:$id", DONATION_TTL_MILLIS) { safeApiCall { api.donationDetail(id) } }

    suspend fun createDonation(body: CreateDonationBody): ApiResult<IdStatusDto> =
        safeApiCall { api.createDonation(body) }.invalidatingDonations()

    suspend fun claim(id: Long): ApiResult<ClaimDto> = safeApiCall { api.claim(id) }.invalidatingDonations()

    suspend fun closeDonation(id: Long): ApiResult<Unit> = safeUnitCall { api.closeDonation(id) }.invalidatingDonations()

    suspend fun reopenDonation(id: Long): ApiResult<Unit> = safeUnitCall { api.reopenDonation(id) }.invalidatingDonations()

    suspend fun deleteDonation(id: Long): ApiResult<Unit> = safeUnitCall { api.deleteDonation(id) }.invalidatingDonations()

    suspend fun moveToSwap(id: Long, body: CreateSwapBookBody? = null): ApiResult<IdStatusDto> =
        safeApiCall { api.moveToSwap(id, body) }.invalidatingDonations()

    suspend fun myDonations(): ApiResult<List<MyDonationDto>> = safeApiCall { api.myDonations() }

    suspend fun myClaims(): ApiResult<List<MyClaimDto>> = safeApiCall { api.myClaims() }

    // Talep aşamaları (gönderim, teslim, iptal…) stok/uygunluğu etkileyebildiği için bunlar da ön belleği düşürür.
    suspend fun shipClaim(id: Long): ApiResult<Unit> = safeUnitCall { api.shipClaim(id) }.invalidatingDonations()

    suspend fun arrangeMeeting(id: Long, body: ArrangeMeetingBody): ApiResult<Unit> =
        safeUnitCall { api.arrangeMeeting(id, body) }.invalidatingDonations()

    suspend fun noShowClaim(id: Long): ApiResult<Unit> = safeUnitCall { api.noShowClaim(id) }.invalidatingDonations()

    suspend fun deliverClaim(id: Long): ApiResult<Unit> = safeUnitCall { api.deliverClaim(id) }.invalidatingDonations()

    suspend fun thankClaim(id: Long, message: String?): ApiResult<Unit> =
        safeUnitCall { api.thankClaim(id, if (message != null) ThankBody(message) else null) }.invalidatingDonations()

    suspend fun cancelClaim(id: Long): ApiResult<Unit> = safeUnitCall { api.cancelClaim(id) }.invalidatingDonations()

    private fun <T> ApiResult<T>.invalidatingDonations(): ApiResult<T> {
        if (this is ApiResult.Success) cache.removePrefix(DONATION_CACHE_PREFIX)
        return this
    }
}
