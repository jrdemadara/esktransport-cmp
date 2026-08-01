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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.composables.icons.heroicons.outline.Cog6Tooth
import com.composables.icons.heroicons.outline.Sparkles
import com.composables.icons.heroicons.outline.Home
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.User
import com.composables.icons.heroicons.outline.RectangleStack
import com.composables.icons.heroicons.outline.Wallet
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.logo
import esktransport.shared.generated.resources.logo_nobg
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.realtime.passenger.PassengerRealtimeCoordinator
import org.noztek.esktransport.core.session.SessionManager
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.common.cashout.presentation.CashoutScreen
import org.noztek.esktransport.feature.common.chat.domain.model.TripChatParticipantRole
import org.noztek.esktransport.feature.common.chat.presentation.TripChatScreen
import org.noztek.esktransport.feature.common.presence.domain.lifecycle.UserPresenceCoordinator
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceContext
import org.noztek.esktransport.feature.common.presence.domain.model.UserPresenceRole
import org.noztek.esktransport.feature.common.topup.presentation.TopUpScreen
import org.noztek.esktransport.feature.passenger.booking_review.domain.model.BookingReviewInput
import org.noztek.esktransport.feature.passenger.booking_review.presentation.BookingReviewScreen
import org.noztek.esktransport.feature.passenger.booking_review.presentation.BookingReviewUiEvent
import org.noztek.esktransport.feature.passenger.booking_review.presentation.BookingReviewViewModel
import org.noztek.esktransport.feature.passenger.activity.presentation.ActivityScreen
import org.noztek.esktransport.feature.passenger.home.presentation.PassengerHomeScreen
import org.noztek.esktransport.feature.passenger.kudi.presentation.KudiScreen
import org.noztek.esktransport.feature.passenger.location_search.presentation.LocationSearchScreen
import org.noztek.esktransport.feature.passenger.location_search.presentation.SelectedLocation
import org.noztek.esktransport.feature.passenger.ride_planner.presentation.RidePlannerScreen
import org.noztek.esktransport.feature.passenger.ride_planner.presentation.RidePlannerUiEvent
import org.noztek.esktransport.feature.passenger.ride_planner.presentation.RidePlannerViewModel
import org.noztek.esktransport.feature.passenger.settings.presentation.AccountSettingsScreen
import org.noztek.esktransport.feature.passenger.settings.presentation.SavedPlacesScreen
import org.noztek.esktransport.feature.passenger.settings.presentation.SettingsScreen
import org.noztek.esktransport.feature.passenger.session.presentation.PassengerSessionUiEvent
import org.noztek.esktransport.feature.passenger.session.presentation.PassengerSessionViewModel
import org.noztek.esktransport.feature.passenger.trip_tracking.presentation.TripTrackingScreen
import org.noztek.esktransport.feature.passenger.wallet.presentation.WalletScreen
import org.noztek.esktransport.feature.driver.onboarding.presentation.CapturedDocumentPreviewImage

private const val ROUTE_HOME = "home"
private const val ROUTE_WALLET = "wallet"
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
private const val ROUTE_PASSENGER_ACCOUNT_SETTINGS = "passenger/settings/account"
private const val ROUTE_PASSENGER_SAVED_PLACES = "passenger/settings/saved-places"
private const val ROUTE_PASSENGER_TOP_UP = "passenger/wallet/top-up"
private const val ROUTE_PASSENGER_CASHOUT = "passenger/wallet/cashout"

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
    val sessionManager: SessionManager = koinInject()
    val passengerName by sessionManager.userName.collectAsState(initial = null)
    val bookingReviewUiState by bookingReviewViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeTripTrackingBookingId by remember { mutableStateOf<String?>(null) }
    var isTripTrackingVisible by remember { mutableStateOf(false) }
    var activeTripChatBookingId by remember { mutableStateOf<String?>(null) }
    val tabs = passengerTabs
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val locationMode = navBackStackEntry?.arguments?.read { getStringOrNull(ARG_MODE) } ?: "destination"
    val isRidePlannerRoute = currentRoute == ROUTE_RIDE_PLANNER ||
        currentRoute == ROUTE_RIDE_PLANNER_WITH_VEHICLE
    val isTripTrackingActive = activeTripTrackingBookingId != null && isTripTrackingVisible
    val showChrome = !isTripTrackingActive &&
        !isRidePlannerRoute &&
        currentRoute != ROUTE_BOOKING_REVIEW &&
        currentRoute != ROUTE_KUDI &&
        currentRoute != ROUTE_PASSENGER_TOP_UP &&
        currentRoute != ROUTE_PASSENGER_CASHOUT &&
        currentRoute?.startsWith("location-search/") != true

    LaunchedEffect(currentRoute, bookingReviewUiState.isSearchingForRider, activeTripTrackingBookingId, isTripTrackingVisible) {
        userPresenceCoordinator.updateContext(
            role = UserPresenceRole.Passenger,
            context = if (isTripTrackingActive) {
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
                    isTripTrackingVisible = true
                    activeTripChatBookingId = null
                }
            }
        }
    }

    LaunchedEffect(passengerRealtimeCoordinator) {
        passengerRealtimeCoordinator.subscribePassengerDriverAssigned()
        launch {
            passengerRealtimeCoordinator.passengerBookingAccepted().collectLatest { event ->
                activeTripTrackingBookingId = event.bookingPublicId
                isTripTrackingVisible = true
            }
        }
        launch {
            passengerRealtimeCoordinator.passengerBookingCancelled().collectLatest { event ->
                if (event.cancelledBy == "passenger") return@collectLatest
                bookingReviewViewModel.showReviewSheet()
                activeTripTrackingBookingId = null
                isTripTrackingVisible = false
                activeTripChatBookingId = null
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
                        isTripTrackingVisible = true
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
                currentRoute == ROUTE_KUDI -> Unit
                currentRoute == ROUTE_PROFILE -> Unit
                currentRoute == ROUTE_PASSENGER_ACCOUNT_SETTINGS -> Unit
                currentRoute == ROUTE_PASSENGER_SAVED_PLACES -> Unit
                currentRoute == ROUTE_PASSENGER_TOP_UP -> Unit
                currentRoute == ROUTE_PASSENGER_CASHOUT -> Unit
                currentRoute?.startsWith("location-search/") == true -> {
                    PassengerBackTopBar(if (locationMode == "pickup") "Search Pickup" else "Search Destination") {
                        navController.popBackStack()
                    }
                }
                else -> PassengerHomeTopBar(
                    greetingName = passengerName,
                    title = when (currentRoute) {
                        ROUTE_WALLET -> "Wallet"
                        ROUTE_ACTIVITY -> "Activity"
                        else -> null
                    },
                    onProfileClick = {
                        navController.navigate(ROUTE_PROFILE) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
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
                        onPlaceClick = { label, point ->
                            ridePlannerViewModel.setDestinationLocation(label, point)
                            navController.navigate(ROUTE_RIDE_PLANNER)
                        },
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
            composable(ROUTE_WALLET) {
                WalletScreen(
                    contentPadding = innerPadding,
                    onTopUpClick = {
                        navController.navigate(ROUTE_PASSENGER_TOP_UP) {
                            launchSingleTop = true
                        }
                    },
                    onCashoutClick = {
                        navController.navigate(ROUTE_PASSENGER_CASHOUT) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(ROUTE_PASSENGER_TOP_UP) {
                TopUpScreen(onBackClick = { navController.popBackStack() })
            }
            composable(ROUTE_PASSENGER_CASHOUT) {
                CashoutScreen(onBackClick = { navController.popBackStack() })
            }
            composable(ROUTE_KUDI) {
                KudiScreen(
                    onProfileClick = {
                        navController.navigate(ROUTE_PROFILE) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(ROUTE_ACTIVITY) {
                ActivityScreen(
                    contentPadding = innerPadding,
                    onTrackTripClick = { bookingId ->
                        activeTripTrackingBookingId = bookingId
                        isTripTrackingVisible = true
                    },
                )
            }
                composable(ROUTE_PROFILE) {
                    SettingsScreen(
                        onBackClick = {
                            navController.navigate(ROUTE_HOME) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        contentPadding = innerPadding,
                        onAccountClick = {
                            navController.navigate(ROUTE_PASSENGER_ACCOUNT_SETTINGS) {
                                launchSingleTop = true
                            }
                        },
                        onSavedPlacesClick = {
                            navController.navigate(ROUTE_PASSENGER_SAVED_PLACES) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(ROUTE_PASSENGER_ACCOUNT_SETTINGS) {
                    AccountSettingsScreen(
                        onBackClick = { navController.popBackStack() },
                        onLogout = onLogout,
                        contentPadding = innerPadding,
                    )
                }
                composable(ROUTE_PASSENGER_SAVED_PLACES) {
                    SavedPlacesScreen(
                        onBackClick = { navController.popBackStack() },
                        contentPadding = innerPadding,
                    )
                }
            }

            if (isTripTrackingVisible) activeTripTrackingBookingId?.let { bookingId ->
                TripTrackingScreen(
                    bookingId = bookingId,
                    onCancelled = {
                        activeTripTrackingBookingId = null
                        isTripTrackingVisible = false
                        activeTripChatBookingId = null
                        bookingReviewViewModel.showReviewSheet()
                        navController.navigate(ROUTE_BOOKING_REVIEW) {
                            popUpTo(ROUTE_HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onCompleted = {
                        activeTripTrackingBookingId = null
                        isTripTrackingVisible = false
                        activeTripChatBookingId = null
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onHomeClick = {
                        isTripTrackingVisible = false
                        activeTripChatBookingId = null
                        navController.navigate(ROUTE_HOME) {
                            popUpTo(ROUTE_HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onChatClick = {
                        activeTripChatBookingId = bookingId
                    },
                )
            }

            activeTripChatBookingId?.let { bookingId ->
                TripChatScreen(
                    bookingPublicId = bookingId,
                    role = TripChatParticipantRole.Passenger,
                    onBack = { activeTripChatBookingId = null },
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
private fun PassengerHomeTopBar(
    greetingName: String?,
    title: String? = null,
    onProfileClick: () -> Unit,
    profilePhotoBytes: ByteArray? = null,
) {
    TopAppBar(
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {
            PassengerTopBarBrand(greetingName = greetingName, title = title)
        },
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
                    PassengerProfileAvatar(
                        name = greetingName,
                        profilePhotoBytes = profilePhotoBytes,
                    )
                }
            }
        },
    )
}

@Composable
private fun PassengerTopBarBrand(
    greetingName: String?,
    title: String?,
) {
    val displayName = greetingName
        ?.trim()
        ?.uppercaseFirstLetterOfEachWord()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppLogoBadge()
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else if (!displayName.isNullOrBlank()) {
            Text(
                text = "Hello, $displayName!",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PassengerProfileAvatar(
    name: String?,
    profilePhotoBytes: ByteArray?,
) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        if (profilePhotoBytes != null) {
            CapturedDocumentPreviewImage(
                bytes = profilePhotoBytes,
                contentDescription = "Profile",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = name.initials(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun AppLogoBadge() {
    Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = Color.Transparent) {
        Image(
            painter = painterResource(Res.drawable.logo_nobg),
            contentDescription = "eSK0Transport",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}

private fun String?.initials(): String {
    val parts = this
        ?.trim()
        ?.split(Regex("\\s+"))
        ?.filter { it.isNotBlank() }
        .orEmpty()

    return parts
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifBlank { "P" }
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
    PassengerTab(ROUTE_WALLET, "Wallet", Heroicons.Outline.Wallet),
    PassengerTab(ROUTE_KUDI, "Ask Kudi", Heroicons.Outline.Sparkles),
    PassengerTab(ROUTE_ACTIVITY, "Activity", Heroicons.Outline.RectangleStack),
    PassengerTab(ROUTE_PROFILE, "Settings", Heroicons.Outline.Cog6Tooth),
)

private fun String?.toUserPresenceContext(isSearchingForRider: Boolean): UserPresenceContext {
    return when {
        this == ROUTE_BOOKING_REVIEW && isSearchingForRider -> UserPresenceContext.BookingSearch
        this == ROUTE_BOOKING_REVIEW -> UserPresenceContext.BookingReview
        this == ROUTE_HOME -> UserPresenceContext.PassengerHome
        this == ROUTE_RIDE_PLANNER || this == ROUTE_RIDE_PLANNER_WITH_VEHICLE -> UserPresenceContext.RidePlanner
        this?.startsWith("location-search/") == true -> UserPresenceContext.LocationSearch
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
