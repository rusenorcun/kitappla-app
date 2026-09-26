package com.kitappla.app.data.repo

import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.core.net.safeResponse
import com.kitappla.app.core.net.safeUnitCall
import com.kitappla.app.data.api.SwapApi
import com.kitappla.app.data.dto.ArrangeMeetingBody
import com.kitappla.app.data.dto.CreateOfferBody
import com.kitappla.app.data.dto.CreateSwapBookBody
import com.kitappla.app.data.dto.IdStatusDto
import com.kitappla.app.data.dto.OfferDto
import com.kitappla.app.data.dto.SwapListingDto
import com.kitappla.app.data.dto.SwapStatusBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SwapRepository @Inject constructor(
    private val api: SwapApi,
    private val cache: ApiCache,
) {
    suspend fun discover(
        query: String? = null,
        page: Int? = null,
        size: Int = DEFAULT_PAGE_SIZE,
    ): ApiResult<PagedResult<SwapListingDto>> =
        when (val r = safeResponse { api.discover(query, page, size) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> {
                val items = r.value.body().orEmpty()
                val total = r.value.headers()["X-Total-Count"]?.toLongOrNull() ?: items.size.toLong()
                ApiResult.Success(PagedResult(items, total))
            }
        }

    suspend fun myBooks(): ApiResult<List<SwapListingDto>> = safeApiCall { api.myBooks() }

    suspend fun addSwapBook(body: CreateSwapBookBody): ApiResult<IdStatusDto> =
        safeApiCall { api.addSwapBook(body) }

    suspend fun setSwapBookStatus(id: Long, status: String): ApiResult<Unit> =
        safeUnitCall { api.setSwapBookStatus(id, SwapStatusBody(status)) }

    suspend fun removeSwapBook(id: Long): ApiResult<Unit> = safeUnitCall { api.removeSwapBook(id) }

    /** Yeni bir bağış oluşur; Keşfet'teki ön bellekteki listeler eskimiş sayılır. */
    suspend fun moveToDonation(id: Long): ApiResult<IdStatusDto> =
        safeApiCall { api.moveToDonation(id) }.also {
            if (it is ApiResult.Success) cache.removePrefix(DONATION_CACHE_PREFIX)
        }

    suspend fun incomingOffers(): ApiResult<List<OfferDto>> = safeApiCall { api.incomingOffers() }

    suspend fun outgoingOffers(): ApiResult<List<OfferDto>> = safeApiCall { api.outgoingOffers() }

    suspend fun createOffer(targetBookId: Long, offeredBookId: Long, message: String? = null): ApiResult<IdStatusDto> =
        safeApiCall { api.createOffer(CreateOfferBody(targetBookId, offeredBookId, message)) }

    suspend fun acceptOffer(id: Long): ApiResult<IdStatusDto> = safeApiCall { api.acceptOffer(id) }

    suspend fun rejectOffer(id: Long): ApiResult<Unit> = safeUnitCall { api.rejectOffer(id) }

    suspend fun cancelOffer(id: Long): ApiResult<Unit> = safeUnitCall { api.cancelOffer(id) }

    suspend fun handoverOffer(id: Long): ApiResult<Unit> = safeUnitCall { api.handoverOffer(id) }

    suspend fun arrangeMeeting(id: Long, body: ArrangeMeetingBody): ApiResult<Unit> =
        safeUnitCall { api.arrangeMeeting(id, body) }

    suspend fun noShow(id: Long): ApiResult<Unit> = safeUnitCall { api.noShowOffer(id) }
}
