package com.jeremykenedy.aquariumlive;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Random;

/**
 * Paints each fish species, nose to the right, into a texture. Every shape
 * is drawn here from scratch, so no outside artwork is involved. Positions
 * are fractions of the texture width; y is measured from the vertical
 * centre, negative up.
 */
final class FishArt {

    static final int FORKED = 0;
    static final int ROUNDED = 1;
    static final int LUNATE = 2;
    static final int TRUNCATE = 3;
    static final int FLOWING = 4;
    static final int HETERO = 5;
    static final int FLUKE = 6;

    interface Painter {
        void paint(FishArt a);
    }

    /** One fin: front base, front tip, rear tip, rear base, and how much the outer edge bulges. */
    static final class Fin {
        final float ax;
        final float fx;
        final float fy;
        final float rx;
        final float ry;
        final float bx;
        final float bulge;

        Fin(float ax, float fx, float fy, float rx, float ry, float bx, float bulge) {
            this.ax = ax;
            this.fx = fx;
            this.fy = fy;
            this.rx = rx;
            this.ry = ry;
            this.bx = bx;
            this.bulge = bulge;
        }
    }

    static final class Look {
        float nose = 0.96f;
        float ped = 0.24f;
        float top = 0.12f;
        float bot = 0.11f;
        float peak = 0.55f;
        float pedH = 0.04f;
        float blunt = 2.3f;
        float noseY = 0f;
        int tail = FORKED;
        float tailLen = 0.2f;
        float tailH = 0.12f;
        Fin[] topFins = new Fin[0];
        Fin[] bottomFins = new Fin[0];
        Fin pectoral;
        int back;
        int side;
        int belly;
        int fin;
        int finAlpha = 200;
        int finEdgeAlpha = 90;
        int ray = 0x33000000;
        int tailColor;
        int edge;
        float edgeWidth;
        float eyeX = 0.86f;
        float eyeY = -0.03f;
        float eyeR = 0.028f;
        int iris = 0xFFB08D57;
        Painter pattern;
        boolean patternOverFins;
        Painter extras;
    }

    final int texW;
    final int texH;
    final float cy;
    final Canvas c;
    final Bitmap bitmap;
    final Look k;
    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    Path body;
    Path all;

    private FishArt(int texW, int texH, Look look) {
        this.texW = texW;
        this.texH = texH;
        this.cy = texH * 0.5f;
        this.k = look;
        this.bitmap = Bitmap.createBitmap(texW, texH, Bitmap.Config.ARGB_8888);
        this.c = new Canvas(bitmap);
    }

    private Config.Style style = Config.Style.REALISTIC;

    static Bitmap paint(Species s, Config.Style style) {
        FishArt a = new FishArt(s.texW, s.texH, look(s.id));
        a.style = style;
        a.draw();
        return ArtStyle.finish(a.bitmap, style);
    }

    float x(float f) {
        return f * texW;
    }

    float y(float f) {
        return cy + f * texW;
    }

    private float t(float xFrac) {
        return Math.max(0f, Math.min(1f, (xFrac - k.ped) / (k.nose - k.ped)));
    }

    private float profile(float t, float h) {
        if (t <= k.peak) {
            float s = t / k.peak;
            return k.pedH + (h - k.pedH) * (1f - (1f - s) * (1f - s));
        }
        float s = (t - k.peak) / (1f - k.peak);
        return h * (float) Math.sqrt(Math.max(0.0, 1.0 - Math.pow(s, k.blunt)));
    }

    private float mid(float t) {
        return k.noseY * t * t * t * t;
    }

    /** Body's upper edge at a given x, as a y fraction. */
    float topAt(float xFrac) {
        float t = t(xFrac);
        return mid(t) - profile(t, k.top);
    }

    float bottomAt(float xFrac) {
        float t = t(xFrac);
        return mid(t) + profile(t, k.bot);
    }

    float midAt(float xFrac) {
        return mid(t(xFrac));
    }

    private Path bodyPath() {
        Path path = new Path();
        int n = 64;
        for (int i = 0; i <= n; i++) {
            float tt = i / (float) n;
            float xf = k.ped + (k.nose - k.ped) * tt;
            float yv = y(mid(tt) - profile(tt, k.top));
            if (i == 0) {
                path.moveTo(x(xf), yv);
            } else {
                path.lineTo(x(xf), yv);
            }
        }
        for (int i = n; i >= 0; i--) {
            float tt = i / (float) n;
            float xf = k.ped + (k.nose - k.ped) * tt;
            path.lineTo(x(xf), y(mid(tt) + profile(tt, k.bot)));
        }
        path.close();
        return path;
    }

    private Path finPath(Fin f, boolean top) {
        float sign = top ? -1f : 1f;
        float ay = y(midAt(f.ax) + sign * 0.45f * Math.abs((top ? topAt(f.ax) : bottomAt(f.ax)) - midAt(f.ax)));
        float by = y(midAt(f.bx) + sign * 0.45f * Math.abs((top ? topAt(f.bx) : bottomAt(f.bx)) - midAt(f.bx)));
        float ax = x(f.ax);
        float bx = x(f.bx);
        float fx = x(f.fx);
        float fy = y(f.fy);
        float rx = x(f.rx);
        float ry = y(f.ry);
        Path path = new Path();
        path.moveTo(ax, ay);
        path.quadTo(ax + (fx - ax) * 0.15f, fy + (ay - fy) * 0.25f, fx, fy);
        path.quadTo((fx + rx) * 0.5f, Math.min(fy, ry) * (top ? 1f : 0f) + Math.max(fy, ry) * (top ? 0f : 1f) + sign * f.bulge * texW, rx, ry);
        path.quadTo(rx + (bx - rx) * 0.7f, ry + (by - ry) * 0.45f, bx, by);
        path.close();
        return path;
    }

    private Path tailPath() {
        float x0 = x(k.ped) + 6f;
        float pTop = y(topAt(k.ped));
        float pBot = y(bottomAt(k.ped));
        float len = k.tailLen * texW;
        float xt = x0 - 6f - len;
        float th = k.tailH * texW;
        Path path = new Path();
        path.moveTo(x0, pTop);
        switch (k.tail) {
            case ROUNDED:
                path.cubicTo(x0 - len * 0.5f, cy - th * 0.95f, xt - len * 0.05f, cy - th * 0.7f, xt, cy);
                path.cubicTo(xt - len * 0.05f, cy + th * 0.7f, x0 - len * 0.5f, cy + th * 0.95f, x0, pBot);
                break;
            case LUNATE:
                path.cubicTo(x0 - len * 0.35f, pTop - th * 0.1f, xt + len * 0.2f, cy - th * 0.75f, xt, cy - th);
                path.quadTo(xt + len * 0.55f, cy, xt, cy + th);
                path.cubicTo(xt + len * 0.2f, cy + th * 0.75f, x0 - len * 0.35f, pBot + th * 0.1f, x0, pBot);
                break;
            case TRUNCATE:
                path.cubicTo(x0 - len * 0.4f, pTop - th * 0.2f, xt + len * 0.15f, cy - th, xt, cy - th * 0.95f);
                path.quadTo(xt - len * 0.12f, cy, xt, cy + th * 0.95f);
                path.cubicTo(xt + len * 0.15f, cy + th, x0 - len * 0.4f, pBot + th * 0.2f, x0, pBot);
                break;
            case HETERO:
                path.cubicTo(x0 - len * 0.3f, pTop - th * 0.3f, xt + len * 0.2f, cy - th * 0.9f, xt, cy - th);
                path.quadTo(xt + len * 0.3f, cy - th * 0.3f, xt + len * 0.55f, cy + th * 0.02f);
                path.quadTo(xt + len * 0.5f, cy + th * 0.3f, xt + len * 0.38f, cy + th * 0.5f);
                path.cubicTo(xt + len * 0.62f, cy + th * 0.35f, x0 - len * 0.25f, pBot + th * 0.05f, x0, pBot);
                break;
            case FLUKE:
                path.quadTo(x0 - len * 0.55f, cy - th * 0.32f, xt, cy - th * 0.5f);
                path.quadTo(xt + len * 0.1f, cy - th * 0.05f, xt + len * 0.04f, cy + th * 0.28f);
                path.quadTo(x0 - len * 0.5f, cy + th * 0.15f, x0, pBot);
                break;
            case FLOWING:
                path.cubicTo(x0 - len * 0.25f, cy - th * 0.85f, xt + len * 0.05f, cy - th * 1.05f, xt - len * 0.02f, cy - th * 0.15f);
                path.cubicTo(xt - len * 0.05f, cy + th * 0.6f, xt + len * 0.35f, cy + th * 1.15f, x0 - len * 0.45f, cy + th * 0.75f);
                path.cubicTo(x0 - len * 0.2f, cy + th * 0.45f, x0 - len * 0.05f, pBot + th * 0.1f, x0, pBot);
                break;
            default:
                path.cubicTo(x0 - len * 0.4f, pTop - th * 0.25f, xt + len * 0.15f, cy - th * 0.9f, xt, cy - th);
                path.quadTo(xt + len * 0.38f, cy - th * 0.3f, xt + len * 0.55f, cy);
                path.quadTo(xt + len * 0.38f, cy + th * 0.3f, xt, cy + th);
                path.cubicTo(xt + len * 0.15f, cy + th * 0.9f, x0 - len * 0.4f, pBot + th * 0.25f, x0, pBot);
                break;
        }
        path.close();
        return path;
    }

    private void fillFin(Path path, float ox, float oy, int color) {
        RectF r = new RectF();
        path.computeBounds(r, true);
        float radius = Math.max(8f, farthest(r, ox, oy));
        int a0 = Color.argb(k.finAlpha, Color.red(color), Color.green(color), Color.blue(color));
        int a1 = Color.argb(k.finEdgeAlpha, Color.red(color), Color.green(color), Color.blue(color));
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(ox, oy, radius, new int[] {a0, a0, a1}, new float[] {0f, 0.35f, 1f}, Shader.TileMode.CLAMP));
        c.drawPath(path, p);
        p.setShader(null);

        if (Color.alpha(k.ray) == 0 || !ArtStyle.fineDetail(style)) {
            if (k.edgeWidth > 0f) {
                p.setStyle(Paint.Style.STROKE);
                p.setColor(k.edge);
                p.setStrokeWidth(k.edgeWidth);
                c.drawPath(path, p);
                p.setStyle(Paint.Style.FILL);
            }
            return;
        }
        c.save();
        c.clipPath(path);
        double a1Angle = Math.atan2(r.top - oy, r.left - ox);
        double[] angles = {
            Math.atan2(r.top - oy, r.left - ox), Math.atan2(r.top - oy, r.right - ox),
            Math.atan2(r.bottom - oy, r.left - ox), Math.atan2(r.bottom - oy, r.right - ox),
        };
        double lo = a1Angle;
        double hi = a1Angle;
        for (double a : angles) {
            double d = a - a1Angle;
            while (d > Math.PI) {
                d -= Math.PI * 2;
            }
            while (d < -Math.PI) {
                d += Math.PI * 2;
            }
            lo = Math.min(lo, a1Angle + d);
            hi = Math.max(hi, a1Angle + d);
        }
        int rays = Math.max(6, (int) ((hi - lo) * radius / 9f));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1.2f, texW / 420f));
        p.setColor(k.ray);
        for (int i = 0; i <= rays; i++) {
            double a = lo + (hi - lo) * i / rays;
            c.drawLine(ox, oy, ox + (float) Math.cos(a) * radius * 1.2f, oy + (float) Math.sin(a) * radius * 1.2f, p);
        }
        c.restore();
        if (k.edgeWidth > 0f) {
            p.setColor(k.edge);
            p.setStrokeWidth(k.edgeWidth);
            p.setStrokeJoin(Paint.Join.ROUND);
            c.drawPath(path, p);
        }
        p.setStyle(Paint.Style.FILL);
    }

    private static float farthest(RectF r, float x, float y) {
        float dx = Math.max(Math.abs(r.left - x), Math.abs(r.right - x));
        float dy = Math.max(Math.abs(r.top - y), Math.abs(r.bottom - y));
        return (float) Math.hypot(dx, dy);
    }

    private void draw() {
        body = bodyPath();
        all = new Path(body);
        Path tail = tailPath();
        all.op(tail, Path.Op.UNION);
        Path[] tops = new Path[k.topFins.length];
        Path[] bottoms = new Path[k.bottomFins.length];
        for (int i = 0; i < tops.length; i++) {
            tops[i] = finPath(k.topFins[i], true);
            all.op(tops[i], Path.Op.UNION);
        }
        for (int i = 0; i < bottoms.length; i++) {
            bottoms[i] = finPath(k.bottomFins[i], false);
            all.op(bottoms[i], Path.Op.UNION);
        }

        fillFin(tail, x(k.ped) + 8f, y(midAt(k.ped)), k.tailColor != 0 ? k.tailColor : k.fin);
        for (int i = 0; i < tops.length; i++) {
            Fin f = k.topFins[i];
            fillFin(tops[i], x((f.ax + f.bx) * 0.5f), y(midAt((f.ax + f.bx) * 0.5f)), k.fin);
        }
        for (int i = 0; i < bottoms.length; i++) {
            Fin f = k.bottomFins[i];
            fillFin(bottoms[i], x((f.ax + f.bx) * 0.5f), y(midAt((f.ax + f.bx) * 0.5f)), k.fin);
        }

        RectF b = new RectF();
        body.computeBounds(b, true);
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, b.top, 0, b.bottom, new int[] {k.back, k.side, k.belly},
                new float[] {0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
        c.drawPath(body, p);
        p.setShader(null);

        if (k.pattern != null) {
            c.save();
            c.clipPath(k.patternOverFins ? all : body);
            k.pattern.paint(this);
            c.restore();
        }

        shadeBody(b);
        drawGill(b);
        drawEye();

        if (k.pectoral != null) {
            Fin f = k.pectoral;
            Path path = new Path();
            float ax = x(f.ax);
            float ay = y(f.fy * 0.3f + midAt(f.ax));
            path.moveTo(ax, ay);
            path.quadTo(x(f.fx) + (ax - x(f.fx)) * 0.4f, y(f.fy) - texW * 0.01f, x(f.fx), y(f.fy));
            path.quadTo(x(f.rx) - texW * f.bulge, y((f.fy + f.ry) * 0.5f), x(f.rx), y(f.ry));
            path.quadTo(x(f.bx) - texW * 0.01f, y(f.ry), ax, ay + texW * 0.012f);
            path.close();
            int saved = k.finAlpha;
            k.finAlpha = Math.min(saved, 170);
            int savedEdge = k.edgeWidth > 0f ? 1 : 0;
            float ew = k.edgeWidth;
            k.edgeWidth = 0f;
            fillFin(path, ax, ay, k.fin);
            k.edgeWidth = savedEdge == 1 ? ew : 0f;
            k.finAlpha = saved;
        }
        if (k.extras != null) {
            k.extras.paint(this);
        }
    }

    private void shadeBody(RectF b) {
        c.save();
        c.clipPath(body);
        float cx = b.centerX() + b.width() * 0.05f;
        float ccy = b.centerY() - b.height() * 0.12f;
        float rx = b.width() * 0.62f;
        float ry = b.height() * 0.66f;
        RadialGradient edgeShade = new RadialGradient(0f, 0f, 1f,
                new int[] {0x00000000, 0x00000000, 0x55000000}, new float[] {0f, 0.62f, 1f}, Shader.TileMode.CLAMP);
        Matrix m = new Matrix();
        m.setScale(rx, ry);
        m.postTranslate(cx, ccy);
        edgeShade.setLocalMatrix(m);
        p.setShader(edgeShade);
        c.drawRect(b, p);

        RadialGradient shine = new RadialGradient(0f, 0f, 1f,
                new int[] {0x40FFFFFF, 0x00FFFFFF}, new float[] {0f, 1f}, Shader.TileMode.CLAMP);
        Matrix m2 = new Matrix();
        m2.setScale(b.width() * 0.32f, b.height() * 0.2f);
        m2.postTranslate(b.left + b.width() * 0.62f, b.top + b.height() * 0.3f);
        shine.setLocalMatrix(m2);
        p.setShader(shine);
        c.drawRect(b, p);
        p.setShader(null);

        if (style == Config.Style.MOVIE) {
            gloss(b);
        }
        if (!ArtStyle.fineDetail(style)) {
            p.setStyle(Paint.Style.FILL);
            c.restore();
            return;
        }
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(1f, texW / 512f));
        p.setColor(0x10000000);
        float step = texW * 0.022f;
        for (float sx = b.left; sx < b.right; sx += step) {
            for (float sy = b.top; sy < b.bottom; sy += step * 0.9f) {
                float off = ((int) ((sx - b.left) / step) % 2) * step * 0.45f;
                c.drawArc(new RectF(sx - step * 0.6f, sy + off - step * 0.5f, sx + step * 0.6f, sy + off + step * 0.5f), 100f, 160f, false, p);
            }
        }
        p.setStyle(Paint.Style.FILL);
        c.restore();
    }

    private void drawGill(RectF b) {
        float gx = x(k.eyeX - (k.nose - k.eyeX) * 1.3f);
        float topY = y(topAt(gx / texW) * 0.7f + midAt(gx / texW) * 0.3f);
        float botY = y(bottomAt(gx / texW) * 0.7f + midAt(gx / texW) * 0.3f);
        Path g = new Path();
        g.moveTo(gx + texW * 0.01f, topY);
        g.quadTo(gx - texW * 0.025f, (topY + botY) * 0.5f, gx + texW * 0.01f, botY);
        c.save();
        c.clipPath(body);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(texW * 0.006f);
        p.setColor(0x30000000);
        c.drawPath(g, p);
        p.setStyle(Paint.Style.FILL);
        c.restore();
    }

    private void drawEye() {
        float ex = x(k.eyeX);
        float ey = y(k.eyeY + midAt(k.eyeX));
        float r = k.eyeR * texW;
        if (ArtStyle.bigEyes(style) && r > texW * 0.012f) {
            drawCartoonEye(ex, ey, Math.max(r * 1.8f, texW * 0.03f));
            return;
        }
        p.setColor(0x40000000);
        c.drawCircle(ex, ey, r * 1.18f, p);
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(ex, ey, r, new int[] {darken(k.iris, 0.6f), k.iris, darken(k.iris, 0.7f)},
                new float[] {0.4f, 0.75f, 1f}, Shader.TileMode.CLAMP));
        c.drawCircle(ex, ey, r, p);
        p.setShader(null);
        p.setColor(0xFF0B0B0F);
        c.drawCircle(ex + r * 0.05f, ey, r * 0.55f, p);
        p.setColor(0xDDFFFFFF);
        c.drawCircle(ex + r * 0.28f, ey - r * 0.3f, r * 0.2f, p);
        p.setColor(0x66FFFFFF);
        c.drawCircle(ex - r * 0.2f, ey + r * 0.25f, r * 0.09f, p);

        float mx = x(k.nose) - texW * 0.004f;
        float my = y(midAt(k.nose) + k.bot * 0.12f);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(texW * 0.004f);
        p.setColor(0x55000000);
        c.drawLine(mx, my, mx - texW * 0.025f, my + texW * 0.004f, p);
        p.setStyle(Paint.Style.FILL);
    }

    /** Big expressive eye: white, a large coloured iris and pupil looking forward, catch-lights. */
    private void drawCartoonEye(float ex, float ey, float r) {
        boolean movie = style == Config.Style.MOVIE;
        p.setColor(style == Config.Style.PAINTED ? 0xFF1C2E4A : 0xFF14161C);
        c.drawCircle(ex, ey, r * 1.1f, p);
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(ex - r * 0.2f, ey - r * 0.3f, r * 1.2f, 0xFFFFFFFF, 0xFFD8DEE6, Shader.TileMode.CLAMP));
        c.drawCircle(ex, ey, r, p);
        p.setShader(null);
        float irisR = r * (movie ? 0.7f : 0.6f);
        float ix = ex + r * 0.26f;
        if (movie) {
            p.setColor(0xFFFFFFFF);
            p.setShader(new RadialGradient(ix, ey + r * 0.15f, irisR, lighten(k.iris, 1.4f), darken(k.iris, 0.7f), Shader.TileMode.CLAMP));
        } else {
            p.setColor(darken(k.iris, 0.9f));
        }
        c.drawCircle(ix, ey + r * 0.05f, irisR, p);
        p.setShader(null);
        p.setColor(0xFF0A0A0E);
        c.drawCircle(ix + r * 0.06f, ey + r * 0.05f, irisR * 0.62f, p);
        p.setColor(0xFFFFFFFF);
        c.drawCircle(ex + r * 0.12f, ey - r * 0.24f, r * (movie ? 0.24f : 0.2f), p);
        c.drawCircle(ex + r * 0.5f, ey + r * 0.26f, r * 0.09f, p);
        if (style == Config.Style.PAINTED) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(texW * 0.006f);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(0xFF1C2E4A);
            c.drawArc(new RectF(ex - r * 1.1f, ey - r * 1.15f, ex + r * 1.1f, ey + r * 1.05f), 200f, 140f, false, p);
            p.setStyle(Paint.Style.FILL);
        }
        if (style == Config.Style.CARTOON || style == Config.Style.ANIMATED) {
            float mx = x(k.nose) - texW * 0.006f;
            float my = y(midAt(k.nose) + k.bot * 0.18f);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(texW * 0.006f);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(0xCC14161C);
            c.drawArc(new RectF(mx - texW * 0.05f, my - texW * 0.03f, mx, my + texW * 0.012f), 20f, 70f, false, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    /** The 3D-film look: a strong soft highlight and a cool rim light along the back. */
    private void gloss(RectF b) {
        RadialGradient shine = new RadialGradient(0f, 0f, 1f, new int[] {0x88FFFFFF, 0x22FFFFFF, 0x00FFFFFF}, new float[] {0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        Matrix m = new Matrix();
        m.setScale(b.width() * 0.22f, b.height() * 0.14f);
        m.postTranslate(b.left + b.width() * 0.66f, b.top + b.height() * 0.26f);
        shine.setLocalMatrix(m);
        p.setShader(shine);
        c.drawRect(b, p);
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, b.top, 0, b.top + b.height() * 0.18f, 0x55BFE6FF, 0x00BFE6FF, Shader.TileMode.CLAMP));
        c.drawRect(b, p);
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, b.bottom - b.height() * 0.25f, 0, b.bottom, 0x00FFE8C8, 0x40FFE8C8, Shader.TileMode.CLAMP));
        c.drawRect(b, p);
        p.setShader(null);
    }

    static int lighten(int color, float f) {
        return Color.argb(Color.alpha(color), Math.min(255, (int) (Color.red(color) * f)), Math.min(255, (int) (Color.green(color) * f)), Math.min(255, (int) (Color.blue(color) * f)));
    }

    static int darken(int color, float f) {
        return Color.argb(Color.alpha(color), (int) (Color.red(color) * f), (int) (Color.green(color) * f), (int) (Color.blue(color) * f));
    }

    static int alpha(int color, int a) {
        return (color & 0x00FFFFFF) | (a << 24);
    }

    // Pattern helpers, all in texture-width fractions.

    void band(float xf, float width, int color, float curve) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width * texW);
        p.setStrokeCap(Paint.Cap.BUTT);
        p.setColor(color);
        Path b = new Path();
        b.moveTo(x(xf), y(-0.5f));
        b.quadTo(x(xf + curve), cy, x(xf), y(0.5f));
        c.drawPath(b, p);
        p.setStyle(Paint.Style.FILL);
    }

    void stroke(int color, float width, float... pts) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width * texW);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setColor(color);
        Path path = new Path();
        path.moveTo(x(pts[0]), y(pts[1]));
        if (pts.length == 8) {
            path.cubicTo(x(pts[2]), y(pts[3]), x(pts[4]), y(pts[5]), x(pts[6]), y(pts[7]));
        } else if (pts.length == 6) {
            path.quadTo(x(pts[2]), y(pts[3]), x(pts[4]), y(pts[5]));
        } else {
            for (int i = 2; i + 1 < pts.length; i += 2) {
                path.lineTo(x(pts[i]), y(pts[i + 1]));
            }
        }
        c.drawPath(path, p);
        p.setStyle(Paint.Style.FILL);
    }

    void oval(int color, float cxf, float cyf, float rxf, float ryf) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        c.drawOval(new RectF(x(cxf - rxf), y(cyf - ryf), x(cxf + rxf), y(cyf + ryf)), p);
    }

    void hGradient(float x0, float x1, int[] colors, float[] stops) {
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(x(x0), 0f, x(x1), 0f, colors, stops, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, texW, texH, p);
        p.setShader(null);
    }

    static Fin fin(float ax, float fx, float fy, float rx, float ry, float bx, float bulge) {
        return new Fin(ax, fx, fy, rx, ry, bx, bulge);
    }

    static Look look(String id) {
        Look k = new Look();
        switch (id) {
            case "clownfish":
                k.ped = 0.22f;
                k.top = 0.125f;
                k.bot = 0.118f;
                k.pedH = 0.042f;
                k.blunt = 2.6f;
                k.noseY = 0.01f;
                k.tail = ROUNDED;
                k.tailLen = 0.19f;
                k.tailH = 0.11f;
                k.topFins = new Fin[] {fin(0.70f, 0.66f, -0.172f, 0.53f, -0.168f, 0.50f, 0.012f), fin(0.51f, 0.47f, -0.19f, 0.33f, -0.15f, 0.30f, 0.02f)};
                k.bottomFins = new Fin[] {fin(0.47f, 0.43f, 0.175f, 0.33f, 0.14f, 0.30f, 0.02f), fin(0.70f, 0.61f, 0.18f, 0.60f, 0.175f, 0.63f, 0f)};
                k.pectoral = fin(0.73f, 0.62f, 0.05f, 0.60f, 0.09f, 0.70f, 0.01f);
                k.back = 0xFFD9480F;
                k.side = 0xFFF76707;
                k.belly = 0xFFFF922B;
                k.fin = 0xFFF76707;
                k.finAlpha = 245;
                k.finEdgeAlpha = 230;
                k.edge = 0xFF141414;
                k.edgeWidth = 5f;
                k.eyeX = 0.865f;
                k.eyeY = -0.035f;
                k.eyeR = 0.03f;
                k.iris = 0xFFE8590C;
                k.pattern = a -> {
                    int edge = 0xFF141414;
                    a.band(0.765f, 0.085f, edge, 0.03f);
                    a.band(0.765f, 0.062f, 0xFFFDFDFD, 0.03f);
                    a.band(0.52f, 0.09f, edge, 0.04f);
                    a.band(0.52f, 0.066f, 0xFFFDFDFD, 0.04f);
                    a.band(0.262f, 0.055f, edge, 0.01f);
                    a.band(0.262f, 0.034f, 0xFFFDFDFD, 0.01f);
                };
                break;
            case "blue_tang":
                k.ped = 0.24f;
                k.nose = 0.95f;
                k.top = 0.19f;
                k.bot = 0.18f;
                k.peak = 0.5f;
                k.pedH = 0.05f;
                k.blunt = 2.0f;
                k.noseY = 0.02f;
                k.tail = LUNATE;
                k.tailLen = 0.2f;
                k.tailH = 0.17f;
                k.topFins = new Fin[] {fin(0.80f, 0.74f, -0.235f, 0.32f, -0.13f, 0.30f, 0.03f)};
                k.bottomFins = new Fin[] {fin(0.62f, 0.58f, 0.225f, 0.32f, 0.12f, 0.30f, 0.02f), fin(0.74f, 0.66f, 0.23f, 0.65f, 0.22f, 0.68f, 0f)};
                k.pectoral = fin(0.74f, 0.60f, 0.07f, 0.58f, 0.11f, 0.70f, 0.01f);
                k.back = 0xFF1A3A9E;
                k.side = 0xFF2F6BE0;
                k.belly = 0xFF5C9DF2;
                k.fin = 0xFF1C3FAA;
                k.finAlpha = 245;
                k.finEdgeAlpha = 220;
                k.tailColor = 0xFFFFD43B;
                k.edge = 0xFF0A0A14;
                k.edgeWidth = 4f;
                k.eyeX = 0.86f;
                k.eyeY = -0.04f;
                k.eyeR = 0.03f;
                k.iris = 0xFF364FC7;
                k.pattern = a -> {
                    int ink = 0xFF0B0B18;
                    a.stroke(ink, 0.06f, 0.85f, -0.06f, 0.72f, -0.18f, 0.45f, -0.17f, 0.29f, -0.05f);
                    a.stroke(ink, 0.055f, 0.29f, -0.03f, 0.42f, 0.08f, 0.60f, 0.07f, 0.67f, -0.03f);
                    Path tri = new Path();
                    tri.moveTo(a.x(0.22f), a.y(-0.06f));
                    tri.lineTo(a.x(0.34f), a.y(0f));
                    tri.lineTo(a.x(0.22f), a.y(0.06f));
                    tri.close();
                    a.p.setColor(0xFFFFD43B);
                    a.c.drawPath(tri, a.p);
                };
                break;
            case "yellow_tang":
                k.ped = 0.27f;
                k.nose = 0.92f;
                k.top = 0.25f;
                k.bot = 0.23f;
                k.peak = 0.5f;
                k.pedH = 0.05f;
                k.blunt = 1.45f;
                k.noseY = -0.02f;
                k.tail = LUNATE;
                k.tailLen = 0.14f;
                k.tailH = 0.13f;
                k.topFins = new Fin[] {fin(0.72f, 0.66f, -0.36f, 0.37f, -0.25f, 0.33f, 0.05f)};
                k.bottomFins = new Fin[] {fin(0.64f, 0.60f, 0.34f, 0.37f, 0.23f, 0.33f, 0.04f), fin(0.70f, 0.64f, 0.30f, 0.63f, 0.29f, 0.66f, 0f)};
                k.pectoral = fin(0.74f, 0.62f, 0.04f, 0.60f, 0.08f, 0.70f, 0.01f);
                k.back = 0xFFF59F00;
                k.side = 0xFFFCC419;
                k.belly = 0xFFFFE066;
                k.fin = 0xFFFCC419;
                k.finAlpha = 245;
                k.finEdgeAlpha = 200;
                k.ray = 0x22B07000;
                k.eyeX = 0.80f;
                k.eyeY = -0.06f;
                k.eyeR = 0.03f;
                k.iris = 0xFF5C3B00;
                k.pattern = a -> {
                    a.p.setStyle(Paint.Style.STROKE);
                    a.p.setStrokeWidth(a.texW * 0.004f);
                    a.p.setColor(0x14A05A00);
                    for (float xf = 0.3f; xf < 0.9f; xf += 0.018f) {
                        a.c.drawLine(a.x(xf), 0, a.x(xf - 0.02f), a.texH, a.p);
                    }
                    a.p.setStyle(Paint.Style.FILL);
                    a.oval(0xFFFFFFFF, 0.30f, 0.005f, 0.022f, 0.008f);
                };
                break;
            case "royal_gramma":
                k.ped = 0.22f;
                k.top = 0.11f;
                k.bot = 0.10f;
                k.peak = 0.5f;
                k.pedH = 0.04f;
                k.blunt = 2.4f;
                k.tail = ROUNDED;
                k.tailLen = 0.19f;
                k.tailH = 0.1f;
                k.topFins = new Fin[] {fin(0.72f, 0.68f, -0.155f, 0.30f, -0.135f, 0.28f, 0.01f)};
                k.bottomFins = new Fin[] {fin(0.50f, 0.46f, 0.15f, 0.30f, 0.12f, 0.28f, 0.01f), fin(0.72f, 0.62f, 0.17f, 0.61f, 0.165f, 0.66f, 0f)};
                k.pectoral = fin(0.75f, 0.64f, 0.04f, 0.62f, 0.07f, 0.71f, 0.01f);
                k.back = 0xFF862E9C;
                k.side = 0xFFAE3EC9;
                k.belly = 0xFFCC5DE8;
                k.fin = 0xFFF2B01E;
                k.finAlpha = 235;
                k.tailColor = 0xFFFCC419;
                k.eyeX = 0.87f;
                k.eyeR = 0.026f;
                k.iris = 0xFF2B2B2B;
                k.patternOverFins = true;
                k.pattern = a -> {
                    a.hGradient(0.40f, 0.56f, new int[] {0xFFF5B800, 0x00F5B800}, new float[] {0f, 1f});
                    a.stroke(0xCCFFD43B, 0.008f, 0.95f, -0.055f, 0.87f, -0.03f, 0.80f, -0.005f);
                    a.oval(0xFF141414, 0.66f, -0.12f, 0.022f, 0.02f);
                };
                break;
            case "moorish_idol":
                k.ped = 0.27f;
                k.nose = 0.90f;
                k.top = 0.24f;
                k.bot = 0.22f;
                k.peak = 0.48f;
                k.pedH = 0.045f;
                k.blunt = 1.35f;
                k.noseY = 0.03f;
                k.tail = LUNATE;
                k.tailLen = 0.13f;
                k.tailH = 0.13f;
                k.topFins = new Fin[] {fin(0.64f, 0.56f, -0.40f, 0.50f, -0.34f, 0.40f, 0f)};
                k.bottomFins = new Fin[] {fin(0.60f, 0.52f, 0.38f, 0.47f, 0.31f, 0.38f, 0f), fin(0.70f, 0.64f, 0.28f, 0.63f, 0.27f, 0.66f, 0f)};
                k.back = 0xFFF8F4E3;
                k.side = 0xFFFDFBF2;
                k.belly = 0xFFF5F0DC;
                k.fin = 0xFFF8F4E3;
                k.finAlpha = 245;
                k.tailColor = 0xFF161616;
                k.eyeX = 0.78f;
                k.eyeY = -0.05f;
                k.eyeR = 0.024f;
                k.iris = 0xFFC9A227;
                k.patternOverFins = true;
                k.pattern = a -> {
                    a.hGradient(0.24f, 0.42f, new int[] {0xFFFCC419, 0xFFFCC419, 0x00FCC419}, new float[] {0f, 0.75f, 1f});
                    a.p.setColor(0xFFFFFFFF);
                    a.p.setShader(new LinearGradient(0, a.y(-0.3f), 0, a.y(0.05f), 0x66FCC419, 0x00FCC419, Shader.TileMode.CLAMP));
                    a.c.drawRect(a.x(0.55f), 0, a.x(0.72f), a.texH, a.p);
                    a.p.setShader(null);
                    a.band(0.745f, 0.075f, 0xFF161616, 0.02f);
                    a.band(0.47f, 0.13f, 0xFF161616, 0.03f);
                    a.band(0.285f, 0.04f, 0xFF161616, 0f);
                    a.oval(0xFFF08C00, 0.855f, -0.045f, 0.03f, 0.022f);
                };
                k.extras = a -> {
                    a.stroke(0xEEF8F4E3, 0.012f, 0.555f, -0.40f, 0.42f, -0.47f, 0.25f, -0.42f, 0.10f, -0.33f);
                };
                break;
            case "chromis":
                k.ped = 0.25f;
                k.top = 0.11f;
                k.bot = 0.095f;
                k.peak = 0.56f;
                k.pedH = 0.035f;
                k.tail = FORKED;
                k.tailLen = 0.23f;
                k.tailH = 0.14f;
                k.topFins = new Fin[] {fin(0.72f, 0.68f, -0.16f, 0.34f, -0.12f, 0.32f, 0.01f)};
                k.bottomFins = new Fin[] {fin(0.50f, 0.46f, 0.14f, 0.34f, 0.11f, 0.32f, 0.01f), fin(0.72f, 0.64f, 0.14f, 0.63f, 0.135f, 0.66f, 0f)};
                k.pectoral = fin(0.76f, 0.66f, 0.03f, 0.64f, 0.06f, 0.72f, 0.01f);
                k.back = 0xFF0CA678;
                k.side = 0xFF38D9A9;
                k.belly = 0xFFC3FAE8;
                k.fin = 0xFF63E6BE;
                k.finAlpha = 170;
                k.finEdgeAlpha = 60;
                k.eyeX = 0.88f;
                k.eyeY = -0.025f;
                k.eyeR = 0.032f;
                k.iris = 0xFF1F3A35;
                k.pattern = a -> a.oval(0x33E6FCF5, 0.62f, -0.04f, 0.2f, 0.05f);
                break;
            case "neon_tetra":
                k.ped = 0.25f;
                k.top = 0.08f;
                k.bot = 0.075f;
                k.pedH = 0.03f;
                k.blunt = 2.4f;
                k.tail = FORKED;
                k.tailLen = 0.22f;
                k.tailH = 0.095f;
                k.topFins = new Fin[] {fin(0.58f, 0.55f, -0.135f, 0.47f, -0.11f, 0.46f, 0f), fin(0.33f, 0.31f, -0.08f, 0.29f, -0.075f, 0.285f, 0f)};
                k.bottomFins = new Fin[] {fin(0.42f, 0.40f, 0.12f, 0.30f, 0.09f, 0.29f, 0.01f)};
                k.back = 0xFF6B6150;
                k.side = 0xFF9AA0A6;
                k.belly = 0xFFF1F3F5;
                k.fin = 0xFFDDE3EA;
                k.finAlpha = 110;
                k.finEdgeAlpha = 30;
                k.ray = 0x18000000;
                k.eyeX = 0.875f;
                k.eyeY = -0.012f;
                k.eyeR = 0.032f;
                k.iris = 0xFF7AA6C2;
                k.pattern = a -> {
                    a.p.setColor(0xFFFFFFFF);
                    a.p.setShader(new LinearGradient(a.x(0.3f), 0, a.x(0.62f), 0, 0xFFE03131, 0x00E03131, Shader.TileMode.CLAMP));
                    a.c.drawRect(a.x(0.2f), a.y(0.012f), a.x(0.7f), a.texH, a.p);
                    a.p.setShader(null);
                    a.stroke(0x5522B8CF, 0.05f, 0.86f, -0.018f, 0.6f, -0.022f, 0.33f, -0.004f);
                    a.p.setColor(0xFFFFFFFF);
                    a.p.setShader(new LinearGradient(a.x(0.33f), 0, a.x(0.86f), 0, 0xFF15AABF, 0xFF4DABF7, Shader.TileMode.CLAMP));
                    a.stroke(0xFFFFFFFF, 0.028f, 0.86f, -0.018f, 0.6f, -0.022f, 0.33f, -0.004f);
                    a.p.setShader(null);
                };
                break;
            case "rummynose":
                k.ped = 0.25f;
                k.top = 0.085f;
                k.bot = 0.078f;
                k.pedH = 0.03f;
                k.blunt = 2.3f;
                k.tail = FORKED;
                k.tailLen = 0.22f;
                k.tailH = 0.1f;
                k.topFins = new Fin[] {fin(0.58f, 0.55f, -0.14f, 0.47f, -0.115f, 0.46f, 0f), fin(0.33f, 0.31f, -0.085f, 0.29f, -0.08f, 0.285f, 0f)};
                k.bottomFins = new Fin[] {fin(0.42f, 0.40f, 0.125f, 0.30f, 0.095f, 0.29f, 0.01f)};
                k.back = 0xFF8C9399;
                k.side = 0xFFCED4DA;
                k.belly = 0xFFF8F9FA;
                k.fin = 0xFFE9ECEF;
                k.finAlpha = 120;
                k.finEdgeAlpha = 40;
                k.ray = 0x18000000;
                k.tailColor = 0xFFF8F9FA;
                k.eyeX = 0.875f;
                k.eyeY = -0.012f;
                k.eyeR = 0.03f;
                k.iris = 0xFFC92A2A;
                k.pattern = a -> {
                    a.p.setColor(0xFFFFFFFF);
                    a.p.setShader(new RadialGradient(a.x(0.94f), a.y(-0.01f), a.texW * 0.17f,
                            new int[] {0xFFE03131, 0xCCE03131, 0x00E03131}, new float[] {0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
                    a.c.drawRect(0, 0, a.texW, a.texH, a.p);
                    a.p.setShader(null);
                };
                k.extras = a -> {
                    Path clip = new Path();
                    clip.addRect(0, 0, a.x(a.k.ped + 0.02f), a.texH, Path.Direction.CW);
                    clip.op(a.all, Path.Op.INTERSECT);
                    a.c.save();
                    a.c.clipPath(clip);
                    a.stroke(0xEE141414, 0.03f, 0.27f, 0f, 0.10f, 0f);
                    a.stroke(0xEE141414, 0.024f, 0.20f, -0.045f, 0.06f, -0.085f);
                    a.stroke(0xEE141414, 0.024f, 0.20f, 0.045f, 0.06f, 0.085f);
                    a.c.restore();
                };
                break;
            case "angelfish":
                k.ped = 0.32f;
                k.nose = 0.86f;
                k.top = 0.2f;
                k.bot = 0.19f;
                k.pedH = 0.045f;
                k.blunt = 1.8f;
                k.tail = ROUNDED;
                k.tailLen = 0.17f;
                k.tailH = 0.15f;
                k.topFins = new Fin[] {fin(0.68f, 0.42f, -0.47f, 0.40f, -0.46f, 0.36f, 0f)};
                k.bottomFins = new Fin[] {fin(0.62f, 0.42f, 0.47f, 0.40f, 0.46f, 0.36f, 0f)};
                k.pectoral = fin(0.70f, 0.60f, 0.03f, 0.58f, 0.07f, 0.66f, 0.01f);
                k.back = 0xFFADB5BD;
                k.side = 0xFFE9ECEF;
                k.belly = 0xFFF8F9FA;
                k.fin = 0xFFDEE2E6;
                k.finAlpha = 170;
                k.finEdgeAlpha = 70;
                k.ray = 0x22000000;
                k.tailColor = 0xFFC9CED4;
                k.eyeX = 0.80f;
                k.eyeY = -0.04f;
                k.eyeR = 0.03f;
                k.iris = 0xFFC92A2A;
                k.patternOverFins = true;
                k.pattern = a -> {
                    a.band(0.80f, 0.035f, 0xD0212529, 0f);
                    a.band(0.635f, 0.06f, 0xD0212529, -0.02f);
                    a.band(0.46f, 0.055f, 0xC8212529, -0.03f);
                    a.band(0.345f, 0.03f, 0xB0212529, 0f);
                    a.p.setColor(0xFFFFFFFF);
                    a.p.setShader(new LinearGradient(0, a.y(-0.25f), 0, a.y(0f), 0x40E8B04A, 0x00E8B04A, Shader.TileMode.CLAMP));
                    a.c.drawRect(a.x(0.6f), 0, a.x(0.9f), a.texH, a.p);
                    a.p.setShader(null);
                };
                k.extras = a -> {
                    a.stroke(0xC0DEE2E6, 0.007f, 0.66f, 0.17f, 0.62f, 0.32f, 0.52f, 0.44f, 0.46f, 0.48f);
                    a.stroke(0xA0DEE2E6, 0.006f, 0.65f, 0.17f, 0.60f, 0.30f, 0.53f, 0.40f, 0.49f, 0.45f);
                    a.stroke(0x90DEE2E6, 0.006f, 0.17f, -0.135f, 0.10f, -0.17f, 0.04f, -0.2f);
                    a.stroke(0x90DEE2E6, 0.006f, 0.17f, 0.135f, 0.10f, 0.17f, 0.04f, 0.2f);
                };
                break;
            case "discus":
                k.ped = 0.22f;
                k.nose = 0.84f;
                k.top = 0.26f;
                k.bot = 0.26f;
                k.peak = 0.5f;
                k.pedH = 0.06f;
                k.blunt = 2.0f;
                k.noseY = 0.02f;
                k.tail = TRUNCATE;
                k.tailLen = 0.11f;
                k.tailH = 0.11f;
                k.topFins = new Fin[] {fin(0.72f, 0.66f, -0.33f, 0.30f, -0.17f, 0.27f, 0.07f)};
                k.bottomFins = new Fin[] {fin(0.62f, 0.58f, 0.32f, 0.30f, 0.16f, 0.27f, 0.06f), fin(0.68f, 0.62f, 0.30f, 0.60f, 0.29f, 0.64f, 0f)};
                k.pectoral = fin(0.70f, 0.60f, 0.04f, 0.58f, 0.08f, 0.66f, 0.01f);
                k.back = 0xFF0B7285;
                k.side = 0xFF15AABF;
                k.belly = 0xFF66D9E8;
                k.fin = 0xFF1098AD;
                k.finAlpha = 235;
                k.finEdgeAlpha = 190;
                k.eyeX = 0.78f;
                k.eyeY = -0.06f;
                k.eyeR = 0.028f;
                k.iris = 0xFFE03131;
                k.patternOverFins = true;
                k.pattern = a -> {
                    a.p.setStyle(Paint.Style.STROKE);
                    a.p.setStrokeWidth(a.texW * 0.017f);
                    a.p.setStrokeCap(Paint.Cap.ROUND);
                    a.p.setColor(0xE8E8590C);
                    for (int i = -9; i <= 9; i++) {
                        Path w = new Path();
                        float base = i * 0.042f;
                        w.moveTo(0, a.y(base));
                        for (float xf = 0f; xf <= 1f; xf += 0.01f) {
                            w.lineTo(a.x(xf), a.y(base + 0.012f * (float) Math.sin(xf * 34f + i * 1.7f) + 0.006f * (float) Math.sin(xf * 71f + i)));
                        }
                        a.c.drawPath(w, a.p);
                    }
                    a.p.setStyle(Paint.Style.FILL);
                    a.band(0.70f, 0.03f, 0x1A000000, 0f);
                    a.band(0.58f, 0.03f, 0x1A000000, 0f);
                    a.band(0.46f, 0.03f, 0x1A000000, 0f);
                    a.band(0.34f, 0.03f, 0x1A000000, 0f);
                };
                break;
            case "betta":
                k.ped = 0.44f;
                k.nose = 0.94f;
                k.top = 0.085f;
                k.bot = 0.08f;
                k.pedH = 0.05f;
                k.blunt = 2.3f;
                k.tail = FLOWING;
                k.tailLen = 0.40f;
                k.tailH = 0.30f;
                k.topFins = new Fin[] {fin(0.62f, 0.58f, -0.22f, 0.40f, -0.27f, 0.42f, 0.03f)};
                k.bottomFins = new Fin[] {fin(0.74f, 0.66f, 0.30f, 0.40f, 0.36f, 0.42f, 0.05f), fin(0.76f, 0.70f, 0.24f, 0.69f, 0.235f, 0.72f, 0f)};
                k.pectoral = fin(0.82f, 0.74f, 0.03f, 0.72f, 0.06f, 0.78f, 0.01f);
                k.back = 0xFF1864AB;
                k.side = 0xFF1C7ED6;
                k.belly = 0xFF4DABF7;
                k.fin = 0xFFC92A2A;
                k.finAlpha = 230;
                k.finEdgeAlpha = 150;
                k.ray = 0x40500000;
                k.eyeX = 0.885f;
                k.eyeY = -0.015f;
                k.eyeR = 0.022f;
                k.iris = 0xFF2B2B2B;
                k.extras = a -> {
                    a.c.save();
                    Path fins = new Path(a.all);
                    fins.op(a.body, Path.Op.DIFFERENCE);
                    a.c.clipPath(fins);
                    a.p.setColor(0xFFFFFFFF);
                    a.p.setShader(new RadialGradient(a.x(0.5f), a.cy, a.texW * 0.48f,
                            new int[] {0x00000000, 0x00000000, 0x664DABF7}, new float[] {0f, 0.7f, 1f}, Shader.TileMode.CLAMP));
                    a.c.drawRect(0, 0, a.texW, a.texH, a.p);
                    a.p.setShader(null);
                    a.c.restore();
                };
                break;
            case "dwarf_gourami":
                k.ped = 0.24f;
                k.nose = 0.95f;
                k.top = 0.14f;
                k.bot = 0.13f;
                k.peak = 0.5f;
                k.pedH = 0.045f;
                k.blunt = 2.2f;
                k.tail = TRUNCATE;
                k.tailLen = 0.17f;
                k.tailH = 0.11f;
                k.topFins = new Fin[] {fin(0.66f, 0.60f, -0.19f, 0.32f, -0.16f, 0.30f, 0.015f)};
                k.bottomFins = new Fin[] {fin(0.68f, 0.62f, 0.2f, 0.32f, 0.16f, 0.30f, 0.02f)};
                k.back = 0xFFC2410C;
                k.side = 0xFFE8590C;
                k.belly = 0xFFFF8F3F;
                k.fin = 0xFFE8590C;
                k.finAlpha = 220;
                k.finEdgeAlpha = 140;
                k.edge = 0xCC3BC9DB;
                k.edgeWidth = 3f;
                k.eyeX = 0.86f;
                k.eyeY = -0.03f;
                k.eyeR = 0.026f;
                k.iris = 0xFFE8590C;
                k.patternOverFins = true;
                k.pattern = a -> {
                    a.p.setStyle(Paint.Style.STROKE);
                    a.p.setStrokeWidth(a.texW * 0.016f);
                    a.p.setColor(0xD03BC9DB);
                    for (float xf = 0.2f; xf < 0.9f; xf += 0.045f) {
                        a.c.drawLine(a.x(xf), a.y(-0.3f), a.x(xf - 0.08f), a.y(0.3f), a.p);
                    }
                    a.p.setStyle(Paint.Style.FILL);
                    a.p.setColor(0xFFFFFFFF);
                    a.p.setShader(new LinearGradient(0, a.y(0.02f), 0, a.y(0.14f), 0x003BC9DB, 0xB03BC9DB, Shader.TileMode.CLAMP));
                    a.c.drawRect(a.x(0.6f), a.y(0.02f), a.x(0.96f), a.texH, a.p);
                    a.p.setShader(null);
                };
                k.extras = a -> {
                    a.stroke(0xCCFF8F3F, 0.006f, 0.73f, 0.09f, 0.66f, 0.18f, 0.55f, 0.235f);
                    a.stroke(0xAAFF8F3F, 0.005f, 0.72f, 0.09f, 0.64f, 0.16f, 0.52f, 0.22f);
                };
                break;
            case "reef_shark":
            case "hammerhead":
            case "leopard_shark": {
                boolean hammer = id.equals("hammerhead");
                boolean leopard = id.equals("leopard_shark");
                k.nose = 0.97f;
                k.ped = 0.2f;
                k.top = leopard ? 0.07f : 0.085f;
                k.bot = leopard ? 0.065f : 0.075f;
                k.peak = 0.45f;
                k.pedH = 0.024f;
                k.blunt = hammer ? 3.2f : 1.45f;
                k.noseY = -0.012f;
                k.tail = HETERO;
                k.tailLen = 0.2f;
                k.tailH = 0.15f;
                float dh = hammer ? -0.27f : -0.2f;
                k.topFins = new Fin[] {fin(0.62f, 0.53f, dh, 0.50f, dh + 0.01f, 0.46f, 0f), fin(0.31f, 0.285f, -0.1f, 0.275f, -0.095f, 0.255f, 0f)};
                k.bottomFins = new Fin[] {fin(0.72f, 0.60f, 0.2f, 0.585f, 0.19f, 0.63f, 0f), fin(0.39f, 0.345f, 0.12f, 0.335f, 0.115f, 0.355f, 0f),
                    fin(0.275f, 0.255f, 0.09f, 0.25f, 0.085f, 0.24f, 0f)};
                k.back = hammer ? 0xFF6E6656 : (leopard ? 0xFF7A7464 : 0xFF5F6B78);
                k.side = hammer ? 0xFF958C78 : (leopard ? 0xFFA39C88 : 0xFF8D99A6);
                k.belly = 0xFFF1F3F5;
                k.fin = k.side;
                k.finAlpha = 255;
                k.finEdgeAlpha = 255;
                k.ray = 0;
                k.eyeX = 0.885f;
                k.eyeY = -0.012f;
                k.eyeR = 0.009f;
                k.iris = 0xFF1B1B1B;
                k.patternOverFins = true;
                final boolean tips = !hammer && !leopard;
                k.pattern = a -> {
                    Path belly = new Path();
                    belly.moveTo(0, a.texH);
                    belly.lineTo(0, a.y(0.02f));
                    for (float xf = 0f; xf <= 1f; xf += 0.02f) {
                        belly.lineTo(a.x(xf), a.y(0.012f + 0.006f * (float) Math.sin(xf * 40f) + (xf > 0.8f ? (xf - 0.8f) * 0.08f : 0f)));
                    }
                    belly.lineTo(a.texW, a.texH);
                    belly.close();
                    a.p.setColor(0xFFF1F3F5);
                    a.c.drawPath(belly, a.p);
                    if (tips) {
                        a.oval(0xEE15181C, 0.53f, -0.2f, 0.02f, 0.02f);
                        a.oval(0xEE15181C, 0.6f, 0.198f, 0.02f, 0.014f);
                        a.oval(0xEE15181C, 0.0f, -0.15f, 0.035f, 0.025f);
                    }
                    if (leopard) {
                        for (float xf = 0.3f; xf < 0.85f; xf += 0.08f) {
                            a.oval(0xC02B2620, xf, a.topAt(xf) * 0.75f, 0.022f, 0.03f);
                            a.oval(0x902B2620, xf + 0.04f, -0.02f, 0.008f, 0.008f);
                        }
                    }
                };
                k.extras = a -> {
                    a.c.save();
                    a.c.clipPath(a.body);
                    for (int i = 0; i < 5; i++) {
                        float gx = 0.77f - i * 0.013f;
                        a.stroke(0x50202428, 0.0035f, gx, -0.03f, gx - 0.006f, 0.025f);
                    }
                    a.stroke(0x70202428, 0.004f, 0.965f, 0.022f, 0.92f, 0.035f, 0.86f, 0.03f);
                    a.c.restore();
                    if (hammer) {
                        a.p.setColor(0xFFFFFFFF);
                        a.p.setShader(new LinearGradient(0, a.y(-0.03f), 0, a.y(0.03f), a.k.back, a.k.side, Shader.TileMode.CLAMP));
                        a.c.drawOval(new RectF(a.x(0.915f), a.y(-0.04f), a.x(0.985f), a.y(0.035f)), a.p);
                        a.p.setShader(null);
                        a.oval(0xFF1B1B1B, 0.945f, -0.028f, 0.007f, 0.007f);
                    }
                };
                break;
            }
            case "humpback":
                k.nose = 0.97f;
                k.ped = 0.2f;
                k.top = 0.11f;
                k.bot = 0.125f;
                k.peak = 0.4f;
                k.pedH = 0.022f;
                k.blunt = 1.8f;
                k.noseY = 0.015f;
                k.tail = FLUKE;
                k.tailLen = 0.2f;
                k.tailH = 0.1f;
                k.topFins = new Fin[] {fin(0.34f, 0.31f, -0.105f, 0.295f, -0.1f, 0.27f, 0f)};
                k.back = 0xFF262A31;
                k.side = 0xFF363C45;
                k.belly = 0xFF7D858F;
                k.fin = 0xFF363C45;
                k.finAlpha = 255;
                k.finEdgeAlpha = 255;
                k.ray = 0;
                k.eyeX = 0.84f;
                k.eyeY = 0.035f;
                k.eyeR = 0.006f;
                k.iris = 0xFF101010;
                k.pattern = a -> {
                    a.p.setStyle(Paint.Style.STROKE);
                    a.p.setStrokeWidth(a.texW * 0.003f);
                    a.p.setColor(0x55C8CDD3);
                    for (int i = 0; i < 9; i++) {
                        float yy = 0.05f + i * 0.009f;
                        a.c.drawLine(a.x(0.6f), a.y(yy), a.x(0.95f), a.y(yy * 0.6f + 0.02f), a.p);
                    }
                    a.p.setStyle(Paint.Style.FILL);
                    Random r = new Random(7);
                    for (int i = 0; i < 40; i++) {
                        float xf = 0.25f + r.nextFloat() * 0.7f;
                        a.oval(0x30E9ECEF, xf, a.bottomAt(xf) * (0.4f + r.nextFloat() * 0.5f), 0.006f + r.nextFloat() * 0.01f, 0.004f + r.nextFloat() * 0.006f);
                    }
                    for (float xf = 0.82f; xf < 0.96f; xf += 0.018f) {
                        a.oval(0x80596069, xf, a.topAt(xf) + 0.008f, 0.005f, 0.004f);
                    }
                    a.stroke(0x90101215, 0.0035f, 0.97f, 0.018f, 0.9f, 0.045f, 0.84f, 0.042f);
                };
                k.extras = a -> {
                    Path flipper = new Path();
                    flipper.moveTo(a.x(0.74f), a.y(0.03f));
                    flipper.cubicTo(a.x(0.7f), a.y(0.12f), a.x(0.58f), a.y(0.22f), a.x(0.47f), a.y(0.26f));
                    flipper.cubicTo(a.x(0.55f), a.y(0.2f), a.x(0.66f), a.y(0.1f), a.x(0.7f), a.y(0.03f));
                    flipper.close();
                    a.p.setColor(0xFFFFFFFF);
                    a.p.setShader(new LinearGradient(a.x(0.74f), 0, a.x(0.47f), 0, 0xFF4A515B, 0xFFE9ECEF, Shader.TileMode.CLAMP));
                    a.c.drawPath(flipper, a.p);
                    a.p.setShader(null);
                    for (float t = 0.2f; t < 0.95f; t += 0.11f) {
                        float fx = 0.72f - 0.24f * t;
                        a.oval(0xB0F8F9FA, fx, 0.04f + 0.21f * t, 0.004f, 0.004f);
                    }
                };
                break;
            case "dolphin":
                k.nose = 0.99f;
                k.ped = 0.2f;
                k.top = 0.09f;
                k.bot = 0.085f;
                k.peak = 0.42f;
                k.pedH = 0.02f;
                k.blunt = 1.25f;
                k.noseY = 0.035f;
                k.tail = FLUKE;
                k.tailLen = 0.17f;
                k.tailH = 0.09f;
                k.topFins = new Fin[] {fin(0.56f, 0.47f, -0.18f, 0.455f, -0.17f, 0.42f, 0f)};
                k.bottomFins = new Fin[] {fin(0.76f, 0.67f, 0.13f, 0.66f, 0.125f, 0.70f, 0f)};
                k.back = 0xFF46535F;
                k.side = 0xFF7D8B99;
                k.belly = 0xFFE9ECEF;
                k.fin = 0xFF46535F;
                k.finAlpha = 255;
                k.finEdgeAlpha = 255;
                k.ray = 0;
                k.eyeX = 0.87f;
                k.eyeY = 0.0f;
                k.eyeR = 0.008f;
                k.iris = 0xFF101010;
                k.pattern = a -> {
                    a.p.setColor(0xFFFFFFFF);
                    a.p.setShader(new LinearGradient(0, a.y(-0.1f), 0, a.y(0.03f), 0x80303A44, 0x00303A44, Shader.TileMode.CLAMP));
                    a.c.drawRect(a.x(0.3f), 0, a.x(0.85f), a.texH, a.p);
                    a.p.setShader(null);
                    a.oval(0x30FFFFFF, 0.88f, -0.045f, 0.04f, 0.02f);
                    a.stroke(0x70202830, 0.003f, 0.99f, 0.036f, 0.95f, 0.03f, 0.9f, 0.022f);
                };
                break;
            case "yellowfin_tuna":
                k.nose = 0.97f;
                k.ped = 0.2f;
                k.top = 0.11f;
                k.bot = 0.1f;
                k.peak = 0.45f;
                k.pedH = 0.02f;
                k.blunt = 1.8f;
                k.tail = LUNATE;
                k.tailLen = 0.2f;
                k.tailH = 0.17f;
                k.topFins = new Fin[] {fin(0.62f, 0.58f, -0.15f, 0.46f, -0.12f, 0.43f, 0f), fin(0.41f, 0.37f, -0.23f, 0.35f, -0.22f, 0.33f, 0f)};
                k.bottomFins = new Fin[] {fin(0.39f, 0.35f, 0.22f, 0.33f, 0.21f, 0.31f, 0f)};
                k.pectoral = fin(0.76f, 0.6f, 0.0f, 0.56f, 0.02f, 0.7f, 0.01f);
                k.back = 0xFF14254A;
                k.side = 0xFF7186A3;
                k.belly = 0xFFE9ECEF;
                k.fin = 0xFFFAB005;
                k.finAlpha = 250;
                k.finEdgeAlpha = 230;
                k.ray = 0x18000000;
                k.tailColor = 0xFF2B3A55;
                k.eyeX = 0.88f;
                k.eyeY = -0.015f;
                k.eyeR = 0.018f;
                k.iris = 0xFF9BA7B6;
                k.pattern = a -> {
                    a.stroke(0xA0FCC419, 0.012f, 0.86f, -0.03f, 0.6f, -0.045f, 0.3f, -0.01f);
                    a.p.setStyle(Paint.Style.STROKE);
                    a.p.setStrokeWidth(a.texW * 0.003f);
                    a.p.setColor(0x30FFFFFF);
                    for (float xf = 0.3f; xf < 0.7f; xf += 0.03f) {
                        a.c.drawLine(a.x(xf), a.y(0.02f), a.x(xf - 0.01f), a.y(0.07f), a.p);
                    }
                    a.p.setStyle(Paint.Style.FILL);
                };
                k.extras = a -> {
                    for (int i = 0; i < 7; i++) {
                        float xf = 0.31f - i * 0.016f;
                        Path f1 = new Path();
                        f1.moveTo(a.x(xf), a.y(a.topAt(xf) + 0.004f));
                        f1.lineTo(a.x(xf - 0.01f), a.y(a.topAt(xf) - 0.022f));
                        f1.lineTo(a.x(xf - 0.014f), a.y(a.topAt(xf) + 0.004f));
                        f1.close();
                        a.p.setColor(0xFFFCC419);
                        a.c.drawPath(f1, a.p);
                        Path f2 = new Path();
                        f2.moveTo(a.x(xf), a.y(a.bottomAt(xf) - 0.004f));
                        f2.lineTo(a.x(xf - 0.01f), a.y(a.bottomAt(xf) + 0.022f));
                        f2.lineTo(a.x(xf - 0.014f), a.y(a.bottomAt(xf) - 0.004f));
                        f2.close();
                        a.c.drawPath(f2, a.p);
                    }
                };
                break;
            case "barracuda":
                k.nose = 0.985f;
                k.ped = 0.2f;
                k.top = 0.058f;
                k.bot = 0.058f;
                k.peak = 0.5f;
                k.pedH = 0.022f;
                k.blunt = 1.3f;
                k.noseY = 0.008f;
                k.tail = FORKED;
                k.tailLen = 0.16f;
                k.tailH = 0.1f;
                k.topFins = new Fin[] {fin(0.53f, 0.50f, -0.1f, 0.475f, -0.09f, 0.45f, 0f), fin(0.35f, 0.32f, -0.09f, 0.30f, -0.085f, 0.29f, 0f)};
                k.bottomFins = new Fin[] {fin(0.35f, 0.32f, 0.09f, 0.30f, 0.085f, 0.29f, 0f), fin(0.55f, 0.51f, 0.085f, 0.50f, 0.08f, 0.52f, 0f)};
                k.back = 0xFF3D4651;
                k.side = 0xFFADB5BD;
                k.belly = 0xFFF8F9FA;
                k.fin = 0xFF6C757D;
                k.finAlpha = 220;
                k.eyeX = 0.9f;
                k.eyeY = -0.012f;
                k.eyeR = 0.014f;
                k.iris = 0xFFCED4DA;
                k.pattern = a -> {
                    for (float xf = 0.28f; xf < 0.8f; xf += 0.045f) {
                        a.stroke(0x40212529, 0.012f, xf + 0.012f, -0.06f, xf - 0.01f, -0.005f);
                    }
                    Random r = new Random(3);
                    for (int i = 0; i < 14; i++) {
                        a.oval(0x90212529, 0.22f + r.nextFloat() * 0.3f, 0.015f + r.nextFloat() * 0.025f, 0.004f, 0.004f);
                    }
                    a.stroke(0x80212529, 0.0035f, 0.985f, 0.008f, 0.88f, 0.012f);
                };
                break;
            case "sardine":
                k.ped = 0.24f;
                k.top = 0.08f;
                k.bot = 0.075f;
                k.pedH = 0.026f;
                k.blunt = 2.1f;
                k.tail = FORKED;
                k.tailLen = 0.22f;
                k.tailH = 0.1f;
                k.topFins = new Fin[] {fin(0.6f, 0.56f, -0.12f, 0.5f, -0.1f, 0.48f, 0f)};
                k.bottomFins = new Fin[] {fin(0.38f, 0.36f, 0.1f, 0.3f, 0.085f, 0.29f, 0f)};
                k.back = 0xFF1C4E6B;
                k.side = 0xFFAEBCC9;
                k.belly = 0xFFF8F9FA;
                k.fin = 0xFFCED4DA;
                k.finAlpha = 140;
                k.finEdgeAlpha = 50;
                k.eyeX = 0.88f;
                k.eyeY = -0.012f;
                k.eyeR = 0.028f;
                k.iris = 0xFFB8C4D0;
                k.pattern = a -> {
                    for (float xf = 0.72f; xf > 0.3f; xf -= 0.05f) {
                        a.oval(0x9014202C, xf, -0.03f, 0.008f, 0.008f);
                    }
                    a.oval(0x40FFFFFF, 0.58f, -0.005f, 0.28f, 0.02f);
                };
                break;
            case "garibaldi":
                k.ped = 0.23f;
                k.top = 0.14f;
                k.bot = 0.13f;
                k.peak = 0.5f;
                k.pedH = 0.045f;
                k.blunt = 2.2f;
                k.tail = TRUNCATE;
                k.tailLen = 0.18f;
                k.tailH = 0.12f;
                k.topFins = new Fin[] {fin(0.7f, 0.66f, -0.2f, 0.32f, -0.165f, 0.3f, 0.02f)};
                k.bottomFins = new Fin[] {fin(0.5f, 0.46f, 0.19f, 0.32f, 0.155f, 0.3f, 0.015f), fin(0.7f, 0.62f, 0.19f, 0.61f, 0.185f, 0.65f, 0f)};
                k.pectoral = fin(0.74f, 0.63f, 0.04f, 0.61f, 0.08f, 0.71f, 0.01f);
                k.back = 0xFFF25C05;
                k.side = 0xFFFF7A00;
                k.belly = 0xFFFF9A2E;
                k.fin = 0xFFFF7A00;
                k.finAlpha = 245;
                k.finEdgeAlpha = 210;
                k.eyeX = 0.865f;
                k.eyeR = 0.028f;
                k.iris = 0xFFE8590C;
                break;
            case "kelp_bass":
                k.ped = 0.22f;
                k.top = 0.12f;
                k.bot = 0.11f;
                k.peak = 0.5f;
                k.pedH = 0.04f;
                k.blunt = 1.9f;
                k.tail = TRUNCATE;
                k.tailLen = 0.18f;
                k.tailH = 0.11f;
                k.topFins = new Fin[] {fin(0.72f, 0.66f, -0.19f, 0.5f, -0.15f, 0.48f, 0.01f), fin(0.48f, 0.45f, -0.17f, 0.32f, -0.14f, 0.3f, 0.015f)};
                k.bottomFins = new Fin[] {fin(0.45f, 0.42f, 0.17f, 0.32f, 0.13f, 0.3f, 0.01f), fin(0.7f, 0.62f, 0.17f, 0.61f, 0.165f, 0.65f, 0f)};
                k.pectoral = fin(0.74f, 0.63f, 0.04f, 0.61f, 0.08f, 0.71f, 0.01f);
                k.back = 0xFF3F3C2A;
                k.side = 0xFF6E6A48;
                k.belly = 0xFFC2BC92;
                k.fin = 0xFF5E5A3E;
                k.finAlpha = 230;
                k.eyeX = 0.87f;
                k.eyeR = 0.024f;
                k.iris = 0xFF8C7A3E;
                k.pattern = a -> {
                    Random r = new Random(11);
                    for (int i = 0; i < 18; i++) {
                        float xf = 0.3f + r.nextFloat() * 0.5f;
                        a.oval(0x55E9E3C0, xf, -0.06f + r.nextFloat() * 0.07f, 0.02f + r.nextFloat() * 0.015f, 0.012f + r.nextFloat() * 0.01f);
                    }
                };
                break;
            default:
                throw new IllegalArgumentException(id);
        }
        return k;
    }
}
