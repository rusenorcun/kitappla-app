package com.kitappla.app.ui.screens.admin

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitappla.app.core.document.DocumentRenderer
import com.kitappla.app.core.document.RenderedDocument
import com.kitappla.app.core.net.ApiResult
import com.kitappla.app.data.repo.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AdminBelgeState(
    val userName: String = "",
    val loading: Boolean = true,
    val document: RenderedDocument? = null,
    val error: String? = null,
)

/**
 * Öğrenci belgesini yöneticinin oturumuyla indirip uygulama içinde gösterir (web: /admin/belge/{id}). Tarayıcıya
 * yönlendirmek işe yaramıyordu (tarayıcıda uygulamanın oturumu yok); başka bir uygulamaya da verilmez.
 */
@HiltViewModel
class AdminBelgeGoruntuleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val adminRepository: AdminRepository,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {

    private val userId: Long? = savedStateHandle.get<String>(ID_ARG)?.toLongOrNull()

    private val _state = MutableStateFlow(AdminBelgeState(userName = savedStateHandle.get<String>(NAME_ARG).orEmpty()))
    val state: StateFlow<AdminBelgeState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        val id = userId
        if (id == null) {
            _state.update { it.copy(loading = false, error = "Belge bulunamadı.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val r = adminRepository.getDocumentFile(id)) {
                is ApiResult.Failure -> _state.update { it.copy(loading = false, error = r.message) }
                is ApiResult.Success -> {
                    val rendered = try {
                        withContext(Dispatchers.Default) {
                            DocumentRenderer.render(r.value.bytes, r.value.contentType, context.cacheDir)
                        }
                    } catch (e: Exception) {
                        null
                    } catch (e: OutOfMemoryError) {
                        null
                    }
                    _state.update {
                        if (rendered == null) it.copy(loading = false, error = RENDER_FAILED)
                        else it.copy(loading = false, document = rendered)
                    }
                }
            }
        }
    }

    companion object {
        const val ID_ARG = "id"
        const val NAME_ARG = "ad"
        const val RENDER_FAILED = "Belge açılamadı; dosya bozuk olabilir. Web yönetim panelinden kontrol edebilirsin."
    }
}
