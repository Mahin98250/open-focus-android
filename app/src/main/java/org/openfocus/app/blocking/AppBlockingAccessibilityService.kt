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
 * It observes foreground app changes. When the user explicitly enables YouTube
 * Study Mode, it inspects visible YouTube titles only; it never reads messages,
 * keystrokes or other apps' content. A touchable overlay is used instead of starting an Activity from
 * the background, which Android restricts on modern releases.
 */
class AppBlockingAccessibilityService : AccessibilityService() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var blockerOverlay: View? = null
    private var lastBlockedPackage: String? = null
    private var lastBlockedAtMillis: Long = 0L
    private var youtubeStudyOverlay = false

    // Reuse stores on accessibility callbacks and watchdog ticks.
    private val timerStore by lazy { FocusTimerStore(this) }
    private val appBlockStore by lazy { AppBlockStore(this) }
    private val scheduleStore by lazy { BlockScheduleStore(this) }
    private val dailyAppLimitStore by lazy { DailyAppLimitStore(this) }
    private val youtubeStudyModeStore by lazy { YoutubeStudyModeStore(this) }

    private val overlayWatchdog = object : Runnable {
        override fun run() {
            if (youtubeStudyOverlay) {
                if (youtubeStudyModeStore.isEnabled()) {
                    // The foreground-window event below clears this guard when the
                    // user leaves YouTube. Do not rely on rootInActiveWindow here:
                    // accessibility overlays can make that root temporarily null.
                    mainHandler.postDelayed(this, WATCHDOG_INTERVAL_MILLIS)
                } else {
                    youtubeStudyOverlay = false
                    removeBlockerOverlay()
                }
                return
            }
            val focusActive = timerStore.load().isRunning
            if (!focusActive && timerStore.takeCompletionNotification()) {
                FocusCompletionNotification.show(this@AppBlockingAccessibilityService)
            }
            val scheduleActive = scheduleStore.isBlockingNow()
            val lastPackage = lastBlockedPackage
            val selectedForBlocking = lastPackage?.let {
                appBlockStore.isBlocked(it)
            } ?: false
            val dailyLimitActive = lastPackage?.let {
                dailyAppLimitStore.isLimitReached(it)
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
        if (event == null) return
        val foregroundPackage = event.packageName?.toString() ?: return
        // Adding/removing TYPE_ACCESSIBILITY_OVERLAY can itself generate a window event
        // attributed to this app. Treating that event as an unblocked app used to
        // remove the blocker immediately, letting the selected game continue opening.
        // Ignore our own overlay/app windows; only evaluate other foreground apps.
        if (foregroundPackage == packageName) return

        if (youtubeStudyOverlay && foregroundPackage != YOUTUBE_PACKAGE) {
            youtubeStudyOverlay = false
            removeBlockerOverlay()
        }

        if (foregroundPackage == YOUTUBE_PACKAGE && youtubeStudyModeStore.isEnabled()) {
            val inspection = inspectYoutubeScreen()
            if (inspection.decision != YoutubeContentDecision.EDUCATIONAL) {
                youtubeStudyOverlay = true
                lastBlockedPackage = YOUTUBE_PACKAGE
                lastBlockedAtMillis = System.currentTimeMillis()
                showBlockerOverlay(YOUTUBE_PACKAGE, dailyLimitReached = false, studyMode = true,
                    detectedTitle = inspection.blockedTitle)
                return
            }
            if (blockerOverlay != null && lastBlockedPackage == YOUTUBE_PACKAGE && youtubeStudyOverlay) {
                youtubeStudyOverlay = false
                removeBlockerOverlay()
            }
        }

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val focusActive = timerStore.load().isRunning
        if (!focusActive && timerStore.takeCompletionNotification()) {
            FocusCompletionNotification.show(this)
        }
        val scheduleActive = scheduleStore.isBlockingNow()
        val selectedForBlocking = appBlockStore.isBlocked(foregroundPackage)
        val dailyLimitReached = dailyAppLimitStore.isLimitReached(foregroundPackage)
        val blockedBySession = (focusActive || scheduleActive) && selectedForBlocking
        if (!blockedBySession && !dailyLimitReached) {
            // Do not dismiss an already-visible guard merely because Android reports
            // a transient launcher/system window event. The watchdog checks whether
            // the original package is still restricted and removes it when the rule ends.
            if (blockerOverlay == null) return
            val guardedPackage = lastBlockedPackage ?: return
            val stillRestricted = (
                (focusActive || scheduleActive) && appBlockStore.isBlocked(guardedPackage)
            ) || dailyAppLimitStore.isLimitReached(guardedPackage)
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

    private fun showBlockerOverlay(blockedPackage: String, dailyLimitReached: Boolean, studyMode: Boolean = false, detectedTitle: String? = null) {
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
            text = if (studyMode) "Back to learning." else "A little pause."
            textSize = 28f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(10))
        }
        val message = TextView(this).apply {
            text = if (studyMode) {
                if (detectedTitle.isNullOrBlank()) {
                    "YouTube is in Study Mode. Only clearly educational or motivational videos are allowed. This screen could not be verified, so it is paused."
                } else {
                    "“" + detectedTitle.take(100) + "” does not match your study-only rules. Choose a lesson, tutorial or motivational video instead."
                }
            } else if (dailyLimitReached) {
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
        }.onFailure {
            if (studyMode) performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }


    private data class YoutubeScreenInspection(
        val decision: YoutubeContentDecision,
        val blockedTitle: String? = null
    )

    /**
     * For a video player, classify its visible title. For feed/search screens,
     * allow the screen only when every detected video title is clearly educational.
     * Unknown screens are paused in strict mode.
     */
    private fun inspectYoutubeScreen(): YoutubeScreenInspection {
        val root = runCatching { rootInActiveWindow }.getOrNull()
            ?: return YoutubeScreenInspection(YoutubeContentDecision.UNCERTAIN)
        val titles = mutableListOf<Pair<Int, String>>()
        var playerEvidence = false
        var visited = 0

        fun visit(node: android.view.accessibility.AccessibilityNodeInfo?) {
            if (node == null || visited >= MAX_ACCESSIBILITY_NODES) return
            visited += 1
            val viewId = runCatching { node.viewIdResourceName.orEmpty().lowercase() }.getOrDefault("")
            val className = node.className?.toString().orEmpty()
            val text = node.text?.toString()?.trim().orEmpty()
            val description = node.contentDescription?.toString()?.trim().orEmpty()
            val combined = "$text $description".lowercase()

            if (viewId.contains("player") || viewId.contains("watch") ||
                className.contains("PlayerView", ignoreCase = true) ||
                combined.contains("pause video") || combined.contains("play video")) {
                playerEvidence = true
            }
            if (text.length in 5..180 && viewId.contains("title")) {
                val priority = when {
                    viewId.contains("video_title") || viewId.contains("watch_title") -> 3
                    viewId.contains("player_title") -> 2
                    else -> 1
                }
                titles.add(priority to text)
            }
            for (index in 0 until node.childCount) visit(node.getChild(index))
        }

        visit(root)
        val distinctTitles = titles.distinctBy { it.second.lowercase() }
        if (distinctTitles.isEmpty()) return YoutubeScreenInspection(YoutubeContentDecision.UNCERTAIN)

        if (playerEvidence || distinctTitles.any { it.first >= 2 }) {
            val currentTitle = distinctTitles.sortedByDescending { it.first }.first().second
            return YoutubeScreenInspection(
                YoutubeStudyClassifier.classify(currentTitle),
                currentTitle.takeIf { YoutubeStudyClassifier.classify(currentTitle) != YoutubeContentDecision.EDUCATIONAL }
            )
        }

        val decisions = distinctTitles.map { YoutubeStudyClassifier.classify(it.second) }
        return when {
            decisions.all { it == YoutubeContentDecision.EDUCATIONAL } ->
                YoutubeScreenInspection(YoutubeContentDecision.EDUCATIONAL)
            decisions.any { it == YoutubeContentDecision.DISTRACTING } ->
                YoutubeScreenInspection(
                    YoutubeContentDecision.DISTRACTING,
                    distinctTitles.firstOrNull { YoutubeStudyClassifier.classify(it.second) == YoutubeContentDecision.DISTRACTING }?.second
                )
            else -> YoutubeScreenInspection(YoutubeContentDecision.UNCERTAIN, distinctTitles.first().second)
        }
    }

    private fun removeBlockerOverlay() {
        mainHandler.removeCallbacks(overlayWatchdog)
        youtubeStudyOverlay = false
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
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val MAX_ACCESSIBILITY_NODES = 350
        private const val REPEAT_GUARD_MILLIS = 2_000L
        private const val WATCHDOG_INTERVAL_MILLIS = 1_000L
    }
}