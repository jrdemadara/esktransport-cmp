package org.noztek.esktransport.core.ui.composables.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun HoldToCancelButton(
    isCancelling: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Press and hold 3s to cancel",
    cancellingText: String = "Cancelling...",
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    val errorColor = MaterialTheme.colorScheme.error
    val onErrorColor = MaterialTheme.colorScheme.onError
    val errorContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(errorContainerColor)
            .pointerInput(isCancelling, onCancel) {
                if (isCancelling) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent(PointerEventPass.Main)
                            .changes
                            .firstOrNull { it.pressed }
                        if (down == null) continue

                        val holdJob = scope.launch {
                            progress.snapTo(0f)
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = 3000, easing = LinearEasing),
                            )
                            onCancel()
                        }

                        do {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val stillPressed = event.changes.any { it.pressed }
                            if (!stillPressed) {
                                holdJob.cancel()
                                scope.launch { progress.animateTo(0f, tween(durationMillis = 160)) }
                                break
                            }
                        } while (true)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxHeight()
                .width(maxWidth * progress.value)
                .background(errorColor),
        )
        Text(
            text = if (isCancelling) cancellingText else text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (progress.value > 0.55f) onErrorColor else errorColor,
        )
    }
}
