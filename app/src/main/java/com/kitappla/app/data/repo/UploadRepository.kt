package com.kitappla.app.data.repo

import android.content.Context
import android.net.Uri
import com.kitappla.app.core.image.CoverImage
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.data.api.UploadApi
import com.kitappla.app.data.dto.UploadedFileDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: UploadApi,
) {
    /** Görsel yüklemeden önce sunucu sınırlarına sığdırılır (bkz. [CoverImage]); okuma/küçültme ana iş parçacığında yapılmaz. */
    suspend fun uploadImage(uri: Uri): ApiResult<UploadedFileDto> {
        return try {
            val image = withContext(Dispatchers.IO) { CoverImage.prepare(context.contentResolver, uri) }
                ?: return ApiResult.Failure("Görsel okunamadı. JPG, PNG, WEBP ya da GIF bir görsel seçin.")

            val requestBody = image.bytes.toRequestBody(image.mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", image.fileName, requestBody)

            safeApiCall { api.uploadFile(filePart) }
        } catch (e: OutOfMemoryError) {
            ApiResult.Failure("Görsel çok büyük; daha küçük bir görsel seçin.")
        } catch (e: Exception) {
            ApiResult.Failure(e.localizedMessage ?: "Görsel yüklenirken bir hata oluştu.")
        }
    }
}
