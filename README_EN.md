# Nrfr

[简体中文](README.md) | [English](README_EN.md)

A Shizuku-powered, root-free SIM carrier configuration tool.

## v1.0.5: one APK on Android 12+

On Android 12 and later, install the main APK and keep [Shizuku](https://github.com/RikkaApps/Shizuku) installed, running, and authorized. Operations use self-targeted Instrumentation with `--no-restart` to retain the main app process.

| Android version | Execution path | Installation |
| --- | --- | --- |
| 12 and later | Self-targeted Instrumentation with `--no-restart` | Main APK and Shizuku |
| 10–11 | Instrumentation in the separate helper target | Matching main APK, helper, and Shizuku |
| 8–9 | Legacy Shizuku Binder fallback | Main APK and Shizuku; stock systems lack the override API, so support depends on the ROM |

The minimum installation version remains Android 8. **Installation compatibility does not establish carrier-override support.** On a Seeker running Android 16, changes, reset, and restoration of the original configuration were verified without the helper, with the main process retained throughout. Android 8–11 devices and clearing actual persistent overrides have not been verified in this run. See the [validation record](docs/SINGLE_APK_VALIDATION.md) and [changelog](CHANGELOG.md).

## Features

- Uses Shizuku system capabilities to override the SIM country code and carrier name.
- Displays current configuration and override state for SIM 1 and SIM 2, with separate save and reset operations.
- Follows the system language by default and supports 简体中文, 繁體中文, English, 日本語, 한국어, and Español.
- Languages can be changed under **Settings → Language**; names are displayed in their native form.

## Installation and upgrades

Download APKs from the [v1.0.5 release](https://github.com/baiyanwu/Nrfr/releases/tag/v1.0.5) and start Shizuku first.

**Android 12 and later:** install only `nrfr-v1.0.5.apk`.

```bash
adb install -r nrfr-v1.0.5.apk
```

An existing helper can remain installed; the main app does not depend on it on these systems. You can remove it after confirming that configuration changes and reset work normally.

**Android 10–11:** install both APKs from the same release, starting with the helper.

```bash
adb install -r nrfr-instrumentation-target-v1.0.5.apk
adb install -r nrfr-v1.0.5.apk
```

The helper package is `com.github.nrfr.instrumentationtarget`. It has no launcher icon and does not need to be opened. Its module and execution path remain available for older systems.

**Android 8–9:** the main APK retains the legacy fallback, but stock systems lack the override API. Installing the helper cannot supply a missing platform API.

Automatic releases continue to use the existing Debug signing build. In-place upgrades require matching signing certificates; local builds, historical releases, and device-validation packages can use different certificates. Check the package source and certificate if an update is rejected.

## Reporting issues

Include a screenshot or full text from the device's **About phone** or **Device information** page, explicitly stating the **Android version** (for example, Android 15 or Android 16). Manufacturer versions such as HyperOS, One UI, ColorOS, or OriginOS do not replace it. Include the Nrfr version, whether Shizuku is running, and whether a matching helper is installed on older systems.

## Building

Use JDK 17 or a compatible version and Android SDK 35. The repository includes the Gradle 8.9 Wrapper.

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :instrumentation-target:assembleDebug
```

Outputs are `app/build/outputs/apk/debug/app-debug.apk` and `instrumentation-target/build/outputs/apk/debug/instrumentation-target-debug.apk`.

For local Release builds, use `:app:assembleRelease :instrumentation-target:assembleRelease`. Both modules share the version in `gradle.properties` and support `signing.storeFile`, `signing.storePassword`, `signing.keyAlias`, and `signing.keyPassword` in `local.properties`. Keep signing files and passwords out of version control.

## Publishing

[Build Release](.github/workflows/build.yml) checks tag/version consistency, runs unit tests and Lint, builds APKs, and creates a GitHub Release from `CHANGELOG.md` when a `v*` tag is pushed. Each release still includes:

- `nrfr-<version>.apk`: the main app, the only APK needed on Android 12+.
- `nrfr-instrumentation-target-<version>.apk`: the helper for Android 10–11.
- `SHA256SUMS.txt`: SHA-256 checksums for both APKs.

The workflow also accepts a manual version and prerelease selection. Update the shared app/helper version and changelog before publishing.

## License and disclaimer

This repository is a fork of [Ackites/Nrfr](https://github.com/Ackites/Nrfr), focused on Android 16 support with the desktop client removed.

It is licensed under [Apache-2.0](LICENSE), preserves upstream attribution, and is not an official upstream release.

This tool is intended for learning and research. Carrier-configuration changes can affect networking, roaming, and carrier features; users assume responsibility for their use.
