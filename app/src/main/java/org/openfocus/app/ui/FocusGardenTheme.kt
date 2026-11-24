package org.openfocus.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

@Composable
fun FocusGardenTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = OpenFocusColors, content = content)
}
