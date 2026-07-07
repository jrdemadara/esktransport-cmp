package org.noztek.esktransport.feature.common.topup.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.composables.icons.heroicons.outline.Share
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import kotlin.math.abs

private val TopUpPresets = listOf(100.0, 200.0, 500.0, 1000.0)

@Composable
fun TopUpScreen(
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit = {},
    onCopyReferenceClick: (String) -> Unit = {},
    onShareReferenceClick: (String) -> Unit = {},
    viewModel: TopUpViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
            TopUpHeader(
                onBackClick = onBackClick,
                onHelpClick = onHelpClick,
            )
            AmountCard(
                amountText = uiState.amountText,
                selectedPresetAmount = uiState.selectedPresetAmount,
                onAmountChange = viewModel::onAmountChange,
                onPresetClick = viewModel::selectPreset,
            )
            KioskQrCard(
                qrPayload = uiState.activeTopup?.qrPayload,
                referenceCode = uiState.activeTopup?.referenceCode,
                expiresAt = uiState.activeTopup?.expiresAt,
                onCopyClick = onCopyReferenceClick,
            )
            TopUpStepsCard()
            AppPrimaryButton(
                text = if (uiState.isGenerating) "Generating..." else "Generate New QR",
                onClick = viewModel::generateTopup,
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
            OutlinedButton(
                onClick = { uiState.activeTopup?.referenceCode?.toReferenceDisplay()?.let(onShareReferenceClick) },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                enabled = uiState.activeTopup != null,
                shape = RoundedCornerShape(10.dp),
            ) {
                Icon(
                    imageVector = Heroicons.Outline.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Share Reference",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun TopUpHeader(
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
            text = "Top Up Wallet",
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
private fun AmountCard(
    amountText: String,
    selectedPresetAmount: Double,
    onAmountChange: (String) -> Unit,
    onPresetClick: (Double) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Enter top up amount",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AmountInput(
                value = amountText,
                onValueChange = onAmountChange,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TopUpPresets.forEach { amount ->
                    PresetAmountButton(
                        amount = amount,
                        selected = selectedPresetAmount == amount,
                        onClick = { onPresetClick(amount) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Text(
                text = "Minimum top up ${formatWalletAmount(TopUpMinimumAmount, "PHP")}",
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
private fun PresetAmountButton(
    amount: Double,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = formatWalletAmount(amount, "PHP").replace(".00", ""),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun KioskQrCard(
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
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
                    modifier = Modifier.width(172.dp).aspectRatio(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
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
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
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
                            style = MaterialTheme.typography.titleLarge,
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
private fun TopUpStepsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            StepRow(
                icon = null,
                number = "1",
                title = "Enter amount",
                description = "Set the amount you want to add.",
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
                title = "Wait for cashier confirmation",
                description = "Your wallet updates after approval.",
            )
        }
    }
}

@Composable
private fun StepRow(
    icon: ImageVector?,
    title: String,
    description: String,
    number: String? = null,
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
    Canvas(modifier = modifier) {
        val modules = 29
        val cell = size.minDimension / modules
        val origin = Offset(
            x = (size.width - cell * modules) / 2f,
            y = (size.height - cell * modules) / 2f,
        )

        drawRect(Color.White, topLeft = Offset.Zero, size = size)
        drawFinder(origin, cell, 0, 0)
        drawFinder(origin, cell, modules - 7, 0)
        drawFinder(origin, cell, 0, modules - 7)

        val seed = payload.hashCode()
        for (row in 0 until modules) {
            for (column in 0 until modules) {
                if (isFinderArea(column, row, modules)) continue
                val value = abs(seed + column * 37 + row * 71 + column * row * 13)
                if (value % 5 == 0 || value % 11 == 0) {
                    drawRect(
                        color = Color.Black,
                        topLeft = origin + Offset(column * cell, row * cell),
                        size = Size(cell * 0.92f, cell * 0.92f),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawFinder(
    origin: Offset,
    cell: Float,
    column: Int,
    row: Int,
) {
    fun square(offset: Int, span: Int, color: Color) {
        drawRect(
            color = color,
            topLeft = origin + Offset((column + offset) * cell, (row + offset) * cell),
            size = Size(span * cell, span * cell),
        )
    }
    square(0, 7, Color.Black)
    square(1, 5, Color.White)
    square(2, 3, Color.Black)
}

private fun isFinderArea(column: Int, row: Int, modules: Int): Boolean {
    return (column < 8 && row < 8) ||
        (column >= modules - 8 && row < 8) ||
        (column < 8 && row >= modules - 8)
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
