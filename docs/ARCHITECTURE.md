# Architecture

Aquarium Live is a single Android app with no third-party libraries. Everything on screen is drawn by the app: the APK holds no videos, bitmap images or other media (the icon and TV banner are vector drawings), and nothing is downloaded.

- [Components](#components)
- [Source files](#source-files)
- [Settings](#settings)
- [Simulation](#simulation)
- [Art](#art)
- [Rendering](#rendering)
- [Frame pacing](#frame-pacing)
- [Resolution](#resolution)
- [Privacy by construction](#privacy-by-construction)

## Components

| Component | Class | Role |
|-----------|-------|------|
| Screensaver | `AquariumDream` | The `DreamService` the TV starts when it goes idle. Not interactive, so any button wakes the TV. |
| Settings | `SettingsActivity` | The launcher entry and the screensaver's settings screen. |
| Preview | `PreviewActivity` | Full-screen preview from the settings screen. Any button closes it. |

The screensaver and the preview share one view, `AquariumView`, so what you see in the preview is what the screensaver draws.

## Source files

All source lives in `src/com/jeremykenedy/aquariumlive/`.

| File | What it does |
|------|--------------|
| `Config.java` | Reads the saved settings, including Random and Surprise me, into plain fields. Anything missing or unrecognised falls back to its default. Also holds the day, evening and night light colours and works out the render size for the Resolution setting. |
| `Species.java` | Every fish and sea creature: size, speed, how it swims, which scene it lives in and which sea-life group it belongs to. |
| `Sim.java` | The tank's state and motion. No Android code, so it is tested on a plain JVM. |
| `Tone.java` | The colour maths for the cel-shaded and retro looks. Also plain Java and tested. |
| `FramePacer.java` | The automatic frame rate: when to drop from 60 to a steady 30. Plain Java and tested. |
| `ArtCache.java` | Deletes saved art from an older install. Plain Java and tested. |
| `FishArt.java`, `CreatureArt.java`, `PlantArt.java` | Paint each fish, creature, plant, coral and rock from shapes described in code, using Android's 2D `Canvas`. |
| `ArtStyle.java` | The finishing pass that gives painted art the chosen look: ink outlines, colour, cel bands or a reduced palette. |
| `Textures.java` | Smaller generated textures: sand, bubbles, light patterns. |
| `Gl.java` | Small OpenGL ES helpers: shaders, meshes, texture upload. |
| `AquariumRenderer.java` | Draws every frame with OpenGL ES. |
| `AquariumView.java` | Hosts the renderer and the optional clock, and drives frames from the display's refresh. |
| `AquariumDream.java`, `SettingsActivity.java`, `PreviewActivity.java` | The three components above. |

## Settings

Settings are standard Android preferences (`res/xml/settings.xml`). `AquariumView` reads them once each time the screensaver or preview starts and hands them to `Config.fromMap`, which:

- parses each value and falls back to the default for anything unknown,
- picks a value for every setting left on Random, and for every scene, look and content setting when Surprise me is on,
- never randomizes Brightness, Resolution, Frame rate or the clock.

Because the choice is made once per start, a random scene stays the same until the screensaver starts again.

## Simulation

`Sim` holds every creature and plant and moves them each frame. The tank is 1080 world units tall and as wide as the screen's shape requires. `y` points up, and `z` runs from the front glass (0) to the back wall (1); things further back are drawn smaller and hazier.

- Fish wander, rest and turn around, and stay inside the tank.
- Schools hold together and move as one.
- Small fish keep clear of sharks and dolphins.
- Crabs and starfish stay on the floor, octopuses crawl and sometimes swim, seahorses hover near plants, and jellyfish drift and pulse.
- Whales visit: one swims through far in the background every so often, then leaves.
- Plants and corals are laid out from placement rules per scene, scaled by the Plants and coral setting.

The same seed always builds the same tank, which the tests rely on.

## Art

Each sprite is painted on the CPU the first time it is needed:

1. The renderer asks for the art and a pool of three background threads paints it, so the main thread and the frame loop are never blocked.
2. The finished picture is saved as a PNG in the app's cache folder (`cache/art`). Later starts load the PNG instead of painting again.
3. Finished art is uploaded to the GPU a few textures per frame, so no single frame stalls.
4. New art fades in over 0.8 seconds rather than popping in. The whole scene fades in over 1.6 seconds when it starts.

The saved art is tied to the installed version of the app: after an update, `ArtCache` deletes the old files and the art is painted again, because it may have changed.

## Rendering

`AquariumRenderer` draws with OpenGL ES:

- **Batching.** Creatures and plants are queued as instances and drawn in batches, one draw call per run of items sharing a texture. On OpenGL ES 3.0 the batch is a single instanced draw; on OpenGL ES 2.0 it falls back to one draw per item. This keeps the driver's per-call cost from dominating on a TV's small CPU.
- **Swimming.** Bodies bend in the vertex shader: tails beat, flukes kick, wings flap, bells pulse.
- **Water.** A gradient per scene and look, with haze that grows with depth.
- **Sunlight.** The light shafts only vary along one slanted axis, so the CPU fills a 512-texel strip each frame and the shader looks it up, instead of working it out per pixel. Rippling light on the sand comes from scrolling textures; on the creatures it is a moving highlight worked out from position and time.
- **Lighting.** Day, evening and night are colour multipliers. Follow the clock blends between them by the hour and is rechecked every 30 seconds. Brightness multiplies on top.
- **Looks.** Each look sets the water colours and how strong the haze, shafts and ripples are. Retro turns all three off and uses hard-edged (nearest) texture filtering.

## Frame pacing

Frames are driven by the display's refresh (`Choreographer`), not a timer, and the GL view only renders when asked. `FramePacer` decides the rate.

- **Automatic:** draws at 60 frames a second. After the first three seconds, while art is still being uploaded, it measures for four seconds; below 54 frames a second it switches to drawing on every other refresh, a steady 30. A steady 30 looks smoother than a rate that keeps changing between 30 and 60.
- **60** or **30:** fixed.

On the AFTDEC012E Fire TV, the release build of the default reef runs at a steady 30 frames a second (measured at 30.0 fps, median frame 33.3 ms, 95th percentile 33.4 ms).

## Resolution

On the AFTDEC012E Fire TV, `dumpsys SurfaceFlinger` reports an on-screen graphics layer of 1920x1080, and app graphics are composed into it and scaled up to the 4K panel. Only video playback reaches that panel at full 4K. Drawing at 4K there cost speed (6.5 to 10 frames a second) without adding detail.

So **Automatic** draws at the size the TV gives the app's window. The fixed choices draw at that many lines, capped at the screen's own height, for TVs that do compose apps at 4K. The Retro look draws at 540 lines on Automatic for its chunky pixels.

## Privacy by construction

- The manifest requests no permissions at all, so the app has no network access. CI fails the build if a permission ever appears.
- No third-party code: the APK is built from this repository's source and the Android framework only.
- Settings and the art cache stay on the TV, and backup is turned off (`android:allowBackup="false"`).
