package sangiorgi.wps.opensource.ui.motion

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Material 3 Expressive interaction modifiers.
 *
 * Expressive design treats every touch as a physical gesture: pressed surfaces
 * squish down quickly and spring back with a slight bounce, and idle "hero"
 * elements breathe with a slow, organic pulse. Both modifiers animate a scale
 * value and feed it to a `graphicsLayer` lambda, so the animation runs on the
 * render thread without recomposing the content.
 */

/**
 * Squishy press feedback for clickable surfaces.
 *
 * Compresses to [pressedScale] while the pointer is down (fast effects spring,
 * so the press feels immediate) and springs back to full size on release
 * (spatial spring with a slight overshoot, so the release feels elastic).
 *
 * Pass the same [interactionSource] to the clickable component (Card, Button,
 * FAB) so the modifier can observe its pressed state.
 */
@Composable
fun Modifier.expressivePress(interactionSource: MutableInteractionSource, pressedScale: Float = 0.96f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = if (pressed) ExpressiveMotion.EffectsFastFloat else ExpressiveMotion.SpatialDefaultFloat,
        label = "expressivePressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Breathing pulse for idle hero elements (permission icon, scanning indicator).
 *
 * Oscillates the scale between [minScale] and [maxScale] with the expressive
 * ease-in-out curve, creating a calm, organic "alive" motion instead of a
 * static icon. Repeats seamlessly back and forth.
 */
@Composable
fun Modifier.expressivePulse(minScale: Float = 0.96f, maxScale: Float = 1.04f, durationMillis: Int = 1600): Modifier {
    val transition = rememberInfiniteTransition(label = "expressivePulse")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "expressivePulseScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
