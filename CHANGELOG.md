# Changelog

## Unreleased

### Changed

- 100% line and branch coverage of every class that runs on a plain JVM. The render size, the automatic frame rate and the art cache clean-up moved out of the Android classes so they are tested.
- Fixed the SonarCloud reliability findings: whole-number arithmetic passed where decimals are expected, and unchecked file deletes.
- New Code style, Documentation and Security workflows and a Scrutinizer configuration. Every action is pinned to a commit.

## 1.0.1

### Fixed

- The preview and screensaver closed on Android 5.1 when Resolution was set to anything other than Automatic. They now work there at every resolution.

### Documentation

- New guides in `docs/`: installation, configuration, architecture, building, testing, troubleshooting, releasing and CI.
- The README covers downloading and checking a release, the guides, and the devices it was tested on.

## 1.0.0

First release.

- Four scenes: open ocean, coral reef, kelp forest and a fish tank.
- Six looks: realistic, animated, cartoon, 3D movie, hand-painted classic and retro screensaver.
- Day, evening and night lighting, or lighting that follows the clock.
- Sharks, whales, dolphins, manta rays, turtles, octopuses, jellyfish, seahorses, crabs and starfish, each switchable.
- Random choices for every scene, look and content setting, or Surprise me to randomize them all each time it starts.
- Works on Fire TV, Android TV and Google TV.
- No permissions, no network access, no ads, no tracking or analytics.
