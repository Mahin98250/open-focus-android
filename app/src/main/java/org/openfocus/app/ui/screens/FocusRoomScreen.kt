package org.openfocus.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
        } else saved
    }
    var snapshot by remember { mutableStateOf(initialSnapshot) }
    var remaining by remember { mutableStateOf(snapshot.remainingMillis) }
    var selectedMinutes by remember { mutableIntStateOf((snapshot.totalDurationMillis / 60_000L).toInt()) }
    var customMinutesText by remember { mutableStateOf("") }
    var intention by remember { mutableStateOf(initialSnapshot.intention.orEmpty()) }
    var strictModeEnabled by remember { mutableStateOf(store.isStrictModeEnabled()) }
    val focusUiPreferences = remember(context) { context.getSharedPreferences("focus_ui_settings", Context.MODE_PRIVATE) }
    var selectedTheme by remember { mutableStateOf(focusUiPreferences.getString("focus_theme", "Forest") ?: "Forest") }
    var pomodoroMode by remember { mutableStateOf(false) }
    val focusGradient = when (selectedTheme) {
        "Aurora" -> listOf(Color(0xFF123A42), Color(0xFF101D29), Color(0xFF0B1017))
        "Midnight" -> listOf(Color(0xFF292347), Color(0xFF17172D), Color(0xFF0B0D16))
        else -> listOf(Color(0xFF24451F), Color(0xFF111D14), Color(0xFF0B110D))
    }

    LaunchedEffect(snapshot.isRunning, snapshot.endAtEpochMillis) {
        while (snapshot.isRunning) {
            delay(1_000L)
            snapshot = store.load()
            remaining = snapshot.remainingMillis
            strictModeEnabled = store.isStrictModeEnabled()
            if (snapshot.isCompleted && store.takeCompletionNotification()) FocusCompletionNotification.show(context)
        }
        remaining = snapshot.remainingMillis
        strictModeEnabled = store.isStrictModeEnabled()
        if (snapshot.isCompleted && store.takeCompletionNotification()) FocusCompletionNotification.show(context)
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
        } else store.start(intention = intention.trim())
        snapshot = store.load()
        remaining = snapshot.remainingMillis
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        startOrResume()
    }

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("FOCUS TIMER", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.8.sp)
            Text("Lock in. Tune out.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text("Make a little space for your most important work.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !pomodoroMode,
                onClick = { if (!snapshot.isRunning) pomodoroMode = false },
                enabled = !snapshot.isRunning,
                label = { Text("Timer") }
            )
            FilterChip(
                selected = pomodoroMode,
                onClick = {
                    if (!snapshot.isRunning) {
                        pomodoroMode = true
                        selectedMinutes = 25
                        store.reset(25 * 60_000L)
                        snapshot = store.load()
                        remaining = snapshot.remainingMillis
                    }
                },
                enabled = !snapshot.isRunning,
                label = { Text("Pomodoro · 25/5") }
            )
        }
        if (pomodoroMode) {
            Text(
                "One 25-minute focus round. Plan a 5-minute rest after the round; automatic break cycling is not enabled yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp))
                .background(Brush.linearGradient(focusGradient))
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    if (snapshot.isRunning) "SESSION IN PROGRESS" else if (snapshot.isCompleted) "SESSION COMPLETE" else "READY WHEN YOU ARE",
                    color = Color(0xFFD5F8B7),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp
                )
                Box(
                    Modifier.size(252.dp).background(
                        Brush.radialGradient(listOf(Color(0x553C8A32), Color(0x22264D25), Color.Transparent)),
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(224.dp)) {
                        val strokeWidth = 9.dp.toPx()
                        drawArc(trackColor.copy(alpha = 0.8f), -90f, 360f, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                        drawArc(progressColor, -90f, 360f * progress, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(timeText, color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                        Text("MINUTES LEFT", color = Color(0xFFB7D0B4), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.8.sp)
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
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("STRICT SESSION", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                        Text(
                            if (strictModeEnabled && snapshot.isRunning) "Pause and reset are locked"
                            else if (strictModeEnabled) "Lock controls when you start"
                            else "Normal timer controls",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Switch(
                        checked = strictModeEnabled,
                        enabled = !snapshot.isRunning,
                        onCheckedChange = { enabled ->
                            store.setStrictModeEnabled(enabled)
                            strictModeEnabled = store.isStrictModeEnabled()
                        }
                    )
                }
                Text(
                    "When active, Open Focus disables in-app pause and reset until the timer finishes. It cannot prevent force-stop, clearing app data, removing Accessibility access or restarting the phone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Text("SESSION LENGTH", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
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
                    enabled = !(snapshot.isRunning && strictModeEnabled),
                    onClick = {
                        if (snapshot.isRunning) {
                            store.pause()
                            snapshot = store.load()
                            remaining = snapshot.remainingMillis
                        } else if (Build.VERSION.SDK_INT >= 33 &&
                            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else startOrResume()
                    }
                ) {
                    Text(
                        when {
                            snapshot.isRunning && strictModeEnabled -> "Strict session in progress"
                            snapshot.isRunning -> "Pause session"
                            snapshot.isCompleted -> "Start again"
                            else -> "Start focus session"
                        }
                    )
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !(snapshot.isRunning && strictModeEnabled),
                    onClick = {
                        store.reset(selectedMinutes * 60_000L)
                        snapshot = store.load()
                        remaining = snapshot.remainingMillis
                    }
                ) { Text("Reset timer") }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("WHAT ARE YOU WORKING ON?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.1.sp)
                OutlinedTextField(
                    value = intention,
                    onValueChange = { intention = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Revise quadratic equations") },
                    singleLine = true,
                    enabled = !snapshot.isRunning
                )
                Text(
                    if (snapshot.isRunning) "Your intention is saved with this session. Pause to edit it before resuming."
                    else "Your intention is saved with the completed session in local history. Selected apps are redirected during this timer when the Accessibility service is enabled.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("SESSION LOOK", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("Choose your focus atmosphere.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("Forest", "Aurora", "Midnight").forEach { theme ->
                        FilterChip(
                            selected = selectedTheme == theme,
                            onClick = {
                                selectedTheme = theme
                                focusUiPreferences.edit().putString("focus_theme", theme).apply()
                            },
                            label = { Text(theme) }
                        )
                    }
                }
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("♫", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                        Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Ambient sounds", fontWeight = FontWeight.SemiBold)
                            Text("Audio playback is not connected in this build.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
