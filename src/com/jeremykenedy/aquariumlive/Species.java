package com.jeremykenedy.aquariumlive;

/**
 * How a kind of fish or sea creature looks and moves. Sizes are in world
 * units, where the tank is 1080 units tall. {@code length} is the full sprite
 * width, tail included; the sprite's height follows from the texture's
 * aspect ratio.
 */
public final class Species {

    /** Decides how the creature is drawn and animated. */
    public enum Kind { FISH, SHARK, WHALE, DOLPHIN, RAY, TURTLE, OCTOPUS, JELLY, SEAHORSE, CRAB, STARFISH }

    /** Decides how the creature moves around the tank. */
    public enum Motion { SWIM, ROAM, VISIT, DRIFT, CRAWL, HOVER }

    public static final String SHARKS = "sharks";
    public static final String WHALES = "whales";
    public static final String DOLPHINS = "dolphins";
    public static final String RAYS = "rays";
    public static final String TURTLES = "turtles";
    public static final String OCTOPUSES = "octopuses";
    public static final String JELLYFISH = "jellyfish";
    public static final String SEAHORSES = "seahorses";
    public static final String CRABS = "crabs";

    public static final String[] GROUPS = {SHARKS, WHALES, DOLPHINS, RAYS, TURTLES, OCTOPUSES, JELLYFISH, SEAHORSES, CRABS};

    public final String id;
    public final Kind kind;
    public final Motion motion;
    public final Config.Theme[] themes;
    /** The sea-life toggle this creature belongs to, or null for ordinary fish. */
    public final String group;
    public final int texW;
    public final int texH;
    public final float length;
    /** Cruising speed in world units per second at the front of the tank. */
    public final float speed;
    public final boolean schooling;
    /** Peak tail (or fluke, or wing) movement. */
    public final float tailAmp;
    /** Beats per second when cruising. */
    public final float tailFreq;
    /** Fraction of the body, nose first, that stays rigid. */
    public final float rigid;
    /** Relative odds of being picked as an individual fish. */
    public final int weight;
    public final float zMin;
    public final float zMax;

    private Species(Builder b) {
        this.id = b.id;
        this.kind = b.kind;
        this.motion = b.motion;
        this.themes = b.themes;
        this.group = b.group;
        this.texW = b.texW;
        this.texH = b.texH;
        this.length = b.length;
        this.speed = b.speed;
        this.schooling = b.schooling;
        this.tailAmp = b.tailAmp;
        this.tailFreq = b.tailFreq;
        this.rigid = b.rigid;
        this.weight = b.weight;
        this.zMin = b.zMin;
        this.zMax = b.zMax;
    }

    public float height() {
        return length * texH / texW;
    }

    public boolean livesIn(Config.Theme theme) {
        for (Config.Theme t : themes) {
            if (t == theme) {
                return true;
            }
        }
        return false;
    }

    /** True for anything bigger than a fish that small fish keep away from. */
    public boolean isPredator() {
        return kind == Kind.SHARK || kind == Kind.DOLPHIN;
    }

    private static final class Builder {
        final String id;
        Kind kind = Kind.FISH;
        Motion motion = Motion.SWIM;
        Config.Theme[] themes;
        String group;
        int texW = 512;
        int texH = 256;
        float length;
        float speed;
        boolean schooling;
        float tailAmp = 0.5f;
        float tailFreq = 2f;
        float rigid = 0.45f;
        int weight;
        float zMin = 0.04f;
        float zMax = 0.93f;

        Builder(String id, float length, float speed, Config.Theme... themes) {
            this.id = id;
            this.length = length;
            this.speed = speed;
            this.themes = themes;
        }

        Builder kind(Kind k, Motion m, String g) {
            kind = k;
            motion = m;
            group = g;
            return this;
        }

        Builder tex(int w, int h) {
            texW = w;
            texH = h;
            return this;
        }

        Builder tail(float amp, float freq, float rigidPart) {
            tailAmp = amp;
            tailFreq = freq;
            rigid = rigidPart;
            return this;
        }

        Builder weight(int w) {
            weight = w;
            return this;
        }

        Builder school() {
            schooling = true;
            return this;
        }

        Builder depth(float lo, float hi) {
            zMin = lo;
            zMax = hi;
            return this;
        }

        Species build() {
            return new Species(this);
        }
    }

    static final Config.Theme OCEAN = Config.Theme.OCEAN;
    static final Config.Theme REEF = Config.Theme.REEF;
    static final Config.Theme KELP = Config.Theme.KELP_FOREST;
    static final Config.Theme TANK = Config.Theme.FISH_TANK;

    public static final Species[] ALL = {
        new Builder("clownfish", 118f, 62f, REEF).tail(0.55f, 2.6f, 0.45f).weight(4).build(),
        new Builder("blue_tang", 168f, 78f, REEF).tex(512, 512).tail(0.45f, 2f, 0.5f).weight(3).build(),
        new Builder("yellow_tang", 150f, 66f, REEF).tex(512, 512).tail(0.45f, 2.1f, 0.5f).weight(3).build(),
        new Builder("royal_gramma", 82f, 46f, REEF).tail(0.6f, 2.8f, 0.45f).weight(2).build(),
        new Builder("moorish_idol", 160f, 52f, REEF).tex(512, 512).tail(0.4f, 1.8f, 0.5f).weight(2).build(),
        new Builder("chromis", 58f, 72f, REEF).tail(0.6f, 3.4f, 0.42f).school().build(),

        new Builder("neon_tetra", 68f, 74f, TANK).tail(0.65f, 3.6f, 0.4f).school().build(),
        new Builder("rummynose", 76f, 80f, TANK).tail(0.6f, 3.4f, 0.4f).school().build(),
        new Builder("angelfish", 235f, 44f, TANK).tex(512, 512).tail(0.35f, 1.4f, 0.55f).weight(4).build(),
        new Builder("discus", 215f, 40f, TANK).tex(512, 512).tail(0.35f, 1.5f, 0.6f).weight(3).build(),
        new Builder("betta", 195f, 32f, TANK).tex(512, 512).tail(0.3f, 1.2f, 0.4f).weight(2).build(),
        new Builder("dwarf_gourami", 150f, 46f, TANK).tail(0.45f, 1.9f, 0.5f).weight(3).build(),

        new Builder("yellowfin_tuna", 270f, 150f, OCEAN).tail(0.5f, 3.2f, 0.55f).weight(3).build(),
        new Builder("barracuda", 300f, 90f, OCEAN).tail(0.45f, 1.8f, 0.55f).weight(2).build(),
        new Builder("sardine", 62f, 115f, OCEAN, KELP).tail(0.6f, 4f, 0.42f).school().build(),
        new Builder("garibaldi", 125f, 50f, KELP).tail(0.5f, 2.2f, 0.45f).weight(4).build(),
        new Builder("kelp_bass", 180f, 60f, KELP).tail(0.45f, 1.9f, 0.5f).weight(3).build(),

        new Builder("reef_shark", 430f, 105f, REEF, OCEAN).tex(1024, 512).kind(Kind.SHARK, Motion.ROAM, SHARKS)
                .tail(0.42f, 0.9f, 0.45f).depth(0.2f, 0.9f).build(),
        new Builder("hammerhead", 520f, 95f, OCEAN).tex(1024, 512).kind(Kind.SHARK, Motion.ROAM, SHARKS)
                .tail(0.4f, 0.8f, 0.45f).depth(0.35f, 0.92f).build(),
        new Builder("leopard_shark", 360f, 80f, KELP).tex(1024, 512).kind(Kind.SHARK, Motion.ROAM, SHARKS)
                .tail(0.45f, 1f, 0.42f).depth(0.2f, 0.85f).build(),
        new Builder("humpback", 1500f, 70f, OCEAN, KELP).tex(1024, 512).kind(Kind.WHALE, Motion.VISIT, WHALES)
                .tail(0.07f, 0.28f, 0.45f).depth(0.82f, 0.93f).build(),
        new Builder("dolphin", 300f, 170f, OCEAN).tex(1024, 512).kind(Kind.DOLPHIN, Motion.ROAM, DOLPHINS)
                .tail(0.06f, 1.6f, 0.5f).depth(0.15f, 0.8f).build(),
        new Builder("manta", 330f, 70f, OCEAN, REEF).tex(512, 1024).kind(Kind.RAY, Motion.ROAM, RAYS)
                .tail(0.45f, 0.35f, 0.2f).depth(0.3f, 0.9f).build(),
        new Builder("sea_turtle", 270f, 48f, OCEAN, REEF).tex(512, 512).kind(Kind.TURTLE, Motion.ROAM, TURTLES)
                .tail(0.7f, 0.45f, 1f).depth(0.1f, 0.85f).build(),
        new Builder("octopus", 230f, 26f, REEF, KELP).tex(512, 512).kind(Kind.OCTOPUS, Motion.CRAWL, OCTOPUSES)
                .tail(0.5f, 0.6f, 1f).depth(0.1f, 0.6f).build(),
        new Builder("jellyfish", 120f, 10f, OCEAN, REEF, KELP).tex(512, 1024).kind(Kind.JELLY, Motion.DRIFT, JELLYFISH)
                .tail(0.25f, 0.42f, 1f).depth(0.08f, 0.9f).build(),
        new Builder("seahorse", 105f, 9f, REEF, KELP).tex(256, 512).kind(Kind.SEAHORSE, Motion.HOVER, SEAHORSES)
                .tail(0.1f, 0.3f, 1f).depth(0.08f, 0.5f).build(),
        new Builder("crab", 92f, 22f, OCEAN, REEF, KELP).kind(Kind.CRAB, Motion.CRAWL, CRABS)
                .tail(0.1f, 3f, 1f).depth(0.04f, 0.5f).build(),
        new Builder("starfish", 80f, 1.2f, OCEAN, REEF, KELP).tex(256, 256).kind(Kind.STARFISH, Motion.CRAWL, CRABS)
                .tail(0f, 0f, 1f).depth(0.04f, 0.45f).build(),
    };
}
