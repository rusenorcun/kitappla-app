package com.kitappla.app.data.api

import com.kitappla.app.data.dto.UploadedFileDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApi {
    @Multipart
    @POST("api/v1/uploads")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
    ): Response<UploadedFileDto>
}
