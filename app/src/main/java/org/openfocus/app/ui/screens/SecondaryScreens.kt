package org.openfocus.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.text.DateFormat
import java.util.Calendar
import android.content.Intent
import android.provider.Settings
import org.openfocus.app.usage.UsageStatsRepository
import org.openfocus.app.usage.AppUsageSummary
import org.openfocus.app.focus.FocusSessionHistoryStore
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.openfocus.app.planner.PlannerStore
import org.openfocus.app.planner.PlannerTask

@Composable
fun PlannerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { PlannerStore(context) }
    val tasks = remember { mutableStateListOf<PlannerTask>().apply { addAll(store.load()) } }
    var draft by remember { mutableStateOf("") }

    Column(modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("TURN INTENTIONS INTO ACTION", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
            Text("A plan that breathes.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Keep your next steps clear, kind and achievable.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("YOUR TASKS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add a small next step") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val title = draft.trim()
                        if (title.isNotEmpty()) {
                            tasks.add(store.add(title))
                            draft = ""
                        }
                    }) { Text("Add") }
                }
                Text(tasks.count { it.completed }.toString() + " of " + tasks.size + " complete", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            items(tasks, key = { it.id }) { task ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.completed,
                            onCheckedChange = { checked ->
                                store.setCompleted(task.id, checked)
                                val index = tasks.indexOfFirst { it.id == task.id }
                                if (index >= 0) tasks[index] = task.copy(completed = checked)
                            }
                        )
                        Text(
                            task.title,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium,
                            color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = {
                            store.delete(task.id)
                            tasks.removeAll { it.id == task.id }
                        }) { Text("Remove") }
                    }
                }
            }
            if (tasks.isEmpty()) {
                item { Text("Your list is clear. Add one small task to begin.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
fun ScheduleScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Column(modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← App garden") }
        Text("MAKE SPACE AUTOMATIC", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        Text("Find your rhythm.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Scheduled blocking is the next milestone after focus-session blocking. These examples are design previews, not active schedules.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        PreviewSchedule("Morning deep work", "Monday – Friday · 7:00 AM – 9:00 AM")
        PreviewSchedule("Evening study time", "Monday – Thursday · 6:00 PM – 8:00 PM")
        PreviewSchedule("Sleep & unwind", "Every day · 10:00 PM – 6:00 AM")
    }
}

@Composable
private fun PreviewSchedule(title: String, details: String) {
    var enabled by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(details, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text("Preview only", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall)
            }
            Switch(checked = enabled, onCheckedChange = { enabled = it })
        }
    }
}

@Composable
fun InsightsScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val sessions = remember(context) { FocusSessionHistoryStore(context).load() }
    val usageRepository = remember(context) { UsageStatsRepository(context) }
    val disclosurePreferences = remember(context) {
        context.getSharedPreferences("usage_access_disclosure", android.content.Context.MODE_PRIVATE)
    }
    var disclosureAccepted by remember {
        mutableStateOf(disclosurePreferences.getBoolean("accepted", false))
    }
    var usageAccessGranted by remember { mutableStateOf(usageRepository.hasUsageAccess()) }
    var usageSummary by remember { mutableStateOf(usageRepository.loadLast24Hours()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    fun refreshUsage() {
        usageAccessGranted = usageRepository.hasUsageAccess()
        usageSummary = if (usageAccessGranted) usageRepository.loadLast24Hours() else null
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshUsage()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val weekStart = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
    }
    val weekSessions = sessions.filter { it.completedAtEpochMillis >= weekStart }
    val weekFocusMillis = weekSessions.sumOf { it.durationMillis }
    val allFocusMillis = sessions.sumOf { it.durationMillis }

    Column(modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← Overview") }
        Text("NOTICE YOUR PROGRESS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        Text("Your attention, in bloom.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            InsightMetric("LAST 7 DAYS", formatInsightDuration(weekFocusMillis), Modifier.weight(1f))
            InsightMetric("SESSIONS", weekSessions.size.toString(), Modifier.weight(1f))
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ALL-TIME FOCUS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                Text(formatInsightDuration(allFocusMillis), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(sessions.size.toString() + " completed sessions saved locally", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("DEVICE APP USAGE · LAST 24 HOURS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                if (!usageAccessGranted) {
                    Text("See how much time your apps use.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Android keeps this data behind a special Usage Access setting. When enabled, this app reads the system's foreground-time totals and last-used times, calculates the summary on this device and does not upload it.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = disclosureAccepted,
                            onCheckedChange = {
                                disclosureAccepted = it
                                disclosurePreferences.edit().putBoolean("accepted", it).apply()
                            }
                        )
                        Text(
                            "I understand what app-usage information this feature reads and how it is used.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        enabled = disclosureAccepted,
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }
                        }
                    ) { Text("Open Usage Access settings") }
                } else if (usageSummary == null) {
                    Text(
                        "Usage Access is enabled, but Android has not returned any usage records for this period yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = { refreshUsage() }) { Text("Refresh usage data") }
                } else {
                    val summary = usageSummary!!
                    Text(formatInsightDuration(summary.totalForegroundMillis), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Foreground time reported by Android", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    if (summary.apps.isEmpty()) {
                        Text("No app usage has been reported in the last 24 hours.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        summary.apps.take(8).forEach { app ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(app.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(formatInsightDuration(app.foregroundMillis), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    TextButton(onClick = { refreshUsage() }) { Text("Refresh usage data") }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("RECENT FOCUS SESSIONS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                if (sessions.isEmpty()) {
                    Text("Your first completed focus session will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    sessions.takeLast(8).asReversed().forEach { session ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(session.completedAtEpochMillis), style = MaterialTheme.typography.bodySmall)
                            Text(formatInsightDuration(session.durationMillis), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Text(
            "Focus history comes from sessions completed in this app. Device usage comes from Android's Usage Access API and stays local. These are different measurements.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun InsightMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatInsightDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes}m"
}

@Composable
fun GrowthScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Column(modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← Overview") }
        Text("CELEBRATE THE SMALL WINS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        Text("Look how you can grow.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("✿", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Your garden starts with one session.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Streaks and achievements will be calculated from real completed sessions—not demo values.", color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("YOUR APP, YOUR RULES", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        Text("Make it yours.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRIVACY PROMISE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                Text("Local-first. No ads. No account required.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Your selected app package names, timer state and tasks are stored on this device. No analytics or advertising SDKs are included.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ABOUT", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                Text("Focus Garden · Working title", fontWeight = FontWeight.SemiBold)
                Text("Independent open-source Android project. The final public brand is not selected yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Core and advanced features are intended to remain free. This is an early build; not all planned features are implemented.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}