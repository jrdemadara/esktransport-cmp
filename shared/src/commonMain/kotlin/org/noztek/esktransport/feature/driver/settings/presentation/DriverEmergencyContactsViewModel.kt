package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContact
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContactPayload
import org.noztek.esktransport.feature.driver.settings.domain.usecase.DeleteDriverEmergencyContactUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.GetDriverEmergencyContactsUseCase
import org.noztek.esktransport.feature.driver.settings.domain.usecase.SaveDriverEmergencyContactUseCase

data class DriverEmergencyContactsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val contacts: List<DriverEmergencyContact> = emptyList(),
    val editor: DriverEmergencyContactEditorState? = null,
    val errorMessage: String? = null,
)

data class DriverEmergencyContactEditorState(
    val contactId: Long? = null,
    val name: String = "",
    val phone: String = "",
    val relationship: String = "",
    val errorMessage: String? = null,
) {
    val isEditing: Boolean
        get() = contactId != null
}

class DriverEmergencyContactsViewModel(
    private val getDriverEmergencyContactsUseCase: GetDriverEmergencyContactsUseCase,
    private val saveDriverEmergencyContactUseCase: SaveDriverEmergencyContactUseCase,
    private val deleteDriverEmergencyContactUseCase: DeleteDriverEmergencyContactUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverEmergencyContactsUiState())
    val uiState: StateFlow<DriverEmergencyContactsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { getDriverEmergencyContactsUseCase() }
            result.fold(
                onSuccess = { contacts ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            contacts = contacts,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load emergency contacts.",
                        )
                    }
                },
            )
        }
    }

    fun startAddContact() {
        if (_uiState.value.contacts.size >= MAX_CONTACTS) {
            _uiState.update { it.copy(errorMessage = "You can add up to $MAX_CONTACTS emergency contacts.") }
            return
        }
        _uiState.update { it.copy(editor = DriverEmergencyContactEditorState(), errorMessage = null) }
    }

    fun startEditContact(contact: DriverEmergencyContact) {
        _uiState.update {
            it.copy(
                editor = DriverEmergencyContactEditorState(
                    contactId = contact.id,
                    name = contact.name,
                    phone = contact.phone,
                    relationship = contact.relationship,
                ),
                errorMessage = null,
            )
        }
    }

    fun dismissEditor() {
        _uiState.update { it.copy(editor = null) }
    }

    fun updateEditorName(value: String) {
        updateEditor { it.copy(name = value, errorMessage = null) }
    }

    fun updateEditorPhone(value: String) {
        updateEditor { it.copy(phone = value, errorMessage = null) }
    }

    fun updateEditorRelationship(value: String) {
        updateEditor { it.copy(relationship = value, errorMessage = null) }
    }

    fun saveEditor() {
        val editor = _uiState.value.editor ?: return
        val validationError = editor.validationError()
        if (validationError != null) {
            updateEditor { it.copy(errorMessage = validationError) }
            return
        }
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = withContext(ioDispatcher) {
                saveDriverEmergencyContactUseCase(
                    DriverEmergencyContactPayload(
                        id = editor.contactId,
                        name = editor.name,
                        phone = editor.phone,
                        relationship = editor.relationship,
                    ),
                )
            }
            result.fold(
                onSuccess = { contacts ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            contacts = contacts,
                            editor = null,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Unable to save emergency contact.",
                        )
                    }
                },
            )
        }
    }

    fun deleteContact(contact: DriverEmergencyContact) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { deleteDriverEmergencyContactUseCase(contact.id) }
            result.fold(
                onSuccess = { contacts ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            contacts = contacts,
                            editor = null,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Unable to delete emergency contact.",
                        )
                    }
                },
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun updateEditor(transform: (DriverEmergencyContactEditorState) -> DriverEmergencyContactEditorState) {
        _uiState.update { state ->
            state.copy(editor = state.editor?.let(transform))
        }
    }

    private fun DriverEmergencyContactEditorState.validationError(): String? {
        return when {
            name.isBlank() -> "Enter the contact name."
            phone.isBlank() -> "Enter the phone number."
            relationship.isBlank() -> "Enter the relationship."
            else -> null
        }
    }

    private companion object {
        private const val MAX_CONTACTS = 3
    }
}
