package org.openfocus.app.ui.regain

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DayStat(val label: String, val minutes: Int)
data class SubjectStat(val name: String, val minutes: Int)

/**
 * Stats screen: today's focus time, streak, a 7-day bar chart and a
 * per-subject breakdown. Pass in your own data (e.g. from your database).
 */
@Composable
fun FocusStatsScreen(
    todayMinutes: Int,
    streakDays: Int,
    week: List<DayStat>,
    subjects: List<SubjectStat>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RegainPalette.Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Your focus", color = RegainPalette.TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Today", formatMinutes(todayMinutes), Modifier.weight(1f))
            StatCard("Streak", "$streakDays day" + if (streakDays == 1) "" else "s", Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))
        Text("This week", color = RegainPalette.TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        WeekChart(week)

        Spacer(Modifier.height(28.dp))
        Text("By subject", color = RegainPalette.TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        val maxSubject = (subjects.maxOfOrNull { it.minutes } ?: 1).coerceAtLeast(1)
        subjects.forEach { s ->
            SubjectRow(s, s.minutes / maxSubject.toFloat())
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(RegainPalette.Surface)
            .padding(18.dp)
    ) {
        Text(title, color = RegainPalette.TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = RegainPalette.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WeekChart(week: List<DayStat>) {
    val maxMinutes = (week.maxOfOrNull { it.minutes } ?: 1).coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(RegainPalette.Surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        week.forEach { day ->
            var target by remember { mutableStateOf(0f) }
            LaunchedEffect(day.minutes) { target = day.minutes / maxMinutes.toFloat() }
            val fraction by animateFloatAsState(
                targetValue = target,
                animationSpec = tween(700, easing = FastOutSlowInEasing),
                label = "bar"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
                Box(modifier = Modifier.weight(1f).width(22.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .fillMaxHeight(fraction.coerceIn(0.03f, 1f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(RegainPalette.Accent)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(day.label, color = RegainPalette.TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SubjectRow(subject: SubjectStat, fraction: Float) {
    var target by remember { mutableStateOf(0f) }
    LaunchedEffect(fraction) { target = fraction }
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "subject"
    )
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(subject.name, color = RegainPalette.TextPrimary, fontSize = 15.sp)
            Text(formatMinutes(subject.minutes), color = RegainPalette.TextSecondary, fontSize = 14.sp)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(RegainPalette.SurfaceHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(RegainPalette.Accent)
            )
        }
    }
}

private fun formatMinutes(total: Int): String {
    val h = total / 60
    val m = total % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
