package org.openfocus.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(dateLabel.uppercase(Locale.getDefault()), color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("$greeting.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            }
            Surface(shape = CircleShape, color = Color(0xFF1C2B20)) {
                Text("◉", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.primary, fontSize = 22.sp)
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14271A))) {
            Box(Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF234C2A), Color(0xFF14271A), Color(0xFF101A14))))
                .padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Surface(shape = RoundedCornerShape(50), color = Color(0xFF365B31)) {
                            Text("✦  YOUR FOCUS SPACE", Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                color = Color(0xFFD8F6C2), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                        Text("One thing at a time.", color = Color.White, style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp)
                        Text("Give your best energy to what matters most.", color = Color(0xFFC6D8C8),
                            style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { onNavigate("focus") }) { Text("Start focusing  →") }
                    }
                    Box(Modifier.padding(start = 8.dp).size(112.dp)
                        .border(1.dp, Color(0xFFB8E89B).copy(alpha = .32f), CircleShape).padding(10.dp)
                        .border(1.dp, Color(0xFFB8E89B).copy(alpha = .24f), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("◷", color = Color(0xFFD9F6C2), fontSize = 27.sp)
                            Text("25:00", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("READY", color = Color(0xFFB9D0B9), fontSize = 8.sp, letterSpacing = 1.2.sp)
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("DAILY FOCUS GOAL", color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall, letterSpacing = 1.1.sp)
                        Text(formatFocusDuration(todayFocusMillis), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("YOUR TARGET", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        Text(if (dailyStudyGoal > 0f) dailyStudyGoal.toHoursLabel() else "Not set", fontWeight = FontWeight.SemiBold)
                    }
                }
                LinearProgressIndicator(progress = animatedGoalProgress, modifier = Modifier.fillMaxWidth().clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                Text(
                    when {
                        goalMillis <= 0L -> "Choose a daily target during setup or update it in settings."
                        todayFocusMillis >= goalMillis -> "You've reached today's target. Take a moment to enjoy it."
                        else -> "${formatFocusDuration(goalMillis - todayFocusMillis)} left today · ${todaySessions.size} completed sessions"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("YOUR DAY AT A GLANCE", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("On this device", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("APPS ON PAUSE", selectedAppCount.toString(), "Your block list", Modifier.weight(1f))
                MetricCard("TASKS DONE", "$completedTaskCount / ${tasks.size}", "Study planner", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("SESSIONS TODAY", todaySessions.size.toString(), "Completed sessions", Modifier.weight(1f))
                MetricCard("FOCUS TIME", formatFocusDuration(todayFocusMillis), "Today so far", Modifier.weight(1f))
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("QUICK ACCESS", color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedButton(onClick = { onNavigate("apps") }, modifier = Modifier.weight(1f)) { Text("App blocker") }
                OutlinedButton(onClick = { onNavigate("planner") }, modifier = Modifier.weight(1f)) { Text("Study planner") }
            }
            OutlinedButton(onClick = { onNavigate("groups") }, modifier = Modifier.fillMaxWidth()) {
                Text("Focus with friends  →")
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("KEEP YOUR MOMENTUM", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp)
                Text("Your progress, at a glance.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("See your weekly focus time and achievements, calculated from completed sessions stored on this phone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Row {
                    TextButton(onClick = { onNavigate("insights") }) { Text("View insights") }
                    TextButton(onClick = { onNavigate("growth") }) { Text("Achievements") }
                }
            }
        }
        Spacer(Modifier.width(1.dp))
    }
}

@Composable
private fun MetricCard(label: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)) {
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
