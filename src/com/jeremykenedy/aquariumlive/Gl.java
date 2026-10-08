package com.jeremykenedy.aquariumlive;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/** Small helpers over GLES 2.0: shader programs, textures and meshes. */
final class Gl {
    private static int bound = -1;

    private Gl() {
    }

    static int program(String vertex, String fragment) {
        int vs = shader(GLES20.GL_VERTEX_SHADER, vertex);
        int fs = shader(GLES20.GL_FRAGMENT_SHADER, fragment);
        int prog = GLES20.glCreateProgram();
        GLES20.glAttachShader(prog, vs);
        GLES20.glAttachShader(prog, fs);
        GLES20.glBindAttribLocation(prog, 0, "aPos");
        GLES20.glBindAttribLocation(prog, 1, "aUv");
        GLES20.glBindAttribLocation(prog, 2, "iA");
        GLES20.glBindAttribLocation(prog, 3, "iB");
        GLES20.glBindAttribLocation(prog, 4, "iC");
        GLES20.glBindAttribLocation(prog, 5, "iD");
        GLES20.glLinkProgram(prog);
        int[] ok = new int[1];
        GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, ok, 0);
        if (ok[0] == 0) {
            String log = GLES20.glGetProgramInfoLog(prog);
            GLES20.glDeleteProgram(prog);
            throw new IllegalStateException("Program link failed: " + log);
        }
        GLES20.glDeleteShader(vs);
        GLES20.glDeleteShader(fs);
        return prog;
    }

    private static int shader(int type, String src) {
        int s = GLES20.glCreateShader(type);
        GLES20.glShaderSource(s, src);
        GLES20.glCompileShader(s);
        int[] ok = new int[1];
        GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, ok, 0);
        if (ok[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(s);
            GLES20.glDeleteShader(s);
            throw new IllegalStateException("Shader compile failed: " + log);
        }
        return s;
    }

    /** Uploads a bitmap with mipmaps, then recycles it. Nearest filtering gives the chunky retro look. */
    static int texture(Bitmap bitmap, boolean repeat, boolean nearest) {
        int[] id = new int[1];
        GLES20.glGenTextures(1, id, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, nearest ? GLES20.GL_NEAREST_MIPMAP_NEAREST : GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, nearest ? GLES20.GL_NEAREST : GLES20.GL_LINEAR);
        int wrap = repeat ? GLES20.GL_REPEAT : GLES20.GL_CLAMP_TO_EDGE;
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrap);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, repeat ? GLES20.GL_REPEAT : GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        bitmap.recycle();
        return id[0];
    }

    static FloatBuffer floats(float[] data) {
        FloatBuffer b = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        b.put(data).position(0);
        return b;
    }

    static ShortBuffer shorts(short[] data) {
        ShortBuffer b = ByteBuffer.allocateDirect(data.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        b.put(data).position(0);
        return b;
    }

    /** Forgets which mesh is bound; call when a new GL context starts. */
    static void resetBindings() {
        bound = -1;
    }

    /** Marks that something other than a Mesh owns the buffer bindings now. */
    static void unbindMeshes() {
        if (bound != 0) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            bound = 0;
        }
    }

    /** A grid mesh kept in GPU buffers: interleaved x, y, u, v plus triangle indices. */
    static final class Mesh {
        final int vbo;
        final int ibo;
        final int count;

        Mesh(float x0, float x1, float y0, float y1, int cols, int rows, boolean flipV) {
            float[] v = new float[(cols + 1) * (rows + 1) * 4];
            int i = 0;
            for (int r = 0; r <= rows; r++) {
                for (int c = 0; c <= cols; c++) {
                    float fx = c / (float) cols;
                    float fy = r / (float) rows;
                    v[i++] = x0 + (x1 - x0) * fx;
                    v[i++] = y0 + (y1 - y0) * fy;
                    v[i++] = fx;
                    v[i++] = flipV ? 1f - fy : fy;
                }
            }
            short[] idx = new short[cols * rows * 6];
            int k = 0;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    short a = (short) (r * (cols + 1) + c);
                    short b = (short) (a + 1);
                    short d = (short) (a + cols + 1);
                    short e = (short) (d + 1);
                    idx[k++] = a;
                    idx[k++] = b;
                    idx[k++] = d;
                    idx[k++] = b;
                    idx[k++] = e;
                    idx[k++] = d;
                }
            }
            int[] ids = new int[2];
            GLES20.glGenBuffers(2, ids, 0);
            vbo = ids[0];
            ibo = ids[1];
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, v.length * 4, floats(v), GLES20.GL_STATIC_DRAW);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, idx.length * 2, shorts(idx), GLES20.GL_STATIC_DRAW);
            count = idx.length;
            resetBindings();
        }

        /** Binds this mesh's buffers and points the position and uv attributes at them. */
        void bindAttributes() {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
            GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 16, 0);
            GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 16, 8);
            GLES20.glEnableVertexAttribArray(0);
            GLES20.glEnableVertexAttribArray(1);
            bound = vbo;
        }

        void draw() {
            if (bound != vbo) {
                bindAttributes();
            }
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, count, GLES20.GL_UNSIGNED_SHORT, 0);
        }
    }
}
