package org.noztek.esktransport.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.savedstate.read
import org.koin.compose.koinInject
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.feature.common.presence.domain.lifecycle.UserPresenceCoordinator
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceContext
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceRole
import org.noztek.esktransport.feature.driver.go.presentation.GoScreen
import org.noztek.esktransport.feature.driver.home.presentation.HomeScreen
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverOnboardingStatus
import org.noztek.esktransport.feature.driver.onboarding.domain.model.DriverRequirementStatus
import org.noztek.esktransport.feature.driver.onboarding.presentation.DriverIdentityVerificationScreen
import org.noztek.esktransport.feature.driver.onboarding.presentation.DriverServiceZoneScreen
import org.noztek.esktransport.feature.driver.onboarding.presentation.DriverVehicleRegistrationScreen
import org.noztek.esktransport.feature.driver.trip_navigation.presentation.TripNavigationScreen

private const val ROUTE_DRIVER_TRIP_TRACKING = "driver-trip-tracking"
private const val ROUTE_DRIVER_IDENTITY_VERIFICATION = "driver-onboarding/identity"
private const val ROUTE_DRIVER_VEHICLE_REGISTRATION = "driver-onboarding/vehicle-registration"
private const val ROUTE_DRIVER_SERVICE_ZONE = "driver-onboarding/service-zone"

fun NavGraphBuilder.driverNavGraph(navController: NavHostController) {
    navigation(startDestination = DriverRoute.HOME, route = RootRoute.DRIVER) {
        composable(
            route = DriverRoute.HOME,
            popEnterTransition = {
                if (initialState.destination.route == DriverRoute.GO) {
                    EnterTransition.None
                } else {
                    null
                }
            },
        ) {
            val userPresenceCoordinator: UserPresenceCoordinator = koinInject()
            LaunchedEffect(Unit) {
                userPresenceCoordinator.updateContext(
                    role = UserPresenceRole.Driver,
                    context = UserPresenceContext.DriverHome,
                )
            }
            HomeScreen(
                onSetupClick = { status ->
                    navController.navigate(status.nextSetupRoute()) {
                        launchSingleTop = true
                    }
                },
                onDriverModeClick = {
                    navController.navigate(DriverRoute.GO) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = DriverRoute.GO,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            val userPresenceCoordinator: UserPresenceCoordinator = koinInject()
            LaunchedEffect(Unit) {
                userPresenceCoordinator.updateContext(
                    role = UserPresenceRole.Driver,
                    context = UserPresenceContext.DriverGo,
                )
            }
            GoScreen(
                onNavigateHome = {
                    navController.popBackStack(DriverRoute.HOME, inclusive = false)
                },
                onNavigateToTrip = { bookingPublicId ->
                    navController.navigate("$ROUTE_DRIVER_TRIP_TRACKING/$bookingPublicId") {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(ROUTE_DRIVER_IDENTITY_VERIFICATION) {
            val userPresenceCoordinator: UserPresenceCoordinator = koinInject()
            LaunchedEffect(Unit) {
                userPresenceCoordinator.updateContext(
                    role = UserPresenceRole.Driver,
                    context = UserPresenceContext.DriverIdentityVerification,
                )
            }
            DriverIdentityVerificationScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.popBackStack(DriverRoute.HOME, inclusive = false)
                },
            )
        }
        composable(ROUTE_DRIVER_VEHICLE_REGISTRATION) {
            val userPresenceCoordinator: UserPresenceCoordinator = koinInject()
            LaunchedEffect(Unit) {
                userPresenceCoordinator.updateContext(
                    role = UserPresenceRole.Driver,
                    context = UserPresenceContext.DriverVehicleRegistration,
                )
            }
            DriverVehicleRegistrationScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.popBackStack(DriverRoute.HOME, inclusive = false)
                },
            )
        }
        composable(ROUTE_DRIVER_SERVICE_ZONE) {
            val userPresenceCoordinator: UserPresenceCoordinator = koinInject()
            LaunchedEffect(Unit) {
                userPresenceCoordinator.updateContext(
                    role = UserPresenceRole.Driver,
                    context = UserPresenceContext.DriverServiceZone,
                )
            }
            DriverServiceZoneScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.popBackStack(DriverRoute.HOME, inclusive = false)
                },
            )
        }
        composable("$ROUTE_DRIVER_TRIP_TRACKING/{bookingId}") { backStackEntry ->
            val userPresenceCoordinator: UserPresenceCoordinator = koinInject()
            LaunchedEffect(Unit) {
                userPresenceCoordinator.updateContext(
                    role = UserPresenceRole.Driver,
                    context = UserPresenceContext.DriverTripTracking,
                )
            }
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
        stepStatuses.serviceRadius.needsDriverAction() -> ROUTE_DRIVER_SERVICE_ZONE
        else -> DriverRoute.HOME
    }
}

private fun DriverRequirementStatus.needsDriverAction(): Boolean {
    return this == DriverRequirementStatus.Missing ||
        this == DriverRequirementStatus.Rejected ||
        this == DriverRequirementStatus.Expired
}
