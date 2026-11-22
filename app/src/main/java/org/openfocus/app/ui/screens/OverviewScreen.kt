package org.openfocus.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import org.openfocus.app.blocking.AppBlockStore
import org.openfocus.app.focus.FocusSessionHistoryStore
import org.openfocus.app.planner.PlannerStore
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun OverviewScreen(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val sessions = remember(context) { FocusSessionHistoryStore(context).load() }
    val selectedAppCount = remember(context) { AppBlockStore(context).blockedPackages().size }
    val tasks = remember(context) { PlannerStore(context).load() }
    val dailyStudyGoal = remember(context) {
        context.getSharedPreferences("app_onboarding", android.content.Context.MODE_PRIVATE)
            .getFloat("daily_study_goal_hours", 0f)
    }
    val focusPriorities = remember(context) {
        context.getSharedPreferences("app_onboarding", android.content.Context.MODE_PRIVATE)
            .getStringSet("focus_priorities", emptySet())?.toSet().orEmpty()
    }
    val completedTaskCount = tasks.count { it.completed }
    val todayRange = remember {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        start to calendar.timeInMillis
    }
    val todaySessions = sessions.filter {
        it.completedAtEpochMillis >= todayRange.first && it.completedAtEpochMillis < todayRange.second
    }
    val todayFocusMillis = todaySessions.sumOf { it.durationMillis }
    val goalMillis = (dailyStudyGoal * 3_600_000f).toLong().coerceAtLeast(0L)
    val goalProgress = if (goalMillis > 0L) {
        (todayFocusMillis.toFloat() / goalMillis).coerceIn(0f, 1f)
    } else 0f
    val animatedGoalProgress by animateFloatAsState(
        targetValue = goalProgress,
        label = "daily-focus-goal-progress"
    )
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("A FRESH START", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
            Text("Make room for what matters.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("A little progress is still progress. What would you like to grow today?", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("✿  YOUR FOCUS SPACE", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
                Text("One thing at a time.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Give your best energy to what matters most.", color = MaterialTheme.colorScheme.onPrimaryContainer)
                Button(onClick = { onNavigate("focus") }) { Text("Start focusing  →") }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("THE GOAL YOU CHOSE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                Text(
                    if (dailyStudyGoal > 0f) String.format(java.util.Locale.ROOT, "%.1f hours a day", dailyStudyGoal) else "Choose a daily study goal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (focusPriorities.isNotEmpty()) {
                    Text(focusPriorities.joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("TODAY'S FOCUS PROGRESS", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                if (goalMillis > 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatFocusDuration(todayFocusMillis), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("${(goalProgress * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    LinearProgressIndicator(
                        progress = animatedGoalProgress,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                    )
                    Text(
                        if (todayFocusMillis >= goalMillis) "Daily target reached. Be proud of the time you protected today."
                        else "${formatFocusDuration(goalMillis - todayFocusMillis)} to reach your ${dailyStudyGoal.toHoursLabel()} target · ${todaySessions.size} completed sessions",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text("Choose a daily target to make your progress visible.", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    TextButton(onClick = { onNavigate("settings") }) { Text("Set daily target", color = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
            }
        }
        Text("YOUR DAY AT A GLANCE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("TODAY'S FOCUS", formatFocusDuration(todayFocusMillis), "${todaySessions.size} completed sessions", Modifier.weight(1f))
            MetricCard("BLOCK LIST", selectedAppCount.toString(), "Apps you selected", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("TASKS", completedTaskCount.toString() + " / " + tasks.size, "Completed in your planner", Modifier.weight(1f))
            MetricCard("SESSIONS", sessions.size.toString(), "Saved on this device", Modifier.weight(1f))
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("YOUR NEXT SMALL STEP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text("Choose the distractions you want to keep away.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Selected apps are blocked during a running focus session or an active recurring schedule, after you explicitly enable the Android accessibility service.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = { onNavigate("apps") }) { Text("Set up my app garden") }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("GROW AT YOUR OWN PACE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text("Progress isn't a race.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Your dashboard will fill with real focus sessions and locally collected data as you use the app. No sample statistics are presented as personal results.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Row {
                    TextButton(onClick = { onNavigate("insights") }) { Text("Insights") }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = { onNavigate("growth") }) { Text("Growth") }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(caption, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun Float.toHoursLabel(): String =
    if (this % 1f == 0f) "${toInt()}h/day" else "${"%.1f".format(java.util.Locale.ROOT, this)}h/day"

private fun formatFocusDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes}m"
}
