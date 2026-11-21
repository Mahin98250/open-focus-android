# Open Focus for Android

An independent, open-source Android focus and digital-wellbeing application. The project is being developed as an offline-first, privacy-first alternative in the focus and distraction-control category.

> **Status:** Early development — Phase 0 (project foundation). This is not yet a feature-complete app. Blocking, usage analytics, synchronization and other advanced capabilities will be implemented and tested in later milestones.

## Principles

- Core functionality works without an account or network connection.
- No advertisements, tracking SDKs or mandatory subscriptions.
- Local-first data storage.
- Clear, user-controlled Android permissions.
- Public source code and reproducible CI builds.

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

## Project status

The initial milestone establishes the build, app shell, design foundation and CI. Planned modules include focus sessions, app blocking, usage insights, study planning, website filtering and optional community features. Android permission and platform restrictions will be documented honestly; no feature will be described as impossible to bypass.

## License

Apache License 2.0. See [LICENSE](LICENSE).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Please open an issue before substantial changes.
