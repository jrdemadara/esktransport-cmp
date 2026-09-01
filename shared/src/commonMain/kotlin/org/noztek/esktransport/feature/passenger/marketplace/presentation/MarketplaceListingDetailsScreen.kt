package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.Briefcase
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.ChatBubbleOvalLeft
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Cube
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Tag
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.User
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.driver.onboarding.presentation.CapturedDocumentPreviewImage
import org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceListingDetailsScreen(
    listingPublicId: String,
    onBackClick: () -> Unit = {},
    onMessageOwnerClick: (String) -> Unit = {},
    viewModel: MarketplaceListingDetailsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(listingPublicId) {
        viewModel.load(listingPublicId)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is MarketplaceListingDetailsUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ListingDetailsTopBar(onBackClick = onBackClick)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val listing = uiState.listing
            when {
                uiState.isLoading && listing == null -> {
                    item { DetailsLoadingState() }
                }
                uiState.errorMessage != null && listing == null -> {
                    item {
                        DetailsMessageState(
                            title = "Unable to load listing",
                            message = uiState.errorMessage.orEmpty(),
                        )
                    }
                }
                listing != null -> {
                    item {
                        ListingImageCarousel(
                            listing = listing,
                            vehiclePhotoBytes = uiState.vehiclePhotoBytes,
                            isPhotoLoading = uiState.isVehiclePhotoLoading,
                        )
                    }
                    item { ListingHeader(listing = listing) }
                    item { VehicleDetailsCard(listing = listing) }
                    item { PricingCard(listing = listing) }
                    item { DescriptionCard(listing = listing) }
                    item {
                        RequestVehicleCard(
                            listing = listing,
                            uiState = uiState,
                            onRequestedStartAtChange = viewModel::onRequestedStartAtChange,
                            onRequestedEndAtChange = viewModel::onRequestedEndAtChange,
                            onPickupLabelChange = viewModel::onPickupLabelChange,
                            onDestinationLabelChange = viewModel::onDestinationLabelChange,
                            onPassengerCountChange = viewModel::onPassengerCountChange,
                            onCargoDescriptionChange = viewModel::onCargoDescriptionChange,
                            onCargoWeightKgChange = viewModel::onCargoWeightKgChange,
                            onCargoPiecesChange = viewModel::onCargoPiecesChange,
                            onCustomerNoteChange = viewModel::onCustomerNoteChange,
                            onSendRequestClick = viewModel::sendRequest,
                            onMessageOwnerClick = { onMessageOwnerClick(listing.publicId) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingDetailsTopBar(
    onBackClick: () -> Unit,
) {
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
                text = "Listing details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
        },
    )
}

@Composable
private fun ListingImageCarousel(
    listing: MarketplaceListing,
    vehiclePhotoBytes: ByteArray?,
    isPhotoLoading: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(212.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        ) {
            vehiclePhotoBytes?.let { bytes ->
                CapturedDocumentPreviewImage(
                    bytes = bytes,
                    contentDescription = listing.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    shape = RoundedCornerShape(7.dp),
                    color = Color.Black.copy(alpha = 0.70f),
                    contentColor = Color.White,
                ) {
                    Text(
                        text = "1 / 1",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (vehiclePhotoBytes == null && isPhotoLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(26.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ListingHeader(listing: MarketplaceListing) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = listing.title.displayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SolidPill(text = listing.serviceType.displayLabel())
                    SoftPill(text = listing.vehicle.vehicleTypeDisplayLabel(), color = MaterialTheme.colorScheme.primary)
                }
            }
            Icon(
                imageVector = Heroicons.Outline.Heart,
                contentDescription = "Save",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
        }
        OwnerRow(listing = listing)
    }
}

@Composable
private fun OwnerRow(listing: MarketplaceListing) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Heroicons.Outline.User,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = listing.owner.name.displayPersonName(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            Icon(
                imageVector = Heroicons.Outline.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = listing.updatedAt?.let { "Updated ${formatApiDate(it)}" } ?: "Recently updated",
            modifier = Modifier.padding(start = 22.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VehicleDetailsCard(listing: MarketplaceListing) {
    DetailsSectionCard(
        title = "Vehicle details",
        icon = Heroicons.Outline.Truck,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCell(
                icon = Heroicons.Outline.DocumentText,
                label = "Plate number",
                value = listing.vehicle.plate,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(45.dp))
            InfoCell(
                icon = Heroicons.Outline.Truck,
                label = "Make / Model / Year",
                value = listing.vehicle.modelYearLabel(),
                modifier = Modifier.weight(1.2f),
            )
        }
        SectionDivider()
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCell(
                icon = Heroicons.Outline.Briefcase,
                label = "Payload",
                value = formatWeight(listing.vehicle.payloadKg),
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(45.dp))
            InfoCell(
                icon = Heroicons.Outline.Cube,
                label = "Volume",
                value = listing.vehicle.volumeM3?.let { "${formatCompactNumber(it)} m3" } ?: "-",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(45.dp))
            InfoCell(
                icon = Heroicons.Outline.User,
                label = "Passenger capacity",
                value = listing.vehicle.passengerCapacity?.toString() ?: "-",
                modifier = Modifier.weight(1.08f),
            )
        }
    }
}

@Composable
private fun PricingCard(listing: MarketplaceListing) {
    DetailsSectionCard(
        title = "Pricing",
        icon = Heroicons.Outline.Tag,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Base rate",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = listing.rateLabel(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = listing.minimumHours?.let { "Minimum ${formatCompactNumber(it)} hours" } ?: "Minimum hours not set",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            VerticalDivider(modifier = Modifier.height(58.dp))
            Column(
                modifier = Modifier.weight(0.8f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Included km",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = listing.includedKm?.let { "${formatCompactNumber(it)} km" } ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            SoftPill(text = listing.currency, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DescriptionCard(listing: MarketplaceListing) {
    DetailsSectionCard(
        title = "Description",
        icon = Heroicons.Outline.DocumentText,
    ) {
        Text(
            text = listing.description?.takeIf { it.isNotBlank() } ?: "No description provided yet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RequestVehicleCard(
    listing: MarketplaceListing,
    uiState: MarketplaceListingDetailsUiState,
    onRequestedStartAtChange: (String) -> Unit,
    onRequestedEndAtChange: (String) -> Unit,
    onPickupLabelChange: (String) -> Unit,
    onDestinationLabelChange: (String) -> Unit,
    onPassengerCountChange: (String) -> Unit,
    onCargoDescriptionChange: (String) -> Unit,
    onCargoWeightKgChange: (String) -> Unit,
    onCargoPiecesChange: (String) -> Unit,
    onCustomerNoteChange: (String) -> Unit,
    onSendRequestClick: () -> Unit,
    onMessageOwnerClick: () -> Unit,
) {
    var activeDatePicker by remember { mutableStateOf<RequestDateField?>(null) }

    DetailsSectionCard(
        title = "Request this vehicle",
        icon = Heroicons.Outline.CalendarDays,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RequestDatePickerField(
                value = uiState.requestedStartAt,
                label = "Requested start",
                placeholder = "Select date",
                icon = Heroicons.Outline.CalendarDays,
                onClick = { activeDatePicker = RequestDateField.Start },
                modifier = Modifier.weight(1f),
            )
            RequestDatePickerField(
                value = uiState.requestedEndAt,
                label = "Requested end",
                placeholder = "Select date",
                icon = Heroicons.Outline.CalendarDays,
                onClick = { activeDatePicker = RequestDateField.End },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RequestInputField(
                value = uiState.pickupLabel,
                onValueChange = onPickupLabelChange,
                label = "Pickup location",
                placeholder = "Pickup",
                icon = Heroicons.Outline.MapPin,
                modifier = Modifier.weight(1f),
            )
            RequestInputField(
                value = uiState.destinationLabel,
                onValueChange = onDestinationLabelChange,
                label = "Destination",
                placeholder = "Destination",
                icon = Heroicons.Outline.MapPin,
                modifier = Modifier.weight(1f),
            )
        }
        if (listing.serviceType == "cargo") {
            RequestInputField(
                value = uiState.cargoDescription,
                onValueChange = onCargoDescriptionChange,
                label = "Cargo details",
                placeholder = "100 sacks cacao",
                icon = Heroicons.Outline.Cube,
                singleLine = false,
                minHeight = 84.dp,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RequestInputField(
                    value = uiState.cargoWeightKg,
                    onValueChange = onCargoWeightKgChange,
                    label = "Weight kg",
                    placeholder = "300",
                    icon = Heroicons.Outline.Briefcase,
                    modifier = Modifier.weight(1f),
                )
                RequestInputField(
                    value = uiState.cargoPieces,
                    onValueChange = onCargoPiecesChange,
                    label = "Pieces",
                    placeholder = "100",
                    icon = Heroicons.Outline.Cube,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            RequestInputField(
                value = uiState.passengerCount,
                onValueChange = onPassengerCountChange,
                label = "Passengers",
                placeholder = "1",
                icon = Heroicons.Outline.User,
            )
        }
        RequestInputField(
            value = uiState.customerNote,
            onValueChange = onCustomerNoteChange,
            label = "Customer note (optional)",
            placeholder = "Add pickup notes",
            icon = Heroicons.Outline.DocumentText,
            singleLine = false,
            minHeight = 84.dp,
        )
        AppPrimaryButton(
            text = when {
                uiState.isSendingRequest -> "Sending..."
                uiState.sentRequestPublicId != null -> "Request sent"
                else -> "Send request"
            },
            onClick = onSendRequestClick,
            enabled = !uiState.isSendingRequest && uiState.sentRequestPublicId == null,
            height = 44.dp,
        )
        OutlinedButton(
            onClick = onMessageOwnerClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = PaddingValues(horizontal = 14.dp),
        ) {
            Icon(
                imageVector = Heroicons.Outline.ChatBubbleOvalLeft,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Message owner",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    activeDatePicker?.let { field ->
        RequestDatePickerDialog(
            title = if (field == RequestDateField.Start) "Requested start" else "Requested end",
            onDismiss = { activeDatePicker = null },
            onDateSelected = { selectedDate ->
                if (field == RequestDateField.Start) {
                    onRequestedStartAtChange(selectedDate)
                } else {
                    onRequestedEndAtChange(selectedDate)
                }
                activeDatePicker = null
            },
        )
    }
}

@Composable
private fun DetailsLoadingState() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Loading listing",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailsMessageState(
    title: String,
    message: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoCell(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RequestInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Dp = if (singleLine) 56.dp else 84.dp,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(minHeight),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 3,
        textStyle = MaterialTheme.typography.bodySmall,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun RequestDatePickerField(
    value: String,
    label: String,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value.ifBlank { placeholder },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                color = if (value.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestDatePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis
                        ?.let(::formatPickerDate)
                        ?.let(onDateSelected)
                },
                enabled = datePickerState.selectedDateMillis != null,
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            },
        )
    }
}

private enum class RequestDateField {
    Start,
    End,
}

@Composable
private fun SolidPill(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SoftPill(
    text: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.10f),
        contentColor = color,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.7.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(0.7.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

private fun MarketplaceListing.rateLabel(): String {
    val amount = baseRate ?: return "-"
    val unit = rateUnit?.takeIf { it.isNotBlank() } ?: "day"
    return "${formatMoney(amount, currency)}/$unit"
}

private fun org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListingVehicle.modelYearLabel(): String {
    val name = listOfNotNull(make, model).joinToString(" ").ifBlank {
        vehicleTypeDisplayLabel()
    }
    val displayName = name.displayTitle()
    return year?.let { "$displayName  •  $it" } ?: displayName
}

private fun org.noztek.esktransport.feature.passenger.marketplace.domain.model.MarketplaceListingVehicle.vehicleTypeDisplayLabel(): String {
    return (vehicleTypeLabel ?: vehicleTypeCode).displayLabel()
}

private fun String.displayTitle(): String {
    return trim().uppercaseFirstLetterOfEachWord(lowercaseRest = false)
}

private fun String.displayPersonName(): String {
    return trim().uppercaseFirstLetterOfEachWord()
}

private fun String.displayLabel(): String {
    return replace("_", " ").trim().uppercaseFirstLetterOfEachWord()
}

private fun formatWeight(value: Double?): String {
    return value?.let { "${formatCompactNumber(it)} kg" } ?: "-"
}

private fun formatMoney(amount: Double, currency: String): String {
    val whole = amount.toLong()
    val formatted = whole.toString().reversed().chunked(3).joinToString(",").reversed()
    return if (currency.equals("PHP", ignoreCase = true)) {
        "PHP $formatted"
    } else {
        "$currency $formatted"
    }
}

private fun formatCompactNumber(value: Double): String {
    val whole = value.toLong()
    return if (value == whole.toDouble()) {
        whole.toString().reversed().chunked(3).joinToString(",").reversed()
    } else {
        value.toString()
    }
}

private fun formatApiDate(value: String): String {
    val date = value.take(10)
    val parts = date.split("-")
    if (parts.size != 3) return date

    val month = when (parts[1]) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
        else -> return date
    }
    val day = parts[2].trimStart('0').ifBlank { parts[2] }
    return "$month $day, ${parts[0]}"
}

private fun formatPickerDate(epochMillis: Long): String {
    val epochDay = epochMillis.floorDiv(MillisPerDay)
    val (year, month, day) = civilDateFromEpochDay(epochDay)
    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

private fun civilDateFromEpochDay(epochDay: Long): Triple<Int, Int, Int> {
    var zeroDay = epochDay + 719468L
    val era = zeroDay.floorDiv(146097L)
    val dayOfEra = zeroDay - era * 146097L
    val yearOfEra = (dayOfEra - dayOfEra / 1460L + dayOfEra / 36524L - dayOfEra / 146096L) / 365L
    val year = yearOfEra + era * 400L
    val dayOfYear = dayOfEra - (365L * yearOfEra + yearOfEra / 4L - yearOfEra / 100L)
    val monthPart = (5L * dayOfYear + 2L) / 153L
    val day = dayOfYear - (153L * monthPart + 2L) / 5L + 1L
    val month = monthPart + if (monthPart < 10L) 3L else -9L
    val adjustedYear = year + if (month <= 2L) 1L else 0L
    return Triple(adjustedYear.toInt(), month.toInt(), day.toInt())
}

private const val MillisPerDay = 86_400_000L
