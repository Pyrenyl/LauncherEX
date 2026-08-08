# LauncherEX

Standalone, non-Quickstep Launcher3 fork for Android 17 / API 37.

## Features

- Launcher3 home screen, app drawer, folders, search, and widgets.
- Private Space support, including hiding its entry point while locked.
- Toggle Private Space from the dialer with `*#*#3825#*#*`.
- Lock Private Space from the lock-screen camera shortcut or the locked-state double-press power
  gesture by selecting LauncherEX as the secure-camera handler.
- Standalone Gradle build using only the public Android SDK and Maven dependencies.

```sh
./gradlew assembleRelease
```

The APK is written to `build/outputs/apk/release/LauncherEX-release.apk` and is signed with the
standard Android debug key. The build uses the public Android SDK and Maven dependencies only; it
does not require an Android Repo checkout, Soong outputs, `ANDROID_BUILD_TOP`, or platform keys.

Included: Launcher3 core, the non-Quickstep implementation, Widget Picker, and private space.
Quickstep, Recents, Taskbar, Go, TAPL, AppFunctions, AOSP test variants, and legacy Soong build
definitions have been removed.
