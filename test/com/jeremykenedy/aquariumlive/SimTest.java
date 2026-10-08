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
