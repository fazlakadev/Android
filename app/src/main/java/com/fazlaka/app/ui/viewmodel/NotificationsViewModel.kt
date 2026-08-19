package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.model.dto.NotificationDto
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val loading: Boolean = false,
    val isRefreshing: Boolean = false,
    val list: ApiResult<Paginated<NotificationDto>>? = null,
    val unread: Int = 0,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        load()
        refreshUnread()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                list = profileRepository.notifications(),
                loading = false,
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            _state.value = _state.value.copy(
                list = profileRepository.notifications(),
                isRefreshing = false,
            )
            refreshUnread()
        }
    }

    fun refreshUnread() {
        viewModelScope.launch {
            (profileRepository.unreadCount() as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(unread = it.data.count)
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            profileRepository.markNotificationsRead(null)
            _state.value = _state.value.copy(unread = 0)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            profileRepository.deleteNotification(id)
            load()
            refreshUnread()
        }
    }
}
