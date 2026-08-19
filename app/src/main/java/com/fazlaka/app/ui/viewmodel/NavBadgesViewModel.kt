package com.fazlaka.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fazlaka.app.core.network.ApiResult
import com.fazlaka.app.core.realtime.RealtimeEvent
import com.fazlaka.app.core.realtime.RealtimeManager
import com.fazlaka.app.data.repository.MessagingRepository
import com.fazlaka.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NavBadgesState(
    val unreadMessages: Int = 0,
    val unreadNotifications: Int = 0,
)

/** Unread counters shown on the bottom navigation bar. */
@HiltViewModel
class NavBadgesViewModel @Inject constructor(
    private val messaging: MessagingRepository,
    private val profile: ProfileRepository,
    private val realtime: RealtimeManager,
) : ViewModel() {

    private val _state = MutableStateFlow(NavBadgesState())
    val state: StateFlow<NavBadgesState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            realtime.events.collect { event ->
                when (event) {
                    is RealtimeEvent.NewMessage -> refresh()
                    is RealtimeEvent.Notification -> refresh()
                    else -> Unit
                }
            }
        }
        // Periodic safety refresh (cheap calls)
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val conversations = (messaging.conversations(limit = 50) as? ApiResult.Success)?.data
            val unreadMessages = conversations?.data?.sumOf { it.unreadCount } ?: 0
            val unreadNotifications =
                (profile.unreadCount() as? ApiResult.Success)?.data?.count ?: 0
            _state.value = NavBadgesState(
                unreadMessages = unreadMessages,
                unreadNotifications = unreadNotifications,
            )
        }
    }
}
