# Testing

- [Automated tests](#automated-tests)
- [Coverage](#coverage)
- [What the tests do not cover](#what-the-tests-do-not-cover)
- [Device testing](#device-testing)
- [Checking a build by hand](#checking-a-build-by-hand)

## Automated tests

```bash
./test.sh
```

The tests run on a plain JVM, with no Android device or emulator. They cover the parts of the app with no Android code: settings (`Config`), species (`Species`), the simulation (`Sim`) and the shading maths (`Tone`). The test file is `test/com/jeremykenedy/aquariumlive/SimTest.java`; it prints each failing check by name, then a count such as `276 passed, 0 failed`, and exits non-zero if any check fails.

| Area | What is checked |
|------|-----------------|
| Settings | Defaults; every stored value parses; bad values (unknown names, out-of-range numbers, NaN, wrong types) fall back to the defaults |
| Settings screen | Every list setting has a name for each choice, defaults to one of its own choices and never to Random; Which sea life defaults to every group. Reads the real `res/xml/settings.xml` and `res/values/arrays.xml` |
| Random and Surprise me | Random only picks from each setting's own choices and reaches every one of them; Random lighting never picks Follow the clock; Surprise me overrides the saved choices and leaves Brightness, Resolution, Frame rate and the clock alone; with Surprise me off, fixed settings stay fixed; a random sea-life mix is never empty; the same seed gives the same picks |
| Lighting | Follow the clock gives day, evening and night at the right hours |
| Tank contents | Fish counts and schools match the settings for every scene; each scene only has its own species; sea-life toggles and amounts are respected; no schools when Schools is None |
| Motion | Everything stays inside the tank over 20 simulated minutes for every scene at the busiest settings; fish turn around; schools stay together; whales come and go; animation phases stay bounded |
| Layout | Plant counts follow Plants and coral; front plants keep to the edges so they do not hide the fish |
| Bubbles | Bubbles pop at the surface; none when Bubbles is Off |
| Repeatability | The same seed builds the same tank |
| Shading | Cel bands and palette reduction for the stylised looks, including no bands and brightness above every band |
| Render size | Automatic uses the window; Retro draws 540 lines; fixed choices keep the screen's shape and are capped at its height; tall or unknown screens |
| Frame rate | Fixed 30 and 60; automatic stays at 60 when frames keep up and drops to a steady 30 when they do not; the warm-up and zero-length frames are ignored; restart |
| Art cache | Old art is deleted and current art kept; a missing folder or an undeletable file does not fail |
| Edge cases | Every scene has solo and schooling fish; no specks; zero and negative time steps; seahorses with no plants; two airstones on very wide screens; fish on top of each other or on a shark; a lifted octopus; an empty tank; airstone-only bubbles; whales at every amount |

## Coverage

```bash
./coverage.sh
```

Runs the same tests under JaCoCo and writes `build/jacoco.xml` (for SonarCloud) and `build/jacoco.csv`. The first run downloads the JaCoCo agent and CLI from Maven Central into `build/jacoco/`.

Every class that can run on a plain JVM is covered in full:

| Class | Lines | Branches |
|-------|-------|----------|
| `Config` | 100% | 100% |
| `Sim` | 100% | 100% |
| `Species` | 100% | 100% |
| `Tone` | 100% | 100% |
| `FramePacer` | 100% | 100% |
| `ArtCache` | 100% | 100% |

## What the tests do not cover

The other classes call Android: the painting (`FishArt`, `CreatureArt`, `PlantArt`, `ArtStyle`, `Textures`), the OpenGL renderer (`AquariumRenderer`, `Gl`) and the Android components (`AquariumView`, `AquariumDream`, `SettingsActivity`, `PreviewActivity`). They cannot run on a plain JVM, so they are listed in `sonar.coverage.exclusions` in `sonar-project.properties` and checked on devices instead. Logic that does not need Android is kept out of them so it can be tested: the render size lives in `Config`, the frame-rate switch in `FramePacer`, and the cache clean-up in `ArtCache`.

## Device testing

| Device | Android | Checked |
|--------|---------|---------|
| Fire TV Edition TV (AFTDEC012E) | 11 (API 30) | Settings, preview, every scene and look, the real screensaver started and woken from the remote, frame rate |
| Google TV emulator | 14 (API 34) | Install, launcher entry, settings with the remote, Surprise me and Random, preview, screensaver started on its own after the idle timeout |
| Android TV emulator | 12 (API 31) | Install, launcher entry, screensaver started on its own after the idle timeout |
| Android emulator | 5.1.1 (API 22) | Install, settings, preview at a fixed 1080p resolution, screensaver started |

Physical Android TV and Google TV devices have not been tested yet.

To make an emulator start the screensaver on its own when idle:

```bash
adb shell settings put secure screensaver_components com.jeremykenedy.aquariumlive/.AquariumDream
adb shell settings put secure screensaver_enabled 1
adb shell settings put secure screensaver_activate_on_sleep 1
adb shell settings put global stay_on_while_plugged_in 0
adb shell settings put system screen_off_timeout 15000
```

`stay_on_while_plugged_in` has to be 0: when it is on, the device never goes idle, so the screensaver never starts. To see which screensaver is running:

```bash
adb shell dumpsys dreams | grep -i dream
```

## Checking a build by hand

```bash
AAPT="$(ls -d "$ANDROID_HOME"/build-tools/* | sort -V | tail -1)/aapt"
"$AAPT" dump permissions build/aquarium-live.apk
"$AAPT" dump badging build/aquarium-live.apk | grep -c application-debuggable
```

The first command should list only the package name, with no `uses-permission` lines. The second should print `0` for a release build.
