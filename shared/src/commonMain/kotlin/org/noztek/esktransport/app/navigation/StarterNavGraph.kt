package org.noztek.esktransport.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import org.noztek.esktransport.feature.common.starter.presentation.StarterScreen

fun NavGraphBuilder.starterNavGraph(
    onLoginClick: () -> Unit,
    onCustomerRegisterClick: () -> Unit,
    onDriverRegisterClick: () -> Unit,
) {
    navigation(startDestination = StarterRoute.WELCOME, route = RootRoute.STARTER) {
        composable(StarterRoute.WELCOME) {
            StarterScreen(
                onLoginClick = onLoginClick,
                onCustomerRegisterClick = onCustomerRegisterClick,
                onDriverRegisterClick = onDriverRegisterClick,
            )
        }
    }
}
