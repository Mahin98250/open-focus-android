package org.openfocus.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openfocus.app.blocking.AppBlockStore
import org.openfocus.app.focus.FocusSessionHistoryStore
import org.openfocus.app.planner.PlannerStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val todaySessions = sessions.filter { it.completedAtEpochMillis >= todayRange.first && it.completedAtEpochMillis < todayRange.second }
    val todayFocusMillis = todaySessions.sumOf { it.durationMillis }
    val goalMillis = (dailyStudyGoal * 3_600_000f).toLong().coerceAtLeast(0L)
    val goalProgress = if (goalMillis > 0L) (todayFocusMillis.toFloat() / goalMillis).coerceIn(0f, 1f) else 0f
    val animatedGoalProgress = animateFloatAsState(targetValue = goalProgress, label = "daily-focus-goal-progress").value
    val dateLabel = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()) }

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(dateLabel.uppercase(Locale.getDefault()), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
            Text("Make today count.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text("Small sessions. Real progress. One day at a time.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF315C26), Color(0xFF17291A), Color(0xFF101A14))))
        ) {
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text("YOUR NEXT FOCUS SESSION", color = Color(0xFFD5F8B7), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
                Text("Ready to get in the zone?", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Put your phone aside and give one task your full attention.", color = Color(0xFFD2E2D0), style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { onNavigate("focus") }) { Text("Start focusing  →") }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("TODAY'S FOCUS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                        Text(formatFocusDuration(todayFocusMillis), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("DAILY GOAL", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        Text(if (dailyStudyGoal > 0f) dailyStudyGoal.toHoursLabel() else "Not set", fontWeight = FontWeight.SemiBold)
                    }
                }
                LinearProgressIndicator(
                    progress = animatedGoalProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    when {
                        goalMillis <= 0L -> "Set a daily goal to track your progress here."
                        todayFocusMillis >= goalMillis -> "Daily goal reached. Great work protecting your time."
                        else -> "${formatFocusDuration(goalMillis - todayFocusMillis)} left to reach your goal · ${todaySessions.size} completed sessions"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("BLOCKED APPS", selectedAppCount.toString(), "On your pause list", Modifier.weight(1f))
            MetricCard("TASKS DONE", "${completedTaskCount} / ${tasks.size}", "In your planner", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("FOCUS SESSIONS", sessions.size.toString(), "Completed on this device", Modifier.weight(1f))
            MetricCard("PRIORITIES", focusPriorities.size.toString(), "Personal focus goals", Modifier.weight(1f))
        }

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("QUICK ACCESS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
                Text("Make it yours", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(onClick = { onNavigate("apps") }, modifier = Modifier.weight(1f)) { Text("App blocker") }
                OutlinedButton(onClick = { onNavigate("planner") }, modifier = Modifier.weight(1f)) { Text("Study planner") }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("KEEP YOUR MOMENTUM", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("Your progress, at a glance.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("See your streaks, weekly focus time and achievements—calculated from completed sessions stored on this phone.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Row {
                    TextButton(onClick = { onNavigate("insights") }) { Text("View insights") }
                    Spacer(Modifier.width(3.dp))
                    TextButton(onClick = { onNavigate("growth") }) { Text("Growth & streaks") }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(caption, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun Float.toHoursLabel(): String {
    val decimalHours = "%.1f".format(Locale.ROOT, this)
    return if (this % 1f == 0f) "${toInt()}h/day" else "${decimalHours}h/day"
}

private fun formatFocusDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return if (hours > 0L) "${hours}h ${minutes}m" else "${minutes}m"
}
