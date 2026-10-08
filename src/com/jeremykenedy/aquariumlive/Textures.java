package com.jeremykenedy.aquariumlive;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import java.util.Random;

/** Generated textures that are not creatures or plants: sea floor, light and bubbles. */
final class Textures {

    private Textures() {
    }

    /**
     * Sea floor strip, 1024x256, tiling left to right. The top fades out so
     * the back of the floor melts into the water.
     */
    static Bitmap floor(Config.Theme theme) {
        int w = 1024;
        int h = 256;
        Random rnd = new Random(theme.ordinal() * 977L + 13);
        int back;
        int front;
        switch (theme) {
            case OCEAN:
                back = 0xFF9FB4C0;
                front = 0xFF6D8594;
                break;
            case KELP_FOREST:
                back = 0xFF7D7A68;
                front = 0xFF4E4A3A;
                break;
            case FISH_TANK:
                back = 0xFF8A7A62;
                front = 0xFF5A4B38;
                break;
            default:
                back = 0xFFEDE3C8;
                front = 0xFFC9B48A;
                break;
        }
        int[] px = new int[w * h];
        for (int y = 0; y < h; y++) {
            float t = y / (float) (h - 1);
            int base = mix(back, front, t);
            float fade = Math.min(1f, t / 0.16f);
            fade = fade * fade * (3f - 2f * fade);
            for (int x = 0; x < w; x++) {
                float grain = (rnd.nextFloat() - 0.5f) * (0.10f + 0.12f * t);
                float ripple = theme == Config.Theme.REEF || theme == Config.Theme.OCEAN
                        ? 0.05f * (float) Math.sin(x * 0.045f + Math.sin(x * 0.011f) * 3f + y * 0.35f) : 0f;
                int c = shade(base, 1f + grain + ripple);
                int a = (int) (255 * fade);
                px[y * w + x] = Color.argb(a, Color.red(c), Color.green(c), Color.blue(c));
            }
        }
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.setPixels(px, 0, w, 0, 0, w, h);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int pebbles = theme == Config.Theme.FISH_TANK ? 2600 : (theme == Config.Theme.KELP_FOREST ? 900 : 300);
        int[] tankColors = {0xFF3B3026, 0xFF6E5A44, 0xFFA08A6A, 0xFF2A2622, 0xFFC4B08C, 0xFF7C6F60};
        for (int i = 0; i < pebbles; i++) {
            float y = 30f + rnd.nextFloat() * (h - 30f);
            float t = y / h;
            float r = (theme == Config.Theme.FISH_TANK ? 2.5f : 2f) + rnd.nextFloat() * (theme == Config.Theme.KELP_FOREST ? 9f : 4f) * t;
            float x = rnd.nextFloat() * w;
            int col = theme == Config.Theme.FISH_TANK ? tankColors[rnd.nextInt(tankColors.length)]
                    : shade(mix(back, front, t), 0.7f + rnd.nextFloat() * 0.6f);
            for (int dx = -1; dx <= 1; dx++) {
                float px0 = x + dx * w;
                if (px0 + r < 0 || px0 - r > w) {
                    continue;
                }
                p.setShader(new RadialGradient(px0 - r * 0.3f, y - r * 0.4f, r * 1.4f, shade(col, 1.35f), shade(col, 0.6f), Shader.TileMode.CLAMP));
                c.drawOval(px0 - r, y - r * 0.7f, px0 + r, y + r * 0.7f, p);
            }
        }
        p.setShader(null);
        return b;
    }

    /** Tileable 256x256 web of light, the pattern sunlight makes through ripples. */
    static Bitmap caustics() {
        int n = 256;
        int cells = 22;
        Random rnd = new Random(5);
        float[] fx = new float[cells];
        float[] fy = new float[cells];
        for (int i = 0; i < cells; i++) {
            fx[i] = rnd.nextFloat();
            fy[i] = rnd.nextFloat();
        }
        int[] px = new int[n * n];
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                float u = x / (float) n;
                float v = y / (float) n;
                float d1 = 9f;
                float d2 = 9f;
                for (int i = 0; i < cells; i++) {
                    float dx = Math.abs(u - fx[i]);
                    float dy = Math.abs(v - fy[i]);
                    dx = Math.min(dx, 1f - dx);
                    dy = Math.min(dy, 1f - dy);
                    float d = dx * dx + dy * dy;
                    if (d < d1) {
                        d2 = d1;
                        d1 = d;
                    } else if (d < d2) {
                        d2 = d;
                    }
                }
                float edge = (float) (Math.sqrt(d2) - Math.sqrt(d1));
                float k = Math.max(0f, 1f - edge / 0.055f);
                k = k * k * (3f - 2f * k);
                int g = (int) (255 * k);
                px[y * n + x] = Color.argb(255, g, g, g);
            }
        }
        Bitmap b = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888);
        b.setPixels(px, 0, n, 0, 0, n, n);
        return b;
    }

    /** A soft shaft of light, brightest along the bottom edge (where it leaves the surface). */
    static Bitmap ray() {
        int w = 64;
        int h = 256;
        int[] px = new int[w * h];
        for (int y = 0; y < h; y++) {
            float along = y / (float) (h - 1);
            float fall = (float) Math.pow(along, 1.6);
            for (int x = 0; x < w; x++) {
                float d = (x - (w - 1) / 2f) / (w / 2f);
                float a = (float) Math.exp(-d * d * 3.2f) * fall;
                int v = (int) (255 * a);
                px[y * w + x] = Color.argb(v, 255, 255, 255);
            }
        }
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.setPixels(px, 0, w, 0, 0, w, h);
        return b;
    }

    /** Soft round dot, used for shadows and drifting specks. */
    static Bitmap dot() {
        int n = 64;
        int[] px = new int[n * n];
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                float dx = (x - (n - 1) / 2f) / (n / 2f);
                float dy = (y - (n - 1) / 2f) / (n / 2f);
                float r = Math.min(1f, (float) Math.sqrt(dx * dx + dy * dy));
                int a = (int) (255 * (1f - r) * (1f - r));
                px[y * n + x] = Color.argb(a, 255, 255, 255);
            }
        }
        Bitmap b = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888);
        b.setPixels(px, 0, n, 0, 0, n, n);
        return b;
    }

    static Bitmap bubble() {
        int n = 64;
        int[] px = new int[n * n];
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                float dx = (x - (n - 1) / 2f) / (n / 2f - 1f);
                float dy = (y - (n - 1) / 2f) / (n / 2f - 1f);
                float r = (float) Math.sqrt(dx * dx + dy * dy);
                float a;
                if (r > 1f) {
                    a = Math.max(0f, 1f - (r - 1f) * 12f) * 0.7f;
                } else {
                    a = 0.07f + 0.6f * (float) Math.pow(r, 8);
                }
                float hx = dx + 0.38f;
                float hy = dy + 0.38f;
                float spot = Math.max(0f, 1f - (float) Math.sqrt(hx * hx + hy * hy) / 0.22f);
                a = Math.min(1f, a + spot * 0.95f);
                px[y * n + x] = Color.argb((int) (255 * a), 235, 248, 255);
            }
        }
        Bitmap b = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888);
        b.setPixels(px, 0, n, 0, 0, n, n);
        return b;
    }

    static int mix(int a, int b, float t) {
        return Color.argb(255,
                (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t),
                (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t),
                (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t));
    }

    static int shade(int c, float f) {
        return Color.argb(Color.alpha(c),
                Math.max(0, Math.min(255, (int) (Color.red(c) * f))),
                Math.max(0, Math.min(255, (int) (Color.green(c) * f))),
                Math.max(0, Math.min(255, (int) (Color.blue(c) * f))));
    }
}
