package com.kitap.app.data.api

import com.kitap.app.data.dto.ArrangeMeetingBody
import com.kitap.app.data.dto.ClaimDto
import com.kitap.app.data.dto.CreateDonationBody
import com.kitap.app.data.dto.CreateSwapBookBody
import com.kitap.app.data.dto.DonationDto
import com.kitap.app.data.dto.IdStatusDto
import com.kitap.app.data.dto.MyClaimDto
import com.kitap.app.data.dto.MyDonationDto
import com.kitap.app.data.dto.ThankBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface DonationApi {
    @GET("api/v1/donations")
    suspend fun donations(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("level") level: String?,
        @Query("q") query: String?,
        @Query("available") available: Boolean,
    ): Response<List<DonationDto>>

    @GET("api/v1/donations/{id}")
    suspend fun donationDetail(@Path("id") id: Long): Response<DonationDto>

    @POST("api/v1/donations")
    suspend fun createDonation(@Body body: CreateDonationBody): Response<IdStatusDto>

    @POST("api/v1/donations/{id}/claim")
    suspend fun claim(@Path("id") id: Long): Response<ClaimDto>

    @POST("api/v1/donations/{id}/close")
    suspend fun closeDonation(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/donations/{id}/reopen")
    suspend fun reopenDonation(@Path("id") id: Long): Response<Unit>

    @DELETE("api/v1/donations/{id}")
    suspend fun deleteDonation(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/donations/{id}/to-swap")
    suspend fun moveToSwap(@Path("id") id: Long, @Body body: CreateSwapBookBody? = null): Response<IdStatusDto>

    @GET("api/v1/my/donations")
    suspend fun myDonations(): Response<List<MyDonationDto>>

    @GET("api/v1/my/claims")
    suspend fun myClaims(): Response<List<MyClaimDto>>

    @POST("api/v1/claims/{id}/ship")
    suspend fun shipClaim(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/claims/{id}/meeting")
    suspend fun arrangeMeeting(@Path("id") id: Long, @Body body: ArrangeMeetingBody): Response<Unit>

    @POST("api/v1/claims/{id}/no-show")
    suspend fun noShowClaim(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/claims/{id}/deliver")
    suspend fun deliverClaim(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/claims/{id}/thank")
    suspend fun thankClaim(@Path("id") id: Long, @Body body: ThankBody?): Response<Unit>

    @POST("api/v1/claims/{id}/cancel")
    suspend fun cancelClaim(@Path("id") id: Long): Response<Unit>
}
