package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.util.BitmapUtils;

public class BitmapConfigModifier {
    private static final Bitmap.Config[] VALUES = {
            null, Bitmap.Config.ALPHA_8, null, Bitmap.Config.RGB_565, Bitmap.Config.ARGB_4444, Bitmap.Config.ARGB_8888, null, null, null
    };

    public interface OnChangedListener {
        void onChanged(Bitmap bitmap);
    }

    private Bitmap.Config config;

    private BitmapConfigModifier(Bitmap.Config config) {
        this.config = config;
    }

    public static void showDialog(Context context, Bitmap bitmap, OnChangedListener listener) {
        final Bitmap.Config config = bitmap.getConfig();
        final BitmapConfigModifier bcm = new BitmapConfigModifier(config);
        final int checkedItem = switch (config) {
            default -> 0;
            case ALPHA_8 -> 1;
            case RGB_565 -> 3;
            case ARGB_4444 -> 4;
            case ARGB_8888 -> 5;
            case RGBA_F16 -> 6;
            case RGBA_1010102 -> 8;
        };
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.config)
                .setSingleChoiceItems(R.array.bitmap_configs, checkedItem, (dialog, which) -> {
                    bcm.config = VALUES[which];
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (bcm.config == null) {
                        listener.onChanged(null);
                        return;
                    }
                    final ColorSpace colorSpace = bitmap.getColorSpace();
                    final Bitmap bm = BitmapUtils.createBitmap(bitmap, null, bcm.config, bitmap.hasAlpha(),
                            colorSpace != null || bcm.config == Bitmap.Config.ALPHA_8 ? colorSpace : ColorSpace.get(ColorSpace.Named.SRGB));
                    listener.onChanged(bm);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
