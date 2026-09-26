package com.kitappla.app.data.api

import com.kitappla.app.data.dto.ArrangeMeetingBody
import com.kitappla.app.data.dto.CreateOfferBody
import com.kitappla.app.data.dto.CreateSwapBookBody
import com.kitappla.app.data.dto.IdStatusDto
import com.kitappla.app.data.dto.OfferDto
import com.kitappla.app.data.dto.SwapListingDto
import com.kitappla.app.data.dto.SwapStatusBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SwapApi {
    @GET("api/v1/swap/discover")
    suspend fun discover(
        @Query("q") query: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int = 24,
    ): Response<List<SwapListingDto>>

    @GET("api/v1/swap/my-books")
    suspend fun myBooks(): Response<List<SwapListingDto>>

    @POST("api/v1/swap/books")
    suspend fun addSwapBook(@Body body: CreateSwapBookBody): Response<IdStatusDto>

    @POST("api/v1/swap/books/{id}/status")
    suspend fun setSwapBookStatus(@Path("id") id: Long, @Body body: SwapStatusBody): Response<Unit>

    @DELETE("api/v1/swap/books/{id}")
    suspend fun removeSwapBook(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/swap/books/{id}/to-donation")
    suspend fun moveToDonation(@Path("id") id: Long): Response<IdStatusDto>

    @GET("api/v1/swaps/incoming")
    suspend fun incomingOffers(): Response<List<OfferDto>>

    @GET("api/v1/swaps/outgoing")
    suspend fun outgoingOffers(): Response<List<OfferDto>>

    @POST("api/v1/swaps")
    suspend fun createOffer(@Body body: CreateOfferBody): Response<IdStatusDto>

    @POST("api/v1/swaps/{id}/accept")
    suspend fun acceptOffer(@Path("id") id: Long): Response<IdStatusDto>

    @POST("api/v1/swaps/{id}/reject")
    suspend fun rejectOffer(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/swaps/{id}/cancel")
    suspend fun cancelOffer(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/swaps/{id}/handover")
    suspend fun handoverOffer(@Path("id") id: Long): Response<Unit>

    @POST("api/v1/swaps/{id}/meeting")
    suspend fun arrangeMeeting(@Path("id") id: Long, @Body body: ArrangeMeetingBody): Response<Unit>

    @POST("api/v1/swaps/{id}/no-show")
    suspend fun noShowOffer(@Path("id") id: Long): Response<Unit>
}
