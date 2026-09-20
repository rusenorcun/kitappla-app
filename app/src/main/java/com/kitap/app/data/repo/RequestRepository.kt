package com.kitap.app.data.repo

import com.kitap.app.core.cache.ApiCache
import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeApiCall
import com.kitap.app.core.net.safeResponse
import com.kitap.app.core.net.safeUnitCall
import com.kitap.app.data.api.RequestApi
import com.kitap.app.data.dto.ArrangeMeetingBody
import com.kitap.app.data.dto.CreateRequestBody
import com.kitap.app.data.dto.FulfillBody
import com.kitap.app.data.dto.IdStatusDto
import com.kitap.app.data.dto.RequestDto
import com.kitap.app.data.dto.ThankBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestRepository @Inject constructor(
    private val api: RequestApi,
    private val cache: ApiCache,
) {
    suspend fun openRequests(
        query: String? = null,
        page: Int? = null,
        size: Int = DEFAULT_PAGE_SIZE,
    ): ApiResult<PagedResult<RequestDto>> =
        when (val r = safeResponse { api.openRequests(query, page, size) }) {
            is ApiResult.Failure -> r
            is ApiResult.Success -> {
                val items = r.value.body().orEmpty()
                val total = r.value.headers()["X-Total-Count"]?.toLongOrNull() ?: items.size.toLong()
                ApiResult.Success(PagedResult(items, total))
            }
        }

    suspend fun myRequests(): ApiResult<List<RequestDto>> = safeApiCall { api.myRequests() }

    suspend fun fulfilledByMe(): ApiResult<List<RequestDto>> = safeApiCall { api.fulfilledByMe() }

    suspend fun createRequest(body: CreateRequestBody): ApiResult<IdStatusDto> =
        safeApiCall { api.createRequest(body) }

    /** İsteği karşılamak bağış/talep kaydı doğurabilir; Keşfet'teki ön bellek eskimiş sayılır. */
    suspend fun fulfillRequest(id: Long, source: String = "OWN"): ApiResult<IdStatusDto> =
        safeApiCall { api.fulfillRequest(id, FulfillBody(source)) }.also {
            if (it is ApiResult.Success) cache.removePrefix(DONATION_CACHE_PREFIX)
        }

    suspend fun shipRequest(id: Long): ApiResult<Unit> = safeUnitCall { api.shipRequest(id) }

    suspend fun arrangeMeeting(id: Long, body: ArrangeMeetingBody): ApiResult<Unit> =
        safeUnitCall { api.arrangeMeeting(id, body) }

    suspend fun noShow(id: Long): ApiResult<Unit> = safeUnitCall { api.noShowRequest(id) }

    suspend fun deliver(id: Long): ApiResult<Unit> = safeUnitCall { api.deliverRequest(id) }

    suspend fun thank(id: Long, message: String?): ApiResult<Unit> =
        safeUnitCall { api.thankRequest(id, if (message != null) ThankBody(message) else null) }

    suspend fun delete(id: Long): ApiResult<Unit> = safeUnitCall { api.deleteRequest(id) }
}
