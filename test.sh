#!/usr/bin/env bash
# Runs the plain-JVM tests for the simulation and settings parsing.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
PKG=com/jeremykenedy/aquariumlive
javac -nowarn -d "$OUT" "$HERE/src/$PKG/Config.java" "$HERE/src/$PKG/Species.java" "$HERE/src/$PKG/Sim.java" "$HERE/src/$PKG/Tone.java" "$HERE/test/$PKG/SimTest.java"
java -cp "$OUT" com.jeremykenedy.aquariumlive.SimTest
