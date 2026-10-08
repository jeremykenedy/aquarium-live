package com.jeremykenedy.aquariumlive;

import java.util.HashMap;
import java.util.Map;

/** Plain-JVM tests for the parts that do not touch Android. Run with test.sh. */
public final class SimTest {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        configDefaults();
        configParsesStoredValues();
        configRejectsBadValues();
        tintFollowsTheClock();
        randomSettingsPickFromTheirChoices();
        randomReachesEveryChoice();
        surpriseRandomisesEverythingButDisplay();
        surpriseOffKeepsChoices();
        randomSeaLifeMix();
        specksParse();
        fishCountMatchesConfig();
        onlyThemeSpecies();
        noSchoolsWhenOff();
        seaLifeFollowsToggles();
        whalesComeAndGo();
        fishStayInTheTank();
        fishTurnAround();
        schoolsStayTogether();
        tailPhaseStaysWrapped();
        plantsScaleWithDensity();
        frontPlantsKeepToTheEdges();
        bubblesPopAtTheSurface();
        noBubblesWhenOff();
        sameSeedSameTank();
        toneBands();
        toneQuantize();
        everySceneHasSoloAndSchoolingFish();
        settingsDefaultsAreRealChoices();
        tintCoversEveryHour();
        configRejectsOutOfRangeNumbers();
        renderSizeFollowsResolution();
        toneEdgeCases();
        framePacerSettlesOnThirty();
        artCachePrunesOldArt();
        simEdgeCases();
        steeringHandlesArrival();
        crowdedAndCaughtFishStayFinite();
        liftedOctopusSwimsFaster();
        strayBubblesNeedCreatures();
        whalesVisitAtEveryAmount();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void configDefaults() {
        Config c = Config.fromMap(new HashMap<String, Object>());
        check("default theme", c.theme == Config.Theme.REEF);
        check("default style is realistic", c.style == Config.Style.REALISTIC);
        check("default fish count", c.fishCount == 20);
        check("default schools", c.schools == 2);
        check("default shimmer", c.shimmer == 1);
        check("default sea life is everything", c.seaLife.size() == Species.GROUPS.length && c.seaLifeAmount == 2);
        check("default brightness", c.brightness == 1f);
        check("default resolution native", c.resolution == 0);
        check("default fps is automatic", c.fps == 0);
        check("default bubbles", c.bubbles == 2);
        check("default clock off", !c.clock);
    }

    static void configParsesStoredValues() {
        Map<String, Object> m = new HashMap<>();
        m.put(Config.THEME, "kelp_forest");
        m.put(Config.STYLE, "animated");
        m.put(Config.FISH_COUNT, "45");
        m.put(Config.SPEED, "1.4");
        m.put(Config.BUBBLES, "1");
        m.put(Config.LIGHTING, "auto");
        m.put(Config.BRIGHTNESS, "60");
        m.put(Config.RESOLUTION, "1440");
        m.put(Config.FPS, "30");
        m.put(Config.CLOCK, Boolean.TRUE);
        m.put(Config.SHIMMER, "2");
        m.put(Config.SCHOOLS, "3");
        m.put(Config.SEA_LIFE, new java.util.HashSet<>(java.util.Arrays.asList("sharks", "whales", "unicorns")));
        m.put(Config.SEA_LIFE_AMOUNT, "3");
        Config c = Config.fromMap(m);
        check("theme parsed", c.theme == Config.Theme.KELP_FOREST);
        check("style parsed", c.style == Config.Style.ANIMATED);
        for (Config.Style st : Config.Style.values()) {
            Map<String, Object> one = new HashMap<>();
            one.put(Config.STYLE, st.name().toLowerCase(java.util.Locale.ROOT));
            check("style " + st + " round trips", Config.fromMap(one).style == st);
        }
        check("fish count parsed", c.fishCount == 45);
        check("speed parsed", c.speed == 1.4f);
        check("bubbles parsed", c.bubbles == 1);
        check("lighting parsed", c.lighting == Config.Lighting.AUTO);
        check("brightness parsed", Math.abs(c.brightness - 0.6f) < 1e-6);
        check("resolution parsed", c.resolution == 1440);
        check("fps parsed", c.fps == 30);
        check("clock parsed", c.clock);
        check("shimmer parsed", c.shimmer == 2);
        check("schools parsed", c.schools == 3);
        check("sea life parsed, unknown names dropped", c.seaLife.size() == 2 && c.seaLife.contains("sharks") && c.seaLife.contains("whales"));
        check("sea life amount parsed", c.seaLifeAmount == 3);
    }

    static void configRejectsBadValues() {
        Map<String, Object> m = new HashMap<>();
        m.put(Config.THEME, "lava");
        m.put(Config.STYLE, "oil painting");
        m.put(Config.FISH_COUNT, "1000");
        m.put(Config.SPEED, "NaN");
        m.put(Config.PLANTS, "abc");
        m.put(Config.RESOLUTION, "720");
        m.put(Config.FPS, "144");
        m.put(Config.BRIGHTNESS, "5");
        m.put(Config.CLOCK, "maybe");
        m.put(Config.SHIMMER, "7");
        m.put(Config.SEA_LIFE, "sharks");
        Config c = Config.fromMap(m);
        check("unknown theme falls back", c.theme == Config.Theme.REEF);
        check("unknown style falls back", c.style == Config.Style.REALISTIC);
        check("fish count off the list falls back", c.fishCount == 20);
        check("NaN speed falls back", c.speed == 1f);
        check("garbage density falls back", c.plantDensity == 1f);
        check("resolution off the list falls back", c.resolution == 0);
        check("fps off the list falls back", c.fps == 0);
        check("brightness off the list falls back", c.brightness == 1f);
        check("unparseable boolean falls back", !c.clock);
        check("shimmer off the list falls back", c.shimmer == 1);
        check("sea life that is not a set falls back to everything", c.seaLife.size() == Species.GROUPS.length);
    }

    private static Map<String, Object> allRandom() {
        Map<String, Object> m = new HashMap<>();
        for (String k : new String[] {Config.THEME, Config.STYLE, Config.LIGHTING, Config.FISH_COUNT, Config.SCHOOLS, Config.SEA_LIFE_AMOUNT,
            Config.SHIMMER, Config.SPEED, Config.PLANTS, Config.BUBBLES, Config.PARTICLES}) {
            m.put(k, "RANDOM");
        }
        return m;
    }

    /** Sequential seeds give java.util.Random near-identical first draws, so spread them out. */
    private static java.util.Random rng(long seed) {
        return new java.util.Random(seed * 0x9E3779B97F4A7C15L ^ 0x2545F4914F6CDD1DL);
    }

    private static boolean in(int v, int[] options) {
        for (int o : options) {
            if (o == v) {
                return true;
            }
        }
        return false;
    }

    private static boolean in(float v, float[] options) {
        for (float o : options) {
            if (o == v) {
                return true;
            }
        }
        return false;
    }

    static void randomSettingsPickFromTheirChoices() {
        Map<String, Object> m = allRandom();
        boolean ok = true;
        for (int seed = 0; seed < 300; seed++) {
            Config c = Config.fromMap(m, rng(seed));
            ok &= c.theme != null && c.style != null;
            ok &= c.lighting != Config.Lighting.AUTO;
            ok &= in(c.fishCount, Config.FISH_COUNTS) && in(c.schools, Config.SCHOOL_LEVELS) && in(c.seaLifeAmount, Config.SEA_LIFE_LEVELS);
            ok &= in(c.shimmer, Config.SHIMMER_LEVELS) && in(c.bubbles, Config.BUBBLE_LEVELS);
            ok &= in(c.speed, Config.SPEEDS) && in(c.plantDensity, Config.PLANT_DENSITIES);
        }
        check("random settings stay on their own choices", ok);
        Config a = Config.fromMap(m, new java.util.Random(42));
        Config b = Config.fromMap(m, new java.util.Random(42));
        check("same seed, same random picks", a.theme == b.theme && a.style == b.style && a.fishCount == b.fishCount && a.speed == b.speed);
        Map<String, Object> lower = new HashMap<>();
        lower.put(Config.THEME, " random ");
        java.util.Set<Config.Theme> seen = java.util.EnumSet.noneOf(Config.Theme.class);
        for (int seed = 0; seed < 200; seed++) {
            seen.add(Config.fromMap(lower, rng(seed)).theme);
        }
        check("random is read in any case", seen.size() == Config.Theme.values().length);
    }

    static void randomReachesEveryChoice() {
        Map<String, Object> m = allRandom();
        java.util.Set<Object> themes = new java.util.HashSet<>();
        java.util.Set<Object> styles = new java.util.HashSet<>();
        java.util.Set<Object> lights = new java.util.HashSet<>();
        java.util.Set<Object> fish = new java.util.HashSet<>();
        java.util.Set<Object> schools = new java.util.HashSet<>();
        java.util.Set<Object> amounts = new java.util.HashSet<>();
        java.util.Set<Object> shimmer = new java.util.HashSet<>();
        java.util.Set<Object> speeds = new java.util.HashSet<>();
        java.util.Set<Object> plants = new java.util.HashSet<>();
        java.util.Set<Object> bubbles = new java.util.HashSet<>();
        java.util.Set<Object> specks = new java.util.HashSet<>();
        for (int seed = 0; seed < 500; seed++) {
            Config c = Config.fromMap(m, rng(seed));
            themes.add(c.theme);
            styles.add(c.style);
            lights.add(c.lighting);
            fish.add(c.fishCount);
            schools.add(c.schools);
            amounts.add(c.seaLifeAmount);
            shimmer.add(c.shimmer);
            speeds.add(c.speed);
            plants.add(c.plantDensity);
            bubbles.add(c.bubbles);
            specks.add(c.particles);
        }
        check("random reaches every scene", themes.size() == Config.Theme.values().length);
        check("random reaches every look", styles.size() == Config.Style.values().length);
        check("random lighting is day, evening or night", lights.size() == 3 && !lights.contains(Config.Lighting.AUTO));
        check("random reaches every fish count", fish.size() == Config.FISH_COUNTS.length);
        check("random reaches every school level", schools.size() == Config.SCHOOL_LEVELS.length);
        check("random reaches every sea life amount", amounts.size() == Config.SEA_LIFE_LEVELS.length);
        check("random reaches every shimmer level", shimmer.size() == Config.SHIMMER_LEVELS.length);
        check("random reaches every speed", speeds.size() == Config.SPEEDS.length);
        check("random reaches every plant density", plants.size() == Config.PLANT_DENSITIES.length);
        check("random reaches every bubble level", bubbles.size() == Config.BUBBLE_LEVELS.length);
        check("random specks go both ways", specks.size() == 2);
    }

    static void surpriseRandomisesEverythingButDisplay() {
        Map<String, Object> m = new HashMap<>();
        m.put(Config.SURPRISE, Boolean.TRUE);
        m.put(Config.THEME, "OCEAN");
        m.put(Config.STYLE, "RETRO");
        m.put(Config.BRIGHTNESS, "60");
        m.put(Config.RESOLUTION, "1080");
        m.put(Config.FPS, "30");
        m.put(Config.CLOCK, Boolean.TRUE);
        m.put(Config.SEA_LIFE, new java.util.HashSet<>(java.util.Arrays.asList("sharks")));
        java.util.Set<Object> themes = new java.util.HashSet<>();
        java.util.Set<Object> styles = new java.util.HashSet<>();
        java.util.Set<Object> mixes = new java.util.HashSet<>();
        boolean displayKept = true;
        for (int seed = 0; seed < 200; seed++) {
            Config c = Config.fromMap(m, rng(seed));
            themes.add(c.theme);
            styles.add(c.style);
            mixes.add(c.seaLife);
            displayKept &= Math.abs(c.brightness - 0.6f) < 1e-6 && c.resolution == 1080 && c.fps == 30 && c.clock;
        }
        check("surprise me overrides the chosen scene", themes.size() == Config.Theme.values().length);
        check("surprise me overrides the chosen look", styles.size() == Config.Style.values().length);
        check("surprise me mixes the sea life", mixes.size() > 10);
        check("surprise me leaves brightness, resolution, frame rate and clock alone", displayKept);
        Map<String, Object> asText = new HashMap<>();
        asText.put(Config.SURPRISE, "true");
        java.util.Set<Object> t = new java.util.HashSet<>();
        for (int seed = 0; seed < 100; seed++) {
            t.add(Config.fromMap(asText, rng(seed)).theme);
        }
        check("surprise me stored as text also works", t.size() > 1);
    }

    static void surpriseOffKeepsChoices() {
        Map<String, Object> m = new HashMap<>();
        m.put(Config.SURPRISE, Boolean.FALSE);
        m.put(Config.THEME, "OCEAN");
        m.put(Config.STYLE, "RANDOM");
        boolean themeKept = true;
        java.util.Set<Object> styles = new java.util.HashSet<>();
        for (int seed = 0; seed < 200; seed++) {
            Config c = Config.fromMap(m, rng(seed));
            themeKept &= c.theme == Config.Theme.OCEAN && c.fishCount == 20 && c.lighting == Config.Lighting.DAY;
            styles.add(c.style);
        }
        check("with surprise me off, fixed settings stay fixed", themeKept);
        check("with surprise me off, a setting on Random still varies", styles.size() == Config.Style.values().length);
        Config d = Config.fromMap(new HashMap<String, Object>(), new java.util.Random(1));
        check("defaults are not random", d.theme == Config.Theme.REEF && d.style == Config.Style.REALISTIC && d.seaLife.size() == Species.GROUPS.length);
    }

    static void randomSeaLifeMix() {
        Map<String, Object> m = new HashMap<>();
        m.put(Config.SEA_LIFE, new java.util.HashSet<>(java.util.Arrays.asList("sharks", "RANDOM")));
        java.util.Set<String> union = new java.util.HashSet<>();
        boolean valid = true;
        java.util.Set<Object> mixes = new java.util.HashSet<>();
        for (int seed = 0; seed < 300; seed++) {
            java.util.Set<String> s = Config.fromMap(m, rng(seed)).seaLife;
            valid &= !s.isEmpty() && java.util.Arrays.asList(Species.GROUPS).containsAll(s) && !s.contains("RANDOM");
            union.addAll(s);
            mixes.add(s);
        }
        check("a random sea life mix is never empty and only real groups", valid);
        check("a random sea life mix can include every group", union.size() == Species.GROUPS.length);
        check("a random sea life mix changes between starts", mixes.size() > 20);
        java.util.Random never = new java.util.Random() {
            @Override
            public boolean nextBoolean() {
                return false;
            }
        };
        check("a mix that drew nothing still gets one group", Config.randomMix(never).size() == 1);
    }

    static void specksParse() {
        Map<String, Object> m = new HashMap<>();
        m.put(Config.PARTICLES, "false");
        check("specks off parsed", !Config.fromMap(m).particles);
        m.put(Config.PARTICLES, "true");
        check("specks on parsed", Config.fromMap(m).particles);
        m.put(Config.PARTICLES, "sometimes");
        check("specks garbage falls back to on", Config.fromMap(m).particles);
    }

    private static String read(java.io.File f) {
        try {
            return new String(java.nio.file.Files.readAllBytes(f.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            check("could not read " + f + ": " + e.getMessage(), false);
            return "";
        }
    }

    private static java.util.List<String> array(String arrays, String name) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("<string-array name=\"" + name + "\">(.*?)</string-array>", java.util.regex.Pattern.DOTALL).matcher(arrays);
        java.util.List<String> items = new java.util.ArrayList<>();
        if (m.find()) {
            java.util.regex.Matcher item = java.util.regex.Pattern.compile("<item>(.*?)</item>").matcher(m.group(1));
            while (item.find()) {
                items.add(item.group(1));
            }
        }
        return items;
    }

    static void settingsDefaultsAreRealChoices() {
        String res = System.getProperty("res.dir", "res");
        String settings = read(new java.io.File(res, "xml/settings.xml"));
        String arrays = read(new java.io.File(res, "values/arrays.xml"));
        java.util.regex.Matcher pref = java.util.regex.Pattern.compile("<(?:MultiSelect)?ListPreference([^>]*)/>", java.util.regex.Pattern.DOTALL).matcher(settings);
        int lists = 0;
        while (pref.find()) {
            String attrs = pref.group(1);
            String key = attr(attrs, "key");
            java.util.List<String> names = array(arrays, attr(attrs, "entries").replace("@array/", ""));
            java.util.List<String> values = array(arrays, attr(attrs, "entryValues").replace("@array/", ""));
            String def = attr(attrs, "defaultValue");
            check(key + " has a name for every choice", !values.isEmpty() && names.size() == values.size());
            java.util.List<String> defaults = def.startsWith("@array/") ? array(arrays, def.replace("@array/", "")) : java.util.Collections.singletonList(def);
            check(key + " defaults to real choices", !defaults.isEmpty() && values.containsAll(defaults));
            check(key + " never defaults to Random", !defaults.contains(Config.RANDOM));
            if ("sea_life".equals(key)) {
                check("sea life defaults to every group", new java.util.HashSet<>(defaults).equals(new java.util.HashSet<>(java.util.Arrays.asList(Species.GROUPS))));
            }
            lists++;
        }
        check("every list setting was checked (" + lists + ")", lists == 15);
    }

    private static String attr(String attrs, String name) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("android:" + name + "=\"([^\"]*)\"").matcher(attrs);
        return m.find() ? m.group(1) : "";
    }

    static void everySceneHasSoloAndSchoolingFish() {
        for (Config.Theme theme : Config.Theme.values()) {
            boolean solo = false;
            boolean schooling = false;
            for (Species sp : Species.ALL) {
                if (sp.kind == Species.Kind.FISH && sp.livesIn(theme)) {
                    if (sp.schooling) {
                        schooling = true;
                    } else {
                        solo = true;
                        check(sp.id + " can be picked", sp.weight > 0);
                    }
                }
            }
            check(theme + " has solo fish", solo);
            check(theme + " has a schooling fish", schooling);
        }
    }

    static void tintCoversEveryHour() {
        check("fixed evening", same(Config.tint(Config.Lighting.EVENING, 3f), Config.EVENING_TINT));
        check("fixed day", same(Config.tint(Config.Lighting.DAY, 23f), Config.DAY_TINT));
        float[] dusk = Config.tint(Config.Lighting.AUTO, 18f);
        check("18:00 sits between day and evening", dusk[2] < Config.DAY_TINT[2] && dusk[2] > Config.EVENING_TINT[2]);
        float[] late = Config.tint(Config.Lighting.AUTO, 20f);
        check("20:00 sits between evening and night", late[0] < Config.EVENING_TINT[0] && late[0] > Config.NIGHT_TINT[0]);
        check("21:30 is night", same(Config.tint(Config.Lighting.AUTO, 21.5f), Config.NIGHT_TINT));
        check("5:00 is night", same(Config.tint(Config.Lighting.AUTO, 5f), Config.NIGHT_TINT));
        check("8:00 is day", same(Config.tint(Config.Lighting.AUTO, 8f), Config.DAY_TINT));
        check("hours wrap past midnight", same(Config.tint(Config.Lighting.AUTO, 36f), Config.DAY_TINT));
        check("negative hours wrap", same(Config.tint(Config.Lighting.AUTO, -12f), Config.DAY_TINT));
        float prev = Config.tint(Config.Lighting.AUTO, 0f)[0];
        boolean smooth = true;
        for (int i = 1; i <= 24 * 60; i++) {
            float now = Config.tint(Config.Lighting.AUTO, i / 60f)[0];
            smooth &= Math.abs(now - prev) < 0.01f;
            prev = now;
        }
        check("follow the clock never jumps from one minute to the next", smooth);
    }

    static void configRejectsOutOfRangeNumbers() {
        Map<String, Object> m = new HashMap<>();
        m.put(Config.FISH_COUNT, "lots");
        m.put(Config.SPEED, "0.1");
        m.put(Config.PLANTS, "9");
        Config c = Config.fromMap(m);
        check("a word where a number belongs falls back", c.fishCount == 20);
        check("speed below the range falls back", c.speed == 1f);
        check("density above the range falls back", c.plantDensity == 1f);
    }

    static void renderSizeFollowsResolution() {
        Config c = new Config();
        check("automatic draws at the window size", c.renderSize(3840, 2160) == null);
        c.style = Config.Style.RETRO;
        int[] retro = c.renderSize(1920, 1080);
        check("retro on automatic draws 960x540", retro != null && retro[0] == 960 && retro[1] == 540);
        c.style = Config.Style.REALISTIC;
        c.resolution = 1080;
        int[] hd = c.renderSize(3840, 2160);
        check("1080p on a 4K screen is 1920x1080", hd != null && hd[0] == 1920 && hd[1] == 1080);
        int[] portrait = c.renderSize(2160, 3840);
        check("a screen reported tall is treated as wide", portrait != null && portrait[0] == 1920 && portrait[1] == 1080);
        c.resolution = 2160;
        int[] capped = c.renderSize(1920, 1080);
        check("4K on a 1080p screen is capped at 1080", capped != null && capped[0] == 1920 && capped[1] == 1080);
        int[] wide = c.renderSize(2560, 1080);
        check("ultra-wide keeps its shape", wide != null && wide[0] == 2560 && wide[1] == 1080);
        check("an unknown screen size draws at the window size", c.renderSize(0, 0) == null);
    }

    static void toneEdgeCases() {
        check("no bands leaves brightness alone", Tone.snap(0.42f, new float[0], 0.05f) == 0.42f);
        float top = Tone.snap(0.99f, new float[] {0.3f, 0.6f}, 0.01f);
        check("above every band edge lands in the top band", Math.abs(top - (0.3f + 0.6f) * 0.5f - 0.04f) < 1e-5);
    }

    private static void runPacer(FramePacer p, double start, double seconds, float dt) {
        for (double t = start; t < start + seconds; t += dt) {
            p.frame(t, dt);
        }
    }

    static void framePacerSettlesOnThirty() {
        check("fixed 30 draws every other refresh", new FramePacer(30).halfRate());
        FramePacer sixty = new FramePacer(60);
        runPacer(sixty, 0, 20, 1f / 20f);
        check("fixed 60 never drops to 30, even when slow", !sixty.halfRate());
        FramePacer smooth = new FramePacer(0);
        runPacer(smooth, 0, 20, 1f / 60f);
        check("automatic stays at 60 when frames keep up", !smooth.halfRate());
        FramePacer slow = new FramePacer(0);
        runPacer(slow, 0, FramePacer.WARM_UP - 0.1, 1f / 20f);
        check("slow frames while art loads are ignored", !slow.halfRate());
        runPacer(slow, FramePacer.WARM_UP, FramePacer.WINDOW + 0.5, 1f / 40f);
        check("automatic drops to a steady 30 when frames fall behind", slow.halfRate());
        runPacer(slow, 30, 10, 1f / 60f);
        check("once at 30 it stays at 30", slow.halfRate());
        FramePacer idle = new FramePacer(0);
        for (int i = 0; i < 1000; i++) {
            idle.frame(10.0, 0f);
        }
        check("zero-length frames are ignored", !idle.halfRate());
        FramePacer restarted = new FramePacer(0);
        runPacer(restarted, FramePacer.WARM_UP, FramePacer.WINDOW - 0.5, 1f / 40f);
        restarted.restart();
        runPacer(restarted, 10, FramePacer.WINDOW - 0.5, 1f / 60f);
        check("restart throws away the earlier frames", !restarted.halfRate());
    }

    static void artCachePrunesOldArt() {
        try {
            java.io.File dir = java.nio.file.Files.createTempDirectory("aql-art").toFile();
            java.io.File keep = new java.io.File(dir, "v2-realistic-clownfish.png");
            java.io.File old = new java.io.File(dir, "v1-realistic-clownfish.png");
            java.io.File stray = new java.io.File(dir, "other.tmp");
            for (java.io.File f : new java.io.File[] {keep, old, stray}) {
                check("made " + f.getName(), f.createNewFile());
            }
            ArtCache.prune(dir, "v2-");
            check("art from this install is kept", keep.isFile());
            check("art from an older install is removed", !old.exists());
            check("anything else is removed", !stray.exists());
            ArtCache.prune(new java.io.File(dir, "missing"), "v2-");
            check("a missing folder is left alone", dir.isDirectory());
            java.io.File busy = new java.io.File(dir, "v1-busy");
            check("made a folder that cannot be deleted directly", busy.mkdir() && new java.io.File(busy, "inner").createNewFile());
            ArtCache.prune(dir, "v2-");
            check("a file that cannot be deleted is left for later instead of failing", busy.exists());
            ArtCache.discard(new java.io.File(busy, "inner"));
            ArtCache.discard(busy);
            ArtCache.discard(keep);
            check("discard removes files", !busy.exists() && !keep.exists());
            check("temp folder cleaned up", dir.delete());
        } catch (java.io.IOException e) {
            check("art cache test could not use the temp folder: " + e.getMessage(), false);
        }
    }

    static void simEdgeCases() {
        Config quiet = new Config();
        quiet.particles = false;
        Sim still = new Sim(quiet, 1920f, 3);
        check("no floating specks when they are off", still.motes.isEmpty());
        float x = still.creatures.get(0).x;
        still.update(0f);
        still.update(-1f);
        check("a zero or negative step changes nothing", still.creatures.get(0).x == x);

        Config bare = new Config();
        bare.plantDensity = 0f;
        bare.seaLife = new java.util.HashSet<>(java.util.Collections.singleton(Species.SEAHORSES));
        bare.seaLifeAmount = 3;
        Sim noPlants = new Sim(bare, 1920f, 4);
        boolean seahorses = false;
        boolean inside = true;
        for (Sim.Creature c : noPlants.creatures) {
            if (c.species.kind == Species.Kind.SEAHORSE) {
                seahorses = true;
                inside &= c.x >= 0f && c.x <= 1920f;
            }
        }
        check("seahorses still find a spot with no plants to hover by", seahorses && inside);

        Config tank = new Config();
        tank.theme = Config.Theme.FISH_TANK;
        check("a normal screen has one airstone", new Sim(tank, 1920f, 5).airstones.length == 1);
        Sim wide = new Sim(tank, 3000f, 5);
        check("a very wide screen has two airstones", wide.airstones.length == 2);
        check("the two airstones are apart", Math.abs(wide.airstones[0] - wide.airstones[1]) > 600f);
    }

    static void steeringHandlesArrival() {
        check("steers toward the target", Sim.toward(30f, 50f, 10f) == 6f);
        check("stops once there", Sim.toward(0f, 0f, 10f) == 0f);
        check("stops when close enough", Sim.toward(0.001f, 0.005f, 10f) == 0f);
    }

    private static boolean finite(Sim sim) {
        for (Sim.Creature c : sim.creatures) {
            if (Float.isNaN(c.x) || Float.isNaN(c.y) || Float.isInfinite(c.x) || Float.isInfinite(c.y)) {
                return false;
            }
        }
        return true;
    }

    static void crowdedAndCaughtFishStayFinite() {
        Config c = new Config();
        c.seaLife = new java.util.HashSet<>(java.util.Collections.singleton(Species.SHARKS));
        c.seaLifeAmount = 3;
        Sim sim = new Sim(c, 1920f, 8);
        Sim.Creature a = null;
        Sim.Creature b = null;
        Sim.Creature shark = null;
        for (Sim.Creature f : sim.creatures) {
            if (f.species.kind == Species.Kind.SHARK) {
                shark = f;
            } else if (f.isFish() && f.school() == null) {
                if (a == null) {
                    a = f;
                } else if (b == null) {
                    b = f;
                }
            }
        }
        check("a shark and two fish to test with", a != null && b != null && shark != null);
        if (a == null || b == null || shark == null) {
            return;
        }
        b.x = a.x;
        b.y = a.y;
        b.z = a.z;
        sim.update(1f / 30f);
        check("two fish in exactly the same spot stay finite", finite(sim));
        a.x = shark.x;
        a.y = shark.y;
        a.z = shark.z;
        sim.update(1f / 30f);
        check("a fish exactly on a shark stays finite", finite(sim));
    }

    static void liftedOctopusSwimsFaster() {
        Config c = new Config();
        c.seaLife = new java.util.HashSet<>(java.util.Collections.singleton(Species.OCTOPUSES));
        c.seaLifeAmount = 3;
        Sim sim = new Sim(c, 1920f, 9);
        Sim.Creature octopus = null;
        for (Sim.Creature f : sim.creatures) {
            if (f.species.kind == Species.Kind.OCTOPUS) {
                octopus = f;
            }
        }
        check("an octopus to test with", octopus != null);
        if (octopus == null) {
            return;
        }
        octopus.tx = octopus.x + 500f;
        octopus.timer = 100f;
        octopus.lift = 0f;
        octopus.liftTarget = 0f;
        sim.update(1f / 30f);
        float crawl = Math.abs(octopus.vx);
        octopus.tx = octopus.x + 500f;
        octopus.lift = 200f;
        octopus.liftTarget = 200f;
        sim.update(1f / 30f);
        check("an octopus off the floor swims faster than it crawls", Math.abs(octopus.vx) > crawl * 1.5f);
    }

    static void strayBubblesNeedCreatures() {
        Config c = new Config();
        c.theme = Config.Theme.FISH_TANK;
        c.fishCount = 0;
        c.schools = 0;
        c.seaLifeAmount = 0;
        Sim sim = new Sim(c, 1920f, 10);
        check("an empty tank has no creatures", sim.creatures.isEmpty());
        for (int i = 0; i < 30 * 20; i++) {
            sim.update(1f / 30f);
        }
        check("an empty tank still runs its airstone", !sim.bubbles.isEmpty());
        Config stoneOnly = new Config();
        stoneOnly.theme = Config.Theme.FISH_TANK;
        stoneOnly.bubbles = 1;
        Sim tank = new Sim(stoneOnly, 1920f, 11);
        boolean fromStone = true;
        int seen = 0;
        for (int i = 0; i < 30 * 20; i++) {
            tank.update(1f / 30f);
            for (Sim.Bubble b : tank.bubbles) {
                fromStone &= Math.abs(b.x - tank.airstones[0]) < 40f;
                seen++;
            }
        }
        check("airstone only: every bubble comes from the airstone", fromStone && seen > 0);
    }

    static void whalesVisitAtEveryAmount() {
        for (int amount = 1; amount <= 3; amount++) {
            Config c = new Config();
            c.theme = Config.Theme.KELP_FOREST;
            c.seaLife = new java.util.HashSet<>(java.util.Collections.singleton(Species.WHALES));
            c.seaLifeAmount = amount;
            Sim sim = new Sim(c, 1920f, 60 + amount);
            Sim.Creature whale = null;
            for (Sim.Creature f : sim.creatures) {
                if (f.species.kind == Species.Kind.WHALE) {
                    whale = f;
                }
            }
            check("a whale is stocked at amount " + amount, whale != null);
            if (whale == null) {
                continue;
            }
            int arrivals = 0;
            boolean was = whale.active;
            for (int i = 0; i < 30 * 60 * 15; i++) {
                sim.update(1f / 30f);
                if (whale.active && !was) {
                    arrivals++;
                }
                was = whale.active;
            }
            check("whales come back within 15 minutes at amount " + amount + " (" + arrivals + ")", arrivals >= 1);
        }
    }

    static void tintFollowsTheClock() {
        check("noon is day", same(Config.tint(Config.Lighting.AUTO, 12f), Config.DAY_TINT));
        check("midnight is night", same(Config.tint(Config.Lighting.AUTO, 0f), Config.NIGHT_TINT));
        check("3am is night", same(Config.tint(Config.Lighting.AUTO, 3f), Config.NIGHT_TINT));
        check("19:00 is evening", same(Config.tint(Config.Lighting.AUTO, 19f), Config.EVENING_TINT));
        float[] dawn = Config.tint(Config.Lighting.AUTO, 7f);
        check("dawn sits between night and day", dawn[0] > Config.NIGHT_TINT[0] && dawn[0] < Config.DAY_TINT[0]);
        check("fixed night ignores the hour", same(Config.tint(Config.Lighting.NIGHT, 12f), Config.NIGHT_TINT));
        check("tint returns a copy", Config.tint(Config.Lighting.DAY, 0f) != Config.DAY_TINT);
    }

    static int soloFish(Sim sim) {
        int n = 0;
        for (Sim.Creature f : sim.creatures) {
            if (f.isFish() && f.school() == null) {
                n++;
            }
        }
        return n;
    }

    static void fishCountMatchesConfig() {
        for (Config.Theme theme : Config.Theme.values()) {
            for (int n : Config.FISH_COUNTS) {
                for (int schools : Config.SCHOOL_LEVELS) {
                    Config c = new Config();
                    c.theme = theme;
                    c.fishCount = n;
                    c.schools = schools;
                    Sim sim = new Sim(c, 1920f, 7);
                    check(theme + " " + n + " schools=" + schools + " fish count", soloFish(sim) == n);
                    int schooled = 0;
                    for (Sim.Creature f : sim.creatures) {
                        if (f.school() != null) {
                            schooled++;
                        }
                    }
                    int expected = 0;
                    for (int size : Sim.schoolSizes(schools)) {
                        expected += size;
                    }
                    check(theme + " schools=" + schools + " school members", schooled == expected);
                }
            }
        }
    }

    static void onlyThemeSpecies() {
        for (Config.Theme theme : Config.Theme.values()) {
            Config c = new Config();
            c.theme = theme;
            c.fishCount = 45;
            c.seaLifeAmount = 3;
            Sim sim = new Sim(c, 1920f, 11);
            boolean ok = true;
            for (Sim.Creature f : sim.creatures) {
                ok &= f.species.livesIn(theme);
            }
            check(theme + " holds only its own species", ok);
        }
    }

    static void noSchoolsWhenOff() {
        Config c = new Config();
        c.schools = 0;
        Sim sim = new Sim(c, 1920f, 3);
        boolean ok = true;
        for (Sim.Creature f : sim.creatures) {
            ok &= !f.species.schooling;
        }
        check("no schooling fish when schools are off", ok);
    }

    static void seaLifeFollowsToggles() {
        Config off = new Config();
        off.theme = Config.Theme.OCEAN;
        off.seaLifeAmount = 0;
        boolean none = true;
        for (Sim.Creature f : new Sim(off, 1920f, 41).creatures) {
            none &= f.species.group == null;
        }
        check("sea life amount none adds no sea life", none);

        Config sharks = new Config();
        sharks.theme = Config.Theme.OCEAN;
        sharks.seaLife = new java.util.HashSet<>(java.util.Collections.singleton(Species.SHARKS));
        int count = 0;
        boolean onlySharks = true;
        for (Sim.Creature f : new Sim(sharks, 1920f, 43).creatures) {
            if (f.species.group != null) {
                count++;
                onlySharks &= Species.SHARKS.equals(f.species.group);
            }
        }
        check("only the ticked group appears", onlySharks && count > 0);

        Config tank = new Config();
        tank.theme = Config.Theme.FISH_TANK;
        tank.seaLifeAmount = 3;
        boolean tankClean = true;
        for (Sim.Creature f : new Sim(tank, 1920f, 47).creatures) {
            tankClean &= f.species.group == null;
        }
        check("a fish tank has no sharks or whales", tankClean);

        for (Config.Theme theme : new Config.Theme[] {Config.Theme.OCEAN, Config.Theme.REEF, Config.Theme.KELP_FOREST}) {
            Config all = new Config();
            all.theme = theme;
            all.seaLifeAmount = 3;
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (Sim.Creature f : new Sim(all, 1920f, 53).creatures) {
                if (f.species.group != null) {
                    seen.add(f.species.group);
                }
            }
            java.util.Set<String> expected = new java.util.HashSet<>();
            for (Species s : Species.ALL) {
                if (s.group != null && s.livesIn(theme)) {
                    expected.add(s.group);
                }
            }
            check(theme + " shows every group that lives there " + seen, seen.equals(expected));
        }
    }

    static void fishStayInTheTank() {
        for (Config.Theme theme : Config.Theme.values()) {
            Config c = new Config();
            c.theme = theme;
            c.fishCount = 45;
            c.schools = 3;
            c.seaLifeAmount = 3;
            c.speed = 1.4f;
            Sim sim = new Sim(c, 1920f, 5);
            boolean ok = true;
            for (int i = 0; i < 30 * 60 * 20; i++) {
                sim.update(1f / 30f);
                if (i % 30 != 0) {
                    continue;
                }
                for (Sim.Creature f : sim.creatures) {
                    ok &= !Float.isNaN(f.x) && !Float.isNaN(f.y) && !Float.isNaN(f.z);
                    ok &= f.z >= 0.04f && f.z <= 0.93f;
                    switch (f.species.motion) {
                        case VISIT:
                            break;
                        case DRIFT:
                            ok &= f.y >= Sim.yMin(f.species, f.z) - 0.5f && f.y <= Sim.yMax(f.species, f.z) + 0.5f;
                            break;
                        case CRAWL:
                            ok &= f.x >= sim.xMin(f) - 0.5f && f.x <= sim.xMax(f) + 0.5f;
                            ok &= Math.abs(f.y - Sim.floorY(f)) < 0.5f;
                            break;
                        default:
                            ok &= f.x >= sim.xMin(f) - 0.5f && f.x <= sim.xMax(f) + 0.5f;
                            ok &= f.y >= Sim.yMin(f.species, f.z) - 0.5f && f.y <= Sim.yMax(f.species, f.z) + 0.5f;
                            break;
                    }
                }
            }
            check(theme + " everything stays inside the tank for 20 minutes", ok);
        }
    }

    static void whalesComeAndGo() {
        Config c = new Config();
        c.theme = Config.Theme.OCEAN;
        c.seaLife = new java.util.HashSet<>(java.util.Collections.singleton(Species.WHALES));
        c.seaLifeAmount = 3;
        Sim sim = new Sim(c, 1920f, 59);
        Sim.Creature whale = null;
        for (Sim.Creature f : sim.creatures) {
            if (f.species.kind == Species.Kind.WHALE) {
                whale = f;
            }
        }
        check("a whale is stocked", whale != null);
        if (whale == null) {
            return;
        }
        int passes = 0;
        boolean wasActive = whale.active;
        boolean steady = true;
        float lastX = whale.x;
        for (int i = 0; i < 30 * 60 * 10; i++) {
            sim.update(1f / 30f);
            if (whale.active && wasActive) {
                steady &= Math.signum(whale.x - lastX) == Math.signum(whale.vx);
            }
            if (whale.active && !wasActive) {
                passes++;
            }
            wasActive = whale.active;
            lastX = whale.x;
        }
        check("whales pass through more than once in 10 minutes (" + passes + ")", passes >= 2);
        check("a passing whale keeps going one way", steady);
    }

    static void fishTurnAround() {
        Config c = new Config();
        c.schools = 0;
        c.seaLifeAmount = 0;
        Sim sim = new Sim(c, 1920f, 9);
        boolean[] sawLeft = new boolean[sim.creatures.size()];
        boolean[] sawRight = new boolean[sim.creatures.size()];
        for (int i = 0; i < 30 * 60 * 5; i++) {
            sim.update(1f / 30f);
            for (int j = 0; j < sim.creatures.size(); j++) {
                Sim.Creature f = sim.creatures.get(j);
                sawLeft[j] |= f.face < -0.99f;
                sawRight[j] |= f.face > 0.99f;
            }
        }
        int turned = 0;
        for (int j = 0; j < sawLeft.length; j++) {
            if (sawLeft[j] && sawRight[j]) {
                turned++;
            }
        }
        check("most fish turn around within 5 minutes (" + turned + "/" + sawLeft.length + ")", turned >= sawLeft.length * 0.8);
    }

    static void schoolsStayTogether() {
        Config c = new Config();
        c.schools = 3;
        Sim sim = new Sim(c, 1920f, 13);
        for (int i = 0; i < 30 * 30; i++) {
            sim.update(1f / 30f);
        }
        boolean ok = true;
        for (int i = 0; i < 30 * 60 * 3; i++) {
            sim.update(1f / 30f);
            if (i % 15 != 0) {
                continue;
            }
            for (Sim.Creature f : sim.creatures) {
                if (f.school() == null) {
                    continue;
                }
                float limit = f.school().radius() * 1.5f;
                ok &= Math.hypot(f.x - f.school().x, f.y - f.school().y) < limit;
            }
        }
        check("school members stay near their school", ok);
    }

    static void tailPhaseStaysWrapped() {
        Sim sim = new Sim(new Config(), 1920f, 17);
        boolean ok = true;
        for (int i = 0; i < 30 * 60 * 2; i++) {
            sim.update(1f / 30f);
            for (Sim.Creature f : sim.creatures) {
                ok &= f.tailPhase >= 0f && f.tailPhase < Sim.TWO_PI;
            }
        }
        check("tail phase stays within one turn", ok);
    }

    static void plantsScaleWithDensity() {
        Config sparse = new Config();
        sparse.plantDensity = 0.5f;
        Config lush = new Config();
        lush.plantDensity = 1.6f;
        int a = new Sim(sparse, 1920f, 1).plants.size();
        int b = new Sim(lush, 1920f, 1).plants.size();
        check("lush has more plants than sparse (" + a + " < " + b + ")", a < b);
        boolean inside = true;
        for (Sim.Plant p : new Sim(lush, 1920f, 1).plants) {
            inside &= p.x >= 0f && p.x <= 1920f && p.z >= 0f && p.z <= 1f && p.height > 0f;
        }
        check("every plant is rooted inside the tank", inside);
    }

    static void frontPlantsKeepToTheEdges() {
        for (Config.Theme theme : Config.Theme.values()) {
            Config c = new Config();
            c.theme = theme;
            c.plantDensity = 1.6f;
            Sim sim = new Sim(c, 1920f, 21);
            boolean ok = true;
            for (Sim.Plant p : sim.plants) {
                if (p.z < 0.31f && p.height > 200f) {
                    ok &= p.x <= 1920f * 0.2f || p.x >= 1920f * 0.8f;
                }
            }
            check(theme + " tall front plants keep the middle clear", ok);
        }
    }

    static void bubblesPopAtTheSurface() {
        Sim sim = new Sim(new Config(), 1920f, 23);
        int peak = 0;
        boolean below = true;
        for (int i = 0; i < 30 * 120; i++) {
            sim.update(1f / 30f);
            peak = Math.max(peak, sim.bubbles.size());
            for (Sim.Bubble b : sim.bubbles) {
                below &= b.y <= Sim.H - 30f;
            }
        }
        check("bubbles are removed at the surface", below);
        check("bubble count stays bounded (" + peak + ")", peak > 0 && peak < 400);
    }

    static void noBubblesWhenOff() {
        Config c = new Config();
        c.bubbles = 0;
        Sim sim = new Sim(c, 1920f, 29);
        for (int i = 0; i < 300; i++) {
            sim.update(1f / 30f);
        }
        check("no bubbles when bubbles are off", sim.bubbles.isEmpty());
    }

    static void sameSeedSameTank() {
        Sim a = new Sim(new Config(), 1920f, 31);
        Sim b = new Sim(new Config(), 1920f, 31);
        for (int i = 0; i < 600; i++) {
            a.update(1f / 30f);
            b.update(1f / 30f);
        }
        boolean ok = a.creatures.size() == b.creatures.size();
        for (int i = 0; ok && i < a.creatures.size(); i++) {
            ok = a.creatures.get(i).x == b.creatures.get(i).x && a.creatures.get(i).y == b.creatures.get(i).y;
        }
        check("same seed gives the same tank", ok);
    }

    static void toneBands() {
        float[] levels = {0.28f, 0.55f, 0.82f, 1f};
        check("darkest band leaves blacks alone", Tone.snap(0.05f, levels, 0.04f) == 0.05f);
        check("one band is one flat value", Tone.snap(0.62f, levels, 0.04f) == Tone.snap(0.74f, levels, 0.04f));
        check("bands step up", Tone.snap(0.4f, levels, 0.04f) < Tone.snap(0.68f, levels, 0.04f));
        boolean smooth = true;
        for (float edge : new float[] {0.28f, 0.55f, 0.82f}) {
            smooth &= Math.abs(Tone.snap(edge - 0.0005f, levels, 0.04f) - Tone.snap(edge + 0.0005f, levels, 0.04f)) < 0.02f;
        }
        check("band edges blend instead of jumping", smooth);
    }

    static void toneQuantize() {
        check("quantize keeps black", Tone.quantize(0, 6) == 0);
        check("quantize keeps white", Tone.quantize(255, 6) == 255);
        check("quantize snaps to six levels", Tone.quantize(128, 6) == 153 && Tone.quantize(20, 6) == 0 && Tone.quantize(40, 6) == 51);
    }

    private static boolean same(float[] a, float[] b) {
        for (int i = 0; i < 3; i++) {
            if (Math.abs(a[i] - b[i]) > 1e-5) {
                return false;
            }
        }
        return true;
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }
}
