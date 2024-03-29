package com.misterchan.iconeditor.tool;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.misterchan.iconeditor.FloatingLayer;
import com.misterchan.iconeditor.History;
import com.misterchan.iconeditor.ui.CoordinateConversions;
import com.misterchan.iconeditor.util.BitmapUtils;

public class Transformer implements FloatingLayer {

    public static class Mesh {
        public final int width, height;
        public final float[] verts;

        public Mesh(int width, int height) {
            this.width = width;
            this.height = height;
            verts = new float[(width + 1) * (height + 1) << 1];
        }
    }

    public History.Action undoAction;
    private Bitmap bitmap, srcBm;
    private Canvas canvas;
    public Mesh mesh;
    public Rect rect;
    private Rect origRect;

    private final Paint paint = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
        }
    };

    public void apply() {
        if (isRecycled()) {
            return;
        }
        srcBm.recycle();
        srcBm = BitmapUtils.createBitmap(bitmap);
    }

    public void createMesh(int width, int height) {
        mesh = new Mesh(width, height);
        final float dx = (float) rect.width() / (float) width, dy = (float) rect.height() / (float) height;
        for (int r = 0; r <= height; ++r) {
            for (int c = 0; c <= width; ++c) {
                mesh.verts[r * (width + 1) + c << 1] = c * dx;
                mesh.verts[(r * (width + 1) + c << 1) + 1] = r * dy;
            }
        }
    }

    public void drawMesh(CoordinateConversions cc, Canvas canvas, Paint paint) {
        if (mesh != null) {
            for (int i = 0, r = 0; r <= mesh.height; ++r) {
                for (int c = 0; c <= mesh.width; ++c, i += 2) {
                    final float x = cc.toViewX(rect.left + mesh.verts[i]), y = cc.toViewY(rect.top + mesh.verts[i + 1]);
                    if (c < mesh.width) {
                        canvas.drawLine(x, y,
                                cc.toViewX(rect.left + mesh.verts[r * (mesh.width + 1) + (c + 1) << 1]),
                                cc.toViewY(rect.top + mesh.verts[r * (mesh.width + 1) + (c + 1) << 1 | 1]),
                                paint);
                    }
                    if (r < mesh.height) {
                        canvas.drawLine(x, y,
                                cc.toViewX(rect.left + mesh.verts[(r + 1) * (mesh.width + 1) + c << 1]),
                                cc.toViewY(rect.top + mesh.verts[(r + 1) * (mesh.width + 1) + c << 1 | 1]),
                                paint);
                    }
                }
            }
        }
    }

    @Override
    public boolean hasRect() {
        return true;
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    public Rect getOrigRect() {
        return origRect;
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
        return srcBm == null || bitmap == null;
    }

    public void recycle() {
        if (undoAction != null) {
            undoAction.recycle();
            undoAction = null;
        }
        if (srcBm != null) {
            srcBm.recycle();
            srcBm = null;
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    public void reset() {
        if (srcBm.getWidth() != bitmap.getWidth() || srcBm.getHeight() != bitmap.getHeight()) {
            bitmap.recycle();
            bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }
        canvas.drawBitmap(srcBm, 0.0f, 0.0f, BitmapUtils.PAINT_SRC);
    }

    public void resetMesh() {
        createMesh(mesh.width, mesh.height);
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
        if (undoAction != null) undoAction.recycle();
        if (srcBm != null) srcBm.recycle();
        if (this.bitmap != null) this.bitmap.recycle();
        srcBm = bitmap;
        this.bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(this.bitmap);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, BitmapUtils.PAINT_SRC);
        origRect = new Rect(rect);
        this.rect = rect;
    }

    public void stretch(boolean filter, boolean antiAlias) {
        final Matrix matrix = new Matrix();
        matrix.setScale((float) rect.width() / (float) bitmap.getWidth(), (float) rect.height() / (float) bitmap.getHeight());
        transform(matrix, filter, antiAlias);
    }

    public RectF transform(Matrix matrix, boolean filter, boolean antiAlias) {
        final RectF r = new RectF(0.0f, 0.0f, bitmap.getWidth(), bitmap.getHeight());
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
        canvas = new Canvas(bm);
        return r;
    }

    public void transformMesh(boolean filter, boolean antiAlias) {
        bitmap.eraseColor(Color.TRANSPARENT);
        paint.setAntiAlias(antiAlias);
        paint.setFilterBitmap(filter);
        canvas.drawBitmapMesh(srcBm, mesh.width, mesh.height, mesh.verts, 0, null, 0, paint);
    }
}
