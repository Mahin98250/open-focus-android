package org.openfocus.app.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.content.res.ColorStateList
import org.openfocus.app.focus.FocusTimerStore
import org.openfocus.app.focus.FocusCompletionNotification
import org.openfocus.app.blocking.BlockScheduleStore

/**
 * Optional, explicitly user-enabled app guard.
 *
 * It observes only the foreground package identifier in window-state events. It
 * never requests access to window contents, text, gestures, keystrokes or messages.
 * A touchable accessibility overlay is used instead of starting an Activity from
 * the background, which Android restricts on modern releases.
 */
class AppBlockingAccessibilityService : AccessibilityService() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var blockerOverlay: View? = null
    private var lastBlockedPackage: String? = null
    private var lastBlockedAtMillis: Long = 0L

    private val overlayWatchdog = object : Runnable {
        override fun run() {
            val timerStore = FocusTimerStore(this@AppBlockingAccessibilityService)
            val focusActive = timerStore.load().isRunning
            if (!focusActive && timerStore.takeCompletionNotification()) {
                FocusCompletionNotification.show(this@AppBlockingAccessibilityService)
            }
            val scheduleActive = BlockScheduleStore(this@AppBlockingAccessibilityService).isBlockingNow()
            val lastPackage = lastBlockedPackage
            val selectedForBlocking = lastPackage?.let {
                AppBlockStore(this@AppBlockingAccessibilityService).isBlocked(it)
            } ?: false
            val dailyLimitActive = lastPackage?.let {
                DailyAppLimitStore(this@AppBlockingAccessibilityService).isLimitReached(it)
            } ?: false
            val restrictionStillActive = ((focusActive || scheduleActive) && selectedForBlocking) || dailyLimitActive
            if (!restrictionStillActive) {
                removeBlockerOverlay()
            } else if (blockerOverlay != null) {
                // Keep the full-screen guard attached while the blocked app is underneath.
                // Some launchers/Android builds emit an intermediate window event for
                // the launcher or system UI; those events must not dismiss the guard.
                mainHandler.postDelayed(this, WATCHDOG_INTERVAL_MILLIS)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val foregroundPackage = event.packageName?.toString() ?: return
        // Adding/removing TYPE_ACCESSIBILITY_OVERLAY can itself generate a window event
        // attributed to this app. Treating that event as an unblocked app used to
        // remove the blocker immediately, letting the selected game continue opening.
        // Ignore our own overlay/app windows; only evaluate other foreground apps.
        if (foregroundPackage == packageName) return

        val timerStore = FocusTimerStore(this)
        val focusActive = timerStore.load().isRunning
        if (!focusActive && timerStore.takeCompletionNotification()) {
            FocusCompletionNotification.show(this)
        }
        val scheduleActive = BlockScheduleStore(this).isBlockingNow()
        val selectedForBlocking = AppBlockStore(this).isBlocked(foregroundPackage)
        val dailyLimitReached = DailyAppLimitStore(this).isLimitReached(foregroundPackage)
        val blockedBySession = (focusActive || scheduleActive) && selectedForBlocking
        if (!blockedBySession && !dailyLimitReached) {
            // Do not dismiss an already-visible guard merely because Android reports
            // a transient launcher/system window event. The watchdog checks whether
            // the original package is still restricted and removes it when the rule ends.
            if (blockerOverlay == null) return
            val guardedPackage = lastBlockedPackage ?: return
            val stillRestricted = (
                (focusActive || scheduleActive) && AppBlockStore(this).isBlocked(guardedPackage)
            ) || DailyAppLimitStore(this).isLimitReached(guardedPackage)
            if (!stillRestricted) removeBlockerOverlay()
            return
        }

        val now = System.currentTimeMillis()
        if (foregroundPackage == lastBlockedPackage &&
            now - lastBlockedAtMillis < REPEAT_GUARD_MILLIS &&
            blockerOverlay != null
        ) return

        lastBlockedPackage = foregroundPackage
        lastBlockedAtMillis = now
        showBlockerOverlay(foregroundPackage, dailyLimitReached)
    }

    private fun showBlockerOverlay(blockedPackage: String, dailyLimitReached: Boolean) {
        if (blockerOverlay != null) return

        val appLabel = runCatching {
            val appInfo = packageManager.getApplicationInfo(blockedPackage, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault("This app")

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(28), dp(28), dp(28))
            setBackgroundColor(Color.rgb(16, 23, 19))
        }

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(34), dp(24), dp(28))
            background = GradientDrawable().apply {
                setColor(Color.rgb(29, 43, 34))
                cornerRadius = dp(28).toFloat()
                setStroke(dp(1), Color.rgb(69, 99, 76))
            }
        }

        val wordmark = TextView(this).apply {
            text = "Open Focus"
            textSize = 27f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            letterSpacing = -0.025f
            setTextColor(Color.rgb(195, 235, 201))
            gravity = Gravity.CENTER
        }
        val heading = TextView(this).apply {
            text = "A little pause."
            textSize = 28f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(10))
        }
        val message = TextView(this).apply {
            text = if (dailyLimitReached) {
                "You have reached today's app limit for " + appLabel + ". Your limit resets at local midnight."
            } else {
                appLabel + " is on your pause list right now. Take a breath, then choose what you want to do next."
            }
            textSize = 15f
            setTextColor(Color.rgb(190, 207, 194))
            gravity = Gravity.CENTER
            setLineSpacing(dp(4).toFloat(), 1f)
        }
        val leaveButton = Button(this).apply {
            text = "Back to my day"
            setTextColor(Color.rgb(24, 48, 32))
            backgroundTintList = ColorStateList.valueOf(Color.rgb(183, 230, 196))
            setOnClickListener {
                removeBlockerOverlay()
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        }


        panel.addView(wordmark, matchWrap())
        panel.addView(heading, matchWrap())
        panel.addView(message, matchWrap())
        panel.addView(leaveButton, buttonLayoutParams())
        root.addView(panel, LinearLayout.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        ))

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
            PixelFormat.TRANSLUCENT
        )

        runCatching {
            (getSystemService(Context.WINDOW_SERVICE) as WindowManager).addView(root, params)
            blockerOverlay = root
            mainHandler.removeCallbacks(overlayWatchdog)
            mainHandler.postDelayed(overlayWatchdog, WATCHDOG_INTERVAL_MILLIS)
        }
    }

    private fun removeBlockerOverlay() {
        mainHandler.removeCallbacks(overlayWatchdog)
        val view = blockerOverlay ?: return
        blockerOverlay = null
        runCatching {
            (getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(view)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun matchWrap() = LinearLayout.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    )

    private fun buttonLayoutParams() = LinearLayout.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT
    ).apply {
        topMargin = dp(12)
    }

    override fun onInterrupt() {
        removeBlockerOverlay()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        removeBlockerOverlay()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        removeBlockerOverlay()
        super.onDestroy()
    }

    companion object {
        private const val REPEAT_GUARD_MILLIS = 2_000L
        private const val WATCHDOG_INTERVAL_MILLIS = 1_000L
    }
}