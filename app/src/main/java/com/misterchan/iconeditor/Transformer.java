package com.misterchan.iconeditor;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

class Transformer implements FloatingLayer {

    private Bitmap bitmap;
    private final Rect rect;

    private final Paint paint = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
        }
    };

    public Transformer(Bitmap bitmap, Rect rect) {
        this.bitmap = bitmap;
        this.rect = rect;
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

    public void recycle() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
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

    public void stretch(int width, int height, boolean filter, boolean antiAlias) {
        final Matrix matrix = new Matrix();
        matrix.setScale((float) width / (float) bitmap.getWidth(), (float) height / (float) bitmap.getHeight());
        transform(matrix, filter, antiAlias);
    }

    public RectF transform(Matrix matrix, boolean filter, boolean antiAlias) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final RectF r = new RectF(0.0f, 0.0f, w, h);
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
        cv.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        bitmap.recycle();
        bitmap = bm;
        return r;
    }

    public void transformMesh(float[] verts, boolean filter, boolean antiAlias) {
        final Bitmap bm = Bitmap.createBitmap(bitmap);
        bitmap.eraseColor(Color.TRANSPARENT);
        final Canvas canvas = new Canvas(bitmap);
        final int count = (verts.length - 8) / 10;
        paint.setAntiAlias(antiAlias);
        paint.setFilterBitmap(filter);
        canvas.drawBitmapMesh(bm, count + 1, count + 1, verts, 0, null, 0, paint);
        bm.recycle();
    }
}
