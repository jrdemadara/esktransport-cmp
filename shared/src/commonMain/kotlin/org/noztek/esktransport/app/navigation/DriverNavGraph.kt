package org.noztek.esktransport.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.savedstate.read
import org.koin.compose.koinInject
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.driver.home.presentation.DriverHomeScreen
import org.noztek.esktransport.feature.driver.trip_navigation.presentation.TripNavigationScreen

private const val ROUTE_DRIVER_TRIP_TRACKING = "driver-trip-tracking"

fun NavGraphBuilder.driverNavGraph(navController: NavHostController) {
    navigation(startDestination = DriverRoute.HOME, route = RootRoute.DRIVER) {
        composable(DriverRoute.HOME) {
            DriverHomeScreen(
                onNavigateToTrip = { bookingPublicId ->
                    navController.navigate("$ROUTE_DRIVER_TRIP_TRACKING/$bookingPublicId", navOptions {
                        launchSingleTop = true
                    })
                },
            )
        }
        composable("$ROUTE_DRIVER_TRIP_TRACKING/{bookingId}") { backStackEntry ->
            val bookingId = backStackEntry.arguments?.read { getStringOrNull("bookingId") }.orEmpty()
            val mapboxConfig: MapboxConfig = koinInject()
            TripNavigationScreen(
                bookingPublicId = bookingId,
                mapboxConfig = mapboxConfig,
            )
        }
    }
}
