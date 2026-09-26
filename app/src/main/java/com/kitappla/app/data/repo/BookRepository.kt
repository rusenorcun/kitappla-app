package com.kitappla.app.data.repo

import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.net.safeApiCall
import com.kitappla.app.data.api.BookApi
import com.kitappla.app.data.dto.BookDto
import com.kitappla.app.data.dto.BookMetadataDto
import com.kitappla.app.data.dto.PreviewBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(private val api: BookApi) {
    suspend fun search(query: String? = null): ApiResult<List<BookDto>> = safeApiCall { api.searchBooks(query) }

    suspend fun preview(purchaseLink: String): ApiResult<BookMetadataDto> =
        safeApiCall { api.previewBook(PreviewBody(purchaseLink)) }
}
