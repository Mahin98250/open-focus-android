# Architecture

## Current milestone

The repository currently contains a small native Android application shell built with Kotlin and Jetpack Compose. The project is intentionally starting with one Gradle application module; feature modules will be extracted when their boundaries and dependencies are real rather than speculative.

## Target architecture

- **Presentation:** Jetpack Compose screens, lifecycle-aware state collection and ViewModels.
- **Domain:** framework-independent models, policies and use cases.
- **Data:** repository implementations, Room database and DataStore preferences.
- **Platform adapters:** Android usage access, accessibility integration, VPN service, notifications and background execution.
- **Optional sync:** isolated cloud adapter. The core app must not require an account or network.

## Dependency direction

Presentation depends on domain contracts. Data and platform adapters implement domain contracts. Domain code must not depend on Compose or Android UI classes. Features communicate through explicit interfaces rather than reaching into each other's storage.

## Android service principles

- Request permissions only when a user enables the related feature.
- Explain the purpose and limitations before opening system settings.
- Handle permission revocation and service shutdown safely.
- Never claim app blocking is impossible to bypass.
- Keep a usable offline experience if optional services are unavailable.

## Build

GitHub Actions uses JDK 17, Android SDK 35 and Gradle 8.9 to run unit tests, lint and assemble a debug APK.
