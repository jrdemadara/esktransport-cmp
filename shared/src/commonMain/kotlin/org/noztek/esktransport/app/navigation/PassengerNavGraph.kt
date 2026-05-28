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
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import asktransport_cmp.shared.generated.resources.Res
import asktransport_cmp.shared.generated.resources.compose_multiplatform
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Bot
import com.composables.icons.lucide.Grid2x2
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.User
import com.composables.icons.lucide.WalletCards
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
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
import org.noztek.esktransport.feature.passenger.trip_tracking.presentation.TripTrackingScreen

private const val ROUTE_HOME = "home"
private const val ROUTE_RIDE_PLANNER = "ride-planner"
private const val ROUTE_BOOKING_REVIEW = "booking-review"
private const val ROUTE_LOCATION_SEARCH = "location-search/{mode}"
private const val ARG_MODE = "mode"
private const val ROUTE_SERVICES = "services"
private const val ROUTE_KUDI = "kudi"
private const val ROUTE_ACTIVITY = "activity"
private const val ROUTE_PROFILE = "profile"
private const val ROUTE_TRIP_TRACKING = "trip-tracking"

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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val tabs = passengerTabs
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val locationMode = navBackStackEntry?.arguments?.read { getStringOrNull(ARG_MODE) } ?: "destination"
    val showChrome = currentRoute != ROUTE_RIDE_PLANNER &&
        currentRoute != ROUTE_BOOKING_REVIEW &&
        currentRoute?.startsWith(ROUTE_TRIP_TRACKING) != true &&
        currentRoute?.startsWith("location-search/") != true

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
                    navController.navigate("$ROUTE_TRIP_TRACKING/${event.bookingId}") {
                        popUpTo(ROUTE_HOME) { inclusive = false }
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
        topBar = {
            when {
                currentRoute == ROUTE_RIDE_PLANNER -> PassengerBackTopBar("Plan your trip") { navController.popBackStack() }
                currentRoute == ROUTE_BOOKING_REVIEW -> PassengerBackTopBar("Review Booking") { navController.popBackStack() }
                currentRoute?.startsWith(ROUTE_TRIP_TRACKING) == true -> PassengerBackTopBar("Trip Tracking") { navController.popBackStack() }
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
                NavigationBar(windowInsets = WindowInsets(0)) {
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
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
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
            composable(
                route = "$ROUTE_TRIP_TRACKING/{bookingId}",
                arguments = listOf(navArgument("bookingId") { type = NavType.StringType }),
            ) { backStackEntry ->
                TripTrackingScreen(bookingId = backStackEntry.arguments?.read { getStringOrNull("bookingId") }.orEmpty())
            }
            composable(ROUTE_SERVICES) { PlaceholderTabScreen("Services") }
            composable(ROUTE_KUDI) { PlaceholderTabScreen("Kudi AI") }
            composable(ROUTE_ACTIVITY) { PlaceholderTabScreen("Activity") }
            composable(ROUTE_PROFILE) { PassengerProfileScreen(onLogout = onLogout, contentPadding = innerPadding) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerBackTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Lucide.ArrowLeft, contentDescription = "Back")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerHomeTopBar(onProfileClick: () -> Unit) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        title = {},
        navigationIcon = { AppLogoBadge() },
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) {
                    Box {
                        Icon(Lucide.Bell, contentDescription = "Notifications")
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(9.dp)
                                .background(Color(0xFFE53935), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        )
                    }
                }
                IconButton(onClick = onProfileClick) {
                    Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Lucide.User, contentDescription = "Profile", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun AppLogoBadge() {
    Surface(modifier = Modifier.size(42.dp), shape = CircleShape, color = Color.Transparent) {
        Image(
            painter = painterResource(Res.drawable.compose_multiplatform),
            contentDescription = "Esk Transport",
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
    PassengerTab(ROUTE_HOME, "Home", Lucide.House),
    PassengerTab(ROUTE_SERVICES, "Services", Lucide.Grid2x2),
    PassengerTab(ROUTE_KUDI, "Kudi AI", Lucide.Bot),
    PassengerTab(ROUTE_ACTIVITY, "Activity", Lucide.WalletCards),
    PassengerTab(ROUTE_PROFILE, "Profile", Lucide.User),
)


private fun NavHostController.navigatePassengerRoot(route: String) {
    navigate(route, navOptions {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
        restoreState = false
    })
}
