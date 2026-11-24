package org.openfocus.app.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openfocus.app.ui.screens.AppGardenScreen
import org.openfocus.app.ui.screens.FocusRoomScreen
import org.openfocus.app.ui.screens.FocusTogetherScreen
import org.openfocus.app.ui.screens.GrowthScreen
import org.openfocus.app.ui.screens.InsightsScreen
import org.openfocus.app.ui.screens.OnboardingScreen
import org.openfocus.app.ui.screens.OverviewScreen
import org.openfocus.app.ui.screens.PlannerScreen
import org.openfocus.app.ui.screens.PermissionSetupScreen
import org.openfocus.app.ui.screens.ScheduleScreen
import org.openfocus.app.ui.screens.SettingsScreen

private data class MainDestination(val route: String, val label: String, val icon: ImageVector)

private val mainDestinations = listOf(
    MainDestination("home", "Home", Icons.Outlined.Home),
    MainDestination("focus", "Focus", Icons.Outlined.Timer),
    MainDestination("apps", "Block", Icons.Outlined.Apps),
    MainDestination("insights", "Insights", Icons.Outlined.Insights),
    MainDestination("planner", "Plan", Icons.Outlined.CalendarMonth)
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
            currentRoute = "permissions"
        }
        return
    }

    if (currentRoute == "permissions") {
        PermissionSetupScreen {
            preferences.edit().putBoolean("permissions_setup_seen", true).apply()
            currentRoute = "home"
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(38.dp).clip(RoundedCornerShape(13.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("◉", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                "Open Focus",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.6).sp,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "MAKE ROOM FOR WHAT MATTERS",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 8.sp,
                                letterSpacing = 1.15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { currentRoute = "groups" }) {
                            Icon(Icons.Outlined.Groups, contentDescription = "Focus together", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { currentRoute = "settings" }) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp
            ) {
                mainDestinations.forEach { destination ->
                    val selected = currentRoute == destination.route ||
                        (destination.route == "apps" && currentRoute == "schedules") ||
                        (destination.route == "insights" && currentRoute == "growth")
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentRoute = destination.route },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                destination.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        }
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
            "groups" -> FocusTogetherScreen(Modifier.padding(innerPadding)) { focusIntention ->
                context.getSharedPreferences("focus_ui_settings", Context.MODE_PRIVATE)
                    .edit().putString("next_focus_intention", focusIntention).apply()
                currentRoute = "focus"
            }
            "settings" -> SettingsScreen(Modifier.padding(innerPadding))
            else -> OverviewScreen(Modifier.padding(innerPadding)) { currentRoute = it }
        }
    }
}
