package com.kitap.app.data.api

import com.kitap.app.data.dto.BookDto
import com.kitap.app.data.dto.BookMetadataDto
import com.kitap.app.data.dto.PreviewBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BookApi {
    @GET("api/v1/books")
    suspend fun searchBooks(@Query("q") query: String? = null): Response<List<BookDto>>

    @POST("api/v1/books/preview")
    suspend fun previewBook(@Body body: PreviewBody): Response<BookMetadataDto>
}
