package org.openfocus.app.ui.regain

import android.Manifest
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.openfocus.app.guard.YouTubeGuardService

/** Checks whether each permission is currently granted. */
object PermissionChecker {

    fun hasUsageAccess(c: Context): Boolean {
        val ops = c.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        @Suppress("DEPRECATION")
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), c.packageName)
        } else {
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), c.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasAccessibility(c: Context): Boolean {
        val enabled = Settings.Secure.getString(
            c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val me = ComponentName(c, YouTubeGuardService::class.java)
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    fun hasOverlay(c: Context): Boolean = Settings.canDrawOverlays(c)

    fun hasNotifications(c: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    fun hasBatteryExemption(c: Context): Boolean =
        (c.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(c.packageName)
}

private enum class Perm(val title: String, val reason: String, val required: Boolean) {
    Usage("Usage access", "Lets Open Focus see which app is open so it can block distracting ones.", true),
    Accessibility("Accessibility", "Lets the YouTube guard tell study videos from entertainment. Everything stays on your phone.", true),
    Overlay("Display over other apps", "Shows the \"Back to focus\" screen on top of blocked apps.", true),
    Notifications("Notifications", "Reminds you when a session ends or your focus slips.", false),
    Battery("Run in background", "Keeps blocking reliable while your screen is off.", false)
}

/**
 * Permission checklist. Each row turns into a green tick the moment the permission
 * is granted, and the next missing one is highlighted.
 * [onContinue] is enabled once the three required permissions are granted.
 */
@Composable
fun PermissionsScreen(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var tick by remember { mutableStateOf(0) }

    // Re-check while the screen is visible so ticks appear when the user returns from Settings.
    LaunchedEffect(Unit) {
        while (true) {
            delay(600)
            tick++
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { tick++ }

    val granted: Map<Perm, Boolean> = remember(tick) {
        mapOf(
            Perm.Usage to PermissionChecker.hasUsageAccess(context),
            Perm.Accessibility to PermissionChecker.hasAccessibility(context),
            Perm.Overlay to PermissionChecker.hasOverlay(context),
            Perm.Notifications to PermissionChecker.hasNotifications(context),
            Perm.Battery to PermissionChecker.hasBatteryExemption(context)
        )
    }
    val next = Perm.values().firstOrNull { granted[it] != true }
    val doneCount = granted.values.count { it }
    val requiredDone = Perm.values().filter { it.required }.all { granted[it] == true }

    fun request(p: Perm) {
        fun open(intent: Intent) = context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        when (p) {
            Perm.Usage -> open(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            Perm.Accessibility -> open(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Perm.Overlay -> open(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            )
            Perm.Notifications ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            Perm.Battery -> open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RegainPalette.Background)
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text("Let's set you up", color = RegainPalette.TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "$doneCount of ${Perm.values().size} ready",
            color = RegainPalette.TextSecondary,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(16.dp))
        ProgressBar(doneCount / Perm.values().size.toFloat())
        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Perm.values().forEach { p ->
                PermissionRow(
                    perm = p,
                    granted = granted[p] == true,
                    isNext = p == next,
                    onAllow = { request(p) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(if (requiredDone) RegainPalette.Accent else RegainPalette.SurfaceHigh)
                .clickable(enabled = requiredDone, onClick = onContinue)
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (requiredDone) "Continue" else "Grant the required ones to continue",
                color = if (requiredDone) RegainPalette.Background else RegainPalette.TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PermissionRow(perm: Perm, granted: Boolean, isNext: Boolean, onAllow: () -> Unit) {
    val circleColor by animateColorAsState(
        targetValue = if (granted) RegainPalette.Success else RegainPalette.SurfaceHigh,
        label = "circle"
    )
    val pop by animateFloatAsState(
        targetValue = if (granted) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pop"
    )
    val borderColor = when {
        granted -> RegainPalette.Success
        isNext -> RegainPalette.Accent
        else -> RegainPalette.SurfaceHigh
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RegainPalette.Surface)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .scale(pop)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (granted) "\u2713" else "",
                color = RegainPalette.Background,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(perm.title, color = RegainPalette.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (!perm.required) {
                    Spacer(Modifier.width(8.dp))
                    Text("optional", color = RegainPalette.TextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(perm.reason, color = RegainPalette.TextSecondary, fontSize = 13.sp)
        }
        Spacer(Modifier.width(12.dp))
        when {
            granted -> Text("Granted", color = RegainPalette.Success, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            isNext -> Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(RegainPalette.Accent)
                    .clickable(onClick = onAllow)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Allow", color = RegainPalette.Background, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            else -> Text(
                "Next up",
                color = RegainPalette.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.clickable(onClick = onAllow)
            )
        }
    }
}

@Composable
private fun ProgressBar(fraction: Float) {
    val animated by animateFloatAsState(targetValue = fraction, label = "permProgress")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(RegainPalette.SurfaceHigh)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(50))
                .background(RegainPalette.Success)
        )
    }
}
