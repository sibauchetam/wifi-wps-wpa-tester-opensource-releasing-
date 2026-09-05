package sangiorgi.wps.opensource.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Material 3 Expressive motion tokens and screen transition builders.
 *
 * The spring values mirror the official Material 3 Expressive motion tokens
 * (androidx.compose.material3 `ExpressiveMotionTokens`, spec v0_14_0):
 *
 * | Token           | Damping ratio | Stiffness |
 * |-----------------|---------------|-----------|
 * | Spatial default | 0.8           | 380       |
 * | Spatial fast    | 0.6           | 800       |
 * | Spatial slow    | 0.8           | 200       |
 * | Effects default | 1.0           | 1600      |
 * | Effects fast    | 1.0           | 3800      |
 * | Effects slow    | 1.0           | 800       |
 *
 * Spatial springs animate geometry (position, size, scale) and are allowed to
 * overshoot slightly (damping ratio < 1) to create the characteristic expressive
 * feel. Effects springs animate non-geometric values (alpha, color) and are
 * critically damped, because overshooting alpha or color would visibly flash.
 *
 * Note: `MotionScheme` from androidx.compose.material3 is internal in 1.4.0, so
 * these tokens are declared locally with the same values until the public API
 * becomes available.
 */
object ExpressiveMotion {
    // --- Spatial springs: geometry (position, size, scale) ---

    /** Default spatial spring, used for the main movement of entering screens. */
    val SpatialDefaultFloat: SpringSpec<Float> = spring(dampingRatio = 0.8f, stiffness = 380f)

    /** Fast spatial spring with a visible bounce, used for exiting screens. */
    val SpatialFastFloat: SpringSpec<Float> = spring(dampingRatio = 0.6f, stiffness = 800f)

    /** Default spatial spring for horizontal offsets (slide transitions). */
    val SpatialDefaultOffset: SpringSpec<IntOffset> = spring(dampingRatio = 0.8f, stiffness = 380f)

    /** Fast spatial spring for horizontal offsets (parallax slides). */
    val SpatialFastOffset: SpringSpec<IntOffset> = spring(dampingRatio = 0.6f, stiffness = 800f)

    /** Default spatial spring for size changes (expand/collapse sections). */
    val SpatialDefaultSize: SpringSpec<IntSize> =
        spring(dampingRatio = 0.8f, stiffness = 380f, visibilityThreshold = IntSize.VisibilityThreshold)

    /** Fast spatial spring for size changes (collapse of sections). */
    val SpatialFastSize: SpringSpec<IntSize> =
        spring(dampingRatio = 0.6f, stiffness = 800f, visibilityThreshold = IntSize.VisibilityThreshold)

    // --- Effects springs: non-geometric values (alpha, color), critically damped ---

    /** Default effects spring, used for entering fades and color changes. */
    val EffectsDefaultFloat: SpringSpec<Float> = spring(dampingRatio = 1f, stiffness = 1600f)

    /** Fast effects spring, used for quick fades of outgoing content. */
    val EffectsFastFloat: SpringSpec<Float> = spring(dampingRatio = 1f, stiffness = 3800f)

    /** Slow effects spring, used for gentle fade-through entrances. */
    val EffectsSlowFloat: SpringSpec<Float> = spring(dampingRatio = 1f, stiffness = 800f)

    /** Default effects spring for color transitions. */
    val EffectsDefaultColor: SpringSpec<Color> = spring(dampingRatio = 1f, stiffness = 1600f)

    // --- Transition builders ---

    // Fraction of the screen width traveled by the moving (top) screen.
    private const val SLIDE_FULL = 4

    // Fraction of the screen width traveled by the parallax (bottom) screen.
    private const val SLIDE_PARALLAX = 8

    // Scale of a screen at the start of an enter or the end of an exit.
    private const val SCALE_RECESS = 0.92f

    // Scale of a screen being gently covered by (or revealed under) a rising task screen.
    private const val SCALE_COVER = 0.96f

    // Fraction of the screen height traveled by a rising (task) screen.
    private const val RISE_FRACTION = 6

    // Fraction of the item height traveled by a staggered entrance.
    private const val STAGGER_FRACTION = 8

    /**
     * Forward navigation: the new screen springs in from the right edge while the
     * previous screen recedes with a parallax slide and a slight scale down.
     */
    fun enterPush(): EnterTransition = fadeIn(EffectsDefaultFloat) +
        slideInHorizontally(SpatialDefaultOffset) { it / SLIDE_FULL } +
        scaleIn(SpatialDefaultFloat, initialScale = SCALE_RECESS)

    /** Forward navigation: the covered screen recedes and fades out quickly. */
    fun exitPush(): ExitTransition = fadeOut(EffectsFastFloat) +
        slideOutHorizontally(SpatialFastOffset) { -it / SLIDE_PARALLAX } +
        scaleOut(SpatialFastFloat, targetScale = SCALE_RECESS)

    /** Back navigation: the revealed screen springs back with a parallax slide. */
    fun popEnter(): EnterTransition = fadeIn(EffectsDefaultFloat) +
        slideInHorizontally(SpatialDefaultOffset) { -it / SLIDE_PARALLAX } +
        scaleIn(SpatialDefaultFloat, initialScale = SCALE_RECESS)

    /** Back navigation: the top screen springs out to the right edge. */
    fun popExit(): ExitTransition = fadeOut(EffectsFastFloat) +
        slideOutHorizontally(SpatialFastOffset) { it / SLIDE_FULL } +
        scaleOut(SpatialFastFloat, targetScale = SCALE_RECESS)

    /** Expand + gentle fade for collapsible sections that reveal content. */
    fun expandEnter(): EnterTransition = expandVertically(SpatialDefaultSize) + fadeIn(EffectsDefaultFloat)

    /** Collapse + quick fade for collapsible sections that hide content. */
    fun collapseExit(): ExitTransition = shrinkVertically(SpatialFastSize) + fadeOut(EffectsFastFloat)

    /**
     * Task screens (connection progress) rise from the bottom edge like a sheet:
     * springy vertical slide + gentle fade + a slight scale settle.
     */
    fun enterRise(): EnterTransition = fadeIn(EffectsDefaultFloat) +
        slideInVertically(SpatialDefaultOffset) { it / RISE_FRACTION } +
        scaleIn(SpatialDefaultFloat, initialScale = SCALE_COVER)

    /** Back navigation: the task screen sinks back down towards the bottom edge. */
    fun popExitRise(): ExitTransition = fadeOut(EffectsFastFloat) +
        slideOutVertically(SpatialFastOffset) { it / RISE_FRACTION } +
        scaleOut(SpatialFastFloat, targetScale = SCALE_COVER)

    /**
     * Forward navigation: the screen being covered by a rising task screen recedes
     * in place with a quick fade and a slight scale down (no horizontal slide, so the
     * vertical rise reads as a separate motion layer).
     */
    fun exitReceive(): ExitTransition = fadeOut(EffectsFastFloat) +
        scaleOut(SpatialFastFloat, targetScale = SCALE_COVER)

    /** Back navigation: the revealed screen settles back from a gentle scale. */
    fun popEnterReceive(): EnterTransition = fadeIn(EffectsDefaultFloat) +
        scaleIn(SpatialDefaultFloat, initialScale = SCALE_COVER)

    /**
     * Staggered entrance for content revealed one card after another: each item
     * springs up a short distance while fading in, so lists cascade into place.
     */
    fun staggerIn(): EnterTransition = fadeIn(EffectsSlowFloat) +
        slideInVertically(SpatialDefaultOffset) { it / STAGGER_FRACTION } +
        scaleIn(SpatialDefaultFloat, initialScale = SCALE_COVER)

    /**
     * Springy scale + fade-through entrance for state swaps (`AnimatedContent`).
     * The incoming content fades in gently while popping from [scaleFrom].
     */
    fun swapIn(scaleFrom: Float = 0.85f): EnterTransition =
        fadeIn(EffectsSlowFloat) + scaleIn(SpatialDefaultFloat, initialScale = scaleFrom)

    /**
     * Springy scale + fade-through exit for state swaps (`AnimatedContent`).
     * The outgoing content fades out quickly while shrinking towards [scaleTo].
     */
    fun swapOut(scaleTo: Float = 0.85f): ExitTransition =
        fadeOut(EffectsFastFloat) + scaleOut(SpatialFastFloat, targetScale = scaleTo)
}
