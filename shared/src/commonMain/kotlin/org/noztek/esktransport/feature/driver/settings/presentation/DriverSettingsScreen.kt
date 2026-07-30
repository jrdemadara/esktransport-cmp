package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.AdjustmentsHorizontal
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.ChartBarSquare
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.InformationCircle
import com.composables.icons.heroicons.outline.Lifebuoy
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.QuestionMarkCircle
import com.composables.icons.heroicons.outline.ShieldCheck
import com.composables.icons.heroicons.outline.Star
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.User
import com.composables.icons.heroicons.outline.Wallet
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.logo_nobg
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.noztek.esktransport.core.platform.AppBuildInfo
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute
import org.noztek.esktransport.core.utils.uppercaseFirstLetterOfEachWord
import org.noztek.esktransport.feature.driver.onboarding.presentation.CapturedDocumentPreviewImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverSettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onAccountClick: () -> Unit = {},
    onBottomBarNavigate: (String) -> Unit = {},
    viewModel: DriverSettingsViewModel = koinViewModel(),
    appBuildInfo: AppBuildInfo = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val infoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeInfoSheet by remember { mutableStateOf<SettingsInfoSheet?>(null) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            viewModel.clearLogoutState()
            onLogout()
        }
    }

    Scaffold(
        topBar = { SettingsTopBar(onBackClick = onBackClick) },
        bottomBar = {
            DriverBottomBar(
                currentRoute = DriverBottomBarRoute.PROFILE,
                onNavigate = onBottomBarNavigate,
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 6.dp),
        ) {
            SettingsProfileRow(uiState = uiState)
            SettingsDivider()
            settingsMenuItems.forEachIndexed { index, item ->
                SettingsMenuRow(
                    item = item,
                    onClick = {
                        if (item == SettingsMenuItem.Account) {
                            onAccountClick()
                        }
                    },
                )
                if (index < settingsMenuItems.lastIndex) {
                    SettingsDivider()
                }
            }
            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            SettingsFooter(
                versionName = appBuildInfo.versionName,
                onSheetClick = { sheet ->
                    activeInfoSheet = sheet
                },
            )
        }
    }

    activeInfoSheet?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = { activeInfoSheet = null },
            sheetState = infoSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            SettingsInfoSheetContent(sheet = sheet)
        }
    }
}

@Composable
private fun SettingsTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Heroicons.Outline.ArrowLeft,
                    contentDescription = "Back",
                    modifier = Modifier.size(22.dp),
                )
            }
        },
        title = {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun SettingsProfileRow(uiState: DriverSettingsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(58.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (uiState.profilePhotoBytes != null) {
                    CapturedDocumentPreviewImage(
                        bytes = uiState.profilePhotoBytes,
                        contentDescription = "Driver profile photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Heroicons.Outline.User,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = uiState.name.takeIf { it.isNotBlank() }?.uppercaseFirstLetterOfEachWord() ?: "Driver",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = uiState.driverId.driverIdLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsMenuRow(
    item: SettingsMenuItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = item == SettingsMenuItem.Account,
                onClick = onClick,
            )
            .padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = Heroicons.Outline.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
}

@Composable
private fun SettingsFooter(
    versionName: String,
    onSheetClick: (SettingsInfoSheet) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 10.dp),
    ) {
        settingsFooterItems(versionName).forEach { item ->
            SettingsFooterRow(
                item = item,
                onClick = {
                    item.sheet?.let(onSheetClick)
                },
            )
        }
    }
}

@Composable
private fun SettingsFooterRow(
    item: SettingsFooterItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = item.sheet != null,
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
        )
        Text(
            text = item.title,
            modifier = if (item.value == null) Modifier.weight(1f) else Modifier,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        item.value?.let { value ->
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun SettingsInfoSheetContent(sheet: SettingsInfoSheet) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SettingsInfoSheetHeader(sheet = sheet)
        sheet.sections.forEach { section ->
            SettingsInfoSection(section = section)
        }
    }
}

@Composable
private fun SettingsInfoSheetHeader(sheet: SettingsInfoSheet) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (sheet == SettingsInfoSheet.About) {
                    Image(
                        painter = painterResource(Res.drawable.logo_nobg),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Icon(
                        imageVector = sheet.icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = sheet.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = sheet.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsInfoSection(section: SettingsInfoSection) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        section.paragraphs.forEach { paragraph ->
            Text(
                text = paragraph,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class SettingsMenuItem(
    val title: String,
    val icon: ImageVector,
) {
    Account("Account", Heroicons.Outline.User),
    DriverVerification("Driver Verification", Heroicons.Outline.ShieldCheck),
    Vehicle("Vehicle", Heroicons.Outline.Truck),
    Wallet("Wallet", Heroicons.Outline.Wallet),
    Earnings("Earnings", Heroicons.Outline.ChartBarSquare),
    ServiceAreas("Service Areas", Heroicons.Outline.MapPin),
    Safety("Safety", Heroicons.Outline.ShieldCheck),
    AppPreferences("App Preferences", Heroicons.Outline.AdjustmentsHorizontal),
}

private data class SettingsFooterItem(
    val title: String,
    val icon: ImageVector,
    val value: String? = null,
    val sheet: SettingsInfoSheet? = null,
)

private data class SettingsInfoSection(
    val title: String,
    val paragraphs: List<String> = emptyList(),
)

private enum class SettingsInfoSheet(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val sections: List<SettingsInfoSection>,
) {
    Terms(
        title = "Terms and Conditions",
        subtitle = "Service rules for using eSKTransport.",
        icon = Heroicons.Outline.DocumentText,
        sections = listOf(
            SettingsInfoSection(
                title = "Using the service",
                paragraphs = listOf(
                    "eSKTransport provides mobile booking, driver dispatch, trip tracking, wallet, and support features for local transport services in supported Sultan Kudarat service zones. By using the app, you agree to use these features only for lawful transport-related activity and to provide accurate account, booking, contact, vehicle, and service-zone information.",
                    "You must not misuse the app, interfere with dispatch, submit false verification documents, abuse chat or safety tools, attempt unauthorized access, or use the service in a way that may harm passengers, drivers, operators, or the platform.",
                ),
            ),
            SettingsInfoSection(
                title = "Bookings and payments",
                paragraphs = listOf(
                    "Passengers should review pickup, drop-off, vehicle type, fare estimate, and driver details before confirming a ride. Phase 1 rides are cash-based unless ESK Transport enables another supported payment method.",
                    "Drivers must keep their account, identity verification, vehicle registration, service zone, wallet balance, and availability status accurate. Drivers are responsible for accepting only rides they can safely and lawfully complete.",
                ),
            ),
            SettingsInfoSection(
                title = "Safety, policy, and account action",
                paragraphs = listOf(
                    "eSKTransport may restrict, suspend, or review accounts, bookings, wallet actions, or driver access when there are safety, fraud, verification, payment, legal, or policy concerns. Trip records, location events, chat records, document review records, and wallet ledger entries may be used to investigate disputes and support requests.",
                    "The app is intended to comply with applicable laws and Google Play policies. Features that use sensitive information are limited to app functionality that users can reasonably expect from a ride-hailing and driver operations service.",
                ),
            ),
        ),
    ),
    Privacy(
        title = "Privacy Policy",
        subtitle = "How app data is collected, used, and protected.",
        icon = Heroicons.Outline.ShieldCheck,
        sections = listOf(
            SettingsInfoSection(
                title = "Data we collect",
                paragraphs = listOf(
                    "eSKTransport collects account details such as name, phone number, role, session information, and account status. For drivers, the app may collect identity and vehicle verification data, including license details, license images, selfie, vehicle registration document, vehicle photo, plate number, vehicle details, selected service zones, and verification review status.",
                    "The app collects ride and operational data needed to provide the service, including pickup and drop-off locations, fare quote, booking status, route information, chat messages, trip feedback, cancellation records, driver availability, and driver location while the driver is online or while a trip is active. Wallet and top-up request records are collected for balance management, kiosk-assisted top-ups, and ledger tracing.",
                ),
            ),
            SettingsInfoSection(
                title = "How we use and share data",
                paragraphs = listOf(
                    "Data is used to create and secure accounts, verify drivers, match passengers with nearby available drivers, calculate fares, complete trips, sync real-time booking and trip events, process wallet top-up requests, provide support, improve reliability, prevent abuse, and resolve safety or operational issues.",
                    "Operational data is shared only where needed for app functionality. Passengers and drivers may see relevant booking, trip, contact, chat, vehicle, route, fare, and location information for an active ride. Authorized administrators may access verification, trip, wallet, and support records to operate the service. Service providers such as hosting, maps, notifications, analytics, and real-time messaging may process data only as needed to support the app. eSKTransport does not sell personal or sensitive user data.",
                ),
            ),
            SettingsInfoSection(
                title = "Security, retention, and deletion",
                paragraphs = listOf(
                    "Personal and sensitive data is handled using security measures appropriate for a production transport service, including secure transmission for network requests and restricted access to private verification files. Permissions such as camera and location are requested only when needed for app features such as document capture, selfie capture, dispatch, navigation, and trip tracking.",
                    "Records are retained while needed for account operation, driver verification, trip history, wallet ledger tracing, support, audit, dispute handling, safety, and legal compliance. Users may request account or data deletion through the app support channel or operator support process. Some records may be kept when retention is required for legal, security, fraud prevention, accounting, or dispute-resolution reasons.",
                    "This Privacy Policy is intended to match the app's Google Play Data safety disclosures. If app data practices change, eSKTransport should update this in-app policy and the Play Console Data safety section before release.",
                ),
            ),
            SettingsInfoSection(
                title = "Privacy contact",
                paragraphs = listOf(
                    "For privacy questions, data access requests, correction requests, or deletion requests, contact eSKTransport support through the official support channel provided by the operator.",
                ),
            ),
        ),
    ),
    About(
        title = "About eSKTransport",
        subtitle = "Built for local mobility in Sultan Kudarat.",
        icon = Heroicons.Outline.InformationCircle,
        sections = listOf(
            SettingsInfoSection(
                title = "Purpose",
                paragraphs = listOf(
                    "eSKTransport is a mobile transport platform for passengers, drivers, and operators serving Sultan Kudarat communities.",
                    "Passengers can plan rides, review fare details, book trips, track drivers, chat, and send ride feedback. Drivers can complete onboarding, select service zones, receive booking offers, navigate trips, and monitor wallet, trips, and earnings. Operators can review driver identity and vehicle documents, manage settings, and support safe local dispatch.",
                ),
            ),
            SettingsInfoSection(
                title = "Coverage",
                paragraphs = listOf(
                    "The initial production-testing release focuses on configured service zones in and around Sultan Kudarat, including areas such as Tacurong City and nearby operating zones.",
                ),
            ),
        ),
    ),
}

private val settingsMenuItems = listOf(
    SettingsMenuItem.Account,
    SettingsMenuItem.DriverVerification,
    SettingsMenuItem.Vehicle,
    // SettingsMenuItem.Wallet,
    // SettingsMenuItem.Earnings,
    SettingsMenuItem.ServiceAreas,
    SettingsMenuItem.Safety,
    SettingsMenuItem.AppPreferences,
)

private fun settingsFooterItems(versionName: String) = listOf(
    SettingsFooterItem("Rate", Heroicons.Outline.Star),
    SettingsFooterItem("Help and tips", Heroicons.Outline.Lifebuoy),
    SettingsFooterItem(
        title = "Terms and Conditions",
        icon = Heroicons.Outline.DocumentText,
        sheet = SettingsInfoSheet.Terms,
    ),
    SettingsFooterItem(
        title = "Privacy Policy",
        icon = Heroicons.Outline.ShieldCheck,
        sheet = SettingsInfoSheet.Privacy,
    ),
    SettingsFooterItem(
        title = "About",
        icon = Heroicons.Outline.QuestionMarkCircle,
        sheet = SettingsInfoSheet.About,
    ),
    SettingsFooterItem("Version", Heroicons.Outline.InformationCircle, versionName),
)
