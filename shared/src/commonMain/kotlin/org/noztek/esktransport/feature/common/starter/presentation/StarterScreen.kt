package org.noztek.esktransport.feature.common.starter.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import esktransport.shared.generated.resources.Res
import esktransport.shared.generated.resources.big_truck
import esktransport.shared.generated.resources.car
import esktransport.shared.generated.resources.medium_truck
import esktransport.shared.generated.resources.scooter
import esktransport.shared.generated.resources.starter
import esktransport.shared.generated.resources.tricycle
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.noztek.esktransport.core.ui.composables.common.AppCommonTopBar
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun StarterScreen(
    onLoginClick: () -> Unit,
    onCustomerRegisterClick: () -> Unit,
    onDriverRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        containerColor = backgroundColor,
        topBar = {
            AppCommonTopBar(containerColor = backgroundColor)
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(contentPadding)
                .padding(bottom = 18.dp),
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            IllustrationPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(
                    text = "Move smarter with\n eSKTransport",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.6).sp,
                )
                Text(
                    text = "Book a ride as a customer or start earning as a driver. One clean transport app for everyday trips.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            ArcImageRail(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                Button(
                    onClick = onCustomerRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                ) {
                    Text("Continue as Customer", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onDriverRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("Continue as Driver", fontWeight = FontWeight.SemiBold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onLoginClick),
                )
            }
        }
    }
}

@Composable
private fun IllustrationPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.starter),
            contentDescription = "Starter illustration",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun ArcImageRail(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val infiniteTransition = rememberInfiniteTransition(label = "ArcImageRail")
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 7600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "arcProgress",
        )

        ArcAvatar(
            progress = progress,
            widthPx = widthPx,
            heightPx = heightPx,
            size = 62,
            image = Res.drawable.scooter,
            contentDescription = "Motorcycle",
            phase = 0f,
        )
        ArcAvatar(
            progress = progress,
            widthPx = widthPx,
            heightPx = heightPx,
            size = 68,
            image = Res.drawable.medium_truck,
            contentDescription = "Medium truck",
            phase = 0.8f,
        )
        ArcAvatar(
            progress = progress,
            widthPx = widthPx,
            heightPx = heightPx,
            size = 66,
            image = Res.drawable.tricycle,
            contentDescription = "Tricycle",
            phase = 0.2f,
        )
        ArcAvatar(
            progress = progress,
            widthPx = widthPx,
            heightPx = heightPx,
            size = 64,
            image = Res.drawable.car,
            contentDescription = "Car",
            phase = 0.6f,
        )
        ArcAvatar(
            progress = progress,
            widthPx = widthPx,
            heightPx = heightPx,
            size = 70,
            image = Res.drawable.big_truck,
            contentDescription = "Large truck",
            phase = 0.4f,
        )
    }
}

@Composable
private fun ArcAvatar(
    progress: Float,
    widthPx: Float,
    heightPx: Float,
    size: Int,
    image: DrawableResource,
    contentDescription: String,
    phase: Float,
) {
    val density = LocalDensity.current
    val sizePx = with(density) { size.dp.toPx() }
    val adjusted = (progress + phase) % 1f
    val x = (-sizePx + (widthPx + sizePx * 2f) * adjusted).roundToInt()
    val arc = sin(adjusted * PI).toFloat()
    val y = ((heightPx * 0.58f) - (arc * heightPx * 0.46f)).roundToInt()

    Box(
        modifier = Modifier
            .offset { IntOffset(x, y) }
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
