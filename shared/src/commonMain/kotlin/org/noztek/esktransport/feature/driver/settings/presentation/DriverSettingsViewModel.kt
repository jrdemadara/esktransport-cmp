package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noztek.esktransport.core.session.domain.usecase.ObserveCurrentSessionUseCase
import org.noztek.esktransport.feature.common.logout.domain.usecase.LogoutUseCase
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingState
import org.noztek.esktransport.feature.driver.onboarding.domain.usecase.GetDriverOnboardingStatusUseCase
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverAccountProfile
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverAccountUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverProfilePhotoUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.UpdateDriverAccountUseCase

class DriverSettingsViewModel(
    private val observeCurrentSessionUseCase: ObserveCurrentSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getDriverAccountUseCase: GetDriverAccountUseCase,
    private val updateDriverAccountUseCase: UpdateDriverAccountUseCase,
    private val getDriverProfilePhotoUseCase: GetDriverProfilePhotoUseCase,
    private val getDriverOnboardingStatusUseCase: GetDriverOnboardingStatusUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverSettingsUiState())
    val uiState: StateFlow<DriverSettingsUiState> = _uiState.asStateFlow()

    init {
        observeSession()
        loadAccount()
        loadProfilePhoto()
        loadDriverStatus()
    }

    fun logout() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoggingOut = true, errorMessage = null) }
            logoutUseCase().fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            isLoggedOut = true,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoggingOut = false,
                            errorMessage = throwable.message ?: "Logout failed.",
                        )
                    }
                },
            )
        }
    }

    fun clearLogoutState() {
        _uiState.update { it.copy(isLoggedOut = false) }
    }

    fun editEmail() {
        _uiState.update {
            it.copy(
                activeEditField = DriverAccountEditableField.Email,
                editValue = it.email,
                errorMessage = null,
                statusMessage = null,
            )
        }
    }

    fun editAddress() {
        _uiState.update {
            it.copy(
                activeEditField = DriverAccountEditableField.Address,
                editValue = it.address,
                errorMessage = null,
                statusMessage = null,
            )
        }
    }

    fun onEditValueChange(value: String) {
        _uiState.update { it.copy(editValue = value) }
    }

    fun dismissAccountEditor() {
        _uiState.update {
            it.copy(
                activeEditField = null,
                editValue = "",
                isSavingAccount = false,
            )
        }
    }

    fun saveAccountEdit() {
        val state = _uiState.value
        val field = state.activeEditField ?: return
        val nextValue = state.editValue.trim()

        viewModelScope.launch(ioDispatcher) {
            _uiState.update {
                it.copy(
                    isSavingAccount = true,
                    errorMessage = null,
                    statusMessage = null,
                )
            }

            val nextEmail = when (field) {
                DriverAccountEditableField.Email -> nextValue
                DriverAccountEditableField.Address -> state.email
            }
            val nextAddress = when (field) {
                DriverAccountEditableField.Email -> state.address
                DriverAccountEditableField.Address -> nextValue
            }

            updateDriverAccountUseCase(
                email = nextEmail,
                address = nextAddress,
            ).fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.withAccount(profile).copy(
                            activeEditField = null,
                            editValue = "",
                            isSavingAccount = false,
                            statusMessage = "${field.label} updated.",
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingAccount = false,
                            errorMessage = throwable.message ?: "Failed to update account details.",
                        )
                    }
                },
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun observeSession() {
        viewModelScope.launch {
            observeCurrentSessionUseCase().collect { user ->
                _uiState.update {
                    it.copy(
                        name = user.name.orEmpty(),
                        phone = user.phone.orEmpty(),
                        role = user.primaryRole?.replaceFirstChar { char -> char.uppercase() } ?: "Driver",
                    )
                }
            }
        }
    }

    private fun loadProfilePhoto() {
        viewModelScope.launch(ioDispatcher) {
            getDriverProfilePhotoUseCase().onSuccess { bytes ->
                _uiState.update { it.copy(profilePhotoBytes = bytes) }
            }
        }
    }

    private fun loadAccount() {
        viewModelScope.launch(ioDispatcher) {
            getDriverAccountUseCase().fold(
                onSuccess = { profile ->
                    _uiState.update { it.withAccount(profile) }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Failed to load account details.")
                    }
                },
            )
        }
    }

    private fun loadDriverStatus() {
        viewModelScope.launch(ioDispatcher) {
            getDriverOnboardingStatusUseCase().onSuccess { status ->
                _uiState.update {
                    it.copy(
                        driverId = status.driverId,
                        isVerifiedDriver = status.canGo || status.status == DriverOnboardingState.Ready,
                    )
                }
            }
        }
    }
}

private val DriverAccountEditableField.label: String
    get() = when (this) {
        DriverAccountEditableField.Email -> "Email"
        DriverAccountEditableField.Address -> "Address"
    }

private fun DriverSettingsUiState.withAccount(profile: DriverAccountProfile): DriverSettingsUiState {
    return copy(
        name = profile.name.ifBlank { name },
        phone = profile.phone.ifBlank { phone },
        email = profile.email,
        address = profile.address,
        driverId = profile.driverId ?: driverId,
    )
}
