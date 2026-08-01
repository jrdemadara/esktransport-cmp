package org.noztek.esktransport.feature.passenger.settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.EllipsisVertical
import com.composables.icons.heroicons.outline.Home
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.Trash
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.map_pin_red
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView
import org.noztek.esktransport.feature.passenger.location_search.domain.model.GeoPoint
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlace
import org.noztek.esktransport.feature.passenger.settings.domain.model.SavedPlaceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPlacesScreen(
    onBackClick: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: SavedPlacesViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val editorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeEditor by remember { mutableStateOf<SavedPlaceEditor?>(null) }
    var formState by remember { mutableStateOf(SavedPlaceFormState()) }

    val openEditor: (SavedPlace?, SavedPlaceType) -> Unit = { place, placeType ->
        activeEditor = SavedPlaceEditor(place = place, defaultPlaceType = placeType)
        val currentLocation = uiState.currentLocationPoint
        formState = place?.toFormState() ?: SavedPlaceFormState(
            placeType = placeType,
            label = when (placeType) {
                SavedPlaceType.Home -> "Home"
                SavedPlaceType.Work -> "Work"
                SavedPlaceType.Custom -> ""
            },
            latitude = currentLocation?.latitude?.toString().orEmpty(),
            longitude = currentLocation?.longitude?.toString().orEmpty(),
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                SavedPlacesUiEvent.CloseEditor -> activeEditor = null
                is SavedPlacesUiEvent.FillPinnedAddress -> {
                    formState = formState.copy(
                        address = event.address,
                        latitude = event.point.latitude.toString(),
                        longitude = event.point.longitude.toString(),
                    )
                }
                is SavedPlacesUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { SavedPlacesTopBar(onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SavedPlacesSectionTitle("Pinned")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
            ) {
                Column {
                    PinnedPlaceRow(
                        title = "Home",
                        address = uiState.homePlace?.address ?: "Not set",
                        icon = Heroicons.Outline.Home,
                        onClick = { openEditor(uiState.homePlace, SavedPlaceType.Home) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
                    PinnedPlaceRow(
                        title = "Work",
                        address = uiState.workPlace?.address ?: "Not set",
                        icon = Heroicons.Outline.MapPin,
                        onClick = { openEditor(uiState.workPlace, SavedPlaceType.Work) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SavedPlacesSectionTitle("Saved Places")
                Text(
                    text = "${uiState.customPlaces.size} places",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when {
                    uiState.isLoading -> SavedPlacesLoadingCard()
                    uiState.errorMessage != null -> SavedPlacesErrorCard(
                        message = uiState.errorMessage.orEmpty(),
                        onRetry = viewModel::refresh,
                    )
                    uiState.customPlaces.isEmpty() -> SavedPlacesEmptyCard()
                    else -> uiState.customPlaces.forEach { place ->
                        SavedPlaceCard(
                            place = place,
                            onMoreClick = { openEditor(place, SavedPlaceType.Custom) },
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { openEditor(null, SavedPlaceType.Custom) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Heroicons.Outline.Plus,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Add saved place",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    activeEditor?.let { editor ->
        ModalBottomSheet(
            onDismissRequest = {},
            sheetState = editorSheetState,
            sheetGesturesEnabled = false,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            SavedPlaceEditorSheet(
                title = if (editor.place == null) "Add saved place" else "Edit saved place",
                formState = formState,
                isSubmitting = uiState.isSubmitting,
                canDelete = editor.place != null,
                currentLocationPoint = uiState.currentLocationPoint,
                mapboxConfig = mapboxConfig,
                onFormChange = { formState = it },
                onDelete = { editor.place?.let(viewModel::deletePlace) },
                onCancel = { activeEditor = null },
                onSave = { viewModel.savePlace(editor.place, formState) },
                onPinSettled = { point ->
                    viewModel.onPinLocationSettled(GeoPoint(point.latitude, point.longitude))
                },
            )
        }
    }
}

@Composable
private fun SavedPlacesTopBar(onBackClick: () -> Unit) {
    CenterAlignedTopAppBar(
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
                text = "Saved Places",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun PinnedPlaceRow(
    title: String,
    address: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = RoundedCornerShape(11.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SavedPlaceCard(
    place: SavedPlace,
    onMoreClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.MapPin,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = place.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onMoreClick, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Heroicons.Outline.EllipsisVertical,
                    contentDescription = "More",
                    modifier = Modifier.size(19.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SavedPlaceEditorSheet(
    title: String,
    formState: SavedPlaceFormState,
    isSubmitting: Boolean,
    canDelete: Boolean,
    currentLocationPoint: GeoPoint?,
    mapboxConfig: MapboxConfig,
    onFormChange: (SavedPlaceFormState) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onPinSettled: (MapPoint) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SavedPlaceTypeChip(
                text = "Home",
                selected = formState.placeType == SavedPlaceType.Home,
                onClick = { onFormChange(formState.copy(placeType = SavedPlaceType.Home, label = formState.label.ifBlank { "Home" })) },
                modifier = Modifier.weight(1f),
            )
            SavedPlaceTypeChip(
                text = "Work",
                selected = formState.placeType == SavedPlaceType.Work,
                onClick = { onFormChange(formState.copy(placeType = SavedPlaceType.Work, label = formState.label.ifBlank { "Work" })) },
                modifier = Modifier.weight(1f),
            )
            SavedPlaceTypeChip(
                text = "Custom",
                selected = formState.placeType == SavedPlaceType.Custom,
                onClick = { onFormChange(formState.copy(placeType = SavedPlaceType.Custom)) },
                modifier = Modifier.weight(1f),
            )
        }
        SavedPlaceTextField(
            value = formState.label,
            onValueChange = { onFormChange(formState.copy(label = it)) },
            label = "Label",
        )
        SavedPlaceTextField(
            value = formState.address,
            onValueChange = { onFormChange(formState.copy(address = it)) },
            label = "Address",
        )
        SavedPlaceMapPicker(
            selectedPoint = formState.toMapPoint(),
            currentLocationPoint = currentLocationPoint?.toMapPoint(),
            mapboxConfig = mapboxConfig,
            onPointPicked = { point ->
                onFormChange(
                    formState.copy(
                        latitude = point.latitude.toString(),
                        longitude = point.longitude.toString(),
                    ),
                )
                onPinSettled(point)
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (canDelete) {
                TextButton(
                    onClick = onDelete,
                    enabled = !isSubmitting,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Icon(
                        imageVector = Heroicons.Outline.Trash,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Delete")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = onCancel,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(11.dp),
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSave,
                enabled = !isSubmitting && formState.toPayloadOrNull() != null,
                shape = RoundedCornerShape(11.dp),
            ) {
                Text(if (isSubmitting) "Saving..." else "Save")
            }
        }
    }
}

@Composable
private fun SavedPlaceMapPicker(
    selectedPoint: MapPoint?,
    currentLocationPoint: MapPoint?,
    mapboxConfig: MapboxConfig,
    onPointPicked: (MapPoint) -> Unit,
) {
    val cameraCenter = selectedPoint ?: currentLocationPoint
    val mapCameraDefaults = remember { MapCameraDefaults(zoom = 16.0, pitch = 0.0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Pin exact location",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (selectedPoint == null) "Move map to set" else "Pinned",
                style = MaterialTheme.typography.labelSmall,
                color = if (selectedPoint == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        ) {
            if (cameraCenter == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Turn on location access to pin this place on the map.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))) {
                    PlatformMapView(
                        modifier = Modifier.fillMaxSize(),
                        config = mapboxConfig,
                        cameraCenter = cameraCenter,
                        cameraDefaults = mapCameraDefaults,
                        showUserLocation = true,
                        syncCameraPosition = false,
                        onCameraIdle = onPointPicked,
                    )
                    Image(
                        painter = painterResource(Res.drawable.map_pin_red),
                        contentDescription = "Pinned location",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = (-18).dp)
                            .size(42.dp),
                    )
                }
            }
        }
        Text(
            text = "Move the map until the pin is on the exact pickup point.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SavedPlaceTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f)
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = BorderStroke(
            width = 0.5.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            },
        ),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SavedPlaceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun SavedPlacesLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Text(
                text = "Loading saved places",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SavedPlacesErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            ) {
                Text(
                    text = "Retry",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SavedPlacesEmptyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)),
    ) {
        Text(
            text = "No custom places yet.",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SavedPlacesSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

private data class SavedPlaceEditor(
    val place: SavedPlace?,
    val defaultPlaceType: SavedPlaceType,
)

private fun SavedPlaceFormState.toMapPoint(): MapPoint? {
    val latitude = latitude.toDoubleOrNull()
    val longitude = longitude.toDoubleOrNull()
    if (latitude == null || longitude == null) return null
    return MapPoint(latitude = latitude, longitude = longitude)
}

private fun GeoPoint.toMapPoint(): MapPoint {
    return MapPoint(latitude = latitude, longitude = longitude)
}
