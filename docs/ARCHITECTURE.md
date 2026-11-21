# Architecture

## Product direction

This is a full-featured, independent Android digital-wellbeing app targeting broad feature parity with Regain under a different, original brand. It is not intended to remain a timer-only app.

Planned feature domains:

- Focus timer, Pomodoro cycles and break scheduling.
- App blocking, app limits, schedules and strict focus sessions.
- Usage tracking, screen-time insights and reports.
- Study planning, routines, goals and reminders.
- Website/content filtering, subject to Android platform capabilities.
- Gamification and optional accountability/community features.
- Privacy, data export/deletion and accessibility.

All core and advanced features are intended to remain free and open source. Core functionality must not require paid APIs, paid hosted infrastructure, ads, subscriptions or an account. Optional online capabilities must not compromise offline core functionality.

## Independence

Use original branding, interface assets and independently written code. Do not copy Regain's proprietary implementation, brand assets or protected content. Review dependency licenses and keep the source and build/release configuration public.

## Current milestone

The repository currently contains a native Android application built with Kotlin and Jetpack Compose, including a first 25-minute focus timer backed by locally persisted end-time state. This is only the foundation; the planned feature domains above are not yet implemented.

The project is intentionally starting with one Gradle application module. Feature modules will be extracted when their boundaries and dependencies are real rather than speculative.

## Target architecture

- **Presentation:** Jetpack Compose screens, lifecycle-aware state collection and ViewModels.
- **Domain:** framework-independent models, policies and use cases.
- **Data:** repository implementations, Room database and DataStore preferences.
- **Platform adapters:** Android usage access, accessibility integration, VPN service, notifications and background execution.
- **Optional sync/community:** isolated adapters. The core app must not require an account or network.

## Dependency direction

Presentation depends on domain contracts. Data and platform adapters implement domain contracts. Domain code must not depend on Compose or Android UI classes. Features communicate through explicit interfaces rather than reaching into each other's storage.

## Android service principles

- Request permissions only when a user enables the related feature.
- Explain the purpose and limitations before opening system settings.
- Handle permission revocation and service shutdown safely.
- Never claim app blocking is impossible to bypass.
- Keep a usable offline experience if optional services are unavailable.
- Respect Android background-execution, accessibility and VPN platform requirements; do not promise functionality that the OS cannot reliably support.

## Build

GitHub Actions uses JDK 17, Android SDK 35 and Gradle 8.9 to run unit tests, lint and assemble a debug APK.
