package com.kitap.app.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitap.app.core.net.ApiResult
import com.kitap.app.data.dto.UserDto
import com.kitap.app.data.repo.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUyelerUiState(
    val users: List<UserDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    val isActionLoading: Boolean = false,
)

@HiltViewModel
class AdminUyelerViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUyelerUiState())
    val state: StateFlow<AdminUyelerUiState> = _state.asStateFlow()

    init {
        searchUsers("")
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchUsers(query)
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val q = query.trim().ifBlank { null }
            when (val res = adminRepository.getAllUsers(q)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(users = res.value, isLoading = false, error = null) }
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = res.message) }
                }
            }
        }
    }

    fun toggleBlockUser(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val res = adminRepository.toggleBlockUser(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionMessage = "Kullanıcı durumu güncellendi.", isActionLoading = false) }
                    searchUsers(_state.value.searchQuery)
                }
                is ApiResult.Failure -> {
                    _state.update { it.copy(actionMessage = res.message, isActionLoading = false) }
                }
            }
        }
    }

    fun clearActionMessage() {
        _state.update { it.copy(actionMessage = null) }
    }
}
