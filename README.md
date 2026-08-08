# LauncherEX

Standalone, non-Quickstep Launcher3 fork for Android 17 / API 37.

```sh
./gradlew assembleRelease
```

The APK is written to `build/outputs/apk/release/LauncherEX-release.apk` and is signed with the
standard Android debug key. The build uses the public Android SDK and Maven dependencies only; it
does not require an Android Repo checkout, Soong outputs, `ANDROID_BUILD_TOP`, or platform keys.

Included: Launcher3 core, the non-Quickstep implementation, Widget Picker, and private space.
Quickstep, Recents, Taskbar, Go, TAPL, AppFunctions, AOSP test variants, and legacy Soong build
definitions have been removed.
