package org.openfocus.app.guard

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Watches the YouTube app. If the video on screen looks like entertainment
 * (or it is a Short), a full-screen "Back to focus" screen appears and YouTube
 * is sent to the background. Educational videos are left alone.
 *
 * Everything happens on the device; screen text is never stored or uploaded.
 */
class YouTubeGuardService : AccessibilityService() {

    private var lastCheck = 0L
    private var overlay: View? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName?.toString() != YOUTUBE_PACKAGE) return
        if (!GuardPrefs.isEnabled(this)) return
        if (overlay != null) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastCheck < CHECK_INTERVAL_MS) return
        lastCheck = now

        val root = rootInActiveWindow ?: return
        val texts = ArrayList<String>()
        val state = ScanState()
        collect(root, texts, 0, state)

        if (state.shorts && GuardPrefs.blockShorts(this)) {
            block("YouTube Shorts are blocked while you focus.")
            return
        }

        when (EducationalClassifier.classify(texts)) {
            Verdict.Distracting -> block("This doesn't look study-related.")
            Verdict.Unknown ->
                if (GuardPrefs.strictUnknown(this) && state.watchPage) {
                    block("Only study-related videos are allowed right now.")
                }
            Verdict.Educational -> Unit
        }
    }

    private class ScanState {
        var shorts = false
        var watchPage = false
        var visited = 0
    }

    private fun collect(node: AccessibilityNodeInfo?, out: MutableList<String>, depth: Int, state: ScanState) {
        if (node == null || depth > 30 || state.visited > 500) return
        state.visited++

        val id = node.viewIdResourceName?.lowercase().orEmpty()
        if (id.contains("reel") || id.contains("shorts")) state.shorts = true
        if (id.contains("watch") || id.contains("player")) state.watchPage = true

        node.text?.toString()?.let { if (it.length in 3..200) out.add(it) }
        node.contentDescription?.toString()?.let { if (it.length in 3..200) out.add(it) }

        for (i in 0 until node.childCount) {
            collect(node.getChild(i), out, depth + 1, state)
        }
    }

    private fun block(message: String) {
        showOverlay(message)
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    private fun showOverlay(message: String) {
        if (overlay != null) return
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val dp = resources.displayMetrics.density

        val title = TextView(this).apply {
            text = "Back to focus"
            setTextColor(Color.WHITE)
            textSize = 30f
            gravity = Gravity.CENTER
        }
        val body = TextView(this).apply {
            text = message
            setTextColor(0xFF8B93A3.toInt())
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding((32 * dp).toInt(), (12 * dp).toInt(), (32 * dp).toInt(), (28 * dp).toInt())
        }
        val button = Button(this).apply {
            text = "Okay"
            setOnClickListener { hideOverlay() }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xF20E1116.toInt())
            addView(title)
            addView(body)
            addView(button)
            isClickable = true
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        try {
            wm.addView(layout, params)
            overlay = layout
        } catch (_: RuntimeException) {
            overlay = null
        }
    }

    private fun hideOverlay() {
        val v = overlay ?: return
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(v)
        } catch (_: RuntimeException) {
        }
        overlay = null
    }

    override fun onInterrupt() = hideOverlay()

    override fun onDestroy() {
        hideOverlay()
        super.onDestroy()
    }

    companion object {
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val CHECK_INTERVAL_MS = 1500L
    }
}
