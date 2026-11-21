# Contributing

Thanks for helping improve Open Focus for Android.

## Before starting

1. Search existing issues and pull requests.
2. Open an issue to discuss substantial features or architectural changes.
3. Keep changes focused and explain the user impact.

## Engineering expectations

- Use Kotlin and follow existing package conventions.
- Keep business rules out of Compose UI.
- Keep the core experience offline-first.
- Request Android permissions only when a user enables the related feature.
- Never add analytics, advertising SDKs or network dependencies without an explicit project decision.
- Add unit or UI tests for behaviour changes.
- Do not commit credentials, signing keys, local properties or generated build outputs.

## Pull requests

Include:
- What changed and why.
- How it was tested.
- Screenshots for visual changes.
- Any Android version or permission limitations.

All pull requests must pass the Android CI workflow.
