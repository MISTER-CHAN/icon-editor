package com.misterchan.iconeditor.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;
import androidx.annotation.Size;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.OneShotPreDrawListener;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnSliderValueChangeListener;
import com.misterchan.iconeditor.util.BitmapUtils;

class HsvColorPicker extends ColorPicker {
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

    private final boolean hasAlpha;
    private final boolean type;
    private Canvas hueCanvas, satValCanvas;
    private ImageView ivHue, ivSatVal;
    private final int alphaRadix;
    private int hueImageW, hueImageH, satValImageW, satValImageH;
    private final Paint huePaint = new Paint(BitmapUtils.PAINT_SRC);
    private final Paint satValPaint = new Paint(BitmapUtils.PAINT_SRC);
    private Shader valShader;
    private Slider sAlpha;
    private Slider sHue, sSaturation, sValue;
    private final String alphaFormat;
    private TextInputEditText tietAlpha;
    private TextInputEditText tietHue, tietSaturation, tietValue;

    @Size(3)
    private final float[] hsv = new float[3], hue = {0.0f, 1.0f, 1.0f};

    private HsvColorPicker(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        this(context, R.string.convert_hsv_to_rgb, onColorPickListener, oldColor, 0);
    }

    HsvColorPicker(Context context, @StringRes int titleId,
                   final OnColorPickListener onColorPickListener,
                   @ColorLong final Long oldColor, @StringRes int neutralFunction) {
        dialogBuilder = new MaterialAlertDialogBuilder(context)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog, which) -> onColorPickListener.onPick(oldColor, newColor))
                .setTitle(titleId)
                .setView(R.layout.color_picker_hsv);

        deployNeutralFunction(onColorPickListener, oldColor, neutralFunction);
        hasAlpha = Settings.INST.pickInHsv();
        type = Settings.INST.colorRep();
        alphaFormat = type ? null : Settings.INST.colorIntCompFormat();
        alphaRadix = type ? 10 : Settings.INST.colorIntCompRadix();
    }

    private void drawHue() {
        if (hueCanvas == null) {
            return;
        }
        hueCanvas.drawRect(0.0f, 0.0f, hueImageW, hueImageH, huePaint);
        hueCanvas.drawCircle(hsv[0] / 360.0f * hueImageW, hueImageH / 2.0f, THUMB_RADIUS, PAINT_THUMB);
        ivHue.invalidate();
    }

    private void drawSatVal() {
        if (satValCanvas == null) {
            return;
        }
        hue[0] = hsv[0];
        final int color = Color.BLACK | Color.HSVToColor(hue);
        final Shader satShader = new LinearGradient(0.0f, 0.0f, ivSatVal.getWidth(), 0.0f,
                Color.WHITE, color, Shader.TileMode.CLAMP);
        final Shader satValShader = new ComposeShader(valShader, satShader, BlendMode.MULTIPLY);
        satValPaint.setShader(satValShader);
        satValCanvas.drawRect(0.0f, 0.0f, satValImageW, satValImageH, satValPaint);
        satValCanvas.drawCircle(hsv[1] * satValImageW, (1.0f - hsv[2]) * satValImageH, THUMB_RADIUS, PAINT_THUMB);
        ivSatVal.invalidate();
    }

    static ColorPicker make(Context context, final OnColorPickListener onColorPickListener, @ColorLong final Long oldColor) {
        return new HsvColorPicker(context, onColorPickListener, oldColor);
    }

    private void onAlphaChanged(String s) {
        final float f;
        try {
            f = type ? Float.parseFloat(s) : Integer.parseUnsignedInt(s, alphaRadix);
        } catch (NumberFormatException e) {
            return;
        }
        if (!(0.0f <= f && f <= (type ? 1.0f : 255.0f))) return;
        sAlpha.setValue(f);
        onComponentChanged();
    }

    private void onComponentChanged() {
        @ColorInt final int rgb = Color.HSVToColor(hsv);
        final int color = hasAlpha
                ? Color.argb((int) (type ? sAlpha.getValue() * 0xFF : sAlpha.getValue()), rgb)
                : Color.BLACK | rgb;
        newColor = Color.pack(color);
        vPreview.setBackgroundColor(color);
    }

    private void onHueChanged(String s) {
        final float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        sHue.setValue(f == 360.0f ? 360.0f : f % 360.0f);
        hsv[0] = f % 360.0f;
        onComponentChanged();
        drawHue();
        drawSatVal();
    }

    private void onSatOrValChanged(@IntRange(from = 1, to = 2) int index, String s, Slider slider) {
        final float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        if (!(0.0f <= f && f <= 100.0f)) return;
        slider.setValue(f);
        hsv[index] = f / 100.0f;
        onComponentChanged();
        drawSatVal();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void show() {
        final AlertDialog dialog = dialogBuilder.show();

        final GridLayout gl = dialog.findViewById(R.id.gl);

        ivHue = dialog.findViewById(R.id.iv_hue);
        ivSatVal = dialog.findViewById(R.id.iv_sat_val);
        sHue = dialog.findViewById(R.id.s_comp_0);
        sSaturation = dialog.findViewById(R.id.s_comp_1);
        sValue = dialog.findViewById(R.id.s_comp_2);
        final TextInputLayout tilHue = dialog.findViewById(R.id.til_comp_0);
        final TextInputLayout tilSaturation = dialog.findViewById(R.id.til_comp_1);
        final TextInputLayout tilValue = dialog.findViewById(R.id.til_comp_2);
        tietHue = (TextInputEditText) tilHue.getEditText();
        tietSaturation = (TextInputEditText) tilSaturation.getEditText();
        tietValue = (TextInputEditText) tilValue.getEditText();
        vPreview = dialog.findViewById(R.id.v_color);
        if (hasAlpha) {
            sAlpha = dialog.findViewById(R.id.s_alpha);
            tietAlpha = dialog.findViewById(R.id.tiet_alpha);
        }

        if (!hasAlpha) {
            hideAlphaComp(gl);
        }

        OneShotPreDrawListener.add(ivHue, () -> {
            hueImageW = ivHue.getWidth();
            hueImageH = ivHue.getHeight();
            final Bitmap bitmap = Bitmap.createBitmap(hueImageW, hueImageH, Bitmap.Config.ARGB_4444);
            hueCanvas = new Canvas(bitmap);
            ivHue.setImageBitmap(bitmap);
            final Shader hueShader = new LinearGradient(0.0f, 0.0f, hueImageW, 0.0f,
                    new int[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED},
                    null, Shader.TileMode.CLAMP);
            huePaint.setShader(hueShader);
            drawHue();
        });
        OneShotPreDrawListener.add(ivSatVal, () -> {
            satValImageW = ivSatVal.getWidth();
            satValImageH = ivSatVal.getHeight();
            final Bitmap bitmap = Bitmap.createBitmap(satValImageW, satValImageH, Bitmap.Config.ARGB_4444);
            satValCanvas = new Canvas(bitmap);
            ivSatVal.setImageBitmap(bitmap);
            valShader = new LinearGradient(0.0f, 0.0f, 0.0f, satValImageH, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
            drawSatVal();
        });

        sHue.setValueTo(360.0f);
        sSaturation.setValueTo(100.0f);
        sValue.setValueTo(100.0f);
        tietHue.setInputType(EDITOR_TYPE_NUM_DEC);
        tietSaturation.setInputType(EDITOR_TYPE_NUM_DEC);
        tietValue.setInputType(EDITOR_TYPE_NUM_DEC);
        tilHue.setHint(R.string.h);
        tilHue.setSuffixText("°");
        tilSaturation.setHint(R.string.s);
        tilSaturation.setSuffixText("%");
        tilValue.setHint(R.string.v);
        tilValue.setSuffixText("%");
        sHue.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietHue.setText(String.valueOf(value)));
        sSaturation.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietSaturation.setText(String.valueOf(value)));
        sValue.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietValue.setText(String.valueOf(value)));
        tietHue.addTextChangedListener((AfterTextChangedListener) this::onHueChanged);
        tietSaturation.addTextChangedListener((AfterTextChangedListener) s -> onSatOrValChanged(1, s, sSaturation));
        tietValue.addTextChangedListener((AfterTextChangedListener) s -> onSatOrValChanged(2, s, sValue));
        if (hasAlpha) {
            sAlpha.setValueTo(type ? 1.0f : 0xFF);
            if (type) {
                tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM_DEC);
            } else {
                if (alphaRadix <= 10) {
                    tietAlpha.setInputType(ColorPicker.EDITOR_TYPE_NUM);
                } else if (alphaRadix == 16) {
                    tietAlpha.setKeyListener(ColorPicker.KEY_LISTENER_HEX);
                }
            }
            sAlpha.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> tietAlpha.setText(type ? String.valueOf(value) : String.format(alphaFormat, (int) value)));
            tietAlpha.addTextChangedListener((AfterTextChangedListener) this::onAlphaChanged);
        }

        ivHue.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    tietHue.setText(String.valueOf(Math.min(Math.max(x, 0.0f), hueImageW) / hueImageW * 360.0f));
                }
            }
            return true;
        });
        ivSatVal.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    tietSaturation.setText(String.valueOf(Math.min(Math.max(x, 0.0f), satValImageW) / satValImageW * 100.0f));
                    tietValue.setText(String.valueOf((1.0f - Math.min(Math.max(y, 0.0f), satValImageH) / satValImageH) * 100.0f));
                }
            }
            return true;
        });

        @ColorInt final int oldColorInt = Color.toArgb(oldColor);
        Color.colorToHSV(oldColorInt, hsv);
        tietHue.setText(String.valueOf(hsv[0]));
        tietSaturation.setText(String.valueOf(hsv[1] * 100.0f));
        tietValue.setText(String.valueOf(hsv[2] * 100.0f));
        if (hasAlpha) {
            tietAlpha.setText(type ? String.valueOf(Color.alpha(oldColor)) : String.format(alphaFormat, Color.alpha(oldColorInt)));
        }
    }
}
