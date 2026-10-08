package com.jeremykenedy.aquariumlive;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Random;

/**
 * Paints the sea life that is not built like a fish: rays, turtles,
 * octopuses, jellyfish, seahorses, crabs and starfish. Creatures made of
 * several moving parts get one texture per part.
 */
final class CreatureArt {

    static final String MANTA = "manta";
    static final String TURTLE_BODY = "turtle_body";
    static final String TURTLE_FLIPPER = "turtle_flipper";
    static final String OCTOPUS_MANTLE = "octopus_mantle";
    static final String OCTOPUS_ARM = "octopus_arm";
    static final String JELLY_BELL = "jelly_bell";
    static final String JELLY_TENTACLE = "jelly_tentacle";
    static final String JELLY_ARM = "jelly_arm";
    static final String SEAHORSE = "seahorse";
    static final String CRAB = "crab";
    static final String STARFISH = "starfish";

    /** Turtle flipper joints, in body-local units (x right, y up, centred). */
    static final float[] TURTLE_FRONT = {0.22f, -0.07f};
    static final float[] TURTLE_REAR = {-0.27f, -0.09f};

    private final Bitmap bitmap;
    private final Canvas c;
    private final int w;
    private final int h;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rnd = new Random(42);

    private CreatureArt(int w, int h) {
        this.w = w;
        this.h = h;
        this.bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        this.c = new Canvas(bitmap);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
    }

    static Bitmap paint(String part, Config.Style style) {
        return ArtStyle.finish(paint(part), style);
    }

    private static Bitmap paint(String part) {
        CreatureArt a;
        switch (part) {
            case MANTA:
                a = new CreatureArt(512, 1024);
                a.manta();
                break;
            case TURTLE_BODY:
                a = new CreatureArt(512, 512);
                a.turtleBody();
                break;
            case TURTLE_FLIPPER:
                a = new CreatureArt(128, 512);
                a.flipper();
                break;
            case OCTOPUS_MANTLE:
                a = new CreatureArt(512, 512);
                a.octopusMantle();
                break;
            case OCTOPUS_ARM:
                a = new CreatureArt(64, 512);
                a.octopusArm();
                break;
            case JELLY_BELL:
                a = new CreatureArt(512, 256);
                a.jellyBell();
                break;
            case JELLY_TENTACLE:
                a = new CreatureArt(32, 512);
                a.jellyTentacle();
                break;
            case JELLY_ARM:
                a = new CreatureArt(128, 512);
                a.jellyArm();
                break;
            case SEAHORSE:
                a = new CreatureArt(256, 512);
                a.seahorse();
                break;
            case CRAB:
                a = new CreatureArt(512, 256);
                a.crab();
                break;
            case STARFISH:
                a = new CreatureArt(256, 256);
                a.starfish();
                break;
            default:
                throw new IllegalArgumentException(part);
        }
        return a.bitmap;
    }

    private float r(float lo, float hi) {
        return lo + rnd.nextFloat() * (hi - lo);
    }

    private void shadeInside(Path clip, float lx, float ly, float radius, int darkEdge) {
        RectF b = new RectF();
        clip.computeBounds(b, true);
        c.save();
        c.clipPath(clip);
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(lx, ly, radius, new int[] {0x33FFFFFF, 0x00000000, darkEdge}, new float[] {0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(b, p);
        p.setShader(null);
        c.restore();
    }

    private void manta() {
        float cy = h * 0.5f;
        Path body = new Path();
        body.moveTo(462, cy - 58);
        body.cubicTo(400, cy - 140, 330, cy - 360, 212, cy - 485);
        body.cubicTo(232, cy - 350, 205, cy - 180, 122, cy - 72);
        body.quadTo(98, cy, 122, cy + 72);
        body.cubicTo(205, cy + 180, 232, cy + 350, 212, cy + 485);
        body.cubicTo(330, cy + 360, 400, cy + 140, 462, cy + 58);
        body.quadTo(488, cy, 462, cy - 58);
        body.close();

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(7f);
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(120, 0, 0, 0, 0xFF2A2E35, 0x002A2E35, Shader.TileMode.CLAMP));
        c.drawLine(122, cy, 4, cy + 6, p);
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);

        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(340, cy, 520, new int[] {0xFF3A414B, 0xFF262B32, 0xFF15181C}, new float[] {0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        c.drawPath(body, p);
        p.setShader(null);

        c.save();
        c.clipPath(body);
        for (int s = -1; s <= 1; s += 2) {
            Path patch = new Path();
            patch.moveTo(420, cy + s * 70);
            patch.quadTo(370, cy + s * 210, 285, cy + s * 330);
            patch.quadTo(330, cy + s * 190, 335, cy + s * 75);
            patch.close();
            p.setColor(0xD8E9ECEF);
            c.drawPath(patch, p);
        }
        p.setColor(0x26FFFFFF);
        c.drawOval(new RectF(250, cy - 60, 440, cy + 60), p);
        c.restore();
        shadeInside(body, 360, cy, 560, 0x66000000);

        for (int s = -1; s <= 1; s += 2) {
            Path lobe = new Path();
            lobe.moveTo(455, cy + s * 50);
            lobe.cubicTo(500, cy + s * 46, 512, cy + s * 18, 498, cy + s * 8);
            lobe.cubicTo(486, cy + s * 22, 470, cy + s * 32, 450, cy + s * 34);
            lobe.close();
            p.setColor(0xFF2E333A);
            c.drawPath(lobe, p);
            p.setColor(0xFF0E0F12);
            c.drawCircle(452, cy + s * 46, 5f, p);
        }
    }

    private void turtleBody() {
        Path head = new Path();
        head.moveTo(392, 250);
        head.cubicTo(428, 228, 455, 212, 482, 220);
        head.cubicTo(508, 228, 508, 262, 488, 274);
        head.cubicTo(462, 286, 430, 292, 392, 298);
        head.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, 210, 0, 300, 0xFF9C8B57, 0xFF6B5E36, Shader.TileMode.CLAMP));
        c.drawPath(head, p);
        p.setShader(null);
        c.save();
        c.clipPath(head);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f);
        p.setColor(0x804A4024);
        for (int i = 0; i < 14; i++) {
            float x = r(410, 495);
            float y = r(215, 290);
            c.drawRect(x, y, x + r(8, 16), y + r(6, 12), p);
        }
        p.setStyle(Paint.Style.FILL);
        c.restore();
        p.setColor(0xFF15140F);
        c.drawCircle(474, 236, 7f, p);
        p.setColor(0xCCFFFFFF);
        c.drawCircle(476, 233, 2.2f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3f);
        p.setColor(0x904A4024);
        c.drawLine(500, 254, 478, 262, p);
        p.setStyle(Paint.Style.FILL);

        Path tail = new Path();
        tail.moveTo(96, 296);
        tail.lineTo(58, 304);
        tail.lineTo(96, 312);
        tail.close();
        p.setColor(0xFF7A6B42);
        c.drawPath(tail, p);

        Path plastron = new Path();
        plastron.moveTo(84, 296);
        plastron.quadTo(250, 336, 408, 296);
        plastron.lineTo(404, 316);
        plastron.quadTo(250, 352, 90, 312);
        plastron.close();
        p.setColor(0xFFD9C58A);
        c.drawPath(plastron, p);

        Path shell = new Path();
        shell.moveTo(80, 302);
        shell.cubicTo(88, 172, 214, 118, 296, 126);
        shell.cubicTo(384, 134, 424, 206, 412, 302);
        shell.quadTo(250, 330, 80, 302);
        shell.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, 120, 0, 310, 0xFF7C6A34, 0xFF3B3117, Shader.TileMode.CLAMP));
        c.drawPath(shell, p);
        p.setShader(null);
        c.save();
        c.clipPath(shell);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4f);
        p.setColor(0xB02A2210);
        float[][] scutes = {{150, 200}, {230, 160}, {310, 165}, {370, 215}, {200, 250}, {290, 245}};
        for (float[] s : scutes) {
            Path hex = new Path();
            for (int i = 0; i < 6; i++) {
                double a = Math.PI / 3 * i + 0.3;
                float x = s[0] + (float) Math.cos(a) * 46f;
                float y = s[1] + (float) Math.sin(a) * 34f;
                if (i == 0) {
                    hex.moveTo(x, y);
                } else {
                    hex.lineTo(x, y);
                }
            }
            hex.close();
            c.drawPath(hex, p);
        }
        float x = 96;
        while (x < 410) {
            c.drawLine(x, 300, x + 6, 278, p);
            x += 26;
        }
        p.setStyle(Paint.Style.FILL);
        for (float[] s : scutes) {
            p.setColor(0x30E8C26A);
            c.drawOval(new RectF(s[0] - 22, s[1] - 18, s[0] + 18, s[1] + 8), p);
        }
        c.restore();
        shadeInside(shell, 250, 170, 260, 0x60000000);
    }

    private void flipper() {
        Path f = new Path();
        f.moveTo(44, h);
        f.cubicTo(30, h * 0.75f, 18, h * 0.42f, 34, h * 0.14f);
        f.quadTo(52, h * 0.01f, 70, h * 0.03f);
        f.cubicTo(108, h * 0.2f, 104, h * 0.6f, 84, h);
        f.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, h, 0, 0, 0xFF6B5E36, 0xFF9C8B57, Shader.TileMode.CLAMP));
        c.drawPath(f, p);
        p.setShader(null);
        c.save();
        c.clipPath(f);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.5f);
        p.setColor(0x904A4024);
        float y = h * 0.05f;
        while (y < h) {
            c.drawLine(0, y, w, y + 8, p);
            y += 22f;
        }
        p.setStrokeWidth(6f);
        p.setColor(0x60E6D8A8);
        c.drawLine(36, h * 0.15f, 30, h * 0.8f, p);
        p.setStyle(Paint.Style.FILL);
        c.restore();
    }

    private void octopusMantle() {
        int base = 0xFFB0482C;
        Path mantle = new Path();
        mantle.moveTo(150, 360);
        mantle.cubicTo(70, 300, 70, 110, 190, 50);
        mantle.cubicTo(300, 0, 420, 70, 380, 200);
        mantle.cubicTo(360, 280, 350, 330, 360, 360);
        mantle.close();
        Path skirt = new Path();
        skirt.moveTo(150, 340);
        skirt.cubicTo(140, 420, 90, 470, 40, h);
        skirt.lineTo(472, h);
        skirt.cubicTo(420, 470, 370, 420, 365, 340);
        skirt.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, 330, 0, h, base, FishArt.darken(base, 0.7f), Shader.TileMode.CLAMP));
        c.drawPath(skirt, p);
        p.setShader(null);
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(230, 140, 300, new int[] {0xFFD06A44, base, FishArt.darken(base, 0.6f)}, new float[] {0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        c.drawPath(mantle, p);
        p.setShader(null);
        Path all = new Path(mantle);
        all.op(skirt, Path.Op.UNION);
        c.save();
        c.clipPath(all);
        for (int i = 0; i < 90; i++) {
            p.setColor(rnd.nextBoolean() ? 0x40F5C6A5 : 0x40501A0C);
            float rr = r(4, 16);
            float x = r(60, 450);
            float y = r(20, h);
            c.drawOval(new RectF(x - rr, y - rr * 0.7f, x + rr, y + rr * 0.7f), p);
        }
        c.restore();
        for (int i = 0; i < 2; i++) {
            float ex = i == 0 ? 330 : 205;
            float ey = i == 0 ? 330 : 340;
            float er = i == 0 ? 34 : 24;
            p.setColor(FishArt.darken(base, 0.8f));
            c.drawCircle(ex, ey, er * 1.25f, p);
            p.setColor(0xFFFFFFFF);
            p.setShader(new RadialGradient(ex, ey, er, 0xFFF2D27A, 0xFFB08A2E, Shader.TileMode.CLAMP));
            c.drawCircle(ex, ey, er, p);
            p.setShader(null);
            p.setColor(0xFF111111);
            c.drawRoundRect(new RectF(ex - er * 0.7f, ey - er * 0.18f, ex + er * 0.7f, ey + er * 0.18f), er * 0.18f, er * 0.18f, p);
            p.setColor(0xBBFFFFFF);
            c.drawCircle(ex + er * 0.3f, ey - er * 0.4f, er * 0.15f, p);
        }
    }

    private void octopusArm() {
        Path arm = new Path();
        arm.moveTo(4, h);
        arm.cubicTo(8, h * 0.6f, 20, h * 0.25f, 30, 6);
        arm.quadTo(33, 2, 36, 6);
        arm.cubicTo(42, h * 0.25f, 56, h * 0.6f, 60, h);
        arm.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, h, 0, 0, 0xFFB0482C, 0xFFC86A48, Shader.TileMode.CLAMP));
        c.drawPath(arm, p);
        p.setShader(null);
        c.save();
        c.clipPath(arm);
        float y = h - 14f;
        while (y > 20) {
            float t = y / h;
            float rr = 2.5f + 6f * t;
            float x = 32 + 18f * t;
            p.setColor(0xFFF0C9A8);
            c.drawCircle(x, y, rr, p);
            p.setColor(0x60803020);
            c.drawCircle(x, y, rr * 0.45f, p);
            y -= 18f * (0.4f + 0.6f * y / h);
        }
        c.restore();
    }

    private void jellyBell() {
        Path bell = new Path();
        bell.moveTo(16, h - 12f);
        bell.cubicTo(20, 60, 140, 14, 256, 14);
        bell.cubicTo(372, 14, 492, 60, 496, h - 12f);
        for (int i = 0; i <= 16; i++) {
            float x = 496 - i * (480f / 16f);
            bell.quadTo(x + 15, h + 2f, x, h - 12f);
        }
        bell.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(256, 160, 290, new int[] {0x70F8E8FF, 0x90EBD5FA, 0xD8D9B8F2}, new float[] {0f, 0.6f, 1f}, Shader.TileMode.CLAMP));
        c.drawPath(bell, p);
        p.setShader(null);
        c.save();
        c.clipPath(bell);
        p.setColor(0x709C5FD1);
        for (int i = 0; i < 4; i++) {
            float x = 120 + i * 90f;
            c.drawOval(new RectF(x - 36, 120, x + 36, 175), p);
        }
        p.setColor(0x30FFFFFF);
        c.drawOval(new RectF(120, 30, 330, 90), p);
        c.restore();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4f);
        p.setColor(0x90FFFFFF);
        c.drawPath(bell, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void jellyTentacle() {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4f);
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, h, 0, 0, 0xB0F3D9FA, 0x10F3D9FA, Shader.TileMode.CLAMP));
        c.drawLine(16, h, 16, 0, p);
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        float y = h - 20f;
        while (y > 40) {
            p.setColor((int) (0x60 * y / h) << 24 | 0xFFFFFF);
            c.drawCircle(16, y, 3f, p);
            y -= 26;
        }
    }

    private void jellyArm() {
        Path ribbon = new Path();
        ribbon.moveTo(40, h);
        float y = h;
        while (y > 20) {
            float t = y / h;
            ribbon.lineTo(64 - 26 * t + (float) Math.sin(y * 0.09f) * 12 * t - 6, y);
            y -= 14;
        }
        ribbon.lineTo(64, 6);
        y = 20;
        while (y <= h) {
            float t = y / h;
            ribbon.lineTo(64 + 26 * t + (float) Math.sin(y * 0.09f + 1.5f) * 12 * t + 6, y);
            y += 14;
        }
        ribbon.close();
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, h, 0, 0, 0xB0E599F7, 0x20F3D9FA, Shader.TileMode.CLAMP));
        c.drawPath(ribbon, p);
        p.setShader(null);
    }

    private void seahorse() {
        float[][] spine = {
            {128, 78}, {112, 128}, {108, 178}, {132, 232}, {150, 282}, {140, 330}, {118, 372},
            {104, 412}, {96, 448}, {78, 476}, {56, 468}, {54, 444}, {70, 432}, {84, 442},
        };
        float[] widths = {50, 34, 40, 54, 58, 48, 34, 24, 18, 13, 10, 8, 6, 4};
        int samples = 140;
        float[] lx = new float[samples];
        float[] ly = new float[samples];
        float[] rx = new float[samples];
        float[] ry = new float[samples];
        float[] cx = new float[samples];
        float[] cyArr = new float[samples];
        for (int i = 0; i < samples; i++) {
            float t = i / (float) (samples - 1) * (spine.length - 1);
            int k = Math.min(spine.length - 2, (int) t);
            float f = t - k;
            float[] p0 = spine[Math.max(0, k - 1)];
            float[] p1 = spine[k];
            float[] p2 = spine[k + 1];
            float[] p3 = spine[Math.min(spine.length - 1, k + 2)];
            cx[i] = catmull(p0[0], p1[0], p2[0], p3[0], f);
            cyArr[i] = catmull(p0[1], p1[1], p2[1], p3[1], f);
            float wdt = widths[k] + (widths[k + 1] - widths[k]) * f;
            float dx = catmullD(p0[0], p1[0], p2[0], p3[0], f);
            float dy = catmullD(p0[1], p1[1], p2[1], p3[1], f);
            float len = (float) Math.hypot(dx, dy) + 1e-4f;
            float nx = -dy / len;
            float ny = dx / len;
            lx[i] = cx[i] + nx * wdt * 0.5f;
            ly[i] = cyArr[i] + ny * wdt * 0.5f;
            rx[i] = cx[i] - nx * wdt * 0.5f;
            ry[i] = cyArr[i] - ny * wdt * 0.5f;
        }
        Path body = new Path();
        body.moveTo(lx[0], ly[0]);
        for (int i = 1; i < samples; i++) {
            body.lineTo(lx[i], ly[i]);
        }
        for (int i = samples - 1; i >= 0; i--) {
            body.lineTo(rx[i], ry[i]);
        }
        body.close();
        Path snout = new Path();
        snout.moveTo(140, 66);
        snout.cubicTo(180, 70, 214, 82, 236, 92);
        snout.lineTo(234, 108);
        snout.cubicTo(212, 104, 176, 104, 140, 100);
        snout.close();
        body.op(snout, Path.Op.UNION);
        body.addCircle(128, 70, 30, Path.Direction.CW);

        Path fin = new Path();
        fin.moveTo(108, 250);
        fin.quadTo(66, 270, 74, 318);
        fin.quadTo(98, 300, 120, 300);
        fin.close();
        p.setColor(0x80FCC419);
        c.drawPath(fin, p);

        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, 40, 0, 480, 0xFFFAB005, 0xFFD9480F, Shader.TileMode.CLAMP));
        c.drawPath(body, p);
        p.setShader(null);
        c.save();
        c.clipPath(body);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3f);
        p.setColor(0x70802E06);
        for (int i = 8; i < samples; i += 5) {
            c.drawLine(lx[i], ly[i], rx[i], ry[i], p);
        }
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 40; i++) {
            p.setColor(0x60FFFFFF);
            c.drawCircle(r(60, 200), r(40, 420), r(1.5f, 3.5f), p);
        }
        c.restore();
        shadeInside(body, 150, 200, 260, 0x50000000);

        p.setColor(0xFFE8590C);
        for (int i = 0; i < 4; i++) {
            Path spike = new Path();
            float bx = 112 + i * 9f;
            spike.moveTo(bx, 50);
            spike.lineTo(bx + 4, 30 - (i % 2) * 6f);
            spike.lineTo(bx + 8, 50);
            spike.close();
            c.drawPath(spike, p);
        }
        p.setColor(0xFF1A1208);
        c.drawCircle(138, 70, 7f, p);
        p.setColor(0xCCFFFFFF);
        c.drawCircle(140, 67, 2.2f, p);
    }

    private static float catmull(float p0, float p1, float p2, float p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        return 0.5f * (2 * p1 + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 + (-p0 + 3 * p1 - 3 * p2 + p3) * t3);
    }

    private static float catmullD(float p0, float p1, float p2, float p3, float t) {
        float t2 = t * t;
        return 0.5f * ((-p0 + p2) + 2 * (2 * p0 - 5 * p1 + 4 * p2 - p3) * t + 3 * (-p0 + 3 * p1 - 3 * p2 + p3) * t2);
    }

    private void crab() {
        int dark = 0xFF9C2F06;
        p.setStyle(Paint.Style.STROKE);
        p.setColor(dark);
        for (int s = -1; s <= 1; s += 2) {
            for (int i = 0; i < 4; i++) {
                float bx = 256 + s * (70 + i * 18f);
                float by = 150 + i * 6f;
                float kx = 256 + s * (120 + i * 30f);
                float ky = 165 + i * 4f;
                float fx = 256 + s * (140 + i * 30f);
                float fy = h - 6f;
                p.setStrokeWidth(13f - i);
                c.drawLine(bx, by, kx, ky - 20, p);
                p.setStrokeWidth(10f - i);
                c.drawLine(kx, ky - 20, fx, fy, p);
            }
        }
        p.setStrokeWidth(18f);
        for (int s = -1; s <= 1; s += 2) {
            c.drawLine(256 + s * 100f, 120, 256 + s * 160f, 80, p);
            c.drawLine(256 + s * 160f, 80, 256 + s * 195f, 60, p);
        }
        p.setStyle(Paint.Style.FILL);
        for (int s = -1; s <= 1; s += 2) {
            c.save();
            c.translate(256 + s * 205f, 50);
            c.rotate(s * -25f);
            p.setColor(0xFFFFFFFF);
            p.setShader(new LinearGradient(0, -40, 0, 40, 0xFFFF6B2C, dark, Shader.TileMode.CLAMP));
            c.drawOval(new RectF(-42, -26, 42, 26), p);
            p.setShader(null);
            p.setColor(dark);
            Path finger = new Path();
            finger.moveTo(s * 30f, -16);
            finger.quadTo(s * 70f, -30, s * 76f, -6);
            finger.quadTo(s * 56f, -12, s * 34f, -4);
            finger.close();
            c.drawPath(finger, p);
            Path thumb = new Path();
            thumb.moveTo(s * 30f, 8);
            thumb.quadTo(s * 66f, 16, s * 72f, 2);
            thumb.quadTo(s * 54f, 4, s * 32f, 0);
            thumb.close();
            c.drawPath(thumb, p);
            p.setColor(0x40FFFFFF);
            c.drawOval(new RectF(-26, -18, 10, -4), p);
            c.restore();
        }
        Path carapace = new Path();
        carapace.addOval(new RectF(126, 62, 386, 190), Path.Direction.CW);
        p.setColor(0xFFFFFFFF);
        p.setShader(new LinearGradient(0, 62, 0, 190, 0xFFFF7A3C, dark, Shader.TileMode.CLAMP));
        c.drawPath(carapace, p);
        p.setShader(null);
        c.save();
        c.clipPath(carapace);
        for (int i = 0; i < 60; i++) {
            p.setColor(rnd.nextBoolean() ? 0x30FFFFFF : 0x30400000);
            c.drawCircle(r(130, 380), r(62, 190), r(2, 7), p);
        }
        c.restore();
        shadeInside(carapace, 230, 90, 180, 0x60000000);
        for (int s = -1; s <= 1; s += 2) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(6f);
            p.setColor(dark);
            c.drawLine(256 + s * 26f, 76, 256 + s * 32f, 44, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFF111111);
            c.drawCircle(256 + s * 32f, 40, 9f, p);
            p.setColor(0xBBFFFFFF);
            c.drawCircle(256 + s * 30f, 37, 3f, p);
        }
    }

    private void starfish() {
        Path star = new Path();
        float cx = 128;
        float cy = 128;
        for (int i = 0; i < 5; i++) {
            double tip = -Math.PI / 2 + i * Math.PI * 2 / 5;
            double inner = tip + Math.PI / 5;
            float tx = cx + (float) Math.cos(tip) * 118;
            float ty = cy + (float) Math.sin(tip) * 118;
            float ix = cx + (float) Math.cos(inner) * 44;
            float iy = cy + (float) Math.sin(inner) * 44;
            double prevInner = tip - Math.PI / 5;
            float px = cx + (float) Math.cos(prevInner) * 44;
            float py = cy + (float) Math.sin(prevInner) * 44;
            if (i == 0) {
                star.moveTo(px, py);
            }
            star.quadTo((px + tx) / 2f + (float) Math.cos(tip) * 6, (py + ty) / 2f + (float) Math.sin(tip) * 6, tx, ty);
            star.quadTo((ix + tx) / 2f + (float) Math.cos(tip) * 6, (iy + ty) / 2f + (float) Math.sin(tip) * 6, ix, iy);
        }
        star.close();
        Matrix m = new Matrix();
        m.setScale(1f, 0.62f, cx, h);
        m.postTranslate(0, -6);
        star.transform(m);
        p.setColor(0xFFFFFFFF);
        p.setShader(new RadialGradient(cx, h - 90f, 150, new int[] {0xFFB45AD6, 0xFF7B2D8E, 0xFF4A1657}, new float[] {0f, 0.6f, 1f}, Shader.TileMode.CLAMP));
        c.drawPath(star, p);
        p.setShader(null);
        c.save();
        c.clipPath(star);
        for (int i = 0; i < 140; i++) {
            p.setColor(0xA0F3E6F8);
            c.drawCircle(r(0, w), r(h - 160f, h), r(1.5f, 3.6f), p);
        }
        c.restore();
    }
}
