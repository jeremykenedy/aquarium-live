#!/usr/bin/env bash
# Code style checks: Java warnings, whitespace and line endings, plain
# punctuation, no logging left in the app, and shellcheck on every script.
set -euo pipefail
HERE="$(cd "$(dirname "$0")/.." && pwd)"
cd "$HERE"
PKG=src/com/jeremykenedy/aquariumlive
failed=0

fail() {
  echo "$1"
  failed=1
}

# The classes with no Android code compile with every warning turned into an error.
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac --release 8 -Xlint:all,-options -Werror -d "$OUT" \
  "$PKG/Config.java" "$PKG/Species.java" "$PKG/Sim.java" "$PKG/Tone.java" \
  "$PKG/ArtCache.java" "$PKG/FramePacer.java" test/com/jeremykenedy/aquariumlive/SimTest.java \
  || fail "Java warnings in the plain-Java classes or tests"

files=()
while IFS= read -r file; do files+=("$file"); done < <(git ls-files --cached --others --exclude-standard | grep -vE '\.(jpg|png)$' | sort)

for file in "${files[@]}"; do
  [[ -f "$file" ]] || continue
  if grep -nP '\t' "$file" >/dev/null && [[ "$file" != *.yml ]]; then
    fail "$file: tab characters"
  fi
  if grep -nP ' +$' "$file" >/dev/null; then
    fail "$file: trailing spaces"
  fi
  if grep -lP '\r' "$file" >/dev/null; then
    fail "$file: Windows line endings"
  fi
  if [[ -s "$file" && -n "$(tail -c1 "$file")" ]]; then
    fail "$file: no newline at the end"
  fi
  if grep -nP '[\x{2014}\x{2013}\x{2018}\x{2019}\x{201C}\x{201D}]' "$file" >/dev/null; then
    fail "$file: em dashes, en dashes or curly quotes"
  fi
done

if grep -rnE 'android\.util\.Log|\bLog\.[dviwe]\(|System\.(out|err)\.' "$PKG"; then
  fail "logging left in the app"
fi

scripts=()
while IFS= read -r file; do scripts+=("$file"); done < <(find . -maxdepth 2 -name '*.sh' -not -path './build/*' | sort)
shellcheck "${scripts[@]}" || fail "shellcheck"

if [[ "$failed" != 0 ]]; then
  exit 1
fi
echo "Style checks passed: ${#files[@]} files, ${#scripts[@]} scripts"
