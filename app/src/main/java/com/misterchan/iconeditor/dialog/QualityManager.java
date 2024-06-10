package com.misterchan.iconeditor.dialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.misterchan.iconeditor.Project;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnCBCheckedListener;
import com.misterchan.iconeditor.listener.OnItemSelectedListener;
import com.misterchan.iconeditor.listener.OnSliderValueChangeListener;
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
        builder = new MaterialAlertDialogBuilder(context)
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

    public static void setQuality(Context context, Project project, DirectorySelector.OnFileNameApplyCallback callback) {
        switch (project.fileType) {
            case PNG -> callback.onApply(project);
            case GIF -> {
                new QualityManager(context,
                        project.gifEncodingType == null ? GifEncoder.EncodingType.ENCODING_TYPE_NORMAL_LOW_MEMORY : project.gifEncodingType,
                        project.gifDither,
                        (encodingType, dither) -> {
                            project.gifEncodingType = encodingType;
                            project.gifDither = dither;
                            callback.onApply(project);
                        })
                        .show();
            }
            default -> {
                new QualityManager(context,
                        project.quality < 0 ? 100 : project.quality,
                        project.compressFormat,
                        (quality, format) -> {
                            project.quality = quality;
                            project.compressFormat = format;
                            if (callback != null) callback.onApply(project);
                        })
                        .show();
            }
        }
    }

    public void show() {
        final AlertDialog dialog = builder.show();

        final boolean isFormatWebp = format == Bitmap.CompressFormat.WEBP_LOSSY || format == Bitmap.CompressFormat.WEBP_LOSSLESS;
        final Group gGif = dialog.findViewById(R.id.g_gif);
        final Group gQuality = dialog.findViewById(R.id.g_quality);
        final RadioButton rbLossless = dialog.findViewById(R.id.rb_lossless);
        final RadioButton rbLossy = dialog.findViewById(R.id.rb_lossy);
        final Slider sQuality = dialog.findViewById(R.id.s_quality);
        final Spinner sGifEncodingType = dialog.findViewById(R.id.s_gif_encoding_type);
        final SwitchCompat sGifDither = dialog.findViewById(R.id.s_gif_dither);
        final TextInputEditText tietQuality = dialog.findViewById(R.id.tiet_quality);

        if (isFormatWebp) {
            dialog.findViewById(R.id.g_format).setVisibility(View.VISIBLE);
            rbLossless.setChecked(format == Bitmap.CompressFormat.WEBP_LOSSLESS);
            rbLossless.setOnCheckedChangeListener((OnCBCheckedListener) buttonView -> format = Bitmap.CompressFormat.WEBP_LOSSLESS);
            rbLossy.setChecked(format == Bitmap.CompressFormat.WEBP_LOSSY);
            rbLossy.setOnCheckedChangeListener((OnCBCheckedListener) buttonView -> format = Bitmap.CompressFormat.WEBP_LOSSY);
        }
        if (gifEncodingType != null) {
            gGif.setVisibility(View.VISIBLE);
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
            gQuality.setVisibility(View.VISIBLE);
            sQuality.setValue(quality);
            sQuality.addOnChangeListener((OnSliderValueChangeListener) (slider, value) -> {
                tietQuality.setText(String.valueOf((int) value));
            });
            tietQuality.setText(String.valueOf(quality));
            tietQuality.addTextChangedListener((AfterTextChangedListener) s -> {
                try {
                    final int i = Integer.parseUnsignedInt(s);
                    if (!(0 <= i && i <= 100)) return;
                    quality = i;
                    sQuality.setValue(i);
                } catch (NumberFormatException e) {
                }
            });
        }
    }
}
