package org.noztek.esktransport.feature.driver.bookings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.Cube
import com.composables.icons.heroicons.outline.CurrencyDollar
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.User
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.core.utils.formatApiDateForDisplay
import org.noztek.esktransport.feature.driver.bookings.domain.model.DriverMarketplaceRequest

@Composable
fun DriverMarketplaceRequestDetailsScreen(
    requestPublicId: String,
    onBack: () -> Unit,
    onChat: (threadId: Long, title: String, subtitle: String) -> Unit,
    viewModel: DriverMarketplaceRequestDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showQuoteDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var quoteAmount by remember { mutableStateOf("") }
    val request = state.request

    LaunchedEffect(requestPublicId) { viewModel.load(requestPublicId) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Request details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Heroicons.Outline.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            request?.let {
                RequestActionBar(
                    request = it,
                    isUpdating = state.isUpdating,
                    onChat = {
                        it.threadId?.let { threadId -> onChat(threadId, it.customerName, it.serviceType) }
                    },
                    onAccept = { quoteAmount = ""; showQuoteDialog = true },
                    onReject = { showRejectDialog = true },
                    onStart = viewModel::start,
                    onComplete = viewModel::complete,
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            request != null -> RequestDetailsContent(request, Modifier.padding(padding))
            else -> RequestErrorState(state.errorMessage ?: "Unable to load request.", Modifier.padding(padding))
        }
    }

    if (showQuoteDialog) {
        AlertDialog(
            onDismissRequest = { showQuoteDialog = false },
            title = { Text("Send a quote") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the total amount for this service.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = quoteAmount,
                        onValueChange = { quoteAmount = it.filter { character -> character.isDigit() || character == '.' } },
                        label = { Text("Amount") },
                        prefix = { Text("PHP ") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = quoteAmount.toDoubleOrNull() != null,
                    onClick = { quoteAmount.toDoubleOrNull()?.let { viewModel.accept(it); showQuoteDialog = false } },
                ) { Text("Send quote") }
            },
            dismissButton = { TextButton(onClick = { showQuoteDialog = false }) { Text("Cancel") } },
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject request?") },
            text = { Text("The customer will see that this request was rejected." ) },
            confirmButton = {
                TextButton(onClick = { viewModel.reject(); showRejectDialog = false }) { Text("Reject") }
            },
            dismissButton = { TextButton(onClick = { showRejectDialog = false }) { Text("Keep request") } },
        )
    }
}

@Composable
private fun RequestDetailsContent(request: DriverMarketplaceRequest, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RequestHeader(request)
        RequestSectionCard("Customer", Heroicons.Outline.User) {
            Text(request.customerName.uppercaseFirstLetterOfEachWord(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            request.customerPhone?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        RequestSectionCard("Service and vehicle", if (request.serviceType == "cargo") Heroicons.Outline.Cube else Heroicons.Outline.CalendarDays) {
            DetailRow("Service", request.serviceType.replace('_', ' ').uppercaseFirstLetterOfEachWord())
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            DetailRow("Vehicle", listOfNotNull(
                request.vehicle.make?.uppercaseFirstLetterOfEachWord(),
                request.vehicle.model?.uppercaseFirstLetterOfEachWord(),
                request.vehicle.plate?.uppercase(),
            ).joinToString(" ").ifBlank { "-" })
        }
        RequestSectionCard("Route and schedule", Heroicons.Outline.MapPin) {
            RouteRow(Heroicons.Outline.MapPin, "Pickup", request.pickupLabel ?: "Not provided")
            RouteRow(Heroicons.Outline.MapPin, "Destination", request.destinationLabel ?: "Not provided")
            DetailRow("Requested dates", listOfNotNull(
                request.requestedStartAt.formatApiDateForDisplay(fallback = ""),
                request.requestedEndAt.formatApiDateForDisplay(fallback = ""),
            ).joinToString(" to ").ifBlank { "Not provided" })
        }
        if (request.passengerCount != null || request.cargoDescription != null || request.cargoWeightKg != null || request.cargoPieces != null) {
            RequestSectionCard(if (request.serviceType == "cargo") "Cargo details" else "Passenger details", if (request.serviceType == "cargo") Heroicons.Outline.Truck else Heroicons.Outline.User) {
                request.passengerCount?.let { DetailRow("Passengers", it.toString()) }
                request.cargoDescription?.let { DetailRow("Description", it) }
                request.cargoWeightKg?.let { DetailRow("Weight", "$it kg") }
                request.cargoPieces?.let { DetailRow("Pieces", it.toString()) }
            }
        }
        request.customerNote?.takeIf { it.isNotBlank() }?.let {
            RequestSectionCard("Customer note", Heroicons.Outline.ChatBubbleOvalLeft) {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        request.quotedAmount?.let {
            RequestSectionCard("Quote", Heroicons.Outline.CurrencyDollar) {
                Text("${request.currency} $it", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    if (request.workflowStatus == "awaiting_passenger_confirmation") "Waiting for customer confirmation" else "Final service amount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(88.dp))
    }
}

@Composable
private fun RequestHeader(request: DriverMarketplaceRequest) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (request.serviceType == "cargo") Heroicons.Outline.Truck else Heroicons.Outline.CalendarDays,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatusPill(request.workflowStatus)
            Text(request.customerName.uppercaseFirstLetterOfEachWord(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                request.listingTitle.uppercaseFirstLetterOfEachWord().ifBlank { request.serviceType.replace('_', ' ').uppercaseFirstLetterOfEachWord() },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f), contentColor = MaterialTheme.colorScheme.primary) {
        Text(status.replace('_', ' ').uppercaseFirstLetterOfEachWord(), modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun RequestSectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, contentColor = MaterialTheme.colorScheme.onSurface) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Text(title, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
        Text(label, modifier = Modifier.width(108.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RouteRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(top = 2.dp).size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun RequestActionBar(request: DriverMarketplaceRequest, isUpdating: Boolean, onChat: () -> Unit, onAccept: () -> Unit, onReject: () -> Unit, onStart: () -> Unit, onComplete: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp, shadowElevation = 3.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            request.threadId?.let {
                OutlinedButton(onClick = onChat, enabled = !isUpdating, modifier = Modifier.height(40.dp).width(48.dp), contentPadding = PaddingValues(0.dp), shape = RoundedCornerShape(8.dp)) {
                    Icon(Heroicons.Outline.ChatBubbleOvalLeft, contentDescription = "Chat")
                }
            }
            when (request.workflowStatus) {
                "awaiting_driver_review" -> {
                    OutlinedButton(onClick = onReject, enabled = !isUpdating, modifier = Modifier.weight(0.8f), shape = RoundedCornerShape(8.dp)) { Text("Reject") }
                    Button(onClick = onAccept, enabled = !isUpdating, modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(8.dp)) { Text("Accept with quote") }
                }
                "confirmed" -> Button(onClick = onStart, enabled = !isUpdating, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Start service") }
                "in_progress" -> Button(onClick = onComplete, enabled = !isUpdating, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Mark complete") }
                else -> Text("No action required", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RequestErrorState(message: String, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Heroicons.Outline.CheckCircle, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
