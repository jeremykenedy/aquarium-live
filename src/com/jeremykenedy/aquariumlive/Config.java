package com.jeremykenedy.aquariumlive;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Every user-facing option. Values arrive as the strings a ListPreference
 * stores (or booleans from a SwitchPreference), so anything missing or
 * unrecognised falls back to its default instead of failing.
 */
public final class Config {

    public enum Theme { OCEAN, REEF, KELP_FOREST, FISH_TANK }

    public enum Lighting { DAY, EVENING, NIGHT, AUTO }

    /**
     * How everything is drawn. MOVIE is a glossy 3D animated-film look,
     * PAINTED a classic hand-painted 2D animation look, RETRO an old
     * desktop screensaver look.
     */
    public enum Style { REALISTIC, ANIMATED, CARTOON, MOVIE, PAINTED, RETRO }

    public static final String THEME = "theme";
    public static final String STYLE = "style";
    public static final String FISH_COUNT = "fish_count";
    public static final String SCHOOLS = "schools";
    public static final String SPEED = "speed";
    public static final String PLANTS = "plants";
    public static final String BUBBLES = "bubbles";
    public static final String SEA_LIFE = "sea_life";
    public static final String SEA_LIFE_AMOUNT = "sea_life_amount";
    public static final String SHIMMER = "shimmer";
    public static final String PARTICLES = "specks";
    public static final String SURPRISE = "surprise";
    /** Stored by any list setting whose choice is Random, and in the sea-life set for a random mix. */
    public static final String RANDOM = "RANDOM";
    public static final String LIGHTING = "lighting";
    public static final String BRIGHTNESS = "brightness";
    public static final String RESOLUTION = "resolution";
    public static final String FPS = "fps";
    public static final String CLOCK = "clock";

    static final int[] FISH_COUNTS = {6, 12, 20, 30, 45};
    static final int[] SCHOOL_LEVELS = {0, 1, 2, 3};
    static final int[] SEA_LIFE_LEVELS = {0, 1, 2, 3};
    static final int[] SHIMMER_LEVELS = {0, 1, 2};
    static final int[] RESOLUTIONS = {0, 2160, 1440, 1080};
    static final int[] FRAME_RATES = {0, 60, 30};
    static final int[] BRIGHTNESS_LEVELS = {100, 80, 60, 40};
    static final float[] SPEEDS = {0.6f, 1f, 1.4f};
    static final float[] PLANT_DENSITIES = {0.5f, 1f, 1.6f};
    static final int[] BUBBLE_LEVELS = {0, 1, 2};
    static final Lighting[] RANDOM_LIGHTING = {Lighting.DAY, Lighting.EVENING, Lighting.NIGHT};

    public Theme theme = Theme.REEF;
    public Style style = Style.REALISTIC;
    public int fishCount = 20;
    /** 0 none, 1 one school, 2 a few schools, 3 huge schools. */
    public int schools = 2;
    /** Which sea-life groups may appear (see Species.GROUPS). */
    public java.util.Set<String> seaLife = new java.util.HashSet<>(java.util.Arrays.asList(Species.GROUPS));
    /** 0 none, 1 a little, 2 some, 3 lots. */
    public int seaLifeAmount = 2;
    /** Sunlight from the surface: 0 off, 1 soft, 2 bright. */
    public int shimmer = 1;
    public float speed = 1f;
    public float plantDensity = 1f;
    /** 0 off, 1 airstone only, 2 airstone plus stray bubbles. */
    public int bubbles = 2;
    public boolean particles = true;
    public Lighting lighting = Lighting.DAY;
    public float brightness = 1f;
    /** Render height in pixels, 0 for the panel's native resolution. */
    public int resolution = 0;
    /** 60, 30, or 0 to start at 60 and settle on a steady 30 if the TV cannot keep up. */
    public int fps = 0;
    public boolean clock = false;

    public static Config fromMap(Map<String, ?> values) {
        return fromMap(values, new Random());
    }

    /**
     * Any setting left on Random, or every scene, look and content setting
     * when Surprise me is on, is picked from its choices with {@code rng}.
     * Display settings (brightness, resolution, frame rate, clock) never are.
     */
    public static Config fromMap(Map<String, ?> values, Random rng) {
        Config c = new Config();
        boolean all = parseBool(values.get(SURPRISE), false);
        c.theme = random(all, values.get(THEME)) ? pick(Theme.values(), rng) : parseEnum(values.get(THEME), Theme.class, c.theme);
        c.style = random(all, values.get(STYLE)) ? pick(Style.values(), rng) : parseEnum(values.get(STYLE), Style.class, c.style);
        c.lighting = random(all, values.get(LIGHTING)) ? pick(RANDOM_LIGHTING, rng) : parseEnum(values.get(LIGHTING), Lighting.class, c.lighting);
        c.fishCount = random(all, values.get(FISH_COUNT)) ? pick(FISH_COUNTS, rng) : parseChoice(values.get(FISH_COUNT), FISH_COUNTS, c.fishCount);
        c.schools = random(all, values.get(SCHOOLS)) ? pick(SCHOOL_LEVELS, rng) : parseChoice(values.get(SCHOOLS), SCHOOL_LEVELS, c.schools);
        c.seaLife = all || isRandomSet(values.get(SEA_LIFE)) ? randomMix(rng) : parseSet(values.get(SEA_LIFE), c.seaLife);
        c.seaLifeAmount = random(all, values.get(SEA_LIFE_AMOUNT)) ? pick(SEA_LIFE_LEVELS, rng) : parseChoice(values.get(SEA_LIFE_AMOUNT), SEA_LIFE_LEVELS, c.seaLifeAmount);
        c.shimmer = random(all, values.get(SHIMMER)) ? pick(SHIMMER_LEVELS, rng) : parseChoice(values.get(SHIMMER), SHIMMER_LEVELS, c.shimmer);
        c.speed = random(all, values.get(SPEED)) ? pick(SPEEDS, rng) : parseFloat(values.get(SPEED), 0.4f, 2f, c.speed);
        c.plantDensity = random(all, values.get(PLANTS)) ? pick(PLANT_DENSITIES, rng) : parseFloat(values.get(PLANTS), 0.3f, 2f, c.plantDensity);
        c.bubbles = random(all, values.get(BUBBLES)) ? pick(BUBBLE_LEVELS, rng) : Math.round(parseFloat(values.get(BUBBLES), 0f, 2f, c.bubbles));
        c.particles = random(all, values.get(PARTICLES)) ? rng.nextBoolean() : parseBool(values.get(PARTICLES), c.particles);
        c.brightness = parseChoice(values.get(BRIGHTNESS), BRIGHTNESS_LEVELS, 100) / 100f;
        c.resolution = parseChoice(values.get(RESOLUTION), RESOLUTIONS, c.resolution);
        c.fps = parseChoice(values.get(FPS), FRAME_RATES, c.fps);
        c.clock = parseBool(values.get(CLOCK), c.clock);
        return c;
    }

    private static boolean random(boolean all, Object value) {
        return all || (value != null && RANDOM.equalsIgnoreCase(value.toString().trim()));
    }

    private static boolean isRandomSet(Object value) {
        if (!(value instanceof Set)) {
            return false;
        }
        for (Object o : (Set<?>) value) {
            if (RANDOM.equalsIgnoreCase(String.valueOf(o))) {
                return true;
            }
        }
        return false;
    }

    /** Each sea-life group has an even chance of being in; never none, so the choice is not wasted. */
    static Set<String> randomMix(Random rng) {
        Set<String> out = new HashSet<>();
        for (String g : Species.GROUPS) {
            if (rng.nextBoolean()) {
                out.add(g);
            }
        }
        if (out.isEmpty()) {
            out.add(Species.GROUPS[rng.nextInt(Species.GROUPS.length)]);
        }
        return out;
    }

    private static <T> T pick(T[] options, Random rng) {
        return options[rng.nextInt(options.length)];
    }

    private static int pick(int[] options, Random rng) {
        return options[rng.nextInt(options.length)];
    }

    private static float pick(float[] options, Random rng) {
        return options[rng.nextInt(options.length)];
    }

    /**
     * Light colour multiplier for the given lighting mode and local hour
     * (0 to 24, fractional). AUTO follows the clock: day from 8 to 17,
     * evening until 20, night until 6, then a dawn ramp back to day.
     */
    public static float[] tint(Lighting mode, float hour) {
        switch (mode) {
            case EVENING:
                return EVENING_TINT.clone();
            case NIGHT:
                return NIGHT_TINT.clone();
            case AUTO:
                return autoTint(hour);
            default:
                return DAY_TINT.clone();
        }
    }

    static final float[] DAY_TINT = {1f, 1f, 1f};
    static final float[] EVENING_TINT = {0.95f, 0.78f, 0.62f};
    static final float[] NIGHT_TINT = {0.20f, 0.29f, 0.52f};

    private static float[] autoTint(float hour) {
        float h = ((hour % 24f) + 24f) % 24f;
        if (h >= 8f && h < 17f) {
            return DAY_TINT.clone();
        }
        if (h >= 17f && h < 19f) {
            return mix(DAY_TINT, EVENING_TINT, (h - 17f) / 2f);
        }
        if (h >= 19f && h < 21f) {
            return mix(EVENING_TINT, NIGHT_TINT, (h - 19f) / 2f);
        }
        if (h >= 6f && h < 8f) {
            return mix(NIGHT_TINT, DAY_TINT, (h - 6f) / 2f);
        }
        return NIGHT_TINT.clone();
    }

    private static float[] mix(float[] a, float[] b, float t) {
        return new float[] {a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t};
    }

    private static <E extends Enum<E>> E parseEnum(Object value, Class<E> type, E fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.toString().trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static int parseChoice(Object value, int[] allowed, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(value.toString().trim());
            for (int a : allowed) {
                if (a == parsed) {
                    return parsed;
                }
            }
        } catch (NumberFormatException ignored) {
            // falls through to the default
        }
        return fallback;
    }

    private static float parseFloat(Object value, float min, float max, float fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            float parsed = Float.parseFloat(value.toString().trim());
            if (Float.isNaN(parsed) || parsed < min || parsed > max) {
                return fallback;
            }
            return parsed;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** A MultiSelectListPreference stores a Set; anything else falls back. Unknown names are dropped. */
    private static java.util.Set<String> parseSet(Object value, java.util.Set<String> fallback) {
        if (!(value instanceof java.util.Set)) {
            return fallback;
        }
        java.util.Set<String> out = new java.util.HashSet<>();
        for (Object o : (java.util.Set<?>) value) {
            for (String g : Species.GROUPS) {
                if (g.equals(String.valueOf(o))) {
                    out.add(g);
                }
            }
        }
        return out;
    }

    public boolean allows(String group) {
        return seaLifeAmount > 0 && seaLife.contains(group);
    }

    private static boolean parseBool(Object value, boolean fallback) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return fallback;
        }
        String s = value.toString().trim();
        if (s.equalsIgnoreCase("true")) {
            return true;
        }
        if (s.equalsIgnoreCase("false")) {
            return false;
        }
        return fallback;
    }
}
