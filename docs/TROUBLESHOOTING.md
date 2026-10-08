# Troubleshooting

- [The screensaver never starts](#the-screensaver-never-starts)
- [The install or update fails](#the-install-or-update-fails)
- [It takes a few seconds to appear](#it-takes-a-few-seconds-to-appear)
- [A setting did not change anything](#a-setting-did-not-change-anything)
- [Motion is not smooth](#motion-is-not-smooth)
- [It does not look 4K](#it-does-not-look-4k)
- [It is too bright at night](#it-is-too-bright-at-night)
- [It closed when opening Preview on Android 5.1](#it-closed-when-opening-preview-on-android-51)

## The screensaver never starts

1. Check it is the selected screensaver:

    ```bash
    adb shell settings get secure screensaver_components
    ```

    It should print `com.jeremykenedy.aquariumlive/.AquariumDream`. If not, set it again as in [Installation](INSTALLATION.md#4-set-it-as-the-screensaver).

2. Check screensavers are turned on:

    ```bash
    adb shell settings get secure screensaver_enabled
    ```

    It should print `1`.

3. Check the device is allowed to go idle. When `stay_on_while_plugged_in` is not `0`, the device never goes idle and the screensaver never starts:

    ```bash
    adb shell settings get global stay_on_while_plugged_in
    ```

4. Check that the screensaver itself works by starting it directly. This works on Fire TV and Android 5.1, but did not on the Google TV 14 emulator:

    ```bash
    adb shell am start -n com.android.systemui/.Somnambulator
    ```

5. See what is running:

    ```bash
    adb shell dumpsys dreams | grep -i dream
    ```

## The install or update fails

| Error | Cause | Fix |
|-------|-------|-----|
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | The installed copy was signed with a different key, for example one you built yourself. Android only updates an app with an APK signed by the same key. | Note your settings, `adb uninstall com.jeremykenedy.aquariumlive`, then install again. |
| The checksum does not match | The download is incomplete or not the published file. | Do not install it. Download it again from the [releases page](https://github.com/jeremykenedy/aquarium-live/releases). |

The app needs Android 5.1 (API 22) or newer and will not install on anything older.

## It takes a few seconds to appear

The first time a scene and look are shown, every fish, plant and creature is painted on the TV, which takes a few seconds on a TV's processor. The water appears first and each creature fades in as its art is ready. The art is saved, so later starts with the same scene and look are quicker. After an app update the art is painted again once.

## A setting did not change anything

- Settings apply the next time the screensaver starts. Use **Preview** to see them at once.
- If the setting is greyed out, **Surprise me** is on and is choosing it for you. Turn Surprise me off to set it yourself.
- **Sunlight shimmer** has no effect in the Retro screensaver look, which has no light effects.
- **Which sea life** and **How much sea life** do nothing in the fish tank, which has no sea life.

## Motion is not smooth

- Set **Resolution** to Automatic. A fixed resolution higher than the TV's own graphics layer costs speed without adding detail.
- Set **Frame rate** to Automatic or 30. Automatic already settles on a steady 30 when the TV cannot keep up with 60.
- Fewer fish, smaller schools or less sea life means less to draw.

## It does not look 4K

On the Fire TV this was built on (AFTDEC012E), apps draw on a 1920x1080 graphics layer that the TV scales up to its 4K panel; only video playback reaches the panel at full 4K. That is a limit of the TV, not a setting. See [Architecture](ARCHITECTURE.md#resolution).

## It is too bright at night

- Set **Day or night** to Night (dim), or to Follow the clock, which fades to evening from 17:00 and to night by 21:00.
- Lower **Brightness** to 60% or 40%. It dims on top of the lighting.
- Turn **Sunlight shimmer** to Soft or Off.

## It closed when opening Preview on Android 5.1

Version 1.0.0 closed on Android 5.1 when Resolution was set to anything other than Automatic. Version 1.0.1 fixes this; update, or set Resolution back to Automatic.
