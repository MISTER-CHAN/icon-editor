package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnCBCheckedListener;
import com.misterchan.iconeditor.listener.OnItemSelectedListener;
import com.misterchan.iconeditor.listener.OnSBProgressChangedListener;
import com.misterchan.iconeditor.R;
import com.waynejo.androidndkgif.GifEncoder;

public class QualityManager {

    public interface OnApplyListener {
    }

    public interface OnApplyGifEncodingTypeListener extends OnApplyListener {
        void onApply(GifEncoder.EncodingType encodingType, boolean dither);
    }

    public interface OnApplyQualityListener extends OnApplyListener {
        void onApply(int quality, Bitmap.CompressFormat format);
    }

    private boolean gifDither;
    private final AlertDialog.Builder builder;
    private Bitmap.CompressFormat format;
    private GifEncoder.EncodingType gifEncodingType;
    private int quality;

    private QualityManager(Context context) {
        builder = new AlertDialog.Builder(context)
                .setTitle(R.string.quality)
                .setView(R.layout.quality)
                .setNegativeButton(R.string.cancel, null);
    }

    public QualityManager(Context context,
                          int defaultQuality, Bitmap.CompressFormat defaultFormat,
                          OnApplyQualityListener listener) {
        this(context);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> listener.onApply(quality, format));
        quality = defaultQuality;
        format = defaultFormat;
    }

    public QualityManager(Context context,
                          GifEncoder.EncodingType defaultGifEncodingType, boolean defaultGifDither,
                          OnApplyGifEncodingTypeListener listener) {
        this(context);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> listener.onApply(gifEncodingType, gifDither));
        gifEncodingType = defaultGifEncodingType;
        gifDither = defaultGifDither;
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        final boolean isFormatWebp = format == Bitmap.CompressFormat.WEBP_LOSSY || format == Bitmap.CompressFormat.WEBP_LOSSLESS;
        final GridLayout glGif = dialog.findViewById(R.id.gl_gif);
        final LinearLayout llQuality = dialog.findViewById(R.id.ll_quality);
        final RadioButton rbLossless = dialog.findViewById(R.id.rb_lossless);
        final RadioButton rbLossy = dialog.findViewById(R.id.rb_lossy);
        final SeekBar sbQuality = dialog.findViewById(R.id.sb_quality);
        final Spinner sGifEncodingType = dialog.findViewById(R.id.s_gif_encoding_type);
        final SwitchCompat sGifDither = dialog.findViewById(R.id.s_gif_dither);
        final TextInputEditText tietQuality = dialog.findViewById(R.id.tiet_quality);

        if (isFormatWebp) {
            dialog.findViewById(R.id.rg_formats).setVisibility(View.VISIBLE);
            rbLossless.setChecked(format == Bitmap.CompressFormat.WEBP_LOSSLESS);
            rbLossless.setOnCheckedChangeListener((OnCBCheckedListener) buttonView -> format = Bitmap.CompressFormat.WEBP_LOSSLESS);
            rbLossy.setChecked(format == Bitmap.CompressFormat.WEBP_LOSSY);
            rbLossy.setOnCheckedChangeListener((OnCBCheckedListener) buttonView -> format = Bitmap.CompressFormat.WEBP_LOSSY);
        }
        if (gifEncodingType != null) {
            glGif.setVisibility(View.VISIBLE);
            sGifDither.setChecked(gifDither);
            sGifDither.setOnCheckedChangeListener((buttonView, isChecked) -> gifDither = isChecked);
            sGifEncodingType.setSelection(switch (gifEncodingType) {
                case ENCODING_TYPE_SIMPLE_FAST -> 0;
                case ENCODING_TYPE_FAST -> 1;
                case ENCODING_TYPE_NORMAL_LOW_MEMORY -> 2;
                case ENCODING_TYPE_STABLE_HIGH_MEMORY -> 3;
                default -> 2;
            });
            sGifEncodingType.setOnItemSelectedListener((OnItemSelectedListener) (parent, view, position, id) -> {
                gifEncodingType = switch (position) {
                    case 0 -> GifEncoder.EncodingType.ENCODING_TYPE_SIMPLE_FAST;
                    case 1 -> GifEncoder.EncodingType.ENCODING_TYPE_FAST;
                    case 2 -> GifEncoder.EncodingType.ENCODING_TYPE_NORMAL_LOW_MEMORY;
                    case 3 -> GifEncoder.EncodingType.ENCODING_TYPE_STABLE_HIGH_MEMORY;
                    default -> GifEncoder.EncodingType.ENCODING_TYPE_NORMAL_LOW_MEMORY;
                };
            });
        } else {
            llQuality.setVisibility(View.VISIBLE);
            sbQuality.setProgress(quality);
            sbQuality.setOnSeekBarChangeListener((OnSBProgressChangedListener) (seekBar, progress) -> {
                quality = progress;
                tietQuality.setText(String.valueOf(progress));
            });
            tietQuality.setText(String.valueOf(quality));
            tietQuality.addTextChangedListener((AfterTextChangedListener) s -> {
                try {
                    final int i = Integer.parseUnsignedInt(s);
                    quality = i;
                    sbQuality.setProgress(i);
                } catch (NumberFormatException e) {
                }
            });
        }
    }
}
