# Changelog

## 1.0.3

### Added

- Kinds in a school: One kind per school, Mixed kinds, or Random. With Mixed kinds, different fish from the scene swim together in the same school, and the school spreads out to fit its biggest fish. Surprise me picks it too.

### Changed

- The README no longer opens with a large screenshot under the badges; the screenshots sit in the Scenes, Looks and Day and night sections.
- The GitHub stars badge sits with the Follow, Star and Sponsor badges, and the CodeFactor badge now shows the repository's grade.

## 1.0.2

### Fixed

- A fresh install started with Which sea life on a random mix instead of every group, because the setting's default list included the random-mix choice. It now defaults to every group, and a test checks every setting's default against its own choices.

### Security

- The screensaver service is no longer exported; only the system can start it. Checked on the Fire TV, Google TV 14, Android TV 12 and Android 5.1.
- A network security policy refuses cleartext traffic and user-installed certificates.

### Changed

- 100% line and branch coverage of every class that runs on a plain JVM. The render size, the automatic frame rate and the art cache clean-up moved out of the Android classes so they are tested.
- Fixed the SonarCloud reliability findings and the Codacy code findings: fields come before methods, loops count with whole numbers or step a float in a while loop, and a few unused names are gone. The painted art is byte-for-byte unchanged.
- New Code style, Documentation and Security workflows, Codacy configuration, and Dependabot for the pinned GitHub Actions.

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
