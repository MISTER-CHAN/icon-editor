package com.misterchan.iconeditor;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
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
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
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
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.Q)
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
    private BitmapWithFilter bitmapWithFilter;
    private BitmapWithFilter thresholdBitmap;
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
    private CheckBox cbBucketFillKeepColorDiff;
    private CheckBox cbFilterClear;
    private CheckBox cbGradientAntiAlias;
    private CheckBox cbPencilAntiAlias;
    private CheckBox cbShapeAntiAlias;
    private CheckBox cbShapeFill;
    private CheckBox cbTextAntiAlias;
    private CheckBox cbTextFill;
    private CheckBox cbTransformerLar;
    private CheckBox cbZoom;
    private ColorAdapter colorAdapter;
    private double prevDiagonal;
    private EditText etCloneStampStrokeWidth;
    private EditText etEraserStrokeWidth;
    private EditText etFileName;
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
    private float strokeWidth = 1.0f;
    private FrameLayout flImageView;
    private FrameLayout flToolOptions;
    private HorizontalScrollView hsvOptionsBucketFill;
    private HorizontalScrollView hsvOptionsFilter;
    private ImageView imageView;
    private ImageView ivChessboard;
    private ImageView ivGrid;
    private ImageView ivPreview;
    private ImageView ivRulerH, ivRulerV;
    private ImageView ivSelection;
    private InputMethodManager inputMethodManager;
    private int colorRange = 0b111111;
    private int currentTabIndex;
    @ColorInt
    private int gradientColor0;
    private int imageWidth, imageHeight;
    private int selectionStartX, selectionStartY;
    private int selectionEndX, selectionEndY;
    private int shapeStartX, shapeStartY;
    private int textX, textY;
    private int threshold;
    private int viewWidth, viewHeight;
    private LinearLayout llOptionsCloneStamp;
    private LinearLayout llOptionsEraser;
    private LinearLayout llOptionsGradient;
    private LinearLayout llOptionsPencil;
    private LinearLayout llOptionsShape;
    private LinearLayout llOptionsText;
    private LinearLayout llOptionsTransformer;
    private LinkedList<Integer> palette;
    private List<Tab> tabs = new ArrayList<>();
    private Point cloneStampSrc; // Sel. - Selection
    private Point cloneStampSrcDist = new Point(0, 0); // Dist. - Distance
    private Position draggingBound = Position.NULL;
    private RadioButton rbBucketFill;
    private RadioButton rbCloneStamp;
    private RadioButton rbFilter;
    private RadioButton rbTransformer;
    private Rect selection = new Rect();
    private RectF transfromeeDpb = new RectF(); // DPB - Distance from point to bounds
    private RecyclerView rvSwatches;
    private Spinner sFileType;
    private String tree = "";
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

    private final Paint blackBlurPaint = new Paint() {
        {
            setAntiAlias(true);
            setColor(Color.BLACK);
            setDither(true);
        }
    };

    private final Paint blackPaint = new Paint() {
        {
            setColor(Color.BLACK);
        }
    };

    private final Paint cellGridPaint = new Paint() {
        {
            setColor(Color.RED);
            setStrokeWidth(2.0f);
        }
    };

    private final Paint clear = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    };

    private final Paint cloner = new Paint();

    private final Paint colorPaint = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    };

    private final Paint dstIn = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
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
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
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
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        }
    };

    private final Paint filterClear = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
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

    private final Paint opaquePaint = new Paint() {
        {
            setColor(Color.BLACK);
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
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

    private final Paint pointPaint = new Paint() {
        {
            setColor(Color.RED);
            setStrokeWidth(4.0f);
            setTextSize(32.0f);
        }
    };

    private final Paint roundCopy = new Paint() {
        {
            setAntiAlias(true);
            setColor(Color.BLACK);
            setDither(true);
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
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

    private final Paint srcIn = new Paint() {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        }
    };

    private final Paint textLine = new Paint() {
        {
            setColor(Color.BLUE);
            setStrokeWidth(2.0f);
        }
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
        sFileType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.file_types)));
    };

    private final ActivityResultLauncher<String> getImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), imageCallback);

    private final ActivityResultLauncher<Uri> getTree =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), treeCallback);

    private final DialogInterface.OnCancelListener onFilterCancelListener = dialog -> {
        drawBitmapOnView();
        bitmapWithFilter.recycle();
        bitmapWithFilter = null;
        tvState.setText("");
    };

    private final DialogInterface.OnClickListener onFilterConfirmListener = (dialog, which) -> {
        drawBitmapWithFilterOnCanvas();
        tvState.setText("");
    };

    private final DialogInterface.OnCancelListener onThresholdCancelListener = dialog -> {
        drawBitmapOnView();
        tvState.setText("");
    };

    private final DialogInterface.OnClickListener onThresholdConfirmListener = (dialog, which) -> {
        drawBitmapOnView();
        tvState.setText("");
    };

    private final View.OnClickListener onAddSwatchViewClickListener = v ->
            ColorPicker.make(MainActivity.this,
                            R.string.add,
                            (oldColor, newColor) -> {
                                palette.offerFirst(newColor);
                                colorAdapter.notifyDataSetChanged();
                            },
                            paint.getColor())
                    .show();

    private final View.OnClickListener onBackgroundColorClickListener = v ->
            ColorPicker.make(MainActivity.this,
                            R.string.background_color,
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
                            (oldColor, newColor) -> {
                                paint.setColor(newColor);
                                vForegroundColor.setBackgroundColor(newColor);
                                if (isEditingText) {
                                    drawTextOnView();
                                }
                            },
                            paint.getColor())
                    .show();

    private final ColorRangeDialog.OnColorRangeChangeListener onColorRangeChangeListener = range -> {
        colorRange = range;
        if (range == 0b000000) {
            thresholdBitmap.drawColor(Color.TRANSPARENT);
        } else if (range == 0b111111) {
            thresholdBitmap.clearFilter();
        } else {
            final int w = thresholdBitmap.getWidth(), h = thresholdBitmap.getHeight(), area = w * h;
            final int[] pixels = new int[area];
            thresholdBitmap.getPixels(pixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < area; ++i) {
                if ((getColorRangeOf(pixels[i]) | range) != range) {
                    pixels[i] = Color.TRANSPARENT;
                }
            }
            thresholdBitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        drawBitmapWithFilterOnView(thresholdBitmap);
    };

    private final View.OnClickListener onColorRangeButtonClickListener = v -> {
        new ColorRangeDialog(this)
                .setDefaultRange(colorRange)
                .setOnCancelListener(dialog -> drawBitmapOnView())
                .setOnColorRangeChangeListener(onColorRangeChangeListener)
                .setOnPositiveButtonClickListener((dialog, which) -> drawBitmapOnView())
                .show();
        onColorRangeChangeListener.onChange(colorRange);
        tvState.setText("");
    };

    private final HiddenImageMaker.OnFinishSettingListener onFinishMakingHiddenImageListener = bm -> {
        createGraphic(bm.getWidth(), bm.getHeight());
        canvas.drawBitmap(bm, 0.0f, 0.0f, opaquePaint);
        drawBitmapOnView();
        bm.recycle();
    };

    private final NewGraphicPropertiesDialog.OnFinishSettingListener onFinishSettingNewGraphicPropertiesListener = this::createGraphic;

    private final ColorMatrixManager.OnMatrixElementsChangeListener onColorMatrixChangeListener = matrix -> {
        bitmapWithFilter.setFilter(new ColorMatrix(matrix));
        drawBitmapWithFilterOnView();
    };

    private final OnProgressChangeListener onThresholdChangeListener = progress -> {
        threshold = progress;
        if (progress == 0x100) {
            thresholdBitmap.drawColor(Color.BLACK);
        } else if (progress == 0x0) {
            thresholdBitmap.clearFilter();
        } else {
            final int w = thresholdBitmap.getWidth(), h = thresholdBitmap.getHeight(), area = w * h;
            final int[] pixels = new int[area];
            thresholdBitmap.getPixels(pixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < area; ++i) {
                final int pixel = pixels[i];
                pixels[i] = Color.argb(Color.alpha(pixel),
                        Color.red(pixel) / progress * progress,
                        Color.green(pixel) / progress * progress,
                        Color.blue(pixel) / progress * progress);
            }
            thresholdBitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        drawBitmapWithFilterOnView(thresholdBitmap);
        tvState.setText(String.format(getString(R.string.state_threshold), progress));
    };

    private final ColorMatrixManager.OnMatrixElementsChangeListener onPaintFilterChangeListener = matrix -> {
        colorMatrix = matrix;
        filter.setColorFilter(new ColorMatrixColorFilter(matrix));
    };

    private final View.OnClickListener onThresholdButtonClickListener = v -> {
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
            drawTransformeeOnCanvas();
            drawTextOnCanvas();

            currentTabIndex = tab.getPosition();
            MainActivity.this.tab = tabs.get(currentTabIndex);
            bitmap = MainActivity.this.tab.bitmap;
            canvas = new Canvas(bitmap);
            history = MainActivity.this.tab.history;
            cellGrid = MainActivity.this.tab.cellGrid;

            int width = bitmap.getWidth(), height = bitmap.getHeight();
            imageWidth = (int) toScaled(width);
            imageHeight = (int) toScaled(height);

            if (transformer != null) {
                transformer.recycle();
                transformer = null;
            }
            hasSelection = false;

            if (rbFilter.isChecked()) {
                bitmapWithoutFilter.recycle();
                bitmapWithoutFilter = Bitmap.createBitmap(bitmap);
                createThresholdBitmap(0x100);
            } else if (rbBucketFill.isChecked()) {
                createThresholdBitmap(0x0);
            } else if (rbCloneStamp.isChecked()) {
                cloneStampSrc = null;
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
                int originalX = toOriginal(x - tab.translationX), originalY = toOriginal(y - tab.translationY);
                if (!(0 <= originalX && originalX < bitmap.getWidth() && 0 <= originalY && originalY < bitmap.getHeight())) {
                    break;
                }
                if (cbBucketFillContiguous.isChecked()) {
                    floodFill(bitmap, bitmap, originalX, originalY, paint.getColor(),
                            threshold, colorRange, cbBucketFillKeepColorDiff.isChecked());
                } else {
                    bucketFill(bitmap, originalX, originalY, paint.getColor(),
                            threshold, colorRange, cbBucketFillKeepColorDiff.isChecked());
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
        int originalX = toOriginal(x - tab.translationX), originalY = toOriginal(y - tab.translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (cloneStampSrc == null) {
                    break;
                }
                cloneStampSrcDist.x = cloneStampSrc.x - originalX;
                cloneStampSrcDist.y = cloneStampSrc.y - originalY;
                prevX = x;
                prevY = y;

            case MotionEvent.ACTION_MOVE: {
                if (cloneStampSrc == null) {
                    break;
                }
                int originalPrevX = toOriginal(prevX - tab.translationX),
                        originalPrevY = toOriginal(prevY - tab.translationY);

                int width = (int) (Math.abs(originalX - originalPrevX) + strokeWidth),
                        height = (int) (Math.abs(originalY - originalPrevY) + strokeWidth);
                Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                float rad = strokeWidth / 2.0f;
                float left = Math.min(originalPrevX, originalX) - rad, top = Math.min(originalPrevY, originalY) - rad;
                int l = (int) (left + cloneStampSrcDist.x), t = (int) (top + cloneStampSrcDist.y);
                Canvas cv = new Canvas(bm);
                cv.drawLine(originalPrevX - left, originalPrevY - top,
                        originalX - left, originalY - top,
                        paint);
                cv.drawRect(0.0f, 0.0f, -l, height, clear);
                cv.drawRect(0.0f, 0.0f, width, -t, clear);
                cv.drawRect(bitmap.getWidth() - l, 0.0f, width, height, clear);
                cv.drawRect(0.0f, bitmap.getHeight() - t, width, height, clear);
                cv.drawBitmap(bitmap,
                        new Rect(l, t, l + width, t + height),
                        new RectF(0.0f, 0.0f, width, height),
                        srcIn);
                canvas.drawBitmap(bm, left, top, paint);
                bm.recycle();
                drawBitmapOnView();
                drawCloneStampSrcOnView(originalX + cloneStampSrcDist.x, originalY + cloneStampSrcDist.y);
                tvState.setText(String.format(getString(R.string.coordinate), originalX, originalY));

                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (cloneStampSrc == null) {
                    cloneStampSrc = new Point(originalX, originalY);
                    drawCloneStampSrcOnView(originalX, originalY);
                    tvState.setText(String.format(getString(R.string.coordinate), originalX, originalY));
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
    private final View.OnTouchListener onImageViewTouchWithEraserListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                prevX = x;
                prevY = y;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                int originalX = toOriginal(x - tab.translationX), originalY = toOriginal(y - tab.translationY);
                drawLineOnCanvas(
                        toOriginal(prevX - tab.translationX),
                        toOriginal(prevY - tab.translationY),
                        originalX,
                        originalY,
                        eraser);
                drawBitmapOnView();
                tvState.setText(String.format(getString(R.string.coordinate), originalX, originalY));
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
                int originalX = inRange(toOriginal(x - tab.translationX), 0, bitmap.getWidth() - 1),
                        originalY = inRange(toOriginal(y - tab.translationY), 0, bitmap.getHeight() - 1);
                int color = bitmap.getPixel(originalX, originalY);
                paint.setColor(color);
                vForegroundColor.setBackgroundColor(color);
                tvState.setText(String.format(getString(R.string.state_eyedropper),
                        originalX, originalY,
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
                int originalX = toOriginal(x - tab.translationX), originalY = toOriginal(y - tab.translationY);
                int originalPrevX = toOriginal(prevX - tab.translationX), originalPrevY = toOriginal(prevY - tab.translationY);

                int rad = (int) (strokeWidth / 2.0f);
                int left = Math.min(originalPrevX, originalX) - rad,
                        top = Math.min(originalPrevY, originalY) - rad,
                        right = Math.max(originalPrevX, originalX) + rad + 1,
                        bottom = Math.max(originalPrevY, originalY) + rad + 1;
                int width = right - left + 1, height = bottom - top + 1;
                int relativeX = originalX - left, relativeY = originalY - top;
                Rect absolute = new Rect(left, top, right + 1, bottom + 1),
                        relative = new Rect(0, 0, width, height);
                Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas cv = new Canvas(bm);
                cv.drawLine(originalPrevX - left, originalPrevY - top,
                        relativeX, relativeY,
                        paint);
                if (threshold < 0x100 || colorRange != 0b111111) {
                    cv.drawBitmap(bitmapWithoutFilter, absolute, relative, srcIn);
                    Bitmap ft = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    floodFill(bm, ft, relativeX, relativeY, Color.BLACK,
                            threshold, colorRange, false);
                    cv.drawBitmap(ft, 0, 0, srcIn);
                    ft.recycle();
                }
                if (cbFilterClear.isChecked()) {
                    canvas.drawBitmap(bm, left, top, filterClear);
                }
                cv.drawBitmap(bitmapWithoutFilter, absolute, relative, filter);
                canvas.drawBitmap(bm, left, top, paint);
                bm.recycle();

                drawBitmapOnView();
                tvState.setText(String.format(getString(R.string.coordinate), originalX, originalY));
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
        int originalX = toOriginal(x - tab.translationX), originalY = toOriginal(y - tab.translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                paint.setStrokeWidth(toScaled((int) paint.getStrokeWidth()));
                if (isShapeStopped) {
                    isShapeStopped = false;
                    drawPointOnView(originalX, originalY);
                    shapeStartX = originalX;
                    shapeStartY = originalY;
                    gradientColor0 =
                            bitmap.getPixel(inRange(originalX, 0, bitmap.getWidth() - 1),
                                    inRange(originalY, 0, bitmap.getHeight() - 1));
                    tvState.setText(String.format(getString(R.string.coordinate), originalX, originalY));
                    break;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                float startX = tab.translationX + toScaled(shapeStartX + 0.5f),
                        startY = tab.translationY + toScaled(shapeStartY + 0.5f),
                        stopX = tab.translationX + toScaled(originalX + 0.5f),
                        stopY = tab.translationY + toScaled(originalY + 0.5f);
                paint.setShader(new LinearGradient(startX, startY, stopX, stopY,
                        gradientColor0,
                        bitmap.getPixel(inRange(originalX, 0, bitmap.getWidth() - 1),
                                inRange(originalY, 0, bitmap.getHeight() - 1)),
                        Shader.TileMode.CLAMP));
                clearCanvas(previewCanvas);
                previewCanvas.drawLine(startX, startY, stopX, stopY, paint);
                ivPreview.invalidate();
                tvState.setText(String.format(getString(R.string.state_start_stop),
                        shapeStartX, shapeStartY, originalX, originalY));
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                paint.setStrokeWidth(strokeWidth);
                if (originalX != shapeStartX || originalY != shapeStartY) {
                    paint.setShader(new LinearGradient(shapeStartX, shapeStartY, originalX, originalY,
                            gradientColor0,
                            bitmap.getPixel(inRange(originalX, 0, bitmap.getWidth() - 1),
                                    inRange(originalY, 0, bitmap.getHeight() - 1)),
                            Shader.TileMode.CLAMP));
                    drawLineOnCanvas(shapeStartX, shapeStartY, originalX, originalY, paint);
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
                int originalX = toOriginal(x - tab.translationX), originalY = toOriginal(y - tab.translationY);
                drawLineOnCanvas(
                        toOriginal(prevX - tab.translationX),
                        toOriginal(prevY - tab.translationY),
                        originalX,
                        originalY,
                        paint);
                drawBitmapOnView();
                tvState.setText(String.format(getString(R.string.coordinate), originalX, originalY));
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
                            selectionEndX = toOriginal(x - tab.translationX);
                            selectionEndY = toOriginal(y - tab.translationY);
                        } else {
                            hasSelection = true;
                            selectionStartX = toOriginal(x - tab.translationX);
                            selectionStartY = toOriginal(y - tab.translationY);
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
                    selectionEndX = toOriginal(x - tab.translationX);
                    selectionEndY = toOriginal(y - tab.translationY);
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
        int originalX = toOriginal(x - tab.translationX), originalY = toOriginal(y - tab.translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                paint.setStrokeWidth(toScaled((int) paint.getStrokeWidth()));
                if (isShapeStopped) {
                    isShapeStopped = false;
                    drawPointOnView(originalX, originalY);
                    shapeStartX = originalX;
                    shapeStartY = originalY;
                    tvState.setText(String.format(getString(R.string.coordinate), originalX, originalY));
                    break;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                String result = drawShapeOnView(shapeStartX, shapeStartY, originalX, originalY);
                tvState.setText(
                        String.format(getString(R.string.state_start_stop_),
                                shapeStartX, shapeStartY, originalX, originalY)
                                + result);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                paint.setStrokeWidth(strokeWidth);
                if (originalX != shapeStartX || originalY != shapeStartY) {
                    drawShapeOnCanvas(shapeStartX, shapeStartY, originalX, originalY);
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
                    textX = toOriginal(x - prevX);
                    textY = toOriginal(y - prevY);
                    drawTextOnView();
                    break;
                }
            }

        } else {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    textX = toOriginal(event.getX() - tab.translationX);
                    textY = toOriginal(event.getY() - tab.translationY);
                    llOptionsText.setVisibility(View.VISIBLE);
                    isEditingText = true;
                    scaleTextSizeAndDrawTextOnView();
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
                                selection.left -= toOriginal(transfromeeDpb.left - dpb.left);
                                selection.right += toOriginal(transfromeeDpb.right - dpb.right);
                                double width = selection.width() + 1, height = width / transformer.getAspectRatio();
                                selection.top = (int) (transformer.getCenterY() - height / 2.0);
                                selection.bottom = (int) (transformer.getCenterY() + height / 2.0);
                                scaledSelection.top = tab.translationY + toScaled(selection.top);
                                scaledSelection.bottom = tab.translationY + toScaled(selection.bottom);
                                transfromeeDpb.top = Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top);
                                transfromeeDpb.bottom = Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1);
                            } else {
                                selection.top -= toOriginal(transfromeeDpb.top - dpb.top);
                                selection.bottom += toOriginal(transfromeeDpb.bottom - dpb.bottom);
                                double height = selection.height() + 1, width = height * transformer.getAspectRatio();
                                selection.left = (int) (transformer.getCenterX() - width / 2.0);
                                selection.right = (int) (transformer.getCenterX() + width / 2.0);
                                scaledSelection.left = tab.translationX + toScaled(selection.left);
                                scaledSelection.right = tab.translationX + toScaled(selection.right);
                                transfromeeDpb.left = Math.min(x0 - scaledSelection.left, x1 - scaledSelection.left);
                                transfromeeDpb.right = Math.min(scaledSelection.right - x0, scaledSelection.right - x1);
                            }
                        } else {
                            selection.left -= toOriginal(transfromeeDpb.left - dpb.left);
                            selection.top -= toOriginal(transfromeeDpb.top - dpb.top);
                            selection.right += toOriginal(transfromeeDpb.right - dpb.right);
                            selection.bottom += toOriginal(transfromeeDpb.bottom - dpb.bottom);
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
                                toOriginal(x - tab.translationX), toOriginal(y - tab.translationY)));
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float x = event.getX(), y = event.getY();
                        float deltaX = x - prevX, deltaY = y - prevY;
                        tab.translationX += deltaX;
                        tab.translationY += deltaY;
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
                        drawChessboardOnView();
                        drawBitmapOnView();
                        drawGridOnView();
                        if (transformer != null) {
                            drawTransformeeOnViewBySelection();
                        } else if (isEditingText) {
                            scaleTextSizeAndDrawTextOnView();
                        } else if (!isShapeStopped) {
                            drawPointOnView(shapeStartX, shapeStartY);
                        } else if (cloneStampSrc != null) {
                            drawCloneStampSrcOnView(cloneStampSrc.x, cloneStampSrc.y);
                        }
                        drawSelectionOnView();
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
                                toOriginal(x - tab.translationX), toOriginal(y - tab.translationY)));
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
            drawTransformeeOnCanvas();
            drawTextOnCanvas();
            createThresholdBitmap(0x0);
            onToolChange(onImageViewTouchWithBucketListener);
            hsvOptionsBucketFill.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onCloneStampRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithCloneStampListener);
            etCloneStampStrokeWidth.setText(String.valueOf(strokeWidth));
            llOptionsCloneStamp.setVisibility(View.VISIBLE);
        } else {
            cloneStampSrc = null;
            clearCanvasAndInvalidateView(previewCanvas, ivPreview);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onFilterRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            bitmapWithoutFilter = Bitmap.createBitmap(bitmap);
            drawTransformeeOnCanvas();
            drawTextOnCanvas();
            createThresholdBitmap(0x100);
            onToolChange(onImageViewTouchWithFilterListener);
            etFilterStrokeWidth.setText(String.valueOf(strokeWidth));
            hsvOptionsFilter.setVisibility(View.VISIBLE);
        } else {
            bitmapWithoutFilter.recycle();
            bitmapWithoutFilter = null;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onGradientRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithGradientListener);
            cbGradientAntiAlias.setChecked(antiAlias);
            paint.setAntiAlias(antiAlias);
            etGradientStrokeWidth.setText(String.valueOf(strokeWidth));
            etGradientBlurRadius.setText(String.valueOf(blurRadius));
            setBlurRadius(blurRadius);
            llOptionsGradient.setVisibility(View.VISIBLE);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onPencilRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            onToolChange(onImageViewTouchWithPencilListener);
            cbPencilAntiAlias.setChecked(antiAlias);
            paint.setAntiAlias(antiAlias);
            etPencilStrokeWidth.setText(String.valueOf(strokeWidth));
            etPencilBlurRadius.setText(String.valueOf(blurRadius));
            setBlurRadius(blurRadius);
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
            paint.setAntiAlias(antiAlias);
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
            cbTextAntiAlias.setChecked(antiAlias);
            paint.setAntiAlias(antiAlias);
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

    private final OnProgressChangeListener onFilterContrastSeekBarProgressChangeListener = progress -> {
        float scale = progress / 10.0f, shift = 0x80 * (1.0f - scale);
        bitmapWithFilter.setFilter(new ColorMatrix(new float[]{
                scale, 0.0f, 0.0f, 0.0f, shift,
                0.0f, scale, 0.0f, 0.0f, shift,
                0.0f, 0.0f, scale, 0.0f, shift,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        }));
        drawBitmapWithFilterOnView();
        tvState.setText(String.format(getString(R.string.state_contrast), scale));
    };

    private final OnProgressChangeListener onFilterInvertSeekBarProgressChangeListener = progress -> {
        float scale = progress / 10.0f - 1, shift = (1.0f - progress / 20.0f) * 0xFF;
        bitmapWithFilter.setFilter(new ColorMatrix(new float[]{
                scale, 0.0f, 0.0f, 0.0f, shift,
                0.0f, scale, 0.0f, 0.0f, shift,
                0.0f, 0.0f, scale, 0.0f, shift,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        }));
        drawBitmapWithFilterOnView();
        tvState.setText(String.format(getString(R.string.state_invert), scale));
    };

    private final OnProgressChangeListener onFilterLightnessSeekBarProgressChangeListener = progress -> {
        bitmapWithFilter.setFilter(new ColorMatrix(new float[]{
                1.0f, 0.0f, 0.0f, 0.0f, progress,
                0.0f, 1.0f, 0.0f, 0.0f, progress,
                0.0f, 0.0f, 1.0f, 0.0f, progress,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        }));
        drawBitmapWithFilterOnView();
        tvState.setText(String.format(getString(R.string.state_lightness), progress));
    };

    private final OnProgressChangeListener onFilterSaturationSeekBarProgressChangeListener = progress -> {
        float f = progress / 10.0f;
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(f);
        bitmapWithFilter.setFilter(colorMatrix);
        drawBitmapWithFilterOnView();
        tvState.setText(String.format(getString(R.string.state_saturation), f));
    };

    private final CellGridManager.OnUpdateListener onUpdateCellGridListener = this::drawGridOnView;

    private final ImageSizeManager.OnUpdateListener onUpdateImageSizeListener = (width, height, stretch) -> {
        resizeBitmap(width, height, stretch);
        drawBitmapOnView();
        history.offer(bitmap);
    };

    private final View.OnClickListener onFilterButtonClickListener = v -> {
        ColorMatrixManager.make(this,
                        R.string.filter,
                        onPaintFilterChangeListener,
                        null,
                        colorMatrix)
                .show();
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

    private void addBitmap(Bitmap bitmap, int width, int height) {
        addBitmap(bitmap,
                width, height,
                null, getString(R.string.untitled), null);
    }

    private void addBitmap(Bitmap bitmap,
                           int width, int height,
                           String path, String title, Bitmap.CompressFormat compressFormat) {
        tab = new Tab();
        tabs.add(tab);
        tab.bitmap = bitmap;
        currentTabIndex = tabs.size() - 1;
        history = new BitmapHistory();
        tab.history = history;
        history.offer(bitmap);
        tab.path = path;
        tab.compressFormat = compressFormat;
        cellGrid = new CellGrid();
        tab.cellGrid = cellGrid;

        tab.scale = (float) ((double) viewWidth / (double) width);
        imageWidth = (int) toScaled(width);
        imageHeight = (int) toScaled(height);
        tab.translationX = 0.0f;
        tab.translationY = 0.0f;

        if (transformer != null) {
            transformer.recycle();
            transformer = null;
        }
        hasSelection = false;

        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
        drawSelectionOnView();

        tabLayout.addTab(tabLayout.newTab().setText(title).setTag(bitmap));
        tabLayout.getTabAt(currentTabIndex).select();
    }

    private void bucketFill(Bitmap bitmap, int x, int y, @ColorInt final int color) {
        bucketFill(bitmap, x, y, color, 0, 0b111111, false);
    }

    private void bucketFill(Bitmap bitmap, int x, int y, @ColorInt final int color,
                            final int threshold, final int colorRange, final boolean keepColorDiff) {
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
        final float[] hsv0 = new float[3], hsv0_ = new float[3];
        int hi = 0;
        float f = 0.0f;
        float alpha = 0.0f;
        if (keepColorDiff) {
            Color.colorToHSV(pixel, hsv0);
            Color.colorToHSV(color, hsv0_);
            hi = (int) (hsv0_[0] / 60.0f);
            f = hsv0_[0] / 60.0f - hi;
            alpha = Color.alpha(color) / 255.0f;
        }
        int i = 0;
        for (y = top; y <= bottom; ++y) {
            for (x = left; x <= right; ++i, ++x) {
                int px = pixels[i];
                boolean match = false;
                int c = color;
                if (threshold > 0) {
                    if ((colorRange == 0b111111 || (getColorRangeOf(px) | colorRange) == colorRange)) {
                        int r = Color.red(px), g = Color.green(px), b = Color.blue(px);
                        match = checkColorIsWithinThreshold(
                                Color.red(pixel), Color.green(pixel), Color.blue(pixel),
                                r, g, b);
                        if (match && keepColorDiff) {
                            c = fillButKeepColorDiff(r / 255.0f, g / 255.0f, b / 255.0f,
                                    hsv0[1], hsv0[2], hsv0_[1], hsv0_[2],
                                    f, hi, alpha);
                        }
                    }
                } else {
                    match = px == pixel;
                }
                if (match) {
                    pixels[i] = c;
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

    private void createBitmapWithFilter() {
        if (!hasSelection) {
            selectAll();
        }
        bitmapWithFilter = new BitmapWithFilter(bitmap, selection);
    }

    private void createGraphic(int width, int height) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        addBitmap(bitmap, width, height);
    }

    private void createThresholdBitmap(int threshold) {
        if (thresholdBitmap != null) {
            thresholdBitmap.recycle();
        }
        if (!hasSelection) {
            selectAll();
        }
        thresholdBitmap = new BitmapWithFilter(bitmap, selection);
        this.threshold = threshold;
    }

    private void dragBound(float viewX, float viewY) {
        float halfScale = tab.scale / 2.0f;
        switch (draggingBound) {
            case LEFT: {
                int left = toOriginal(viewX - tab.translationX + halfScale);
                if (left != selection.left) selection.left = left;
                else return;
                break;
            }
            case TOP: {
                int top = toOriginal(viewY - tab.translationY + halfScale);
                if (top != selection.top) selection.top = top;
                else return;
                break;
            }
            case RIGHT: {
                int right = toOriginal(viewX - tab.translationX + halfScale) - 1;
                if (right != selection.right) selection.right = right;
                else return;
                break;
            }
            case BOTTOM: {
                int bottom = toOriginal(viewY - tab.translationY + halfScale) - 1;
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
        int startX = translX >= 0.0f ? 0 : toOriginal(-translX);
        int startY = translY >= 0.0f ? 0 : toOriginal(-translY);
        int endX = Math.min(toOriginal(translX + scaledBitmapW <= viewWidth ? scaledBitmapW : viewWidth - translX) + 1, bitmapWidth);
        int endY = Math.min(toOriginal(translY + scaledBitmapH <= viewHeight ? scaledBitmapH : viewHeight - translY) + 1, bitmapHeight);
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
                    opaquePaint);
        }
    }

    private void drawBitmapOnView() {
        clearCanvas(viewCanvas);
        drawBitmapOnCanvas(bitmap, tab.translationX, tab.translationY, viewCanvas);
        imageView.invalidate();
    }

    private void drawBitmapWithFilterOnCanvas() {
        canvas.drawBitmap(bitmapWithFilter.getBitmap(), 0.0f, 0.0f, opaquePaint);
        drawBitmapOnView();
        history.offer(bitmap);
    }

    private void drawBitmapWithFilterOnView() {
        drawBitmapWithFilterOnView(bitmapWithFilter);
    }

    private void drawBitmapWithFilterOnView(BitmapWithFilter bitmapWithFilter) {
        clearCanvas(viewCanvas);
        drawBitmapOnCanvas(bitmapWithFilter.getBitmap(), tab.translationX, tab.translationY, viewCanvas);
        imageView.invalidate();
        tvState.setText("");
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
                opaquePaint);

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
        canvas.drawLine(x - 100.0f, y, x + 100.0f, y, pointPaint);
        canvas.drawLine(x, y - 100.0f, x, y + 100.0f, pointPaint);
        canvas.drawText(text, x, y, pointPaint);
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
        int originalX = (int) (-tab.translationX / scaledMultiplier) * multiplier;
        for (;
             x < viewWidth;
             x += scaledMultiplier, originalX += multiplier) {
            rulerHCanvas.drawLine(x, 0.0f, x, height, rulerPaint);
            rulerHCanvas.drawText(String.valueOf(originalX), x, height, rulerPaint);
        }
        float y = tab.translationY % scaledMultiplier, width = rulerVBitmap.getWidth();
        int originalY = (int) (-tab.translationY / scaledMultiplier) * multiplier;
        float ascent = rulerPaint.getFontMetrics().ascent;
        for (;
             y < viewHeight;
             y += scaledMultiplier, originalY += multiplier) {
            rulerVCanvas.drawLine(0.0f, y, width, y, rulerPaint);
            rulerVCanvas.drawText(String.valueOf(originalY), 0.0f, y - ascent, rulerPaint);
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
        try {
            paint.setTextSize(Float.parseFloat(etTextSize.getText().toString()));
        } catch (NumberFormatException e) {
        }
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
                canvas.drawBitmap(transformer.getBitmap(), selection.left, selection.top, blackPaint);
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
            selection.left = toOriginal(transformer.getTranslationX() - tab.translationX);
            selection.top = toOriginal(transformer.getTranslationY() - tab.translationY);
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

    private int fillButKeepColorDiff(float r, float g, float b, float s0, float v0, float s0_,
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
        floodFill(bitmap, bitmap, x, y, color, 0, 0b111111, false);
    }

    private void floodFill(final Bitmap src, final Bitmap dst, int x, int y,
                           @ColorInt final int color,
                           final int threshold, final int colorRange, final boolean keepColorDiff) {
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
        final float[] hsv0 = new float[3], hsv0_ = new float[3];
        int hi = 0;
        float f = 0.0f;
        float alpha = 0.0f;
        if (keepColorDiff) {
            Color.colorToHSV(pixel, hsv0);
            Color.colorToHSV(color, hsv0_);
            hi = (int) (hsv0_[0] / 60.0f);
            f = hsv0_[0] / 60.0f - hi;
            alpha = Color.alpha(color) / 255.0f;
        }
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
            int c = color;
            if (threshold > 0) {
                if ((colorRange == 0b111111 || (getColorRangeOf(px) | colorRange) == colorRange)) {
                    int r = Color.red(px), g = Color.green(px), b = Color.blue(px);
                    match = checkColorIsWithinThreshold(
                            Color.red(pixel), Color.green(pixel), Color.blue(pixel),
                            r, g, b);
                    if (match && keepColorDiff) {
                        c = fillButKeepColorDiff(r / 255.0f, g / 255.0f, b / 255.0f,
                                hsv0[1], hsv0[2], hsv0_[1], hsv0_[2],
                                f, hi, alpha);
                    }
                }
            } else {
                match = px == pixel;
            }
            if (match) {
                srcPixels[i] = c;
                if (src != dst) {
                    dstPixels[i] = c;
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
        currentTabIndex = 0;
        canvas = new Canvas(bitmap);
        history = new BitmapHistory();
        tab.history = history;
        history.offer(bitmap);
        tab.path = null;
        cellGrid = new CellGrid();
        tab.cellGrid = cellGrid;

        tab.scale = 20.0f;
        imageWidth = 960;
        imageHeight = 960;
        tab.translationX = 0.0f;
        tab.translationY = 0.0f;

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
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cbBucketFillContiguous = findViewById(R.id.cb_bucket_fill_contiguous);
        cbBucketFillKeepColorDiff = findViewById(R.id.cb_bucket_fill_keep_color_diff);
        cbFilterClear = findViewById(R.id.cb_filter_clear);
        cbGradientAntiAlias = findViewById(R.id.cb_gradient_anti_alias);
        cbPencilAntiAlias = findViewById(R.id.cb_pencil_anti_alias);
        cbShapeAntiAlias = findViewById(R.id.cb_shape_anti_alias);
        cbShapeFill = findViewById(R.id.cb_shape_fill);
        cbTextAntiAlias = findViewById(R.id.cb_text_anti_alias);
        cbTextFill = findViewById(R.id.cb_text_fill);
        cbTransformerLar = findViewById(R.id.cb_transformer_lar);
        cbZoom = findViewById(R.id.cb_zoom);
        etCloneStampStrokeWidth = findViewById(R.id.et_clone_stamp_stroke_width);
        etEraserStrokeWidth = findViewById(R.id.et_eraser_stroke_width);
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
        hsvOptionsFilter = findViewById(R.id.hsv_options_filter);
        imageView = findViewById(R.id.iv);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        ivChessboard = findViewById(R.id.iv_chessboard);
        ivGrid = findViewById(R.id.iv_grid);
        ivPreview = findViewById(R.id.iv_preview);
        ivRulerH = findViewById(R.id.iv_ruler_horizontal);
        ivRulerV = findViewById(R.id.iv_ruler_vertical);
        ivSelection = findViewById(R.id.iv_selection);
        llOptionsCloneStamp = findViewById(R.id.ll_options_clone_stamp);
        llOptionsEraser = findViewById(R.id.ll_options_eraser);
        llOptionsGradient = findViewById(R.id.ll_options_gradient);
        llOptionsPencil = findViewById(R.id.ll_options_pencil);
        llOptionsShape = findViewById(R.id.ll_options_shape);
        llOptionsText = findViewById(R.id.ll_options_text);
        llOptionsTransformer = findViewById(R.id.ll_options_transformer);
        rvSwatches = findViewById(R.id.rv_swatches);
        rbBucketFill = findViewById(R.id.rb_bucket_fill);
        rbCloneStamp = findViewById(R.id.rb_clone_stamp);
        rbFilter = findViewById(R.id.rb_filter);
        RadioButton rbPencil = findViewById(R.id.rb_pencil);
        rbTransformer = findViewById(R.id.rb_transformer);
        tabLayout = findViewById(R.id.tl);
        tvState = findViewById(R.id.tv_state);
        vBackgroundColor = findViewById(R.id.v_background_color);
        vForegroundColor = findViewById(R.id.v_foreground_color);

        findViewById(R.id.b_bucket_fill_color_range).setOnClickListener(onColorRangeButtonClickListener);
        findViewById(R.id.b_bucket_fill_threshold).setOnClickListener(onThresholdButtonClickListener);
        findViewById(R.id.b_clone_stamp_src).setOnClickListener(onCloneStampSrcButtonClickListener);
        findViewById(R.id.b_color_filter).setOnClickListener(onFilterButtonClickListener);
        findViewById(R.id.b_filter_color_range).setOnClickListener(onColorRangeButtonClickListener);
        findViewById(R.id.b_filter_threshold).setOnClickListener(onThresholdButtonClickListener);
        findViewById(R.id.b_text_draw).setOnClickListener(v -> drawTextOnCanvas());
        ((CompoundButton) findViewById(R.id.cb_eraser_anti_alias)).setOnCheckedChangeListener((buttonView, isChecked) -> eraser.setAntiAlias(isChecked));
        cbGradientAntiAlias.setOnCheckedChangeListener((buttonView, isChecked) -> setAntiAlias(isChecked));
        cbPencilAntiAlias.setOnCheckedChangeListener((buttonView, isChecked) -> setAntiAlias(isChecked));
        cbShapeAntiAlias.setOnCheckedChangeListener((buttonView, isChecked) -> setAntiAlias(isChecked));
        cbShapeFill.setOnCheckedChangeListener((buttonView, isChecked) -> paint.setStyle(isChecked ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE));
        cbTextAntiAlias.setOnCheckedChangeListener((buttonView, isChecked) -> setAntiAlias(isChecked));
        cbZoom.setOnCheckedChangeListener(onZoomToolCheckBoxCheckedChangeListener);
        cbZoom.setTag(onImageViewTouchWithPencilListener);
        etCloneStampStrokeWidth.addTextChangedListener((AfterTextChangedListener) this::setStrokeWidth);
        etFilterStrokeWidth.addTextChangedListener((AfterTextChangedListener) this::setStrokeWidth);
        etGradientBlurRadius.addTextChangedListener((AfterTextChangedListener) this::setBlurRadius);
        etGradientStrokeWidth.addTextChangedListener((AfterTextChangedListener) this::setStrokeWidth);
        etPencilBlurRadius.addTextChangedListener((AfterTextChangedListener) this::setBlurRadius);
        etPencilStrokeWidth.addTextChangedListener((AfterTextChangedListener) this::setStrokeWidth);
        etShapeStrokeWidth.addTextChangedListener((AfterTextChangedListener) this::setStrokeWidth);
        etText.addTextChangedListener((AfterTextChangedListener) s -> drawTextOnView());
        etTextSize.addTextChangedListener((AfterTextChangedListener) s -> scaleTextSizeAndDrawTextOnView());
        flImageView.setOnTouchListener(onImageViewTouchWithPencilListener);
        rbBucketFill.setOnCheckedChangeListener(onBucketFillRadioButtonCheckedChangeListener);
        ((CompoundButton) findViewById(R.id.rb_circle)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = circle);
        rbCloneStamp.setOnCheckedChangeListener(onCloneStampRadioButtonCheckedChangeListener);
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
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        findViewById(R.id.tv_color_add).setOnClickListener(onAddSwatchViewClickListener);
        vBackgroundColor.setOnClickListener(onBackgroundColorClickListener);
        vForegroundColor.setOnClickListener(onForegroundColorClickListener);

        cbTextFill.setOnCheckedChangeListener((buttonView, isChecked) -> {
            paint.setStyle(isChecked ? Paint.Style.FILL : Paint.Style.STROKE);
            drawTextOnView();
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

        if (thresholdBitmap != null) {
            thresholdBitmap.recycle();
            thresholdBitmap = null;
        }

        gridCanvas = null;
        gridBitmap.recycle();
        gridBitmap = null;

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
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChange(View.OnTouchListener onImageViewTouchListener) {
        cbZoom.setChecked(false);
        cbZoom.setTag(onImageViewTouchListener);
        flImageView.setOnTouchListener(onImageViewTouchListener);
        hideToolOptions();
        paint.setAntiAlias(false);
        paint.setMaskFilter(null);
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
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.i_about:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.about)
                        .setMessage(R.string.author)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                break;

            case R.id.i_cell_grid: {
                CellGridManager.make(this, cellGrid,
                                onUpdateCellGridListener)
                        .show();
                break;
            }
            case R.id.i_close:
                if (tabs.size() == 1) {
                    break;
                }
                if (transformer != null) {
                    transformer.recycle();
                    transformer = null;
                }
                bitmap.recycle();
                history.recycle();
                tabs.remove(currentTabIndex);
                tabLayout.removeTabAt(currentTabIndex);
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
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
                int width = selection.width() + 1, height = selection.height() + 1;
                Bitmap bm = Bitmap.createBitmap(bitmap, selection.left, selection.top, width, height);
                resizeBitmap(width, height, false);
                canvas.drawBitmap(bm, 0.0f, 0.0f, opaquePaint);
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
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
                hasSelection = false;
                clearCanvasAndInvalidateView(selectionCanvas, ivSelection);
                recreateThresholdBitmap();
                tvState.setText("");
                break;

            case R.id.i_filter_channels:
                createBitmapWithFilter();
                new ChannelsDialog(this)
                        .setOnCancelListener(onFilterCancelListener)
                        .setOnMatrixChangeListener(onColorMatrixChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_contrast:
                createBitmapWithFilter();
                new SeekBarDialog(this).setTitle(R.string.contrast).setMin(0).setMax(100).setProgress(10)
                        .setOnProgressChangeListener(onFilterContrastSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_invert:
                createBitmapWithFilter();
                new SeekBarDialog(this).setTitle(R.string.invert).setMin(0).setMax(20).setProgress(20)
                        .setOnProgressChangeListener(onFilterInvertSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_lightness:
                createBitmapWithFilter();
                new SeekBarDialog(this).setTitle(R.string.lightness).setMin(-0xFF).setMax(0xFF).setProgress(0)
                        .setOnProgressChangeListener(onFilterLightnessSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_matrix:
                createBitmapWithFilter();
                ColorMatrixManager
                        .make(this,
                                R.string.custom,
                                onColorMatrixChangeListener,
                                onFilterConfirmListener,
                                onFilterCancelListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_threshold:
                createBitmapWithFilter();
                new ThresholdDialog(this)
                        .setOnCancelListener(onFilterCancelListener)
                        .setOnMatrixChangeListener(onColorMatrixChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_filter_saturation:
                createBitmapWithFilter();
                new SeekBarDialog(this).setTitle(R.string.saturation).setMin(0).setMax(100).setProgress(10)
                        .setOnProgressChangeListener(onFilterSaturationSeekBarProgressChangeListener)
                        .setOnPositiveButtonClickListener(onFilterConfirmListener)
                        .setOnCancelListener(onFilterCancelListener)
                        .show();
                tvState.setText("");
                break;

            case R.id.i_flip_horizontally: {
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
                scale(-1.0f, 1.0f, false);
                break;
            }
            case R.id.i_flip_vertically: {
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
                scale(1.0f, -1.0f, false);
                break;
            }
            case R.id.i_homepage:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.homepage)
                        .setMessage(R.string.homepage_address)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                break;

            case R.id.i_merge_with_gray: {
                if (currentTabIndex + 1 >= tabs.size()) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.exception_merge_as_hidden)
                            .setPositiveButton(R.string.ok, null)
                            .setTitle(R.string.merge_as_hidden)
                            .show();
                    break;
                }
                HiddenImageMaker.merge(this,
                        new Bitmap[]{bitmap, tabs.get(currentTabIndex + 1).bitmap},
                        onFinishMakingHiddenImageListener);
                break;
            }
            case R.id.i_new: {
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
                new NewGraphicPropertiesDialog(this)
                        .setOnFinishSettingListener(onFinishSettingNewGraphicPropertiesListener)
                        .show();
                break;
            }
            case R.id.i_open:
                drawTransformeeOnCanvas();
                drawTextOnCanvas();

                getImage.launch("image/*");
                break;

            case R.id.i_paste:
                if (clipboard == null) {
                    break;
                }
                drawTransformeeOnCanvas();
                drawTextOnCanvas();

                selection.left = tab.translationX >= 0.0f ? 0 : toOriginal(-tab.translationX) + 1;
                selection.top = tab.translationY >= 0.0f ? 0 : toOriginal(-tab.translationY) + 1;
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
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
                rotate(90.0f, false);
                break;

            case R.id.i_rotate_180:
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
                rotate(180.0f, false);
                break;

            case R.id.i_rotate_270:
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
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
                recreateThresholdBitmap();
                tvState.setText("");
                break;

            case R.id.i_size: {
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
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
        }
        return super.onOptionsItemSelected(item);
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
        canvas.drawBitmap(bm, 0.0f, 0.0f, opaquePaint);
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
                width, height,
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
        if (selection.left < bitmapWidth && selection.top < bitmapHeight
                && selection.right >= 0 && selection.bottom >= 0) {
            selection.left = Math.max(0, selection.left);
            selection.top = Math.max(0, selection.top);
            selection.right = Math.min(bitmapWidth - 1, selection.right);
            selection.bottom = Math.min(bitmapHeight - 1, selection.bottom);
            if (selection.left > selection.right) {
                int i = selection.left;
                selection.left = selection.right;
                selection.right = i;
            }
            if (selection.top > selection.bottom) {
                int i = selection.top;
                selection.top = selection.bottom;
                selection.bottom = i;
            }
        } else {
            hasSelection = false;
        }
    }

    private void recreateThresholdBitmap() {
        if (thresholdBitmap != null) {
            if (rbFilter.isChecked()) {
                bitmapWithoutFilter.recycle();
                bitmapWithoutFilter = Bitmap.createBitmap(bitmap);
            }
            createThresholdBitmap(threshold);
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
                    opaquePaint);
        } else {
            cv.drawRect(0.0f, 0.0f, width, height, eraser);
            cv.drawBitmap(bitmap, 0.0f, 0.0f, opaquePaint);
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
        canvas.drawBitmap(bm, left, top, opaquePaint);
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

        drawTransformeeOnCanvas();
        drawTextOnCanvas();

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
        canvas.drawBitmap(bm, left, top, opaquePaint);
        bm.recycle();
        drawBitmapOnView();
        history.offer(bitmap);
    }

    private void scaleTextSizeAndDrawTextOnView() {
        try {
            paint.setTextSize(toScaled((int) Float.parseFloat(etTextSize.getText().toString())));
        } catch (NumberFormatException e) {
        }
        drawTextOnView();
    }

    private void selectAll() {
        selection.left = 0;
        selection.top = 0;
        selection.right = bitmap.getWidth() - 1;
        selection.bottom = bitmap.getHeight() - 1;
    }

    private void setAntiAlias(boolean b) {
        antiAlias = b;
        paint.setAntiAlias(b);
    }

    private void setBlurRadius(String s) {
        try {
            float f = Float.parseFloat(s);
            blurRadius = f;
            setBlurRadius(f);
        } catch (NumberFormatException e) {
        }
    }

    private void setBlurRadius(float f) {
        paint.setMaskFilter(f > 0.0f ? new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL) : null);
    }

    private void setStrokeWidth(String s) {
        try {
            float f = Float.parseFloat(s);
            strokeWidth = f;
            paint.setStrokeWidth(f);
        } catch (NumberFormatException e) {
        }
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

    private int toOriginal(float scaled) {
        return (int) (scaled / tab.scale);
    }

    private float toScaled(int original) {
        return original * tab.scale;
    }

    private float toScaled(float original) {
        return original * tab.scale;
    }

    private void undoOrRedo(Bitmap bm) {
        optimizeSelection();
        bitmap.recycle();
        bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        tab.bitmap = bitmap;
        canvas = new Canvas(bitmap);
        canvas.drawBitmap(bm, 0.0f, 0.0f, opaquePaint);

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