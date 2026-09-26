package com.kitappla.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.dto.AdminMonitorDto
import com.kitappla.app.data.repo.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AdminIzlecState(
    val data: AdminMonitorDto? = null,
    val isLoading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null,
    /** Canlı izleme açıkken ekran görünür olduğu sürece [AdminIzlecViewModel.POLL_MILLIS]'de bir yenilenir. */
    val live: Boolean = true,
    val lastUpdated: String? = null,
    /** Sonlandırılmakta olan oturumun tanıtıcısı (düğmesi yükleniyor görünür). */
    val expiringId: String? = null,
    /** Sonlandırma sonucu; ekran kısa bir uyarıyla gösterip [AdminIzlecViewModel.clearActionMessage] çağırır. */
    val actionMessage: String? = null,
)

/**
 * Yönetim izleci (web: /admin/izlec): canlı oturumlar ve sunucu çalışma parametreleri. Web sayfası gibi 10 saniyede bir
 * sessizce yenilenir; yalnızca ekran görünürken ([startPolling]/[stopPolling]) ve "Canlı" açıkken. Sessiz yenileme
 * hata verirse son veri yerinde kalır, hata üstte küçük bir uyarı olarak gösterilir.
 */
@HiltViewModel
class AdminIzlecViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminIzlecState())
    val state: StateFlow<AdminIzlecState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var pollJob: Job? = null

    init {
        load(silent = false)
    }

    /** [silent] değilse yenileme göstergesi (ilk yüklemede tam ekran, sonra aşağı çekme) görünür. */
    fun load(silent: Boolean = false): Job {
        loadJob?.takeIf { it.isActive }?.let { return it }
        return viewModelScope.launch {
            if (!silent) _state.update { it.copy(refreshing = it.data != null) }
            when (val r = adminRepository.getMonitor()) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        data = r.value,
                        isLoading = false,
                        refreshing = false,
                        error = null,
                        lastUpdated = SimpleDateFormat("HH:mm:ss", Locale("tr", "TR")).format(Date()),
                    )
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(isLoading = false, refreshing = false, error = r.message)
                }
            }
        }.also { loadJob = it }
    }

    /**
     * Oturumu sonlandırır (kendi oturumu sunucuda reddedilir). Başarıda liste hemen yenilenir; sonlandırılan kullanıcı
     * bir sonraki isteğinde oturumsuz kalır.
     */
    fun expireSession(id: String, userName: String) {
        if (_state.value.expiringId != null || id.isBlank()) return
        _state.update { it.copy(expiringId = id) }
        viewModelScope.launch {
            val message = when (val r = adminRepository.expireSession(id)) {
                is ApiResult.Success -> "$userName adlı kullanıcının oturumu sonlandırıldı."
                is ApiResult.Failure -> r.message
            }
            _state.update { it.copy(expiringId = null, actionMessage = message) }
            loadJob?.join()
            load(silent = true)
        }
    }

    fun clearActionMessage() {
        _state.update { it.copy(actionMessage = null) }
    }

    fun setLive(live: Boolean) {
        _state.update { it.copy(live = live) }
        if (live) load(silent = true)
    }

    fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(POLL_MILLIS)
                if (_state.value.live) load(silent = true).join()
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    companion object {
        const val POLL_MILLIS = 10_000L

        /** Bellek kullanımı bu yüzdeyi aşınca uyarı rengiyle gösterilir. */
        const val MEMORY_WARN_PERCENT = 85
    }
}
