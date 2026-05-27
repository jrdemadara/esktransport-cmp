package org.noztek.esktransport.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import org.noztek.esktransport.feature.driver.home.presentation.DriverHomeScreen

fun NavGraphBuilder.driverNavGraph(navController: NavHostController) {
    navigation(startDestination = DriverRoute.HOME, route = RootRoute.DRIVER) {
        composable(DriverRoute.HOME) {
            DriverHomeScreen()
        }
    }
}
