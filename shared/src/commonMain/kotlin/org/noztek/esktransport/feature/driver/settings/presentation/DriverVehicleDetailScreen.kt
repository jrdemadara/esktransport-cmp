package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.Camera
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.Clock
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.PencilSquare
import com.composables.icons.heroicons.outline.Photo
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.XCircle
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.presentation.DriverDocumentCaptureScreen
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicle
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleDocument
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverVehicleServiceStatus
import org.noztek.esktransport.feature.driver.settings.domain.model.displayName

@Composable
fun DriverVehicleDetailScreen(
    vehiclePublicId: String?,
    refreshToken: Long = 0L,
    onBackClick: () -> Unit,
    onEditClick: (DriverVehicle) -> Unit,
    onVehicleUpdated: () -> Unit = {},
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverVehicleDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var captureType by remember { mutableStateOf<DriverOnboardingDocumentType?>(null) }

    captureType?.let { type ->
        DriverDocumentCaptureScreen(
            type = type,
            onCaptured = { capture ->
                viewModel.uploadCapturedDocument(
                    type = type,
                    fileName = capture.fileName,
                    mimeType = capture.mimeType,
                    bytes = capture.bytes,
                )
                captureType = null
            },
            onClose = { captureType = null },
        )
        return
    }

    LaunchedEffect(vehiclePublicId) {
        viewModel.load(vehiclePublicId)
    }

    LaunchedEffect(refreshToken) {
        if (refreshToken > 0L) {
            viewModel.refresh(showLoading = false)
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.statusMessage) {
        val message = uiState.errorMessage ?: uiState.statusMessage ?: return@LaunchedEffect
        if (uiState.statusMessage != null) {
            onVehicleUpdated()
        }
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessages()
    }

    DriverVehicleDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onCaptureClick = { type -> captureType = type },
        onBottomBarNavigate = onBottomBarNavigate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverVehicleDetailContent(
    uiState: DriverVehicleDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onEditClick: (DriverVehicle) -> Unit,
    onCaptureClick: (DriverOnboardingDocumentType) -> Unit,
    onBottomBarNavigate: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            VehicleDetailTopBar(onBackClick = onBackClick)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.PROFILE,
                onNavigate = onBottomBarNavigate,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.vehicle == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Vehicle not found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                val vehicle = uiState.vehicle
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    VehicleSummaryCard(
                        vehicle = vehicle,
                        onEditClick = { onEditClick(vehicle) },
                    )
                    VehicleDocumentSection(
                        documents = vehicle.documents,
                        uploadingType = uiState.isUploadingDocumentType,
                        onCaptureClick = onCaptureClick,
                    )
                    VehicleServicesSection(services = vehicle.services)
                    VehicleStatusNote(status = vehicle.verificationStatus)
                }
            }
        }
    }
}

@Composable
private fun VehicleDetailTopBar(onBackClick: () -> Unit) {
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
                text = "Vehicle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun VehicleSummaryCard(
    vehicle: DriverVehicle,
    onEditClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Heroicons.Outline.Truck,
                            contentDescription = null,
                            modifier = Modifier.size(23.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = vehicle.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOfNotNull(vehicle.vehicleTypeCode?.titleLabel(), vehicle.plate)
                            .joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                RequirementStatusPill(status = vehicle.verificationStatus)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Details and documents must be approved before this vehicle can be used.",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Heroicons.Outline.PencilSquare,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
private fun VehicleDocumentSection(
    documents: List<DriverVehicleDocument>,
    uploadingType: DriverOnboardingDocumentType?,
    onCaptureClick: (DriverOnboardingDocumentType) -> Unit,
) {
    VehicleDetailSection(title = "Verification documents") {
        VehicleDocumentRow(
            title = "Registration document",
            helper = "Capture the vehicle registration clearly.",
            type = DriverOnboardingDocumentType.VehicleRegistration,
            document = documents.findByType(DriverOnboardingDocumentType.VehicleRegistration),
            uploadingType = uploadingType,
            icon = Heroicons.Outline.DocumentText,
            onCaptureClick = onCaptureClick,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
        VehicleDocumentRow(
            title = "Vehicle photo",
            helper = "Use a clear exterior photo of the vehicle.",
            type = DriverOnboardingDocumentType.VehiclePhoto,
            document = documents.findByType(DriverOnboardingDocumentType.VehiclePhoto),
            uploadingType = uploadingType,
            icon = Heroicons.Outline.Photo,
            onCaptureClick = onCaptureClick,
        )
    }
}

@Composable
private fun VehicleDocumentRow(
    title: String,
    helper: String,
    type: DriverOnboardingDocumentType,
    document: DriverVehicleDocument?,
    uploadingType: DriverOnboardingDocumentType?,
    icon: ImageVector,
    onCaptureClick: (DriverOnboardingDocumentType) -> Unit,
) {
    val status = document?.status ?: DriverRequirementStatus.Missing
    val isUploading = uploadingType == type
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = status.statusColor(),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = document?.rejectionReason ?: helper,
                style = MaterialTheme.typography.bodySmall,
                color = if (status == DriverRequirementStatus.Rejected) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            RequirementStatusPill(status = status)
            OutlinedButton(
                onClick = { onCaptureClick(type) },
                enabled = uploadingType == null,
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Heroicons.Outline.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                }
                Text(if (status == DriverRequirementStatus.Missing) "Capture" else "Retake")
            }
        }
    }
}

@Composable
private fun VehicleServicesSection(services: List<DriverVehicleServiceStatus>) {
    VehicleDetailSection(title = "Services") {
        if (services.isEmpty()) {
            Text(
                text = "No service selected.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            services.forEachIndexed { index, service ->
                ServiceStatusRow(service = service)
                if (index < services.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f))
                }
            }
        }
    }
}

@Composable
private fun ServiceStatusRow(service: DriverVehicleServiceStatus) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Heroicons.Outline.Truck,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = service.status.statusColor(),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = service.serviceType.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = service.rejectionReason ?: if (service.isEnabled) "Enabled for this vehicle." else "Disabled",
                style = MaterialTheme.typography.bodySmall,
                color = if (service.status == DriverRequirementStatus.Rejected) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        RequirementStatusPill(status = service.status)
    }
}

@Composable
private fun VehicleStatusNote(status: DriverRequirementStatus) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = status.statusColor().copy(alpha = 0.08f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Text(
            text = status.statusNote(),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VehicleDetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun RequirementStatusPill(status: DriverRequirementStatus) {
    val color = status.statusColor()
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Text(
            text = status.statusLabel(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

private fun List<DriverVehicleDocument>.findByType(type: DriverOnboardingDocumentType): DriverVehicleDocument? {
    return firstOrNull { it.type == type.apiValue }
}

private fun String.titleLabel(): String {
    return split("_", "-")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}

private fun DriverRequirementStatus.statusLabel(): String {
    return when (this) {
        DriverRequirementStatus.Approved -> "Approved"
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> "Under review"
        DriverRequirementStatus.Rejected -> "Needs update"
        DriverRequirementStatus.Expired -> "Expired"
        DriverRequirementStatus.Missing -> "Required"
    }
}

@Composable
private fun DriverRequirementStatus.statusColor(): Color {
    return when (this) {
        DriverRequirementStatus.Approved -> Color(0xFF16A34A)
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> Color(0xFFE0A100)
        DriverRequirementStatus.Rejected,
        DriverRequirementStatus.Expired -> MaterialTheme.colorScheme.error
        DriverRequirementStatus.Missing -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun DriverRequirementStatus.statusNote(): String {
    return when (this) {
        DriverRequirementStatus.Approved -> "This vehicle is approved and ready for its enabled services."
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> "Your vehicle documents are under review. Verification may take up to 3 working days."
        DriverRequirementStatus.Rejected -> "Update the requested vehicle details or documents to continue."
        DriverRequirementStatus.Expired -> "One or more vehicle documents expired. Upload updated documents."
        DriverRequirementStatus.Missing -> "Capture the registration document and vehicle photo to submit this vehicle for review."
    }
}

private fun DriverRequirementStatus.statusIcon(): ImageVector {
    return when (this) {
        DriverRequirementStatus.Approved -> Heroicons.Outline.CheckCircle
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> Heroicons.Outline.Clock
        DriverRequirementStatus.Rejected,
        DriverRequirementStatus.Expired -> Heroicons.Outline.XCircle
        DriverRequirementStatus.Missing -> Heroicons.Outline.Truck
    }
}
