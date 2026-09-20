package com.kitap.app.data.dto

import com.kitap.app.core.net.KitapJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentStatusTest {
    private fun user(status: String?) = UserDto(id = 1, studentStatus = status)

    @Test
    fun onlyApprovedCountsAsAnApprovedStudent() {
        assertTrue(user("APPROVED").isApprovedStudent)
        listOf("NONE", "PENDING", "REJECTED", null).forEach { assertFalse(it.toString(), user(it).isApprovedStudent) }
    }

    /** Eskiden uygulamanın uydurduğu değerler backend'de yok; onaylı sayılmamalı. */
    @Test
    fun valuesTheBackendNeverSendsAreNotApproved() {
        assertFalse(user("CONFIRMED").isApprovedStudent)
        assertFalse(user("VERIFIED").isApprovedStudent)
    }

    @Test
    fun constantsMatchTheBackendEnumNames() {
        assertEquals(listOf("NONE", "PENDING", "APPROVED", "REJECTED"),
            listOf(StudentStatus.NONE, StudentStatus.PENDING, StudentStatus.APPROVED, StudentStatus.REJECTED))
    }

    @Test
    fun backendPayloadIsRecognisedAfterParsing() {
        val me = KitapJson.instance.decodeFromString(
            MeDto.serializer(),
            """{"user":{"id":1,"name":"Ayşe","admin":false,"studentStatus":"APPROVED"}}""",
        )
        assertTrue(me.user.isApprovedStudent)
    }
}
