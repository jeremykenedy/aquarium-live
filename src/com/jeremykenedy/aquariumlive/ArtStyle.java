package com.jeremykenedy.aquariumlive;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

/** The finishing pass that gives every painted sprite the chosen look, plus per-look switches. */
final class ArtStyle {

    private ArtStyle() {
    }

    /** Big, expressive animated-film eyes instead of small realistic ones. */
    static boolean bigEyes(Config.Style s) {
        return s == Config.Style.ANIMATED || s == Config.Style.CARTOON || s == Config.Style.MOVIE || s == Config.Style.PAINTED;
    }

    /** Fine detail such as scales and fin rays. Stylised looks leave it out. */
    static boolean fineDetail(Config.Style s) {
        return s == Config.Style.REALISTIC || s == Config.Style.RETRO;
    }

    static Bitmap finish(Bitmap src, Config.Style style) {
        Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        switch (style) {
            case CARTOON:
                outline(c, src, 0xFF111318, Math.max(2.5f, src.getWidth() / 140f));
                break;
            case PAINTED:
                outline(c, src, 0xFF1C2E4A, Math.max(1.6f, src.getWidth() / 260f));
                break;
            default:
                break;
        }
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        p.setColorFilter(new ColorMatrixColorFilter(colour(style)));
        c.drawBitmap(src, 0, 0, p);
        src.recycle();
        switch (style) {
            case CARTOON:
                bands(out, new float[] {0.28f, 0.55f, 0.82f, 1f}, 0.04f);
                break;
            case PAINTED:
                bands(out, new float[] {0.42f, 0.78f, 1f}, 0.07f);
                break;
            case RETRO:
                posterize(out, 6);
                break;
            default:
                break;
        }
        return out;
    }

    private static ColorMatrix colour(Config.Style style) {
        ColorMatrix m = new ColorMatrix();
        switch (style) {
            case ANIMATED:
                m.setSaturation(1.25f);
                scale(m, 1.06f, 1.06f, 1.06f);
                break;
            case CARTOON:
                m.setSaturation(1.45f);
                scale(m, 1.08f, 1.08f, 1.08f);
                break;
            case MOVIE:
                m.setSaturation(1.32f);
                scale(m, 1.1f, 1.1f, 1.1f);
                break;
            case PAINTED:
                m.setSaturation(1.12f);
                scale(m, 1.06f, 1.02f, 0.94f);
                break;
            case RETRO:
                m.setSaturation(1.2f);
                break;
            default:
                m.setSaturation(0.9f);
                break;
        }
        return m;
    }

    private static void scale(ColorMatrix m, float r, float g, float b) {
        ColorMatrix s = new ColorMatrix();
        s.setScale(r, g, b, 1f);
        m.postConcat(s);
    }

    /** Draws the sprite's silhouette, offset all round, in a solid colour: an ink line once the sprite goes on top. */
    private static void outline(Canvas c, Bitmap src, int colour, float width) {
        Bitmap alpha = src.extractAlpha();
        Paint ink = new Paint(Paint.FILTER_BITMAP_FLAG);
        ink.setColorFilter(new PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_IN));
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2 * i / 12;
            c.drawBitmap(alpha, (float) Math.cos(a) * width, (float) Math.sin(a) * width, ink);
        }
        alpha.recycle();
    }

    /**
     * Cel shading: snaps brightness to a few flat bands while keeping each
     * colour's hue, with a short soft ramp between bands so edges do not shimmer.
     */
    static void bands(Bitmap b, float[] levels, float soft) {
        int w = b.getWidth();
        int h = b.getHeight();
        int[] px = new int[w * h];
        b.getPixels(px, 0, w, 0, 0, w, h);
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            int a = c >>> 24;
            if (a == 0) {
                continue;
            }
            float r = ((c >> 16) & 0xFF) / 255f;
            float g = ((c >> 8) & 0xFF) / 255f;
            float bl = (c & 0xFF) / 255f;
            float lum = 0.3f * r + 0.59f * g + 0.11f * bl;
            if (lum < 0.004f) {
                continue;
            }
            float target = Tone.snap(lum, levels, soft);
            float k = target / lum;
            px[i] = Color.argb(a, clamp(r * k), clamp(g * k), clamp(bl * k));
        }
        b.setPixels(px, 0, w, 0, 0, w, h);
    }

    /** Fewer colours and hard-edged transparency, like an old 256-colour screensaver. */
    static void posterize(Bitmap b, int steps) {
        int w = b.getWidth();
        int h = b.getHeight();
        int[] px = new int[w * h];
        b.getPixels(px, 0, w, 0, 0, w, h);
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            int a = c >>> 24;
            if (a < 110) {
                px[i] = 0;
                continue;
            }
            px[i] = Color.argb(255, Tone.quantize((c >> 16) & 0xFF, steps), Tone.quantize((c >> 8) & 0xFF, steps), Tone.quantize(c & 0xFF, steps));
        }
        b.setPixels(px, 0, w, 0, 0, w, h);
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v * 255f)));
    }
}
