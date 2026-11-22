package org.openfocus.app.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openfocus.app.ui.screens.AppGardenScreen
import org.openfocus.app.ui.screens.FocusRoomScreen
import org.openfocus.app.ui.screens.GrowthScreen
import org.openfocus.app.ui.screens.InsightsScreen
import org.openfocus.app.ui.screens.OnboardingScreen
import org.openfocus.app.ui.screens.OverviewScreen
import org.openfocus.app.ui.screens.PlannerScreen
import org.openfocus.app.ui.screens.ScheduleScreen
import org.openfocus.app.ui.screens.SettingsScreen

private data class MainDestination(val route: String, val label: String, val glyph: String)
private val mainDestinations = listOf(
    MainDestination("home", "Home", "⌂"),
    MainDestination("focus", "Focus", "◷"),
    MainDestination("apps", "Apps", "◉"),
    MainDestination("planner", "Plan", "☷")
)

@Composable
fun FocusGardenApp() {
    val context = LocalContext.current
    val preferences = context.getSharedPreferences("app_onboarding", Context.MODE_PRIVATE)
    var onboarded by rememberSaveable { mutableStateOf(preferences.getBoolean("completed", false)) }
    var currentRoute by rememberSaveable { mutableStateOf("home") }

    if (!onboarded) {
        OnboardingScreen {
            preferences.edit().putBoolean("completed", true).apply()
            onboarded = true
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Open Focus",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.7).sp,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { currentRoute = "insights" }) { Text("Insights") }
                        TextButton(onClick = { currentRoute = "settings" }) { Text("Settings") }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                mainDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = { currentRoute = destination.route },
                        icon = { Text(destination.glyph, style = MaterialTheme.typography.titleLarge) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentRoute) {
            "home" -> OverviewScreen(Modifier.padding(innerPadding)) { currentRoute = it }
            "focus" -> FocusRoomScreen(Modifier.padding(innerPadding))
            "apps" -> AppGardenScreen(Modifier.padding(innerPadding)) { currentRoute = "schedules" }
            "planner" -> PlannerScreen(Modifier.padding(innerPadding))
            "schedules" -> ScheduleScreen(Modifier.padding(innerPadding)) { currentRoute = "apps" }
            "insights" -> InsightsScreen(Modifier.padding(innerPadding)) { currentRoute = "home" }
            "growth" -> GrowthScreen(Modifier.padding(innerPadding)) { currentRoute = "home" }
            "settings" -> SettingsScreen(Modifier.padding(innerPadding))
            else -> OverviewScreen(Modifier.padding(innerPadding)) { currentRoute = it }
        }
    }
}