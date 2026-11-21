package org.openfocus.app.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.openfocus.app.blocking.AppBlockStore
import org.openfocus.app.blocking.AppBlockingAccessibilityService

private data class LaunchableApp(val packageName: String, val label: String)

@Composable
fun AppGardenScreen(modifier: Modifier = Modifier, onSchedules: () -> Unit) {
    val context = LocalContext.current
    val store = remember { AppBlockStore(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var blockedPackages by remember { mutableStateOf(store.blockedPackages()) }
    var serviceEnabled by remember { mutableStateOf(isBlockingServiceEnabled(context)) }
    val disclosurePreferences = remember { context.getSharedPreferences("app_block_disclosure", Context.MODE_PRIVATE) }
    var disclosureAccepted by remember { mutableStateOf(disclosurePreferences.getBoolean("accessibility_disclosure_accepted", false)) }

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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceEnabled = isBlockingServiceEnabled(context)
                blockedPackages = store.blockedPackages()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("PROTECT YOUR ATTENTION", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
            Text("Your app garden.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Choose which apps take a pause when you begin a focus session.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (serviceEnabled) "BLOCKING ACCESS ENABLED" else "ONE-TIME ANDROID SETUP", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall)
                Text(
                    if (serviceEnabled) "Your selected apps are redirected while a focus timer is running." else "Android requires you to explicitly enable the accessibility service before selected apps can be blocked.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = disclosureAccepted,
                        onCheckedChange = {
                            disclosureAccepted = it
                            disclosurePreferences.edit().putBoolean("accessibility_disclosure_accepted", it).apply()
                        }
                    )
                    Text(
                        "I understand this service reads the foreground app's package name to enforce my own block list. It does not read screen content.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    enabled = disclosureAccepted || serviceEnabled,
                    onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
                ) {
                    Text(if (serviceEnabled) "Review Android access" else "Open Accessibility settings")
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
            androidx.compose.material3.TextButton(onClick = onSchedules) { Text("Schedules") }
        }
        if (installedApps.isEmpty()) {
            Text("No launchable apps were found. Try again after checking Android app visibility settings.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(installedApps, key = { it.packageName }) { app ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Text(app.label.take(1).uppercase(), modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp), fontWeight = FontWeight.Bold)
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(app.label, fontWeight = FontWeight.SemiBold)
                                Text(app.packageName, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                            Switch(
                                checked = app.packageName in blockedPackages,
                                onCheckedChange = { selected ->
                                    store.setBlocked(app.packageName, selected)
                                    blockedPackages = store.blockedPackages()
                                }
                            )
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