package org.openfocus.app.ui.regain

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OnboardingAnswers(
    val goal: String,
    val dailyHours: Int,
    val distractions: Set<String>
)

private val HeadlineStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.Normal,
    fontSize = 40.sp,
    lineHeight = 46.sp,
    letterSpacing = (-0.8).sp,
    color = RegainPalette.TextPrimary
)

private val GoalOptions = listOf(
    "School exams", "JEE / NEET", "University", "Competitive exams", "Learning a skill"
)
private val DistractionOptions = listOf(
    "Instagram", "YouTube", "TikTok", "Snapchat", "Games", "Reddit"
)

/**
 * Minimal, Apple-style onboarding: one calm question per page, large serif
 * headlines, smooth slide transitions. Finish by showing PermissionsScreen.
 */
@Composable
fun OnboardingFlow(
    onFinished: (OnboardingAnswers) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(0) }
    var goal by remember { mutableStateOf<String?>(null) }
    var hours by remember { mutableStateOf(3) }
    var distractions by remember { mutableStateOf(setOf<String>()) }
    val lastStep = 3

    val canContinue = when (step) {
        1 -> goal != null
        3 -> distractions.isNotEmpty()
        else -> true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RegainPalette.Background)
            .padding(horizontal = 28.dp, vertical = 32.dp)
    ) {
        StepDots(current = step, total = lastStep + 1)
        Spacer(Modifier.height(36.dp))

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val forward = targetState > initialState
                val dir = if (forward) 1 else -1
                (slideInHorizontally(tween(420)) { it / 4 * dir } + fadeIn(tween(420))) togetherWith
                    (slideOutHorizontally(tween(420)) { -it / 4 * dir } + fadeOut(tween(250)))
            },
            label = "onboardingStep"
        ) { s ->
            when (s) {
                0 -> WelcomePage()
                1 -> GoalPage(selected = goal, onSelect = { goal = it })
                2 -> HoursPage(hours = hours, onChange = { hours = it })
                else -> DistractionPage(
                    selected = distractions,
                    onToggle = { d ->
                        distractions = if (d in distractions) distractions - d else distractions + d
                    }
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) {
                Text(
                    text = "Back",
                    color = RegainPalette.TextSecondary,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { step -= 1 }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (canContinue) RegainPalette.TextPrimary else RegainPalette.SurfaceHigh)
                    .clickable(enabled = canContinue) {
                        if (step < lastStep) {
                            step += 1
                        } else {
                            onFinished(OnboardingAnswers(goal ?: "", hours, distractions))
                        }
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        step == 0 -> "Begin"
                        step == lastStep -> "Get started"
                        else -> "Continue"
                    },
                    color = if (canContinue) RegainPalette.Background else RegainPalette.TextSecondary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text("Take your\nfocus back.", style = HeadlineStyle.copy(fontSize = 52.sp, lineHeight = 58.sp))
        Spacer(Modifier.height(20.dp))
        Text(
            "A quieter way to study. A few quick questions, then we'll set things up for you.",
            color = RegainPalette.TextSecondary,
            fontSize = 17.sp,
            lineHeight = 25.sp
        )
    }
}

@Composable
private fun GoalPage(selected: String?, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("What are you\nstudying for?", style = HeadlineStyle)
        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GoalOptions.forEach { option ->
                ChoiceCard(text = option, selected = option == selected) { onSelect(option) }
            }
        }
    }
}

@Composable
private fun HoursPage(hours: Int, onChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("How long do you\nwant to focus daily?", style = HeadlineStyle)
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperButton("\u2212") { onChange((hours - 1).coerceAtLeast(1)) }
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$hours",
                    style = HeadlineStyle.copy(fontSize = 96.sp, lineHeight = 100.sp, letterSpacing = (-2).sp)
                )
                Text(if (hours == 1) "hour a day" else "hours a day", color = RegainPalette.TextSecondary, fontSize = 16.sp)
            }
            StepperButton("+") { onChange((hours + 1).coerceAtMost(12)) }
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun DistractionPage(selected: Set<String>, onToggle: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("What pulls you\naway most?", style = HeadlineStyle)
        Spacer(Modifier.height(8.dp))
        Text("Pick all that apply. We'll block them while you focus.", color = RegainPalette.TextSecondary, fontSize = 15.sp)
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DistractionOptions.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { option ->
                        ChoiceCard(
                            text = option,
                            selected = option in selected,
                            modifier = Modifier.weight(1f)
                        ) { onToggle(option) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceCard(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val border by animateColorAsState(
        targetValue = if (selected) RegainPalette.Accent else RegainPalette.SurfaceHigh,
        animationSpec = tween(250),
        label = "cardBorder"
    )
    val fill by animateColorAsState(
        targetValue = if (selected) RegainPalette.AccentSoft else RegainPalette.Surface,
        animationSpec = tween(250),
        label = "cardFill"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(fill)
            .border(1.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, color = RegainPalette.TextPrimary, fontSize = 17.sp)
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) RegainPalette.Accent else RegainPalette.SurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Text("\u2713", color = RegainPalette.Background, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(RegainPalette.SurfaceHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = RegainPalette.TextPrimary, fontSize = 26.sp)
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { i ->
            val width by animateFloatAsState(
                targetValue = if (i == current) 28f else 8f,
                animationSpec = tween(300),
                label = "dotWidth"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (i <= current) RegainPalette.TextPrimary else RegainPalette.SurfaceHigh)
            )
        }
    }
}
