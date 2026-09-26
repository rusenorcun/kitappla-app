package com.kitappla.app.core.session

import com.kitappla.app.core.cache.ApiCache
import com.kitappla.app.core.net.PersistentCookieJar
import com.kitappla.app.data.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SessionState {
    data object Loading : SessionState
    data object Guest : SessionState
    data class Member(val user: UserDto) : SessionState
    data class Admin(val user: UserDto) : SessionState
}

@Singleton
class SessionManager @Inject constructor(
    private val cookieJar: PersistentCookieJar,
    private val cache: ApiCache,
) {
    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    /**
     * Ön bellekteki bağış yanıtları oturuma özel alanlar (`eligibility`) taşır; oturum durumu her değiştiğinde
     * ön bellek boşaltılır ki bir hesabın verisi başkasına (ya da misafire) gösterilmesin.
     */
    private fun transition(next: SessionState) {
        cache.clear()
        _state.value = next
    }

    fun signedIn(user: UserDto) {
        transition(if (user.admin) SessionState.Admin(user) else SessionState.Member(user))
    }

    /**
     * Açık üye oturumunun kullanıcı bilgisini yeniler (ör. e-posta doğrulandı). Oturum başka bir hesaba ya da role
     * aitse, misafirse ya da bilgi değişmediyse hiçbir şey yapmaz: grafik yeniden kurulmaz, ön bellek boşa atılmaz.
     */
    fun updateUser(user: UserDto) {
        val current = _state.value as? SessionState.Member ?: return
        if (user.admin || current.user.id != user.id || current.user == user) return
        transition(SessionState.Member(user))
    }

    /** Çıkış/oturum düşmesi: çerezleri siler. Durum, çerez temizliği ne olursa olsun önce Misafir olur. */
    fun signedOut() {
        transition(SessionState.Guest)
        cookieJar.clear()
    }

    /** Ağ hatası gibi durumlarda çerezleri koruyarak misafir görünümüne düşer. */
    fun markGuest() {
        transition(SessionState.Guest)
    }

    fun onUnauthorized() {
        if (_state.value !is SessionState.Guest) signedOut()
    }
}
