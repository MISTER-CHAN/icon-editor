package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.Guide;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnCBCheckedListener;
import com.misterchan.iconeditor.listener.OnSliderValueChangeListener;

public class GuideEditor {

    public interface OnNewGuideChangeListener {
        void onChange(Guide guide);
    }

    private final AlertDialog.Builder builder;
    private final Guide guide;
    private final int width, height;
    private final OnNewGuideChangeListener onNewGuideChangeListener;
    private TextInputEditText tietPosition;

    private AfterTextChangedListener onPositionTextChangedListener;

    public GuideEditor(Context context, Guide guide, int width, int height,
                       OnNewGuideChangeListener onNewGuideChangeListener, DialogInterface.OnCancelListener onCancelListener) {
        builder = new MaterialAlertDialogBuilder(context)
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

        final RadioButton rbHorizontal = dialog.findViewById(R.id.rb_horizontal);
        final RadioButton rbVertical = dialog.findViewById(R.id.rb_vertical);
        final Slider sPosition = dialog.findViewById(R.id.s_position);
        tietPosition = dialog.findViewById(R.id.tiet_position);
        final OnSliderValueChangeListener onPositionSliderValueChange = (slider, value) -> {
            guide.position = (int) value;
            onNewGuideChangeListener.onChange(guide);
            setPositionTextSilently((int) value);
        };

        onPositionTextChangedListener = s -> {
            final int position;
            try {
                position = Integer.parseUnsignedInt(tietPosition.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            guide.position = position;
            sPosition.setValue(position);
            onNewGuideChangeListener.onChange(guide);
        };

        sPosition.setValueTo(height);
        sPosition.addOnChangeListener(onPositionSliderValueChange);
        tietPosition.addTextChangedListener(onPositionTextChangedListener);

        rbHorizontal.setOnCheckedChangeListener((OnCBCheckedListener) buttonView -> {
            guide.orientation = Guide.ORIENTATION_HORIZONTAL;
            if (guide.position > height) {
                guide.position = height;
                sPosition.setValue(height);
                setPositionTextSilently(height);
            }
            sPosition.setValueTo(height);
            onNewGuideChangeListener.onChange(guide);
        });

        rbVertical.setOnCheckedChangeListener((OnCBCheckedListener) buttonView -> {
            guide.orientation = Guide.ORIENTATION_VERTICAL;
            if (guide.position > width) {
                guide.position = width;
                sPosition.setValue(width);
                setPositionTextSilently(width);
            }
            sPosition.setValueTo(width);
            onNewGuideChangeListener.onChange(guide);
        });
    }
}
