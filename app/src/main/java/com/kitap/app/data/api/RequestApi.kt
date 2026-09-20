package com.kitap.app.data.api

import com.kitap.app.data.dto.ArrangeMeetingBody
import com.kitap.app.data.dto.CreateRequestBody
import com.kitap.app.data.dto.FulfillBody
import com.kitap.app.data.dto.IdStatusDto
import com.kitap.app.data.dto.RequestDto
import com.kitap.app.data.dto.ThankBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RequestApi {
    @GET("api/v1/requests/open")
    suspend fun openRequests(
        @Query("q") query: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int = 24,
    ): Response<List<RequestDto>>

    @GET("api/v1/my/requests")
    suspend fun myRequests(): Response<List<RequestDto>>

    @GET("api/v1/my/fulfilled")
    suspend fun fulfilledByMe(): Response<List<RequestDto>>

    @POST("api/v1/requests")
    suspend fun createRequest(@Body body: CreateRequestBody): Response<IdStatusDto>

    @POST("api/v1/requests/{id}/fulfill")
    suspend fun fulfillRequest(@Path("id") id: Long, @Body body: FulfillBody? = null): Response<IdStatusDto>

    @POST("api/v1/requests/{id}/ship")
    suspend fun shipRequest(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/requests/{id}/meeting")
    suspend fun arrangeMeeting(@Path("id") id: Long, @Body body: ArrangeMeetingBody): Response<Unit>

    @POST("api/v1/requests/{id}/no-show")
    suspend fun noShowRequest(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/requests/{id}/deliver")
    suspend fun deliverRequest(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/requests/{id}/thank")
    suspend fun thankRequest(@Path("id") id: Long, @Body body: ThankBody?): Response<Unit>

    @DELETE("api/v1/requests/{id}")
    suspend fun deleteRequest(@Path("id") id: Long): Response<Unit>
}
