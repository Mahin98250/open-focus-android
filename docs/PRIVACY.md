# Privacy

Open Focus is designed to be local-first.

## Current data handling

- Focus timer state is stored in the app's private preferences as a deadline, remaining duration and running/completed flags.
- Selected apps are stored as launcher package identifiers in the app's private preferences.
- Planner tasks are stored in app-private preferences as local JSON records.
- Recurring app-blocking schedules are stored locally as a name, selected weekdays, local start/end minutes and enabled state.
- When the user grants Android Usage Access and opens Insights, the app queries Android's aggregate foreground-use records for the previous 24 hours. The summary is calculated on-device and is not uploaded or written to a usage-history database.
- The app currently has no account requirement, analytics SDK, advertising SDK or remote sync service.
- The app-blocking AccessibilityService receives foreground package identifiers from Android window-state events. Its service declaration sets canRetrieveWindowContent=false; it does not request active-window content or inspect text, UI nodes, messages or keystrokes.
- Before users open Accessibility settings for the blocker, the app shows a separate disclosure and requires an affirmative acknowledgement.
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
