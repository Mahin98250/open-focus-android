# Android permissions and distribution notes

This app must explain every sensitive Android capability before asking the user to enable it. A system settings screen is not a substitute for an in-app explanation.

## App blocking: AccessibilityService

The explicitly user-enabled AccessibilityService detects foreground app changes and shows a touchable TYPE_ACCESSIBILITY_OVERLAY when a selected package is blocked by a running focus session, active schedule or reached daily limit.

When the user separately enables YouTube Study Mode and accepts its in-app disclosure, the same service also inspects visible YouTube accessibility-window titles. It classifies visible video titles locally:
- Clearly educational, study-related and motivational titles can continue.
- Known entertainment signals (for example, MrBeast, pranks, challenges, gaming, reactions and music videos) are blocked.
- Unknown titles and screens that cannot be verified are blocked in strict mode.
- Feed/search screens are allowed only when all detected video titles are classified as educational.
- A verified educational YouTube video can bypass a whole-app YouTube block; daily app limits still apply.

The service configuration therefore:
- Listens for window-state, window-content-change and view-scroll events.
- Sets canRetrieveWindowContent=true and requests view IDs / interactive windows, solely to inspect YouTube's visible title nodes when Study Mode is enabled.
- Does not read messages, keystrokes or unrelated app content.
- Does not prevent the user from disabling the service or uninstalling the app.
- Stores selected package identifiers and Study Mode preferences locally.
- Uses a heuristic title classifier, not video-frame/audio analysis or a guarantee of perfect semantic moderation. YouTube UI changes and device variants can cause false positives/negatives.

The first-run permission checklist verifies Accessibility, Usage Access and Notifications. Accessibility is required for app blocking; Usage Access and Notifications are optional. The in-app disclosure must accurately explain the additional YouTube title inspection and require affirmative acknowledgement before Study Mode can be enabled.

## Google Play distribution

Using AccessibilityService for a non-assistive core feature requires special policy care. The app must not claim isAccessibilityTool=true because its core purpose is not to provide disability assistance. Google Play's current guidance requires non-accessibility-tool uses to provide prominent in-app disclosure and affirmative consent, document the use in the Play listing and complete any applicable Play Console declaration. Google also directs apps to use narrower APIs where possible. A Play Store submission must be reviewed against the then-current policy; approval is not guaranteed.

For reference:
- Android AccessibilityService API: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- Google Play AccessibilityService policy: https://support.google.com/googleplay/android-developer/answer/10964491
- Google Play Developer Program Policy: https://support.google.com/googleplay/android-developer/answer/18265346

## Usage analytics: Usage Access

The Insights screen presents an in-app explanation and requires an affirmative acknowledgement before opening Android's Usage Access settings. After the user enables access, the app queries aggregated foreground-time usage for the preceding 24 hours when Insights opens or resumes. The calculation stays on-device; the app does not continuously poll usage events or upload them. Users can revoke Usage Access in Android Settings.

## Other future permissions

| Feature | Permission / system access | Consent and limits |
|---|---|---|
| Screen-time analytics | Usage access via Android Settings | Implemented as an opt-in last-24-hour aggregate; verify data correctness, package visibility and revocation on real devices |
| Website filtering | Android VpnService consent dialog | Run a local-only filter; disclose VPN conflict with other VPN apps; never route traffic to a paid remote provider |
| Notifications | POST_NOTIFICATIONS on supported Android versions | Request only when reminders or session completion notifications are enabled |
| Focus completion reminders | POST_NOTIFICATIONS and an inexact AlarmManager alarm | Notification permission is requested in context on Android 13+; delivery may be delayed by Doze/battery policy; exact-alarm access is not requested |
| Weekly app-blocking schedules | No exact alarm for foreground app checks | Local time rules are evaluated when a window-state event arrives; schedule start is not a guaranteed background wake-up |

Never request broad permissions such as QUERY_ALL_PACKAGES or SYSTEM_ALERT_WINDOW merely for convenience. Select the narrowest capability that meets the user-visible feature.
