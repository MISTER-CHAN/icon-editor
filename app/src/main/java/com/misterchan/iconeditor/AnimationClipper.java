package com.misterchan.iconeditor;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.OneShotPreDrawListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class AnimationClipper {

    public interface OnApplyListener {
        void onApply(int from, int to);
    }

    private final AlertDialog.Builder builder;
    private ImageView iv;
    private final List<Bitmap> frames;
    private SeekBar sbFrom, sbTo;

    public AnimationClipper(Context context, List<Tab> tabs, Tab firstFrame, OnApplyListener listener) {
        frames = new ArrayList<>();

        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.clip)
                .setView(R.layout.animation_clipper)
                .setPositiveButton(R.string.ok, (dialog, which) ->
                        listener.onApply(frames.size() - sbTo.getProgress(), frames.size() - sbFrom.getProgress()))
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
        sbFrom = dialog.findViewById(R.id.sb_from);
        sbTo = dialog.findViewById(R.id.sb_to);

        final OnSeekBarChangeListener l = (progress, stopped) -> iv.setImageBitmap(frames.get(progress));

        sbFrom.setMax(lastPos);
        sbFrom.setOnSeekBarChangeListener(l);
        sbTo.setMax(lastPos);
        sbTo.setProgress(lastPos);
        sbTo.setOnSeekBarChangeListener(l);

        OneShotPreDrawListener.add(flIv, () -> {
            final int w = flIv.getMeasuredWidth();
            final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) flIv.getLayoutParams();
            lp.height = w * width / height;
            flIv.setLayoutParams(lp);
        });

        iv.setImageBitmap(frames.get(0));

        return this;
    }
}
