package org.openfocus.app.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import org.openfocus.app.blocking.ui.BlockedAppActivity

/**
 * This service only reads the foreground package identifier from window-state events.
 * It does not inspect screen text, UI nodes, keystrokes, messages or user content.
 * The user must enable it explicitly in Android Accessibility settings.
 */
class AppBlockingAccessibilityService : AccessibilityService() {
    private var lastBlockedPackage: String? = null
    private var lastBlockedAtMillis: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val foregroundPackage = event.packageName?.toString() ?: return
        if (foregroundPackage == packageName) return
        if (!AppBlockStore(this).isBlocked(foregroundPackage)) return

        val now = System.currentTimeMillis()
        if (foregroundPackage == lastBlockedPackage && now - lastBlockedAtMillis < REPEAT_GUARD_MILLIS) {
            return
        }

        lastBlockedPackage = foregroundPackage
        lastBlockedAtMillis = now

        // Return to the launcher before displaying our own full-screen explanation.
        performGlobalAction(GLOBAL_ACTION_HOME)
        val blockerIntent = Intent(this, BlockedAppActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockedAppActivity.EXTRA_BLOCKED_PACKAGE, foregroundPackage)
        }
        runCatching { startActivity(blockerIntent) }
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val REPEAT_GUARD_MILLIS = 2_000L
    }
}