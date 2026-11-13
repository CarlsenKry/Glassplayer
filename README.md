# Krypton Player — Native Android

A native Android music player foundation built with **Kotlin, Jetpack Compose, Material 3 and Haze Glass**. It is not a PWA and does not use Flutter or React Native.

## Project identity

- App label: Krypton Player
- Application ID: `com.nexus7.kryptonplayer`
- Minimum Android: 8.0 / API 26
- Target Android: 16 / API 36
- Architecture: single activity, MVVM, Kotlin Flow/StateFlow, Hilt

## Implemented in this delivery

### Stage 1 — Project setup + Glass theme

- Native single-activity Android application.
- Kotlin + Jetpack Compose + Material 3.
- Haze 2.1 Glass surfaces with refraction-capable renderer, translucent tint and light edge treatment.
- Shared glass component used by the floating mini-player and bottom navigation.
- Dark palette with contrast-conscious text and a gradient fallback backdrop.
- English and French Android resource files are present as the localization base.

### Stage 2 — Device library scan + library screens

- Runtime permission request for `READ_MEDIA_AUDIO` on Android 13+ and `READ_EXTERNAL_STORAGE` on older versions.
- MediaStore scanning for title, artist, album, album id, duration, date added, track number, year, relative path, embedded-album-art URI and genre membership.
- Room cache with transactional refresh and favorite preservation across rescans.
- ContentObserver rescan when the audio collection changes, plus manual refresh.
- Songs, Albums, Artists, Genres, Folders and Playlists sections.
- Search across song, album and artist metadata.
- Title, artist, date-added and duration sorting; list/grid toggle; alphabet rail for song lists.

### Stage 3 — Playback service + mini-player + Now Playing

- Media3 ExoPlayer owned by an Android `MediaSessionService`, separate from the activity lifecycle.
- MediaSession for system media controls and Android media notification integration.
- Audio focus handling and pause on audio becoming noisy (for example, unplugging wired headphones).
- Controller connection through `MediaController`.
- Queue playback from library results; play/pause, previous, next, seek, shuffle and repeat modes.
- Glass mini-player dock and expanded Now Playing screen with artwork, progress and transport controls.

## Test the delivered stages

1. **Glass theme:** install and launch the app. Confirm the dark Compose surface, floating glass navigation and rounded glass mini-player container render. The glass renderer automatically uses its simplified fallback where advanced refraction is unsupported.
2. **Library scan:** grant audio permission, tap refresh, then check Songs, Albums, Artists, Genres and Folders. Try the search entry, sort menu, grid/list toggle, alphabet rail and a favorite toggle. Add or remove an audio file and wait for the MediaStore observer to trigger a refresh.
3. **Playback:** tap a song. Confirm the mini-player and Now Playing screen update, seek and transport controls work, then send the app to the background and verify playback and the system media notification continue. Test wired-headset unplug behavior and Bluetooth media buttons on a physical device.

The repository also includes a static integrity script (`python3 scripts/selfcheck.py`) and a GitHub Actions workflow. The self-check verifies project structure and selected architectural wiring; it is not a substitute for Gradle compilation, lint, instrumentation tests or device QA.

## Not implemented yet (not represented as complete)

The following stages are planned but are deliberately not faked in this foundation:

4. Full editable queue, drag reorder, Room-backed playlists, smart playlists and M3U import/export.
5. True overlapping-player crossfade engine, fade curves, smart/gapless album rules and interruption-safe transition state machine.
6. ReplayGain tag parsing, peak-aware gain and cached loudness analysis.
7. Custom lock-screen activity, full-screen artwork lock screen, notification seek/custom actions and wallpaper-aware palette.
8. Equalizer/audio effects, synchronized lyrics, tag editor, home widgets, Android Auto, Cast routing and backup/restore.

These are substantial audio/platform features. The crossfade engine in particular needs instrumentation on real devices; it must not be implemented as a cosmetic single-player volume animation and then called a true crossfade.

## Build locally

Requirements:

- JDK 17
- Android SDK Platform 37.0 (`compileSdk 37`; `targetSdk` stays 36)
- Android Build Tools 36.0.0 or newer
- Gradle 9.3.1 (required by Android Gradle Plugin 9.1) (the `gradlew` / `gradlew.bat` bootstrap scripts use an installed Gradle if available, otherwise download the pinned distribution; Windows requires PowerShell)

From the project root:

```bash
python3 scripts/selfcheck.py
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open the app, grant audio access, press refresh, tap a song and verify that playback continues when the activity is backgrounded. Verify the system media notification and lock-screen transport controls on a physical Android device.

## GitHub Actions

The workflow at `.github/workflows/android-ci.yml` runs on pushes to `main` / `develop`, pull requests and manual dispatch. It uses the current major releases of the GitHub Actions checkout, Java, Gradle, Android SDK setup and artifact upload actions. It runs `scripts/selfcheck.py` before toolchain provisioning, installs JDK 17, Gradle 9.3.1, Android Platform 37.0 and Build Tools 36.0.0 / 37.0.0, then runs unit tests, lint and the debug APK build. It uploads the APK on success and build reports/test results even when a later build step fails. Android SDK setup uses `android-actions/setup-android@v4`, which no longer asks for Google's removed legacy `tools` package.

## Notes

- This environment did not have an Android SDK or Gradle installation, so no local APK compilation is claimed. GitHub Actions is configured to perform the actual Gradle build. CI runs the Python self-check before Android SDK provisioning so structural failures remain visible even if SDK installation fails.
- The CI workflow invokes the pinned Gradle 9.3.1 installation directly. The included `gradlew` scripts are lightweight local bootstrap scripts, not the standard Gradle Wrapper JAR distribution.
- The repository is configured for `targetSdk 36`; confirm the final target policy and SDK packages again before Play Store publication.
- `applicationId` is a project identifier. Change it before first public release if a different package namespace is desired.
- Haze Glass is designed to fall back when the full rendering path is unavailable. Profile glass density and GPU memory on low/mid-range hardware; do not apply full glass blur to every item in a fast-scrolling list.
