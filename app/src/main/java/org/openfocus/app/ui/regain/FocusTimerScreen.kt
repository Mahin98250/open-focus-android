package org.openfocus.app.ui.regain

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.openfocus.app.guard.BlockerState

enum class TimerMode(val label: String) {
    Focus("Focus"),
    Pomodoro("Pomodoro"),
    Stopwatch("Stopwatch")
}

enum class PomodoroPhase(val label: String, val minutes: Int) {
    Focus("Focus", 0), // uses the user-selected focus length
    ShortBreak("Short break", 5),
    LongBreak("Long break", 15)
}

/**
 * Clean full-screen focus timer: Focus, Pomodoro and Stopwatch modes,
 * animated progress ring, Strict Mode, sound + haptic feedback.
 * While a focus block is running it also switches the app blocker on.
 *
 * Drop it anywhere in your navigation: FocusTimerScreen()
 */
@Composable
fun FocusTimerScreen(
    modifier: Modifier = Modifier,
    onFocusSessionComplete: (focusSeconds: Int) -> Unit = {}
) {
    val context = LocalContext.current
    val sounds = remember { FocusSounds(context) }
    DisposableEffect(Unit) { onDispose { sounds.release() } }
    DisposableEffect(Unit) { onDispose { BlockerState.stopSession(context) } }

    var mode by remember { mutableStateOf(TimerMode.Focus) }
    var phase by remember { mutableStateOf(PomodoroPhase.Focus) }
    var focusMinutes by remember { mutableStateOf(25) }
    var running by remember { mutableStateOf(false) }
    var strict by remember { mutableStateOf(false) }
    var elapsed by remember { mutableStateOf(0) }
    var completedFocusBlocks by remember { mutableStateOf(0) }

    val totalSeconds = when (mode) {
        TimerMode.Focus -> focusMinutes * 60
        TimerMode.Pomodoro -> if (phase == PomodoroPhase.Focus) focusMinutes * 60 else phase.minutes * 60
        TimerMode.Stopwatch -> 0
    }

    // Strict Mode: block leaving the screen with the back button while running.
    BackHandler(enabled = strict && running) { }

    LaunchedEffect(running, mode, phase, totalSeconds) {
        while (running) {
            delay(1000)
            elapsed += 1
            if (mode != TimerMode.Stopwatch && elapsed >= totalSeconds) {
                running = false
                sounds.playComplete()
                val wasFocus = mode == TimerMode.Focus || phase == PomodoroPhase.Focus
                if (wasFocus) {
                    completedFocusBlocks += 1
                    onFocusSessionComplete(totalSeconds)
                }
                if (mode == TimerMode.Pomodoro) {
                    phase = when {
                        !wasFocus -> PomodoroPhase.Focus
                        completedFocusBlocks % 4 == 0 -> PomodoroPhase.LongBreak
                        else -> PomodoroPhase.ShortBreak
                    }
                }
                elapsed = 0
            }
        }
    }

    // App blocker: on only while a focus block is actually running (not on pause or breaks).
    val blockingNow = running && (mode != TimerMode.Pomodoro || phase == PomodoroPhase.Focus)
    LaunchedEffect(blockingNow, mode, phase, totalSeconds) {
        if (blockingNow) {
            val remaining = if (mode == TimerMode.Stopwatch) 4 * 3600 else (totalSeconds - elapsed).coerceAtLeast(0)
            BlockerState.startSession(context, remaining + 120)
        } else {
            BlockerState.stopSession(context)
        }
    }

    val progressTarget = when {
        mode == TimerMode.Stopwatch -> (elapsed % 3600) / 3600f
        totalSeconds > 0 -> elapsed / totalSeconds.toFloat()
        else -> 0f
    }
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "pulseScale"
    )

    val shownSeconds = if (mode == TimerMode.Stopwatch) elapsed else (totalSeconds - elapsed).coerceAtLeast(0)
    val subtitle = when {
        mode == TimerMode.Stopwatch -> "Counting up"
        mode == TimerMode.Pomodoro -> phase.label
        else -> "Stay focused"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RegainPalette.Background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimerMode.values().forEach { m ->
                Pill(
                    text = m.label,
                    selected = mode == m,
                    enabled = !running
                ) {
                    sounds.playTap()
                    mode = m
                    phase = PomodoroPhase.Focus
                    elapsed = 0
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(300.dp)
                .scale(if (running) pulseScale else 1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 14.dp.toPx()
                val inset = stroke / 2
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = RegainPalette.SurfaceHigh,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = if (phase == PomodoroPhase.Focus || mode != TimerMode.Pomodoro)
                        RegainPalette.Accent else RegainPalette.Success,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatTime(shownSeconds),
                    color = RegainPalette.TextPrimary,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Light
                )
                Spacer(Modifier.height(4.dp))
                Text(text = subtitle, color = RegainPalette.TextSecondary, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (mode != TimerMode.Stopwatch && !running) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton("\u2212") {
                    sounds.playTap()
                    focusMinutes = (focusMinutes - 5).coerceAtLeast(5)
                }
                Text(
                    text = "$focusMinutes min",
                    color = RegainPalette.TextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                RoundIconButton("+") {
                    sounds.playTap()
                    focusMinutes = (focusMinutes + 5).coerceAtMost(120)
                }
            }
        } else {
            Spacer(Modifier.height(44.dp))
        }

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(RegainPalette.Surface)
                .clickable(enabled = !running) {
                    sounds.playTap()
                    strict = !strict
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (strict) RegainPalette.Danger else RegainPalette.TextSecondary)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (strict) "Strict Mode on" else "Strict Mode off",
                color = RegainPalette.TextPrimary,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        val strictLocked = strict && running
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!strictLocked && (running || elapsed > 0)) {
                Pill(text = "Reset", selected = false, enabled = true) {
                    sounds.playTap()
                    running = false
                    elapsed = 0
                    phase = PomodoroPhase.Focus
                }
                Spacer(Modifier.width(16.dp))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (strictLocked) RegainPalette.SurfaceHigh else RegainPalette.Accent)
                    .clickable(enabled = !strictLocked) {
                        if (!running) sounds.playStart() else sounds.playTap()
                        running = !running
                    }
                    .padding(horizontal = 48.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        strictLocked -> "Locked in"
                        running -> "Pause"
                        elapsed > 0 -> "Resume"
                        else -> "Start"
                    },
                    color = if (strictLocked) RegainPalette.TextSecondary else RegainPalette.Background,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun Pill(text: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) RegainPalette.Accent else RegainPalette.SurfaceHigh)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (selected) RegainPalette.Background else RegainPalette.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RoundIconButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(RegainPalette.SurfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, color = RegainPalette.TextPrimary, fontSize = 20.sp)
    }
}

private fun formatTime(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
