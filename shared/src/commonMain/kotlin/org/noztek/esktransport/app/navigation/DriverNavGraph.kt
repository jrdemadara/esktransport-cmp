package org.noztek.esktransport.app.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.savedstate.read
import org.koin.compose.koinInject
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.driver.home.presentation.HomeScreen
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.presentation.DriverIdentityVerificationScreen
import org.noztek.esktransport.feature.driver.onboarding.presentation.DriverOnboardingScreen
import org.noztek.esktransport.feature.driver.onboarding.presentation.DriverVehicleRegistrationScreen
import org.noztek.esktransport.feature.driver.trip_navigation.presentation.TripNavigationScreen

private const val ROUTE_DRIVER_TRIP_TRACKING = "driver-trip-tracking"
private const val ROUTE_DRIVER_ONBOARDING = "driver-onboarding"
private const val ROUTE_DRIVER_IDENTITY_VERIFICATION = "driver-onboarding/identity"
private const val ROUTE_DRIVER_VEHICLE_REGISTRATION = "driver-onboarding/vehicle-registration"

fun NavGraphBuilder.driverNavGraph(navController: NavHostController) {
    navigation(startDestination = DriverRoute.HOME, route = RootRoute.DRIVER) {
        composable(DriverRoute.HOME) {
            HomeScreen(
                onSetupClick = { status ->
                    navController.navigate(status.nextSetupRoute()) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(ROUTE_DRIVER_IDENTITY_VERIFICATION) {
            DriverIdentityVerificationScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.popBackStack(DriverRoute.HOME, inclusive = false)
                },
            )
        }
        composable(ROUTE_DRIVER_VEHICLE_REGISTRATION) {
            DriverVehicleRegistrationScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.popBackStack(DriverRoute.HOME, inclusive = false)
                },
            )
        }
        composable(ROUTE_DRIVER_ONBOARDING) {
            DriverOnboardingScreen(
                onBack = { navController.popBackStack() },
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

private fun DriverOnboardingStatus?.nextSetupRoute(): String {
    if (this == null) return ROUTE_DRIVER_IDENTITY_VERIFICATION

    return when {
        stepStatuses.identityVerification.needsDriverAction() -> ROUTE_DRIVER_IDENTITY_VERIFICATION
        stepStatuses.vehicleRegistration.needsDriverAction() -> ROUTE_DRIVER_VEHICLE_REGISTRATION
        stepStatuses.serviceRadius.needsDriverAction() -> ROUTE_DRIVER_ONBOARDING
        else -> ROUTE_DRIVER_ONBOARDING
    }
}

private fun DriverRequirementStatus.needsDriverAction(): Boolean {
    return this == DriverRequirementStatus.Missing ||
        this == DriverRequirementStatus.Rejected ||
        this == DriverRequirementStatus.Expired
}
