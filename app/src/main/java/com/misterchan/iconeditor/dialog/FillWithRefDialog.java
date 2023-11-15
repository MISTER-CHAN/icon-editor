package com.misterchan.iconeditor.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Shader;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;
import com.misterchan.iconeditor.util.BitmapUtils;

public class FillWithRefDialog {
    public interface OnChangeListener {
        void onChange(Bitmap bitmap, Shader.TileMode tileMode, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private Bitmap src, dst;
    private OnChangeListener listener;
    private Shader.TileMode tileMode = Shader.TileMode.REPEAT;

    @SuppressLint("NonConstantResourceId")
    private final MaterialButtonToggleGroup.OnButtonCheckedListener onTileModeButtonCheckedListener = (group, checkedId, isChecked) -> {
        if (!isChecked) {
            return;
        }
        tileMode = switch (checkedId) {
            case R.id.b_repeat -> Shader.TileMode.REPEAT;
            case R.id.b_mirror -> Shader.TileMode.MIRROR;
            default -> Shader.TileMode.REPEAT;
        };
        onChange(true);
    };

    private final OnSliderChangeListener onSizeSliderChangeListener = (slider, value, stopped) -> {
        if (dst != null) {
            dst.recycle();
        }
        final int sw = src.getWidth(), sh = src.getHeight(), dw, dh;
        if (sw <= sh) {
            dw = (int) value;
            dh = dw * sh / sw;
        } else {
            dh = (int) value;
            dw = dh * sw / sh;
        }
        dst = Bitmap.createBitmap(dw, dh, Bitmap.Config.ARGB_8888);
        new Canvas(dst).drawBitmap(src, null, new Rect(0, 0, dw, dh), BitmapUtils.PAINT_SRC);
        onChange(stopped);
    };

    public FillWithRefDialog(Context context, Bitmap src) {
        builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.fill_with_reference)
                .setView(R.layout.fill_with_ref);

        this.src = src;
    }

    private void onChange(boolean stopped) {
        listener.onChange(dst, tileMode, stopped);
    }

    public FillWithRefDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public FillWithRefDialog setOnChangeListener(OnChangeListener listener) {
        this.listener = listener;
        return this;
    }

    public FillWithRefDialog setOnDismissListener(DialogInterface.OnDismissListener listener) {
        builder.setOnDismissListener(dialog -> {
            listener.onDismiss(dialog);
            if (dst != null) {
                dst.recycle();
            }
        });
        return this;
    }

    public FillWithRefDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public FillWithRefDialog show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final MaterialButtonToggleGroup btgTileMode = dialog.findViewById(R.id.btg_tile_modes);
        final Slider sSize = dialog.findViewById(R.id.s_size);

        btgTileMode.addOnButtonCheckedListener(onTileModeButtonCheckedListener);
        sSize.setValueTo(Math.min(src.getWidth(), src.getHeight()));
        sSize.setValue(sSize.getValueTo());
        sSize.addOnChangeListener(onSizeSliderChangeListener);
        sSize.addOnSliderTouchListener(onSizeSliderChangeListener);

        onSizeSliderChangeListener.onChange(sSize, sSize.getValueTo(), true);
        return this;
    }
}
