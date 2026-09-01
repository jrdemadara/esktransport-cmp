package org.noztek.esktransport.feature.passenger.settings.presentation

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
import org.noztek.esktransport.feature.passenger.settings.domain.model.PassengerAccountProfile
import org.noztek.esktransport.feature.passenger.settings.domain.model.PassengerProfilePhotoUpload
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.GetPassengerAccountUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.GetPassengerProfilePhotoUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.UpdatePassengerAccountUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.UploadPassengerProfilePhotoUseCase

class PassengerAccountViewModel(
    private val observeCurrentSessionUseCase: ObserveCurrentSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getPassengerAccountUseCase: GetPassengerAccountUseCase,
    private val updatePassengerAccountUseCase: UpdatePassengerAccountUseCase,
    private val getPassengerProfilePhotoUseCase: GetPassengerProfilePhotoUseCase,
    private val uploadPassengerProfilePhotoUseCase: UploadPassengerProfilePhotoUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PassengerAccountUiState(isLoading = true))
    val uiState: StateFlow<PassengerAccountUiState> = _uiState.asStateFlow()

    init {
        observeSession()
        loadAccount()
        loadProfilePhoto()
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
                activeEditField = PassengerAccountEditableField.Email,
                editValue = it.email,
                errorMessage = null,
                statusMessage = null,
            )
        }
    }

    fun editAddress() {
        _uiState.update {
            it.copy(
                activeEditField = PassengerAccountEditableField.Address,
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
                PassengerAccountEditableField.Email -> nextValue
                PassengerAccountEditableField.Address -> state.email
            }
            val nextAddress = when (field) {
                PassengerAccountEditableField.Email -> state.address
                PassengerAccountEditableField.Address -> nextValue
            }

            updatePassengerAccountUseCase(
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

    fun uploadProfilePhoto(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update {
                it.copy(
                    profilePhotoBytes = bytes,
                    isUploadingPhoto = true,
                    errorMessage = null,
                    statusMessage = null,
                )
            }

            uploadPassengerProfilePhotoUseCase(
                PassengerProfilePhotoUpload(
                    fileName = fileName,
                    mimeType = mimeType,
                    bytes = bytes,
                ),
            ).fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.withAccount(profile).copy(
                            profilePhotoBytes = bytes,
                            isUploadingPhoto = false,
                            statusMessage = "Profile photo updated.",
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            errorMessage = throwable.message ?: "Failed to update profile photo.",
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
                        id = user.userId ?: it.id,
                        name = user.name.orEmpty().ifBlank { it.name },
                        phone = user.phone.orEmpty().ifBlank { it.phone },
                    )
                }
            }
        }
    }

    private fun loadAccount() {
        viewModelScope.launch(ioDispatcher) {
            getPassengerAccountUseCase().fold(
                onSuccess = { profile ->
                    _uiState.update {
                        it.withAccount(profile).copy(isLoading = false)
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to load account details.",
                        )
                    }
                },
            )
        }
    }

    private fun loadProfilePhoto() {
        viewModelScope.launch(ioDispatcher) {
            getPassengerProfilePhotoUseCase().onSuccess { bytes ->
                _uiState.update { it.copy(profilePhotoBytes = bytes) }
            }
        }
    }
}

private val PassengerAccountEditableField.label: String
    get() = when (this) {
        PassengerAccountEditableField.Email -> "Email"
        PassengerAccountEditableField.Address -> "Address"
    }

private fun PassengerAccountUiState.withAccount(profile: PassengerAccountProfile): PassengerAccountUiState {
    return copy(
        id = profile.id ?: id,
        publicId = profile.publicId ?: publicId,
        name = profile.name.ifBlank { name },
        phone = profile.phone.ifBlank { phone },
        email = profile.email,
        address = profile.address,
        status = profile.status,
    )
}
