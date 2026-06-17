package org.noztek.esktransport.feature.driver.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.platform.rememberPlatformFilePicker
import org.noztek.esktransport.core.ui.composables.common.AppInputField
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingState
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus

@Composable
fun DriverOnboardingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverOnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingUploadType by remember { mutableStateOf<DriverOnboardingDocumentType?>(null) }
    val filePicker = rememberPlatformFilePicker { file ->
        val type = pendingUploadType ?: return@rememberPlatformFilePicker
        viewModel.uploadDocument(
            type = type,
            fileName = file.fileName,
            mimeType = file.mimeType,
            bytes = file.bytes,
        )
        pendingUploadType = null
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    DriverOnboardingContent(
        state = state,
        onBack = onBack,
        onLicenseNoChange = viewModel::updateLicenseNo,
        onLicenseExpiryChange = viewModel::updateLicenseExpiry,
        onVehicleTypeChange = viewModel::updateVehicleType,
        onPlateChange = viewModel::updatePlate,
        onMakeChange = viewModel::updateMake,
        onModelChange = viewModel::updateModel,
        onYearChange = viewModel::updateYear,
        onPassengerCapacityChange = viewModel::updatePassengerCapacity,
        onSaveVehicle = viewModel::saveVehicle,
        onUploadClick = { type ->
            if (filePicker.isSupported) {
                pendingUploadType = type
                filePicker.launch(arrayOf("image/*", "application/pdf"))
            } else {
                viewModel.showPickerNotReady(type)
            }
        },
        onSubmit = viewModel::submitForReview,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverOnboardingContent(
    state: DriverOnboardingUiState,
    onBack: () -> Unit,
    onLicenseNoChange: (String) -> Unit,
    onLicenseExpiryChange: (String) -> Unit,
    onVehicleTypeChange: (String) -> Unit,
    onPlateChange: (String) -> Unit,
    onMakeChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPassengerCapacityChange: (String) -> Unit,
    onSaveVehicle: () -> Unit,
    onUploadClick: (DriverOnboardingDocumentType) -> Unit,
    onSubmit: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = { Text("Driver setup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Heroicons.Outline.ArrowLeft, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SetupHeader(state)
            RequirementChecklist(state)
            IdentitySection(
                state = state,
                onLicenseNoChange = onLicenseNoChange,
                onLicenseExpiryChange = onLicenseExpiryChange,
                onUploadClick = onUploadClick,
            )
            VehicleSection(
                state = state,
                onVehicleTypeChange = onVehicleTypeChange,
                onPlateChange = onPlateChange,
                onMakeChange = onMakeChange,
                onModelChange = onModelChange,
                onYearChange = onYearChange,
                onPassengerCapacityChange = onPassengerCapacityChange,
                onSaveVehicle = onSaveVehicle,
            )
            DocumentsSection(state = state, onUploadClick = onUploadClick)
            AppPrimaryButton(
                text = if (state.isSubmitting) "Submitting..." else "Submit for review",
                onClick = onSubmit,
                enabled = !state.isSubmitting,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
            )
        }
    }
}

@Composable
private fun SetupHeader(state: DriverOnboardingUiState) {
    val status = state.status
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = when {
                    status?.canGo == true -> "You're ready to drive"
                    status?.status == DriverOnboardingState.PendingReview -> "Setup is under review"
                    status?.status == DriverOnboardingState.Rejected -> "Some items need changes"
                    else -> "Complete your driver setup"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Verify your identity and vehicle before going online.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun RequirementChecklist(state: DriverOnboardingUiState) {
    SectionSurface(title = "Checklist") {
        val requirements = state.status?.requirements.orEmpty()
        requirements.forEachIndexed { index, requirement ->
            RequirementRow(
                title = requirement.label,
                status = requirement.status,
                detail = requirement.rejectionReason,
            )
            if (index < requirements.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun IdentitySection(
    state: DriverOnboardingUiState,
    onLicenseNoChange: (String) -> Unit,
    onLicenseExpiryChange: (String) -> Unit,
    onUploadClick: (DriverOnboardingDocumentType) -> Unit,
) {
    SectionSurface(title = "Identity") {
        AppInputField(
            value = state.licenseNo,
            onValueChange = onLicenseNoChange,
            label = "License number",
            modifier = Modifier.fillMaxWidth(),
        )
        AppInputField(
            value = state.licenseExpiry,
            onValueChange = onLicenseExpiryChange,
            label = "License expiry (YYYY-MM-DD)",
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        UploadRow(
            title = "License front",
            type = DriverOnboardingDocumentType.LicenseFront,
            state = state,
            onUploadClick = onUploadClick,
            modifier = Modifier.padding(top = 12.dp),
        )
        UploadRow(
            title = "License back",
            type = DriverOnboardingDocumentType.LicenseBack,
            state = state,
            onUploadClick = onUploadClick,
        )
        UploadRow(
            title = "Selfie for matching",
            type = DriverOnboardingDocumentType.Selfie,
            state = state,
            onUploadClick = onUploadClick,
        )
    }
}

@Composable
private fun VehicleSection(
    state: DriverOnboardingUiState,
    onVehicleTypeChange: (String) -> Unit,
    onPlateChange: (String) -> Unit,
    onMakeChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPassengerCapacityChange: (String) -> Unit,
    onSaveVehicle: () -> Unit,
) {
    SectionSurface(title = "Vehicle") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VehicleTypeChip("motorcycle", "Moto", state.vehicleTypeCode, onVehicleTypeChange)
            VehicleTypeChip("tricycle", "Trike", state.vehicleTypeCode, onVehicleTypeChange)
            VehicleTypeChip("car", "Car", state.vehicleTypeCode, onVehicleTypeChange)
        }
        AppInputField(
            value = state.plate,
            onValueChange = onPlateChange,
            label = "Plate number",
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppInputField(
                value = state.make,
                onValueChange = onMakeChange,
                label = "Make",
                modifier = Modifier.weight(1f),
            )
            AppInputField(
                value = state.model,
                onValueChange = onModelChange,
                label = "Model",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppInputField(
                value = state.year,
                onValueChange = onYearChange,
                label = "Year",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            AppInputField(
                value = state.passengerCapacity,
                onValueChange = onPassengerCapacityChange,
                label = "Seats",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        Button(
            onClick = onSaveVehicle,
            enabled = !state.isSavingVehicle,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            Text(if (state.isSavingVehicle) "Saving..." else "Save vehicle")
        }
    }
}

@Composable
private fun DocumentsSection(
    state: DriverOnboardingUiState,
    onUploadClick: (DriverOnboardingDocumentType) -> Unit,
) {
    SectionSurface(title = "Vehicle documents") {
        UploadRow(
            title = "Vehicle registration",
            type = DriverOnboardingDocumentType.VehicleRegistration,
            state = state,
            onUploadClick = onUploadClick,
        )
    }
}

@Composable
private fun SectionSurface(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

@Composable
private fun RequirementRow(
    title: String,
    status: DriverRequirementStatus,
    detail: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (!detail.isNullOrBlank()) {
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        StatusPill(status = status)
    }
}

@Composable
private fun UploadRow(
    title: String,
    type: DriverOnboardingDocumentType,
    state: DriverOnboardingUiState,
    onUploadClick: (DriverOnboardingDocumentType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val requirement = state.status?.requirements?.firstOrNull { it.type == type }
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                requirement?.status?.label().orEmpty().ifBlank { "Not uploaded" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(
            onClick = { onUploadClick(type) },
            enabled = state.uploadingType == null,
        ) {
            Text(if (state.uploadingType == type) "Uploading" else "Upload")
        }
    }
}

@Composable
private fun VehicleTypeChip(
    value: String,
    label: String,
    selectedValue: String,
    onSelect: (String) -> Unit,
) {
    FilterChip(
        selected = selectedValue == value,
        onClick = { onSelect(value) },
        label = { Text(label) },
    )
}

@Composable
private fun StatusPill(status: DriverRequirementStatus) {
    val color = when (status) {
        DriverRequirementStatus.Approved -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        DriverRequirementStatus.Rejected -> MaterialTheme.colorScheme.errorContainer
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> MaterialTheme.colorScheme.secondaryContainer
        DriverRequirementStatus.Expired -> MaterialTheme.colorScheme.errorContainer
        DriverRequirementStatus.Missing -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (status) {
        DriverRequirementStatus.Approved -> MaterialTheme.colorScheme.primary
        DriverRequirementStatus.Rejected,
        DriverRequirementStatus.Expired -> MaterialTheme.colorScheme.onErrorContainer
        DriverRequirementStatus.PendingReview,
        DriverRequirementStatus.Uploaded -> MaterialTheme.colorScheme.onSecondaryContainer
        DriverRequirementStatus.Missing -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color,
        contentColor = contentColor,
    ) {
        Text(
            text = status.label(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
}

private fun DriverRequirementStatus.label(): String {
    return when (this) {
        DriverRequirementStatus.Missing -> "Finish setup"
        DriverRequirementStatus.Uploaded -> "Uploaded"
        DriverRequirementStatus.PendingReview -> "Review"
        DriverRequirementStatus.Approved -> "Approved"
        DriverRequirementStatus.Rejected -> "Rejected"
        DriverRequirementStatus.Expired -> "Expired"
    }
}
