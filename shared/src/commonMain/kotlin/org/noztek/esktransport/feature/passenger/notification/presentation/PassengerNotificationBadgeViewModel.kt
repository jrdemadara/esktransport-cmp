package org.noztek.esktransport.feature.passenger.notification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.notification.domain.usecase.GetNotificationsUseCase

data class PassengerNotificationBadgeUiState(
    val unreadCount: Int = 0,
)

class PassengerNotificationBadgeViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PassengerNotificationBadgeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            withContext(ioDispatcher) { getNotificationsUseCase() }
                .onSuccess { page ->
                    _uiState.update { it.copy(unreadCount = page.unreadCount) }
                }
        }
    }
}
