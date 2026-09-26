package com.kitappla.app.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.UserDto
import com.kitappla.app.data.dto.isApprovedStudent
import com.kitappla.app.data.repo.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OgrenciDogrulamaState(
    val user: UserDto? = null,
    val email: String = "",
    val token: String = "",
    val isConfirmed: Boolean = false,
    val sendingEmail: Boolean = false,
    val confirmingToken: Boolean = false,
    val verificationSent: Boolean = false,
    /** Ekran okul e-postasındaki bağlantıdan (jetonla) açıldı: üstte onayın sonucu gösterilir. */
    val openedFromLink: Boolean = false,
    /** Onay bu ekranda az önce tamamlandı ("zaten onaylısın" yerine kutlama gösterilir). */
    val justConfirmed: Boolean = false,
    // Belge Yükleme
    val schoolLevel: String = "LISE",
    val documentNo: String = "",
    val documentUri: Uri? = null,
    val uploadingDocument: Boolean = false,
    val error: String? = null,
    /** Bağlantıdan açılışta yapılan otomatik onayın hatası; form altı yerine üstteki sonuç panelinde gösterilir. */
    val linkError: String? = null,
    val actionMessage: String? = null,
)

@HiltViewModel
class OgrenciDogrulamaViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(OgrenciDogrulamaState())
    val state: StateFlow<OgrenciDogrulamaState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        val initialToken = savedStateHandle?.get<String>("token")
        if (!initialToken.isNullOrBlank()) {
            _state.value = _state.value.copy(token = initialToken, openedFromLink = true)
            confirmToken(fromLink = true)
        }
        load()
    }

    /**
     * Profili sessizce (yükleme göstergesi olmadan) yeniler; ekrana her dönüşte de çağrılır, çünkü doğrulama bağlantısı
     * başka bir cihazda/tarayıcıda onaylanmış olabilir. Devam eden bir yükleme varsa yeni istek atmadan ona katılır.
     */
    fun load(): Job {
        loadJob?.takeIf { it.isActive }?.let { return it }
        return viewModelScope.launch {
            when (val r = profileRepository.getProfile()) {
                is ApiResult.Failure -> {}
                is ApiResult.Success -> {
                    val u = r.value.user
                    // Onayla aynı anda giden profil isteği onaydan önceki (bayat) durumu getirebilir; bu ekranda az önce
                    // yapılan onay geri alınmaz. Güncelleme atomiktir: araya giren onay sonucu ezilmez.
                    _state.update {
                        it.copy(
                            user = u,
                            isConfirmed = u.isApprovedStudent || it.justConfirmed,
                            documentNo = u.documentNo ?: it.documentNo,
                            schoolLevel = u.schoolLevel ?: it.schoolLevel,
                        )
                    }
                }
            }
        }.also { loadJob = it }
    }

    fun updateEmail(v: String) { _state.value = _state.value.copy(email = v) }
    fun updateToken(v: String) { _state.value = _state.value.copy(token = v) }
    fun updateSchoolLevel(v: String) { _state.value = _state.value.copy(schoolLevel = v) }
    fun updateDocumentNo(v: String) { _state.value = _state.value.copy(documentNo = v) }
    fun updateDocumentUri(v: Uri?) { _state.value = _state.value.copy(documentUri = v) }

    fun sendVerification() {
        val em = _state.value.email.trim()
        if (em.isBlank()) {
            _state.value = _state.value.copy(error = "Lütfen öğrenci e-posta adresinizi girin.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(sendingEmail = true, error = null)
            when (val r = profileRepository.verifyStudent(em)) {
                // Backend, e-posta zaten başka bir hesapta kayıtlıysa (ör. "Bu okul adresi başka bir hesapta
                // kullanılıyor.") bunu hata olarak döner; mesajı olduğu gibi göstermek yeterli.
                is ApiResult.Failure -> _state.value = _state.value.copy(sendingEmail = false, error = r.message)
                is ApiResult.Success -> {
                    val sent = r.value.emailSent
                    _state.value = _state.value.copy(
                        sendingEmail = false,
                        verificationSent = sent,
                        actionMessage = if (sent) "Doğrulama bağlantısı e-posta adresinize gönderildi." else null,
                        // Backend 200 dönüp e-postayı sessizce atlayabilir (mail servisi kapalı ya da art arda
                        // çok deneme); bu durumda "gönderildi" yalanını söylemek yerine kullanıcıyı bilgilendir.
                        error = if (sent) null
                            else "Öğrenci doğrulaman beklemede; e-posta gönderilmedi. Daha sonra tekrar dene.",
                    )
                }
            }
        }
    }

    /** @param fromLink ekran açılırken bağlantı jetonuyla yapılan otomatik onay (hata üst panelde gösterilir) */
    fun confirmToken(fromLink: Boolean = false) {
        val t = extractVerificationToken(_state.value.token)
        if (t.isBlank()) {
            _state.value = _state.value.copy(error = "Lütfen doğrulama bağlantısını ya da kodunu yapıştırın.")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(confirmingToken = true, error = null, linkError = null) }
            when (val r = profileRepository.confirmStudent(t)) {
                is ApiResult.Failure -> {
                    // Aynı bağlantıya ikinci kez dokunuldu (jeton harcandı) ama öğrenci zaten onaylı: hata gösterilmez.
                    val approved = r.code != null &&
                        (profileRepository.getProfile() as? ApiResult.Success)?.value?.user?.isApprovedStudent == true
                    _state.update {
                        when {
                            approved -> it.copy(confirmingToken = false, isConfirmed = true)
                            fromLink -> it.copy(confirmingToken = false, linkError = r.message)
                            else -> it.copy(confirmingToken = false, error = r.message)
                        }
                    }
                }
                is ApiResult.Success -> {
                    // Sonuç ekranın üstünde büyük gösterilir (bkz. OgrenciDogrulamaScreen); ayrıca uyarı çıkmaz.
                    _state.update {
                        it.copy(user = r.value, confirmingToken = false, isConfirmed = true, justConfirmed = true)
                    }
                }
            }
        }
    }

    fun uploadDocument() {
        val uri = _state.value.documentUri
        if (uri == null) {
            _state.value = _state.value.copy(error = "Lütfen öğrenci belgenizi (PDF veya görsel) seçin.")
            return
        }
        val docNo = _state.value.documentNo.trim()
        if (docNo.isBlank()) {
            _state.value = _state.value.copy(error = "Lütfen öğrenci belge numaranızı girin.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(uploadingDocument = true, error = null)
            when (val r = profileRepository.uploadStudentDocument(uri, _state.value.schoolLevel, docNo)) {
                is ApiResult.Failure -> {
                    _state.value = _state.value.copy(uploadingDocument = false, error = r.message)
                }
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        uploadingDocument = false,
                        user = r.value,
                        actionMessage = "Belgeniz incelemeye alındı. Onaylandığında bağışlarda 48 saat öncelik kazanacaksınız.",
                    )
                    load()
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
