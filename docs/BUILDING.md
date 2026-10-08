# Building

The APK is built by `build.sh` with the JDK and the Android SDK build tools only. There is no Gradle and nothing is downloaded during the build.

- [Requirements](#requirements)
- [Build](#build)
- [What build.sh does](#what-buildsh-does)
- [Signing key](#signing-key)
- [Debug builds](#debug-builds)
- [Versions](#versions)

## Requirements

| Tool | Notes |
|------|-------|
| JDK | CI builds with 17 and tests with 17 and 21. Sources compile with `--release 8`. |
| Android SDK | At least one `platforms/android-*` and one `build-tools/*`. The newest of each is used. |
| `zip`, `openssl`, `keytool` | `keytool` comes with the JDK. `openssl` is only used to make the signing key the first time. |

The SDK is found at `$ANDROID_HOME`, or `~/Library/Android/sdk` if that is not set.

## Build

```bash
./build.sh
```

The signed APK is written to `build/aquarium-live.apk`, and its SHA-256 is printed at the end.

## What build.sh does

1. `aapt2 compile` and `aapt2 link` build the resources and manifest, and generate `R.java`. The minimum and target SDK versions are set here.
2. `javac` compiles every source file against `android.jar`.
3. `d8` converts the classes to `classes.dex`, which is added to the APK.
4. `zipalign` aligns the APK, and `apksigner` signs and verifies it.

`build/` is recreated on every run and is ignored by git.

## Signing key

The first build makes a signing key:

| File | Contents |
|------|----------|
| `~/.android/aquarium-live.jks` | The keystore (RSA 2048, valid for 30 years) |
| `~/.android/aquarium-live.pass` | Its random password, readable only by you |

Both live outside the repository and are listed in `.gitignore`. Back them up. Android only installs an update over an existing install when it is signed with the same key, so builds signed with a different key cannot update the released app; it has to be uninstalled first.

CI makes a throwaway key for each run, so CI builds are for checking only and are not published.

## Debug builds

```bash
DEBUG=1 ./build.sh
```

Marks the APK debuggable, so `adb shell run-as com.jeremykenedy.aquariumlive` works for looking at its files on a device. Do not publish debug builds. Debuggable builds also run noticeably slower on a TV because Android adds extra runtime checks.

## Versions

`VERSION_CODE`, `VERSION_NAME`, `MIN_SDK` and `TARGET_SDK` are set at the top of `build.sh`. See [Releasing](RELEASING.md) for when to change them.
