package org.openfocus.app.ui.regain

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A soft gradient background plus an accent color for the timer screen. */
data class FocusTheme(
    val name: String,
    val top: Color,
    val bottom: Color,
    val accent: Color
)

object FocusThemes {
    val Midnight = FocusTheme("Midnight", Color(0xFF0E1116), Color(0xFF171B26), Color(0xFF7C9CFF))
    val Forest = FocusTheme("Forest", Color(0xFF0C1A14), Color(0xFF17302A), Color(0xFF6FD3A0))
    val Dusk = FocusTheme("Dusk", Color(0xFF1A1230), Color(0xFF3A1F4D), Color(0xFFFF9EC7))
    val Ocean = FocusTheme("Ocean", Color(0xFF071826), Color(0xFF0F3A52), Color(0xFF5CC8FF))
    val Lofi = FocusTheme("Lofi", Color(0xFF1F1A24), Color(0xFF3B2F3F), Color(0xFFFFC36B))

    val all = listOf(Midnight, Forest, Dusk, Ocean, Lofi)
}

/** Wrap any screen to give it the theme's gradient background. */
@Composable
fun ThemedBackground(
    theme: FocusTheme,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(theme.top, theme.bottom)))
    ) {
        content()
    }
}

/** Row of round gradient swatches. Tap one to pick a theme. */
@Composable
fun ThemePicker(
    selected: FocusTheme,
    onSelect: (FocusTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Theme", color = RegainPalette.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            FocusThemes.all.forEach { t ->
                val isSelected = t.name == selected.name
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(t.top, t.bottom, t.accent)))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) t.accent else RegainPalette.SurfaceHigh,
                            shape = CircleShape
                        )
                        .clickable { onSelect(t) }
                )
            }
        }
    }
}
