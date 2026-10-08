<p align="center">
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="art/banner-dark.svg">
        <source media="(prefers-color-scheme: light)" srcset="art/banner-light.svg">
        <img src="art/banner-light.svg" alt="aquarium-live" width="800">
    </picture>
</p>

<p align="center">A living aquarium screensaver for Fire TV. Every fish, plant and coral is drawn by the app in real time: no video, no downloads, no ads, and no tracking or analytics of any kind.</p>

<p align="center">
    <a href="https://github.com/jeremykenedy/aquarium-live/actions/workflows/tests.yml"><img src="https://github.com/jeremykenedy/aquarium-live/actions/workflows/tests.yml/badge.svg" alt="Tests"></a>
    <a href="https://dashboard.gitguardian.com/"><img src="https://github.com/jeremykenedy/aquarium-live/actions/workflows/gitguardian.yml/badge.svg" alt="GitGuardian scan"></a>
    <a href="https://sonarcloud.io/summary/new_code?id=jeremykenedy_aquarium-live"><img src="https://sonarcloud.io/api/project_badges/measure?project=jeremykenedy_aquarium-live&metric=alert_status" alt="Quality Gate Status"></a>
    <a href="https://sonarcloud.io/summary/new_code?id=jeremykenedy_aquarium-live"><img src="https://sonarcloud.io/api/project_badges/measure?project=jeremykenedy_aquarium-live&metric=coverage" alt="Coverage"></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT"></a>
</p>

<p align="center">
    <a href="https://github.com/jeremykenedy"><img src="https://img.shields.io/github/followers/jeremykenedy?label=Follow%20me&amp;style=social" alt="Follow me on GitHub"></a>
    <a href="https://github.com/jeremykenedy/aquarium-live" title="Open the repository and click Star"><img src="https://img.shields.io/badge/Star-this%20repo-yellow?logo=github&amp;style=social" alt="Star this repo"></a>
</p>

<p align="center">
    <img src="docs/screenshots/coral-reef-realistic.jpg" alt="Coral reef scene running as the Fire TV screensaver" width="800">
</p>

## Table of Contents

- [Features](#features)
- [Scenes](#scenes)
- [Looks](#looks)
- [Day and night](#day-and-night)
- [Settings](#settings)
- [Privacy](#privacy)
- [Requirements](#requirements)
- [Installation](#installation)
- [Setting it as the screensaver](#setting-it-as-the-screensaver)
- [Building from source](#building-from-source)
- [How it works](#how-it-works)
- [Testing](#testing)
- [Uninstalling](#uninstalling)
- [License](#license)

## Features

- Four scenes: open ocean, coral reef, kelp forest and a planted fish tank.
- Six looks, from realistic to cartoon, a glossy 3D-movie look, a hand-painted classic look and a retro desktop screensaver look.
- Sharks, whales, dolphins, manta rays, sea turtles, octopuses, jellyfish, seahorses, crabs and starfish, each group switchable on or off.
- Fish counts from a few to a ton, plus schools that swim together and scatter around sharks and dolphins.
- Sunlight shimmer: shafts of light from the surface and rippling light on the sand.
- Day, evening and night lighting, or lighting that follows the clock.
- An optional clock that drifts around the screen so it never sits in one spot.
- No permissions, no network access, no ads, no analytics.

## Scenes

| Scene | Fish | Sea life |
|-------|------|----------|
| Open ocean | Yellowfin tuna, barracuda, schools of sardines | Reef sharks, hammerheads, humpback whales, dolphins, manta rays, sea turtles, jellyfish, crabs, starfish |
| Coral reef | Clownfish, blue tangs, yellow tangs, royal grammas, Moorish idols, schools of chromis | Reef sharks, manta rays, sea turtles, octopuses, jellyfish, seahorses, crabs, starfish |
| Kelp forest | Garibaldi, kelp bass, schools of sardines | Leopard sharks, humpback whales, octopuses, jellyfish, seahorses, crabs, starfish |
| Fish tank | Angelfish, discus, bettas, dwarf gouramis, schools of neon tetras and rummy-nose tetras | None. It is a home aquarium. |

Whales don't stay: one swims through every so often, far in the background, then leaves.

<p align="center">
    <img src="docs/screenshots/open-ocean.jpg" alt="Open ocean scene" width="49%">
    <img src="docs/screenshots/kelp-forest.jpg" alt="Kelp forest scene" width="49%">
    <img src="docs/screenshots/fish-tank.jpg" alt="Fish tank scene" width="49%">
    <img src="docs/screenshots/coral-reef-realistic.jpg" alt="Coral reef scene" width="49%">
</p>

## Looks

| Look | What it does |
|------|--------------|
| Realistic | Soft shading, fine scales and fin rays, slightly muted colour. The default. |
| Animated | Brighter colour, big expressive eyes, no fine detail. |
| Cartoon | Bold ink outlines, flat bands of colour, big friendly eyes. |
| 3D movie | Glossy highlights, rim light, large glossy eyes, saturated colour. |
| Hand-painted classic | Thin painted outlines, soft cel shading, a teal painted sea. |
| Retro screensaver | Chunky low-resolution pixels, a limited palette and a deep blue sea, like an old desktop screensaver. |

<p align="center">
    <img src="docs/screenshots/look-animated.jpg" alt="Animated look" width="49%">
    <img src="docs/screenshots/look-cartoon.jpg" alt="Cartoon look" width="49%">
    <img src="docs/screenshots/look-3d-movie.jpg" alt="3D movie look" width="49%">
    <img src="docs/screenshots/look-hand-painted.jpg" alt="Hand-painted classic look" width="49%">
    <img src="docs/screenshots/look-retro.jpg" alt="Retro screensaver look" width="49%">
    <img src="docs/screenshots/coral-reef-realistic.jpg" alt="Realistic look" width="49%">
</p>

## Day and night

**Day or night** sets how bright the water is. Day is bright, Evening is warm, and Night is a dim moonlit blue that is easy on the eyes in a dark room. Follow the clock moves between them through the day: full daylight from 8:00 to 17:00, fading to evening by 19:00 and to night by 21:00, night until 6:00, then brightening back to day by 8:00. Sunlight shimmer fades to moonlight as it gets darker. **Brightness** dims everything further.

<p align="center">
    <img src="docs/screenshots/evening.jpg" alt="Kelp forest in the evening" width="49%">
    <img src="docs/screenshots/night.jpg" alt="Coral reef at night" width="49%">
</p>

## Settings

Open **Aquarium Live** from your apps on the Fire TV, or run `adb shell am start -n com.jeremykenedy.aquariumlive/.SettingsActivity`. Every setting applies the next time the screensaver starts; **Preview** shows it straight away.

<p align="center">
    <img src="docs/screenshots/settings.png" alt="Aquarium Live settings screen" width="640">
</p>

| Setting | Choices | Default |
|---------|---------|---------|
| Scene | Open ocean, Coral reef, Kelp forest, Fish tank | Coral reef |
| Style | Realistic, Animated, Cartoon, 3D movie, Hand-painted classic, Retro screensaver | Realistic |
| Day or night | Day (bright), Evening (warm), Night (dim), Follow the clock | Day (bright) |
| Plants and coral | Sparse, Normal, Lush | Normal |
| Sunlight shimmer | Off, Soft, Bright | Soft |
| Bubbles | Off, Airstone, Airstone and fish bubbles | Airstone and fish bubbles |
| Floating specks | On, Off | On |
| Fish | A few (6), A handful (12), A bunch (20), A lot (30), A ton (45) | A bunch |
| Schools | None, One school, A few schools, Huge schools | A few schools |
| Swimming speed | Calm, Normal, Lively | Normal |
| Which sea life | Sharks, Whales, Dolphins, Manta rays, Sea turtles, Octopuses, Jellyfish, Seahorses, Crabs and starfish | All |
| How much sea life | None, A little, Some, Lots | Some |
| Brightness | Full, 80%, 60%, 40% | Full |
| Resolution | Automatic (recommended), 4K (2160p), 1440p, 1080p | Automatic |
| Frame rate | Automatic (recommended), 60, 30 | Automatic |
| Show the time | On, Off | Off |

Schools come on top of the fish count: one school is 14 fish, a few schools are 14 and 18, and huge schools are 28, 26 and 24. Each scene only shows the sea life that lives there.

## Privacy

Aquarium Live requests no Android permissions at all, not even internet access, so it cannot send or receive anything. It has no ads, no analytics, no crash reporting and no third-party libraries: the app is built from its own source and the Android framework, nothing else. Settings stay on the TV.

You can check the permissions yourself on any build:

```bash
aapt dump permissions aquarium-live.apk
```

The output lists the package name and no `uses-permission` lines. The CI build fails if a permission ever appears.

## Requirements

- A Fire TV with Developer Mode and ADB debugging turned on. The steps, with screenshots, are in [Putting your Fire TV in developer mode](https://github.com/jeremykenedy/amazon-fire-tv-fixes#putting-your-fire-tv-in-developer-mode).
- [adb](https://developer.android.com/tools/adb) on a computer on the same network
- To build from source: a JDK (CI runs 17 and 21) and the Android SDK build tools

## Installation

1. Connect to the TV over the network, using your TV's IP address:

    ```bash
    adb connect <tv-ip>:5555
    ```

2. Install the APK:

    ```bash
    adb install aquarium-live.apk
    ```

3. Open **Aquarium Live** to choose your settings and preview them.

## Setting it as the screensaver

Set it as the screensaver over adb:

```bash
adb shell settings put secure screensaver_components com.jeremykenedy.aquariumlive/.AquariumDream
```

To see it right away instead of waiting for the TV to go idle:

```bash
adb shell am start -n com.android.systemui/.Somnambulator
```

Press any button on the remote to wake the TV.

## Building from source

```bash
./build.sh
```

This builds and signs `build/aquarium-live.apk` with only the JDK and the Android SDK build tools. There is no Gradle. The first build creates a signing key in `~/.android/aquarium-live.jks`; keep it backed up, because the TV only accepts updates signed with the same key. `DEBUG=1 ./build.sh` makes a debuggable build for testing on a device.

## How it works

- **Drawn in real time.** Each fish, coral, plant and creature is painted by the app when it starts, from shapes described in code, then animated on the GPU with OpenGL ES. Fish bend their tails as they swim, whales and dolphins kick their flukes, manta rays flap, turtles row with their flippers, octopuses curl their arms and jellyfish pulse.
- **Behaviour.** Fish wander, rest and turn around, schools hold together and move as one, small fish keep clear of sharks and dolphins, crabs scuttle, octopuses crawl and sometimes swim, seahorses hover near plants, and whales pass through from time to time.
- **Fast start.** The painted art is saved on the TV after the first run, so later starts load it instead of painting it again. The water appears at once and each creature fades in as its art is ready.
- **Resolution.** On the Fire TV Edition TV this was built on (model AFTDEC012E), apps draw on a 1920x1080 graphics layer that the TV scales up to its 4K panel; only video playback reaches the panel at full 4K. Automatic resolution draws at the size the TV actually shows, which keeps motion smooth. Higher resolutions are in the settings for TVs that compose apps at 4K.
- **Frame rate.** Automatic starts at 60 frames per second and settles on a steady 30 if the TV cannot keep up, because even 30 looks smoother than a rate that keeps changing. On the AFTDEC012E the default scene runs at a steady 30.

## Testing

```bash
./test.sh
```

Runs the plain-JVM tests for the simulation, the settings parsing and the shading maths: fish counts and schools for every scene and setting, sea-life toggles, everything staying inside the tank over 20 simulated minutes, whales coming and going, schools holding together, and fish turning around. `./coverage.sh` runs the same tests under JaCoCo and writes `build/jacoco.xml`.

## Uninstalling

Put the stock screensaver back first, then remove the app:

```bash
adb shell settings put secure screensaver_components com.amazon.ftv.screensaver/.app.services.ScreensaverService
adb uninstall com.jeremykenedy.aquariumlive
```

## License

Aquarium Live is open-sourced software licensed under the [MIT license](LICENSE).
