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
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

public class HsDialog {
    public interface OnChangedListener {
        void onChanged(@Size(4) float[] deltaHs, boolean stopped);
    }

    private final AlertDialog.Builder builder;
    private OnChangedListener listener;

    /**
     * <table>
     *     <tr><th>Index &nbsp;</th><td>0 &#x2013; 2</td><td>3</td></tr>
     *     <tr><th>Value &nbsp;</th><td>Components &nbsp;</td><td>(0: HSV | 1: HSL)</td></tr>
     * </table>
     */
    @Size(4)
    private final float[] deltaHs;

    public HsDialog(Context context) {
        this(context, null);
    }

    public HsDialog(Context context, @Size(4) float[] defaultDeltaHs) {
        builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.hue_saturation)
                .setView(R.layout.hs);

        deltaHs = defaultDeltaHs != null ? defaultDeltaHs : new float[]{0.0f, 0.0f, 0.0f, 1.0f};
    }

    public HsDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        builder.setOnCancelListener(listener);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> listener.onCancel(dialog));
        return this;
    }

    public HsDialog setOnPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        builder.setPositiveButton(R.string.ok, listener);
        return this;
    }

    public HsDialog setOnChangeListener(OnChangedListener listener) {
        this.listener = listener;
        return this;
    }

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
        final TabLayout tlColorSpace = dialog.findViewById(R.id.tl_color_spaces);
        final TextView tvComp2 = dialog.findViewById(R.id.tv_comp_2);
        final int cs = (int) deltaHs[3]; // Color space
        final OnSliderChangeListener l = this::update;

        sHue.setValue(deltaHs[0]);
        sSaturation.setValue(deltaHs[1]);
        sComp2.setValue(deltaHs[2]);
        sHue.addOnChangeListener(l);
        sHue.addOnSliderTouchListener(l);
        sSaturation.addOnChangeListener(l);
        sSaturation.addOnSliderTouchListener(l);
        sComp2.addOnChangeListener(l);
        sComp2.addOnSliderTouchListener(l);
        tlColorSpace.getTabAt(cs).select();

        tvComp2.setText(switch (cs) {
            default -> R.string.v;
            case 1 -> R.string.l;
        });

        tlColorSpace.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final int position = tab.getPosition();
                deltaHs[3] = position;
                tvComp2.setText(switch (position) {
                    default -> R.string.v;
                    case 1 -> R.string.l;
                });
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

    private void update(Slider slider, float value, boolean stopped) {
        deltaHs[slider.getTag().toString().charAt(0) - '0'] = value;
        listener.onChanged(deltaHs, stopped);
    }
}
