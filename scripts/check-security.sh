#!/usr/bin/env bash
# Security checks: the manifest asks for nothing it does not need, and no
# secrets are in the files or anywhere in the git history.
set -euo pipefail
HERE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$HERE"
failed=0

fail() {
  echo "$1"
  failed=1
}

MANIFEST=AndroidManifest.xml
if grep -n '<uses-permission' "$MANIFEST"; then
  fail "$MANIFEST requests a permission. Aquarium Live must not request any."
fi
grep -q 'android:allowBackup="false"' "$MANIFEST" || fail "$MANIFEST must turn backup off"
grep -q 'android:permission="android.permission.BIND_DREAM_SERVICE"' "$MANIFEST" \
  || fail "$MANIFEST: the screensaver must only be startable by the system"
grep -rqE 'java\.net\.|HttpURLConnection|okhttp|WebView' src && fail "network code found in src"

GITLEAKS_VERSION=8.30.1
case "$(uname -s):$(uname -m)" in
  Darwin:arm64) archive="gitleaks_${GITLEAKS_VERSION}_darwin_arm64.tar.gz"; digest=b40ab0ae55c505963e365f271a8d3846efbc170aa17f2607f13df610a9aeb6a5 ;;
  Linux:x86_64) archive="gitleaks_${GITLEAKS_VERSION}_linux_x64.tar.gz"; digest=551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb ;;
  *) echo "The secret scan runs on macOS arm64 or Linux x86_64." >&2; exit 1 ;;
esac
TOOLS=build/tools/gitleaks
mkdir -p "$TOOLS"
if [[ ! -f "$TOOLS/$archive" ]]; then
  curl --proto '=https' --proto-redir '=https' -fsSL \
    "https://github.com/gitleaks/gitleaks/releases/download/v${GITLEAKS_VERSION}/$archive" -o "$TOOLS/$archive"
fi
actual="$(shasum -a 256 "$TOOLS/$archive" | cut -d' ' -f1)"
if [[ "$actual" != "$digest" ]]; then
  echo "gitleaks download does not match its published checksum" >&2
  exit 1
fi
tar -xzf "$TOOLS/$archive" -C "$TOOLS" gitleaks
"$TOOLS/gitleaks" git --redact --no-banner . || fail "secrets found in the git history"
"$TOOLS/gitleaks" dir --redact --no-banner . || fail "secrets found in the files"

if [[ "$failed" != 0 ]]; then
  exit 1
fi
echo "Security checks passed: manifest, no network code, no secrets in files or history"
