package com.jeremykenedy.aquariumlive;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextClock;

import java.util.Random;

/** The live tank plus the optional drifting clock. Shared by the screensaver and the preview. */
final class AquariumView extends FrameLayout {

    private final GLSurfaceView gl;
    private final AquariumRenderer renderer;
    private final TextClock clock;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random rnd = new Random();
    private boolean running;
    private long vsyncs;
    private final android.view.Choreographer.FrameCallback vsync = new android.view.Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (!running) {
                return;
            }
            vsyncs++;
            if (!renderer.halfRate() || vsyncs % 2 == 0) {
                gl.requestRender();
            }
            android.view.Choreographer.getInstance().postFrameCallback(this);
        }
    };
    private final Runnable moveClock = new Runnable() {
        @Override
        public void run() {
            placeClock();
            handler.postDelayed(this, 45_000);
        }
    };

    AquariumView(Context context) {
        super(context);
        setBackgroundColor(0xFF000000);
        Config cfg = load(context);
        gl = new GLSurfaceView(context);
        android.app.ActivityManager am = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean gles3 = am.getDeviceConfigurationInfo().reqGlEsVersion >= 0x30000;
        gl.setEGLContextClientVersion(gles3 ? 3 : 2);
        gl.setEGLConfigChooser(8, 8, 8, 0, 0, 0);
        gl.setPreserveEGLContextOnPause(true);
        renderer = new AquariumRenderer(cfg, gles3, artCache(context), "v" + installStamp(context) + "-");
        gl.setRenderer(renderer);
        gl.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        int[] size = renderSize(context, cfg.resolution == 0 && cfg.style == Config.Style.RETRO ? 540 : cfg.resolution);
        if (size != null) {
            gl.getHolder().setFixedSize(size[0], size[1]);
        }
        addView(gl, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (cfg.clock) {
            clock = new TextClock(context);
            clock.setFormat12Hour("h:mm");
            clock.setFormat24Hour("H:mm");
            clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 46);
            clock.setTextColor(0xCCFFFFFF);
            clock.setShadowLayer(12f, 0f, 2f, 0x99000000);
            clock.setTypeface(android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL));
            addView(clock, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.START));
        } else {
            clock = null;
        }
    }

    /**
     * Folder for generated art. Painting every sprite takes seconds on a TV,
     * so the results are kept between runs; files from an older install of
     * the app are removed because the art may have changed.
     */
    private static java.io.File artCache(Context context) {
        java.io.File dir = new java.io.File(context.getCacheDir(), "art");
        dir.mkdirs();
        String keep = "v" + installStamp(context) + "-";
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File f : files) {
                if (!f.getName().startsWith(keep)) {
                    f.delete();
                }
            }
        }
        return dir;
    }

    private static long installStamp(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).lastUpdateTime;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return 0L;
        }
    }

    static Config load(Context context) {
        return Config.fromMap(PreferenceManager.getDefaultSharedPreferences(context).getAll());
    }

    /**
     * Render size for the chosen resolution, or null to use the window's own
     * size. Automatic (0) uses the window, which is the size the TV actually
     * composes app graphics at; a Fire TV 4K draws apps at 1920x1080 and
     * scales them to the panel, so rendering larger there only costs speed.
     */
    private static int[] renderSize(Context context, int resolution) {
        if (resolution == 0) {
            return null;
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int w;
        int h;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            Display.Mode mode = display.getMode();
            w = mode.getPhysicalWidth();
            h = mode.getPhysicalHeight();
        } else {
            android.graphics.Point real = new android.graphics.Point();
            display.getRealSize(real);
            w = real.x;
            h = real.y;
        }
        int pw = Math.max(w, h);
        int ph = Math.min(w, h);
        if (pw <= 0 || ph <= 0) {
            return null;
        }
        int targetH = Math.min(resolution, ph);
        return new int[] {Math.round(targetH * pw / (float) ph), targetH};
    }

    private void placeClock() {
        if (clock == null || getWidth() == 0) {
            return;
        }
        float maxX = Math.max(0, getWidth() - clock.getWidth() - getWidth() * 0.06f);
        float maxY = Math.max(0, getHeight() - clock.getHeight() - getHeight() * 0.08f);
        float x = getWidth() * 0.06f + rnd.nextFloat() * (maxX - getWidth() * 0.06f);
        float y = getHeight() * 0.08f + rnd.nextFloat() * (maxY - getHeight() * 0.08f);
        clock.animate().alpha(0f).setDuration(900).withEndAction(() -> {
            clock.setTranslationX(x);
            clock.setTranslationY(y);
            clock.animate().alpha(1f).setDuration(900).start();
        }).start();
    }

    void resume() {
        gl.onResume();
        running = true;
        android.view.Choreographer.getInstance().postFrameCallback(vsync);
        if (clock != null) {
            handler.postDelayed(moveClock, 1_000);
        }
    }

    void pause() {
        running = false;
        android.view.Choreographer.getInstance().removeFrameCallback(vsync);
        handler.removeCallbacks(moveClock);
        gl.onPause();
    }

    /** Stops the background art painters; call when the view goes away for good. */
    void release() {
        renderer.release();
    }
}
