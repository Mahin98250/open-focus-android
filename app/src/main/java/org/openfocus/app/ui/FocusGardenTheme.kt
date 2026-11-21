package org.openfocus.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GardenColors = darkColorScheme(
    primary = Color(0xFFB7E6C4),
    onPrimary = Color(0xFF183322),
    primaryContainer = Color(0xFF294936),
    onPrimaryContainer = Color(0xFFD8F1DC),
    secondary = Color(0xFFE9C3A9),
    onSecondary = Color(0xFF38271D),
    background = Color(0xFF101713),
    onBackground = Color(0xFFF0F4ED),
    surface = Color(0xFF18231D),
    onSurface = Color(0xFFF0F4ED),
    surfaceVariant = Color(0xFF25352B),
    onSurfaceVariant = Color(0xFFA0B0A4),
    outline = Color(0xFF536459),
    error = Color(0xFFE7A39A)
)

@Composable
fun FocusGardenTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = GardenColors, content = content)
}