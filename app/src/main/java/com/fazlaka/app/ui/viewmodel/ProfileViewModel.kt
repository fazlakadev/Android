package com.fazlaka.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.common.ErrorMessages.localized
import com.fazlaka.app.core.datastore.SessionManager
import com.fazlaka.app.core.model.dto.PublicProfileStatsDto
import com.fazlaka.app.core.model.dto.UpdateProfileRequest
import com.fazlaka.app.core.model.dto.UserDto
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

data class ProfileUiState(
    val me: ApiResult<UserDto>? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val uploading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val session: SessionManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    val userFlow = session.currentUserFlow()

    private val _stats = MutableStateFlow<PublicProfileStatsDto?>(null)
    val stats: StateFlow<PublicProfileStatsDto?> = _stats.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            when (val r = profileRepository.me()) {
                is ApiResult.Success -> {
                    session.updateUser(r.data)
                    _state.value = _state.value.copy(me = r)
                    loadStats(r.data.publicId ?: r.data.username)
                }
                is ApiResult.Failure -> _state.value = _state.value.copy(me = r)
            }
        }
    }

    private fun loadStats(identifier: String?) {
        if (identifier.isNullOrBlank()) return
        viewModelScope.launch {
            when (val r = profileRepository.publicProfile(identifier)) {
                is ApiResult.Success -> _stats.value = r.data.stats
                is ApiResult.Failure -> Unit
            }
        }
    }

    fun updateProfile(request: UpdateProfileRequest) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, saved = false, error = null)
            when (val r = profileRepository.updateProfile(request)) {
                is ApiResult.Success -> {
                    session.updateUser(r.data)
                    _state.value = _state.value.copy(me = r, saving = false, saved = true)
                }
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(saving = false, error = r.localized())
            }
        }
    }

    fun uploadAvatar(uri: Uri) = uploadImage(uri, isAvatar = true)

    fun uploadBanner(uri: Uri) = uploadImage(uri, isAvatar = false)

    private fun uploadImage(uri: Uri, isAvatar: Boolean) {
        if (_state.value.uploading) return
        val part = buildPart(uri, isAvatar) ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(uploading = true, error = null)
            val r = if (isAvatar) profileRepository.uploadAvatar(part)
            else profileRepository.uploadBanner(part)
            when (r) {
                is ApiResult.Success -> {
                    session.updateUser(r.data)
                    _state.value = _state.value.copy(me = r, uploading = false, saved = true)
                }
                is ApiResult.Failure -> _state.value =
                    _state.value.copy(uploading = false, error = r.localized())
            }
        }
    }

    private fun buildPart(uri: Uri, isAvatar: Boolean): MultipartBody.Part? {
        return try {
            val resolver = context.contentResolver
            val name = "image"
            val mime = resolver.getType(uri) ?: "image/jpeg"
            val body = resolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                bytes.toRequestBody(mime.toMediaTypeOrNull())
            } ?: return null
            val filename = "${name}_${System.currentTimeMillis()}.jpg"
            val partName = if (isAvatar) "avatar" else "banner"
            MultipartBody.Part.createFormData(partName, filename, body)
        } catch (e: Exception) {
            null
        }
    }

    fun markOnboarded() {
        viewModelScope.launch {
            runCatching { profileRepository.markOnboarded() }
            session.setOnboarded(true)
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(saved = false, error = null)
    }
}
