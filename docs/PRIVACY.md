# Privacy

Open Focus is designed to be local-first.

## Current data handling

- Focus timer state is stored in the app's private preferences as a deadline, remaining duration and running/completed flags.
- Selected apps are stored as launcher package identifiers in the app's private preferences.
- Planner tasks are stored in app-private preferences as local JSON records.
- Recurring app-blocking schedules are stored locally as a name, selected weekdays, local start/end minutes and enabled state.
- When the user grants Android Usage Access and opens Insights, the app queries Android's aggregate foreground-use records for the previous 24 hours. The summary is calculated on-device and is not uploaded or written to a usage-history database.
- The app currently has no account requirement, analytics SDK, advertising SDK or remote sync service.
- The app-blocking AccessibilityService receives foreground package identifiers to enforce selected-app rules.
- If the user explicitly enables YouTube Study Mode and accepts its disclosure, the service also inspects visible YouTube video-title nodes from YouTube's accessibility window. It does not inspect video frames/audio, messages, keystrokes or unrelated app content. The title classifier runs on-device; no titles are uploaded.
- The strict YouTube classifier allows clearly educational, study-related and motivational titles, blocks known entertainment signals, and pauses unknown/unverifiable screens. This is heuristic and cannot guarantee perfect semantic classification.
- The AccessibilityService declaration enables window-content access and view-ID reporting because those are needed for YouTube title inspection. The feature remains opt-in and the user can disable the service in Android Settings.
- The first-run permission checklist verifies Accessibility, Usage Access and Notifications. Accessibility is required for app blocking; the other two are optional.
- Before users open Usage Access settings, Insights explains the app-usage data involved and requires an affirmative acknowledgement. Usage data is queried on demand and remains local.

## Product requirements

- Focus sessions, preferences, planner records and usage summaries should remain on the device by default.
- No advertising or behavioural analytics SDKs are permitted.
- An account must not be required for core focus functionality.
- Any future cloud synchronization must be opt-in, documented and limited to data needed for the feature.
- Users must be able to inspect, export and delete their locally stored data.
- Sensitive Android permissions must be requested in context with clear explanations.
- Avoid collecting more app usage or installed-app information than is needed for a feature.

This document describes current data handling and future requirements; it does not claim that every planned privacy feature is already implemented.
