# Open Focus for Android

An independent, fully open-source Android digital-wellbeing and focus app. The goal is to build a comprehensive alternative to Regain with a distinct original brand, original UI assets and an independently written codebase.

> **Status:** Early development. The first native multi-screen experience, persisted focus timer, local app selection/blocking foundation and local study-task planner are now in the repository. This is not yet a complete Regain alternative and has not yet passed real-device verification.

## Product goal

We are targeting broad feature parity in user value and workflows with the focus, distraction-control and digital-wellbeing capabilities people expect from Regain, while maintaining a different identity and an independent implementation. This is a full product, not just a Pomodoro timer.

### Full product scope

- Animated welcome and onboarding, dashboard and dedicated feature pages.
- Focus sessions, Pomodoro cycles, breaks and configurable timers.
- App blocking, schedules, app limits and user-configurable strict focus sessions.
- Screen-time and app-usage analytics, trends and reports.
- Study planner, routines, goals and reminders.
- Website and distracting-content blocking where Android's platform capabilities permit it.
- Gamification: streaks, progress, milestones and achievements.
- Optional community or accountability features that never become required for core use.
- Privacy controls, data export/deletion, accessibility and reliable onboarding.

See [the feature-by-feature verification matrix](docs/FEATURE_PARITY.md). A feature is not marked tested until relevant automated checks and real Android verification have passed.

## Current native implementation

- Three-step animated onboarding with an explicit skip path.
- Native Compose app shell with Overview, Focus, Apps, Planner, Insights, Schedules, Growth and Settings pages.
- Focus timer with 25, 45 and 60 minute presets, custom 1–180 minute sessions, start/pause/reset and a persisted deadline that resumes after process death.
- Completed focus-session history stored locally, with one-shot completion notifications through a battery-friendly inexact alarm. Android may defer delivery; no exact-alarm access is requested.
- Local installed-launcher-app picker and recurring weekly app-blocking schedules, including overnight windows. Selected apps are blocked during a running focus session or active schedule after the user enables the accessibility service.
- An Insights page with local focus-session history and optional Android Usage Access for app foreground-time summaries. Usage aggregates are calculated on-device and are not uploaded.
- Local installed-launcher-app picker. Selected package names are stored on-device.
- Optional app blocking: after a separate in-app disclosure and explicit user acknowledgement, the user can enable the accessibility service in Android Settings. It observes only foreground package identifiers and displays a touchable accessibility overlay for selected apps while a focus timer or saved recurring schedule is active.
- Local planner with task add, completion, removal and persistence.
- Timer deadline calculation unit tests.

**Important:** The blocking service is an early implementation and has not yet been verified across real devices and manufacturers. It is not tamper-proof. Users can disable the service, end the focus session or use device-specific bypass paths. Do not treat it as parental control or enterprise device management.

## Not implemented or not yet verified

- Reliable timer notification delivery under battery restrictions and timer/alarm restoration after device reboot. The current alarm is inexact and may be delayed.
- Daily app-use limits, schedule exceptions, advanced schedule conflicts and full historical usage trends.
- Screen-time reports beyond the available Android usage aggregates; usage data and schedule behaviour still need real-device verification.
- Website filtering / local VPN.
- Completed-session streaks, achievements and rewards logic.
- Community, accounts or cloud sync.
- Device-matrix tests for the app-blocking service and the full accessibility consent flow.

The web preview under the site directory demonstrates the intended product experience, but its illustrative charts and browser-only interactions are not Android functionality or real usage data.

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
- **Optional services only:** any future sync/community service must be optional, transparent and must not make core functionality dependent on it.
- **Honest platform limits:** explain Android permission requirements and limitations. Never claim blocking is impossible to bypass if it is not.

## Build an APK

Every push and pull request runs the Android CI workflow. Successful builds upload a debug APK as the open-focus-debug-apk workflow artifact. Public GitHub Releases will be enabled after a stable APK signing process is established.

1. Open the **Actions** tab.
2. Select **Android CI** and open a successful run.
3. Download the open-focus-debug-apk artifact.
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

MIT License. See LICENSE.

## Contributing

See CONTRIBUTING.md. Please open an issue before substantial changes.
