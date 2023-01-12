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
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorLong;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private enum Position {
        LEFT(R.string.left),
        TOP(R.string.top),
        RIGHT(R.string.right),
        BOTTOM(R.string.bottom),
        NULL;

        @StringRes
        private int name;

        Position() {
        }

        Position(@StringRes int name) {
            this.name = name;
        }
    }

    private static final BlendMode[] BLEND_MODES = BlendMode.values();

    private static final Looper MAIN_LOOPER = Looper.getMainLooper();

    private static final Pattern PATTERN_FILE_NAME = Pattern.compile("[\"*/:<>?\\\\|]");
    private static final Pattern PATTERN_TREE = Pattern.compile("^content://com\\.android\\.externalstorage\\.documents/tree/primary%3A(?<path>.*)$");

    private static final String STRING_EMPTY = "";

    private static final Bitmap.CompressFormat[] COMPRESS_FORMATS = {
            Bitmap.CompressFormat.PNG,
            Bitmap.CompressFormat.JPEG
    };

    private static final InputFilter[] FILTERS_FILE_NAME = new InputFilter[]{
            (source, sourceStart, sourceEnd, dest, destStart, destEnd) -> {
                final Matcher matcher = PATTERN_FILE_NAME.matcher(source.toString());
                if (matcher.find()) {
                    return STRING_EMPTY;
                }
                return null;
            }
    };

    private static final Paint PAINT_BLACK = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
            setColor(Color.BLACK);
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

    private Bitmap bitmap;
    private Bitmap bitmapSource;
    private Bitmap chessboard;
    private Bitmap chessboardBitmap;
    private Bitmap clipboard;
    private Bitmap gridBitmap;
    private Bitmap lastMerged;
    private Bitmap previewBitmap;
    private Bitmap rulerHBitmap, rulerVBitmap;
    private Bitmap selectionBitmap;
    private Bitmap viewBitmap;
    private boolean antiAlias = false;
    private boolean hasDragged = false;
    private boolean hasNotLoaded = true;
    private boolean hasSelection = false;
    private boolean isDraggingCorner = false;
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
    private CheckBox cbPencilAntiAlias;
    private CheckBox cbPencilWithEraser;
    private CheckBox cbShapeFill;
    private CheckBox cbTextFill;
    private CheckBox cbTransformerLar;
    private CheckBox cbZoom;
    private ColorAdapter colorAdapter;
    private double prevDiagonal;
    private double prevTheta;
    private EditText etCloneStampBlurRadius;
    private EditText etCloneStampStrokeWidth;
    private EditText etEraserBlurRadius;
    private EditText etEraserStrokeWidth;
    private EditText etGradientBlurRadius;
    private EditText etGradientStrokeWidth;
    private EditText etMagicEraserStrokeWidth;
    private EditText etMagicPaintBlurRadius;
    private EditText etMagicPaintStrokeWidth;
    private EditText etPatcherBlurRadius;
    private EditText etPatcherStrokeWidth;
    private EditText etPencilBlurRadius;
    private EditText etPencilStrokeWidth;
    private EditText etShapeStrokeWidth;
    private EditText etText;
    private EditText etTextSize;
    private float blurRadius = 0.0f, blurRadiusEraser = 0.0f;
    private float pivotX, pivotY;
    private float prevX, prevY;
    private float scale;
    private float strokeWidth = 1.0f, strokeHalfWidthEraser = 0.5f;
    private float textSize = 12.0f;
    private float translationX, translationY;
    private FrameLayout flImageView;
    private FrameLayout flToolOptions;
    private HorizontalScrollView hsvOptionsBucketFill;
    private HorizontalScrollView hsvOptionsCloneStamp;
    private HorizontalScrollView hsvOptionsMagicPaint;
    private HorizontalScrollView hsvOptionsPencil;
    private ImageView imageView;
    private ImageView ivChessboard;
    private ImageView ivGrid;
    private ImageView ivPreview;
    private ImageView ivRulerH, ivRulerV;
    private ImageView ivSelection;
    private InputMethodManager inputMethodManager;
    private int imageWidth, imageHeight;
    private int selectionBeginX, selectionBeginY;
    private int selectionEndX, selectionEndY;
    private int shapeStartX, shapeStartY;
    private int textX, textY;
    private int threshold;
    private int viewWidth, viewHeight;
    private LayerTree layerTree;
    private LinearLayout llOptionsEraser;
    private LinearLayout llOptionsEyedropper;
    private LinearLayout llOptionsGradient;
    private LinearLayout llOptionsMagicEraser;
    private LinearLayout llOptionsPatcher;
    private LinearLayout llOptionsShape;
    private LinearLayout llOptionsText;
    private LinearLayout llOptionsTransformer;
    private LinkedList<Long> palette;
    private final List<Tab> tabs = new ArrayList<>();
    private MenuItem miHasAlpha;
    private MenuItem miLayerColorFilter;
    private MenuItem miLayerCurves;
    private MenuItem miLayerDrawBelow;
    private MenuItem miLayerFilterSet;
    private MenuItem miLayerHsv;
    private MenuItem miLayerLevelUp;
    private Point cloneStampSrc;
    private final Point cloneStampSrcDist = new Point(0, 0); // Distance
    private Point magErB, magErF; // Magic eraser background and foreground
    private final PointF magErBD = new PointF(0.0f, 0.0f), magErFD = new PointF(0.0f, 0.0f); // Distance
    private Position draggingBound = Position.NULL;
    private PreviewBitmap preview;
    private RadioButton rbCloneStamp;
    private RadioButton rbEyedropper;
    private RadioButton rbEyedropperAllLayers;
    private RadioButton rbMagicEraserLeft, rbMagicEraserRight;
    private CompoundButton cbMagicEraserPosition;
    private RadioButton rbPencil;
    private RadioButton rbTransformer;
    private final Rect selection = new Rect();
    private final Ruler ruler = new Ruler();
    private Settings settings;
    private String tree = "";
    private SubMenu smBlendModes;
    private Tab tab;
    private TabLayout tabLayout;
    private TextView tvStatus;
    private Thread thread = new Thread();
    private Transformer transformer;
    private Uri fileToBeOpened;
    private View vBackgroundColor;
    private View vForegroundColor;

    @ColorLong
    private long color0;

    private final Paint colorPaint = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
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
            setColor(Color.BLACK);
            setStyle(Style.FILL_AND_STROKE);
        }
    };

    private final Paint imageBound = new Paint() {
        {
            setColor(Color.DKGRAY);
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

    private Paint pencil;

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

    private final Paint textLine = new Paint() {
        {
            setColor(Color.BLUE);
            setStrokeWidth(2.0f);
        }
    };

    private final CompoundButton.OnCheckedChangeListener onAntiAliasCheckedChangeListener = (buttonView, isChecked) -> {
        antiAlias = isChecked;
        paint.setAntiAlias(isChecked);
    };

    private final DialogInterface.OnClickListener onFileNameDialogPosButtonClickListener = (dialog, which) -> {
        final EditText etFileName = ((AlertDialog) dialog).findViewById(R.id.et_file_name);
        final AppCompatSpinner sFileType = ((AlertDialog) dialog).findViewById(R.id.s_file_type);
        final String fileName = etFileName.getText().toString() + sFileType.getSelectedItem().toString();
        if (fileName.length() <= 0) {
            return;
        }
        tab.path = Environment.getExternalStorageDirectory().getPath() + File.separator + tree + File.separator + fileName;
        tab.compressFormat = COMPRESS_FORMATS[sFileType.getSelectedItemPosition()];
        save();
        tab.tvTitle.setText(fileName);
    };

    private final ActivityResultCallback<List<Uri>> imagesCallback = result -> result.forEach(this::openFile);

    private final ActivityResultCallback<Uri> treeCallback = result -> {
        if (result == null) {
            return;
        }

        final Matcher matcher = PATTERN_TREE.matcher(result.toString());
        if (!matcher.find()) {
            return;
        }
        tree = matcher.group("path").replace("%2F", "/");

        final AlertDialog fileNameDialog = new AlertDialog.Builder(this)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, onFileNameDialogPosButtonClickListener)
                .setTitle(R.string.file_name)
                .setView(R.layout.file_name)
                .show();

        final EditText etFileName = fileNameDialog.findViewById(R.id.et_file_name);
        etFileName.setFilters(FILTERS_FILE_NAME);
        etFileName.setText(getTabName());
    };

    private final ActivityResultLauncher<String> getImages =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), imagesCallback);

    private final ActivityResultLauncher<Uri> getTree =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), treeCallback);

    private final AfterTextChangedListener onBlurRadiusTextChangedListener = s -> {
        try {
            final float f = Float.parseFloat(s);
            blurRadius = f;
            setBlurRadius(paint, f);
        } catch (NumberFormatException e) {
        }
    };

    private final AfterTextChangedListener onStrokeWidthTextChangedListener = s -> {
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

    private final CurvesDialog.OnCurvesChangeListener onFilterCurvesChangeListener = (curves, stopped) -> {
        runOrStart(() -> {
            final int width = preview.getWidth(), height = preview.getHeight(), area = width * height;
            final int[] src = preview.getPixels(), dst = new int[area];
            BitmapUtil.applyCurves(src, dst, curves);
            preview.setPixels(dst, width, height);
            drawPreviewBitmapOnView(stopped);
        }, stopped);
    };

    private final DialogInterface.OnCancelListener onPreviewCancelListener = dialog -> {
        drawBitmapOnView(selection, true);
        preview.recycle();
        preview = null;
        clearStatus();
    };

    private final DialogInterface.OnClickListener onPreviewConfirmListener = (dialog, which) -> {
        drawPreviewBitmapOnCanvas();
        addHistory();
        clearStatus();
    };

    private final DialogInterface.OnClickListener onLayerRenameDialogPosButtonClickListener = (dialog, which) -> {
        final EditText etFileName = ((AlertDialog) dialog).findViewById(R.id.et_file_name);
        final Editable name = etFileName.getText();
        if (name.length() <= 0) {
            return;
        }
        tab.tvTitle.setText(name);
    };

    private final View.OnClickListener onAddSwatchViewClickListener = v ->
            ArgbColorIntPicker.make(MainActivity.this,
                            R.string.add,
                            (oldColor, newColor) -> {
                                palette.offerFirst(newColor);
                                colorAdapter.notifyDataSetChanged();
                            },
                            paint.getColorLong())
                    .show();

    private final View.OnClickListener onBackgroundColorClickListener = v ->
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

    private final View.OnClickListener onCloneStampSrcButtonClickListener = v -> {
        cloneStampSrc = null;
        eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
    };

    private final View.OnClickListener onForegroundColorClickListener = v ->
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

    private final ColorRangeDialog.OnColorRangeChangeListener onColorRangeChangeListener = (hueMin, hueMax, valueMin, valueMax, stopped) -> {
        runOrStart(() -> {
            if (hueMin == 0 && hueMax == 360 && valueMin == 0x0 && valueMax == 0xFF) {
                preview.clearFilter();
            } else if (valueMin > valueMax) {
                preview.drawColor(Color.TRANSPARENT);
            } else {
                final int width = preview.getWidth(), height = preview.getHeight(), area = width * height;
                final int[] src = preview.getPixels(), dst = new int[area];
                for (int i = 0; i < area; ++i) {
                    final float h = Color.hue(src[i]);
                    final int v = Color.luminosity(src[i]);
                    dst[i] = (hueMin <= h && h <= hueMax
                            || hueMin > hueMax && (hueMin <= h || h <= hueMax))
                            && (valueMin <= v && v <= valueMax)
                            ? src[i] : Color.TRANSPARENT;
                }
                preview.setPixels(dst, width, height);
            }
            drawPreviewBitmapOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_color_range), hueMin, hueMax, valueMin, valueMax));
    };

    private final ColorRangeDialog.OnColorRangeChangeListener onLayerDuplicateByColorRangeConfirmListener = (hueMin, hueMax, valueMin, valueMax, stopped) -> {
        final Bitmap p = preview.getEntire();
        final Bitmap bm = Bitmap.createBitmap(p.getWidth(), p.getHeight(),
                p.getConfig(), true, p.getColorSpace());
        final Canvas cv = new Canvas(bm);
        if (hasSelection) {
            cv.drawBitmap(p, selection, selection, PAINT_SRC);
        } else {
            cv.drawBitmap(p, 0.0f, 0.0f, PAINT_SRC);
        }
        preview.recycle();
        preview = null;
        addBitmap(bm, tab.visible, tabLayout.getSelectedTabPosition());
        clearStatus();
    };

    private final HiddenImageMaker.OnFinishSettingListener onFinishMakingHiddenImageListener = bm -> {
        createGraphic(bm.getWidth(), bm.getHeight(), tabLayout.getSelectedTabPosition() + 2);
        canvas.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC);
        drawBitmapOnView(true, true);
        bm.recycle();
    };

    private final NewGraphicPropertiesDialog.OnFinishSettingListener onFinishSettingNewGraphicPropertiesListener = this::createGraphic;

    private final HsvDialog.OnHsvChangeListener onFilterHSVChangeListener = (deltaHsv, stopped) -> {
        runOrStart(() -> {
            if (deltaHsv[0] == 0.0f && deltaHsv[1] == 0.0f && deltaHsv[2] == 0.0f) {
                preview.clearFilter();
            } else {
                final int w = preview.getWidth(), h = preview.getHeight(), area = w * h;
                final int[] src = preview.getPixels(), dst = new int[area];
                BitmapUtil.shiftHsv(src, dst, deltaHsv);
                preview.setPixels(dst, w, h);
            }
            drawPreviewBitmapOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_hsv), deltaHsv[0], deltaHsv[1], deltaHsv[2]));
    };

    private final HsvDialog.OnHsvChangeListener onLayerHSVChangeListener = (deltaHsv, stopped) -> {
        tab.deltaHsv = deltaHsv;
        drawBitmapOnView(stopped);
        tvStatus.setText(String.format(getString(R.string.state_hsv), deltaHsv[0], deltaHsv[1], deltaHsv[2]));
    };

    private final LevelsDialog.OnLevelsChangeListener onFilterLevelsChangeListener = (inputShadows, inputHighlights, outputShadows, outputHighlights, stopped) -> {
        final float ratio = (float) (outputHighlights - outputShadows) / (float) (inputHighlights - inputShadows);
        runOrStart(() -> {
            preview.addLightingColorFilter(ratio, -inputShadows * ratio + outputShadows);
            drawPreviewBitmapOnView(stopped);
        }, stopped);
    };

    private final ChannelLighting.OnLightingChangeListener onLightingChangeListener = (lighting, stopped) -> {
        runOrStart(() -> {
            preview.addLightingColorFilter(lighting);
            drawPreviewBitmapOnView(stopped);
        }, stopped);
    };

    private final ColorMatrixManager.OnMatrixElementsChangeListener onColorMatrixChangeListener = matrix -> {
        runOrStart(() -> {
            preview.addColorMatrixColorFilter(matrix);
            drawPreviewBitmapOnView(true);
        }, true);
    };

    private final ColorMatrixManager.OnMatrixElementsChangeListener onLayerColorFilterChangeListener = matrix -> {
        tab.colorMatrix = matrix;
        drawBitmapOnView(true);
    };

    private final OnSeekBarChangeListener onThresholdChangeListener = (progress, stopped) -> {
        threshold = progress;
        runOrStart(() -> {
            if (progress == 0xFF) {
                preview.drawColor(Color.BLACK);
            } else if (progress == 0x0) {
                preview.clearFilter();
            } else {
                final int w = preview.getWidth(), h = preview.getHeight(), area = w * h;
                final int[] src = preview.getPixels(), dst = new int[area];
                for (int i = 0; i < area; ++i) {
                    final int pixel = src[i];
                    dst[i] = pixel & Color.BLACK | Color.rgb(
                            Color.red(pixel) / progress * progress,
                            Color.green(pixel) / progress * progress,
                            Color.blue(pixel) / progress * progress);
                }
                preview.setPixels(dst, 0, w, 0, 0, w, h);
            }
            drawPreviewBitmapOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_threshold), progress));
    };

    private final DialogInterface.OnClickListener onThresholdConfirmListener = (dialog, which) -> {
        onPreviewCancelListener.onCancel(dialog);
    };

    private final View.OnClickListener onThresholdButtonClickListener = v -> {
        createPreviewBitmap();
        new SeekBarDialog(this).setTitle(R.string.threshold).setMin(0x0).setMax(0xFF)
                .setOnCancelListener(onPreviewCancelListener, false)
                .setOnPositiveButtonClickListener(onThresholdConfirmListener)
                .setOnChangeListener(onThresholdChangeListener)
                .setProgress(threshold)
                .show();
        onThresholdChangeListener.onChanged(threshold, true);
    };

    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            drawFloatingLayers();

            MainActivity.this.tab = tabs.get(tab.getPosition());
            final boolean areSizesNotEqual = MainActivity.this.tab.bitmap.getWidth() != bitmap.getWidth()
                    || MainActivity.this.tab.bitmap.getHeight() != bitmap.getHeight();
            bitmap = MainActivity.this.tab.bitmap;
            canvas = new Canvas(bitmap);

            final int width = bitmap.getWidth(), height = bitmap.getHeight();
            if (settings.getIndependentTranslAndScale() || areSizesNotEqual) {
                translationX = MainActivity.this.tab.translationX;
                translationY = MainActivity.this.tab.translationY;
                scale = MainActivity.this.tab.scale;
            }
            imageWidth = (int) toScaled(width);
            imageHeight = (int) toScaled(height);

//            calculateStackingOrder();
            computeLayerTree();

            if (transformer != null) {
                recycleTransformer();
            }
            if (areSizesNotEqual) {
                hasSelection = false;
            }

            if (rbCloneStamp.isChecked()) {
                cloneStampSrc = null;
            }
            if (bitmapSource != null) {
                bitmapSource.recycle();
                bitmapSource = Bitmap.createBitmap(bitmap);
            }

            miHasAlpha.setChecked(bitmap.hasAlpha());
            miLayerColorFilter.setChecked(MainActivity.this.tab.filter == Tab.Filter.COLOR_FILTER);
            miLayerCurves.setChecked(MainActivity.this.tab.filter == Tab.Filter.CURVES);
            miLayerDrawBelow.setChecked(MainActivity.this.tab.drawBelow);
            miLayerFilterSet.setEnabled(MainActivity.this.tab.filter != null);
            miLayerHsv.setChecked(MainActivity.this.tab.filter == Tab.Filter.HSV);
            miLayerLevelUp.setEnabled(MainActivity.this.tab.level > 0);
            for (int i = 0; i < BLEND_MODES.length; ++i) {
                final MenuItem mi = smBlendModes.getItem(i);
                final BlendMode blendMode = MainActivity.this.tab.paint.getBlendMode();
                mi.setChecked(blendMode == BLEND_MODES[i]);
            }

            drawBitmapOnView(true, true);
            drawChessboardOnView();
            drawGridOnView();
            drawSelectionOnView();
            eraseBitmapAndInvalidateView(previewBitmap, ivPreview);

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
                tl.addTab(tl.newTab().setText(tabs.get(i).tvTitle.getText()), i == position);
            }
            tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab t) {
                    dialog.dismiss();
                    final int p = t.getPosition();
                    final Tab selected = tabs.remove(position);
                    final View cv = tab.getCustomView();
                    tabLayout.removeTabAt(position);
                    tabs.add(p, selected);
                    final TabLayout.Tab nt = tabLayout.newTab()
                            .setCustomView(cv)
                            .setTag(selected);
                    tabLayout.addTab(nt, p, true);
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

    private final Shape circle = new Shape() {
        @Override
        public void drawBitmapOnView(int x0, int y0, int x1, int y1) {
            final int radius = (int) Math.ceil(Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0)));
            MainActivity.this.drawBitmapOnView(x0 - radius, y0 - radius, x1 + radius, y1 + radius,
                    strokeWidth / 2.0f + blurRadius);
        }

        @Override
        public void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
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
        public void drawBitmapOnView(int x0, int y0, int x1, int y1) {
            MainActivity.this.drawBitmapOnView(x0, y0, x1, y1, strokeWidth / 2.0f + blurRadius);
        }

        @Override
        public void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
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
        public void drawBitmapOnView(int x0, int y0, int x1, int y1) {
            MainActivity.this.drawBitmapOnView(x0, y0, x1, y1, strokeWidth / 2.0f + blurRadius);
        }

        @Override
        public void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
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
        public void drawBitmapOnView(int x0, int y0, int x1, int y1) {
            MainActivity.this.drawBitmapOnView(x0, y0, x1, y1, strokeWidth / 2.0f + blurRadius);
        }

        @Override
        public void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
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

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithBucketListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                final int bx = toBitmapX(x), by = toBitmapY(y);
                if (!(0 <= bx && bx < bitmap.getWidth() && 0 <= by && by < bitmap.getHeight())) {
                    break;
                }
                if (cbBucketFillContiguous.isChecked()) {
                    floodFill(bitmap, bx, by, paint.getColor(),
                            cbBucketFillIgnoreAlpha.isChecked(), threshold);
                } else {
                    bucketFill(bitmap, bx, by, paint.getColor(),
                            cbBucketFillIgnoreAlpha.isChecked(), threshold);
                }
                drawBitmapOnView(true);
                addHistory();
                clearStatus();
                break;
            }
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onImageViewTouchWithCloneStampListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        final int bx = toBitmapX(x), by = toBitmapY(y);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (cloneStampSrc == null) {
                    break;
                }
                cloneStampSrcDist.x = cloneStampSrc.x - bx;
                cloneStampSrcDist.y = cloneStampSrc.y - by;
                prevX = x;
                prevY = y;

            case MotionEvent.ACTION_MOVE: {
                if (cloneStampSrc == null) {
                    break;
                }
                final int prevBX = toBitmapX(prevX), prevBY = toBitmapY(prevY);

                final int width = (int) (Math.abs(bx - prevBX) + strokeWidth + blurRadius * 2.0f),
                        height = (int) (Math.abs(by - prevBY) + strokeWidth + blurRadius * 2.0f);
                final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final float rad = strokeWidth / 2.0f + blurRadius;
                final float left = Math.min(prevBX, bx) - rad, top = Math.min(prevBY, by) - rad;
                final int l = (int) (left + cloneStampSrcDist.x), t = (int) (top + cloneStampSrcDist.y);
                final Canvas cv = new Canvas(bm);
                cv.drawLine(prevBX - left, prevBY - top, bx - left, by - top, paint);
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
                drawCrossOnView(bx + cloneStampSrcDist.x, by + cloneStampSrcDist.y);
                tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));

                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (cloneStampSrc == null) {
                    cloneStampSrc = new Point(bx, by);
                    drawCrossOnView(bx, by);
                    tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));
                } else {
                    drawCrossOnView(cloneStampSrc.x, cloneStampSrc.y);
                    addHistory();
                    clearStatus();
                }
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithEraserListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                prevX = x;
                prevY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int prevBX = toBitmapX(prevX), prevBY = toBitmapY(prevY);
                final int bx = toBitmapX(x), by = toBitmapY(y);
                drawLineOnCanvas(prevBX, prevBY, bx, by, eraser);
                drawBitmapOnView(prevBX, prevBY, bx, by, strokeHalfWidthEraser + blurRadiusEraser);
                tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                addHistory();
                clearStatus();
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithEyedropperImpreciseListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int bx = satX(bitmap, toBitmapX(x)), by = satY(bitmap, toBitmapY(y));
                final int color = rbEyedropperAllLayers.isChecked()
                        ? viewBitmap.getPixel((int) x, (int) y) : bitmap.getPixel(bx, by);
                paint.setColor(color);
                vForegroundColor.setBackgroundColor(color);
                tvStatus.setText(String.format(
                        String.format(getString(R.string.state_eyedropper_imprecise), settings.getArgbComponentFormat()),
                        bx, by,
                        Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color)));
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                clearStatus();
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithEyedropperPreciseListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int bx = satX(bitmap, toBitmapX(x)), by = satY(bitmap, toBitmapY(y));
                final android.graphics.Color color = rbEyedropperAllLayers.isChecked()
                        ? viewBitmap.getColor((int) x, (int) y) : bitmap.getColor(bx, by);
                paint.setColor(color.pack());
                vForegroundColor.setBackgroundColor(color.toArgb());
                tvStatus.setText(String.format(
                        getString(R.string.state_eyedropper_precise),
                        bx, by, String.valueOf(color.alpha()),
                        String.valueOf(color.red()), String.valueOf(color.green()), String.valueOf(color.blue())));
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                clearStatus();
                break;
        }
        return true;
    };

    private View.OnTouchListener onImageViewTouchWithEyedropperListener;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onImageViewTouchWithGradientListener = (v, event) -> {
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
                    color0 = bitmap.getColor(satX(bitmap, bx), satY(bitmap, by)).pack();
                    tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));
                    break;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                final float startX = toViewX(shapeStartX + 0.5f), startY = toViewY(shapeStartY + 0.5f),
                        stopX = toViewX(bx + 0.5f), stopY = toViewY(by + 0.5f);
                paint.setShader(new LinearGradient(startX, startY, stopX, stopY,
                        color0,
                        bitmap.getColor(satX(bitmap, bx), satY(bitmap, by)).pack(),
                        Shader.TileMode.CLAMP));
                eraseBitmap(previewBitmap);
                previewCanvas.drawLine(startX, startY, stopX, stopY, paint);
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
                    eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
                    addHistory();
                    clearStatus();
                }
                paint.setShader(null);
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithMagicEraserImpreciseListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                prevX = x;
                prevY = y;
                break;

            case MotionEvent.ACTION_MOVE: {
                final int bx = toBitmapX(x), by = toBitmapY(y);
                final int prevBX = toBitmapX(prevX), prevBY = toBitmapY(prevY);
                final int rad = (int) (strokeWidth / 2.0f + blurRadius);
                final float radF = toScaled(rad);
                final double theta = Math.atan2(y - prevY, x - prevX);
                final int colorLeft = bitmapSource.getPixel(
                        satX(bitmapSource, toBitmapX(x + radF * (float) Math.sin(theta))),
                        satY(bitmapSource, toBitmapY(y - radF * (float) Math.cos(theta))));
                final int colorRight = bitmapSource.getPixel(
                        satX(bitmapSource, toBitmapX(x - radF * (float) Math.sin(theta))),
                        satY(bitmapSource, toBitmapY(y + radF * (float) Math.cos(theta))));
                final int backgroundColor = cbMagicEraserPosition == rbMagicEraserLeft ? colorLeft : colorRight;
                final int foregroundColor = backgroundColor == colorLeft ? colorRight : colorLeft;

                final int left = Math.min(prevBX, bx) - rad, top = Math.min(prevBY, by) - rad,
                        right = Math.max(prevBX, bx) + rad + 1, bottom = Math.max(prevBY, by) + rad + 1;
                final int width = right - left, height = bottom - top;
                final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final Canvas cLine = new Canvas(bLine);
                cLine.drawLine(prevBX - left, prevBY - top,
                        bx - left, by - top,
                        paint);
                canvas.drawBitmap(bLine, left, top, PAINT_DST_OUT);
                final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                new Canvas(bm).drawBitmap(bitmapSource,
                        new Rect(left, top, right, bottom),
                        new Rect(0, 0, width, height),
                        PAINT_SRC);
                BitmapUtil.removeBackground(bm, foregroundColor, backgroundColor);
                cLine.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC_IN);
                bm.recycle();
                canvas.drawBitmap(bLine, left, top, PAINT_SRC_OVER);
                bLine.recycle();

                drawBitmapOnView(left, top, right, bottom);
                tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));
                prevX = x;
                prevY = y;
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                addHistory();
                clearStatus();
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithMagicEraserPreciseListener = (v, event) -> {
        switch (event.getPointerCount()) {

            case 1:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        final float x = event.getX(), y = event.getY();
                        final Rect vs = getVisibleSubset();
                        if (magErB == null || magErF == null
                                || (!vs.contains(magErB.x, magErB.y) && !vs.contains(magErF.x, magErF.y))) {
                            final int bx = toBitmapX(x), by = toBitmapY(y);
                            magErB = new Point(bx, by);
                            magErF = new Point(bx, by);
                            drawCrossOnView(bx, by);
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        addHistory();
                        break;
                }
                break;

            case 2:
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE: {
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
                        final int backgroundColor = bitmapSource.getPixel(
                                satX(bitmapSource, magErB.x), satY(bitmapSource, magErB.y));
                        final int foregroundColor = bitmapSource.getPixel(
                                satX(bitmapSource, magErF.x), satY(bitmapSource, magErF.y));

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
                        new Canvas(bm).drawBitmap(bitmapSource,
                                new Rect(left, top, right, bottom),
                                new Rect(0, 0, width, height),
                                PAINT_SRC);
                        BitmapUtil.removeBackground(bm, foregroundColor, backgroundColor);
                        cLine.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC_IN);
                        bm.recycle();
                        canvas.drawBitmap(bLine, left, top, PAINT_SRC_OVER);
                        bLine.recycle();

                        drawBitmapOnView(left, top, right, bottom);
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_DOWN: {
                        final float x0 = event.getX(0), y0 = event.getY(0);
                        final float x1 = event.getX(1), y1 = event.getY(1);
                        magErBD.set(toViewX(magErB.x) - x0, toViewY(magErB.y) - y0);
                        magErFD.set(toViewX(magErF.x) - x1, toViewY(magErF.y) - y1);
                        break;
                    }
                }
                break;
        }
        return true;
    };

    private View.OnTouchListener onImageViewTouchWithMagicEraserListener = onImageViewTouchWithMagicEraserImpreciseListener;

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithMagicPaintListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                prevX = x;
                prevY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int bx = toBitmapX(x), by = toBitmapY(y);
                final int prevBX = toBitmapX(prevX), prevBY = toBitmapY(prevY);

                final int rad = (int) (strokeWidth / 2.0f + blurRadius);
                final int left = Math.min(prevBX, bx) - rad,
                        top = Math.min(prevBY, by) - rad,
                        right = Math.max(prevBX, bx) + rad + 1,
                        bottom = Math.max(prevBY, by) + rad + 1;
                final int width = right - left, height = bottom - top;
                final int relativeX = bx - left, relativeY = by - top;
                final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final Canvas cLine = new Canvas(bLine);
                cLine.drawLine(prevBX - left, prevBY - top,
                        relativeX, relativeY,
                        paint);
                if (threshold < 0xFF) {
                    final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    final Canvas cv = new Canvas(bm);
                    cv.drawBitmap(bLine, 0.0f, 0.0f, PAINT_SRC);
                    final Rect absolute = new Rect(left, top, right, bottom),
                            relative = new Rect(0, 0, width, height);
                    cv.drawBitmap(bitmapSource, absolute, relative, PAINT_SRC_IN);
                    final Bitmap bThr = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444); // Threshold
                    floodFill(bm, bThr, relativeX, relativeY, Color.BLACK, true, threshold);
                    bm.recycle();
                    cLine.drawBitmap(bThr, 0.0f, 0.0f, PAINT_DST_IN);
                    bThr.recycle();
                }
                canvas.drawBitmap(bLine, left, top, magicPaint);
                bLine.recycle();

                drawBitmapOnView(left, top, right, bottom);
                tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                addHistory();
                clearStatus();
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithPatcherListener = (v, event) -> {
        if (!hasSelection) {
            return true;
        }
        final float radius = strokeWidth / 2.0f + blurRadius;
        if (selection.left + radius * 2.0f >= selection.right || selection.top + radius * 2.0f >= selection.bottom) {
            return true;
        }
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                createPreviewBitmap();

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
                preview.reset();
                preview.drawBitmap(bm);
                bm.recycle();
                drawBitmapOnView(preview.getEntire(), selection);
                tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                drawPreviewBitmapOnCanvas();
                preview.recycle();
                preview = null;
                addHistory();
                clearStatus();
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithPencilListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                pencil = cbPencilWithEraser.isChecked()
                        && bitmap.getColor(satX(bitmap, toBitmapX(x)), satY(bitmap, toBitmapY(y))).pack() != eraser.getColorLong()
                        ? eraser : paint;
                prevX = x;
                prevY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int prevBX = toBitmapX(prevX), prevBY = toBitmapY(prevY);
                final int bx = toBitmapX(x), by = toBitmapY(y);
                drawLineOnCanvas(prevBX, prevBY, bx, by, pencil);
                drawBitmapOnView(prevBX, prevBY, bx, by, strokeWidth / 2.0f + blurRadius);
                tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                addHistory();
                clearStatus();
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithRulerListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        final float halfScale = scale / 2.0f;
        final int bx = toBitmapX(x + halfScale), by = toBitmapY(y + halfScale);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isShapeStopped) {
                    isShapeStopped = false;
                    ruler.enabled = false;
                    eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
                    drawPointOnView(bx, by);
                    shapeStartX = bx;
                    shapeStartY = by;
                    tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));
                    break;
                }

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

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithSelectorListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                if (draggingBound == Position.NULL) {
                    if (hasSelection && checkDraggingBound(x, y) != Position.NULL) {
                        tvStatus.setText(String.format(getString(R.string.state_selected_bound),
                                getString(draggingBound.name)));
                    } else {
                        if (hasSelection && selectionBeginX == selectionEndX - 1 && selectionBeginY == selectionEndY - 1) {
                            selectionEndX = toBitmapX(x) + 1;
                            selectionEndY = toBitmapY(y) + 1;
                        } else {
                            hasSelection = true;
                            selectionBeginX = toBitmapX(x);
                            selectionBeginY = toBitmapY(y);
                            selectionEndX = selectionBeginX + 1;
                            selectionEndY = selectionBeginY + 1;
                        }
                        drawSelectionOnViewByStartsAndEnds();
                        tvStatus.setText(String.format(getString(R.string.state_start_end_size_1),
                                selectionBeginX, selectionBeginY, selectionBeginX, selectionBeginY));
                    }
                } else {
                    dragBound(x, y);
                    drawSelectionOnView();
                    tvStatus.setText(String.format(getString(R.string.state_l_t_r_b_size),
                            selection.left, selection.top, selection.right - 1, selection.bottom - 1,
                            selection.width(), selection.height()));
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                if (draggingBound == Position.NULL) {
                    selectionEndX = toBitmapX(x) + 1;
                    selectionEndY = toBitmapY(y) + 1;
                    drawSelectionOnViewByStartsAndEnds();
                    tvStatus.setText(String.format(getString(R.string.state_start_end_size),
                            selectionBeginX, selectionBeginY, selectionEndX - 1, selectionEndY - 1,
                            Math.abs(selectionEndX - selectionBeginX - 1) + 1, Math.abs(selectionEndY - selectionBeginY - 1) + 1));
                } else {
                    dragBound(x, y);
                    drawSelectionOnView();
                    tvStatus.setText(String.format(getString(R.string.state_l_t_r_b_size),
                            selection.left, selection.top, selection.right - 1, selection.bottom - 1,
                            selection.width(), selection.height()));
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                optimizeSelection();
                drawSelectionOnView();
                if (draggingBound != Position.NULL) {
                    if (hasDragged) {
                        draggingBound = Position.NULL;
                        hasDragged = false;
                        tvStatus.setText(hasSelection ?
                                String.format(getString(R.string.state_l_t_r_b_size),
                                        selection.left, selection.top, selection.right - 1, selection.bottom - 1,
                                        selection.width(), selection.height()) :
                                STRING_EMPTY);
                    }
                } else {
                    tvStatus.setText(hasSelection ?
                            String.format(getString(R.string.state_l_t_r_b_size),
                                    selection.left, selection.top, selection.right - 1, selection.bottom - 1,
                                    selection.width(), selection.height()) :
                            STRING_EMPTY);
                }
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onImageViewTouchWithShapeListener = (v, event) -> {
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
                    tvStatus.setText(String.format(getString(R.string.coordinate), bx, by));
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
                    shape.drawBitmapOnView(shapeStartX, shapeStartY, bx, by);
                    eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
                    addHistory();
                    clearStatus();
                }
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithTextListener = (v, event) -> {
        if (isEditingText) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    prevX = event.getX() - toScaled(textX);
                    prevY = event.getY() - toScaled(textY);
                    drawTextOnView();
                    break;

                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX(), y = event.getY();
                    textX = toBitmapX(x);
                    textY = toBitmapY(y);
                    drawTextOnView();
                    break;
                }
            }

        } else {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    textX = toBitmapX(event.getX());
                    textY = toBitmapY(event.getY());
                    llOptionsText.setVisibility(View.VISIBLE);
                    isEditingText = true;
                    drawTextOnView();
                    prevX = translationX;
                    prevY = translationY;
                    break;
            }
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithTransformerOfRotationListener = (v, event) -> {
        if (!hasSelection) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (selection.isEmpty()) {
                    break;
                }
                final float x = event.getX(), y = event.getY();
                if (transformer == null) {
                    createTransformer();
                }
                ivSelection.setPivotX(toViewX(selection.exactCenterX()));
                ivSelection.setPivotY(toViewY(selection.exactCenterY()));
                prevTheta = (float) Math.atan2(y - ivSelection.getPivotY(), x - ivSelection.getPivotX());
                clearStatus();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (transformer == null) {
                    break;
                }
                final float x = event.getX(), y = event.getY();
                final float degrees = (float) Math.toDegrees(Math.atan2(y - ivSelection.getPivotY(), x - ivSelection.getPivotX()) - prevTheta);
                ivSelection.setRotation(degrees);
                drawSelectionOnView();
                tvStatus.setText(String.format(getString(R.string.degrees_), degrees));
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (transformer == null) {
                    break;
                }
                final int w = transformer.getWidth(), h = transformer.getHeight();
                transformer.rotate(ivSelection.getRotation());
                ivSelection.setRotation(0.0f);
                final int w_ = transformer.getWidth(), h_ = transformer.getHeight();
                selection.left += w - w_ >> 1;
                selection.top += h - h_ >> 1;
                selection.right = selection.left + w_;
                selection.bottom = selection.top + h_;
                drawBitmapOnView(selection);
                drawSelectionOnView();
                clearStatus();
                break;
            }
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithTransformerOfScaleListener = (v, event) -> {
        if (!hasSelection) {
            return true;
        }

        switch (event.getPointerCount()) {

            case 1:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (selection.isEmpty()) {
                            break;
                        }
                        final float x = event.getX(), y = event.getY();
                        if (transformer == null) {
                            createTransformer();
                        }
                        drawSelectionOnView(false);
                        if (draggingBound == Position.NULL) {
                            if (checkDraggingBound(x, y) != Position.NULL) {
                                if (cbTransformerLar.isChecked()) {
                                    transformer.calculateByLocation();
                                }
                                tvStatus.setText(String.format(getString(R.string.state_selected_bound),
                                        getString(draggingBound.name)));
                            }
                        } else {
                            stretchByBound(x, y);
                            tvStatus.setText(String.format(getString(R.string.state_left_top),
                                    selection.left, selection.top));
                        }
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        if (transformer == null) {
                            break;
                        }
                        final float x = event.getX(), y = event.getY();
                        if (draggingBound != Position.NULL) {
                            stretchByBound(x, y);
                            tvStatus.setText(String.format(getString(R.string.state_size),
                                    selection.width(), selection.height()));
                        }
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (transformer == null) {
                            break;
                        }
                        if (draggingBound != Position.NULL && hasDragged) {
                            draggingBound = Position.NULL;
                            isDraggingCorner = false;
                            hasDragged = false;
                            final int width = selection.width(), height = selection.height();
                            if (width > 0 && height > 0) {
                                transformer.stretch(width, height);
                            } else {
                                hasSelection = false;
                                recycleTransformer();
                            }
                            drawBitmapOnView(true);
                            drawSelectionOnView(false);
                            clearStatus();
                        }
                        break;
                }
                break;

            case 2:
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE: {
                        final float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        final RectF scaledSelection = new RectF(
                                toViewX(selection.left), toViewY(selection.top),
                                toViewX(selection.right), toViewY(selection.bottom));
                        final RectF dpb = new RectF(
                                Math.min(x0 - scaledSelection.left, x1 - scaledSelection.left),
                                Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top),
                                Math.min(scaledSelection.right - x0, scaledSelection.right - x1),
                                Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1));
                        final RectF transformerDpb = transformer.getDpb();
                        if (cbTransformerLar.isChecked()) {
                            final RectF dpbDiff = new RectF(transformerDpb.left - dpb.left,
                                    transformerDpb.top - dpb.top,
                                    transformerDpb.right - dpb.right,
                                    transformerDpb.bottom - dpb.bottom);
                            if (Math.abs(dpbDiff.left) + Math.abs(dpbDiff.right) >= Math.abs(dpbDiff.top) + Math.abs(dpbDiff.bottom)) {
                                selection.left -= toUnscaled(transformerDpb.left - dpb.left);
                                selection.right += toUnscaled(transformerDpb.right - dpb.right);
                                final double width = selection.width(), height = width / transformer.getAspectRatio();
                                selection.top = (int) (transformer.getCenterY() - height / 2.0);
                                selection.bottom = (int) (transformer.getCenterY() + height / 2.0);
                                scaledSelection.top = toViewY(selection.top);
                                scaledSelection.bottom = toViewY(selection.bottom);
                                transformerDpb.top = Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top);
                                transformerDpb.bottom = Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1);
                            } else {
                                selection.top -= toUnscaled(transformerDpb.top - dpb.top);
                                selection.bottom += toUnscaled(transformerDpb.bottom - dpb.bottom);
                                final double height = selection.height(), width = height * transformer.getAspectRatio();
                                selection.left = (int) (transformer.getCenterX() - width / 2.0);
                                selection.right = (int) (transformer.getCenterX() + width / 2.0);
                                scaledSelection.left = toViewX(selection.left);
                                scaledSelection.right = toViewX(selection.right);
                                transformerDpb.left = Math.min(x0 - scaledSelection.left, x1 - scaledSelection.left);
                                transformerDpb.right = Math.min(scaledSelection.right - x0, scaledSelection.right - x1);
                            }
                        } else {
                            selection.left -= toUnscaled(transformerDpb.left - dpb.left);
                            selection.top -= toUnscaled(transformerDpb.top - dpb.top);
                            selection.right += toUnscaled(transformerDpb.right - dpb.right);
                            selection.bottom += toUnscaled(transformerDpb.bottom - dpb.bottom);
                        }
                        drawSelectionOnView();
                        tvStatus.setText(String.format(getString(R.string.state_size),
                                selection.width(), selection.height()));
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_DOWN: {
                        draggingBound = Position.NULL;
                        isDraggingCorner = false;
                        final float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        final RectF viewSelection = new RectF(
                                toViewX(selection.left), toViewY(selection.top),
                                toViewX(selection.right), toViewY(selection.bottom));
                        transformer.getDpb().set(Math.min(x0 - viewSelection.left, x1 - viewSelection.left),
                                Math.min(y0 - viewSelection.top, y1 - viewSelection.top),
                                Math.min(viewSelection.right - x0, viewSelection.right - x1),
                                Math.min(viewSelection.bottom - y0, viewSelection.bottom - y1));
                        if (cbTransformerLar.isChecked()) {
                            transformer.calculateByLocation();
                        }
                        tvStatus.setText(String.format(getString(R.string.state_size),
                                selection.width(), selection.height()));
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        final int width = selection.width(), height = selection.height();
                        if (width > 0 && height > 0) {
                            transformer.stretch(width, height);
                        } else if (transformer != null) {
                            recycleTransformer();
                        }
                        drawSelectionOnView();
                        clearStatus();
                        break;
                    }
                }
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithTransformerOfTranslationListener = (v, event) -> {
        if (!hasSelection) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                if (!selection.isEmpty()) {
                    if (transformer == null) {
                        createTransformer();
                    }
                    drawSelectionOnView(false);
                    tvStatus.setText(String.format(getString(R.string.state_left_top),
                            selection.left, selection.top));
                }
                prevX = x - toViewX(selection.left);
                prevY = y - toViewY(selection.top);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (transformer == null) {
                    break;
                }
                final float x = event.getX(), y = event.getY();
                selection.offsetTo(toBitmapX(x - prevX), toBitmapY(y - prevY));
                drawBitmapOnView();
                drawSelectionOnView(true);
                tvStatus.setText(String.format(getString(R.string.state_left_top),
                        selection.left, selection.top));
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (transformer == null) {
                    break;
                }
                drawBitmapOnView(true);
                drawSelectionOnView(false);
                break;
        }
        return true;
    };

    private View.OnTouchListener onImageViewTouchWithTransformerListener = onImageViewTouchWithTransformerOfTranslationListener;

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithZoomToolListener = (v, event) -> {
        switch (event.getPointerCount()) {

            case 1: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        final float x = event.getX(), y = event.getY();
                        tvStatus.setText(String.format(getString(R.string.coordinate),
                                toBitmapX(x), toBitmapY(y)));
                        if (lastMerged == null) {
                            drawBitmapEntireOnView();
                        }
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        final float x = event.getX(), y = event.getY();
                        final float deltaX = x - prevX, deltaY = y - prevY;
                        translationX += deltaX;
                        translationY += deltaY;
                        drawAfterTranslatingOrScaling(true);
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        drawBitmapLastOnView(true);
                        clearStatus();
                        break;
                }
                break;
            }

            case 2: {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE: {
                        final float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        final double diagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        final double diagonalRatio = diagonal / prevDiagonal;
                        final float s = (float) (scale * diagonalRatio);
                        final int scaledWidth = (int) (bitmap.getWidth() * s), scaledHeight = (int) (bitmap.getHeight() * s);
                        scale = s;
                        imageWidth = scaledWidth;
                        imageHeight = scaledHeight;
                        final float pivotX = (float) (this.pivotX * diagonalRatio), pivotY = (float) (this.pivotY * diagonalRatio);
                        translationX = translationX - pivotX + this.pivotX;
                        translationY = translationY - pivotY + this.pivotY;
                        drawAfterTranslatingOrScaling(true);
                        this.pivotX = pivotX;
                        this.pivotY = pivotY;
                        prevDiagonal = diagonal;
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_DOWN: {
                        final float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        this.pivotX = (x0 + x1) / 2.0f - translationX;
                        this.pivotY = (y0 + y1) / 2.0f - translationY;
                        prevDiagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        clearStatus();
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        final int index = 1 - event.getActionIndex();
                        final float x = event.getX(index);
                        final float y = event.getY(index);
                        tvStatus.setText(String.format(getString(R.string.coordinate),
                                toBitmapX(x), toBitmapY(y)));
                        prevX = x;
                        prevY = y;
                        break;
                    }
                }
                break;
            }
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onBucketFillRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            drawFloatingLayers();
            onToolChange(onImageViewTouchWithBucketListener);
            threshold = 0x0;
            hsvOptionsBucketFill.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onCloneStampRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithCloneStampListener);
            cbCloneStampAntiAlias.setChecked(antiAlias);
            etCloneStampBlurRadius.setText(String.valueOf(blurRadius));
            etCloneStampStrokeWidth.setText(String.valueOf(strokeWidth));
            hsvOptionsCloneStamp.setVisibility(View.VISIBLE);
        } else {
            cloneStampSrc = null;
            eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onGradientRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithGradientListener);
            cbGradientAntiAlias.setChecked(antiAlias);
            etGradientBlurRadius.setText(String.valueOf(blurRadius));
            etGradientStrokeWidth.setText(String.valueOf(strokeWidth));
            llOptionsGradient.setVisibility(View.VISIBLE);
        }
    };

    private final CompoundButton.OnCheckedChangeListener onMagicEraserRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            drawFloatingLayers();
            onToolChange(onImageViewTouchWithMagicEraserListener);
            bitmapSource = Bitmap.createBitmap(bitmap);
            paint.setAntiAlias(false);
            paint.setMaskFilter(null);
            paint.setStrokeCap(Paint.Cap.BUTT);
            etMagicEraserStrokeWidth.setText(String.valueOf(strokeWidth));
            llOptionsMagicEraser.setVisibility(View.VISIBLE);
        } else {
            paint.setStrokeCap(Paint.Cap.ROUND);
            magErB = null;
            magErF = null;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onMagicPaintRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            drawFloatingLayers();
            onToolChange(onImageViewTouchWithMagicPaintListener);
            bitmapSource = Bitmap.createBitmap(bitmap);
            threshold = 0xFF;
            cbMagicPaintAntiAlias.setChecked(antiAlias);
            etMagicPaintBlurRadius.setText(String.valueOf(blurRadius));
            etMagicPaintStrokeWidth.setText(String.valueOf(strokeWidth));
            hsvOptionsMagicPaint.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onPatcherRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithPatcherListener);
            cbPatcherAntiAlias.setChecked(antiAlias);
            etPatcherBlurRadius.setText(String.valueOf(blurRadius));
            etPatcherStrokeWidth.setText(String.valueOf(strokeWidth));
            llOptionsPatcher.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onPencilRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithPencilListener);
            cbPencilAntiAlias.setChecked(antiAlias);
            etPencilBlurRadius.setText(String.valueOf(blurRadius));
            etPencilStrokeWidth.setText(String.valueOf(strokeWidth));
            hsvOptionsPencil.setVisibility(View.VISIBLE);
        }
    };

    private final CompoundButton.OnCheckedChangeListener onRulerRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithRulerListener);
        } else {
            ruler.enabled = false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onShapeRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithShapeListener);
            cbShapeFill.setChecked(isPaintStyleFill());
            etShapeStrokeWidth.setText(String.valueOf(paint.getStrokeWidth()));
            llOptionsShape.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onTransformerRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithTransformerListener);
            llOptionsTransformer.setVisibility(View.VISIBLE);
            selector.setColor(Color.BLUE);
            drawSelectionOnView();
        } else {
            drawTransformerOnCanvas();
            draggingBound = Position.NULL;
            isDraggingCorner = false;
            selector.setColor(Color.DKGRAY);
            drawSelectionOnView();
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onTextRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithTextListener);
            cbTextFill.setChecked(isPaintStyleFill());
        } else {
            drawTextOnCanvas(false);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onZoomToolCheckBoxCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            flImageView.setOnTouchListener(onImageViewTouchWithZoomToolListener);
        } else {
            flImageView.setOnTouchListener((View.OnTouchListener) cbZoom.getTag());
        }
    };

    private final OnSeekBarChangeListener onFilterContrastSeekBarProgressChangeListener = (progress, stopped) -> {
        final float scale = progress / 10.0f, shift = 0xFF / 2.0f * (1.0f - scale);
        runOrStart(() -> {
            preview.addLightingColorFilter(scale, shift);
            drawPreviewBitmapOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_contrast), scale));
    };

    private final OnSeekBarChangeListener onFilterHToASeekBarProgressChangeListener = (progress, stopped) -> {
        runOrStart(() -> {
            final int w = preview.getWidth(), h = preview.getHeight();
            final int[] src = preview.getPixels(), dst = new int[w * h];
            BitmapUtil.setAlphaByHue(src, dst, progress);
            preview.setPixels(dst, w, h);
            drawPreviewBitmapOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_hue), (float) progress));
    };

    private final OnSeekBarChangeListener onFilterLightnessSeekBarProgressChangeListener = (progress, stopped) -> {
        runOrStart(() -> {
            preview.addLightingColorFilter(1.0f, progress);
            drawPreviewBitmapOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_lightness), progress));
    };

    private final OnSeekBarChangeListener onFilterSaturationSeekBarProgressChangeListener = (progress, stopped) -> {
        final float f = progress / 10.0f;
        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(f);
        runOrStart(() -> {
            preview.addColorMatrixColorFilter(colorMatrix.getArray());
            drawPreviewBitmapOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_saturation), f));
    };

    private final OnSeekBarChangeListener onFilterThresholdSeekBarProgressChangeListener = (progress, stopped) -> {
        final float f = -0x100 * progress;
        runOrStart(() -> {
            preview.addColorMatrixColorFilter(new float[]{
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            });
            drawPreviewBitmapOnView(stopped);
        }, stopped);
        tvStatus.setText(String.format(getString(R.string.state_threshold), progress));
    };

    private final CellGridManager.OnUpdateListener onUpdateCellGridListener = this::drawGridOnView;

    private final ImageSizeManager.OnUpdateListener onUpdateImageSizeListener = (width, height, stretch) -> {
        resizeBitmap(width, height, stretch);
        drawBitmapOnView(true, true);
        addHistory();
    };

    private final OnSeekBarChangeListener onLayerAlphaSeekBarProgressChangeListener = (progress, stopped) -> {
        tab.paint.setAlpha(progress);
        drawBitmapOnView(stopped);
        tvStatus.setText(String.format(
                String.format(getString(R.string.state_alpha), settings.getArgbComponentFormat()),
                progress));
    };

    private final OnSeekBarChangeListener onNoiseSeekBarProgressChangeListener = (progress, stopped) -> {
        runOrStart(() -> {
            if (progress == 0) {
                preview.clearFilter();
            } else if (progress == 100) {
                preview.drawColor(paint.getColor());
            } else {
                final int w = preview.getWidth(), h = preview.getHeight(), area = w * h;
                final int[] pixels = preview.getPixels(w, h, area);
                BitmapUtil.generateNoise(pixels, area, paint.getColor(), progress / 100.0f, null);
                preview.setPixels(pixels, w, h);
            }
            drawPreviewBitmapOnView(stopped);
        }, stopped);
        clearStatus();
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

    private void addBitmap(Bitmap bitmap, int position) {
        final Tab t = new Tab();
        t.bitmap = bitmap;
        t.paint = new Paint();
        t.paint.setBlendMode(BlendMode.SRC_OVER);
        addBitmap(t, position, getString(R.string.untitled));
    }

    private void addBitmap(Bitmap bitmap, boolean visible, int position) {
        final Tab t = new Tab();
        t.bitmap = bitmap;
        t.paint = new Paint();
        t.paint.setBlendMode(BlendMode.SRC_OVER);
        t.visible = visible;
        addBitmap(t, position, getString(R.string.untitled));
    }

    private void addBitmap(Bitmap bitmap, int position,
                           String title, String path, Bitmap.CompressFormat compressFormat) {
        final Tab t = new Tab();
        t.bitmap = bitmap;
        t.paint = new Paint();
        t.paint.setBlendMode(BlendMode.SRC_OVER);
        t.path = path;
        t.compressFormat = compressFormat;
        addBitmap(t, position, title);
    }

    private void addBitmap(Tab tab, int position, String title) {
        this.tab = tab;
        tabs.add(position, tab);
        tab.history = new BitmapHistory();
        addHistory();
        tab.cellGrid = new CellGrid();

        resetTranslAndScale();

        if (transformer != null) {
            recycleTransformer();
        }
        hasSelection = false;

        final TabLayout.Tab t = tabLayout.newTab()
                .setCustomView(R.layout.tab)
                .setTag(tab);
        final View customView = t.getCustomView();
        tab.cbLayerVisible = customView.findViewById(R.id.cb_layer_visible);
        tab.cbLayerVisible.setChecked(tab.visible);
        tab.cbLayerVisible.setOnCheckedChangeListener(getOnLayerVisibleCheckBoxCheckedChangeListener(tab));
        tab.tvLayerLevel = customView.findViewById(R.id.tv_layer_level);
        tab.tvTitle = customView.findViewById(R.id.tv_title);
        tab.tvTitle.setText(title);
        tabLayout.addTab(t, position, true);
    }

    private void addHistory() {
        tab.history.offer(tab.bitmap);
    }

    private void bucketFill(final Bitmap bitmap, int x, int y, @ColorInt final int color) {
        bucketFill(bitmap, x, y, color, false, 0);
    }

    private void bucketFill(final Bitmap bitmap, int x, int y, @ColorInt final int color,
                            final boolean ignoreAlpha, final int threshold) {
        int left, top, right, bottom;
        if (hasSelection) {
            left = selection.left;
            top = selection.top;
            right = selection.right;
            bottom = selection.bottom;
        } else {
            left = 0;
            top = 0;
            right = bitmap.getWidth();
            bottom = bitmap.getHeight();
        }
        if (!(left <= x && x < right && top <= y && y < bottom)) {
            return;
        }
        final int pixel = bitmap.getPixel(x, y);
        if (pixel == color && threshold == 0) {
            return;
        }
        final int w = right - left, h = bottom - top, area = w * h;
        final int[] pixels = new int[area];
        bitmap.getPixels(pixels, 0, w, left, top, w, h);
        for (int i = 0; i < area; ++i) {
            final int px = pixels[i];
            if (ignoreAlpha) {
                if (threshold == 0 ?
                        rgb(px) == rgb(pixel) :
                        checkIfColorIsWithinThreshold(
                                Color.red(pixel), Color.green(pixel), Color.blue(pixel),
                                Color.red(px), Color.green(px), Color.blue(px))) {
                    pixels[i] = px & Color.BLACK | rgb(color);
                }
            } else {
                if (threshold == 0 ?
                        px == pixel :
                        Color.alpha(px) == Color.alpha(pixel) && checkIfColorIsWithinThreshold(
                                Color.red(pixel), Color.green(pixel), Color.blue(pixel),
                                Color.red(px), Color.green(px), Color.blue(px))) {
                    pixels[i] = color;
                }
            }
        }
        bitmap.setPixels(pixels, 0, w, left, top, w, h);
    }

    private void computeLayerTree() {
        final Stack<LayerTree> stack = new Stack<>();
        final int w = bitmap.getWidth(), h = bitmap.getHeight();
        LayerTree layerTree = new LayerTree();
        LayerTree.Node prev = null;

        stack.push(layerTree);
        for (int i = tabs.size() - 1; i >= 0; --i) {
            final Tab t = tabs.get(i);
            if (isSizeEqualTo(t.bitmap, w, h)) {
                t.cbLayerVisible.setVisibility(View.VISIBLE);
                if (prev == null) {
                    prev = layerTree.offer(t);
                    continue;
                }
            } else {
                t.cbLayerVisible.setVisibility(View.GONE);
                continue;
            }
            final Tab prevTab = prev.getTab();
            final int levelDiff = t.level - prevTab.level;

            if (levelDiff == 0) {
                prev = stack.peek().offer(t);

            } else if (levelDiff > 0) {
                LayerTree lt = null;
                for (int j = 0; j < levelDiff; ++j) {
                    lt = new LayerTree();
                    prev.setBranch(lt);
                    prev = lt.offer(prevTab);
                    stack.push(lt);
                }
                prev = lt.offer(t);

            } else /* if (levelDiff < 0) */ {
                if (-levelDiff < stack.size()) {
                    for (int j = 0; j > levelDiff; --j) {
                        stack.pop();
                    }
                    prev = stack.peek().offer(t);
                } else {
                    stack.clear();
                    layerTree = stack.push(new LayerTree());
                    prev = layerTree.offer(t);
                }

            }
        }

        this.layerTree = layerTree;
    }

    private Position checkDraggingBound(float x, float y) {
        draggingBound = Position.NULL;
        isDraggingCorner = false;

        final RectF sb = new RectF(
                toViewX(selection.left), toViewY(selection.top),
                toViewX(selection.right), toViewY(selection.bottom)); // sb - Selection Bounds

        if (sb.left - 50.0f <= x && x < sb.left + 50.0f) {
            if (sb.top + 50.0f <= y && y < sb.bottom - 50.0f) {

                draggingBound = Position.LEFT;
            }
        } else if (sb.top - 50.0f <= y && y < sb.top + 50.0f) {
            if (sb.left + 50.0f <= x && x < sb.right - 50.0f) {

                draggingBound = Position.TOP;
            }
        } else if (sb.right - 50.0f <= x && x < sb.right + 50.0f) {
            if (sb.top + 50.0f <= y && y < sb.bottom - 50.0f) {

                draggingBound = Position.RIGHT;
            }
        } else if (sb.bottom - 50.0f <= y && y < sb.bottom + 50.0f) {
            if (sb.left + 50.0f <= x && x < sb.right - 50.0f) {

                draggingBound = Position.BOTTOM;
            }
        }

        return draggingBound;
    }

    private boolean checkIfColorIsWithinThreshold(int r0, int g0, int b0, int r, int g, int b) {
        return Math.abs(r - r0) <= threshold
                && Math.abs(g - g0) <= threshold
                && Math.abs(b - b0) <= threshold;
    }

    private static float clamp(float a, float min, float max) {
        return a <= min ? min : a >= max ? max : a;
    }

    private static int clamp(int a, int min, int max) {
        return a <= min ? min : a >= max ? max : a;
    }

    private void clearStatus() {
        tvStatus.setText(STRING_EMPTY);
    }

    private void closeTab() {
        closeTab(tabLayout.getSelectedTabPosition());
    }

    private void closeTab(int position) {
        final Tab tab = tabs.get(position);
        final Bitmap bm = tab.bitmap;
        final BitmapHistory h = tab.history;
        tabs.remove(position);
        tabLayout.removeTabAt(position);
        bm.recycle();
        h.clear();
    }

    private void createGraphic(int width, int height) {
        createGraphic(width, height, -1);
    }

    private void createGraphic(int width, int height, int position) {
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (position == -1) {
            position = tabs.size();
        }
        addBitmap(bm, position);
    }

    private void createLayer(int width, int height, Bitmap.Config config, boolean hasAlpha, ColorSpace colorSpace,
                             int position) {
        addBitmap(Bitmap.createBitmap(width, height,
                        config, hasAlpha, colorSpace),
                true, position);
    }

    private void createPreviewBitmap() {
        if (preview != null) {
            preview.recycle();
        }
        if (!hasSelection) {
            selectAll();
        }
        preview = new PreviewBitmap(bitmap, selection);
    }

    private void createTransformer() {
        transformer = new Transformer(
                Bitmap.createBitmap(bitmap,
                        selection.left, selection.top, selection.width(), selection.height()),
                selection);
        canvas.drawRect(selection.left, selection.top, selection.right, selection.bottom,
                eraser);
    }

    private void disableAllLayerFilters() {
        tab.filter = null;
        miLayerColorFilter.setChecked(false);
        miLayerCurves.setChecked(false);
        miLayerHsv.setChecked(false);
    }

    private void dragBound(float viewX, float viewY) {
        final float halfScale = scale / 2.0f;
        switch (draggingBound) {
            case LEFT: {
                final int left = toBitmapX(viewX + halfScale);
                if (left != selection.left) selection.left = left;
                else return;
                break;
            }
            case TOP: {
                final int top = toBitmapY(viewY + halfScale);
                if (top != selection.top) selection.top = top;
                else return;
                break;
            }
            case RIGHT: {
                final int right = toBitmapX(viewX + halfScale);
                if (right != selection.right) selection.right = right;
                else return;
                break;
            }
            case BOTTOM: {
                final int bottom = toBitmapY(viewY + halfScale);
                if (bottom != selection.bottom) selection.bottom = bottom;
                else return;
                break;
            }
            case NULL:
                return;
        }
        hasDragged = true;
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

    private void drawBitmapOnCanvas(Bitmap bitmap, Canvas canvas, float translX, float translY) {
        final Rect vs = getVisibleSubset(translX, translY, bitmap.getWidth(), bitmap.getHeight());
        drawBitmapOnCanvas(bitmap, canvas, translX, translY, vs);
    }

    private void drawBitmapOnCanvas(Bitmap bitmap, Canvas canvas, float translX, float translY, Rect vs) {
        if (vs.isEmpty()) {
            return;
        }
        final RectF svs = getScaledVisibleSubset(bitmap, translX, translY);
        if (isScaledMuch()) {
            final int w = vs.width(), h = vs.height();
            final int[] pixels = new int[w * h];
            bitmap.getPixels(pixels, 0, w, vs.left, vs.top, w, h);
            float t = svs.top, b = t + scale;
            for (int i = 0, y = vs.top; y < vs.bottom; ++y, t += scale, b += scale) {
                float l = svs.left;
                for (int x = vs.left; x < vs.right; ++x, ++i) {
                    colorPaint.setColor(pixels[i]);
                    canvas.drawRect(l, t, l += scale, b, colorPaint);
                }
            }
        } else {
            canvas.drawBitmap(bitmap, vs, svs, PAINT_SRC);
        }
    }

    private final Runnable drawBitmapOnViewRunnable = () ->
            drawBitmapSubsetOnView(bitmap,
                    0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    false, false);

    private void drawBitmapOnView() {
        runOrStart(drawBitmapOnViewRunnable);
    }

    private void drawBitmapOnView(final boolean wait) {
        runOrStart(drawBitmapOnViewRunnable, wait);
    }

    private void drawBitmapOnView(final Bitmap bitmap, final Rect rect) {
        runOrStart(() ->
                drawBitmapSubsetOnView(bitmap,
                        rect.left, rect.top, rect.right, rect.bottom,
                        false, false));
    }

    private void drawBitmapOnView(final Bitmap bitmap, final Rect rect, final boolean wait) {
        runOrStart(
                () -> drawBitmapSubsetOnView(bitmap,
                        rect.left, rect.top, rect.right, rect.bottom,
                        false, false),
                wait);
    }

    private void drawBitmapOnView(final boolean eraseVisible, final boolean wait) {
        runOrStart(
                () -> drawBitmapSubsetOnView(bitmap,
                        0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        false, eraseVisible),
                wait);
    }

    private void drawBitmapOnView(final int left, final int top, final int right, final int bottom) {
        runOrStart(() ->
                drawBitmapSubsetOnView(bitmap,
                        left, top, right, bottom,
                        false, false));
    }

    private void drawBitmapOnView(final int x0, final int y0, final int x1, final int y1, final float radius) {
        final boolean x = x0 <= x1, y = y0 <= y1;
        final int rad = (int) Math.ceil(radius);
        final int left = (x ? x0 : x1) - rad, top = (y ? y0 : y1) - rad,
                right = (x ? x1 : x0) + rad + 1, bottom = (y ? y1 : y0) + rad + 1;
        runOrStart(() ->
                drawBitmapSubsetOnView(bitmap,
                        left, top, right, bottom,
                        false, false));
    }

    private void drawBitmapOnView(final Rect rect) {
        runOrStart(() ->
                drawBitmapSubsetOnView(bitmap,
                        rect.left, rect.top, rect.right, rect.bottom,
                        false, false));
    }

    private void drawBitmapOnView(final Rect rect, final boolean wait) {
        runOrStart(
                () -> drawBitmapSubsetOnView(bitmap,
                        rect.left, rect.top, rect.right, rect.bottom,
                        false, false),
                wait);
    }

    private final Runnable drawBitmapEntireOnViewRunner = () ->
            drawBitmapSubsetOnView(bitmap,
                    0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    true, true);

    private void drawBitmapEntireOnView() {
        runOrStart(drawBitmapEntireOnViewRunner, true);
    }

    private final Runnable drawBitmapLastOnViewRunner = () -> drawBitmapLastOnView(bitmap);

    private void drawBitmapLastOnView(final boolean wait) {
        runOrStart(drawBitmapLastOnViewRunner, wait);
    }

    private void drawBitmapLastOnView(final Bitmap bitmap) {
        final Rect vs = getVisibleSubset(translationX, translationY,
                bitmap.getWidth(), bitmap.getHeight());

        runOnUiThread(() -> {
            eraseBitmap(viewBitmap);
            if (vs.isEmpty()) {
                return;
            }
            drawBitmapOnCanvas(lastMerged, viewCanvas, translationX, translationY, vs);
            imageView.invalidate();
        });
    }

    private final Runnable erasingViewRunner = () -> eraseBitmap(viewBitmap);

    private void drawBitmapSubsetOnView(final Bitmap bitmap,
                                        int left, int top, int right, int bottom,
                                        boolean mergeEntire, boolean eraseVisible) {
        final int width = bitmap.getWidth(), height = bitmap.getHeight();
        left = Math.max(left, 0);
        top = Math.max(top, 0);
        right = Math.min(right, width);
        bottom = Math.min(bottom, height);
        if (left >= right || top >= bottom) {
            return;
        }
        final Rect vs = mergeEntire
                ? new Rect(0, 0, width, height)
                : getVisibleSubset(translationX, translationY, bitmap.getWidth(), bitmap.getHeight());
        if (vs.isEmpty()) {
            runOnUiThread(erasingViewRunner);
            return;
        }
        if (!mergeEntire && !vs.intersect(left, top, right, bottom)) {
            return;
        }
        final int w = vs.width(), h = vs.height();
        final Rect relative = new Rect(0, 0, w, h);

        Bitmap excludedBitmap = null;
        boolean excludedIntegrity = true;
        try {
            if (transformer != null) {
                excludedBitmap = Bitmap.createBitmap(bitmap, vs.left, vs.top, w, h);
                excludedIntegrity = false;
                final Rect intersect = new Rect(Math.max(vs.left, selection.left), Math.max(vs.top, selection.top),
                        Math.min(vs.right, selection.right), Math.min(vs.bottom, selection.bottom));
                new Canvas(excludedBitmap).drawBitmap(transformer.getBitmap(),
                        new Rect(intersect.left - selection.left, intersect.top - selection.top,
                                intersect.right - selection.left, intersect.bottom - selection.top),
                        new Rect(intersect.left - vs.left, intersect.top - vs.top,
                                intersect.right - vs.left, intersect.bottom - vs.top),
                        PAINT_SRC_OVER);
            } else {
                excludedBitmap = bitmap;
            }

            final Bitmap merged = mergeLayers(layerTree, vs,
                    tab, excludedBitmap, excludedIntegrity);
            recycleBitmap(lastMerged);
            if (mergeEntire) {
                lastMerged = merged;
            } else {
                lastMerged = null;
                final float translLeft = toViewX(left), translTop = toViewY(top);
                runOnUiThread(() -> {
                    if (eraseVisible) {
                        eraseBitmap(viewBitmap);
                    }
                    drawBitmapOnCanvas(merged, viewCanvas,
                            translLeft > -scale ? translLeft : translLeft % scale,
                            translTop > -scale ? translTop : translTop % scale,
                            relative);
                    merged.recycle();
                    imageView.invalidate();
                });
            }

        } catch (RuntimeException e) {
            if (!excludedIntegrity) {
                excludedBitmap.recycle();
            }
            throw e;
        }
    }

    private void drawChessboardOnView() {
        eraseBitmap(chessboardBitmap);

        final float left = Math.max(0.0f, translationX);
        final float top = Math.max(0.0f, translationY);
        final float right = Math.min(translationX + imageWidth, viewWidth);
        final float bottom = Math.min(translationY + imageHeight, viewHeight);

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
            eraseBitmap(previewBitmap);
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
        eraseBitmap(gridBitmap);

        float startX = translationX >= 0.0f ? translationX : translationX % scale,
                startY = translationY >= 0.0f ? translationY : translationY % scale,
                endX = Math.min(translationX + imageWidth, viewWidth),
                endY = Math.min(translationY + imageHeight, viewHeight);
        if (isScaledMuch()) {
            for (float x = startX; x < endX; x += scale) {
                gridCanvas.drawLine(x, startY, x, endY, PAINT_GRID);
            }
            for (float y = startY; y < endY; y += scale) {
                gridCanvas.drawLine(startX, y, endX, y, PAINT_GRID);
            }
        }

        gridCanvas.drawLine(startX, startY, startX - 100.0f, startY, imageBound);
        gridCanvas.drawLine(endX, startY, endX + 100.0f, startY, imageBound);
        gridCanvas.drawLine(endX, startY - 100.0f, endX, startY, imageBound);
        gridCanvas.drawLine(endX, endY, endX, endY + 100.0f, imageBound);
        gridCanvas.drawLine(endX + 100.0f, endY, endX, endY, imageBound);
        gridCanvas.drawLine(startX, endY, startX - 100.0f, endY, imageBound);
        gridCanvas.drawLine(startX, endY + 100.0f, startX, endY, imageBound);
        gridCanvas.drawLine(startX, startY, startX, startY - 100.0f, imageBound);

        final CellGrid cellGrid = tab.cellGrid;
        if (cellGrid.enabled) {
            if (cellGrid.sizeX > 1) {
                final float scaledSizeX = toScaled(cellGrid.sizeX),
                        scaledSpacingX = toScaled(cellGrid.spacingX);
                startX = (translationX >= 0.0f ? translationX : translationX % (scaledSizeX + scaledSpacingX)) + toScaled(cellGrid.offsetX);
                startY = Math.max(0.0f, translationY);
                if (cellGrid.spacingX <= 0) {
                    float x = startX;
                    while (x < endX) {
                        gridCanvas.drawLine(x, startY, x, endY, PAINT_CELL_GRID);
                        x += scaledSizeX;
                    }
                } else {
                    float x = startX;
                    while (true) {
                        gridCanvas.drawLine(x, startY, x, endY, PAINT_CELL_GRID);
                        if ((x += scaledSizeX) >= endX) {
                            break;
                        }
                        gridCanvas.drawLine(x, startY, x, endY, PAINT_CELL_GRID);
                        if ((x += scaledSpacingX) >= endX) {
                            break;
                        }
                    }
                }
            }
            if (cellGrid.sizeY > 1) {
                final float scaledSizeY = toScaled(cellGrid.sizeY),
                        scaledSpacingY = toScaled(cellGrid.spacingY);
                startY = (translationY >= 0.0f ? translationY : translationY % (scaledSizeY + scaledSpacingY)) + toScaled(cellGrid.offsetY);
                startX = Math.max(0.0f, translationX);
                if (cellGrid.spacingY <= 0) {
                    float y = startY;
                    while (y < endY) {
                        gridCanvas.drawLine(startX, y, endX, y, PAINT_CELL_GRID);
                        y += scaledSizeY;
                    }
                } else {
                    float y = startY;
                    while (true) {
                        gridCanvas.drawLine(startX, y, endX, y, PAINT_CELL_GRID);
                        if ((y += scaledSizeY) >= endY) {
                            break;
                        }
                        gridCanvas.drawLine(startX, y, endX, y, PAINT_CELL_GRID);
                        if ((y += scaledSpacingY) >= endY) {
                            break;
                        }
                    }
                }
            }
        }

        ivGrid.invalidate();
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
        eraseBitmap(previewBitmap);
        fillPaint.setColor(paint.getColorLong());
        final float left = toViewX(x), top = toViewY(y), right = left + scale, bottom = top + scale;
        previewCanvas.drawRect(left, top, right, bottom, fillPaint);
        ivPreview.invalidate();
    }

    private void drawPreviewBitmapOnCanvas() {
        canvas.drawBitmap(preview.getEntire(), 0.0f, 0.0f, PAINT_SRC);
        drawBitmapOnView(true);
    }

    private void drawPreviewBitmapOnView() {
        drawPreviewBitmapOnView(false);
    }

    private void drawPreviewBitmapOnView(final boolean wait) {
        drawBitmapOnView(preview.getEntire(), selection, wait);
    }

    private void drawRuler() {
        eraseBitmap(rulerHBitmap);
        eraseBitmap(rulerVBitmap);
        final int multiplier = (int) Math.ceil(96.0 / scale);
        final float scaledMultiplier = toScaled(multiplier);

        rulerPaint.setTextAlign(Paint.Align.LEFT);
        int unscaledX = (int) Math.floor(-translationX / scaledMultiplier) * multiplier;
        for (float x = translationX % scaledMultiplier + (translationX <= 0.0f ? 0.0f : -scaledMultiplier),
             height = rulerHBitmap.getHeight();
             x < viewWidth;
             x += scaledMultiplier, unscaledX += multiplier) {
            rulerHCanvas.drawLine(x, 0.0f, x, height, rulerPaint);
            rulerHCanvas.drawText(String.valueOf(unscaledX), x, height, rulerPaint);
        }

        rulerPaint.setTextAlign(Paint.Align.RIGHT);
        final float ascent = rulerPaint.getFontMetrics().ascent;
        int unscaledY = (int) Math.floor(-translationY / scaledMultiplier) * multiplier;
        for (float y = translationY % scaledMultiplier + (translationY <= 0.0f ? 0.0f : -scaledMultiplier),
             width = rulerVBitmap.getWidth();
             y < viewHeight;
             y += scaledMultiplier, unscaledY += multiplier) {
            rulerVCanvas.drawLine(0.0f, y, width, y, rulerPaint);
            rulerVCanvas.drawText(String.valueOf(unscaledY), width, y - ascent, rulerPaint);
        }

        ivRulerH.invalidate();
        ivRulerV.invalidate();
    }

    private void drawRulerOnView() {
        eraseBitmap(previewBitmap);
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
        eraseBitmap(selectionBitmap);
        if (hasSelection) {
            final float left = Math.max(0.0f, toViewX(selection.left)),
                    top = Math.max(0.0f, toViewY(selection.top)),
                    right = Math.min(viewWidth, toViewX(selection.right)),
                    bottom = Math.min(viewHeight, toViewY(selection.bottom));
            selectionCanvas.drawRect(left, top, right, bottom, selector);
            if (showMargins) {
                final float imageLeft = Math.max(0.0f, translationX),
                        imageTop = Math.max(0.0f, translationY),
                        imageRight = Math.min(viewWidth, translationX + imageWidth),
                        imageBottom = Math.min(viewHeight, translationY + imageHeight);
                final float centerHorizontal = (left + right) / 2.0f,
                        centerVertical = (top + bottom) / 2.0f;
                if (left > 0.0f) {
                    selectionCanvas.drawLine(left, centerVertical, imageLeft, centerVertical, marginPaint);
                    selectionCanvas.drawText(String.valueOf(selection.left), (imageLeft + left) / 2.0f, centerVertical, marginPaint);
                }
                if (top > 0.0f) {
                    selectionCanvas.drawLine(centerHorizontal, top, centerHorizontal, imageTop, marginPaint);
                    selectionCanvas.drawText(String.valueOf(selection.top), centerHorizontal, (imageTop + top) / 2.0f, marginPaint);
                }
                if (right < viewWidth) {
                    selectionCanvas.drawLine(right, centerVertical, imageRight, centerVertical, marginPaint);
                    selectionCanvas.drawText(String.valueOf(bitmap.getWidth() - selection.right), (imageRight + right) / 2.0f, centerVertical, marginPaint);
                }
                if (bottom < viewHeight) {
                    selectionCanvas.drawLine(centerHorizontal, bottom, centerHorizontal, imageBottom, marginPaint);
                    selectionCanvas.drawText(String.valueOf(bitmap.getHeight() - selection.bottom), centerHorizontal, (imageBottom + bottom) / 2.0f, marginPaint);
                }
            }
        }
        ivSelection.invalidate();
    }

    private void drawSelectionOnViewByStartsAndEnds() {
        eraseBitmap(selectionBitmap);
        if (hasSelection) {
            if (selectionBeginX < selectionEndX) {
                selection.left = selectionBeginX;
                selection.right = selectionEndX;
            } else {
                selection.left = selectionEndX - 1;
                selection.right = selectionBeginX + 1;
            }
            if (selectionBeginY < selectionEndY) {
                selection.top = selectionBeginY;
                selection.bottom = selectionEndY;
            } else {
                selection.top = selectionEndY - 1;
                selection.bottom = selectionBeginY + 1;
            }
            selectionCanvas.drawRect(
                    toViewX(selection.left), toViewY(selection.top),
                    toViewX(selection.right), toViewY(selection.bottom),
                    selector);
        }
        ivSelection.invalidate();
    }

    private void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
        shape.drawShapeOnCanvas(x0, y0, x1, y1);
    }

    private String drawShapeOnView(int x0, int y0, int x1, int y1) {
        eraseBitmap(previewBitmap);
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
        canvas.drawText(etText.getText().toString(), textX, textY, paint);
        drawBitmapOnView(true, true);
        eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
        hideSoftInputFromWindow();
        if (hideOptions) {
            llOptionsText.setVisibility(View.INVISIBLE);
        }
        addHistory();
    }

    private void drawTextOnView() {
        if (!isEditingText) {
            return;
        }
        eraseBitmap(previewBitmap);
        final float x = toViewX(textX), y = toViewY(textY);
        paint.setTextSize(toScaled(textSize));
        final Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        final float centerVertical = y + fontMetrics.ascent / 2.0f;
        previewCanvas.drawText(etText.getText().toString(), x, y, paint);
        previewCanvas.drawLine(x, 0.0f, x, viewHeight, PAINT_CELL_GRID);
        previewCanvas.drawLine(0.0f, y, viewWidth, y, textLine);
        previewCanvas.drawLine(0.0f, centerVertical, viewWidth, centerVertical, PAINT_CELL_GRID);
        ivPreview.invalidate();
    }

    private void drawTransformerOnCanvas() {
        if (transformer == null || !hasSelection) {
            return;
        }
        canvas.drawBitmap(transformer.getBitmap(), selection.left, selection.top, PAINT_SRC_OVER);
        recycleTransformer();
        optimizeSelection();
        drawSelectionOnView();
        drawBitmapOnView(selection);
        addHistory();
        clearStatus();
    }

    private static void eraseBitmap(Bitmap bitmap) {
        bitmap.eraseColor(Color.TRANSPARENT);
    }

    private static void eraseBitmapAndInvalidateView(Bitmap bitmap, ImageView imageView) {
        eraseBitmap(bitmap);
        imageView.invalidate();
    }

    private void floodFill(final Bitmap bitmap, int x, int y, @ColorInt final int color) {
        floodFill(bitmap, bitmap, x, y, color, false, 0);
    }

    private void floodFill(final Bitmap bitmap, int x, int y, @ColorInt final int color,
                           final boolean ignoreAlpha, final int threshold) {
        floodFill(bitmap, bitmap, x, y, color, ignoreAlpha, threshold);
    }

    private void floodFill(final Bitmap src, final Bitmap dst,
                           int x, int y, @ColorInt final int color,
                           final boolean ignoreAlpha, final int threshold) {
        int left, top, right, bottom;
        if (hasSelection) {
            left = selection.left;
            top = selection.top;
            right = selection.right;
            bottom = selection.bottom;
        } else {
            left = 0;
            top = 0;
            right = src.getWidth();
            bottom = src.getHeight();
        }
        if (!(left <= x && x < right && top <= y && y < bottom)) {
            return;
        }
        final int pixel = src.getPixel(x, y);
        if (pixel == color && threshold == 0) {
            return;
        }
        final int w = right - left, h = bottom - top, area = w * h;
        final int[] srcPixels = new int[area], dstPixels = src == dst ? srcPixels : new int[area];
        src.getPixels(srcPixels, 0, w, left, top, w, h);
//      final long a = System.currentTimeMillis();
        final Queue<Point> pointsToBeSet = new LinkedList<>();
        final boolean[] havePointsBeenSet = new boolean[area];
        pointsToBeSet.offer(new Point(x, y));
        Point point;
        while ((point = pointsToBeSet.poll()) != null) {
            final int i = (point.y - top) * w + (point.x - left);
            if (havePointsBeenSet[i]) {
                continue;
            }
            havePointsBeenSet[i] = true;
            final int px = srcPixels[i];
            boolean match;
            int newColor;
            if (ignoreAlpha) {
                match = threshold == 0 ?
                        rgb(px) == rgb(pixel) :
                        checkIfColorIsWithinThreshold(
                                Color.red(pixel), Color.green(pixel), Color.blue(pixel),
                                Color.red(px), Color.green(px), Color.blue(px));
                newColor = px & Color.BLACK | rgb(color);
            } else {
                match = threshold == 0 ?
                        px == pixel :
                        Color.alpha(px) == Color.alpha(pixel) && checkIfColorIsWithinThreshold(
                                Color.red(pixel), Color.green(pixel), Color.blue(pixel),
                                Color.red(px), Color.green(px), Color.blue(px));
                newColor = color;
            }
            if (match) {
                srcPixels[i] = newColor;
                if (src != dst) {
                    dstPixels[i] = newColor;
                }
                final int xn = point.x - 1, xp = point.x + 1, yn = point.y - 1, yp = point.y + 1; // n - negative, p - positive
                if (left <= xn && !havePointsBeenSet[i - 1])
                    pointsToBeSet.offer(new Point(xn, point.y));
                if (xp < right && !havePointsBeenSet[i + 1])
                    pointsToBeSet.offer(new Point(xp, point.y));
                if (top <= yn && !havePointsBeenSet[i - w])
                    pointsToBeSet.offer(new Point(point.x, yn));
                if (yp < bottom && !havePointsBeenSet[i + w])
                    pointsToBeSet.offer(new Point(point.x, yp));
            }
        }
//      final long b = System.currentTimeMillis();
//      Toast.makeText(this, String.valueOf(b - a), Toast.LENGTH_SHORT).show();
        dst.setPixels(dstPixels, 0, w, left, top, w, h);
    }

    private CompoundButton.OnCheckedChangeListener getOnLayerVisibleCheckBoxCheckedChangeListener(final Tab tab) {
        return (buttonView, isChecked) -> {
            tab.visible = isChecked;
            drawBitmapOnView(true);
        };
    }

    private RectF getScaledVisibleSubset(Bitmap bitmap, float translX, float translY) {
        final float left = translX > -scale ? translX : translX % scale;
        final float top = translY > -scale ? translY : translY % scale;
        final float right = Math.min(translX + toScaled(bitmap.getWidth()), viewWidth);
        final float bottom = Math.min(translY + toScaled(bitmap.getHeight()), viewHeight);
        return new RectF(left, top, right, bottom);
    }

    private String getTabName() {
        final String s = tab.tvTitle.getText().toString();
        final int i = s.lastIndexOf('.');
        return i == -1 ? s : s.substring(0, i);
    }

    private Rect getVisibleSubset() {
        return getVisibleSubset(translationX, translationY, bitmap.getWidth(), bitmap.getHeight());
    }

    private Rect getVisibleSubset(float translX, float translY, int width, int height) {
        final int scaledBitmapW = (int) toScaled(width), scaledBitmapH = (int) toScaled(height);
        final int startX = translX >= 0.0f ? 0 : toUnscaled(-translX);
        final int startY = translY >= 0.0f ? 0 : toUnscaled(-translY);
        final int endX = Math.min(toUnscaled(translX + scaledBitmapW <= viewWidth ? scaledBitmapW : viewWidth - translX) + 1, width);
        final int endY = Math.min(toUnscaled(translY + scaledBitmapH <= viewHeight ? scaledBitmapH : viewHeight - translY) + 1, height);
        return new Rect(startX, startY, endX, endY);
    }

    private void hideSoftInputFromWindow() {
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void hideToolOptions() {
        for (int i = 0; i < flToolOptions.getChildCount(); ++i) {
            flToolOptions.getChildAt(i).setVisibility(View.INVISIBLE);
        }
    }

    private boolean isPaintStyleFill() {
        return paint.getStyle() != Paint.Style.STROKE;
    }

    private boolean isScaledMuch() {
        return scale >= 16.0f;
    }

    private static boolean isSizeEqualTo(Bitmap a, Bitmap b) {
        return a.getWidth() == b.getWidth() && a.getHeight() == b.getHeight();
    }

    private static boolean isSizeEqualTo(Bitmap bitmap, int w, int h) {
        return bitmap.getWidth() == w && bitmap.getHeight() == h;
    }

    private void load() {
        viewWidth = imageView.getWidth();
        viewHeight = imageView.getHeight();

        tab = new Tab();
        tabs.add(tab);
        bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
        tab.bitmap = bitmap;
        canvas = new Canvas(bitmap);

        tab.cellGrid = new CellGrid();

        tab.history = new BitmapHistory();
        addHistory();

        tab.paint = new Paint();
        tab.paint.setBlendMode(BlendMode.SRC_OVER);

        tab.path = null;

        resetTranslAndScale();
        scale = tab.scale;
        translationX = tab.translationX;
        translationY = tab.translationY;

        viewBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        imageView.setImageBitmap(viewBitmap);

        rulerHBitmap = Bitmap.createBitmap(viewWidth, ivRulerH.getHeight(), Bitmap.Config.ARGB_4444);
        rulerHCanvas = new Canvas(rulerHBitmap);
        ivRulerH.setImageBitmap(rulerHBitmap);
        rulerVBitmap = Bitmap.createBitmap(ivRulerV.getWidth(), viewHeight, Bitmap.Config.ARGB_4444);
        rulerVCanvas = new Canvas(rulerVBitmap);
        ivRulerV.setImageBitmap(rulerVBitmap);

        chessboardBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        chessboardCanvas = new Canvas(chessboardBitmap);
        ivChessboard.setImageBitmap(chessboardBitmap);
        drawChessboardOnView();

        gridBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        gridCanvas = new Canvas(gridBitmap);
        ivGrid.setImageBitmap(gridBitmap);
        drawGridOnView();

        previewBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        previewCanvas = new Canvas(previewBitmap);
        ivPreview.setImageBitmap(previewBitmap);

        selectionBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        selectionCanvas = new Canvas(selectionBitmap);
        ivSelection.setImageBitmap(selectionBitmap);
        drawSelectionOnView();

        final TabLayout.Tab t = tabLayout.newTab()
                .setCustomView(R.layout.tab)
                .setTag(tab);
        final View customView = t.getCustomView();
        tab.cbLayerVisible = customView.findViewById(R.id.cb_layer_visible);
        tab.cbLayerVisible.setChecked(tab.visible);
        tab.cbLayerVisible.setOnCheckedChangeListener(getOnLayerVisibleCheckBoxCheckedChangeListener(tab));
        tab.tvLayerLevel = customView.findViewById(R.id.tv_layer_level);
        tab.tvTitle = customView.findViewById(R.id.tv_title);
        tab.tvTitle.setText(R.string.untitled);
        tabLayout.addTab(t, true);

        etPencilBlurRadius.setText(String.valueOf(0.0f));
        etEraserBlurRadius.setText(String.valueOf(0.0f));
        etEraserStrokeWidth.setText(String.valueOf(eraser.getStrokeWidth()));
        etPencilStrokeWidth.setText(String.valueOf(paint.getStrokeWidth()));
        etTextSize.setText(String.valueOf(paint.getTextSize()));

        if (fileToBeOpened != null) {
            closeTab(0);
            openFile(fileToBeOpened);
        }

        rbPencil.setChecked(true);
    }

    private static Bitmap mergeLayers(final LayerTree tree, final Rect rect) {
        return mergeLayers(tree, rect, null, null, true);
    }

    private static Bitmap mergeLayers(final LayerTree tree, final Rect rect,
                                      final Tab excludedTab, final Bitmap excludedBitmap, final boolean excludedIntegrity) {
        return mergeLayers(tree, rect,
                null, excludedTab, excludedBitmap, excludedIntegrity);
    }

    private static Bitmap mergeLayers(final LayerTree tree, final Rect rect, final Bitmap background,
                                      final Tab excludedTab, final Bitmap excludedBitmap, final boolean excludedIntegrity) {
        final int w = rect.width(), h = rect.height();
        final Rect relative = new Rect(0, 0, w, h);
        final LayerTree.Node root = tree.peekBackground();
        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        if (background != null) {
            canvas.drawBitmap(background, 0.0f, 0.0f, PAINT_SRC);
        }

        try {
            for (LayerTree.Node node = root; node != null; node = node.getFront()) {
                final Tab tab = node.getTab();
                if (!tab.visible && tab != excludedTab) {
                    continue;
                }
                final LayerTree branch = node.getBranch();
                if (branch == null) {
                    final Paint paint = node == root && background != null ? PAINT_SRC : tab.paint;
                    if (tab.filter == null) {
                        if (tab.drawBelow) {
                            canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
                        } else if (tab == excludedTab) {
                            if (excludedIntegrity) {
                                canvas.drawBitmap(excludedBitmap, rect, relative, paint);
                            } else {
                                canvas.drawBitmap(excludedBitmap, 0.0f, 0.0f, paint);
                            }
                        } else {
                            canvas.drawBitmap(tab.bitmap, rect, relative, paint);
                        }
                    } else {
                        final Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                        final Canvas cv = new Canvas(bm);
                        try {
                            if (tab.drawBelow) {
                                cv.drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
                            } else if (tab == excludedTab) {
                                if (excludedIntegrity) {
                                    cv.drawBitmap(excludedBitmap, rect, relative, PAINT_SRC);
                                } else {
                                    cv.drawBitmap(excludedBitmap, 0.0f, 0.0f, PAINT_SRC);
                                }
                            } else {
                                cv.drawBitmap(tab.bitmap, rect, relative, PAINT_SRC);
                            }
                            switch (tab.filter) {
                                case COLOR_FILTER:
                                    BitmapUtil.addColorMatrixColorFilter(bm, 0, 0, bm, 0, 0,
                                            tab.colorMatrix);
                                    break;
                                case CURVES:
                                    BitmapUtil.applyCurves(bm, tab.curves);
                                    break;
                                case HSV:
                                    BitmapUtil.shiftHsv(bm, tab.deltaHsv);
                                    break;
                            }
                            canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
                        } finally {
                            bm.recycle();
                        }
                    }
                } else {
                    final Bitmap branchBitmap = mergeLayers(branch, rect,
                            !tab.drawBelow || node == root ? null : bitmap,
                            excludedTab, excludedBitmap, excludedIntegrity);
                    canvas.drawBitmap(branchBitmap, 0.0f, 0.0f, tab.paint);
                    branchBitmap.recycle();
                }
            }
        } catch (RuntimeException e) {
            bitmap.recycle();
            throw e;
        }

        return bitmap;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        tab = tabs.get(tabLayout.getSelectedTabPosition());
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Preferences
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        settings = ((MainApplication) getApplicationContext()).getSettings();
        settings.setMainActivity(this);
        settings.update(preferences);

        // Locale
        final String def = "def", loc = preferences.getString(Settings.KEY_LOC, def);
        if (!def.equals(loc)) {
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

        // Content view
        setContentView(R.layout.activity_main);

        cbBucketFillContiguous = findViewById(R.id.cb_bucket_fill_contiguous);
        cbBucketFillIgnoreAlpha = findViewById(R.id.cb_bucket_fill_ignore_alpha);
        cbCloneStampAntiAlias = findViewById(R.id.cb_clone_stamp_anti_alias);
        cbGradientAntiAlias = findViewById(R.id.cb_gradient_anti_alias);
        cbMagicPaintAntiAlias = findViewById(R.id.cb_magic_paint_anti_alias);
        cbPatcherAntiAlias = findViewById(R.id.cb_patcher_anti_alias);
        cbPencilAntiAlias = findViewById(R.id.cb_pencil_anti_alias);
        cbPencilWithEraser = findViewById(R.id.cb_pencil_with_eraser);
        cbShapeFill = findViewById(R.id.cb_shape_fill);
        cbTextFill = findViewById(R.id.cb_text_fill);
        cbTransformerLar = findViewById(R.id.cb_transformer_lar);
        cbZoom = findViewById(R.id.cb_zoom);
        etCloneStampBlurRadius = findViewById(R.id.et_clone_stamp_blur_radius);
        etCloneStampStrokeWidth = findViewById(R.id.et_clone_stamp_stroke_width);
        etEraserBlurRadius = findViewById(R.id.et_eraser_blur_radius);
        etEraserStrokeWidth = findViewById(R.id.et_eraser_stroke_width);
        etGradientBlurRadius = findViewById(R.id.et_gradient_blur_radius);
        etGradientStrokeWidth = findViewById(R.id.et_gradient_stroke_width);
        etMagicEraserStrokeWidth = findViewById(R.id.et_magic_eraser_stroke_width);
        etMagicPaintBlurRadius = findViewById(R.id.et_magic_paint_blur_radius);
        etMagicPaintStrokeWidth = findViewById(R.id.et_magic_paint_stroke_width);
        etPatcherBlurRadius = findViewById(R.id.et_patcher_blur_radius);
        etPatcherStrokeWidth = findViewById(R.id.et_patcher_stroke_width);
        etPencilBlurRadius = findViewById(R.id.et_pencil_blur_radius);
        etPencilStrokeWidth = findViewById(R.id.et_pencil_stroke_width);
        etShapeStrokeWidth = findViewById(R.id.et_shape_stroke_width);
        etText = findViewById(R.id.et_text);
        etTextSize = findViewById(R.id.et_text_size);
        flToolOptions = findViewById(R.id.fl_tool_options);
        flImageView = findViewById(R.id.fl_iv);
        hsvOptionsBucketFill = findViewById(R.id.hsv_options_bucket_fill);
        hsvOptionsCloneStamp = findViewById(R.id.hsv_options_clone_stamp);
        hsvOptionsMagicPaint = findViewById(R.id.hsv_options_magic_paint);
        hsvOptionsPencil = findViewById(R.id.hsv_options_pencil);
        imageView = findViewById(R.id.iv);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        ivChessboard = findViewById(R.id.iv_chessboard);
        ivGrid = findViewById(R.id.iv_grid);
        ivPreview = findViewById(R.id.iv_preview);
        ivRulerH = findViewById(R.id.iv_ruler_horizontal);
        ivRulerV = findViewById(R.id.iv_ruler_vertical);
        ivSelection = findViewById(R.id.iv_selection);
        llOptionsEraser = findViewById(R.id.ll_options_eraser);
        llOptionsEyedropper = findViewById(R.id.ll_options_eyedropper);
        llOptionsGradient = findViewById(R.id.ll_options_gradient);
        llOptionsMagicEraser = findViewById(R.id.ll_options_magic_eraser);
        llOptionsPatcher = findViewById(R.id.ll_options_patcher);
        llOptionsShape = findViewById(R.id.ll_options_shape);
        llOptionsText = findViewById(R.id.ll_options_text);
        llOptionsTransformer = findViewById(R.id.ll_options_transformer);
        final RecyclerView rvSwatches = findViewById(R.id.rv_swatches);
        rbCloneStamp = findViewById(R.id.rb_clone_stamp);
        rbEyedropper = findViewById(R.id.rb_eyedropper);
        rbEyedropperAllLayers = findViewById(R.id.rb_eyedropper_all_layers);
        rbMagicEraserLeft = findViewById(R.id.rb_magic_eraser_left);
        rbMagicEraserRight = findViewById(R.id.rb_magic_eraser_right);
        cbMagicEraserPosition = rbMagicEraserLeft;
        rbPencil = findViewById(R.id.rb_pencil);
        final RadioButton rbRuler = findViewById(R.id.rb_ruler);
        rbTransformer = findViewById(R.id.rb_transformer);
        final RadioGroup rgMagicEraserPosition = findViewById(R.id.rg_magic_eraser_position);
        tabLayout = findViewById(R.id.tl);
        tvStatus = findViewById(R.id.tv_status);
        vBackgroundColor = findViewById(R.id.v_background_color);
        vForegroundColor = findViewById(R.id.v_foreground_color);

        findViewById(R.id.b_bucket_fill_threshold).setOnClickListener(onThresholdButtonClickListener);
        findViewById(R.id.b_clone_stamp_src).setOnClickListener(onCloneStampSrcButtonClickListener);
        findViewById(R.id.b_magic_paint_tolerance).setOnClickListener(onThresholdButtonClickListener);
        findViewById(R.id.b_text_draw).setOnClickListener(v -> drawTextOnCanvas());
        cbCloneStampAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.cb_eraser_anti_alias)).setOnCheckedChangeListener((buttonView, isChecked) -> eraser.setAntiAlias(isChecked));
        cbGradientAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbMagicPaintAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.cb_magic_paint_clear)).setOnCheckedChangeListener(((buttonView, isChecked) -> magicPaint.setBlendMode(isChecked ? BlendMode.DST_OUT : null)));
        cbMagErAccEnabled = findViewById(R.id.cb_magic_eraser_acc_enabled);
        cbPatcherAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbPencilAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbShapeFill.setOnCheckedChangeListener((buttonView, isChecked) -> paint.setStyle(isChecked ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE));
        cbZoom.setOnCheckedChangeListener(onZoomToolCheckBoxCheckedChangeListener);
        cbZoom.setTag(onImageViewTouchWithPencilListener);
        etCloneStampBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etCloneStampStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etMagicEraserStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etMagicPaintBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etMagicPaintStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etGradientBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etGradientStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etPatcherBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etPatcherStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etPencilBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etPencilStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etShapeStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etText.addTextChangedListener((AfterTextChangedListener) s -> drawTextOnView());
        etTextSize.addTextChangedListener(onTextSizeChangedListener);
        flImageView.setOnTouchListener(onImageViewTouchWithPencilListener);
        ((CompoundButton) findViewById(R.id.rb_bucket_fill)).setOnCheckedChangeListener(onBucketFillRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_circle)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = circle);
        rbCloneStamp.setOnCheckedChangeListener(onCloneStampRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_magic_eraser)).setOnCheckedChangeListener(onMagicEraserRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_magic_paint)).setOnCheckedChangeListener(onMagicPaintRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_eraser)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(isChecked, onImageViewTouchWithEraserListener, llOptionsEraser));
        rbEyedropper.setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(isChecked, onImageViewTouchWithEyedropperListener, llOptionsEyedropper));
        ((CompoundButton) findViewById(R.id.rb_gradient)).setOnCheckedChangeListener(onGradientRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_line)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = line);
        ((CompoundButton) findViewById(R.id.rb_oval)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = oval);
        ((CompoundButton) findViewById(R.id.rb_patcher)).setOnCheckedChangeListener(onPatcherRadioButtonCheckedChangeListener);
        rbPencil.setOnCheckedChangeListener(onPencilRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_rect)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = rect);
        rbRuler.setOnCheckedChangeListener(onRulerRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_selector)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(isChecked, onImageViewTouchWithSelectorListener));
        ((CompoundButton) findViewById(R.id.rb_shape)).setOnCheckedChangeListener(onShapeRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_text)).setOnCheckedChangeListener(onTextRadioButtonCheckedChangeListener);
        rbTransformer.setOnCheckedChangeListener(onTransformerRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_transformer_rotation)).setOnCheckedChangeListener((OnCheckedListener) () -> onTransformerChange(onImageViewTouchWithTransformerOfRotationListener));
        ((CompoundButton) findViewById(R.id.rb_transformer_scale)).setOnCheckedChangeListener((OnCheckedListener) () -> onTransformerChange(onImageViewTouchWithTransformerOfScaleListener));
        ((CompoundButton) findViewById(R.id.rb_transformer_translation)).setOnCheckedChangeListener((OnCheckedListener) () -> onTransformerChange(onImageViewTouchWithTransformerOfTranslationListener));
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        findViewById(R.id.tv_color_add).setOnClickListener(onAddSwatchViewClickListener);
        vBackgroundColor.setOnClickListener(onBackgroundColorClickListener);
        vForegroundColor.setOnClickListener(onForegroundColorClickListener);

        cbTextFill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            paint.setStyle(isChecked ? Paint.Style.FILL : Paint.Style.STROKE);
            drawTextOnView();
        });

        etEraserBlurRadius.addTextChangedListener((AfterTextChangedListener) s -> {
            try {
                final float f = Float.parseFloat(s);
                blurRadiusEraser = f;
                setBlurRadius(eraser, f);
            } catch (NumberFormatException e) {
            }
        });

        etEraserStrokeWidth.addTextChangedListener((AfterTextChangedListener) s -> {
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
            onImageViewTouchWithMagicEraserListener = isChecked
                    ? onImageViewTouchWithMagicEraserPreciseListener
                    : onImageViewTouchWithMagicEraserImpreciseListener;
            cbZoom.setTag(onImageViewTouchWithMagicEraserListener);
            if (!cbZoom.isChecked()) {
                flImageView.setOnTouchListener(onImageViewTouchWithMagicEraserListener);
            }
            if (!isChecked) {
                magErB = null;
                magErF = null;
                eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
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

        rvSwatches.setItemAnimator(new DefaultItemAnimator());
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rvSwatches.setLayoutManager(layoutManager);
        palette = new LinkedList<>() {
            {
                offer(Color.pack(Color.BLACK));
                offer(Color.pack(Color.WHITE));
                offer(Color.pack(Color.RED));
                offer(Color.pack(Color.YELLOW));
                offer(Color.pack(Color.GREEN));
                offer(Color.pack(Color.CYAN));
                offer(Color.pack(Color.BLUE));
                offer(Color.pack(Color.MAGENTA));
            }
        };
        colorAdapter = new ColorAdapter(palette) {
            {
                setOnItemClickListener(view -> {
                    final long color = (Long) view.getTag();
                    paint.setColor(color);
                    vForegroundColor.setBackgroundColor(Color.toArgb(color));
                    if (isEditingText) {
                        drawTextOnView();
                    }
                });
                setOnItemLongClickListener(view -> {
                    ArgbColorIntPicker.make(MainActivity.this,
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
        };
        rvSwatches.setAdapter(colorAdapter);

        chessboard = BitmapFactory.decodeResource(getResources(), R.mipmap.chessboard);

        fileToBeOpened = getIntent().getData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        miHasAlpha = menu.findItem(R.id.i_has_alpha);
        miLayerColorFilter = menu.findItem(R.id.i_layer_color_filter);
        miLayerCurves = menu.findItem(R.id.i_layer_curves);
        miLayerDrawBelow = menu.findItem(R.id.i_layer_draw_below);
        miLayerFilterSet = menu.findItem(R.id.i_layer_filter_set);
        miLayerHsv = menu.findItem(R.id.i_layer_hsv);
        miLayerLevelUp = menu.findItem(R.id.i_layer_level_up);
        smBlendModes = menu.findItem(R.id.i_blend_modes).getSubMenu();
        return true;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.i_blend_mode_clear:
            case R.id.i_blend_mode_src:
            case R.id.i_blend_mode_dst:
            case R.id.i_blend_mode_src_over:
            case R.id.i_blend_mode_dst_over:
            case R.id.i_blend_mode_src_in:
            case R.id.i_blend_mode_dst_in:
            case R.id.i_blend_mode_src_out:
            case R.id.i_blend_mode_dst_out:
            case R.id.i_blend_mode_src_atop:
            case R.id.i_blend_mode_dst_atop:
            case R.id.i_blend_mode_xor:
            case R.id.i_blend_mode_plus:
            case R.id.i_blend_mode_modulate:
            case R.id.i_blend_mode_screen:
            case R.id.i_blend_mode_overlay:
            case R.id.i_blend_mode_darken:
            case R.id.i_blend_mode_lighten:
            case R.id.i_blend_mode_color_dodge:
            case R.id.i_blend_mode_color_burn:
            case R.id.i_blend_mode_hard_light:
            case R.id.i_blend_mode_soft_light:
            case R.id.i_blend_mode_difference:
            case R.id.i_blend_mode_exclusion:
            case R.id.i_blend_mode_multiply:
            case R.id.i_blend_mode_hue:
            case R.id.i_blend_mode_saturation:
            case R.id.i_blend_mode_color:
            case R.id.i_blend_mode_luminosity:
                drawFloatingLayers();
                for (int i = 0; i < BLEND_MODES.length; ++i) {
                    final MenuItem mi = smBlendModes.getItem(i);
                    if (mi == item) {
                        tab.paint.setBlendMode(BLEND_MODES[i]);
                        mi.setChecked(true);
                    } else if (mi.isChecked()) {
                        mi.setChecked(false);
                    }
                }
                drawBitmapOnView(true);
                break;

            case R.id.i_cell_grid: {
                CellGridManager.make(this, tab.cellGrid,
                                onUpdateCellGridListener)
                        .show();
                break;
            }
            case R.id.i_clone:
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
                    addHistory();
                }
                if (hasSelection) {
                    drawBitmapOnView(selection, true);
                } else {
                    drawBitmapOnView(true);
                }
                break;

            case R.id.i_clone_as_new: {
                final Bitmap bm = hasSelection ?
                        transformer == null ?
                                Bitmap.createBitmap(bitmap, selection.left, selection.top, selection.width(), selection.height()) :
                                Bitmap.createBitmap(transformer.getBitmap()) :
                        Bitmap.createBitmap(bitmap);
                addBitmap(bm, tabLayout.getSelectedTabPosition() + 1);
                break;
            }
            case R.id.i_copy:
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
                break;
            case R.id.i_crop: {
                if (!hasSelection) {
                    break;
                }
                drawFloatingLayers();
                final int width = selection.width(), height = selection.height();
                final Bitmap bm = Bitmap.createBitmap(bitmap, selection.left, selection.top, width, height);
                resizeBitmap(width, height, false);
                canvas.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC);
                bm.recycle();
                drawBitmapOnView(true, true);
                addHistory();
                break;
            }
            case R.id.i_cut:
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
                    addHistory();
                } else {
                    clipboard = Bitmap.createBitmap(transformer.getBitmap());
                    recycleTransformer();
                }
                if (hasSelection) {
                    drawBitmapOnView(selection, true);
                } else {
                    drawBitmapOnView(true);
                }
                break;

            case R.id.i_delete:
                if (transformer == null) {
                    if (hasSelection) {
                        canvas.drawRect(selection.left, selection.top, selection.right, selection.bottom, eraser);
                    } else {
                        canvas.drawColor(eraser.getColorLong(), BlendMode.SRC);
                    }
                    addHistory();
                } else {
                    recycleTransformer();
                }
                if (hasSelection) {
                    drawBitmapOnView(selection, true);
                } else {
                    drawBitmapOnView(true);
                }
                clearStatus();
                break;

            case R.id.i_deselect:
                drawFloatingLayers();
                hasSelection = false;
                eraseBitmapAndInvalidateView(selectionBitmap, ivSelection);
                clearStatus();
                break;

            case R.id.i_draw_color:
                if (transformer == null) {
                    if (hasSelection) {
                        canvas.drawRect(selection.left, selection.top, selection.right, selection.bottom, paint);
                    } else {
                        canvas.drawColor(paint.getColorLong());
                    }
                    addHistory();
                } else {
                    transformer.getBitmap().eraseColor(paint.getColorLong());
                }
                if (hasSelection) {
                    drawBitmapOnView(selection, true);
                } else {
                    drawBitmapOnView(true);
                }
                clearStatus();
                break;

            case R.id.i_file_new: {
                drawFloatingLayers();
                new NewGraphicPropertiesDialog(this)
                        .setOnFinishSettingListener(onFinishSettingNewGraphicPropertiesListener)
                        .show();
                break;
            }
            case R.id.i_file_open:
                drawFloatingLayers();

                getImages.launch("image/*");
                break;

            case R.id.i_file_open_from_clipboard: {
                final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (!clipboardManager.hasPrimaryClip()) {
                    break;
                }

                final ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData == null || clipData.getItemCount() < 1) {
                    break;
                }

                openFile(clipData.getItemAt(0).getUri());
                break;
            }
            case R.id.i_file_close:
            case R.id.i_layer_delete:
                if (tabs.size() == 1) {
                    break;
                }
                if (transformer != null) {
                    recycleTransformer();
                }
                closeTab();
                break;

            case R.id.i_file_refer_to_clipboard: {
                if (tab.path == null) {
                    Toast.makeText(this, getString(R.string.please_save_first), Toast.LENGTH_SHORT).show();
                    break;
                }
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                        .setPrimaryClip(ClipData.newUri(getContentResolver(), "Image",
                                FileProvider.getUriForFile(this,
                                        getApplicationContext().getPackageName() + ".provider",
                                        new File(tab.path))));
                break;
            }
            case R.id.i_file_save:
            case R.id.i_save:
                save();
                break;

            case R.id.i_file_save_as:
                saveAs();
                break;

            case R.id.i_filter_channel_lighting:
                drawFloatingLayers();
                createPreviewBitmap();
                new ChannelLighting(this)
                        .setOnCancelListener(onPreviewCancelListener)
                        .setOnLightingChangeListener(onLightingChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .show();
                clearStatus();
                break;

            case R.id.i_filter_color_balance:
                drawFloatingLayers();
                createPreviewBitmap();
                new ColorBalanceDialog(this)
                        .setOnCancelListener(onPreviewCancelListener)
                        .setOnMatrixChangeListener(onColorMatrixChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .show();
                clearStatus();
                break;

            case R.id.i_filter_contrast:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.contrast).setMin(-10).setMax(100).setProgress(10)
                        .setOnChangeListener(onFilterContrastSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();
                clearStatus();
                break;

            case R.id.i_filter_curves:
                drawFloatingLayers();
                createPreviewBitmap();
                new CurvesDialog(this)
                        .setSource(preview.getPixels())
                        .setOnCurvesChangeListener(onFilterCurvesChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();
                clearStatus();
                break;

            case R.id.i_filter_hsv:
                drawFloatingLayers();
                createPreviewBitmap();
                new HsvDialog(this)
                        .setOnHSVChangeListener(onFilterHSVChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();
                clearStatus();
                break;

            case R.id.i_filter_hue_to_alpha:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.hue).setMin(0).setMax(360).setProgress(0)
                        .setOnChangeListener(onFilterHToASeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();
                onFilterHToASeekBarProgressChangeListener.onChanged(0, true);
                break;

            case R.id.i_filter_levels:
                drawFloatingLayers();
                createPreviewBitmap();
                new LevelsDialog(this)
                        .setOnLevelsChangeListener(onFilterLevelsChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show()
                        .drawHistogram(preview.getPixels());
                clearStatus();
                break;

            case R.id.i_filter_lightness:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.lightness).setMin(-0xFF).setMax(0xFF).setProgress(0)
                        .setOnChangeListener(onFilterLightnessSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();
                clearStatus();
                break;

            case R.id.i_filter_matrix:
                drawFloatingLayers();
                createPreviewBitmap();
                ColorMatrixManager
                        .make(this,
                                R.string.channel_mixer,
                                onColorMatrixChangeListener,
                                onPreviewConfirmListener,
                                onPreviewCancelListener)
                        .show();
                clearStatus();
                break;

            case R.id.i_filter_saturation:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.saturation).setMin(0).setMax(100).setProgress(10)
                        .setOnChangeListener(onFilterSaturationSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();
                clearStatus();
                break;

            case R.id.i_filter_threshold:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.threshold).setMin(0).setMax(255).setProgress(128)
                        .setOnChangeListener(onFilterThresholdSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();
                onFilterThresholdSeekBarProgressChangeListener.onChanged(128, true);
                clearStatus();
                break;

            case R.id.i_filter_white_balance: {
                drawFloatingLayers();
                createPreviewBitmap();
                final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.white_balance)
                        .setPositiveButton(R.string.ok, onPreviewConfirmListener)
                        .setNegativeButton(R.string.cancel,
                                ((d, which) -> onPreviewCancelListener.onCancel(d)))
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();

                final Window window = dialog.getWindow();
                final WindowManager.LayoutParams lp = window.getAttributes();
                lp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                lp.gravity = Gravity.BOTTOM;
                window.setAttributes(lp);

                final int w = preview.getWidth(), h = preview.getHeight();
                final int[] src = preview.getPixels(), dst = new int[w * h];
                runOrStart(() -> {
                    BitmapUtil.whiteBalance(src, dst, paint.getColor());
                    preview.setPixels(dst, w, h);
                    drawBitmapOnView(preview.getEntire(), selection);
                });
                clearStatus();
                break;
            }
            case R.id.i_flip_horizontally: {
                drawFloatingLayers();
                scale(-1.0f, 1.0f, false);
                break;
            }
            case R.id.i_flip_vertically: {
                drawFloatingLayers();
                scale(1.0f, -1.0f, false);
                break;
            }
            case R.id.i_has_alpha: {
                final boolean checked = !item.isChecked();
                item.setChecked(checked);
                bitmap.setHasAlpha(checked);
                drawBitmapOnView(true);
                break;
            }
            case R.id.i_information: {
                final StringBuilder message = new StringBuilder()
                        .append(getString(R.string.config)).append('\n').append(bitmap.getConfig()).append("\n\n")
                        .append(getString(R.string.has_alpha)).append('\n').append(bitmap.hasAlpha()).append("\n\n")
                        .append(getString(R.string.color_space)).append('\n').append(bitmap.getColorSpace());
                new AlertDialog.Builder(this)
                        .setTitle(R.string.information)
                        .setMessage(message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                break;
            }
            case R.id.i_layer_add_mask: {
                drawFloatingLayers();
                tab.cbLayerVisible.setChecked(true);
                final Tab t = new Tab();
                t.bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                        bitmap.getConfig(), true, bitmap.getColorSpace());
                t.level = tab.level + 1;
                t.paint = new Paint();
                t.paint.setBlendMode(BlendMode.DST_IN);
                t.visible = true;
                final Canvas cv = new Canvas(t.bitmap);
                if (hasSelection) {
                    cv.drawRect(selection, PAINT_BLACK);
                } else {
                    cv.drawColor(Color.BLACK, PorterDuff.Mode.SRC);
                }
                addBitmap(t, tabLayout.getSelectedTabPosition(), getString(R.string.mask));
                showLayerLevel();
                break;
            }
            case R.id.i_layer_alpha:
                drawFloatingLayers();
                new SeekBarDialog(this).setTitle(R.string.alpha_value).setMin(0).setMax(255)
                        .setProgress(tab.paint.getAlpha())
                        .setOnChangeListener(onLayerAlphaSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener((dialog, which) -> clearStatus())
                        .setOnCancelListener(dialog -> clearStatus(), false)
                        .show();
                clearStatus();
                break;

            case R.id.i_layer_color_filter: {
                final boolean checked = !item.isChecked();
                if (checked) {
                    disableAllLayerFilters();
                }
                item.setChecked(checked);
                tab.filter = checked ? Tab.Filter.COLOR_FILTER : null;
                miLayerFilterSet.setEnabled(checked);
                drawBitmapOnView(true);
                break;
            }
            case R.id.i_layer_curves: {
                final boolean checked = !item.isChecked();
                if (checked) {
                    disableAllLayerFilters();
                }
                item.setChecked(checked);
                tab.filter = checked ? Tab.Filter.CURVES : null;
                miLayerFilterSet.setEnabled(checked);
                drawBitmapOnView(true);
                break;
            }
            case R.id.i_layer_delete_invisible:
                for (int i = tabs.size() - 1; i >= 0; --i) {
                    final Tab t = tabs.get(i);
                    if (t != tab && t.cbLayerVisible.getVisibility() == View.VISIBLE && !t.visible) {
                        closeTab(i);
                    }
                }
                if (!tab.visible && tabs.size() > 1) {
                    closeTab();
                }
                break;

            case R.id.i_layer_draw_below: {
                final boolean checked = !item.isChecked();
                item.setChecked(checked);
                tab.drawBelow = checked;
                drawBitmapOnView(true);
                break;
            }
            case R.id.i_layer_duplicate: {
                drawFloatingLayers();
                final Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                        bitmap.getConfig(), true, bitmap.getColorSpace());
                final Canvas cv = new Canvas(bm);
                if (hasSelection) {
                    cv.drawBitmap(bitmap, selection, selection, PAINT_SRC);
                } else {
                    cv.drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
                }
                addBitmap(bm, tab.visible, tabLayout.getSelectedTabPosition());
                break;
            }
            case R.id.i_layer_duplicate_by_color_range: {
                drawFloatingLayers();
                createPreviewBitmap();
                new ColorRangeDialog(this)
                        .setOnColorRangeChangeListener(onColorRangeChangeListener)
                        .setOnPositiveButtonClickListener(onLayerDuplicateByColorRangeConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();
                break;
            }
            case R.id.i_layer_hsv: {
                final boolean checked = !item.isChecked();
                if (checked) {
                    disableAllLayerFilters();
                }
                item.setChecked(checked);
                tab.filter = checked ? Tab.Filter.HSV : null;
                miLayerFilterSet.setEnabled(checked);
                drawBitmapOnView(true);
                break;
            }
            case R.id.i_layer_filter_set:
                if (tab.filter == null) {
                    break;
                }
                switch (tab.filter) {
                    case COLOR_FILTER:
                        ColorMatrixManager.make(this,
                                        R.string.channel_mixer,
                                        onLayerColorFilterChangeListener,
                                        null,
                                        tab.colorMatrix)
                                .show();
                        clearStatus();
                        break;
                    case CURVES:
                        new CurvesDialog(this)
                                .setSource(bitmap)
                                .setDefaultCurves(tab.curves)
                                .setOnCurvesChangeListener((curves, stopped) -> drawBitmapOnView(stopped))
                                .setOnPositiveButtonClickListener(null)
                                .show();
                        clearStatus();
                        break;
                    case HSV:
                        new HsvDialog(this)
                                .setOnHSVChangeListener(onLayerHSVChangeListener)
                                .setOnPositiveButtonClickListener(null)
                                .setDefaultDeltaHSV(tab.deltaHsv)
                                .show();
                        tvStatus.setText(String.format(getString(R.string.state_hsv),
                                tab.deltaHsv[0], tab.deltaHsv[1], tab.deltaHsv[2]));
                        break;
                }
                break;

            case R.id.i_layer_merge: {
                drawFloatingLayers();
                final int pos = tabLayout.getSelectedTabPosition(), next = pos + 1, size = tabs.size();
                if (next >= size) {
                    break;
                }
                final int w = bitmap.getWidth(), h = bitmap.getHeight();
                for (int i = next; i < size; ++i) {
                    final Tab t = tabs.get(i);
                    if (t.visible && t.bitmap.getWidth() == w && t.bitmap.getHeight() == h) {
                        new Canvas(t.bitmap).drawBitmap(tab.bitmap, 0.0f, 0.0f, tab.paint);
                        t.history.offer(t.bitmap);
                        break;
                    }
                }
                closeTab();
                tabLayout.getTabAt(pos).select();
                break;
            }
            case R.id.i_layer_merge_as_hidden: {
                drawFloatingLayers();
                final int j = tabLayout.getSelectedTabPosition() + 1;
                if (j >= tabs.size()) {
                    break;
                }
                HiddenImageMaker.merge(this,
                        new Bitmap[]{bitmap, tabs.get(j).bitmap},
                        onFinishMakingHiddenImageListener);
                break;
            }
            case R.id.i_layer_merge_visible: {
                drawFloatingLayers();
                final int selected = tabLayout.getSelectedTabPosition();
                final int w = bitmap.getWidth(), h = bitmap.getHeight();
                final Bitmap bm = mergeLayers(layerTree, new Rect(0, 0, w, h));
                addBitmap(bm, selected);
                break;
            }
            case R.id.i_layer_new:
                drawFloatingLayers();
                createLayer(bitmap.getWidth(), bitmap.getHeight(),
                        bitmap.getConfig(), true, bitmap.getColorSpace(),
                        tabLayout.getSelectedTabPosition());
                break;

            case R.id.i_layer_rename: {
                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, onLayerRenameDialogPosButtonClickListener)
                        .setTitle(R.string.rename)
                        .setView(R.layout.file_name)
                        .show();

                final EditText et = dialog.findViewById(R.id.et_file_name);

                et.setFilters(FILTERS_FILE_NAME);
                et.setText(getTabName());
                dialog.findViewById(R.id.s_file_type).setVisibility(View.GONE);

                break;
            }
            case R.id.i_layer_level_down:
                ++tab.level;
                computeLayerTree();
                drawBitmapOnView(true);
                showLayerLevel();
                miLayerLevelUp.setEnabled(true);
                break;

            case R.id.i_layer_level_up:
                if (tab.level <= 0) {
                    break;
                }
                --tab.level;
                computeLayerTree();
                drawBitmapOnView(true);
                showLayerLevel();
                miLayerLevelUp.setEnabled(tab.level > 0);
                break;

            case R.id.i_noise:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.noise).setMin(0).setMax(100).setProgress(0)
                        .setOnChangeListener(onNoiseSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onPreviewConfirmListener)
                        .setOnCancelListener(onPreviewCancelListener)
                        .show();
                clearStatus();
                break;

            case R.id.i_paste: {
                if (clipboard == null) {
                    break;
                }
                drawFloatingLayers();

                boolean si = !hasSelection; // Is selection invisible
                if (hasSelection) {
                    final Rect vs = getVisibleSubset();
                    si = !(vs.left < selection.right && selection.left < vs.right
                            && vs.top < selection.bottom && selection.top < vs.bottom);
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
                break;
            }
            case R.id.i_redo:
                if (tab.history.canRedo()) {
                    undoOrRedo(tab.history.redo());
                }
                break;

            case R.id.i_rotate_90:
                drawFloatingLayers();
                rotate(90.0f, false);
                break;

            case R.id.i_rotate_180:
                drawFloatingLayers();
                rotate(180.0f, false);
                break;

            case R.id.i_rotate_270:
                drawFloatingLayers();
                rotate(270.0f, false);
                break;

            case R.id.i_select_all:
                selectAll();
                hasSelection = true;
                drawSelectionOnView();
                selectionBeginX = selection.left;
                selectionBeginY = selection.top;
                selectionEndX = selection.right;
                selectionEndY = selection.bottom;
                clearStatus();
                break;

            case R.id.i_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.i_size: {
                drawFloatingLayers();
                ImageSizeManager
                        .make(this, bitmap, onUpdateImageSizeListener)
                        .show();
                break;
            }

            case R.id.i_undo: {
                if (transformer != null) {
                    undoOrRedo(tab.history.getCurrent());
                } else if (!isShapeStopped) {
                    isShapeStopped = true;
                    eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
                } else if (tab.history.canUndo()) {
                    undoOrRedo(tab.history.undo());
                }
                break;
            }

            case R.id.i_view_refit:
                resetTranslAndScale();
                translationX = tab.translationX;
                translationY = tab.translationY;
                scale = tab.scale;
                drawAfterTranslatingOrScaling(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChange(View.OnTouchListener onImageViewTouchListener) {
        cbZoom.setChecked(false);
        cbZoom.setTag(onImageViewTouchListener);
        flImageView.setOnTouchListener(onImageViewTouchListener);
        hideToolOptions();
        if (bitmapSource != null) {
            bitmapSource.recycle();
            bitmapSource = null;
        }
        isShapeStopped = true;
        eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
        paint.setAntiAlias(antiAlias);
        setBlurRadius(paint, blurRadius);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChange(boolean isChecked, View.OnTouchListener onImageViewTouchListener) {
        onToolChange(isChecked, onImageViewTouchListener, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChange(boolean isChecked, View.OnTouchListener onImageViewTouchListener, View toolOption) {
        if (isChecked) {
            onToolChange(onImageViewTouchListener);
            if (toolOption != null) {
                toolOption.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onTransformerChange(View.OnTouchListener l) {
        cbTransformerLar.setVisibility(l == onImageViewTouchWithTransformerOfScaleListener ? View.VISIBLE : View.GONE);
        onImageViewTouchWithTransformerListener = l;
        cbZoom.setTag(l);
        if (!cbZoom.isChecked()) {
            flImageView.setOnTouchListener(l);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasNotLoaded && hasFocus) {
            hasNotLoaded = false;
            load();
        }
    }

    private void openBitmap(Bitmap bitmap, Uri uri) {
        final int width = bitmap.getWidth(), height = bitmap.getHeight();
        final Bitmap bm = Bitmap.createBitmap(width, height,
                bitmap.getConfig(), bitmap.hasAlpha(), bitmap.getColorSpace());
        new Canvas(bm).drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
        bitmap.recycle();
        final DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        final String type = documentFile.getType();
        if (type != null) {
            String path = null;
            Bitmap.CompressFormat compressFormat = null;
            switch (type) {
                case "image/jpeg":
                    compressFormat = Bitmap.CompressFormat.JPEG;
                    break;
                case "image/png":
                    compressFormat = Bitmap.CompressFormat.PNG;
                    break;
                case "image/webp":
                    compressFormat = Bitmap.CompressFormat.WEBP_LOSSLESS;
                    break;
                case "image/gif":
                default:
                    Toast.makeText(this, R.string.not_supported_file_type, Toast.LENGTH_SHORT).show();
                    break;
            }
            if (compressFormat != null) {
                path = UriToPathUtil.getRealFilePath(this, uri);
            }
            addBitmap(bm, tabs.size(), documentFile.getName(), path, compressFormat);
        } else {
            addBitmap(bm, tabs.size());
        }
    }

    private void openFile(Uri uri) {
        if (uri == null) {
            return;
        }
        try (final InputStream inputStream = getContentResolver().openInputStream(uri)) {
            final Bitmap bm = BitmapFactory.decodeStream(inputStream);
            openBitmap(bm, uri);
            bm.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void optimizeSelection() {
        final int bitmapWidth = bitmap.getWidth(), bitmapHeight = bitmap.getHeight();
        selection.sort();
        if (!selection.isEmpty()
                && selection.left < bitmapWidth && selection.top < bitmapHeight
                && selection.right > 0 && selection.bottom > 0) {
            selection.set(Math.max(0, selection.left), Math.max(0, selection.top),
                    Math.min(bitmapWidth, selection.right), Math.min(bitmapHeight, selection.bottom));
        } else {
            hasSelection = false;
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

    private void resetTranslAndScale() {
        tab.translationX = 0.0f;
        tab.translationY = 0.0f;
        tab.scale = (float) ((double) viewWidth / (double) tab.bitmap.getWidth());
        imageWidth = viewWidth;
        imageHeight = (int) (tab.bitmap.getHeight() * tab.scale);
    }

    private void resizeBitmap(int width, int height, boolean stretch) {
        final Bitmap bm = Bitmap.createBitmap(width, height,
                bitmap.getConfig(), bitmap.hasAlpha(), bitmap.getColorSpace());
        final Canvas cv = new Canvas(bm);
        if (stretch) {
            cv.drawBitmap(bitmap,
                    new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                    new RectF(0.0f, 0.0f, width, height),
                    PAINT_SRC);
        } else {
            cv.drawRect(0.0f, 0.0f, width, height, eraser);
            cv.drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
        }
        bitmap.recycle();
        bitmap = bm;
        tab.bitmap = bitmap;
        canvas = cv;
        imageWidth = (int) toScaled(width);
        imageHeight = (int) toScaled(height);

//        calculateStackingOrder();
        computeLayerTree();

        if (transformer != null) {
            recycleTransformer();
        }
        hasSelection = false;

        drawChessboardOnView();
        drawGridOnView();
        drawSelectionOnView();

        clearStatus();
    }

    @ColorInt
    private static int rgb(@ColorInt int color) {
        return color & 0x00FFFFFF;
    }

    private void rotate(float degrees) {
        rotate(degrees, true);
    }

    private void rotate(float degrees, boolean filter) {
        int left, top, width, height;
        if (hasSelection) {
            left = selection.left;
            top = selection.top;
            width = selection.width();
            height = selection.height();
        } else {
            left = 0;
            top = 0;
            width = bitmap.getWidth();
            height = bitmap.getHeight();
        }
        final Matrix matrix = new Matrix();
        matrix.setRotate(degrees, width / 2.0f, height / 2.0f);
        final Bitmap bm = Bitmap.createBitmap(bitmap, left, top, width, height, matrix, filter);
        canvas.drawBitmap(bm, left, top, PAINT_SRC);
        bm.recycle();
        drawBitmapOnView(left, top, left + width, top + height);
        addHistory();
    }

    private void runOrStart(final Runnable target) {
        runOrStart(target, false);
    }

    private void runOrStart(final Runnable target, final boolean wait) {
        runnableRunner.runRunnable(target, wait);
    }

    private static int satX(Bitmap bitmap, int x) {
        final int w = bitmap.getWidth();
        return x <= 0 ? 0 : x >= w ? w - 1 : x;
    }

    private static int satY(Bitmap bitmap, int y) {
        final int h = bitmap.getHeight();
        return y <= 0 ? 0 : y >= h ? h - 1 : y;
    }

    private static float saturate(float v) {
        return v <= 0.0f ? 0.0f : v >= 1.0f ? 1.0f : v;
    }

    private void save() {
        if (tab.path == null) {
            getTree.launch(null);
            return;
        }

        drawFloatingLayers();

        final File file = new File(tab.path);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(tab.compressFormat, 100, fos);
            fos.flush();
        } catch (IOException e) {
            Toast.makeText(this, "Failed\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        final Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }

    private void saveAs() {
        getTree.launch(null);
    }

    private void scale(float x, float y) {
        scale(x, y, true);
    }

    private void scale(float x, float y, boolean filter) {
        int left, top, width, height;
        if (hasSelection) {
            left = selection.left;
            top = selection.top;
            width = selection.width();
            height = selection.height();
        } else {
            left = 0;
            top = 0;
            width = bitmap.getWidth();
            height = bitmap.getHeight();
        }
        final Matrix matrix = new Matrix();
        matrix.setScale(x, y, 0.0f, 0.0f);
        final Bitmap bm = Bitmap.createBitmap(bitmap, left, top, width, height, matrix, filter);
        canvas.drawBitmap(bm, left, top, PAINT_SRC);
        bm.recycle();
        drawBitmapOnView(left, top, left + width, top + height);
        addHistory();
    }

    private static void scaleAlpha(Bitmap bitmap) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < area; ++i) {
            if (Color.alpha(pixels[i]) > 0x00) {
                pixels[i] |= Color.BLACK;
            }
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private void selectAll() {
        selection.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    @SuppressLint("ClickableViewAccessibility")
    void setArgbComponentType() {
        onImageViewTouchWithEyedropperListener = settings.getArgbComponentType()
                ? onImageViewTouchWithEyedropperImpreciseListener : onImageViewTouchWithEyedropperPreciseListener;
        if (rbEyedropper != null && rbEyedropper.isChecked()) {
            cbZoom.setTag(onImageViewTouchWithEyedropperListener);
            if (!cbZoom.isChecked()) {
                flImageView.setOnTouchListener(onImageViewTouchWithEyedropperListener);
            }
        }
    }

    private void setBlurRadius(Paint paint, float f) {
        paint.setMaskFilter(f > 0.0f ? new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL) : null);
    }

    void setRunnableRunner(boolean multithreaded) {
        runnableRunner = multithreaded ? runnableStartingRunner : runnableRunningRunner;
    }

    private void showLayerLevel() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tab.level; ++i) {
            sb.append('→');
        }
        tab.tvLayerLevel.setText(sb);
    }

    private void stretchByBound(float viewX, float viewY) {
        dragBound(viewX, viewY);
        if (cbTransformerLar.isChecked()) {
            if (draggingBound == Position.LEFT || draggingBound == Position.RIGHT) {
                final double halfHeight = selection.width() / transformer.getAspectRatio() / 2.0;
                selection.top = (int) (transformer.getCenterY() - halfHeight);
                selection.bottom = (int) (transformer.getCenterY() + halfHeight);
            } else if (draggingBound == Position.TOP || draggingBound == Position.BOTTOM) {
                final double halfWidth = selection.height() * transformer.getAspectRatio() / 2.0;
                selection.left = (int) (transformer.getCenterX() - halfWidth);
                selection.right = (int) (transformer.getCenterX() + halfWidth);
            }
        }
        drawSelectionOnView(true);
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
     * @return X coordinate on bitmap.
     */
    private int toBitmapX(float x) {
        return (int) ((x - translationX) / scale);
    }

    /**
     * @return Y coordinate on bitmap.
     */
    private int toBitmapY(float y) {
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
     * @return X coordinate on view.
     */
    private float toViewX(int x) {
        return translationX + x * scale;
    }

    /**
     * @return X coordinate on view.
     */
    private float toViewX(float x) {
        return translationX + x * scale;
    }

    /**
     * @return Y coordinate on view.
     */
    private float toViewY(int y) {
        return translationY + y * scale;
    }

    /**
     * @return Y coordinate on view.
     */
    private float toViewY(float y) {
        return translationY + y * scale;
    }

    private void undoOrRedo(Bitmap bm) {
        optimizeSelection();
        bitmap.recycle();
        bitmap = bm;
        tab.bitmap = bitmap;
        canvas = new Canvas(bitmap);

        imageWidth = (int) toScaled(bitmap.getWidth());
        imageHeight = (int) toScaled(bitmap.getHeight());

        if (transformer != null) {
            recycleTransformer();
        }

        miHasAlpha.setChecked(bitmap.hasAlpha());

        optimizeSelection();
        isShapeStopped = true;
        hasDragged = false;
        if (magErB != null && magErF != null) {
            drawCrossOnView(magErB.x, magErB.y, true);
            drawCrossOnView(magErF.x, magErF.y, false);
        } else {
            eraseBitmapAndInvalidateView(previewBitmap, ivPreview);
        }

        computeLayerTree();

        drawBitmapOnView(true, true);
        drawChessboardOnView();
        drawGridOnView();
        drawSelectionOnView();

        if (cloneStampSrc != null) {
            drawCrossOnView(cloneStampSrc.x, cloneStampSrc.y);
        }

        clearStatus();
    }
}