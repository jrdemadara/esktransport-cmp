package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.DocumentText
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceServiceRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceRequestsScreen(
    onBackClick: () -> Unit = {},
    viewModel: MarketplaceRequestsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My requests") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Heroicons.Outline.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading && state.requests.isEmpty() -> {
                Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null && state.requests.isEmpty() -> {
                Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
                    Text(state.errorMessage ?: "Unable to load requests.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = viewModel::refresh) { Text("Retry") }
                }
            }
            state.requests.isEmpty() -> {
                Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Heroicons.Outline.DocumentText, contentDescription = null)
                    Spacer(Modifier.height(8.dp))
                    Text("No requests yet", fontWeight = FontWeight.SemiBold)
                    Text("Your rental and cargo requests will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.requests, key = { it.publicId }) { request ->
                        MarketplaceRequestCard(
                            request = request,
                            isUpdating = state.updatingRequestId == request.publicId,
                            onConfirm = { viewModel.confirm(request) },
                            onCancel = { viewModel.cancel(request) },
                            onComplete = { viewModel.complete(request) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketplaceRequestCard(
    request: MarketplaceServiceRequest,
    isUpdating: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = request.listingTitle?.takeIf { it.isNotBlank() } ?: "Vehicle request",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = listOfNotNull(
                    request.serviceType.replace('_', ' ').uppercaseFirstLetterOfEachWord(),
                    request.ownerName?.takeIf { it.isNotBlank() }?.let { "Owner: $it" },
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = request.workflowStatus.replace('_', ' ').uppercaseFirstLetterOfEachWord(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
            request.quotedAmount?.let {
                Text("Quote: ${request.currency} ${"%.2f".format(it)}")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (request.workflowStatus) {
                    "awaiting_passenger_confirmation" -> {
                        Button(onClick = onConfirm, enabled = !isUpdating) { Text("Confirm") }
                        OutlinedButton(onClick = onCancel, enabled = !isUpdating) { Text("Cancel") }
                    }
                    "driver_completed" -> Button(onClick = onComplete, enabled = !isUpdating) { Text("Confirm cash paid") }
                    "awaiting_driver_review", "confirmed" -> OutlinedButton(onClick = onCancel, enabled = !isUpdating) { Text("Cancel") }
                }
            }
        }
    }
}
