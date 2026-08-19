package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.common.ErrorMessages.localized
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.model.dto.SupportTicketDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SupportUiState(
    val loading: Boolean = false,
    val tickets: ApiResult<Paginated<SupportTicketDto>>? = null,
    val creating: Boolean = false,
    val error: String? = null,
    val created: Boolean = false,
)

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SupportUiState())
    val state: StateFlow<SupportUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                tickets = profileRepository.myTickets(),
                loading = false,
            )
        }
    }

    fun createTicket(subject: String, message: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(creating = true, error = null, created = false)
            val r = profileRepository.createTicket(
                com.fazlaka.app.core.model.dto.CreateTicketRequest(
                    subject = subject,
                    message = message,
                ),
            )
            when (r) {
                is ApiResult.Success -> _state.value = _state.value.copy(creating = false, created = true)
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(creating = false, error = r.localized())
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, created = false)
    }
}

data class SupportTicketUiState(
    val loading: Boolean = false,
    val ticket: ApiResult<SupportTicketDto>? = null,
    val sending: Boolean = false,
)

@HiltViewModel
class SupportTicketViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle,
) : ViewModel() {

    private val ticketId: String = savedStateHandle.get<String>("ticketId") ?: ""

    private val _state = MutableStateFlow(SupportTicketUiState())
    val state: StateFlow<SupportTicketUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                ticket = profileRepository.ticketDetail(ticketId),
                loading = false,
            )
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(sending = true)
            profileRepository.addTicketMessage(ticketId, text)
            _state.value = _state.value.copy(sending = false)
            load()
        }
    }
}
