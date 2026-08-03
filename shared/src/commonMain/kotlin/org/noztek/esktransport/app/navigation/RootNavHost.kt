package org.noztek.esktransport.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import org.koin.compose.koinInject
import org.noztek.esktransport.core.lifecycle.setPlatformDriverOfflineCallback
import org.noztek.esktransport.core.lifecycle.setPlatformUserOfflineCallback
import org.noztek.esktransport.core.notify.domain.lifecycle.PushNotificationRegistrationCoordinator
import org.noztek.esktransport.core.realtime.BaseRealtimeCoordinator
import org.noztek.esktransport.core.realtime.driver.DriverBookingOfferRealtime
import org.noztek.esktransport.core.session.domain.SessionUser
import org.noztek.esktransport.core.session.domain.usecase.MarkStarterSeenUseCase
import org.noztek.esktransport.core.session.domain.usecase.ObserveCurrentSessionUseCase
import org.noztek.esktransport.feature.common.map_preview.presentation.MapPreviewScreen
import org.noztek.esktransport.feature.common.presence.domain.lifecycle.UserPresenceCoordinator
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceContext
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceRole
import org.noztek.esktransport.feature.driver.go.domain.lifecycle.DriverAvailabilityLifecycleCoordinator

@Composable
fun RootNavHost(
    startupViewModel: StartupViewModel = koinInject(),
    realtimeCoordinator: BaseRealtimeCoordinator = koinInject(),
    driverBookingOfferRealtime: DriverBookingOfferRealtime = koinInject(),
    driverAvailabilityLifecycleCoordinator: DriverAvailabilityLifecycleCoordinator = koinInject(),
    userPresenceCoordinator: UserPresenceCoordinator = koinInject(),
    pushNotificationRegistrationCoordinator: PushNotificationRegistrationCoordinator = koinInject(),
    markStarterSeenUseCase: MarkStarterSeenUseCase = koinInject(),
    observeCurrentSessionUseCase: ObserveCurrentSessionUseCase = koinInject(),
) {
    val uiState by startupViewModel.uiState.collectAsState()
    val session by observeCurrentSessionUseCase().collectAsState(
        initial = SessionUser(
            userId = null,
            name = null,
            phone = null,
            roles = emptySet(),
            primaryRole = null,
        )
    )
    val authenticatedRoute = remember(session.roles, session.primaryRole) {
        session.authenticatedRootRoute()
    }

    if (!uiState.isReady) {
        Box(
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        realtimeCoordinator.connect()
        onDispose { realtimeCoordinator.disconnect() }
    }

    DisposableEffect(lifecycleOwner, driverAvailabilityLifecycleCoordinator) {
        setPlatformDriverOfflineCallback {
            driverAvailabilityLifecycleCoordinator.markOfflineOnAppBackground()
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                driverAvailabilityLifecycleCoordinator.markOfflineOnAppBackground()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            setPlatformDriverOfflineCallback(null)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(lifecycleOwner, userPresenceCoordinator) {
        setPlatformUserOfflineCallback {
            userPresenceCoordinator.markOffline()
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> userPresenceCoordinator.markForeground()
                Lifecycle.Event.ON_STOP -> userPresenceCoordinator.markOffline()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            userPresenceCoordinator.stop()
            setPlatformUserOfflineCallback(null)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(authenticatedRoute, session.userId, driverBookingOfferRealtime) {
        if (authenticatedRoute == RootRoute.DRIVER && session.userId != null) {
            driverBookingOfferRealtime.subscribeDriverBookingOffers()
            onDispose {
                driverBookingOfferRealtime.unsubscribeDriverBookingOffers()
            }
        } else {
            onDispose {}
        }
    }

    LaunchedEffect(authenticatedRoute, session.userId, session.roles) {
        when (authenticatedRoute) {
            RootRoute.PASSENGER -> {
                userPresenceCoordinator.markForeground(
                    role = UserPresenceRole.Passenger,
                    context = UserPresenceContext.PassengerHome,
                )
                if (session.userId != null) {
                    pushNotificationRegistrationCoordinator.registerCurrentDevice()
                }
            }
            RootRoute.DRIVER -> {
                userPresenceCoordinator.markForeground(
                    role = UserPresenceRole.Driver,
                    context = UserPresenceContext.DriverHome,
                )
                if (session.userId != null) {
                    pushNotificationRegistrationCoordinator.registerCurrentDevice()
                }
            }
            else -> userPresenceCoordinator.stop()
        }
    }

    NavHost(
        navController = navController,
        startDestination = uiState.startRoute,
    ) {
        starterNavGraph(
            onLoginClick = {
                markStarterSeenUseCase()
                navController.navigate(AuthRoute.LOGIN)
            },
            onCustomerRegisterClick = {
                markStarterSeenUseCase()
                navController.navigate("${AuthRoute.REGISTER}/passenger")
            },
            onDriverRegisterClick = {
                markStarterSeenUseCase()
                navController.navigate("${AuthRoute.REGISTER}/driver")
            },
        )
        authNavGraph(navController = navController) { route ->
            navController.navigateRoot(route)
        }
        passengerNavGraph(navController)
        driverNavGraph(
            navController = navController,
            onLogout = {
                navController.navigateRoot(RootRoute.AUTH)
            },
        )
        composable(DevRoute.MAP_PREVIEW) {
            MapPreviewScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}

private fun NavHostController.navigateRoot(route: String) {
    navigate(route, navOptions {
        popUpTo(graph.id) {
            inclusive = true
        }
        launchSingleTop = true
        restoreState = false
    })
}
