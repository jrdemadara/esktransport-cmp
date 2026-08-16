package org.noztek.esktransport.feature.driver.settings.presentation

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.PencilSquare
import com.composables.icons.heroicons.outline.Phone
import com.composables.icons.heroicons.outline.Plus
import com.composables.icons.heroicons.outline.Trash
import com.composables.icons.heroicons.outline.User
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.feature.driver.settings.domain.model.DriverEmergencyContact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverEmergencyContactsScreen(
    onBackClick: () -> Unit,
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverEmergencyContactsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val editorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    DriverEmergencyContactsContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onAddClick = viewModel::startAddContact,
        onEditClick = viewModel::startEditContact,
        onBottomBarNavigate = onBottomBarNavigate,
    )

    uiState.editor?.let { editor ->
        ModalBottomSheet(
            onDismissRequest = {
                if (!uiState.isSaving) {
                    viewModel.dismissEditor()
                }
            },
            sheetState = editorSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            EmergencyContactEditorSheet(
                editor = editor,
                isSaving = uiState.isSaving,
                contact = uiState.contacts.firstOrNull { it.id == editor.contactId },
                onNameChange = viewModel::updateEditorName,
                onPhoneChange = viewModel::updateEditorPhone,
                onRelationshipChange = viewModel::updateEditorRelationship,
                onSave = viewModel::saveEditor,
                onDelete = { contact -> viewModel.deleteContact(contact) },
                onCancel = viewModel::dismissEditor,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverEmergencyContactsContent(
    uiState: DriverEmergencyContactsUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (DriverEmergencyContact) -> Unit,
    onBottomBarNavigate: (String) -> Unit,
) {
    Scaffold(
        topBar = { EmergencyContactsTopBar(onBackClick = onBackClick) },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.PROFILE,
                onNavigate = onBottomBarNavigate,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    EmergencyContactsSummary(count = uiState.contacts.size)
                    EmergencyContactsList(
                        contacts = uiState.contacts,
                        onEditClick = onEditClick,
                    )
                    AppPrimaryButton(
                        text = "Add contact",
                        onClick = onAddClick,
                        enabled = uiState.contacts.size < 3 && !uiState.isSaving,
                        height = 46.dp,
                        modifier = Modifier.padding(bottom = 18.dp),
                        trailingIcon = {
                            Icon(
                                imageVector = Heroicons.Outline.Plus,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmergencyContactsTopBar(onBackClick: () -> Unit) {
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
                text = "Emergency Contacts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun EmergencyContactsSummary(count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "$count of 3 contacts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Add people eSKTransport support can use during urgent driver safety cases.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmergencyContactsList(
    contacts: List<DriverEmergencyContact>,
    onEditClick: (DriverEmergencyContact) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (contacts.isEmpty()) {
                Text(
                    text = "No emergency contacts yet.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                contacts.forEachIndexed { index, contact ->
                    EmergencyContactRow(
                        contact = contact,
                        onEditClick = { onEditClick(contact) },
                    )
                    if (index < contacts.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
                            modifier = Modifier.padding(start = 58.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyContactRow(
    contact: DriverEmergencyContact,
    onEditClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Heroicons.Outline.User,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${contact.relationship} • ${contact.phone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Heroicons.Outline.PencilSquare,
            contentDescription = "Edit contact",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmergencyContactEditorSheet(
    editor: DriverEmergencyContactEditorState,
    isSaving: Boolean,
    contact: DriverEmergencyContact?,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRelationshipChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: (DriverEmergencyContact) -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 22.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Text(
            text = if (editor.isEditing) "Edit contact" else "Add contact",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        EmergencyContactTextField(
            value = editor.name,
            onValueChange = onNameChange,
            label = "Full name",
            keyboardType = KeyboardType.Text,
            enabled = !isSaving,
        )
        EmergencyContactTextField(
            value = editor.phone,
            onValueChange = onPhoneChange,
            label = "Phone number",
            keyboardType = KeyboardType.Phone,
            enabled = !isSaving,
        )
        EmergencyContactTextField(
            value = editor.relationship,
            onValueChange = onRelationshipChange,
            label = "Relationship",
            keyboardType = KeyboardType.Text,
            enabled = !isSaving,
        )
        editor.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (contact != null) {
                TextButton(
                    onClick = { onDelete(contact) },
                    enabled = !isSaving,
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
                enabled = !isSaving,
                shape = RoundedCornerShape(11.dp),
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSave,
                enabled = !isSaving,
                shape = RoundedCornerShape(11.dp),
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        }
    }
}

@Composable
private fun EmergencyContactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next,
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}
