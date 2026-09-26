package com.kitappla.app.ui.screens.profile

import com.kitappla.app.data.api.ProfileApi
import com.kitappla.app.data.repo.ProfileRepository
import com.kitappla.app.data.repo.testRetrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Sunucu şifre değişince bu oturum dahil tüm oturumları düşürür (backend `FreshPrincipalFilter`); ekran "başarılı"
 * deyip kalmamalı, çıkış akışını başlatmalı (bkz. MemberNavHost → Giriş, açıklamalı).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfilViewModelTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        server = MockWebServer().also { it.start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun newVm(): ProfilViewModel {
        server.enqueue(MockResponse().setBody("""{"user":{"id":1,"name":"Ayşe","email":"a@b.c"}}"""))
        val vm = ProfilViewModel(ProfileRepository(android.content.ContextWrapper(null), testRetrofit(server).create(ProfileApi::class.java)))
        runBlocking { withTimeout(5_000) { vm.state.first { !it.loading } } }
        return vm
    }

    private fun ProfilViewModel.fillPasswords() {
        updateCurrentPassword("eski123")
        updateNewPassword("yeni456")
        updateConfirmPassword("yeni456")
    }

    @Test
    fun aChangedPasswordStartsTheSignOutFlow() {
        val vm = newVm()
        vm.fillPasswords()
        server.enqueue(MockResponse().setResponseCode(204))

        vm.changePassword()

        val s = runBlocking { withTimeout(5_000) { vm.state.first { it.passwordChanged } } }
        assertFalse(s.changingPassword)
        assertEquals("", s.newPassword)
    }

    @Test
    fun aRejectedChangeKeepsTheUserHereWithTheReason() {
        val vm = newVm()
        vm.fillPasswords()
        server.enqueue(MockResponse().setResponseCode(400).setBody("""{"error":"Mevcut şifren hatalı."}"""))

        vm.changePassword()

        val s = runBlocking { withTimeout(5_000) { vm.state.first { it.error != null } } }
        assertEquals("Mevcut şifren hatalı.", s.error)
        assertFalse(s.passwordChanged)
        assertTrue("yazılanlar silinmez", s.newPassword.isNotEmpty())
    }
}
