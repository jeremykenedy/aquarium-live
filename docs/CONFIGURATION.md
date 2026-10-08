# Configuration

Every setting lives on the TV. Open **Aquarium Live** from the apps on the TV, or run:

```bash
adb shell am start -n com.jeremykenedy.aquariumlive/.SettingsActivity
```

Settings apply the next time the screensaver starts. **Preview** at the top of the screen shows them straight away; press any button to come back. **Reset to defaults** at the bottom puts every setting back.

- [Surprise me and Random](#surprise-me-and-random)
- [Scene](#scene)
- [Fish](#fish)
- [Sea life](#sea-life)
- [Display](#display)
- [Recipes](#recipes)
- [Screenshots](#screenshots)

## Surprise me and Random

**Surprise me** picks a random scene, look, lighting, fish and sea life every time the screensaver starts. While it is on, the settings it controls are greyed out. Their saved choices are not changed, so turning it off brings them back as they were.

Each of those settings also has its own **Random** choice, picked again on every start. Use it to vary just the settings you want and keep the rest fixed.

| Setting | What Random picks from |
|---------|------------------------|
| Scene | Open ocean, Coral reef, Kelp forest, Fish tank |
| Style | All six looks |
| Day or night | Day, Evening or Night (never Follow the clock) |
| Plants and coral | Sparse, Normal, Lush |
| Sunlight shimmer | Off, Soft, Bright |
| Bubbles | Off, Airstone, Airstone and fish bubbles |
| Floating specks | On or Off |
| Fish | 6, 12, 20, 30 or 45 |
| Schools | None, One, A few, Huge |
| Kinds in a school | One kind per school or Mixed kinds |
| Swimming speed | Calm, Normal, Lively |
| Which sea life | A random mix: each group has an even chance, and at least one is always in |
| How much sea life | None, A little, Some, Lots |

Brightness, Resolution, Frame rate and Show the time are never randomized, so the screensaver never surprises you with a brighter screen or a slower frame rate.

## Scene

| Setting | Choices | Default | Notes |
|---------|---------|---------|-------|
| Scene | Open ocean, Coral reef, Kelp forest, Fish tank, Random | Coral reef | Each scene has its own fish and sea life; see the scene table in the [README](../README.md#scenes). |
| Style | Realistic, Animated, Cartoon, 3D movie, Hand-painted classic, Retro screensaver, Random | Realistic | How everything is drawn. Retro has no haze, light shafts or rippling light, so Sunlight shimmer does nothing there; with Resolution on Automatic it draws at 540 lines with hard pixel edges. |
| Day or night | Day (bright), Evening (warm), Night (dim), Follow the clock, Random | Day (bright) | Follow the clock: day from 8:00 to 17:00, fading to evening by 19:00 and to night by 21:00, night until 6:00, then back to day by 8:00. The light is checked every 30 seconds. |
| Plants and coral | Sparse, Normal, Lush, Random | Normal | How much grows on the floor. |
| Sunlight shimmer | Off, Soft, Bright, Random | Soft | Light shafts from the surface and rippling light on the sand. It fades to moonlight as the lighting gets darker. |
| Bubbles | Off, Airstone, Airstone and fish bubbles, Random | Airstone and fish bubbles | |
| Floating specks | On, Off, Random | On | Tiny bits drifting in the water. |

## Fish

| Setting | Choices | Default | Notes |
|---------|---------|---------|-------|
| Fish | A few (6), A handful (12), A bunch (20), A lot (30), A ton (45), Random | A bunch | Individual fish, not counting schools. |
| Schools | None, One school, A few schools, Huge schools, Random | A few schools | Added on top of the fish count: one school is 14 fish, a few are 14 and 18, huge are 28, 26 and 24. Schools swim as one and scatter around sharks and dolphins. |
| Kinds in a school | One kind per school, Mixed kinds, Random | One kind per school | Mixed kinds fills each school with different fish from the scene, which swim together at the school's pace. The school spreads out to fit its biggest fish. |
| Swimming speed | Calm, Normal, Lively, Random | Normal | Calm is 0.6 times normal speed, Lively 1.4 times. |

## Sea life

| Setting | Choices | Default |
|---------|---------|---------|
| Which sea life | Sharks, Whales, Dolphins, Manta rays, Sea turtles, Octopuses, Jellyfish, Seahorses, Crabs and starfish, A random mix each time | Every group |
| How much sea life | None, A little, Some, Lots, Random | Some |

Each scene only shows the sea life that lives there, and the fish tank has none. How many of each group appear:

| Group | A little | Some | Lots |
|-------|----------|------|------|
| Sharks | 1 | 2 | 3 |
| Whales | 1 | 1 | 2 |
| Dolphins | 2 | 3 | 5 |
| Jellyfish | 2 | 4 | 8 |
| Seahorses | 1 | 2 | 3 |
| Crabs and starfish | 1 | 2 | 3 |
| Manta rays, sea turtles, octopuses | 1 | 1 | 2 |

Whales do not stay: one swims through every so often, far in the background, then leaves.

## Display

| Setting | Choices | Default | Notes |
|---------|---------|---------|-------|
| Brightness | Full, 80%, 60%, 40% | Full | Dims the whole picture on top of Day or night. |
| Resolution | Automatic (recommended), 4K (2160p), 1440p, 1080p | Automatic | Automatic draws at the size the TV gives the app. A fixed choice draws at that height, capped at the screen's own height. See [Architecture](ARCHITECTURE.md#resolution). |
| Frame rate | Automatic (recommended), 60, 30 | Automatic | Automatic starts at 60 frames a second and switches to a steady 30 if the TV cannot keep up. |
| Show the time | On, Off | Off | A small clock that fades to a new spot every 45 seconds, so it never sits in one place. |

## Recipes

- **Something new every night:** turn on Surprise me.
- **Always a reef, but a different look:** Scene Coral reef, Style Random.
- **Dark room:** Day or night Night (dim), Brightness 60% or 40%, Sunlight shimmer Soft or Off.
- **Calm tank:** Scene Fish tank, Swimming speed Calm, Schools One school.
- **Big ocean:** Scene Open ocean, How much sea life Lots, Schools Huge schools.
- **Old desktop screensaver:** Style Retro screensaver, Scene Fish tank.

## Screenshots

Captured on the Google TV 14 emulator. Select any screenshot to open it full size.

<p align="center">
    <a href="screenshots/settings-top.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/settings-top-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/settings-top-tablet.jpg 2x"><img src="screenshots/settings-top.jpg" alt="Settings: Preview, Surprise me and the scene" title="Settings: Preview, Surprise me and the scene"></picture></a>
    <a href="screenshots/settings-scene.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/settings-scene-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/settings-scene-tablet.jpg 2x"><img src="screenshots/settings-scene.jpg" alt="Settings: style, lighting, plants, shimmer, bubbles and specks" title="Settings: style, lighting, plants, shimmer, bubbles and specks"></picture></a>
    <a href="screenshots/settings-fish.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/settings-fish-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/settings-fish-tablet.jpg 2x"><img src="screenshots/settings-fish.jpg" alt="Settings: fish, schools, kinds in a school and swimming speed" title="Settings: fish, schools, kinds in a school and swimming speed"></picture></a>
    <a href="screenshots/settings-display.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/settings-display-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/settings-display-tablet.jpg 2x"><img src="screenshots/settings-display.jpg" alt="Settings: sea life, brightness, resolution, frame rate and the clock" title="Settings: sea life, brightness, resolution, frame rate and the clock"></picture></a>
    <a href="screenshots/choice-scene.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/choice-scene-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/choice-scene-tablet.jpg 2x"><img src="screenshots/choice-scene.jpg" alt="Choosing a scene, with Random" title="Choosing a scene, with Random"></picture></a>
    <a href="screenshots/choice-style.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/choice-style-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/choice-style-tablet.jpg 2x"><img src="screenshots/choice-style.jpg" alt="Choosing a look, with Random" title="Choosing a look, with Random"></picture></a>
    <a href="screenshots/choice-sea-life.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/choice-sea-life-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/choice-sea-life-tablet.jpg 2x"><img src="screenshots/choice-sea-life.jpg" alt="Choosing which sea life can appear" title="Choosing which sea life can appear"></picture></a>
    <a href="screenshots/surprise-me.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/surprise-me-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/surprise-me-tablet.jpg 2x"><img src="screenshots/surprise-me.jpg" alt="Surprise me on, with the settings it chooses greyed out" title="Surprise me on, with the settings it chooses greyed out"></picture></a>
    <a href="screenshots/mixed-schools.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/mixed-schools-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/mixed-schools-tablet.jpg 2x"><img src="screenshots/mixed-schools.jpg" alt="Mixed kinds of fish swimming together in schools" title="Mixed kinds of fish swimming together in schools"></picture></a>
    <a href="screenshots/clock.jpg"><picture><source media="(min-width: 1280px)" srcset="screenshots/grid/clock-desktop.jpg 2x"><source media="(min-width: 600px)" srcset="screenshots/grid/clock-tablet.jpg 2x"><img src="screenshots/clock.jpg" alt="Kelp forest with the clock shown" title="Kelp forest with the clock shown"></picture></a>
</p>
