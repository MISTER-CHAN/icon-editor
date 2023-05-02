package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.OneShotPreDrawListener;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Tab;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;

import java.util.ArrayList;
import java.util.List;

public class AnimationClipper {

    public interface OnConfirmListener {
        void onApply(int from, int to);
    }

    private final AlertDialog.Builder builder;
    private ImageView iv;
    private final List<Bitmap> frames;
    private Slider sFrom, sTo;

    public AnimationClipper(Context context, List<Tab> tabs, Tab firstFrame, OnConfirmListener listener) {
        frames = new ArrayList<>();

        builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.clip)
                .setView(R.layout.animation_clipper)
                .setPositiveButton(R.string.ok, (dialog, which) ->
                        listener.onApply(frames.size() - (int) sTo.getValue(), frames.size() - (int) sFrom.getValue()))
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener(dialog -> frames.forEach(Bitmap::recycle));

        for (int i = 0; i < tabs.size(); ++i) {
            final Tab tab = tabs.get(i).getBackground();
            i = tab.getBackgroundPosition();
            if (tab.getFirstFrame() != firstFrame) {
                if (frames.isEmpty()) {
                    continue;
                } else {
                    break;
                }
            }
            frames.add(Tab.mergeLayers(tab.layerTree));
        }
    }

    public AnimationClipper show() {
        final AlertDialog dialog = builder.show();

        final Bitmap firstFrame = frames.get(0);
        final int lastPos = frames.size() - 1;
        final int width = firstFrame.getWidth(), height = firstFrame.getHeight();

        final FrameLayout flIv = dialog.findViewById(R.id.fl_iv);
        iv = dialog.findViewById(R.id.iv);
        sFrom = dialog.findViewById(R.id.s_from);
        sTo = dialog.findViewById(R.id.s_to);

        sFrom.setValueTo(lastPos);
        sTo.setValueTo(lastPos);
        sTo.setValue(lastPos);

        {
            final Slider.OnChangeListener ocl = (OnSliderChangeListener) (slider, value, stopped) ->
                    iv.setImageBitmap(frames.get((int) value));

            final Slider.OnSliderTouchListener ostl = new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                    iv.setImageBitmap(frames.get((int) slider.getValue()));
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                }
            };

            sFrom.addOnChangeListener(ocl);
            sFrom.addOnSliderTouchListener(ostl);
            sTo.addOnChangeListener(ocl);
            sTo.addOnSliderTouchListener(ostl);
        }

        OneShotPreDrawListener.add(flIv, () -> {
            final int w = flIv.getMeasuredWidth();
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) flIv.getLayoutParams();
            lp.height = w * height / width;
            flIv.setLayoutParams(lp);
        });

        iv.setImageBitmap(frames.get(0));

        return this;
    }
}
