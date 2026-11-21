# Architecture

## Product direction

This is a full-featured, independent Android digital-wellbeing app targeting broad feature parity in user value and workflows with Regain under a different, original brand. It is not intended to remain a timer-only app.

All core and advanced features are intended to remain free and open source. Core functionality must not require paid APIs, paid hosted infrastructure, ads, subscriptions or an account. Optional online capabilities must not compromise offline core functionality.

## Independence

Use original branding, interface assets and independently written code. Do not copy Regain's proprietary implementation, brand assets or protected content. Review dependency licenses and keep the source and build/release configuration public.

## Current Android structure

The project currently has one Gradle application module, with feature boundaries represented by packages:

- ui/ — app shell, shared theme and Compose screens.
- focus/ — persisted deadline-based focus timer state.
- blocking/ — local selected-package rules and the opt-in accessibility service.
- planner/ — locally persisted task model and store.
- res/xml/ — the minimum accessibility-service declaration.

The current app includes onboarding, Overview, Focus, Apps, Planner, Insights, Schedules, Growth and Settings pages. Some of these are intentionally honest empty/planned states rather than fake metrics.

The native app picker queries launcher activities through a narrow manifest queries declaration. It does not request the broad QUERY_ALL_PACKAGES permission.

## Current app-blocking path

1. User selects launcher apps in the App Garden.
2. Package names are stored locally in the app's private preferences.
3. The app displays a separate disclosure and requires affirmative acknowledgement before opening Android Accessibility settings.
4. The user explicitly enables the service in Android settings.
5. The service receives window-state events and checks only the foreground package identifier against the local block list.
6. A selected app is covered by a touchable accessibility overlay only while a focus timer is running.
7. The user can return to the launcher or end the focus session.

The service sets canRetrieveWindowContent=false. It does not request screen text, view hierarchy, gestures, keystrokes or message contents. It is an initial implementation and is not yet verified across real devices or Android manufacturers. Accessibility APIs are sensitive and distribution requirements must be reviewed; see ANDROID_PERMISSIONS.md.

## Target architecture

- **Presentation:** Jetpack Compose screens, lifecycle-aware state and eventually ViewModels.
- **Domain:** framework-independent models, policies and use cases.
- **Data:** repository implementations, Room database and DataStore preferences when the domain grows beyond small local records.
- **Platform adapters:** Android usage access, accessibility integration, VPN service, notifications and background execution.
- **Optional sync/community:** isolated adapters. The core app must not require an account or network.

## Dependency direction

Presentation depends on domain contracts. Data and platform adapters implement domain contracts. Domain code must not depend on Compose or Android UI classes. Features communicate through explicit interfaces rather than reaching into each other's storage. Extract further Gradle modules when module boundaries and dependency needs are justified.

## Android service principles

- Request permissions only when a user enables the related feature.
- Explain the purpose and limitations before opening system settings.
- Obtain affirmative consent for sensitive service access.
- Handle permission revocation and service shutdown safely.
- Never claim app blocking is impossible to bypass.
- Keep a usable offline experience if optional services are unavailable.
- Respect Android background-execution, accessibility and VPN platform requirements; do not promise functionality that the OS cannot reliably support.

## Build

GitHub Actions uses JDK 17, Android SDK 35 and Gradle 8.9 to run unit tests, lint and assemble a debug APK.
