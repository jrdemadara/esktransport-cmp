package org.noztek.esktransport.core.ui.composables.driver

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ChartBar
import com.composables.icons.heroicons.outline.ChartBarSquare
import com.composables.icons.heroicons.outline.CurrencyDollar
import com.composables.icons.heroicons.outline.Home
import com.composables.icons.heroicons.outline.QueueList
import com.composables.icons.heroicons.outline.User
import com.composables.icons.heroicons.solid.ChartBar

data class DriverBottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

object DriverBottomBarRoute {
    const val HOME = "driver/home"
    const val TRIPS = "driver/trips"
    const val EARNINGS = "driver/earnings"
    const val PROFILE = "driver/profile"
}

val defaultDriverBottomBarItems = listOf(
    DriverBottomBarItem(
        route = DriverBottomBarRoute.HOME,
        label = "Home",
        icon = Heroicons.Outline.Home,
    ),
    DriverBottomBarItem(
        route = DriverBottomBarRoute.TRIPS,
        label = "Trips",
        icon = Heroicons.Outline.QueueList,
    ),
    DriverBottomBarItem(
        route = DriverBottomBarRoute.EARNINGS,
        label = "Earnings",
        icon = Heroicons.Outline.ChartBarSquare,
    ),
    DriverBottomBarItem(
        route = DriverBottomBarRoute.PROFILE,
        label = "Profile",
        icon = Heroicons.Outline.User,
    ),
)

@Composable
fun DriverBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<DriverBottomBarItem> = defaultDriverBottomBarItems,
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
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
