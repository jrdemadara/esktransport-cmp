package org.noztek.esktransport.feature.driver.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import com.composables.icons.heroicons.solid.ChartBar
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.solid.UserCircle
import com.composables.icons.heroicons.solid.Flag
import com.composables.icons.heroicons.outline.QueueList
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Bars3
import com.composables.icons.heroicons.outline.MagnifyingGlass
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.AdjustmentsHorizontal
import com.composables.icons.heroicons.solid.Star
import com.composables.icons.heroicons.outline.User
import org.noztek.esktransport.core.audio.SoundEffect
import org.noztek.esktransport.core.audio.SoundEffectPlayer
import org.noztek.esktransport.core.map.MapCameraDefaults
import org.noztek.esktransport.core.map.MapPoint
import org.noztek.esktransport.core.map.MapboxConfig
import org.noztek.esktransport.core.map.PlatformMapView

private val GoButtonBlue = Color(0xFF2F80ED)
private val GoButtonSubmittingGray = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(
    viewModel: DriverHomeViewModel = koinViewModel(),
    mapboxConfig: MapboxConfig = koinInject(),
    cameraDefaults: MapCameraDefaults = koinInject(),
    soundEffectPlayer: SoundEffectPlayer = koinInject(),
    onNavigateToTrip: (bookingPublicId: String) -> Unit = {},
) {
    val homeState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val safetySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lifecycleOwner = LocalLifecycleOwner.current
    var showSafetySheet by remember { mutableStateOf(false) }
    var lastAvailability by remember { mutableStateOf(homeState.isAvailable) }

    LaunchedEffect(viewModel) {
        viewModel.startRealtime()
        viewModel.refreshAvailability()
        viewModel.uiEvents.collect { event ->
            when (event) {
                is DriverHomeUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is DriverHomeUiEvent.NavigateToTrip -> onNavigateToTrip(event.bookingPublicId)
            }
        }
    }

    DisposableEffect(viewModel) {
        onDispose { viewModel.stopRealtime() }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.refreshAvailability()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(homeState.isAvailable) {
        if (!lastAvailability && homeState.isAvailable) {
            soundEffectPlayer.play(SoundEffect.Online)
        }
        lastAvailability = homeState.isAvailable
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = { DriverDrawerContent() },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0),
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                PlatformMapView(
                    modifier = Modifier.fillMaxSize(),
                    config = mapboxConfig,
                    cameraCenter = MapPoint(latitude = 6.6881, longitude = 124.6779),
                    cameraDefaults = cameraDefaults.copy(zoom = 12.4, pitch = 0.0),
                )

                DriverHomeToolbar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    onMenuClick = { scope.launch { drawerState.open() } },
                )

                DriverFloatingActions(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 92.dp),
                    onMockOfferClick = viewModel::showMockIncomingOffer,
                    onSafetyClick = { showSafetySheet = true },
                )

                if (!homeState.isAvailable) {
                    DriverGoButton(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp),
                        isSubmitting = homeState.isSubmitting,
                        onClick = {
                            soundEffectPlayer.play(SoundEffect.Tap)
                            viewModel.onGoToggle()
                        },
                    )
                }

                if (homeState.currentOffer == null) {
                    DriverAvailabilitySheet(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        isAvailable = homeState.isAvailable,
                        isSubmitting = homeState.isSubmitting,
                    )
                }

                homeState.currentOffer?.let { offer ->
                    DriverIncomingOfferOverlay(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        offer = offer,
                        isAccepting = homeState.isAcceptingOffer,
                        soundEffectPlayer = soundEffectPlayer,
                        onDecline = {
                            soundEffectPlayer.play(SoundEffect.Close)
                            viewModel.dismissOfferSheet()
                        },
                        onAccept = viewModel::acceptCurrentOffer,
                    )
                }
            }
        }
    }

    if (showSafetySheet) {
        ModalBottomSheet(
            onDismissRequest = { showSafetySheet = false },
            sheetState = safetySheetState,
        ) {
            SafetyToolkitSheet()
        }
    }
}

@Composable
private fun DriverHomeToolbar(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Heroicons.Outline.Bars3,
                    contentDescription = "Menu",
                )
            }
            Surface(shape = RoundedCornerShape(999.dp), color = Color.Black) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("₱", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    Text(
                        "0.00",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Heroicons.Outline.MagnifyingGlass,
                    contentDescription = "Search",
                )
            }
        }
    }
}

@Composable
private fun DriverFloatingActions(
    modifier: Modifier = Modifier,
    onMockOfferClick: () -> Unit,
    onSafetyClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircleIconButton(onClick = onMockOfferClick) {
            Icon(
                imageVector = Heroicons.Solid.Flag,
                contentDescription = "Incoming request",
            )
        }
        CircleIconButton(onClick = {}) {
            Icon(
                imageVector = Heroicons.Solid.ChartBar,
                contentDescription = "Stats",
            )
        }
        CircleIconButton(onClick = onSafetyClick) {
            Icon(
                imageVector = Heroicons.Outline.ShieldCheck,
                contentDescription = "Safety",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DriverGoButton(
    modifier: Modifier = Modifier,
    isSubmitting: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.size(94.dp),
        contentAlignment = Alignment.Center,
    ) {
        val transition = rememberInfiniteTransition(label = "go-radiating")
        val waveScale = transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.75f,
            animationSpec = infiniteRepeatable(
                animation = tween(1900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "go-wave-scale",
        )
        val waveAlpha = transition.animateFloat(
            initialValue = 0.18f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "go-wave-alpha",
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(if (isSubmitting) 1f else waveScale.value)
                .graphicsLayer(alpha = if (isSubmitting) 0f else waveAlpha.value)
                .clip(CircleShape)
                .background(if (isSubmitting) GoButtonSubmittingGray else GoButtonBlue),
        )
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(94.dp),
                strokeWidth = 3.dp,
                color = Color.White,
            )
        }
        Surface(
            onClick = onClick,
            enabled = !isSubmitting,
            shape = CircleShape,
            color = if (isSubmitting) GoButtonSubmittingGray else GoButtonBlue,
            shadowElevation = 10.dp,
        ) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                Text(
                    "GO",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DriverAvailabilitySheet(
    modifier: Modifier = Modifier,
    isAvailable: Boolean,
    isSubmitting: Boolean,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Heroicons.Outline.AdjustmentsHorizontal,
                    contentDescription = "Filter",
                )
                Text(
                    if (isSubmitting) "Going online..." else if (isAvailable) "You're online" else "You're offline",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Normal,
                )
                Icon(
                    imageVector = Heroicons.Outline.QueueList,
                    contentDescription = "List",
                )
            }
            AnimatedVisibility(
                visible = isAvailable && !isSubmitting,
                enter = fadeIn(animationSpec = tween(240)) + slideInVertically(
                    animationSpec = tween(280, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 2 },
                ),
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
private fun DriverIncomingOfferOverlay(
    modifier: Modifier = Modifier,
    offer: DriverHomeBookingOfferUiModel,
    isAccepting: Boolean,
    soundEffectPlayer: SoundEffectPlayer,
    onDecline: () -> Unit,
    onAccept: () -> Unit,
) {
    var secondsLeft by remember(offer.bookingPublicId) { mutableIntStateOf(10) }
    val timeProgress = remember(offer.bookingPublicId) { Animatable(1f) }

    LaunchedEffect(offer.bookingPublicId) {
        secondsLeft = 10
        timeProgress.snapTo(1f)
        soundEffectPlayer.play(SoundEffect.Alert)
        launch {
            timeProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 10_000),
            )
        }
        repeat(10) { tick ->
            delay(1000)
            secondsLeft = 9 - tick
        }
        soundEffectPlayer.play(SoundEffect.Denied)
        onDecline()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Priority Request", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(999.dp), color = Color.Black) {
                    Text(
                        "00:${secondsLeft.toString().padStart(2, '0')}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            LinearProgressIndicator(
                progress = { timeProgress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = Color(0xFF1D4ED8),
                trackColor = Color(0xFF1D4ED8).copy(alpha = 0.2f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Heroicons.Outline.User,
                                contentDescription = "Passenger",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Column {
                        Text(offer.passengerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Heroicons.Solid.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFF59E0B),
                            )
                            Text("4.6", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        offer.fareLabel.replace("PHP", "₱"),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text("1.6 km", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
            RouteSummary(offer = offer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDecline,
                    enabled = !isAccepting,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Decline", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAccept,
                    enabled = !isAccepting,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4FD9)),
                ) {
                    Text(if (isAccepting) "Accepting..." else "Accept", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RouteSummary(offer: DriverHomeBookingOfferUiModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Heroicons.Solid.UserCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color(0xFFF59E0B),
            )
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(48.dp)
                    .background(Color(0xFFD7D7D7)),
            )
            Icon(
                imageVector = Heroicons.Outline.MapPin,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Column {
            Text("Pickup point", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Text(offer.pickupLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
            Text("Dropoff point", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Text(offer.destinationLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DriverDrawerContent(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth(0.78f)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(58.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Heroicons.Outline.User,
                            contentDescription = "Driver",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column {
                    Text("Driver", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Heroicons.Solid.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFF59E0B),
                        )
                        Text("4.50", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            HorizontalDivider()
            DrawerMenuItem("Inbox", badge = "3")
            DrawerMenuItem("Refer Friend")
            DrawerMenuItem("Opportunities", badge = "1")
            DrawerMenuItem("Earnings")
            DrawerMenuItem("Wallet")
            DrawerMenuItem("Account")
            Spacer(modifier = Modifier.weight(1f))
            Text("Help", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Learning Center", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DrawerMenuItem(label: String, badge: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal)
        badge?.let {
            Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primary) {
                Text(
                    it,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun SafetyToolkitSheet() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Safety Toolkit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        SafetyToolkitItem("911 assistance", "Get urgent help fast.", accent = Color(0xFFDC2626))
        SafetyToolkitItem("Record My Ride", "Capture audio for extra safety.", action = "Set up")
        SafetyToolkitItem("Follow my ride", "Share trip status with trusted contacts.")
        SafetyToolkitItem("Proof of trip status", "View safety and route proof.")
        SafetyToolkitItem("Report a crash", "Tell support when something happened.")
        SafetyToolkitItem("Safety Hub", "Explore safety tools and resources.")
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SafetyToolkitItem(
    title: String,
    subtitle: String,
    accent: Color = MaterialTheme.colorScheme.onSurface,
    action: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(modifier = Modifier.size(34.dp), shape = CircleShape, color = accent.copy(alpha = 0.12f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Heroicons.Outline.ShieldCheck,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = accent,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Normal)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        if (action != null) {
            Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(
                    action,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                )
            }
        } else {
            Icon(
                imageVector = Heroicons.Outline.ChevronRight,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
