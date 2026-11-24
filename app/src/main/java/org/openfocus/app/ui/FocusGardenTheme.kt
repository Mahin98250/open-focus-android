package org.openfocus.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val OpenFocusColors = darkColorScheme(
    primary = Color(0xFFC3F36B),
    onPrimary = Color(0xFF17220B),
    primaryContainer = Color(0xFF263B24),
    onPrimaryContainer = Color(0xFFE8F8DC),
    secondary = Color(0xFF9DDCC6),
    onSecondary = Color(0xFF10251E),
    secondaryContainer = Color(0xFF1D3931),
    onSecondaryContainer = Color(0xFFD9F5E9),
    tertiary = Color(0xFFA8BFFF),
    onTertiary = Color(0xFF152344),
    tertiaryContainer = Color(0xFF263653),
    onTertiaryContainer = Color(0xFFE1E9FF),
    background = Color(0xFF080D12),
    onBackground = Color(0xFFF3F6F2),
    surface = Color(0xFF111A21),
    onSurface = Color(0xFFF3F6F2),
    surfaceVariant = Color(0xFF202C34),
    onSurfaceVariant = Color(0xFFA9B6B4),
    outline = Color(0xFF40504F),
    outlineVariant = Color(0xFF29373D),
    error = Color(0xFFFF9C91)
)

private val OpenFocusTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-2.0).sp, lineHeight = 58.sp),
        displayMedium = base.displayMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-1.6).sp, lineHeight = 48.sp),
        headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-1.1).sp, lineHeight = 38.sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-0.7).sp, lineHeight = 32.sp),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.35).sp),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.15).sp),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif, lineHeight = 26.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.SansSerif, lineHeight = 23.sp),
        bodySmall = base.bodySmall.copy(fontFamily = FontFamily.SansSerif, lineHeight = 19.sp),
        labelLarge = base.labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        labelSmall = base.labelSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
    )
}

@Composable
fun FocusGardenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OpenFocusColors,
        typography = OpenFocusTypography,
        content = content
    )
}
