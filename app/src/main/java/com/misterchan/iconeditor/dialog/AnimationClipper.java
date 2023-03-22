package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.OneShotPreDrawListener;

import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Tab;

import java.util.ArrayList;
import java.util.List;

public class AnimationClipper {

    public interface OnConfirmListener {
        void onApply(int from, int to);
    }

    private final AlertDialog.Builder builder;
    private ImageView iv;
    private final List<Bitmap> frames;
    private SeekBar sbFrom, sbTo;

    public AnimationClipper(Context context, List<Tab> tabs, Tab firstFrame, OnConfirmListener listener) {
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

        final SeekBar.OnSeekBarChangeListener l = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                iv.setImageBitmap(frames.get(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                iv.setImageBitmap(frames.get(seekBar.getProgress()));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };

        sbFrom.setMax(lastPos);
        sbFrom.setOnSeekBarChangeListener(l);
        sbTo.setMax(lastPos);
        sbTo.setProgress(lastPos);
        sbTo.setOnSeekBarChangeListener(l);

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
