#!/usr/bin/env bash
# Builds and signs the Aquarium Live APK without Gradle, using only the JDK
# and the Android SDK build tools. Output: build/aquarium-live.apk
# DEBUG=1 ./build.sh makes a debuggable build for on-device testing.
#
# The signing key lives outside the repo. Every release must be signed with
# the same key or the TV will refuse the update, so keep both files backed up:
#   ~/.android/aquarium-live.jks
#   ~/.android/aquarium-live.pass
set -euo pipefail

VERSION_CODE=4
VERSION_NAME=1.0.3
MIN_SDK=22
TARGET_SDK=30

HERE="$(cd "$(dirname "$0")" && pwd)"
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
BUILD_TOOLS="$(find "$SDK/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -1)"
ANDROID_JAR="$(find "$SDK/platforms" -mindepth 1 -maxdepth 1 -type d -name 'android-*' | sort -V | tail -1)/android.jar"
KEYSTORE="$HOME/.android/aquarium-live.jks"
KEYPASS="$HOME/.android/aquarium-live.pass"
OUT="$HERE/build"

if [[ ! -f "$KEYSTORE" ]]; then
  mkdir -p "$(dirname "$KEYSTORE")"
  (umask 077 && openssl rand -hex 24 > "$KEYPASS")
  keytool -genkeypair -keystore "$KEYSTORE" -storepass:file "$KEYPASS" -alias aquarium-live \
    -keyalg RSA -keysize 2048 -validity 10950 -dname "CN=Jeremy Kenedy"
  chmod 600 "$KEYSTORE"
  echo "Created a new signing key at $KEYSTORE. Back it up."
fi

rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex" "$OUT/gen"

"$BUILD_TOOLS/aapt2" compile --dir "$HERE/res" -o "$OUT/res.zip"
"$BUILD_TOOLS/aapt2" link -o "$OUT/unsigned.apk" -I "$ANDROID_JAR" \
  --manifest "$HERE/AndroidManifest.xml" --java "$OUT/gen" ${DEBUG:+--debug-mode} \
  --min-sdk-version "$MIN_SDK" --target-sdk-version "$TARGET_SDK" \
  --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" \
  "$OUT/res.zip"

find "$HERE/src" "$OUT/gen" -name '*.java' > "$OUT/sources.txt"
javac -nowarn -Xlint:-options --release 8 -classpath "$ANDROID_JAR" -d "$OUT/classes" @"$OUT/sources.txt"
find "$OUT/classes" -name '*.class' > "$OUT/classes.txt"
"$BUILD_TOOLS/d8" --release --lib "$ANDROID_JAR" --min-api "$MIN_SDK" --output "$OUT/dex" @"$OUT/classes.txt"

(cd "$OUT/dex" && zip -q -j "$OUT/unsigned.apk" classes.dex)
"$BUILD_TOOLS/zipalign" -f 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
"$BUILD_TOOLS/apksigner" sign --ks "$KEYSTORE" --ks-pass "file:$KEYPASS" --ks-key-alias aquarium-live \
  --out "$OUT/aquarium-live.apk" "$OUT/aligned.apk"
"$BUILD_TOOLS/apksigner" verify "$OUT/aquarium-live.apk"

echo "Built $OUT/aquarium-live.apk"
shasum -a 256 "$OUT/aquarium-live.apk"
