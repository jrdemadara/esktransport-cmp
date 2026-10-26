package org.noztek.esktransport.feature.driver.bookings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
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
    var quoteAmount by remember { mutableStateOf("") }

    LaunchedEffect(requestPublicId) { viewModel.load(requestPublicId) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text("Request details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Heroicons.Outline.ArrowLeft, "Back") } },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(padding))
            state.request != null -> RequestDetailsContent(
                request = state.request!!,
                isUpdating = state.isUpdating,
                modifier = Modifier.padding(padding),
                onChat = onChat,
                onAccept = { quoteAmount = ""; showQuoteDialog = true },
                onStart = viewModel::start,
                onComplete = viewModel::complete,
            )
            else -> Text(state.errorMessage ?: "Unable to load request.", modifier = Modifier.padding(padding))
        }
    }

    if (showQuoteDialog) {
        AlertDialog(
            onDismissRequest = { showQuoteDialog = false },
            title = { Text("Send a quote") },
            text = { OutlinedTextField(value = quoteAmount, onValueChange = { quoteAmount = it }, label = { Text("Amount") }, singleLine = true) },
            confirmButton = {
                TextButton(enabled = quoteAmount.toDoubleOrNull() != null, onClick = { quoteAmount.toDoubleOrNull()?.let { viewModel.accept(it); showQuoteDialog = false } }) { Text("Send") }
            },
            dismissButton = { TextButton(onClick = { showQuoteDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun RequestDetailsContent(
    request: DriverMarketplaceRequest,
    isUpdating: Boolean,
    modifier: Modifier,
    onChat: (Long, String, String) -> Unit,
    onAccept: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(request.customerName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(request.workflowStatus.replace('_', ' ').uppercaseFirstLetterOfEachWord(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        DetailLine("Service", request.serviceType.replace('_', ' ').uppercaseFirstLetterOfEachWord())
        DetailLine("Vehicle", listOfNotNull(request.vehicle.make, request.vehicle.model, request.vehicle.plate).joinToString(" "))
        DetailLine("Pickup", request.pickupLabel ?: "-")
        DetailLine("Destination", request.destinationLabel ?: "-")
        DetailLine("Requested dates", listOfNotNull(request.requestedStartAt, request.requestedEndAt).joinToString(" to ").ifBlank { "-" })
        request.passengerCount?.let { DetailLine("Passengers", it.toString()) }
        request.cargoDescription?.let { DetailLine("Cargo", it) }
        request.cargoWeightKg?.let { DetailLine("Weight", "$it kg") }
        request.cargoPieces?.let { DetailLine("Pieces", it.toString()) }
        request.customerNote?.let { DetailLine("Customer note", it) }
        request.quotedAmount?.let { DetailLine("Quote", "${request.currency} $it") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            request.threadId?.let { threadId ->
                OutlinedButton(onClick = { onChat(threadId, request.customerName, request.serviceType) }, enabled = !isUpdating) {
                    Icon(Heroicons.Outline.ChatBubbleOvalLeft, null)
                    Text("Chat", modifier = Modifier.padding(start = 6.dp))
                }
            }
            when (request.workflowStatus) {
                "awaiting_driver_review" -> Button(onClick = onAccept, enabled = !isUpdating) { Text("Accept") }
                "confirmed" -> Button(onClick = onStart, enabled = !isUpdating) { Text("Start") }
                "in_progress" -> Button(onClick = onComplete, enabled = !isUpdating) { Text("Complete") }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
