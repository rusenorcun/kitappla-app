package com.kitap.app.data.dto

import com.kitap.app.core.net.KitapJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DtoParsingTest {
    private val json = KitapJson.instance

    @Test
    fun meDtoParsesAndIgnoresUnknownKeys() {
        val text = """
            {"user":{"id":7,"name":"Ayşe Demir","email":"ayse@ornek.com","admin":false,
             "studentStatus":null,"schoolLevel":null,"initials":"AD","address":"İzmir",
             "phone":null,"school":null,"yeniAlan":"yok sayilir"},
             "quota":{"tier":"member","weeklyUsed":0,"weeklyLimit":3,"weeklyRemaining":3,
             "monthlyUsed":1,"monthlyLimit":5,"monthlyRemaining":4,"canReceive":true}}
        """.trimIndent()
        val me = json.decodeFromString(MeDto.serializer(), text)
        assertEquals(7L, me.user.id)
        assertFalse(me.user.admin)
        assertNull(me.user.phone)
        assertEquals(4L, me.quota?.monthlyRemaining)
    }

    @Test
    fun adminFlagIsRead() {
        val text = """{"user":{"id":1,"name":"Yönetici","email":"admin@kitapla.app","admin":true,"initials":"Y"}}"""
        assertTrue(json.decodeFromString(MeDto.serializer(), text).user.admin)
    }

    @Test
    fun csrfTokenParses() {
        val text = """{"parameterName":"_csrf","token":"abc123","headerName":"X-CSRF-TOKEN"}"""
        val dto = json.decodeFromString(CsrfTokenDto.serializer(), text)
        assertEquals("abc123", dto.token)
        assertEquals("X-CSRF-TOKEN", dto.headerName)
    }

    @Test
    fun donationListParsesWithNullableParts() {
        val text = """
            [{"id":5,"book":{"id":2,"title":"Sefiller","author":"Victor Hugo","coverUrl":"/uploads/covers/a.jpg",
              "purchaseLink":null,"description":null},"donorName":"Ayşe","donorInitials":"A","description":null,
              "quantity":2,"claimed":1,"remaining":1,"source":"USER","targetLevel":"HEPSI","status":"OPEN",
              "priorityActive":false,"priorityLeft":null,"point":null,"createdAt":"2026-09-01T10:00:00Z","eligibility":null}]
        """.trimIndent()
        val list = json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(DonationDto.serializer()), text)
        assertEquals(1, list.size)
        assertEquals("Sefiller", list[0].book.title)
        assertEquals(1L, list[0].remaining)
        assertNull(list[0].point)
    }

    @Test
    fun loginRequestEncodesFields() {
        val text = json.encodeToString(LoginRequest.serializer(), LoginRequest("a@b.com", "sifre"))
        assertEquals("""{"email":"a@b.com","password":"sifre"}""", text)
    }
}
