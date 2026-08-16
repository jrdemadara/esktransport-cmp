package org.noztek.esktransport.feature.driver.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.Banknotes
import com.composables.icons.heroicons.outline.ChatBubbleLeftRight
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.ExclamationTriangle
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.ShieldCheck
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBar
import org.noztek.esktransport.core.ui.composables.driver.DriverBottomBarRoute

@Composable
fun DriverSafetyTipsScreen(
    onBackClick: () -> Unit,
    onBottomBarNavigate: (String) -> Unit = {},
) {
    DriverSafetyTipsContent(
        onBackClick = onBackClick,
        onBottomBarNavigate = onBottomBarNavigate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverSafetyTipsContent(
    onBackClick: () -> Unit,
    onBottomBarNavigate: (String) -> Unit,
) {
    Scaffold(
        topBar = { SafetyTipsTopBar(onBackClick = onBackClick) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SafetyTipsIntroCard()
            safetyTipSections.forEach { section ->
                SafetyTipSectionCard(section = section)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SafetyTipsTopBar(onBackClick: () -> Unit) {
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
                text = "Safety Tips",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun SafetyTipsIntroCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.ShieldCheck,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Drive with clear checkpoints",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Short reminders for pickups, trips, cash handling, and urgent cases.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SafetyTipSectionCard(section: SafetyTipSection) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 48.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f),
            )
            section.tips.forEachIndexed { index, tip ->
                SafetyTipRow(text = tip)
                if (index < section.tips.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 48.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SafetyTipRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Heroicons.Outline.CheckCircle,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private data class SafetyTipSection(
    val title: String,
    val icon: ImageVector,
    val tips: List<String>,
)

private val safetyTipSections = listOf(
    SafetyTipSection(
        title = "Before pickup",
        icon = Heroicons.Outline.MapPin,
        tips = listOf(
            "Check the pickup pin and passenger name before moving.",
            "Park in a visible area and avoid unsafe pickup spots when possible.",
            "Keep your phone mounted and battery ready for trip updates.",
        ),
    ),
    SafetyTipSection(
        title = "During the trip",
        icon = Heroicons.Outline.ChatBubbleLeftRight,
        tips = listOf(
            "Use in-app chat or call for trip-related messages only.",
            "Follow the app route unless the passenger requests a safer public route.",
            "Do not continue a trip if the situation feels unsafe.",
        ),
    ),
    SafetyTipSection(
        title = "Cash handling",
        icon = Heroicons.Outline.Banknotes,
        tips = listOf(
            "Confirm the fare before ending the trip.",
            "Count cash before marking the trip complete.",
            "Report payment disputes from the Safety settings as soon as possible.",
        ),
    ),
    SafetyTipSection(
        title = "Urgent cases",
        icon = Heroicons.Outline.ExclamationTriangle,
        tips = listOf(
            "For immediate danger, move to a safe public place first.",
            "Contact local authorities for emergencies that need urgent response.",
            "Submit an incident report after you are safe so support has a record.",
        ),
    ),
)
