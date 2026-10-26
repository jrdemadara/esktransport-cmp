package org.noztek.esktransport

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.noztek.esktransport.app.navigation.RootNavHost
import org.noztek.esktransport.core.permission.RequestLocationPermissionIfNeeded
import org.noztek.esktransport.core.ui.theme.AppTheme

@Composable
@Preview
fun App(
    pushAction: String? = null,
    pushRequestPublicId: String? = null,
    pushThreadId: String? = null,
    pushListingPublicId: String? = null,
) {
    AppTheme {
        RequestLocationPermissionIfNeeded()
        RootNavHost(
            pushAction = pushAction,
            pushRequestPublicId = pushRequestPublicId,
            pushThreadId = pushThreadId,
            pushListingPublicId = pushListingPublicId,
        )
    }
}
