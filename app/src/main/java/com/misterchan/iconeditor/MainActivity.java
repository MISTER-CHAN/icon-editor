package com.misterchan.iconeditor;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private enum Position {
        LEFT("Left"),
        TOP("Top"),
        RIGHT("Right"),
        BOTTOM("Bottom"),
        NULL;

        private String name;

        Position() {
        }

        Position(String name) {
            this.name = name;
        }
    }

    private static final ColorMatrix COLOR_MATRIX_BLACK = new ColorMatrix(new float[]{
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
    });

    private static final ColorMatrix COLOR_MATRIX_INVERT = new ColorMatrix(new float[]{
            -1.0f, 0.0f, 0.0f, 0.0f, 0xFF,
            0.0f, -1.0f, 0.0f, 0.0f, 0xFF,
            0.0f, 0.0f, -1.0f, 0.0f, 0xFF,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    });

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

    private static final InputFilter[] FILE_NAME_FILTERS = new InputFilter[]{
            (source, sourceStart, sourceEnd, dest, destStart, destEnd) -> {
                Matcher matcher = PATTERN_FILE_NAME.matcher(source.toString());
                if (matcher.find()) {
                    return "";
                }
                return null;
            }
    };

    private static final Paint PAINT = new Paint();

    private static final Paint PAINT_BLACK = new Paint() {
        {
            setColor(Color.BLACK);
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

    private static final Paint PAINT_OPAQUE = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
            setColor(Color.BLACK);
        }
    };

    private static final Paint PAINT_POINT = new Paint() {
        {
            setColor(Color.RED);
            setStrokeWidth(4.0f);
            setTextSize(32.0f);
        }
    };

    private static final Paint PAINT_SRC_IN = new Paint() {
        {
            setBlendMode(BlendMode.SRC_IN);
        }
    };

    private AppCompatSpinner sColorReplacerBlendMode;
    private Bitmap bitmap;
    private Bitmap bitmapWithoutFilter;
    private Bitmap chessboard;
    private Bitmap chessboardBitmap;
    private Bitmap clipboard;
    private Bitmap gridBitmap;
    private Bitmap previewBitmap;
    private Bitmap rulerHBitmap, rulerVBitmap;
    private Bitmap selectionBitmap;
    private Bitmap viewBitmap;
    private BitmapHistory history;
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
    private CellGrid cellGrid;
    private CheckBox cbBucketFillContiguous;
    private CheckBox cbCloneStampAntiAlias;
    private CheckBox cbColorReplacerAntiAlias;
    private CheckBox cbFilterAntiAlias;
    private CheckBox cbFilterClear;
    private CheckBox cbGradientAntiAlias;
    private CheckBox cbPencilAntiAlias;
    private CheckBox cbShapeAntiAlias;
    private CheckBox cbShapeFill;
    private CheckBox cbTextFill;
    private CheckBox cbTransformerLar;
    private CheckBox cbZoom;
    private ColorAdapter colorAdapter;
    private double prevDiagonal;
    private EditText etCloneStampBlurRadius;
    private EditText etCloneStampStrokeWidth;
    private EditText etColorReplacerBlurRadius;
    private EditText etColorReplacerStrokeWidth;
    private EditText etEraserBlurRadius;
    private EditText etEraserStrokeWidth;
    private EditText etFileName;
    private EditText etFilterBlurRadius;
    private EditText etFilterStrokeWidth;
    private EditText etGradientBlurRadius;
    private EditText etGradientStrokeWidth;
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
    private HorizontalScrollView hsvOptionsColorReplacer;
    private HorizontalScrollView hsvOptionsFilter;
    private ImageView imageView;
    private ImageView ivChessboard;
    private ImageView ivGrid;
    private ImageView ivPreview;
    private ImageView ivRulerH, ivRulerV;
    private ImageView ivSelection;
    private InputMethodManager inputMethodManager;
    //    private int colorRange = 0b111111;
    @ColorInt
    private int gradientColor0;
    private int imageWidth, imageHeight;
    private int selectionStartX, selectionStartY;
    private int selectionEndX, selectionEndY;
    private int shapeStartX, shapeStartY;
    private int textX, textY;
    private int threshold;
    private int viewWidth, viewHeight;
    private LinearLayout llOptionsEraser;
    private LinearLayout llOptionsGradient;
    private LinearLayout llOptionsPencil;
    private LinearLayout llOptionsShape;
    private LinearLayout llOptionsText;
    private LinearLayout llOptionsTransformer;
    private LinkedList<Integer> palette;
    private List<Tab> tabs = new ArrayList<>();
    private MenuItem miLayerVisible;
    private Point cloneStampSrc;
    private final Point cloneStampSrcDist = new Point(0, 0); // Dist. - Distance
    private Position draggingBound = Position.NULL;
    private RadioButton rbBucketFill;
    private RadioButton rbCloneStamp;
    private RadioButton rbColorReplacer;
    private RadioButton rbFilter;
    private RadioButton rbTransformer;
    private Rect selection = new Rect();
    private RectF transfromeeDpb = new RectF(); // DPB - Distance from point to bounds
    private RecyclerView rvSwatches;
    private Settings settings;
    private Spinner sFileType;
    private String tree = "";
    private SubMenu smBlendModes;
    private Tab tab;
    private TabLayout tabLayout;
    private TextView tvState;
    private Transformer transformer;
    private Uri fileToBeOpened;
    private View vBackgroundColor;
    private View vForegroundColor;

    @Size(value = 20)
    private float[] colorMatrix = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    private final Paint cellGridPaint = new Paint() {
        {
            setColor(Color.RED);
            setStrokeWidth(2.0f);
        }
    };

    private final Paint colorPaint = new Paint() {
        {
            setBlendMode(BlendMode.SRC);
        }
    };

    private final Paint colorReplacer = new Paint();

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

    private final Paint filter = new Paint() {
        {
            setBlendMode(BlendMode.SRC_IN);
        }
    };

    private final Paint gridPaint = new Paint() {
        {
            setColor(Color.GRAY);
        }
    };

    private final Paint imageBound = new Paint() {
        {
            setColor(Color.DKGRAY);
        }
    };

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
        String fileName = etFileName.getText().toString();
        if ("".equals(fileName)) {
            return;
        }
        fileName += sFileType.getSelectedItem().toString();
        tab.path = Environment.getExternalStorageDirectory().getPath() + File.separator + tree + File.separator + fileName;
        tab.compressFormat = COMPRESS_FORMATS[sFileType.getSelectedItemPosition()];
        save(tab.path);
        tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).setText(fileName);
    };

    private final ActivityResultCallback<Uri> imageCallback = this::openFile;

    private final ActivityResultCallback<Uri> treeCallback = result -> {
        if (result == null) {
            return;
        }
        Matcher matcher = PATTERN_TREE.matcher(result.toString());
        if (!matcher.find()) {
            return;
        }
        tree = matcher.group("path").replace("%2F", "/");
        AlertDialog fileNameDialog = new AlertDialog.Builder(this)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, onFileNameDialogPosButtonClickListener)
                .setTitle(R.string.file_name)
                .setView(R.layout.file_name)
                .show();

        etFileName = fileNameDialog.findViewById(R.id.et_file_name);
        sFileType = fileNameDialog.findViewById(R.id.s_file_type);

        etFileName.setFilters(FILE_NAME_FILTERS);
    };

    private final ActivityResultLauncher<String> getImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), imageCallback);

    private final ActivityResultLauncher<Uri> getTree =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), treeCallback);

    private final AfterTextChangedListener onBlurRadiusTextChangedListener = s -> {
        try {
            float f = Float.parseFloat(s);
            blurRadius = f;
            setBlurRadius(f);
        } catch (NumberFormatException e) {
        }
    };

    private final AfterTextChangedListener onStrokeWidthTextChangedListener = s -> {
        try {
            float f = Float.parseFloat(s);
            strokeWidth = f;
            paint.setStrokeWidth(f);
        } catch (NumberFormatException e) {
        }
    };

    private final AfterTextChangedListener onTextSizeChangedListener = s -> {
        try {
            float f = Float.parseFloat(etTextSize.getText().toString());
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
        tvState.setText("");
    };

    private final DialogInterface.OnClickListener onFilterConfirmListener = (dialog, which) -> {
        drawPreviewBitmapOnCanvas();
        tvState.setText("");
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

    private final ColorRangeDialog.OnColorRangeChangeListener onColorRangeChangeListener = (min, max) -> {
        if (min == 0.0f && max == 360.0f) {
            preview.clearFilter();
        } else {
            final int width = preview.getWidth(), height = preview.getHeight(), area = width * height;
            final int[] pixels = new int[area];
            preview.getPixels(pixels, 0, width, 0, 0, width, height);
            for (int i = 0; i < area; ++i) {
                float h = hue(pixels[i]);
                if (!(min <= max && (min <= h && h <= max) || min > max && (min <= h || h <= max))) {
                    pixels[i] = Color.TRANSPARENT;
                }
            }
            preview.setPixels(pixels, 0, width, 0, 0, width, height);
        }
        drawBitmapOnView(preview.getBitmap());
        tvState.setText(String.format(getString(R.string.state_min_max), min, max));
    };

    private final ColorRangeDialog.OnColorRangeChangeListener onLayerDuplicateByHueConfirmListener = (min, max) -> {
        Bitmap bm = Bitmap.createBitmap(preview.getBitmap());
        preview.recycle();
        preview = null;
        addBitmap(bm, tabLayout.getSelectedTabPosition());
        tab.visible = true;
        tvState.setText("");
    };

    private final HiddenImageMaker.OnFinishSettingListener onFinishMakingHiddenImageListener = bm -> {
        createGraphic(bm.getWidth(), bm.getHeight(), tabLayout.getSelectedTabPosition() + 2);
        canvas.drawBitmap(bm, 0.0f, 0.0f, PAINT_OPAQUE);
        drawBitmapOnView();
        bm.recycle();
    };

    private final NewGraphicPropertiesDialog.OnFinishSettingListener onFinishSettingNewGraphicPropertiesListener = this::createGraphic;

    private final OnItemSelectedListener onColorReplacerBlendModeSelectedListener = (parent, view, position, id) -> {
        BlendMode blendMode;
        if (1 <= position && position <= 4) {
            blendMode = BlendMode.values()[position + 24];
        } else if (position == 5) {
            blendMode = BlendMode.DST_OUT;
        } else {
            blendMode = null;
        }
        colorReplacer.setBlendMode(blendMode);
    };

    private final ColorMatrixManager.OnMatrixElementsChangeListener onColorMatrixChangeListener = matrix -> {
        preview.setFilter(matrix);
        drawBitmapOnView(preview.getBitmap());
    };

    private final OnProgressChangeListener onThresholdChangeListener = progress -> {
        threshold = progress;
        if (progress == 0x100) {
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
        tvState.setText(String.format(getString(R.string.state_threshold), progress));
    };

    private final ColorMatrixManager.OnMatrixElementsChangeListener onPaintFilterChangeListener = matrix -> {
        colorMatrix = matrix;
        filter.setColorFilter(new ColorMatrixColorFilter(matrix));
    };

    private final View.OnClickListener onFilterButtonClickListener = v -> {
        ColorMatrixManager.make(this,
                        R.string.filter,
                        onPaintFilterChangeListener,
                        null,
                        colorMatrix)
                .show();
    };

    private final DialogInterface.OnCancelListener onThresholdCancelListener = dialog -> {
        drawBitmapOnView();
        preview.recycle();
        preview = null;
        tvState.setText("");
    };

    private final DialogInterface.OnClickListener onThresholdConfirmListener = (dialog, which) -> {
        onThresholdCancelListener.onCancel(dialog);
    };

    private final View.OnClickListener onThresholdButtonClickListener = v -> {
        createPreviewBitmap();
        new SeekBarDialog(this).setTitle(R.string.threshold).setMin(0x0).setMax(0x100)
                .setOnCancelListener(onThresholdCancelListener, false)
                .setOnPositiveButtonClickListener(onThresholdConfirmListener)
                .setOnProgressChangeListener(onThresholdChangeListener)
                .setProgress(threshold)
                .show();
        onThresholdChangeListener.onProgressChanged(threshold);
    };

    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            drawFloatingLayers();

            MainActivity.this.tab = tabs.get(tab.getPosition());
            bitmap = MainActivity.this.tab.bitmap;
            canvas = new Canvas(bitmap);
            history = MainActivity.this.tab.history;
            cellGrid = MainActivity.this.tab.cellGrid;

            if (settings.getUnifiedTranslAndScale()) {
                MainActivity.this.tab.translationX = translationX;
                MainActivity.this.tab.translationY = translationY;
                MainActivity.this.tab.scale = scale;
            }

            int width = bitmap.getWidth(), height = bitmap.getHeight();
            imageWidth = (int) toScaled(width);
            imageHeight = (int) toScaled(height);

            if (transformer != null) {
                transformer.recycle();
                transformer = null;
            }
            hasSelection = false;

            if (rbCloneStamp.isChecked()) {
                cloneStampSrc = null;
            }

            miLayerVisible.setChecked(MainActivity.this.tab.visible);
            for (int i = 0; i <= 29; ++i) {
                MenuItem mi = smBlendModes.getItem(i);
                BlendMode blendMode = MainActivity.this.tab.paint.getBlendMode();
                mi.setChecked(i == 0 && blendMode == null
                        || i > 0 && blendMode == BlendMode.values()[i - 1]);
            }

            drawChessboardOnView();
            drawBitmapOnView();
            drawGridOnView();
            drawSelectionOnView();
            clearCanvasAndInvalidateView(previewCanvas, ivPreview);

            tvState.setText(String.format(getString(R.string.state_size), width, height));
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            if (settings.getUnifiedTranslAndScale()) {
                int i = tab.getPosition();
                if (i >= 0) {
                    Tab t = tabs.get(i);
                    translationX = t.translationX;
                    translationY = t.translationY;
                    scale = t.scale;
                }
            }
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithBucketListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                int unscaledX = toUnscaled(x - tab.translationX), unscaledY = toUnscaled(y - tab.translationY);
                if (!(0 <= unscaledX && unscaledX < bitmap.getWidth() && 0 <= unscaledY && unscaledY < bitmap.getHeight())) {
                    break;
                }
                if (cbBucketFillContiguous.isChecked()) {
                    floodFill(bitmap, bitmap, unscaledX, unscaledY, paint.getColor(),
                            threshold);
                } else {
                    bucketFill(bitmap, unscaledX, unscaledY, paint.getColor(),
                            threshold);
                }
                drawBitmapOnView();
                history.offer(bitmap);
                tvState.setText("");
                break;
            }
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onImageViewTouchWithCloneStampListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        int unscaledX = toUnscaled(x - tab.translationX), unscaledY = toUnscaled(y - tab.translationY);
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
                int unscaledPrevX = toUnscaled(prevX - tab.translationX),
                        unscaledPrevY = toUnscaled(prevY - tab.translationY);

                int width = (int) (Math.abs(unscaledX - unscaledPrevX) + strokeWidth + blurRadius * 2.0f),
                        height = (int) (Math.abs(unscaledY - unscaledPrevY) + strokeWidth + blurRadius * 2.0f);
                Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                float rad = strokeWidth / 2.0f + blurRadius;
                float left = Math.min(unscaledPrevX, unscaledX) - rad, top = Math.min(unscaledPrevY, unscaledY) - rad;
                int l = (int) (left + cloneStampSrcDist.x), t = (int) (top + cloneStampSrcDist.y);
                Canvas cv = new Canvas(bm);
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
                canvas.drawBitmap(bm, left, top, PAINT);
                bm.recycle();
                drawBitmapOnView();
                drawCloneStampSrcOnView(unscaledX + cloneStampSrcDist.x, unscaledY + cloneStampSrcDist.y);
                tvState.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));

                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (cloneStampSrc == null) {
                    cloneStampSrc = new Point(unscaledX, unscaledY);
                    drawCloneStampSrcOnView(unscaledX, unscaledY);
                    tvState.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                } else {
                    drawCloneStampSrcOnView(cloneStampSrc.x, cloneStampSrc.y);
                    history.offer(bitmap);
                    tvState.setText("");
                }
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithColorReplacerListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                prevX = x;
                prevY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                int unscaledX = toUnscaled(x - tab.translationX), unscaledY = toUnscaled(y - tab.translationY);
                int unscaledPrevX = toUnscaled(prevX - tab.translationX), unscaledPrevY = toUnscaled(prevY - tab.translationY);

                int rad = (int) (strokeWidth / 2.0f + blurRadius);
                int left = Math.min(unscaledPrevX, unscaledX) - rad,
                        top = Math.min(unscaledPrevY, unscaledY) - rad,
                        right = Math.max(unscaledPrevX, unscaledX) + rad + 1,
                        bottom = Math.max(unscaledPrevY, unscaledY) + rad + 1;
                int width = right - left + 1, height = bottom - top + 1;
                int relativeX = unscaledX - left, relativeY = unscaledY - top;
                Rect absolute = new Rect(left, top, right + 1, bottom + 1),
                        relative = new Rect(0, 0, width, height);
                Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas cLine = new Canvas(bLine);
                cLine.drawLine(unscaledPrevX - left, unscaledPrevY - top,
                        relativeX, relativeY,
                        paint);
                if (threshold < 0x100) {
                    Bitmap bm = Bitmap.createBitmap(bLine);
                    new Canvas(bm).drawBitmap(bitmapWithoutFilter, absolute, relative, PAINT_SRC_IN);
                    Bitmap bThr = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444); // Threshold
                    floodFill(bm, bThr, relativeX, relativeY, Color.BLACK, threshold);
                    bm.recycle();
                    cLine.drawBitmap(bThr, 0.0f, 0.0f, PAINT_DST_IN);
                    bThr.recycle();
                }
                canvas.drawBitmap(bLine, left, top, colorReplacer);
                bLine.recycle();

                drawBitmapOnView();
                tvState.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                history.offer(bitmap);
                tvState.setText("");
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithEraserListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                prevX = x;
                prevY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                int unscaledX = toUnscaled(x - tab.translationX), unscaledY = toUnscaled(y - tab.translationY);
                drawLineOnCanvas(
                        toUnscaled(prevX - tab.translationX),
                        toUnscaled(prevY - tab.translationY),
                        unscaledX,
                        unscaledY,
                        eraser);
                drawBitmapOnView();
                tvState.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                history.offer(bitmap);
                tvState.setText("");
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithEyedropperListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                int unscaledX = inRange(toUnscaled(x - tab.translationX), 0, bitmap.getWidth() - 1),
                        unscaledY = inRange(toUnscaled(y - tab.translationY), 0, bitmap.getHeight() - 1);
                int color = bitmap.getPixel(unscaledX, unscaledY);
                paint.setColor(color);
                vForegroundColor.setBackgroundColor(color);
                tvState.setText(String.format(
                        String.format(getString(R.string.state_eyedropper), settings.getArgbChannelsFormat()),
                        unscaledX, unscaledY,
                        Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color)));
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                tvState.setText("");
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithFilterListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                prevX = x;
                prevY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                int unscaledX = toUnscaled(x - tab.translationX), unscaledY = toUnscaled(y - tab.translationY);
                int unscaledPrevX = toUnscaled(prevX - tab.translationX), unscaledPrevY = toUnscaled(prevY - tab.translationY);

                int rad = (int) (strokeWidth / 2.0f + blurRadius);
                int left = Math.min(unscaledPrevX, unscaledX) - rad,
                        top = Math.min(unscaledPrevY, unscaledY) - rad,
                        right = Math.max(unscaledPrevX, unscaledX) + rad + 1,
                        bottom = Math.max(unscaledPrevY, unscaledY) + rad + 1;
                int width = right - left + 1, height = bottom - top + 1;
                int relativeX = unscaledX - left, relativeY = unscaledY - top;
                Rect absolute = new Rect(left, top, right + 1, bottom + 1),
                        relative = new Rect(0, 0, width, height);
                Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas cLine = new Canvas(bLine);
                cLine.drawLine(unscaledPrevX - left, unscaledPrevY - top,
                        relativeX, relativeY,
                        paint);
                if (threshold < 0x100) {
                    Bitmap bm = Bitmap.createBitmap(bLine);
                    new Canvas(bm).drawBitmap(bitmapWithoutFilter, absolute, relative, PAINT_SRC_IN);
                    Bitmap bThr = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444); // Threshold
                    floodFill(bm, bThr, relativeX, relativeY, Color.BLACK, threshold);
                    bm.recycle();
                    cLine.drawBitmap(bThr, 0.0f, 0.0f, PAINT_DST_IN);
                    bThr.recycle();
                }
                if (cbFilterClear.isChecked()) {
                    canvas.drawBitmap(bLine, left, top, PAINT_DST_OUT);
                }
                cLine.drawBitmap(bitmapWithoutFilter, absolute, relative, filter);
                canvas.drawBitmap(bLine, left, top, PAINT);
                bLine.recycle();

                drawBitmapOnView();
                tvState.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                history.offer(bitmap);
                tvState.setText("");
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onImageViewTouchWithGradientListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        int unscaledX = toUnscaled(x - tab.translationX), unscaledY = toUnscaled(y - tab.translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                paint.setStrokeWidth(toScaled((int) paint.getStrokeWidth()));
                if (isShapeStopped) {
                    isShapeStopped = false;
                    drawPointOnView(unscaledX, unscaledY);
                    shapeStartX = unscaledX;
                    shapeStartY = unscaledY;
                    gradientColor0 =
                            bitmap.getPixel(inRange(unscaledX, 0, bitmap.getWidth() - 1),
                                    inRange(unscaledY, 0, bitmap.getHeight() - 1));
                    tvState.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                    break;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                float startX = tab.translationX + toScaled(shapeStartX + 0.5f),
                        startY = tab.translationY + toScaled(shapeStartY + 0.5f),
                        stopX = tab.translationX + toScaled(unscaledX + 0.5f),
                        stopY = tab.translationY + toScaled(unscaledY + 0.5f);
                paint.setShader(new LinearGradient(startX, startY, stopX, stopY,
                        gradientColor0,
                        bitmap.getPixel(inRange(unscaledX, 0, bitmap.getWidth() - 1),
                                inRange(unscaledY, 0, bitmap.getHeight() - 1)),
                        Shader.TileMode.CLAMP));
                clearCanvas(previewCanvas);
                previewCanvas.drawLine(startX, startY, stopX, stopY, paint);
                ivPreview.invalidate();
                tvState.setText(String.format(getString(R.string.state_start_stop),
                        shapeStartX, shapeStartY, unscaledX, unscaledY));
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                paint.setStrokeWidth(strokeWidth);
                if (unscaledX != shapeStartX || unscaledY != shapeStartY) {
                    paint.setShader(new LinearGradient(shapeStartX, shapeStartY, unscaledX, unscaledY,
                            gradientColor0,
                            bitmap.getPixel(inRange(unscaledX, 0, bitmap.getWidth() - 1),
                                    inRange(unscaledY, 0, bitmap.getHeight() - 1)),
                            Shader.TileMode.CLAMP));
                    drawLineOnCanvas(shapeStartX, shapeStartY, unscaledX, unscaledY, paint);
                    isShapeStopped = true;
                    drawBitmapOnView();
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                    history.offer(bitmap);
                    tvState.setText("");
                }
                paint.setShader(null);
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithPencilListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                prevX = x;
                prevY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                int unscaledX = toUnscaled(x - tab.translationX), unscaledY = toUnscaled(y - tab.translationY);
                drawLineOnCanvas(
                        toUnscaled(prevX - tab.translationX),
                        toUnscaled(prevY - tab.translationY),
                        unscaledX,
                        unscaledY,
                        paint);
                drawBitmapOnView();
                tvState.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                history.offer(bitmap);
                tvState.setText("");
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithSelectorListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                if (draggingBound == Position.NULL) {
                    if (hasSelection && checkDraggingBound(x, y) != Position.NULL) {
                        tvState.setText(String.format(getString(R.string.state_selected_bound),
                                draggingBound.name));
                    } else {
                        if (hasSelection && selectionStartX == selectionEndX && selectionStartY == selectionEndY) {
                            selectionEndX = toUnscaled(x - tab.translationX);
                            selectionEndY = toUnscaled(y - tab.translationY);
                        } else {
                            hasSelection = true;
                            selectionStartX = toUnscaled(x - tab.translationX);
                            selectionStartY = toUnscaled(y - tab.translationY);
                            selectionEndX = selectionStartX;
                            selectionEndY = selectionStartY;
                        }
                        drawSelectionOnViewByStartsAndEnds();
                        tvState.setText(String.format(getString(R.string.state_start_end_size_1),
                                selectionStartX, selectionStartY, selectionStartX, selectionStartY));
                    }
                } else {
                    dragBound(x, y);
                    drawSelectionOnView();
                    tvState.setText(String.format(getString(R.string.state_l_t_r_b_size),
                            selection.left, selection.top, selection.right, selection.bottom,
                            selection.width() + 1, selection.height() + 1));
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                if (draggingBound == Position.NULL) {
                    selectionEndX = toUnscaled(x - tab.translationX);
                    selectionEndY = toUnscaled(y - tab.translationY);
                    drawSelectionOnViewByStartsAndEnds();
                    tvState.setText(String.format(getString(R.string.state_start_end_size),
                            selectionStartX, selectionStartY, selectionEndX, selectionEndY,
                            Math.abs(selectionEndX - selectionStartX) + 1, Math.abs(selectionEndY - selectionStartY) + 1));
                } else {
                    dragBound(x, y);
                    drawSelectionOnView();
                    tvState.setText(String.format(getString(R.string.state_l_t_r_b_size),
                            selection.left, selection.top, selection.right, selection.bottom,
                            selection.width() + 1, selection.height() + 1));
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
                        tvState.setText(hasSelection ?
                                String.format(getString(R.string.state_l_t_r_b_size),
                                        selection.left, selection.top, selection.right, selection.bottom,
                                        selection.width() + 1, selection.height() + 1) :
                                "");
                    }
                } else {
                    tvState.setText(hasSelection ?
                            String.format(getString(R.string.state_l_t_r_b_size),
                                    selection.left, selection.top, selection.right, selection.bottom,
                                    selection.width() + 1, selection.height() + 1) :
                            "");
                }
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onImageViewTouchWithShapeListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        int unscaledX = toUnscaled(x - tab.translationX), unscaledY = toUnscaled(y - tab.translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                paint.setStrokeWidth(toScaled((int) paint.getStrokeWidth()));
                if (isShapeStopped) {
                    isShapeStopped = false;
                    drawPointOnView(unscaledX, unscaledY);
                    shapeStartX = unscaledX;
                    shapeStartY = unscaledY;
                    tvState.setText(String.format(getString(R.string.coordinate), unscaledX, unscaledY));
                    break;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                String result = drawShapeOnView(shapeStartX, shapeStartY, unscaledX, unscaledY);
                tvState.setText(
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
                    history.offer(bitmap);
                    tvState.setText("");
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
                    float x = event.getX(), y = event.getY();
                    textX = toUnscaled(x - prevX);
                    textY = toUnscaled(y - prevY);
                    drawTextOnView();
                    break;
                }
            }

        } else {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    textX = toUnscaled(event.getX() - tab.translationX);
                    textY = toUnscaled(event.getY() - tab.translationY);
                    llOptionsText.setVisibility(View.VISIBLE);
                    isEditingText = true;
                    drawTextOnView();
                    prevX = tab.translationX;
                    prevY = tab.translationY;
                    break;
            }
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithTransformerListener = (v, event) -> {
        if (!hasSelection) {
            return true;
        }

        switch (event.getPointerCount()) {

            case 1:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        float x = event.getX(), y = event.getY();
                        int width = selection.width() + 1, height = selection.height() + 1;
                        if (width > 0 && height > 0) {
                            if (transformer == null) {
                                transformer = new Transformer(
                                        Bitmap.createBitmap(bitmap, selection.left, selection.top, width, height),
                                        tab.translationX + toScaled(selection.left),
                                        tab.translationY + toScaled(selection.top));
                                canvas.drawRect(selection.left, selection.top, selection.right + 1, selection.bottom + 1, eraser);
                            }
                            drawBitmapOnView();
                            drawTransformeeAndSelectionOnViewByTranslation(false);
                            if (draggingBound == Position.NULL) {
                                if (checkDraggingBound(x, y) != Position.NULL) {
                                    if (cbTransformerLar.isChecked()) {
                                        transformer.calculateByLocation(selection);
                                    }
                                    tvState.setText(String.format(getString(R.string.state_selected_bound),
                                            draggingBound.name));
                                } else {
                                    tvState.setText(String.format(getString(R.string.state_left_top),
                                            selection.left, selection.top));
                                }
                            } else {
                                stretchByBound(x, y);
                                tvState.setText(String.format(getString(R.string.state_left_top),
                                        selection.left, selection.top));
                            }
                        }
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        if (transformer == null) {
                            break;
                        }
                        float x = event.getX(), y = event.getY();
                        if (draggingBound == Position.NULL) {
                            transformer.translateBy(x - prevX, y - prevY);
                            drawTransformeeAndSelectionOnViewByTranslation(true);
                            tvState.setText(String.format(getString(R.string.state_left_top),
                                    selection.left, selection.top));
                        } else {
                            stretchByBound(x, y);
                            tvState.setText(String.format(getString(R.string.state_size),
                                    selection.width() + 1, selection.height() + 1));
                        }
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (draggingBound != Position.NULL) {
                            if (hasDragged) {
                                draggingBound = Position.NULL;
                                isDraggingCorner = false;
                                hasDragged = false;
                                int width = selection.width() + 1, height = selection.height() + 1;
                                if (width > 0 && height > 0) {
                                    transformer.stretch(width, height,
                                            tab.translationX + toScaled(selection.left),
                                            tab.translationY + toScaled(selection.top));
                                } else if (transformer != null) {
                                    transformer.recycle();
                                    transformer = null;
                                }
                                drawTransformeeAndSelectionOnViewByTranslation(false);
                                tvState.setText("");
                            }
                        } else {
                            drawSelectionOnView(false);
                        }
                        break;
                }
                break;

            case 2:
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE: {
                        float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        RectF scaledSelection = new RectF(
                                tab.translationX + toScaled(selection.left),
                                tab.translationY + toScaled(selection.top),
                                tab.translationX + toScaled(selection.right),
                                tab.translationY + toScaled(selection.bottom));
                        RectF dpb = new RectF(
                                Math.min(x0 - scaledSelection.left, x1 - scaledSelection.left),
                                Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top),
                                Math.min(scaledSelection.right - x0, scaledSelection.right - x1),
                                Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1));
                        if (cbTransformerLar.isChecked()) {
                            RectF dpbDiff = new RectF();
                            dpbDiff.left = transfromeeDpb.left - dpb.left;
                            dpbDiff.top = transfromeeDpb.top - dpb.top;
                            dpbDiff.right = transfromeeDpb.right - dpb.right;
                            dpbDiff.bottom = transfromeeDpb.bottom - dpb.bottom;
                            if (Math.abs(dpbDiff.left) + Math.abs(dpbDiff.right) >= Math.abs(dpbDiff.top) + Math.abs(dpbDiff.bottom)) {
                                selection.left -= toUnscaled(transfromeeDpb.left - dpb.left);
                                selection.right += toUnscaled(transfromeeDpb.right - dpb.right);
                                double width = selection.width() + 1, height = width / transformer.getAspectRatio();
                                selection.top = (int) (transformer.getCenterY() - height / 2.0);
                                selection.bottom = (int) (transformer.getCenterY() + height / 2.0);
                                scaledSelection.top = tab.translationY + toScaled(selection.top);
                                scaledSelection.bottom = tab.translationY + toScaled(selection.bottom);
                                transfromeeDpb.top = Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top);
                                transfromeeDpb.bottom = Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1);
                            } else {
                                selection.top -= toUnscaled(transfromeeDpb.top - dpb.top);
                                selection.bottom += toUnscaled(transfromeeDpb.bottom - dpb.bottom);
                                double height = selection.height() + 1, width = height * transformer.getAspectRatio();
                                selection.left = (int) (transformer.getCenterX() - width / 2.0);
                                selection.right = (int) (transformer.getCenterX() + width / 2.0);
                                scaledSelection.left = tab.translationX + toScaled(selection.left);
                                scaledSelection.right = tab.translationX + toScaled(selection.right);
                                transfromeeDpb.left = Math.min(x0 - scaledSelection.left, x1 - scaledSelection.left);
                                transfromeeDpb.right = Math.min(scaledSelection.right - x0, scaledSelection.right - x1);
                            }
                        } else {
                            selection.left -= toUnscaled(transfromeeDpb.left - dpb.left);
                            selection.top -= toUnscaled(transfromeeDpb.top - dpb.top);
                            selection.right += toUnscaled(transfromeeDpb.right - dpb.right);
                            selection.bottom += toUnscaled(transfromeeDpb.bottom - dpb.bottom);
                        }
                        drawSelectionOnView();
                        tvState.setText(String.format(getString(R.string.state_size),
                                selection.width() + 1, selection.height() + 1));
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_DOWN: {
                        draggingBound = Position.NULL;
                        isDraggingCorner = false;
                        float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        RectF scaledSelection = new RectF();
                        scaledSelection.left = tab.translationX + toScaled(selection.left);
                        scaledSelection.top = tab.translationY + toScaled(selection.top);
                        scaledSelection.right = tab.translationX + toScaled(selection.right);
                        scaledSelection.bottom = tab.translationY + toScaled(selection.bottom);
                        transfromeeDpb.left = Math.min(x0 - scaledSelection.left, x1 - scaledSelection.left);
                        transfromeeDpb.top = Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top);
                        transfromeeDpb.right = Math.min(scaledSelection.right - x0, scaledSelection.right - x1);
                        transfromeeDpb.bottom = Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1);
                        if (cbTransformerLar.isChecked()) {
                            transformer.calculateByLocation(selection);
                        }
                        tvState.setText(String.format(getString(R.string.state_size),
                                selection.width() + 1, selection.height() + 1));
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        int width = selection.width() + 1, height = selection.height() + 1;
                        if (width > 0 && height > 0) {
                            transformer.stretch(width, height,
                                    tab.translationX + toScaled(selection.left),
                                    tab.translationY + toScaled(selection.top));
                        } else if (transformer != null) {
                            transformer.recycle();
                            transformer = null;
                        }
                        drawTransformeeAndSelectionOnViewByTranslation();
                        prevX = event.getX(1 - event.getActionIndex());
                        prevY = event.getY(1 - event.getActionIndex());
                        tvState.setText("");
                        break;
                    }
                }
                break;
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onImageViewTouchWithZoomToolListener = (v, event) -> {
        switch (event.getPointerCount()) {

            case 1: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        float x = event.getX(), y = event.getY();
                        tvState.setText(String.format(getString(R.string.coordinate),
                                toUnscaled(x - tab.translationX), toUnscaled(y - tab.translationY)));
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float x = event.getX(), y = event.getY();
                        float deltaX = x - prevX, deltaY = y - prevY;
                        tab.translationX += deltaX;
                        tab.translationY += deltaY;
                        drawAfterTranslatingOrScaling();
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        tvState.setText("");
                        break;
                }
                break;
            }

            case 2: {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE: {
                        float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        double diagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        double diagonalRatio = diagonal / prevDiagonal;
                        float scale = (float) (tab.scale * diagonalRatio);
                        int scaledWidth = (int) (bitmap.getWidth() * scale), scaledHeight = (int) (bitmap.getHeight() * scale);
                        tab.scale = scale;
                        imageWidth = scaledWidth;
                        imageHeight = scaledHeight;
                        float pivotX = (float) (this.pivotX * diagonalRatio), pivotY = (float) (this.pivotY * diagonalRatio);
                        tab.translationX = tab.translationX - pivotX + this.pivotX;
                        tab.translationY = tab.translationY - pivotY + this.pivotY;
                        drawAfterTranslatingOrScaling();
                        this.pivotX = pivotX;
                        this.pivotY = pivotY;
                        prevDiagonal = diagonal;
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_DOWN: {
                        float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        this.pivotX = (x0 + x1) / 2.0f - tab.translationX;
                        this.pivotY = (y0 + y1) / 2.0f - tab.translationY;
                        prevDiagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        tvState.setText("");
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        float x = event.getX(1 - event.getActionIndex());
                        float y = event.getY(1 - event.getActionIndex());
                        tvState.setText(String.format(getString(R.string.coordinate),
                                toUnscaled(x - tab.translationX), toUnscaled(y - tab.translationY)));
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
    private final CompoundButton.OnCheckedChangeListener onColorReplacerRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            drawFloatingLayers();
            onToolChange(onImageViewTouchWithColorReplacerListener);
            bitmapWithoutFilter = Bitmap.createBitmap(bitmap);
            threshold = 0x100;
            cbColorReplacerAntiAlias.setChecked(antiAlias);
            etColorReplacerBlurRadius.setText(String.valueOf(blurRadius));
            etColorReplacerStrokeWidth.setText(String.valueOf(strokeWidth));
            hsvOptionsColorReplacer.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onFilterRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            drawFloatingLayers();
            onToolChange(onImageViewTouchWithFilterListener);
            bitmapWithoutFilter = Bitmap.createBitmap(bitmap);
            threshold = 0x100;
            cbFilterAntiAlias.setChecked(antiAlias);
            etFilterBlurRadius.setText(String.valueOf(blurRadius));
            etFilterStrokeWidth.setText(String.valueOf(strokeWidth));
            hsvOptionsFilter.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onGradientRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithGradientListener);
            cbGradientAntiAlias.setChecked(antiAlias);
            paint.setAntiAlias(antiAlias);
            etGradientBlurRadius.setText(String.valueOf(blurRadius));
            etGradientStrokeWidth.setText(String.valueOf(strokeWidth));
            llOptionsGradient.setVisibility(View.VISIBLE);
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
        } else {
//            paint.setMaskFilter(null);
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
            drawTransformeeOnCanvas();
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

    private final LevelsDialog.OnLevelsChangeListener onFilterLevelsSeekBarProgressChangeListener = (shadows, highlights) -> {
        float ratio = 0xFF / (float) (highlights - shadows);
        preview.setFilter(new float[]{
                ratio, 0.0f, 0.0f, 0.0f, -shadows * ratio,
                0.0f, ratio, 0.0f, 0.0f, -shadows * ratio,
                0.0f, 0.0f, ratio, 0.0f, -shadows * ratio,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        });
        drawBitmapOnView(preview.getBitmap());
        tvState.setText(String.format(getString(R.string.state_levels), shadows, highlights));
    };

    private final OnProgressChangeListener onFilterContrastSeekBarProgressChangeListener = progress -> {
        float scale = progress / 10.0f, shift = 0xFF / 2.0f * (1.0f - scale);
        preview.setFilter(new float[]{
                scale, 0.0f, 0.0f, 0.0f, shift,
                0.0f, scale, 0.0f, 0.0f, shift,
                0.0f, 0.0f, scale, 0.0f, shift,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        });
        drawBitmapOnView(preview.getBitmap());
        tvState.setText(String.format(getString(R.string.state_contrast), scale));
    };

    private final OnProgressChangeListener onFilterLightnessSeekBarProgressChangeListener = progress -> {
        preview.setFilter(new float[]{
                1.0f, 0.0f, 0.0f, 0.0f, progress,
                0.0f, 1.0f, 0.0f, 0.0f, progress,
                0.0f, 0.0f, 1.0f, 0.0f, progress,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        });
        drawBitmapOnView(preview.getBitmap());
        tvState.setText(String.format(getString(R.string.state_lightness), progress));
    };

    private final OnProgressChangeListener onFilterSaturationSeekBarProgressChangeListener = progress -> {
        float f = progress / 10.0f;
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(f);
        preview.setFilter(colorMatrix.getArray());
        drawBitmapOnView(preview.getBitmap());
        tvState.setText(String.format(getString(R.string.state_saturation), f));
    };

    private final OnProgressChangeListener onFilterThresholdSeekBarProgressChangeListener = progress -> {
        float f = -0x100 * progress;
        preview.setFilter(new float[]{
                0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        });
        drawBitmapOnView(preview.getBitmap());
        tvState.setText(String.format(getString(R.string.state_threshold), progress));
    };

    private final CellGridManager.OnUpdateListener onUpdateCellGridListener = this::drawGridOnView;

    private final ImageSizeManager.OnUpdateListener onUpdateImageSizeListener = (width, height, stretch) -> {
        resizeBitmap(width, height, stretch);
        drawBitmapOnView();
        history.offer(bitmap);
    };

    private final OnProgressChangeListener onLayerAlphaSeekBarProgressChangeListener = progress -> {
        tab.paint.setAlpha(progress);
        drawBitmapOnView();
        tvState.setText(String.format(
                String.format(getString(R.string.state_alpha), settings.getArgbChannelsFormat()),
                progress));
    };

    private final OnProgressChangeListener onRotateDegreeSeekBarProgressChangeListener = progress -> {
        ivSelection.setRotation(progress);
        drawSelectionOnView();
        tvState.setText(String.format(getString(R.string.degrees_), progress));
    };

    private final DialogInterface.OnCancelListener onRotateCancelListener = dialog -> {
        ivSelection.setRotation(0.0f);
        drawTransformeeAndSelectionOnViewByTranslation();
        tvState.setText("");
    };

    private final DialogInterface.OnClickListener onRotateConfirmListener = (dialog, which) -> {
        transformer.rotate(ivSelection.getRotation());
        ivSelection.setRotation(0.0f);
        drawTransformeeAndSelectionOnViewByTranslation();
        tvState.setText("");
    };

    private final View.OnClickListener onRotateButtonClickListener = v -> {
        int width = selection.width() + 1, height = selection.height() + 1;
        if (width <= 0 || height <= 0) {
            return;
        }
        if (transformer == null) {
            transformer = new Transformer(
                    Bitmap.createBitmap(bitmap, selection.left, selection.top, width, height),
                    tab.translationX + toScaled(selection.left),
                    tab.translationY + toScaled(selection.top));
            canvas.drawRect(selection.left, selection.top, selection.right + 1, selection.bottom + 1, eraser);
            drawBitmapOnView();
            drawTransformeeAndSelectionOnViewByTranslation(false);
        }
        ivSelection.setPivotX(transformer.getTranslationX() + (toScaled(selection.right) - toScaled(selection.left)) / 2.0f);
        ivSelection.setPivotY(transformer.getTranslationY() + (toScaled(selection.bottom) - toScaled(selection.top)) / 2.0f);
        new SeekBarDialog(this).setTitle(R.string.rotate).setMin(0).setMax(359).setProgress(0)
                .setOnCancelListener(onRotateCancelListener)
                .setOnPositiveButtonClickListener(onRotateConfirmListener)
                .setOnProgressChangeListener(onRotateDegreeSeekBarProgressChangeListener)
                .show();
        tvState.setText("");
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
            int radius =
                    (int) Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0));
            previewCanvas.drawCircle(
                    tab.translationX + toScaled(x0 + 0.5f),
                    tab.translationY + toScaled(y0 + 0.5f),
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
                    tab.translationX + toScaled(x0 + 0.5f),
                    tab.translationY + toScaled(y0 + 0.5f),
                    tab.translationX + toScaled(x1 + 0.5f),
                    tab.translationY + toScaled(y1 + 0.5f),
                    paint);
            return String.format(getString(R.string.state_length), Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0)) + 1);
        }
    };

    private final Shape oval = new Shape() {
        @Override
        public void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
            float startX = Math.min(x0, x1), startY = Math.min(y0, y1),
                    stopX = Math.max(x0, x1) + 1.0f, stopY = Math.max(y0, y1) + 1.0f;
            canvas.drawOval(startX, startY, stopX, stopY, paint);
        }

        @Override
        public String drawShapeOnView(int x0, int y0, int x1, int y1) {
            previewCanvas.drawOval(
                    tab.translationX + toScaled(x0 + 0.5f),
                    tab.translationY + toScaled(y0 + 0.5f),
                    tab.translationX + toScaled(x1 + 0.5f),
                    tab.translationY + toScaled(y1 + 0.5f),
                    paint);
            return String.format(getString(R.string.state_axes), Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1);
        }
    };

    private final Shape rect = new Shape() {
        @Override
        public void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
            float startX = Math.min(x0, x1), startY = Math.min(y0, y1),
                    stopX = Math.max(x0, x1), stopY = Math.max(y0, y1);
            canvas.drawRect(startX, startY, stopX, stopY, paint);
        }

        @Override
        public String drawShapeOnView(int x0, int y0, int x1, int y1) {
            previewCanvas.drawRect(
                    tab.translationX + toScaled(x0 + 0.5f),
                    tab.translationY + toScaled(y0 + 0.5f),
                    tab.translationX + toScaled(x1 + 0.5f),
                    tab.translationY + toScaled(y1 + 0.5f),
                    paint);
            return String.format(getString(R.string.state_size), Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1);
        }
    };

    private Shape shape = rect;

    private void addBitmap(Bitmap bitmap, int position) {
        addBitmap(bitmap, position,
                null, getString(R.string.untitled), null);
    }

    private void addBitmap(Bitmap bitmap,
                           String path, String title, Bitmap.CompressFormat compressFormat) {
        addBitmap(bitmap, tabs.size(), path, title, compressFormat);
    }

    private void addBitmap(Bitmap bitmap, int position,
                           String path, String title, Bitmap.CompressFormat compressFormat) {
        tab = new Tab();
        tabs.add(position, tab);
        tab.bitmap = bitmap;
        history = new BitmapHistory();
        tab.history = history;
        history.offer(bitmap);
        tab.paint = new Paint();
        tab.path = path;
        tab.compressFormat = compressFormat;
        cellGrid = new CellGrid();
        tab.cellGrid = cellGrid;

        resetTranslAndScale();

        if (transformer != null) {
            transformer.recycle();
            transformer = null;
        }
        hasSelection = false;

        tabLayout.addTab(tabLayout.newTab().setText(title).setTag(bitmap), position, true);
    }

    private void resetTranslAndScale() {
        tab.translationX = 0.0f;
        tab.translationY = 0.0f;
        tab.scale = (float) ((double) viewWidth / (double) bitmap.getWidth());
        imageWidth = (int) toScaled(bitmap.getWidth());
        imageHeight = (int) toScaled(bitmap.getHeight());
    }

    private boolean areRGBsEqual(@ColorInt int a, @ColorInt int b) {
        int a_a = Color.alpha(a), a_b = Color.alpha(b);
        int rgb_a = rgb(a), rgb_b = rgb(b);
        return a_a == 0x00 && a_b == 0x00 || a_a > 0x00 && a_b > 0x00 && rgb_a == rgb_b;
    }

    private void bucketFill(Bitmap bitmap, int x, int y, @ColorInt final int color) {
        bucketFill(bitmap, x, y, color, 0);
    }

    private void bucketFill(Bitmap bitmap, int x, int y, @ColorInt final int color,
                            final int threshold) {
        int left, top, right, bottom;
        if (hasSelection) {
            left = selection.left;
            top = selection.top;
            right = selection.right;
            bottom = selection.bottom;
        } else {
            left = 0;
            top = 0;
            right = bitmap.getWidth() - 1;
            bottom = bitmap.getHeight() - 1;
        }
        if (!(left <= x && x <= right && top <= y && y <= bottom)) {
            return;
        }
        final int pixel = bitmap.getPixel(x, y);
        if (pixel == color && threshold == 0) {
            return;
        }
        final int w = right - left + 1, h = bottom - top + 1;
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, left, top, w, h);
        int i = 0;
        for (y = top; y <= bottom; ++y) {
            for (x = left; x <= right; ++i, ++x) {
                int px = pixels[i];
                boolean match = false;
                if (threshold > 0) {
                    int r = Color.red(px), g = Color.green(px), b = Color.blue(px);
                    match = checkColorIsWithinThreshold(
                            Color.red(pixel), Color.green(pixel), Color.blue(pixel),
                            r, g, b);
                } else {
                    match = px == pixel;
                }
                if (match) {
                    pixels[i] = color;
                }
            }
        }
        bitmap.setPixels(pixels, 0, w, left, top, w, h);
    }

    private Position checkDraggingBound(float x, float y) {
        draggingBound = Position.NULL;
        isDraggingCorner = false;

        RectF sb = new RectF( // sb - Selection Bounds
                tab.translationX + toScaled(selection.left),
                tab.translationY + toScaled(selection.top),
                tab.translationX + toScaled(selection.right + 1),
                tab.translationY + toScaled(selection.bottom + 1));

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

    private boolean checkColorIsWithinThreshold(int r0, int g0, int b0, int r, int g, int b) {
        return Math.abs(r - r0) <= threshold
                && Math.abs(g - g0) <= threshold
                && Math.abs(b - b0) <= threshold;
    }

    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private void clearCanvasAndInvalidateView(Canvas canvas, ImageView imageView) {
        clearCanvas(canvas);
        imageView.invalidate();
    }

    private void closeTab() {
        bitmap.recycle();
        history.recycle();
        int i = tabLayout.getSelectedTabPosition();
        translationX = tab.translationX;
        translationY = tab.translationY;
        scale = tab.scale;
        tabs.remove(i);
        tabLayout.removeTabAt(i);
    }

    private void createGraphic(int width, int height) {
        createGraphic(width, height, -1);
    }

    private void createGraphic(int width, int height, int position) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        if (position == -1) {
            position = tabs.size();
        }
        addBitmap(bitmap, position);
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

    private void drawAfterTranslatingOrScaling() {
        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
        if (transformer != null) {
            drawTransformeeOnViewBySelection();
        } else if (isEditingText) {
            drawTextOnView();
        } else if (!isShapeStopped) {
            drawPointOnView(shapeStartX, shapeStartY);
        } else if (cloneStampSrc != null) {
            drawCloneStampSrcOnView(cloneStampSrc.x, cloneStampSrc.y);
        }
        drawSelectionOnView();
    }

    private void dragBound(float viewX, float viewY) {
        float halfScale = tab.scale / 2.0f;
        switch (draggingBound) {
            case LEFT: {
                int left = toUnscaled(viewX - tab.translationX + halfScale);
                if (left != selection.left) selection.left = left;
                else return;
                break;
            }
            case TOP: {
                int top = toUnscaled(viewY - tab.translationY + halfScale);
                if (top != selection.top) selection.top = top;
                else return;
                break;
            }
            case RIGHT: {
                int right = toUnscaled(viewX - tab.translationX + halfScale) - 1;
                if (right != selection.right) selection.right = right;
                else return;
                break;
            }
            case BOTTOM: {
                int bottom = toUnscaled(viewY - tab.translationY + halfScale) - 1;
                if (bottom != selection.bottom) selection.bottom = bottom;
                else return;
                break;
            }
            case NULL:
                return;
        }
        hasDragged = true;
    }

    private void drawBitmapOnCanvas(Bitmap bm, float translX, float translY, Canvas cv) {
        int bitmapWidth = bm.getWidth(), bitmapHeight = bm.getHeight();
        int scaledBitmapW = (int) toScaled(bitmapWidth), scaledBitmapH = (int) toScaled(bitmapHeight);
        int startX = translX >= 0.0f ? 0 : toUnscaled(-translX);
        int startY = translY >= 0.0f ? 0 : toUnscaled(-translY);
        int endX = Math.min(toUnscaled(translX + scaledBitmapW <= viewWidth ? scaledBitmapW : viewWidth - translX) + 1, bitmapWidth);
        int endY = Math.min(toUnscaled(translY + scaledBitmapH <= viewHeight ? scaledBitmapH : viewHeight - translY) + 1, bitmapHeight);
        if (startX >= endX || startY >= endY) {
            return;
        }
        float left = translX >= 0.0f ? translX : translX % tab.scale;
        float top = translY >= 0.0f ? translY : translY % tab.scale;
        if (isScaledMuch()) {
            int w = endX - startX, h = endY - startY;
            int[] pixels = new int[w * h];
            bm.getPixels(pixels, 0, w, startX, startY, w, h);
            float t = top, b = t + tab.scale;
            for (int i = 0, y = startY; y < endY; ++y, t += tab.scale, b += tab.scale) {
                float l = left;
                for (int x = startX; x < endX; ++x, ++i) {
                    colorPaint.setColor(pixels[i]);
                    cv.drawRect(l, t, l += tab.scale, b, colorPaint);
                }
            }
        } else {
            float right = Math.min(translX + scaledBitmapW, viewWidth);
            float bottom = Math.min(translY + scaledBitmapH, viewHeight);
            cv.drawBitmap(bm,
                    new Rect(startX, startY, endX, endY),
                    new RectF(left, top, right, bottom),
                    PAINT_OPAQUE);
        }
    }

    private void drawBitmapOnView(Bitmap bitmap) {
        clearCanvas(viewCanvas);
        Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bm);
        for (int i = tabs.size() - 1, selected = tabLayout.getSelectedTabPosition(); i >= 0; --i) {
            Tab tab = tabs.get(i);
            if (i == selected) {
                cv.drawBitmap(bitmap, 0.0f, 0.0f, tab.paint);
            } else if (tab.visible) {
                cv.drawBitmap(tab.bitmap, 0.0f, 0.0f, tab.paint);
            }
        }
        drawBitmapOnCanvas(bm, tab.translationX, tab.translationY, viewCanvas);
        bm.recycle();
        imageView.invalidate();
    }

    private void drawBitmapOnView() {
        drawBitmapOnView(bitmap);
    }

    private void drawPreviewBitmapOnCanvas() {
        canvas.drawBitmap(preview.getBitmap(), 0.0f, 0.0f, PAINT_OPAQUE);
        drawBitmapOnView();
        history.offer(bitmap);
    }

    private void drawChessboardOnView() {
        clearCanvas(chessboardCanvas);
        float left = Math.max(0.0f, tab.translationX);
        float top = Math.max(0.0f, tab.translationY);
        float right = Math.min(tab.translationX + imageWidth, viewWidth);
        float bottom = Math.min(tab.translationY + imageHeight, viewHeight);

        chessboardCanvas.drawBitmap(chessboard,
                new Rect((int) left, (int) top, (int) right, (int) bottom),
                new RectF(left, top, right, bottom),
                PAINT_OPAQUE);

        ivChessboard.invalidate();

        drawRuler();
    }

    private void drawCloneStampSrcOnView(int x, int y) {
        clearCanvas(previewCanvas);
        float scaledX = tab.translationX + toScaled(x), scaledY = tab.translationY + toScaled(y);
        previewCanvas.drawLine(scaledX - 50.0f, scaledY, scaledX + 50.0f, scaledY, selector);
        previewCanvas.drawLine(scaledX, scaledY - 50.0f, scaledX, scaledY + 50.0f, selector);
        ivPreview.invalidate();
    }

    private void drawFloatingLayers() {
        drawTransformeeOnCanvas();
        drawTextOnCanvas();
    }

    private void drawGridOnView() {
        clearCanvas(gridCanvas);
        float startX = tab.translationX >= 0.0f ? tab.translationX : tab.translationX % tab.scale,
                startY = tab.translationY >= 0.0f ? tab.translationY : tab.translationY % tab.scale,
                endX = Math.min(tab.translationX + imageWidth, viewWidth),
                endY = Math.min(tab.translationY + imageHeight, viewHeight);
        if (isScaledMuch()) {
            for (float x = startX; x < endX; x += tab.scale) {
                gridCanvas.drawLine(x, startY, x, endY, gridPaint);
            }
            for (float y = startY; y < endY; y += tab.scale) {
                gridCanvas.drawLine(startX, y, endX, y, gridPaint);
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

        if (cellGrid.enabled) {
            if (cellGrid.sizeX > 1) {
                float scaledSizeX = toScaled(cellGrid.sizeX),
                        scaledSpacingX = toScaled(cellGrid.spacingX);
                startX = (tab.translationX >= 0.0f ? tab.translationX : tab.translationX % (scaledSizeX + scaledSpacingX)) + toScaled(cellGrid.offsetX);
                startY = Math.max(0.0f, tab.translationY);
                if (cellGrid.spacingX <= 0) {
                    float x = startX;
                    while (x < endX) {
                        gridCanvas.drawLine(x, startY, x, endY, cellGridPaint);
                        x += scaledSizeX;
                    }
                } else {
                    float x = startX;
                    while (true) {
                        gridCanvas.drawLine(x, startY, x, endY, cellGridPaint);
                        if ((x += scaledSizeX) >= endX) {
                            break;
                        }
                        gridCanvas.drawLine(x, startY, x, endY, cellGridPaint);
                        if ((x += scaledSpacingX) >= endX) {
                            break;
                        }
                    }
                }
            }
            if (cellGrid.sizeY > 1) {
                float scaledSizeY = toScaled(cellGrid.sizeY),
                        scaledSpacingY = toScaled(cellGrid.spacingY);
                startY = (tab.translationY >= 0.0f ? tab.translationY : tab.translationY % (scaledSizeY + scaledSpacingY)) + toScaled(cellGrid.offsetY);
                startX = Math.max(0.0f, tab.translationX);
                if (cellGrid.spacingY <= 0) {
                    float y = startY;
                    while (y < endY) {
                        gridCanvas.drawLine(startX, y, endX, y, cellGridPaint);
                        y += scaledSizeY;
                    }
                } else {
                    float y = startY;
                    while (true) {
                        gridCanvas.drawLine(startX, y, endX, y, cellGridPaint);
                        if ((y += scaledSizeY) >= endY) {
                            break;
                        }
                        gridCanvas.drawLine(startX, y, endX, y, cellGridPaint);
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
                tab.translationX + toScaled(x),
                tab.translationY + toScaled(y),
                tab.translationX + toScaled(x + 1),
                tab.translationY + toScaled(y + 1),
                fillPaint);
        ivPreview.invalidate();
    }

    private void drawRuler() {
        clearCanvas(rulerHCanvas);
        clearCanvas(rulerVCanvas);
        final int multiplier = (int) Math.ceil(96.0 / tab.scale);
        final float scaledMultiplier = toScaled(multiplier);
        float x = tab.translationX % scaledMultiplier, height = rulerHBitmap.getHeight();
        int unscaledX = (int) (-tab.translationX / scaledMultiplier) * multiplier;
        for (;
             x < viewWidth;
             x += scaledMultiplier, unscaledX += multiplier) {
            rulerHCanvas.drawLine(x, 0.0f, x, height, rulerPaint);
            rulerHCanvas.drawText(String.valueOf(unscaledX), x, height, rulerPaint);
        }
        float y = tab.translationY % scaledMultiplier, width = rulerVBitmap.getWidth();
        int unscaledY = (int) (-tab.translationY / scaledMultiplier) * multiplier;
        float ascent = rulerPaint.getFontMetrics().ascent;
        for (;
             y < viewHeight;
             y += scaledMultiplier, unscaledY += multiplier) {
            rulerVCanvas.drawLine(0.0f, y, width, y, rulerPaint);
            rulerVCanvas.drawText(String.valueOf(unscaledY), 0.0f, y - ascent, rulerPaint);
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
            float left = Math.max(0.0f, tab.translationX + toScaled(selection.left)),
                    top = Math.max(0.0f, tab.translationY + toScaled(selection.top)),
                    right = Math.min(viewWidth, tab.translationX + toScaled(selection.right + 1)),
                    bottom = Math.min(viewHeight, tab.translationY + toScaled(selection.bottom + 1));
            selectionCanvas.drawRect(left, top, right, bottom, selector);
            if (showMargins) {
                float imageLeft = Math.max(0.0f, tab.translationX),
                        imageTop = Math.max(0.0f, tab.translationY),
                        imageRight = Math.min(viewWidth, tab.translationX + imageWidth),
                        imageBottom = Math.min(viewHeight, tab.translationY + imageHeight);
                float centerHorizontal = (left + right) / 2.0f,
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
                    selectionCanvas.drawText(String.valueOf(bitmap.getWidth() - selection.right - 1), (imageRight + right) / 2.0f, centerVertical, marginPaint);
                }
                if (bottom < viewHeight) {
                    selectionCanvas.drawLine(centerHorizontal, bottom, centerHorizontal, imageBottom, marginPaint);
                    selectionCanvas.drawText(String.valueOf(bitmap.getHeight() - selection.bottom - 1), centerHorizontal, (imageBottom + bottom) / 2.0f, marginPaint);
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
            if (selectionStartX <= selectionEndX) {
                selection.left = selectionStartX;
                selection.right = selectionEndX;
            } else {
                selection.left = selectionEndX;
                selection.right = selectionStartX;
            }
            if (selectionStartY <= selectionEndY) {
                selection.top = selectionStartY;
                selection.bottom = selectionEndY;
            } else {
                selection.top = selectionEndY;
                selection.bottom = selectionStartY;
            }
            selectionCanvas.drawRect(
                    tab.translationX + toScaled(selection.left),
                    tab.translationY + toScaled(selection.top),
                    tab.translationX + toScaled(selection.right + 1),
                    tab.translationY + toScaled(selection.bottom + 1),
                    selector);
        }
        ivSelection.invalidate();
    }

    private void drawShapeOnCanvas(int x0, int y0, int x1, int y1) {
        shape.drawShapeOnCanvas(x0, y0, x1, y1);
    }

    private String drawShapeOnView(int x0, int y0, int x1, int y1) {
        clearCanvas(previewCanvas);
        String result = shape.drawShapeOnView(x0, y0, x1, y1);
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
        history.offer(bitmap);
    }

    private void drawTextOnView() {
        if (!isEditingText) {
            return;
        }
        clearCanvas(previewCanvas);
        float x = tab.translationX + toScaled(textX), y = tab.translationY + toScaled(textY);
        paint.setTextSize(toScaled(textSize));
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float centerVertical = y + fontMetrics.ascent / 2.0f;
        previewCanvas.drawText(etText.getText().toString(), x, y, paint);
        previewCanvas.drawLine(x, 0.0f, x, viewHeight, cellGridPaint);
        previewCanvas.drawLine(0.0f, y, viewWidth, y, textLine);
        previewCanvas.drawLine(0.0f, centerVertical, viewWidth, centerVertical, cellGridPaint);
        ivPreview.invalidate();
    }

    private void drawTransformeeOnCanvas() {
        if (transformer != null) {
            if (hasSelection) {
                canvas.drawBitmap(transformer.getBitmap(), selection.left, selection.top, PAINT_BLACK);
                optimizeSelection();
                drawSelectionOnView();
                drawBitmapOnView();
                history.offer(bitmap);
                tvState.setText("");
            }
            transformer.recycle();
            transformer = null;
        }
        clearCanvasAndInvalidateView(previewCanvas, ivPreview);
    }

    private void drawTransformeeAndSelectionOnViewByTranslation() {
        drawTransformeeAndSelectionOnViewByTranslation(false);
    }

    private void drawTransformeeAndSelectionOnViewByTranslation(boolean showMargins) {
        clearCanvas(previewCanvas);
        if (hasSelection && transformer != null) {
            selection.left = toUnscaled(transformer.getTranslationX() - tab.translationX);
            selection.top = toUnscaled(transformer.getTranslationY() - tab.translationY);
            selection.right = selection.left + transformer.getWidth() - 1;
            selection.bottom = selection.top + transformer.getHeight() - 1;
            float ttx = toScaled(selection.left) + tab.translationX;
            float tty = toScaled(selection.top) + tab.translationY;
            drawBitmapOnCanvas(transformer.getBitmap(), ttx, tty, previewCanvas);
        }
        ivPreview.invalidate();
        drawSelectionOnView(showMargins);
    }

    private void drawTransformeeOnViewBySelection() {
        clearCanvas(previewCanvas);
        if (hasSelection && transformer != null) {
            float ttx = toScaled(selection.left) + tab.translationX;
            float tty = toScaled(selection.top) + tab.translationY;
            drawBitmapOnCanvas(transformer.getBitmap(), ttx, tty, previewCanvas);
            transformer.translateTo(ttx, tty);
        }
        ivPreview.invalidate();
    }

    private int fillButOnlyHue(float r, float g, float b, float s0, float v0, float s0_,
                               float v0_, float f, int hi, float alpha) {
        int color = 0;
        final float max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
        final float s = max == 0.0f ? 0.0f : 1.0f - min / max, v = max;
        final float s_ = inRange(s0_ + s - s0, 0.0f, 1.0f),
                v_ = inRange(v0_ + v - v0, 0.0f, 1.0f);
        final float p = v_ * (1.0f - s_),
                q = v_ * (1.0f - f * s_),
                t = v_ * (1.0f - (1.0f - f) * s_);
        switch (hi) {
            case 0:
                color = Color.argb(alpha, v_, t, p);
                break;
            case 1:
                color = Color.argb(alpha, q, v_, p);
                break;
            case 2:
                color = Color.argb(alpha, p, v_, t);
                break;
            case 3:
                color = Color.argb(alpha, p, q, v_);
                break;
            case 4:
                color = Color.argb(alpha, t, p, v_);
                break;
            case 5:
                color = Color.argb(alpha, v_, p, q);
                break;
        }
        return color;
    }

    private void floodFill(Bitmap bitmap, int x, int y, @ColorInt final int color) {
        floodFill(bitmap, bitmap, x, y, color, 0);
    }

    private void floodFill(final Bitmap src, final Bitmap dst, int x, int y,
                           @ColorInt final int color,
                           final int threshold) {
        int left, top, right, bottom;
        if (hasSelection) {
            left = selection.left;
            top = selection.top;
            right = selection.right;
            bottom = selection.bottom;
        } else {
            left = 0;
            top = 0;
            right = src.getWidth() - 1;
            bottom = src.getHeight() - 1;
        }
        if (!(left <= x && x <= right && top <= y && y <= bottom)) {
            return;
        }
        final int pixel = src.getPixel(x, y);
        if (pixel == color && threshold == 0) {
            return;
        }
        final int w = right - left + 1, h = bottom - top + 1, area = w * h;
        final int[] srcPixels = new int[area], dstPixels = src == dst ? srcPixels : new int[area];
        src.getPixels(srcPixels, 0, w, left, top, w, h);
//        long a = System.currentTimeMillis();
        Queue<Point> pointsToBeSet = new LinkedList<>();
        boolean[] havePointsBeenSet = new boolean[area];
        pointsToBeSet.offer(new Point(x, y));
        Point point;
        while ((point = pointsToBeSet.poll()) != null) {
            int i = (point.y - top) * w + (point.x - left);
            if (havePointsBeenSet[i]) {
                continue;
            }
            havePointsBeenSet[i] = true;
            int px = srcPixels[i];
            boolean match = false;
            if (threshold > 0) {
                int r = Color.red(px), g = Color.green(px), b = Color.blue(px);
                match = checkColorIsWithinThreshold(
                        Color.red(pixel), Color.green(pixel), Color.blue(pixel),
                        r, g, b);
            } else {
                match = px == pixel;
            }
            if (match) {
                srcPixels[i] = color;
                if (src != dst) {
                    dstPixels[i] = color;
                }
                int xn = point.x - 1, xp = point.x + 1, yn = point.y - 1, yp = point.y + 1; // n - negative, p - positive
                if (left <= xn && !havePointsBeenSet[i - 1])
                    pointsToBeSet.offer(new Point(xn, point.y));
                if (xp <= right && !havePointsBeenSet[i + 1])
                    pointsToBeSet.offer(new Point(xp, point.y));
                if (top <= yn && !havePointsBeenSet[i - w])
                    pointsToBeSet.offer(new Point(point.x, yn));
                if (yp <= bottom && !havePointsBeenSet[i + w])
                    pointsToBeSet.offer(new Point(point.x, yp));
            }
        }
//        long b = System.currentTimeMillis();
//        Toast.makeText(this, String.valueOf(b - a), Toast.LENGTH_SHORT).show();
        dst.setPixels(dstPixels, 0, w, left, top, w, h);
    }

    private int getColorRangeOf(@ColorInt int color) {
        int range = 0b000000;
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        if (r >= b && b >= g) range |= 0b100000;
        if (r >= g && g >= b) range |= 0b010000;
        if (g >= r && r >= b) range |= 0b001000;
        if (g >= b && b >= r) range |= 0b000100;
        if (b >= g && g >= r) range |= 0b000010;
        if (b >= r && r >= g) range |= 0b000001;
        return range;
    }

    private void hideSoftInputFromWindow() {
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void hideToolOptions() {
        for (int i = 0; i < flToolOptions.getChildCount(); ++i) {
            flToolOptions.getChildAt(i).setVisibility(View.INVISIBLE);
        }
    }

    private float hue(@ColorInt int color) {
        float r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        float max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
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
        return Math.max(Math.min(a, max), min);
    }

    private static int inRange(int a, int min, int max) {
        return Math.max(Math.min(a, max), min);
    }

    private boolean isScaledMuch() {
        return tab.scale >= 16.0f;
    }

    private void load() {
        viewWidth = imageView.getWidth();
        viewHeight = imageView.getHeight();

        tab = new Tab();
        tabs.add(tab);
        bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
        tab.bitmap = bitmap;
        canvas = new Canvas(bitmap);

        cellGrid = new CellGrid();
        tab.cellGrid = cellGrid;

        history = new BitmapHistory();
        tab.history = history;
        history.offer(bitmap);

        tab.paint = new Paint();

        tab.path = null;
        cellGrid = new CellGrid();

        resetTranslAndScale();
        translationX = tab.translationX;
        translationY = tab.translationY;
        scale = tab.scale;

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

        tabLayout.addTab(tabLayout.newTab().setText(R.string.untitled).setTag(bitmap));

        etPencilBlurRadius.setText(String.valueOf(0.0f));
        etEraserBlurRadius.setText(String.valueOf(0.0f));
        etEraserStrokeWidth.setText(String.valueOf(eraser.getStrokeWidth()));
        etPencilStrokeWidth.setText(String.valueOf(paint.getStrokeWidth()));
        etTextSize.setText(String.valueOf(paint.getTextSize()));

        clearCanvasAndInvalidateView(previewCanvas, ivPreview);

        if (fileToBeOpened != null) {
            openFile(fileToBeOpened);
            tabs.remove(0);
            tabLayout.removeTabAt(0);
        }
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
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        settings = ((MainApplication) getApplicationContext()).getSettings();
        settings.update(preferences);

        cbBucketFillContiguous = findViewById(R.id.cb_bucket_fill_contiguous);
        cbCloneStampAntiAlias = findViewById(R.id.cb_clone_stamp_anti_alias);
        cbColorReplacerAntiAlias = findViewById(R.id.cb_color_replacer_anti_alias);
        cbFilterAntiAlias = findViewById(R.id.cb_filter_anti_alias);
        cbFilterClear = findViewById(R.id.cb_filter_clear);
        cbGradientAntiAlias = findViewById(R.id.cb_gradient_anti_alias);
        cbPencilAntiAlias = findViewById(R.id.cb_pencil_anti_alias);
        cbShapeAntiAlias = findViewById(R.id.cb_shape_anti_alias);
        cbShapeFill = findViewById(R.id.cb_shape_fill);
        cbTextFill = findViewById(R.id.cb_text_fill);
        cbTransformerLar = findViewById(R.id.cb_transformer_lar);
        cbZoom = findViewById(R.id.cb_zoom);
        etCloneStampBlurRadius = findViewById(R.id.et_clone_stamp_blur_radius);
        etCloneStampStrokeWidth = findViewById(R.id.et_clone_stamp_stroke_width);
        etColorReplacerBlurRadius = findViewById(R.id.et_color_replacer_blur_radius);
        etColorReplacerStrokeWidth = findViewById(R.id.et_color_replacer_stroke_width);
        etEraserBlurRadius = findViewById(R.id.et_eraser_blur_radius);
        etEraserStrokeWidth = findViewById(R.id.et_eraser_stroke_width);
        etFilterBlurRadius = findViewById(R.id.et_filter_blur_radius);
        etFilterStrokeWidth = findViewById(R.id.et_filter_stroke_width);
        etGradientBlurRadius = findViewById(R.id.et_gradient_blur_radius);
        etGradientStrokeWidth = findViewById(R.id.et_gradient_stroke_width);
        etPencilBlurRadius = findViewById(R.id.et_pencil_blur_radius);
        etPencilStrokeWidth = findViewById(R.id.et_pencil_stroke_width);
        etShapeStrokeWidth = findViewById(R.id.et_shape_stroke_width);
        etText = findViewById(R.id.et_text);
        etTextSize = findViewById(R.id.et_text_size);
        flToolOptions = findViewById(R.id.fl_tool_options);
        flImageView = findViewById(R.id.fl_iv);
        hsvOptionsBucketFill = findViewById(R.id.hsv_options_bucket_fill);
        hsvOptionsCloneStamp = findViewById(R.id.hsv_options_clone_stamp);
        hsvOptionsColorReplacer = findViewById(R.id.hsv_options_color_replacer);
        hsvOptionsFilter = findViewById(R.id.hsv_options_filter);
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
        llOptionsPencil = findViewById(R.id.ll_options_pencil);
        llOptionsShape = findViewById(R.id.ll_options_shape);
        llOptionsText = findViewById(R.id.ll_options_text);
        llOptionsTransformer = findViewById(R.id.ll_options_transformer);
        rvSwatches = findViewById(R.id.rv_swatches);
        rbBucketFill = findViewById(R.id.rb_bucket_fill);
        rbCloneStamp = findViewById(R.id.rb_clone_stamp);
        rbColorReplacer = findViewById(R.id.rb_color_replacer);
        rbFilter = findViewById(R.id.rb_filter);
        RadioButton rbPencil = findViewById(R.id.rb_pencil);
        rbTransformer = findViewById(R.id.rb_transformer);
        sColorReplacerBlendMode = findViewById(R.id.s_color_replacer_blend_mode);
        tabLayout = findViewById(R.id.tl);
        tvState = findViewById(R.id.tv_state);
        vBackgroundColor = findViewById(R.id.v_background_color);
        vForegroundColor = findViewById(R.id.v_foreground_color);

        findViewById(R.id.b_bucket_fill_threshold).setOnClickListener(onThresholdButtonClickListener);
        findViewById(R.id.b_clone_stamp_src).setOnClickListener(onCloneStampSrcButtonClickListener);
        findViewById(R.id.b_color_filter).setOnClickListener(onFilterButtonClickListener);
        findViewById(R.id.b_color_replacer_tolerance).setOnClickListener(onThresholdButtonClickListener);
        findViewById(R.id.b_filter_tolerance).setOnClickListener(onThresholdButtonClickListener);
        findViewById(R.id.b_text_draw).setOnClickListener(v -> drawTextOnCanvas());
        findViewById(R.id.b_transformer_rotate).setOnClickListener(onRotateButtonClickListener);
        cbCloneStampAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbColorReplacerAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.cb_eraser_anti_alias)).setOnCheckedChangeListener((buttonView, isChecked) -> eraser.setAntiAlias(isChecked));
        cbFilterAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbGradientAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbPencilAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbShapeAntiAlias.setOnCheckedChangeListener(onAntiAliasCheckedChangeListener);
        cbShapeFill.setOnCheckedChangeListener((buttonView, isChecked) -> paint.setStyle(isChecked ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE));
        cbZoom.setOnCheckedChangeListener(onZoomToolCheckBoxCheckedChangeListener);
        cbZoom.setTag(onImageViewTouchWithPencilListener);
        etCloneStampBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etCloneStampStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etColorReplacerBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etColorReplacerStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etFilterBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etFilterStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etGradientBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etGradientStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etPencilBlurRadius.addTextChangedListener(onBlurRadiusTextChangedListener);
        etPencilStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etShapeStrokeWidth.addTextChangedListener(onStrokeWidthTextChangedListener);
        etText.addTextChangedListener((AfterTextChangedListener) s -> drawTextOnView());
        etTextSize.addTextChangedListener(onTextSizeChangedListener);
        flImageView.setOnTouchListener(onImageViewTouchWithPencilListener);
        rbBucketFill.setOnCheckedChangeListener(onBucketFillRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_circle)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = circle);
        rbCloneStamp.setOnCheckedChangeListener(onCloneStampRadioButtonCheckedChangeListener);
        rbColorReplacer.setOnCheckedChangeListener(onColorReplacerRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_eraser)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(isChecked, onImageViewTouchWithEraserListener, llOptionsEraser));
        ((CompoundButton) findViewById(R.id.rb_eyedropper)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(isChecked, onImageViewTouchWithEyedropperListener));
        rbFilter.setOnCheckedChangeListener(onFilterRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_gradient)).setOnCheckedChangeListener(onGradientRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_line)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = line);
        ((CompoundButton) findViewById(R.id.rb_oval)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = oval);
        rbPencil.setOnCheckedChangeListener(onPencilRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_rect)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = rect);
        ((CompoundButton) findViewById(R.id.rb_selector)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(isChecked, onImageViewTouchWithSelectorListener));
        ((CompoundButton) findViewById(R.id.rb_shape)).setOnCheckedChangeListener(onShapeRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_text)).setOnCheckedChangeListener(onTextRadioButtonCheckedChangeListener);
        rbTransformer.setOnCheckedChangeListener(onTransformerRadioButtonCheckedChangeListener);
        sColorReplacerBlendMode.setOnItemSelectedListener(onColorReplacerBlendModeSelectedListener);
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
                float f = Float.parseFloat(s);
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

        rvSwatches.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
                    int color = ((ColorDrawable) view.getBackground()).getColor();
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
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        smBlendModes = menu.findItem(R.id.i_blend_modes).getSubMenu();
        miLayerVisible = menu.findItem(R.id.i_layer_visible);
        return true;
    }

    @Override
    protected void onDestroy() {

        canvas = null;
        bitmap.recycle();
        bitmap = null;

        if (bitmapWithoutFilter != null) {
            bitmapWithoutFilter.recycle();
            bitmapWithoutFilter = null;
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
            transformer.recycle();
            transformer = null;
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
            case R.id.i_blend_mode_null:
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
                for (int i = 0; i <= 29; ++i) {
                    MenuItem mi = smBlendModes.getItem(i);
                    if (mi == item) {
                        tab.paint.setBlendMode(i == 0 ? null : BlendMode.values()[i - 1]);
                        mi.setChecked(true);
                    } else if (mi.isChecked()) {
                        mi.setChecked(false);
                    }
                }
                drawBitmapOnView();
                break;

            case R.id.i_cell_grid: {
                CellGridManager.make(this, cellGrid,
                                onUpdateCellGridListener)
                        .show();
                break;
            }
            case R.id.i_close:
            case R.id.i_layer_delete:
                if (tabs.size() == 1) {
                    break;
                }
                if (transformer != null) {
                    transformer.recycle();
                    transformer = null;
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
                    clipboard = Bitmap.createBitmap(bitmap, selection.left, selection.top,
                            selection.width() + 1, selection.height() + 1);
                } else {
                    clipboard = Bitmap.createBitmap(transformer.getBitmap());
                }
                break;

            case R.id.i_crop: {
                if (!hasSelection) {
                    break;
                }
                drawFloatingLayers();
                int width = selection.width() + 1, height = selection.height() + 1;
                Bitmap bm = Bitmap.createBitmap(bitmap, selection.left, selection.top, width, height);
                resizeBitmap(width, height, false);
                canvas.drawBitmap(bm, 0.0f, 0.0f, PAINT_OPAQUE);
                bm.recycle();
                drawBitmapOnView();
                history.offer(bitmap);
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
                            selection.width() + 1, selection.height() + 1);
                    canvas.drawRect(selection.left, selection.top, selection.right + 1, selection.bottom + 1, eraser);
                    drawBitmapOnView();
                    history.offer(bitmap);
                } else {
                    clipboard = Bitmap.createBitmap(transformer.getBitmap());
                    transformer.recycle();
                    transformer = null;
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                }
                break;

            case R.id.i_delete:
                if (!hasSelection) {
                    break;
                }
                if (transformer == null) {
                    canvas.drawRect(selection.left, selection.top, selection.right + 1, selection.bottom + 1, eraser);
                    drawBitmapOnView();
                    history.offer(bitmap);
                } else {
                    transformer.recycle();
                    transformer = null;
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                }
                break;

            case R.id.i_deselect:
                drawFloatingLayers();
                hasSelection = false;
                clearCanvasAndInvalidateView(selectionCanvas, ivSelection);
                tvState.setText("");
                break;

            case R.id.i_filter_channels:
                drawFloatingLayers();
                createPreviewBitmap();
                new ChannelMixer(this)
                        .setOnCancelListener(onFilterCancelListener)
                        .setOnMatrixChangeListener(onColorMatrixChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_color_balance:
                drawFloatingLayers();
                createPreviewBitmap();
                new ColorBalanceDialog(this)
                        .setOnCancelListener(onFilterCancelListener)
                        .setOnMatrixChangeListener(onColorMatrixChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_contrast:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.contrast).setMin(-10).setMax(100).setProgress(10)
                        .setOnProgressChangeListener(onFilterContrastSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_levels:
                drawFloatingLayers();
                createPreviewBitmap();
                new LevelsDialog(this)
                        .setOnLevelsChangeListener(onFilterLevelsSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_lightness:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.lightness).setMin(-0xFF).setMax(0xFF).setProgress(0)
                        .setOnProgressChangeListener(onFilterLightnessSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvState.setText("");
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
                tvState.setText("");
                break;

            case R.id.i_filter_threshold:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.threshold).setMin(0).setMax(255).setProgress(128)
                        .setOnProgressChangeListener(onFilterThresholdSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                onFilterThresholdSeekBarProgressChangeListener.onProgressChanged(128);
                tvState.setText("");
                break;

            case R.id.i_filter_saturation:
                drawFloatingLayers();
                createPreviewBitmap();
                new SeekBarDialog(this).setTitle(R.string.saturation).setMin(0).setMax(100).setProgress(10)
                        .setOnProgressChangeListener(onFilterSaturationSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvState.setText("");
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
            case R.id.i_layer_alpha:
                drawFloatingLayers();
                new SeekBarDialog(this).setTitle(R.string.alpha_value).setMin(0).setMax(255)
                        .setProgress(tab.paint.getAlpha())
                        .setOnProgressChangeListener(onLayerAlphaSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener((dialog, which) -> tvState.setText(""))
                        .setOnCancelListener(dialog -> tvState.setText(""), false)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_layer_duplicate: {
                drawFloatingLayers();
                Bitmap bm = Bitmap.createBitmap(bitmap);
                addBitmap(bm, tabLayout.getSelectedTabPosition());
                tab.visible = true;
                break;
            }
            case R.id.i_layer_duplicate_by_hue: {
                drawFloatingLayers();
                createPreviewBitmap();
                new ColorRangeDialog(this)
                        .setOnColorRangeChangeListener(onColorRangeChangeListener)
                        .setOnPositiveButtonClickListener(onLayerDuplicateByHueConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                break;
            }
            case R.id.i_layer_merge: {
                drawFloatingLayers();
                int i = tabLayout.getSelectedTabPosition();
                if (i + 1 >= tabs.size()) {
                    break;
                }
                Bitmap bm = Bitmap.createBitmap(bitmap);
                Paint paint = tab.paint;
                closeTab();
                tabLayout.getTabAt(i).select();
                canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
                bm.recycle();
                history.offer(bitmap);
                drawBitmapOnView();
                break;
            }
            case R.id.i_layer_merge_as_hidden: {
                drawFloatingLayers();
                int j = tabLayout.getSelectedTabPosition() + 1;
                if (j >= tabs.size()) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.exception_merge_as_hidden)
                            .setPositiveButton(R.string.ok, null)
                            .setTitle(R.string.merge_as_a_hidden_image)
                            .show();
                    break;
                }
                HiddenImageMaker.merge(this,
                        new Bitmap[]{bitmap, tabs.get(j).bitmap},
                        onFinishMakingHiddenImageListener);
                break;
            }
            case R.id.i_layer_merge_as_new: {
                drawFloatingLayers();
                int i = tabLayout.getSelectedTabPosition(), j = i + 1;
                if (j >= tabs.size()) {
                    break;
                }
                Bitmap bm = Bitmap.createBitmap(tabs.get(j).bitmap);
                Canvas cv = new Canvas(bm);
                cv.drawBitmap(bitmap, 0.0f, 0.0f, tab.paint);
                addBitmap(bm, i);
                break;
            }
            case R.id.i_layer_merge_visible: {
                drawFloatingLayers();
                Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas cv = new Canvas(bm);
                int selected = tabLayout.getSelectedTabPosition();
                for (int i = tabs.size() - 1; i >= 0; --i) {
                    Tab tab = tabs.get(i);
                    if (tab.visible || i == selected) {
                        cv.drawBitmap(tab.bitmap, 0.0f, 0.0f, tab.paint);
                    }
                }
                addBitmap(bm, selected);
                break;
            }
            case R.id.i_layer_new:
                drawFloatingLayers();
                createGraphic(bitmap.getWidth(), bitmap.getHeight(), tabLayout.getSelectedTabPosition());
                tab.visible = true;
                break;

            case R.id.i_layer_send_to_back:
                drawFloatingLayers();
                int i = tabLayout.getSelectedTabPosition(), j = i + 1;
                if (j >= tabs.size()) {
                    break;
                }
                Collections.swap(tabs, i, j);
                TabLayout.Tab ti = tabLayout.getTabAt(i), tj = tabLayout.getTabAt(j);
                CharSequence csi = ti.getText(), csj = tj.getText();
                ti.setText(csj);
                tj.setText(csi);
                tabLayout.selectTab(tj);
                break;

            case R.id.i_layer_visible:
                item.setChecked(!item.isChecked());
                tab.visible = item.isChecked();
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

            case R.id.i_paste:
                if (clipboard == null) {
                    break;
                }
                drawFloatingLayers();

                selection.left = tab.translationX >= 0.0f ? 0 : toUnscaled(-tab.translationX) + 1;
                selection.top = tab.translationY >= 0.0f ? 0 : toUnscaled(-tab.translationY) + 1;
                selection.right = selection.left + Math.min(clipboard.getWidth(), bitmap.getWidth());
                selection.bottom = selection.top + Math.min(clipboard.getHeight(), bitmap.getHeight());
                transformer = new Transformer(Bitmap.createBitmap(clipboard),
                        tab.translationX + toScaled(selection.left),
                        tab.translationY + toScaled(selection.top));
                hasSelection = true;
                rbTransformer.setChecked(true);
                drawTransformeeAndSelectionOnViewByTranslation();
                break;

            case R.id.i_redo:
                if (history.canRedo()) {
                    undoOrRedo(history.redo());
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
                tvState.setText("");
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
                    undoOrRedo(history.getCurrent());
                } else if (!isShapeStopped) {
                    isShapeStopped = true;
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                } else if (history.canUndo()) {
                    undoOrRedo(history.undo());
                }
                break;
            }

            case R.id.i_view_refit:
                resetTranslAndScale();
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
        if (bitmapWithoutFilter != null) {
            bitmapWithoutFilter.recycle();
            bitmapWithoutFilter = null;
        }
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasNotLoaded && hasFocus) {
            hasNotLoaded = false;
            load();
        }
    }

    private void openBitmap(Bitmap bm, Uri uri) {
        int width = bm.getWidth(), height = bm.getHeight();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawBitmap(bm, 0.0f, 0.0f, PAINT_OPAQUE);
        bm.recycle();
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        String path = null;
        Bitmap.CompressFormat compressFormat = null;
        switch (documentFile.getType()) {
            case "image/jpeg":
                compressFormat = Bitmap.CompressFormat.JPEG;
                path = UriToPathUtil.getRealFilePath(this, uri);
                break;
            case "image/png":
                compressFormat = Bitmap.CompressFormat.PNG;
                path = UriToPathUtil.getRealFilePath(this, uri);
                break;
            default:
                Toast.makeText(this, R.string.not_supported_file_type, Toast.LENGTH_SHORT).show();
                break;
        }
        addBitmap(bitmap,
                path, documentFile.getName(), compressFormat);
    }

    private void openFile(Uri uri) {
        if (uri == null) {
            return;
        }
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            Bitmap bm = BitmapFactory.decodeStream(inputStream);
            openBitmap(bm, uri);
            bm.recycle();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void optimizeSelection() {
        int bitmapWidth = bitmap.getWidth(), bitmapHeight = bitmap.getHeight();
        if (selection.left > selection.right) {
            int i = selection.left;
            selection.left = selection.right + 1;
            selection.right = i - 1;
        }
        if (selection.top > selection.bottom) {
            int i = selection.top;
            selection.top = selection.bottom + 1;
            selection.bottom = i - 1;
        }
        if (selection.left < bitmapWidth && selection.top < bitmapHeight
                && selection.right >= 0 && selection.bottom >= 0) {
            selection.left = Math.max(0, selection.left);
            selection.top = Math.max(0, selection.top);
            selection.right = Math.min(bitmapWidth - 1, selection.right);
            selection.bottom = Math.min(bitmapHeight - 1, selection.bottom);
        } else {
            hasSelection = false;
        }
    }

    private void recycleBitmap(Bitmap bm) {
        if (bm != null) {
            bm.recycle();
        }
    }

    private void resizeBitmap(int width, int height, boolean stretch) {
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bm);
        if (stretch) {
            cv.drawBitmap(bitmap,
                    new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                    new RectF(0.0f, 0.0f, width, height),
                    PAINT_OPAQUE);
        } else {
            cv.drawRect(0.0f, 0.0f, width, height, eraser);
            cv.drawBitmap(bitmap, 0.0f, 0.0f, PAINT_OPAQUE);
        }
        bitmap.recycle();
        bitmap = bm;
        tab.bitmap = bitmap;
        canvas = cv;
        imageWidth = (int) toScaled(width);
        imageHeight = (int) toScaled(height);

        if (transformer != null) {
            transformer.recycle();
            transformer = null;
        }
        hasSelection = false;

        drawChessboardOnView();
        drawGridOnView();
        drawSelectionOnView();

        tvState.setText("");
    }

    @ColorInt
    private int rgb(@ColorInt int color) {
        return color & 0x00FFFFFF;
    }

    private void rotate(float degrees) {
        rotate(degrees, true);
    }

    private void rotate(float degrees, boolean filter) {
        int left = 0, top = 0, width = bitmap.getWidth(), height = bitmap.getHeight();
        if (hasSelection) {
            left = selection.left;
            top = selection.top;
            width = selection.width() + 1;
            height = selection.height() + 1;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, width / 2.0f, height / 2.0f);
        Bitmap bm = Bitmap.createBitmap(bitmap, left, top, width, height, matrix, filter);
        canvas.drawBitmap(bm, left, top, PAINT_OPAQUE);
        bm.recycle();
        drawBitmapOnView();
        history.offer(bitmap);
    }

    private void save() {
        save(tab.path);
    }

    private void save(String path) {
        if (path == null) {
            getTree.launch(null);
            return;
        }

        drawFloatingLayers();

        File file = new File(path);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(tab.compressFormat, 100, fos);
            fos.flush();
        } catch (IOException e) {
            Toast.makeText(this, "Failed\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }

    private void saveAs() {
        String path = null;
        getTree.launch(null);
    }

    private void scale(float x, float y) {
        scale(x, y, true);
    }

    private void scale(float x, float y, boolean filter) {
        int left = 0, top = 0, width = bitmap.getWidth(), height = bitmap.getHeight();
        if (hasSelection) {
            left = selection.left;
            top = selection.top;
            width = selection.width() + 1;
            height = selection.height() + 1;
        }
        Matrix matrix = new Matrix();
        matrix.setScale(x, y, 0.0f, 0.0f);
        Bitmap bm = Bitmap.createBitmap(bitmap, left, top, width, height, matrix, filter);
        canvas.drawBitmap(bm, left, top, PAINT_OPAQUE);
        bm.recycle();
        drawBitmapOnView();
        history.offer(bitmap);
    }

    private void scaleAlpha(Bitmap bitmap) {
        int w = bitmap.getWidth(), h = bitmap.getHeight(), area = w * h;
        int[] pixels = new int[area];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < area; ++i) {
            if (Color.alpha(pixels[i]) > 0x00) {
                pixels[i] |= 0xFF000000;
            }
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private void selectAll() {
        selection.left = 0;
        selection.top = 0;
        selection.right = bitmap.getWidth() - 1;
        selection.bottom = bitmap.getHeight() - 1;
    }

    private void setBlurRadius(float f) {
        paint.setMaskFilter(f > 0.0f ? new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL) : null);
    }

    private void stretchByBound(float viewX, float viewY) {
        dragBound(viewX, viewY);
        if (cbTransformerLar.isChecked()) {
            if (draggingBound == Position.LEFT || draggingBound == Position.RIGHT) {
                double width = selection.width() + 1, height = width / transformer.getAspectRatio();
                selection.top = (int) (transformer.getCenterY() - height / 2.0);
                selection.bottom = (int) (transformer.getCenterY() + height / 2.0);
            } else if (draggingBound == Position.TOP || draggingBound == Position.BOTTOM) {
                double height = selection.height() + 1, width = height * transformer.getAspectRatio();
                selection.left = (int) (transformer.getCenterX() - width / 2.0);
                selection.right = (int) (transformer.getCenterX() + width / 2.0);
            }
        }
        drawSelectionOnView(true);
    }

    private int toUnscaled(float scaled) {
        return (int) (scaled / tab.scale);
    }

    private float toScaled(int unscaled) {
        return unscaled * tab.scale;
    }

    private float toScaled(float unscaled) {
        return unscaled * tab.scale;
    }

    private void undoOrRedo(Bitmap bm) {
        optimizeSelection();
        bitmap.recycle();
        bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        tab.bitmap = bitmap;
        canvas = new Canvas(bitmap);
        canvas.drawBitmap(bm, 0.0f, 0.0f, PAINT_OPAQUE);

        imageWidth = (int) toScaled(bitmap.getWidth());
        imageHeight = (int) toScaled(bitmap.getHeight());

        if (transformer != null) {
            transformer.recycle();
            transformer = null;
        }
        clearCanvasAndInvalidateView(previewCanvas, ivPreview);

        optimizeSelection();
        isShapeStopped = true;
        hasDragged = false;

        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
        drawSelectionOnView();

        if (cloneStampSrc != null) {
            drawCloneStampSrcOnView(cloneStampSrc.x, cloneStampSrc.y);
        }

        tvState.setText("");
    }
}