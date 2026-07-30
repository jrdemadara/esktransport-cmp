package org.noztek.esktransport.feature.common.topup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CancelDriverTopupUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CreateDriverTopupUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.GetDriverWalletUseCase

class TopUpViewModel(
    private val getDriverWalletUseCase: GetDriverWalletUseCase,
    private val createDriverTopupUseCase: CreateDriverTopupUseCase,
    private val cancelDriverTopupUseCase: CancelDriverTopupUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TopUpUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshWallet()
    }

    fun refreshWallet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = withContext(ioDispatcher) { getDriverWalletUseCase() }
            result
                .onSuccess { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            walletBalance = dashboard.wallet.balance,
                            currency = dashboard.wallet.currency,
                            activeTopup = dashboard.pendingTopups.firstOrNull(),
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load wallet.",
                        )
                    }
                }
        }
    }

    fun onAmountChange(value: String) {
        val normalized = value.toAmountText()
        _uiState.update {
            it.copy(
                amountText = normalized,
                selectedPresetAmount = normalized.toDoubleOrNull() ?: 0.0,
                errorMessage = null,
            )
        }
    }

    fun selectPreset(amount: Double) {
        _uiState.update {
            it.copy(
                amountText = amount.toAmountInput(),
                selectedPresetAmount = amount,
                errorMessage = null,
            )
        }
    }

    fun generateTopup() {
        val state = _uiState.value
        val amount = state.amountText.toDoubleOrNull()
        if (amount == null || amount < TopUpMinimumAmount) {
            _uiState.update { it.copy(errorMessage = "Minimum top up is ${formatWalletAmount(TopUpMinimumAmount, state.currency)}.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, errorMessage = null, statusMessage = null) }
            val result = withContext(ioDispatcher) {
                createDriverTopupUseCase(amount = amount, currency = state.currency)
            }
            result
                .onSuccess { topup ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            activeTopup = topup,
                            statusMessage = "Top up reference is ready.",
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            errorMessage = throwable.message ?: "Unable to create top up request.",
                        )
                    }
                }
        }
    }

    fun cancelTopup() {
        val referenceCode = _uiState.value.activeTopup?.referenceCode ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, errorMessage = null, statusMessage = null) }
            val result = withContext(ioDispatcher) { cancelDriverTopupUseCase(referenceCode = referenceCode) }
            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            activeTopup = null,
                            statusMessage = "Top-up reference cancelled.",
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            errorMessage = throwable.message ?: "Unable to cancel top-up request.",
                        )
                    }
                }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}

internal fun formatWalletAmount(amount: Double, currency: String): String {
    val prefix = if (currency.equals("PHP", ignoreCase = true)) "₱" else "$currency "
    val rounded = (amount * 100).toInt()
    val whole = rounded / 100
    val decimals = (rounded % 100).toString().padStart(2, '0')
    return "$prefix$whole.$decimals"
}

private fun Double.toAmountInput(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}

private fun String.toAmountText(): String {
    val builder = StringBuilder()
    var hasDecimal = false
    var decimalCount = 0

    forEach { char ->
        when {
            char.isDigit() && !hasDecimal -> builder.append(char)
            char.isDigit() && decimalCount < 2 -> {
                builder.append(char)
                decimalCount += 1
            }
            char == '.' && !hasDecimal -> {
                builder.append(char)
                hasDecimal = true
            }
        }
    }

    val value = builder.toString()
    return when {
        value.isBlank() -> ""
        value == "." -> "0."
        value.startsWith("0") && value.length > 1 && value[1] != '.' -> value.trimStart('0').ifBlank { "0" }
        else -> value
    }
}
