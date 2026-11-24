# YouTube guard + permissions: setup

The new code needs one manual step, because the existing `AndroidManifest.xml` was not edited.

## 1. Register the service in `AndroidManifest.xml`

Inside `<application>`:

```xml
<service
    android:name=".guard.YouTubeGuardService"
    android:exported="false"
    android:label="Open Focus YouTube guard"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/youtube_guard_config" />
</service>
```

At the top level (outside `<application>`), if not already present:

```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
    tools:ignore="ProtectedPermissions" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

(`tools:ignore` needs `xmlns:tools="http://schemas.android.com/tools"` on the `<manifest>` tag.)

## 2. Turn the guard on

```kotlin
GuardPrefs.setEnabled(context, true)
```

Optional: `GuardPrefs.setStrictUnknown(context, true)` also blocks videos the classifier can't place.

## 3. Show the permissions page

```kotlin
PermissionsScreen(onContinue = { /* go to the app */ })
```

## How the guard decides

- It reads the text on screen in the YouTube app and scores it against the keyword lists in `EducationalClassifier.kt`.
- Entertainment, or Shorts: a full-screen "Back to focus" screen appears and YouTube goes to the background.
- Study-related: the video keeps playing.
- It is keyword-based, so it will sometimes get it wrong. Edit the lists to tune it.
- YouTube can change its screen layout in an update, which may affect Shorts detection.

## Play Store note

Apps that use the Accessibility API must explain why in the Play Console, and the in-app description should be honest about what is read. A school or study-focus use case is fine, but expect a review.
