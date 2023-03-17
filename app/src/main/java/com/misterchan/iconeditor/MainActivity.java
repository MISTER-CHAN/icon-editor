package com.misterchan.iconeditor;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorSpace;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.MessageQueue;
import android.text.Editable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorLong;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.OneShotPreDrawListener;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.colorpicker.ArgbColorIntPicker;
import com.misterchan.iconeditor.colorpicker.ArgbColorPicker;
import com.misterchan.iconeditor.dialog.AnimationClipper;
import com.misterchan.iconeditor.dialog.CellGridManager;
import com.misterchan.iconeditor.dialog.ColorBalanceDialog;
import com.misterchan.iconeditor.dialog.ColorMatrixManager;
import com.misterchan.iconeditor.dialog.ColorRangeDialog;
import com.misterchan.iconeditor.dialog.CurvesDialog;
import com.misterchan.iconeditor.dialog.DirectorySelector;
import com.misterchan.iconeditor.dialog.EditNumberDialog;
import com.misterchan.iconeditor.dialog.GuideEditor;
import com.misterchan.iconeditor.dialog.HiddenImageMaker;
import com.misterchan.iconeditor.dialog.HsvDialog;
import com.misterchan.iconeditor.dialog.ImageSizeManager;
import com.misterchan.iconeditor.dialog.LevelsDialog;
import com.misterchan.iconeditor.dialog.LightingDialog;
import com.misterchan.iconeditor.dialog.MatrixManager;
import com.misterchan.iconeditor.dialog.NewImageDialog;
import com.misterchan.iconeditor.dialog.QualityManager;
import com.misterchan.iconeditor.dialog.SeekBarDialog;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnCheckedListener;
import com.misterchan.iconeditor.listener.OnSeekBarChangeListener;
import com.misterchan.iconeditor.util.BitmapUtils;
import com.misterchan.iconeditor.util.RunnableRunnable;
import com.misterchan.iconeditor.util.UriUtils;
import com.waynejo.androidndkgif.GifDecoder;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private enum Position {
        LEFT(R.string.left),
        TOP(R.string.top),
        RIGHT(R.string.right),
        BOTTOM(R.string.bottom);

        @StringRes
        private final int name;

        Position(@StringRes int name) {
            this.name = name;
        }
    }

    private static final BlendMode[] BLEND_MODES = BlendMode.values();

    private static final Looper MAIN_LOOPER = Looper.getMainLooper();

    private static final Paint PAINT_BITMAP = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
            setAntiAlias(false);
            setFilterBitmap(false);
        }
    };

    private static final Paint PAINT_CELL_GRID = new Paint() {
        {
            setColor(Color.RED);
            setStrokeWidth(2.0f);
        }
    };

    private static final Paint PAINT_CLEAR = new Paint() {
        {
            setBlendMode(BlendMode.CLEAR);
        }
    };

    private static final Paint PAINT_DST_IN = new Paint() {
        {
            setBlendMode(BlendMode.DST_IN);
        }
    };

    private static final Paint PAINT_DST_OUT = new Paint() {
        {
            setBlendMode(BlendMode.DST_OUT);
        }
    };

    private static final Paint PAINT_GRID = new Paint() {
        {
            setColor(Color.GRAY);
        }
    };

    private static final Paint PAINT_GUIDE = new Paint() {
        {
            setColor(Color.CYAN);
            setStrokeWidth(2.0f);
        }
    };

    private static final Paint PAINT_IMAGE_BOUND = new Paint() {
        {
            setColor(Color.DKGRAY);
        }
    };

    private static final Paint PAINT_POINT = new Paint() {
        {
            setColor(Color.RED);
            setStrokeWidth(4.0f);
            setTextSize(32.0f);
        }
    };

    private static final Paint PAINT_SRC = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
        }
    };

    private static final Paint PAINT_SRC_IN = new Paint() {
        {
            setBlendMode(BlendMode.SRC_IN);
        }
    };

    private static final Paint PAINT_SRC_OVER = new Paint();

    private static final Paint PAINT_TEXT_LINE = new Paint() {
        {
            setColor(Color.BLUE);
            setStrokeWidth(2.0f);
        }
    };

    private Bitmap bitmap;
    private Bitmap refBm;
    private Bitmap chessboard;
    private Bitmap chessboardImage;
    private Bitmap clipboard;
    private Bitmap gridImage;
    private Bitmap lastMerged;
    private Bitmap previewImage;
    private Bitmap rulerHImage, rulerVImage;
    private Bitmap selectionImage;
    private Bitmap viewImage;
    private boolean antiAlias = false;
    private boolean hasNotLoaded = true;
    private boolean hasSelection = false;
    private boolean isEditingText = false;
    private boolean isShapeStopped = true;
    private Canvas canvas;
    private Canvas chessboardCanvas;
    private Canvas gridCanvas;
    private Canvas previewCanvas;
    private Canvas rulerHCanvas, rulerVCanvas;
    private Canvas selectionCanvas;
    private Canvas viewCanvas;
    private CheckBox cbBucketFillContiguous;
    private CheckBox cbBucketFillIgnoreAlpha;
    private CheckBox cbCloneStampAntiAlias;
    private CheckBox cbGradientAntiAlias;
    private CheckBox cbMagicPaintAntiAlias;
    private CheckBox cbMagErAccEnabled;
    private CheckBox cbPatcherAntiAlias;
    private CheckBox cbPathAntiAlias;
    private CheckBox cbPathFill;
    private CheckBox cbPencilAntiAlias;
    private CheckBox cbPencilWithEraser;
    private CheckBox cbShapeFill;
    private CheckBox cbTextFill;
    private CheckBox cbTransformerFilter;
    private CheckBox cbTransformerLar;
    private CheckBox cbZoom;
    private ColorAdapter colorAdapter;
    private final DirectorySelector dirSelector = new DirectorySelector(this);
    private float backgroundScaledW, backgroundScaledH;
    private float blurRadius = 0.0f, blurRadiusEraser = 0.0f;
    private float scale;
    private float strokeWidth = 1.0f, strokeHalfWidthEraser = 0.5f;
    private float textSize = 12.0f;
    private float translationX, translationY;
    private FrameLayout flImageView;
    private FrameLayout flToolOptions;
    private FrameLayout svOptionsBrush;
    private FrameLayout svOptionsBucketFill;
    private FrameLayout svOptionsCloneStamp;
    private FrameLayout svOptionsEraser;
    private FrameLayout svOptionsEyedropper;
    private FrameLayout svOptionsGradient;
    private FrameLayout svOptionsMagicEraser;
    private FrameLayout svOptionsMagicPaint;
    private FrameLayout svOptionsPatcher;
    private FrameLayout svOptionsPath;
    private FrameLayout svOptionsPencil;
    private FrameLayout svOptionsTransformer;
    private ImageView imageView;
    private ImageView ivChessboard;
    private ImageView ivGrid;
    private ImageView ivPreview;
    private ImageView ivRulerH, ivRulerV;
    private ImageView ivSelection;
    private InputMethodManager inputMethodManager;
    private int rulerHHeight, rulerVWidth;
    private int shapeStartX, shapeStartY;
    private int textX, textY;
    private int threshold;
    private int viewWidth, viewHeight;
    private LinearLayout llOptionsShape;
    private LinearLayout llOptionsText;
    private LinkedList<Long> palette;
    private List<Tab> tabs;
    private MenuItem miHasAlpha;
    private MenuItem miLayerColorMatrix;
    private MenuItem miLayerCurves;
    private MenuItem miLayerDrawBelow;
    private MenuItem miLayerFilterSet;
    private MenuItem miLayerHsv;
    private MenuItem miLayerLevelUp;
    private MenuItem miLayerReference;
    private Point cloneStampSrc;
    private Point magErB, magErF; // Magic eraser background and foreground
    private final PointF magErBD = new PointF(0.0f, 0.0f), magErFD = new PointF(0.0f, 0.0f); // Distance
    private Position marqueeBoundBeingDragged = null;
    private Preview imagePreview;
    private RadioButton rbCloneStamp;
    private RadioButton rbEyedropper;
    private RadioButton rbEyedropperAllLayers;
    private RadioButton rbMagicEraser;
    private RadioButton rbMagicEraserLeft, rbMagicEraserRight;
    private CompoundButton cbMagicEraserPosition;
    private RadioButton rbMagicPaint;
    private RadioButton rbPencil;
    private RadioButton rbTransformer;
    private final Rect selection = new Rect();
    private final Ruler ruler = new Ruler();
    private Settings settings;
    private SubMenu smLayerBlendModes;
    private Paint.Style style = Paint.Style.FILL_AND_STROKE;
    private Tab tab;
    private TabLayout tabLayout;
    private TextInputEditText tietBrushBlurRadius;
    private TextInputEditText tietBrushStrokeWidth;
    private TextInputEditText tietCloneStampBlurRadius;
    private TextInputEditText tietCloneStampStrokeWidth;
    private TextInputEditText tietGradientBlurRadius;
    private TextInputEditText tietGradientStrokeWidth;
    private TextInputEditText tietMagicEraserStrokeWidth;
    private TextInputEditText tietMagicPaintBlurRadius;
    private TextInputEditText tietMagicPaintStrokeWidth;
    private TextInputEditText tietPatcherBlurRadius;
    private TextInputEditText tietPatcherStrokeWidth;
    private TextInputEditText tietPathBlurRadius;
    private TextInputEditText tietPathStrokeWidth;
    private TextInputEditText tietPencilBlurRadius;
    private TextInputEditText tietPencilStrokeWidth;
    private TextInputEditText tietShapeStrokeWidth;
    private TextInputEditText tietText;
    private TextView tvStatus;
    private Thread thread = new Thread();
    private Transformer transformer;
    private Uri fileToBeOpened;
    private View vBackgroundColor;
    private View vForegroundColor;

    private final Paint bitmapPaint = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
            setAntiAlias(false);
        }
    };

    private final Paint eraser = new Paint() {
        {
            setAntiAlias(false);
            setColor(Color.TRANSPARENT);
            setDither(false);
            setStrokeCap(Cap.ROUND);
            setStrokeJoin(Join.ROUND);
            setStrokeWidth(1.0f);
            setBlendMode(BlendMode.SRC);
        }
    };

    private final Paint fillPaint = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setColor(Color.BLACK);
            setStyle(Style.FILL_AND_STROKE);
        }
    };

    private final Paint magicPaint = new Paint();

    private final Paint marginPaint = new Paint() {
        {
            setColor(Color.MAGENTA);
            setStrokeWidth(2.0f);
            setTextSize(24.0f);
        }
    };

    private final Paint paint = new Paint() {
        {
            setAntiAlias(false);
            setColor(Color.BLACK);
            setDither(false);
            setStrokeCap(Cap.ROUND);
            setStrokeJoin(Join.ROUND);
            setStrokeWidth(1.0f);
            setStyle(Style.FILL_AND_STROKE);
            setTextAlign(Paint.Align.CENTER);
        }
    };

    private final Paint patcher = new Paint() {
        {
            setBlendMode(BlendMode.DST_IN);
            setColor(Color.BLACK);
            setStyle(Style.FILL_AND_STROKE);
        }
    };

    private final Paint rulerPaint = new Paint() {
        {
            setColor(Color.GRAY);
            setStrokeWidth(1.0f);
            setTextSize(24.0f);
        }
    };

    private final Paint selector = new Paint() {
        {
            setColor(Color.DKGRAY);
            setStrokeWidth(4.0f);
            setStyle(Style.STROKE);
        }
    };

    private final ActivityResultCallback<List<Uri>> onImagesPickedCallback = result -> result.forEach(this::openFile);

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(
                    new ActivityResultContracts.PickMultipleVisualMedia(Integer.MAX_VALUE),
                    onImagesPickedCallback);

    private final PickVisualMediaRequest pickVisualMediaRequest = new PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
            .build();

    private final ActivityResultLauncher<Intent> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Environment.isExternalStorageManager()) {
                    save();
                }
            });

    private final AfterTextChangedListener onBlurRadiusTextChangedListener = s -> {
        try {
            final float f = Float.parseFloat(s);
            blurRadius = f;
            setBlurRadius(paint, f);
        } catch (NumberFormatException e) {
        }
    };

    private final AfterTextChangedListener onStrokeWidthETTextChangedListener = s -> {
        try {
            final float f = Float.parseFloat(s);
            strokeWidth = f;
            paint.setStrokeWidth(f);
        } catch (NumberFormatException e) {
        }
    };

    private final AfterTextChangedListener onTextSizeChangedListener = s -> {
        try {
            final float f = Float.parseFloat(s);
            textSize = f;
            paint.setTextSize(f);
        } catch (NumberFormatException e) {
        }
        drawTextOnView();
    };

    private final CompoundButton.OnCheckedChangeListener onAntiAliasCBCheckedChangeListener = (buttonView, isChecked) -> {
        antiAlias = isChecked;
        paint.setAntiAlias(isChecked);
    };

    private final CompoundButton.OnCheckedChangeListener onFillCBCheckedChangeListener = (buttonView, isChecked) -> {
        style = isChecked ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE;
        paint.setStyle(style);
    };

    private final DialogInterface.OnClickListener onApplyLayerNameListener = (dialog, which) -> {
        final TextInputEditText tietFileName = ((AlertDialog) dialog).findViewById(R.id.tiet_file_name);
        final Editable name = tietFileName.getText();
        if (name.length() <= 0) {
            return;
        }
        tab.setTitle(name);
    };

    private final View.OnClickListener onClickAddSwatchViewListener = v ->
            ArgbColorIntPicker.make(MainActivity.this,
                            R.string.add,
                            (oldColor, newColor) -> {
                                palette.offerFirst(newColor);
                                colorAdapter.notifyDataSetChanged();
                            },
                            paint.getColorLong())
                    .show();

    private final View.OnClickListener onClickBackgroundColorListener = v ->
            ArgbColorIntPicker.make(MainActivity.this,
                            R.string.background_color,
                            (oldColor, newColor) -> {
                                if (newColor != null) {
                                    eraser.setColor(newColor);
                                    vBackgroundColor.setBackgroundColor(Color.toArgb(newColor));
                                } else {
                                    swapColor();
                                }
                            },
                            eraser.getColorLong(),
                            R.string.swap)
                    .show();

    private final View.OnClickListener onClickCloneStampSrcButtonListener = v -> {
        cloneStampSrc = null;
        eraseBitmapAndInvalidateView(previewImage, ivPreview);
    };

    private final View.OnClickListener onClickForegroundColorListener = v ->
            ArgbColorIntPicker.make(MainActivity.this,
                            R.string.foreground_color,
                            (oldColor, newColor) -> {
                                if (newColor != null) {
                                    paint.setColor(newColor);
                                    vForegroundColor.setBackgroundColor(Color.toArgb(newColor));
                                    if (isEditingText) {
                                        drawTextOnView();
                                    }
                                } else {
                                    swapColor();
                                }
                            },
                            paint.getColorLong(),
                            R.string.swap)
                    .show();

    private final ColorMatrixManager.OnMatrixElementsChangedListener onColorMatrixChangedListener = matrix -> runOrStart(() -> {
        imagePreview.addColorMatrixColorFilter(matrix);
        drawImagePreviewOnView(true);
    }, true);

    private final ColorMatrixManager.OnMatrixElementsChangedListener onLayerColorMatrixChangedListener = matrix -> {
        tab.colorMatrix = matrix;
        drawBitmapOnView(true);
    };

    private final ColorRangeDialog.OnColorRangeChangedListener onColorRangeChangedListener = (hueMin, hueMax, lumMin, lumMax, stopped) -> {
        runOrStart(() -> {
            if (hueMin == 0 && hueMax == 360 && lumMin == 0x0 && lumMax == 0xFF) {
                imagePreview.clearFilter();
            } else if (lumMin > lumMax) {
                imagePreview.drawColor(Color.TRANSPARENT);
            } else {
                final int width = imagePreview.getWidth(), height = imagePreview.getHeight(), area = width * height;
                final int[] src = imagePreview.getPixels(), dst = new int[area];
                for (int i = 0; i < area; ++i) {
                    final float h = Color.hue(src[i]);
                    final int v = (int) (Color.luminance(src[i]) * 100.0f);
                    dst[i] = (hueMin <= h && h <= hueMax
                            || hueMin > hueMax && (hueMin <= h || h <= hueMax))
                            && (lumMin <= v && v <= lumMax)
                            ? src[i] : Color.TRANSPARENT;
                }
                imagePreview.setPixels(dst, width, height);
            }
            drawImagePreviewOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_color_range), hueMin, hueMax, lumMin, lumMax));
    };

    private final ColorRangeDialog.OnColorRangeChangedListener onApplyLayerDuplicatingByColorRangeListener = (hueMin, hueMax, valueMin, valueMax, stopped) -> {
        final Bitmap p = imagePreview.getEntire();
        final Bitmap bm = Bitmap.createBitmap(p.getWidth(), p.getHeight(),
                p.getConfig(), true, p.getColorSpace());
        final Canvas cv = new Canvas(bm);
        if (hasSelection) {
            cv.drawBitmap(p, selection, selection, PAINT_SRC);
        } else {
            cv.drawBitmap(p, 0.0f, 0.0f, PAINT_SRC);
        }
        imagePreview.recycle();
        imagePreview = null;
        addLayer(bm, tabLayout.getSelectedTabPosition(),
                tab.getLevel(), tab.left, tab.top,
                tab.visible, getString(R.string.copy_noun));
        clearStatus();
    };

    private final CurvesDialog.OnCurvesChangedListener onFilterCurvesChangedListener = (curves, stopped) -> runOrStart(() -> {
        final int w = imagePreview.getWidth(), h = imagePreview.getHeight();
        final int[] src = imagePreview.getPixels(), dst = new int[w * h];
        BitmapUtils.applyCurves(src, dst, curves);
        imagePreview.setPixels(dst, w, h);
        drawImagePreviewOnView(stopped);
    }, stopped);

    private final HiddenImageMaker.OnMakeListener onMakeHiddenImageListener = bitmap -> {
        final Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        new Canvas(bm).drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
        addTab(bm, tabLayout.getSelectedTabPosition() + 2);
        bitmap.recycle();
    };

    private final HsvDialog.OnHsvChangedListener onFilterHsvChangedListener = (deltaHsv, stopped) -> {
        runOrStart(() -> {
            if (deltaHsv[0] == 0.0f && deltaHsv[1] == 0.0f && deltaHsv[2] == 0.0f) {
                imagePreview.clearFilter();
            } else {
                final int w = imagePreview.getWidth(), h = imagePreview.getHeight();
                final int[] src = imagePreview.getPixels(), dst = new int[w * h];
                BitmapUtils.shiftHsv(src, dst, deltaHsv);
                imagePreview.setPixels(dst, w, h);
            }
            drawImagePreviewOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_hsv), deltaHsv[0], deltaHsv[1], deltaHsv[2]));
    };

    private final HsvDialog.OnHsvChangedListener onLayerHsvChangedListener = (deltaHsv, stopped) -> {
        tab.deltaHsv = deltaHsv;
        drawBitmapOnView(stopped);
        tvStatus.setText(String.format(getString(R.string.state_hsv), deltaHsv[0], deltaHsv[1], deltaHsv[2]));
    };

    private final MatrixManager.OnMatrixElementsChangedListener onMatrixChangedListener = matrix -> runOrStart(() -> {
        imagePreview.transform(matrix);
        drawBitmapOnView(imagePreview.getEntire(), true);
    }, true);

    private final MatrixManager.OnMatrixElementsChangedListener onLayerMatrixChangedListener = matrix -> runOrStart(() -> {
        drawBitmapOnView(true);
    }, true);

    private final DialogInterface.OnCancelListener onCancelImagePreviewListener = dialog -> {
        drawBitmapOnView(selection, true);
        imagePreview.recycle();
        imagePreview = null;
        clearStatus();
    };

    private final DialogInterface.OnClickListener onClickImagePreviewNBListener =
            (dialog, which) -> onCancelImagePreviewListener.onCancel(dialog);

    private final DialogInterface.OnClickListener onClickImagePreviewPBListener = (dialog, which) -> {
        drawImagePreviewOnImage();
        addToHistory();
        clearStatus();
    };

    private final LevelsDialog.OnLevelsChangedListener onFilterLevelsChangedListener = (inputShadows, inputHighlights, outputShadows, outputHighlights, stopped) -> {
        final float ratio = (float) (outputHighlights - outputShadows) / (float) (inputHighlights - inputShadows);
        runOrStart(() -> {
            imagePreview.addLightingColorFilter(ratio, -inputShadows * ratio + outputShadows);
            drawImagePreviewOnView(stopped);
        }, stopped);
    };

    private final LightingDialog.OnLightingChangedListener onLightingChangedListener = (lighting, stopped) -> runOrStart(() -> {
        imagePreview.addLightingColorFilter(lighting);
        drawImagePreviewOnView(stopped);
    }, stopped);

    private final NewImageDialog.OnApplyListener onApplyNewImagePropertiesListener = this::createImage;

    private final EditNumberDialog.OnPositiveButtonClickListener onApplyUniformFrameDelayListener = number -> {
        final Tab firstFrame = tab.getBackground().getFirstFrame();
        for (int i = firstFrame.getBackgroundPosition(); i < tabs.size(); ++i) {
            final Tab frame = tabs.get(i).getBackground();
            if (frame.getFirstFrame() != firstFrame) {
                break;
            }
            i = frame.getBackgroundPosition();
            frame.delay = number;
        }
    };

    private final OnSeekBarChangeListener onFilterContrastSeekBarChangeListener = (progress, stopped) -> {
        final float scale = progress / 10.0f, shift = 0xFF / 2.0f * (1.0f - scale);
        runOrStart(() -> {
            imagePreview.addLightingColorFilter(scale, shift);
            drawImagePreviewOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_contrast), scale));
    };

    private final OnSeekBarChangeListener onFilterHToASeekBarChangeListener = (progress, stopped) -> {
        runOrStart(() -> {
            final int w = imagePreview.getWidth(), h = imagePreview.getHeight();
            final int[] src = imagePreview.getPixels(), dst = new int[w * h];
            BitmapUtils.setAlphaByHue(src, dst, progress);
            imagePreview.setPixels(dst, w, h);
            drawImagePreviewOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_hue), (float) progress));
    };

    private final OnSeekBarChangeListener onFilterLightnessSeekBarChangeListener = (progress, stopped) -> {
        runOrStart(() -> {
            imagePreview.addLightingColorFilter(1.0f, progress);
            drawImagePreviewOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_lightness), progress));
    };

    private final OnSeekBarChangeListener onFilterSaturationSeekBarChangeListener = (progress, stopped) -> {
        final float f = progress / 10.0f;
        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(f);
        runOrStart(() -> {
            imagePreview.addColorMatrixColorFilter(colorMatrix.getArray());
            drawImagePreviewOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_saturation), f));
    };

    private final OnSeekBarChangeListener onFilterThresholdSeekBarChangeListener = (progress, stopped) -> {
        final float f = -0x100 * progress;
        runOrStart(() -> {
            imagePreview.addColorMatrixColorFilter(new float[]{
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            });
            drawImagePreviewOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_threshold), progress));
    };

    private final OnSeekBarChangeListener onLayerAlphaSeekBarChangeListener = (progress, stopped) -> {
        tab.paint.setAlpha(progress);
        drawBitmapOnView(stopped);
        tvStatus.setText(String.format(
                String.format(getString(R.string.state_alpha), settings.getArgbComponentFormat()),
                progress));
    };

    private final OnSeekBarChangeListener onNoiseSeekBarChangeListener = (progress, stopped) -> {
        runOrStart(() -> {
            if (progress == 0) {
                imagePreview.clearFilter();
            } else if (progress == 100) {
                imagePreview.drawColor(paint.getColor());
            } else {
                final int w = imagePreview.getWidth(), h = imagePreview.getHeight(), area = w * h;
                final int[] pixels = imagePreview.getPixels(w, h, area);
                BitmapUtils.generateNoise(pixels, area, paint.getColor(), progress / 100.0f, null);
                imagePreview.setPixels(pixels, w, h);
            }
            drawImagePreviewOnView(stopped);
        }, stopped);
        clearStatus();
    };

    private final OnSeekBarChangeListener onChangeThresholdListener = (progress, stopped) -> {
        threshold = progress;
        runOrStart(() -> {
            if (progress == 0xFF) {
                imagePreview.drawColor(Color.BLACK);
            } else if (progress == 0x00) {
                imagePreview.clearFilter();
            } else {
                final int w = imagePreview.getWidth(), h = imagePreview.getHeight(), area = w * h;
                final int[] src = imagePreview.getPixels(), dst = new int[area];
                for (int i = 0; i < area; ++i) {
                    final int pixel = src[i];
                    dst[i] = pixel & Color.BLACK | Color.rgb(
                            Color.red(pixel) / progress * progress,
                            Color.green(pixel) / progress * progress,
                            Color.blue(pixel) / progress * progress);
                }
                imagePreview.setPixels(dst, 0, w, 0, 0, w, h);
            }
            drawImagePreviewOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_threshold), progress));
    };

    private final DialogInterface.OnClickListener onApplyThresholdListener = onClickImagePreviewNBListener;

    private final View.OnClickListener onClickToleranceButtonListener = v -> {
        createImagePreview();
        new SeekBarDialog(this).setTitle(R.string.tolerance).setMin(0x00).setMax(0xFF).setProgress(threshold)
                .setOnChangeListener(onChangeThresholdListener)
                .setOnApplyListener(onApplyThresholdListener)
                .setOnCancelListener(onCancelImagePreviewListener, false)
                .show();
        onChangeThresholdListener.onChanged(threshold, true);
    };

    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        private Tab oldBackground, oldFirstFrame;

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            drawFloatingLayers();

            final int position = tab.getPosition();
            final Tab t = tabs.get(position);
            MainActivity.this.tab = t;
            final Tab background = t.getBackground(), firstFrame = background.getFirstFrame();
            final boolean isFromAnotherFrame = background != oldBackground;
            final boolean isFromAnotherProj = firstFrame != oldFirstFrame;
            oldBackground = background;
            oldFirstFrame = firstFrame;
            if (isFromAnotherFrame) {
                Tab.updateVisibilityIcons(tabs, t);
            }
            bitmap = t.bitmap;
            canvas = new Canvas(bitmap);

            final int width = bitmap.getWidth(), height = bitmap.getHeight();
            if (settings.getIndependentTranslAndScale() || isFromAnotherProj) {
                translationX = t.translationX;
                translationY = t.translationY;
                scale = t.scale;
            }
            backgroundScaledW = toScaled(background.bitmap.getWidth());
            backgroundScaledH = toScaled(background.bitmap.getHeight());

            if (transformer != null) {
                recycleTransformer();
            }
            if (isFromAnotherProj) {
                hasSelection = false;
                optimizeSelection();
            }

            if (rbCloneStamp.isChecked()) {
                cloneStampSrc = null;
            }
            updateReference();

            miHasAlpha.setChecked(bitmap.hasAlpha());
            miLayerColorMatrix.setChecked(t.filter == Tab.Filter.COLOR_MATRIX);
            miLayerCurves.setChecked(t.filter == Tab.Filter.CURVES);
            miLayerDrawBelow.setChecked(t.drawBelow);
            miLayerFilterSet.setEnabled(t.filter != null);
            miLayerHsv.setChecked(t.filter == Tab.Filter.HSV);
            miLayerLevelUp.setEnabled(t.getLevel() > 0);
            miLayerReference.setChecked(t.reference);
            checkLayerBlendModeMenuItem(t.paint.getBlendMode());

            drawBitmapOnView(true, true);
            drawChessboardOnView();
            drawGridOnView();
            drawSelectionOnView();
            eraseBitmapAndInvalidateView(previewImage, ivPreview);

            tvStatus.setText(String.format(getString(R.string.state_size), width, height));
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            final int i = tab.getPosition();
            if (i >= 0) {
                final Tab t = tabs.get(i);
                t.translationX = translationX;
                t.translationY = translationY;
                t.scale = scale;
            }
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            final int position = tab.getPosition();

            final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.move_layer)
                    .setView(R.layout.tab_layout)
                    .setNegativeButton(R.string.cancel, null)
                    .show();

            final Window window = dialog.getWindow();
            final WindowManager.LayoutParams lp = window.getAttributes();
            lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.gravity = Gravity.TOP;
            window.setAttributes(lp);

            final TabLayout tl = dialog.findViewById(R.id.tl);
            for (int i = 0; i < tabLayout.getTabCount(); ++i) {
                final TabLayout.Tab nt = tl.newTab().setCustomView(R.layout.tab);
                final View cv = nt.getCustomView();
                final Tab t = tabs.get(i);
                t.showTo(cv);
                tl.addTab(nt, i == position);
            }
            tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab t) {
                    dialog.dismiss();
                    final int p = t.getPosition();
                    final Tab selectedTab = tabs.get(position);
                    final Tab oldAdjacentLayer = selectedTab.isBackground ? Tab.getAbove(tabs, position) : Tab.getBelow(tabs, position);
                    tabs.remove(position);
                    final View cv = tab.getCustomView();
                    tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
                    tabLayout.removeTabAt(position);
                    tabLayout.addOnTabSelectedListener(onTabSelectedListener);
                    tabs.add(p, selectedTab);
                    final TabLayout.Tab nt = tabLayout.newTab()
                            .setCustomView(cv)
                            .setTag(selectedTab);
                    tabLayout.addTab(nt, p, false);
                    Tab.onTabPositionChanged(tabs, selectedTab, position, p);
                    Tab.distinguishProjects(tabs);
                    Tab.updateBackgroundIcons(tabs);
                    Tab.updateVisibilityIcons(tabs, selectedTab);

                    // Update the new frame
                    Tab.computeLayerTree(tabs, MainActivity.this.tab);
                    Tab.updateLevelIcons(tabs, MainActivity.this.tab);

                    // Update the old frame
                    if (oldAdjacentLayer != null && oldAdjacentLayer.getBackground() != selectedTab.getBackground()) {
                        Tab.computeLayerTree(tabs, oldAdjacentLayer);
                        Tab.updateLevelIcons(tabs, oldAdjacentLayer);
                    }

                    tabLayout.selectTab(nt);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab t) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab t) {
                    dialog.cancel();
                }
            });

        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchRulerHListener = new View.OnTouchListener() {
        private Guide guide;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    guide = new Guide();
                    guide.orientation = Guide.ORIENTATION_HORIZONTAL;
                    tab.guides.offerFirst(guide); // Add at the front for faster removal if necessary later
                }
                case MotionEvent.ACTION_MOVE -> {
                    guide.position = toBitmapY(event.getY() - rulerHHeight);
                    drawGridOnView();
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    final float y = event.getY();
                    if (!(rulerHHeight <= y && y < rulerHHeight + viewHeight)) {
                        tab.guides.remove(guide);
                        drawGridOnView();
                    }
                    guide = null;
                }
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchRulerVListener = new View.OnTouchListener() {
        private Guide guide;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    guide = new Guide();
                    guide.orientation = Guide.ORIENTATION_VERTICAL;
                    tab.guides.offerFirst(guide); // Add at the front for faster removal if necessary later
                }
                case MotionEvent.ACTION_MOVE -> {
                    guide.position = toBitmapX(event.getX() - rulerVWidth);
                    drawGridOnView();
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    final float x = event.getX();
                    if (!(rulerVWidth <= x && x < rulerVWidth + viewWidth)) {
                        tab.guides.remove(guide);
                        drawGridOnView();
                    }
                    guide = null;
                }
            }
            return true;
        }
    };

    private final AnimationClipper.OnApplyListener onApplyClipListener = (from, to) -> {
        final Tab firstFrame = tab.getBackground().getFirstFrame();
        Tab firstFrameKept = null;
        for (int i = tabs.size() - 1, j = 0; i >= 0; --i) {
            final Tab tab = tabs.get(i);
            if (tab.isBackground) {
                if (tab.getBackground().getFirstFrame() == firstFrame) {
                    ++j;
                } else if (j > 0) {
                    break;
                }
            }
            if (j <= 0) {
                continue;
            }
            if (from <= j && j <= to
                    || from > to && (from <= j || j <= to)) {
                if (tab.isBackground) {
                    firstFrameKept = tab;
                }
            } else {
                closeTab(i, false);
                if (tab.isFirstFrame && firstFrameKept != null) {
                    firstFrameKept.isFirstFrame = true;
                }
            }
        }
        Tab.distinguishProjects(tabs);
        final int pos = tabLayout.getSelectedTabPosition();
        selectTab(tabs.get(pos).getBackground().getFirstFrame() == firstFrameKept
                ? pos : pos + 1);
    };

    private final CellGridManager.OnApplyListener onApplyCellGridListener = this::drawGridOnView;

    private final ImageSizeManager.OnApplyListener onApplyImageSizeListener = (width, height, transform) -> {
        if (tab.isBackground) {
            final Tab firstFrame = tab.getBackground().getFirstFrame();
            for (int i = firstFrame.getBackgroundPosition(); i < tabs.size(); ++i) {
                final Tab frame = tabs.get(i).getBackground();
                if (frame.getFirstFrame() != firstFrame) {
                    break;
                }
                i = frame.getBackgroundPosition();
                resizeImage(frame, width, height, transform, null, 0, 0);
            }
        } else {
            resizeImage(tab, width, height, transform, null, 0, 0);
        }
        drawBitmapOnView(true, true);
    };

    private final RunnableRunnable runnableRunningRunner = (target, wait) -> target.run();

    private final RunnableRunnable runnableStartingRunner = (target, wait) -> {
        if (Looper.myLooper() == MAIN_LOOPER) {
            if (!thread.isAlive() || wait) {
                thread = new Thread(() -> {
                    synchronized (this) {
                        try {
                            target.run();
                        } catch (RuntimeException e) {
                        }
                    }
                });
                thread.start();
            }
        } else {
            target.run();
        }
    };

    private RunnableRunnable runnableRunner;

    private final Shape circle = new Shape() {
        @Override
        public void drawImageOnView(int x0, int y0, int x1, int y1) {
            final int radius = (int) Math.ceil(Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0)));
            MainActivity.this.drawBitmapOnView(x0 - radius, y0 - radius, x1 + radius, y1 + radius,
                    strokeWidth / 2.0f + blurRadius);
        }

        @Override
        public void drawShapeOnImage(int x0, int y0, int x1, int y1) {
            canvas.drawCircle(x0 + 0.5f, y0 + 0.5f,
                    (int) Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0)),
                    paint);
        }

        @Override
        public String drawShapeOnView(int x0, int y0, int x1, int y1) {
            final int radius = (int) Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0));
            previewCanvas.drawCircle(
                    toViewX(x0 + 0.5f), toViewY(y0 + 0.5f),
                    toScaled(radius),
                    paint);
            return String.format(getString(R.string.state_radius), radius + 0.5f);
        }
    };

    private final Shape line = new Shape() {
        @Override
        public void drawImageOnView(int x0, int y0, int x1, int y1) {
            MainActivity.this.drawBitmapOnView(x0, y0, x1, y1, strokeWidth / 2.0f + blurRadius);
        }

        @Override
        public void drawShapeOnImage(int x0, int y0, int x1, int y1) {
            if (x0 <= x1) ++x1;
            else ++x0;
            if (y0 <= y1) ++y1;
            else ++y0;
            canvas.drawLine(x0, y0, x1, y1, paint);
        }

        @Override
        public String drawShapeOnView(int x0, int y0, int x1, int y1) {
            previewCanvas.drawLine(
                    toViewX(x0 + 0.5f), toViewY(y0 + 0.5f),
                    toViewX(x1 + 0.5f), toViewY(y1 + 0.5f),
                    paint);
            return String.format(getString(R.string.state_length), Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0)) + 1);
        }
    };

    private final Shape oval = new Shape() {
        @Override
        public void drawImageOnView(int x0, int y0, int x1, int y1) {
            MainActivity.this.drawBitmapOnView(x0, y0, x1, y1, strokeWidth / 2.0f + blurRadius);
        }

        @Override
        public void drawShapeOnImage(int x0, int y0, int x1, int y1) {
            final float left = Math.min(x0, x1) + 0.5f, top = Math.min(y0, y1) + 0.5f,
                    right = Math.max(x0, x1) + 0.5f, bottom = Math.max(y0, y1) + 0.5f;
            canvas.drawOval(left, top, right, bottom, paint);
        }

        @Override
        public String drawShapeOnView(int x0, int y0, int x1, int y1) {
            previewCanvas.drawOval(
                    toViewX(x0 + 0.5f), toViewY(y0 + 0.5f),
                    toViewX(x1 + 0.5f), toViewY(y1 + 0.5f),
                    paint);
            return String.format(getString(R.string.state_axes), Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1);
        }
    };

    private final Shape rect = new Shape() {
        @Override
        public void drawImageOnView(int x0, int y0, int x1, int y1) {
            MainActivity.this.drawBitmapOnView(x0, y0, x1, y1, strokeWidth / 2.0f + blurRadius);
        }

        @Override
        public void drawShapeOnImage(int x0, int y0, int x1, int y1) {
            final float left = Math.min(x0, x1), top = Math.min(y0, y1),
                    right = Math.max(x0, x1) + 0.5f, bottom = Math.max(y0, y1) + 0.5f;
            canvas.drawRect(left, top, right, bottom, paint);
        }

        @Override
        public String drawShapeOnView(int x0, int y0, int x1, int y1) {
            previewCanvas.drawRect(
                    toViewX(x0 + 0.5f), toViewY(y0 + 0.5f),
                    toViewX(x1 + 0.5f), toViewY(y1 + 0.5f),
                    paint);
            return String.format(getString(R.string.state_size), Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1);
        }
    };

    private Shape shape = rect;

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithBrushListener = new View.OnTouchListener() {
        private Path path;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    path = new Path();
                    path.moveTo(bx, by);
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                }
                case MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    path.lineTo(bx, by);
                    canvas.drawPath(path, paint);
                    drawBitmapOnView();
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    path = null;
                    addToHistory();
                    clearStatus();
                }
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchIVWithBucketListener = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                final float x = event.getX(), y = event.getY();
                final int bx = toBitmapX(x), by = toBitmapY(y);
                if (!(0 <= bx && bx < bitmap.getWidth() && 0 <= by && by < bitmap.getHeight())) {
                    break;
                }
                final Bitmap src = refBm != null ? refBm : bitmap;
                final Rect rect = hasSelection ? selection : null;
                runOrStart(() -> {
                    if (cbBucketFillContiguous.isChecked()) {
                        BitmapUtils.floodFill(src, bitmap, rect, bx, by, paint.getColor(),
                                cbBucketFillIgnoreAlpha.isChecked(), threshold);
                    } else {
                        BitmapUtils.bucketFill(src, bitmap, rect, bx, by, paint.getColor(),
                                cbBucketFillIgnoreAlpha.isChecked(), threshold);
                    }
                    if (rect == null) {
                        drawBitmapOnView(true);
                    } else {
                        drawBitmapOnView(rect, true);
                    }
                    addToHistory();
                }, true);
                clearStatus();
            }
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onTouchIVWithCloneStampListener = new View.OnTouchListener() {
        private float lastX, lastY;
        private int dx, dy;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final float x = event.getX(), y = event.getY();
            final int bx = toBitmapX(x), by = toBitmapY(y);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (cloneStampSrc == null) {
                        break;
                    }
                    dx = cloneStampSrc.x - bx;
                    dy = cloneStampSrc.y - by;
                    lastX = x;
                    lastY = y;

                case MotionEvent.ACTION_MOVE: {
                    if (cloneStampSrc == null) {
                        break;
                    }
                    final int lastBX = toBitmapX(lastX), lastBY = toBitmapY(lastY);

                    final int width = (int) (Math.abs(bx - lastBX) + strokeWidth + blurRadius * 2.0f),
                            height = (int) (Math.abs(by - lastBY) + strokeWidth + blurRadius * 2.0f);
                    final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    final float rad = strokeWidth / 2.0f + blurRadius;
                    final float left = Math.min(lastBX, bx) - rad, top = Math.min(lastBY, by) - rad;
                    final int l = (int) (left + dx), t = (int) (top + dy);
                    final Canvas cv = new Canvas(bm);
                    cv.drawLine(lastBX - left, lastBY - top, bx - left, by - top, paint);
                    cv.drawRect(0.0f, 0.0f, -l, height, PAINT_CLEAR);
                    cv.drawRect(0.0f, 0.0f, width, -t, PAINT_CLEAR);
                    cv.drawRect(bitmap.getWidth() - l, 0.0f, width, height, PAINT_CLEAR);
                    cv.drawRect(0.0f, bitmap.getHeight() - t, width, height, PAINT_CLEAR);
                    cv.drawBitmap(bitmap,
                            new Rect(l, t, l + width, t + height),
                            new RectF(0.0f, 0.0f, width, height),
                            PAINT_SRC_IN);
                    canvas.drawBitmap(bm, left, top, PAINT_SRC_OVER);
                    bm.recycle();
                    drawBitmapOnView((int) left, (int) top, (int) (left + width), (int) (top + height));
                    drawCrossOnView(bx + dx, by + dy);
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));

                    lastX = x;
                    lastY = y;
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (cloneStampSrc == null) {
                        cloneStampSrc = new Point(bx, by);
                        drawCrossOnView(bx, by);
                        tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                    } else {
                        drawCrossOnView(cloneStampSrc.x, cloneStampSrc.y);
                        addToHistory();
                        clearStatus();
                    }
                    break;
            }
            return true;
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithEraserListener = new View.OnTouchListener() {
        private float lastX, lastY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    final float x = event.getX(), y = event.getY();
                    lastX = x;
                    lastY = y;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX(), y = event.getY();
                    final int lastBX = toBitmapX(lastX), lastBY = toBitmapY(lastY);
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    drawLineOnCanvas(lastBX, lastBY, bx, by, eraser);
                    drawBitmapOnView(lastBX, lastBY, bx, by, strokeHalfWidthEraser + blurRadiusEraser);
                    tvStatus.setText(String.format(MainActivity.this.getString(R.string.coordinates), bx, by));
                    lastX = x;
                    lastY = y;
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    addToHistory();
                    clearStatus();
                    break;
            }
            return true;
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithImpreciseEyedropperListener = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                final float x = event.getX(), y = event.getY();
                final int bx = satX(bitmap, toBitmapX(x)), by = satY(bitmap, toBitmapY(y));
                final int color = rbEyedropperAllLayers.isChecked()
                        ? viewImage.getPixel((int) x, (int) y) : bitmap.getPixel(bx, by);
                paint.setColor(color);
                vForegroundColor.setBackgroundColor(color);
                tvStatus.setText(String.format(
                        String.format(getString(R.string.state_eyedropper_imprecise), settings.getArgbComponentFormat()),
                        bx, by,
                        Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color)));
            }
            case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> clearStatus();
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithPreciseEyedropperListener = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                final float x = event.getX(), y = event.getY();
                final int bx = satX(bitmap, toBitmapX(x)), by = satY(bitmap, toBitmapY(y));
                final android.graphics.Color color = rbEyedropperAllLayers.isChecked()
                        ? viewImage.getColor((int) x, (int) y) : bitmap.getColor(bx, by);
                paint.setColor(color.pack());
                vForegroundColor.setBackgroundColor(color.toArgb());
                tvStatus.setText(String.format(
                        getString(R.string.state_eyedropper_precise),
                        bx, by, String.valueOf(color.alpha()),
                        String.valueOf(color.red()), String.valueOf(color.green()), String.valueOf(color.blue())));
            }
            case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> clearStatus();
        }
        return true;
    };

    private View.OnTouchListener onTouchIVWithEyedropperListener;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onTouchIVWithGradientListener = new View.OnTouchListener() {
        @ColorLong
        private long color0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final float x = event.getX(), y = event.getY();
            final int bx = MainActivity.this.toBitmapX(x), by = MainActivity.this.toBitmapY(y);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    paint.setStrokeWidth(MainActivity.this.toScaled((int) paint.getStrokeWidth()));
                    if (isShapeStopped) {
                        isShapeStopped = false;
                        drawPointOnView(bx, by);
                        shapeStartX = bx;
                        shapeStartY = by;
                        color0 = bitmap.getColor(satX(bitmap, bx), satY(bitmap, by)).pack();
                        tvStatus.setText(String.format(MainActivity.this.getString(R.string.coordinates), bx, by));
                        break;
                    }
                }
                case MotionEvent.ACTION_MOVE: {
                    final float startVX = toViewX(shapeStartX + 0.5f), startVY = toViewY(shapeStartY + 0.5f),
                            stopX = toViewX(bx + 0.5f), stopY = toViewY(by + 0.5f);
                    paint.setShader(new LinearGradient(startVX, startVY, stopX, stopY,
                            color0,
                            bitmap.getColor(satX(bitmap, bx), satY(bitmap, by)).pack(),
                            Shader.TileMode.CLAMP));
                    eraseBitmap(previewImage);
                    previewCanvas.drawLine(startVX, startVY, stopX, stopY, paint);
                    ivPreview.invalidate();
                    tvStatus.setText(String.format(getString(R.string.state_start_stop),
                            shapeStartX, shapeStartY, bx, by));
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    paint.setStrokeWidth(strokeWidth);
                    if (bx != shapeStartX || by != shapeStartY) {
                        paint.setShader(new LinearGradient(shapeStartX, shapeStartY, bx, by,
                                color0,
                                bitmap.getColor(satX(bitmap, bx), satY(bitmap, by)).pack(),
                                Shader.TileMode.CLAMP));
                        drawLineOnCanvas(shapeStartX, shapeStartY, bx, by, paint);
                        isShapeStopped = true;
                        drawBitmapOnView(shapeStartX, shapeStartY, bx, by, strokeWidth / 2.0f + blurRadius);
                        eraseBitmapAndInvalidateView(previewImage, ivPreview);
                        addToHistory();
                        clearStatus();
                    }
                    paint.setShader(null);
                    break;
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchIVWithImpreciseMagicEraserListener = new View.OnTouchListener() {
        private float lastX, lastY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final float x = event.getX(), y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    lastX = x;
                    lastY = y;
                }
                case MotionEvent.ACTION_MOVE -> {
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    final int lastBX = toBitmapX(lastX), lastBY = toBitmapY(lastY);
                    final int rad = (int) (strokeWidth / 2.0f + blurRadius);
                    final float radF = toScaled(rad);
                    final double theta = Math.atan2(y - lastY, x - lastX);
                    final int colorLeft = refBm.getPixel(
                            satX(refBm, toBitmapX(x + radF * (float) Math.sin(theta))),
                            satY(refBm, toBitmapY(y - radF * (float) Math.cos(theta))));
                    final int colorRight = refBm.getPixel(
                            satX(refBm, toBitmapX(x - radF * (float) Math.sin(theta))),
                            satY(refBm, toBitmapY(y + radF * (float) Math.cos(theta))));
                    final int backgroundColor = cbMagicEraserPosition == rbMagicEraserLeft ? colorLeft : colorRight;
                    final int foregroundColor = backgroundColor == colorLeft ? colorRight : colorLeft;

                    final int left = Math.min(lastBX, bx) - rad, top = Math.min(lastBY, by) - rad,
                            right = Math.max(lastBX, bx) + rad + 1, bottom = Math.max(lastBY, by) + rad + 1;
                    final int width = right - left, height = bottom - top;
                    final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    final Canvas cLine = new Canvas(bLine);
                    cLine.drawLine(lastBX - left, lastBY - top,
                            bx - left, by - top,
                            paint);
                    canvas.drawBitmap(bLine, left, top, PAINT_DST_OUT);
                    final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    new Canvas(bm).drawBitmap(refBm,
                            new Rect(left, top, right, bottom),
                            new Rect(0, 0, width, height),
                            PAINT_SRC);
                    BitmapUtils.removeBackground(bm, foregroundColor, backgroundColor);
                    cLine.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC_IN);
                    bm.recycle();
                    canvas.drawBitmap(bLine, left, top, PAINT_SRC_OVER);
                    bLine.recycle();

                    drawBitmapOnView(left, top, right, bottom);
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                    lastX = x;
                    lastY = y;
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    addToHistory();
                    clearStatus();
                }
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchIVWithPreciseMagicEraserListener = (v, event) -> {
        switch (event.getPointerCount()) {
            case 1 -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN -> {
                        final float x = event.getX(), y = event.getY();
                        final Rect vs = getVisibleSubset();
                        if (magErB == null || magErF == null
                                || (!vs.contains(magErB.x, magErB.y) && !vs.contains(magErF.x, magErF.y))) {
                            final int bx = toBitmapX(x), by = toBitmapY(y);
                            magErB = new Point(bx, by);
                            magErF = new Point(bx, by);
                            drawCrossOnView(bx, by);
                        }
                    }
                    case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> addToHistory();
                }
            }
            case 2 -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE -> {
                        final float x0 = event.getX(0), y0 = event.getY(0);
                        final float x1 = event.getX(1), y1 = event.getY(1);
                        magErB.set(toBitmapX(x0 + magErBD.x), toBitmapY(y0 + magErBD.y));
                        magErF.set(toBitmapX(x1 + magErFD.x), toBitmapY(y1 + magErFD.y));
                        drawCrossOnView(magErB.x, magErB.y, true);
                        drawCrossOnView(magErF.x, magErF.y, false);

                        if (!cbMagErAccEnabled.isChecked()) {
                            break;
                        }

                        final int rad = (int) (strokeWidth / 2.0f + blurRadius);
                        final int backgroundColor = refBm.getPixel(
                                satX(refBm, magErB.x), satY(refBm, magErB.y));
                        final int foregroundColor = refBm.getPixel(
                                satX(refBm, magErF.x), satY(refBm, magErF.y));

                        final int left = Math.min(magErB.x, magErF.x) - rad,
                                top = Math.min(magErB.y, magErF.y) - rad,
                                right = Math.max(magErB.x, magErF.x) + rad + 1,
                                bottom = Math.max(magErB.y, magErF.y) + rad + 1;
                        final int width = right - left, height = bottom - top;
                        final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        final Canvas cLine = new Canvas(bLine);
                        cLine.drawLine(magErB.x - left, magErB.y - top,
                                magErF.x - left, magErF.y - top,
                                paint);
                        canvas.drawBitmap(bLine, left, top, PAINT_DST_OUT);
                        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        new Canvas(bm).drawBitmap(refBm,
                                new Rect(left, top, right, bottom),
                                new Rect(0, 0, width, height),
                                PAINT_SRC);
                        BitmapUtils.removeBackground(bm, foregroundColor, backgroundColor);
                        cLine.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC_IN);
                        bm.recycle();
                        canvas.drawBitmap(bLine, left, top, PAINT_SRC_OVER);
                        bLine.recycle();

                        drawBitmapOnView(left, top, right, bottom);
                    }
                    case MotionEvent.ACTION_POINTER_DOWN -> {
                        final float x0 = event.getX(0), y0 = event.getY(0);
                        final float x1 = event.getX(1), y1 = event.getY(1);
                        magErBD.set(toViewX(magErB.x) - x0, toViewY(magErB.y) - y0);
                        magErFD.set(toViewX(magErF.x) - x1, toViewY(magErF.y) - y1);
                    }
                }
            }
        }
        return true;
    };

    private View.OnTouchListener onTouchIVWithMagicEraserListener = onTouchIVWithImpreciseMagicEraserListener;

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithMagicPaintListener = new View.OnTouchListener() {
        private float lastX, lastY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    final float x = event.getX(), y = event.getY();
                    lastX = x;
                    lastY = y;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    final int lastBX = toBitmapX(lastX), lastBY = toBitmapY(lastY);

                    final int rad = (int) (strokeWidth / 2.0f + blurRadius);
                    final int left = Math.min(lastBX, bx) - rad,
                            top = Math.min(lastBY, by) - rad,
                            right = Math.max(lastBX, bx) + rad + 1,
                            bottom = Math.max(lastBY, by) + rad + 1;
                    final int width = right - left, height = bottom - top;
                    final int relativeX = bx - left, relativeY = by - top;
                    final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    final Canvas cLine = new Canvas(bLine);
                    cLine.drawLine(lastBX - left, lastBY - top,
                            relativeX, relativeY,
                            paint);
                    if (threshold < 0xFF) {
                        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        final Canvas cv = new Canvas(bm);
                        cv.drawBitmap(bLine, 0.0f, 0.0f, PAINT_SRC);
                        final Rect absolute = new Rect(left, top, right, bottom),
                                relative = new Rect(0, 0, width, height);
                        cv.drawBitmap(refBm, absolute, relative, PAINT_SRC_IN);
                        final Bitmap bThr = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444); // Threshold
                        BitmapUtils.floodFill(bm, bThr, hasSelection ? selection : null,
                                relativeX, relativeY, Color.BLACK, true, threshold);
                        bm.recycle();
                        cLine.drawBitmap(bThr, 0.0f, 0.0f, PAINT_DST_IN);
                        bThr.recycle();
                    }
                    canvas.drawBitmap(bLine, left, top, magicPaint);
                    bLine.recycle();

                    drawBitmapOnView(left, top, right, bottom);
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                    lastX = x;
                    lastY = y;
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    addToHistory();
                    clearStatus();
                    break;
            }
            return true;
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithMarqueeListener = new View.OnTouchListener() {
        private boolean hasDraggedBound = false;
        private int startX, startY, stopX, stopY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    final float x = event.getX(), y = event.getY();
                    if (marqueeBoundBeingDragged == null) {
                        if (hasSelection && checkDraggingMarqueeBound(x, y) != null) {
                            tvStatus.setText(String.format(getString(R.string.state_selected_bound),
                                    getString(marqueeBoundBeingDragged.name)));
                        } else {
                            if (hasSelection && selection.width() == 1 && selection.height() == 1) {
                                stopX = toBitmapX(x) + 1;
                                stopY = toBitmapY(y) + 1;
                            } else {
                                hasSelection = true;
                                startX = toBitmapX(x);
                                startY = toBitmapY(y);
                                stopX = startX + 1;
                                stopY = startY + 1;
                            }
                            setSelection(startX, startY, stopX, stopY);
                            tvStatus.setText(String.format(getString(R.string.state_from_to_size_1),
                                    startX, startY, startX, startY));
                        }
                    } else {
                        hasDraggedBound |= dragMarqueeBound(x, y);
                        drawSelectionOnView();
                        tvStatus.setText(String.format(getString(R.string.state_l_t_r_b_size),
                                selection.left, selection.top, selection.right - 1, selection.bottom - 1,
                                selection.width(), selection.height()));
                    }
                }
                case MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    if (marqueeBoundBeingDragged == null) {
                        stopX = toBitmapX(x) + 1;
                        stopY = toBitmapY(y) + 1;
                        setSelection(startX, startY, stopX, stopY);
                    } else {
                        hasDraggedBound |= dragMarqueeBound(x, y);
                        drawSelectionOnView();
                    }
                    tvStatus.setText(String.format(getString(R.string.state_l_t_r_b_size),
                            selection.left, selection.top, selection.right - 1, selection.bottom - 1,
                            selection.width(), selection.height()));
                }
                case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    optimizeSelection();
                    drawSelectionOnView();
                    if (marqueeBoundBeingDragged != null) {
                        if (hasDraggedBound) {
                            marqueeBoundBeingDragged = null;
                            hasDraggedBound = false;
                            tvStatus.setText(hasSelection ?
                                    String.format(getString(R.string.state_l_t_r_b_size),
                                            selection.left, selection.top, selection.right - 1, selection.bottom - 1,
                                            selection.width(), selection.height()) :
                                    "");
                        }
                    } else {
                        tvStatus.setText(hasSelection ?
                                String.format(getString(R.string.state_l_t_r_b_size),
                                        selection.left, selection.top, selection.right - 1, selection.bottom - 1,
                                        selection.width(), selection.height()) :
                                "");
                    }
                }
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchIVWithPatcherListener = (v, event) -> {
        if (!hasSelection) {
            return true;
        }
        final float radius = strokeWidth / 2.0f + blurRadius;
        if (selection.left + radius * 2.0f >= selection.right || selection.top + radius * 2.0f >= selection.bottom) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                createImagePreview();

            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int bx = toBitmapX(x), by = toBitmapY(y);
                final int w = selection.width(), h = selection.height();
                final Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                final Canvas cv = new Canvas(bm);
                final int wh = w >> 1, hh = h >> 1; // h - Half
                cv.drawBitmap(bitmap,
                        new Rect(bx - wh, by - hh, bx + w - wh, by + h - hh),
                        new Rect(0, 0, w, h),
                        PAINT_SRC);
                final Bitmap rect = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                new Canvas(rect).drawRect(radius, radius, w - radius, h - radius, paint);
                cv.drawBitmap(rect, 0.0f, 0.0f, patcher);
                rect.recycle();
                imagePreview.reset();
                imagePreview.drawBitmap(bm);
                bm.recycle();
                drawBitmapOnView(imagePreview.getEntire(), selection);
                tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                drawImagePreviewOnImage();
                imagePreview.recycle();
                imagePreview = null;
                addToHistory();
                clearStatus();
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithPathListener = new View.OnTouchListener() {
        private Path path, previewPath;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    path = new Path();
                    path.moveTo(bx, by);
                    previewPath = new Path();
                    previewPath.moveTo(x, y);
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                }
                case MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    path.lineTo(bx, by);
                    previewPath.lineTo(x, y);
                    eraseBitmap(previewImage);
                    previewCanvas.drawPath(previewPath, selector);
                    ivPreview.invalidate();
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    canvas.drawPath(path, paint);
                    final RectF bounds = new RectF();
                    path.computeBounds(bounds, false);
                    drawBitmapOnView((int) Math.floor(bounds.left), (int) Math.floor(bounds.top),
                            (int) Math.ceil(bounds.right), (int) Math.ceil(bounds.bottom),
                            strokeWidth / 2.0f + blurRadius);
                    eraseBitmapAndInvalidateView(previewImage, ivPreview);
                    addToHistory();
                    clearStatus();
                    path = null;
                    previewPath = null;
                }
            }
            return true;
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithPencilListener = new View.OnTouchListener() {
        private float lastX, lastY;
        private Paint pencil;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    final float x = event.getX(), y = event.getY();
                    pencil = cbPencilWithEraser.isChecked()
                            && bitmap.getColor(satX(bitmap, toBitmapX(x)), satY(bitmap, toBitmapY(y))).pack() != eraser.getColorLong()
                            ? eraser : paint;
                    lastX = x;
                    lastY = y;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX(), y = event.getY();
                    final int lastBX = toBitmapX(lastX), lastBY = toBitmapY(lastY);
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    drawLineOnCanvas(lastBX, lastBY, bx, by, pencil);
                    drawBitmapOnView(lastBX, lastBY, bx, by, strokeWidth / 2.0f + blurRadius);
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                    lastX = x;
                    lastY = y;
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    addToHistory();
                    clearStatus();
                    break;
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchIVWithRulerListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        final float halfScale = scale / 2.0f;
        final int bx = toBitmapX(x + halfScale), by = toBitmapY(y + halfScale);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isShapeStopped) {
                    isShapeStopped = false;
                    ruler.enabled = false;
                    eraseBitmapAndInvalidateView(previewImage, ivPreview);
                    drawPointOnView(bx, by);
                    shapeStartX = bx;
                    shapeStartY = by;
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                    break;
                }
                // Fall through
            case MotionEvent.ACTION_MOVE:
                if (bx != shapeStartX || by != shapeStartY) {
                    isShapeStopped = true;
                    ruler.set(shapeStartX, shapeStartY, bx, by);
                    ruler.enabled = true;
                    drawRulerOnView();
                    final int dx = ruler.stopX - ruler.startX, dy = ruler.stopY - ruler.startY;
                    tvStatus.setText(String.format(getString(R.string.state_ruler),
                            ruler.startX, ruler.startY, ruler.stopX, ruler.stopY, dx, dy,
                            String.valueOf((float) Math.sqrt(dx * dx + dy * dy))));
                }
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onTouchIVWithShapeListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        final int bx = toBitmapX(x), by = toBitmapY(y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                paint.setStrokeWidth(toScaled((int) paint.getStrokeWidth()));
                if (isShapeStopped) {
                    isShapeStopped = false;
                    drawPointOnView(bx, by);
                    shapeStartX = bx;
                    shapeStartY = by;
                    tvStatus.setText(String.format(getString(R.string.coordinates), bx, by));
                    break;
                }
            }
            case MotionEvent.ACTION_MOVE: {
                final String result = drawShapeOnView(shapeStartX, shapeStartY, bx, by);
                tvStatus.setText(
                        String.format(getString(R.string.state_start_stop_),
                                shapeStartX, shapeStartY, bx, by)
                                + result);
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                paint.setStrokeWidth(strokeWidth);
                if (bx != shapeStartX || by != shapeStartY) {
                    drawShapeOnCanvas(shapeStartX, shapeStartY, bx, by);
                    isShapeStopped = true;
                    shape.drawImageOnView(shapeStartX, shapeStartY, bx, by);
                    eraseBitmapAndInvalidateView(previewImage, ivPreview);
                    addToHistory();
                    clearStatus();
                }
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchIVWithTextListener = new View.OnTouchListener() {
        private float dx, dy;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (isEditingText) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN -> {
                        final float x = event.getX(), y = event.getY();
                        dx = x - toScaled(textX);
                        dy = y - toScaled(textY);
                        drawTextOnView();
                    }
                    case MotionEvent.ACTION_MOVE -> {
                        final float x = event.getX(), y = event.getY();
                        textX = toUnscaled(x - dx);
                        textY = toUnscaled(y - dy);
                        drawTextOnView();
                    }
                }

            } else {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN -> {
                        final float x = event.getX(), y = event.getY();
                        textX = toBitmapX(x);
                        textY = toBitmapY(y);
                        llOptionsText.setVisibility(View.VISIBLE);
                        isEditingText = true;
                        drawTextOnView();
                        dx = toViewX(0);
                        dy = toViewY(0);
                    }
                }
            }
            return true;
        }
    };

    /**
     * Callback to call on touch image view with poly transformer
     */
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onTouchIVWithPTListener = new View.OnTouchListener() {
        private float[] src, dst, bmSrc, bmDst;
        private int pointCount = 0;
        private Matrix matrix;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!hasSelection) {
                return true;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN -> {
                    if (selection.isEmpty()) {
                        break;
                    }
                    if (transformer == null) {
                        createTransformer();
                    }
                    src = new float[8];
                    dst = new float[8];
                    bmSrc = new float[8];
                    bmDst = new float[8];
                    pointCount = 1;
                    final float x = event.getX(), y = event.getY();
                    src[0] = x;
                    src[1] = y;
                    bmSrc[0] = toBitmapX(x) - selection.left;
                    bmSrc[1] = toBitmapY(y) - selection.top;
                    matrix = new Matrix();
                    clearStatus();
                }
                case MotionEvent.ACTION_POINTER_DOWN -> {
                    if (transformer == null) {
                        break;
                    }
                    final int pointerCount = event.getPointerCount();
                    if (pointerCount > 4) {
                        break;
                    }
                    pointCount = pointerCount;
                    final int index = event.getActionIndex();
                    final float x = event.getX(index), y = event.getY(index);
                    src[index * 2] = x;
                    src[index * 2 + 1] = y;
                    bmSrc[index * 2] = toBitmapX(x) - selection.left;
                    bmSrc[index * 2 + 1] = toBitmapY(y) - selection.top;
                }
                case MotionEvent.ACTION_MOVE -> {
                    if (transformer == null) {
                        break;
                    }
                    pointCount = Math.min(event.getPointerCount(), 4);
                    for (int i = 0; i < pointCount; ++i) {
                        final float x = event.getX(i), y = event.getY(i);
                        dst[i * 2] = x;
                        dst[i * 2 + 1] = y;
                        bmDst[i * 2] = toBitmapX(x) - selection.left;
                        bmDst[i * 2 + 1] = toBitmapY(y) - selection.top;
                    }
                    matrix.setPolyToPoly(src, 0, dst, 0, pointCount);
                    ivSelection.setImageMatrix(matrix);
                    drawSelectionOnView(false);
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (transformer == null) {
                        break;
                    }
                    matrix = null;
                    src = null;
                    dst = null;
                    final Matrix bmMatrix = new Matrix();
                    bmMatrix.setPolyToPoly(bmSrc, 0, bmDst, 0, pointCount);
                    bmSrc = null;
                    bmDst = null;
                    pointCount = 0;
                    ivSelection.setImageMatrix(null);
                    final RectF r = transformer.transform(bmMatrix, cbTransformerFilter.isChecked(), antiAlias);
                    if (r != null) {
                        final int w_ = transformer.getWidth(), h_ = transformer.getHeight();
                        selection.left += r.left;
                        selection.top += r.top;
                        selection.right = selection.left + w_;
                        selection.bottom = selection.top + h_;
                        drawBitmapOnView(true);
                    }
                    drawSelectionOnView();
                    clearStatus();
                }
            }
            return true;
        }
    };

    /**
     * Callback to call on touch image view with rotation transformer
     */
    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithRTListener = new View.OnTouchListener() {
        private double lastTheta;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!hasSelection) {
                return true;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    if (selection.isEmpty()) {
                        break;
                    }
                    final float x = event.getX(), y = event.getY();
                    if (transformer == null) {
                        createTransformer();
                    }
                    ivSelection.setPivotX(toViewX(selection.exactCenterX()));
                    ivSelection.setPivotY(toViewY(selection.exactCenterY()));
                    lastTheta = (float) Math.atan2(y - ivSelection.getPivotY(), x - ivSelection.getPivotX());
                    clearStatus();
                }
                case MotionEvent.ACTION_MOVE -> {
                    if (transformer == null) {
                        break;
                    }
                    final float x = event.getX(), y = event.getY();
                    final float degrees = (float) Math.toDegrees(Math.atan2(y - ivSelection.getPivotY(), x - ivSelection.getPivotX()) - lastTheta);
                    ivSelection.setRotation(degrees);
                    drawSelectionOnView();
                    tvStatus.setText(String.format(getString(R.string.degrees_), degrees));
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (transformer == null) {
                        break;
                    }
                    final int w = transformer.getWidth(), h = transformer.getHeight();
                    transformer.rotate(ivSelection.getRotation(), cbTransformerFilter.isChecked(), antiAlias);
                    ivSelection.setRotation(0.0f);
                    final int w_ = transformer.getWidth(), h_ = transformer.getHeight();
                    selection.left += w - w_ >> 1;
                    selection.top += h - h_ >> 1;
                    selection.right = selection.left + w_;
                    selection.bottom = selection.top + h_;
                    drawBitmapOnView(selection, true);
                    drawSelectionOnView();
                    clearStatus();
                }
            }
            return true;
        }
    };

    /**
     * Callback to call on touch image view with scale transformer
     */
    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithSTListener = new View.OnTouchListener() {
        private boolean hasDraggedBound = false;
        private float dlpbLeft, dlpbTop, dlpbRight, dlpbBottom; // Distances from last point to bound

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!hasSelection) {
                return true;
            }
            switch (event.getPointerCount()) {
                case 1 -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN -> {
                            if (selection.isEmpty()) {
                                break;
                            }
                            final float x = event.getX(), y = event.getY();
                            if (transformer == null) {
                                createTransformer();
                            }
                            drawSelectionOnView(false);
                            if (marqueeBoundBeingDragged == null) {
                                if (checkDraggingMarqueeBound(x, y) != null) {
                                    if (cbTransformerLar.isChecked()) {
                                        transformer.calculateByLocation();
                                    }
                                    tvStatus.setText(String.format(getString(R.string.state_selected_bound),
                                            getString(marqueeBoundBeingDragged.name)));
                                }
                            } else {
                                hasDraggedBound |= stretchByDraggedMarqueeBound(x, y);
                                tvStatus.setText(String.format(getString(R.string.state_left_top),
                                        selection.left, selection.top));
                            }
                        }
                        case MotionEvent.ACTION_MOVE -> {
                            if (transformer == null) {
                                break;
                            }
                            final float x = event.getX(), y = event.getY();
                            if (marqueeBoundBeingDragged != null) {
                                hasDraggedBound |= stretchByDraggedMarqueeBound(x, y);
                                tvStatus.setText(String.format(getString(R.string.state_size),
                                        selection.width(), selection.height()));
                            }
                        }
                        case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (transformer == null) {
                                break;
                            }
                            if (marqueeBoundBeingDragged != null && hasDraggedBound) {
                                marqueeBoundBeingDragged = null;
                                hasDraggedBound = false;
                                final int w = selection.width(), h = selection.height();
                                if (w > 0 && h > 0) {
                                    transformer.stretch(selection.width(), selection.height(),
                                            cbTransformerFilter.isChecked(), antiAlias);
                                    selection.sort();
                                } else {
                                    selection.right = selection.left + transformer.getWidth();
                                    selection.bottom = selection.top + transformer.getHeight();
                                }
                                drawBitmapOnView(true);
                                drawSelectionOnView(false);
                            }
                        }
                    }
                }
                case 2 -> {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_MOVE -> {
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            float marqLOnView = toViewX(selection.left), marqTOnView = toViewY(selection.top),
                                    marqROnView = toViewX(selection.right), marqBOnView = toViewY(selection.bottom);
                            final float dpbLeft = Math.min(x0 - marqLOnView, x1 - marqLOnView),
                                    dpbTop = Math.min(y0 - marqTOnView, y1 - marqTOnView),
                                    dpbRight = Math.min(marqROnView - x0, marqROnView - x1),
                                    dpbBottom = Math.min(marqBOnView - y0, marqBOnView - y1); // Distances from point to bound
                            final float dpbDiffL = dlpbLeft - dpbLeft, dpbDiffT = dlpbTop - dpbTop,
                                    dpbDiffR = dlpbRight - dpbRight, dpbDiffB = dlpbBottom - dpbBottom;
                            if (cbTransformerLar.isChecked()) {
                                if (Math.abs(dpbDiffL) + Math.abs(dpbDiffR) >= Math.abs(dpbDiffT) + Math.abs(dpbDiffB)) {
                                    selection.left -= toUnscaled(dpbDiffL);
                                    selection.right += toUnscaled(dpbDiffR);
                                    final double width = selection.width(), height = width / transformer.getAspectRatio();
                                    selection.top = (int) (transformer.getCenterY() - height / 2.0);
                                    selection.bottom = (int) (transformer.getCenterY() + height / 2.0);
                                    marqTOnView = toViewY(selection.top);
                                    marqBOnView = toViewY(selection.bottom);
                                    dlpbTop = Math.min(y0 - marqTOnView, y1 - marqTOnView);
                                    dlpbBottom = Math.min(marqBOnView - y0, marqBOnView - y1);
                                } else {
                                    selection.top -= toUnscaled(dpbDiffT);
                                    selection.bottom += toUnscaled(dpbDiffB);
                                    final double height = selection.height(), width = height * transformer.getAspectRatio();
                                    selection.left = (int) (transformer.getCenterX() - width / 2.0);
                                    selection.right = (int) (transformer.getCenterX() + width / 2.0);
                                    marqLOnView = toViewX(selection.left);
                                    marqROnView = toViewX(selection.right);
                                    dlpbLeft = Math.min(x0 - marqLOnView, x1 - marqLOnView);
                                    dlpbRight = Math.min(marqROnView - x0, marqROnView - x1);
                                }
                            } else {
                                selection.left -= toUnscaled(dpbDiffL);
                                selection.top -= toUnscaled(dpbDiffT);
                                selection.right += toUnscaled(dpbDiffR);
                                selection.bottom += toUnscaled(dpbDiffB);
                            }
                            drawSelectionOnView();
                            tvStatus.setText(String.format(getString(R.string.state_size),
                                    selection.width(), selection.height()));
                        }
                        case MotionEvent.ACTION_POINTER_DOWN -> {
                            marqueeBoundBeingDragged = null;
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            final RectF viewSelection = new RectF(
                                    toViewX(selection.left), toViewY(selection.top),
                                    toViewX(selection.right), toViewY(selection.bottom));
                            dlpbLeft = Math.min(x0 - viewSelection.left, x1 - viewSelection.left);
                            dlpbTop = Math.min(y0 - viewSelection.top, y1 - viewSelection.top);
                            dlpbRight = Math.min(viewSelection.right - x0, viewSelection.right - x1);
                            dlpbBottom = Math.min(viewSelection.bottom - y0, viewSelection.bottom - y1);
                            if (cbTransformerLar.isChecked()) {
                                transformer.calculateByLocation();
                            }
                            tvStatus.setText(String.format(getString(R.string.state_size),
                                    selection.width(), selection.height()));
                        }
                        case MotionEvent.ACTION_POINTER_UP -> {
                            transformer.stretch(selection.width(), selection.height(),
                                    cbTransformerFilter.isChecked(), antiAlias);
                            selection.sort();
                            drawBitmapOnView(true);
                            drawSelectionOnView();
                        }
                    }
                }
            }
            return true;
        }
    };

    /**
     * Callback to call on touch image view with translation transformer
     */
    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithTTListener = new View.OnTouchListener() {
        private float dx, dy;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    final float x = event.getX(), y = event.getY();
                    if (hasSelection) {
                        if (transformer == null) {
                            createTransformer();
                        }
                        drawSelectionOnView(false);
                        tvStatus.setText(String.format(getString(R.string.state_left_top),
                                selection.left, selection.top));
                        dx = x - toViewX(selection.left);
                        dy = y - toViewY(selection.top);
                    } else {
                        tvStatus.setText(String.format(getString(R.string.state_left_top),
                                tab.left, tab.top));
                        dx = x - toViewX(0);
                        dy = y - toViewY(0);
                    }
                }
                case MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    if (transformer != null) {
                        selection.offsetTo(toBitmapX(x - dx), toBitmapY(y - dy));
                        drawSelectionOnView(true);
                        tvStatus.setText(String.format(getString(R.string.state_left_top),
                                selection.left, selection.top));
                    } else {
                        tab.left = toBitmapXAbs(x - dx);
                        tab.top = toBitmapYAbs(y - dy);
                        drawChessboardOnView();
                        drawGridOnView();
                        tvStatus.setText(String.format(getString(R.string.state_left_top),
                                tab.left, tab.top));
                    }
                    drawBitmapOnView();
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (transformer != null) {
                        drawSelectionOnView(false);
                    }
                    drawBitmapOnView(true);
                }
            }
            return true;
        }
    };

    private View.OnTouchListener onTouchIVWithTransformerListener = onTouchIVWithTTListener;

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onTouchIVWithZoomToolListener = new View.OnTouchListener() {
        private float dx, dy;
        private float lastPivotX, lastPivotY;
        private double lastDiagonal;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getPointerCount()) {
                case 1 -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN -> {
                            final float x = event.getX(), y = event.getY();
                            tvStatus.setText(String.format(getString(R.string.coordinates),
                                    toBitmapX(x), toBitmapY(y)));
                            if (lastMerged == null) {
                                mergeLayersEntire();
                            }
                            dx = x;
                            dy = y;
                        }
                        case MotionEvent.ACTION_MOVE -> {
                            final float x = event.getX(), y = event.getY();
                            final float deltaX = x - dx, deltaY = y - dy;
                            translationX += deltaX;
                            translationY += deltaY;
                            drawAfterTranslatingOrScaling(true);
                            dx = x;
                            dy = y;
                        }
                        case MotionEvent.ACTION_UP -> {
                            drawBitmapLastOnView(true);
                            clearStatus();
                        }
                    }
                }
                case 2 -> {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_MOVE -> {
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            final double diagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                            final double diagonalRatio = diagonal / lastDiagonal;
                            scale = (float) (scale * diagonalRatio);
                            calculateBackgroundSizeOnView();
                            final float pivotX = (float) (lastPivotX * diagonalRatio), pivotY = (float) (lastPivotY * diagonalRatio);
                            translationX = translationX - pivotX + lastPivotX;
                            translationY = translationY - pivotY + lastPivotY;
                            drawAfterTranslatingOrScaling(true);
                            lastPivotX = pivotX;
                            lastPivotY = pivotY;
                            lastDiagonal = diagonal;
                        }
                        case MotionEvent.ACTION_POINTER_DOWN -> {
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            lastPivotX = (x0 + x1) / 2.0f - translationX;
                            lastPivotY = (y0 + y1) / 2.0f - translationY;
                            lastDiagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                            clearStatus();
                        }
                        case MotionEvent.ACTION_POINTER_UP -> {
                            final int index = 1 - event.getActionIndex();
                            final float x = event.getX(index);
                            final float y = event.getY(index);
                            tvStatus.setText(String.format(getString(R.string.coordinates),
                                    toBitmapX(x), toBitmapY(y)));
                            dx = x;
                            dy = y;
                        }
                    }
                }
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onBrushRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithBrushListener);
            paint.setAntiAlias(true);
            tietBrushBlurRadius.setText(String.valueOf(blurRadius));
            tietBrushStrokeWidth.setText(String.valueOf(strokeWidth));
            paint.setStyle(Paint.Style.STROKE);
            svOptionsBrush.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onBucketFillRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithBucketListener);
            threshold = 0x0;
            svOptionsBucketFill.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onCloneStampRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithCloneStampListener);
            cbCloneStampAntiAlias.setChecked(antiAlias);
            tietCloneStampBlurRadius.setText(String.valueOf(blurRadius));
            tietCloneStampStrokeWidth.setText(String.valueOf(strokeWidth));
            svOptionsCloneStamp.setVisibility(View.VISIBLE);
        } else {
            cloneStampSrc = null;
            eraseBitmapAndInvalidateView(previewImage, ivPreview);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onGradientRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithGradientListener);
            cbGradientAntiAlias.setChecked(antiAlias);
            tietGradientBlurRadius.setText(String.valueOf(blurRadius));
            tietGradientStrokeWidth.setText(String.valueOf(strokeWidth));
            svOptionsGradient.setVisibility(View.VISIBLE);
        }
    };

    private final CompoundButton.OnCheckedChangeListener onMagicEraserRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithMagicEraserListener);
            updateReference();
            paint.setAntiAlias(false);
            paint.setMaskFilter(null);
            paint.setStrokeCap(Paint.Cap.BUTT);
            tietMagicEraserStrokeWidth.setText(String.valueOf(strokeWidth));
            svOptionsMagicEraser.setVisibility(View.VISIBLE);
        } else {
            paint.setStrokeCap(Paint.Cap.ROUND);
            magErB = null;
            magErF = null;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onMagicPaintRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithMagicPaintListener);
            updateReference();
            threshold = 0xFF;
            cbMagicPaintAntiAlias.setChecked(antiAlias);
            tietMagicPaintBlurRadius.setText(String.valueOf(blurRadius));
            tietMagicPaintStrokeWidth.setText(String.valueOf(strokeWidth));
            svOptionsMagicPaint.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onPatcherRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithPatcherListener);
            cbPatcherAntiAlias.setChecked(antiAlias);
            tietPatcherBlurRadius.setText(String.valueOf(blurRadius));
            tietPatcherStrokeWidth.setText(String.valueOf(strokeWidth));
            svOptionsPatcher.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onPathRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithPathListener);
            cbPathAntiAlias.setChecked(antiAlias);
            cbPathFill.setChecked(isPaintStyleFill());
            tietPathBlurRadius.setText(String.valueOf(blurRadius));
            tietPathStrokeWidth.setText(String.valueOf(strokeWidth));
            svOptionsPath.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onPencilRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithPencilListener);
            cbPencilAntiAlias.setChecked(antiAlias);
            tietPencilBlurRadius.setText(String.valueOf(blurRadius));
            tietPencilStrokeWidth.setText(String.valueOf(strokeWidth));
            svOptionsPencil.setVisibility(View.VISIBLE);
        }
    };

    private final CompoundButton.OnCheckedChangeListener onRulerRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithRulerListener);
        } else {
            ruler.enabled = false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onShapeRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithShapeListener);
            cbShapeFill.setChecked(isPaintStyleFill());
            tietShapeStrokeWidth.setText(String.valueOf(paint.getStrokeWidth()));
            llOptionsShape.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onTransformerRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithTransformerListener);
            svOptionsTransformer.setVisibility(View.VISIBLE);
            selector.setColor(Color.BLUE);
            drawSelectionOnView();
        } else {
            drawTransformerOnCanvas();
            marqueeBoundBeingDragged = null;
            selector.setColor(Color.DKGRAY);
            drawSelectionOnView();
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onTextRBCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onTouchIVWithTextListener);
            cbTextFill.setChecked(isPaintStyleFill());
        } else {
            drawTextOnCanvas(false);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onZoomToolCheckBoxCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            flImageView.setOnTouchListener(onTouchIVWithZoomToolListener);
        } else {
            flImageView.setOnTouchListener((View.OnTouchListener) cbZoom.getTag());
        }
    };

    private final MessageQueue.IdleHandler onUiThreadWaitForMsgSinceMAHasBeenCreatedHandler = () -> {
        load();
        return false;
    };

    private void addDefaultTab() {
        addTab(Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888), 0);
    }

    private Tab addFrame(Bitmap bitmap, int position, boolean isFirst, int delay,
                         CharSequence title, String path, Tab.FileType type) {
        return addFrame(bitmap, position, isFirst, delay, title, path, type, false);
    }

    private Tab addFrame(Bitmap bitmap, int position, boolean isFirst, int delay,
                         CharSequence title, String path, Tab.FileType type, boolean setSelected) {
        final Tab t = new Tab();
        t.bitmap = bitmap;
        t.paint.setBlendMode(BlendMode.SRC_OVER);
        t.isFirstFrame = isFirst;
        t.delay = delay;
        if (isFirst) {
            t.filePath = path;
            t.fileType = type;
        }
        addTab(t, position, title, setSelected);
        return t;
    }

    private Tab addLayer(Bitmap bitmap, int position) {
        return addLayer(bitmap, position, 0, 0, 0);
    }

    private Tab addLayer(Bitmap bitmap, int position, int level, int left, int top) {
        return addLayer(bitmap, position, level, left, top, true, getString(R.string.untitled));
    }

    private Tab addLayer(Bitmap bitmap, int position, int level, int left, int top,
                         boolean visible, CharSequence title) {
        return addLayer(bitmap, position, level, left, top, visible, title, true);
    }

    private Tab addLayer(Bitmap bitmap, int position, int level, int left, int top,
                         boolean visible, CharSequence title, boolean setSelected) {
        final Tab t = new Tab();
        t.bitmap = bitmap;
        t.isBackground = false;
        t.isFirstFrame = false;
        t.setLevel(level);
        t.moveTo(left, top);
        t.paint.setBlendMode(BlendMode.SRC_OVER);
        t.visible = visible;
        addTab(t, position, title, setSelected);
        return t;
    }

    private Tab addTab(Bitmap bitmap, int position) {
        return addTab(bitmap, position, getString(R.string.untitled));
    }

    private Tab addTab(Bitmap bitmap, int position, CharSequence title) {
        return addTab(bitmap, position, title, null, null, null);
    }

    private Tab addTab(Bitmap bitmap, int position,
                       CharSequence title, String path, Tab.FileType type, Bitmap.CompressFormat compressFormat) {
        final Tab t = new Tab();
        t.bitmap = bitmap;
        t.paint.setBlendMode(BlendMode.SRC_OVER);
        t.filePath = path;
        t.fileType = type;
        t.compressFormat = compressFormat;
        addTab(t, position, title);
        return t;
    }

    private void addTab(Tab tab, int position, CharSequence title) {
        addTab(tab, position, title, true);
    }

    private void addTab(Tab tab, int position, CharSequence title, boolean setSelected) {
        tabs.add(position, tab);
        addToHistory(tab);
        final TabLayout.Tab t = loadTab(tab, position, title);
        if (setSelected) {
            Tab.distinguishProjects(tabs);
            Tab.updateBackgroundIcons(tabs);
            tab.showVisibilityIcon();
            Tab.computeLayerTree(tabs, tab);
            Tab.updateLevelIcons(tabs, tab);
            tabLayout.selectTab(t);
        }
    }

    private void addToHistory() {
        addToHistory(tab);
    }

    private void addToHistory(Tab tab) {
        tab.history.add(tab.bitmap);
    }

    private void calculateBackgroundSizeOnView() {
        final Bitmap background = tab.getBackground().bitmap;
        backgroundScaledW = toScaled(background.getWidth());
        backgroundScaledH = toScaled(background.getHeight());
    }

    private Position checkDraggingMarqueeBound(float x, float y) {
        marqueeBoundBeingDragged = null;

// Marquee Bounds
        final float mbLeft = toViewX(selection.left), mbTop = toViewY(selection.top),
                mbRight = toViewX(selection.right), mbBottom = toViewY(selection.bottom);

        if (mbLeft - 50.0f <= x && x < mbLeft + 50.0f) {
            if (mbTop + 50.0f <= y && y < mbBottom - 50.0f) {

                marqueeBoundBeingDragged = Position.LEFT;
            }
        } else if (mbTop - 50.0f <= y && y < mbTop + 50.0f) {
            if (mbLeft + 50.0f <= x && x < mbRight - 50.0f) {

                marqueeBoundBeingDragged = Position.TOP;
            }
        } else if (mbRight - 50.0f <= x && x < mbRight + 50.0f) {
            if (mbTop + 50.0f <= y && y < mbBottom - 50.0f) {

                marqueeBoundBeingDragged = Position.RIGHT;
            }
        } else if (mbBottom - 50.0f <= y && y < mbBottom + 50.0f) {
            if (mbLeft + 50.0f <= x && x < mbRight - 50.0f) {

                marqueeBoundBeingDragged = Position.BOTTOM;
            }
        }

        return marqueeBoundBeingDragged;
    }

    private boolean checkIfHaveSelectedToolNeedsRef() {
        return rbMagicEraser.isChecked() || rbMagicPaint.isChecked();
    }

    private void checkLayerBlendModeMenuItem(BlendMode blendMode) {
        for (int i = 0; i < BLEND_MODES.length; ++i) {
            final MenuItem mi = smLayerBlendModes.getItem(i);
            mi.setChecked(BLEND_MODES[i] == blendMode);
        }
    }

    private void checkLayerFilterMenuItem(Tab.Filter filter) {
        miLayerColorMatrix.setChecked(filter == Tab.Filter.COLOR_MATRIX);
        miLayerCurves.setChecked(filter == Tab.Filter.CURVES);
        miLayerHsv.setChecked(filter == Tab.Filter.HSV);
    }

    private boolean checkOrRequestPermission() {
        if (!Environment.isExternalStorageManager()) {
            try {
                final Intent intent = new Intent()
                        .setAction(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        .setData(Uri.fromParts("package", getPackageName(), null));
                requestPermissionLauncher.launch(intent);
            } catch (RuntimeException e) {
                final Intent intent = new Intent()
                        .setAction(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                requestPermissionLauncher.launch(intent);
            }
            return false;
        }
        return true;
    }

    private void clearStatus() {
        tvStatus.setText("");
    }

    private void closeTab() {
        if (tabs.size() > 1) {
            closeTab(tabLayout.getSelectedTabPosition(), true);
        } else {
            closeTab(0, false);
            addDefaultTab();
        }
    }

    private void closeTab(int position, boolean select) {
        final Tab tab = tabs.get(position);
        if (tab == this.tab && transformer != null) {
            recycleTransformer();
        }
        tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
        tabLayout.removeTabAt(position);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        tab.bitmap.recycle();
        tab.history.clear();
        if (select) {
            final Tab background = tab.getBackground();
            final Tab above = Tab.getAbove(tabs, position), below = Tab.getBelow(tabs, position);
            tabs.remove(position); // Remove tab
            boolean isFirstOfMultipleFrames = false;
            if (background.bitmap.isRecycled()) {
                if (above != null) {
                    above.inheritPropertiesFrom(background);
                } else if (background.isFirstFrame && position < tabs.size()) {
                    final Tab nextFrame = tabs.get(position).getBackground();
                    if (!nextFrame.isFirstFrame) {
                        nextFrame.isFirstFrame = true;
                        isFirstOfMultipleFrames = true;
                    }
                }
            }
            Tab.distinguishProjects(tabs);
            Tab.updateBackgroundIcons(tabs);
            int selectedPos = tabLayout.getSelectedTabPosition();
            if (above != null) {
                Tab.computeLayerTree(tabs, above);
                Tab.updateLevelIcons(tabs, above);
            } else if (below != null) {
                Tab.computeLayerTree(tabs, below);
                Tab.updateLevelIcons(tabs, below);
                selectedPos = position;
            } else /* must have closed the entire frame */ if (isFirstOfMultipleFrames) {
                selectedPos = position;
            } /* else must have closed the entire project */
            selectTab(selectedPos);
        } else {
            tabs.remove(position); // Remove tab
        }
    }

    private void createFrame() {
        final Tab firstFrame = tab.getBackground().getFirstFrame();
        final Bitmap bm = Bitmap.createBitmap(firstFrame.bitmap.getWidth(), firstFrame.bitmap.getHeight(),
                firstFrame.bitmap.getConfig(), firstFrame.bitmap.hasAlpha(), firstFrame.bitmap.getColorSpace());
        final int pos = Tab.getPosOfProjEnd(tabs, firstFrame);
        addFrame(bm, pos, false, firstFrame.delay,
                getString(R.string.untitled), firstFrame.filePath, firstFrame.fileType, true);
    }

    private void createImage(int width, int height) {
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        addTab(bm, tabs.size());
    }

    private void createLayer(int width, int height,
                             Bitmap.Config config, ColorSpace colorSpace,
                             int level, int left, int top, int position) {
        final Bitmap bm = Bitmap.createBitmap(width, height, config, true, colorSpace);
        addLayer(bm, position, level, left, top);
    }

    private void createImagePreview() {
        if (imagePreview != null) {
            imagePreview.recycle();
        }
        if (!hasSelection) {
            selectAll();
        }
        imagePreview = new Preview(bitmap, selection);
    }

    private void createTransformer() {
        transformer = new Transformer(
                Bitmap.createBitmap(bitmap,
                        selection.left, selection.top, selection.width(), selection.height()),
                selection);
        canvas.drawRect(selection.left, selection.top, selection.right, selection.bottom,
                eraser);
    }

    private boolean dragMarqueeBound(float viewX, float viewY) {
        final float halfScale = scale / 2.0f;
        if (marqueeBoundBeingDragged == null) {
            return false;
        }
        switch (marqueeBoundBeingDragged) {
            case LEFT -> {
                final int left = toBitmapX(viewX + halfScale);
                if (left != selection.left) selection.left = left;
                else return false;
            }
            case TOP -> {
                final int top = toBitmapY(viewY + halfScale);
                if (top != selection.top) selection.top = top;
                else return false;
            }
            case RIGHT -> {
                final int right = toBitmapX(viewX + halfScale);
                if (right != selection.right) selection.right = right;
                else return false;
            }
            case BOTTOM -> {
                final int bottom = toBitmapY(viewY + halfScale);
                if (bottom != selection.bottom) selection.bottom = bottom;
                else return false;
            }
        }
        return true;
    }

    private void drawAfterTranslatingOrScaling(boolean doNotMerge) {
        if (doNotMerge) {
            drawBitmapLastOnView(false);
        } else {
            drawBitmapOnView(true, false);
        }
        drawChessboardOnView();
        drawGridOnView();
        if (transformer != null) {

        } else if (isEditingText) {
            drawTextOnView();
        } else if (!isShapeStopped) {
            drawPointOnView(shapeStartX, shapeStartY);
        } else if (cloneStampSrc != null) {
            drawCrossOnView(cloneStampSrc.x, cloneStampSrc.y);
        } else if (ruler.enabled) {
            drawRulerOnView();
        } else if (magErB != null && magErF != null) {
            drawCrossOnView(magErB.x, magErB.y, true);
            drawCrossOnView(magErF.x, magErF.y, false);
        }
        drawSelectionOnView();
    }

    private void drawBitmapOnCanvas(Canvas canvas, Bitmap bitmap, float translX, float translY) {
        final Rect vs = getVisibleSubset(translX, translY, bitmap.getWidth(), bitmap.getHeight());
        drawBitmapOnCanvas(canvas, bitmap, translX, translY, vs);
    }

    private void drawBitmapOnCanvas(Canvas canvas, Bitmap bitmap, float translX,
                                    float translY, Rect vs) {
        if (vs.isEmpty()) {
            return;
        }
        final RectF svs = getVisibleSubsetOfView(vs, translX, translY);
        try {
            canvas.drawBitmap(bitmap, vs, svs, isScaledMuch() ? PAINT_BITMAP : bitmapPaint);
        } catch (RuntimeException e) {
        }
    }

    private void drawBitmapOnView() {
        drawBitmapOnView(false);
    }

    private void drawBitmapOnView(final boolean wait) {
        drawBitmapOnView(false, wait);
    }

    private void drawBitmapOnView(final boolean eraseVisible, final boolean wait) {
        drawBitmapOnView(bitmap, eraseVisible, wait);
    }

    private void drawBitmapOnView(final Bitmap bitmap, final boolean eraseVisible, final boolean wait) {
        runOrStart(() -> drawBitmapVisibleOnView(bitmap, eraseVisible), wait);
    }

    private void drawBitmapOnView(final Rect rect) {
        drawBitmapOnView(rect, false);
    }

    private void drawBitmapOnView(final Rect rect, final boolean wait) {
        drawBitmapOnView(bitmap, rect, wait);
    }

    private void drawBitmapOnView(final Bitmap bitmap, final Rect rect) {
        drawBitmapOnView(bitmap, rect, false);
    }

    private void drawBitmapOnView(final Bitmap bitmap, final boolean wait) {
        drawBitmapOnView(bitmap, false, wait);
    }

    private void drawBitmapOnView(final Bitmap bitmap, final Rect rect, final boolean wait) {
        drawBitmapOnView(bitmap, rect.left, rect.top, rect.right, rect.bottom, wait);
    }

    private void drawBitmapOnView(final int left, final int top, final int right,
                                  final int bottom) {
        drawBitmapOnView(bitmap, left, top, right, bottom, false);
    }

    private void drawBitmapOnView(final Bitmap bitmap,
                                  final int left, final int top, final int right, final int bottom,
                                  final boolean wait) {
        runOrStart(() ->
                        drawBitmapSubsetOnView(bitmap,
                                tab.left + left, tab.top + top, tab.left + right, tab.top + bottom
                        ),
                wait);
    }

    private void drawBitmapOnView(final int x0, final int y0, final int x1, final int y1,
                                  final float radius) {
        final boolean x = x0 <= x1, y = y0 <= y1;
        final int rad = (int) Math.ceil(radius);
        final int left = tab.left + (x ? x0 : x1) - rad, top = tab.top + (y ? y0 : y1) - rad,
                right = tab.left + (x ? x1 : x0) + rad + 1, bottom = tab.top + (y ? y1 : y0) + rad + 1;
        runOrStart(() ->
                drawBitmapSubsetOnView(bitmap, left, top, right, bottom));
    }

    private final Runnable mergeLayersEntireRunner = () -> {
        final Tab background = tab.getBackground();
        final Rect vs = new Rect(0, 0, background.bitmap.getWidth(), background.bitmap.getHeight());

        Tab extraExclTab = null;
        if (transformer != null) {
            extraExclTab = transformer.makeTab();
        }
        final Bitmap merged = Tab.mergeLayers(background.layerTree, vs, tab, bitmap, extraExclTab);
        recycleBitmap(lastMerged);
        lastMerged = merged;
    };

    private void mergeLayersEntire() {
        runOrStart(mergeLayersEntireRunner, true);
    }

    private void drawBitmapEntireOnView(final boolean wait) {
        mergeLayersEntire();
        drawBitmapLastOnView(wait);
    }

    private final Runnable drawingImageLastOnViewRunner = this::drawBitmapLastOnView;

    private void drawBitmapLastOnView(final boolean wait) {
        runOrStart(drawingImageLastOnViewRunner, wait);
    }

    private void drawBitmapLastOnView() {
        final Bitmap background = tab.getBackground().bitmap;
        final Rect vs = getVisibleSubset(translationX, translationY,
                background.getWidth(), background.getHeight());

        runOnUiThread(() -> {
            eraseBitmap(viewImage);
            if (vs.isEmpty()) {
                return;
            }
            drawBitmapOnCanvas(viewCanvas, lastMerged, translationX, translationY, vs);
            imageView.invalidate();
        });
    }

    private final Runnable erasingViewRunner = () -> eraseBitmap(viewImage);

    private void drawBitmapSubsetOnView(final Bitmap bitmap,
                                        int left, int top, int right, int bottom) {
        final Tab background = tab.getBackground();
        final int width = background.bitmap.getWidth(), height = background.bitmap.getHeight();
        left = Math.max(left, 0);
        top = Math.max(top, 0);
        right = Math.min(right, width);
        bottom = Math.min(bottom, height);
        if (left >= right || top >= bottom) {
            return;
        }
        final Rect vs = getVisibleSubset(translationX, translationY, width, height);
        if (vs.isEmpty()) {
            runOnUiThread(erasingViewRunner);
            return;
        }
        if (!vs.intersect(left, top, right, bottom)) {
            return;
        }

        Tab extraTab = null;
        if (transformer != null) {
            extraTab = transformer.makeTab();
        }
        final Bitmap merged = Tab.mergeLayers(background.layerTree, vs, tab, bitmap, extraTab);
        recycleBitmap(lastMerged);
        lastMerged = null;
        final float translLeft = toViewXRel(left), translTop = toViewYRel(top);
        final Rect relative = new Rect(0, 0, vs.width(), vs.height());
        runOnUiThread(() -> {
            drawBitmapOnCanvas(viewCanvas, merged,
                    translLeft > -scale ? translLeft : translLeft % scale,
                    translTop > -scale ? translTop : translTop % scale,
                    relative);
            merged.recycle();
            imageView.invalidate();
        });
    }

    private void drawBitmapVisibleOnView(final Bitmap bitmap, final boolean eraseVisible) {
        final Tab background = tab.getBackground();
        final Rect vs = getVisibleSubset(translationX, translationY, background.bitmap.getWidth(), background.bitmap.getHeight());
        if (vs.isEmpty()) {
            runOnUiThread(erasingViewRunner);
            return;
        }

        Tab extraTab = null;
        if (transformer != null) {
            extraTab = transformer.makeTab();
        }
        final Bitmap merged = Tab.mergeLayers(background.layerTree, vs, tab, bitmap, extraTab);
        recycleBitmap(lastMerged);
        lastMerged = null;
        final Rect relative = new Rect(0, 0, vs.width(), vs.height());
        runOnUiThread(() -> {
            if (eraseVisible) {
                eraseBitmap(viewImage);
            }
            drawBitmapOnCanvas(viewCanvas, merged,
                    translationX > -scale ? translationX : translationX % scale,
                    translationY > -scale ? translationY : translationY % scale,
                    relative);
            merged.recycle();
            imageView.invalidate();
        });
    }

    private void drawChessboardOnView() {
        eraseBitmap(chessboardImage);

        final boolean isBackground = tab.isBackground;
        final float left = Math.max(0.0f, translationX);
        final float top = Math.max(0.0f, translationY);
        final float right = Math.min(translationX + backgroundScaledW, viewWidth);
        final float bottom = Math.min(translationY + backgroundScaledH, viewHeight);

        chessboardCanvas.drawBitmap(chessboard,
                new Rect((int) left, (int) top, (int) right, (int) bottom),
                new RectF(left, top, right, bottom),
                PAINT_SRC);

        ivChessboard.invalidate();

        drawRuler();
    }

    private void drawCrossOnView(float x, float y) {
        drawCrossOnView(x, y, true);
    }

    private void drawCrossOnView(float x, float y, boolean isFirst) {
        if (isFirst) {
            eraseBitmap(previewImage);
        }
        final float viewX = toViewX(x), viewY = toViewY(y);
        previewCanvas.drawLine(viewX - 50.0f, viewY, viewX + 50.0f, viewY, selector);
        previewCanvas.drawLine(viewX, viewY - 50.0f, viewX, viewY + 50.0f, selector);
        ivPreview.invalidate();
    }

    private void drawFloatingLayers() {
        drawTransformerOnCanvas();
        drawTextOnCanvas();
    }

    private void drawGridOnView() {
        eraseBitmap(gridImage);

        final float left = toViewX(0), top = toViewY(0);

        float l = left >= 0.0f ? left : left % scale,
                t = top >= 0.0f ? top : top % scale,
                r = Math.min(left + toScaled(bitmap.getWidth()), viewWidth),
                b = Math.min(top + toScaled(bitmap.getHeight()), viewHeight);
        if (isScaledMuch()) {
            for (float x = l; x < r; x += scale) {
                gridCanvas.drawLine(x, t, x, b, PAINT_GRID);
            }
            for (float y = t; y < b; y += scale) {
                gridCanvas.drawLine(l, y, r, y, PAINT_GRID);
            }
        }

        gridCanvas.drawLine(l, t, l - 100.0f, t, PAINT_IMAGE_BOUND);
        gridCanvas.drawLine(r, t, r + 100.0f, t, PAINT_IMAGE_BOUND);
        gridCanvas.drawLine(r, t - 100.0f, r, t, PAINT_IMAGE_BOUND);
        gridCanvas.drawLine(r, b, r, b + 100.0f, PAINT_IMAGE_BOUND);
        gridCanvas.drawLine(r + 100.0f, b, r, b, PAINT_IMAGE_BOUND);
        gridCanvas.drawLine(l, b, l - 100.0f, b, PAINT_IMAGE_BOUND);
        gridCanvas.drawLine(l, b + 100.0f, l, b, PAINT_IMAGE_BOUND);
        gridCanvas.drawLine(l, t, l, t - 100.0f, PAINT_IMAGE_BOUND);

        final CellGrid cellGrid = tab.cellGrid;
        if (cellGrid.enabled) {
            if (cellGrid.sizeX > 1) {
                final float scaledSizeX = toScaled(cellGrid.sizeX),
                        scaledSpacingX = toScaled(cellGrid.spacingX);
                l = (left >= 0.0f ? left : left % (scaledSizeX + scaledSpacingX)) + toScaled(cellGrid.offsetX);
                t = Math.max(0.0f, top);
                float x = l;
                if (cellGrid.spacingX <= 0) {
                    while (x < r) {
                        gridCanvas.drawLine(x, t, x, b, PAINT_CELL_GRID);
                        x += scaledSizeX;
                    }
                } else {
                    while (true) {
                        gridCanvas.drawLine(x, t, x, b, PAINT_CELL_GRID);
                        if ((x += scaledSizeX) >= r) {
                            break;
                        }
                        gridCanvas.drawLine(x, t, x, b, PAINT_CELL_GRID);
                        if ((x += scaledSpacingX) >= r) {
                            break;
                        }
                    }
                }
            }
            if (cellGrid.sizeY > 1) {
                final float scaledSizeY = toScaled(cellGrid.sizeY),
                        scaledSpacingY = toScaled(cellGrid.spacingY);
                t = (top >= 0.0f ? top : top % (scaledSizeY + scaledSpacingY)) + toScaled(cellGrid.offsetY);
                l = Math.max(0.0f, left);
                float y = t;
                if (cellGrid.spacingY <= 0) {
                    while (y < b) {
                        gridCanvas.drawLine(l, y, r, y, PAINT_CELL_GRID);
                        y += scaledSizeY;
                    }
                } else {
                    while (true) {
                        gridCanvas.drawLine(l, y, r, y, PAINT_CELL_GRID);
                        if ((y += scaledSizeY) >= b) {
                            break;
                        }
                        gridCanvas.drawLine(l, y, r, y, PAINT_CELL_GRID);
                        if ((y += scaledSpacingY) >= b) {
                            break;
                        }
                    }
                }
            }
        }

        for (final Guide guide : tab.guides) {
            if (guide.orientation) {
                final float x = toViewX(guide.position);
                gridCanvas.drawLine(x, 0.0f, x, viewHeight, PAINT_GUIDE);
            } else {
                final float y = toViewY(guide.position);
                gridCanvas.drawLine(0.0f, y, viewWidth, y, PAINT_GUIDE);
            }
        }

        ivGrid.invalidate();
    }

    private void drawImagePreviewOnImage() {
        canvas.drawBitmap(imagePreview.getEntire(), 0.0f, 0.0f, PAINT_SRC);
        drawBitmapOnView(true);
    }

    private void drawImagePreviewOnView() {
        drawImagePreviewOnView(false);
    }

    private void drawImagePreviewOnView(final boolean wait) {
        drawBitmapOnView(imagePreview.getEntire(), selection, wait);
    }

    private void drawRuler() {
        eraseBitmap(rulerHImage);
        eraseBitmap(rulerVImage);
        final int multiplier = (int) Math.ceil(96.0 / scale);
        final float scaledMultiplier = toScaled(multiplier);

        rulerPaint.setTextAlign(Paint.Align.LEFT);
        int unscaledX = (int) Math.floor(-translationX / scaledMultiplier) * multiplier;
        for (float x = translationX % scaledMultiplier + (translationX <= 0.0f ? 0.0f : -scaledMultiplier),
             height = rulerHImage.getHeight();
             x < viewWidth;
             x += scaledMultiplier, unscaledX += multiplier) {
            rulerHCanvas.drawLine(x, 0.0f, x, height, rulerPaint);
            rulerHCanvas.drawText(String.valueOf(unscaledX), x, height, rulerPaint);
        }

        rulerPaint.setTextAlign(Paint.Align.RIGHT);
        final float ascent = rulerPaint.getFontMetrics().ascent;
        int unscaledY = (int) Math.floor(-translationY / scaledMultiplier) * multiplier;
        for (float y = translationY % scaledMultiplier + (translationY <= 0.0f ? 0.0f : -scaledMultiplier),
             width = rulerVImage.getWidth();
             y < viewHeight;
             y += scaledMultiplier, unscaledY += multiplier) {
            rulerVCanvas.drawLine(0.0f, y, width, y, rulerPaint);
            rulerVCanvas.drawText(String.valueOf(unscaledY), width, y - ascent, rulerPaint);
        }

        ivRulerH.invalidate();
        ivRulerV.invalidate();
    }

    private void drawLineOnCanvas(int startX, int startY, int stopX, int stopY, Paint paint) {
        if (startX <= stopX) ++stopX;
        else ++startX;
        if (startY <= stopY) ++stopY;
        else ++startY;

        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }

    private void drawPoint(Canvas canvas, float x, float y, String text) {
        canvas.drawLine(x - 100.0f, y, x + 100.0f, y, PAINT_POINT);
        canvas.drawLine(x, y - 100.0f, x, y + 100.0f, PAINT_POINT);
        canvas.drawText(text, x, y, PAINT_POINT);
        imageView.invalidate();
    }

    private void drawPointOnView(int x, int y) {
        eraseBitmap(previewImage);
        fillPaint.setColor(paint.getColorLong());
        final float left = toViewX(x), top = toViewY(y), right = left + scale, bottom = top + scale;
        previewCanvas.drawRect(left, top, right, bottom, fillPaint);
        ivPreview.invalidate();
    }

    private void drawRulerOnView() {
        eraseBitmap(previewImage);
        previewCanvas.drawLine(
                toViewX(ruler.startX), toViewY(ruler.startY),
                toViewX(ruler.stopX), toViewY(ruler.stopY),
                PAINT_CELL_GRID);
        ivPreview.invalidate();
    }

    private void drawSelectionOnView() {
        drawSelectionOnView(false);
    }

    private void drawSelectionOnView(boolean showMargins) {
        eraseBitmap(selectionImage);
        if (hasSelection) {
            final float left = Math.max(0.0f, toViewX(selection.left)),
                    top = Math.max(0.0f, toViewY(selection.top)),
                    right = Math.min(viewWidth, toViewX(selection.right)),
                    bottom = Math.min(viewHeight, toViewY(selection.bottom));
            selectionCanvas.drawRect(left, top, right, bottom, selector);
            if (showMargins) {
                final float viewImLeft = Math.max(0.0f, translationX),
                        viewImTop = Math.max(0.0f, translationY),
                        viewImRight = Math.min(viewWidth, translationX + backgroundScaledW),
                        viewImBottom = Math.min(viewHeight, translationY + backgroundScaledH);
                final float centerHorizontal = (left + right) / 2.0f,
                        centerVertical = (top + bottom) / 2.0f;
                if (left > 0.0f) {
                    selectionCanvas.drawLine(left, centerVertical, viewImLeft, centerVertical, marginPaint);
                    selectionCanvas.drawText(String.valueOf(tab.left + selection.left), (viewImLeft + left) / 2.0f, centerVertical, marginPaint);
                }
                if (top > 0.0f) {
                    selectionCanvas.drawLine(centerHorizontal, top, centerHorizontal, viewImTop, marginPaint);
                    selectionCanvas.drawText(String.valueOf(tab.top + selection.top), centerHorizontal, (viewImTop + top) / 2.0f, marginPaint);
                }
                if (right < viewWidth) {
                    selectionCanvas.drawLine(right, centerVertical, viewImRight, centerVertical, marginPaint);
                    selectionCanvas.drawText(String.valueOf(tab.left + bitmap.getWidth() - selection.right), (viewImRight + right) / 2.0f, centerVertical, marginPaint);
                }
                if (bottom < viewHeight) {
                    selectionCanvas.drawLine(centerHorizontal, bottom, centerHorizontal, viewImBottom, marginPaint);
                    selectionCanvas.drawText(String.valueOf(tab.top + bitmap.getHeight() - selection.bottom), centerHorizontal, (viewImBottom + bottom) / 2.0f, marginPaint);
                }
            }
        }
        ivSelection.invalidate();
    }

    private void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
        shape.drawShapeOnImage(x0, y0, x1, y1);
    }

    private String drawShapeOnView(int x0, int y0, int x1, int y1) {
        eraseBitmap(previewImage);
        final String result = shape.drawShapeOnView(x0, y0, x1, y1);
        ivPreview.invalidate();
        return result;
    }

    private void drawTextOnCanvas() {
        drawTextOnCanvas(true);
    }

    private void drawTextOnCanvas(boolean hideOptions) {
        if (!isEditingText) {
            return;
        }
        isEditingText = false;
        paint.setTextSize(textSize);
        canvas.drawText(tietText.getText().toString(), textX, textY, paint);
        drawBitmapOnView(true, true);
        eraseBitmapAndInvalidateView(previewImage, ivPreview);
        hideSoftInputFromWindow();
        if (hideOptions) {
            llOptionsText.setVisibility(View.INVISIBLE);
        }
        addToHistory();
    }

    private void drawTextOnView() {
        if (!isEditingText) {
            return;
        }
        eraseBitmap(previewImage);
        final float x = toViewX(textX), y = toViewY(textY);
        paint.setTextSize(toScaled(textSize));
        final Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        final float centerVertical = y + fontMetrics.ascent / 2.0f;
        previewCanvas.drawText(tietText.getText().toString(), x, y, paint);
        previewCanvas.drawLine(x, 0.0f, x, viewHeight, PAINT_CELL_GRID);
        previewCanvas.drawLine(0.0f, y, viewWidth, y, PAINT_TEXT_LINE);
        previewCanvas.drawLine(0.0f, centerVertical, viewWidth, centerVertical, PAINT_CELL_GRID);
        ivPreview.invalidate();
    }

    private void drawTransformerOnCanvas() {
        if (transformer == null || !hasSelection) {
            return;
        }
        canvas.drawBitmap(transformer.getBitmap(), selection.left, selection.top, PAINT_SRC_OVER);
        recycleTransformer();
        drawBitmapOnView(selection);
        optimizeSelection();
        drawSelectionOnView();
        addToHistory();
        clearStatus();
    }

    private static void eraseBitmap(Bitmap bitmap) {
        bitmap.eraseColor(Color.TRANSPARENT);
    }

    private static void eraseBitmapAndInvalidateView(Bitmap bitmap, ImageView imageView) {
        eraseBitmap(bitmap);
        imageView.invalidate();
    }

    private void export() {
        if (tab.filePath == null) {
            if (!checkOrRequestPermission()) {
                return;
            }
            exportAs();
            return;
        }
        int quality = 100;
        if (tab.fileType == Tab.FileType.GIF) {
            if (tab.gifEncodingType == null) {
                exportInQuality();
                return;
            }
        } else if (tab.fileType != Tab.FileType.PNG) {
            quality = tab.quality;
            if (quality < 0) {
                exportInQuality();
                return;
            }
        }

        drawFloatingLayers();

        final File file = new File(tab.filePath);
        if (tab.compressFormat != null) {
            try (final FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(tab.compressFormat, quality, fos);
                fos.flush();
            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.failed) + '\n' + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                bitmap.recycle();
            }
        } else if (tab.fileType == Tab.FileType.GIF) {
            final GifEncoder gifEncoder = new GifEncoder();
            final int width = tab.bitmap.getWidth(), height = tab.bitmap.getHeight();
            try {
                gifEncoder.init(width, height, tab.filePath);
            } catch (FileNotFoundException e) {
                return;
            }
            gifEncoder.setDither(tab.gifDither);
            gifEncoder.encodeFrame(bitmap, tab.delay);
            gifEncoder.close();
        }
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);
    }

    private void exportAs() {
        dirSelector.open(tab, this::exportInQuality);
    }

    private void exportInQuality() {
        setQuality(this::export);
    }

    private void fitOnScreen() {
        fitOnScreen(tab);
    }

    private void fitOnScreen(Tab tab) {
        final float width = tab.bitmap.getWidth(), height = tab.bitmap.getHeight();
        final float scaleW = (float) viewWidth / width, scaleH = (float) viewHeight / height;
        if (scaleW <= scaleH) {
            tab.scale = scaleW;
            tab.translationX = 0.0f;
            tab.translationY = (viewHeight >> 1) - height * scaleW / 2.0f;
        } else {
            tab.scale = scaleH;
            tab.translationX = (viewWidth >> 1) - width * scaleH / 2.0f;
            tab.translationY = 0.0f;
        }
    }

    /**
     * @see Tab#addOVCBCCListener(CompoundButton.OnCheckedChangeListener)
     */
    private CompoundButton.OnCheckedChangeListener getOVCBCCListener(final Tab tab) {
        return (buttonView, isChecked) -> {
            tab.visible = isChecked;
            drawBitmapOnView(true);
        };
    }

    private Rect getVisibleSubset() {
        return getVisibleSubset(translationX, translationY, bitmap.getWidth(), bitmap.getHeight());
    }

    private Rect getVisibleSubset(float translX, float translY, int width, int height) {
        final int left = translX >= 0.0f ? 0 : toUnscaled(-translX);
        final int top = translY >= 0.0f ? 0 : toUnscaled(-translY);
        final int right = Math.min(toUnscaled(translX + backgroundScaledW <= viewWidth ? backgroundScaledW : viewWidth - translX) + 1, width);
        final int bottom = Math.min(toUnscaled(translY + backgroundScaledH <= viewHeight ? backgroundScaledH : viewHeight - translY) + 1, height);
        return new Rect(left, top, right, bottom);
    }

    private RectF getVisibleSubsetOfView(Rect subset, float translX, float translY) {
        final float left = translX > -scale ? translX : translX % scale;
        final float top = translY > -scale ? translY : translY % scale;
        final float right = left + toScaled(subset.width());
        final float bottom = top + toScaled(subset.height());
        return new RectF(left, top, right, bottom);
    }

    private void hideSoftInputFromWindow() {
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void hideToolOptions() {
        for (int i = 0; i < flToolOptions.getChildCount(); ++i) {
            flToolOptions.getChildAt(i).setVisibility(View.INVISIBLE);
        }
    }

    private void initColorAdapter() {
        colorAdapter.setOnItemClickListener(view -> {
            final long color = (Long) view.getTag();
            paint.setColor(color);
            vForegroundColor.setBackgroundColor(Color.toArgb(color));
            if (isEditingText) {
                drawTextOnView();
            }
        });
        colorAdapter.setOnItemLongClickListener(view -> {
            ArgbColorPicker.make(MainActivity.this,
                            R.string.swatch,
                            (oldColor, newColor) -> {
                                if (newColor != null) {
                                    palette.set(palette.indexOf(oldColor), newColor);
                                } else {
                                    palette.remove(oldColor);
                                }
                                colorAdapter.notifyDataSetChanged();
                            },
                            (Long) view.getTag(),
                            R.string.delete)
                    .show();
            return true;
        });
    }

    private boolean isPaintStyleFill() {
        return paint.getStyle() != Paint.Style.STROKE;
    }

    private boolean isScaledMuch() {
        return scale >= 16.0f;
    }

    private void load() {
        viewWidth = imageView.getWidth();
        viewHeight = imageView.getHeight();
        rulerHHeight = ivRulerH.getHeight();
        rulerVWidth = ivRulerV.getWidth();

        viewImage = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewImage);
        imageView.setImageBitmap(viewImage);

        rulerHImage = Bitmap.createBitmap(viewWidth, ivRulerH.getHeight(), Bitmap.Config.ARGB_4444);
        rulerHCanvas = new Canvas(rulerHImage);
        ivRulerH.setImageBitmap(rulerHImage);
        rulerVImage = Bitmap.createBitmap(ivRulerV.getWidth(), viewHeight, Bitmap.Config.ARGB_4444);
        rulerVCanvas = new Canvas(rulerVImage);
        ivRulerV.setImageBitmap(rulerVImage);

        chessboardImage = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        chessboardCanvas = new Canvas(chessboardImage);
        ivChessboard.setImageBitmap(chessboardImage);

        gridImage = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        gridCanvas = new Canvas(gridImage);
        ivGrid.setImageBitmap(gridImage);

        previewImage = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        previewCanvas = new Canvas(previewImage);
        ivPreview.setImageBitmap(previewImage);

        selectionImage = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        selectionCanvas = new Canvas(selectionImage);
        ivSelection.setImageBitmap(selectionImage);

        if (tabs.isEmpty()) {
            if (fileToBeOpened != null) {
                openFile(fileToBeOpened);
            } else {
                addDefaultTab();
            }
            rbPencil.setChecked(true);
        } else {
            for (int i = 0; i < tabs.size(); ++i) {
                final Tab tab = tabs.get(i);
                loadTab(tab, i, tab.getTitle());
            }
            tabLayout.getTabAt(0).select();
        }

    }

    private TabLayout.Tab loadTab(Tab tab, int position, CharSequence title) {
        fitOnScreen(tab);

        final TabLayout.Tab t = tabLayout.newTab().setCustomView(R.layout.tab).setTag(tab);
        tab.initViews(t.getCustomView());
        tab.setTitle(title);
        tab.setVisible(tab.visible);
        tab.addOVCBCCListener(getOVCBCCListener(tab));
        tabLayout.addTab(t, position, false);
        return t;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Preferences
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        settings = ((MainApplication) getApplication()).getSettings();
        settings.setMainActivity(this);
        settings.update(preferences);

        // Locale
        final String loc = preferences.getString(Settings.KEY_LOC, "def");
        if (!"def".equals(loc)) {
            final int i = loc.indexOf('_');
            final Locale locale = i == -1
                    ? new Locale(loc)
                    : new Locale(loc.substring(0, i), loc.substring(i + 1));
            final Resources resources = getResources();
            final Configuration configuration = resources.getConfiguration();
            configuration.setLocale(locale);
            Locale.setDefault(locale);
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }

        final boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (isLandscape) {
            getSupportActionBar().hide();
        }

        // Content view
        setContentView(R.layout.activity_main);

        cbBucketFillContiguous = findViewById(R.id.cb_bucket_fill_contiguous);
        cbBucketFillIgnoreAlpha = findViewById(R.id.cb_bucket_fill_ignore_alpha);
        cbCloneStampAntiAlias = findViewById(R.id.cb_clone_stamp_anti_alias);
        cbGradientAntiAlias = findViewById(R.id.cb_gradient_anti_alias);
        cbMagicPaintAntiAlias = findViewById(R.id.cb_magic_paint_anti_alias);
        cbPatcherAntiAlias = findViewById(R.id.cb_patcher_anti_alias);
        cbPathAntiAlias = findViewById(R.id.cb_path_anti_alias);
        cbPathFill = findViewById(R.id.cb_path_fill);
        cbPencilAntiAlias = findViewById(R.id.cb_pencil_anti_alias);
        cbPencilWithEraser = findViewById(R.id.cb_pencil_with_eraser);
        cbShapeFill = findViewById(R.id.cb_shape_fill);
        cbTextFill = findViewById(R.id.cb_text_fill);
        cbTransformerFilter = findViewById(R.id.cb_transformer_filter);
        cbTransformerLar = findViewById(R.id.cb_transformer_lar);
        cbZoom = findViewById(R.id.cb_zoom);
        flToolOptions = findViewById(R.id.fl_tool_options);
        flImageView = findViewById(R.id.fl_iv);
        imageView = findViewById(R.id.iv);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        ivChessboard = findViewById(R.id.iv_chessboard);
        ivGrid = findViewById(R.id.iv_grid);
        ivPreview = findViewById(R.id.iv_preview);
        ivRulerH = findViewById(R.id.iv_ruler_horizontal);
        ivRulerV = findViewById(R.id.iv_ruler_vertical);
        ivSelection = findViewById(R.id.iv_selection);
        llOptionsShape = findViewById(R.id.ll_options_shape);
        llOptionsText = findViewById(R.id.ll_options_text);
        svOptionsTransformer = findViewById(R.id.sv_options_transformer);
        final RecyclerView rvSwatches = findViewById(R.id.rv_swatches);
        rbCloneStamp = findViewById(R.id.rb_clone_stamp);
        rbEyedropper = findViewById(R.id.rb_eyedropper);
        rbEyedropperAllLayers = findViewById(R.id.rb_eyedropper_all_layers);
        rbMagicEraser = findViewById(R.id.rb_magic_eraser);
        rbMagicEraserLeft = findViewById(R.id.rb_magic_eraser_left);
        rbMagicEraserRight = findViewById(R.id.rb_magic_eraser_right);
        cbMagicEraserPosition = rbMagicEraserLeft;
        rbMagicPaint = findViewById(R.id.rb_magic_paint);
        rbPencil = findViewById(R.id.rb_pencil);
        final RadioButton rbRuler = findViewById(R.id.rb_ruler);
        rbTransformer = findViewById(R.id.rb_transformer);
        final RadioGroup rgMagicEraserPosition = findViewById(R.id.rg_magic_eraser_position);
        svOptionsBrush = findViewById(R.id.sv_options_brush);
        svOptionsBucketFill = findViewById(R.id.sv_options_bucket_fill);
        svOptionsCloneStamp = findViewById(R.id.sv_options_clone_stamp);
        svOptionsEraser = findViewById(R.id.sv_options_eraser);
        svOptionsEyedropper = findViewById(R.id.sv_options_eyedropper);
        svOptionsGradient = findViewById(R.id.sv_options_gradient);
        svOptionsMagicEraser = findViewById(R.id.sv_options_magic_eraser);
        svOptionsMagicPaint = findViewById(R.id.sv_options_magic_paint);
        svOptionsPatcher = findViewById(R.id.sv_options_patcher);
        svOptionsPath = findViewById(R.id.sv_options_path);
        svOptionsPencil = findViewById(R.id.sv_options_pencil);
        tabLayout = findViewById(R.id.tl);
        tietBrushBlurRadius = findViewById(R.id.tiet_brush_blur_radius);
        tietBrushStrokeWidth = findViewById(R.id.tiet_brush_stroke_width);
        tietCloneStampBlurRadius = findViewById(R.id.tiet_clone_stamp_blur_radius);
        tietCloneStampStrokeWidth = findViewById(R.id.tiet_clone_stamp_stroke_width);
        final TextInputEditText tietEraserBlurRadius = findViewById(R.id.tiet_eraser_blur_radius);
        final TextInputEditText tietEraserStrokeWidth = findViewById(R.id.tiet_eraser_stroke_width);
        tietGradientBlurRadius = findViewById(R.id.tiet_gradient_blur_radius);
        tietGradientStrokeWidth = findViewById(R.id.tiet_gradient_stroke_width);
        tietMagicEraserStrokeWidth = findViewById(R.id.tiet_magic_eraser_stroke_width);
        tietMagicPaintBlurRadius = findViewById(R.id.tiet_magic_paint_blur_radius);
        tietMagicPaintStrokeWidth = findViewById(R.id.tiet_magic_paint_stroke_width);
        tietPatcherBlurRadius = findViewById(R.id.tiet_patcher_blur_radius);
        tietPatcherStrokeWidth = findViewById(R.id.tiet_patcher_stroke_width);
        tietPathBlurRadius = findViewById(R.id.tiet_path_blur_radius);
        tietPathStrokeWidth = findViewById(R.id.tiet_path_stroke_width);
        tietPencilBlurRadius = findViewById(R.id.tiet_pencil_blur_radius);
        tietPencilStrokeWidth = findViewById(R.id.tiet_pencil_stroke_width);
        tietShapeStrokeWidth = findViewById(R.id.tiet_shape_stroke_width);
        tietText = findViewById(R.id.tiet_text);
        final TextInputEditText tietTextSize = findViewById(R.id.tiet_text_size);
        tvStatus = findViewById(R.id.tv_status);
        vBackgroundColor = findViewById(R.id.v_background_color);
        vForegroundColor = findViewById(R.id.v_foreground_color);
        final ViewModel viewModel = new ViewModelProvider(this).get(ViewModel.class);

        findViewById(R.id.b_bucket_fill_tolerance).setOnClickListener(onClickToleranceButtonListener);
        findViewById(R.id.b_clone_stamp_src).setOnClickListener(onClickCloneStampSrcButtonListener);
        findViewById(R.id.b_magic_paint_tolerance).setOnClickListener(onClickToleranceButtonListener);
        findViewById(R.id.b_text_draw).setOnClickListener(v -> drawTextOnCanvas());
        cbCloneStampAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.cb_eraser_anti_alias)).setOnCheckedChangeListener((buttonView, isChecked) -> eraser.setAntiAlias(isChecked));
        cbGradientAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        cbMagicPaintAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.cb_magic_paint_clear)).setOnCheckedChangeListener(((buttonView, isChecked) -> magicPaint.setBlendMode(isChecked ? BlendMode.DST_OUT : null)));
        cbMagErAccEnabled = findViewById(R.id.cb_magic_eraser_acc_enabled);
        cbPatcherAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        cbPathAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        cbPathFill.setOnCheckedChangeListener(onFillCBCheckedChangeListener);
        cbPencilAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        cbShapeFill.setOnCheckedChangeListener(onFillCBCheckedChangeListener);
        cbTransformerFilter.setChecked(true);
        cbZoom.setOnCheckedChangeListener(onZoomToolCheckBoxCheckedChangeListener);
        cbZoom.setTag(onTouchIVWithPencilListener);
        flImageView.setOnTouchListener(onTouchIVWithPencilListener);
        ivRulerH.setOnTouchListener(onTouchRulerHListener);
        ivRulerV.setOnTouchListener(onTouchRulerVListener);
        ((CompoundButton) findViewById(R.id.rb_brush)).setOnCheckedChangeListener(onBrushRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_bucket_fill)).setOnCheckedChangeListener(onBucketFillRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_circle)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = circle);
        rbCloneStamp.setOnCheckedChangeListener(onCloneStampRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_eraser)).setOnCheckedChangeListener((OnCheckedListener) () -> onToolChange(onTouchIVWithEraserListener, svOptionsEraser));
        rbEyedropper.setOnCheckedChangeListener((OnCheckedListener) () -> onToolChange(onTouchIVWithEyedropperListener, svOptionsEyedropper));
        ((CompoundButton) findViewById(R.id.rb_gradient)).setOnCheckedChangeListener(onGradientRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_line)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = line);
        rbMagicEraser.setOnCheckedChangeListener(onMagicEraserRBCheckedChangeListener);
        rbMagicPaint.setOnCheckedChangeListener(onMagicPaintRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_oval)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = oval);
        ((CompoundButton) findViewById(R.id.rb_patcher)).setOnCheckedChangeListener(onPatcherRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_path)).setOnCheckedChangeListener(onPathRBCheckedChangeListener);
        rbPencil.setOnCheckedChangeListener(onPencilRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_rect)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = rect);
        rbRuler.setOnCheckedChangeListener(onRulerRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_selector)).setOnCheckedChangeListener((OnCheckedListener) () -> onToolChange(onTouchIVWithMarqueeListener));
        ((CompoundButton) findViewById(R.id.rb_shape)).setOnCheckedChangeListener(onShapeRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_text)).setOnCheckedChangeListener(onTextRBCheckedChangeListener);
        rbTransformer.setOnCheckedChangeListener(onTransformerRBCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_transformer_poly)).setOnCheckedChangeListener((OnCheckedListener) () -> onTransformerChange(onTouchIVWithPTListener));
        ((CompoundButton) findViewById(R.id.rb_transformer_rotation)).setOnCheckedChangeListener((OnCheckedListener) () -> onTransformerChange(onTouchIVWithRTListener));
        ((CompoundButton) findViewById(R.id.rb_transformer_scale)).setOnCheckedChangeListener((OnCheckedListener) () -> onTransformerChange(onTouchIVWithSTListener));
        ((CompoundButton) findViewById(R.id.rb_transformer_translation)).setOnCheckedChangeListener((OnCheckedListener) () -> onTransformerChange(onTouchIVWithTTListener));
        rvSwatches.setItemAnimator(new DefaultItemAnimator());
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        tietCloneStampBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        tietCloneStampStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        tietMagicEraserStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        tietMagicPaintBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        tietMagicPaintStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        tietGradientBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        tietGradientStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        tietBrushBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        tietBrushStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        tietPatcherBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        tietPatcherStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        tietPathBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        tietPathStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        tietPencilBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        tietPencilStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        tietShapeStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        tietText.addTextChangedListener((AfterTextChangedListener) s -> drawTextOnView());
        tietTextSize.addTextChangedListener(onTextSizeChangedListener);
        findViewById(R.id.tv_color_add).setOnClickListener(onClickAddSwatchViewListener);
        vBackgroundColor.setOnClickListener(onClickBackgroundColorListener);
        vForegroundColor.setOnClickListener(onClickForegroundColorListener);

        cbTextFill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            paint.setStyle(isChecked ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
            drawTextOnView();
        });

        tietEraserBlurRadius.addTextChangedListener((AfterTextChangedListener) s -> {
            try {
                final float f = Float.parseFloat(s);
                blurRadiusEraser = f;
                setBlurRadius(eraser, f);
            } catch (NumberFormatException e) {
            }
        });

        tietEraserStrokeWidth.addTextChangedListener((AfterTextChangedListener) s -> {
            try {
                final float f = Float.parseFloat(s);
                strokeHalfWidthEraser = f / 2.0f;
                eraser.setStrokeWidth(f);
            } catch (NumberFormatException e) {
            }
        });

        ((CompoundButton) findViewById(R.id.cb_magic_eraser_style)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            rgMagicEraserPosition.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            cbMagErAccEnabled.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            onTouchIVWithMagicEraserListener = isChecked
                    ? onTouchIVWithPreciseMagicEraserListener
                    : onTouchIVWithImpreciseMagicEraserListener;
            cbZoom.setTag(onTouchIVWithMagicEraserListener);
            if (!cbZoom.isChecked()) {
                flImageView.setOnTouchListener(onTouchIVWithMagicEraserListener);
            }
            if (!isChecked) {
                magErB = null;
                magErF = null;
                eraseBitmapAndInvalidateView(previewImage, ivPreview);
            }
        });

        rbEyedropper.setOnLongClickListener(v -> {
            v.setVisibility(View.GONE);
            rbRuler.setVisibility(View.VISIBLE);
            rbRuler.setChecked(true);
            return true;
        });

        {
            final CompoundButton.OnCheckedChangeListener l = (buttonView, isChecked) -> {
                if (isChecked) {
                    cbMagicEraserPosition = buttonView;
                }
            };
            rbMagicEraserLeft.setOnCheckedChangeListener(l);
            rbMagicEraserRight.setOnCheckedChangeListener(l);
        }

        rbRuler.setOnLongClickListener(v -> {
            v.setVisibility(View.GONE);
            rbEyedropper.setVisibility(View.VISIBLE);
            rbEyedropper.setChecked(true);
            return true;
        });

        {
            final LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(isLandscape ? RecyclerView.VERTICAL : RecyclerView.HORIZONTAL);
            rvSwatches.setLayoutManager(llm);
        }

        tietPencilBlurRadius.setText(String.valueOf(0.0f));
        tietPencilStrokeWidth.setText(String.valueOf(paint.getStrokeWidth()));
        tietEraserBlurRadius.setText(String.valueOf(0.0f));
        tietEraserStrokeWidth.setText(String.valueOf(eraser.getStrokeWidth()));
        tietTextSize.setText(String.valueOf(paint.getTextSize()));

        chessboard = BitmapFactory.decodeResource(getResources(), R.mipmap.chessboard);
        fileToBeOpened = getIntent().getData();
        tabs = viewModel.getTabs();

        palette = viewModel.getPalette();
        colorAdapter = new ColorAdapter(palette);
        initColorAdapter();
        rvSwatches.setAdapter(colorAdapter);

        if (isLandscape) {
            final LinearLayout ll = findViewById(R.id.ll_tl);
            OneShotPreDrawListener.add(ll, () -> {
                final int width = ll.getMeasuredHeight(), height = tabLayout.getMeasuredHeight();
                final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) ll.getLayoutParams();
                lp.width = width;
                lp.height = height;
                ll.setLayoutParams(lp);
                final float radius = height >> 1;
                ll.setPivotX(width - radius);
                ll.setPivotY(radius);
                ll.setRotation(90.0f);
            });
            Toast.makeText(this, getString(R.string.please_switch_orientation_to_vertical_to_get_all_functions), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        miHasAlpha = menu.findItem(R.id.i_has_alpha);
        miLayerColorMatrix = menu.findItem(R.id.i_layer_color_matrix);
        miLayerCurves = menu.findItem(R.id.i_layer_curves);
        miLayerDrawBelow = menu.findItem(R.id.i_layer_draw_below);
        miLayerFilterSet = menu.findItem(R.id.i_layer_filter_set);
        miLayerHsv = menu.findItem(R.id.i_layer_hsv);
        miLayerLevelUp = menu.findItem(R.id.i_layer_level_up);
        miLayerReference = menu.findItem(R.id.i_layer_reference);
        smLayerBlendModes = menu.findItem(R.id.i_blend_mode).getSubMenu();
        return true;
    }

    @Override
    protected void onDestroy() {
        recycleAllBitmaps(this);
        super.onDestroy();
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.i_cell_grid ->
                    new CellGridManager(this, tab.cellGrid, onApplyCellGridListener).show();
            case R.id.i_clone -> {
                if (transformer == null) {
                    drawFloatingLayers();
                    final Bitmap bm = hasSelection
                            ? Bitmap.createBitmap(bitmap, selection.left, selection.top, selection.width(), selection.height())
                            : Bitmap.createBitmap(bitmap);
                    transformer = new Transformer(bm, selection);
                    rbTransformer.setChecked(true);
                    drawSelectionOnView();
                } else {
                    canvas.drawBitmap(transformer.getBitmap(),
                            selection.left, selection.top,
                            PAINT_SRC_OVER);
                    addToHistory();
                }
                if (hasSelection) {
                    drawBitmapOnView(selection, true);
                } else {
                    drawBitmapOnView(true);
                }
            }
            case R.id.i_clone_as_new -> {
                final Bitmap bm = hasSelection ?
                        transformer == null ?
                                Bitmap.createBitmap(bitmap, selection.left, selection.top, selection.width(), selection.height()) :
                                Bitmap.createBitmap(transformer.getBitmap()) :
                        Bitmap.createBitmap(bitmap);
                addTab(bm, Tab.getPosOfProjEnd(tabs, tab.getBackground().getFirstFrame()));
            }
            case R.id.i_copy -> {
                if (transformer == null) {
                    if (clipboard != null) {
                        clipboard.recycle();
                    }
                    clipboard = hasSelection
                            ? Bitmap.createBitmap(bitmap, selection.left, selection.top, selection.width(), selection.height())
                            : Bitmap.createBitmap(bitmap);
                } else {
                    clipboard = Bitmap.createBitmap(transformer.getBitmap());
                }
            }
            case R.id.i_crop -> {
                if (!hasSelection) {
                    break;
                }
                drawFloatingLayers();
                final int width = selection.width(), height = selection.height();
                if (tab.isBackground) {
                    final Tab firstFrame = tab.getFirstFrame();
                    for (int i = firstFrame.getBackgroundPosition(); i < tabs.size(); ++i) {
                        final Tab frame = tabs.get(i).getBackground();
                        if (frame.getFirstFrame() != firstFrame) {
                            break;
                        }
                        i = frame.getBackgroundPosition();
                        final Bitmap bm = Bitmap.createBitmap(frame.bitmap, selection.left, selection.top, width, height);
                        resizeImage(frame, width, height,
                                ImageSizeManager.Transform.CROP, bm,
                                selection.left, selection.top);
                        bm.recycle();
                    }
                } else {
                    final Bitmap bm = Bitmap.createBitmap(bitmap, selection.left, selection.top, width, height);
                    resizeImage(tab, width, height,
                            ImageSizeManager.Transform.CROP, bm,
                            selection.left, selection.top);
                    bm.recycle();
                }
                drawBitmapOnView(true, true);
            }
            case R.id.i_cut -> {
                if (transformer == null) {
                    if (clipboard != null) {
                        clipboard.recycle();
                    }
                    if (hasSelection) {
                        clipboard = Bitmap.createBitmap(bitmap, selection.left, selection.top,
                                selection.width(), selection.height());
                        canvas.drawRect(selection.left, selection.top, selection.right, selection.bottom, eraser);
                    } else {
                        clipboard = Bitmap.createBitmap(bitmap);
                        canvas.drawColor(eraser.getColorLong(), BlendMode.SRC);
                    }
                    addToHistory();
                } else {
                    clipboard = Bitmap.createBitmap(transformer.getBitmap());
                    recycleTransformer();
                }
                if (hasSelection) {
                    drawBitmapOnView(selection, true);
                } else {
                    drawBitmapOnView(true);
                }
            }
            case R.id.i_delete -> {
                if (transformer == null) {
                    if (hasSelection) {
                        canvas.drawRect(selection.left, selection.top, selection.right, selection.bottom, eraser);
                    } else {
                        canvas.drawColor(eraser.getColorLong(), BlendMode.SRC);
                    }
                    addToHistory();
                } else {
                    recycleTransformer();
                }
                if (hasSelection) {
                    drawBitmapOnView(selection, true);
                } else {
                    drawBitmapOnView(true);
                }
                clearStatus();
            }
            case R.id.i_deselect -> {
                drawFloatingLayers();
                hasSelection = false;
                eraseBitmapAndInvalidateView(selectionImage, ivSelection);
                clearStatus();
            }
            case R.id.i_draw_color -> {
                if (transformer == null) {
                    if (hasSelection) {
                        canvas.drawRect(selection.left, selection.top, selection.right, selection.bottom, paint);
                    } else {
                        canvas.drawColor(paint.getColorLong());
                    }
                    addToHistory();
                } else {
                    transformer.getBitmap().eraseColor(paint.getColorLong());
                }
                if (hasSelection) {
                    drawBitmapOnView(selection, true);
                } else {
                    drawBitmapOnView(true);
                }
                clearStatus();
            }
            case R.id.i_file_close -> {
                final int last = tabs.size() - 1;
                final Tab firstFrame = tab.getBackground().getFirstFrame();
                boolean currProj = false;
                for (int i = last; i >= 0; --i) {
                    final Tab t = tabs.get(i);
                    if (t.isBackground) {
                        final boolean cp = t.getFirstFrame() == firstFrame; // Belongs to current project
                        if (currProj) {
                            if (!cp) {
                                break;
                            }
                        } else if (cp) {
                            currProj = true;
                        }
                    }
                    if (currProj && t != tab) {
                        closeTab(i, false);
                    }
                }
                closeTab();
            }
            case R.id.i_file_export -> export();
            case R.id.i_file_new -> {
                new NewImageDialog(this)
                        .setOnFinishSettingListener(onApplyNewImagePropertiesListener)
                        .show();
            }
            case R.id.i_file_open -> pickMultipleMedia.launch(pickVisualMediaRequest);
            case R.id.i_file_open_from_clipboard -> {
                final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (!clipboardManager.hasPrimaryClip()) {
                    break;
                }

                final ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData == null || clipData.getItemCount() < 1) {
                    break;
                }

                openFile(clipData.getItemAt(0).getUri());
            }
            case R.id.i_file_refer_to_clipboard -> {
                if (tab.filePath == null) {
                    Toast.makeText(this, getString(R.string.please_save_first), Toast.LENGTH_SHORT).show();
                    break;
                }
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                        .setPrimaryClip(ClipData.newUri(getContentResolver(), "Image",
                                FileProvider.getUriForFile(this,
                                        getApplicationContext().getPackageName() + ".provider",
                                        new File(tab.filePath))));
            }
            case R.id.i_file_save, R.id.i_save -> save();
            case R.id.i_file_save_as -> saveAs();
            case R.id.i_filter_channel_lighting -> {
                drawFloatingLayers();
                createImagePreview();
                new LightingDialog(this)
                        .setOnLightingChangeListener(onLightingChangedListener)
                        .setOnPositiveButtonClickListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_color_balance -> {
                drawFloatingLayers();
                createImagePreview();
                new ColorBalanceDialog(this)
                        .setOnColorBalanceChangeListener(onLightingChangedListener)
                        .setOnPositiveButtonClickListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_contrast -> {
                drawFloatingLayers();
                createImagePreview();
                new SeekBarDialog(this).setTitle(R.string.contrast).setMin(-10).setMax(100).setProgress(10)
                        .setOnChangeListener(onFilterContrastSeekBarChangeListener)
                        .setOnApplyListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_curves -> {
                drawFloatingLayers();
                createImagePreview();
                new CurvesDialog(this)
                        .setSource(imagePreview.getPixels())
                        .setOnCurvesChangeListener(onFilterCurvesChangedListener)
                        .setOnPositiveButtonClickListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_hsv -> {
                drawFloatingLayers();
                createImagePreview();
                new HsvDialog(this)
                        .setOnHsvChangeListener(onFilterHsvChangedListener)
                        .setOnPositiveButtonClickListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_hue_to_alpha -> {
                drawFloatingLayers();
                createImagePreview();
                new SeekBarDialog(this).setTitle(R.string.hue).setMin(0).setMax(360).setProgress(0)
                        .setOnChangeListener(onFilterHToASeekBarChangeListener)
                        .setOnApplyListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                onFilterHToASeekBarChangeListener.onChanged(0, true);
            }
            case R.id.i_filter_levels -> {
                drawFloatingLayers();
                createImagePreview();
                new LevelsDialog(this)
                        .setOnLevelsChangeListener(onFilterLevelsChangedListener)
                        .setOnPositiveButtonClickListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show()
                        .drawHistogram(imagePreview.getPixels());
                clearStatus();
            }
            case R.id.i_filter_lightness -> {
                drawFloatingLayers();
                createImagePreview();
                new SeekBarDialog(this).setTitle(R.string.lightness).setMin(-0xFF).setMax(0xFF).setProgress(0)
                        .setOnChangeListener(onFilterLightnessSeekBarChangeListener)
                        .setOnApplyListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_color_matrix -> {
                drawFloatingLayers();
                createImagePreview();
                new ColorMatrixManager(this,
                        onColorMatrixChangedListener,
                        onClickImagePreviewPBListener,
                        onCancelImagePreviewListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_saturation -> {
                drawFloatingLayers();
                createImagePreview();
                new SeekBarDialog(this).setTitle(R.string.saturation).setMin(0).setMax(100).setProgress(10)
                        .setOnChangeListener(onFilterSaturationSeekBarChangeListener)
                        .setOnApplyListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_threshold -> {
                drawFloatingLayers();
                createImagePreview();
                new SeekBarDialog(this).setTitle(R.string.threshold).setMin(0).setMax(255).setProgress(128)
                        .setOnChangeListener(onFilterThresholdSeekBarChangeListener)
                        .setOnApplyListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                onFilterThresholdSeekBarChangeListener.onChanged(128, true);
                clearStatus();
            }
            case R.id.i_filter_white_balance -> {
                drawFloatingLayers();
                createImagePreview();
                final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.white_balance)
                        .setPositiveButton(R.string.ok, onClickImagePreviewPBListener)
                        .setNegativeButton(R.string.cancel, onClickImagePreviewNBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();

                final Window window = dialog.getWindow();
                final WindowManager.LayoutParams lp = window.getAttributes();
                lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                lp.gravity = Gravity.BOTTOM;
                window.setAttributes(lp);

                final int w = imagePreview.getWidth(), h = imagePreview.getHeight();
                final int[] src = imagePreview.getPixels(), dst = new int[w * h];
                runOrStart(() -> {
                    BitmapUtils.whiteBalance(src, dst, paint.getColor());
                    imagePreview.setPixels(dst, w, h);
                    drawBitmapOnView(imagePreview.getEntire(), selection);
                });
                clearStatus();
            }
            case R.id.i_flip_horizontally -> {
                drawFloatingLayers();
                scale(-1.0f, 1.0f, false);
            }
            case R.id.i_flip_vertically -> {
                drawFloatingLayers();
                scale(1.0f, -1.0f, false);
            }
            case R.id.i_frame_clip -> {
                new AnimationClipper(this, tabs, tab.getBackground().getFirstFrame(), onApplyClipListener)
                        .show();
            }
            case R.id.i_frame_delay -> {
                final Tab frame = this.tab.getBackground();
                new EditNumberDialog(this)
                        .setTitle(R.string.delay)
                        .setOnApplyListener(number -> frame.delay = number)
                        .show(frame.delay, "ms");
            }
            case R.id.i_frame_delete -> {
                final Tab frame = tab.getBackground();
                int i = frame.getBackgroundPosition();
                for (; i >= 0; --i) {
                    final Tab tab = tabs.get(i);
                    if (tab.getBackground() != frame) {
                        break;
                    }
                    if (tab != this.tab) {
                        closeTab(i, false);
                    }
                }
                closeTab();
            }
            case R.id.i_frame_duplicate -> {
                final Tab background = tab.getBackground();
                final int backgroundPos = background.getBackgroundPosition(), newPos = backgroundPos + 1;
                final Tab newFrame = addFrame(Bitmap.createBitmap(background.bitmap), newPos, false, background.delay,
                        background.getTitle(), background.filePath, background.fileType, false);
                for (int i = backgroundPos - 1; i >= 0; --i) {
                    final Tab tab = tabs.get(i);
                    if (tab.isBackground) {
                        break;
                    }
                    addLayer(Bitmap.createBitmap(tab.bitmap), newPos,
                            tab.getLevel(), tab.left, tab.top, tab.visible, tab.getTitle(), false);
                }
                Tab.distinguishProjects(tabs);
                Tab.updateBackgroundIcons(tabs);
                Tab.computeLayerTree(tabs, newFrame);
                Tab.updateLevelIcons(tabs, newFrame);
                tabLayout.getTabAt(newPos).select();
            }
            case R.id.i_frame_new -> createFrame();
            case R.id.i_frame_unify_delays -> {
                new EditNumberDialog(this)
                        .setTitle(R.string.delay)
                        .setOnApplyListener(onApplyUniformFrameDelayListener)
                        .show(tab.getBackground().delay, "ms");
            }
            case R.id.i_guides_clear -> {
                tab.guides.clear();
                drawGridOnView();
            }
            case R.id.i_guides_new -> {
                final Guide guide = new Guide();
                tab.guides.offerFirst(guide); // Add at the front for faster removal if necessary later
                new GuideEditor(this, guide, bitmap.getWidth(), bitmap.getHeight(),
                        g -> drawGridOnView(),
                        dialog -> {
                            tab.guides.remove(guide);
                            drawGridOnView();
                        })
                        .show();
            }
            case R.id.i_has_alpha -> {
                final boolean checked = !item.isChecked();
                item.setChecked(checked);
                bitmap.setHasAlpha(checked);
                drawBitmapOnView(true);
            }
            case R.id.i_information -> {
                final StringBuilder message = new StringBuilder()
                        .append(getString(R.string.config)).append('\n').append(bitmap.getConfig()).append("\n\n")
                        .append(getString(R.string.has_alpha)).append('\n').append(bitmap.hasAlpha()).append("\n\n")
                        .append(getString(R.string.color_space)).append('\n').append(bitmap.getColorSpace());
                new AlertDialog.Builder(this)
                        .setTitle(R.string.information)
                        .setMessage(message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
            case R.id.i_layer_add_layer_mask -> {
                final Tab t = new Tab();
                t.bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                        bitmap.getConfig(), true, bitmap.getColorSpace());
                t.isBackground = false;
                t.setLevel(tab.getLevel() + 1);
                t.moveTo(tab.left, tab.top);
                t.paint.setBlendMode(BlendMode.DST_OUT);
                if (hasSelection) {
                    final Canvas cv = new Canvas(t.bitmap);
                    t.bitmap.eraseColor(Color.BLACK);
                    cv.drawRect(selection, PAINT_DST_OUT);
                }
                addTab(t, tabLayout.getSelectedTabPosition(), getString(R.string.mask));
            }
            case R.id.i_layer_alpha -> {
                new SeekBarDialog(this).setTitle(R.string.alpha_value).setMin(0x00).setMax(0xFF)
                        .setProgress(tab.paint.getAlpha())
                        .setOnChangeListener(onLayerAlphaSeekBarChangeListener)
                        .setOnApplyListener((dialog, which) -> clearStatus())
                        .setOnCancelListener(dialog -> clearStatus(), false)
                        .show();
                tvStatus.setText(String.format(
                        String.format(getString(R.string.state_alpha), settings.getArgbComponentFormat()),
                        tab.paint.getAlpha()));
            }
            case R.id.i_layer_blend_mode_clear,
                    R.id.i_layer_blend_mode_src, R.id.i_layer_blend_mode_dst,
                    R.id.i_layer_blend_mode_src_over, R.id.i_layer_blend_mode_dst_over,
                    R.id.i_layer_blend_mode_src_in, R.id.i_layer_blend_mode_dst_in,
                    R.id.i_layer_blend_mode_src_out, R.id.i_layer_blend_mode_dst_out,
                    R.id.i_layer_blend_mode_src_atop, R.id.i_layer_blend_mode_dst_atop,
                    R.id.i_layer_blend_mode_xor, R.id.i_layer_blend_mode_plus,
                    R.id.i_layer_blend_mode_modulate, R.id.i_layer_blend_mode_screen, R.id.i_layer_blend_mode_overlay,
                    R.id.i_layer_blend_mode_darken, R.id.i_layer_blend_mode_lighten,
                    R.id.i_layer_blend_mode_color_dodge, R.id.i_layer_blend_mode_color_burn,
                    R.id.i_layer_blend_mode_hard_light, R.id.i_layer_blend_mode_soft_light,
                    R.id.i_layer_blend_mode_difference, R.id.i_layer_blend_mode_exclusion, R.id.i_layer_blend_mode_multiply,
                    R.id.i_layer_blend_mode_hue, R.id.i_layer_blend_mode_saturation, R.id.i_layer_blend_mode_color, R.id.i_layer_blend_mode_luminosity -> {
                for (int i = 0; i < BLEND_MODES.length; ++i) {
                    final MenuItem mi = smLayerBlendModes.getItem(i);
                    if (mi == item) {
                        tab.paint.setBlendMode(BLEND_MODES[i]);
                        mi.setChecked(true);
                    } else if (mi.isChecked()) {
                        mi.setChecked(false);
                    }
                }
                drawBitmapOnView(true);
            }
            case R.id.i_layer_color_matrix -> {
                final boolean checked = !item.isChecked();
                final Tab.Filter filter = checked ? Tab.Filter.COLOR_MATRIX : null;
                checkLayerFilterMenuItem(filter);
                if (checked && tab.colorMatrix == null) {
                    tab.initColorMatrix();
                }
                tab.filter = filter;
                miLayerFilterSet.setEnabled(checked);
                drawBitmapOnView(true);
            }
            case R.id.i_layer_create_clipping_mask -> {
                final BlendMode blendMode = tab.drawBelow ? BlendMode.SRC : BlendMode.SRC_ATOP;
                tab.paint.setBlendMode(blendMode);
                checkLayerBlendModeMenuItem(blendMode);
                Tab.levelDown(tabs, tabLayout.getSelectedTabPosition());
                miLayerLevelUp.setEnabled(true);
                Tab.computeLayerTree(tabs, tab);
                Tab.updateLevelIcons(tabs, tab);
                drawBitmapOnView(true);
            }
            case R.id.i_layer_create_group -> {
                if (!tab.visible) {
                    break;
                }
                Tab.group(tabs, tabLayout.getSelectedTabPosition());
                miLayerLevelUp.setEnabled(tab.getLevel() > 0);
                Tab.computeLayerTree(tabs, tab);
                Tab.updateLevelIcons(tabs, tab);
                drawBitmapOnView(true);
            }
            case R.id.i_layer_curves -> {
                final boolean checked = !item.isChecked();
                final Tab.Filter filter = checked ? Tab.Filter.CURVES : null;
                checkLayerFilterMenuItem(filter);
                if (checked && tab.curves == null) {
                    tab.initCurves();
                }
                tab.filter = filter;
                miLayerFilterSet.setEnabled(checked);
                drawBitmapOnView(true);
            }
            case R.id.i_layer_delete -> closeTab();
            case R.id.i_layer_delete_invisible -> {
                final Tab background = tab.getBackground();
                Tab newBackground = background.visible ? background : null;
                for (int i = tab.getBackgroundPosition(); i >= 0; --i) {
                    final Tab t = tabs.get(i);
                    if (t.isBackground && t != background) {
                        break;
                    }
                    if (!t.visible) {
                        if (t != tab) {
                            closeTab(i, false);
                        }
                    } else if (newBackground == null && !t.isBackground) {
                        newBackground = t;
                    }
                }
                if (!tab.visible) {
                    closeTab();
                } else {
                    if (newBackground != background && newBackground != null) {
                        newBackground.inheritPropertiesFrom(background);
                    }
                    Tab.distinguishProjects(tabs);
                    Tab.updateBackgroundIcons(tabs);
                    Tab.computeLayerTree(tabs, tab);
                    Tab.updateLevelIcons(tabs, tab);
                    onTabSelectedListener.onTabSelected(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()));
                }
            }
            case R.id.i_layer_draw_below -> {
                final boolean checked = !item.isChecked();
                item.setChecked(checked);
                tab.drawBelow = checked;
                drawBitmapOnView(true);
            }
            case R.id.i_layer_duplicate -> {
                drawFloatingLayers();
                final Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                        bitmap.getConfig(), true, bitmap.getColorSpace());
                final Canvas cv = new Canvas(bm);
                if (hasSelection) {
                    cv.drawBitmap(bitmap, selection, selection, PAINT_SRC);
                } else {
                    cv.drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
                }
                addLayer(bm, tabLayout.getSelectedTabPosition(),
                        tab.getLevel(), tab.left, tab.top,
                        tab.visible, getString(R.string.copy_noun));
            }
            case R.id.i_layer_duplicate_by_color_range -> {
                drawFloatingLayers();
                createImagePreview();
                new ColorRangeDialog(this)
                        .setOnColorRangeChangeListener(onColorRangeChangedListener)
                        .setOnPositiveButtonClickListener(onApplyLayerDuplicatingByColorRangeListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
            }
            case R.id.i_layer_hsv -> {
                final boolean checked = !item.isChecked();
                final Tab.Filter filter = checked ? Tab.Filter.HSV : null;
                checkLayerFilterMenuItem(filter);
                if (checked && tab.deltaHsv == null) {
                    tab.initDeltaHsv();
                }
                tab.filter = filter;
                miLayerFilterSet.setEnabled(checked);
                drawBitmapOnView(true);
            }
            case R.id.i_layer_filter_set -> {
                if (tab.filter == null) {
                    break;
                }
                switch (tab.filter) {
                    case COLOR_MATRIX -> {
                        new ColorMatrixManager(this,
                                onLayerColorMatrixChangedListener,
                                tab.colorMatrix)
                                .show();
                        clearStatus();
                    }
                    case CURVES -> {
                        new CurvesDialog(this)
                                .setSource(bitmap)
                                .setDefaultCurves(tab.curves)
                                .setOnCurvesChangeListener((curves, stopped) -> drawBitmapOnView(stopped))
                                .setOnPositiveButtonClickListener(null)
                                .show();
                        clearStatus();
                    }
                    case HSV -> {
                        new HsvDialog(this)
                                .setOnHsvChangeListener(onLayerHsvChangedListener)
                                .setOnPositiveButtonClickListener(null)
                                .setDefaultDeltaHsv(tab.deltaHsv)
                                .show();
                        tvStatus.setText(String.format(getString(R.string.state_hsv),
                                tab.deltaHsv[0], tab.deltaHsv[1], tab.deltaHsv[2]));
                    }
                }
            }
            case R.id.i_layer_level_down -> {
                tab.levelDown();
                miLayerLevelUp.setEnabled(true);
                Tab.computeLayerTree(tabs, tab);
                Tab.updateLevelIcons(tabs, tab);
                drawBitmapOnView(true);
            }
            case R.id.i_layer_level_up -> {
                tab.levelUp();
                miLayerLevelUp.setEnabled(tab.getLevel() > 0);
                Tab.computeLayerTree(tabs, tab);
                Tab.updateLevelIcons(tabs, tab);
                drawBitmapOnView(true);
            }
            case R.id.i_layer_merge_alpha -> {
                final int pos = tabLayout.getSelectedTabPosition(), posBelow = pos + 1;
                if (posBelow >= tabs.size()) {
                    break;
                }
                drawFloatingLayers();
                final Tab tabBelow = tabs.get(posBelow);
                BitmapUtils.mergeAlpha(tab.bitmap, tabBelow.bitmap);
                selectTab(posBelow);
                addToHistory();
            }
            case R.id.i_layer_merge_as_hidden -> {
                final int j = tabLayout.getSelectedTabPosition() + 1;
                if (j >= tabs.size()) {
                    break;
                }
                drawFloatingLayers();
                HiddenImageMaker.merge(this,
                        new Bitmap[]{bitmap, tabs.get(j).bitmap},
                        onMakeHiddenImageListener);
            }
            case R.id.i_layer_merge_down -> {
                final int pos = tabLayout.getSelectedTabPosition(), posBelow = pos + 1;
                if (posBelow >= tabs.size()) {
                    break;
                }
                drawFloatingLayers();
                final Tab tabBelow = tabs.get(posBelow);
                Tab.mergeLayers(tab, tabBelow);
                closeTab(pos, false);
                Tab.distinguishProjects(tabs);
                Tab.updateVisibilityIcons(tabs, tabBelow.getBackground());
                Tab.computeLayerTree(tabs, tabBelow);
                Tab.updateLevelIcons(tabs, tabBelow);
                selectTab(pos);
                addToHistory();
            }
            case R.id.i_layer_merge_visible -> {
                drawFloatingLayers();
                final Tab background = tab.getBackground();
                final Bitmap bm = Tab.mergeLayers(background.layerTree);
                int i = tab.getBackgroundPosition();
                for (; i >= 0; --i) {
                    final Tab t = tabs.get(i);
                    if (t.getBackground() != background) {
                        break;
                    } else if (t.visible) {
                        t.removeOVCBCCListener();
                        t.setVisible(false);
                        t.visible = false;
                        t.addOVCBCCListener(getOVCBCCListener(t));
                    }
                }
                addLayer(bm, i + 1);
            }
            case R.id.i_layer_new -> {
                if (settings.getNewLayerLevel()) {
                    createLayer(bitmap.getWidth(), bitmap.getHeight(),
                            bitmap.getConfig(), bitmap.getColorSpace(),
                            tab.getLevel(), tab.left, tab.top, tabLayout.getSelectedTabPosition());
                } else {
                    final Bitmap background = tab.getBackground().bitmap;
                    createLayer(background.getWidth(), background.getHeight(),
                            background.getConfig(), background.getColorSpace(),
                            0, 0, 0, tabLayout.getSelectedTabPosition());
                }
            }
            case R.id.i_layer_reference -> {
                final boolean checked = !item.isChecked();
                tab.reference = checked;
                item.setChecked(checked);
                updateReference();
            }
            case R.id.i_layer_rename -> {
                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, onApplyLayerNameListener)
                        .setTitle(R.string.rename)
                        .setView(R.layout.file_name)
                        .show();

                final TextInputLayout til = dialog.findViewById(R.id.til_file_name);
                final TextInputEditText tiet = (TextInputEditText) til.getEditText();

                tiet.setFilters(DirectorySelector.FileNameHelper.FILTERS);
                tiet.setText(tab.getName());
                til.setHint(R.string.layer_name);
                dialog.findViewById(R.id.s_file_type).setVisibility(View.GONE);
            }
            case R.id.i_noise -> {
                drawFloatingLayers();
                createImagePreview();
                new SeekBarDialog(this).setTitle(R.string.noise).setMin(0).setMax(100).setProgress(0)
                        .setOnChangeListener(onNoiseSeekBarChangeListener)
                        .setOnApplyListener(onClickImagePreviewPBListener)
                        .setOnCancelListener(onCancelImagePreviewListener)
                        .show();
                clearStatus();
            }
            case R.id.i_paste -> {
                if (clipboard == null) {
                    break;
                }
                drawFloatingLayers();

                boolean si = !hasSelection; // Is selection invisible
                if (hasSelection) {
                    final Rect vs = getVisibleSubset();
                    si = !Rect.intersects(selection, vs);
                }
                if (si) {
                    hasSelection = true;
                    selection.left = translationX >= 0.0f ? 0 : toUnscaled(-translationX) + 1;
                    selection.top = translationY >= 0.0f ? 0 : toUnscaled(-translationY) + 1;
                }
                selection.right = selection.left + clipboard.getWidth();
                selection.bottom = selection.top + clipboard.getHeight();
                transformer = new Transformer(Bitmap.createBitmap(clipboard), selection);
                rbTransformer.setChecked(true);
                drawBitmapOnView(selection);
                drawSelectionOnView();
            }
            case R.id.i_redo -> {
                if (tab.history.canRedo()) {
                    undoOrRedo(tab.history.redo());
                }
            }
            case R.id.i_rotate_90 -> {
                drawFloatingLayers();
                rotate(90.0f, false);
            }
            case R.id.i_rotate_180 -> {
                drawFloatingLayers();
                rotate(180.0f, false);
            }
            case R.id.i_rotate_270 -> {
                drawFloatingLayers();
                rotate(270.0f, false);
            }
            case R.id.i_select_all -> {
                selectAll();
                hasSelection = true;
                drawSelectionOnView();
                clearStatus();
            }
            case R.id.i_settings -> startActivity(new Intent(this, SettingsActivity.class));
            case R.id.i_size -> {
                drawFloatingLayers();
                new ImageSizeManager(this, bitmap, onApplyImageSizeListener).show();
            }
            case R.id.i_transform -> {
                drawFloatingLayers();
                createImagePreview();
                new MatrixManager(this,
                        onMatrixChangedListener,
                        onClickImagePreviewPBListener,
                        dialog -> {
                            drawBitmapOnView(true);
                            imagePreview.recycle();
                            imagePreview = null;
                            clearStatus();
                        })
                        .show();
                clearStatus();
            }
            case R.id.i_undo -> {
                if (transformer != null) {
                    undoOrRedo(tab.history.getCurrent());
                } else if (!isShapeStopped) {
                    isShapeStopped = true;
                    eraseBitmapAndInvalidateView(previewImage, ivPreview);
                } else if (isEditingText) {
                    isEditingText = false;
                    paint.setTextSize(textSize);
                    eraseBitmapAndInvalidateView(previewImage, ivPreview);
                    hideSoftInputFromWindow();
                    llOptionsText.setVisibility(View.INVISIBLE);
                } else if (tab.history.canUndo()) {
                    undoOrRedo(tab.history.undo());
                }
            }
            case R.id.i_view_actual_pixels -> {
                translationX = tab.translationX = 0.0f;
                translationY = tab.translationY = 0.0f;
                scale = tab.scale = 1.0f;
                calculateBackgroundSizeOnView();
                drawAfterTranslatingOrScaling(false);
            }
            case R.id.i_view_fit_on_screen -> {
                fitOnScreen();
                translationX = tab.translationX;
                translationY = tab.translationY;
                scale = tab.scale;
                calculateBackgroundSizeOnView();
                drawAfterTranslatingOrScaling(false);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasNotLoaded) {
            hasNotLoaded = false;
            MAIN_LOOPER.getQueue().addIdleHandler(onUiThreadWaitForMsgSinceMAHasBeenCreatedHandler);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChange(View.OnTouchListener onImageViewTouchListener) {
        if (hasNotLoaded) {
            return;
        }
        cbZoom.setChecked(false);
        cbZoom.setTag(onImageViewTouchListener);
        flImageView.setOnTouchListener(onImageViewTouchListener);
        hideToolOptions();
        isShapeStopped = true;
        eraseBitmapAndInvalidateView(previewImage, ivPreview);
        paint.setAntiAlias(antiAlias);
        setBlurRadius(paint, blurRadius);
        paint.setStyle(style);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChange(View.OnTouchListener onImageViewTouchListener, View toolOption) {
        onToolChange(onImageViewTouchListener);
        if (toolOption != null) {
            toolOption.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onTransformerChange(View.OnTouchListener l) {
        cbTransformerFilter.setVisibility(l != onTouchIVWithTTListener ? View.VISIBLE : View.GONE);
        cbTransformerLar.setVisibility(l == onTouchIVWithSTListener ? View.VISIBLE : View.GONE);
        onTouchIVWithTransformerListener = l;
        cbZoom.setTag(l);
        if (!cbZoom.isChecked()) {
            flImageView.setOnTouchListener(l);
        }
    }

    private void openFile(Uri uri) {
        if (uri == null) {
            return;
        }
        try (final InputStream inputStream = getContentResolver().openInputStream(uri)) {
            final Bitmap bm = BitmapFactory.decodeStream(inputStream);
            openImage(bm, uri);
            bm.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openImage(Bitmap bitmap, Uri uri) {
        final int width = bitmap.getWidth(), height = bitmap.getHeight();
        final Bitmap bm = Bitmap.createBitmap(width, height,
                bitmap.getConfig(), bitmap.hasAlpha(), bitmap.getColorSpace());
        new Canvas(bm).drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
        bitmap.recycle();
        final DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        final String name = documentFile.getName(), mimeType = documentFile.getType();
        if (mimeType != null) {
            final String path;
            final Tab.FileType type = switch (mimeType) {
                case "image/jpeg" -> Tab.FileType.JPEG;
                case "image/png" -> Tab.FileType.PNG;
                case "image/gif" -> Tab.FileType.GIF;
                case "image/webp" -> Tab.FileType.WEBP;
                default -> null;
            };
            final Bitmap.CompressFormat compressFormat = switch (mimeType) {
                case "image/jpeg" -> Bitmap.CompressFormat.JPEG;
                case "image/png" -> Bitmap.CompressFormat.PNG;
                case "image/webp" -> Bitmap.CompressFormat.WEBP_LOSSLESS;
                default -> null;
            };
            if (compressFormat != null) {
                path = UriUtils.getRealPath(this, uri);
                addTab(bm, tabs.size(), name, path, type, compressFormat);
            } else if (type == Tab.FileType.GIF) {
                path = UriUtils.getRealPath(this, uri);
                final GifDecoder gifDecoder = new GifDecoder();
                if (gifDecoder.load(path)) {
                    final int begin = tabs.size();
                    final Tab[] frames = new Tab[gifDecoder.frameNum()];
                    for (int i = 0; i < gifDecoder.frameNum(); ++i) {
                        final Tab newFrame = addFrame(gifDecoder.frame(i), tabs.size(),
                                i == 0, gifDecoder.delay(i), name, path, type);
                        frames[i] = newFrame;
                    }
                    Tab.distinguishProjects(tabs);
                    Tab.updateBackgroundIcons(tabs);
                    for (final Tab frame : frames) {
                        Tab.computeLayerTree(tabs, frame);
                    }
                    tabLayout.getTabAt(begin).select();
                } else {
                    addTab(bm, tabs.size(), name, path, type, null);
                }
            } else {
                Toast.makeText(this, R.string.not_supported_file_type, Toast.LENGTH_SHORT).show();
                addTab(bm, tabs.size(), name);
            }
        } else {
            addTab(bm, tabs.size());
        }
    }

    private void optimizeSelection() {
        final int imageWidth = bitmap.getWidth(), imageHeight = bitmap.getHeight();
        selection.sort();
        if (!selection.isEmpty()
                && selection.left < imageWidth && selection.top < imageHeight
                && selection.right > 0 && selection.bottom > 0) {
            selection.set(Math.max(0, selection.left), Math.max(0, selection.top),
                    Math.min(imageWidth, selection.right), Math.min(imageHeight, selection.bottom));
        } else {
            hasSelection = false;
        }
    }

    private static void recycleAllBitmaps(MainActivity ma) {
        if (ma.refBm != null) {
            ma.refBm.recycle();
            ma.refBm = null;
        }
        if (ma.chessboard != null) {
            ma.chessboard.recycle();
            ma.chessboard = null;
        }
        if (ma.chessboardImage != null) {
            ma.chessboardImage.recycle();
            ma.chessboardImage = null;
            ma.chessboardCanvas = null;
        }
        if (ma.clipboard != null) {
            ma.clipboard.recycle();
            ma.clipboard = null;
        }
        if (ma.gridImage != null) {
            ma.gridImage.recycle();
            ma.gridImage = null;
            ma.gridCanvas = null;
        }
        if (ma.lastMerged != null) {
            ma.lastMerged.recycle();
            ma.lastMerged = null;
        }
        if (ma.previewImage != null) {
            ma.previewImage.recycle();
            ma.previewImage = null;
            ma.previewCanvas = null;
        }
        if (ma.rulerHImage != null) {
            ma.rulerHImage.recycle();
            ma.rulerHImage = null;
        }
        if (ma.rulerVImage != null) {
            ma.rulerVImage.recycle();
            ma.rulerVImage = null;
        }
        if (ma.selectionImage != null) {
            ma.selectionImage.recycle();
            ma.selectionImage = null;
            ma.selectionCanvas = null;
        }
        if (ma.viewImage != null) {
            ma.viewImage.recycle();
            ma.viewImage = null;
            ma.viewCanvas = null;
        }
    }

    private static void recycleBitmap(Bitmap bm) {
        if (bm != null) {
            bm.recycle();
        }
    }

    private void recycleTransformer() {
        transformer.recycle();
        transformer = null;
    }

    private void resizeImage(Tab tab, int width, int height,
                             ImageSizeManager.Transform transform, @Nullable Bitmap newImage,
                             int offsetX, int offsetY) {
        final Bitmap bm = Bitmap.createBitmap(width, height,
                tab.bitmap.getConfig(), tab.bitmap.hasAlpha(), tab.bitmap.getColorSpace());
        final Canvas cv = new Canvas(bm);
        if (transform != null) {
            if (newImage == null) {
                newImage = tab.bitmap;
            }
            switch (transform) {
                case STRETCH -> cv.drawBitmap(newImage,
                        new Rect(0, 0, tab.bitmap.getWidth(), tab.bitmap.getHeight()),
                        new RectF(0.0f, 0.0f, width, height),
                        PAINT_BITMAP);
                case STRETCH_FILTER -> cv.drawBitmap(newImage,
                        new Rect(0, 0, tab.bitmap.getWidth(), tab.bitmap.getHeight()),
                        new RectF(0.0f, 0.0f, width, height),
                        PAINT_SRC);
                case CROP -> cv.drawBitmap(newImage, 0.0f, 0.0f, PAINT_BITMAP);
            }
        }
        if (tab.isBackground) {
            for (int i = tab.getBackgroundPosition() - 1; i >= 0; --i) {
                final Tab t = tabs.get(i);
                if (t.isBackground) {
                    break;
                }
                t.moveBy(-offsetX, -offsetY);
            }
        } else {
            tab.moveBy(offsetX, offsetY);
        }
        tab.bitmap.recycle();
        tab.bitmap = bm;
        addToHistory(tab);

        if (tab == MainActivity.this.tab) {
            bitmap = bm;
            canvas = cv;
            calculateBackgroundSizeOnView();

            if (transformer != null) {
                recycleTransformer();
            }
            hasSelection = false;

            drawChessboardOnView();
            drawGridOnView();
            drawSelectionOnView();

            clearStatus(); // Prevent from displaying old size
        }
    }

    private void rotate(float degrees) {
        rotate(degrees, true);
    }

    private void rotate(float degrees, boolean filter) {
        if (!hasSelection) {
            selectAll();
        }
        final int left = selection.left, top = selection.top, width = selection.width(), height = selection.height();
        final Matrix matrix = new Matrix();
        matrix.setRotate(degrees, width / 2.0f, height / 2.0f);
        final Bitmap bm = Bitmap.createBitmap(bitmap, left, top, width, height, matrix, filter);
        canvas.drawBitmap(bm, left, top, PAINT_SRC);
        bm.recycle();
        drawBitmapOnView(left, top, left + width, top + height);
        addToHistory();
    }

    private void runOrStart(final Runnable target) {
        runOrStart(target, false);
    }

    private void runOrStart(final Runnable target, final boolean wait) {
        runnableRunner.runRunnable(target, wait);
    }

    private static int satX(Bitmap bitmap, int x) {
        if (x <= 0) return 0;
        final int w = bitmap.getWidth();
        return x >= w ? w - 1 : x;
    }

    private static int satY(Bitmap bitmap, int y) {
        if (y <= 0) return 0;
        final int h = bitmap.getHeight();
        return y >= h ? h - 1 : y;
    }

    private static float saturate(float v) {
        return v <= 0.0f ? 0.0f : v >= 1.0f ? 1.0f : v;
    }

    private void save() {
        final Tab tab = this.tab.getBackground().getFirstFrame();
        if (tab.filePath == null) {
            if (!checkOrRequestPermission()) {
                return;
            }
            saveAs();
            return;
        }
        int quality = 100;
        if (tab.fileType == Tab.FileType.GIF) {
            if (tab.gifEncodingType == null) {
                saveInQuality();
                return;
            }
        } else if (tab.fileType != Tab.FileType.PNG) {
            quality = tab.quality;
            if (quality < 0) {
                saveInQuality();
                return;
            }
        }

        drawFloatingLayers();

        final File file = new File(tab.filePath);
        if (tab.compressFormat != null) {
            final Bitmap merged = Tab.mergeLayers(tab.layerTree);
            try (final FileOutputStream fos = new FileOutputStream(file)) {
                merged.compress(tab.compressFormat, quality, fos);
                fos.flush();
            } catch (IOException e) {
                Toast.makeText(this, getString(R.string.failed) + '\n' + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                merged.recycle();
            }
            MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);

        } else if (tab.fileType == Tab.FileType.GIF) {
            final GifEncoder gifEncoder = new GifEncoder();
            final int width = tab.bitmap.getWidth(), height = tab.bitmap.getHeight();
            try {
                gifEncoder.init(width, height, tab.filePath);
            } catch (FileNotFoundException e) {
                return;
            }
            gifEncoder.setDither(tab.gifDither);
            final int size = tabs.size();
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.exporting)
                    .setView(R.layout.progress_bar)
                    .show();
            final ProgressBar pb = dialog.findViewById(R.id.progress_bar);
            pb.setMax(size - 1);
            new Thread(() -> {
                final List<String> invalidFrames = new LinkedList<>();
                for (int i = tab.getBackgroundPosition(); i < size; ++i) {
                    final Tab t = tabs.get(i).getBackground();
                    i = t.getBackgroundPosition();
                    if (t.getFirstFrame() != tab) {
                        break;
                    }
                    if (t.bitmap.getWidth() == width && t.bitmap.getHeight() == height
                            && t.bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
                        final Bitmap merged = Tab.mergeLayers(t.layerTree);
                        gifEncoder.encodeFrame(merged, t.delay);
                        merged.recycle();
                    } else {
                        invalidFrames.add(String.valueOf(i));
                    }
                    final int progress = i;
                    runOnUiThread(() -> pb.setProgress(progress));
                }
                gifEncoder.close();
                MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);
                runOnUiThread(() -> {
                    dialog.dismiss();
                    if (invalidFrames.isEmpty()) {
                        Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.done)
                                .setMessage(String.format(
                                        getString(R.string.there_are_frames_invalid_which_are),
                                        invalidFrames.size(),
                                        String.join(", ", invalidFrames)))
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                });
            }).start();
        }

    }

    private void saveAs() {
        dirSelector.open(tab.getBackground().getFirstFrame(), this::saveInQuality);
    }

    private void saveInQuality() {
        setQuality(this::save);
    }

    private void scale(float x, float y) {
        scale(x, y, true);
    }

    private void scale(float x, float y, boolean filter) {
        if (!hasSelection) {
            selectAll();
        }
        final int left = selection.left, top = selection.top, width = selection.width(), height = selection.height();
        final Matrix matrix = new Matrix();
        matrix.setScale(x, y, 0.0f, 0.0f);
        final Bitmap bm = Bitmap.createBitmap(bitmap, left, top, width, height, matrix, filter);
        canvas.drawBitmap(bm, left, top, PAINT_SRC);
        bm.recycle();
        drawBitmapOnView(left, top, left + width, top + height);
        addToHistory();
    }

    private void selectAll() {
        selection.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    private void selectTab(int position) {
        final TabLayout.Tab tabToBeSelected = tabLayout.getTabAt(position);
        if (position != tabLayout.getSelectedTabPosition()) {
            tabToBeSelected.select();
        } else {
            onTabSelectedListener.onTabSelected(tabToBeSelected);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    void setArgbColorType() {
        onTouchIVWithEyedropperListener = settings.getArgbColorType()
                ? onTouchIVWithPreciseEyedropperListener : onTouchIVWithImpreciseEyedropperListener;
        if (rbEyedropper != null && rbEyedropper.isChecked()) {
            cbZoom.setTag(onTouchIVWithEyedropperListener);
            if (!cbZoom.isChecked()) {
                flImageView.setOnTouchListener(onTouchIVWithEyedropperListener);
            }
        }
    }

    private void setBlurRadius(Paint paint, float f) {
        paint.setMaskFilter(f > 0.0f ? new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL) : null);
    }

    void setFilterBitmap(boolean filterBitmap) {
        bitmapPaint.setFilterBitmap(filterBitmap);
        if (!hasNotLoaded) {
            drawBitmapOnView(true);
        }
    }

    private void setQuality(Runnable callback) {
        final Tab tab = this.tab.getBackground().getFirstFrame();

        switch (tab.fileType) {
            case PNG -> save();
            case GIF -> {
                new QualityManager(this,
                        tab.gifEncodingType == null ? GifEncoder.EncodingType.ENCODING_TYPE_NORMAL_LOW_MEMORY : tab.gifEncodingType,
                        tab.gifDither,
                        (encodingType, dither) -> {
                            tab.gifEncodingType = encodingType;
                            tab.gifDither = dither;
                            callback.run();
                        })
                        .show();
            }
            default -> {
                new QualityManager(this,
                        tab.quality < 0 ? 100 : tab.quality,
                        tab.compressFormat,
                        (quality, format) -> {
                            tab.quality = quality;
                            tab.compressFormat = format;
                            callback.run();
                        })
                        .show();
            }
        }
    }

    void setRunnableRunner(boolean multithreaded) {
        runnableRunner = multithreaded ? runnableStartingRunner : runnableRunningRunner;
    }

    private void setSelection(int fromX, int fromY, int toX, int toY) {
        if (fromX < toX) {
            selection.left = fromX;
            selection.right = toX;
        } else {
            selection.left = toX - 1;
            selection.right = fromX + 1;
        }
        if (fromY < toY) {
            selection.top = fromY;
            selection.bottom = toY;
        } else {
            selection.top = toY - 1;
            selection.bottom = fromY + 1;
        }
        drawSelectionOnView();
    }

    private boolean stretchByDraggedMarqueeBound(float viewX, float viewY) {
        final boolean hasDragged = dragMarqueeBound(viewX, viewY);
        if (cbTransformerLar.isChecked()) {
            if (marqueeBoundBeingDragged == Position.LEFT || marqueeBoundBeingDragged == Position.RIGHT) {
                final double halfHeight = selection.width() / transformer.getAspectRatio() / 2.0;
                selection.top = (int) (transformer.getCenterY() - halfHeight);
                selection.bottom = (int) (transformer.getCenterY() + halfHeight);
            } else if (marqueeBoundBeingDragged == Position.TOP || marqueeBoundBeingDragged == Position.BOTTOM) {
                final double halfWidth = selection.height() * transformer.getAspectRatio() / 2.0;
                selection.left = (int) (transformer.getCenterX() - halfWidth);
                selection.right = (int) (transformer.getCenterX() + halfWidth);
            }
        }
        drawSelectionOnView(true);
        return hasDragged;
    }

    private void swapColor() {
        final long backgroundColor = paint.getColorLong(), foregroundColor = eraser.getColorLong();
        paint.setColor(foregroundColor);
        eraser.setColor(backgroundColor);
        vForegroundColor.setBackgroundColor(Color.toArgb(foregroundColor));
        vBackgroundColor.setBackgroundColor(Color.toArgb(backgroundColor));
        if (isEditingText) {
            drawTextOnView();
        }
    }

    /**
     * @return The x coordinate on bitmap.
     */
    private int toBitmapX(float x) {
        return (int) ((x - translationX) / scale) - tab.left;
    }

    private int toBitmapXAbs(float x) {
        return (int) ((x - translationX) / scale);
    }

    /**
     * @return The y coordinate on bitmap.
     */
    private int toBitmapY(float y) {
        return (int) ((y - translationY) / scale) - tab.top;
    }

    private int toBitmapYAbs(float y) {
        return (int) ((y - translationY) / scale);
    }

    private int toUnscaled(float scaled) {
        return (int) (scaled / scale);
    }

    private float toUnscaledExact(float scaled) {
        return scaled / scale;
    }

    private float toScaled(int unscaled) {
        return unscaled * scale;
    }

    private float toScaled(float unscaled) {
        return unscaled * scale;
    }

    /**
     * @return The x coordinate on view.
     */
    private float toViewX(int x) {
        return translationX + (x + tab.left) * scale;
    }

    /**
     * @return The x coordinate on view.
     */
    private float toViewX(float x) {
        return translationX + (x + tab.left) * scale;
    }

    private float toViewXRel(int x) {
        return translationX + x * scale;
    }

    /**
     * @return The y coordinate on view.
     */
    private float toViewY(int y) {
        return translationY + (y + tab.top) * scale;
    }

    /**
     * @return The y coordinate on view.
     */
    private float toViewY(float y) {
        return translationY + (y + tab.top) * scale;
    }

    private float toViewYRel(int y) {
        return translationY + y * scale;
    }

    private void undoOrRedo(Bitmap bitmap) {
        optimizeSelection();
        this.bitmap.recycle();
        this.bitmap = bitmap;
        tab.bitmap = this.bitmap;
        canvas = new Canvas(this.bitmap);

        calculateBackgroundSizeOnView();

        if (transformer != null) {
            recycleTransformer();
        }

        miHasAlpha.setChecked(this.bitmap.hasAlpha());

        optimizeSelection();
        isShapeStopped = true;
        marqueeBoundBeingDragged = null;
        if (magErB != null && magErF != null) {
            drawCrossOnView(magErB.x, magErB.y, true);
            drawCrossOnView(magErF.x, magErF.y, false);
        } else {
            eraseBitmapAndInvalidateView(previewImage, ivPreview);
        }

        drawBitmapOnView(true, true);
        drawChessboardOnView();
        drawGridOnView();
        drawSelectionOnView();

        if (cloneStampSrc != null) {
            drawCrossOnView(cloneStampSrc.x, cloneStampSrc.y);
        }

        clearStatus();
    }

    private void updateReference() {
        if (refBm != null) {
            refBm.recycle();
        }
        if (tab.reference) {
            refBm = checkIfHaveSelectedToolNeedsRef() ? Bitmap.createBitmap(bitmap) : null;
        } else {
            final Bitmap rb = Tab.mergeReferenceLayers(tabs, tab);
            refBm = rb != null ? rb : checkIfHaveSelectedToolNeedsRef() ? Bitmap.createBitmap(bitmap) : null;
        }
    }
}