package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.model.dto.FriendRequestDto
import com.fazlaka.app.core.model.dto.FriendUserDto
import com.fazlaka.app.core.model.dto.Paginated
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.MessagingRepository
import com.fazlaka.app.data.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsUiState(
    val loading: Boolean = false,
    val isRefreshing: Boolean = false,
    val friends: ApiResult<Paginated<FriendUserDto>>? = null,
    val incoming: ApiResult<Paginated<FriendRequestDto>>? = null,
    val outgoing: ApiResult<Paginated<FriendRequestDto>>? = null,
    val suggestions: ApiResult<List<FriendUserDto>>? = null,
    val searchQuery: String = "",
    val searchResults: ApiResult<List<FriendUserDto>>? = null,
    val searching: Boolean = false,
    val message: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: SocialRepository,
    private val messaging: MessagingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FriendsUiState())
    val state: StateFlow<FriendsUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            _state.value = _state.value.copy(
                friends = repository.friends(),
                incoming = repository.incomingRequests(),
                outgoing = repository.outgoingRequests(),
                suggestions = repository.friendSuggestions(),
                loading = false,
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            _state.value = _state.value.copy(
                friends = repository.friends(),
                incoming = repository.incomingRequests(),
                outgoing = repository.outgoingRequests(),
                suggestions = repository.friendSuggestions(),
                isRefreshing = false,
            )
        }
    }

    fun accept(requestId: String) {
        viewModelScope.launch {
            repository.acceptFriendRequest(requestId)
            load()
        }
    }

    fun reject(requestId: String) {
        viewModelScope.launch {
            repository.rejectFriendRequest(requestId)
            load()
        }
    }

    fun sendRequest(userId: String) {
        viewModelScope.launch {
            when (val r = repository.sendFriendRequest(userId)) {
                is ApiResult.Success -> _state.value =
                    _state.value.copy(message = "تم إرسال طلب الصداقة")
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(message = r.message ?: "تعذر إرسال الطلب")
            }
            load()
        }
    }

    fun remove(friendId: String) {
        viewModelScope.launch {
            repository.removeFriend(friendId)
            _state.value = _state.value.copy(message = "تم حذف الصديق")
            load()
        }
    }

    fun block(userId: String) {
        viewModelScope.launch {
            repository.blockUser(userId)
            _state.value = _state.value.copy(message = "تم حظر المستخدم")
            load()
        }
    }

    fun unblock(userId: String) {
        viewModelScope.launch {
            repository.unblockUser(userId)
            _state.value = _state.value.copy(message = "تم فك الحظر")
            load()
        }
    }

    fun onSearchChange(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.value = _state.value.copy(searchResults = null, searching = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            _state.value = _state.value.copy(searching = true)
            val results = repository.searchFriends(query.trim())
            _state.value = _state.value.copy(searchResults = results, searching = false)
        }
    }

    /** Opens (or creates) the direct conversation with a friend. */
    fun openConversation(userId: String, onReady: (String) -> Unit) {
        viewModelScope.launch {
            when (val r = messaging.getOrCreateConversation(userId)) {
                is ApiResult.Success -> onReady(r.data.conversation.id)
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(message = r.message ?: "تعذّر فتح المحادثة")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
