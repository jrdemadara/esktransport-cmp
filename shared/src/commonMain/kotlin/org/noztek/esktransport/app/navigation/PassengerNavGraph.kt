package org.noztek.esktransport.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.savedstate.read
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.Bell
import com.composables.icons.heroicons.outline.Sparkles
import com.composables.icons.heroicons.outline.SquaresPlus
import com.composables.icons.heroicons.outline.Home
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.User
import com.composables.icons.heroicons.outline.RectangleStack
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.logo
import esktransport.shared.generated.resources.logo_nobg
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.realtime.passenger.PassengerRealtimeCoordinator
import org.noztek.esktransport.feature.common.presence.domain.lifecycle.UserPresenceCoordinator
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceContext
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceRole
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
import org.noztek.esktransport.feature.passenger.booking_review.presentation.BookingReviewScreen
import org.noztek.esktransport.feature.passenger.booking_review.presentation.BookingReviewUiEvent
import org.noztek.esktransport.feature.passenger.booking_review.presentation.BookingReviewViewModel
import org.noztek.esktransport.feature.passenger.home.presentation.PassengerHomeScreen
import org.noztek.esktransport.feature.passenger.home.presentation.PassengerProfileScreen
import org.noztek.esktransport.feature.passenger.location_search.presentation.LocationSearchScreen
import org.noztek.esktransport.feature.passenger.location_search.presentation.SelectedLocation
import org.noztek.esktransport.feature.passenger.ride_planner.presentation.RidePlannerScreen
import org.noztek.esktransport.feature.passenger.ride_planner.presentation.RidePlannerUiEvent
import org.noztek.esktransport.feature.passenger.ride_planner.presentation.RidePlannerViewModel
import org.noztek.esktransport.feature.passenger.session.presentation.PassengerSessionUiEvent
import org.noztek.esktransport.feature.passenger.session.presentation.PassengerSessionViewModel
import org.noztek.esktransport.feature.passenger.trip_tracking.presentation.TripTrackingScreen

private const val ROUTE_HOME = "home"
private const val ROUTE_RIDE_PLANNER = "ride-planner"
private const val ROUTE_RIDE_PLANNER_WITH_VEHICLE = "ride-planner/{vehicleTypeIndex}"
private const val ROUTE_BOOKING_REVIEW = "booking-review"
private const val ROUTE_LOCATION_SEARCH = "location-search/{mode}"
private const val ARG_MODE = "mode"
private const val ARG_VEHICLE_TYPE_INDEX = "vehicleTypeIndex"
private const val ROUTE_SERVICES = "services"
private const val ROUTE_KUDI = "kudi"
private const val ROUTE_ACTIVITY = "activity"
private const val ROUTE_PROFILE = "profile"

fun NavGraphBuilder.passengerNavGraph(navController: NavHostController) {
    navigation(startDestination = PassengerRoute.HOME, route = RootRoute.PASSENGER) {
        composable(PassengerRoute.HOME) {
            PassengerShell(onLogout = { navController.navigatePassengerRoot(RootRoute.AUTH) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerShell(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val ridePlannerViewModel: RidePlannerViewModel = koinViewModel()
    val bookingReviewViewModel: BookingReviewViewModel = koinViewModel()
    val passengerSessionViewModel: PassengerSessionViewModel = koinViewModel()
    val passengerRealtimeCoordinator: PassengerRealtimeCoordinator = koinInject()
    val userPresenceCoordinator: UserPresenceCoordinator = koinInject()
    val bookingReviewUiState by bookingReviewViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeTripTrackingBookingId by remember { mutableStateOf<String?>(null) }
    val tabs = passengerTabs
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val locationMode = navBackStackEntry?.arguments?.read { getStringOrNull(ARG_MODE) } ?: "destination"
    val isRidePlannerRoute = currentRoute == ROUTE_RIDE_PLANNER ||
        currentRoute == ROUTE_RIDE_PLANNER_WITH_VEHICLE
    val isTripTrackingActive = activeTripTrackingBookingId != null
    val showChrome = !isTripTrackingActive &&
        !isRidePlannerRoute &&
        currentRoute != ROUTE_BOOKING_REVIEW &&
        currentRoute?.startsWith("location-search/") != true

    LaunchedEffect(currentRoute, bookingReviewUiState.isSearchingForRider, activeTripTrackingBookingId) {
        userPresenceCoordinator.updateContext(
            role = UserPresenceRole.Passenger,
            context = if (activeTripTrackingBookingId != null) {
                UserPresenceContext.TripTracking
            } else {
                currentRoute.toUserPresenceContext(
                    isSearchingForRider = bookingReviewUiState.isSearchingForRider,
                )
            },
        )
    }

    LaunchedEffect(Unit) {
        ridePlannerViewModel.uiEvents.collectLatest { event ->
            when (event) {
                is RidePlannerUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is RidePlannerUiEvent.NavigateToBookingReview -> navController.navigate(ROUTE_BOOKING_REVIEW)
            }
        }
    }

    LaunchedEffect(Unit) {
        bookingReviewViewModel.uiEvents.collectLatest { event ->
            when (event) {
                is BookingReviewUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is BookingReviewUiEvent.NavigateToTripTracking -> {
                    activeTripTrackingBookingId = event.bookingId
                }
            }
        }
    }

    LaunchedEffect(passengerRealtimeCoordinator) {
        passengerRealtimeCoordinator.subscribePassengerDriverAssigned()
        launch {
            passengerRealtimeCoordinator.passengerBookingAccepted().collectLatest { event ->
                activeTripTrackingBookingId = event.bookingPublicId
            }
        }
        launch {
            passengerRealtimeCoordinator.passengerBookingCancelled().collectLatest { event ->
                if (event.cancelledBy == "passenger") return@collectLatest
                bookingReviewViewModel.showReviewSheet()
                activeTripTrackingBookingId = null
                navController.navigate(ROUTE_BOOKING_REVIEW) {
                    popUpTo(ROUTE_HOME) { inclusive = false }
                    launchSingleTop = true
                }
                snackbarHostState.showSnackbar("Trip cancelled by driver.")
            }
        }
        launch {
            passengerRealtimeCoordinator.passengerTripLocationUpdated().collectLatest { event ->
                if (event.bookingPublicId == activeTripTrackingBookingId) {
                    snackbarHostState.showSnackbar(
                        "Driver location received: ${event.latitude.formatCoordinate()}, ${event.longitude.formatCoordinate()}",
                    )
                }
            }
        }
    }

    DisposableEffect(passengerRealtimeCoordinator) {
        onDispose {
            passengerRealtimeCoordinator.unsubscribePassengerDriverAssigned()
        }
    }

    LaunchedEffect(passengerSessionViewModel) {
        launch {
            passengerSessionViewModel.uiEvents.collectLatest { event ->
                when (event) {
                    is PassengerSessionUiEvent.NavigateToTripTracking -> {
                        activeTripTrackingBookingId = event.bookingPublicId
                    }
                }
            }
        }
        passengerSessionViewModel.restoreActiveBooking()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            when {
                isTripTrackingActive -> Unit
                isRidePlannerRoute -> PassengerBackTopBar("Plan your trip") { navController.popBackStack() }
                currentRoute == ROUTE_BOOKING_REVIEW -> PassengerBackTopBar("Review Booking") { navController.popBackStack() }
                currentRoute?.startsWith("location-search/") == true -> {
                    PassengerBackTopBar(if (locationMode == "pickup") "Search Pickup" else "Search Destination") {
                        navController.popBackStack()
                    }
                }
                else -> PassengerHomeTopBar(onProfileClick = {
                    navController.navigate(ROUTE_PROFILE) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
        },
        bottomBar = {
            if (showChrome) {
                NavigationBar(
                    windowInsets = WindowInsets(0),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                ) {
                    tabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = ROUTE_HOME,
                enterTransition = { fadeIn(animationSpec = tween(180)) },
                exitTransition = { fadeOut(animationSpec = tween(120)) },
                popEnterTransition = { fadeIn(animationSpec = tween(180)) },
                popExitTransition = { fadeOut(animationSpec = tween(120)) },
            ) {
                composable(ROUTE_HOME) {
                    PassengerHomeScreen(
                        onWhereToClick = { navController.navigate(ROUTE_RIDE_PLANNER) },
                        onSuggestionClick = { vehicleTypeIndex ->
                            navController.navigate("ride-planner/$vehicleTypeIndex")
                        },
                        contentPadding = innerPadding,
                    )
                }
            composable(ROUTE_RIDE_PLANNER) {
                val pickupLocation by ridePlannerViewModel.pickupLocation.collectAsState()
                val destinationLocation by ridePlannerViewModel.destinationLocation.collectAsState()
                RidePlannerScreen(
                    contentPadding = innerPadding,
                    onPickupClick = { navController.navigate("location-search/pickup") },
                    onDestinationClick = { navController.navigate("location-search/destination") },
                    onUseCurrentLocationClick = {
                        scope.launch {
                            val currentLocationLabel = ridePlannerViewModel.resolveCurrentLocationLabel()
                            val currentLocationPoint = ridePlannerViewModel.resolveCurrentLocationPoint()
                            if (currentLocationLabel != null && currentLocationPoint != null) {
                                ridePlannerViewModel.setPickupLocation(currentLocationLabel, currentLocationPoint)
                            } else {
                                snackbarHostState.showSnackbar("Unable to get current location. Check location permission/GPS.")
                            }
                        }
                    },
                    pickupLocation = pickupLocation,
                    destinationLocation = destinationLocation,
                    viewModel = ridePlannerViewModel,
                )
            }
            composable(
                route = ROUTE_RIDE_PLANNER_WITH_VEHICLE,
                arguments = listOf(navArgument(ARG_VEHICLE_TYPE_INDEX) { type = NavType.IntType }),
            ) { backStackEntry ->
                val selectedVehicleTypeIndex = backStackEntry.arguments?.read {
                    getInt(ARG_VEHICLE_TYPE_INDEX)
                } ?: 0
                LaunchedEffect(selectedVehicleTypeIndex) {
                    ridePlannerViewModel.setVehicleType(selectedVehicleTypeIndex)
                }
                val pickupLocation by ridePlannerViewModel.pickupLocation.collectAsState()
                val destinationLocation by ridePlannerViewModel.destinationLocation.collectAsState()
                RidePlannerScreen(
                    contentPadding = innerPadding,
                    onPickupClick = { navController.navigate("location-search/pickup") },
                    onDestinationClick = { navController.navigate("location-search/destination") },
                    onUseCurrentLocationClick = {
                        scope.launch {
                            val currentLocationLabel = ridePlannerViewModel.resolveCurrentLocationLabel()
                            val currentLocationPoint = ridePlannerViewModel.resolveCurrentLocationPoint()
                            if (currentLocationLabel != null && currentLocationPoint != null) {
                                ridePlannerViewModel.setPickupLocation(currentLocationLabel, currentLocationPoint)
                            } else {
                                snackbarHostState.showSnackbar("Unable to get current location. Check location permission/GPS.")
                            }
                        }
                    },
                    pickupLocation = pickupLocation,
                    destinationLocation = destinationLocation,
                    viewModel = ridePlannerViewModel,
                )
            }
            composable(
                route = ROUTE_LOCATION_SEARCH,
                arguments = listOf(navArgument(ARG_MODE) { type = NavType.StringType }),
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.read { getStringOrNull(ARG_MODE) } ?: "destination"
                LocationSearchScreen(
                    mode = mode,
                    contentPadding = innerPadding,
                    onLocationSelected = { selected: SelectedLocation ->
                        if (mode == "pickup") {
                            ridePlannerViewModel.setPickupLocation(selected.label, selected.point)
                        } else {
                            ridePlannerViewModel.setDestinationLocation(selected.label, selected.point)
                        }
                        navController.popBackStack()
                    },
                )
            }
            composable(ROUTE_BOOKING_REVIEW) {
                val pickupLocation by ridePlannerViewModel.pickupLocation.collectAsState()
                val destinationLocation by ridePlannerViewModel.destinationLocation.collectAsState()
                val pickupPoint by ridePlannerViewModel.pickupPoint.collectAsState()
                val destinationPoint by ridePlannerViewModel.destinationPoint.collectAsState()
                val routePoints by ridePlannerViewModel.routePoints.collectAsState()
                val passengerCount by ridePlannerViewModel.passengerCount.collectAsState()
                val vehicleTypeIndex by ridePlannerViewModel.selectedVehicleType.collectAsState()
                val reviewInput = if (pickupPoint != null && destinationPoint != null) {
                    BookingReviewInput(
                        pickupLocation = pickupLocation,
                        destinationLocation = destinationLocation,
                        pickupPoint = pickupPoint!!,
                        destinationPoint = destinationPoint!!,
                        passengerCount = passengerCount,
                        vehicleTypeIndex = vehicleTypeIndex,
                        routePoints = routePoints,
                    )
                } else null
                if (reviewInput != null) {
                    BookingReviewScreen(contentPadding = innerPadding, input = reviewInput, viewModel = bookingReviewViewModel)
                } else {
                    LaunchedEffect(Unit) {
                        snackbarHostState.showSnackbar("Missing booking review data.")
                        navController.popBackStack()
                    }
                }
            }
            composable(ROUTE_SERVICES) { PlaceholderTabScreen("Services") }
            composable(ROUTE_KUDI) { PlaceholderTabScreen("Kudi AI") }
            composable(ROUTE_ACTIVITY) { PlaceholderTabScreen("Activity") }
                composable(ROUTE_PROFILE) { PassengerProfileScreen(onLogout = onLogout, contentPadding = innerPadding) }
            }

            activeTripTrackingBookingId?.let { bookingId ->
                TripTrackingScreen(
                    bookingId = bookingId,
                    onCancelled = {
                        activeTripTrackingBookingId = null
                        bookingReviewViewModel.showReviewSheet()
                        navController.navigate(ROUTE_BOOKING_REVIEW) {
                            popUpTo(ROUTE_HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onCompleted = {
                        activeTripTrackingBookingId = null
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerBackTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Heroicons.Outline.ArrowLeft, contentDescription = "Back")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerHomeTopBar(onProfileClick: () -> Unit) {
    CenterAlignedTopAppBar(
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {},
        navigationIcon = { AppLogoBadge() },
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) {
                    Box {
                        Icon(Heroicons.Outline.Bell, contentDescription = "Notifications")
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(9.dp)
                                .background(Color(0xFFE53935), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.background, CircleShape),
                        )
                    }
                }
                IconButton(onClick = onProfileClick) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Heroicons.Outline.User,
                                contentDescription = "Profile",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun AppLogoBadge() {
    Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Color.Transparent) {
        Image(
            painter = painterResource(Res.drawable.logo_nobg),
            contentDescription = "eSK0Transport",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun PlaceholderTabScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

private data class PassengerTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val passengerTabs = listOf(
    PassengerTab(ROUTE_HOME, "Home", Heroicons.Outline.Home),
    PassengerTab(ROUTE_SERVICES, "Services", Heroicons.Outline.SquaresPlus),
    PassengerTab(ROUTE_KUDI, "Kudi AI", Heroicons.Outline.Sparkles),
    PassengerTab(ROUTE_ACTIVITY, "Activity", Heroicons.Outline.RectangleStack),
    PassengerTab(ROUTE_PROFILE, "Profile", Heroicons.Outline.User),
)

private fun String?.toUserPresenceContext(isSearchingForRider: Boolean): UserPresenceContext {
    return when {
        this == ROUTE_BOOKING_REVIEW && isSearchingForRider -> UserPresenceContext.BookingSearch
        this == ROUTE_BOOKING_REVIEW -> UserPresenceContext.BookingReview
        this == ROUTE_HOME -> UserPresenceContext.PassengerHome
        this == ROUTE_RIDE_PLANNER || this == ROUTE_RIDE_PLANNER_WITH_VEHICLE -> UserPresenceContext.RidePlanner
        this?.startsWith("location-search/") == true -> UserPresenceContext.LocationSearch
        this == ROUTE_SERVICES -> UserPresenceContext.Services
        this == ROUTE_KUDI -> UserPresenceContext.Kudi
        this == ROUTE_ACTIVITY -> UserPresenceContext.Activity
        this == ROUTE_PROFILE -> UserPresenceContext.Profile
        else -> UserPresenceContext.PassengerHome
    }
}

private fun Double.formatCoordinate(): String {
    val scaled = kotlin.math.round(this * 100_000.0) / 100_000.0
    return scaled.toString()
}


private fun NavHostController.navigatePassengerRoot(route: String) {
    navigate(route, navOptions {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
        restoreState = false
    })
}
