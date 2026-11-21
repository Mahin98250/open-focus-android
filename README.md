# Open Focus for Android

An independent, fully open-source Android digital-wellbeing and focus app. The goal is to build a comprehensive alternative to Regain with a distinct original brand, original UI assets and an independently written codebase.

> **Status:** Early development — foundation and first focus-timer slice only. This project is not yet feature-complete and must not be presented as a complete Regain replacement until the planned feature set is implemented and tested.

## Product goal

We are targeting broad feature parity with the focus, distraction-control and digital-wellbeing capabilities users expect from Regain, while maintaining a different identity and an independent implementation. This is a full product, not just a Pomodoro timer.

### Planned feature areas

- Focus sessions, Pomodoro cycles, breaks and configurable timers.
- App blocking, schedules, app limits and user-configurable strict focus sessions.
- Screen-time and app-usage analytics, trends and reports.
- Study planner, routines, goals and reminders.
- Website and distracting-content blocking where Android's platform capabilities permit it.
- Gamification: streaks, progress, milestones and achievements.
- Optional community or accountability features, designed so they do not become a requirement for core use.
- Privacy controls, data export and deletion, accessibility and reliable onboarding.

The exact feature-by-feature parity checklist will be maintained as the project develops. Each feature must be implemented, tested on real Android versions and documented before it is marked complete.

## Independence and licensing

- Use a distinct original name, visual identity, icons, illustrations and interface assets.
- Write our own implementation. Do not copy Regain's proprietary source code, branding, artwork or other protected assets.
- Keep the app's source code and build/release configuration public under the repository's open-source license.
- Review third-party dependencies and their licenses before including them.

## Non-negotiable principles

- **Free:** no paid tiers, forced subscriptions, ads or paywalled core/advanced features.
- **No paid infrastructure requirement:** core features must not depend on paid APIs, hosted services or subscriptions.
- **Offline-first:** focus, blocking, planning and local analytics should work without an account or internet connection wherever technically possible.
- **Private by default:** local data storage; no advertising or tracking SDKs.
- **Optional services only:** any future sync/community service must be optional, transparent and designed without making core functionality dependent on it.
- **Honest platform limits:** explain Android permission requirements and limitations. Never claim blocking is impossible to bypass if it is not.

## Current functionality

- 25-minute focus countdown.
- Start, pause and reset controls.
- Timer end-time is saved locally so the countdown can be reconstructed after the app process is closed and reopened.
- Dark Jetpack Compose dashboard foundation.

The current timer does not yet include background completion notifications, session history or configurable durations. App blocking, usage insights, study planning, website/content filtering, gamification and community features are planned, not implemented.

## Build an APK

Every push and pull request runs the Android CI workflow. Successful builds upload a debug APK as the `open-focus-debug-apk` workflow artifact. Public GitHub Releases will be enabled after a stable APK signing process is established.

1. Open the **Actions** tab.
2. Select **Android CI** and open a successful run.
3. Download the `open-focus-debug-apk` artifact.
4. Extract the ZIP and install the APK on a compatible Android device.

Debug APKs are for testing. A stable public release will use a separately managed signing process.

## Development

- Kotlin
- Android SDK
- Jetpack Compose + Material 3
- Gradle Kotlin DSL
- Minimum Android version: Android 8.0 (API 26), subject to feature-specific requirements

The project uses GitHub Actions hosted runners; contributors do not need to commit local build outputs.

## License

MIT License. See [LICENSE](LICENSE).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Please open an issue before substantial changes.
