package org.openfocus.app.ui.screens

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.openfocus.app.blocking.AppBlockingAccessibilityService
import org.openfocus.app.blocking.DailyAppLimitStore

@Composable
fun PermissionSetupScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var accessibilityGranted by remember { mutableStateOf(isAccessibilityEnabled(context)) }
    var usageGranted by remember { mutableStateOf(DailyAppLimitStore(context).hasUsageAccess()) }
    var notificationsGranted by remember { mutableStateOf(hasNotificationPermission(context)) }
    val disclosurePreferences = remember {
        context.getSharedPreferences("app_block_disclosure", Context.MODE_PRIVATE)
    }
    var disclosureAccepted by remember {
        mutableStateOf(disclosurePreferences.getBoolean("accessibility_disclosure_accepted", false))
    }

    fun refresh() {
        accessibilityGranted = isAccessibilityEnabled(context)
        usageGranted = DailyAppLimitStore(context).hasUsageAccess()
        notificationsGranted = hasNotificationPermission(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notificationsGranted = granted }

    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0B1710), MaterialTheme.colorScheme.background)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("OPEN FOCUS", color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge, letterSpacing = 2.sp)
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Text("SETUP  ·  1/1", Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Let's connect your focus tools.", style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold)
            Text("Android keeps these permissions under your control. Open each setting, allow access, then return here. We'll verify it and add a tick automatically.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = disclosureAccepted,
                onCheckedChange = { accepted ->
                    disclosureAccepted = accepted
                    disclosurePreferences.edit().putBoolean("accessibility_disclosure_accepted", accepted).apply()
                }
            )
            Text("I understand that Open Focus uses Accessibility to detect selected apps and, only when enabled, inspect visible YouTube video titles. It does not read messages or keystrokes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }

        PermissionCard(
            number = "01",
            title = "App blocking access",
            required = true,
            enabled = disclosureAccepted,
            granted = accessibilityGranted,
            description = "Required. Lets Open Focus detect when a selected app opens and place the focus interruption screen over it.",
            buttonLabel = if (accessibilityGranted) "Review permission" else "Open Accessibility settings",
            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        )
        PermissionCard(
            number = "02",
            title = "Usage access",
            required = false,
            granted = usageGranted,
            description = "Optional. Enables daily app limits and more accurate usage summaries. Basic focus blocking does not depend on it.",
            buttonLabel = if (usageGranted) "Review permission" else "Open Usage Access settings",
            onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        )
        PermissionCard(
            number = "03",
            title = "Notifications",
            required = false,
            granted = notificationsGranted,
            description = "Optional. Shows focus-session completion reminders. You can still use the timer without this permission.",
            buttonLabel = if (notificationsGranted) "Permission given" else "Allow notifications",
            onClick = {
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    notificationsGranted = true
                }
            }
        )

        val completed = listOf(accessibilityGranted, usageGranted, notificationsGranted).count { it }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("$completed OF 3 PERMISSIONS VERIFIED", color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelSmall, letterSpacing = 1.3.sp, fontWeight = FontWeight.Bold)
                Text(if (accessibilityGranted) "App blocking is ready." else "App blocking is waiting for Accessibility access.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                Text("Return from Android Settings and this checklist updates automatically. You can also check it manually.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodySmall)
            }
        }

        OutlinedButton(onClick = { refresh() }, modifier = Modifier.fillMaxWidth()) {
            Text("Verify permissions again")
        }
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(if (accessibilityGranted) "Enter Open Focus" else "Continue and set this up later",
                Modifier.padding(vertical = 5.dp))
        }
        Text("You can return to this checklist from Block → Permission setup. Open Focus does not enable permissions without your approval.",
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PermissionCard(
    number: String,
    title: String,
    required: Boolean,
    granted: Boolean,
    enabled: Boolean = true,
    description: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(number, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Text(title, Modifier.weight(1f).padding(start = 11.dp),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(if (granted) "✓ GIVEN" else "REMAINING",
                    color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
            if (!granted || required) {
                OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text(buttonLabel) }
            }
        }
    }
}

private fun isAccessibilityEnabled(context: Context): Boolean {
    val expected = ComponentName(context, AppBlockingAccessibilityService::class.java)
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabled.split(':').mapNotNull { ComponentName.unflattenFromString(it) }.any { it == expected }
}

private fun hasNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < 33 ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
