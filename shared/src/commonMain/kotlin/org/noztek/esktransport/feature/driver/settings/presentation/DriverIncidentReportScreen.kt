package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.ExclamationTriangle
import com.composables.icons.heroicons.outline.PaperAirplane
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.utils.formatApiDateTimeForDisplay
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentCategory
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentReport
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverIncidentUrgency

@Composable
fun DriverIncidentReportScreen(
    onBackClick: () -> Unit,
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverIncidentReportViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        val message = uiState.errorMessage ?: uiState.successMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    DriverIncidentReportContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onCategoryClick = viewModel::selectCategory,
        onUrgencyClick = viewModel::selectUrgency,
        onBookingReferenceChange = viewModel::updateBookingReference,
        onDetailsChange = viewModel::updateDetails,
        onSubmitClick = viewModel::submit,
        onBottomBarNavigate = onBottomBarNavigate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverIncidentReportContent(
    uiState: DriverIncidentReportUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onCategoryClick: (DriverIncidentCategory) -> Unit,
    onUrgencyClick: (DriverIncidentUrgency) -> Unit,
    onBookingReferenceChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onBottomBarNavigate: (String) -> Unit,
) {
    Scaffold(
        topBar = { IncidentReportTopBar(onBackClick = onBackClick) },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.PROFILE,
                onNavigate = onBottomBarNavigate,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IncidentIntroCard()
            IncidentFormCard(
                uiState = uiState,
                onCategoryClick = onCategoryClick,
                onUrgencyClick = onUrgencyClick,
                onBookingReferenceChange = onBookingReferenceChange,
                onDetailsChange = onDetailsChange,
                onSubmitClick = onSubmitClick,
            )
            RecentReportsCard(reports = uiState.reports)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncidentReportTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Heroicons.Outline.ArrowLeft,
                    contentDescription = "Back",
                    modifier = Modifier.size(22.dp),
                )
            }
        },
        title = {
            Text(
                text = "Report Incident",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun IncidentIntroCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.error,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.ExclamationTriangle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Send a safety report",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Use this for trip, passenger, vehicle, or payment concerns.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun IncidentFormCard(
    uiState: DriverIncidentReportUiState,
    onCategoryClick: (DriverIncidentCategory) -> Unit,
    onUrgencyClick: (DriverIncidentUrgency) -> Unit,
    onBookingReferenceChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Incident details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            ChoiceSection(title = "Category") {
                ChipRows(
                    options = DriverIncidentCategory.entries,
                    selected = uiState.category,
                    label = { it.label },
                    onSelect = onCategoryClick,
                )
            }
            ChoiceSection(title = "Urgency") {
                ChipRows(
                    options = DriverIncidentUrgency.entries,
                    selected = uiState.urgency,
                    label = { it.label },
                    onSelect = onUrgencyClick,
                )
            }
            OutlinedTextField(
                value = uiState.bookingReference,
                onValueChange = onBookingReferenceChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
                singleLine = true,
                label = { Text("Trip reference optional") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
            )
            OutlinedTextField(
                value = uiState.details,
                onValueChange = onDetailsChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting,
                minLines = 4,
                maxLines = 6,
                label = { Text("What happened?") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                shape = RoundedCornerShape(12.dp),
            )
            uiState.formErrorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            AppPrimaryButton(
                text = if (uiState.isSubmitting) "Submitting..." else "Submit report",
                onClick = onSubmitClick,
                enabled = !uiState.isSubmitting,
                height = 46.dp,
                contentPadding = PaddingValues(horizontal = 16.dp),
                trailingIcon = {
                    Icon(
                        imageVector = Heroicons.Outline.PaperAirplane,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun ChoiceSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun <T> ChipRows(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.chunked(3).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowOptions.forEach { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { onSelect(option) },
                        label = {
                            Text(
                                text = label(option),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentReportsCard(reports: List<DriverIncidentReport>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Recent reports",
                modifier = Modifier.padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            if (reports.isEmpty()) {
                Text(
                    text = "No incident reports yet.",
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                reports.take(5).forEachIndexed { index, report ->
                    IncidentReportRow(report = report)
                    if (index < reports.take(5).lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 52.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncidentReportRow(report: DriverIncidentReport) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = report.icon(),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = report.category.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = report.statusLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = report.statusColor(),
                )
            }
            Text(
                text = report.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = report.createdAt.formatApiDateTimeForDisplay(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun DriverIncidentReport.icon(): ImageVector {
    return when (category) {
        DriverIncidentCategory.Trip -> Heroicons.Outline.Clock
        DriverIncidentCategory.Passenger -> Heroicons.Outline.ExclamationTriangle
        DriverIncidentCategory.Payment -> Heroicons.Outline.DocumentText
        DriverIncidentCategory.Vehicle -> Heroicons.Outline.DocumentText
        DriverIncidentCategory.Safety -> Heroicons.Outline.ExclamationTriangle
        DriverIncidentCategory.Other -> Heroicons.Outline.DocumentText
    }
}

@Composable
private fun DriverIncidentReport.statusColor(): Color {
    return when (status) {
        "resolved" -> Color(0xFF16A34A)
        "in_review" -> Color(0xFFD97706)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun DriverIncidentReport.statusLabel(): String {
    return when (status) {
        "in_review" -> "In review"
        "resolved" -> "Resolved"
        "dismissed" -> "Closed"
        else -> "Open"
    }
}
