package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

public class HsDialog extends FilterDialog {
    public interface OnChangedListener {
        void onChanged(@Size(4) float[] deltaHs, boolean stopped);
    }

    private final OnChangedListener listener;

    /**
     * <table>
     *     <tr><th>Index &nbsp;</th><td>0 &#x2013; 2</td><td>3</td></tr>
     *     <tr><th>Value &nbsp;</th><td>Components &nbsp;</td><td>Representation</td></tr>
     * </table>
     */
    @Size(4)
    private final float[] deltaHs;

    public HsDialog(Context context, OnChangedListener onChangedListener) {
        this(context, null, onChangedListener);
    }

    public HsDialog(Context context, @Size(4) float[] defaultDeltaHs, OnChangedListener onChangedListener) {
        super(context);
        builder.setTitle(R.string.hue_saturation).setView(R.layout.hs);

        deltaHs = defaultDeltaHs != null ? defaultDeltaHs : new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        listener = onChangedListener;
    }

    public static String comp2Symbol(int rep) {
        return rep == 1 ? "L" : "V";
    }

    private void onCompETTextChanged(int index, String s, Slider slider) {
        float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        if (index == 0) {
            f = (f % 360.0f + 360.0f) % 360.0f;
            if (f > 180.0f) f -= 360.0f;
        } else {
            f = f < -100.0f ? -1.0f : f > 100.0f ? 1.0f : f / 100.0f;
        }
        if (f == slider.getValue()) return;
        update(index, f, true);
        slider.setValue(f);
    }

    private void onCompSliderChanged(int index, float value, boolean stopped,
                                     TextInputEditText tiet, AfterTextChangedListener onETTextChangedListener) {
        update(index, value, stopped);
        tiet.removeTextChangedListener(onETTextChangedListener);
        tiet.setText(String.valueOf(index == 0 ? value : value * 100.0f));
        tiet.addTextChangedListener(onETTextChangedListener);
    }

    @Override
    void onFilterCommit() {
        listener.onChanged(deltaHs, true);
    }

    @Override
    public void show() {
        final AlertDialog dialog = builder.show();

        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);

        final Slider sHue = dialog.findViewById(R.id.s_hue);
        final Slider sSaturation = dialog.findViewById(R.id.s_saturation);
        final Slider sComp2 = dialog.findViewById(R.id.s_comp_2);
        final TextInputLayout tilComp2 = dialog.findViewById(R.id.til_comp_2);
        final TextInputEditText tietHue = dialog.findViewById(R.id.tiet_hue);
        final TextInputEditText tietSaturation = dialog.findViewById(R.id.tiet_saturation);
        final TextInputEditText tietComp2 = (TextInputEditText) tilComp2.getEditText();
        final TabLayout tlRepresentation = dialog.findViewById(R.id.tl_representation);
        final int rep = (int) deltaHs[3]; // Representation

        final AfterTextChangedListener onHueETTextChangedListener = s -> onCompETTextChanged(0, s, sHue);
        final AfterTextChangedListener onSatETTextChangedListener = s -> onCompETTextChanged(1, s, sSaturation);
        final AfterTextChangedListener onComp2ETTextChangedListener = s -> onCompETTextChanged(2, s, sComp2);
        final OnSliderChangeListener onHueSliderChangeListener = (slider, value, stopped) -> onCompSliderChanged(0, value, stopped, tietHue, onHueETTextChangedListener);
        final OnSliderChangeListener onSatSliderChangeListener = (slider, value, stopped) -> onCompSliderChanged(1, value, stopped, tietSaturation, onSatETTextChangedListener);
        final OnSliderChangeListener onComp2SliderChangeListener = (slider, value, stopped) -> onCompSliderChanged(2, value, stopped, tietComp2, onComp2ETTextChangedListener);

        sHue.setLabelFormatter(v -> String.format("%+.0f\u00B0", v));
        sHue.setValue(deltaHs[0]);
        sSaturation.setLabelFormatter(v -> String.format("%+.0f%%", v * 100.0f));
        sSaturation.setValue(deltaHs[1]);
        sComp2.setLabelFormatter(v -> String.format("%+.0f%%", v * 100.0f));
        sComp2.setValue(deltaHs[2]);
        tietHue.setText(String.valueOf(deltaHs[0]));
        tietSaturation.setText(String.valueOf(deltaHs[1] * 100.0f));
        tietComp2.setText(String.valueOf(deltaHs[2] * 100.0f));
        sHue.addOnChangeListener(onHueSliderChangeListener);
        sHue.addOnSliderTouchListener(onHueSliderChangeListener);
        sSaturation.addOnChangeListener(onSatSliderChangeListener);
        sSaturation.addOnSliderTouchListener(onSatSliderChangeListener);
        sComp2.addOnChangeListener(onComp2SliderChangeListener);
        sComp2.addOnSliderTouchListener(onComp2SliderChangeListener);
        tietHue.addTextChangedListener(onHueETTextChangedListener);
        tietSaturation.addTextChangedListener(onSatETTextChangedListener);
        tietComp2.addTextChangedListener(onComp2ETTextChangedListener);
        tlRepresentation.getTabAt(rep).select();

        tilComp2.setHint(comp2Symbol(rep));

        tlRepresentation.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final int position = tab.getPosition();
                deltaHs[3] = position;
                tilComp2.setHint(comp2Symbol(position));
                listener.onChanged(deltaHs, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void update(int index, float value, boolean stopped) {
        deltaHs[index] = value;
        listener.onChanged(deltaHs, stopped);
    }
}
