package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.model.dto.PlaylistDto
import com.fazlaka.app.core.model.dto.PublicProfileDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ContentRepository
import com.fazlaka.app.data.repository.MessagingRepository
import com.fazlaka.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val idOrSlug: String = savedStateHandle.get<String>("idOrSlug") ?: ""

    private val _state = MutableStateFlow<ApiResult<PlaylistDto>?>(null)
    val state: StateFlow<ApiResult<PlaylistDto>?> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = contentRepository.playlist(idOrSlug)
        }
    }
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val messagingRepository: MessagingRepository,
    private val socialRepository: com.fazlaka.app.data.repository.SocialRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val identifier: String = savedStateHandle.get<String>("identifier") ?: ""

    private val _state = MutableStateFlow<ApiResult<PublicProfileDto>?>(null)
    val state: StateFlow<ApiResult<PublicProfileDto>?> = _state.asStateFlow()

    private val _message = MutableStateFlow<MessageStartState>(MessageStartState.Idle)
    val message: StateFlow<MessageStartState> = _message.asStateFlow()

    private val _relation = MutableStateFlow<com.fazlaka.app.core.model.dto.FriendRelationDto?>(
        null,
    )
    val relation: StateFlow<com.fazlaka.app.core.model.dto.FriendRelationDto?> =
        _relation.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    /** The other user's id once the profile has loaded. */
    private var profileUserId: String? = null

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = profileRepository.publicProfile(identifier)
            val id = (_state.value as? ApiResult.Success)?.data?.id
            if (id != null) {
                profileUserId = id
                _relation.value =
                    (socialRepository.relationship(id) as? ApiResult.Success)?.data
            }
        }
    }

    fun startConversation() {
        val profileId = (_state.value as? ApiResult.Success)?.data?.id ?: return
        if (_message.value is MessageStartState.Loading) return
        _message.value = MessageStartState.Loading
        viewModelScope.launch {
            _message.value = when (val r = messagingRepository.getOrCreateConversation(profileId)) {
                is ApiResult.Success -> MessageStartState.Ready(r.data.conversation.id)
                is ApiResult.Failure -> MessageStartState.Failed
            }
        }
    }

    fun addFriend() {
        val id = profileUserId ?: return
        viewModelScope.launch {
            _busy.value = true
            val r = socialRepository.sendFriendRequest(id)
            _toast.value = when (r) {
                is ApiResult.Success -> "تم إرسال طلب الصداقة"
                is ApiResult.Failure -> r.message ?: "تعذّر إرسال الطلب"
            }
            _relation.value = (socialRepository.relationship(id) as? ApiResult.Success)?.data
            _busy.value = false
        }
    }

    fun respondToRequest(accept: Boolean) {
        val requestId = _relation.value?.id ?: return
        val id = profileUserId ?: return
        viewModelScope.launch {
            _busy.value = true
            if (accept) socialRepository.acceptFriendRequest(requestId)
            else socialRepository.rejectFriendRequest(requestId)
            _toast.value = if (accept) "تمت إضافة الصديق" else "تم رفض الطلب"
            _relation.value = (socialRepository.relationship(id) as? ApiResult.Success)?.data
            _busy.value = false
        }
    }

    fun removeFriend() {
        val id = profileUserId ?: return
        viewModelScope.launch {
            _busy.value = true
            socialRepository.removeFriend(id)
            _toast.value = "تم حذف الصديق"
            _relation.value = (socialRepository.relationship(id) as? ApiResult.Success)?.data
            _busy.value = false
        }
    }

    fun toggleBlock() {
        val id = profileUserId ?: return
        val currentlyBlocked = _relation.value?.status == "blocked"
        viewModelScope.launch {
            _busy.value = true
            if (currentlyBlocked) socialRepository.unblockUser(id)
            else socialRepository.blockUser(id)
            _toast.value = if (currentlyBlocked) "تم فك الحظر" else "تم حظر المستخدم"
            _relation.value = (socialRepository.relationship(id) as? ApiResult.Success)?.data
            _busy.value = false
        }
    }

    fun resetMessage() {
        _message.value = MessageStartState.Idle
    }

    fun clearToast() {
        _toast.value = null
    }
}

sealed interface MessageStartState {
    data object Idle : MessageStartState
    data object Loading : MessageStartState
    data class Ready(val conversationId: String) : MessageStartState
    data object Failed : MessageStartState
}
