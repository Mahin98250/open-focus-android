package org.openfocus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.openfocus.app.focus.FocusTimerStore

private val Ink = Color(0xFF0B0E13)
private val Panel = Color(0xFF151B24)
private val Accent = Color(0xFF9BE7C4)
private val Muted = Color(0xFF9AA6B5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenFocusTheme {
                DashboardScreen()
            }
        }
    }
}

@Composable
private fun OpenFocusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            onPrimary = Ink,
            background = Ink,
            surface = Panel,
            onSurface = Color(0xFFF3F6FA),
            secondary = Muted
        ),
        content = content
    )
}

@Composable
private fun DashboardScreen() {
    val context = LocalContext.current
    val timerStore = remember { FocusTimerStore(context) }
    var timerSnapshot by remember { mutableStateOf(timerStore.load()) }
    var remainingMillis by remember { mutableStateOf(timerSnapshot.remainingMillis) }

    LaunchedEffect(timerSnapshot.isRunning, timerSnapshot.endAtEpochMillis) {
        val endAt = timerSnapshot.endAtEpochMillis
        if (timerSnapshot.isRunning && endAt != null) {
            while (true) {
                val remaining = (endAt - System.currentTimeMillis()).coerceAtLeast(0L)
                remainingMillis = remaining
                if (remaining == 0L) {
                    timerStore.finish()
                    timerSnapshot = timerStore.load()
                    break
                }
                delay(1_000L)
            }
        } else {
            remainingMillis = timerSnapshot.remainingMillis
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Ink
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "OPEN FOCUS",
                    color = Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Make room\nfor what matters.",
                    color = Color.White,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Your private space to focus and reset.",
                    color = Muted,
                    fontSize = 14.sp
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Panel)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "READY WHEN YOU ARE",
                        color = Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        color = Color(0xFF202B35),
                        shape = CircleShape
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = String.format("%02d:%02d", remainingMillis / 60_000L, (remainingMillis / 1_000L) % 60L),
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                text = "FOCUS SESSION",
                                color = Accent,
                                fontSize = 10.sp,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (timerSnapshot.isRunning) {
                                timerStore.pause()
                            } else {
                                timerStore.start()
                            }
                            timerSnapshot = timerStore.load()
                            remainingMillis = timerSnapshot.remainingMillis
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor = Ink
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (timerSnapshot.isRunning) "Pause focus session" else if (timerSnapshot.isCompleted) "Start another session" else "Start focus session",
                            modifier = Modifier.padding(vertical = 5.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(
                        onClick = {
                            timerStore.reset()
                            timerSnapshot = timerStore.load()
                            remainingMillis = timerSnapshot.remainingMillis
                        }
                    ) {
                        Text("Reset timer", color = Muted)
                    }
                    Text(
                        text = if (timerSnapshot.isCompleted) "Session complete. Take a short break." else "Your timer state is saved on this device.",
                        color = Muted,
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = "YOUR SPACE",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "App limits",
                    subtitle = "Build healthier habits",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    title = "Insights",
                    subtitle = "Understand your time",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "OFFLINE FIRST  ·  PRIVATE BY DESIGN  ·  OPEN SOURCE",
                modifier = Modifier.fillMaxWidth(),
                color = Muted,
                fontSize = 9.sp,
                letterSpacing = 0.7.sp
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Panel)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
