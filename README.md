# HiLight Control — Starter

A Pixel-first Android app scaffold for assigning a custom HiLight color/effect to launchable apps.

## Milestone 1 included

- Material 3 / Material You dynamic color UI
- Pixel-style expressive shapes and large color preview
- Installed launchable-app list (no QUERY_ALL_PACKAGES permission)
- Search + assigned-only filtering
- Per-app RGB/HSV color selection
- Effects: Solid, Pulse, Breathe, Wave, Comet, Rainbow
- Duration + enable/disable rule
- Local rule persistence
- NotificationListenerService that resolves the posting package to a saved rule
- Hardware boundary (`HiLightController`) ready for Shizuku-backed implementation

## Current limitation

The hardware controller is intentionally a logging stub in this starter milestone. It does not yet illuminate the Pixel 11 Pro HiLight LEDs.

The next milestone is to implement the Shizuku transport and Android lights-service session for supported Pixel 11 Pro / Pro XL / Pro Fold devices on Android 17.

## Toolchain

- compileSdk / targetSdk: 37 (Android 17)
- Android Gradle Plugin: 9.3.1
- Gradle required by AGP: 9.5.0
- Kotlin: 2.4.10
- Compose BOM: 2026.08.00
- Material 3 stable: 1.4.0 via BOM
- JDK: 17

## Open in Android Studio

1. Open this folder in a recent Android Studio version.
2. Configure Gradle 9.5.0 if Android Studio asks (this starter ZIP does not include the Gradle wrapper JAR).
3. Install Android SDK Platform 37.
4. Sync the project.
5. Run on the Pixel 11 Pro XL.
6. Open HiLight Control and enable Notification Access from the status card.
7. Pick an app, choose a color/effect, and save the rule.

## Architecture

`Installed apps -> AppRule -> NotificationListenerService -> HiLightController -> (next: Shizuku -> Android lights service -> Pixel HiLight)`

## Privacy direction

The app currently requests no internet permission. App rules are stored locally. Notification contents are not persisted.
