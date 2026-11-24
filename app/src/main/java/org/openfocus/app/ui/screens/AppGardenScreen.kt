package org.openfocus.app.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.openfocus.app.blocking.AppBlockStore
import org.openfocus.app.blocking.DailyAppLimitStore
import org.openfocus.app.blocking.AppBlockingAccessibilityService

private data class LaunchableApp(val packageName: String, val label: String)

@Composable
fun AppGardenScreen(modifier: Modifier = Modifier, onSchedules: () -> Unit) {
    val context = LocalContext.current
    val store = remember { AppBlockStore(context) }
    val dailyLimitStore = remember { DailyAppLimitStore(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var blockedPackages by remember { mutableStateOf(store.blockedPackages()) }
    var dailyLimits by remember { mutableStateOf(dailyLimitStore.limitsMinutes()) }
    var expandedLimitPackage by remember { mutableStateOf<String?>(null) }
    var usageAccessEnabled by remember { mutableStateOf(dailyLimitStore.hasUsageAccess()) }
    var serviceEnabled by remember { mutableStateOf(isBlockingServiceEnabled(context)) }
    var showAccessibilitySteps by remember { mutableStateOf(false) }
    var showUsageSteps by remember { mutableStateOf(false) }
    val disclosurePreferences = remember { context.getSharedPreferences("app_block_disclosure", Context.MODE_PRIVATE) }
    var disclosureAccepted by remember { mutableStateOf(disclosurePreferences.getBoolean("accessibility_disclosure_accepted", false)) }
    var searchQuery by remember { mutableStateOf("") }
    var showSelectedOnly by remember { mutableStateOf(false) }
    var blockerMode by remember { mutableStateOf("Apps") }

    val installedApps = remember {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        context.packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { resolve ->
                val packageName = resolve.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == context.packageName) return@mapNotNull null
                LaunchableApp(packageName, resolve.loadLabel(context.packageManager).toString())
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    val filteredApps = installedApps.filter { app ->
        val query = searchQuery.trim()
        val matchesQuery = query.isBlank() || app.label.contains(query, ignoreCase = true) || app.packageName.contains(query, ignoreCase = true)
        val matchesSelection = !showSelectedOnly || app.packageName in blockedPackages
        matchesQuery && matchesSelection
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceEnabled = isBlockingServiceEnabled(context)
                blockedPackages = store.blockedPackages()
                dailyLimits = dailyLimitStore.limitsMinutes()
                usageAccessEnabled = dailyLimitStore.hasUsageAccess()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("PROTECT YOUR ATTENTION", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
            Text("Choose apps to pause.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Choose which apps take a pause during your focus sessions or recurring schedules.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            listOf("Apps", "Reels", "YouTube", "Websites").forEach { tab ->
                FilterChip(
                    selected = blockerMode == tab,
                    onClick = { blockerMode = tab },
                    label = { Text(tab) }
                )
            }
        }
        if (blockerMode == "Apps") {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("PERMISSION SETUP", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.4.sp)
                Text("Let's make blocking work.", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Complete the steps below. Open Focus checks each permission again when you return from Android Settings.", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("1. App blocking access", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Text(if (serviceEnabled) "✓ VERIFIED" else "NOT ENABLED", color = if (serviceEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Text(if (serviceEnabled) "Android confirms that Open Focus's Accessibility service is enabled." else "This lets Open Focus detect when one of your selected apps comes to the foreground. It does not read screen contents.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { showAccessibilitySteps = !showAccessibilitySteps }) {
                            Text(if (showAccessibilitySteps) "Hide step-by-step guide" else "Show me each step")
                        }
                        if (showAccessibilitySteps) {
                            Text("STEP 1  Tap the button below to open Accessibility settings.", style = MaterialTheme.typography.bodySmall)
                            Text("STEP 2  Find Open Focus under Installed apps, Downloaded apps or Installed services. The label varies by Android version.", style = MaterialTheme.typography.bodySmall)
                            Text("STEP 3  Open Open Focus and switch on its Accessibility service.", style = MaterialTheme.typography.bodySmall)
                            Text("STEP 4  Read the Android disclosure and tap Allow. Return here; the status will be checked again.", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (serviceEnabled) "Review Accessibility settings" else "Open Accessibility settings") }
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("2. Usage Access", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Text(if (usageAccessEnabled) "✓ VERIFIED" else "NOT ENABLED", color = if (usageAccessEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                        Text(if (usageAccessEnabled) "Android confirms that Open Focus can read aggregate app-usage time for your daily limits." else "Needed only for daily app limits. It lets Android provide app-usage totals; it does not reveal messages or screen content.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { showUsageSteps = !showUsageSteps }) {
                            Text(if (showUsageSteps) "Hide step-by-step guide" else "Show me each step")
                        }
                        if (showUsageSteps) {
                            Text("STEP 1  Tap the button below to open Usage Access settings.", style = MaterialTheme.typography.bodySmall)
                            Text("STEP 2  Find Open Focus in the app list. You may need to scroll.", style = MaterialTheme.typography.bodySmall)
                            Text("STEP 3  Open it and enable Allow usage tracking / Permit usage access.", style = MaterialTheme.typography.bodySmall)
                            Text("STEP 4  Return here. Open Focus will verify the setting again.", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (usageAccessEnabled) "Review Usage Access" else "Open Usage Access settings") }
                    }
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        if (serviceEnabled && usageAccessEnabled) "All permission checks passed." else if (serviceEnabled) "App blocking is ready. Usage Access is still needed for daily limits." else "Finish the required step to activate app blocking.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        serviceEnabled = isBlockingServiceEnabled(context)
                        usageAccessEnabled = dailyLimitStore.hasUsageAccess()
                    }) { Text("Verify") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = disclosureAccepted,
                        onCheckedChange = {
                            disclosureAccepted = it
                            disclosurePreferences.edit().putBoolean("accessibility_disclosure_accepted", it).apply()
                        }
                    )
                    Text(
                        "I understand that the service checks the foreground app name to apply my block list. It does not read screen content.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("Selected apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(blockedPackages.size.toString() + " selected · stored only on this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onSchedules) { Text("Schedules") }
            }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search installed apps") },
            placeholder = { Text("Search by app name") }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${filteredApps.size} shown · ${blockedPackages.size} selected", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            FilterChip(
                selected = showSelectedOnly,
                onClick = { showSelectedOnly = !showSelectedOnly },
                label = { Text("Selected only") }
            )
        }
        if (installedApps.isEmpty()) {
            Text("No launchable apps were found. Try again after checking Android app visibility settings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (filteredApps.isEmpty()) {
            Text(
                if (showSelectedOnly && searchQuery.isBlank()) "No apps selected yet. Turn on an app's switch to add it here."
                else "No apps match this search. Try another name or turn off the Selected only filter.",
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InstalledAppIcon(app.packageName, app.label)
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(app.label, fontWeight = FontWeight.SemiBold)
                                Text(app.packageName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Switch(
                                    checked = app.packageName in blockedPackages,
                                    onCheckedChange = { selected ->
                                        store.setBlocked(app.packageName, selected)
                                        blockedPackages = store.blockedPackages()
                                    }
                                )
                                Box {
                                    TextButton(onClick = { expandedLimitPackage = app.packageName }) {
                                        val minutes = dailyLimits[app.packageName]
                                        Text(
                                            when {
                                                minutes == null -> "No limit"
                                                minutes < 60 -> "${minutes}m/day"
                                                minutes % 60 == 0 -> "${minutes / 60}h/day"
                                                else -> "${minutes / 60}h ${minutes % 60}m"
                                            },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedLimitPackage == app.packageName,
                                        onDismissRequest = { expandedLimitPackage = null }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("No daily limit") },
                                            onClick = {
                                                dailyLimitStore.setLimit(app.packageName, null)
                                                dailyLimits = dailyLimitStore.limitsMinutes()
                                                expandedLimitPackage = null
                                            }
                                        )
                                        listOf(15, 30, 60, 90, 120, 180, 240).forEach { minutes ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(if (minutes < 60) "${minutes} minutes / day" else "${minutes / 60}${if (minutes % 60 == 0) "" else ".5"} hours / day")
                                                },
                                                onClick = {
                                                    dailyLimitStore.setLimit(app.packageName, minutes)
                                                    dailyLimits = dailyLimitStore.limitsMinutes()
                                                    expandedLimitPackage = null
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Text(
            "This service reads only the foreground app package name. It does not read screen text, messages, keystrokes or page content. Blocking is not tamper-proof and may vary by device maker.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        } else {
            val title = when (blockerMode) {
                "Reels" -> "Reels & Shorts"
                "YouTube" -> "YouTube Study Mode"
                else -> "Website protection"
            }
            val description = when (blockerMode) {
                "Reels" -> "Regain-style short-feed controls need screen-level detection. Open Focus currently detects the foreground app only, so it can pause Instagram or YouTube as a whole, but cannot isolate Reels or Shorts."
                "YouTube" -> "A channel allow-list and Shorts restriction require reading the YouTube screen structure. That capability is not enabled in this build, so no channel-level filtering is active."
                else -> "Website filtering needs a dedicated local VPN-based filter and Android's VPN consent. That service is not included yet, so websites are not currently filtered."
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text(
                        when (blockerMode) {
                            "Reels" -> "SHORT-FORM CONTENT"
                            "YouTube" -> "STUDY-ONLY BROWSING"
                            else -> "BROWSER CONTROL"
                        },
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.3.sp
                    )
                    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("◷", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                            Column(Modifier.weight(1f).padding(start = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Not active in this build", fontWeight = FontWeight.SemiBold)
                                Text("No hidden permissions or fake blocking.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (blockerMode == "Reels") {
                        Text("AVAILABLE ALTERNATIVE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        Text("Add the full Instagram, YouTube, Snapchat or Facebook app to your pause list. This blocks that app while your focus timer or schedule is active.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { blockerMode = "Apps" }, modifier = Modifier.fillMaxWidth()) {
                        Text("Manage whole-app blocks")
                    }
                }
            }
        }
    }
}

private fun isBlockingServiceEnabled(context: Context): Boolean {
    val expected = ComponentName(context, AppBlockingAccessibilityService::class.java)
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabledServices.split(':')
        .mapNotNull { ComponentName.unflattenFromString(it) }
        .any { it == expected }
}
@Composable
private fun InstalledAppIcon(packageName: String, label: String) {
    val context = LocalContext.current
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
            modifier = Modifier.size(44.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Text(label.take(1).uppercase(), fontWeight = FontWeight.Bold)
    }
}
