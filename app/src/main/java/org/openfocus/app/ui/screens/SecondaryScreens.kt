package org.openfocus.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.DateFormat
import java.util.Calendar
import android.content.Intent
import android.provider.Settings
import org.openfocus.app.usage.UsageStatsRepository
import org.openfocus.app.focus.FocusSessionHistoryStore
import org.openfocus.app.focus.FocusTimerStore
import org.openfocus.app.blocking.AppBlockSchedule
import org.openfocus.app.blocking.AppBlockStore
import org.openfocus.app.blocking.DailyAppLimitStore
import org.openfocus.app.blocking.YoutubeStudyModeStore
import org.openfocus.app.blocking.BlockScheduleStore
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openfocus.app.planner.PlannerStore
import org.openfocus.app.planner.PlannerTask
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PlannerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { PlannerStore(context) }
    val tasks = remember { mutableStateListOf<PlannerTask>().apply { addAll(store.load()) } }
    var draft by remember { mutableStateOf("") }
    val completed = tasks.count { it.completed }
    val progress = if (tasks.isEmpty()) 0f else completed.toFloat() / tasks.size

    Column(modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("STUDY PLANNER", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.6.sp)
            Text("Plan your day.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text("Turn your next study goals into small, clear steps.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("TODAY'S CHECKLIST", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                        Text("$completed of ${tasks.size} complete", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Text("${(progress * 100).toInt()}%", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                androidx.compose.material3.LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                )
                Text(
                    if (tasks.isEmpty()) "Add your first task to begin building a plan."
                    else if (completed == tasks.size) "Everything on your list is complete. Nice work."
                    else "One task at a time. Keep the next step achievable.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ADD A STUDY TASK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.1.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it.take(100) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("e.g. Revise chemical reactions") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = draft.isNotBlank(),
                        onClick = {
                            val title = draft.trim()
                            if (title.isNotEmpty()) {
                                tasks.add(store.add(title))
                                draft = ""
                            }
                        }
                    ) { Text("Add") }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("YOUR TASKS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
            Text("${tasks.size} total", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks, key = { it.id }) { task ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(17.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 7.dp),
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
                        Column(Modifier.weight(1f).padding(start = 5.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                task.title,
                                fontWeight = FontWeight.SemiBold,
                                color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            Text(if (task.completed) "Completed" else "Ready when you are", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = {
                            store.delete(task.id)
                            tasks.removeAll { it.id == task.id }
                        }) { Text("Remove") }
                    }
                }
            }
            if (tasks.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("☷", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineLarge)
                            Text("Your plan starts here", fontWeight = FontWeight.Bold)
                            Text("Add one small task. You can tick it off when it is done.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleScreen(modifier: Modifier = Modifier, onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember(context) { BlockScheduleStore(context) }
    val schedules = remember { mutableStateListOf<AppBlockSchedule>().apply { addAll(store.load()) } }
    var name by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("16:00") }
    var endTime by remember { mutableStateOf("18:00") }
    var selectedDays by remember {
        mutableStateOf(
            setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)
        )
    }

    val weekdays = listOf(
        Calendar.SUNDAY to "S",
        Calendar.MONDAY to "M",
        Calendar.TUESDAY to "T",
        Calendar.WEDNESDAY to "W",
        Calendar.THURSDAY to "T",
        Calendar.FRIDAY to "F",
        Calendar.SATURDAY to "S"
    )
    val parsedStart = parseClockTime(startTime)
    val parsedEnd = parseClockTime(endTime)
    val canAdd = name.isNotBlank() && parsedStart != null && parsedEnd != null &&
        parsedStart != parsedEnd && selectedDays.isNotEmpty()

    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text("← App blocking") }
        Text("SCHEDULE PLANNER", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
        Text("Plan your studies.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Text(
            "Set focus windows for your week. Your selected apps pause automatically during these times.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(23.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("BUILD A CONSISTENT ROUTINE", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("Protect time for the things that matter.", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Choose a preset or create your own repeating schedule.", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(selected = name == "Morning Study", onClick = { name = "Morning Study"; startTime = "06:00"; endTime = "08:00" }, label = { Text("Morning") })
            FilterChip(selected = name == "Afternoon Study", onClick = { name = "Afternoon Study"; startTime = "14:00"; endTime = "16:00" }, label = { Text("Afternoon") })
            FilterChip(selected = name == "Evening Study", onClick = { name = "Evening Study"; startTime = "18:00"; endTime = "20:00" }, label = { Text("Evening") })
        }

        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("CREATE A FOCUS WINDOW", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(64) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Schedule name") },
                    placeholder = { Text("e.g. Evening study") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it.filter { ch -> ch.isDigit() || ch == ':' }.take(5) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Start · HH:mm") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it.filter { ch -> ch.isDigit() || ch == ':' }.take(5) },
                        modifier = Modifier.weight(1f),
                        label = { Text("End · HH:mm") },
                        singleLine = true
                    )
                }
                Text("REPEAT ON", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    weekdays.forEach { (day, label) ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
                            },
                            label = { Text(label) }
                        )
                    }
                }
                if (parsedStart != null && parsedStart == parsedEnd) {
                    Text("Start and end times must be different.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                } else if (parsedStart == null || parsedEnd == null) {
                    Text("Enter times in 24-hour HH:mm format, such as 07:30 or 18:15.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                } else if (parsedEnd < parsedStart) {
                    Text("Overnight window: this schedule continues into the next selected day.", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canAdd,
                    onClick = {
                        val schedule = store.add(name, parsedStart!!, parsedEnd!!, selectedDays)
                        schedules.add(schedule)
                        name = ""
                    }
                ) { Text("＋ Add focus schedule") }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("YOUR SCHEDULES", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp)
            Text("${schedules.size} saved", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
        if (schedules.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Text(
                    "No schedules yet. Add a focus window above to automatically block your selected apps during those hours.",
                    modifier = Modifier.padding(18.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            schedules.toList().forEach { schedule ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(schedule.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    formatClockTime(schedule.startMinuteOfDay) + " – " + formatClockTime(schedule.endMinuteOfDay),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Switch(
                                checked = schedule.enabled,
                                onCheckedChange = { enabled ->
                                    store.setEnabled(schedule.id, enabled)
                                    val index = schedules.indexOfFirst { it.id == schedule.id }
                                    if (index >= 0) schedules[index] = schedule.copy(enabled = enabled)
                                }
                            )
                        }
                        Text(
                            weekdays.filter { it.first in schedule.daysOfWeek }.joinToString(" · ") { it.second },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = {
                            store.delete(schedule.id)
                            schedules.removeAll { it.id == schedule.id }
                        }) { Text("Remove schedule") }
                    }
                }
            }
        }
        Text(
            "A schedule only blocks apps in your selected blocked-app list and only while the Android accessibility service is enabled. You can turn schedules off or disable access in Android Settings. Blocking is not tamper-proof.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun parseClockTime(value: String): Int? {
    val parts = value.trim().split(":")
    if (parts.size != 2) return null
    val hours = parts[0].toIntOrNull() ?: return null
    val minutes = parts[1].toIntOrNull() ?: return null
    if (hours !in 0..23 || minutes !in 0..59) return null
    return hours * 60 + minutes
}

private fun formatClockTime(minuteOfDay: Int): String =
    "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

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
    var disclosureAccepted by remember { mutableStateOf(disclosurePreferences.getBoolean("accepted", false)) }
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

    val weekStart = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis }
    val weekSessions = sessions.filter { it.completedAtEpochMillis >= weekStart }
    val weekFocusMillis = weekSessions.sumOf { it.durationMillis }
    val allFocusMillis = sessions.sumOf { it.durationMillis }
    val dayBars = (6 downTo 0).map { offset ->
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -offset)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val from = start.timeInMillis
        val untilTime = Calendar.getInstance().apply { timeInMillis = from; add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
        val duration = sessions.filter { it.completedAtEpochMillis >= from && it.completedAtEpochMillis < untilTime }.sumOf { it.durationMillis }
        val label = DateFormat.getDateInstance(DateFormat.SHORT).format(from)
        Triple(label, duration, from)
    }
    val maxDayFocus = dayBars.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L

    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← Home") }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("SCREEN TIME & FOCUS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
            Text("Know where your time goes.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text("A clear view of your focus sessions and Android-reported app usage.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LAST 7 DAYS · FOCUS TIME", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text(formatInsightDuration(weekFocusMillis), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("${weekSessions.size} completed sessions", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            InsightMetric("ALL-TIME FOCUS", formatInsightDuration(allFocusMillis), Modifier.weight(1f))
            InsightMetric("TOTAL SESSIONS", sessions.size.toString(), Modifier.weight(1f))
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
        ) {
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text("DAILY FOCUS TREND", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                dayBars.forEach { (label, duration, _) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatInsightDuration(duration), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = (duration.toFloat() / maxDayFocus).coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
                if (sessions.isEmpty()) Text("Complete a focus session to start building your trend.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
        ) {
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("DEVICE APP USAGE · LAST 24 HOURS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                if (!usageAccessGranted) {
                    Text("See how much time your apps use.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Android protects this information behind Usage Access. When enabled, Open Focus reads aggregate foreground-time totals and calculates the breakdown on this phone. It does not upload the data.",
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
                        Text("I understand what app-usage information this feature reads.", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        enabled = disclosureAccepted,
                        onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) } },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Open Usage Access settings") }
                } else if (usageSummary == null) {
                    Text("Usage Access is enabled, but Android has not returned records for this period yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { refreshUsage() }) { Text("Refresh usage data") }
                } else {
                    val summary = usageSummary!!
                    Text(formatInsightDuration(summary.totalForegroundMillis), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Foreground time reported by Android", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    val maxAppTime = summary.apps.maxOfOrNull { it.foregroundMillis }?.coerceAtLeast(1L) ?: 1L
                    if (summary.apps.isEmpty()) {
                        Text("No app usage has been reported in the last 24 hours.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        summary.apps.take(8).forEach { app ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(app.displayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(formatInsightDuration(app.foregroundMillis), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                }
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = (app.foregroundMillis.toFloat() / maxAppTime).coerceIn(0f, 1f),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                    TextButton(onClick = { refreshUsage() }) { Text("Refresh usage data") }
                }
            }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
        ) {
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("RECENT FOCUS SESSIONS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                if (sessions.isEmpty()) {
                    Text("Your first completed focus session will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    sessions.takeLast(8).asReversed().forEach { session ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(session.completedAtEpochMillis), style = MaterialTheme.typography.bodySmall)
                                session.intention?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall) }
                            }
                            Text(formatInsightDuration(session.durationMillis), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Text(
            "Focus history comes from sessions completed in Open Focus. Device app usage comes from Android's Usage Access API. These are different measurements and both stay local.",
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
    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val historyStore = remember(context) { FocusSessionHistoryStore(context) }
    var sessions by remember(context) { mutableStateOf(historyStore.load()) }
    DisposableEffect(lifecycleOwner, historyStore) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                sessions = historyStore.load()
                refreshKey += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val now = System.currentTimeMillis()
    val stats = remember(sessions, now, refreshKey) {
        org.openfocus.app.focus.FocusGrowthStats.calculate(sessions, now)
    }
    val nextSessionMilestone = listOf(1, 5, 10, 25, 50, 100).firstOrNull { it > stats.totalSessions } ?: 100
    val sessionProgress = (stats.totalSessions.toFloat() / nextSessionMilestone).coerceIn(0f, 1f)
    val nextHourMilestone = listOf(1, 5, 10, 25, 50, 100).firstOrNull { it > stats.totalFocusHours } ?: 100
    val hourProgress = (stats.totalFocusHours.toFloat() / nextHourMilestone).coerceIn(0f, 1f)

    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← Overview") }
        Text("YOUR PROGRESS, MADE VISIBLE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        Text("Small steps add up.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Every number below comes from focus sessions completed on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            InsightMetric("CURRENT STREAK", "${stats.currentStreak} days", Modifier.weight(1f))
            InsightMetric("BEST STREAK", "${stats.longestStreak} days", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            InsightMetric("SESSIONS", stats.totalSessions.toString(), Modifier.weight(1f))
            InsightMetric("FOCUS TIME", "${stats.totalFocusHours}h", Modifier.weight(1f))
        }
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("LAST 7 DAYS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                if (stats.lastSevenDays.all { it.focusMillis == 0L }) {
                    Text("Your first completed session will start this chart.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    stats.lastSevenDays.forEach { day ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(day.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                Text(formatInsightDuration(day.focusMillis), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = day.barProgress,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("NEXT MILESTONES", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                Text("Sessions · ${stats.totalSessions} / $nextSessionMilestone", fontWeight = FontWeight.SemiBold)
                androidx.compose.material3.LinearProgressIndicator(
                    progress = sessionProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Focus time · ${stats.totalFocusHours} / $nextHourMilestone hours", fontWeight = FontWeight.SemiBold)
                androidx.compose.material3.LinearProgressIndicator(
                    progress = hourProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Milestones unlock from real completed sessions. Paused or abandoned timers do not count.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ACHIEVEMENTS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                val achievements = listOf(
                    Triple("First focus", "Complete your first session", stats.totalSessions >= 1),
                    Triple("Finding rhythm", "Complete 5 sessions", stats.totalSessions >= 5),
                    Triple("Ten deep dives", "Complete 10 sessions", stats.totalSessions >= 10),
                    Triple("A steady habit", "Focus on 5 different days", stats.activeDays >= 5),
                    Triple("One hour protected", "Accumulate 1 hour of focus", stats.totalFocusMillis >= 3_600_000L),
                    Triple("Ten hours invested", "Accumulate 10 hours of focus", stats.totalFocusMillis >= 36_000_000L)
                )
                achievements.forEach { (title, description, unlocked) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(if (unlocked) "✓" else "○", color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleLarge)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(title, fontWeight = FontWeight.SemiBold)
                            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(if (unlocked) "UNLOCKED" else "LOCKED", color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun FocusTogetherScreen(modifier: Modifier = Modifier, onStartFocus: (String) -> Unit) {
    var roomName by remember { mutableStateOf("Study session") }
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("FOCUS WITH OTHERS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
        Text("Better together.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Text("A place for shared study sessions and accountability.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("YOUR FOCUS ROOM", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("Set an intention for your next session.", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("What will you focus on?") }
                )
                Button(
                    onClick = { onStartFocus(roomName.trim().ifBlank { "Study session" }) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Open my focus timer") }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LIVE GROUPS & LEADERBOARDS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("Multiplayer is not connected yet.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Live rooms, member activity and global rankings need a shared online service. Open Focus is currently offline-first and does not create fake participants, scores or live room counts.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                ) {
                    Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("◎", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall)
                        Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Local focus is ready", fontWeight = FontWeight.SemiBold)
                            Text("Your timer, history and streaks work without an account.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("WEEKLY LEADERBOARD", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("Your next challenge starts with consistency.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "No public rankings are shown until shared group sessions exist. Your personal streak and milestones are available in Growth.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val onboardingPreferences = remember {
        context.getSharedPreferences("app_onboarding", android.content.Context.MODE_PRIVATE)
    }
    var goalHours by remember {
        mutableStateOf(
            onboardingPreferences.getFloat("daily_study_goal_hours", 3f).coerceIn(1f, 12f)
        )
    }
    var goalSaved by remember { mutableStateOf(false) }
    var exportPayload by remember { mutableStateOf("") }
    var dataActionStatus by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) {
            dataActionStatus = "Export cancelled."
        } else {
            val result = runCatching {
                val output = context.contentResolver.openOutputStream(uri)
                    ?: error("Android could not open the selected file.")
                output.bufferedWriter(Charsets.UTF_8).use { it.write(exportPayload) }
            }
            dataActionStatus = if (result.isSuccess) {
                "Backup saved successfully."
            } else {
                "Backup could not be saved. Please choose another location."
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Open Focus data?") },
            text = {
                Text("This permanently clears your local focus history, tasks, app block list, schedules, limits and preferences from this phone. It cannot be undone. Android's Accessibility permission itself will not be changed.")
            },
            confirmButton = {
                TextButton(onClick = {
                    clearOpenFocusLocalData(context)
                    showDeleteConfirmation = false
                    dataActionStatus = "Local data cleared. Restart Open Focus to run setup again."
                }) { Text("Delete data") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SETTINGS & PREFERENCES", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
        Text("Make it yours.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Text("Personalise your daily target and review how Open Focus protects your data.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("DAILY STUDY TARGET", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall)
                Text(
                    if (goalHours % 1f == 0f) "${goalHours.toInt()} hours a day" else "${"%.1f".format(java.util.Locale.ROOT, goalHours)} hours a day",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Slider(
                    value = goalHours,
                    onValueChange = {
                        goalHours = it
                        goalSaved = false
                    },
                    valueRange = 1f..12f,
                    steps = 10
                )
                Text("Your dashboard counts completed focus sessions towards this target. Change it whenever your schedule changes.", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onboardingPreferences.edit().putFloat("daily_study_goal_hours", goalHours).apply()
                        goalSaved = true
                    }
                ) { Text(if (goalSaved) "Target saved" else "Save daily target") }
            }
        }
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PRIVACY PROMISE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                Text("Local-first. No ads. No account required.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Your selected app package names, timer state and tasks are stored on this device. No analytics or advertising SDKs are included.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("YOUR DATA", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                Text("Back up or clear this device.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Export creates a readable JSON backup of your focus history, tasks, app selections, schedules and preferences. Nothing is uploaded.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        exportPayload = buildOpenFocusLocalExport(context)
                        val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(Date())
                        exportLauncher.launch("open-focus-backup-$stamp.json")
                    }
                ) { Text("Export my data") }
                TextButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete all local app data", color = MaterialTheme.colorScheme.error) }
                dataActionStatus?.let {
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ABOUT", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                Text("Open Focus", fontWeight = FontWeight.SemiBold)
                Text("An independent, open-source digital-wellbeing app built to help you protect your attention.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Core and advanced features are intended to remain free. This is an early build; not all planned features are implemented.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun buildOpenFocusLocalExport(context: android.content.Context): String {
    val root = JSONObject()
        .put("format", "open-focus-backup")
        .put("schema_version", 1)
        .put("exported_at_epoch_millis", System.currentTimeMillis())

    val onboarding = context.getSharedPreferences("app_onboarding", android.content.Context.MODE_PRIVATE)
    val priorities = JSONArray()
    onboarding.getStringSet("focus_priorities", emptySet()).orEmpty().sorted().forEach(priorities::put)
    root.put(
        "preferences",
        JSONObject()
            .put("daily_study_goal_hours", onboarding.getFloat("daily_study_goal_hours", 0f).toDouble())
            .put("preferred_focus_minutes", onboarding.getInt("preferred_focus_minutes", 25))
            .put("reported_daily_screen_hours", onboarding.getInt("reported_daily_screen_hours", 0))
            .put("focus_priorities", priorities)
    )

    val blockedApps = JSONArray()
    AppBlockStore(context).blockedPackages().sorted().forEach(blockedApps::put)
    root.put("blocked_apps", blockedApps)

    val limits = JSONObject()
    DailyAppLimitStore(context).limitsMinutes().toSortedMap().forEach { (packageName, minutes) ->
        limits.put(packageName, minutes)
    }
    root.put("daily_app_limits_minutes", limits)
    root.put("youtube_study_mode_enabled", YoutubeStudyModeStore(context).isEnabled())

    val schedules = JSONArray()
    BlockScheduleStore(context).load().forEach { schedule ->
        val days = JSONArray()
        schedule.daysOfWeek.sorted().forEach(days::put)
        schedules.put(
            JSONObject()
                .put("id", schedule.id)
                .put("name", schedule.name)
                .put("start_minute_of_day", schedule.startMinuteOfDay)
                .put("end_minute_of_day", schedule.endMinuteOfDay)
                .put("days_of_week", days)
                .put("enabled", schedule.enabled)
        )
    }
    root.put("schedules", schedules)

    val tasks = JSONArray()
    PlannerStore(context).load().forEach { task ->
        tasks.put(JSONObject().put("id", task.id).put("title", task.title).put("completed", task.completed))
    }
    root.put("tasks", tasks)

    val sessions = JSONArray()
    FocusSessionHistoryStore(context).load().forEach { session ->
        val item = JSONObject()
            .put("completed_at_epoch_millis", session.completedAtEpochMillis)
            .put("duration_millis", session.durationMillis)
        session.intention?.let { item.put("intention", it) }
        sessions.put(item)
    }
    root.put("completed_focus_sessions", sessions)

    val timer = FocusTimerStore(context).load()
    root.put(
        "timer",
        JSONObject()
            .put("is_running", timer.isRunning)
            .put("end_at_epoch_millis", timer.endAtEpochMillis ?: JSONObject.NULL)
            .put("remaining_millis", timer.remainingMillis)
            .put("total_duration_millis", timer.totalDurationMillis)
            .put("is_completed", timer.isCompleted)
            .put("intention", timer.intention ?: JSONObject.NULL)
    )
    return root.toString(2)
}

private fun clearOpenFocusLocalData(context: android.content.Context) {
    // Cancel any scheduled completion alarm before clearing the timer's preferences.
    FocusTimerStore(context).reset()
    listOf(
        "app_onboarding",
        "app_block_rules",
        "app_block_schedules",
        "daily_app_limits",
        "youtube_study_mode",
        "app_block_disclosure",
        "focus_session_history",
        "focus_timer",
        "planner_tasks",
        "focus_ui_settings"
    ).forEach(context::deleteSharedPreferences)
}
