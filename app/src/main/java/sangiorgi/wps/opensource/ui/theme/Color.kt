package sangiorgi.wps.opensource.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Warm Charcoal & Olive theme for WiFi WPS Tester
// A calm, muted, earthy palette: deep warm charcoal surfaces, soft sage-olive
// accents, cream text and restrained clay highlights.
// =============================================================================

// Primary - Muted Olive-Sage (calm, confident, natural)
val Olive10 = Color(0xFF171E08)
val Olive20 = Color(0xFF2C3314)
val Olive30 = Color(0xFF434A29)
val Olive40 = Color(0xFF5B6240)
val Olive80 = Color(0xFFB7C48F)
val Olive90 = Color(0xFFD3E0AC)
val Olive100 = Color(0xFFF9FBEF)

// Secondary - Warm Sage Gray (quiet, supporting)
val Sage10 = Color(0xFF14180D)
val Sage20 = Color(0xFF2F3324)
val Sage30 = Color(0xFF454A38)
val Sage40 = Color(0xFF5D624F)
val Sage80 = Color(0xFFC4C8B2)
val Sage90 = Color(0xFFE0E4CE)
val Sage100 = Color(0xFFFAFBF0)

// Tertiary - Soft Clay (terracotta highlight, used sparingly)
val Clay10 = Color(0xFF2E1505)
val Clay20 = Color(0xFF452A1A)
val Clay30 = Color(0xFF5D4030)
val Clay40 = Color(0xFF785745)
val Clay80 = Color(0xFFE3B394)
val Clay90 = Color(0xFFFFD9C2)
val Clay100 = Color(0xFFFFF8F4)

// Error - Muted Brick (danger without neon)
val ErrorRed10 = Color(0xFF410E0B)
val ErrorRed20 = Color(0xFF690D05)
val ErrorRed30 = Color(0xFF8C2B1D)
val ErrorRed40 = Color(0xFFB3261E)
val ErrorRed80 = Color(0xFFFFB4A6)
val ErrorRed90 = Color(0xFFFFDAD2)
val ErrorRed100 = Color(0xFFFFF8F4)

// Neutral - Warm Charcoal (backgrounds, surfaces)
val Neutral10 = Color(0xFF131311)
val Neutral20 = Color(0xFF28281F)
val Neutral60 = Color(0xFF93907F)
val Neutral90 = Color(0xFFE5E3D5)
val Neutral95 = Color(0xFFF2F0E4)
val Neutral99 = Color(0xFFFAF8EF)

val NeutralVariant30 = Color(0xFF3B3B31)
val NeutralVariant50 = Color(0xFF6F6D5C)
val NeutralVariant60 = Color(0xFF898673)
val NeutralVariant80 = Color(0xFFC2C0AE)
val NeutralVariant90 = Color(0xFFDEDCD0)

// =============================================================================
// Semantic Colors (for direct use in components)
// Muted, earthy tones - no neon.
// =============================================================================

// Signal strength colors (sage -> clay ramp)
val SignalExcellent = Color(0xFF9FC383) // Muted sage - excellent
val SignalGood = Color(0xFFC1C98B) // Pale olive - good
val SignalFair = Color(0xFFD8C47E) // Muted gold - fair
val SignalWeak = Color(0xFFDA9A6B) // Muted apricot - weak

// Security level colors (one warm, restrained family)
val SecurityOpen = Color(0xFFE08A79) // Muted coral - open/insecure
val SecurityWep = Color(0xFFD9AC7C) // Muted amber - weak security
val SecurityWpa = Color(0xFFC9BE87) // Muted gold - moderate security
val SecurityWpa2 = Color(0xFFA9BF8B) // Soft sage - good security
val SecurityWpa3 = Color(0xFF86B89B) // Eucalyptus - excellent security

val AttackSuccess = Color(0xFFA9C98F) // Soft sage - PIN found
val AttackFailed = Color(0xFFE08A79) // Muted coral - failed
val AttackWarning = Color(0xFFD8C47E) // Muted gold - warning
