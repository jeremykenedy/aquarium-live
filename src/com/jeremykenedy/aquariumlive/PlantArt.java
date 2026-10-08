package com.jeremykenedy.aquariumlive;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Random;

/**
 * Paints plants, corals, rocks and wood. Each sprite stands on the bottom
 * edge of its texture, centred, so the renderer can root it in the sand and
 * bend its top for sway.
 */
final class PlantArt {

    private final Canvas c;
    private final Bitmap bitmap;
    private final int w;
    private final int h;
    private final Random rnd;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    private PlantArt(Sim.PlantKind kind, int variant) {
        this.w = kind.texW;
        this.h = kind.texH;
        this.bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        this.c = new Canvas(bitmap);
        this.rnd = new Random(kind.id.hashCode() * 31L + variant);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
    }

    static Bitmap paint(Sim.PlantKind kind, int variant, Config.Style style) {
        return ArtStyle.finish(paint(kind, variant), style);
    }

    private static Bitmap paint(Sim.PlantKind kind, int variant) {
        PlantArt a = new PlantArt(kind, variant);
        switch (kind.id) {
            case "live_rock":
                a.rock(true, variant);
                break;
            case "boulder":
                a.rock(false, variant);
                break;
            case "kelp":
                a.kelp(variant);
                break;
            case "stones":
                a.stones(variant);
                break;
            case "staghorn":
                a.staghorn(variant);
                break;
            case "brain":
                a.brain(variant);
                break;
            case "sea_fan":
                a.seaFan(variant);
                break;
            case "anemone":
                a.anemone(variant);
                break;
            case "seagrass":
                a.blades(variant, 0xFF2B6A1F, 0xFF8BC34A, 8, 0.045f);
                break;
            case "vallis":
                a.blades(variant, 0xFF1E6B2E, 0xFF8CE99A, 11, 0.032f);
                break;
            case "sword":
                a.sword(variant);
                break;
            case "red_stem":
                a.redStem(variant);
                break;
            case "driftwood":
                a.driftwood(variant);
                break;
            default:
                throw new IllegalArgumentException(kind.id);
        }
        return a.bitmap;
    }

    private float r(float lo, float hi) {
        return lo + rnd.nextFloat() * (hi - lo);
    }

    private static int lerp(int a, int b, float t) {
        return Color.argb(
                (int) (Color.alpha(a) + (Color.alpha(b) - Color.alpha(a)) * t),
                (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t),
                (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t),
                (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t));
    }

    /** Irregular rounded outline around (cx, cy), flattened along the bottom. */
    private Path blob(float cx, float cy, float rx, float ry, int points, float jag, float floorY) {
        float[] radii = new float[points];
        for (int i = 0; i < points; i++) {
            radii[i] = 1f - jag + rnd.nextFloat() * jag * 2f;
        }
        Path path = new Path();
        float[] xs = new float[points];
        float[] ys = new float[points];
        for (int i = 0; i < points; i++) {
            double a = Math.PI * 2 * i / points;
            float smooth = (radii[i] * 2 + radii[(i + 1) % points] + radii[(i + points - 1) % points]) / 4f;
            xs[i] = cx + (float) Math.cos(a) * rx * smooth;
            ys[i] = Math.min(floorY, cy + (float) Math.sin(a) * ry * smooth);
        }
        path.moveTo((xs[0] + xs[points - 1]) / 2f, (ys[0] + ys[points - 1]) / 2f);
        for (int i = 0; i < points; i++) {
            int n = (i + 1) % points;
            path.quadTo(xs[i], ys[i], (xs[i] + xs[n]) / 2f, (ys[i] + ys[n]) / 2f);
        }
        path.close();
        return path;
    }

    private void speckle(Path clip, int count, int dark, int light, float size) {
        RectF b = new RectF();
        clip.computeBounds(b, true);
        c.save();
        c.clipPath(clip);
        for (int i = 0; i < count; i++) {
            p.setColor(rnd.nextBoolean() ? dark : light);
            c.drawCircle(r(b.left, b.right), r(b.top, b.bottom), r(size * 0.3f, size), p);
        }
        c.restore();
    }

    private void shade(Path clip, float lightX, float lightY, float radius) {
        RectF b = new RectF();
        clip.computeBounds(b, true);
        c.save();
        c.clipPath(clip);
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(lightX, lightY, radius, new int[] {0x30FFFFFF, 0x00000000, 0x70000000},
                new float[] {0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(b, p);
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, b.bottom - b.height() * 0.3f, 0, b.bottom, 0x00000000, 0x80000000, Shader.TileMode.CLAMP));
        c.drawRect(b, p);
        p.setShader(null);
        c.restore();
    }

    private void rock(boolean live, int variant) {
        int[][] palettes = live ? new int[][] {
            {0xFF7A6152, 0xFF4E3B31, 0xFFA48A73},
            {0xFF6E5F6E, 0xFF463A47, 0xFF9A8496},
            {0xFF857057, 0xFF54422F, 0xFFB09A7D},
        } : new int[][] {
            {0xFF5E6670, 0xFF353B43, 0xFF8C96A1},
            {0xFF6A6658, 0xFF3E3B32, 0xFF969080},
            {0xFF56606A, 0xFF30363D, 0xFF7F8A95},
        };
        int[] pal = palettes[variant % palettes.length];
        int lumps = 3 + rnd.nextInt(2);
        for (int i = 0; i < lumps; i++) {
            float cx = w * (0.24f + 0.52f * i / Math.max(1, lumps - 1)) + r(-14, 14);
            float rx = Math.min(w * r(0.2f, 0.3f), (Math.min(cx, w - cx) - 6f) / 1.22f);
            float ry = Math.min(h * r(0.5f, 0.66f), rx * 0.78f);
            Path b = blob(cx, h - ry * 0.42f, rx, ry, 13, 0.16f, h - 1f);
            p.setColor(0xFFFFFFFF);
            p.setShader(new LinearGradient(0, h - ry * 1.5f, 0, h, pal[2], pal[1], Shader.TileMode.CLAMP));
            c.drawPath(b, p);
            p.setShader(null);
            speckle(b, 700, FishArt.alpha(pal[1], 90), FishArt.alpha(pal[2], 80), w * 0.006f);
            if (live) {
                c.save();
                c.clipPath(b);
                for (int j = 0; j < 5; j++) {
                    int col = rnd.nextBoolean() ? 0x80AE3EC9 : (rnd.nextBoolean() ? 0x70F06595 : 0x60E8590C);
                    p.setColor(col);
                    c.drawPath(blob(r(cx - rx * 0.8f, cx + rx * 0.8f), r(h - ry * 1.3f, h - ry * 0.3f), r(18, 46), r(8, 18), 9, 0.4f, h), p);
                }
                p.setColor(0x60000000);
                for (int j = 0; j < 9; j++) {
                    float ox = r(cx - rx, cx + rx);
                    float oy = r(h - ry * 1.2f, h - 10);
                    c.drawOval(new RectF(ox, oy, ox + r(6, 16), oy + r(5, 12)), p);
                }
                c.restore();
            }
            shade(b, cx - rx * 0.3f, h - ry * 1.1f, Math.max(rx, ry) * 1.3f);
        }
    }

    private void kelp(int variant) {
        int stipes = 3 + variant % 2;
        for (int s = 0; s < stipes; s++) {
            float x0 = w * 0.5f + r(-34, 34);
            float lean = r(-40, 40);
            float top = r(10, 160);
            Path stipe = new Path();
            stipe.moveTo(x0, h);
            int segs = 32;
            float[] sx = new float[segs + 1];
            float[] sy = new float[segs + 1];
            for (int i = 0; i <= segs; i++) {
                float t = i / (float) segs;
                sy[i] = h - (h - top) * t;
                sx[i] = x0 + lean * t * t + (float) Math.sin(t * 9f + s * 2f) * 12f;
                if (i > 0) {
                    stipe.lineTo(sx[i], sy[i]);
                }
            }
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(6f);
            p.setColor(0xFF5C4A12);
            c.drawPath(stipe, p);
            p.setStyle(Paint.Style.FILL);
            boolean left = rnd.nextBoolean();
            for (float y = h - 60f; y > top + 10f; y -= r(26f, 40f)) {
                float t = (h - y) / (h - top);
                int i = Math.min(segs - 1, (int) (t * segs));
                float bx = sx[i] + (sx[i + 1] - sx[i]) * (t * segs - i);
                float len = r(150f, 230f) * (0.8f + 0.35f * t);
                float ang = (float) Math.toRadians(left ? r(-160, -112) : r(-68, -20));
                left = !left;
                kelpBlade(bx, y, ang, len, r(28f, 42f));
            }
        }
        Path hold = blob(w * 0.5f, h - 22f, w * 0.3f, 30f, 12, 0.35f, h - 1f);
        p.setColor(0xFF3B2E0C);
        c.drawPath(hold, p);
    }

    private void kelpBlade(float x, float y, float angle, float len, float width) {
        c.save();
        c.translate(x, y);
        c.rotate((float) Math.toDegrees(angle));
        Path b = new Path();
        b.moveTo(10f, 0f);
        for (float t = 0f; t <= 1.001f; t += 0.08f) {
            b.lineTo(10f + len * t, -width * (float) Math.sin(Math.PI * Math.min(1f, t * 1.15f)) * 0.5f + (float) Math.sin(t * 30f) * 2.2f);
        }
        for (float t = 1f; t >= 0f; t -= 0.08f) {
            b.lineTo(10f + len * t, width * (float) Math.sin(Math.PI * Math.min(1f, t * 1.15f)) * 0.5f + (float) Math.sin(t * 30f + 1f) * 2.2f);
        }
        b.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, 0, len, 0, 0xE0806A1C, 0xC8C2A443, Shader.TileMode.CLAMP));
        c.drawPath(b, p);
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setColor(0x30FFF3BF);
        c.drawLine(12f, 0f, len * 0.9f, 0f, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(5f, -2f, 9f, 0xFFD4B65A, 0xFF8C7424, Shader.TileMode.CLAMP));
        c.drawOval(new RectF(-2f, -7f, 14f, 7f), p);
        p.setShader(null);
        c.restore();
    }

    private void stones(int variant) {
        int[] bases = {0xFF8D8A85, 0xFF7B746B, 0xFF9A948A};
        int count = 3 + rnd.nextInt(3);
        for (int i = 0; i < count; i++) {
            int base = lerp(bases[(variant + i) % bases.length], 0xFF6B5E50, r(0f, 0.5f));
            float rx = w * r(0.1f, 0.2f);
            float ry = h * r(0.25f, 0.42f);
            float cx = r(rx * 1.15f, w - rx * 1.15f);
            Path b = blob(cx, h - ry * 0.6f, rx, ry, 10, 0.12f, h - 1f);
            p.setColor(0xFFFFFFFF);
            p.setShader(new LinearGradient(0, h - ry * 1.6f, 0, h, lerp(base, 0xFFFFFFFF, 0.25f), lerp(base, 0xFF000000, 0.45f), Shader.TileMode.CLAMP));
            c.drawPath(b, p);
            p.setShader(null);
            speckle(b, 120, 0x30000000, 0x30FFFFFF, w * 0.006f);
            shade(b, cx - rx * 0.4f, h - ry * 1.2f, Math.max(rx, ry) * 1.5f);
        }
    }

    private void branch(float x, float y, float angle, float len, float width, int depth, int base, int tip, float spread) {
        float x2 = x + (float) Math.cos(angle) * len;
        float y2 = y + (float) Math.sin(angle) * len;
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width);
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(x, y, x2, y2, base, depth <= 1 ? tip : lerp(base, tip, 0.45f), Shader.TileMode.CLAMP));
        Path seg = new Path();
        seg.moveTo(x, y);
        seg.quadTo((x + x2) / 2f + r(-len, len) * 0.12f, (y + y2) / 2f, x2, y2);
        c.drawPath(seg, p);
        p.setShader(null);
        if (depth <= 0) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(lerp(tip, 0xFFFFFFFF, 0.35f));
            c.drawCircle(x2, y2, width * 0.55f, p);
            return;
        }
        int kids = 2 + (rnd.nextFloat() < 0.3f ? 1 : 0);
        for (int i = 0; i < kids; i++) {
            float a = angle + (i - (kids - 1) / 2f) * spread + r(-0.15f, 0.15f);
            a = Math.max((float) -Math.PI + 0.35f, Math.min(-0.35f, a));
            branch(x2, y2, a, len * r(0.66f, 0.82f), width * 0.78f, depth - 1, lerp(base, tip, 0.3f), tip, spread);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void staghorn(int variant) {
        int base = variant == 0 ? 0xFFC9A66B : 0xFFB89BD0;
        int tip = variant == 0 ? 0xFF8F6CFF : 0xFFE8F5FF;
        for (int i = 0; i < 4; i++) {
            float x = w * (0.35f + 0.1f * i) + r(-14, 14);
            branch(x, h - 2f, (float) (-Math.PI / 2) + r(-0.5f, 0.5f), h * r(0.18f, 0.26f), w * 0.05f, 4, base, tip, 0.55f);
        }
    }

    private void brain(int variant) {
        int base = variant == 0 ? 0xFFA7B26A : 0xFFD7B98E;
        int groove = variant == 0 ? 0xFF5E6B2E : 0xFF8C6A45;
        Path dome = new Path();
        dome.addArc(new RectF(w * 0.06f, h * 0.08f, w * 0.94f, h * 1.9f), 180f, 180f);
        dome.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(w * 0.42f, h * 0.35f, w * 0.6f, lerp(base, 0xFFFFFFFF, 0.25f), lerp(base, 0xFF000000, 0.4f), Shader.TileMode.CLAMP));
        c.drawPath(dome, p);
        p.setShader(null);
        c.save();
        c.clipPath(dome);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(w * 0.012f);
        p.setColor(FishArt.alpha(groove, 200));
        for (int i = 0; i < 26; i++) {
            Path g = new Path();
            float y0 = h * (0.1f + i * 0.037f);
            g.moveTo(0, y0);
            float phase = r(0f, 6f);
            for (float x = 0; x <= w; x += 6f) {
                g.lineTo(x, y0 + (float) Math.sin(x * 0.05f + phase) * h * 0.035f + (float) Math.sin(x * 0.013f + i) * h * 0.02f);
            }
            c.drawPath(g, p);
        }
        p.setStyle(Paint.Style.FILL);
        c.restore();
        shade(dome, w * 0.38f, h * 0.2f, w * 0.65f);
    }

    private void seaFan(int variant) {
        int base = variant == 0 ? 0xFF862E9C : 0xFFC2255C;
        int tip = variant == 0 ? 0xFFDA77F2 : 0xFFFF8787;
        float x0 = w * 0.5f;
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(w * 0.03f);
        p.setColor(base);
        c.drawLine(x0, h, x0, h * 0.86f, p);
        for (int i = 0; i < 7; i++) {
            float a = (float) (-Math.PI / 2) + (i - 3) * 0.22f;
            branch(x0, h * 0.86f, a, h * 0.2f, w * 0.016f, 4, base, tip, 0.28f);
        }
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1.5f, w * 0.003f));
        p.setColor(FishArt.alpha(tip, 110));
        for (int i = 0; i < 70; i++) {
            float a = (float) (-Math.PI / 2) + r(-0.85f, 0.85f);
            float d = h * r(0.2f, 0.75f);
            float x = x0 + (float) Math.cos(a) * d;
            float y = h * 0.86f + (float) Math.sin(a) * d;
            float a2 = a + r(0.12f, 0.3f) * (rnd.nextBoolean() ? 1 : -1);
            float d2 = d * r(0.95f, 1.05f);
            c.drawLine(x, y, x0 + (float) Math.cos(a2) * d2, h * 0.86f + (float) Math.sin(a2) * d2, p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void anemone(int variant) {
        int body = variant == 0 ? 0xFFE8A0B4 : 0xFFB5D27A;
        int tipCol = variant == 0 ? 0xFFFFD8E4 : 0xFFEFFFD0;
        int tent = variant == 0 ? 0xFFF783AC : 0xFF94D82D;
        float cx = w * 0.5f;
        Path column = new Path();
        column.moveTo(cx - w * 0.17f, h);
        column.cubicTo(cx - w * 0.15f, h * 0.85f, cx - w * 0.2f, h * 0.76f, cx - w * 0.22f, h * 0.72f);
        column.lineTo(cx + w * 0.22f, h * 0.72f);
        column.cubicTo(cx + w * 0.2f, h * 0.76f, cx + w * 0.15f, h * 0.85f, cx + w * 0.17f, h);
        column.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, h * 0.72f, 0, h, body, FishArt.darken(body, 0.55f), Shader.TileMode.CLAMP));
        c.drawPath(column, p);
        p.setShader(null);
        p.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 70; i++) {
            float sx = cx + r(-0.21f, 0.21f) * w;
            float sy = h * 0.73f + r(-0.02f, 0.02f) * h;
            float spread = (sx - cx) / (w * 0.21f);
            float len = h * r(0.32f, 0.62f);
            float ex = sx + spread * w * r(0.12f, 0.3f) + r(-0.06f, 0.06f) * w;
            float ey = sy - len * (1f - Math.abs(spread) * 0.35f);
            Path t = new Path();
            t.moveTo(sx, sy);
            t.cubicTo(sx + r(-30, 30), sy - len * 0.4f, ex + r(-40, 40), ey + len * 0.3f, ex, ey);
            p.setStrokeWidth(w * r(0.012f, 0.02f));
            p.setColor(0xFFFFFFFF);
            p.setShader(new LinearGradient(sx, sy, ex, ey, FishArt.darken(tent, 0.75f), tent, Shader.TileMode.CLAMP));
            c.drawPath(t, p);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(tipCol);
            c.drawCircle(ex, ey, w * 0.011f, p);
            p.setStyle(Paint.Style.STROKE);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private void blades(int variant, int base, int tip, int count, float width) {
        int n = count + rnd.nextInt(4) + variant;
        for (int i = 0; i < n; i++) {
            float bx = w * 0.5f + r(-0.12f, 0.12f) * w;
            float len = h * r(0.55f, 0.97f);
            float lean = r(-0.28f, 0.28f) * w;
            float bw = w * width * r(0.75f, 1.2f);
            float tx = bx + lean;
            float ty = h - len;
            float cx = bx + lean * 0.2f + r(-0.05f, 0.05f) * w;
            Path b = new Path();
            b.moveTo(bx - bw * 0.5f, h);
            b.quadTo(cx - bw * 0.55f, h - len * 0.55f, tx - bw * 0.12f, ty + bw * 0.4f);
            b.quadTo(tx, ty - bw * 0.2f, tx + bw * 0.12f, ty + bw * 0.4f);
            b.quadTo(cx + bw * 0.55f, h - len * 0.55f, bx + bw * 0.5f, h);
            b.close();
            int shade = rnd.nextInt(3);
            int t = shade == 0 ? tip : lerp(tip, base, 0.35f * shade);
            p.setColor(0xFFFFFFFF);
            p.setShader(new LinearGradient(0, h, 0, ty, FishArt.darken(base, 0.7f), FishArt.alpha(t, 235), Shader.TileMode.CLAMP));
            c.drawPath(b, p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, bw * 0.12f));
            p.setColor(0x30FFFFFF);
            Path rib = new Path();
            rib.moveTo(bx, h);
            rib.quadTo(cx, h - len * 0.55f, tx, ty + bw * 0.6f);
            c.drawPath(rib, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    private void leaf(float x, float y, float angle, float len, float width, int base, int tip, boolean veins) {
        c.save();
        c.translate(x, y);
        c.rotate((float) Math.toDegrees(angle));
        Path l = new Path();
        l.moveTo(0, 0);
        l.cubicTo(len * 0.25f, -width, len * 0.75f, -width * 0.9f, len, 0);
        l.cubicTo(len * 0.75f, width * 0.9f, len * 0.25f, width, 0, 0);
        l.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, 0, len, 0, base, tip, Shader.TileMode.CLAMP));
        c.drawPath(l, p);
        p.setShader(null);
        if (veins) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.2f, width * 0.1f));
            p.setColor(0x40FFFFFF);
            c.drawLine(0, 0, len * 0.96f, 0, p);
            p.setStrokeWidth(Math.max(1f, width * 0.05f));
            p.setColor(0x26FFFFFF);
            for (float s = 0.15f; s < 0.9f; s += 0.11f) {
                c.drawLine(len * s, 0, len * (s + 0.09f), -width * 0.6f, p);
                c.drawLine(len * s, 0, len * (s + 0.09f), width * 0.6f, p);
            }
            p.setStyle(Paint.Style.FILL);
        }
        c.restore();
    }

    private void sword(int variant) {
        int base = 0xFF1F6B33;
        int tip = variant == 0 ? 0xFF74C365 : 0xFF8FD16A;
        int n = 11 + rnd.nextInt(4);
        float cx = w * 0.5f;
        for (int i = 0; i < n; i++) {
            float a = (float) (-Math.PI / 2) + (i / (float) (n - 1) - 0.5f) * 2.3f + r(-0.12f, 0.12f);
            float stem = h * r(0.12f, 0.28f);
            float sx = cx + (float) Math.cos(a) * stem;
            float sy = h - 6f + (float) Math.sin(a) * stem;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(w * 0.011f);
            p.setColor(FishArt.darken(base, 0.9f));
            c.drawLine(cx, h - 4f, sx, sy, p);
            p.setStyle(Paint.Style.FILL);
            leaf(sx, sy, a + r(-0.08f, 0.08f), h * r(0.38f, 0.62f), w * r(0.05f, 0.075f), base, tip, true);
        }
    }

    private void redStem(int variant) {
        int stems = 3 + rnd.nextInt(2);
        for (int s = 0; s < stems; s++) {
            float x = w * (0.3f + 0.4f * s / Math.max(1, stems - 1)) + r(-10, 10);
            float top = h * r(0.02f, 0.25f);
            float lean = r(-0.1f, 0.1f) * w;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(w * 0.018f);
            p.setColor(0xFFFFFFFF);
            p.setShader(new LinearGradient(0, h, 0, top, 0xFF2F6B2F, 0xFFB03A2E, Shader.TileMode.CLAMP));
            Path stem = new Path();
            stem.moveTo(x, h);
            stem.quadTo(x + lean * 0.3f, (h + top) / 2f, x + lean, top);
            c.drawPath(stem, p);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            for (float y = h - 14f; y > top + 6f; y -= h * 0.075f) {
                float f = (h - y) / (h - top);
                float lx = x + lean * f * f;
                int col = lerp(0xFF3A8A3A, variant == 0 ? 0xFFE8590C : 0xFFC2255C, f * f);
                float len = w * (0.3f - 0.1f * f);
                leaf(lx, y, (float) (-Math.PI / 2 - 1.15 + 0.25 * f), len, len * 0.24f, FishArt.darken(col, 0.8f), col, true);
                leaf(lx, y - 4f, (float) (-Math.PI / 2 + 1.15 - 0.25 * f), len, len * 0.24f, FishArt.darken(col, 0.8f), col, true);
            }
        }
    }

    private void driftwood(int variant) {
        int base = 0xFF5C3D2E;
        int light = 0xFF9C6B4A;
        float x0 = w * (variant == 0 ? 0.32f : 0.62f);
        float dir = variant == 0 ? 1f : -1f;
        p.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 3; i++) {
            float a = (float) (-Math.PI / 2) + dir * r(0.25f, 0.75f) + (i - 1) * 0.3f;
            woodBranch(x0 + r(-20, 20), h - 4f, a, h * r(0.3f, 0.42f), w * r(0.06f, 0.085f), 3, base, light);
        }
        woodBranch(x0 - dir * w * 0.12f, h - 2f, (float) (-Math.PI / 2) - dir * 0.9f, h * 0.22f, w * 0.05f, 1, base, light);
        p.setStyle(Paint.Style.FILL);
    }

    private void woodBranch(float x, float y, float angle, float len, float width, int depth, int base, int light) {
        float x2 = x + (float) Math.cos(angle) * len;
        float y2 = y + (float) Math.sin(angle) * len;
        Path seg = new Path();
        seg.moveTo(x, y);
        seg.cubicTo(x + (x2 - x) * 0.3f + r(-20, 20), y + (y2 - y) * 0.3f, x + (x2 - x) * 0.7f + r(-20, 20), y + (y2 - y) * 0.7f, x2, y2);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width);
        p.setColor(FishArt.darken(base, 0.75f));
        c.drawPath(seg, p);
        p.setStrokeWidth(width * 0.62f);
        p.setColor(base);
        c.drawPath(seg, p);
        p.setStrokeWidth(Math.max(1.5f, width * 0.12f));
        p.setColor(FishArt.alpha(light, 140));
        c.save();
        c.translate(-width * 0.18f, -width * 0.1f);
        c.drawPath(seg, p);
        c.restore();
        if (depth > 0) {
            int kids = 1 + rnd.nextInt(2);
            for (int i = 0; i < kids; i++) {
                woodBranch(x2, y2, angle + r(-0.7f, 0.7f), len * r(0.5f, 0.75f), width * 0.65f, depth - 1, base, light);
            }
        }
    }
}
