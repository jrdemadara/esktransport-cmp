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
fun DriverIdentityVerificationScreen(
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

    DriverIdentityVerificationContent(
        state = state,
        onBack = onBack,
        onLicenseNoChange = viewModel::updateLicenseNo,
        onAddressChange = viewModel::updateAddress,
        onLicenseExpiryChange = viewModel::updateLicenseExpiry,
        onCaptureClick = { type -> captureType = type },
        onContinue = { viewModel.submitIdentityVerification(onSuccess = onContinue) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverIdentityVerificationContent(
    state: DriverOnboardingUiState,
    onBack: () -> Unit,
    onLicenseNoChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onLicenseExpiryChange: (String) -> Unit,
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
                title = { Text("Identity Verification") },
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
            IdentityStepHeader()
            IdentityLicenseSection(
                state = state,
                onLicenseNoChange = onLicenseNoChange,
                onAddressChange = onAddressChange,
                onLicenseExpiryChange = onLicenseExpiryChange,
            )
            IdentityDocumentsSection(
                state = state,
                onCaptureClick = onCaptureClick,
            )
            AppPrimaryButton(
                text = if (state.isSubmittingIdentity) "Submitting..." else "Continue",
                onClick = onContinue,
                enabled = !state.isSubmittingIdentity,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
            )
        }
    }
}

@Composable
private fun IdentityStepHeader() {
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
                text = "Step 2 of 4",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
            )
            Text(
                text = "Verify your driver license",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Capture the front, back, and a selfie so we can match your identity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
            )
        }
    }
}

@Composable
private fun IdentityLicenseSection(
    state: DriverOnboardingUiState,
    onLicenseNoChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onLicenseExpiryChange: (String) -> Unit,
) {
    IdentitySectionSurface(title = "License details") {
        AppInputField(
            value = state.licenseNo,
            onValueChange = onLicenseNoChange,
            label = "License number",
            modifier = Modifier.fillMaxWidth(),
        )
        AppInputField(
            value = state.address,
            onValueChange = onAddressChange,
            label = "Home address",
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
        AppInputField(
            value = state.licenseExpiry,
            onValueChange = onLicenseExpiryChange,
            label = "License expiry (YYYY-MM-DD)",
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
    }
}

@Composable
private fun IdentityDocumentsSection(
    state: DriverOnboardingUiState,
    onCaptureClick: (DriverOnboardingDocumentType) -> Unit,
) {
    IdentitySectionSurface(title = "Required captures") {
        IdentityUploadRow(
            title = "License front",
            type = DriverOnboardingDocumentType.LicenseFront,
            state = state,
            onCaptureClick = onCaptureClick,
        )
        IdentityUploadRow(
            title = "License back",
            type = DriverOnboardingDocumentType.LicenseBack,
            state = state,
            onCaptureClick = onCaptureClick,
        )
        IdentityUploadRow(
            title = "Selfie",
            type = DriverOnboardingDocumentType.Selfie,
            state = state,
            onCaptureClick = onCaptureClick,
        )
    }
}

@Composable
private fun IdentitySectionSurface(
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
private fun IdentityUploadRow(
    title: String,
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
                    text = if (preview != null) "Ready to submit" else requirement?.status?.identityStatusLabel() ?: "Required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = { onCaptureClick(type) },
                enabled = !state.isSubmittingIdentity,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("Capture")
            }
        }
        if (preview != null) {
            IdentityPreviewCard(title = title, type = type, preview = preview)
        }
    }
}

@Composable
private fun IdentityPreviewCard(
    title: String,
    type: DriverOnboardingDocumentType,
    preview: CapturedDocumentPreview,
) {
    val shape = RoundedCornerShape(8.dp)
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
                    .height(82.dp)
                    .aspectRatio(if (type == DriverOnboardingDocumentType.Selfie) 0.82f else 1.58f)
                    .clip(shape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                CapturedDocumentPreviewImage(
                    bytes = preview.bytes,
                    contentDescription = "$title preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
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
                    text = "Retake if the image is unclear.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun DriverRequirementStatus.identityStatusLabel(): String {
    return when (this) {
        DriverRequirementStatus.Missing -> "Required"
        DriverRequirementStatus.Uploaded,
        DriverRequirementStatus.PendingReview -> "Under review"
        DriverRequirementStatus.Approved -> "Done"
        DriverRequirementStatus.Rejected -> "Needs update"
        DriverRequirementStatus.Expired -> "Expired"
    }
}
