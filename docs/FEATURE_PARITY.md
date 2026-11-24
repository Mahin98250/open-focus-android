# Product feature and verification matrix

This project aims for broad feature parity in user value and workflows with established digital-wellbeing tools, while retaining an original brand, original design and independently written implementation. It does not copy another app's source code or protected assets.

**Status definitions:** Planned = requirements identified; In progress = implementation exists but is incomplete or not yet verified; Implemented = code reviewed; Tested = automated checks and relevant Android-device verification passed; Preview only = browser prototype demonstrates intended interaction, not native Android functionality.

The web preview is a product-design review surface. It cannot validate Android system permissions, usage access, accessibility services, VPN behaviour, background execution or actual app blocking.

## Experience and navigation

| Screen | Intended experience | Status | Acceptance evidence |
|---|---|---|---|
| Welcome / onboarding | Animated multi-step introduction; daily study goal, estimated screen time, focus-session preference, personal priorities, installed-app distraction scan (background thread) and setup summary | In progress | Verify launcher app visibility, suggested-app matching, persisted selections, goal handoff, small/large screens, reduced motion and first-run/resume behaviour |
| Overview | Dashboard, focus CTA, personalised study goal, daily summary, next tasks and progress | In progress | Verify onboarding goal/priority handoff, real data, loading/empty/error states, responsive UI and accessibility tests |
| Focus room | Pomodoro, configurable durations, breaks, intention and session controls | In progress | Timer unit tests; lifecycle, process death, reboot and notification tests |
| App garden | Installed-app picker, selected packages, permission disclosure and block controls | In progress | App visibility, affirmative disclosure, service enable/disable, overlay and manufacturer tests |
| Schedules | Recurring weekdays, time windows and overnight support | In progress | Verify local-time changes, reboot, overlapping schedules, service revocation and OEM behaviour |
| Insights | Completed focus history plus optional last-24-hour Android app usage aggregates | In progress | Verify Usage Access consent, aggregation correctness, package-name visibility, local processing and export |
| Planner | Local tasks, priorities, routines, goals and reminders | In progress | Persistence and CRUD tests; recurrence, notifications and accessibility tests |
| Growth & rewards | Streaks, milestones and transparent progress | Planned | Deterministic rules, time-zone edge cases and non-manipulative mechanics |
| Settings | Theme, motion, privacy, permissions, export, deletion and about | In progress | Data export/deletion and permission-revocation tests |

## Blocking and digital wellbeing

| Capability | Status | Android constraints / acceptance |
|---|---|---|
| Select installed launcher apps | In progress | Current picker queries launcher activities; verify device visibility and special app cases |
| Block selected apps during a focus timer or active weekly schedule | In progress | AccessibilityService reads foreground package identifiers and presents an overlay when a focus session or recurring local schedule is active; test real devices, process death, OEM differences and user disablement |
| YouTube Study Mode | In progress | Opt-in on-device title classification allows clearly educational/motivational videos and blocks known entertainment or unknown screens; verify YouTube view IDs, feed/search results, video navigation and false positives across real devices |
| Recurring weekly schedules | In progress | Local weekday/time evaluator supports overnight windows; test reboot, local time changes, overlapping schedules and boundaries. Daily limits remain planned |
| Strict sessions | In progress | Strict mode disables in-app pause/reset until the timer finishes; it cannot prevent force-stop, app-data clearing, permission removal or device restart |
| Usage analytics | In progress | User-granted Usage Access returns local foreground-time aggregates for the last 24 hours; historical reports and limits remain planned |
| Website filtering | Planned | A local Android VPN service requires explicit system consent; explain conflicts and local-only processing |
| Reels / Shorts / feed controls | In progress | Quick controls now pause supported social apps (Instagram, TikTok, YouTube and Snapchat) during active focus sessions/schedules; granular in-app feed blocking is not guaranteed across third-party apps |
| Focus completion notification | In progress | Android 13+ notification permission requested in context; inexact alarm may be delayed; verify denied permission, Doze, reboot and OEM battery policies |

## Privacy, quality and release

| Requirement | Status | Acceptance evidence |
|---|---|---|
| Offline-first core | In progress | Core flows work in airplane mode; verify no hidden network dependency |
| No ads, subscriptions or paywalls | Product commitment | Dependency and release review |
| No mandatory account | Product commitment | Fresh install and core flows work without sign-in |
| Local export and deletion | Planned | Export is readable; deletion removes app-owned records |
| Accessibility | In progress | TalkBack, scalable text, contrast, touch targets and reduced motion |
| Automated tests | In progress | Unit tests, lint, build and feature integration tests |
| GitHub Pages preview | Blocked by repository Pages setup | Enable GitHub Pages with Source = GitHub Actions in repository Settings |
| Android APK release | In progress | Reproducible CI build; signing key never committed to public source |

## Preview limitations

The site directory contains a browser-based design prototype. Some numbers and charts are illustrative. App selection, schedule toggles and the timer demonstrate interaction only; they do not block Android apps or read device usage. Do not use preview-only data as evidence of actual digital-wellbeing outcomes.
