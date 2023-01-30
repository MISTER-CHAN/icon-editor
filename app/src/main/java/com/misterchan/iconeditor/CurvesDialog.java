package com.misterchan.iconeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
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

import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import java.util.function.Function;

class CurvesDialog {

    public interface OnCurvesChangeListener {
        void onChange(int[][] curves, boolean stopped);
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
    private OnCurvesChangeListener listener;

    @Size(5)
    private Bitmap[] histBitmaps = new Bitmap[5];

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

    private Paint[] paints = new Paint[]{new Paint(), new Paint(), new Paint(), compPaint, compPaint};
    private Paint paint;

    private final Paint eraser = new Paint() {
        {
            setBlendMode(BlendMode.CLEAR);
        }
    };

    {
        initPaint(compPaint);
        initPaint(paints[0]);
        initPaint(paints[1]);
        initPaint(paints[2]);
    }

    public CurvesDialog(Context context) {
        builder = new AlertDialog.Builder(context)
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

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        final int color = typedArray.getColor(0, Color.BLACK);
        normalPaint.setColor(color);
        compPaint.setColor(color);
        final int r = sat(Color.red(color) - 0x40) << 16,
                g = sat(Color.green(color) - 0x40) << 8,
                b = sat(Color.blue(color) - 0x40),
                a = color & Color.BLACK;
        paints[0].setColor(a | sat(r + 0x40) << 16 | g | b);
        paints[1].setColor(a | r | sat(g + 0x40) << 8 | b);
        paints[2].setColor(a | r | g | sat(b + 0x40));
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
        for (int i = 0x00; i < 0xFF; ) {
            canvas.drawLine(i, 255.0f - c[i], ++i, 255.0f - c[i], paint);
        }
        iv.invalidate();
    }

    private void drawGraphics(int leftIncl, int rightIncl) {
        final int leftExcl = leftIncl - 1, rightExcl = rightIncl + 1;
        canvas.drawRect(leftExcl, 0.0f, rightExcl, 256.0f, eraser);
        if (leftExcl >= 0x0) {
            canvas.drawLine(leftExcl, 255.0f - c[leftExcl], leftIncl, 255.0f - c[leftIncl], paint);
        }
        canvas.drawLine(leftIncl, 255.0f - c[leftIncl], rightIncl, 255.0f - c[rightIncl], paint);
        if (rightExcl < 0x100) {
            canvas.drawLine(rightIncl, 255.0f - c[rightIncl], rightExcl, 255.0f - c[rightExcl], paint);
        }
        iv.invalidate();
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
                    compFuncs[selectedCompIndex], 256.0f, paints[selectedCompIndex]);
        }
        ivHistogram.setImageBitmap(histBitmaps[selectedCompIndex]);
    }

    private void initPaint(Paint paint) {
        paint.setAntiAlias(true);
        paint.setDither(true);
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
        paint = paints[index];
        drawHistogram();
        drawGraphics();
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
//      layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);
        window.setBackgroundDrawableResource(R.color.transparent);

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

        rbRgbOutputs.setChecked(true);

        return this;
    }

    private int toBitmap(float coo) {
        return (int) (coo / density);
    }

    private void update(boolean stopped) {
        listener.onChange(curves, stopped);
    }
}
