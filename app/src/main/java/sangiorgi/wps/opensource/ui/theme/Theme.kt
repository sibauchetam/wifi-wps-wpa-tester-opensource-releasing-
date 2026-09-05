package sangiorgi.wps.opensource.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Softly rounded shape system - generous corner radii everywhere,
 * from chips (extra small) to cards (medium/large) and sheets (extra large).
 */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

/**
 * Dark Color Scheme - Warm Charcoal & Olive
 * Deep warm charcoal backgrounds with soft sage-olive accents and cream text:
 * a calm, muted, professional look for a security tool.
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors - Muted Olive-Sage
    primary = Olive80,
    onPrimary = Olive20,
    primaryContainer = Olive30,
    onPrimaryContainer = Olive90,

    // Secondary colors - Warm Sage Gray
    secondary = Sage80,
    onSecondary = Sage20,
    secondaryContainer = Sage30,
    onSecondaryContainer = Sage90,

    // Tertiary colors - Soft Clay
    tertiary = Clay80,
    onTertiary = Clay20,
    tertiaryContainer = Clay30,
    onTertiaryContainer = Clay90,

    // Error colors
    error = ErrorRed80,
    onError = ErrorRed20,
    errorContainer = ErrorRed30,
    onErrorContainer = ErrorRed90,

    // Background and Surface
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = NeutralVariant30,
    onSurfaceVariant = NeutralVariant80,
    surfaceContainer = Neutral20,
    surfaceContainerHigh = NeutralVariant30,

    // Outline and inverse
    outline = NeutralVariant60,
    outlineVariant = NeutralVariant30,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    inversePrimary = Olive40,

    // Scrim
    scrim = Color.Black,
)

/**
 * Light Color Scheme - Warm Cream & Olive
 * A soft cream counterpart of the dark palette for light mode.
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors - Muted Olive-Sage
    primary = Olive40,
    onPrimary = Olive100,
    primaryContainer = Olive90,
    onPrimaryContainer = Olive10,

    // Secondary colors - Warm Sage Gray
    secondary = Sage40,
    onSecondary = Sage100,
    secondaryContainer = Sage90,
    onSecondaryContainer = Sage10,

    // Tertiary colors - Soft Clay
    tertiary = Clay40,
    onTertiary = Clay100,
    tertiaryContainer = Clay90,
    onTertiaryContainer = Clay10,

    // Error colors
    error = ErrorRed40,
    onError = ErrorRed100,
    errorContainer = ErrorRed90,
    onErrorContainer = ErrorRed10,

    // Background and Surface
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant30,
    surfaceContainer = Neutral95,
    surfaceContainerHigh = NeutralVariant90,

    // Outline and inverse
    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = Olive80,

    // Scrim
    scrim = Color.Black,
)

@Composable
fun WIFIWPSWPATESTEROPENSOURCETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // Set to false to use our custom warm charcoal theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Blend the status bar into the background instead of shouting with an
    // accent color - the app chrome stays quiet and seamless.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Material 3 Expressive: the motion scheme factories are internal in
    // material3 1.4.0, so expressive motion comes from ui/motion tokens (official
    // spring values) plus expressive components - not from MaterialExpressiveTheme.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
