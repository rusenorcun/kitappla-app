package com.kitappla.app.data.repo

import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.data.api.PickupPointApi
import com.kitappla.app.data.dto.PickupPointDto
import javax.inject.Inject
import javax.inject.Singleton

private const val PICKUP_POINTS_TTL_MILLIS = 10 * 60_000L

@Singleton
class PickupPointRepository @Inject constructor(
    private val api: PickupPointApi,
    private val cache: ApiCache,
) {
    /** Teslim noktaları nadiren değişir ve birçok ekranda kullanılır; 10 dk ön bellekte tutulur. */
    suspend fun getActivePoints(): ApiResult<List<PickupPointDto>> =
        cache.cached("pickup-points:active", PICKUP_POINTS_TTL_MILLIS) { safeApiCall { api.getActivePickupPoints() } }
}
