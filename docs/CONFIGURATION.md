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
