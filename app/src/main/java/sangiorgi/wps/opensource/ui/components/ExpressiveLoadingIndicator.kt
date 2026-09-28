package sangiorgi.wps.opensource.ui.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Expressive loading indicator: three dots breathing in a wave.
 *
 * The official Material 3 `LoadingIndicator` (morphing shapes) is internal in
 * material3 1.4.0, so this is the local expressive stand-in: each dot eases
 * between scales with a phase offset, echoing the same organic "alive" motion
 * language as [sangiorgi.wps.opensource.ui.motion.expressivePulse].
 */
@Composable
fun ExpressiveLoadingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            LoadingDot(index = index)
        }
    }
}

@Composable
private fun LoadingDot(index: Int) {
    val transition = rememberInfiniteTransition(label = "loadingDot$index")
    val scale by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 640,
                delayMillis = index * 120,
                easing = EaseInOutCubic,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "loadingDotScale$index",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 640,
                delayMillis = index * 120,
                easing = EaseInOutCubic,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "loadingDotAlpha$index",
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(MaterialTheme.colorScheme.primary, CircleShape),
    )
}
