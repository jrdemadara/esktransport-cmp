package org.noztek.esktransport.feature.driver.bookings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.Briefcase
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.Truck
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.utils.formatApiDateTimeForDisplay
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceInquiry

@Composable
fun DriverBookingsScreen(
    onBottomBarNavigate: (String) -> Unit = {},
    onInquiryClick: (DriverMarketplaceInquiry) -> Unit = {},
    viewModel: DriverBookingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var quoteRequestId by remember { mutableStateOf<String?>(null) }
    var quoteAmount by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.subscribe()
        viewModel.load()
    }

    Scaffold(
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.BOOKINGS,
                onNavigate = onBottomBarNavigate,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DriverBookingsHeader()
            BookingFilterRow(
                selectedFilter = uiState.selectedFilter,
                onFilterClick = viewModel::selectFilter,
            )
            BookingStatusSummary(inquiries = uiState.inquiries)
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.dp)
                    }
                }
                uiState.errorMessage != null -> {
                    BookingsMessageState(
                        title = "Unable to load bookings",
                        message = uiState.errorMessage.orEmpty(),
                        icon = Heroicons.Outline.Briefcase,
                    )
                }
                uiState.visibleInquiries.isEmpty() -> {
                    BookingsMessageState(
                        title = "No rental or cargo messages",
                        message = "Customer inquiries will show here.",
                        icon = Heroicons.Outline.ChatBubbleOvalLeft,
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.visibleInquiries.forEach { inquiry ->
                            DriverMarketplaceInquiryCard(
                                inquiry = inquiry,
                                isAccepting = uiState.acceptingRequestPublicId == inquiry.vehicleServiceRequestPublicId,
                                onAcceptClick = { requestId ->
                                    quoteRequestId = requestId
                                    quoteAmount = ""
                                },
                                onStartClick = viewModel::startRequest,
                                onCompleteClick = viewModel::completeRequest,
                                onClick = { onInquiryClick(inquiry) },
                            )
                        }
                    }
                }
            }
        }
    }

    quoteRequestId?.let { requestId ->
        AlertDialog(
            onDismissRequest = { quoteRequestId = null },
            title = { Text("Send a quote") },
            text = {
                OutlinedTextField(
                    value = quoteAmount,
                    onValueChange = { quoteAmount = it },
                    label = { Text("Amount") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = quoteAmount.toDoubleOrNull() != null,
                    onClick = {
                        quoteAmount.toDoubleOrNull()?.let { amount ->
                            quoteRequestId = null
                            viewModel.acceptRequest(requestId, amount)
                        }
                    },
                ) { Text("Send") }
            },
            dismissButton = { TextButton(onClick = { quoteRequestId = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun DriverBookingsHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Bookings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
        )
        Text(
            text = "Rental and cargo requests",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BookingFilterRow(
    selectedFilter: DriverBookingFilter,
    onFilterClick: (DriverBookingFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DriverBookingFilter.entries.forEach { filter ->
            BookingModePill(
                label = filter.label,
                icon = when (filter) {
                    DriverBookingFilter.All -> Heroicons.Outline.Briefcase
                    DriverBookingFilter.Rental -> Heroicons.Outline.CalendarDays
                    DriverBookingFilter.Cargo -> Heroicons.Outline.Truck
                },
                selected = selectedFilter == filter,
                onClick = { onFilterClick(filter) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BookingModePill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        border = if (selected) {
            null
        } else {
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
            Text(
                text = label,
                modifier = Modifier.padding(start = 7.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun BookingStatusSummary(inquiries: List<DriverMarketplaceInquiry>) {
    val rentals = inquiries.count { it.serviceType == "rental" }
    val cargo = inquiries.count { it.serviceType == "cargo" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.34f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookingSummaryCell(
                label = "Requests",
                value = inquiries.size.toString(),
                icon = Heroicons.Outline.Briefcase,
                modifier = Modifier.weight(1f),
            )
            BookingSummaryCell(
                label = "Rental",
                value = rentals.toString(),
                icon = Heroicons.Outline.CalendarDays,
                modifier = Modifier.weight(1f),
            )
            BookingSummaryCell(
                label = "Cargo",
                value = cargo.toString(),
                icon = Heroicons.Outline.Truck,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BookingSummaryCell(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DriverMarketplaceInquiryCard(
    inquiry: DriverMarketplaceInquiry,
    isAccepting: Boolean,
    onAcceptClick: (String) -> Unit,
    onStartClick: (String) -> Unit,
    onCompleteClick: (String) -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (inquiry.serviceType == "cargo") {
                        Heroicons.Outline.Truck
                    } else {
                        Heroicons.Outline.CalendarDays
                    },
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = inquiry.customer.name.displayName(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = inquiry.vehicleLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = inquiry.latestSentAt.formatApiDateTimeForDisplay(
                        fallback = inquiry.updatedAt.formatApiDateTimeForDisplay(""),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                if (!inquiry.canAcceptRequest() && inquiry.requestStatusLabel().isNotBlank()) {
                    Text(
                        text = inquiry.requestStatusLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            if (inquiry.canAcceptRequest()) {
                Button(
                    onClick = { inquiry.vehicleServiceRequestPublicId?.let(onAcceptClick) },
                    enabled = !isAccepting,
                    modifier = Modifier.size(width = 84.dp, height = 32.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text(
                        text = if (isAccepting) "..." else "Accept",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            } else if (inquiry.vehicleServiceRequestWorkflowStatus == "confirmed") {
                Button(onClick = { inquiry.vehicleServiceRequestPublicId?.let(onStartClick) }, enabled = !isAccepting, modifier = Modifier.size(width = 84.dp, height = 32.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 6.dp)) { Text("Start", style = MaterialTheme.typography.labelMedium) }
            } else if (inquiry.vehicleServiceRequestWorkflowStatus == "in_progress") {
                Button(onClick = { inquiry.vehicleServiceRequestPublicId?.let(onCompleteClick) }, enabled = !isAccepting, modifier = Modifier.size(width = 84.dp, height = 32.dp), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 6.dp)) { Text("Complete", style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

@Composable
private fun BookingsMessageState(
    title: String,
    message: String,
    icon: ImageVector,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun DriverMarketplaceInquiry.canAcceptRequest(): Boolean {
    return vehicleServiceRequestPublicId != null && vehicleServiceRequestStatus == "pending_review"
}

private fun DriverMarketplaceInquiry.requestStatusLabel(): String {
    return when (vehicleServiceRequestStatus) {
        "accepted" -> "Accepted"
        "rejected" -> "Rejected"
        "canceled" -> "Canceled"
        "completed" -> "Completed"
        else -> ""
    }
}

fun DriverMarketplaceInquiry.chatTitle(): String {
    return customer.name.displayName().ifBlank { "Customer message" }
}

fun DriverMarketplaceInquiry.chatSubtitle(): String {
    return listOf(
        serviceLabel(),
        vehicleLabel(),
    )
        .filter { it.isNotBlank() }
        .joinToString(" • ")
        .ifBlank { "Marketplace inquiry" }
}

private fun DriverMarketplaceInquiry.serviceLabel(): String {
    return serviceType
        ?.replace("_", " ")
        ?.displayName()
        ?.ifBlank { null }
        ?: "Inquiry"
}

private fun DriverMarketplaceInquiry.vehicleLabel(): String {
    val name = listOfNotNull(vehicle.make, vehicle.model)
        .joinToString(" ")
        .trim()
        .displayName()
    val type = vehicle.vehicleTypeCode?.replace("_", " ")?.displayName().orEmpty()
    val plate = vehicle.plate?.trim().orEmpty()

    return listOf(name.ifBlank { type }, plate)
        .filter { it.isNotBlank() }
        .joinToString(" • ")
        .ifBlank { "Vehicle inquiry" }
}

private fun String.displayName(): String {
    return trim().uppercaseFirstLetterOfEachWord()
}
