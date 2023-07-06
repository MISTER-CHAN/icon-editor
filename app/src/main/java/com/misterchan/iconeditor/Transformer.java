package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

class Transformer implements FloatingLayer {

    private final Paint PAINT_SRC = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setFilterBitmap(false);
        }
    };

    public static class Mesh {
        public final int width, height;
        public final float[] verts;

        public Mesh(int width, int height) {
            this.width = width;
            this.height = height;
            verts = new float[(width + 1) * (height + 1) * 2];
        }
    }

    private Bitmap bitmap;
    private Bitmap src;
    private Canvas canvas;
    public Mesh mesh;
    public Rect rect;

    private final Paint paint = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
        }
    };

    public void apply() {
        if (isRecycled()) {
            return;
        }
        new Canvas(src).drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
    }

    public void createMesh(int width, int height) {
        mesh = new Mesh(width, height);
        final float dx = (float) rect.width() / (float) width, dy = (float) rect.height() / (float) height;
        for (int r = 0; r <= height; ++r) {
            for (int c = 0; c <= width; ++c) {
                mesh.verts[(r * (width + 1) + c) * 2] = c * dx;
                mesh.verts[(r * (width + 1) + c) * 2 + 1] = r * dy;
            }
        }
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public int getLeft() {
        return rect.left;
    }

    @Override
    public int getTop() {
        return rect.top;
    }

    public boolean isRecycled() {
        return src == null || bitmap == null;
    }

    public void recycle() {
        if (src != null) {
            src.recycle();
            src = null;
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public void reset() {
        canvas.drawBitmap(src, 0.0f, 0.0f, PAINT_SRC);
    }

    public void rotate(float degrees, boolean filter, boolean antiAlias) {
        final Matrix matrix = new Matrix();
        matrix.setRotate(degrees);
        transform(matrix, filter, antiAlias);
    }

    public void scale(float sx, float sy, boolean filter, boolean antiAlias) {
        final Matrix matrix = new Matrix();
        matrix.setScale(sx, sy);
        transform(matrix, filter, antiAlias);
    }

    public void setBitmap(Bitmap bitmap, Rect rect) {
        if (src != null) src.recycle();
        if (this.bitmap != null) this.bitmap.recycle();
        src = bitmap;
        this.bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(this.bitmap);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
        this.rect = rect;
    }

    public void stretch(int width, int height, boolean filter, boolean antiAlias) {
        final Matrix matrix = new Matrix();
        matrix.setScale((float) width / (float) src.getWidth(), (float) height / (float) src.getHeight());
        transform(matrix, filter, antiAlias);
    }

    public RectF transform(Matrix matrix, boolean filter, boolean antiAlias) {
        final RectF r = new RectF(0.0f, 0.0f, src.getWidth(), src.getHeight());
        matrix.mapRect(r);
        if (r.isEmpty()) {
            return null;
        }
        matrix.postTranslate(-r.left, -r.top);
        final Bitmap bm;
        try {
            bm = Bitmap.createBitmap((int) r.width(), (int) r.height(), Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return null;
        }
        final Canvas cv = new Canvas(bm);
        cv.setMatrix(matrix);
        paint.setAntiAlias(antiAlias);
        paint.setFilterBitmap(filter);
        cv.drawBitmap(src, 0.0f, 0.0f, paint);
        bitmap.recycle();
        bitmap = bm;
        return r;
    }

    public void transformMesh(boolean filter, boolean antiAlias) {
        bitmap.eraseColor(Color.TRANSPARENT);
        paint.setAntiAlias(antiAlias);
        paint.setFilterBitmap(filter);
        canvas.drawBitmapMesh(src, mesh.width, mesh.height, mesh.verts, 0, null, 0, paint);
    }
}
