# Releasing

Releases are published on GitHub with the signed APK and its SHA-256 checksum. They must be built on the machine that holds the signing key (see [Building](BUILDING.md#signing-key)); CI builds use a throwaway key and are never published.

## Steps

1. Make sure `main` is clean, pushed, and CI is green.

2. Raise the version at the top of `build.sh`. Raise `VERSION_CODE` by one for every release, so each release is numbered above the last; `VERSION_NAME` is the version people see.

    ```bash
    VERSION_CODE=2
    VERSION_NAME=1.0.1
    ```

3. Add the release to [CHANGELOG.md](../CHANGELOG.md).

4. Run the tests and build:

    ```bash
    ./test.sh
    ./build.sh
    ```

5. Check the APK requests no permissions and is not debuggable:

    ```bash
    AAPT="$(ls -d "$ANDROID_HOME"/build-tools/* | sort -V | tail -1)/aapt"
    "$AAPT" dump permissions build/aquarium-live.apk
    "$AAPT" dump badging build/aquarium-live.apk | grep -E "versionName|application-debuggable"
    ```

6. Install it on a TV over the previous release (`adb install -r`) and check the settings, preview and screensaver.

7. Commit the version change and changelog, push, and wait for CI.

8. Write the checksum and publish:

    ```bash
    cd build
    shasum -a 256 aquarium-live.apk > aquarium-live.apk.sha256
    gh release create v1.0.1 aquarium-live.apk aquarium-live.apk.sha256 \
      --repo jeremykenedy/aquarium-live --target main \
      --title "Aquarium Live 1.0.1" --notes-file notes.md
    ```

    Put the changelog entry and the SHA-256 in the release notes.

9. Download the published files and check them:

    ```bash
    gh release download v1.0.1 --repo jeremykenedy/aquarium-live --dir /tmp/aql-check
    cd /tmp/aql-check && shasum -a 256 -c aquarium-live.apk.sha256
    ```
