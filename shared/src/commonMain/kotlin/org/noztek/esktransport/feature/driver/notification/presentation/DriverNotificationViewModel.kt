package org.noztek.esktransport.feature.driver.notification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.driver.notification.domain.model.DriverNotification
import org.noztek.esktransport.feature.driver.notification.domain.usecase.GetDriverNotificationsUseCase
import org.noztek.esktransport.feature.driver.notification.domain.usecase.MarkDriverNotificationsReadUseCase

data class DriverNotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<DriverNotification> = emptyList(),
    val errorMessage: String? = null,
)

class DriverNotificationViewModel(
    private val getDriverNotificationsUseCase: GetDriverNotificationsUseCase,
    private val markDriverNotificationsReadUseCase: MarkDriverNotificationsReadUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverNotificationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            withContext(ioDispatcher) { getDriverNotificationsUseCase() }.fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = page.notifications,
                            errorMessage = null,
                        )
                    }
                    if (page.unreadCount > 0) {
                        withContext(ioDispatcher) { markDriverNotificationsReadUseCase() }
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
