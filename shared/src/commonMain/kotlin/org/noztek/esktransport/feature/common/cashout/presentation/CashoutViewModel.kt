package org.noztek.esktransport.feature.common.cashout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noztek.esktransport.feature.common.topup.presentation.formatWalletAmount
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CancelDriverCashoutUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.CreateDriverCashoutUseCase
import org.noztek.esktransport.feature.driver.wallet.domain.usecase.GetDriverWalletUseCase
import kotlin.math.round

class CashoutViewModel(
    private val getDriverWalletUseCase: GetDriverWalletUseCase,
    private val createDriverCashoutUseCase: CreateDriverCashoutUseCase,
    private val cancelDriverCashoutUseCase: CancelDriverCashoutUseCase,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CashoutUiState())
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
                            minimumWalletBalance = dashboard.driverModeRequirement.minimumWalletBalance,
                            currency = dashboard.wallet.currency,
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
                selectedCashoutPercent = null,
                errorMessage = null,
            )
        }
    }

    fun selectPercentage(percent: Double) {
        val availableCashout = _uiState.value.availableCashout
        val amount = round((availableCashout * percent).coerceAtLeast(0.0) * 100.0) / 100.0
        _uiState.update {
            it.copy(
                amountText = amount.toAmountInput(),
                selectedCashoutPercent = percent,
                errorMessage = null,
            )
        }
    }

    fun generateCashout() {
        val state = _uiState.value
        val amount = state.amountText.toDoubleOrNull()
        when {
            amount == null || amount < CashoutMinimumAmount -> {
                _uiState.update {
                    it.copy(errorMessage = "Minimum cashout is ${formatWalletAmount(CashoutMinimumAmount, state.currency)}.")
                }
                return
            }
            amount > state.availableCashout -> {
                _uiState.update {
                    it.copy(errorMessage = "Available cashout is ${formatWalletAmount(state.availableCashout, state.currency)}.")
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, errorMessage = null, statusMessage = null) }
            val result = withContext(ioDispatcher) {
                createDriverCashoutUseCase(amount = amount, currency = state.currency)
            }
            result
                .onSuccess { cashout ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            activeCashout = cashout,
                            statusMessage = "Cashout reference is ready.",
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            errorMessage = throwable.message ?: "Unable to create cashout request.",
                        )
                    }
                }
        }
    }

    fun cancelCashout() {
        val referenceCode = _uiState.value.activeCashout?.referenceCode ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, errorMessage = null, statusMessage = null) }
            val result = withContext(ioDispatcher) { cancelDriverCashoutUseCase(referenceCode = referenceCode) }
            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            activeCashout = null,
                            statusMessage = "Cashout reference cancelled.",
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            errorMessage = throwable.message ?: "Unable to cancel cashout request.",
                        )
                    }
                }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
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
