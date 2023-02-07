package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;

public class GuideEditor {

    public interface OnNewGuideChangeListener {
        void onChange(Guide guide);
    }

    private final AlertDialog.Builder builder;
    private final Guide guide;
    private final int width, height;
    private final OnNewGuideChangeListener onNewGuideChangeListener;
    private RadioButton rbHorizontal, rbVertical;
    private TextInputEditText tietPosition;

    private AfterTextChangedListener onPositionTextChangedListener;

    public GuideEditor(Context context, Guide guide, int width, int height,
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

    private void setPositionTextSilently(int progress) {
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
            guide.position = progress;
            onNewGuideChangeListener.onChange(guide);
            setPositionTextSilently(progress);
        };

        onPositionTextChangedListener = s -> {
            try {
                final int position = Integer.parseUnsignedInt(tietPosition.getText().toString());
                guide.position = position;
                sbPosition.setProgress(position);
            } catch (NumberFormatException e) {
            }
            onNewGuideChangeListener.onChange(guide);
        };

        sbPosition.setMax(height);
        sbPosition.setOnSeekBarChangeListener(onPositionSBProgressChange);
        tietPosition.addTextChangedListener(onPositionTextChangedListener);

        rbHorizontal.setOnCheckedChangeListener((OnCheckedListener) () -> {
            guide.orientation = Guide.ORIENTATION_HORIZONTAL;
            if (guide.position > height) {
                guide.position = height;
                sbPosition.setMax(height);
                setPositionTextSilently(height);
            }
            onNewGuideChangeListener.onChange(guide);
        });

        rbVertical.setOnCheckedChangeListener((OnCheckedListener) () -> {
            guide.orientation = Guide.ORIENTATION_VERTICAL;
            if (guide.position > width) {
                guide.position = width;
                sbPosition.setMax(width);
                setPositionTextSilently(width);
            }
            onNewGuideChangeListener.onChange(guide);
        });
    }
}
