package org.noztek.esktransport.feature.passenger.marketplace.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.icons.heroicons.Heroicons
import com.composables.icons.heroicons.outline.ArrowLeft
import com.composables.icons.heroicons.outline.Briefcase
import com.composables.icons.heroicons.outline.CalendarDays
import com.composables.icons.heroicons.outline.CheckCircle
import com.composables.icons.heroicons.outline.ChevronRight
import com.composables.icons.heroicons.outline.Cube
import com.composables.icons.heroicons.outline.DocumentText
import com.composables.icons.heroicons.outline.Heart
import com.composables.icons.heroicons.outline.MapPin
import com.composables.icons.heroicons.outline.Tag
import com.composables.icons.heroicons.outline.Truck
import com.composables.icons.heroicons.outline.User
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.medium_truck
import org.jetbrains.compose.resources.painterResource
import org.noztek.esktransport.core.ui.composables.common.AppPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceListingDetailsScreen(
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ListingDetailsTopBar(onBackClick = onBackClick)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ListingImageCarousel() }
            item { ListingHeader() }
            item { VehicleDetailsCard() }
            item { PricingCard() }
            item { DescriptionCard() }
            item { RequestVehicleCard() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingDetailsTopBar(
    onBackClick: () -> Unit,
) {
    TopAppBar(
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
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
                text = "Listing details",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Heroicons.Outline.Heart,
                    contentDescription = "Save",
                    modifier = Modifier.size(24.dp),
                )
            }
        },
    )
}

@Composable
private fun ListingImageCarousel() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(244.dp)
                .clip(RoundedCornerShape(10.dp)),
        ) {
            Image(
                painter = painterResource(Res.drawable.medium_truck),
                contentDescription = "Isuzu N-Series Box Truck",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(7.dp),
                color = Color.Black.copy(alpha = 0.70f),
                contentColor = Color.White,
            ) {
                Text(
                    text = "1 / 6",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (index == 0) 8.dp else 7.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (index == 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun ListingHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = "Isuzu N-Series Box Truck",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SolidPill(text = "Rental")
                    SoftPill(text = "van", color = MaterialTheme.colorScheme.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    StatusPill(text = "active", color = Color(0xFF16A34A))
                    StatusPill(text = "approved", color = Color(0xFFD97706))
                }
            }
            Icon(
                imageVector = Heroicons.Outline.Heart,
                contentDescription = "Save",
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
        }
        OwnerRow()
    }
}

@Composable
private fun OwnerRow() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Heroicons.Outline.User,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Juan Dela Cruz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            Icon(
                imageVector = Heroicons.Outline.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = "Updated May 18, 2025",
            modifier = Modifier.padding(start = 24.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VehicleDetailsCard() {
    DetailsSectionCard(
        title = "Vehicle details",
        icon = Heroicons.Outline.Truck,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCell(
                icon = Heroicons.Outline.DocumentText,
                label = "Plate number",
                value = "ABC 1234",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(45.dp))
            InfoCell(
                icon = Heroicons.Outline.Truck,
                label = "Make / Model / Year",
                value = "Isuzu N-Series  •  2021",
                modifier = Modifier.weight(1.2f),
            )
        }
        SectionDivider()
        Row(modifier = Modifier.fillMaxWidth()) {
            InfoCell(
                icon = Heroicons.Outline.Briefcase,
                label = "Payload",
                value = "4,200 kg",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(45.dp))
            InfoCell(
                icon = Heroicons.Outline.Cube,
                label = "Volume",
                value = "18 m3",
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(45.dp))
            InfoCell(
                icon = Heroicons.Outline.User,
                label = "Passenger capacity",
                value = "0",
                modifier = Modifier.weight(1.08f),
            )
        }
        SectionDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Heroicons.Outline.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Availability",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Available",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PricingCard() {
    DetailsSectionCard(
        title = "Pricing",
        icon = Heroicons.Outline.Tag,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Base rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "PHP 6,800/day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Minimum 2 hours",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            VerticalDivider(modifier = Modifier.height(58.dp))
            Column(
                modifier = Modifier.weight(0.8f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Included km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "100 km",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            SoftPill(text = "PHP", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DescriptionCard() {
    DetailsSectionCard(
        title = "Description",
        icon = Heroicons.Outline.DocumentText,
    ) {
        Text(
            text = "Reliable Isuzu N-Series box truck ideal for deliveries, logistics, and distribution. Spacious cargo area with secure enclosed box for safe transport of goods.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RequestVehicleCard() {
    DetailsSectionCard(
        title = "Request this vehicle",
        icon = Heroicons.Outline.CalendarDays,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RequestField(
                label = "Requested start",
                icon = Heroicons.Outline.CalendarDays,
                modifier = Modifier.weight(1f),
            )
            RequestField(
                label = "Requested end",
                icon = Heroicons.Outline.CalendarDays,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RequestField(
                label = "Pickup location",
                icon = Heroicons.Outline.MapPin,
                modifier = Modifier.weight(1f),
            )
            RequestField(
                label = "Destination",
                icon = Heroicons.Outline.MapPin,
                modifier = Modifier.weight(1f),
            )
        }
        RequestField(
            label = "Customer note (optional)",
            icon = Heroicons.Outline.DocumentText,
            showChevron = false,
        )
        AppPrimaryButton(
            text = "Send request",
            onClick = {},
            height = 48.dp,
        )
    }
}

@Composable
private fun DetailsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Composable
private fun InfoCell(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RequestField(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
            )
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showChevron) {
                Icon(
                    imageVector = Heroicons.Outline.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SolidPill(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SoftPill(
    text: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.10f),
        contentColor = color,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color,
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.10f),
        contentColor = color,
        border = BorderStroke(0.6.dp, color.copy(alpha = 0.45f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.7.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(0.7.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
