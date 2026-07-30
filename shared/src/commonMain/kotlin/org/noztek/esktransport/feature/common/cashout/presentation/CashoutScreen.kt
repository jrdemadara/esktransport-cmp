package org.noztek.esktransport.feature.common.cashout.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ArrowPath
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.ClipboardDocument
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.QrCode
import com.composables.icons.heroicons.outline.XCircle
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.utils.QrCodeMatrix
import org.noztek.esktransport.feature.common.topup.presentation.formatWalletAmount

private val CashoutPercentOptions = listOf(0.25, 0.50, 0.75, 1.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashoutScreen(
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit = {},
    onCopyReferenceClick: (String) -> Unit = {},
    viewModel: CashoutViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val referenceSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden },
    )

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CashoutHeader(
                onBackClick = onBackClick,
                onHelpClick = onHelpClick,
            )
            CashoutAmountCard(
                amountText = uiState.amountText,
                selectedCashoutPercent = uiState.selectedCashoutPercent,
                currency = uiState.currency,
                availableCashout = uiState.availableCashout,
                minimumWalletBalance = uiState.minimumWalletBalance,
                onAmountChange = viewModel::onAmountChange,
                onPercentClick = viewModel::selectPercentage,
            )
            CashoutStepsCard()
            AppPrimaryButton(
                text = if (uiState.isGenerating) "Generating..." else "Generate Cashout QR",
                onClick = viewModel::generateCashout,
                enabled = uiState.canGenerate,
                height = 48.dp,
                trailingIcon = {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Heroicons.Outline.ArrowPath,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                },
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    uiState.activeCashout?.let { cashout ->
        ModalBottomSheet(
            onDismissRequest = {},
            sheetState = referenceSheetState,
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            CashoutReferenceSheet(
                qrPayload = cashout.qrPayload,
                referenceCode = cashout.referenceCode,
                expiresAt = cashout.expiresAt,
                isCancelling = uiState.isCancelling,
                canCancel = uiState.canCancel,
                onCopyReferenceClick = onCopyReferenceClick,
                onCancelClick = viewModel::cancelCashout,
            )
        }
    }
}

@Composable
private fun CashoutHeader(
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Heroicons.Outline.ArrowLeft,
                contentDescription = "Back",
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = "Cashout Wallet",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(onClick = onHelpClick, modifier = Modifier.size(40.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
private fun CashoutAmountCard(
    amountText: String,
    selectedCashoutPercent: Double?,
    currency: String,
    availableCashout: Double,
    minimumWalletBalance: Double,
    onAmountChange: (String) -> Unit,
    onPercentClick: (Double) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.5.dp,
        shadowElevation = 0.5.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Enter cashout amount",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatWalletAmount(availableCashout, currency),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            AmountInput(
                value = amountText,
                onValueChange = onAmountChange,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CashoutPercentOptions.forEach { percent ->
                    PercentAmountButton(
                        percent = percent,
                        selected = selectedCashoutPercent?.let { percent <= it } == true,
                        enabled = availableCashout > 0.0,
                        onClick = { onPercentClick(percent) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Text(
                text = "Available cashout keeps ${formatWalletAmount(minimumWalletBalance, currency)} minimum balance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AmountInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "₱",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun PercentAmountButton(
    percent: Double,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val lineColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f)
        selected -> activeColor
        else -> activeColor.copy(alpha = 0.28f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "${(percent * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(lineColor, RoundedCornerShape(999.dp)),
            )
        }
    }
}

@Composable
private fun CashoutQrCard(
    qrPayload: String?,
    referenceCode: String?,
    expiresAt: String?,
    onCopyClick: (String) -> Unit,
) {
    val hasReference = !referenceCode.isNullOrBlank()
    val displayReferenceCode = referenceCode?.toReferenceDisplay()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.5.dp,
        shadowElevation = 0.5.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Present this QR at the kiosk",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (hasReference) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.72f).widthIn(max = 220.dp).aspectRatio(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 0.5.dp,
                ) {
                    QrPreview(
                        payload = qrPayload.orEmpty(),
                        modifier = Modifier.fillMaxSize().padding(14.dp),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Heroicons.Outline.Clock,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (expiresAt.isNullOrBlank()) "Expires after generation" else "Expires at ${expiresAt.toReadableDateLabel()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                EmptyQrState()
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Reference Code",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = displayReferenceCode ?: "Generate QR first",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    TextButton(
                        onClick = { displayReferenceCode?.let(onCopyClick) },
                        enabled = hasReference,
                        contentPadding = PaddingValues(horizontal = 10.dp),
                    ) {
                        Icon(
                            imageVector = Heroicons.Outline.ClipboardDocument,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp),
                        )
                        Text(
                            text = "Copy",
                            modifier = Modifier.padding(start = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CashoutReferenceSheet(
    qrPayload: String,
    referenceCode: String,
    expiresAt: String?,
    isCancelling: Boolean,
    canCancel: Boolean,
    onCopyReferenceClick: (String) -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Cashout reference",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Keep this open until the kiosk confirms or cancel this request.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        CashoutQrCard(
            qrPayload = qrPayload,
            referenceCode = referenceCode,
            expiresAt = expiresAt,
            onCopyClick = onCopyReferenceClick,
        )
        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            enabled = canCancel,
            shape = RoundedCornerShape(10.dp),
        ) {
            if (isCancelling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(17.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    imageVector = Heroicons.Outline.XCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = if (isCancelling) "Cancelling..." else "Cancel Cashout",
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun EmptyQrState() {
    Surface(
        modifier = Modifier.fillMaxWidth().height(82.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "No QR generated",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Enter an amount, then generate a kiosk reference.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CashoutStepsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.5.dp,
        shadowElevation = 0.5.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            StepRow(
                number = "1",
                title = "Enter amount",
                description = "Set the amount you want to withdraw.",
            )
            StepDivider()
            StepRow(
                icon = Heroicons.Outline.QrCode,
                title = "Show QR or reference code",
                description = "Present this to the kiosk cashier.",
            )
            StepDivider()
            StepRow(
                icon = Heroicons.Outline.CheckCircle,
                title = "Receive cash",
                description = "The kiosk confirms and your wallet ledger updates.",
            )
        }
    }
}

@Composable
private fun StepRow(
    title: String,
    description: String,
    number: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(30.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = if (number == null) 0.10f else 1f),
            contentColor = if (number == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (number != null) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StepDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
    )
}

@Composable
private fun QrPreview(
    payload: String,
    modifier: Modifier = Modifier,
) {
    val matrix = remember(payload) {
        runCatching { QrCodeMatrix.encode(payload) }.getOrDefault(emptyList())
    }

    Canvas(modifier = modifier) {
        if (matrix.isEmpty()) return@Canvas

        val quietZone = 4
        val modules = matrix.size + quietZone * 2
        val cell = size.minDimension / modules
        val origin = Offset(
            x = (size.width - cell * modules) / 2f,
            y = (size.height - cell * modules) / 2f,
        )

        drawRect(Color.White, topLeft = Offset.Zero, size = size)
        matrix.forEachIndexed { row, modulesRow ->
            modulesRow.forEachIndexed { column, isDark ->
                if (isDark) {
                    drawRect(
                        color = Color.Black,
                        topLeft = origin + Offset((column + quietZone) * cell, (row + quietZone) * cell),
                        size = Size(cell, cell),
                    )
                }
            }
        }
    }
}

private fun String.toReadableDateLabel(): String {
    return replace("T", " ")
        .replace("Z", "")
        .take(16)
}

private fun String.toReferenceDisplay(): String {
    val cleaned = filter { it.isLetterOrDigit() }.uppercase()
    if (cleaned.isBlank()) return this

    val prefix = cleaned.takeWhile { it.isLetter() }.ifBlank { "ESK" }
    val body = cleaned.removePrefix(prefix)
    return if (body.isBlank()) {
        prefix
    } else {
        "$prefix-${body.chunked(4).joinToString("-")}"
    }
}
