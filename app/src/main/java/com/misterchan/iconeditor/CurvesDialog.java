package com.misterchan.iconeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

public class CurvesDialog {

    public interface OnCurvesChangeListener {
        void onChange(int[] f);
    }

    private Bitmap bitmap;
    private Bitmap levelsBitmap;
    private final AlertDialog.Builder builder;
    private Canvas canvas;
    private float density;
    private float startX, startY, stopX, stopY;
    private ImageView iv;
    private ImageView ivLevels;
    private int prevBX, prevBY;
    private final int[] f = new int[0x100];
    private OnCurvesChangeListener listener;

    private final Paint grid = new Paint();

    private final Paint paint = new Paint() {
        {
            setAntiAlias(true);
            setDither(true);
            setStrokeCap(Cap.ROUND);
            setStrokeJoin(Join.ROUND);
            setStrokeWidth(2.0f);
        }
    };

    private final Paint eraser = new Paint() {
        {
            setBlendMode(BlendMode.CLEAR);
        }
    };

    {
        reset();
    }

    public CurvesDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.curves)
                .setView(R.layout.curves);

        builder.setOnDismissListener(dialog -> {
            bitmap.recycle();
            bitmap = null;
            canvas = null;
            levelsBitmap.recycle();
            levelsBitmap = null;
        });

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        final int color = typedArray.getColor(0, Color.BLACK);
        paint.setColor(color);
        grid.setColor(color);
        typedArray.recycle();
    }

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchListener = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                final int bx = sat(toBitmap(x)), by = sat(toBitmap(y));
                startX = -1.0f;
                startY = -1.0f;
                stopX = -1.0f;
                stopY = -1.0f;
                prevBX = bx;
                prevBY = by;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int bx = sat(toBitmap(x)), by = sat(toBitmap(y));
                if (bx == prevBX) {
                    canvas.drawRect(bx, 0.0f, bx + 1.0f, 256.0f, eraser);
                    canvas.drawPoint(bx, by, paint);
                    f[bx] = 0xFF - by;
                } else {
                    final int left = Math.min(prevBX, bx), right = Math.max(prevBX, bx) + 1,
                            bottom = Math.max(prevBY, by);
                    canvas.drawRect(left, 0.0f, right, 256.0f, eraser);
                    canvas.drawLine(startX, startY, stopX, stopY, paint);
                    final boolean xAscent = prevBX <= bx, yAscent = prevBY <= by;
                    startX = xAscent ? prevBX : prevBX + 1.0f;
                    startY = yAscent ? prevBY : prevBY + 1.0f;
                    stopX = xAscent ? bx + 1.0f : bx;
                    stopY = yAscent ? by + 1.0f : by;
                    canvas.drawLine(startX, startY, stopX, stopY, paint);
                    final float k = (float) (by - prevBY) / (float) (bx - prevBX);
                    for (int i = left; i < right; ++i) {
                        f[i] = sat((int) (0xFF - ((i - left) * k + bottom)));
                    }
                }
                prevBX = bx;
                prevBY = by;
                iv.invalidate();
                listener.onChange(f);
                break;
            }
        }
        return true;
    };

    private void drawGrid() {
        final Canvas cv = new Canvas(levelsBitmap);
        cv.drawLine(0.0f, 0.0f, 256.0f, 0.0f, grid);
        cv.drawLine(0.0f, 64.0f, 256.0f, 64.0f, grid);
        cv.drawLine(0.0f, 128.0f, 256.0f, 128.0f, grid);
        cv.drawLine(0.0f, 192.0f, 256.0f, 192.0f, grid);
        cv.drawLine(0.0f, 256.0f, 256.0f, 256.0f, grid);
        cv.drawLine(0.0f, 0.0f, 0.0f, 256.0f, grid);
        cv.drawLine(64.0f, 0.0f, 64.0f, 256.0f, grid);
        cv.drawLine(128.0f, 0.0f, 128.0f, 256.0f, grid);
        cv.drawLine(192.0f, 0.0f, 192.0f, 256.0f, grid);
        cv.drawLine(256.0f, 0.0f, 256.0f, 256.0f, grid);
        ivLevels.invalidate();
    }

    private void reset() {
        for (int i = 0x0; i < 0x100; ++i) {
            f[i] = i;
        }
    }

    private static int sat(int coo) {
        return coo < 0x0 ? 0x0 : coo >= 0x100 ? 0xFF : coo;
    }

    public CurvesDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public CurvesDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public CurvesDialog setOnLevelsChangeListener(OnCurvesChangeListener listener) {
        this.listener = listener;
        return this;
    }

    @SuppressLint("ClickableViewAccessibility")
    public CurvesDialog show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);

        final FrameLayout fl = dialog.findViewById(R.id.fl);
        iv = dialog.findViewById(R.id.iv);
        ivLevels = dialog.findViewById(R.id.iv_levels);

        iv.setOnTouchListener(onImageViewTouchListener);

        fl.getViewTreeObserver().addOnPreDrawListener(() -> {
            final int width = fl.getMeasuredWidth();
            density = width / 256.0f;
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) fl.getLayoutParams();
            lp.height = width;
            fl.setLayoutParams(lp);
            return true;
        });

        dialog.findViewById(R.id.tv_reset).setOnClickListener(v -> {
            reset();
            bitmap.eraseColor(Color.TRANSPARENT);
            canvas.drawLine(0.0f, 256.0f, 256.0f, 0.0f, paint);
            iv.invalidate();
            listener.onChange(f);
        });

        bitmap = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);
        canvas.drawLine(0.0f, 256.0f, 256.0f, 0.0f, paint);
        iv.setImageBitmap(bitmap);

        levelsBitmap = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
        ivLevels.setImageBitmap(levelsBitmap);
        drawGrid();

        return this;
    }

    private int toBitmap(float coo) {
        return (int) (coo / density);
    }

    public void updateLevelGraphics(Bitmap src) {
        LevelsDialog.updateLevelGraphics(src, levelsBitmap, 256.0f, grid);
        ivLevels.invalidate();
    }
}
