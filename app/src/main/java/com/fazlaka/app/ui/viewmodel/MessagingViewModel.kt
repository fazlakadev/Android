package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.model.dto.ConversationDetailDto
import com.fazlaka.app.core.model.dto.ConversationSummaryDto
import com.fazlaka.app.core.model.dto.MessageDto
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.core.realtime.RealtimeEvent
import com.fazlaka.app.core.realtime.RealtimeManager
import com.fazlaka.app.data.repository.MessagingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagesUiState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val conversations: ApiResult<Paginated<ConversationSummaryDto>>? = null,
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repository: MessagingRepository,
    private val realtime: RealtimeManager,
) : ViewModel() {

    private val _state = MutableStateFlow(MessagesUiState())
    val state: StateFlow<MessagesUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            realtime.events.collect { event ->
                when (event) {
                    is RealtimeEvent.NewMessage,
                    is RealtimeEvent.GroupInvite,
                    is RealtimeEvent.GroupRemoved,
                    -> load()
                    else -> Unit
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            if (_state.value.conversations is ApiResult.Success) {
                _state.value = _state.value.copy(refreshing = true)
                _state.value = _state.value.copy(
                    conversations = repository.conversations(),
                    refreshing = false,
                )
            } else {
                _state.value = MessagesUiState(loading = true)
                _state.value = MessagesUiState(
                    conversations = repository.conversations(),
                    loading = false,
                )
            }
        }
    }
}

data class ConversationUiState(
    val loading: Boolean = false,
    val detail: ApiResult<ConversationDetailDto>? = null,
    val sending: Boolean = false,
    val messageText: String = "",
    val draftLoaded: Boolean = false,
    val page: Int = 1,
    val loadingOlder: Boolean = false,
    val endReached: Boolean = false,
)

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val repository: MessagingRepository,
    private val realtime: RealtimeManager,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle,
) : ViewModel() {

    private val conversationId: String = savedStateHandle.get<String>("conversationId") ?: ""

    private val _state = MutableStateFlow(ConversationUiState())
    val state: StateFlow<ConversationUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            realtime.events.collect { event ->
                when (event) {
                    is RealtimeEvent.NewMessage ->
                        if (event.conversationId == conversationId) {
                            appendMessage(event.message)
                            repository.markRead(conversationId)
                        }
                    is RealtimeEvent.SentMessage ->
                        if (event.conversationId == conversationId) {
                            appendMessage(event.message)
                        }
                    else -> Unit
                }
            }
        }
    }

    private fun appendMessage(message: MessageDto) {
        val current = _state.value.detail
        if (current is ApiResult.Success) {
            if (current.data.messages.none { it.id == message.id }) {
                val updated = current.data.copy(messages = current.data.messages + message)
                _state.value = _state.value.copy(
                    detail = ApiResult.Success(updated),
                )
            }
        } else {
            load()
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                detail = repository.conversationDetail(conversationId),
                loading = false,
            )
            repository.markRead(conversationId)
            if (!_state.value.draftLoaded) {
                val draft = repository.draft(conversationId)
                if (draft != null) {
                    _state.value = _state.value.copy(messageText = draft, draftLoaded = true)
                }
            }
        }
    }

    fun onTextChange(text: String) {
        _state.value = _state.value.copy(messageText = text)
    }

    /** Prepends the next-older page of messages when the user scrolls up. */
    fun loadOlder() {
        val s = _state.value
        if (s.loading || s.loadingOlder || s.endReached) return
        val current = s.detail as? ApiResult.Success ?: return
        viewModelScope.launch {
            _state.value = s.copy(loadingOlder = true)
            val next = s.page + 1
            when (val r = repository.conversationDetail(conversationId, page = next, limit = 50)) {
                is ApiResult.Success -> {
                    val existing = current.data.messages
                    val older = r.data.messages.filterNot { m -> existing.any { it.id == m.id } }
                    _state.value = _state.value.copy(
                        detail = ApiResult.Success(
                            current.data.copy(messages = older + existing),
                        ),
                        loadingOlder = false,
                        page = next,
                        endReached = r.data.messages.size < 50,
                    )
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(loadingOlder = false)
            }
        }
    }

    fun send(onDone: (MessageDto) -> Unit = {}) {
        val text = _state.value.messageText.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(sending = true)
            when (val r = repository.sendMessage(conversationId, text)) {
                is ApiResult.Success -> {
                    repository.clearDraft(conversationId)
                    _state.value = _state.value.copy(messageText = "", sending = false)
                    onDone(r.data)
                    load()
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(sending = false)
            }
        }
    }

    fun saveDraft() {
        if (_state.value.messageText.isNotBlank()) {
            viewModelScope.launch { repository.saveDraft(conversationId, _state.value.messageText) }
        }
    }

    fun sendMedia(
        uri: android.net.Uri,
        kind: String,
        durationSec: Int? = null,
        onDone: (MessageDto) -> Unit = {},
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(sending = true)
            when (val upload = repository.uploadChatMedia(uri, kind, durationSec)) {
                is ApiResult.Success -> {
                    val mime = upload.data.mimeType
                    when (val r = repository.sendMedia(
                        conversationId,
                        kind,
                        upload.data.url,
                        mime,
                        upload.data.url.substringAfterLast('/'),
                        upload.data.size,
                        durationSec ?: upload.data.durationSec,
                    )) {
                        is ApiResult.Success -> {
                            _state.value = _state.value.copy(sending = false)
                            onDone(r.data)
                            load()
                        }
                        is ApiResult.Failure -> _state.value = _state.value.copy(sending = false)
                    }
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(sending = false)
            }
        }
    }
}
