#!/usr/bin/env sh
# Lightweight wrapper bootstrap. It uses an installed Gradle when available;
# otherwise it downloads the pinned Gradle distribution into GRADLE_USER_HOME.
set -eu
GRADLE_VERSION="8.14.7"
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_DIR="$GRADLE_USER_HOME/distributions/gradle-$GRADLE_VERSION"
GRADLE_BIN="$DIST_DIR/gradle-$GRADLE_VERSION/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$DIST_DIR"
  ZIP_FILE="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"
  echo "Gradle $GRADLE_VERSION is not installed; downloading the pinned distribution..."
  command -v curl >/dev/null 2>&1 || { echo "curl is required to bootstrap Gradle" >&2; exit 1; }
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required to bootstrap Gradle" >&2; exit 1; }
  curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP_FILE"
  unzip -q -o "$ZIP_FILE" -d "$DIST_DIR"
fi
exec "$GRADLE_BIN" "$@"
