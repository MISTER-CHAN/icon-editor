package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;

public class GuideEditor {

    public interface OnNewGuideChangeListener {
        void onAdd(Point guide);
    }

    private final AlertDialog.Builder builder;
    private final int width, height;
    private final OnNewGuideChangeListener onNewGuideChangeListener;
    private final Point guide;
    private RadioButton rbHorizontal, rbVertical;
    private TextInputEditText tietPosition;

    private AfterTextChangedListener onPositionTextChangedListener;

    public GuideEditor(Context context, Point guide, int width, int height,
                       OnNewGuideChangeListener onNewGuideChangeListener, DialogInterface.OnCancelListener onCancelListener) {
        builder = new AlertDialog.Builder(context)
                .setOnCancelListener(onCancelListener)
                .setNegativeButton(R.string.cancel, (dialog, which) -> onCancelListener.onCancel(dialog))
                .setPositiveButton(R.string.ok, null)
                .setTitle(R.string.new_)
                .setView(R.layout.new_guide);

        this.guide = guide;
        this.onNewGuideChangeListener = onNewGuideChangeListener;
        this.width = width;
        this.height = height;
    }

    /**
     * For orientation equality, let's not use 'else' or '? :'
     */
    private Integer onGuideChange(RadioButton orientation, Integer position) {
        if (position == null) {
            try {
                position = Integer.parseUnsignedInt(tietPosition.getText().toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (orientation == null) {
            if (rbHorizontal.isChecked()) {
                orientation = rbHorizontal;
            } else if (rbVertical.isChecked()) {
                orientation = rbVertical;
            }
        }
        if (orientation == rbHorizontal) {
            guide.y = position;
            guide.x = 0;
        } else if (orientation == rbVertical) {
            guide.x = position;
            guide.y = 0;
        }
        onNewGuideChangeListener.onAdd(guide);
        return position;
    }

    public void setPositionTextSilently(int progress) {
        tietPosition.removeTextChangedListener(onPositionTextChangedListener);
        tietPosition.setText(String.valueOf(progress));
        tietPosition.addTextChangedListener(onPositionTextChangedListener);
    }

    public void show() {
        AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        rbHorizontal = dialog.findViewById(R.id.rb_horizontal);
        rbVertical = dialog.findViewById(R.id.rb_vertical);
        final SeekBar sbPosition = dialog.findViewById(R.id.sb_position);
        tietPosition = dialog.findViewById(R.id.tiet_position);
        final OnSeekBarProgressChangeListener onPositionSBProgressChange = (seekBar, progress) -> {
            setPositionTextSilently(progress);
            onGuideChange(null, progress);
        };

        onPositionTextChangedListener = s -> {
            final Integer position = onGuideChange(null, null);
            if (position != null) {
                sbPosition.setProgress(position);
            }
        };

        sbPosition.setMax(height);
        sbPosition.setOnSeekBarChangeListener(onPositionSBProgressChange);
        tietPosition.addTextChangedListener(onPositionTextChangedListener);

        rbHorizontal.setOnCheckedChangeListener((OnCheckedListener) () -> {
            sbPosition.setMax(height);
            final int position = sbPosition.getProgress();
            setPositionTextSilently(position);
            onGuideChange(rbHorizontal, position);
        });

        rbVertical.setOnCheckedChangeListener((OnCheckedListener) () -> {
            sbPosition.setMax(width);
            final int position = sbPosition.getProgress();
            setPositionTextSilently(position);
            onGuideChange(rbVertical, position);
        });
    }
}
