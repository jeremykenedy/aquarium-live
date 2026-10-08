package com.jeremykenedy.aquariumlive;

import android.service.dreams.DreamService;

/** The screensaver itself. Any key press wakes the TV, as with the stock screensaver. */
public final class AquariumDream extends DreamService {

    private AquariumView view;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(true);
        view = new AquariumView(this);
        setContentView(view);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        view.resume();
    }

    @Override
    public void onDreamingStopped() {
        view.pause();
        super.onDreamingStopped();
    }

    @Override
    public void onDetachedFromWindow() {
        if (view != null) {
            view.release();
        }
        super.onDetachedFromWindow();
    }
}
