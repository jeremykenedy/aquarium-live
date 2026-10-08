# Installation

Aquarium Live is installed with `adb` from a computer on the same network as the TV. The same APK works on Fire TV, Android TV and Google TV.

- [What you need](#what-you-need)
- [1. Turn on debugging on the TV](#1-turn-on-debugging-on-the-tv)
- [2. Download the APK and check it](#2-download-the-apk-and-check-it)
- [3. Connect and install](#3-connect-and-install)
- [4. Set it as the screensaver](#4-set-it-as-the-screensaver)
- [5. Try it](#5-try-it)
- [Updating](#updating)
- [Uninstalling](#uninstalling)

## What you need

- A Fire TV, Android TV or Google TV on Android 5.1 (API 22) or newer, with OpenGL ES 2.0.
- A computer with [adb](https://developer.android.com/tools/adb) (Android SDK Platform Tools).
- The TV and the computer on the same network.

## 1. Turn on debugging on the TV

ADB debugging has to be on before a computer can install apps on the TV.

- **Fire TV:** the steps, with screenshots, are in [Putting your Fire TV in developer mode](https://github.com/jeremykenedy/amazon-fire-tv-fixes#putting-your-fire-tv-in-developer-mode).
- **Android TV and Google TV:** turn on the TV's developer options, then turn on USB debugging or network debugging there. Where these settings live differs between TV makers.

Note the TV's IP address from its network settings.

## 2. Download the APK and check it

Download `aquarium-live.apk` and `aquarium-live.apk.sha256` from the [latest release](https://github.com/jeremykenedy/aquarium-live/releases/latest), or with the GitHub CLI:

```bash
gh release download --repo jeremykenedy/aquarium-live --pattern 'aquarium-live.apk*'
```

Check the APK against the published checksum before installing it:

```bash
shasum -a 256 -c aquarium-live.apk.sha256
```

It should print `aquarium-live.apk: OK`. The same checksum is in the release notes. If it does not match, do not install the file; download it again.

## 3. Connect and install

```bash
adb connect <tv-ip>:5555
adb install aquarium-live.apk
```

`adb devices` should list the TV with the state `device` before you install.

## 4. Set it as the screensaver

First note the screensaver the TV uses now, so you can put it back later:

```bash
adb shell settings get secure screensaver_components
```

Then set Aquarium Live:

```bash
adb shell settings put secure screensaver_components com.jeremykenedy.aquariumlive/.AquariumDream
```

Check it took:

```bash
adb shell settings get secure screensaver_components
```

It should print `com.jeremykenedy.aquariumlive/.AquariumDream`.

## 5. Try it

Open **Aquarium Live** from the apps on the TV to choose settings. **Preview** shows the screensaver straight away with those settings; press any button to come back. See [Configuration](CONFIGURATION.md) for every setting.

To start the real screensaver at once instead of waiting for the TV to go idle:

```bash
adb shell am start -n com.android.systemui/.Somnambulator
```

This worked on the Fire TV and on Android 5.1. On the Google TV 14 emulator it did not start the screensaver; there it started on its own once the TV had been idle for its screensaver timeout. Press any button on the remote to wake the TV.

## Updating

Download the new release, check its checksum, then install over the old one:

```bash
adb install -r aquarium-live.apk
```

Your settings are kept. The first start after an update repaints the art, because the saved art is tied to the installed version, so it takes a few seconds longer than usual.

## Uninstalling

Put back the screensaver you noted in step 4, then remove the app:

```bash
adb shell settings put secure screensaver_components <the value you noted>
adb uninstall com.jeremykenedy.aquariumlive
```

If you did not note it, these are the stock values on the devices this was tested on:

| Device | Stock screensaver |
|--------|-------------------|
| Fire TV | `com.amazon.ftv.screensaver/.app.services.ScreensaverService` |
| Google TV 14 emulator | `com.google.android.apps.tv.dreamx/.service.Backdrop` |
| Android TV 12 emulator | `com.google.android.backdrop/.Backdrop` |
