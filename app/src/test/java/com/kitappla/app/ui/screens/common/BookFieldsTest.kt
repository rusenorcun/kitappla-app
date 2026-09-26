package com.kitappla.app.ui.screens.common

import com.kitappla.app.data.dto.BookMetadataDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookFieldsTest {

    @Test
    fun authorIsRequiredLikeTheTitle() {
        assertEquals(BookFields.TITLE_REQUIRED, BookFields(author = "Paulo Coelho").firstProblem())
        assertEquals(BookFields.AUTHOR_REQUIRED, BookFields(title = "Simyacı", author = "  ").firstProblem())
        assertNull(BookFields(title = "Simyacı", author = "Paulo Coelho").firstProblem())
    }

    @Test
    fun fieldErrorsAppearOnlyAfterSubmitting() {
        val empty = BookFields(title = "Simyacı")
        assertNull(empty.authorError)
        assertNull(empty.validationMessage)

        val submitted = empty.copy(showErrors = true)
        assertEquals(BookFields.AUTHOR_REQUIRED, submitted.authorError)
        assertEquals(BookFields.AUTHOR_REQUIRED, submitted.validationMessage)
        // Kullanıcı yazarı girince hata kendiliğinden kalkar.
        assertNull(submitted.copy(author = "Paulo Coelho").validationMessage)
    }

    @Test
    fun linkFillsOnlyEmptyFields() {
        val typed = BookFields(title = "Simyacı (Cep Boy)", coverUrl = "/uploads/covers/benim.jpg")
        val meta = BookMetadataDto(found = true, title = "Simyacı", author = "Paulo Coelho", coverUrl = "https://img/k.jpg")

        val filled = typed.withMetadata(meta)

        assertEquals("Simyacı (Cep Boy)", filled.title)
        assertEquals("Paulo Coelho", filled.author)
        assertEquals("seçilen kapak linktekinden önce gelir", "/uploads/covers/benim.jpg", filled.coverUrl)
        assertNull(filled.notice)
    }

    @Test
    fun linkCoverFillsTheEmptyCover() {
        val meta = BookMetadataDto(found = true, title = "Simyacı", author = "Paulo Coelho", coverUrl = " https://img/k.jpg ")
        assertEquals("https://img/k.jpg", BookFields().withMetadata(meta).coverUrl)
    }

    @Test
    fun missingInformationIsExplained() {
        assertEquals(
            BookFields.LINK_NOT_FOUND,
            BookFields(purchaseLink = "https://x").withMetadata(BookMetadataDto(found = false)).notice,
        )
        assertEquals(
            BookFields.AUTHOR_MISSING,
            BookFields().withMetadata(BookMetadataDto(found = true, title = "Simyacı", coverUrl = "https://img/k.jpg")).notice,
        )
        assertEquals(
            BookFields.COVER_MISSING,
            BookFields().withMetadata(BookMetadataDto(found = true, title = "Simyacı", author = "Paulo Coelho")).notice,
        )
        assertEquals(
            BookFields.AUTHOR_AND_COVER_MISSING,
            BookFields().withMetadata(BookMetadataDto(found = true, title = "Simyacı")).notice,
        )
    }

    @Test
    fun authorTypedByTheUserSatisfiesAMissingLinkAuthor() {
        val filled = BookFields(author = "Paulo Coelho")
            .withMetadata(BookMetadataDto(found = true, title = "Simyacı", coverUrl = "https://img/k.jpg"))
        assertNull(filled.notice)
    }
}
