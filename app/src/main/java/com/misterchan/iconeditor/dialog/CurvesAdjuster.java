package com.misterchan.iconeditor.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.OneShotPreDrawListener;

import com.google.android.material.tabs.TabLayout;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.util.BitmapUtils;

import java.util.Arrays;

public class CurvesAdjuster extends FilterDialog {
    @Size(0x100)
    private static final int[] DEFAULT_CURVE = new int[0x100];

    static {
        reset(DEFAULT_CURVE);
    }

    public interface OnCurvesChangedListener {
        void onChanged(int[][] curves, boolean stopped);
    }

    private final Activity activity;
    private Bitmap bitmap;
    private Bitmap grid;
    private Canvas canvas;
    private float density;
    private ImageView iv;
    private ImageView ivGrid;
    private ImageView ivHistogram;
    private int prevBX, prevBY;
    private int selectedCompIndex;
    private OnCurvesChangedListener listener;
    private final Paint normalPaint = new Paint();

    @Size(5)
    private Bitmap[] histBitmaps = new Bitmap[5];

    @Size(0x400)
    private final float[] pts = new float[0x400];

    @ColorInt
    @Size(5)
    private final int[] curveColors = new int[5];

    @ColorInt
    private int[] srcPixels;

    /**
     * <table>
     *     <tr><th>Index &nbsp;</th><td>0</td><td>1</td><td>2</td><td>3</td><td>4</td></tr>
     *     <tr><th>Curve &nbsp;</th><td>R &nbsp; &nbsp;</td><td>G &nbsp; &nbsp;</td><td>B &nbsp; &nbsp;</td><td>A &nbsp; &nbsp;</td><td>RGB Outputs</td></tr>
     * </table>
     */
    private int[][] curves;

    @Size(0x100)
    private int[] c;

    private final Paint curvePaint = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
            setStrokeCap(Paint.Cap.ROUND);
            setStrokeJoin(Paint.Join.ROUND);
            setStrokeWidth(2.0f);
        }
    };

    {
        for (int i = 0x00; i <= 0xFF; ++i) {
            if (i > 0x00) pts[i * 4 - 2] = i;
            if (i < 0xFF) pts[i * 4] = i;
        }
    }

    public CurvesAdjuster(Activity activity) {
        super(activity);
        builder.setTitle(R.string.curves).setView(R.layout.curves);

        builder.setOnDismissListener(dialog -> {
            canvas = null;
            bitmap.recycle();
            bitmap = null;
            grid.recycle();
            grid = null;
            for (Bitmap bitmap : histBitmaps) {
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
            histBitmaps = null;
        });

        this.activity = activity;
        final Resources.Theme theme = activity.getTheme();
        final TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        final int color = activity.getResources().getColor(typedValue.resourceId, theme);
        final int a = Color.alpha(color) << 24;
        normalPaint.setColor(color);
        curveColors[3] = curveColors[4] = color;
        final int r = sat(Color.red(color) - 0x40) << 16,
                g = sat(Color.green(color) - 0x40) << 8,
                b = sat(Color.blue(color) - 0x40);
        final int cr = sat(r + 0x40) << 16 | g | b, cg = r | sat(g + 0x40) << 8 | b, cb = r | g | sat(b + 0x40);
        curveColors[0] = a | cr;
        curveColors[1] = a | cg;
        curveColors[2] = a | cb;
    }

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchListener = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                final int bx = sat(toBitmap(x)), by = sat(toBitmap(y));
                prevBX = bx;
                prevBY = by;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int bx = sat(toBitmap(x)), by = sat(toBitmap(y));
                if (bx == prevBX) {
                    c[bx] = 0xFF - by;
                    drawGraphics(bx, bx);
                } else {
                    final int left = Math.min(prevBX, bx), right = Math.max(prevBX, bx);
                    final int b = prevBX <= bx ? prevBY : by;
                    final float k = (float) (by - prevBY) / (float) (bx - prevBX);
                    for (int i = left; i <= right; ++i) {
                        c[i] = sat((int) (0xFF - ((i - left) * k + b)));
                    }
                    drawGraphics(left, right);
                }
                prevBX = bx;
                prevBY = by;
                update(false);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                update(true);
                break;
        }
        return true;
    };

    private void drawGraphics() {
        bitmap.eraseColor(Color.TRANSPARENT);
        canvas.drawLines(pts, curvePaint);
        iv.invalidate();
    }

    private void drawGraphics(int leftIncl, int rightIncl) {
        for (int i = leftIncl; i <= rightIncl; ++i) {
            if (i > 0x00) pts[i * 4 - 1] = 255.0f - c[i];
            if (i < 0xFF) pts[i * 4 + 1] = 255.0f - c[i];
        }
        drawGraphics();
    }

    private void drawGrid() {
        final Canvas cv = new Canvas(grid);

        cv.drawLine(0.0f, 0.0f, 256.0f, 0.0f, normalPaint);
        cv.drawLine(0.0f, 64.0f, 256.0f, 64.0f, normalPaint);
        cv.drawLine(0.0f, 128.0f, 256.0f, 128.0f, normalPaint);
        cv.drawLine(0.0f, 192.0f, 256.0f, 192.0f, normalPaint);
        cv.drawLine(0.0f, 256.0f, 256.0f, 256.0f, normalPaint);
        cv.drawLine(0.0f, 0.0f, 0.0f, 256.0f, normalPaint);
        cv.drawLine(64.0f, 0.0f, 64.0f, 256.0f, normalPaint);
        cv.drawLine(128.0f, 0.0f, 128.0f, 256.0f, normalPaint);
        cv.drawLine(192.0f, 0.0f, 192.0f, 256.0f, normalPaint);
        cv.drawLine(256.0f, 0.0f, 256.0f, 256.0f, normalPaint);

        final String format = Settings.INST.colorIntCompFormat();
        final String min = Settings.INST.colorRep() ? "Min" : String.format(format, 0x00);
        final String max = Settings.INST.colorRep() ? "Max" : String.format(format, 0xFF);
        normalPaint.setTextAlign(Paint.Align.LEFT);
        cv.drawText(min, 0.0f, grid.getHeight(), normalPaint);
        cv.drawText(max, 0.0f, -normalPaint.getFontMetrics().ascent, normalPaint);
        normalPaint.setTextAlign(Paint.Align.RIGHT);
        cv.drawText(max, grid.getWidth(), grid.getHeight(), normalPaint);

        ivGrid.invalidate();
    }

    private void drawHistogram() {
        if (histBitmaps[selectedCompIndex] == null) {
            histBitmaps[selectedCompIndex] = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
            new Thread(() -> {
                BitmapUtils.drawHistogram(srcPixels, histBitmaps[selectedCompIndex], selectedCompIndex);
                activity.runOnUiThread(() -> ivHistogram.invalidate());
            }).start();
        }
        ivHistogram.setImageBitmap(histBitmaps[selectedCompIndex]);
    }

    public static int[][] copyOf(@NonNull int[][] original) {
        final int[][] copy = new int[5][];
        for (int i = 0; i <= 4; ++i) {
            copy[i] = original[i] != DEFAULT_CURVE ? Arrays.copyOf(original[i], 0x100) : DEFAULT_CURVE;
        }
        return copy;
    }

    public static int[][] newCurves() {
        return new int[][]{DEFAULT_CURVE, DEFAULT_CURVE, DEFAULT_CURVE, DEFAULT_CURVE, DEFAULT_CURVE};
    }

    @Override
    void onFilterCommit() {
        listener.onChanged(curves, true);
    }

    private void reset() {
        reset(c);
    }

    private static void reset(@Size(0x100) int[] curve) {
        for (int i = 0x0; i < 0x100; ++i) {
            curve[i] = i;
        }
    }

    @IntRange(from = 0x00, to = 0xFF)
    private static int sat(int v) {
        return v <= 0x0 ? 0x00 : v >= 0x100 ? 0xFF : v;
    }

    private void selectComp(int index) {
        selectedCompIndex = index;
        if (curves[index] == DEFAULT_CURVE) {
            curves[index] = new int[0x100];
            reset(curves[index]);
        }
        c = curves[index];
        for (int i = 0x00; i <= 0xFF; ++i) {
            if (i > 0x00) pts[i * 4 - 1] = 255.0f - c[i];
            if (i < 0xFF) pts[i * 4 + 1] = 255.0f - c[i];
        }
        curvePaint.setColor(curveColors[index]);
        drawHistogram();
        drawGraphics();
    }

    public CurvesAdjuster setDefaultCurves(int[][] curves) {
        this.curves = curves;
        return this;
    }

    public CurvesAdjuster setOnCurvesChangedListener(OnCurvesChangedListener listener) {
        this.listener = listener;
        return this;
    }

    public CurvesAdjuster setSource(Bitmap bitmap) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        setSource(pixels);
        return this;
    }

    public CurvesAdjuster setSource(@ColorInt int[] pixels) {
        srcPixels = pixels;
        return this;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//      layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);
        window.setBackgroundDrawableResource(android.R.color.transparent);

        final FrameLayout fl = dialog.findViewById(R.id.fl);
        iv = dialog.findViewById(R.id.iv);
        ivGrid = dialog.findViewById(R.id.iv_grid);
        ivHistogram = dialog.findViewById(R.id.iv_histogram);
        final TabLayout tlComps = dialog.findViewById(R.id.tl_comps);

        iv.setOnTouchListener(onImageViewTouchListener);

        tlComps.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectComp(switch (tab.getPosition()) {
                    case 0 -> 4;
                    case 1 -> 0;
                    case 2 -> 1;
                    case 3 -> 2;
                    case 4 -> 3;
                    default -> 4;
                });
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        OneShotPreDrawListener.add(fl, () -> {
            final int width = fl.getMeasuredWidth();
            density = width / 256.0f;
            final ViewGroup.LayoutParams lp = fl.getLayoutParams();
            lp.height = width;
            fl.setLayoutParams(lp);
        });

        dialog.findViewById(R.id.b_reset).setOnClickListener(v -> {
            reset();
            for (int i = 0x00; i <= 0xFF; ++i) {
                if (i > 0x00) pts[i * 4 - 1] = 255.0f - i;
                if (i < 0xFF) pts[i * 4 + 1] = 255.0f - i;
            }
            bitmap.eraseColor(Color.TRANSPARENT);
            canvas.drawLine(0.0f, 255.0f, 255.0f, 0.0f, curvePaint);
            iv.invalidate();
            update(true);
        });

        bitmap = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);
        iv.setImageBitmap(bitmap);

        grid = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
        ivGrid.setImageBitmap(grid);
        drawGrid();

        ivHistogram.setImageAlpha(0x40);

        if (curves == null) {
            curves = newCurves();
        }

        tlComps.selectTab(null);
        tlComps.getTabAt(0).select();
    }

    private int toBitmap(float coo) {
        return (int) (coo / density);
    }

    private void update(boolean stopped) {
        listener.onChanged(curves, stopped);
    }
}
