# Product feature and verification matrix

This project aims for broad feature parity in user value and workflows with established digital-wellbeing tools, while retaining an original brand, original design and independently written implementation. It does not copy another app's source code or protected assets.

**Status definitions:** Planned = requirements identified; In progress = implementation exists but is incomplete; Implemented = code reviewed; Tested = automated checks and relevant Android-device verification passed; Preview only = browser prototype demonstrates intended interaction, not native Android functionality.

The web preview is a product-design review surface. It cannot validate Android system permissions, usage access, accessibility services, VPN behaviour, background execution or actual app blocking.

## Experience and navigation

| Screen | Intended experience | Status | Acceptance evidence |
|---|---|---|---|
| Welcome / onboarding | Animated, accessible introduction; explain value and privacy; setup choices | Preview only | Native onboarding tested on small/large screens; skip, back and resume work |
| Overview | Dashboard, focus CTA, daily summary, next tasks and progress | Preview only | Real data, loading/empty/error states and responsive UI tested |
| Focus room | Pomodoro, configurable durations, breaks, intention and session controls | In progress | Timer unit tests; lifecycle, process death, reboot and notification tests |
| App garden | App picker, selected apps, categories, schedules and exceptions | Planned | Permission flow and real block/unblock tests |
| Schedules | Recurring days, time windows, exceptions and conflict handling | Preview only | Time-zone, daylight-saving, reboot and overlapping schedule tests |
| Insights | Daily/weekly usage, focus history, trends and app breakdown | Preview only | Usage access, aggregation correctness and export tests |
| Planner | Tasks, priorities, routines, goals and reminders | Preview only | Persistence, recurrence, notifications and accessibility tests |
| Growth & rewards | Streaks, milestones and transparent progress | Preview only | Deterministic rules, time-zone edge cases and non-manipulative mechanics |
| Settings | Theme, motion, privacy, permissions, export, deletion and about | Preview only | Data export/deletion and permission-revocation tests |

## Blocking and digital wellbeing

| Capability | Status | Android constraints / acceptance |
|---|---|---|
| Select Instagram, Snapchat, YouTube and other apps | Preview only | Native picker must respect installed-app visibility rules and disclose selection |
| Block selected apps during focus | Planned | User-enabled Android mechanism; test foreground detection, bypass paths, revocation and OEM differences |
| Schedules and daily limits | Planned | Handle reboot, process death, clock changes, time zones and overlapping rules |
| Strict sessions | Planned | Describe what can and cannot be enforced; never claim impossible-to-bypass protection |
| Usage analytics | Planned | Usage access is user-granted in Settings; collect only what the feature needs |
| Website filtering | Planned | A local Android VPN service requires explicit system consent; explain conflicts and local-only processing |
| Reels / Shorts / feed controls | Planned | Blocking the host app is feasible; granular in-app feed blocking is not guaranteed across third-party apps |
| Focus reminders | Planned | Respect notification permission, exact-alarm restrictions and background-execution limits |

## Privacy, quality and release

| Requirement | Status | Acceptance evidence |
|---|---|---|
| Offline-first core | Planned | Core flows work in airplane mode |
| No ads, subscriptions or paywalls | Product commitment | Dependency and release review |
| No mandatory account | Product commitment | Fresh install and core flows work without sign-in |
| Local export and deletion | Planned | Export is readable; deletion removes app-owned records |
| Accessibility | Planned | TalkBack, scalable text, contrast, touch targets and reduced motion |
| Automated tests | In progress | Unit tests, lint, build and feature integration tests |
| GitHub Pages preview | In progress | Preview deployment succeeds from the public repository |
| Android APK release | In progress | Reproducible CI build; signing key never committed to public source |

## Preview limitations

The site in the site directory is a browser-based design prototype. Some numbers and charts are illustrative. App selection, schedule toggles and the timer demonstrate interaction only; they do not block Android apps or read device usage. Do not use preview-only data as evidence of actual digital-wellbeing outcomes.
