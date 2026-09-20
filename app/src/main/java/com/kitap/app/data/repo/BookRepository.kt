package com.kitap.app.data.repo

import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeApiCall
import com.kitap.app.data.api.BookApi
import com.kitap.app.data.dto.BookDto
import com.kitap.app.data.dto.BookMetadataDto
import com.kitap.app.data.dto.PreviewBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(private val api: BookApi) {
    suspend fun search(query: String? = null): ApiResult<List<BookDto>> = safeApiCall { api.searchBooks(query) }

    suspend fun preview(purchaseLink: String): ApiResult<BookMetadataDto> =
        safeApiCall { api.previewBook(PreviewBody(purchaseLink)) }
}
