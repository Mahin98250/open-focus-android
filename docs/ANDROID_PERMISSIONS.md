# Android permissions and distribution notes

This app must explain every sensitive Android capability before asking the user to enable it. A system settings screen is not a substitute for an in-app explanation.

## App blocking: AccessibilityService

The first implementation uses an explicitly user-enabled AccessibilityService to detect the current foreground package identifier and show a touchable TYPE_ACCESSIBILITY_OVERLAY when that package is on the user's local block list during a focus session.

The service configuration intentionally:
- Listens only for window-state changes.
- Sets canRetrieveWindowContent=false.
- Does not inspect text, view hierarchies, messages, keyboard input or gestures.
- Does not prevent the user from disabling the service or uninstalling the app.
- Stops blocking when the focus timer is no longer running.
- Stores the selected package identifiers locally.

The in-app disclosure must explain this access and require affirmative user acknowledgement before launching Android Accessibility settings.

## Google Play distribution

Using AccessibilityService for a non-assistive core feature requires special policy care. The app must not claim isAccessibilityTool=true because its core purpose is not to provide disability assistance. Google Play's current guidance requires non-accessibility-tool uses to provide prominent in-app disclosure and affirmative consent, document the use in the Play listing and complete any applicable Play Console declaration. Google also directs apps to use narrower APIs where possible. A Play Store submission must be reviewed against the then-current policy; approval is not guaranteed.

For reference:
- Android AccessibilityService API: https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- Google Play AccessibilityService policy: https://support.google.com/googleplay/android-developer/answer/10964491
- Google Play Developer Program Policy: https://support.google.com/googleplay/android-developer/answer/18265346

## Other future permissions

| Feature | Permission / system access | Consent and limits |
|---|---|---|
| Screen-time analytics | Usage access via Android Settings | Explain that package usage timestamps/durations are read locally; provide an off switch and data deletion |
| Website filtering | Android VpnService consent dialog | Run a local-only filter; disclose VPN conflict with other VPN apps; never route traffic to a paid remote provider |
| Notifications | POST_NOTIFICATIONS on supported Android versions | Request only when reminders or session completion notifications are enabled |
| Exact scheduling | Exact alarm access where the OS requires it | Explain why exact timing is needed; gracefully support inexact scheduling when permission is unavailable |

Never request broad permissions such as QUERY_ALL_PACKAGES or SYSTEM_ALERT_WINDOW merely for convenience. Select the narrowest capability that meets the user-visible feature.
