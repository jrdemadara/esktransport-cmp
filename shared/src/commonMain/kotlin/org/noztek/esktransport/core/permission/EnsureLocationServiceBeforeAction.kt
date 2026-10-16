package org.noztek.esktransport.core.permission

import androidx.compose.runtime.Composable

@Composable
expect fun EnsureLocationServiceBeforeAction(
    requested: Boolean,
    settingsConfirmed: Boolean,
    onReady: () -> Unit,
    onSettingsRequired: () -> Unit,
    onUnavailable: () -> Unit,
)
