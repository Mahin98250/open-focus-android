package org.openfocus.app.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openfocus.app.blocking.AppBlockStore
import java.util.Locale

private data class WelcomePage(
    val eyebrow: String,
    val title: String,
    val body: String,
    val symbol: String,
    val action: String
)

private data class ScannedApp(
    val packageName: String,
    val label: String,
    val suggested: Boolean
)

private val welcomePages = listOf(
    WelcomePage(
        "TAKE BACK YOUR ATTENTION",
        "Your time deserves more than endless scrolling.",
        "Create a calmer relationship with your phone. Make room for studying, sleep, hobbies and the people around you.",
        "◎",
        "Let's begin"
    ),
    WelcomePage(
        "LESS NOISE. MORE YOU.",
        "We help distractions take a back seat.",
        "Find the apps that pull you away, choose when they should pause, and build a routine that feels realistic.",
        "✧",
        "Make my plan"
    ),
    WelcomePage(
        "YOUR SPACE. YOUR RULES.",
        "A private focus companion.",
        "No advertisements, no subscriptions and no account required. Your focus choices stay on your device.",
        "◌",
        "Personalise my experience"
    )
)

private val suggestedPackages = setOf(
    "com.instagram.android",
    "com.snapchat.android",
    "com.google.android.youtube",
    "com.zhiliaoapp.musically",
    "com.facebook.katana",
    "com.twitter.android",
    "com.reddit.frontpage",
    "com.pinterest",
    "com.discord",
    "com.facebook.orca",
    "com.netflix.mediaclient",
    "com.amazon.avod.thirdpartyclient",
    "com.google.android.apps.youtube.music"
)

private val distractionWords = listOf(
    "instagram", "snapchat", "youtube", "tiktok", "facebook", "threads",
    "reddit", "pinterest", "discord", "netflix", "prime video", "hotstar",
    "shorts", "games", "gaming", "twitch", "spotify"
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appBlockStore = remember(context) { AppBlockStore(context) }
    val preferences = remember(context) { context.getSharedPreferences("app_onboarding", Context.MODE_PRIVATE) }

    var page by rememberSaveable { mutableIntStateOf(0) }
    var studyHours by rememberSaveable { mutableFloatStateOf(4f) }
    var focusMinutes by rememberSaveable { mutableIntStateOf(25) }
    var currentScreenTime by rememberSaveable { mutableIntStateOf(5) }
    var selectedApps by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    var focusPriorities by rememberSaveable { mutableStateOf(setOf("Study & exams")) }
    var scanComplete by rememberSaveable { mutableStateOf(false) }

    val scannedApps = remember(context) {
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        context.packageManager.queryIntentActivities(launchIntent, 0)
            .mapNotNull { result ->
                val packageName = result.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == context.packageName) return@mapNotNull null
                val label = result.loadLabel(context.packageManager).toString()
                val normalized = (label + " " + packageName).lowercase(Locale.ROOT)
                val suggested = packageName in suggestedPackages ||
                    distractionWords.any { word -> normalized.contains(word) }
                ScannedApp(packageName, label, suggested)
            }
            .distinctBy { it.packageName }
            .sortedWith(compareByDescending<ScannedApp> { it.suggested }.thenBy { it.label.lowercase(Locale.ROOT) })
    }

    val suggested = scannedApps.filter { it.suggested }
    val visibleApps = scannedApps.take(60)
    val progress = (page + 1).toFloat() / 8f
    val rotation by animateFloatAsState(
        targetValue = if (page % 2 == 0) -7f else 7f,
        animationSpec = spring(stiffness = 110f),
        label = "onboarding-illustration-rotation"
    )
    val pulse by animateFloatAsState(
        targetValue = if (page % 2 == 0) 1f else 1.045f,
        animationSpec = spring(stiffness = 90f),
        label = "onboarding-illustration-pulse"
    )

    fun finishSetup() {
        appBlockStore.replaceBlockedPackages(selectedApps)
        preferences.edit()
            .putBoolean("completed", true)
            .putFloat("daily_study_goal_hours", studyHours)
            .putInt("preferred_focus_minutes", focusMinutes)
            .putInt("reported_daily_screen_hours", currentScreenTime)
            .putStringSet("focus_priorities", focusPriorities)
            .putBoolean("distraction_scan_completed", scanComplete)
            .apply()
        onComplete()
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0A160E), MaterialTheme.colorScheme.background, Color(0xFF080D0A))))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(17.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("◉", color = MaterialTheme.colorScheme.primary, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        Text("Open Focus", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.8).sp, style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        if (page < 3) "WELCOME" else "YOUR FOCUS PLAN",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.2.sp
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("YOUR SETUP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${page + 1} OF 8", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(
                        Modifier.fillMaxWidth().height(6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Box(
                            Modifier.fillMaxWidth(progress).height(5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                    ),
                                    CircleShape
                                )
                        )
                    }
                }

                AnimatedContent(
                    targetState = page,
                    transitionSpec = { (fadeIn() togetherWith fadeOut()) },
                    label = "onboarding-step"
                ) { currentPage ->
                    when (currentPage) {
                        0, 1, 2 -> {
                            val intro = welcomePages[currentPage]
                            Column(
                                Modifier.fillMaxWidth().padding(top = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(15.dp)
                            ) {
                                Box(
                                    Modifier.fillMaxWidth().height(245.dp)
                                        .background(
                                            Brush.linearGradient(listOf(Color(0xFF1A512C), Color(0xFF102B1B), Color(0xFF0A160E))),
                                            androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        Modifier.size(220.dp).scale(pulse)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f),
                                                        MaterialTheme.colorScheme.background
                                                    )
                                                ),
                                                CircleShape
                                            )
                                    )
                                    Surface(
                                        Modifier.size(150.dp).rotate(rotation),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shadowElevation = 12.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(intro.symbol, fontSize = 82.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Text("✦", Modifier.align(Alignment.TopEnd).padding(end = 48.dp, top = 25.dp), color = MaterialTheme.colorScheme.tertiary, fontSize = 30.sp)
                                    Text("✧", Modifier.align(Alignment.BottomStart).padding(start = 48.dp, bottom = 25.dp), color = MaterialTheme.colorScheme.primary, fontSize = 34.sp)
                                }
                                Text(intro.eyebrow, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                                Text(intro.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text(intro.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    repeat(3) { index ->
                                        Box(
                                            Modifier.size(if (index == currentPage) 20.dp else 7.dp, 7.dp)
                                                .background(
                                                    if (index == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                        3 -> {
                            Column(Modifier.fillMaxWidth().padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("YOUR PHONE, UNDER YOUR CONTROL", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
                                Text("Let's find your distractions.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    "We scanned the apps that appear in your launcher. Review the suggestions and choose which apps you want paused during focus time. Choosing Instagram blocks the whole app, not only Reels; the same applies to YouTube and Shorts.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                    Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text("✦  DISTRACTION SCAN", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            if (scannedApps.isEmpty()) "No launchable apps were found on this device." else "${scannedApps.size} apps found · ${suggested.size} suggestions",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text("This scan reads app names and package identifiers only. It does not inspect messages, screen content or browsing activity.", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (suggested.isNotEmpty()) {
                                    Text("SUGGESTED DISTRACTIONS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { selectedApps = suggested.map { it.packageName }.toSet() }) {
                                            Text("Select suggestions")
                                        }
                                        TextButton(onClick = { selectedApps = emptySet() }) {
                                            Text("Clear selection")
                                        }
                                    }
                                }
                                visibleApps.forEach { app ->
                                    val checked = app.packageName in selectedApps
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            InstalledAppIcon(app.packageName, app.label)
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = { selected ->
                                                    selectedApps = if (selected) selectedApps + app.packageName else selectedApps - app.packageName
                                                }
                                            )
                                            Column(Modifier.weight(1f).padding(start = 5.dp)) {
                                                Text(app.label, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    if (app.suggested) "Suggested for your pause list" else "Installed app",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            if (app.suggested) {
                                                Text("✦", color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                                if (scannedApps.size > visibleApps.size) {
                                    Text("Showing the first ${visibleApps.size} apps. You can manage the complete app list later in Apps.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                                if (suggested.isEmpty()) {
                                    Text("No familiar social or entertainment apps were recognised. You can select apps later from Apps.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        4 -> {
                            Column(Modifier.fillMaxWidth().padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(19.dp)) {
                                Text("LET'S SET A REALISTIC GOAL", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
                                Text("How much would you like to study each day?", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                                Text("This is your personal target, not a pressure. You can change it whenever life changes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("YOUR DAILY STUDY GOAL", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall)
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(String.format(Locale.ROOT, "%.1f", studyHours), fontSize = 54.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            Spacer(Modifier.width(8.dp))
                                            Text("hours / day", modifier = Modifier.padding(bottom = 10.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                        Slider(
                                            value = studyHours,
                                            onValueChange = { studyHours = it },
                                            valueRange = 1f..12f,
                                            steps = 21
                                        )
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("1 hour", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                            Text("12 hours", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                }
                                Text("We'll use this goal to shape your daily progress, not to judge you.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        5 -> {
                            Column(Modifier.fillMaxWidth().padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
                                Text("YOUR CURRENT ROUTINE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
                                Text("Roughly how much time do you spend on your phone each day?", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                                Text("An estimate is perfectly fine. This helps us show a useful starting point.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                listOf(2, 4, 5, 7, 9).forEach { hours ->
                                    val label = when (hours) {
                                        2 -> "Under 3 hours"
                                        4 -> "3–4 hours"
                                        5 -> "4–6 hours"
                                        7 -> "6–8 hours"
                                        else -> "More than 8 hours"
                                    }
                                    FilterChip(
                                        selected = currentScreenTime == hours,
                                        onClick = { currentScreenTime = hours },
                                        label = { Text(label) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(Modifier.height(5.dp))
                                Text("What focus session feels comfortable?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(25, 45, 60).forEach { minutes ->
                                        FilterChip(
                                            selected = focusMinutes == minutes,
                                            onClick = { focusMinutes = minutes },
                                            label = { Text("$minutes min") }
                                        )
                                    }
                                }
                            }
                        }
                        6 -> {
                            val choices = listOf("Study & exams", "Less scrolling", "Better sleep", "Healthier routine", "More time for hobbies")
                            Column(Modifier.fillMaxWidth().padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("WHAT MATTERS MOST TO YOU?", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
                                Text("What would you most like to improve?", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                                Text("Choose as many as you like. We'll use these to shape your focus space.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                choices.forEach { choice ->
                                    FilterChip(
                                        selected = choice in focusPriorities,
                                        onClick = { focusPriorities = if (choice in focusPriorities) focusPriorities - choice else focusPriorities + choice },
                                        label = { Text(choice) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        else -> {
                            Column(
                                Modifier.fillMaxWidth().padding(top = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    Modifier.size(180.dp).background(
                                        Brush.radialGradient(
                                            listOf(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.background)
                                        ),
                                        CircleShape
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("◎", fontSize = 94.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("YOUR FOCUS PLAN IS READY", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
                                Text("A little more room for what matters.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                Text(
                                    "Your starting plan is ready. You can change every choice later.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                    Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        SummaryLine("Daily study goal", String.format(Locale.ROOT, "%.1f hours", studyHours))
                                        SummaryLine("Focus session", "$focusMinutes minutes")
                                        SummaryLine("Daily phone use (estimate)", "$currentScreenTime hours")
                                        SummaryLine("Apps on pause list", selectedApps.size.toString())
                                        SummaryLine("Main priorities", focusPriorities.size.toString() + " selected")
                                    }
                                }
                                Text("Your choices are stored on this device. App blocking needs your explicit permission; it is never switched on silently.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                                OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) { Text("Enable app blocking in Android Settings") }
                            }
                        }
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (page == 3) scanComplete = true
                        if (page == 7) finishSetup() else page += 1
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when (page) {
                            0 -> "Let's begin"
                            1 -> "Continue"
                            2 -> "Scan my apps"
                            3 -> "Continue with selected apps"
                            4 -> "Set my goals"
                            5 -> "Continue"
                            6 -> "Create my focus plan"
                            else -> "Enter my focus space"
                        },
                        Modifier.padding(vertical = 5.dp)
                    )
                }
                if (page in 3..6) {
                    TextButton(
                        onClick = {
                            if (page == 3) scanComplete = false
                            page += 1
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (page == 3) "I'll choose apps later" else "Skip this question") }
                }
                if (page > 0) {
                    TextButton(onClick = { page -= 1 }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
                }
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun InstalledAppIcon(packageName: String, label: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val icon = remember(context, packageName) {
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            val bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }.getOrNull()
    }
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = "$label app icon",
            modifier = Modifier.size(42.dp).padding(end = 8.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Text(label.take(1).uppercase(), modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
    }
}
