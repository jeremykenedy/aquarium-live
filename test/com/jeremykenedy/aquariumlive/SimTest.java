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
