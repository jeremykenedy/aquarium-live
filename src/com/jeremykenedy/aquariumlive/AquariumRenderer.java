package com.jeremykenedy.aquariumlive;

import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Draws the tank every frame. All sizes are world units; the tank is 1080
 * tall. Creatures and plants are queued as instances and drawn in batches,
 * one call per run of consecutive items sharing a texture, which keeps the
 * GL driver's per-call cost from dominating on a TV's small CPU.
 */
final class AquariumRenderer implements GLSurfaceView.Renderer {

    /** Per-instance inputs: iA x, y, cos, sin. iB width, height, sway, sway phase. iC uv repeat, origin y, glow, alpha. iD r, g, b, fog. */
    private static final String SPRITE_VS =
            "attribute vec2 aPos; attribute vec2 aUv;\n"
            + "attribute vec4 iA; attribute vec4 iB; attribute vec4 iC; attribute vec4 iD;\n"
            + "uniform vec2 uView; uniform vec3 uTint;\n"
            + "varying vec2 vUv; varying float vLight; varying vec4 vColor; varying float vFog;\n"
            + "void main() {\n"
            + "  float t = aPos.y;\n"
            + "  float bend = iB.z * t * t * sin(iB.w - t * 1.6);\n"
            + "  vec2 l = vec2(aPos.x * iB.x + bend, (aPos.y - iC.y) * iB.y);\n"
            + "  vec2 w = iA.xy + vec2(l.x * iA.z - l.y * iA.w, l.x * iA.w + l.y * iA.z);\n"
            + "  vUv = vec2(aUv.x * iC.x, aUv.y);\n"
            + "  vLight = 1.0 + iC.z;\n"
            + "  vColor = vec4(uTint * iD.rgb, iC.w);\n"
            + "  vFog = iD.w;\n"
            + "  gl_Position = vec4(w * uView - 1.0, 0.0, 1.0);\n"
            + "}\n";

    /** Per-instance inputs: iA x, y, cos, sin. iB width, height, rigid part, amplitude. iC phase, mode, glow, alpha. iD r, g, b, fog. */
    private static final String BODY_VS =
            "attribute vec2 aPos; attribute vec2 aUv;\n"
            + "attribute vec4 iA; attribute vec4 iB; attribute vec4 iC; attribute vec4 iD;\n"
            + "uniform vec2 uView; uniform vec3 uTint;\n"
            + "varying vec2 vUv; varying float vLight; varying vec4 vColor; varying float vFog;\n"
            + "float ang(float u) { float k = max(u - iB.z, 0.0) / (1.0 - iB.z); return iB.w * k * k * sin(iC.x - u * 3.0); }\n"
            + "void main() {\n"
            + "  float u = 0.5 - aPos.x;\n"
            + "  vec2 l = aPos;\n"
            + "  float shade = 1.0;\n"
            + "  if (iC.y < 0.5) {\n"
            + "    float x = 0.0; float du = u / 6.0;\n"
            + "    for (int i = 0; i < 6; i++) { float s = (float(i) + 0.5) * du; x += cos(ang(s)) * du; }\n"
            + "    l.x = 0.5 - x;\n"
            + "    shade = 0.84 + 0.16 * cos(ang(u));\n"
            + "  } else if (iC.y < 1.5) {\n"
            + "    float k = max(u - iB.z, 0.0) / (1.0 - iB.z);\n"
            + "    l.y += iB.w * k * k * sin(iC.x - u * 2.5);\n"
            + "  } else if (iC.y < 2.5) {\n"
            + "    float span = abs(aPos.y) * 2.0;\n"
            + "    float f = sin(iC.x - u * 1.2);\n"
            + "    l.y = aPos.y * (1.0 - iB.w * span * (0.5 + 0.5 * f));\n"
            + "    l.x += 0.03 * span * f;\n"
            + "    shade = 1.0 - 0.22 * span * (0.5 + 0.5 * f);\n"
            + "  }\n"
            + "  vec2 lw = vec2(l.x * iB.x, l.y * iB.y);\n"
            + "  vec2 w = iA.xy + vec2(lw.x * iA.z - lw.y * iA.w, lw.x * iA.w + lw.y * iA.z);\n"
            + "  vUv = aUv;\n"
            + "  vLight = shade * (1.0 + iC.z);\n"
            + "  vColor = vec4(uTint * iD.rgb, iC.w);\n"
            + "  vFog = iD.w;\n"
            + "  gl_Position = vec4(w * uView - 1.0, 0.0, 1.0);\n"
            + "}\n";

    private static final String LIT_FS =
            "precision mediump float;\n"
            + "varying vec2 vUv; varying float vLight; varying vec4 vColor; varying float vFog;\n"
            + "uniform sampler2D uTex; uniform vec3 uFogColor;\n"
            + "void main() {\n"
            + "  vec4 t = texture2D(uTex, vUv);\n"
            + "  vec3 c = mix(t.rgb * vLight, uFogColor * t.a, vFog);\n"
            + "  gl_FragColor = vec4(c * vColor.rgb, t.a) * vColor.a;\n"
            + "}\n";

    private static final String FLOOR_VS =
            "attribute vec2 aPos; attribute vec2 aUv;\n"
            + "uniform vec2 uView; uniform vec4 uRect;\n"
            + "varying vec2 vUv; varying vec2 vWorld;\n"
            + "void main() {\n"
            + "  vec2 w = vec2(uRect.x + aPos.x * uRect.z, uRect.y + aPos.y * uRect.w);\n"
            + "  vUv = vec2(aUv.x * uRect.z / (uRect.w * 4.0), aUv.y);\n"
            + "  vWorld = w / 1080.0;\n"
            + "  gl_Position = vec4(w * uView - 1.0, 0.0, 1.0);\n"
            + "}\n";

    private static final String FLOOR_FS =
            "precision mediump float;\n"
            + "varying vec2 vUv; varying vec2 vWorld;\n"
            + "uniform sampler2D uTex; uniform sampler2D uCaust;\n"
            + "uniform vec3 uTint; uniform vec4 uCOff; uniform float uCAmt;\n"
            + "void main() {\n"
            + "  vec4 t = texture2D(uTex, vUv);\n"
            + "  float a = texture2D(uCaust, vWorld * 2.6 + uCOff.xy).r;\n"
            + "  float b = texture2D(uCaust, vWorld * 2.2 + uCOff.zw).r;\n"
            + "  vec3 c = t.rgb + min(a, b) * uCAmt * t.a * vec3(0.6, 0.8, 0.85);\n"
            + "  gl_FragColor = vec4(c * uTint, t.a);\n"
            + "}\n";

    private static final String BG_VS =
            "attribute vec2 aPos; attribute vec2 aUv;\n"
            + "varying vec2 vUv;\n"
            + "void main() { vUv = aPos * 0.5 + 0.5; gl_Position = vec4(aPos, 0.0, 1.0); }\n";

    /**
     * Slanted shafts of sunlight from the surface. They only vary along one
     * slanted axis, so the CPU fills a 512 texel strip each frame and the
     * shader looks it up instead of doing the maths per pixel.
     */
    private static final String RAYS_FN =
            "uniform sampler2D uRayTex; uniform float uRays; uniform float uSRange;\n"
            + "float rays(vec2 p) {\n"
            + "  float s = (p.x + (1.0 - p.y) * 0.32) / uSRange;\n"
            + "  return texture2D(uRayTex, vec2(s, 0.5)).r * 2.4 * p.y * p.y * uRays;\n"
            + "}\n";

    private static final String BG_FS =
            "precision mediump float;\n"
            + "varying vec2 vUv;\n"
            + "uniform vec3 uTop; uniform vec3 uMid; uniform vec3 uBot; uniform vec3 uTint;\n"
            + "uniform sampler2D uCaust; uniform vec4 uCOff; uniform float uCAmt; uniform float uAspect; uniform float uSurface;\n"
            + RAYS_FN
            + "void main() {\n"
            + "  float y = vUv.y;\n"
            + "  vec3 c = mix(uBot, uMid, smoothstep(0.0, 0.55, y));\n"
            + "  c = mix(c, uTop, smoothstep(0.55, 1.0, y));\n"
            + "  vec2 p = vec2(vUv.x * uAspect, y);\n"
            + "  float a = texture2D(uCaust, p * 2.3 + uCOff.xy).r;\n"
            + "  float b = texture2D(uCaust, p * 1.9 + uCOff.zw).r;\n"
            + "  float k = min(a, b);\n"
            + "  c += k * uCAmt * y * y * y * vec3(0.5, 0.75, 0.85);\n"
            + "  float s = smoothstep(0.88, 1.0, y);\n"
            + "  c += s * uSurface * (0.7 + 0.9 * k) * vec3(0.55, 0.75, 0.8);\n"
            + "  c += rays(p) * 0.6 * vec3(0.75, 0.9, 1.0);\n"
            + "  vec2 d = (vUv - 0.5) * vec2(1.0, 0.8);\n"
            + "  c *= 1.0 - smoothstep(0.3, 0.8, length(d)) * 0.35;\n"
            + "  gl_FragColor = vec4(c * uTint, 1.0);\n"
            + "}\n";

    private static final String POINT_VS =
            "attribute vec2 aPos; attribute vec2 aUv;\n"
            + "uniform vec2 uView; uniform float uPx;\n"
            + "varying float vA;\n"
            + "void main() { gl_Position = vec4(aPos * uView - 1.0, 0.0, 1.0); gl_PointSize = max(1.0, aUv.x * uPx); vA = aUv.y; }\n";

    private static final String POINT_FS =
            "precision mediump float;\n"
            + "varying float vA; uniform sampler2D uTex; uniform vec3 uTint;\n"
            + "void main() { gl_FragColor = texture2D(uTex, gl_PointCoord) * vA * vec4(uTint, 1.0); }\n";

    /** Light shafts over everything, added on top; only the upper part of the screen is drawn. */
    private static final String RAYS_VS =
            "attribute vec2 aPos; attribute vec2 aUv;\n"
            + "uniform float uFrom;\n"
            + "varying vec2 vUv;\n"
            + "void main() { vec2 p = vec2(aPos.x, mix(uFrom, 1.0, aPos.y * 0.5 + 0.5)); vUv = p * 0.5 + 0.5; gl_Position = vec4(p, 0.0, 1.0); }\n";

    private static final String RAYS_FS =
            "precision mediump float;\n"
            + "varying vec2 vUv; uniform float uAspect; uniform vec3 uTint;\n"
            + RAYS_FN
            + "void main() { gl_FragColor = vec4(rays(vec2(vUv.x * uAspect, vUv.y)) * 0.35 * vec3(0.8, 0.92, 1.0) * uTint, 0.0); }\n";

    private static final class Look {
        float[] top;
        float[] mid;
        float[] bot;
        float[] fog;
        float fogK;
    }

    private static Look look(Config.Theme theme) {
        Look l = new Look();
        switch (theme) {
            case OCEAN:
                l.top = new float[] {0.26f, 0.60f, 0.84f};
                l.mid = new float[] {0.05f, 0.30f, 0.56f};
                l.bot = new float[] {0.02f, 0.11f, 0.25f};
                l.fog = new float[] {0.06f, 0.30f, 0.52f};
                l.fogK = 0.5f;
                break;
            case KELP_FOREST:
                l.top = new float[] {0.36f, 0.64f, 0.60f};
                l.mid = new float[] {0.11f, 0.36f, 0.38f};
                l.bot = new float[] {0.04f, 0.17f, 0.19f};
                l.fog = new float[] {0.12f, 0.36f, 0.37f};
                l.fogK = 0.48f;
                break;
            case FISH_TANK:
                l.top = new float[] {0.42f, 0.66f, 0.56f};
                l.mid = new float[] {0.15f, 0.40f, 0.36f};
                l.bot = new float[] {0.06f, 0.21f, 0.19f};
                l.fog = new float[] {0.17f, 0.41f, 0.36f};
                l.fogK = 0.42f;
                break;
            default:
                l.top = new float[] {0.34f, 0.72f, 0.86f};
                l.mid = new float[] {0.09f, 0.47f, 0.70f};
                l.bot = new float[] {0.03f, 0.24f, 0.42f};
                l.fog = new float[] {0.12f, 0.50f, 0.70f};
                l.fogK = 0.42f;
                break;
        }
        return l;
    }

    private float fogScale = 1f;
    private float causticScale = 1f;
    private float causticSpeed = 1f;
    private float rayScale = 1f;
    private boolean nearest;

    /** Water colour, haze and sunlight for the chosen look. */
    private void styleScene() {
        switch (cfg.style) {
            case ANIMATED:
                brighten(look.top, 1.1f);
                brighten(look.mid, 1.15f);
                brighten(look.bot, 1.25f);
                fogScale = 0.8f;
                break;
            case CARTOON:
                brighten(look.top, 1.15f);
                brighten(look.mid, 1.25f);
                brighten(look.bot, 1.5f);
                fogScale = 0.45f;
                causticScale = 0.8f;
                break;
            case MOVIE:
                brighten(look.top, 1.12f);
                brighten(look.mid, 1.2f);
                brighten(look.bot, 1.3f);
                fogScale = 0.75f;
                rayScale = 1.35f;
                break;
            case PAINTED:
                mix(look.top, new float[] {0.45f, 0.78f, 0.74f}, 0.3f);
                mix(look.mid, new float[] {0.16f, 0.52f, 0.54f}, 0.3f);
                mix(look.bot, new float[] {0.06f, 0.26f, 0.32f}, 0.3f);
                fogScale = 0.7f;
                causticScale = 1.25f;
                causticSpeed = 0.35f;
                rayScale = 1.2f;
                break;
            case RETRO:
                look.top = new float[] {0.05f, 0.38f, 0.78f};
                look.mid = new float[] {0.02f, 0.22f, 0.58f};
                look.bot = new float[] {0f, 0.08f, 0.3f};
                fogScale = 0f;
                causticScale = 0f;
                rayScale = 0f;
                nearest = true;
                break;
            default:
                break;
        }
        look.fogK *= fogScale;
    }

    private static void mix(float[] c, float[] to, float t) {
        for (int i = 0; i < 3; i++) {
            c[i] += (to[i] - c[i]) * t;
        }
    }

    private static void brighten(float[] c, float k) {
        for (int i = 0; i < 3; i++) {
            c[i] = Math.min(1f, c[i] * k);
        }
    }

    /** A linked program with its uniform locations looked up once. */
    private static final class Prog {
        final int id;
        final int uView;
        final int uTex;
        final int uTint;
        final int uFogColor;
        final int uCaust;
        final int uCOff;
        final int uCAmt;
        final int uRayTex;
        final int uSRange;
        final int uRays;
        final int uAspect;
        final int uSurface;
        final int uTop;
        final int uMid;
        final int uBot;
        final int uPx;
        final int uRect;
        final int uFrom;

        Prog(String vs, String fs) {
            id = Gl.program(vs, fs);
            uView = GLES20.glGetUniformLocation(id, "uView");
            uTex = GLES20.glGetUniformLocation(id, "uTex");
            uTint = GLES20.glGetUniformLocation(id, "uTint");
            uFogColor = GLES20.glGetUniformLocation(id, "uFogColor");
            uCaust = GLES20.glGetUniformLocation(id, "uCaust");
            uCOff = GLES20.glGetUniformLocation(id, "uCOff");
            uCAmt = GLES20.glGetUniformLocation(id, "uCAmt");
            uRayTex = GLES20.glGetUniformLocation(id, "uRayTex");
            uSRange = GLES20.glGetUniformLocation(id, "uSRange");
            uRays = GLES20.glGetUniformLocation(id, "uRays");
            uAspect = GLES20.glGetUniformLocation(id, "uAspect");
            uSurface = GLES20.glGetUniformLocation(id, "uSurface");
            uTop = GLES20.glGetUniformLocation(id, "uTop");
            uMid = GLES20.glGetUniformLocation(id, "uMid");
            uBot = GLES20.glGetUniformLocation(id, "uBot");
            uPx = GLES20.glGetUniformLocation(id, "uPx");
            uRect = GLES20.glGetUniformLocation(id, "uRect");
            uFrom = GLES20.glGetUniformLocation(id, "uFrom");
        }
    }

    private static final int FLOATS_PER_INSTANCE = 16;
    private static final int MAX_INSTANCES = 512;
    private static final int RAY_TEXELS = 512;
    private static final float RAYS_FROM = -0.25f;

    private final Config cfg;
    private final Look look;
    private final boolean instancing;
    private Sim sim;
    private float worldW;
    private int surfaceH;

    private Prog sprite;
    private Prog body;
    private Prog floor;
    private Prog bg;
    private Prog points;
    private Prog shafts;
    private Prog current;
    private Gl.Mesh spriteMesh;
    private Gl.Mesh bodyMesh;
    private Gl.Mesh screenMesh;
    private final Map<String, Integer> textures = new HashMap<>();
    private final Map<Object, String> keyFor = new HashMap<>();
    private final Map<String, BitmapSource> sources = new HashMap<>();
    private final Map<String, java.util.concurrent.Future<android.graphics.Bitmap>> pending = new HashMap<>();
    private final Map<String, Double> bornAt = new HashMap<>();
    private final java.io.File cacheDir;
    private final String cachePrefix;
    private java.util.concurrent.ExecutorService pool;
    private int causticTex;
    private int rayTex;
    private final ByteBuffer rayBytes = ByteBuffer.allocateDirect(RAY_TEXELS);
    private int boundTex = -1;
    private int instanceVbo;

    private final float[] inst = new float[FLOATS_PER_INSTANCE * MAX_INSTANCES];
    private final FloatBuffer instBuffer = Gl.floats(new float[FLOATS_PER_INSTANCE * MAX_INSTANCES]);
    private int instCount;
    private Prog batchProg;
    private int batchTex;

    private final List<Object> drawList = new ArrayList<>();
    private float[] pointData = new float[0];
    private FloatBuffer pointBuffer;

    private volatile boolean halfRate;
    private int paceFrames;
    private float paceSeconds;
    private long lastNanos;
    private double time;
    private float fade;
    private float[] tint = {1f, 1f, 1f};
    private final float[] lightTint = new float[3];
    private float tintTimer;
    private final float[] cOff = new float[4];
    private final float[] spot = new float[2];

    AquariumRenderer(Config cfg, boolean instancing, java.io.File cacheDir, String cachePrefix) {
        this.cfg = cfg;
        this.look = look(cfg.theme);
        this.instancing = instancing;
        this.cacheDir = cacheDir;
        this.cachePrefix = cachePrefix + cfg.style.name().toLowerCase(java.util.Locale.ROOT) + "-";
        styleScene();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        textures.clear();
        pending.clear();
        bornAt.clear();
        boundTex = -1;
        if (pool == null) {
            pool = java.util.concurrent.Executors.newFixedThreadPool(3, r -> {
                Thread t = new Thread(r, "aquarium-art");
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            });
        }
        current = null;
        Gl.resetBindings();
        sprite = new Prog(SPRITE_VS, LIT_FS);
        body = new Prog(BODY_VS, LIT_FS);
        floor = new Prog(FLOOR_VS, FLOOR_FS);
        bg = new Prog(BG_VS, BG_FS);
        points = new Prog(POINT_VS, POINT_FS);
        shafts = new Prog(RAYS_VS, RAYS_FS);
        spriteMesh = new Gl.Mesh(-0.5f, 0.5f, 0f, 1f, 2, 14, true);
        bodyMesh = new Gl.Mesh(-0.5f, 0.5f, -0.5f, 0.5f, 18, 8, true);
        screenMesh = new Gl.Mesh(-1f, 1f, -1f, 1f, 1, 1, true);
        int[] ids = new int[2];
        GLES20.glGenBuffers(1, ids, 0);
        instanceVbo = ids[0];
        causticTex = Gl.texture(Textures.caustics(), true, false);
        GLES20.glGenTextures(1, ids, 1);
        rayTex = ids[1];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, rayTex);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, RAY_TEXELS, 1, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, rayBytes);
        textures.put("dot", Gl.texture(Textures.dot(), false, false));
        textures.put("bubble", Gl.texture(Textures.bubble(), false, nearest));
        textures.put("floor", Gl.texture(Textures.floor(cfg.theme), true, nearest));
        EGL14.eglSwapInterval(EGL14.eglGetCurrentDisplay(), 1);
        paceFrames = 0;
        paceSeconds = 0f;
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DITHER);
        if (sim != null) {
            for (String key : sources.keySet()) {
                request(key);
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        surfaceH = height;
        float w = Sim.H * width / (float) height;
        if (sim == null || Math.abs(w - worldW) > 1f) {
            worldW = w;
            sim = new Sim(cfg, w, System.nanoTime());
            loadSpriteTextures();
            drawList.clear();
            drawList.addAll(sim.plants);
            drawList.addAll(sim.creatures);
        }
        lastNanos = 0;
    }

    private interface BitmapSource {
        android.graphics.Bitmap make();
    }

    /** Remembers which art a sprite needs and starts making it in the background. */
    private void want(Object owner, String key, BitmapSource src) {
        if (owner != null) {
            keyFor.put(owner, key);
        }
        sources.put(key, src);
        request(key);
    }

    private void request(String key) {
        if (textures.containsKey(key) || pending.containsKey(key)) {
            return;
        }
        BitmapSource src = sources.get(key);
        pending.put(key, pool.submit(() -> loadOrMake(key, src)));
    }

    /** Reads the art from the on-device cache, or paints it and saves it for next time. */
    private android.graphics.Bitmap loadOrMake(String key, BitmapSource src) {
        java.io.File file = new java.io.File(cacheDir, cachePrefix + key + ".png");
        if (file.isFile()) {
            android.graphics.BitmapFactory.Options o = new android.graphics.BitmapFactory.Options();
            o.inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888;
            o.inScaled = false;
            android.graphics.Bitmap cached = android.graphics.BitmapFactory.decodeFile(file.getPath(), o);
            if (cached != null) {
                return cached;
            }
        }
        android.graphics.Bitmap made = src.make();
        java.io.File tmp = new java.io.File(cacheDir, file.getName() + ".tmp");
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(tmp)) {
            made.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
        } catch (java.io.IOException e) {
            tmp.delete();
            return made;
        }
        if (!tmp.renameTo(file)) {
            tmp.delete();
        }
        return made;
    }

    /** Uploads finished art, a few textures per frame so no single frame stalls. */
    private void pumpUploads() {
        if (pending.isEmpty()) {
            return;
        }
        int budget = 3;
        java.util.Iterator<Map.Entry<String, java.util.concurrent.Future<android.graphics.Bitmap>>> it = pending.entrySet().iterator();
        while (it.hasNext() && budget > 0) {
            Map.Entry<String, java.util.concurrent.Future<android.graphics.Bitmap>> e = it.next();
            if (!e.getValue().isDone()) {
                continue;
            }
            it.remove();
            try {
                textures.put(e.getKey(), Gl.texture(e.getValue().get(), false, nearest));
                bornAt.put(e.getKey(), time);
                boundTex = -1;
                budget--;
            } catch (InterruptedException | java.util.concurrent.ExecutionException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Texture id for some art, or -1 while it is still being made. */
    private int ready(String key) {
        Integer id = textures.get(key);
        return id == null ? -1 : id;
    }

    /** Fades new art in over a moment instead of popping. */
    private float appear(String key) {
        Double t = bornAt.get(key);
        return t == null ? 0f : (float) Math.min(1.0, (time - t) / 0.8);
    }

    void release() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    private void loadSpriteTextures() {
        for (Sim.Creature c : sim.creatures) {
            Species s = c.species;
            switch (s.kind) {
                case RAY:
                    want(c, CreatureArt.MANTA, () -> CreatureArt.paint(CreatureArt.MANTA, cfg.style));
                    break;
                case TURTLE:
                    want(c, CreatureArt.TURTLE_BODY, () -> CreatureArt.paint(CreatureArt.TURTLE_BODY, cfg.style));
                    want(null, CreatureArt.TURTLE_FLIPPER, () -> CreatureArt.paint(CreatureArt.TURTLE_FLIPPER, cfg.style));
                    break;
                case OCTOPUS:
                    want(c, CreatureArt.OCTOPUS_MANTLE, () -> CreatureArt.paint(CreatureArt.OCTOPUS_MANTLE, cfg.style));
                    want(null, CreatureArt.OCTOPUS_ARM, () -> CreatureArt.paint(CreatureArt.OCTOPUS_ARM, cfg.style));
                    break;
                case JELLY:
                    want(c, CreatureArt.JELLY_BELL, () -> CreatureArt.paint(CreatureArt.JELLY_BELL, cfg.style));
                    want(null, CreatureArt.JELLY_TENTACLE, () -> CreatureArt.paint(CreatureArt.JELLY_TENTACLE, cfg.style));
                    want(null, CreatureArt.JELLY_ARM, () -> CreatureArt.paint(CreatureArt.JELLY_ARM, cfg.style));
                    break;
                case SEAHORSE:
                    want(c, CreatureArt.SEAHORSE, () -> CreatureArt.paint(CreatureArt.SEAHORSE, cfg.style));
                    break;
                case CRAB:
                    want(c, CreatureArt.CRAB, () -> CreatureArt.paint(CreatureArt.CRAB, cfg.style));
                    break;
                case STARFISH:
                    want(c, CreatureArt.STARFISH, () -> CreatureArt.paint(CreatureArt.STARFISH, cfg.style));
                    break;
                default:
                    want(c, s.id, () -> FishArt.paint(s, cfg.style));
                    break;
            }
        }
        for (Sim.Plant p : sim.plants) {
            want(p, p.kind.id + p.variant, () -> PlantArt.paint(p.kind, p.variant, cfg.style));
        }
    }

    private static float zOf(Object o) {
        return o instanceof Sim.Plant ? ((Sim.Plant) o).z : ((Sim.Creature) o).z;
    }

    private void sortDrawList() {
        for (int i = 1; i < drawList.size(); i++) {
            Object item = drawList.get(i);
            float z = zOf(item);
            int j = i - 1;
            while (j >= 0 && zOf(drawList.get(j)) < z) {
                drawList.set(j + 1, drawList.get(j));
                j--;
            }
            drawList.set(j + 1, item);
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        long now = System.nanoTime();
        float dt = lastNanos == 0 ? 0f : Math.min(0.1f, (now - lastNanos) / 1e9f);
        lastNanos = now;
        time += dt;
        pace(dt);
        fade = Math.min(1f, fade + dt / 1.6f);
        sim.update(dt);
        updateTint(dt);
        updateCausticOffsets();
        updateRays();
        pumpUploads();
        sortDrawList();

        current = null;
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawBackground();
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        drawFloor();
        drawShadows();
        boolean bubblesDrawn = false;
        for (int i = 0; i < drawList.size(); i++) {
            Object o = drawList.get(i);
            if (!bubblesDrawn && zOf(o) < 0.78f) {
                flush();
                drawBubbles();
                bubblesDrawn = true;
            }
            if (o instanceof Sim.Plant) {
                drawPlant((Sim.Plant) o);
            } else {
                drawCreature((Sim.Creature) o);
            }
        }
        flush();
        if (!bubblesDrawn) {
            drawBubbles();
        }
        drawMotes();
        if (rayStrength() > 0f) {
            drawShafts();
        }
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    /** True when frames should be drawn on every other display refresh (30 a second). */
    boolean halfRate() {
        return halfRate || cfg.fps == 30;
    }

    /**
     * Automatic frame rate: after a few seconds at 60, drop to a steady 30 if
     * frames are not keeping up. Uneven 60/30 pacing looks worse than 30.
     * The first seconds are skipped because art is still being uploaded.
     */
    private void pace(float dt) {
        if (cfg.fps != 0 || halfRate || dt <= 0f || time < 3.0) {
            return;
        }
        paceFrames++;
        paceSeconds += dt;
        if (paceSeconds < 4f) {
            return;
        }
        if (paceFrames / paceSeconds < 54f) {
            halfRate = true;
        }
        paceFrames = 0;
        paceSeconds = 0f;
    }

    private void updateTint(float dt) {
        tintTimer -= dt;
        if (tintTimer <= 0f) {
            Calendar now = Calendar.getInstance();
            float hour = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60f;
            tint = Config.tint(cfg.lighting, hour);
            tintTimer = 30f;
        }
        float k = cfg.brightness * fade;
        lightTint[0] = tint[0] * k;
        lightTint[1] = tint[1] * k;
        lightTint[2] = tint[2] * k;
    }

    private void updateCausticOffsets() {
        double t = time * cfg.speed * causticSpeed;
        cOff[0] = (float) ((t * 0.011) % 1.0);
        cOff[1] = (float) ((t * 0.007) % 1.0);
        cOff[2] = (float) (1.0 - (t * 0.008) % 1.0);
        cOff[3] = (float) ((t * 0.0095) % 1.0);
    }

    private float sRange() {
        return worldW / Sim.H + 0.32f;
    }

    /** Fills the light-shaft strip for this frame; see RAYS_FN. */
    private void updateRays() {
        if (rayStrength() == 0f) {
            return;
        }
        double t = time * cfg.speed;
        float range = sRange();
        for (int i = 0; i < RAY_TEXELS; i++) {
            double sv = i / (double) (RAY_TEXELS - 1) * range;
            double r = pow(0.5 + 0.5 * Math.sin(sv * 5.3 + t * 0.21), 9)
                    + 0.8 * pow(0.5 + 0.5 * Math.sin(sv * 8.9 - t * 0.17 + 1.7), 12)
                    + 0.6 * pow(0.5 + 0.5 * Math.sin(sv * 3.1 + t * 0.11 + 4.1), 7);
            r *= 0.75 + 0.25 * Math.sin(t * 0.9 + sv * 2.0);
            rayBytes.put(i, (byte) Math.max(0, Math.min(255, (int) (r / 2.4 * 255))));
        }
        rayBytes.position(0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, rayTex);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, RAY_TEXELS, 1, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, rayBytes);
        boundTex = rayTex;
    }

    private static double pow(double v, int n) {
        double out = 1;
        for (int i = 0; i < n; i++) {
            out *= v;
        }
        return out;
    }

    private float shimmer(float base) {
        return (cfg.shimmer == 0 ? 0f : (cfg.shimmer == 1 ? base : base * 1.8f)) * causticScale * daylight();
    }

    private float rayStrength() {
        return (cfg.shimmer == 0 ? 0f : (cfg.shimmer == 1 ? 0.22f : 0.45f)) * rayScale * daylight();
    }

    /** 1 in full daylight, lower at dusk and night, so sunlight effects fade to moonlight. */
    private float daylight() {
        return Math.min(1f, (tint[0] + tint[1] + tint[2]) / 3f);
    }

    /** Sunlight dancing over something at (x, y): a cheap stand-in for caustics on sprites. */
    private float glow(float x, float y, float z) {
        if (cfg.shimmer == 0 || causticScale == 0f) {
            return 0f;
        }
        double t = time * cfg.speed;
        float a = (float) Math.sin(t * 1.3 + x * 0.011 - y * 0.004);
        float b = (float) Math.sin(t * 0.83 - x * 0.017 + y * 0.009 + 1.3);
        return shimmer(0.22f) * Math.max(0f, a * b) * (0.4f + 0.6f * y / Sim.H) * (1f - z * 0.6f);
    }

    private void use(Prog p) {
        if (current != p) {
            GLES20.glUseProgram(p.id);
            current = p;
            if (p.uView >= 0) {
                GLES20.glUniform2f(p.uView, 2f / worldW, 2f / Sim.H);
            }
            if (p.uTex >= 0) {
                GLES20.glUniform1i(p.uTex, 0);
            }
        }
    }

    private void bindTexture(int id) {
        if (id != boundTex) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
            boundTex = id;
        }
    }

    private void bindCaustics(Prog p) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, causticTex);
        GLES20.glUniform1i(p.uCaust, 1);
        GLES20.glUniform4f(p.uCOff, cOff[0], cOff[1], cOff[2], cOff[3]);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }

    private void bindRays(Prog p) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, rayTex);
        GLES20.glUniform1i(p.uRayTex, 2);
        GLES20.glUniform1f(p.uSRange, sRange());
        GLES20.glUniform1f(p.uRays, rayStrength());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }

    private void drawBackground() {
        use(bg);
        GLES20.glUniform3fv(bg.uTop, 1, look.top, 0);
        GLES20.glUniform3fv(bg.uMid, 1, look.mid, 0);
        GLES20.glUniform3fv(bg.uBot, 1, look.bot, 0);
        GLES20.glUniform3fv(bg.uTint, 1, lightTint, 0);
        GLES20.glUniform1f(bg.uCAmt, shimmer(0.22f));
        GLES20.glUniform1f(bg.uAspect, worldW / Sim.H);
        GLES20.glUniform1f(bg.uSurface, cfg.shimmer == 0 ? 0.08f : (cfg.shimmer == 1 ? 0.13f : 0.2f));
        bindRays(bg);
        bindCaustics(bg);
        screenMesh.draw();
    }

    private void drawFloor() {
        use(floor);
        float h = Sim.rootY(1f) + 50f;
        bindTexture(textures.get("floor"));
        GLES20.glUniform3fv(floor.uTint, 1, lightTint, 0);
        GLES20.glUniform1f(floor.uCAmt, shimmer(0.24f));
        GLES20.glUniform4f(floor.uRect, worldW / 2f, 0f, worldW, h);
        bindCaustics(floor);
        spriteMesh.draw();
    }

    // Batching. Each queued instance is 16 floats; a batch is a run with the same program and texture.

    private void queue(Prog p, int tex, float a0, float a1, float a2, float a3, float b0, float b1, float b2, float b3,
            float c0, float c1, float c2, float c3, float d0, float d1, float d2, float d3) {
        if (p != batchProg || tex != batchTex || instCount == MAX_INSTANCES) {
            flush();
            batchProg = p;
            batchTex = tex;
        }
        int o = instCount * FLOATS_PER_INSTANCE;
        float[] f = inst;
        f[o] = a0;
        f[o + 1] = a1;
        f[o + 2] = a2;
        f[o + 3] = a3;
        f[o + 4] = b0;
        f[o + 5] = b1;
        f[o + 6] = b2;
        f[o + 7] = b3;
        f[o + 8] = c0;
        f[o + 9] = c1;
        f[o + 10] = c2;
        f[o + 11] = c3;
        f[o + 12] = d0;
        f[o + 13] = d1;
        f[o + 14] = d2;
        f[o + 15] = d3;
        instCount++;
    }

    /** Queues a sprite: origin oy is 0 for base-anchored, 0.5 for centred. */
    private void sprite(int tex, float x, float y, float angle, float width, float height, float sway, float swayPhase,
            float oy, float glow, float alpha, float r, float g, float b, float fog) {
        queue(sprite, tex, x, y, (float) Math.cos(angle), (float) Math.sin(angle), width, height, sway, swayPhase,
                1f, oy, glow, alpha, r, g, b, fog);
    }

    private void flush() {
        if (instCount == 0) {
            return;
        }
        Prog p = batchProg;
        use(p);
        GLES20.glUniform3fv(p.uTint, 1, lightTint, 0);
        GLES20.glUniform3fv(p.uFogColor, 1, look.fog, 0);
        bindTexture(batchTex);
        Gl.Mesh mesh = p == body ? bodyMesh : spriteMesh;
        mesh.bindAttributes();
        if (instancing) {
            instBuffer.position(0);
            instBuffer.put(inst, 0, instCount * FLOATS_PER_INSTANCE);
            instBuffer.position(0);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, instanceVbo);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, instCount * FLOATS_PER_INSTANCE * 4, instBuffer, GLES20.GL_STREAM_DRAW);
            for (int i = 0; i < 4; i++) {
                GLES20.glEnableVertexAttribArray(2 + i);
                GLES20.glVertexAttribPointer(2 + i, 4, GLES20.GL_FLOAT, false, FLOATS_PER_INSTANCE * 4, i * 16);
                GLES30.glVertexAttribDivisor(2 + i, 1);
            }
            GLES30.glDrawElementsInstanced(GLES20.GL_TRIANGLES, mesh.count, GLES20.GL_UNSIGNED_SHORT, 0, instCount);
            Gl.resetBindings();
        } else {
            for (int i = 0; i < 4; i++) {
                GLES20.glDisableVertexAttribArray(2 + i);
            }
            for (int n = 0; n < instCount; n++) {
                int o = n * FLOATS_PER_INSTANCE;
                for (int i = 0; i < 4; i++) {
                    GLES20.glVertexAttrib4f(2 + i, inst[o + i * 4], inst[o + i * 4 + 1], inst[o + i * 4 + 2], inst[o + i * 4 + 3]);
                }
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, mesh.count, GLES20.GL_UNSIGNED_SHORT, 0);
            }
        }
        instCount = 0;
    }

    private void drawShadows() {
        int dot = textures.get("dot");
        for (int i = 0; i < sim.creatures.size(); i++) {
            Sim.Creature c = sim.creatures.get(i);
            if (!c.active) {
                continue;
            }
            float sc = Sim.scale(c.z);
            float above = c.y - Sim.rootY(c.z);
            float strength = Math.max(0f, 1f - above / 520f) * (1f - c.z * 0.6f);
            if (strength <= 0.02f) {
                continue;
            }
            float len = c.species.length * sc * 0.8f;
            sprite(dot, c.x, Sim.rootY(c.z) - 4f * sc, 0f, len, len * 0.16f, 0f, 0f, 0.5f, 0f, 0.3f * strength, 0f, 0f, 0f, 0f);
        }
        flush();
    }

    private void drawPlant(Sim.Plant p) {
        String key = keyFor.get(p);
        int tex = ready(key);
        if (tex < 0) {
            return;
        }
        float sc = Sim.scale(p.z);
        float shade = 1f - 0.18f * p.z;
        float baseY = Sim.rootY(p.z) - 8f * sc;
        double phase = p.phase + time * Sim.TWO_PI * p.freq * cfg.speed;
        float sway = p.sway * sc * (0.7f + 0.3f * (float) Math.sin(time * 0.17));
        sprite(tex, p.x, baseY, 0f, p.width * sc * (p.flip ? -1f : 1f), p.height * sc, sway, (float) (phase % Sim.TWO_PI),
                0f, glow(p.x, baseY + p.height * sc * 0.5f, p.z), appear(key), shade, shade, shade, p.z * look.fogK);
    }

    private void drawCreature(Sim.Creature c) {
        if (!c.active) {
            return;
        }
        String key = keyFor.get(c);
        int tex = ready(key);
        if (tex < 0) {
            return;
        }
        float alpha = fade * appear(key);
        Species s = c.species;
        float sc = Sim.scale(c.z);
        float fog = c.z * look.fogK;
        float shade = 1f - 0.15f * c.z;
        float lightUp = glow(c.x, c.y, c.z);
        switch (s.kind) {
            case TURTLE:
                if (ready(CreatureArt.TURTLE_FLIPPER) >= 0) {
                    drawTurtle(c, sc, shade, fog, lightUp, alpha);
                }
                return;
            case OCTOPUS:
                if (ready(CreatureArt.OCTOPUS_ARM) >= 0) {
                    drawOctopus(c, sc, shade, fog, lightUp, alpha);
                }
                return;
            case JELLY:
                if (ready(CreatureArt.JELLY_TENTACLE) >= 0 && ready(CreatureArt.JELLY_ARM) >= 0) {
                    drawJelly(c, sc, shade, fog, lightUp, alpha);
                }
                return;
            default:
                break;
        }
        int mode;
        float amp = s.tailAmp;
        float y = c.y;
        float height = s.height() * sc;
        switch (s.kind) {
            case WHALE:
            case DOLPHIN:
                mode = 1;
                amp = s.tailAmp * s.length / s.height();
                break;
            case RAY:
                mode = 2;
                height *= 0.5f;
                break;
            case CRAB:
                mode = 3;
                if (c.vx != 0f) {
                    y += Math.abs((float) Math.sin(c.tailPhase)) * 3f * sc;
                }
                break;
            case SEAHORSE:
            case STARFISH:
                mode = 3;
                break;
            default:
                mode = 0;
                break;
        }
        float angle = c.tilt * c.face;
        float facing = s.kind == Species.Kind.CRAB || s.kind == Species.Kind.STARFISH ? 1f : c.face;
        queue(body, tex, c.x, y, (float) Math.cos(angle), (float) Math.sin(angle),
                s.length * sc * facing, height, s.rigid, amp,
                c.tailPhase, mode, lightUp, alpha, shade, shade, shade, fog);
    }

    /** World position of a point in a creature's own frame (x toward its face), written to spot. */
    private void local(Sim.Creature c, float lx, float ly, float len, float hgt) {
        float angle = c.tilt * c.face;
        float ca = (float) Math.cos(angle);
        float sa = (float) Math.sin(angle);
        float px = lx * len * c.face;
        float py = ly * hgt;
        spot[0] = c.x + px * ca - py * sa;
        spot[1] = c.y + px * sa + py * ca;
    }

    private void drawTurtle(Sim.Creature c, float sc, float shade, float fog, float lightUp, float fade) {
        Species s = c.species;
        float len = s.length * sc;
        float hgt = s.height() * sc;
        float tilt = c.tilt * c.face;
        float front = 2.15f + 0.6f * (float) Math.sin(c.tailPhase);
        float rear = 2.55f + 0.18f * (float) Math.sin(c.tailPhase + 1f);
        float side = c.face >= 0f ? 1f : -1f;
        float squash = Math.max(0.25f, Math.abs(c.face));
        int flipper = ready(CreatureArt.TURTLE_FLIPPER);
        float dark = shade * 0.62f;
        local(c, CreatureArt.TURTLE_FRONT[0] - 0.03f, CreatureArt.TURTLE_FRONT[1] + 0.03f, len, hgt);
        sprite(flipper, spot[0], spot[1], tilt + side * (front - 0.15f), len * 0.13f * side * squash, len * 0.46f, 0f, 0f, 0f, 0f, fade, dark, dark, dark, fog);
        local(c, CreatureArt.TURTLE_REAR[0] + 0.02f, CreatureArt.TURTLE_REAR[1] + 0.02f, len, hgt);
        sprite(flipper, spot[0], spot[1], tilt + side * rear, len * 0.09f * side * squash, len * 0.2f, 0f, 0f, 0f, 0f, fade, dark, dark, dark, fog);
        sprite(ready(CreatureArt.TURTLE_BODY), c.x, c.y, tilt, len * c.face, hgt, 0f, 0f, 0.5f, lightUp, fade, shade, shade, shade, fog);
        local(c, CreatureArt.TURTLE_REAR[0], CreatureArt.TURTLE_REAR[1], len, hgt);
        sprite(flipper, spot[0], spot[1], tilt + side * (rear + 0.1f), len * 0.09f * side * squash, len * 0.2f, 0f, 0f, 0f, lightUp, fade, shade, shade, shade, fog);
        local(c, CreatureArt.TURTLE_FRONT[0], CreatureArt.TURTLE_FRONT[1], len, hgt);
        sprite(flipper, spot[0], spot[1], tilt + side * front, len * 0.14f * side * squash, len * 0.5f, 0f, 0f, 0f, lightUp, fade, shade, shade, shade, fog);
    }

    private static final float[] OCTO_BACK = {-1.25f, -0.8f, 0.8f, 1.25f};
    private static final float[] OCTO_FRONT = {-1.0f, -0.35f, 0.35f, 1.0f};

    private void drawOctopus(Sim.Creature c, float sc, float shade, float fog, float lightUp, float fade) {
        Species s = c.species;
        float len = s.length * sc;
        float camo = 0.5f + 0.5f * (float) Math.sin(time * 0.22 + c.x * 0.003f);
        float r = shade * (1f - 0.22f * camo);
        float g = shade * (1f - 0.1f * camo);
        float b = shade * (1f - 0.3f * camo);
        boolean moving = c.vx != 0f;
        float trail = c.lift > 30f ? -Math.signum(c.vx) * 0.7f * Math.min(1f, c.lift / 200f) : 0f;
        float armLen = len * 0.55f;
        float swayAmp = (moving ? 30f : 14f) * sc;
        int arm = ready(CreatureArt.OCTOPUS_ARM);
        for (int i = 0; i < OCTO_BACK.length; i++) {
            float ph = (float) ((c.tailPhase + i * 1.7f + time * 0.8) % Sim.TWO_PI);
            sprite(arm, c.x + OCTO_BACK[i] * 6f * sc, c.y + 4f * sc, (float) Math.PI + OCTO_BACK[i] + trail, armLen * 0.13f, armLen * 1.05f,
                    swayAmp, ph, 0f, 0f, fade, r * 0.7f, g * 0.7f, b * 0.7f, fog);
        }
        float bob = (float) Math.sin(time * 0.9 + c.x * 0.01f) * 3f * sc;
        sprite(ready(CreatureArt.OCTOPUS_MANTLE), c.x, c.y - 10f * sc + bob, c.tilt, len * 0.6f * (c.face >= 0f ? 1f : -1f), len * 0.6f,
                0f, 0f, 0f, lightUp, fade, r, g, b, fog);
        for (int i = 0; i < OCTO_FRONT.length; i++) {
            float ph = (float) ((c.tailPhase + i * 2.3f + 1f + time * 0.8) % Sim.TWO_PI);
            sprite(arm, c.x + OCTO_FRONT[i] * 8f * sc, c.y, (float) Math.PI + OCTO_FRONT[i] + trail, armLen * 0.13f, armLen,
                    swayAmp, ph, 0f, lightUp, fade, r, g, b, fog);
        }
    }

    private void drawJelly(Sim.Creature c, float sc, float shade, float fog, float lightUp, float fade) {
        Species s = c.species;
        float total = s.height() * sc;
        float bellW = s.length * sc;
        float beat = c.tailPhase / Sim.TWO_PI;
        float squeeze = beat < 0.35f ? (float) Math.sin(beat / 0.35f * Math.PI) : 0f;
        float sx = 1f - 0.16f * squeeze;
        float sy = 1f + 0.14f * squeeze;
        float bellH = bellW * 0.5f * sy;
        float base = c.y + total * 0.5f - bellH;
        float hang = total - bellH;
        int tentacle = ready(CreatureArt.JELLY_TENTACLE);
        for (int i = 0; i < 12; i++) {
            float off = (i / 11f - 0.5f) * bellW * 0.86f * sx;
            float ph = (float) ((time * 1.1 + i * 0.9 + c.x * 0.01f) % Sim.TWO_PI);
            sprite(tentacle, c.x + off, base + 4f * sc, (float) Math.PI + c.tilt, 7f * sc, hang * (0.75f + 0.25f * ((i * 7) % 5) / 4f),
                    22f * sc, ph, 0f, 0f, fade * 0.9f, shade, shade, shade, fog);
        }
        int arm = ready(CreatureArt.JELLY_ARM);
        for (int i = 0; i < 4; i++) {
            float off = (i / 3f - 0.5f) * bellW * 0.3f;
            float ph = (float) ((time * 0.8 + i * 1.6) % Sim.TWO_PI);
            sprite(arm, c.x + off, base + 8f * sc, (float) Math.PI + c.tilt, bellW * 0.16f, hang * 0.62f, 16f * sc, ph, 0f, 0f, fade * 0.9f,
                    shade, shade, shade, fog);
        }
        sprite(ready(CreatureArt.JELLY_BELL), c.x, base, c.tilt, bellW * sx, bellH, 0f, 0f, 0f, lightUp, fade, shade, shade, shade, fog);
    }

    private void drawBubbles() {
        List<Sim.Bubble> bubbles = sim.bubbles;
        if (bubbles.isEmpty()) {
            return;
        }
        ensurePoints(bubbles.size());
        int i = 0;
        for (int k = 0; k < bubbles.size(); k++) {
            Sim.Bubble b = bubbles.get(k);
            pointData[i++] = b.x;
            pointData[i++] = b.y;
            pointData[i++] = b.r * 2f * Sim.scale(b.z);
            pointData[i++] = 1f;
        }
        drawPoints(bubbles.size(), textures.get("bubble"));
    }

    private void drawMotes() {
        List<Sim.Mote> motes = sim.motes;
        if (motes.isEmpty() || cfg.style == Config.Style.RETRO) {
            return;
        }
        ensurePoints(motes.size());
        int i = 0;
        for (int k = 0; k < motes.size(); k++) {
            Sim.Mote m = motes.get(k);
            pointData[i++] = m.x;
            pointData[i++] = m.y;
            pointData[i++] = m.size * Sim.scale(m.z) * 2f;
            pointData[i++] = m.alpha;
        }
        drawPoints(motes.size(), textures.get("dot"));
    }

    private void ensurePoints(int n) {
        if (pointData.length < n * 4) {
            pointData = new float[n * 4 + 256];
            pointBuffer = Gl.floats(pointData);
        }
    }

    private void drawPoints(int n, int tex) {
        pointBuffer.position(0);
        pointBuffer.put(pointData, 0, n * 4);
        use(points);
        Gl.unbindMeshes();
        for (int i = 0; i < 4; i++) {
            GLES20.glDisableVertexAttribArray(2 + i);
        }
        GLES20.glUniform1f(points.uPx, surfaceH / Sim.H);
        GLES20.glUniform3fv(points.uTint, 1, lightTint, 0);
        bindTexture(tex);
        pointBuffer.position(0);
        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 16, pointBuffer);
        pointBuffer.position(2);
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 16, pointBuffer);
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glEnableVertexAttribArray(1);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, n);
    }

    /** Light shafts added over everything, across the upper part of the screen where they are visible. */
    private void drawShafts() {
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);
        use(shafts);
        GLES20.glUniform1f(shafts.uFrom, RAYS_FROM);
        GLES20.glUniform1f(shafts.uAspect, worldW / Sim.H);
        GLES20.glUniform3fv(shafts.uTint, 1, lightTint, 0);
        bindRays(shafts);
        screenMesh.draw();
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }
}
