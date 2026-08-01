package org.noztek.esktransport.feature.passenger.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noztek.esktransport.feature.passenger.home.domain.usecase.GetKnownPlacesUseCase
import org.noztek.esktransport.feature.passenger.settings.domain.usecase.GetSavedPlacesUseCase

class PassengerHomeViewModel(
    private val getSavedPlacesUseCase: GetSavedPlacesUseCase,
    private val getKnownPlacesUseCase: GetKnownPlacesUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PassengerHomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val savedPlacesResult = async { getSavedPlacesUseCase() }
            val knownPlacesResult = async { getKnownPlacesUseCase() }
            val savedPlaces = savedPlacesResult.await()
            val knownPlaces = knownPlacesResult.await()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    savedPlaces = savedPlaces.getOrDefault(emptyList()),
                    knownPlaces = knownPlaces.getOrDefault(emptyList()),
                    errorMessage = savedPlaces.exceptionOrNull()?.message ?: knownPlaces.exceptionOrNull()?.message,
                )
            }
        }
    }
}
