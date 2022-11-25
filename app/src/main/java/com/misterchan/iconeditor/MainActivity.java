package com.misterchan.iconeditor;

import static com.misterchan.iconeditor.Color.HSVToColor;
import static com.misterchan.iconeditor.Color.colorToHSV;

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
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

    private static final ColorMatrixColorFilter COLOR_MATRIX_REPLACE_BLACK_TO_TRANSPARENT = new ColorMatrixColorFilter(new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.3f, 0.4f, 0.3f, 0.0f, 0x00
    });

    private static final ColorMatrixColorFilter COLOR_MATRIX_REPLACE_WHITE_TO_TRANSPARENT = new ColorMatrixColorFilter(new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            -0.3f, -0.4f, -0.3f, 0.0f, 0xFF
    });

    private static final Pattern PATTERN_FILE_NAME = Pattern.compile("[\"*/:<>?\\\\|]");
    private static final Pattern PATTERN_TREE = Pattern.compile("^content://com\\.android\\.externalstorage\\.documents/tree/primary%3A(?<path>.*)$");

    private static final Bitmap.CompressFormat[] COMPRESS_FORMATS = {
            Bitmap.CompressFormat.PNG,
            Bitmap.CompressFormat.JPEG
    };

    private static final InputFilter[] FILTERS_FILE_NAME = new InputFilter[]{
            (source, sourceStart, sourceEnd, dest, destStart, destEnd) -> {
                final Matcher matcher = PATTERN_FILE_NAME.matcher(source.toString());
                if (matcher.find()) {
                    return "";
                }
                return null;
            }
    };

    private static final Paint PAINT_BLACK = new Paint() {
        {
            setColor(Color.BLACK);
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
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
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    };

    private static final Paint PAINT_DST_IN = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }
    };

    private static final Paint PAINT_DST_OUT = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
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
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    };

    private static final Paint PAINT_SRC_IN = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        }
    };

    private static final Paint PAINT_SRC_OVER = new Paint();

    private Bitmap bitmap;
    private Bitmap bitmapOriginal;
    private Bitmap chessboard;
    private Bitmap chessboardBitmap;
    private Bitmap clipboard;
    private Bitmap gridBitmap;
    private Bitmap previewBitmap;
    private Bitmap rulerHBitmap, rulerVBitmap;
    private Bitmap selectionBitmap;
    private Bitmap viewBitmap;
    private PreviewBitmap preview;
    private boolean antiAlias = false;
    private boolean hasNotLoaded = true;
    private boolean hasSelection = false;
    private boolean hasDragged = false;
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
    private CheckBox cbPatcherAntiAlias;
    private CheckBox cbPencilAntiAlias;
    private CheckBox cbShapeAntiAlias;
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
    private float blurRadius = 0.0f;
    private float pivotX, pivotY;
    private float prevX, prevY;
    private float scale;
    private float strokeWidth = 1.0f;
    private float textSize = 12.0f;
    private float translationX, translationY;
    private FrameLayout flImageView;
    private FrameLayout flToolOptions;
    private HorizontalScrollView hsvOptionsBucketFill;
    private HorizontalScrollView hsvOptionsCloneStamp;
    private HorizontalScrollView hsvOptionsMagicPaint;
    private ImageView imageView;
    private ImageView ivChessboard;
    private ImageView ivGrid;
    private ImageView ivPreview;
    private ImageView ivRulerH, ivRulerV;
    private ImageView ivSelection;
    private InputMethodManager inputMethodManager;
    private int imageWidth, imageHeight;
    private int selectionStartX, selectionStartY;
    private int selectionEndX, selectionEndY;
    private int shapeStartX, shapeStartY;
    private int textX, textY;
    private int threshold;
    private int viewWidth, viewHeight;
    private LayerTree layerTree;
    private LinearLayout llOptionsEraser;
    private LinearLayout llOptionsGradient;
    private LinearLayout llOptionsMagicEraser;
    private LinearLayout llOptionsPatcher;
    private LinearLayout llOptionsPencil;
    private LinearLayout llOptionsShape;
    private LinearLayout llOptionsText;
    private LinearLayout llOptionsTransformer;
    private LinkedList<Integer> palette;
    private final List<Tab> tabs = new ArrayList<>();
    private MenuItem miLayerDirection;
    private MenuItem miLayerLevelUp;
    private Point cloneStampSrc;
    private final Point cloneStampSrcDist = new Point(0, 0); // Dist. - Distance
    private Position draggingBound = Position.NULL;
    private RadioButton rbCloneStamp;
    private RadioButton rbMagicEraserLeft, rbMagicEraserRight;
    private CompoundButton cbMagicEraserPosition;
    private RadioButton rbTransformer;
    private final Rect selection = new Rect();
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

    @ColorInt
    private int color0;

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
            setColor(Color.BLACK);
            setStyle(Style.FILL_AND_STROKE);
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
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

    private final ActivityResultCallback<Uri> imageCallback = this::openFile;

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

    private final ActivityResultLauncher<String> getImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), imageCallback);

    private final ActivityResultLauncher<Uri> getTree =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), treeCallback);

    private final AfterTextChangedListener onBlurRadiusTextChangedListener = s -> {
        try {
            final float f = Float.parseFloat(s);
            blurRadius = f;
            setBlurRadius(f);
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
            final float f = Float.parseFloat(etTextSize.getText().toString());
            textSize = f;
            paint.setTextSize(f);
        } catch (NumberFormatException e) {
        }
        drawTextOnView();
    };

    private final DialogInterface.OnCancelListener onFilterCancelListener = dialog -> {
        drawBitmapOnView();
        preview.recycle();
        preview = null;
        tvStatus.setText("");
    };

    private final DialogInterface.OnClickListener onFilterConfirmListener = (dialog, which) -> {
        drawPreviewBitmapOnCanvas();
        addHistory();
        tvStatus.setText("");
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
            ColorPicker.make(MainActivity.this,
                            R.string.add,
                            settings,
                            (oldColor, newColor) -> {
                                palette.offerFirst(newColor);
                                colorAdapter.notifyDataSetChanged();
                            },
                            paint.getColor())
                    .show();

    private final View.OnClickListener onBackgroundColorClickListener = v ->
            ColorPicker.make(MainActivity.this,
                            R.string.background_color,
                            settings,
                            (oldColor, newColor) -> {
                                eraser.setColor(newColor);
                                vBackgroundColor.setBackgroundColor(newColor);
                            },
                            eraser.getColor())
                    .show();

    private final View.OnClickListener onCloneStampSrcButtonClickListener = v -> {
        cloneStampSrc = null;
        clearCanvasAndInvalidateView(previewCanvas, ivPreview);
    };

    private final View.OnClickListener onForegroundColorClickListener = v ->
            ColorPicker.make(MainActivity.this,
                            R.string.foreground_color,
                            settings,
                            (oldColor, newColor) -> {
                                paint.setColor(newColor);
                                vForegroundColor.setBackgroundColor(newColor);
                                if (isEditingText) {
                                    drawTextOnView();
                                }
                            },
                            paint.getColor())
                    .show();

    private final ColorRangeDialog.OnColorRangeChangeListener onColorRangeChangeListener = (hueMin, hueMax, valueMin, valueMax) -> {
        if (hueMin == 0 && hueMax == 360 && valueMin == 0x0 && valueMax == 0xFF) {
            preview.clearFilter();
        } else if (valueMin > valueMax) {
            preview.drawColor(Color.TRANSPARENT);
        } else {
            final int width = preview.getWidth(), height = preview.getHeight(), area = width * height;
            final int[] pixels = new int[area];
            preview.getPixels(pixels, 0, width, 0, 0, width, height);
            for (int i = 0; i < area; ++i) {
                final float h = hue(pixels[i]);
                final int v = luminosity(pixels[i]);
                if (!((hueMin <= h && h <= hueMax
                        || hueMin > hueMax && (hueMin <= h || h <= hueMax))
                        && (valueMin <= v && v <= valueMax))) {
                    pixels[i] = Color.TRANSPARENT;
                }
            }
            preview.setPixels(pixels, 0, width, 0, 0, width, height);
        }
        drawBitmapOnView(preview.getBitmap());
        tvStatus.setText(String.format(getString(R.string.state_color_range), hueMin, hueMax, valueMin, valueMax));
    };

    private final ColorRangeDialog.OnColorRangeChangeListener onLayerDuplicateByColorRangeConfirmListener = (hueMin, hueMax, valueMin, valueMax) -> {
        Bitmap bm;
        if (hasSelection) {
            Bitmap p = preview.getBitmap();
            bm = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Bitmap.Config.ARGB_8888);
            new Canvas(bm).drawBitmap(p, selection, selection, PAINT_SRC);
        } else {
            bm = Bitmap.createBitmap(preview.getBitmap());
        }
        preview.recycle();
        preview = null;
        addBitmap(bm, tab.visible, tabLayout.getSelectedTabPosition());
        tvStatus.setText("");
    };

    private final HiddenImageMaker.OnFinishSettingListener onFinishMakingHiddenImageListener = bm -> {
        createGraphic(bm.getWidth(), bm.getHeight(), tabLayout.getSelectedTabPosition() + 2);
        canvas.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC);
        drawBitmapOnView();
        bm.recycle();
    };

    private final NewGraphicPropertiesDialog.OnFinishSettingListener onFinishSettingNewGraphicPropertiesListener = this::createGraphic;

    private final HSVDialog.OnHSVChangeListener onFilterHSVChangeListener = deltaHSV -> {
        if (deltaHSV[0] == 0.0f && deltaHSV[1] == 0.0f && deltaHSV[2] == 0.0f) {
            preview.clearFilter();
        } else {
            final int w = preview.getWidth(), h = preview.getHeight(), area = w * h;
            final int[] pixels = new int[area];
            preview.getPixels(pixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < area; ++i) {
                final int pixel = pixels[i];
                float[] hsv = colorToHSV(pixel);
                hsv[0] = (hsv[0] + deltaHSV[0] + 360.0f) % 360.0f;
                hsv[1] = saturate(hsv[1] + deltaHSV[1]);
                hsv[2] = saturate(hsv[2] + deltaHSV[2]);
                pixels[i] = pixel & 0xFF000000 | HSVToColor(hsv);
            }
            preview.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        drawBitmapOnView(preview.getBitmap());
        tvStatus.setText(String.format(getString(R.string.state_hsv), deltaHSV[0], deltaHSV[1], deltaHSV[2]));

    };

    private final LevelsDialog.OnLevelsChangeListener onFilterLevelsChangeListener = (inputShadows, inputHighlights, outputShadows, outputHighlights) -> {
        final float ratio = (float) (outputHighlights - outputShadows) / (float) (inputHighlights - inputShadows);
        preview.addColorFilter(ratio, -inputShadows * ratio + outputShadows);
        drawBitmapOnView(preview.getBitmap());
    };

    private final ColorMatrixManager.OnMatrixElementsChangeListener onColorMatrixChangeListener = matrix -> {
        preview.addColorFilter(matrix);
        drawBitmapOnView(preview.getBitmap());
    };

    private final ColorMatrixManager.OnMatrixElementsChangeListener onLayerColorFilterChangeListener = matrix -> {
        tab.colorMatrix = matrix;
        drawBitmapOnView();
    };

    private final OnSeekBarProgressChangeListener onThresholdChangeListener = (seekBar, progress) -> {
        threshold = progress;
        if (progress == 0xFF) {
            preview.drawColor(Color.BLACK);
        } else if (progress == 0x0) {
            preview.clearFilter();
        } else {
            final int w = preview.getWidth(), h = preview.getHeight(), area = w * h;
            final int[] pixels = new int[area];
            preview.getPixels(pixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < area; ++i) {
                final int pixel = pixels[i];
                pixels[i] = Color.argb(Color.alpha(pixel),
                        Color.red(pixel) / progress * progress,
                        Color.green(pixel) / progress * progress,
                        Color.blue(pixel) / progress * progress);
            }
            preview.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        drawBitmapOnView(preview.getBitmap());
        tvStatus.setText(String.format(getString(R.string.state_threshold), progress));
    };

    private final DialogInterface.OnCancelListener onThresholdCancelListener = dialog -> {
        drawBitmapOnView();
        preview.recycle();
        preview = null;
        tvStatus.setText("");
    };

    private final DialogInterface.OnClickListener onThresholdConfirmListener = (dialog, which) -> {
        onThresholdCancelListener.onCancel(dialog);
    };

    private final View.OnClickListener onThresholdButtonClickListener = v -> {
        createPreviewBitmap();
        new SeekBarDialog(this).setTitle(R.string.threshold).setMin(0x0).setMax(0xFF)
                .setOnCancelListener(onThresholdCancelListener, false)
                .setOnPositiveButtonClickListener(onThresholdConfirmListener)
                .setOnProgressChangeListener(onThresholdChangeListener)
                .setProgress(threshold)
                .show();
        onThresholdChangeListener.onProgressChanged(null, threshold);
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
            calculateLayerTree();

            if (transformer != null) {
                recycleTransformer();
            }
            if (areSizesNotEqual) {
                hasSelection = false;
            }

            if (rbCloneStamp.isChecked()) {
                cloneStampSrc = null;
            }
            if (bitmapOriginal != null) {
                bitmapOriginal.recycle();
                bitmapOriginal = Bitmap.createBitmap(bitmap);
            }

            miLayerDirection.setChecked(MainActivity.this.tab.draw_below);
            miLayerLevelUp.setEnabled(MainActivity.this.tab.level > 0);
            for (int i = 0; i < BLEND_MODES.length; ++i) {
                final MenuItem mi = smBlendModes.getItem(i);
                final BlendMode blendMode = MainActivity.this.tab.paint.getBlendMode();
                mi.setChecked(blendMode == BLEND_MODES[i]);
            }

            drawChessboardOnView();
            drawBitmapOnView();
            drawGridOnView();
            drawSelectionOnView();
            clearCanvasAndInvalidateView(previewCanvas, ivPreview);

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

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithBucketListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
                if (!(0 <= unscaledX && unscaledX < bitmap.getWidth() && 0 <= unscaledY && unscaledY < bitmap.getHeight())) {
                    break;
                }
                if (cbBucketFillContiguous.isChecked()) {
                    floodFill(bitmap, unscaledX, unscaledY, paint.getColor(),
                            cbBucketFillIgnoreAlpha.isChecked(), threshold);
                } else {
                    bucketFill(bitmap, unscaledX, unscaledY, paint.getColor(),
                            cbBucketFillIgnoreAlpha.isChecked(), threshold);
                }
                drawBitmapOnView();
                addHistory();
                tvStatus.setText("");
                break;
            }
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onImageViewTouchWithCloneStampListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (cloneStampSrc == null) {
                    break;
                }
                cloneStampSrcDist.x = cloneStampSrc.x - unscaledX;
                cloneStampSrcDist.y = cloneStampSrc.y - unscaledY;
                prevX = x;
                prevY = y;

            case MotionEvent.ACTION_MOVE: {
                if (cloneStampSrc == null) {
                    break;
                }
                final int unscaledPrevX = toUnscaled(prevX - translationX),
                        unscaledPrevY = toUnscaled(prevY - translationY);

                final int width = (int) (Math.abs(unscaledX - unscaledPrevX) + strokeWidth + blurRadius * 2.0f),
                        height = (int) (Math.abs(unscaledY - unscaledPrevY) + strokeWidth + blurRadius * 2.0f);
                final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final float rad = strokeWidth / 2.0f + blurRadius;
                final float left = Math.min(unscaledPrevX, unscaledX) - rad, top = Math.min(unscaledPrevY, unscaledY) - rad;
                final int l = (int) (left + cloneStampSrcDist.x), t = (int) (top + cloneStampSrcDist.y);
                final Canvas cv = new Canvas(bm);
                cv.drawLine(unscaledPrevX - left, unscaledPrevY - top,
                        unscaledX - left, unscaledY - top,
                        paint);
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
                drawBitmapOnView();
                drawCloneStampSrcOnView(unscaledX + cloneStampSrcDist.x, unscaledY + cloneStampSrcDist.y);
                tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));

                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (cloneStampSrc == null) {
                    cloneStampSrc = new Point(unscaledX, unscaledY);
                    drawCloneStampSrcOnView(unscaledX, unscaledY);
                    tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                } else {
                    drawCloneStampSrcOnView(cloneStampSrc.x, cloneStampSrc.y);
                    addHistory();
                    tvStatus.setText("");
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
                final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
                drawLineOnCanvas(
                        toUnscaled(prevX - translationX),
                        toUnscaled(prevY - translationY),
                        unscaledX,
                        unscaledY,
                        eraser);
                drawBitmapOnView();
                tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                addHistory();
                tvStatus.setText("");
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithEyedropperListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int unscaledX = inRange(toUnscaled(x - translationX), 0, bitmap.getWidth() - 1),
                        unscaledY = inRange(toUnscaled(y - translationY), 0, bitmap.getHeight() - 1);
                final int color = bitmap.getPixel(unscaledX, unscaledY);
                paint.setColor(color);
                vForegroundColor.setBackgroundColor(color);
                tvStatus.setText(String.format(
                        String.format(getString(R.string.state_eyedropper), settings.getArgbChannelsFormat()),
                        unscaledX, unscaledY,
                        Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color)));
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                tvStatus.setText("");
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onImageViewTouchWithGradientListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                paint.setStrokeWidth(toScaled((int) paint.getStrokeWidth()));
                if (isShapeStopped) {
                    isShapeStopped = false;
                    drawPointOnView(unscaledX, unscaledY);
                    shapeStartX = unscaledX;
                    shapeStartY = unscaledY;
                    color0 = bitmap.getPixel(inRange(unscaledX, 0, bitmap.getWidth() - 1),
                            inRange(unscaledY, 0, bitmap.getHeight() - 1));
                    tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                    break;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                final float startX = translationX + toScaled(shapeStartX + 0.5f),
                        startY = translationY + toScaled(shapeStartY + 0.5f),
                        stopX = translationX + toScaled(unscaledX + 0.5f),
                        stopY = translationY + toScaled(unscaledY + 0.5f);
                paint.setShader(new LinearGradient(startX, startY, stopX, stopY,
                        color0,
                        bitmap.getPixel(inRange(unscaledX, 0, bitmap.getWidth() - 1),
                                inRange(unscaledY, 0, bitmap.getHeight() - 1)),
                        Shader.TileMode.CLAMP));
                clearCanvas(previewCanvas);
                previewCanvas.drawLine(startX, startY, stopX, stopY, paint);
                ivPreview.invalidate();
                tvStatus.setText(String.format(getString(R.string.state_start_stop),
                        shapeStartX, shapeStartY, unscaledX, unscaledY));
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                paint.setStrokeWidth(strokeWidth);
                if (unscaledX != shapeStartX || unscaledY != shapeStartY) {
                    paint.setShader(new LinearGradient(shapeStartX, shapeStartY, unscaledX, unscaledY,
                            color0,
                            bitmap.getPixel(inRange(unscaledX, 0, bitmap.getWidth() - 1),
                                    inRange(unscaledY, 0, bitmap.getHeight() - 1)),
                            Shader.TileMode.CLAMP));
                    drawLineOnCanvas(shapeStartX, shapeStartY, unscaledX, unscaledY, paint);
                    isShapeStopped = true;
                    drawBitmapOnView();
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                    addHistory();
                    tvStatus.setText("");
                }
                paint.setShader(null);
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithMagicEraserAccurateListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (isShapeStopped) {
                    isShapeStopped = false;
                    shapeStartX = unscaledX;
                    shapeStartY = unscaledY;
                    color0 = bitmapOriginal.getPixel(inRange(unscaledX, 0, bitmapOriginal.getWidth() - 1),
                            inRange(unscaledY, 0, bitmapOriginal.getHeight() - 1));
                    tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                    break;
                }

            case MotionEvent.ACTION_MOVE:
                tvStatus.setText(String.format(getString(R.string.state_start_stop),
                        shapeStartX, shapeStartY, unscaledX, unscaledY));
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (unscaledX == shapeStartX && unscaledY == shapeStartY) {
                    break;
                }
                isShapeStopped = true;
                final int color = bitmapOriginal.getPixel(inRange(unscaledX, 0, bitmapOriginal.getWidth() - 1),
                        inRange(unscaledY, 0, bitmapOriginal.getHeight() - 1));
                final int rad = (int) (strokeWidth / 2.0f + blurRadius);
                final int left = Math.min(shapeStartX, unscaledX) - rad,
                        top = Math.min(shapeStartY, unscaledY) - rad,
                        right = Math.max(shapeStartX, unscaledX) + rad + 1,
                        bottom = Math.max(shapeStartY, unscaledY) + rad + 1;
                final int width = right - left, height = bottom - top;
                final int relativeX = unscaledX - left, relativeY = unscaledY - top;
                final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final Canvas cLine = new Canvas(bLine);
                cLine.drawLine(shapeStartX - left, shapeStartY - top,
                        relativeX, relativeY,
                        paint);
                canvas.drawBitmap(bLine, left, top, PAINT_DST_OUT);
                final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                new Canvas(bm).drawBitmap(bitmapOriginal,
                        new Rect(left, top, right, bottom),
                        new Rect(0, 0, width, height),
                        PAINT_SRC);
                BitmapUtil.removeBackground(bm, color, color0);
                cLine.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC_IN);
                bm.recycle();
                canvas.drawBitmap(bLine, left, top, PAINT_SRC_OVER);
                bLine.recycle();

                drawBitmapOnView();
                addHistory();
                tvStatus.setText("");
                break;
            }
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithMagicEraserConvenientListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                prevX = x;
                prevY = y;
                break;

            case MotionEvent.ACTION_MOVE: {
                final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
                final int unscaledPrevX = toUnscaled(prevX - translationX), unscaledPrevY = toUnscaled(prevY - translationY);
                final int rad = (int) (strokeWidth / 2.0f);
                final float radF = toScaled(rad);
                final double theta = Math.atan2(y - prevY, x - prevX);
                final int colorLeft = bitmapOriginal.getPixel(
                        inRange(toUnscaled(x + radF * (float) Math.sin(theta) - translationX), 0, bitmapOriginal.getWidth() - 1),
                        inRange(toUnscaled(y - radF * (float) Math.cos(theta) - translationY), 0, bitmapOriginal.getHeight() - 1));
                final int colorRight = bitmapOriginal.getPixel(
                        inRange(toUnscaled(x - radF * (float) Math.sin(theta) - translationX), 0, bitmapOriginal.getWidth() - 1),
                        inRange(toUnscaled(y + radF * (float) Math.cos(theta) - translationY), 0, bitmapOriginal.getHeight() - 1));
                final int backgroundColor = cbMagicEraserPosition == rbMagicEraserLeft ? colorLeft : colorRight;
                final int foregroundColor = backgroundColor == colorLeft ? colorRight : colorLeft;

                final int left = Math.min(unscaledPrevX, unscaledX) - rad,
                        top = Math.min(unscaledPrevY, unscaledY) - rad,
                        right = Math.max(unscaledPrevX, unscaledX) + rad + 1,
                        bottom = Math.max(unscaledPrevY, unscaledY) + rad + 1;
                final int width = right - left, height = bottom - top;
                final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final Canvas cLine = new Canvas(bLine);
                cLine.drawLine(unscaledPrevX - left, unscaledPrevY - top,
                        unscaledX - left, unscaledY - top,
                        paint);
                canvas.drawBitmap(bLine, left, top, PAINT_DST_OUT);
                final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                new Canvas(bm).drawBitmap(bitmapOriginal,
                        new Rect(left, top, right, bottom),
                        new Rect(0, 0, width, height),
                        PAINT_SRC);
                BitmapUtil.removeBackground(bm, foregroundColor, backgroundColor);
                cLine.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC_IN);
                bm.recycle();
                canvas.drawBitmap(bLine, left, top, PAINT_SRC_OVER);
                bLine.recycle();

                drawBitmapOnView();
                tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                prevX = x;
                prevY = y;
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                addHistory();
                tvStatus.setText("");
                break;
        }
        return true;
    };

    private View.OnTouchListener onImageViewTouchWithMagicEraserListener = onImageViewTouchWithMagicEraserConvenientListener;

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
                final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
                final int unscaledPrevX = toUnscaled(prevX - translationX), unscaledPrevY = toUnscaled(prevY - translationY);

                final int rad = (int) (strokeWidth / 2.0f + blurRadius);
                final int left = Math.min(unscaledPrevX, unscaledX) - rad,
                        top = Math.min(unscaledPrevY, unscaledY) - rad,
                        right = Math.max(unscaledPrevX, unscaledX) + rad + 1,
                        bottom = Math.max(unscaledPrevY, unscaledY) + rad + 1;
                final int width = right - left, height = bottom - top;
                final int relativeX = unscaledX - left, relativeY = unscaledY - top;
                final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final Canvas cLine = new Canvas(bLine);
                cLine.drawLine(unscaledPrevX - left, unscaledPrevY - top,
                        relativeX, relativeY,
                        paint);
                if (threshold < 0xFF) {
                    final Bitmap bm = Bitmap.createBitmap(bLine);
                    final Rect absolute = new Rect(left, top, right, bottom),
                            relative = new Rect(0, 0, width, height);
                    new Canvas(bm).drawBitmap(bitmapOriginal, absolute, relative, PAINT_SRC_IN);
                    final Bitmap bThr = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444); // Threshold
                    floodFill(bm, bThr, relativeX, relativeY, Color.BLACK, true, threshold);
                    bm.recycle();
                    cLine.drawBitmap(bThr, 0.0f, 0.0f, PAINT_DST_IN);
                    bThr.recycle();
                }
                canvas.drawBitmap(bLine, left, top, magicPaint);
                bLine.recycle();

                drawBitmapOnView();
                tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                addHistory();
                tvStatus.setText("");
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
        if (selection.left + radius * 2 >= selection.right || selection.top + radius * 2 >= selection.bottom) {
            return true;
        }
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                createPreviewBitmap();

            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
                final int w = selection.width(), h = selection.height();
                final Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                final Canvas cv = new Canvas(bm);
                final int wh = w >> 1, hh = h >> 1; // h - Half
                cv.drawBitmap(bitmap,
                        new Rect(unscaledX - wh, unscaledY - hh, unscaledX + w - wh, unscaledY + h - hh),
                        new Rect(0, 0, w, h),
                        PAINT_SRC);
                final Bitmap rect = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                new Canvas(rect).drawRect(radius, radius, w - radius, h - radius, paint);
                cv.drawBitmap(rect, 0.0f, 0.0f, patcher);
                rect.recycle();
                preview.reset();
                preview.drawBitmap(bm);
                bm.recycle();
                drawBitmapOnView(preview.getBitmap());
                tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                drawPreviewBitmapOnCanvas();
                preview.recycle();
                preview = null;
                addHistory();
                tvStatus.setText("");
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithPencilListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX(), y = event.getY();
                prevX = x;
                prevY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = event.getX(), y = event.getY();
                final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
                drawLineOnCanvas(
                        toUnscaled(prevX - translationX),
                        toUnscaled(prevY - translationY),
                        unscaledX,
                        unscaledY,
                        paint);
                drawBitmapOnView();
                tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                addHistory();
                tvStatus.setText("");
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
                        if (hasSelection && selectionStartX == selectionEndX - 1 && selectionStartY == selectionEndY - 1) {
                            selectionEndX = toUnscaled(x - translationX) + 1;
                            selectionEndY = toUnscaled(y - translationY) + 1;
                        } else {
                            hasSelection = true;
                            selectionStartX = toUnscaled(x - translationX);
                            selectionStartY = toUnscaled(y - translationY);
                            selectionEndX = selectionStartX + 1;
                            selectionEndY = selectionStartY + 1;
                        }
                        drawSelectionOnViewByStartsAndEnds();
                        tvStatus.setText(String.format(getString(R.string.state_start_end_size_1),
                                selectionStartX, selectionStartY, selectionStartX, selectionStartY));
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
                    selectionEndX = toUnscaled(x - translationX) + 1;
                    selectionEndY = toUnscaled(y - translationY) + 1;
                    drawSelectionOnViewByStartsAndEnds();
                    tvStatus.setText(String.format(getString(R.string.state_start_end_size),
                            selectionStartX, selectionStartY, selectionEndX - 1, selectionEndY - 1,
                            Math.abs(selectionEndX - selectionStartX - 1) + 1, Math.abs(selectionEndY - selectionStartY - 1) + 1));
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
                                "");
                    }
                } else {
                    tvStatus.setText(hasSelection ?
                            String.format(getString(R.string.state_l_t_r_b_size),
                                    selection.left, selection.top, selection.right - 1, selection.bottom - 1,
                                    selection.width(), selection.height()) :
                            "");
                }
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onImageViewTouchWithShapeListener = (v, event) -> {
        final float x = event.getX(), y = event.getY();
        final int unscaledX = toUnscaled(x - translationX), unscaledY = toUnscaled(y - translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                paint.setStrokeWidth(toScaled((int) paint.getStrokeWidth()));
                if (isShapeStopped) {
                    isShapeStopped = false;
                    drawPointOnView(unscaledX, unscaledY);
                    shapeStartX = unscaledX;
                    shapeStartY = unscaledY;
                    tvStatus.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                    break;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                final String result = drawShapeOnView(shapeStartX, shapeStartY, unscaledX, unscaledY);
                tvStatus.setText(
                        String.format(getString(R.string.state_start_stop_),
                                shapeStartX, shapeStartY, unscaledX, unscaledY)
                                + result);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                paint.setStrokeWidth(strokeWidth);
                if (unscaledX != shapeStartX || unscaledY != shapeStartY) {
                    drawShapeOnCanvas(shapeStartX, shapeStartY, unscaledX, unscaledY);
                    isShapeStopped = true;
                    drawBitmapOnView();
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                    addHistory();
                    tvStatus.setText("");
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
                    textX = toUnscaled(x - prevX);
                    textY = toUnscaled(y - prevY);
                    drawTextOnView();
                    break;
                }
            }

        } else {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    textX = toUnscaled(event.getX() - translationX);
                    textY = toUnscaled(event.getY() - translationY);
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
                ivSelection.setPivotX(translationX + toScaled(selection.exactCenterX()));
                ivSelection.setPivotY(translationY + toScaled(selection.exactCenterY()));
                prevTheta = (float) Math.atan2(y - ivSelection.getPivotY(), x - ivSelection.getPivotX());
                tvStatus.setText("");
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
                drawBitmapOnView();
                drawSelectionOnView();
                tvStatus.setText("");
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
                        drawBitmapOnView();
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
                            drawBitmapOnView();
                            drawSelectionOnView(false);
                            tvStatus.setText("");
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
                                translationX + toScaled(selection.left),
                                translationY + toScaled(selection.top),
                                translationX + toScaled(selection.right),
                                translationY + toScaled(selection.bottom));
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
                                scaledSelection.top = translationY + toScaled(selection.top);
                                scaledSelection.bottom = translationY + toScaled(selection.bottom);
                                transformerDpb.top = Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top);
                                transformerDpb.bottom = Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1);
                            } else {
                                selection.top -= toUnscaled(transformerDpb.top - dpb.top);
                                selection.bottom += toUnscaled(transformerDpb.bottom - dpb.bottom);
                                final double height = selection.height(), width = height * transformer.getAspectRatio();
                                selection.left = (int) (transformer.getCenterX() - width / 2.0);
                                selection.right = (int) (transformer.getCenterX() + width / 2.0);
                                scaledSelection.left = translationX + toScaled(selection.left);
                                scaledSelection.right = translationX + toScaled(selection.right);
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
                        final RectF scaledSelection = new RectF();
                        scaledSelection.set(translationX + toScaled(selection.left),
                                translationY + toScaled(selection.top),
                                translationX + toScaled(selection.right),
                                translationY + toScaled(selection.bottom));
                        transformer.getDpb().set(Math.min(x0 - scaledSelection.left, x1 - scaledSelection.left),
                                Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top),
                                Math.min(scaledSelection.right - x0, scaledSelection.right - x1),
                                Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1));
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
                        drawBitmapOnView();
                        drawSelectionOnView();
                        tvStatus.setText("");
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
                    drawBitmapOnView();
                    drawSelectionOnView(false);
                    tvStatus.setText(String.format(getString(R.string.state_left_top),
                            selection.left, selection.top));
                }
                prevX = x - translationX - toScaled(selection.left);
                prevY = y - translationY - toScaled(selection.top);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (transformer == null) {
                    break;
                }
                final float x = event.getX(), y = event.getY();
                selection.offsetTo(toUnscaled(x - translationX - prevX),
                        toUnscaled(y - translationY - prevY));
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
                                toUnscaled(x - translationX), toUnscaled(y - translationY)));
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        final float x = event.getX(), y = event.getY();
                        final float deltaX = x - prevX, deltaY = y - prevY;
                        translationX += deltaX;
                        translationY += deltaY;
                        drawAfterTranslatingOrScaling();
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        tvStatus.setText("");
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
                        drawAfterTranslatingOrScaling();
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
                        tvStatus.setText("");
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        final float x = event.getX(1 - event.getActionIndex());
                        final float y = event.getY(1 - event.getActionIndex());
                        tvStatus.setText(String.format(getString(R.string.coordinate),
                                toUnscaled(x - translationX), toUnscaled(y - translationY)));
                        prevX = event.getX(1 - event.getActionIndex());
                        prevY = event.getY(1 - event.getActionIndex());
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
            clearCanvasAndInvalidateView(previewCanvas, ivPreview);
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
            bitmapOriginal = Bitmap.createBitmap(bitmap);
            paint.setAntiAlias(false);
            paint.setMaskFilter(null);
            etMagicEraserStrokeWidth.setText(String.valueOf(strokeWidth));
            llOptionsMagicEraser.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onMagicPaintRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            drawFloatingLayers();
            onToolChange(onImageViewTouchWithMagicPaintListener);
            bitmapOriginal = Bitmap.createBitmap(bitmap);
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
            llOptionsPencil.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onShapeRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithShapeListener);
            cbShapeAntiAlias.setChecked(antiAlias);
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

    private final OnSeekBarProgressChangeListener onFilterContrastSeekBarProgressChangeListener = (seekBar, progress) -> {
        final float scale = progress / 10.0f, shift = 0xFF / 2.0f * (1.0f - scale);
        preview.addColorFilter(scale, shift);
        drawBitmapOnView(preview.getBitmap());
        tvStatus.setText(String.format(getString(R.string.state_contrast), scale));
    };

    private final OnSeekBarProgressChangeListener onFilterLightnessSeekBarProgressChangeListener = (seekBar, progress) -> {
        preview.addColorFilter(1.0f, progress);
        drawBitmapOnView(preview.getBitmap());
        tvStatus.setText(String.format(getString(R.string.state_lightness), progress));
    };

    private final OnSeekBarProgressChangeListener onFilterSaturationSeekBarProgressChangeListener = (seekBar, progress) -> {
        final float f = progress / 10.0f;
        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(f);
        preview.addColorFilter(colorMatrix.getArray());
        drawBitmapOnView(preview.getBitmap());
        tvStatus.setText(String.format(getString(R.string.state_saturation), f));
    };

    private final OnSeekBarProgressChangeListener onFilterThresholdSeekBarProgressChangeListener = (seekBar, progress) -> {
        final float f = -0x100 * progress;
        preview.addColorFilter(new float[]{
                0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        });
        drawBitmapOnView(preview.getBitmap());
        tvStatus.setText(String.format(getString(R.string.state_threshold), progress));
    };

    private final CellGridManager.OnUpdateListener onUpdateCellGridListener = this::drawGridOnView;

    private final ImageSizeManager.OnUpdateListener onUpdateImageSizeListener = (width, height, stretch) -> {
        resizeBitmap(width, height, stretch);
        drawBitmapOnView();
        addHistory();
    };

    private final OnSeekBarProgressChangeListener onLayerAlphaSeekBarProgressChangeListener = (seekBar, progress) -> {
        tab.paint.setAlpha(progress);
        drawBitmapOnView();
        tvStatus.setText(String.format(
                String.format(getString(R.string.state_alpha), settings.getArgbChannelsFormat()),
                progress));
    };

    private final Shape circle = new Shape() {
        @Override
        public void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
            canvas.drawCircle(x0 + 0.5f, y0 + 0.5f,
                    (int) Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0)),
                    paint);
        }

        @Override
        public String drawShapeOnView(int x0, int y0, int x1, int y1) {
            final int radius =
                    (int) Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0));
            previewCanvas.drawCircle(
                    translationX + toScaled(x0 + 0.5f),
                    translationY + toScaled(y0 + 0.5f),
                    toScaled(radius),
                    paint);
            return String.format(getString(R.string.state_radius), radius + 0.5f);
        }
    };

    private final Shape line = new Shape() {
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
                    translationX + toScaled(x0 + 0.5f),
                    translationY + toScaled(y0 + 0.5f),
                    translationX + toScaled(x1 + 0.5f),
                    translationY + toScaled(y1 + 0.5f),
                    paint);
            return String.format(getString(R.string.state_length), Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0)) + 1);
        }
    };

    private final Shape oval = new Shape() {
        @Override
        public void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
            final float startX = Math.min(x0, x1), startY = Math.min(y0, y1),
                    stopX = Math.max(x0, x1) + 1.0f, stopY = Math.max(y0, y1) + 1.0f;
            canvas.drawOval(startX, startY, stopX, stopY, paint);
        }

        @Override
        public String drawShapeOnView(int x0, int y0, int x1, int y1) {
            previewCanvas.drawOval(
                    translationX + toScaled(x0 + 0.5f),
                    translationY + toScaled(y0 + 0.5f),
                    translationX + toScaled(x1 + 0.5f),
                    translationY + toScaled(y1 + 0.5f),
                    paint);
            return String.format(getString(R.string.state_axes), Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1);
        }
    };

    private final Shape rect = new Shape() {
        @Override
        public void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
            final float startX = Math.min(x0, x1), startY = Math.min(y0, y1),
                    stopX = Math.max(x0, x1), stopY = Math.max(y0, y1);
            canvas.drawRect(startX, startY, stopX, stopY, paint);
        }

        @Override
        public String drawShapeOnView(int x0, int y0, int x1, int y1) {
            previewCanvas.drawRect(
                    translationX + toScaled(x0 + 0.5f),
                    translationY + toScaled(y0 + 0.5f),
                    translationX + toScaled(x1 + 0.5f),
                    translationY + toScaled(y1 + 0.5f),
                    paint);
            return String.format(getString(R.string.state_size), Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1);
        }
    };

    private Shape shape = rect;

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
                    pixels[i] = px & 0xFF000000 | rgb(color);
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

    private Position checkDraggingBound(float x, float y) {
        draggingBound = Position.NULL;
        isDraggingCorner = false;

        final RectF sb = new RectF( // sb - Selection Bounds
                translationX + toScaled(selection.left),
                translationY + toScaled(selection.top),
                translationX + toScaled(selection.right),
                translationY + toScaled(selection.bottom));

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

    private void calculateLayerTree() {
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

    private boolean checkIfColorIsWithinThreshold(int r0, int g0, int b0, int r, int g, int b) {
        return Math.abs(r - r0) <= threshold
                && Math.abs(g - g0) <= threshold
                && Math.abs(b - b0) <= threshold;
    }

    private static void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private static void clearCanvasAndInvalidateView(Canvas canvas, ImageView imageView) {
        clearCanvas(canvas);
        imageView.invalidate();
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

    private void createLayer(int width, int height, int position) {
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        addBitmap(bm, true, position);
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

    private void dragBound(float viewX, float viewY) {
        final float halfScale = scale / 2.0f;
        switch (draggingBound) {
            case LEFT: {
                final int left = toUnscaled(viewX - translationX + halfScale);
                if (left != selection.left) selection.left = left;
                else return;
                break;
            }
            case TOP: {
                final int top = toUnscaled(viewY - translationY + halfScale);
                if (top != selection.top) selection.top = top;
                else return;
                break;
            }
            case RIGHT: {
                final int right = toUnscaled(viewX - translationX + halfScale);
                if (right != selection.right) selection.right = right;
                else return;
                break;
            }
            case BOTTOM: {
                final int bottom = toUnscaled(viewY - translationY + halfScale);
                if (bottom != selection.bottom) selection.bottom = bottom;
                else return;
                break;
            }
            case NULL:
                return;
        }
        hasDragged = true;
    }

    private void drawAfterTranslatingOrScaling() {
        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
        if (transformer != null) {

        } else if (isEditingText) {
            drawTextOnView();
        } else if (!isShapeStopped) {
            drawPointOnView(shapeStartX, shapeStartY);
        } else if (cloneStampSrc != null) {
            drawCloneStampSrcOnView(cloneStampSrc.x, cloneStampSrc.y);
        }
        drawSelectionOnView();
    }

    private void drawBitmapOnCanvas(Bitmap bitmap, Canvas canvas, float translX, float translY) {
        final Rect vp = getVisiblePart(translX, translY, bitmap.getWidth(), bitmap.getHeight());
        drawBitmapOnCanvas(bitmap, canvas, translX, translY, vp);
    }

    private void drawBitmapOnCanvas(Bitmap bitmap, Canvas canvas, float translX, float translY, Rect vp) {
        if (vp.isEmpty()) {
            return;
        }
        final RectF svp = getScaledVisiblePart(bitmap, translX, translY);
        if (isScaledMuch()) {
            final int w = vp.width(), h = vp.height();
            final int[] pixels = new int[w * h];
            bitmap.getPixels(pixels, 0, w, vp.left, vp.top, w, h);
            float t = svp.top, b = t + scale;
            for (int i = 0, y = vp.top; y < vp.bottom; ++y, t += scale, b += scale) {
                float l = svp.left;
                for (int x = vp.left; x < vp.right; ++x, ++i) {
                    colorPaint.setColor(pixels[i]);
                    canvas.drawRect(l, t, l += scale, b, colorPaint);
                }
            }
        } else {
            canvas.drawBitmap(bitmap, vp, svp, PAINT_SRC);
        }
    }

    private void drawBitmapOnView() {
        drawBitmapOnView(bitmap);
    }

    private void drawBitmapOnView(Bitmap bitmap) {
        drawBitmapOnView(bitmap, settings.getMultithreaded());
    }

    private void drawBitmapOnView(Bitmap bitmap, boolean multithreaded) {
        if (multithreaded) {
            if (!thread.isAlive()) {
                thread = new Thread(() -> drawBmOnView(bitmap));
                thread.start();
            }
        } else {
            drawBmOnView(bitmap);
        }
    }

    private void drawBmOnView(final Bitmap bitmap) {
        clearCanvas(viewCanvas);
        final Rect vp = getVisiblePart(translationX, translationY, bitmap.getWidth(), bitmap.getHeight());
        if (vp.isEmpty()) {
            return;
        }
        final int w = vp.width(), h = vp.height();
        final Rect relative = new Rect(0, 0, w, h);

        final Bitmap merged = mergeLayers(layerTree, w, h, null, tab, (canvas, tab, paint) -> {
            if (tab == MainActivity.this.tab) {
                if (transformer != null) {
                    final Bitmap b = Bitmap.createBitmap(bitmap, vp.left, vp.top, w, h);
                    final Rect intersect = new Rect(Math.max(vp.left, selection.left), Math.max(vp.top, selection.top),
                            Math.min(vp.right, selection.right), Math.min(vp.bottom, selection.bottom));
                    new Canvas(b).drawBitmap(transformer.getBitmap(),
                            new Rect(intersect.left - selection.left, intersect.top - selection.top,
                                    intersect.right - selection.left, intersect.bottom - selection.top),
                            new Rect(intersect.left - vp.left, intersect.top - vp.top,
                                    intersect.right - vp.left, intersect.bottom - vp.top),
                            PAINT_SRC_OVER);
                    canvas.drawBitmap(b, 0.0f, 0.0f, paint);
                    b.recycle();
//              } else if (isEditingText) {
//
                } else {
                    canvas.drawBitmap(bitmap, vp, relative, paint);
                }
            } else if (tab.visible) {
                canvas.drawBitmap(tab.bitmap, vp, relative, paint);
            }
        });

        drawBitmapOnCanvas(merged, viewCanvas,
                translationX > -scale ? translationX : translationX % scale,
                translationY > -scale ? translationY : translationY % scale,
                relative);
        merged.recycle();
        runOnUiThread(() -> imageView.invalidate());
    }

    private void drawChessboardOnView() {
        clearCanvas(chessboardCanvas);

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

    private void drawCloneStampSrcOnView(int x, int y) {
        clearCanvas(previewCanvas);
        final float scaledX = translationX + toScaled(x), scaledY = translationY + toScaled(y);
        previewCanvas.drawLine(scaledX - 50.0f, scaledY, scaledX + 50.0f, scaledY, selector);
        previewCanvas.drawLine(scaledX, scaledY - 50.0f, scaledX, scaledY + 50.0f, selector);
        ivPreview.invalidate();
    }

    private void drawFloatingLayers() {
        drawTransformerOnCanvas();
        drawTextOnCanvas();
    }

    private void drawGridOnView() {
        clearCanvas(gridCanvas);

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

        CellGrid cellGrid = tab.cellGrid;
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
        clearCanvas(previewCanvas);
        fillPaint.setColor(paint.getColor());
        previewCanvas.drawRect(
                translationX + toScaled(x),
                translationY + toScaled(y),
                translationX + toScaled(x + 1),
                translationY + toScaled(y + 1),
                fillPaint);
        ivPreview.invalidate();
    }

    private void drawPreviewBitmapOnCanvas() {
        canvas.drawBitmap(preview.getBitmap(), 0.0f, 0.0f, PAINT_SRC);
        drawBitmapOnView();
    }

    private void drawRuler() {
        clearCanvas(rulerHCanvas);
        clearCanvas(rulerVCanvas);
        final int multiplier = (int) Math.ceil(96.0 / scale);
        final float scaledMultiplier = toScaled(multiplier);

        rulerPaint.setTextAlign(Paint.Align.LEFT);
        int unscaledX = (int) (-translationX / scaledMultiplier) * multiplier;
        for (float x = translationX % scaledMultiplier, height = rulerHBitmap.getHeight();
             x < viewWidth;
             x += scaledMultiplier, unscaledX += multiplier) {
            rulerHCanvas.drawLine(x, 0.0f, x, height, rulerPaint);
            rulerHCanvas.drawText(String.valueOf(unscaledX), x, height, rulerPaint);
        }

        rulerPaint.setTextAlign(Paint.Align.RIGHT);
        final float ascent = rulerPaint.getFontMetrics().ascent;
        int unscaledY = (int) (-translationY / scaledMultiplier) * multiplier;
        for (float y = translationY % scaledMultiplier, width = rulerVBitmap.getWidth();
             y < viewHeight;
             y += scaledMultiplier, unscaledY += multiplier) {
            rulerVCanvas.drawLine(0.0f, y, width, y, rulerPaint);
            rulerVCanvas.drawText(String.valueOf(unscaledY), width, y - ascent, rulerPaint);
        }

        ivRulerH.invalidate();
        ivRulerV.invalidate();
    }

    private void drawSelectionOnView() {
        drawSelectionOnView(false);
    }

    private void drawSelectionOnView(boolean showMargins) {
        clearCanvas(selectionCanvas);
        if (hasSelection) {
            final float left = Math.max(0.0f, translationX + toScaled(selection.left)),
                    top = Math.max(0.0f, translationY + toScaled(selection.top)),
                    right = Math.min(viewWidth, translationX + toScaled(selection.right)),
                    bottom = Math.min(viewHeight, translationY + toScaled(selection.bottom));
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

    private void drawSelectionOnView(float degrees) {

    }

    private void drawSelectionOnViewByStartsAndEnds() {
        clearCanvas(selectionCanvas);
        if (hasSelection) {
            if (selectionStartX < selectionEndX) {
                selection.left = selectionStartX;
                selection.right = selectionEndX;
            } else {
                selection.left = selectionEndX - 1;
                selection.right = selectionStartX + 1;
            }
            if (selectionStartY < selectionEndY) {
                selection.top = selectionStartY;
                selection.bottom = selectionEndY;
            } else {
                selection.top = selectionEndY - 1;
                selection.bottom = selectionStartY + 1;
            }
            selectionCanvas.drawRect(
                    translationX + toScaled(selection.left),
                    translationY + toScaled(selection.top),
                    translationX + toScaled(selection.right),
                    translationY + toScaled(selection.bottom),
                    selector);
        }
        ivSelection.invalidate();
    }

    private void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
        shape.drawShapeOnCanvas(x0, y0, x1, y1);
    }

    private String drawShapeOnView(int x0, int y0, int x1, int y1) {
        clearCanvas(previewCanvas);
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
        drawBitmapOnView();
        clearCanvasAndInvalidateView(previewCanvas, ivPreview);
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
        clearCanvas(previewCanvas);
        final float x = translationX + toScaled(textX), y = translationY + toScaled(textY);
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
        drawBitmapOnView();
        addHistory();
        tvStatus.setText("");
        clearCanvasAndInvalidateView(previewCanvas, ivPreview);
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
                newColor = px & 0xFF000000 | rgb(color);
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
            drawBitmapOnView();
        };
    }

    private RectF getScaledVisiblePart(Bitmap bitmap, float translX, float translY) {
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

    private Rect getVisiblePart() {
        return getVisiblePart(translationX, translationY, bitmap.getWidth(), bitmap.getHeight());
    }

    private Rect getVisiblePart(float translX, float translY, int width, int height) {
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

    private static float hue(@ColorInt int color) {
        final float r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        final float max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
        if (max == min) {
            return 0.0f;
        } else if (max == r) {
            return 60.0f * (g - b) / (max - min) + (g >= b ? 0.0f : 360.0f);
        } else if (max == g) {
            return 60.0f * (b - r) / (max - min) + 120.0f;
        } else if (max == b) {
            return 60.0f * (r - g) / (max - min) + 240.0f;
        } else {
            return 0.0f;
        }
    }

    private boolean isPaintStyleFill() {
        return paint.getStyle() != Paint.Style.STROKE;
    }

    private static float inRange(float a, float min, float max) {
        return a <= min ? min : a >= max ? max : a;
    }

    private static int inRange(int a, int min, int max) {
        return Math.max(Math.min(a, max), min);
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

        gridBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        gridCanvas = new Canvas(gridBitmap);
        ivGrid.setImageBitmap(gridBitmap);
        drawGridOnView();

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

        clearCanvasAndInvalidateView(previewCanvas, ivPreview);

        if (fileToBeOpened != null) {
            closeTab(0);
            openFile(fileToBeOpened);
        }
    }

    private static float luminance(@ColorInt int color) {
        return 0.2126f * Color.red(color) / 255.0f
                + 0.7152f * Color.green(color) / 255.0f
                + 0.0722f * Color.blue(color) / 255.0f;
    }

    private static int luminosity(@ColorInt int color) {
        return Math.max(Math.max(Color.red(color), Color.green(color)), Color.blue(color));
    }

    private static Bitmap mergeLayers(final LayerTree tree, final int w, final int h, final BitmapPrinter printer) {
        return mergeLayers(tree, w, h, null, null, printer);
    }

    private static Bitmap mergeLayers(final LayerTree tree,
                                      final int w, final int h, final Bitmap background,
                                      final Tab visible, final BitmapPrinter printer) {
        final LayerTree.Node root = tree.peekBackground();
        Bitmap bitmap = background == null
                ? Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                : Bitmap.createBitmap(background);
        Canvas canvas = new Canvas(bitmap);

        for (LayerTree.Node node = root; node != null; node = node.getFront()) {
            final Tab tab = node.getTab();
            if (!tab.visible && tab != visible) {
                continue;
            }
            final LayerTree branch = node.getBranch();
            final Paint paint = node != root || background != null ? tab.paint : PAINT_SRC;
            if (branch == null) {
                printer.run(canvas, tab, paint);
                if (tab.enableColorFilter) {
                    BitmapUtil.addColorFilter(bitmap, 0, 0, bitmap, 0, 0,
                            tab.colorMatrix);
                }
            } else {
                final Bitmap bm = mergeLayers(branch, w, h,
                        !tab.draw_below || node == root ? null : bitmap,
                        visible, printer);
                canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
                bm.recycle();
            }
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
        settings.update(this, preferences);

        // Locale
        final String loc = preferences.getString("loc", "def");
        if (!"def".equals(loc)) {
            Locale locale;
            final int i = loc.indexOf('_');
            String lang, country = "";
            if (i == -1) {
                lang = loc;
            } else {
                lang = loc.substring(0, i);
                country = loc.substring(i + 1);
            }
            locale = new Locale(lang, country);
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
        cbShapeAntiAlias = findViewById(R.id.cb_shape_anti_alias);
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
        imageView = findViewById(R.id.iv);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        ivChessboard = findViewById(R.id.iv_chessboard);
        ivGrid = findViewById(R.id.iv_grid);
        ivPreview = findViewById(R.id.iv_preview);
        ivRulerH = findViewById(R.id.iv_ruler_horizontal);
        ivRulerV = findViewById(R.id.iv_ruler_vertical);
        ivSelection = findViewById(R.id.iv_selection);
        llOptionsEraser = findViewById(R.id.ll_options_eraser);
        llOptionsGradient = findViewById(R.id.ll_options_gradient);
        llOptionsMagicEraser = findViewById(R.id.ll_options_magic_eraser);
        llOptionsPatcher = findViewById(R.id.ll_options_patcher);
        llOptionsPencil = findViewById(R.id.ll_options_pencil);
        llOptionsShape = findViewById(R.id.ll_options_shape);
        llOptionsText = findViewById(R.id.ll_options_text);
        llOptionsTransformer = findViewById(R.id.ll_options_transformer);
        final RecyclerView rvSwatches = findViewById(R.id.rv_swatches);
        rbCloneStamp = findViewById(R.id.rb_clone_stamp);
        rbMagicEraserLeft = findViewById(R.id.rb_magic_eraser_left);
        rbMagicEraserRight = findViewById(R.id.rb_magic_eraser_right);
        cbMagicEraserPosition = rbMagicEraserLeft;
        final RadioButton rbPencil = findViewById(R.id.rb_pencil);
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
        cbMagicPaintAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.cb_magic_paint_clear)).setOnCheckedChangeListener(((buttonView, isChecked) -> magicPaint.setBlendMode(isChecked ? BlendMode.DST_OUT : null)));
        ((CompoundButton) findViewById(R.id.cb_eraser_anti_alias)).setOnCheckedChangeListener((buttonView, isChecked) -> eraser.setAntiAlias(isChecked));
        cbGradientAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbPatcherAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbPencilAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbShapeAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
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
        ((RadioButton) findViewById(R.id.rb_bucket_fill)).setOnCheckedChangeListener(onBucketFillRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_circle)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = circle);
        rbCloneStamp.setOnCheckedChangeListener(onCloneStampRadioButtonCheckedChangeListener);
        ((RadioButton) findViewById(R.id.rb_magic_eraser)).setOnCheckedChangeListener(onMagicEraserRadioButtonCheckedChangeListener);
        ((RadioButton) findViewById(R.id.rb_magic_paint)).setOnCheckedChangeListener(onMagicPaintRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_eraser)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(isChecked, onImageViewTouchWithEraserListener, llOptionsEraser));
        ((CompoundButton) findViewById(R.id.rb_eyedropper)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(isChecked, onImageViewTouchWithEyedropperListener));
        ((CompoundButton) findViewById(R.id.rb_gradient)).setOnCheckedChangeListener(onGradientRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_line)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = line);
        ((CompoundButton) findViewById(R.id.rb_oval)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = oval);
        ((CompoundButton) findViewById(R.id.rb_patcher)).setOnCheckedChangeListener(onPatcherRadioButtonCheckedChangeListener);
        rbPencil.setOnCheckedChangeListener(onPencilRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_rect)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = rect);
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
                eraser.setMaskFilter(f > 0.0f ? new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL) : null);
            } catch (NumberFormatException e) {
            }
        });

        etEraserStrokeWidth.addTextChangedListener((AfterTextChangedListener) s -> {
            try {
                eraser.setStrokeWidth(Float.parseFloat(s));
            } catch (NumberFormatException e) {
            }
        });

        ((CompoundButton) findViewById(R.id.cb_magic_eraser_style)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            rgMagicEraserPosition.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            onImageViewTouchWithMagicEraserListener = isChecked ? onImageViewTouchWithMagicEraserAccurateListener : onImageViewTouchWithMagicEraserConvenientListener;
            cbZoom.setTag(onImageViewTouchWithMagicEraserListener);
            if (!cbZoom.isChecked()) {
                flImageView.setOnTouchListener(onImageViewTouchWithMagicEraserListener);
            }
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

        rvSwatches.setItemAnimator(new DefaultItemAnimator());
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        rvSwatches.setLayoutManager(layoutManager);
        palette = new LinkedList<Integer>() {
            {
                offer(Color.BLACK);
                offer(Color.WHITE);
                offer(Color.RED);
                offer(Color.YELLOW);
                offer(Color.GREEN);
                offer(Color.CYAN);
                offer(Color.BLUE);
                offer(Color.MAGENTA);
            }
        };
        colorAdapter = new ColorAdapter(palette) {
            {
                setOnItemClickListener(view -> {
                    final int color = ((ColorDrawable) view.getBackground()).getColor();
                    paint.setColor(color);
                    vForegroundColor.setBackgroundColor(color);
                    if (isEditingText) {
                        drawTextOnView();
                    }
                });
                setOnItemLongClickListener(view -> {
                    ColorPicker.make(MainActivity.this,
                                    R.string.swatch,
                                    settings,
                                    (oldColor, newColor) -> {
                                        if (newColor != null) {
                                            palette.set(palette.indexOf(oldColor), newColor);
                                        } else {
                                            palette.remove(oldColor);
                                        }
                                        colorAdapter.notifyDataSetChanged();
                                    },
                                    (Integer) view.getTag(),
                                    true)
                            .show();
                    return true;
                });
            }
        };
        rvSwatches.setAdapter(colorAdapter);

        chessboard = BitmapFactory.decodeResource(getResources(), R.mipmap.chessboard);

        fileToBeOpened = getIntent().getData();

        rbPencil.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        miLayerDirection = menu.findItem(R.id.i_layer_draw_below);
        miLayerLevelUp = menu.findItem(R.id.i_layer_level_up);
        smBlendModes = menu.findItem(R.id.i_blend_modes).getSubMenu();
        return true;
    }

    @Override
    protected void onDestroy() {

        canvas = null;
        bitmap.recycle();
        bitmap = null;

        if (bitmapOriginal != null) {
            bitmapOriginal.recycle();
            bitmapOriginal = null;
        }

        chessboard.recycle();
        chessboard = null;

        chessboardCanvas = null;
        chessboardBitmap.recycle();
        chessboardBitmap = null;

        if (clipboard != null) {
            clipboard.recycle();
            clipboard = null;
        }

        gridCanvas = null;
        gridBitmap.recycle();
        gridBitmap = null;

        if (preview != null) {
            preview.recycle();
            preview = null;
        }

        previewCanvas = null;
        previewBitmap.recycle();
        previewBitmap = null;

        rulerHCanvas = null;
        rulerHBitmap.recycle();
        rulerHBitmap = null;

        rulerVCanvas = null;
        rulerVBitmap.recycle();
        rulerVBitmap = null;

        selectionCanvas = null;
        selectionBitmap.recycle();
        selectionBitmap = null;

        if (transformer != null) {
            recycleTransformer();
        }

        viewCanvas = null;
        viewBitmap.recycle();
        viewBitmap = null;

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
                drawBitmapOnView();
                break;

            case R.id.i_cell_grid: {
                CellGridManager.make(this, tab.cellGrid,
                                onUpdateCellGridListener)
                        .show();
                break;
            }
            case R.id.i_clone:
                if (!hasSelection) {
                    break;
                }
                if (transformer == null) {
                    final Bitmap bm = Bitmap.createBitmap(bitmap,
                            selection.left, selection.top,
                            selection.width(), selection.height());
                    drawFloatingLayers();
                    transformer = new Transformer(bm, selection);
                    rbTransformer.setChecked(true);
                    drawBitmapOnView();
                    drawSelectionOnView();
                } else {
                    canvas.drawBitmap(transformer.getBitmap(),
                            selection.left, selection.top,
                            PAINT_SRC_OVER);
                    drawBitmapOnView();
                    addHistory();
                }
                break;

            case R.id.i_close:
            case R.id.i_layer_delete:
                if (tabs.size() == 1) {
                    break;
                }
                if (transformer != null) {
                    recycleTransformer();
                }
                closeTab();
                break;

            case R.id.i_copy:
                if (!hasSelection) {
                    break;
                }
                if (transformer == null) {
                    if (clipboard != null) {
                        clipboard.recycle();
                    }
                    clipboard = Bitmap.createBitmap(bitmap,
                            selection.left, selection.top,
                            selection.width(), selection.height());
                } else {
                    clipboard = Bitmap.createBitmap(transformer.getBitmap());
                }
                break;

            case R.id.i_copy_as_new: {
                if (!hasSelection) {
                    break;
                }
                Bitmap bm;
                if (transformer == null) {
                    bm = Bitmap.createBitmap(bitmap,
                            selection.left, selection.top,
                            selection.width(), selection.height());
                } else {
                    bm = Bitmap.createBitmap(transformer.getBitmap());
                }
                addBitmap(bm, tabLayout.getSelectedTabPosition() + 1);
                break;
            }
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
                drawBitmapOnView();
                addHistory();
                break;
            }
            case R.id.i_cut:
                if (!hasSelection) {
                    break;
                }
                if (transformer == null) {
                    if (clipboard != null) {
                        clipboard.recycle();
                    }
                    clipboard = Bitmap.createBitmap(bitmap, selection.left, selection.top,
                            selection.width(), selection.height());
                    canvas.drawRect(selection.left, selection.top, selection.right, selection.bottom + 1, eraser);
                    drawBitmapOnView();
                    addHistory();
                } else {
                    clipboard = Bitmap.createBitmap(transformer.getBitmap());
                    recycleTransformer();
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                }
                break;

            case R.id.i_delete:
                if (transformer == null) {
                    if (hasSelection) {
                        canvas.drawRect(selection.left, selection.top, selection.right, selection.bottom, eraser);
                    } else {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    }
                    drawBitmapOnView();
                    addHistory();
                } else {
                    recycleTransformer();
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                }
                break;

            case R.id.i_deselect:
                drawFloatingLayers();
                hasSelection = false;
                clearCanvasAndInvalidateView(selectionCanvas, ivSelection);
                tvStatus.setText("");
                break;

            case R.id.i_draw_color:
                drawFloatingLayers();
                canvas.drawColor(paint.getColor());
                drawBitmapOnView();
                addHistory();
                tvStatus.setText("");
                break;

            case R.id.i_filter_channels:
                drawFloatingLayers();
                createPreviewBitmap();
                new ChannelMixer(this)
                        .setOnCancelListener(onFilterCancelListener)
                        .setOnMatrixChangeListener(onColorMatrixChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .show();
                tvStatus.setText("");
                break;

            case R.id.i_filter_color_balance:
                drawFloatingLayers();
                createPreviewBitmap();
                new ColorBalanceDialog(this)
                        .setOnCancelListener(onFilterCancelListener)
                        .setOnMatrixChangeListener(onColorMatrixChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .show();
                tvStatus.setText("");
                break;

            case R.id.i_filter_contrast:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.contrast).setMin(-10).setMax(100).setProgress(10)
                        .setOnProgressChangeListener(onFilterContrastSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvStatus.setText("");
                break;

            case R.id.i_filter_hsv:
                drawFloatingLayers();
                createPreviewBitmap();
                new HSVDialog(this)
                        .setOnHSVChangeListener(onFilterHSVChangeListener, settings.getMultithreaded())
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvStatus.setText("");
                break;

            case R.id.i_filter_levels:
                drawFloatingLayers();
                createPreviewBitmap();
                new LevelsDialog(this)
                        .setOnLevelsChangeListener(onFilterLevelsChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show()
                        .updateImage(bitmap);
                tvStatus.setText("");
                break;

            case R.id.i_filter_lightness:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.lightness).setMin(-0xFF).setMax(0xFF).setProgress(0)
                        .setOnProgressChangeListener(onFilterLightnessSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvStatus.setText("");
                break;

            case R.id.i_filter_matrix:
                drawFloatingLayers();
                createPreviewBitmap();
                ColorMatrixManager
                        .make(this,
                                R.string.channel_mixer,
                                onColorMatrixChangeListener,
                                onFilterConfirmListener,
                                onFilterCancelListener)
                        .show();
                tvStatus.setText("");
                break;

            case R.id.i_filter_threshold:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.threshold).setMin(0).setMax(255).setProgress(128)
                        .setOnProgressChangeListener(onFilterThresholdSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                onFilterThresholdSeekBarProgressChangeListener.onProgressChanged(null, 128);
                tvStatus.setText("");
                break;

            case R.id.i_filter_saturation:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.saturation).setMin(0).setMax(100).setProgress(10)
                        .setOnProgressChangeListener(onFilterSaturationSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvStatus.setText("");
                break;

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
            case R.id.i_layer_add_mask: {
                drawFloatingLayers();
                tab.cbLayerVisible.setChecked(true);
                final Tab t = new Tab();
                t.bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
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
                        .setOnProgressChangeListener(onLayerAlphaSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener((dialog, which) -> tvStatus.setText(""))
                        .setOnCancelListener(dialog -> tvStatus.setText(""), false)
                        .show();
                tvStatus.setText("");
                break;

            case R.id.i_layer_color_filter:
                ColorMatrixManager.make(this,
                                R.string.color_filter,
                                onLayerColorFilterChangeListener,
                                null,
                                tab.colorMatrix)
                        .show();
                break;

            case R.id.i_layer_color_filter_enabled: {
                final boolean checked = !item.isChecked();
                item.setChecked(checked);
                tab.enableColorFilter = checked;
                drawBitmapOnView();
                break;
            }
            case R.id.i_layer_draw_below: {
                final boolean checked = !item.isChecked();
                item.setChecked(checked);
                tab.draw_below = checked;
                drawBitmapOnView();
                break;
            }
            case R.id.i_layer_duplicate: {
                drawFloatingLayers();
                Bitmap bm;
                if (hasSelection) {
                    bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    new Canvas(bm)
                            .drawBitmap(bitmap, selection, selection, PAINT_SRC);
                } else {
                    bm = Bitmap.createBitmap(bitmap);
                }
                addBitmap(bm, tab.visible, tabLayout.getSelectedTabPosition());
                break;
            }
            case R.id.i_layer_duplicate_by_hue: {
                drawFloatingLayers();
                createPreviewBitmap();
                new ColorRangeDialog(this)
                        .setOnColorRangeChangeListener(onColorRangeChangeListener)
                        .setOnPositiveButtonClickListener(onLayerDuplicateByColorRangeConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                break;
            }
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
                drawBitmapOnView();
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
                final Bitmap bm = mergeLayers(layerTree, w, h, (canvas, tab, paint) ->
                        canvas.drawBitmap(tab.bitmap, 0.0f, 0.0f, paint));
                addBitmap(bm, selected);
                break;
            }
            case R.id.i_layer_new:
                drawFloatingLayers();
                createLayer(bitmap.getWidth(), bitmap.getHeight(), tabLayout.getSelectedTabPosition());
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
                calculateLayerTree();
                drawBitmapOnView();
                showLayerLevel();
                miLayerLevelUp.setEnabled(true);
                break;

            case R.id.i_layer_level_up:
                if (tab.level <= 0) {
                    break;
                }
                --tab.level;
                calculateLayerTree();
                drawBitmapOnView();
                showLayerLevel();
                miLayerLevelUp.setEnabled(tab.level > 0);
                break;

            case R.id.i_new: {
                drawFloatingLayers();
                new NewGraphicPropertiesDialog(this)
                        .setOnFinishSettingListener(onFinishSettingNewGraphicPropertiesListener)
                        .show();
                break;
            }
            case R.id.i_open:
                drawFloatingLayers();

                getImage.launch("image/*");
                break;

            case R.id.i_open_from_clipboard: {
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
            case R.id.i_paste: {
                if (clipboard == null) {
                    break;
                }
                drawFloatingLayers();

                boolean si = !hasSelection; // Is selection invisible
                if (hasSelection) {
                    final Rect vp = getVisiblePart();
                    si = !(vp.left < selection.right && selection.left < vp.right
                            && vp.top < selection.bottom && selection.top < vp.bottom);
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
                drawBitmapOnView();
                drawSelectionOnView();
                break;
            }
            case R.id.i_redo:
                if (tab.history.canRedo()) {
                    undoOrRedo(tab.history.redo());
                }
                break;

            case R.id.i_refer_to_clipboard: {
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

            case R.id.i_save:
                save();
                break;

            case R.id.i_save_as:
                saveAs();
                break;

            case R.id.i_select_all:
                selectAll();
                hasSelection = true;
                drawSelectionOnView();
                selectionStartX = selection.left;
                selectionStartY = selection.top;
                selectionEndX = selection.right;
                selectionEndY = selection.bottom;
                tvStatus.setText("");
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
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
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
                drawAfterTranslatingOrScaling();
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
        if (bitmapOriginal != null) {
            bitmapOriginal.recycle();
            bitmapOriginal = null;
        }
        paint.setAntiAlias(antiAlias);
        setBlurRadius(blurRadius);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChange(boolean isChecked, View.OnTouchListener onImageViewTouchListener) {
        onToolChange(isChecked, onImageViewTouchListener, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChange(boolean isChecked, View.
            OnTouchListener onImageViewTouchListener, View toolOption) {
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
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
                    path = UriToPathUtil.getRealFilePath(this, uri);
                    break;
                case "image/png":
                    compressFormat = Bitmap.CompressFormat.PNG;
                    path = UriToPathUtil.getRealFilePath(this, uri);
                    break;
                case "image/gif":
                default:
                    Toast.makeText(this, R.string.not_supported_file_type, Toast.LENGTH_SHORT).show();
                    break;
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
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
        calculateLayerTree();

        if (transformer != null) {
            recycleTransformer();
        }
        hasSelection = false;

        drawChessboardOnView();
        drawGridOnView();
        drawSelectionOnView();

        tvStatus.setText("");
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
        drawBitmapOnView();
        addHistory();
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
        drawBitmapOnView();
        addHistory();
    }

    private static void scaleAlpha(Bitmap bitmap) {
        final int w = bitmap.getWidth(), h = bitmap.getHeight(), area = w * h;
        final int[] pixels = new int[area];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < area; ++i) {
            if (Color.alpha(pixels[i]) > 0x00) {
                pixels[i] |= 0xFF000000;
            }
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private void selectAll() {
        selection.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    private void setBlurRadius(float f) {
        paint.setMaskFilter(f > 0.0f ? new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL) : null);
    }

    private void showLayerLevel() {
        final String ra = getString(R.string.rightwards_arrow);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tab.level; ++i) {
            sb.append(ra);
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

    private int toUnscaled(float scaled) {
        return (int) (scaled / scale);
    }

    private float toScaled(int unscaled) {
        return unscaled * scale;
    }

    private float toScaled(float unscaled) {
        return unscaled * scale;
    }

    private void undoOrRedo(Bitmap bm) {
        optimizeSelection();
        bitmap.recycle();
        bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        tab.bitmap = bitmap;
        canvas = new Canvas(bitmap);
        canvas.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC);

        imageWidth = (int) toScaled(bitmap.getWidth());
        imageHeight = (int) toScaled(bitmap.getHeight());

        if (transformer != null) {
            recycleTransformer();
        }
        clearCanvasAndInvalidateView(previewCanvas, ivPreview);

        optimizeSelection();
        isShapeStopped = true;
        hasDragged = false;

//        calculateStackingOrder();
        calculateLayerTree();

        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
        drawSelectionOnView();

        if (cloneStampSrc != null) {
            drawCloneStampSrcOnView(cloneStampSrc.x, cloneStampSrc.y);
        }

        tvStatus.setText("");
    }
}