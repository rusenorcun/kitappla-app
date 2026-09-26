package com.kitappla.app.data.api

import com.kitappla.app.data.dto.PickupPointDto
import retrofit2.Response
import retrofit2.http.GET

interface PickupPointApi {
    @GET("api/v1/pickup-points")
    suspend fun getActivePickupPoints(): Response<List<PickupPointDto>>
}
