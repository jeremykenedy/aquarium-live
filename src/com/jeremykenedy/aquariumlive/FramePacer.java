package com.jeremykenedy.aquariumlive;

/**
 * Automatic frame rate: after a few seconds at 60, drop to a steady 30 if
 * frames are not keeping up. Uneven 60/30 pacing looks worse than 30. The
 * first seconds are skipped because art is still being uploaded. Kept free
 * of Android so it can be tested on a plain JVM.
 */
final class FramePacer {

    static final double WARM_UP = 3.0;
    static final float WINDOW = 4f;
    static final float MIN_FPS = 54f;

    private final int fps;
    private volatile boolean halfRate;
    private int frames;
    private float seconds;

    /** fps is the Frame rate setting: 0 automatic, 60 or 30. */
    FramePacer(int fps) {
        this.fps = fps;
    }

    /** True when frames should be drawn on every other display refresh (30 a second). */
    boolean halfRate() {
        return halfRate || fps == 30;
    }

    /** Starts a fresh measurement, for when the drawing surface is recreated. */
    void restart() {
        frames = 0;
        seconds = 0f;
    }

    /** Records one drawn frame that took dt seconds, time seconds after the scene started. */
    void frame(double time, float dt) {
        if (fps != 0 || halfRate || dt <= 0f || time < WARM_UP) {
            return;
        }
        frames++;
        seconds += dt;
        if (seconds < WINDOW) {
            return;
        }
        if (frames / seconds < MIN_FPS) {
            halfRate = true;
        }
        restart();
    }
}
