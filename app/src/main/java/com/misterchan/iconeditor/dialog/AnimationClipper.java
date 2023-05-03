package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.OneShotPreDrawListener;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.RangeSlider;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Tab;
import com.misterchan.iconeditor.listener.OnCircularRSChangeListener;

import java.util.ArrayList;
import java.util.List;

public class AnimationClipper {

    public interface OnConfirmListener {
        void onConfirm(int from, int to);
    }

    private final AlertDialog.Builder builder;
    private ImageView iv;
    private int from, to;
    private final List<Bitmap> frames;

    public AnimationClipper(Context context, List<Tab> tabs, Tab firstFrame, OnConfirmListener listener) {
        frames = new ArrayList<>();

        builder = new MaterialAlertDialogBuilder(context)
                .setIcon(R.drawable.ic_content_cut).setTitle(R.string.clip)
                .setView(R.layout.animation_clipper)
                .setPositiveButton(R.string.ok, (dialog, which) ->
                        listener.onConfirm(frames.size() - to, frames.size() - from))
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
        if (frames.size() < 2) {
            return this;
        }

        final AlertDialog dialog = builder.show();

        final Bitmap firstFrame = frames.get(0);
        final int lastPos = frames.size() - 1;
        final int width = firstFrame.getWidth(), height = firstFrame.getHeight();

        final FrameLayout flIv = dialog.findViewById(R.id.fl_iv);
        iv = dialog.findViewById(R.id.iv);
        final RangeSlider rs = dialog.findViewById(R.id.rs);

        rs.setValueTo(lastPos);
        rs.setValues(0.0f, (float) lastPos);

        final OnCircularRSChangeListener oscl = new OnCircularRSChangeListener(false) {
            @Override
            public void onChange(@NonNull RangeSlider slider, float value, boolean inclusive, boolean stopped) {
                if (stopped) {
                    return;
                }
                final List<Float> values = slider.getValues();
                from = values.get(inclusive ? 0 : 1).intValue();
                to = values.get(inclusive ? 1 : 0).intValue();
                iv.setImageBitmap(frames.get((int) value));
            }

            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                iv.setImageBitmap(frames.get(slider.getValues().get(slider.getFocusedThumbIndex()).intValue()));
            }
        };
        rs.addOnChangeListener(oscl);
        rs.addOnSliderTouchListener(oscl);

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
