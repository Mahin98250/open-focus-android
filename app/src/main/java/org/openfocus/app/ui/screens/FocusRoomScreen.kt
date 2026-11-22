package org.openfocus.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.openfocus.app.focus.FocusTimerStore
import org.openfocus.app.focus.FocusCompletionNotification

private val focusDurations = listOf(25, 45, 60)

@Composable
fun FocusRoomScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { FocusTimerStore(context) }
    val initialSnapshot = remember(context) {
        val saved = store.load()
        val onboarding = context.getSharedPreferences("app_onboarding", android.content.Context.MODE_PRIVATE)
        if (!onboarding.getBoolean("focus_duration_initialized", false)) {
            val preferred = onboarding.getInt("preferred_focus_minutes", 25).coerceIn(1, 180)
            store.reset(preferred * 60_000L)
            onboarding.edit().putBoolean("focus_duration_initialized", true).apply()
            store.load()
        } else {
            saved
        }
    }
    var snapshot by remember { mutableStateOf(initialSnapshot) }
    var remaining by remember { mutableStateOf(snapshot.remainingMillis) }
    var selectedMinutes by remember { mutableIntStateOf((snapshot.totalDurationMillis / 60_000L).toInt()) }
    var customMinutesText by remember { mutableStateOf("") }
    var intention by remember { mutableStateOf(initialSnapshot.intention.orEmpty()) }

    LaunchedEffect(snapshot.isRunning, snapshot.endAtEpochMillis) {
        while (snapshot.isRunning) {
            delay(1_000L)
            snapshot = store.load()
            remaining = snapshot.remainingMillis
            if (snapshot.isCompleted && store.takeCompletionNotification()) {
                FocusCompletionNotification.show(context)
            }
        }
        remaining = snapshot.remainingMillis
        if (snapshot.isCompleted && store.takeCompletionNotification()) {
            FocusCompletionNotification.show(context)
        }
    }

    val totalMillis = snapshot.totalDurationMillis.coerceAtLeast(1L)
    val progress = (remaining.toFloat() / totalMillis).coerceIn(0f, 1f)
    val seconds = (remaining / 1_000L).coerceAtLeast(0L)
    val timeText = "%02d:%02d".format(seconds / 60L, seconds % 60L)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val startOrResume: () -> Unit = {
        if (snapshot.isCompleted || snapshot.remainingMillis <= 0L) {
            store.start(durationMillis = selectedMinutes * 60_000L, intention = intention.trim())
        } else {
            store.start(intention = intention.trim())
        }
        snapshot = store.load()
        remaining = snapshot.remainingMillis
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // A denied notification permission does not prevent the user from focusing.
        startOrResume()
    }

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("YOUR QUIET CORNER", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
            Text("Enter your focus room.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("One clear intention. A little time. No noise.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    if (snapshot.isRunning) "YOU'RE IN YOUR FOCUS FLOW" else if (snapshot.isCompleted) "SESSION COMPLETE" else "READY WHEN YOU ARE",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp
                )
                Box(Modifier.size(236.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxWidth().size(236.dp)) {
                        val strokeWidth = 9.dp.toPx()
                        drawArc(trackColor, -90f, 360f, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                        drawArc(progressColor, -90f, 360f * progress, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(timeText, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Light)
                        Text("FOCUS SESSION", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    focusDurations.forEach { minutes ->
                        FilterChip(
                            selected = selectedMinutes == minutes,
                            onClick = {
                                if (!snapshot.isRunning) {
                                    selectedMinutes = minutes
                                    store.reset(minutes * 60_000L)
                                    snapshot = store.load()
                                    remaining = snapshot.remainingMillis
                                }
                            },
                            label = { Text("$minutes min") },
                            enabled = !snapshot.isRunning
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customMinutesText,
                        onValueChange = { customMinutesText = it.filter(Char::isDigit).take(3) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Custom minutes") },
                        singleLine = true,
                        enabled = !snapshot.isRunning
                    )
                    OutlinedButton(
                        enabled = !snapshot.isRunning && (customMinutesText.toIntOrNull()?.let { it in 1..180 } == true),
                        onClick = {
                            val minutes = customMinutesText.toIntOrNull() ?: return@OutlinedButton
                            selectedMinutes = minutes
                            store.reset(minutes * 60_000L)
                            snapshot = store.load()
                            remaining = snapshot.remainingMillis
                        }
                    ) { Text("Set") }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (snapshot.isRunning) {
                            store.pause()
                            snapshot = store.load()
                            remaining = snapshot.remainingMillis
                        } else if (
                            Build.VERSION.SDK_INT >= 33 &&
                            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            startOrResume()
                        }
                    }
                ) { Text(if (snapshot.isRunning) "Pause session" else "Start focus session") }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        store.reset(selectedMinutes * 60_000L)
                        snapshot = store.load()
                        remaining = snapshot.remainingMillis
                    }
                ) { Text("Reset timer") }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("SET AN INTENTION", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(
                    value = intention,
                    onValueChange = { intention = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What deserves your attention?") },
                    singleLine = true,
                    enabled = !snapshot.isRunning
                )
                Text(
                    if (snapshot.isRunning) "Your intention is saved with this session. Pause to edit it before resuming."
                    else "Your intention is saved with the completed session in local history. Selected apps are redirected during this focus timer or an active schedule when the accessibility service is enabled.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}