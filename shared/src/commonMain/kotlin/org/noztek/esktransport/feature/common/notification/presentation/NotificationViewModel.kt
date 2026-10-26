package org.noztek.esktransport.feature.common.notification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.notification.domain.model.Notification
import org.noztek.esktransport.feature.common.notification.domain.usecase.GetNotificationsUseCase
import org.noztek.esktransport.feature.common.notification.domain.usecase.MarkNotificationsReadUseCase

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<Notification> = emptyList(),
    val errorMessage: String? = null,
)

class NotificationViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationsReadUseCase: MarkNotificationsReadUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            withContext(ioDispatcher) { getNotificationsUseCase() }.fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = page.notifications,
                            errorMessage = null,
                        )
                    }
                    if (page.unreadCount > 0) {
                        withContext(ioDispatcher) { markNotificationsReadUseCase() }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load notifications.",
                        )
                    }
                },
            )
        }
    }
}
