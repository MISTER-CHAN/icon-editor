package com.misterchan.iconeditor.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.util.Printer;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.OneShotPreDrawListener;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.R;

import java.util.function.Function;

public class CurvesDialog {

    public interface OnCurvesChangedListener {
        void onChanged(int[][] curves, boolean stopped);
    }

    private Bitmap bitmap;
    private Bitmap grid;
    private final AlertDialog.Builder builder;
    private Canvas canvas;
    private float density;
    private ImageView iv;
    private ImageView ivGrid;
    private ImageView ivHistogram;
    private int prevBX, prevBY;
    private int selectedCompIndex;
    private int[] srcPixels;
    private OnCurvesChangedListener listener;

    @Size(5)
    private Bitmap[] histBitmaps = new Bitmap[5];

    @Size(0x400)
    private final float[] pts = new float[0x400];

    /**
     * <table>
     *     <tr>
     *         <th>Index</th><th>Curve</th>
     *     </tr>
     *     <tr><td>0, 1, 2</td><td>R, G, B</td></tr>
     *     <tr><td>3</td><td>A</td></tr>
     *     <tr><td>4</td><td>RGB outputs</td></tr>
     * </table>
     */
    @Size(5)
    private int[][] curves;

    @Size(0x100)
    private int[] c;

    private final Paint normalPaint = new Paint();

    private final Paint compPaint = new Paint();
    private final Paint histCompPaint = new Paint();

    private final Paint[] paints = new Paint[]{new Paint(), new Paint(), new Paint(), compPaint, compPaint};
    private final Paint[] histPaints = new Paint[]{new Paint(), new Paint(), new Paint(), histCompPaint, histCompPaint};
    private Paint paint;

    private final Paint.FontMetrics fontMetrics = normalPaint.getFontMetrics();

    {
        for (int i = 0x00; i <= 0xFF; ++i) {
            if (i > 0x00) pts[i * 4 - 2] = i;
            if (i < 0xFF) pts[i * 4] = i;
        }

        initPaint(compPaint);
        initPaint(paints[0]);
        initPaint(paints[1]);
        initPaint(paints[2]);
        initHistPaint(histCompPaint);
        initHistPaint(histPaints[0]);
        initHistPaint(histPaints[1]);
        initHistPaint(histPaints[2]);
    }

    public CurvesDialog(Context context) {
        builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.curves)
                .setView(R.layout.curves);

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

        final Resources.Theme theme = context.getTheme();
        final TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        final int color = context.getResources().getColor(typedValue.resourceId, theme);
        final int a = Color.alpha(color) << 24;
        final int aHalf = Color.alpha(color) / 2 << 24;
        normalPaint.setColor(color);
        compPaint.setColor(color);
        histCompPaint.setColor(aHalf | Color.rgb(color));
        final int r = sat(Color.red(color) - 0x40) << 16,
                g = sat(Color.green(color) - 0x40) << 8,
                b = sat(Color.blue(color) - 0x40);
        final int cr = sat(r + 0x40) << 16 | g | b, cg = r | sat(g + 0x40) << 8 | b, cb = r | g | sat(b + 0x40);
        paints[0].setColor(a | cr);
        paints[1].setColor(a | cg);
        paints[2].setColor(a | cb);
        histPaints[0].setColor(aHalf | cr);
        histPaints[1].setColor(aHalf | cg);
        histPaints[2].setColor(aHalf | cb);
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
        canvas.drawLines(pts, paint);
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

        final String format = Settings.INST.argbCompFormat();
        final String min = Settings.INST.argbColorType() ? "Min" : String.format(format, 0x00);
        final String max = Settings.INST.argbColorType() ? "Max" : String.format(format, 0xFF);
        normalPaint.setTextAlign(Paint.Align.LEFT);
        cv.drawText(min, 0.0f, grid.getHeight(), normalPaint);
        cv.drawText(max, 0.0f, -fontMetrics.ascent, normalPaint);
        normalPaint.setTextAlign(Paint.Align.RIGHT);
        cv.drawText(max, grid.getWidth(), grid.getHeight(), normalPaint);

        ivGrid.invalidate();
    }

    private static final Function<Integer, Integer> redFunc = Color::red;
    private static final Function<Integer, Integer> greenFunc = Color::green;
    private static final Function<Integer, Integer> blueFunc = Color::blue;
    private static final Function<Integer, Integer> alphaFunc = Color::alpha;
    private final Function<Integer, Integer>[] compFuncs = new Function[]{redFunc, greenFunc, blueFunc, alphaFunc, LevelsDialog.valueFunc};

    private void drawHistogram() {
        if (histBitmaps[selectedCompIndex] == null) {
            histBitmaps[selectedCompIndex] = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
            LevelsDialog.drawHistogram(srcPixels, histBitmaps[selectedCompIndex], ivHistogram,
                    compFuncs[selectedCompIndex], 256.0f, histPaints[selectedCompIndex]);
        }
        ivHistogram.setImageBitmap(histBitmaps[selectedCompIndex]);
    }

    private void initHistPaint(Paint paint) {
        paint.setAntiAlias(false);
        paint.setBlendMode(BlendMode.SRC);
    }

    private void initPaint(Paint paint) {
        paint.setAntiAlias(true);
        paint.setBlendMode(BlendMode.SRC);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(2.0f);
    }

    private void reset() {
        reset(c);
    }

    private void reset(@Size(0x100) int[] curve) {
        for (int i = 0x0; i < 0x100; ++i) {
            curve[i] = i;
        }
    }

    @IntRange(from = 0x00, to = 0xFF)
    private static int sat(int v) {
        return v <= 0x0 ? 0x0 : v >= 0x100 ? 0xFF : v;
    }

    public CurvesDialog setDefaultCurves(@Size(5) int[][] curves) {
        this.curves = curves;
        return this;
    }

    private void selectComp(int index) {
        selectedCompIndex = index;
        c = curves[index];
        for (int i = 0x00; i <= 0xFF; ++i) {
            if (i > 0x00) pts[i * 4 - 1] = 255.0f - c[i];
            if (i < 0xFF) pts[i * 4 + 1] = 255.0f - c[i];
        }
        paint = paints[index];
        drawHistogram();
        drawGraphics();
    }

    public CurvesDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public CurvesDialog setOnCurvesChangeListener(OnCurvesChangedListener listener) {
        this.listener = listener;
        return this;
    }

    public CurvesDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public CurvesDialog setSource(Bitmap bitmap) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        setSource(pixels);
        return this;
    }

    public CurvesDialog setSource(int[] pixels) {
        srcPixels = pixels;
        return this;
    }

    @SuppressLint("ClickableViewAccessibility")
    public CurvesDialog show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//      layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);
        window.setBackgroundDrawableResource(R.color.transparent);

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
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) fl.getLayoutParams();
            lp.height = width;
            fl.setLayoutParams(lp);
        });

        dialog.findViewById(R.id.b_reset).setOnClickListener(v -> {
            reset();
            bitmap.eraseColor(Color.TRANSPARENT);
            canvas.drawLine(0.0f, 256.0f, 256.0f, 0.0f, paint);
            iv.invalidate();
            update(true);
        });

        bitmap = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);
        iv.setImageBitmap(bitmap);

        grid = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
        ivGrid.setImageBitmap(grid);
        drawGrid();

        if (curves == null) {
            curves = new int[][]{new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100]};
            for (int[] curve : curves) {
                reset(curve);
            }
        }

        tlComps.selectTab(null);
        tlComps.getTabAt(0).select();

        return this;
    }

    private int toBitmap(float coo) {
        return (int) (coo / density);
    }

    private void update(boolean stopped) {
        listener.onChanged(curves, stopped);
    }
}
