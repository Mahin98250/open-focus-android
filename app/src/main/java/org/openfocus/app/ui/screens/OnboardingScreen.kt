package org.openfocus.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class WelcomeStep(val eyebrow: String, val title: String, val body: String, val symbol: String, val action: String)
private val welcomeSteps = listOf(
    WelcomeStep("A LITTLE SPACE FOR YOU", "Your attention deserves a garden.", "Make room for the things you care about. We'll help the noisy apps take a little step back.", "✿", "Let's make space"),
    WelcomeStep("GENTLE, NOT GUILTY", "Build habits that feel like you.", "Choose a focus rhythm, set kind boundaries and make steady progress—one small session at a time.", "❀", "That sounds good"),
    WelcomeStep("PRIVATE BY NATURE", "Your progress belongs to you.", "No ads. No paid tiers. No account required for core use. Your personal focus data stays on this device.", "✧", "Enter my space")
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val step = welcomeSteps[stepIndex]
    val rotation by animateFloatAsState(if (stepIndex % 2 == 0) -8f else 8f, label = "welcome-art-rotation")

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("✿  focus garden", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${stepIndex + 1} / ${welcomeSteps.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(contentAlignment = Alignment.Center) {
                    Surface(Modifier.size(228.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)) {}
                    Surface(Modifier.size(174.dp).rotate(rotation), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(step.symbol, fontSize = 86.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("✦", Modifier.align(Alignment.TopEnd).padding(18.dp), color = MaterialTheme.colorScheme.secondary, fontSize = 27.sp)
                    Text("✧", Modifier.align(Alignment.BottomStart).padding(20.dp), color = MaterialTheme.colorScheme.primary, fontSize = 34.sp)
                }
                Spacer(Modifier.height(38.dp))
                Text(step.eyebrow, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                Spacer(Modifier.height(12.dp))
                Text(step.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Text(step.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { if (stepIndex == welcomeSteps.lastIndex) onComplete() else stepIndex += 1 }, modifier = Modifier.fillMaxWidth()) {
                    Text(step.action, Modifier.padding(vertical = 5.dp))
                }
                if (stepIndex < welcomeSteps.lastIndex) {
                    TextButton(onClick = onComplete) { Text("Explore without setup") }
                } else {
                    Text("FREE FOREVER  ·  OFFLINE FIRST  ·  OPEN SOURCE", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}