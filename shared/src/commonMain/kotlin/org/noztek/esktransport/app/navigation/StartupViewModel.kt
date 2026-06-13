package org.noztek.esktransport.app.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.noztek.esktransport.core.session.domain.usecase.ObserveCurrentSessionUseCase
import org.noztek.esktransport.core.session.domain.usecase.ObserveIsLoggedInUseCase

class StartupViewModel(
    observeCurrentSessionUseCase: ObserveCurrentSessionUseCase,
    observeIsLoggedInUseCase: ObserveIsLoggedInUseCase,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(StartupUiState())
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            combine(
                observeIsLoggedInUseCase(),
                observeCurrentSessionUseCase(),
            ) { isLoggedIn, session ->
                val normalizedRoles = session.roles.map { it.trim().lowercase() }.toSet()
                val normalizedRole = session.primaryRole?.trim()?.lowercase()
                val destination = when {
                    !isLoggedIn -> RootRoute.STARTER
                    normalizedRole == "driver" || normalizedRoles.contains("driver") -> RootRoute.DRIVER
                    normalizedRole == "passenger" || normalizedRole == "customer" ||
                        normalizedRoles.contains("passenger") || normalizedRoles.contains("customer") -> RootRoute.PASSENGER
                    else -> RootRoute.AUTH
                }
                StartupUiState(
                    isReady = true,
                    startRoute = destination,
                )
            }.collect { _uiState.value = it }
        }
    }

    fun clear() {
        scope.cancel()
    }
}

data class StartupUiState(
    val isReady: Boolean = false,
    val startRoute: String = RootRoute.STARTER,
)
