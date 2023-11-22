package com.misterchan.iconeditor.colorpicker;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.core.view.OneShotPreDrawListener;

import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.databinding.ColorPickerHsvBinding;
import com.misterchan.iconeditor.util.BitmapUtils;
import com.misterchan.iconeditor.util.ColorUtils;

public class HsvColorPicker extends ColorPicker {
    private static final float THUMB_RADIUS = 20.0f;

    private static final Paint PAINT_THUMB = new Paint() {
        {
            setBlendMode(BlendMode.DIFFERENCE);
            setColor(Color.WHITE);
            setStrokeWidth(2.0f);
            setStyle(Style.STROKE);
            setFilterBitmap(false);
        }
    };

    public interface OnHChangeListener {
        void onChange(float h);
    }

    public interface OnSVChangeListener {
        void onChange(float s, float v);
    }

    private Bitmap hueBitmap, satValBitmap;
    private Canvas hueCanvas, satValCanvas;
    private final ColorPickerHsvBinding binding;
    private int hueImageW, hueImageH, satValImageW, satValImageH;
    private final Paint huePaint = new Paint(BitmapUtils.PAINT_SRC);
    private final Paint satValPaint = new Paint(BitmapUtils.PAINT_SRC);
    private Shader valShader;

    @Size(3)
    private final float[] hsv = new float[3], hue = {0.0f, 1.0f, 1.0f};

    @ColorInt
    private int colorInt;

    @SuppressLint("ClickableViewAccessibility")
    public HsvColorPicker(long color, ColorPickerHsvBinding binding, OnHChangeListener hl, OnSVChangeListener svl) {
        prop = new Properties("H", "S", "V", true);

        this.binding = binding;
        OneShotPreDrawListener.add(binding.ivHue, () -> {
            hueImageW = binding.ivHue.getWidth();
            hueImageH = binding.ivHue.getHeight();
            hueBitmap = Bitmap.createBitmap(hueImageW, hueImageH, Bitmap.Config.ARGB_4444);
            hueCanvas = new Canvas(hueBitmap);
            binding.ivHue.setImageBitmap(hueBitmap);
            final Shader hueShader = new LinearGradient(0.0f, 0.0f, hueImageW, 0.0f,
                    new int[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED},
                    null, Shader.TileMode.CLAMP);
            huePaint.setShader(hueShader);
            drawHue();
        });
        OneShotPreDrawListener.add(binding.ivSatVal, () -> {
            satValImageW = binding.ivSatVal.getWidth();
            satValImageH = binding.ivSatVal.getHeight();
            satValBitmap = Bitmap.createBitmap(satValImageW, satValImageH, Bitmap.Config.ARGB_4444);
            satValCanvas = new Canvas(satValBitmap);
            binding.ivSatVal.setImageBitmap(satValBitmap);
            valShader = new LinearGradient(0.0f, 0.0f, 0.0f, satValImageH, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
            drawSatVal();
        });
        binding.ivHue.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN) {
                hl.onChange(Math.min(Math.max(event.getX(), 0.0f), hueImageW) / hueImageW * 360.0f);
            }
            return true;
        });
        binding.ivSatVal.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN) {
                svl.onChange(Math.min(Math.max(event.getX(), 0.0f), satValImageW) / satValImageW * 100.0f,
                        (1.0f - Math.min(Math.max(event.getY(), 0.0f), satValImageH) / satValImageH) * 100.0f);
            }
            return true;
        });

        setColorFromRgb(color);
    }

    @Override
    public long color() {
        return Color.pack(colorInt);
    }

    @Override
    public int colorInt() {
        return colorInt;
    }

    @Override
    public void dismiss() {
        hueBitmap.recycle();
        satValBitmap.recycle();
    }

    private void drawHue() {
        if (hueCanvas == null) {
            return;
        }
        hueCanvas.drawRect(0.0f, 0.0f, hueImageW, hueImageH, huePaint);
        hueCanvas.drawCircle(hsv[0] / 360.0f * hueImageW, hueImageH / 2.0f, THUMB_RADIUS, PAINT_THUMB);
        binding.ivHue.invalidate();
    }

    private void drawSatVal() {
        if (satValCanvas == null) {
            return;
        }
        hue[0] = hsv[0];
        final int color = Color.BLACK | ColorUtils.HSVToColor(hue);
        final Shader satShader = new LinearGradient(0.0f, 0.0f, binding.ivSatVal.getWidth(), 0.0f,
                Color.WHITE, color, Shader.TileMode.CLAMP);
        final Shader satValShader = new ComposeShader(valShader, satShader, BlendMode.MULTIPLY);
        satValPaint.setShader(satValShader);
        satValCanvas.drawRect(0.0f, 0.0f, satValImageW, satValImageH, satValPaint);
        satValCanvas.drawCircle(hsv[1] * satValImageW, (1.0f - hsv[2]) * satValImageH, THUMB_RADIUS, PAINT_THUMB);
        binding.ivSatVal.invalidate();
    }

    @Override
    public float getComponent(int index) {
        return index == 0 ? hsv[0] : hsv[index] * 100.0f;
    }

    @Override
    public void setAlpha(float a) {
        colorInt = ColorUtils.setAlpha(colorInt, (int) (Settings.INST.colorRep() ? a * 0xFF : a));
    }

    @Override
    public void setComponent(@IntRange(from = 0, to = 2) int index, float c) {
        if (index == 1 || index == 2) c /= 100.0f;
        if (hsv[index] == c) return;
        hsv[index] = c;
        colorInt = colorInt & Color.BLACK | ColorUtils.HSVToColor(hsv);
        if (index == 0) drawHue();
        drawSatVal();
    }

    private void setColorFromRgb(long color) {
        colorInt = Color.toArgb(color);
        ColorUtils.colorToHSV(colorInt, hsv);
    }
}
