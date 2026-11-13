#!/usr/bin/env python3
"""Static project integrity checks for Krypton Player's Android source tree."""
from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "app"
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"

checks: list[tuple[str, bool, str]] = []

def check(name: str, condition: bool, detail: str = "") -> None:
    checks.append((name, condition, detail))

def read(path: Path) -> str:
    return path.read_text(encoding="utf-8") if path.exists() else ""

required_files = [
    "settings.gradle.kts",
    "build.gradle.kts",
    "gradle.properties",
    "gradle/wrapper/gradle-wrapper.properties",
    "gradlew",
    "app/build.gradle.kts",
    "app/src/main/AndroidManifest.xml",
    "app/src/main/java/com/nexus7/kryptonplayer/KryptonApplication.kt",
    "app/src/main/java/com/nexus7/kryptonplayer/MainActivity.kt",
    "app/src/main/java/com/nexus7/kryptonplayer/service/PlaybackService.kt",
    "app/src/main/java/com/nexus7/kryptonplayer/data/media/MediaStoreScanner.kt",
    "app/src/main/java/com/nexus7/kryptonplayer/data/local/MusicDatabase.kt",
    "app/src/main/java/com/nexus7/kryptonplayer/data/repository/MediaLibraryRepository.kt",
    "app/src/main/java/com/nexus7/kryptonplayer/ui/KryptonRoot.kt",
    "app/src/main/java/com/nexus7/kryptonplayer/ui/screens/LibraryScreen.kt",
    "app/src/main/java/com/nexus7/kryptonplayer/ui/screens/NowPlayingScreen.kt",
    "app/src/main/java/com/nexus7/kryptonplayer/ui/components/GlassSurface.kt",
    ".github/workflows/android-ci.yml",
    "scripts/selfcheck.py",
]
for rel in required_files:
    check(f"required file: {rel}", (ROOT / rel).is_file())

build = read(APP / "build.gradle.kts")
manifest_text = read(APP / "src/main/AndroidManifest.xml")
service = read(APP / "src/main/java/com/nexus7/kryptonplayer/service/PlaybackService.kt")
scanner = read(APP / "src/main/java/com/nexus7/kryptonplayer/data/media/MediaStoreScanner.kt")
room = read(APP / "src/main/java/com/nexus7/kryptonplayer/data/local/MusicDatabase.kt")
repo = read(APP / "src/main/java/com/nexus7/kryptonplayer/data/repository/MediaLibraryRepository.kt")
ui = read(APP / "src/main/java/com/nexus7/kryptonplayer/ui/components/GlassSurface.kt")
workflow = read(ROOT / ".github/workflows/android-ci.yml")

check("native Android application plugin", 'id("com.android.application")' in build)
check("Kotlin + Jetpack Compose compiler plugins", 'id("org.jetbrains.kotlin.android")' in build and 'id("org.jetbrains.kotlin.plugin.compose")' in build)
check("minSdk 26", "minSdk = 26" in build)
check("targetSdk 36", "targetSdk = 36" in build)
check("Media3 ExoPlayer dependency", "androidx.media3:media3-exoplayer:1.11.1" in build)
check("Media3 session dependency", "androidx.media3:media3-session:1.11.1" in build)
check("Room dependency and KSP compiler", "androidx.room:room-runtime:2.8.5" in build and "room-compiler:2.8.5" in build)
check("DataStore dependency", "androidx.datastore:datastore-preferences:1.2.1" in build)
check("Hilt dependency", "com.google.dagger:hilt-android:2.57.2" in build)
check("Coil 3 dependency", "io.coil-kt.coil3:coil-compose:3.6.3" in build)
check("Haze Glass dependency", "dev.chrisbanes.haze:haze-glass:2.1.0" in build)
check("no removed android.kotlinOptions DSL (Kotlin 2.4 removed it)", "kotlinOptions" not in build)
check("JVM target set through top-level kotlin.compilerOptions", "compilerOptions" in build and "jvmTarget.set(" in build)
check("kotlin-metadata-jvm aligned for Hilt", "kotlin-metadata-jvm" in build)
check("no Context.mainExecutor (API 28) with minSdk 26", ".mainExecutor" not in read(APP / "src/main/java/com/nexus7/kryptonplayer/ui/viewmodel/PlaybackViewModel.kt"))
check("audio permission in manifest", "android.permission.READ_MEDIA_AUDIO" in manifest_text)
check("legacy audio permission bounded to API 32", 'android:maxSdkVersion="32"' in manifest_text)
check("foreground media playback permission", "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" in manifest_text)
check("MediaSessionService declared", "androidx.media3.session.MediaSessionService" in manifest_text)
check("MediaSessionService implementation", "MediaSessionService" in service and "MediaSession.Builder" in service)
check("play request survives asynchronous controller connection", "pendingPlayback" in read(APP / "src/main/java/com/nexus7/kryptonplayer/ui/viewmodel/PlaybackViewModel.kt"))
check("ExoPlayer handles audio focus", ".setAudioAttributes(" in service and "true," in service)
check("headset unplug behavior", ".setHandleAudioBecomingNoisy(true)" in service)
check("MediaStore scanner reads title/artist/album", all(x in scanner for x in ["MediaStore.Audio.Media.TITLE", "MediaStore.Audio.Media.ARTIST", "MediaStore.Audio.Media.ALBUM"]))
check("MediaStore scanner reads year/track", "MediaStore.Audio.Media.YEAR" in scanner and "MediaStore.Audio.Media.TRACK" in scanner)
check("MediaStore scanner resolves genres", "MediaStore.Audio.Genres.Members" in scanner)
check("Room schema export enabled", "exportSchema = true" in room)
check("database refresh is transactional", "withTransaction" in repo)
check("media scans are serialized", "scanMutex.withLock" in repo)
check("database avoids destructive migration fallback", "fallbackToDestructiveMigration" not in read(APP / "src/main/java/com/nexus7/kryptonplayer/di/DatabaseModule.kt"))
check("automatic MediaStore change observer", "registerContentObserver" in repo)
check("glass uses Haze Glass renderer", ".hazeGlass(" in ui and "HazeInput.Sources" in ui)
check("GitHub Actions builds APK", "assembleDebug" in workflow and "upload-artifact" in workflow)
check("GitHub Actions runs self-check", "scripts/selfcheck.py" in workflow)
check("GitHub Actions uses current Android SDK setup action", "android-actions/setup-android@v4" in workflow)
check("GitHub Actions uses current checkout action", "actions/checkout@v7" in workflow)
check("GitHub Actions uses current Java setup action", "actions/setup-java@v6" in workflow)
check("GitHub Actions uses current Gradle setup action", "gradle/actions/setup-gradle@v6" in workflow)
check("GitHub Actions uses current artifact upload action", "actions/upload-artifact@v7" in workflow)
check("Android SDK setup explicitly installs platform-tools only", "packages: 'platform-tools'" in workflow)
check("workflow does not request removed legacy SDK tools package", "sdkmanager tools" not in workflow and "packages: 'tools" not in workflow)
check("source self-check runs before Android SDK provisioning", workflow.find("Run project self-check") < workflow.find("Set up Android SDK"))
check("Android 36 platform and build-tools are installed explicitly", 'sdkmanager --install "platforms;android-36" "build-tools;36.0.0"' in workflow)
check("CI installs the pinned Gradle version without a placeholder wrapper", "gradle-version: '8.14.7'" in workflow and "run: gradle --no-daemon --stacktrace clean testDebugUnitTest assembleDebug" in workflow)
check("CI exports build diagnostics even after a failure", "krypton-player-build-diagnostics" in workflow and "if: always()" in workflow)
check("Windows Gradle bootstrap matches pinned version", "GRADLE_VERSION=8.14.7" in read(ROOT / "gradlew.bat"))
check("Flutter sources not included", not any(ROOT.rglob("pubspec.yaml")))

manifest_ok = False
try:
    manifest = ET.parse(APP / "src/main/AndroidManifest.xml").getroot()
    app = manifest.find("application")
    service_node = None if app is None else app.find("service")
    manifest_ok = app is not None and service_node is not None and service_node.get(ANDROID_NS + "foregroundServiceType") == "mediaPlayback"
except (ET.ParseError, OSError):
    pass
check("Android manifest XML parses and media service type is valid", manifest_ok)

print("Krypton Player — native Android project self-check")
print("=" * 56)
for name, passed, detail in checks:
    print(f"{'PASS' if passed else 'FAIL'}  {name}" + (f" — {detail}" if detail else ""))
passed_count = sum(1 for _, passed, _ in checks if passed)
print("-" * 56)
print(f"Result: {passed_count}/{len(checks)} checks passed")
if passed_count != len(checks):
    sys.exit(1)
