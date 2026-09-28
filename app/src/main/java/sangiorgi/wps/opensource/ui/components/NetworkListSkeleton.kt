package sangiorgi.wps.opensource.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Shimmer placeholder content for loading states (the WikiReader skeleton
 * pattern): a slow diagonal highlight sweeps across surface-toned placeholders
 * so the user sees the shape of the content that is about to arrive instead of
 * a bare spinner.
 */
@Composable
fun AnimatedShimmer(content: @Composable (Brush) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val shimmerColors = listOf(
        colorScheme.surfaceContainer,
        colorScheme.surfaceContainerHighest,
        colorScheme.surfaceContainer,
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 5000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation.value - 500, y = translateAnimation.value - 500),
        end = Offset(x = translateAnimation.value, y = translateAnimation.value),
    )

    content(brush)
}

/**
 * Skeleton of the network list: placeholder rows that mirror the layout of a
 * [sangiorgi.wps.opensource.ui.screens] network row (name, meta line, chip
 * row, signal indicator), grouped as one continuous surface with dividers.
 */
@Composable
fun NetworkListSkeleton(itemCount: Int = 5) {
    val groupShape = RoundedCornerShape(22.dp)

    AnimatedShimmer { brush ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(brush, groupShape),
        ) {
            repeat(itemCount) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(width = 150.dp, height = 16.dp)
                                .background(brush, RoundedCornerShape(6.dp)),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 12.dp)
                                .background(brush, RoundedCornerShape(6.dp)),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 18.dp)
                                    .background(brush, RoundedCornerShape(50)),
                            )
                            Box(
                                modifier = Modifier
                                    .size(width = 84.dp, height = 18.dp)
                                    .background(brush, RoundedCornerShape(50)),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(brush, CircleShape),
                    )
                }
                if (index != itemCount - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}
