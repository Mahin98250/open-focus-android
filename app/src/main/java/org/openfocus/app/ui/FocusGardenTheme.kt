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
    primary = Color(0xFFB7F36B),
    onPrimary = Color(0xFF18240B),
    primaryContainer = Color(0xFF25401D),
    onPrimaryContainer = Color(0xFFE7FFD0),
    secondary = Color(0xFF9DD9B0),
    onSecondary = Color(0xFF10251A),
    background = Color(0xFF080D0A),
    onBackground = Color(0xFFF4F7F1),
    surface = Color(0xFF111A14),
    onSurface = Color(0xFFF4F7F1),
    surfaceVariant = Color(0xFF202B23),
    onSurfaceVariant = Color(0xFFA7B5AA),
    outline = Color(0xFF46554A),
    error = Color(0xFFFF9C91)
)

private val OpenFocusTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-1.8).sp),
        displayMedium = base.displayMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-1.4).sp),
        headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-1.0).sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = (-0.65).sp),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif, lineHeight = 26.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.SansSerif, lineHeight = 23.sp),
        bodySmall = base.bodySmall.copy(fontFamily = FontFamily.SansSerif, lineHeight = 19.sp),
        labelLarge = base.labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold)
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
