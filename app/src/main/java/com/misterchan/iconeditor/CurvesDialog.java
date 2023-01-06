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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import java.util.function.Function;

public class CurvesDialog {

    public interface OnCurvesChangeListener {
        void onChange(int[][] curves);
    }

    private Bitmap bitmap;
    private Bitmap gridBitmap;
    private Bitmap histBitmap;
    private final AlertDialog.Builder builder;
    private Canvas canvas;
    private float density;
    private ImageView iv;
    private ImageView ivGrid;
    private ImageView ivHistogram;
    private int prevBX, prevBY;
    private int[] srcPixels;
    private OnCurvesChangeListener listener;

    /**
     * o[0], o[1], o[2] - RGB outputs<br />
     * o[3] - Alpha output<br />
     * o[4] - RGB outputs output
     */
    @Size(5)
    private int[][] curves;
    private int[] o;

    private final Paint normalPaint = new Paint();

    private final Paint redHist = new Paint();
    private final Paint greenHist = new Paint();
    private final Paint blueHist = new Paint();
    private final Paint[] hists = new Paint[]{redHist, greenHist, blueHist, normalPaint, normalPaint};
    private Paint hist;

    private final Paint compPaint = new Paint() {
        {
            setAntiAlias(true);
            setDither(true);
            setStrokeCap(Cap.ROUND);
            setStrokeJoin(Join.ROUND);
            setStrokeWidth(2.0f);
        }
    };

    private final Paint redPaint = new Paint() {
        {
            setAntiAlias(true);
            setColor(Color.RED);
            setDither(true);
            setStrokeCap(Cap.ROUND);
            setStrokeJoin(Join.ROUND);
            setStrokeWidth(2.0f);
        }
    };

    private final Paint greenPaint = new Paint() {
        {
            setAntiAlias(true);
            setColor(Color.GREEN);
            setDither(true);
            setStrokeCap(Cap.ROUND);
            setStrokeJoin(Join.ROUND);
            setStrokeWidth(2.0f);
        }
    };

    private final Paint bluePaint = new Paint() {
        {
            setAntiAlias(true);
            setColor(Color.BLUE);
            setDither(true);
            setStrokeCap(Cap.ROUND);
            setStrokeJoin(Join.ROUND);
            setStrokeWidth(2.0f);
        }
    };

    private Paint[] paints = new Paint[]{redPaint, greenPaint, bluePaint, compPaint, compPaint};
    private Paint paint;

    private final Paint eraser = new Paint() {
        {
            setBlendMode(BlendMode.CLEAR);
        }
    };

    public CurvesDialog(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.curves)
                .setView(R.layout.curves);

        builder.setOnDismissListener(dialog -> {
            bitmap.recycle();
            bitmap = null;
            canvas = null;
            histBitmap.recycle();
            histBitmap = null;
        });

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        final int color = typedArray.getColor(0, Color.BLACK);
        compPaint.setColor(color);
        normalPaint.setColor(color);
        final int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        final int r_ = sat(r - 0x20) << 16, g_ = sat(g - 0x20) << 8, b_ = sat(b - 0x20), a_ = Color.alpha(color) << 24;
        redHist.setColor(a_ | sat(r + 0x20) << 16 | g_ | b_);
        greenHist.setColor(a_ | r_ | sat(g + 0x20) << 8 | b_);
        blueHist.setColor(a_ | r_ | g_ | sat(b + 0x20));
        typedArray.recycle();
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
                    o[bx] = 0xFF - by;
                    drawGraphics(bx, bx);
                } else {
                    final int beginX = Math.min(prevBX, bx), endX = Math.max(prevBX, bx);
                    final int b = prevBX <= bx ? prevBY : by;
                    final float k = (float) (by - prevBY) / (float) (bx - prevBX);
                    for (int i = beginX; i <= endX; ++i) {
                        o[i] = sat((int) (0xFF - ((i - beginX) * k + b)));
                    }
                    drawGraphics(beginX, endX);
                }
                prevBX = bx;
                prevBY = by;
                update();
                break;
            }
        }
        return true;
    };

    private void drawGraphics() {
        bitmap.eraseColor(Color.TRANSPARENT);
        for (int i = 0x00; i < 0xFF; ) {
            canvas.drawLine(i, 255.0f - o[i], ++i, 255.0f - o[i], paint);
        }
        iv.invalidate();
    }

    private void drawGraphics(int l, int r) {
        final int begin = l - 1, end = r + 1;
        canvas.drawRect(begin, 0.0f, end, 256.0f, eraser);
        if (begin >= 0x0) {
            canvas.drawLine(begin, 255.0f - o[begin], l, 255.0f - o[l], paint);
        }
        canvas.drawLine(l, 255.0f - o[l], r, 255.0f - o[r], paint);
        if (end < 0x100) {
            canvas.drawLine(r, 255.0f - o[r], end, 255.0f - o[end], paint);
        }
        iv.invalidate();
    }

    private void drawGrid() {
        final Canvas cv = new Canvas(gridBitmap);
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
        ivGrid.invalidate();
    }

    private static final Function<Integer, Integer> redFunc = Color::red;
    private static final Function<Integer, Integer> greenFunc = Color::green;
    private static final Function<Integer, Integer> blueFunc = Color::blue;
    private static final Function<Integer, Integer> alphaFunc = Color::alpha;
    private final Function<Integer, Integer>[] compFuncs = new Function[]{redFunc, greenFunc, blueFunc, alphaFunc, LevelsDialog.valueFunc};
    private Function<Integer, Integer> compFunc;

    private void drawHistogram() {
        LevelsDialog.drawHistogram(srcPixels, histBitmap, ivHistogram,
                compFunc, 256.0f, hist);
    }

    private void selectComp(int index) {
        o = curves[index];
        paint = paints[index];
        compFunc = compFuncs[index];
        hist = hists[index];
        drawHistogram();
        drawGraphics();
    }

    private void reset() {
        for (int i = 0; i <= 4; ++i) {
            for (int j = 0x0; j < 0x100; ++j) {
                curves[i][j] = j;
            }
        }
    }

    private static int sat(int v) {
        return v < 0x0 ? 0x0 : v >= 0x100 ? 0xFF : v;
    }

    public CurvesDialog setDefaultCurves(@Size(5) int[][] curves) {
        this.curves = curves;
        return this;
    }

    public CurvesDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public CurvesDialog setOnCurvesChangeListener(OnCurvesChangeListener listener) {
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
        layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);

        final FrameLayout fl = dialog.findViewById(R.id.fl);
        iv = dialog.findViewById(R.id.iv);
        ivGrid = dialog.findViewById(R.id.iv_grid);
        ivHistogram = dialog.findViewById(R.id.iv_histogram);
        final RadioButton rbRgbOutputs = dialog.findViewById(R.id.rb_rgb_outputs);

        iv.setOnTouchListener(onImageViewTouchListener);
        ((CompoundButton) dialog.findViewById(R.id.rb_red)).setOnCheckedChangeListener((OnCheckedListener) () -> selectComp(0));
        ((CompoundButton) dialog.findViewById(R.id.rb_green)).setOnCheckedChangeListener((OnCheckedListener) () -> selectComp(1));
        ((CompoundButton) dialog.findViewById(R.id.rb_blue)).setOnCheckedChangeListener((OnCheckedListener) () -> selectComp(2));
        ((CompoundButton) dialog.findViewById(R.id.rb_alpha)).setOnCheckedChangeListener((OnCheckedListener) () -> selectComp(3));
        rbRgbOutputs.setOnCheckedChangeListener((OnCheckedListener) () -> selectComp(4));

        {
            final ViewTreeObserver vto = fl.getViewTreeObserver();
            final ViewTreeObserver.OnPreDrawListener l = new ViewTreeObserver.OnPreDrawListener() {

                private boolean preDrawn = false;

                @Override
                public boolean onPreDraw() {
                    if (preDrawn) {
                        return true;
                    }
                    preDrawn = true;
                    final int width = fl.getMeasuredWidth();
                    density = width / 256.0f;
                    final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) fl.getLayoutParams();
                    lp.height = width;
                    fl.setLayoutParams(lp);
                    return true;
                }
            };
            vto.addOnPreDrawListener(l);
        }

        dialog.findViewById(R.id.tv_reset).setOnClickListener(v -> {
            reset();
            bitmap.eraseColor(Color.TRANSPARENT);
            canvas.drawLine(0.0f, 256.0f, 256.0f, 0.0f, paint);
            iv.invalidate();
            update();
        });

        bitmap = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);
        iv.setImageBitmap(bitmap);

        gridBitmap = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
        ivGrid.setImageBitmap(gridBitmap);
        drawGrid();

        histBitmap = Bitmap.createBitmap(0x100, 0x100, Bitmap.Config.ARGB_4444);
        ivHistogram.setImageBitmap(histBitmap);

        if (curves == null) {
            curves = new int[][]{new int[0x100], new int[0x100], new int[0x100], new int[0x100], new int[0x100]};
            reset();
        }

        rbRgbOutputs.setChecked(true);

        return this;
    }

    private int toBitmap(float coo) {
        return (int) (coo / density);
    }

    private void update() {
        listener.onChange(curves);
    }
}
