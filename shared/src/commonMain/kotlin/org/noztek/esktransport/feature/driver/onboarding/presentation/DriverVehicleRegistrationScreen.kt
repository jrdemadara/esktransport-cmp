package org.noztek.esktransport.feature.driver.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ChevronRight
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppInputField
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingDocumentType
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus

@Composable
fun DriverVehicleRegistrationScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DriverOnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var captureType by remember { mutableStateOf<DriverOnboardingDocumentType?>(null) }

    captureType?.let { type ->
        DriverDocumentCaptureScreen(
            type = type,
            onCaptured = { capture ->
                viewModel.captureDocumentPreview(
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

    DriverVehicleRegistrationContent(
        state = state,
        onBack = onBack,
        onVehicleTypeChange = viewModel::updateVehicleType,
        onPlateChange = viewModel::updatePlate,
        onMakeChange = viewModel::updateMake,
        onModelChange = viewModel::updateModel,
        onYearChange = viewModel::updateYear,
        onPassengerCapacityChange = viewModel::updatePassengerCapacity,
        onCaptureClick = { type -> captureType = type },
        onContinue = { viewModel.submitVehicleRegistration(onSuccess = onContinue) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverVehicleRegistrationContent(
    state: DriverOnboardingUiState,
    onBack: () -> Unit,
    onVehicleTypeChange: (String) -> Unit,
    onPlateChange: (String) -> Unit,
    onMakeChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPassengerCapacityChange: (String) -> Unit,
    onCaptureClick: (DriverOnboardingDocumentType) -> Unit,
    onContinue: () -> Unit,
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
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = { Text("Vehicle Registration") },
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
            VehicleRegistrationHeader()
            VehicleDetailsSection(
                state = state,
                onVehicleTypeChange = onVehicleTypeChange,
                onPlateChange = onPlateChange,
                onMakeChange = onMakeChange,
                onModelChange = onModelChange,
                onYearChange = onYearChange,
                onPassengerCapacityChange = onPassengerCapacityChange,
            )
            VehicleRegistrationDocumentSection(
                state = state,
                onCaptureClick = onCaptureClick,
            )
            AppPrimaryButton(
                text = if (state.isSubmittingVehicleRegistration) "Submitting..." else "Continue",
                onClick = onContinue,
                enabled = !state.isSubmittingVehicleRegistration,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                trailingIcon = {
                    Icon(
                        imageVector = Heroicons.Outline.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                },
            )
        }
    }
}

@Composable
private fun VehicleRegistrationHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Step 3 of 4",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
            )
            Text(
                text = "Register your service vehicle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Add the vehicle details, registration document, and a clear photo of the service vehicle.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
            )
        }
    }
}

@Composable
private fun VehicleDetailsSection(
    state: DriverOnboardingUiState,
    onVehicleTypeChange: (String) -> Unit,
    onPlateChange: (String) -> Unit,
    onMakeChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onPassengerCapacityChange: (String) -> Unit,
) {
    VehicleSectionSurface(title = "Vehicle details") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VehicleTypeChip("motorcycle", "Moto", state.vehicleTypeCode, onVehicleTypeChange)
            VehicleTypeChip("tricycle", "Trike", state.vehicleTypeCode, onVehicleTypeChange)
            VehicleTypeChip("car", "Car", state.vehicleTypeCode, onVehicleTypeChange)
        }
        AppInputField(
            value = state.plate,
            onValueChange = onPlateChange,
            label = "Plate number",
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
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
    }
}

@Composable
private fun VehicleRegistrationDocumentSection(
    state: DriverOnboardingUiState,
    onCaptureClick: (DriverOnboardingDocumentType) -> Unit,
) {
    VehicleSectionSurface(title = "Required captures") {
        VehicleCaptureRow(
            title = "Vehicle registration",
            helperText = "Retake if the document is unclear.",
            type = DriverOnboardingDocumentType.VehicleRegistration,
            state = state,
            onCaptureClick = onCaptureClick,
        )
        VehicleCaptureRow(
            title = "Vehicle photo",
            helperText = "Capture the vehicle clearly from the outside.",
            type = DriverOnboardingDocumentType.VehiclePhoto,
            state = state,
            onCaptureClick = onCaptureClick,
        )
    }
}

@Composable
private fun VehicleCaptureRow(
    title: String,
    helperText: String,
    type: DriverOnboardingDocumentType,
    state: DriverOnboardingUiState,
    onCaptureClick: (DriverOnboardingDocumentType) -> Unit,
) {
    val requirement = state.status?.requirements?.firstOrNull { it.type == type }
    val preview = state.capturedPreviews[type]

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = if (preview != null) "Ready to submit" else requirement?.status?.vehicleStatusLabel() ?: "Required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = { onCaptureClick(type) },
                enabled = !state.isSubmittingVehicleRegistration,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("Capture")
            }
        }
        if (preview != null) {
            VehicleRegistrationPreviewCard(
                preview = preview,
                type = type,
                helperText = helperText,
                contentDescription = "$title preview",
            )
        }
    }
}

@Composable
private fun VehicleSectionSurface(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
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
private fun VehicleRegistrationPreviewCard(
    preview: CapturedDocumentPreview,
    type: DriverOnboardingDocumentType,
    helperText: String,
    contentDescription: String,
) {
    val shape = RoundedCornerShape(8.dp)
    val isRegistrationDocument = type == DriverOnboardingDocumentType.VehicleRegistration
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .height(if (isRegistrationDocument) 104.dp else 82.dp)
                    .aspectRatio(if (isRegistrationDocument) 0.70f else 1.58f)
                    .clip(shape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                CapturedDocumentPreviewImage(
                    bytes = preview.bytes,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Preview ready",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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

private fun DriverRequirementStatus.vehicleStatusLabel(): String {
    return when (this) {
        DriverRequirementStatus.Missing -> "Required"
        DriverRequirementStatus.Uploaded,
        DriverRequirementStatus.PendingReview -> "Under review"
        DriverRequirementStatus.Approved -> "Done"
        DriverRequirementStatus.Rejected -> "Needs update"
        DriverRequirementStatus.Expired -> "Expired"
    }
}
