package com.jeremykenedy.aquariumlive;

/** Tone maths for the stylised looks, kept free of Android so it can be tested on a plain JVM. */
final class Tone {

    private Tone() {
    }

    /**
     * Maps a brightness to its band's flat value. The darkest band keeps its
     * own value so black eyes and stripes stay black; within soft of a band
     * edge the two bands blend so the edge stays smooth.
     */
    static float snap(float lum, float[] levels, float soft) {
        if (levels.length == 0) {
            return lum;
        }
        int i = 0;
        while (i < levels.length - 1 && lum > levels[i]) {
            i++;
        }
        float value = bandValue(levels, i, lum);
        if (i + 1 < levels.length && levels[i] - lum < soft) {
            float t = 1f - (levels[i] - lum) / soft;
            value += (bandValue(levels, i + 1, lum) - value) * t * 0.5f;
        }
        if (i > 0 && lum - levels[i - 1] < soft) {
            float t = 1f - (lum - levels[i - 1]) / soft;
            value += (bandValue(levels, i - 1, lum) - value) * t * 0.5f;
        }
        return value;
    }

    private static float bandValue(float[] levels, int i, float lum) {
        if (i == 0) {
            return lum;
        }
        return (levels[i - 1] + levels[i]) * 0.5f + 0.04f;
    }

    /** Rounds an 8-bit channel to one of steps evenly spaced values. */
    static int quantize(int v, int steps) {
        float q = (steps - 1) / 255f;
        return Math.max(0, Math.min(255, Math.round(Math.round(v * q) / q)));
    }
}
