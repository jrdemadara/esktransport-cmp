package org.noztek.esktransport

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.noztek.esktransport.app.navigation.RootNavHost
import org.noztek.esktransport.core.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        RootNavHost()
    }
}
