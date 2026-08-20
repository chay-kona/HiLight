#!/usr/bin/env bash
set -euo pipefail
if command -v ./gradlew >/dev/null 2>&1; then
  ./gradlew :app:assembleDebug --stacktrace
elif command -v gradle >/dev/null 2>&1; then
  gradle :app:assembleDebug --stacktrace
else
  echo "Gradle is required. Use Android Studio or the included GitHub Actions workflow." >&2
  exit 1
fi
printf '\nAPK: app/build/outputs/apk/debug/app-debug.apk\n'
