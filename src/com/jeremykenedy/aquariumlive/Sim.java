package com.jeremykenedy.aquariumlive;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The tank's state and motion, with no Android or GL code so it can be
 * tested on a plain JVM. The tank is {@link #H} world units tall and
 * {@link #w} wide; y points up and z runs from the front glass (0) to the
 * back wall (1).
 */
public final class Sim {
    public static final float H = 1080f;
    static final float TWO_PI = (float) (Math.PI * 2);
    static final float SURFACE_MARGIN = 54f;

    public static final PlantKind LIVE_ROCK = new PlantKind("live_rock", 512, 256, 3);
    public static final PlantKind BOULDER = new PlantKind("boulder", 512, 256, 3);
    public static final PlantKind STAGHORN = new PlantKind("staghorn", 512, 512, 2);
    public static final PlantKind BRAIN = new PlantKind("brain", 512, 256, 2);
    public static final PlantKind SEA_FAN = new PlantKind("sea_fan", 512, 512, 2);
    public static final PlantKind ANEMONE = new PlantKind("anemone", 512, 512, 2);
    public static final PlantKind SEAGRASS = new PlantKind("seagrass", 256, 512, 2);
    public static final PlantKind KELP = new PlantKind("kelp", 256, 2048, 3);
    public static final PlantKind STONES = new PlantKind("stones", 512, 256, 3);
    public static final PlantKind DRIFTWOOD = new PlantKind("driftwood", 512, 512, 2);
    public static final PlantKind VALLIS = new PlantKind("vallis", 256, 512, 3);
    public static final PlantKind SWORD = new PlantKind("sword", 512, 512, 2);
    public static final PlantKind RED_STEM = new PlantKind("red_stem", 256, 512, 2);

    public static final PlantKind[] PLANT_KINDS = {
        LIVE_ROCK, BOULDER, STAGHORN, BRAIN, SEA_FAN, ANEMONE, SEAGRASS, KELP, STONES, DRIFTWOOD, VALLIS, SWORD, RED_STEM,
    };

    static final Placement[] OCEAN_LAYOUT = {
        new Placement(BOULDER, 3f, 0.6f, 0.97f, 120f, 230f, 0f, false),
        new Placement(BOULDER, 1f, 0.2f, 0.5f, 90f, 150f, 0f, false),
        new Placement(SEAGRASS, 3f, 0.7f, 0.98f, 120f, 200f, 12f, false),
    };

    static final Placement[] REEF_LAYOUT = {
        new Placement(LIVE_ROCK, 4f, 0.62f, 0.97f, 190f, 330f, 0f, false),
        new Placement(SEA_FAN, 3f, 0.66f, 0.95f, 260f, 400f, 4f, false),
        new Placement(STAGHORN, 4f, 0.35f, 0.9f, 170f, 280f, 0f, false),
        new Placement(BRAIN, 3f, 0.2f, 0.75f, 80f, 135f, 0f, false),
        new Placement(ANEMONE, 2f, 0.15f, 0.6f, 140f, 210f, 8f, false),
        new Placement(SEAGRASS, 3f, 0.7f, 0.98f, 220f, 320f, 14f, false),
        new Placement(SEAGRASS, 5f, 0.02f, 0.3f, 240f, 360f, 22f, true),
    };

    static final Placement[] KELP_LAYOUT = {
        new Placement(KELP, 8f, 0.55f, 0.97f, 950f, 1200f, 40f, false),
        new Placement(BOULDER, 4f, 0.45f, 0.95f, 130f, 240f, 0f, false),
        new Placement(KELP, 2f, 0.32f, 0.55f, 1000f, 1250f, 44f, false),
        new Placement(STONES, 2f, 0.15f, 0.5f, 80f, 140f, 0f, false),
        new Placement(KELP, 2f, 0.02f, 0.22f, 1100f, 1350f, 50f, true),
    };

    static final Placement[] TANK_LAYOUT = {
        new Placement(VALLIS, 11f, 0.68f, 0.98f, 430f, 700f, 26f, false),
        new Placement(DRIFTWOOD, 1f, 0.45f, 0.8f, 300f, 420f, 0f, false),
        new Placement(RED_STEM, 4f, 0.5f, 0.9f, 300f, 460f, 11f, false),
        new Placement(STONES, 3f, 0.3f, 0.9f, 80f, 150f, 0f, false),
        new Placement(SWORD, 3f, 0.25f, 0.7f, 220f, 330f, 6f, false),
        new Placement(VALLIS, 5f, 0.02f, 0.3f, 300f, 520f, 30f, true),
    };

    public final float w;
    public final Config cfg;
    public final List<Creature> creatures = new ArrayList<>();
    public final List<Plant> plants = new ArrayList<>();
    public final List<Bubble> bubbles = new ArrayList<>();
    public final List<Mote> motes = new ArrayList<>();
    final List<School> schools = new ArrayList<>();
    private final List<Creature> predators = new ArrayList<>();
    private Creature[] fishArray = new Creature[0];
    private Creature[] predatorArray = new Creature[0];
    final float[] airstones;
    private final Random rnd;
    private float strayTimer;
    private final float[] emitTimers;
    private float time;

    /** Where the sea floor meets an object standing at depth z. */
    public static float rootY(float z) {
        return 24f + 176f * z;
    }

    /** On-screen size multiplier for depth z. */
    public static float scale(float z) {
        return 1f - 0.55f * z;
    }

    public static final class Creature {
        public final Species species;
        public float x;
        public float y;
        public float z;
        public float vx;
        public float vy;
        float vz;
        /** Horizontal sprite scale: 1 facing right, -1 facing left, near 0 mid turn. */
        public float face = 1f;
        float faceAngle;
        /** Animation phase: tail beat, fluke kick, wing flap, bell pulse. */
        public float tailPhase;
        public float tilt;
        /** Octopus lift off the floor, in world units. */
        public float lift;
        /** False while a visiting whale is off screen between passes. */
        public boolean active = true;
        float tx;
        float ty;
        float tz;
        float timer;
        float wait;
        float pace = 1f;
        float paceTarget = 1f;
        float paceTimer;
        float liftTarget;
        float anchorX;
        final School school;
        final float ox;
        final float oy;
        final float oz;
        final float drift;

        Creature(Species species, School school, Random rnd) {
            this.species = species;
            this.school = school;
            this.ox = rnd.nextFloat() * 2f - 1f;
            this.oy = rnd.nextFloat() * 2f - 1f;
            this.oz = rnd.nextFloat() * 2f - 1f;
            this.drift = rnd.nextFloat() * TWO_PI;
            this.tailPhase = rnd.nextFloat() * TWO_PI;
        }

        public boolean isFish() {
            return species.kind == Species.Kind.FISH;
        }

        public School school() {
            return school;
        }
    }

    public static final class School {
        final Species species;
        final int size;
        float x;
        float y;
        float z;
        float vx;
        float vy;
        float tx;
        float ty;
        float tz;
        float timer;

        School(Species species, int size) {
            this.species = species;
            this.size = size;
        }

        /** Spread of the school, in world units at the front glass. */
        float radius() {
            return species.length * (1.4f + 0.75f * (float) Math.sqrt(size));
        }
    }

    public static final class Bubble {
        public float x;
        public float y;
        public float z;
        public float r;
        float vy;
        float phase;
        float wobble;
    }

    public static final class Mote {
        public float x;
        public float y;
        public float z;
        public float size;
        public float alpha;
        float vx;
        float vy;
        float phase;
    }

    public static final class Plant {
        public final PlantKind kind;
        public final int variant;
        public final float x;
        public final float z;
        public final float height;
        public final float width;
        public final float sway;
        public final float freq;
        public final float phase;
        public final boolean flip;

        Plant(PlantKind kind, int variant, float x, float z, float height, float sway, float freq, float phase, boolean flip) {
            this.kind = kind;
            this.variant = variant;
            this.x = x;
            this.z = z;
            this.height = height;
            this.width = height * kind.texW / kind.texH;
            this.sway = sway;
            this.freq = freq;
            this.phase = phase;
            this.flip = flip;
        }
    }

    /** One kind of plant, coral, rock or wood. */
    public static final class PlantKind {
        public final String id;
        public final int texW;
        public final int texH;
        public final int variants;

        PlantKind(String id, int texW, int texH, int variants) {
            this.id = id;
            this.texW = texW;
            this.texH = texH;
            this.variants = variants;
        }
    }

    /** A placement rule: how many of a kind, at what depth and size. */
    static final class Placement {
        final PlantKind kind;
        final float perScreen;
        final float zMin;
        final float zMax;
        final float hMin;
        final float hMax;
        final float sway;
        final boolean edgesOnly;

        Placement(PlantKind kind, float perScreen, float zMin, float zMax, float hMin, float hMax, float sway, boolean edgesOnly) {
            this.kind = kind;
            this.perScreen = perScreen;
            this.zMin = zMin;
            this.zMax = zMax;
            this.hMin = hMin;
            this.hMax = hMax;
            this.sway = sway;
            this.edgesOnly = edgesOnly;
        }
    }

    /** How many of each sea-life group to add, by amount (none, a little, some, lots). */
    static int seaLifeCount(String group, int amount) {
        int[] counts;
        switch (group) {
            case Species.SHARKS:
                counts = new int[] {0, 1, 2, 3};
                break;
            case Species.WHALES:
                counts = new int[] {0, 1, 1, 2};
                break;
            case Species.DOLPHINS:
                counts = new int[] {0, 2, 3, 5};
                break;
            case Species.JELLYFISH:
                counts = new int[] {0, 2, 4, 8};
                break;
            case Species.SEAHORSES:
            case Species.CRABS:
                counts = new int[] {0, 1, 2, 3};
                break;
            default:
                counts = new int[] {0, 1, 1, 2};
                break;
        }
        return counts[Math.max(0, Math.min(3, amount))];
    }

    /** School sizes for each schools setting. */
    static int[] schoolSizes(int level) {
        switch (level) {
            case 1:
                return new int[] {14};
            case 2:
                return new int[] {14, 18};
            case 3:
                return new int[] {28, 26, 24};
            default:
                return new int[0];
        }
    }

    public Sim(Config cfg, float width, long seed) {
        this.cfg = cfg;
        this.w = width;
        this.rnd = new Random(seed);
        layoutPlants();
        stockFish();
        stockSchools();
        stockSeaLife();
        List<Creature> fishOnly = new ArrayList<>();
        for (Creature c : creatures) {
            if (c.isFish()) {
                fishOnly.add(c);
            }
        }
        fishArray = fishOnly.toArray(new Creature[0]);
        predatorArray = predators.toArray(new Creature[0]);
        airstones = cfg.bubbles > 0 && cfg.theme != Config.Theme.OCEAN ? placeAirstones() : new float[0];
        emitTimers = new float[airstones.length];
        if (cfg.particles) {
            int count = Math.round(150f * w / 1920f);
            for (int i = 0; i < count; i++) {
                motes.add(newMote());
            }
        }
    }

    static Placement[] layout(Config.Theme theme) {
        switch (theme) {
            case OCEAN:
                return OCEAN_LAYOUT;
            case KELP_FOREST:
                return KELP_LAYOUT;
            case FISH_TANK:
                return TANK_LAYOUT;
            default:
                return REEF_LAYOUT;
        }
    }

    private void layoutPlants() {
        float screens = w / 1920f;
        for (Placement p : layout(cfg.theme)) {
            int count = Math.max(0, Math.round(p.perScreen * screens * cfg.plantDensity));
            for (int i = 0; i < count; i++) {
                float x;
                if (p.edgesOnly) {
                    float side = w * 0.2f;
                    x = (i % 2 == 0) ? rnd.nextFloat() * side : w - rnd.nextFloat() * side;
                } else {
                    x = (i + 0.15f + rnd.nextFloat() * 0.7f) / count * w;
                }
                float z = p.zMin + rnd.nextFloat() * (p.zMax - p.zMin);
                float h = p.hMin + rnd.nextFloat() * (p.hMax - p.hMin);
                int variant = rnd.nextInt(p.kind.variants);
                float freq = 0.12f + rnd.nextFloat() * 0.1f;
                plants.add(new Plant(p.kind, variant, x, z, h, p.sway, freq, rnd.nextFloat() * TWO_PI, rnd.nextBoolean()));
            }
        }
    }

    private void stockFish() {
        List<Species> solo = new ArrayList<>();
        for (Species s : Species.ALL) {
            if (s.kind == Species.Kind.FISH && !s.schooling && s.livesIn(cfg.theme)) {
                solo.add(s);
            }
        }
        int total = cfg.fishCount;
        int cap = Math.max(2, (total + solo.size() - 1) / solo.size() + 1);
        int[] picked = new int[solo.size()];
        for (int i = 0; i < total; i++) {
            int idx = pickWeighted(solo, picked, cap);
            picked[idx]++;
            Creature f = new Creature(solo.get(idx), null, rnd);
            f.z = f.species.zMin + rnd.nextFloat() * (f.species.zMax - f.species.zMin);
            f.x = xMin(f) + rnd.nextFloat() * (xMax(f) - xMin(f));
            f.y = yMin(f.species, f.z) + rnd.nextFloat() * (yMax(f.species, f.z) - yMin(f.species, f.z));
            pickTarget(f);
            faceToward(f, f.tx);
            f.paceTimer = rnd.nextFloat() * 4f;
            creatures.add(f);
        }
    }

    private void stockSchools() {
        List<Species> schoolers = new ArrayList<>();
        for (Species s : Species.ALL) {
            if (s.schooling && s.livesIn(cfg.theme)) {
                schoolers.add(s);
            }
        }
        int[] sizes = schoolSizes(cfg.schools);
        for (int g = 0; g < sizes.length; g++) {
            School school = new School(schoolers.get(g % schoolers.size()), sizes[g]);
            school.z = 0.2f + rnd.nextFloat() * 0.55f;
            school.x = w * (0.2f + 0.6f * rnd.nextFloat());
            school.y = (yMin(school.species, school.z) + yMax(school.species, school.z)) * 0.5f;
            pickSchoolTarget(school);
            schools.add(school);
            for (int i = 0; i < sizes[g]; i++) {
                Creature f = new Creature(school.species, school, rnd);
                f.z = clampZ(school.z + f.oz * 0.06f);
                f.x = clamp(school.x + f.ox * school.radius() * scale(f.z) * 0.5f, xMin(f), xMax(f));
                f.y = clamp(school.y + f.oy * school.radius() * scale(f.z) * 0.3f, yMin(f.species, f.z), yMax(f.species, f.z));
                faceToward(f, school.tx);
                creatures.add(f);
            }
        }
    }

    private void stockSeaLife() {
        for (String group : Species.GROUPS) {
            if (!cfg.allows(group)) {
                continue;
            }
            List<Species> members = new ArrayList<>();
            for (Species s : Species.ALL) {
                if (group.equals(s.group) && s.livesIn(cfg.theme)) {
                    members.add(s);
                }
            }
            if (members.isEmpty()) {
                continue;
            }
            int perSpecies = seaLifeCount(group, cfg.seaLifeAmount);
            for (Species s : members) {
                int n = s.kind == Species.Kind.STARFISH ? Math.max(1, perSpecies) : perSpecies;
                if (members.size() > 1 && s.kind != Species.Kind.STARFISH && s.kind != Species.Kind.CRAB) {
                    n = Math.max(1, (perSpecies + 1) / members.size());
                }
                for (int i = 0; i < n; i++) {
                    creatures.add(spawn(s, i));
                }
            }
        }
        for (Creature c : creatures) {
            if (c.species.isPredator()) {
                predators.add(c);
            }
        }
    }

    private Creature spawn(Species s, int index) {
        Creature c = new Creature(s, null, rnd);
        c.z = s.zMin + rnd.nextFloat() * (s.zMax - s.zMin);
        switch (s.motion) {
            case VISIT:
                c.active = false;
                c.wait = 6f + index * 25f + rnd.nextFloat() * 10f;
                c.x = -10000f;
                c.y = H * 0.5f;
                break;
            case CRAWL:
                c.x = xMin(c) + rnd.nextFloat() * (xMax(c) - xMin(c));
                c.y = floorY(c);
                c.tx = c.x;
                c.timer = rnd.nextFloat() * 4f;
                c.face = 1f;
                break;
            case HOVER:
                c.anchorX = anchorNearPlants();
                c.x = clamp(c.anchorX, xMin(c), xMax(c));
                c.y = rootY(c.z) + 120f + rnd.nextFloat() * 160f;
                c.tx = c.x;
                c.ty = c.y;
                break;
            case DRIFT:
                c.x = rnd.nextFloat() * w;
                c.y = yMin(s, c.z) + rnd.nextFloat() * (yMax(s, c.z) - yMin(s, c.z));
                c.tx = (rnd.nextFloat() - 0.5f) * 16f;
                c.timer = 10f + rnd.nextFloat() * 20f;
                break;
            default:
                c.x = xMin(c) + rnd.nextFloat() * (xMax(c) - xMin(c));
                c.y = yMin(s, c.z) + rnd.nextFloat() * (yMax(s, c.z) - yMin(s, c.z));
                pickTarget(c);
                faceToward(c, c.tx);
                c.paceTimer = rnd.nextFloat() * 4f;
                break;
        }
        return c;
    }

    private float anchorNearPlants() {
        List<Plant> near = new ArrayList<>();
        for (Plant p : plants) {
            if (p.z < 0.7f && p.sway > 0f) {
                near.add(p);
            }
        }
        if (near.isEmpty()) {
            return w * (0.15f + rnd.nextFloat() * 0.7f);
        }
        return near.get(rnd.nextInt(near.size())).x + (rnd.nextFloat() - 0.5f) * 80f;
    }

    /** Velocity along one axis toward a target d away on that axis and dist away in all, at speed want; zero once there. */
    static float toward(float d, float dist, float want) {
        return dist > 0.01f ? d / dist * want : 0f;
    }

    private void faceToward(Creature c, float targetX) {
        c.faceAngle = targetX >= c.x ? 0f : (float) Math.PI;
        c.face = (float) Math.cos(c.faceAngle);
    }

    private int pickWeighted(List<Species> list, int[] picked, int cap) {
        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            if (picked[i] < cap) {
                sum += list.get(i).weight;
            }
        }
        int roll = rnd.nextInt(sum);
        int i = -1;
        while (roll >= 0) {
            i++;
            if (picked[i] < cap) {
                roll -= list.get(i).weight;
            }
        }
        return i;
    }

    private float[] placeAirstones() {
        int n = w > 2400f ? 2 : 1;
        float[] xs = new float[n];
        for (int i = 0; i < n; i++) {
            float lo = n == 1 ? 0.62f : (i == 0 ? 0.12f : 0.7f);
            xs[i] = w * (lo + rnd.nextFloat() * 0.18f);
        }
        return xs;
    }

    float xMin(Creature c) {
        float half = c.species.length * scale(c.z) * 0.5f;
        return c.species.motion == Species.Motion.ROAM ? -half * 0.6f : half + 8f;
    }

    float xMax(Creature c) {
        return w - xMin(c);
    }

    static float yMin(Species s, float z) {
        return rootY(z) + s.height() * scale(z) * 0.36f + 10f;
    }

    static float yMax(Species s, float z) {
        return H - SURFACE_MARGIN - s.height() * scale(z) * 0.36f;
    }

    /** Resting height for something on the sea floor: feet on the sand. */
    static float floorY(Creature c) {
        return rootY(c.z) + c.species.height() * scale(c.z) * 0.42f + c.lift * scale(c.z);
    }

    static float clampZ(float z) {
        return clamp(z, 0.04f, 0.93f);
    }

    static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private void pickTarget(Creature f) {
        Species s = f.species;
        f.tz = clamp(f.z + (rnd.nextFloat() - 0.5f) * 0.5f, s.zMin, s.zMax);
        float sc = scale(f.tz);
        float half = s.length * sc * 0.5f;
        float lo = s.motion == Species.Motion.ROAM ? -half * 0.6f : half + 8f;
        f.tx = lo + rnd.nextFloat() * (w - 2f * lo);
        float ylo = yMin(s, f.tz);
        float yhi = yMax(s, f.tz);
        float mid = clamp(f.y, ylo, yhi);
        f.ty = clamp(mid + (rnd.nextFloat() - 0.5f) * 420f, ylo, yhi);
        f.timer = 7f + rnd.nextFloat() * 9f;
    }

    private void pickSchoolTarget(School s) {
        s.tz = clampZ(0.15f + rnd.nextFloat() * 0.7f);
        float edge = s.species.length + s.radius() * 0.6f;
        s.tx = edge + rnd.nextFloat() * Math.max(1f, w - 2f * edge);
        float lo = yMin(s.species, s.tz) + 60f;
        float hi = yMax(s.species, s.tz) - 60f;
        s.ty = clamp(H * (0.3f + rnd.nextFloat() * 0.45f), lo, Math.max(lo, hi));
        s.timer = 9f + rnd.nextFloat() * 8f;
    }

    public void update(float dt) {
        if (dt <= 0f) {
            return;
        }
        time += dt;
        for (int i = 0; i < schools.size(); i++) {
            updateSchool(schools.get(i), dt);
        }
        for (int i = 0; i < creatures.size(); i++) {
            Creature c = creatures.get(i);
            switch (c.species.motion) {
                case VISIT:
                    updateVisit(c, dt);
                    break;
                case DRIFT:
                    updateDrift(c, dt);
                    break;
                case CRAWL:
                    updateCrawl(c, dt);
                    break;
                case HOVER:
                    updateHover(c, dt);
                    break;
                default:
                    updateSwim(c, dt);
                    break;
            }
        }
        if (airstones.length > 0 || cfg.bubbles >= 2) {
            updateBubbles(dt);
        }
        for (int i = 0; i < motes.size(); i++) {
            updateMote(motes.get(i), dt);
        }
    }

    private void advancePhase(Creature c, float dt, float effort) {
        c.tailPhase += dt * TWO_PI * c.species.tailFreq * (0.45f + 0.75f * effort);
        if (c.tailPhase >= TWO_PI) {
            c.tailPhase %= TWO_PI;
        }
    }

    private void turn(Creature c, float dt, float threshold) {
        float target = c.vx > threshold ? 0f : (c.vx < -threshold ? (float) Math.PI : (c.faceAngle < Math.PI / 2 ? 0f : (float) Math.PI));
        float seconds = 0.6f + c.species.length / 700f;
        float rate = (float) Math.PI / seconds * dt;
        if (Math.abs(target - c.faceAngle) <= rate) {
            c.faceAngle = target;
        } else {
            c.faceAngle += Math.signum(target - c.faceAngle) * rate;
        }
        c.face = (float) Math.cos(c.faceAngle);
    }

    private void updateSchool(School s, float dt) {
        s.timer -= dt;
        float dx = s.tx - s.x;
        float dy = s.ty - s.y;
        float dist = (float) Math.hypot(dx, dy);
        if (s.timer <= 0f || dist < 40f) {
            pickSchoolTarget(s);
            dx = s.tx - s.x;
            dy = s.ty - s.y;
            dist = (float) Math.hypot(dx, dy);
        }
        float speed = s.species.speed * cfg.speed * scale(s.z) * 0.85f;
        float want = Math.min(speed, dist * 0.6f);
        float dvx = toward(dx, dist, want);
        float dvy = toward(dy, dist, want) * 0.5f;
        float k = Math.min(1f, dt * 0.6f);
        s.vx += (dvx - s.vx) * k;
        s.vy += (dvy - s.vy) * k;
        s.x += s.vx * dt;
        s.y += s.vy * dt;
        s.z += (s.tz - s.z) * Math.min(1f, dt * 0.05f);
    }

    private void updateSwim(Creature f, float dt) {
        Species sp = f.species;
        float sc = scale(f.z);
        float maxSpeed;
        if (f.school != null) {
            School s = f.school;
            float r = s.radius() * sc;
            float breathe = 0.8f + 0.2f * (float) Math.sin(time * 0.35f + f.drift);
            f.tx = s.x + f.ox * r * 0.5f * breathe;
            f.ty = s.y + f.oy * r * 0.28f * breathe;
            f.tz = clampZ(s.z + f.oz * 0.07f);
            float lag = (float) Math.hypot(f.tx - f.x, f.ty - f.y);
            maxSpeed = sp.speed * cfg.speed * sc * (0.8f + Math.min(1.2f, lag / (sp.length * 2f)));
        } else {
            f.timer -= dt;
            f.paceTimer -= dt;
            if (f.paceTimer <= 0f) {
                f.paceTarget = rnd.nextFloat() < 0.25f ? 0.3f : 0.75f + rnd.nextFloat() * 0.5f;
                f.paceTimer = 3f + rnd.nextFloat() * 6f;
            }
            f.pace += (f.paceTarget - f.pace) * Math.min(1f, dt * 0.7f);
            float near = sp.length * sc * 0.6f;
            if (f.timer <= 0f || Math.abs(f.tx - f.x) + Math.abs(f.ty - f.y) < near) {
                pickTarget(f);
            }
            maxSpeed = sp.speed * cfg.speed * sc * f.pace;
        }

        float dx = f.tx - f.x;
        float dy = f.ty - f.y;
        float dist = (float) Math.hypot(dx, dy);
        float want = Math.min(maxSpeed, dist * 0.9f);
        float dvx = toward(dx, dist, want);
        float dvy = toward(dy, dist, want) * 0.6f;

        if (f.isFish()) {
            // Keep a little room from neighbours swimming at a similar depth.
            float room = sp.length * sc * 0.55f;
            Creature[] all = fishArray;
            for (int i = 0; i < all.length; i++) {
                Creature o = all[i];
                if (o == f || Math.abs(o.z - f.z) > 0.12f) {
                    continue;
                }
                float ex = f.x - o.x;
                float ey = f.y - o.y;
                float d2 = ex * ex + ey * ey;
                if (d2 < room * room && d2 > 0.0001f) {
                    float d = (float) Math.sqrt(d2);
                    float push = (room - d) / room * maxSpeed;
                    dvx += ex / d * push * 0.5f;
                    dvy += ey / d * push;
                }
            }
            // Small fish give sharks and dolphins a wide berth.
            for (int i = 0; i < predatorArray.length; i++) {
                Creature p = predatorArray[i];
                if (Math.abs(p.z - f.z) > 0.25f) {
                    continue;
                }
                float ex = f.x - p.x;
                float ey = f.y - p.y;
                float reach = p.species.length * scale(p.z) * 0.8f;
                float d = (float) Math.hypot(ex, ey);
                if (d < reach && d > 0.01f) {
                    float push = (reach - d) / reach * sp.speed * cfg.speed * sc * 2.2f;
                    dvx += ex / d * push;
                    dvy += ey / d * push;
                }
            }
        }

        float k = Math.min(1f, dt * (f.school != null ? 2.2f : (f.isFish() ? 1.3f : 0.5f)));
        f.vx += (dvx - f.vx) * k;
        f.vy += (dvy - f.vy) * k;
        f.vz = (f.tz - f.z) * 0.06f;

        f.x += f.vx * dt;
        f.y += f.vy * dt;
        f.z = clamp(f.z + f.vz * dt, sp.zMin, sp.zMax);
        f.z = clampZ(f.z);

        float lo = xMin(f);
        float hi = xMax(f);
        if (f.x < lo) {
            f.x = lo;
            f.vx = Math.max(0f, f.vx);
        } else if (f.x > hi) {
            f.x = hi;
            f.vx = Math.min(0f, f.vx);
        }
        float ylo = yMin(sp, f.z);
        float yhi = yMax(sp, f.z);
        if (f.y < ylo) {
            f.y = ylo;
            f.vy = Math.max(0f, f.vy);
        } else if (f.y > yhi) {
            f.y = yhi;
            f.vy = Math.min(0f, f.vy);
        }

        turn(f, dt, 3f * sc);
        float tiltTarget = clamp((float) Math.atan2(f.vy, Math.abs(f.vx) + maxSpeed * 0.5f + 1f), -0.4f, 0.4f);
        if (sp.kind == Species.Kind.RAY) {
            tiltTarget *= 0.3f;
        }
        f.tilt += (tiltTarget - f.tilt) * Math.min(1f, dt * 2.5f);
        advancePhase(f, dt, Math.min(1.6f, (float) Math.hypot(f.vx, f.vy) / Math.max(1f, sp.speed * sc)));
    }

    private float visitWait() {
        switch (cfg.seaLifeAmount) {
            case 3:
                return 12f + rnd.nextFloat() * 23f;
            case 2:
                return 30f + rnd.nextFloat() * 40f;
            default:
                return 60f + rnd.nextFloat() * 60f;
        }
    }

    private void updateVisit(Creature c, float dt) {
        Species s = c.species;
        if (!c.active) {
            c.wait -= dt;
            if (c.wait > 0f) {
                return;
            }
            c.z = s.zMin + rnd.nextFloat() * (s.zMax - s.zMin);
            float sc = scale(c.z);
            float half = s.length * sc * 0.5f + 20f;
            boolean right = rnd.nextBoolean();
            c.x = right ? -half : w + half;
            float lo = rootY(c.z) + s.height() * sc * 0.5f;
            float hi = H - s.height() * sc * 0.5f - SURFACE_MARGIN;
            c.y = lo + (Math.max(lo, hi) - lo) * (0.3f + rnd.nextFloat() * 0.5f);
            c.vx = (right ? 1f : -1f) * s.speed * cfg.speed * sc;
            c.vy = 0f;
            faceToward(c, right ? w : 0f);
            c.active = true;
        }
        float sc = scale(c.z);
        float half = s.length * sc * 0.5f + 20f;
        c.x += c.vx * dt;
        c.vy = (float) Math.sin(time * 0.12f + c.drift) * 10f * sc;
        c.y += c.vy * dt;
        c.tilt = (float) Math.atan2(c.vy, Math.abs(c.vx) + 1f) * 0.6f;
        advancePhase(c, dt, 1f);
        if ((c.vx > 0f && c.x > w + half) || (c.vx < 0f && c.x < -half)) {
            c.active = false;
            c.wait = visitWait();
            c.x = -10000f;
        }
    }

    private void updateDrift(Creature c, float dt) {
        Species s = c.species;
        float sc = scale(c.z);
        advancePhase(c, dt, 0.7f);
        float beat = c.tailPhase / TWO_PI;
        float thrust = beat < 0.35f ? (float) Math.sin(beat / 0.35f * Math.PI) : 0f;
        float lo = yMin(s, c.z);
        float hi = yMax(s, c.z);
        float up = thrust * 58f * sc - 10f * sc;
        if (c.y > hi - 60f * sc) {
            up = Math.min(up, -8f * sc);
        } else if (c.y < lo + 80f * sc) {
            up += 20f * sc;
        }
        c.vy += (up - c.vy) * Math.min(1f, dt * 3f);
        c.timer -= dt;
        if (c.timer <= 0f) {
            c.tx = (rnd.nextFloat() - 0.5f) * 16f;
            c.timer = 12f + rnd.nextFloat() * 20f;
        }
        c.vx += (c.tx * sc - c.vx) * Math.min(1f, dt * 0.3f);
        c.x += c.vx * dt * cfg.speed;
        c.y = clamp(c.y + c.vy * dt * cfg.speed, lo, hi);
        float half = s.length * sc * 0.6f;
        if (c.x < -half) {
            c.x = w + half;
        } else if (c.x > w + half) {
            c.x = -half;
        }
        c.tilt += (clamp(c.vx * 0.012f, -0.25f, 0.25f) - c.tilt) * Math.min(1f, dt);
    }

    private void updateCrawl(Creature c, float dt) {
        Species s = c.species;
        float sc = scale(c.z);
        c.timer -= dt;
        float dx = c.tx - c.x;
        boolean moving = Math.abs(dx) > 2f;
        if (!moving && c.timer <= 0f) {
            float range = s.kind == Species.Kind.STARFISH ? 60f : w * 0.35f;
            c.tx = clamp(c.x + (rnd.nextFloat() - 0.5f) * 2f * range, xMin(c), xMax(c));
            c.timer = (s.kind == Species.Kind.CRAB ? 2f : 4f) + rnd.nextFloat() * 6f;
            if (s.kind == Species.Kind.OCTOPUS) {
                c.liftTarget = rnd.nextFloat() < 0.35f ? 120f + rnd.nextFloat() * 160f : 0f;
            }
        }
        float speed = s.speed * cfg.speed * sc * (c.lift > 30f ? 2.2f : 1f);
        float step = Math.min(Math.abs(dx), speed * dt);
        c.vx = moving ? Math.signum(dx) * speed : 0f;
        c.x += Math.signum(dx) * step;
        if (!moving) {
            c.liftTarget = 0f;
        }
        c.lift += (c.liftTarget - c.lift) * Math.min(1f, dt * 0.6f);
        c.y = floorY(c);
        if (s.kind == Species.Kind.OCTOPUS) {
            turn(c, dt, 1f);
        }
        advancePhase(c, dt, moving ? 1f : 0.15f);
    }

    private void updateHover(Creature c, float dt) {
        Species s = c.species;
        c.timer -= dt;
        if (c.timer <= 0f) {
            c.tx = clamp(c.anchorX + (rnd.nextFloat() - 0.5f) * 140f, xMin(c), xMax(c));
            c.ty = rootY(c.z) + 100f + rnd.nextFloat() * 240f;
            c.timer = 8f + rnd.nextFloat() * 10f;
        }
        float sc = scale(c.z);
        float speed = s.speed * cfg.speed * sc;
        float dx = c.tx - c.x;
        float dy = c.ty - c.y;
        float d = (float) Math.hypot(dx, dy);
        float want = Math.min(speed, d * 0.5f);
        float dvx = d > 0.01f ? dx / d * want : 0f;
        float dvy = d > 0.01f ? dy / d * want : 0f;
        c.vx += (dvx - c.vx) * Math.min(1f, dt);
        c.vy += (dvy - c.vy) * Math.min(1f, dt);
        c.x = clamp(c.x + c.vx * dt, xMin(c), xMax(c));
        c.y = clamp(c.y + c.vy * dt, yMin(s, c.z), yMax(s, c.z));
        turn(c, dt, 0.8f * sc);
        advancePhase(c, dt, 0.5f);
        c.tilt = (float) Math.sin(time * 0.6f + c.drift) * 0.07f;
    }

    private void updateBubbles(float dt) {
        for (int i = 0; i < airstones.length; i++) {
            emitTimers[i] -= dt;
            while (emitTimers[i] <= 0f) {
                emitTimers[i] += 0.07f + rnd.nextFloat() * 0.11f;
                Bubble b = new Bubble();
                b.z = 0.78f;
                b.x = airstones[i] + (rnd.nextFloat() - 0.5f) * 10f;
                b.y = rootY(b.z) + 6f;
                b.r = 2.5f + rnd.nextFloat() * rnd.nextFloat() * 8f;
                b.vy = 110f + b.r * 14f + rnd.nextFloat() * 40f;
                b.phase = rnd.nextFloat() * TWO_PI;
                b.wobble = 3f + rnd.nextFloat() * 6f;
                bubbles.add(b);
            }
        }
        if (cfg.bubbles >= 2 && !creatures.isEmpty()) {
            strayTimer -= dt;
            if (strayTimer <= 0f) {
                strayTimer = 0.6f + rnd.nextFloat() * 1.6f;
                Creature f = creatures.get(rnd.nextInt(creatures.size()));
                if (f.active && f.x > 0f && f.x < w) {
                    float sc = scale(f.z);
                    int n = 1 + rnd.nextInt(3);
                    for (int j = 0; j < n; j++) {
                        Bubble b = new Bubble();
                        b.z = f.z;
                        b.x = f.x + f.face * f.species.length * sc * 0.42f;
                        b.y = f.y + f.species.height() * sc * 0.05f - j * 9f;
                        b.r = 1.6f + rnd.nextFloat() * 2.6f;
                        b.vy = 70f + b.r * 12f;
                        b.phase = rnd.nextFloat() * TWO_PI;
                        b.wobble = 2f + rnd.nextFloat() * 3f;
                        bubbles.add(b);
                    }
                }
            }
        }
        for (int i = bubbles.size() - 1; i >= 0; i--) {
            Bubble b = bubbles.get(i);
            b.y += b.vy * scale(b.z) * dt;
            b.phase += dt * 5f;
            if (b.phase > TWO_PI) {
                b.phase -= TWO_PI;
            }
            b.x += (float) Math.cos(b.phase) * b.wobble * dt * 4f;
            if (b.y > H - 30f) {
                int last = bubbles.size() - 1;
                bubbles.set(i, bubbles.get(last));
                bubbles.remove(last);
            }
        }
    }

    private Mote newMote() {
        Mote m = new Mote();
        m.x = rnd.nextFloat() * w;
        m.y = rnd.nextFloat() * H;
        m.z = rnd.nextFloat();
        m.size = 1.5f + rnd.nextFloat() * rnd.nextFloat() * 5f;
        m.alpha = 0.12f + rnd.nextFloat() * 0.3f;
        m.vx = (rnd.nextFloat() - 0.5f) * 10f;
        m.vy = (rnd.nextFloat() - 0.6f) * 5f;
        m.phase = rnd.nextFloat() * TWO_PI;
        return m;
    }

    private void updateMote(Mote m, float dt) {
        m.phase += dt * 0.4f;
        if (m.phase > TWO_PI) {
            m.phase -= TWO_PI;
        }
        float sc = scale(m.z);
        m.x += (m.vx + (float) Math.sin(m.phase) * 4f) * sc * dt;
        m.y += (m.vy + (float) Math.cos(m.phase * 1.3f) * 2f) * sc * dt;
        if (m.x < -10f) {
            m.x += w + 20f;
        } else if (m.x > w + 10f) {
            m.x -= w + 20f;
        }
        if (m.y < -10f) {
            m.y += H + 20f;
        } else if (m.y > H + 10f) {
            m.y -= H + 20f;
        }
    }
}
