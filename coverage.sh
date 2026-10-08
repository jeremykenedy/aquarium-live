#!/usr/bin/env bash
# Runs the plain-JVM tests under JaCoCo and writes build/jacoco.xml for SonarCloud.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
JACOCO_VERSION=0.8.12
TOOLS="$HERE/build/jacoco"
CLASSES="$HERE/build/test-classes"
PKG=com/jeremykenedy/aquariumlive
MAVEN=https://repo1.maven.org/maven2/org/jacoco

mkdir -p "$TOOLS" "$CLASSES"
[[ -f "$TOOLS/agent.jar" ]] || curl -fsSL -o "$TOOLS/agent.jar" "$MAVEN/org.jacoco.agent/$JACOCO_VERSION/org.jacoco.agent-$JACOCO_VERSION-runtime.jar"
[[ -f "$TOOLS/cli.jar" ]] || curl -fsSL -o "$TOOLS/cli.jar" "$MAVEN/org.jacoco.cli/$JACOCO_VERSION/org.jacoco.cli-$JACOCO_VERSION-nodeps.jar"

javac -nowarn -d "$CLASSES" "$HERE/src/$PKG/Config.java" "$HERE/src/$PKG/Species.java" "$HERE/src/$PKG/Sim.java" "$HERE/src/$PKG/Tone.java" "$HERE/src/$PKG/ArtCache.java" "$HERE/src/$PKG/FramePacer.java" "$HERE/test/$PKG/SimTest.java"
java -javaagent:"$TOOLS/agent.jar=destfile=$HERE/build/jacoco.exec" -cp "$CLASSES" com.jeremykenedy.aquariumlive.SimTest
java -jar "$TOOLS/cli.jar" report "$HERE/build/jacoco.exec" \
  --classfiles "$CLASSES" --sourcefiles "$HERE/src" --xml "$HERE/build/jacoco.xml" --csv "$HERE/build/jacoco.csv" >/dev/null
echo "Coverage report: build/jacoco.xml"
