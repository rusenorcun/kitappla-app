package com.kitappla.app.ui.screens.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.core.session.SessionManager
import com.kitappla.app.core.session.SessionState
import com.kitappla.app.data.dto.UserDto
import com.kitappla.app.data.dto.isApprovedStudent
import com.kitappla.app.data.repo.AuthRepository
import com.kitappla.app.data.repo.ProfileRepository
import com.kitappla.app.ui.nav.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Ekranın gösterdiği hâl. */
enum class HesapDogrulamaView {
    /** İlk açılışta sunucu soruluyor. */
    CHECKING,

    /** Üye, e-postası doğrulanmamış: gelen kutusu yönlendirmesi + yeniden gönderme. */
    PENDING,

    /** Üye, bağlantı geçersiz/süresi dolmuş ve hâlâ doğrulanmamış. */
    INVALID_LINK,

    /** Üyenin e-postası doğrulanmış. */
    VERIFIED,

    /** Oturum yok; bağlantı web'de onaylandı: giriş yapmaya çağırır. */
    GUEST_VERIFIED,

    /** Oturum yok; bağlantı geçersiz: giriş yapıp yeni bağlantı istemeye çağırır. */
    GUEST_INVALID,

    /** Uygulamada açılan bağlantı sunucuya onaylatılamadı (ağ/sunucu hatası): yeniden denenir. */
    LINK_FAILED,
}

data class HesapDogrulamaState(
    val view: HesapDogrulamaView = HesapDogrulamaView.CHECKING,
    val email: String = "",
    /** Doğrulama az önce bağlantıyla yapıldı ("zaten doğrulanmış" yerine "doğrulandı" denir). */
    val justVerified: Boolean = false,
    /** Okul adresiyle doğrulandığı için öğrenci önceliği de açıldı (backend `confirmAccountEmail`). */
    val studentUnlocked: Boolean = false,
    /** Bağlantı onaylandı ama bu cihazda başka bir hesap açık: açık hesap hâlâ doğrulanmamış. */
    val otherAccountOpen: Boolean = false,
    val checking: Boolean = false,
    val resending: Boolean = false,
    val resendCooldown: Int = 0,
    val sentMessage: String? = null,
    val error: String? = null,
)

/**
 * Hesap e-postası doğrulama ekranı. Üç yoldan açılır:
 * - [Routes.HESAP_DOGRULAMA] (kayıttan sonra, Panom uyarısından, "Doğrula" uyarısından): `durum` argümanı yoktur.
 * - [Routes.EPOSTA_DOGRULA_PATTERN] (e-postadaki bağlantı App Link olarak doğrudan uygulamada açıldı): jetonu önce
 *   sunucuya onaylatır, sonra bağlantıdan dönülmüş gibi davranır.
 * - [Routes.EPOSTA_DOGRULANDI_PATTERN] (web bağlantıyı onaylayıp `kitappla://eposta-dogrulandi` ile döndü): `durum`
 *   boş ya da [Routes.EPOSTA_DURUM_GECERSIZ].
 *
 * Her iki yolda da son söz sunucudadır: kullanıcı yeniden okunur ve açık oturum güncellenir, böylece uygulamanın
 * geri kalanı (Panom rozeti, ön bellekteki bağış uygunluğu) doğrulanmış hâli hemen görür.
 */
@HiltViewModel
class HesapDogrulamaViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val durum: String? = savedStateHandle.get<String>(DURUM_ARG)

    /** Kayıttan hemen sonra açıldıysa doğrulama e-postasının gönderilip gönderilmediği (bkz. [Routes.HESAP_DOGRULAMA_PATTERN]). */
    private var afterRegister: String? = savedStateHandle.get<String>(KAYIT_ARG)

    /** Bağlantıdan mı gelindi (yoksa uygulama içinden açıldı). Uygulamada açılan bağlantıda onaydan sonra belli olur. */
    private var fromLink: Boolean = durum != null
    private var invalidLink: Boolean = durum == Routes.EPOSTA_DURUM_GECERSIZ

    /**
     * Bağlantı doğrudan uygulamada açıldıysa henüz onaylatılmamış jeton. Sunucu yanıt verince silinir; veremezse ekran
     * [HesapDogrulamaView.LINK_FAILED]'da kalır ve sonraki kontrol ("Tekrar dene", uygulamaya dönüş) onayı yineler.
     */
    private var linkToken: String? = savedStateHandle.get<String>(TOKEN_ARG)?.takeIf { it.isNotBlank() }

    private val _state = MutableStateFlow(HesapDogrulamaState())
    val state: StateFlow<HesapDogrulamaState> = _state.asStateFlow()

    private var checkJob: Job? = null
    private var cooldownJob: Job? = null

    init {
        check(silent = false)
    }

    /**
     * Sunucuya doğrulama durumunu sorar. [silent] iken ekran yerinde kalır (uygulamaya geri dönüldüğünde); değilse
     * "Doğruladım, kontrol et" düğmesi yükleniyor görünür. Süren bir kontrol varsa ona katılır.
     */
    fun check(silent: Boolean = true): Job {
        checkJob?.takeIf { it.isActive }?.let { return it }
        return viewModelScope.launch {
            linkToken?.let { token ->
                if (!silent) _state.update { it.copy(checking = true, error = null) }
                when (val r = authRepository.confirmEmail(token)) {
                    is ApiResult.Failure -> {
                        _state.update { it.copy(view = HesapDogrulamaView.LINK_FAILED, checking = false, error = r.message) }
                        return@launch
                    }
                    is ApiResult.Success -> {
                        linkToken = null
                        fromLink = true
                        invalidLink = !r.value
                        _state.update { it.copy(error = null) }
                    }
                }
            }
            val member = sessionManager.state.value as? SessionState.Member
            if (member == null) {
                _state.update {
                    it.copy(
                        view = if (invalidLink) HesapDogrulamaView.GUEST_INVALID else HesapDogrulamaView.GUEST_VERIFIED,
                        checking = false,
                    )
                }
                return@launch
            }
            if (!silent) _state.update { it.copy(checking = true, error = null) }
            val result = authRepository.refreshUser()
            val fresh = (result as? ApiResult.Success)?.value
            val user = fresh ?: member.user
            _state.update { s ->
                val next = s.copy(checking = false, email = user.email)
                when {
                    user.emailVerified -> next.asVerified(user, justNow = fromLink && !invalidLink)
                    // Web "doğrulandı" dedi ama sunucuya ulaşılamadı: bağlantıya güvenilir, oturum sonra tazelenir.
                    fresh == null && fromLink && !invalidLink -> next.asVerified(user, justNow = true)
                    // Onaylanan bağlantı bu cihazdaki hesaba ait değil.
                    fromLink && !invalidLink -> next.copy(view = HesapDogrulamaView.PENDING, otherAccountOpen = true)
                    invalidLink && s.sentMessage == null -> next.copy(view = HesapDogrulamaView.INVALID_LINK)
                    // Kayıttan hemen sonra (bir kez): e-postanın gidip gitmediği söylenir.
                    afterRegister != null -> {
                        val sent = afterRegister == Routes.KAYIT_GONDERILDI
                        afterRegister = null
                        next.copy(
                            view = HesapDogrulamaView.PENDING,
                            sentMessage = if (sent) "${user.email} adresine doğrulama bağlantısı gönderdik." else s.sentMessage,
                            error = if (sent) null else REGISTER_EMAIL_NOT_SENT,
                        )
                    }
                    else -> next.copy(
                        view = HesapDogrulamaView.PENDING,
                        // Açıkça "kontrol et" denip hâlâ doğrulanmamışsa kullanıcıya söylenir; sessiz kontrolde susulur.
                        error = when {
                            result is ApiResult.Failure && !silent -> result.message
                            !silent && s.view == HesapDogrulamaView.PENDING -> NOT_YET_VERIFIED
                            else -> s.error
                        },
                    )
                }
            }
        }.also { checkJob = it }
    }

    fun resend() {
        val s = _state.value
        if (s.resending || s.resendCooldown > 0) return
        _state.update { it.copy(resending = true, error = null, sentMessage = null) }
        viewModelScope.launch {
            when (val r = profileRepository.resendEmailVerification()) {
                is ApiResult.Failure -> _state.update { it.copy(resending = false, error = r.message) }
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            resending = false,
                            sentMessage = r.value,
                            // Yeni bağlantı gönderildi: "geçersiz bağlantı" hâli yerini gelen kutusu yönlendirmesine bırakır.
                            view = if (it.view == HesapDogrulamaView.INVALID_LINK) HesapDogrulamaView.PENDING else it.view,
                        )
                    }
                    startCooldown()
                }
            }
        }
    }

    fun showError(message: String) {
        _state.update { it.copy(error = message) }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (left in RESEND_COOLDOWN_SECONDS downTo 1) {
                _state.update { it.copy(resendCooldown = left) }
                delay(1_000)
            }
            _state.update { it.copy(resendCooldown = 0) }
        }
    }

    private fun HesapDogrulamaState.asVerified(user: UserDto, justNow: Boolean) = copy(
        view = HesapDogrulamaView.VERIFIED,
        justVerified = justNow || (view == HesapDogrulamaView.PENDING || view == HesapDogrulamaView.INVALID_LINK),
        studentUnlocked = user.isApprovedStudent && user.email.lowercase().endsWith(".edu.tr"),
        otherAccountOpen = false,
        error = null,
    )

    companion object {
        const val DURUM_ARG = "durum"
        const val KAYIT_ARG = "kayit"
        const val TOKEN_ARG = "token"
        const val REGISTER_EMAIL_NOT_SENT =
            "Hesabın açıldı ama doğrulama e-postası şu an gönderilemedi. Aşağıdan yeniden isteyebilirsin."
        const val RESEND_COOLDOWN_SECONDS = 30
        const val NOT_YET_VERIFIED = "E-posta adresin henüz doğrulanmamış görünüyor. Bağlantıya dokunduktan sonra tekrar dene."
    }
}
