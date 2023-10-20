package com.misterchan.iconeditor.ui;

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
import android.graphics.BitmapShader;
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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorLong;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuCompat;
import androidx.core.view.OneShotPreDrawListener;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.dialog.BitmapConfigModifier;
import com.misterchan.iconeditor.tool.BrushTool;
import com.misterchan.iconeditor.CellGrid;
import com.misterchan.iconeditor.Color;
import com.misterchan.iconeditor.DrawingPrimitivePreview;
import com.misterchan.iconeditor.EditPreview;
import com.misterchan.iconeditor.FloatingLayer;
import com.misterchan.iconeditor.Frame;
import com.misterchan.iconeditor.Guide;
import com.misterchan.iconeditor.Layer;
import com.misterchan.iconeditor.Layers;
import com.misterchan.iconeditor.Project;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Reference;
import com.misterchan.iconeditor.tool.MagicEraser;
import com.misterchan.iconeditor.tool.Ruler;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.tool.SelectionTool;
import com.misterchan.iconeditor.tool.Shape;
import com.misterchan.iconeditor.tool.TextTool;
import com.misterchan.iconeditor.tool.Transformer;
import com.misterchan.iconeditor.colorpicker.RgbColorPicker;
import com.misterchan.iconeditor.databinding.ActivityMainBinding;
import com.misterchan.iconeditor.databinding.FrameListBinding;
import com.misterchan.iconeditor.databinding.LayerListBinding;
import com.misterchan.iconeditor.dialog.AnimationClipper;
import com.misterchan.iconeditor.dialog.CellGridManager;
import com.misterchan.iconeditor.dialog.ColorBalanceDialog;
import com.misterchan.iconeditor.dialog.ColorMatrixManager;
import com.misterchan.iconeditor.dialog.ColorRangeDialog;
import com.misterchan.iconeditor.dialog.CurvesDialog;
import com.misterchan.iconeditor.dialog.DirectorySelector;
import com.misterchan.iconeditor.dialog.EditNumberDialog;
import com.misterchan.iconeditor.dialog.FillWithRefDialog;
import com.misterchan.iconeditor.dialog.GuideEditor;
import com.misterchan.iconeditor.dialog.HiddenImageMaker;
import com.misterchan.iconeditor.dialog.HsvDialog;
import com.misterchan.iconeditor.dialog.ImageSizeManager;
import com.misterchan.iconeditor.dialog.LevelsDialog;
import com.misterchan.iconeditor.dialog.LightingDialog;
import com.misterchan.iconeditor.dialog.MatrixManager;
import com.misterchan.iconeditor.dialog.NewImageDialog;
import com.misterchan.iconeditor.dialog.NoiseGenerator;
import com.misterchan.iconeditor.dialog.QualityManager;
import com.misterchan.iconeditor.dialog.SliderDialog;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnButtonCheckedListener;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;
import com.misterchan.iconeditor.util.BitmapUtils;
import com.misterchan.iconeditor.util.CanvasUtils;
import com.misterchan.iconeditor.util.RunnableRunnable;
import com.misterchan.iconeditor.util.FileUtils;
import com.waynejo.androidndkgif.GifDecoder;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SelectionTool.CoordinateConversions {

    private static final Looper MAIN_LOOPER = Looper.getMainLooper();

    private static final Paint PAINT_BITMAP = BitmapUtils.PAINT_SRC;

    private static final Paint PAINT_BITMAP_OVER = BitmapUtils.PAINT_SRC_OVER;

    private static final Paint PAINT_CELL_GRID = new Paint() {
        {
            setColor(Color.RED);
            setStrokeWidth(2.0f);
        }
    };

    private static final Paint PAINT_CLEAR = BitmapUtils.PAINT_CLEAR;

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

    private ActionMode textActionMode;
    private ActionMode transformerActionMode;
    private ActivityMainBinding activityMain;
    private Bitmap bitmap;
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
    private boolean hasNotLoaded = true;
    private boolean hasSelection = false;
    private boolean isEditingText = false;
    private boolean isShapeStopped = true;
    private boolean isZoomingEnabled = false;
    private BottomSheetDialog bsdFrameList;
    private final BrushTool brush = new BrushTool();
    private Canvas canvas;
    private Canvas chessboardCanvas;
    private Canvas gridCanvas;
    private Canvas previewCanvas;
    private Canvas rulerHCanvas, rulerVCanvas;
    private Canvas selectionCanvas;
    private Canvas viewCanvas;
    private ColorAdapter colorAdapter;
    private final DirectorySelector dirSelector = new DirectorySelector(this);
    private final DrawingPrimitivePreview dpPreview = new DrawingPrimitivePreview();
    private EditPreview editPreview;
    private float backgroundScaledW, backgroundScaledH;
    private float blurRadius = 0.0f, blurRadiusEraser = 0.0f;
    private float scale;
    private float softness = 0.5f;
    private float strokeWidth = 1.0f, eraserStrokeHalfWidth = 0.5f;
    private float textSize = 12.0f;
    private float translationX, translationY;
    private Frame frame;
    private FrameListBinding frameList;
    private InputMethodManager inputMethodManager;
    private int rulerHHeight, rulerVWidth;
    private int threshold;
    private int viewWidth, viewHeight;
    private Layer layer;
    private LayerListBinding layerList;
    private LinkedList<Long> palette;
    private List<Project> projects;
    private final MagicEraser magEr = new MagicEraser();
    private MenuItem miFrameList;
    private MenuItem miHasAlpha;
    private Point cloneStampSrc;
    private Project project;
    private final Reference ref = new Reference();
    private final Ruler ruler = new Ruler();
    private final SelectionTool selection = new SelectionTool(this);
    private SideSheetDialog ssdLayerList;
    private Paint.Style style = Paint.Style.FILL_AND_STROKE;
    private final TextTool text = new TextTool();
    private Thread thread = new Thread();
    private final Transformer transformer = new Transformer();
    private Uri fileToOpen;
    private View vContent;

    private final Paint bitmapPaint = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
        }
    };

    private final Paint chessboardPaint = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setFilterBitmap(false);
        }
    };

    private final Paint eraser = new Paint() {
        {
            setAntiAlias(false);
            setBlendMode(BlendMode.SRC);
            setColor(Color.TRANSPARENT);
            setDither(false);
            setStrokeCap(Cap.ROUND);
            setStrokeJoin(Join.ROUND);
            setStrokeWidth(1.0f);
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

    private final AfterTextChangedListener onBlurRadiusETTextChangedListener = s -> {
        final float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        blurRadius = f;
        setBlurRadius(paint, f);
    };

    private final AfterTextChangedListener onEraserBlurRadiusETTextChangedListener = s -> {
        final float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        blurRadiusEraser = f;
        setBlurRadius(eraser, f);
    };

    private final AfterTextChangedListener onSoftnessETTextChangedListener = s -> {
        try {
            softness = Float.parseFloat(s);
        } catch (NumberFormatException e) {
        }
    };

    private final AfterTextChangedListener onStrokeWidthETTextChangedListener = s -> {
        final float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        strokeWidth = f;
        paint.setStrokeWidth(f);
    };

    private final AfterTextChangedListener onEraserStrokeWidthETTextChangedListener = s -> {
        final float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        eraserStrokeHalfWidth = f / 2.0f;
        eraser.setStrokeWidth(f);
    };

    private final AfterTextChangedListener onSoftStrokeWidthETTextChangedListener = s -> {
        try {
            strokeWidth = Float.parseFloat(s);
        } catch (NumberFormatException e) {
        }
    };

    private final AfterTextChangedListener onTextSizeETTextChangedListener = s -> {
        final float f;
        try {
            f = Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return;
        }
        textSize = f;
        paint.setTextSize(f);
        drawTextOntoView();
    };

    private final AfterTextChangedListener onTransformerMeshSizeETTextChangedListener = s -> {
        if (!hasSelection) {
            return;
        }
        if (!transformer.isRecycled()) {
            transformer.apply();
        }
        createTransformerMesh();
        drawSelectionOntoView();
    };

    private final ActionMode.Callback onTextActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.action_mode_text, menu);
            menu.setGroupDividerEnabled(true);

            final MenuItem miTypefaceBold = menu.findItem(R.id.i_typeface_bold);
            final MenuItem miTypefaceItalic = menu.findItem(R.id.i_typeface_italic);
            final MenuItem miUnderlined = menu.findItem(R.id.i_underlined);
            final MenuItem miStrikeThru = menu.findItem(R.id.i_strike_thru);
            final MenuItem miAlignLeft = menu.findItem(R.id.i_align_left);
            final MenuItem miAlignCenter = menu.findItem(R.id.i_align_center);
            final MenuItem miAlignRight = menu.findItem(R.id.i_align_right);
            final Typeface typeface = paint.getTypeface();

            miTypefaceBold.setChecked(typeface != null && typeface.isBold());
            miTypefaceItalic.setChecked(typeface != null && typeface.isItalic());
            miUnderlined.setChecked(paint.isUnderlineText());
            miStrikeThru.setChecked(paint.isStrikeThruText());

            (switch (paint.getTextAlign()) {
                case LEFT -> miAlignLeft;
                case CENTER -> miAlignCenter;
                case RIGHT -> miAlignRight;
            }).setChecked(true);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        @SuppressLint({"NonConstantResourceId", "WrongConstant"})
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                default -> {
                    return false;
                }
                case R.id.i_typeface_default -> setTypeface(Typeface.DEFAULT);
                case R.id.i_typeface_default_bold -> setTypeface(Typeface.DEFAULT_BOLD);
                case R.id.i_typeface_sans_serif -> setTypeface(Typeface.SANS_SERIF);
                case R.id.i_typeface_serif -> setTypeface(Typeface.SERIF);
                case R.id.i_typeface_monospace -> setTypeface(Typeface.MONOSPACE);
                case R.id.i_typeface_bold -> {
                    final boolean checked = !item.isChecked();
                    item.setChecked(checked);
                    final Typeface typeface = paint.getTypeface() != null ? paint.getTypeface() : Typeface.DEFAULT;
                    final int oldStyle = typeface.getStyle();
                    final int newStyle = checked ? oldStyle | Typeface.BOLD : oldStyle & ~Typeface.BOLD;
                    paint.setTypeface(Typeface.create(typeface, newStyle));
                    drawTextOntoView();
                }
                case R.id.i_typeface_italic -> {
                    final boolean checked = !item.isChecked();
                    item.setChecked(checked);
                    final Typeface typeface = paint.getTypeface() != null ? paint.getTypeface() : Typeface.DEFAULT;
                    final int oldStyle = typeface.getStyle();
                    final int style = checked ? oldStyle | Typeface.ITALIC : oldStyle & ~Typeface.ITALIC;
                    paint.setTypeface(Typeface.create(typeface, style));
                    drawTextOntoView();
                }
                case R.id.i_underlined -> {
                    final boolean checked = !item.isChecked();
                    item.setChecked(checked);
                    paint.setUnderlineText(checked);
                    drawTextOntoView();
                }
                case R.id.i_strike_thru -> {
                    final boolean checked = !item.isChecked();
                    item.setChecked(checked);
                    paint.setStrikeThruText(checked);
                    drawTextOntoView();
                }
                case R.id.i_align_left -> setAlign(item, Paint.Align.LEFT);
                case R.id.i_align_center -> setAlign(item, Paint.Align.CENTER);
                case R.id.i_align_right -> setAlign(item, Paint.Align.RIGHT);
                case R.id.i_ok -> drawTextIntoImage();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (isEditingText) {
                isEditingText = false;
                if (!dpPreview.isRecycled()) {
                    dpPreview.erase();
                    drawBitmapOntoView(true);
                }
                eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
                hideSoftInputFromWindow();
                activityMain.llOptionsText.setVisibility(View.INVISIBLE);
            }
        }

        private void setAlign(MenuItem item, Paint.Align align) {
            item.setChecked(true);
            paint.setTextAlign(align);
            drawTextOntoView();
        }

        private void setTypeface(Typeface typeface) {
            paint.setTypeface(Typeface.create(typeface,
                    paint.getTypeface() != null ? paint.getTypeface().getStyle() : Typeface.NORMAL));
            drawTextOntoView();
        }
    };

    private final ActionMode.Callback onTransformerActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.action_mode_transformer, menu);
            menu.setGroupDividerEnabled(true);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return onTransformerActionItemClick(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (!transformer.isRecycled()) {
                undoOrRedo(layer.history.getCurrent());
            }
        }
    };

    private final BitmapConfigModifier.OnChangedListener onNewConfigBitmapCreatedListener = bitmap -> {
        if (bitmap == null) {
            Snackbar.make(vContent, R.string.this_config_is_not_supported_yet, Snackbar.LENGTH_LONG).show();
            return;
        }
        this.bitmap.recycle();
        this.bitmap = bitmap;
        layer.bitmap = bitmap;
        drawBitmapOntoView(true);
        addToHistory();
    };

    private final CompoundButton.OnCheckedChangeListener onAntiAliasCBCheckedChangeListener = (buttonView, isChecked) -> {
        antiAlias = isChecked;
        paint.setAntiAlias(isChecked);
    };

    private final CompoundButton.OnCheckedChangeListener onFillCBCheckedChangeListener = (buttonView, isChecked) -> {
        style = isChecked ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE;
        paint.setStyle(style);
    };

    private final CompoundButton.OnCheckedChangeListener onTextFillCBCheckedChangeListener = (buttonView, isChecked) -> {
        style = isChecked ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE;
        paint.setStyle(style);
        drawTextOntoView();
    };

    private final CompoundButton.OnCheckedChangeListener onTransformerFilterCheckedChangeListener = (buttonView, isChecked) -> {
        if (activityMain.optionsTransformer.btgTransformer.getCheckedButtonId() == R.id.b_mesh && !transformer.isRecycled() && transformer.mesh != null) {
            transformer.transformMesh(isChecked, antiAlias);
            drawBitmapOntoView(selection.r, true);
        }
    };

    private final DialogInterface.OnClickListener onLayerNameApplyListener = (dialog, which) -> {
        final TextInputEditText tietFileName = ((AlertDialog) dialog).findViewById(R.id.tiet_file_name);
        final String name = tietFileName.getText().toString();
        if (name.length() == 0) {
            return;
        }
        layer.name = name;
        frame.layerAdapter.notifyItemChanged(frame.selectedLayerIndex, LayerAdapter.Payload.NAME);
    };

    private final View.OnClickListener onAddSwatchButtonClickListener = v ->
            RgbColorPicker.make(this, R.string.add,
                            (oldColor, newColor) -> {
                                palette.offerFirst(newColor);
                                colorAdapter.notifyItemInserted(0);
                            },
                            paint.getColorLong())
                    .show();

    private final View.OnClickListener onBackgroundColorClickListener = v ->
            RgbColorPicker.make(this, R.string.background_color,
                            (oldColor, newColor) -> {
                                if (oldColor != null) {
                                    eraser.setColor(newColor);
                                    activityMain.vBackgroundColor.setBackgroundColor(Color.toArgb(newColor));
                                } else {
                                    setPaintColor(newColor, paint.getColorLong());
                                }
                            },
                            eraser.getColorLong(), R.string.swap)
                    .show();

    private final View.OnClickListener onBrushTipShapeButtonClickListener = v -> {
        final int checkedItem = brush.tipShape.ordinal();
        new MaterialAlertDialogBuilder(this).setTitle(R.string.tip_shape)
                .setSingleChoiceItems(R.array.brush_tip_shapes, checkedItem, (dialog, which) -> {
                    brush.tipShape = switch (which) {
                        case 0 -> BrushTool.TipShape.BRUSH;
                        case 1 -> BrushTool.TipShape.REF;
                        default -> BrushTool.TipShape.BRUSH;
                    };
                    updateBrush();
                })
                .setPositiveButton(R.string.ok, null)
                .show();
    };

    private final View.OnClickListener onCloneStampSrcButtonClickListener = v -> {
        cloneStampSrc = null;
        eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
    };

    private final View.OnClickListener onForegroundColorClickListener = v ->
            RgbColorPicker.make(this, R.string.foreground_color,
                            (oldColor, newColor) -> {
                                if (oldColor != null) {
                                    paint.setColor(newColor);
                                    activityMain.vForegroundColor.setBackgroundColor(Color.toArgb(newColor));
                                    onPaintColorChanged();
                                } else {
                                    setPaintColor(eraser.getColorLong(), newColor);
                                }
                            },
                            paint.getColorLong(), R.string.swap)
                    .show();

    private final ColorMatrixManager.OnMatrixElementsChangedListener onFilterColorMatrixChangedListener = matrix -> runOrStart(() -> {
        editPreview.addColorMatrixColorFilter(matrix);
        drawEditPreviewOntoView(true);
    }, true);

    private final ColorRangeDialog.OnChangedListener onLayerColorRangeChangedListener = (colorRange, stopped) -> {
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(getString(R.string.state_color_range,
                colorRange.cuboid[0], colorRange.cuboid[3], colorRange.cuboid[1] * 100.0f, colorRange.cuboid[4] * 100.0f, colorRange.cuboid[2] * 100.0f, colorRange.cuboid[5] * 100.0f));
    };

    private final CurvesDialog.OnCurvesChangedListener onFilterCurvesChangedListener = (curves, stopped) -> runOrStart(() -> {
        final int w = editPreview.getWidth(), h = editPreview.getHeight();
        final int[] src = editPreview.getPixels(), dst = new int[w * h];
        BitmapUtils.applyCurves(src, dst, curves);
        editPreview.setPixels(dst, w, h);
        drawEditPreviewOntoView(stopped);
    }, stopped);

    private final DialogInterface.OnCancelListener onEditPreviewCancelListener = dialog -> {
        drawBitmapOntoView(selection.r, true);
        editPreview.recycle();
        editPreview = null;
        clearStatus();
    };

    private final DialogInterface.OnClickListener onEditPreviewNBClickListener =
            (dialog, which) -> onEditPreviewCancelListener.onCancel(dialog);

    private final DialogInterface.OnClickListener onEditPreviewPBClickListener = (dialog, which) -> {
        drawEditPreviewIntoImage();
        addToHistory();
        clearStatus();
    };

    private final FillWithRefDialog.OnChangeListener onFillWithRefTileModeChangeListener = (bitmap, tileMode, stopped) -> runOrStart(() -> {
        paint.setShader(new BitmapShader(bitmap, tileMode, tileMode));
        editPreview.clearFilters();
        editPreview.getCanvas().drawRect(editPreview.getRect(), paint);
        drawEditPreviewOntoView(true);
    }, stopped);

    private final HiddenImageMaker.OnMakeListener onHiddenImageMakeListener = bitmap -> {
        final Bitmap bm = BitmapUtils.createBitmap(bitmap);
        addProject(bm, activityMain.tlProjectList.getSelectedTabPosition() + 2);
        bitmap.recycle();
    };

    private final HsvDialog.OnHsvChangedListener onFilterHsvChangedListener = (deltaHsv, stopped) -> {
        runOrStart(() -> {
            if (deltaHsv[0] == 0.0f && deltaHsv[1] == 0.0f && deltaHsv[2] == 0.0f) {
                editPreview.clearFilters();
            } else {
                final int w = editPreview.getWidth(), h = editPreview.getHeight();
                final int[] src = editPreview.getPixels(), dst = new int[w * h];
                BitmapUtils.shiftHsv(src, dst, deltaHsv);
                editPreview.setPixels(dst, w, h);
            }
            drawEditPreviewOntoView(stopped);
        }, stopped);
        activityMain.tvStatus.setText(getString(R.string.state_hsv, deltaHsv[0], deltaHsv[1], deltaHsv[2]));
    };

    private final HsvDialog.OnHsvChangedListener onLayerHsvChangedListener = (deltaHsv, stopped) -> {
        layer.deltaHsv = deltaHsv;
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(getString(R.string.state_hsv, deltaHsv[0], deltaHsv[1], deltaHsv[2]));
    };

    private final LevelsDialog.OnLevelsChangedListener onFilterLevelsChangedListener = (inputShadows, inputHighlights, outputShadows, outputHighlights, stopped) -> {
        final float ratio = (outputHighlights - outputShadows) / (inputHighlights - inputShadows);
        runOrStart(() -> {
            editPreview.addLightingColorFilter(ratio, -inputShadows * ratio + outputShadows);
            drawEditPreviewOntoView(stopped);
        }, stopped);
    };

    private final LevelsDialog.OnLevelsChangedListener onLayerLevelsChangedListener = (inputShadows, inputHighlights, outputShadows, outputHighlights, stopped) -> {
        final float ratio = (outputHighlights - outputShadows) / (inputHighlights - inputShadows);
        layer.lighting[0] = layer.lighting[2] = layer.lighting[4] = ratio;
        layer.lighting[1] = layer.lighting[3] = layer.lighting[5] = -inputShadows * ratio + outputShadows;
        drawBitmapOntoView(stopped);
    };

    private final LightingDialog.OnLightingChangedListener onFilterLightingChangedListener = (lighting, stopped) -> runOrStart(() -> {
        editPreview.addLightingColorFilter(lighting);
        drawEditPreviewOntoView(stopped);
    }, stopped);

    private final MatrixManager.OnMatrixElementsChangedListener onMatrixChangedListener = matrix -> runOrStart(() -> {
        editPreview.transform(matrix);
        drawBitmapOntoView(editPreview.getEntire(), true);
    }, true);

    private final NewImageDialog.OnApplyListener onNewImagePropertiesApplyListener = this::createImage;

    private final NoiseGenerator.OnPropChangedListener onNoisePropChangedListener = (properties, stopped) -> {
        runOrStart(() -> {
            if (properties.noisiness() == 0.0f) {
                editPreview.clearFilters();
            } else switch (properties.drawingPrimitive()) {
                case PIXEL -> {
                    if (properties.noisiness() == 1.0f && properties.noRepeats()) {
                        editPreview.drawColor(paint.getColor(), BlendMode.SRC);
                        break;
                    }
                    final int w = editPreview.getWidth(), h = editPreview.getHeight();
                    final int[] pixels = editPreview.getPixels(w, h);
                    BitmapUtils.generateNoise(pixels, paint.getColor(),
                            properties.noisiness(), properties.seed(), properties.noRepeats());
                    editPreview.setPixels(pixels, w, h);
                }
                case POINT -> {
                    if (properties.noisiness() == 1.0f && properties.noRepeats()) {
                        editPreview.drawColor(paint.getColor(), BlendMode.SRC);
                        break;
                    }
                    editPreview.clearFilters();
                    BitmapUtils.generateNoise(editPreview.getCanvas(), editPreview.getRect(), paint,
                            properties.noisiness(), properties.seed(), properties.noRepeats());
                }
                case REF -> {
                    editPreview.clearFilters();
                    BitmapUtils.generateNoise(editPreview.getCanvas(), editPreview.getRect(),
                            !ref.recycled() ? ref.bm() : editPreview.getOriginal(), paint,
                            properties.noisiness(), properties.seed(), properties.noRepeats());
                }
            }
            drawEditPreviewOntoView(stopped);
        }, stopped);
        clearStatus();
    };

    private final OnSliderChangeListener onFilterContrastSliderChangeListener = (slider, value, stopped) -> {
        final float mul = value, add = 0xFF / 2.0f * (1.0f - mul);
        runOrStart(() -> {
            editPreview.addLightingColorFilter(mul, add);
            drawEditPreviewOntoView(stopped);
        }, stopped);
        activityMain.tvStatus.setText(getString(R.string.state_contrast, mul));
    };

    private final OnSliderChangeListener onLayerContrastSliderChangeListener = (slider, value, stopped) -> {
        final float mul = value, add = 0xFF / 2.0f * (1.0f - mul);
        layer.lighting[0] = layer.lighting[2] = layer.lighting[4] = mul;
        layer.lighting[1] = layer.lighting[3] = layer.lighting[5] = add;
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(getString(R.string.state_contrast, mul));
    };

    private final OnSliderChangeListener onFilterHToASliderChangeListener = (slider, value, stopped) -> {
        runOrStart(() -> {
            final int w = editPreview.getWidth(), h = editPreview.getHeight();
            final int[] src = editPreview.getPixels(), dst = new int[w * h];
            BitmapUtils.setAlphaByHue(src, dst, value);
            editPreview.setPixels(dst, w, h);
            drawEditPreviewOntoView(stopped);
        }, stopped);
        activityMain.tvStatus.setText(getString(R.string.state_hue, value));
    };

    private final OnSliderChangeListener onFilterLightnessSliderChangeListener = (slider, value, stopped) -> {
        runOrStart(() -> {
            editPreview.addLightingColorFilter(1.0f, value);
            drawEditPreviewOntoView(stopped);
        }, stopped);
        activityMain.tvStatus.setText(getString(R.string.state_lightness, (int) value));
    };

    private final OnSliderChangeListener onLayerLightnessSliderChangeListener = (slider, value, stopped) -> {
        layer.lighting[1] = layer.lighting[3] = layer.lighting[5] = value;
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(getString(R.string.state_lightness, (int) value));
    };

    @SuppressLint("StringFormatMatches")
    private final OnSliderChangeListener onFilterPosterizationSliderChangeListener = (slider, value, stopped) -> {
        runOrStart(() -> {
            editPreview.posterize((int) value);
            drawEditPreviewOntoView(stopped);
        }, stopped);
        activityMain.tvStatus.setText(String.format(
                getString(R.string.state_posterization, Settings.INST.argbCompFormat()),
                (int) value));
    };

    private final OnSliderChangeListener onFilterSaturationSliderChangeListener = (slider, value, stopped) -> {
        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(value);
        runOrStart(() -> {
            editPreview.addColorMatrixColorFilter(colorMatrix.getArray());
            drawEditPreviewOntoView(stopped);
        }, stopped);
        activityMain.tvStatus.setText(getString(R.string.state_saturation, value));
    };

    private final OnSliderChangeListener onLayerSaturationSliderChangeListener = (slider, value, stopped) -> {
        layer.colorMatrix.setSaturation(value);
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(getString(R.string.state_saturation, value));
    };

    private final OnSliderChangeListener onFilterThresholdSliderChangeListener = (slider, value, stopped) -> {
        final float f = -0x100 * value;
        runOrStart(() -> {
            editPreview.addColorMatrixColorFilter(new float[]{
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            });
            drawEditPreviewOntoView(stopped);
        }, stopped);
        activityMain.tvStatus.setText(getString(R.string.state_threshold, (int) value));
    };

    private final OnSliderChangeListener onLayerThresholdSliderChangeListener = (slider, value, stopped) -> {
        final float[] cm = layer.colorMatrix.getArray();
        cm[4] = cm[9] = cm[14] = -0x100 * value;
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(getString(R.string.state_threshold, (int) value));
    };

    @SuppressLint("StringFormatMatches")
    private final OnSliderChangeListener onLayerAlphaSliderChangeListener = (slider, value, stopped) -> {
        layer.paint.setAlpha((int) value);
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(String.format(
                getString(R.string.state_alpha, Settings.INST.argbCompFormat()),
                (int) value));
    };

    private final OnSliderChangeListener onThresholdChangeListener = (slider, value, stopped) -> {
        threshold = (int) value;
        runOrStart(() -> {
            if (threshold == 0xFF) {
                editPreview.drawColor(Color.BLACK, BlendMode.SRC_IN);
            } else if (threshold == 0x00) {
                editPreview.clearFilters();
            } else {
                editPreview.posterize(0xFF - threshold);
            }
            drawEditPreviewOntoView(stopped);
        }, stopped);
        activityMain.tvStatus.setText(getString(R.string.state_threshold, threshold));
    };

    private final DialogInterface.OnClickListener onThresholdApplyListener = onEditPreviewNBClickListener;

    private final View.OnClickListener onToleranceButtonClickListener = v -> {
        createEditPreview();
        new SliderDialog(this).setTitle(R.string.tolerance).setValueFrom(0x00).setValueTo(0xFF).setValue(threshold)
                .setStepSize(1.0f)
                .setOnChangeListener(onThresholdChangeListener)
                .setOnApplyListener(onThresholdApplyListener)
                .setOnCancelListener(onEditPreviewCancelListener, false)
                .show();
        onThresholdChangeListener.onChange(null, threshold, true);
    };

    private final ItemMovableAdapter.OnItemMoveListener onFrameItemMoveListener = (fromPos, toPos) -> {
        if (project.selectedFrameIndex == fromPos) {
            project.selectedFrameIndex = toPos;
        } else if (project.selectedFrameIndex == toPos) {
            if (fromPos < toPos) {
                --project.selectedFrameIndex;
            } else /* if (fromPos > toPos) */ {
                ++project.selectedFrameIndex;
            }
        }

        frameList.rvFrameList.post(() -> {
            project.frameAdapter.notifyItemRangeChanged(Math.min(fromPos, toPos), Math.abs(toPos - fromPos) + 1);
        });
    };

    private final ItemMovableAdapter.OnItemMoveListener onLayerItemMoveListener = (fromPos, toPos) -> {
        if (frame.selectedLayerIndex == fromPos) {
            frame.selectedLayerIndex = toPos;
        } else if (frame.selectedLayerIndex == toPos) {
            if (fromPos < toPos) {
                --frame.selectedLayerIndex;
            } else /* if (fromPos > toPos) */ {
                ++frame.selectedLayerIndex;
            }
        }
        frame.computeLayerTree();

        drawBitmapOntoView(true, true);
        drawChessboardOntoView();
        drawGridOntoView();
        drawSelectionOntoView();

        layerList.rvLayerList.post(() -> {
            frame.layerAdapter.notifyItemRangeChanged(Math.min(fromPos, toPos), Math.abs(toPos - fromPos) + 1);
            layerList.rvLayerList.post(frame.layerAdapter::notifyLayerTreeChanged);
        });
    };

    private final ItemMovableAdapter.OnItemSelectedListener onFrameItemSelectedListener = (view, position) -> {
        final int unselectedPos = project.selectedFrameIndex;
        selectFrame(position);
        frameList.rvFrameList.post(() -> {
            project.frameAdapter.notifyItemChanged(unselectedPos, FrameAdapter.Payload.SELECTED);
            project.frameAdapter.notifyItemChanged(position, FrameAdapter.Payload.SELECTED);
        });
    };

    private final ItemMovableAdapter.OnItemSelectedListener onFrameItemReselectedListener = (view, position) -> {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        final Menu menu = popupMenu.getMenu();
        MenuCompat.setGroupDividerEnabled(menu, true);
        popupMenu.getMenuInflater().inflate(R.menu.frame, menu);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(this::onFrameOptionsItemSelected);
        popupMenu.show();

        menu.findItem(R.id.i_frame_clip).setEnabled(project.frames.size() > 1);
    };

    @SuppressLint("NonConstantResourceId")
    private final ItemMovableAdapter.OnItemSelectedListener onLayerItemSelectedListener = (view, position) -> {
        final int unselectedPos = frame.selectedLayerIndex;
        selectLayer(position);
        layerList.rvLayerList.post(() -> {
            frame.layerAdapter.notifyItemChanged(unselectedPos, LayerAdapter.Payload.SELECTED);
            frame.layerAdapter.notifyItemChanged(position, LayerAdapter.Payload.SELECTED);
            layerList.rvLayerList.post(frame.layerAdapter::notifyLayerTreeChanged);
        });
    };

    private final ItemMovableAdapter.OnItemSelectedListener onLayerItemReselectedListener = (view, position) -> {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        final Menu menu = popupMenu.getMenu();
        MenuCompat.setGroupDividerEnabled(menu, true);
        popupMenu.getMenuInflater().inflate(R.menu.layer, menu);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(this::onLayerOptionsItemSelected);
        popupMenu.show();

        menu.findItem(R.id.i_layer_clipping).setChecked(layer.clipToBelow);
        menu.findItem(R.id.i_layer_level_up).setEnabled(layer.getLevel() > 0);
        menu.findItem(R.id.i_layer_pass_below).setChecked(layer.passBelow);
        menu.findItem(R.id.i_layer_reference).setChecked(layer.reference);

        menu.findItem(layer.filter == null ? R.id.i_layer_filter_none : switch (layer.filter) {
            case COLOR_BALANCE -> R.id.i_layer_filter_color_balance;
            case COLOR_MATRIX -> R.id.i_layer_filter_color_matrix;
            case CONTRAST -> R.id.i_layer_filter_contrast;
            case CURVES -> R.id.i_layer_filter_curves;
            case HSV -> R.id.i_layer_filter_hsv;
            case LEVELS -> R.id.i_layer_filter_levels;
            case LIGHTING -> R.id.i_layer_filter_lighting;
            case LIGHTNESS -> R.id.i_layer_filter_lightness;
            case SATURATION -> R.id.i_layer_filter_saturation;
            case THRESHOLD -> R.id.i_layer_filter_threshold;
        }).setChecked(true);

        menu.findItem(R.id.i_blend_mode).getSubMenu()
                .getItem(layer.paint.getBlendMode().ordinal()).setChecked(true);
    };

    private final TabLayout.OnTabSelectedListener onProjTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            final int position = tab.getPosition();
            project = projects.get(position);
            frameList.rvFrameList.setAdapter(project.frameAdapter);

            translationX = project.translationX;
            translationY = project.translationY;
            scale = project.scale;

            selectFrame(project.selectedFrameIndex);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            final int i = tab.getPosition();
            if (i >= 0) {
                final Project proj = projects.get(i);
                proj.translationX = translationX;
                proj.translationY = translationY;
                proj.scale = scale;
            }
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            final PopupMenu popupMenu = new PopupMenu(MainActivity.this, tab.view);
            final Menu menu = popupMenu.getMenu();
            MenuCompat.setGroupDividerEnabled(menu, true);
            popupMenu.getMenuInflater().inflate(R.menu.proj_tab, menu);
            popupMenu.setForceShowIcon(true);
            popupMenu.setOnMenuItemClickListener(MainActivity.this::onProjTabOptionsItemSelected);
            popupMenu.show();
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
                    layer.guides.offerFirst(guide); // Add at the front for faster removal if necessary later
                }
                case MotionEvent.ACTION_MOVE -> {
                    guide.position = toBitmapY(event.getY() - rulerHHeight);
                    drawGridOntoView();
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    final float y = event.getY();
                    if (!(rulerHHeight <= y && y < rulerHHeight + viewHeight)) {
                        layer.guides.remove(guide);
                        drawGridOntoView();
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
                    layer.guides.offerFirst(guide); // Add at the front for faster removal if necessary later
                }
                case MotionEvent.ACTION_MOVE -> {
                    guide.position = toBitmapX(event.getX() - rulerVWidth);
                    drawGridOntoView();
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    final float x = event.getX();
                    if (!(rulerVWidth <= x && x < rulerVWidth + viewWidth)) {
                        layer.guides.remove(guide);
                        drawGridOntoView();
                    }
                    guide = null;
                }
            }
            return true;
        }
    };

    private final AnimationClipper.OnConfirmListener onClipConfirmListener = (from, to) -> {
        for (int i = project.frames.size() - 1; i >= 0; --i) {
            if (!(from <= i && i <= to || from > to && (from <= i || i <= to))) {
                deleteFrame(i);
                if (i < project.selectedFrameIndex) {
                    --project.selectedFrameIndex;
                }
            }
        }
        frameList.rvFrameList.post(() -> project.frameAdapter.notifyDataSetChanged());
        selectFrame(Math.min(project.selectedFrameIndex, project.frames.size() - 1));
    };

    private final CellGridManager.OnApplyListener onCellGridApplyListener = this::drawGridOntoView;

    private final ImageSizeManager.OnApplyListener onImageSizeApplyListener = (width, height, transform) -> {
        drawFloatingLayersIntoImage();
        if (layer == frame.getBackgroundLayer()) {
            for (final Frame f : project.frames) {
                resizeImage(f, f.getBackgroundLayer(), width, height, transform, null, 0, 0);
            }
        } else {
            resizeImage(frame, layer, width, height, transform, null, 0, 0);
        }
    };

    private final RunnableRunnable runnableRunningRunner = (target, wait) -> target.run();

    private final RunnableRunnable runnableStartingRunner = (target, wait) -> {
        if (MAIN_LOOPER.isCurrentThread()) {
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
        public String drawShapeOntoCanvas(Canvas canvas, int x0, int y0, int x1, int y1) {
            final int radius = (int) Math.hypot(x1 - x0, y1 - y0);
            canvas.drawCircle(x0 + 0.5f, y0 + 0.5f, radius, paint);
            return canvas == dpPreview.getCanvas()
                    ? getString(R.string.state_radius, radius + 0.5f) : null;
        }

        @Override
        public Rect mapRect(int x0, int y0, int x1, int y1) {
            final int radius = (int) Math.ceil(Math.hypot(x1 - x0, y1 - y0));
            return MainActivity.this.mapRect(x0 - radius, y0 - radius, x1 + radius, y1 + radius,
                    strokeWidth / 2.0f + blurRadius);
        }
    };

    private final Shape line = new Shape() {
        @Override
        public String drawShapeOntoCanvas(Canvas canvas, int x0, int y0, int x1, int y1) {
            CanvasUtils.drawInclusiveLine(canvas, x0, y0, x1, y1, paint);
            return canvas == dpPreview.getCanvas()
                    ? getString(R.string.state_length, Math.hypot(x1 - x0, y1 - y0) + 1) : null;
        }

        @Override
        public Rect mapRect(int x0, int y0, int x1, int y1) {
            return MainActivity.this.mapRect(x0, y0, x1, y1, strokeWidth / 2.0f + blurRadius);
        }
    };

    private final Shape oval = new Shape() {
        @Override
        public String drawShapeOntoCanvas(Canvas canvas, int x0, int y0, int x1, int y1) {
            final float left = Math.min(x0, x1) + 0.5f, top = Math.min(y0, y1) + 0.5f,
                    right = Math.max(x0, x1) + 0.5f, bottom = Math.max(y0, y1) + 0.5f;
            canvas.drawOval(left, top, right, bottom, paint);
            return canvas == dpPreview.getCanvas()
                    ? getString(R.string.state_axes, Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1) : null;
        }

        @Override
        public Rect mapRect(int x0, int y0, int x1, int y1) {
            return MainActivity.this.mapRect(x0, y0, x1, y1, strokeWidth / 2.0f + blurRadius);
        }
    };

    private final Shape rect = new Shape() {
        @Override
        public String drawShapeOntoCanvas(Canvas canvas, int x0, int y0, int x1, int y1) {
            final float left = Math.min(x0, x1), top = Math.min(y0, y1),
                    right = Math.max(x0, x1) + 0.5f, bottom = Math.max(y0, y1) + 0.5f;
            canvas.drawRect(left, top, right, bottom, paint);
            return canvas == dpPreview.getCanvas()
                    ? getString(R.string.state_size, Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1) : null;
        }

        @Override
        public Rect mapRect(int x0, int y0, int x1, int y1) {
            return MainActivity.this.mapRect(x0, y0, x1, y1, strokeWidth / 2.0f + blurRadius);
        }
    };

    private Shape shape = null;

    private abstract class OnIVTouchListener implements View.OnTouchListener {
        private void blockViews(MotionEvent event) {
            final boolean enabled;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> enabled = false;
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> enabled = true;
                default -> {
                    return;
                }
            }
            activityMain.vBlockerNeg.setClickable(!enabled);
            activityMain.vBlockerPos.setClickable(!enabled);
        }

        public abstract void onIVTouch(View v, MotionEvent event);

        @Override
        public final boolean onTouch(View v, MotionEvent event) {
            blockViews(event);
            if (isZoomingEnabled) {
                onIVTouchWithZoomToolListener.onIVTouch(v, event);
            } else {
                onIVTouch(v, event);
            }
            return true;
        }
    }

    private abstract class OnIVMultiTouchListener extends OnIVTouchListener {
        private boolean multiTouch = false;

        public abstract void onIVSingleTouch(View v, MotionEvent event);

        protected void onStartMultiTouch() {
        }

        @Override
        public final void onIVTouch(View v, MotionEvent event) {
            final int pointerCount = event.getPointerCount(), action = event.getAction();
            if (pointerCount == 1 && !multiTouch) {
                onIVSingleTouch(v, event);
            } else if (pointerCount <= 2) {
                if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && !multiTouch) {
                    multiTouch = true;
                    onStartMultiTouch();
                    isShapeStopped = true;
                    undo();
                    if (lastMerged == null) mergeLayersEntire();
                }
                onIVTouchWithZoomToolListener.onIVTouch(v, event);
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                multiTouch = false;
            }
        }

        public boolean isMultiTouch() {
            return multiTouch;
        }

        protected void undo() {
            if (!dpPreview.isRecycled()) {
                dpPreview.erase();
                return;
            }

            final Bitmap bitmap = layer.history.getCurrent();
            MainActivity.this.bitmap.recycle();
            MainActivity.this.bitmap = bitmap;
            layer.bitmap = bitmap;
            canvas = new Canvas(bitmap);

            eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
            clearStatus();
        }
    }

    private final View.OnTouchListener onIVTouchWithBrushListener = new OnIVMultiTouchListener() {
        private float lastRad;
        private float maxRad;
        private int lastBX, lastBY;
        private VelocityTracker velocityTracker;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            if (brush.recycled()) {
                return;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    velocityTracker = VelocityTracker.obtain();
                    velocityTracker.addMovement(event);
                    maxRad = strokeWidth / 2.0f;
                    clearStatus();
                    lastRad = 0.0f;
                    lastBX = toBitmapX(event.getX());
                    lastBY = toBitmapY(event.getY());
                }
                case MotionEvent.ACTION_MOVE -> {
                    velocityTracker.addMovement(event);
                    velocityTracker.computeCurrentVelocity(1);
                    final int currBX = toBitmapX(event.getX()), currBY = toBitmapY(event.getY());
                    final float vel = (float) Math.hypot(velocityTracker.getXVelocity(), velocityTracker.getYVelocity());
                    final float rad = Math.min(maxRad / vel / softness, maxRad);
                    final float diffBX = currBX - lastBX, diffBY = currBY - lastBY;
                    final float projBX = Math.abs(diffBX), projBY = Math.abs(diffBY);
                    final float stepBX = projBX >= projBY ? Math.signum(diffBX) : diffBX / projBY,
                            stepBY = projBY >= projBX ? Math.signum(diffBY) : diffBY / projBX;
                    final float stepCount = Math.max(projBX, projBY);
                    final float stepRad = (rad - lastRad) / stepCount;

                    {
                        final float lastRad = this.lastRad;
                        final int lastBX = this.lastBX, lastBY = this.lastBY;
                        new Thread(() -> {
                            for (float r = lastRad, bx = lastBX, by = lastBY, s = 0.0f; s < stepCount; r += stepRad, bx += stepBX, by += stepBY, ++s) {
                                canvas.drawBitmap(brush.bm(), brush.rect, new RectF(bx - r, by - r, bx + r, by + r), PAINT_SRC_OVER);
                            }
                            runOnUiThread(() -> drawBitmapOntoView(lastBX, lastBY, currBX, currBY, maxRad + blurRadius));
                        }).start();
                    }

                    lastRad = rad;
                    lastBX = currBX;
                    lastBY = currBY;
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    velocityTracker.recycle();
                    addToHistory();
                    clearStatus();
                }
            }
        }

        @Override
        protected void onStartMultiTouch() {
            velocityTracker.recycle();
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onIVTouchWithBucketListener = new OnIVTouchListener() {
        @Override
        public void onIVTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    if (!(0 <= bx && bx < bitmap.getWidth() && 0 <= by && by < bitmap.getHeight())) {
                        break;
                    }
                    final Bitmap src = !ref.recycled() ? ref.bm() : bitmap;
                    final Rect rect = hasSelection ? selection.r : null;
                    runOrStart(() -> {
                        if (activityMain.optionsBucketFill.cbContiguous.isChecked()) {
                            BitmapUtils.floodFill(src, bitmap, rect, bx, by, paint.getColor(),
                                    activityMain.optionsBucketFill.cbIgnoreAlpha.isChecked(), threshold);
                        } else {
                            BitmapUtils.bucketFill(src, bitmap, rect, bx, by, paint.getColor(),
                                    activityMain.optionsBucketFill.cbIgnoreAlpha.isChecked(), threshold);
                        }
                        if (rect == null) {
                            drawBitmapOntoView(true);
                        } else {
                            drawBitmapOntoView(rect, true);
                        }
                        addToHistory();
                    }, true);
                    clearStatus();
                }
            }
        }
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onIVTouchWithCloneStampListener = new OnIVMultiTouchListener() {
        private int lastBX, lastBY;
        private int dx, dy;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            final float x = event.getX(), y = event.getY();
            final int bx = toBitmapX(x), by = toBitmapY(y);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (cloneStampSrc == null) {
                        break;
                    }
                    dx = cloneStampSrc.x - bx;
                    dy = cloneStampSrc.y - by;
                    lastBX = bx;
                    lastBY = by;

                case MotionEvent.ACTION_MOVE: {
                    if (cloneStampSrc == null) {
                        break;
                    }

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
                    drawBitmapOntoView((int) left, (int) top, (int) (left + width), (int) (top + height));
                    drawCrossOntoView(bx + dx, by + dy);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));

                    lastBX = bx;
                    lastBY = by;
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (cloneStampSrc == null) {
                        cloneStampSrc = new Point(bx, by);
                        drawCrossOntoView(bx, by);
                        activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    } else {
                        drawCrossOntoView(cloneStampSrc.x, cloneStampSrc.y);
                        addToHistory();
                        clearStatus();
                    }
                    break;
            }
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithEraserListener = new OnIVMultiTouchListener() {
        private int lastBX, lastBY;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    lastBX = bx;
                    lastBY = by;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    CanvasUtils.drawInclusiveLine(canvas, lastBX, lastBY, bx, by, eraser);
                    drawBitmapOntoView(lastBX, lastBY, bx, by, eraserStrokeHalfWidth + blurRadiusEraser);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    lastBX = bx;
                    lastBY = by;
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    addToHistory();
                    clearStatus();
                    break;
            }
        }
    };

    @SuppressLint({"ClickableViewAccessibility", "StringFormatMatches"})
    private final View.OnTouchListener onIVTouchWithImpreciseEyedropperListener = new OnIVMultiTouchListener() {
        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    final int bx = satX(bitmap, toBitmapX(x)), by = satY(bitmap, toBitmapY(y));
                    final int color = activityMain.optionsEyedropper.btgSrc.getCheckedButtonId() == R.id.b_all_layers
                            ? viewBitmap.getPixel((int) x, (int) y) : bitmap.getPixel(bx, by);
                    paint.setColor(color);
                    activityMain.vForegroundColor.setBackgroundColor(color);
                    activityMain.tvStatus.setText(String.format(
                            getString(R.string.state_eyedropper_imprecise, Settings.INST.argbCompFormat()),
                            bx, by, Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color)));
                }
                case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> clearStatus();
            }
        }

        @Override
        protected void undo() {
        }
    };

    private final View.OnTouchListener onIVTouchWithPreciseEyedropperListener = new OnIVMultiTouchListener() {
        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    final int bx = satX(bitmap, toBitmapX(x)), by = satY(bitmap, toBitmapY(y));
                    final android.graphics.Color color = activityMain.optionsEyedropper.btgSrc.getCheckedButtonId() == R.id.b_all_layers
                            ? viewBitmap.getColor((int) x, (int) y) : bitmap.getColor(bx, by);
                    paint.setColor(color.pack());
                    activityMain.vForegroundColor.setBackgroundColor(color.toArgb());
                    activityMain.tvStatus.setText(getString(R.string.state_eyedropper_precise,
                            bx, by, String.valueOf(color.alpha()),
                            String.valueOf(color.red()), String.valueOf(color.green()), String.valueOf(color.blue())));
                }
                case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> clearStatus();
            }
        }

        @Override
        public void undo() {
        }
    };

    private View.OnTouchListener onIVTouchWithEyedropperListener;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onIVTouchWithGradientListener = new OnIVMultiTouchListener() {
        private int shapeStartX, shapeStartY;
        private Rect lastRect;

        @ColorLong
        private long color0;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            final float x = event.getX(), y = event.getY();
            final int bx = toBitmapX(x), by = toBitmapY(y);
            final Bitmap src = !ref.recycled() ? ref.bm() : bitmap;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    if (isShapeStopped) {
                        isShapeStopped = false;
                        dpPreview.getCanvas().drawPoint(bx + 0.5f, by + 0.5f, paint);
                        drawBitmapOntoView(bx, by, bx + 1, by + 1);
                        lastRect = new Rect();
                        shapeStartX = bx;
                        shapeStartY = by;
                        color0 = src.getColor(satX(src, bx), satY(src, by)).pack();
                        activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                        break;
                    }
                }
                case MotionEvent.ACTION_MOVE: {
                    paint.setShader(new LinearGradient(shapeStartX, shapeStartY, bx, by,
                            color0,
                            src.getColor(satX(src, bx), satY(src, by)).pack(),
                            Shader.TileMode.CLAMP));
                    dpPreview.erase(lastRect);
                    CanvasUtils.drawInclusiveLine(dpPreview.getCanvas(), shapeStartX, shapeStartY, bx, by, paint);
                    final Rect rect = mapRect(shapeStartX, shapeStartY, bx, by, strokeWidth / 2.0f + blurRadius);
                    lastRect.union(rect);
                    drawBitmapOntoView(lastRect);
                    lastRect = rect;
                    activityMain.tvStatus.setText(getString(R.string.state_start_stop,
                            shapeStartX, shapeStartY, bx, by));
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (bx != shapeStartX || by != shapeStartY) {
                        isShapeStopped = true;
                        CanvasUtils.drawInclusiveLine(canvas, shapeStartX, shapeStartY, bx, by, paint);
                        final Rect rect = mapRect(shapeStartX, shapeStartY, bx, by, strokeWidth / 2.0f + blurRadius);
                        lastRect.union(rect);
                        dpPreview.erase(lastRect);
                        drawBitmapOntoView(lastRect, true);
                        lastRect = null;
                        addToHistory();
                        clearStatus();
                    }
                    break;
            }
        }

        @Override
        public void onStartMultiTouch() {
            paint.setShader(null);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onIVTouchWithImpreciseMagicEraserListener = new OnIVMultiTouchListener() {
        private float lastX, lastY;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
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
                    final int colorLeft = ref.bm().getPixel(
                            satX(ref.bm(), toBitmapX(x + radF * (float) Math.sin(theta))),
                            satY(ref.bm(), toBitmapY(y - radF * (float) Math.cos(theta))));
                    final int colorRight = ref.bm().getPixel(
                            satX(ref.bm(), toBitmapX(x - radF * (float) Math.sin(theta))),
                            satY(ref.bm(), toBitmapY(y + radF * (float) Math.cos(theta))));
                    final int backgroundColor =
                            activityMain.optionsMagicEraser.btgSides.getCheckedButtonId() == R.id.b_left ? colorLeft : colorRight;
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
                    new Canvas(bm).drawBitmap(ref.bm(),
                            new Rect(left, top, right, bottom),
                            new Rect(0, 0, width, height),
                            PAINT_SRC);
                    BitmapUtils.removeBackground(bm, foregroundColor, backgroundColor);
                    cLine.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC_IN);
                    bm.recycle();
                    canvas.drawBitmap(bLine, left, top, PAINT_SRC_OVER);
                    bLine.recycle();

                    drawBitmapOntoView(left, top, right, bottom);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    lastX = x;
                    lastY = y;
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    addToHistory();
                    clearStatus();
                }
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onIVTouchWithPreciseMagicEraserListener = new OnIVTouchListener() {
        @Override
        public void onIVTouch(View v, MotionEvent event) {
            switch (event.getPointerCount()) {
                case 1 -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN -> {
                            final float x = event.getX(), y = event.getY();
                            final Rect vs = getVisibleSubset();
                            if (magEr.b == null || magEr.f == null
                                    || (!vs.contains(magEr.b.x, magEr.b.y) && !vs.contains(magEr.f.x, magEr.f.y))) {
                                final int bx = toBitmapX(x), by = toBitmapY(y);
                                magEr.b = new Point(bx, by);
                                magEr.f = new Point(bx, by);
                                drawCrossOntoView(bx, by);
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
                            magEr.b.set(toBitmapX(x0 + magEr.bd.x), toBitmapY(y0 + magEr.bd.y));
                            magEr.f.set(toBitmapX(x1 + magEr.fd.x), toBitmapY(y1 + magEr.fd.y));
                            drawCrossOntoView(magEr.b.x, magEr.b.y, true);
                            drawCrossOntoView(magEr.f.x, magEr.f.y, false);

                            if (!activityMain.optionsMagicEraser.cbAccEnabled.isChecked()) {
                                break;
                            }

                            final int rad = (int) (strokeWidth / 2.0f + blurRadius);
                            final int backgroundColor = ref.bm().getPixel(
                                    satX(ref.bm(), magEr.b.x), satY(ref.bm(), magEr.b.y));
                            final int foregroundColor = ref.bm().getPixel(
                                    satX(ref.bm(), magEr.f.x), satY(ref.bm(), magEr.f.y));

                            final int left = Math.min(magEr.b.x, magEr.f.x) - rad,
                                    top = Math.min(magEr.b.y, magEr.f.y) - rad,
                                    right = Math.max(magEr.b.x, magEr.f.x) + rad + 1,
                                    bottom = Math.max(magEr.b.y, magEr.f.y) + rad + 1;
                            final int width = right - left, height = bottom - top;
                            final Bitmap bLine = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            final Canvas cLine = new Canvas(bLine);
                            cLine.drawLine(magEr.b.x - left, magEr.b.y - top,
                                    magEr.f.x - left, magEr.f.y - top,
                                    paint);
                            canvas.drawBitmap(bLine, left, top, PAINT_DST_OUT);
                            final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            new Canvas(bm).drawBitmap(ref.bm(),
                                    new Rect(left, top, right, bottom),
                                    new Rect(0, 0, width, height),
                                    PAINT_SRC);
                            BitmapUtils.removeBackground(bm, foregroundColor, backgroundColor);
                            cLine.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC_IN);
                            bm.recycle();
                            canvas.drawBitmap(bLine, left, top, PAINT_SRC_OVER);
                            bLine.recycle();

                            drawBitmapOntoView(left, top, right, bottom);
                        }
                        case MotionEvent.ACTION_POINTER_DOWN -> {
                            final float x0 = event.getX(0), y0 = event.getY(0);
                            final float x1 = event.getX(1), y1 = event.getY(1);
                            magEr.bd.set(toViewX(magEr.b.x) - x0, toViewY(magEr.b.y) - y0);
                            magEr.fd.set(toViewX(magEr.f.x) - x1, toViewY(magEr.f.y) - y1);
                        }
                    }
                }
            }
        }
    };

    private View.OnTouchListener onIVTouchWithMagicEraserListener = onIVTouchWithImpreciseMagicEraserListener;

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithMagicPaintListener = new OnIVMultiTouchListener() {
        private int lastBX, lastBY;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    lastBX = bx;
                    lastBY = by;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);

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
                        cv.drawBitmap(ref.bm(), absolute, relative, PAINT_SRC_IN);
                        final Bitmap bThr = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444); // Threshold
                        BitmapUtils.floodFill(bm, bThr, hasSelection ? selection.r : null,
                                relativeX, relativeY, Color.BLACK, true, threshold);
                        bm.recycle();
                        cLine.drawBitmap(bThr, 0.0f, 0.0f, PAINT_DST_IN);
                        bThr.recycle();
                    }
                    canvas.drawBitmap(bLine, left, top, magicPaint);
                    bLine.recycle();

                    drawBitmapOntoView(left, top, right, bottom);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    lastBX = bx;
                    lastBY = by;
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    addToHistory();
                    clearStatus();
                    break;
            }
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithMarqueeListener = new OnIVTouchListener() {
        private boolean hasDraggedBound = false;
        private int startX, startY;

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    final float x = event.getX(), y = event.getY();
                    if (selection.marqBoundBeingDragged == null) {
                        if (hasSelection && selection.checkDraggingMarqueeBound(x, y) != null) {
                            activityMain.tvStatus.setText(getString(R.string.state_selected_bound,
                                    getString(selection.marqBoundBeingDragged.name)));
                        } else {
                            final int stopX, stopY;
                            if (hasSelection && selection.r.width() == 1 && selection.r.height() == 1) {
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
                            activityMain.tvStatus.setText(getString(R.string.state_from_to_size_1,
                                    startX, startY, startX, startY));
                        }
                    } else {
                        hasDraggedBound |= selection.dragMarqueeBound(x, y, scale);
                        drawSelectionOntoView();
                        activityMain.tvStatus.setText(getString(R.string.state_l_t_r_b_size,
                                selection.r.left, selection.r.top, selection.r.right - 1, selection.r.bottom - 1,
                                selection.r.width(), selection.r.height()));
                    }
                }
                case MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    if (selection.marqBoundBeingDragged == null) {
                        final int stopX, stopY;
                        stopX = toBitmapX(x) + 1;
                        stopY = toBitmapY(y) + 1;
                        setSelection(startX, startY, stopX, stopY);
                    } else {
                        hasDraggedBound |= selection.dragMarqueeBound(x, y, scale);
                        drawSelectionOntoView();
                    }
                    activityMain.tvStatus.setText(getString(R.string.state_l_t_r_b_size,
                            selection.r.left, selection.r.top, selection.r.right - 1, selection.r.bottom - 1,
                            selection.r.width(), selection.r.height()));
                }
                case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    optimizeSelection();
                    drawSelectionOntoView();
                    if (selection.marqBoundBeingDragged != null) {
                        if (hasDraggedBound) {
                            selection.marqBoundBeingDragged = null;
                            hasDraggedBound = false;
                            activityMain.tvStatus.setText(hasSelection ?
                                    getString(R.string.state_l_t_r_b_size,
                                            selection.r.left, selection.r.top, selection.r.right - 1, selection.r.bottom - 1,
                                            selection.r.width(), selection.r.height()) :
                                    "");
                        }
                    } else {
                        activityMain.tvStatus.setText(hasSelection ?
                                getString(R.string.state_l_t_r_b_size,
                                        selection.r.left, selection.r.top, selection.r.right - 1, selection.r.bottom - 1,
                                        selection.r.width(), selection.r.height()) :
                                "");
                    }
                }
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onIVTouchWithPatcherListener = new OnIVMultiTouchListener() {
        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            if (!hasSelection) {
                return;
            }
            final float radius = strokeWidth / 2.0f + blurRadius;
            if (selection.r.left + radius * 2.0f >= selection.r.right || selection.r.top + radius * 2.0f >= selection.r.bottom) {
                return;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    createEditPreview();

                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    final int w = selection.r.width(), h = selection.r.height();
                    final int wh = w >> 1, hh = h >> 1; // h - Half
                    editPreview.reset();
                    final RectF rect = new RectF(selection.r);
                    rect.inset(radius, radius);
                    editPreview.getCanvas().drawBitmap(bitmap,
                            new Rect(bx - wh, by - hh, bx + w - wh, by + h - hh), rect, paint);
                    drawBitmapOntoView(editPreview.getEntire(), selection.r);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    drawEditPreviewIntoImage();
                    editPreview.recycle();
                    editPreview = null;
                    addToHistory();
                    clearStatus();
                    break;
            }
        }

        @Override
        protected void undo() {
            editPreview.recycle();
            editPreview = null;
            clearStatus();
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithPathListener = new OnIVMultiTouchListener() {
        private Path path, previewPath;

        @Override
        @SuppressLint("NonConstantResourceId")
        public void onIVSingleTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    path = new Path();
                    path.moveTo(bx, by);
                    previewPath = new Path();
                    previewPath.moveTo(x, y);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                }
                case MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    path.lineTo(bx, by);
                    previewPath.lineTo(x, y);
                    eraseBitmap(previewBitmap);
                    previewCanvas.drawPath(previewPath, selector);
                    activityMain.canvas.ivPreview.invalidate();
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    switch (activityMain.optionsPath.btgDrawingPrimitives.getCheckedButtonId()) {
                        case R.id.b_path -> canvas.drawPath(path, paint);
                        case R.id.b_text ->
                                canvas.drawTextOnPath(activityMain.optionsText.tietText.getText().toString(), path, 0.0f, 0.0f, paint);
                    }
                    final RectF bounds = new RectF();
                    path.computeBounds(bounds, false);
                    drawBitmapOntoView((int) Math.floor(bounds.left), (int) Math.floor(bounds.top),
                            (int) Math.ceil(bounds.right), (int) Math.ceil(bounds.bottom),
                            strokeWidth / 2.0f + blurRadius
                                    + (activityMain.optionsPath.btgDrawingPrimitives.getCheckedButtonId() == R.id.b_text ? textSize : 0));
                    eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
                    addToHistory();
                    clearStatus();
                    path = null;
                    previewPath = null;
                }
            }
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithPencilListener = new OnIVMultiTouchListener() {
        private int lastBX, lastBY;
        private Paint pencil;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    pencil = activityMain.optionsPencil.cbAttachingEraser.isChecked()
                            && bitmap.getColor(satX(bitmap, bx), satY(bitmap, by)).pack() != eraser.getColorLong()
                            ? eraser : paint;
                    lastBX = bx;
                    lastBY = by;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    CanvasUtils.drawInclusiveLine(canvas, lastBX, lastBY, bx, by, pencil);
                    drawBitmapOntoView(lastBX, lastBY, bx, by, strokeWidth / 2.0f + blurRadius);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    lastBX = bx;
                    lastBY = by;
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    addToHistory();
                    clearStatus();
                    break;
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onIVTouchWithRulerListener = new OnIVMultiTouchListener() {
        private int shapeStartX, shapeStartY;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            final float x = event.getX(), y = event.getY();
            final float halfScale = scale / 2.0f;
            final int bx = toBitmapX(x + halfScale), by = toBitmapY(y + halfScale);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isShapeStopped) {
                        isShapeStopped = false;
                        ruler.enabled = false;
                        eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
                        drawPointOntoView(bx, by);
                        shapeStartX = bx;
                        shapeStartY = by;
                        activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                        break;
                    }
                    // Fall through
                case MotionEvent.ACTION_MOVE:
                    if (bx != shapeStartX || by != shapeStartY) {
                        isShapeStopped = true;
                        ruler.set(shapeStartX, shapeStartY, bx, by);
                        ruler.enabled = true;
                        drawRulerOntoView();
                        final int dx = ruler.stopX - ruler.startX, dy = ruler.stopY - ruler.startY;
                        activityMain.tvStatus.setText(getString(R.string.state_ruler,
                                ruler.startX, ruler.startY, ruler.stopX, ruler.stopY, dx, dy,
                                String.valueOf((float) Math.sqrt(dx * dx + dy * dy))));
                    }
                    break;
            }
        }

        @Override
        protected void undo() {
            ruler.enabled = false;
            eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
            clearStatus();
        }
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onIVTouchWithShapeListener = new OnIVMultiTouchListener() {
        private int shapeStartX, shapeStartY;
        private Rect lastRect;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            final float x = event.getX(), y = event.getY();
            final int bx = toBitmapX(x), by = toBitmapY(y);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    if (isShapeStopped) {
                        isShapeStopped = false;
                        dpPreview.getCanvas().drawPoint(bx + 0.5f, by + 0.5f, paint);
                        drawBitmapOntoView(bx, by, bx + 1, by + 1);
                        lastRect = new Rect();
                        shapeStartX = bx;
                        shapeStartY = by;
                        activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                        break;
                    }
                }
                case MotionEvent.ACTION_MOVE: {
                    dpPreview.erase(lastRect);
                    final String result = shape.drawShapeOntoCanvas(dpPreview.getCanvas(), shapeStartX, shapeStartY, bx, by);
                    final Rect rect = shape.mapRect(shapeStartX, shapeStartY, bx, by);
                    lastRect.union(rect);
                    drawBitmapOntoView(lastRect);
                    lastRect = rect;
                    activityMain.tvStatus.setText(getString(R.string.state_start_stop_, shapeStartX, shapeStartY, bx, by, result));
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (bx != shapeStartX || by != shapeStartY) {
                        isShapeStopped = true;
                        shape.drawShapeOntoCanvas(canvas, shapeStartX, shapeStartY, bx, by);
                        final Rect rect = shape.mapRect(shapeStartX, shapeStartY, bx, by);
                        lastRect.union(rect);
                        dpPreview.erase(lastRect);
                        drawBitmapOntoView(lastRect, true);
                        lastRect = null;
                        addToHistory();
                        clearStatus();
                    }
                    break;
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onIVTouchWithTextListener = new OnIVMultiTouchListener() {
        private float dx, dy;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            if (isEditingText) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN -> {
                        final float x = event.getX(), y = event.getY();
                        dx = x - toScaled(text.x);
                        dy = y - toScaled(text.y);
                        drawTextOntoView();
                    }
                    case MotionEvent.ACTION_MOVE -> {
                        final float x = event.getX(), y = event.getY();
                        text.x = toUnscaled(x - dx);
                        text.y = toUnscaled(y - dy);
                        drawTextOntoView();
                        activityMain.tvStatus.setText(getString(R.string.coordinates, text.x, text.y));
                    }
                }

            } else {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN -> {
                        final float x = event.getX(), y = event.getY();
                        text.x = toBitmapX(x);
                        text.y = toBitmapY(y);
                        activityMain.llOptionsText.setVisibility(View.VISIBLE);
                        isEditingText = true;
                        drawTextOntoView();
                        textActionMode = startSupportActionMode(onTextActionModeCallback);
                        textActionMode.setTitle(R.string.text);
                        dx = toViewX(0);
                        dy = toViewY(0);
                    }
                }
            }
        }

        @Override
        protected void undo() {
        }
    };

    /**
     * Callback to call on touch image view with mesh transformer
     */
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onIVTouchWithMTListener = new OnIVTouchListener() {
        private int lastVertIndex;

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            if (!hasSelection) {
                return;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    if (selection.r.isEmpty() || transformer.mesh == null) {
                        break;
                    }
                    if (transformer.isRecycled()) {
                        createTransformer();
                    }
                    final float rx = toBitmapXExact(event.getX()) - selection.r.left, ry = toBitmapYExact(event.getY()) - selection.r.top;
                    lastVertIndex = Math.round(ry / selection.r.height() * transformer.mesh.height) * (transformer.mesh.width + 1)
                            + Math.round(rx / selection.r.width() * transformer.mesh.width);
                }
                case MotionEvent.ACTION_MOVE -> {
                    if (transformer.isRecycled() || transformer.mesh == null) {
                        break;
                    }
                    final float rx = toBitmapXExact(event.getX()) - selection.r.left, ry = toBitmapYExact(event.getY()) - selection.r.top;
                    transformer.mesh.verts[lastVertIndex * 2] = rx;
                    transformer.mesh.verts[lastVertIndex * 2 + 1] = ry;
                    drawSelectionOntoView();
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (transformer.isRecycled() || transformer.mesh == null) {
                        break;
                    }
                    transformer.transformMesh(activityMain.optionsTransformer.cbFilter.isChecked(), antiAlias);
                    drawBitmapOntoView(selection.r, true);
                }
            }
        }
    };

    /**
     * Callback to call on touch image view with poly transformer
     */
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onIVTouchWithPTListener = new OnIVTouchListener() {
        private float[] src, dst, bmSrc, bmDst;
        private int pointCount = 0;
        private Matrix matrix;

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            if (!hasSelection) {
                return;
            }
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN -> {
                    if (selection.r.isEmpty()) {
                        break;
                    }
                    if (transformer.isRecycled()) {
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
                    bmSrc[0] = toBitmapX(x) - selection.r.left;
                    bmSrc[1] = toBitmapY(y) - selection.r.top;
                    matrix = new Matrix();
                    clearStatus();
                }
                case MotionEvent.ACTION_POINTER_DOWN -> {
                    if (transformer.isRecycled()) {
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
                    bmSrc[index * 2] = toBitmapX(x) - selection.r.left;
                    bmSrc[index * 2 + 1] = toBitmapY(y) - selection.r.top;
                }
                case MotionEvent.ACTION_MOVE -> {
                    if (transformer.isRecycled()) {
                        break;
                    }
                    pointCount = Math.min(event.getPointerCount(), 4);
                    for (int i = 0; i < pointCount; ++i) {
                        final float x = event.getX(i), y = event.getY(i);
                        dst[i * 2] = x;
                        dst[i * 2 + 1] = y;
                        bmDst[i * 2] = toBitmapX(x) - selection.r.left;
                        bmDst[i * 2 + 1] = toBitmapY(y) - selection.r.top;
                    }
                    matrix.setPolyToPoly(src, 0, dst, 0, pointCount);
                    activityMain.canvas.ivSelection.setImageMatrix(matrix);
                    drawSelectionOntoView(false);
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (transformer.isRecycled()) {
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
                    activityMain.canvas.ivSelection.setImageMatrix(null);
                    final RectF rect = transformer.transform(bmMatrix, activityMain.optionsTransformer.cbFilter.isChecked(), antiAlias);
                    if (rect != null) {
                        final Rect r = new Rect(selection.r);
                        final int w_ = transformer.getWidth(), h_ = transformer.getHeight();
                        selection.r.left += rect.left;
                        selection.r.top += rect.top;
                        selection.r.right = selection.r.left + w_;
                        selection.r.bottom = selection.r.top + h_;
                        r.union(selection.r);
                        drawBitmapOntoView(r, true);
                    }
                    drawSelectionOntoView();
                    clearStatus();
                }
            }
        }
    };

    /**
     * Callback to call on touch image view with rotation transformer
     */
    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithRTListener = new OnIVTouchListener() {
        private double lastTheta;

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            if (!hasSelection) {
                return;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    if (selection.r.isEmpty()) {
                        break;
                    }
                    final float x = event.getX(), y = event.getY();
                    if (transformer.isRecycled()) {
                        createTransformer();
                    }
                    activityMain.canvas.ivSelection.setPivotX(toViewX(selection.r.exactCenterX()));
                    activityMain.canvas.ivSelection.setPivotY(toViewY(selection.r.exactCenterY()));
                    lastTheta = (float) Math.atan2(y - activityMain.canvas.ivSelection.getPivotY(), x - activityMain.canvas.ivSelection.getPivotX());
                    clearStatus();
                }
                case MotionEvent.ACTION_MOVE -> {
                    if (transformer.isRecycled()) {
                        break;
                    }
                    final float x = event.getX(), y = event.getY();
                    final float degrees = (float) Math.toDegrees(Math.atan2(y - activityMain.canvas.ivSelection.getPivotY(), x - activityMain.canvas.ivSelection.getPivotX()) - lastTheta);
                    activityMain.canvas.ivSelection.setRotation(degrees);
                    drawSelectionOntoView();
                    activityMain.tvStatus.setText(getString(R.string.degrees_, degrees));
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (transformer.isRecycled()) {
                        break;
                    }
                    final int w = transformer.getWidth(), h = transformer.getHeight();
                    transformer.rotate(activityMain.canvas.ivSelection.getRotation(), activityMain.optionsTransformer.cbFilter.isChecked(), antiAlias);
                    activityMain.canvas.ivSelection.setRotation(0.0f);
                    final int w_ = transformer.getWidth(), h_ = transformer.getHeight();
                    final Rect r = new Rect(selection.r);
                    selection.r.left += w - w_ >> 1;
                    selection.r.top += h - h_ >> 1;
                    selection.r.right = selection.r.left + w_;
                    selection.r.bottom = selection.r.top + h_;
                    r.union(selection.r);
                    drawBitmapOntoView(r, true);
                    drawSelectionOntoView();
                    clearStatus();
                }
            }
        }
    };

    /**
     * Callback to call on touch image view with scale transformer
     */
    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithSTListener = new OnIVTouchListener() {
        private boolean hasDraggedBound = false;
        private float aspectRatio;
        private float centerX, centerY;
        private float dlpbLeft, dlpbTop, dlpbRight, dlpbBottom; // Distances from last point to bound

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            if (!hasSelection) {
                return;
            }
            switch (event.getPointerCount()) {
                case 1 -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN -> {
                            if (selection.r.isEmpty()) {
                                break;
                            }
                            final float x = event.getX(), y = event.getY();
                            if (transformer.isRecycled()) {
                                createTransformer();
                            }
                            drawSelectionOntoView(false);
                            if (selection.marqBoundBeingDragged == null) {
                                if (selection.checkDraggingMarqueeBound(x, y) != null) {
                                    if (activityMain.optionsTransformer.cbLar.isChecked()) {
                                        aspectRatio = (float) selection.r.width() / (float) selection.r.height();
                                        centerX = selection.r.exactCenterX();
                                        centerY = selection.r.exactCenterY();
                                    }
                                    activityMain.tvStatus.setText(getString(R.string.state_selected_bound,
                                            getString(selection.marqBoundBeingDragged.name)));
                                }
                            } else {
                                hasDraggedBound |= stretchByDraggedMarqueeBound(x, y);
                                activityMain.tvStatus.setText(getString(R.string.state_left_top,
                                        selection.r.left, selection.r.top));
                            }
                        }
                        case MotionEvent.ACTION_MOVE -> {
                            if (transformer.isRecycled()) {
                                break;
                            }
                            final float x = event.getX(), y = event.getY();
                            if (selection.marqBoundBeingDragged != null) {
                                hasDraggedBound |= stretchByDraggedMarqueeBound(x, y);
                                activityMain.tvStatus.setText(getString(R.string.state_size,
                                        selection.r.width(), selection.r.height()));
                            }
                        }
                        case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (transformer.isRecycled()) {
                                break;
                            }
                            if (selection.marqBoundBeingDragged != null && hasDraggedBound) {
                                selection.marqBoundBeingDragged = null;
                                hasDraggedBound = false;
                                final int w = selection.r.width(), h = selection.r.height();
                                if (w > 0 && h > 0) {
                                    transformer.stretch(activityMain.optionsTransformer.cbFilter.isChecked(), antiAlias);
                                    selection.r.sort();
                                } else {
                                    selection.r.right = selection.r.left + transformer.getWidth();
                                    selection.r.bottom = selection.r.top + transformer.getHeight();
                                }
                                drawBitmapOntoView(true);
                                drawSelectionOntoView(false);
                            }
                        }
                    }
                }
                case 2 -> {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_MOVE -> {
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            float marqLOnView = toViewX(selection.r.left), marqTOnView = toViewY(selection.r.top),
                                    marqROnView = toViewX(selection.r.right), marqBOnView = toViewY(selection.r.bottom);
                            final float dpbLeft = Math.min(x0 - marqLOnView, x1 - marqLOnView),
                                    dpbTop = Math.min(y0 - marqTOnView, y1 - marqTOnView),
                                    dpbRight = Math.min(marqROnView - x0, marqROnView - x1),
                                    dpbBottom = Math.min(marqBOnView - y0, marqBOnView - y1); // Distances from point to bound
                            final float dpbDiffL = dlpbLeft - dpbLeft, dpbDiffT = dlpbTop - dpbTop,
                                    dpbDiffR = dlpbRight - dpbRight, dpbDiffB = dlpbBottom - dpbBottom;
                            if (activityMain.optionsTransformer.cbLar.isChecked()) {
                                if (Math.abs(dpbDiffL) + Math.abs(dpbDiffR) >= Math.abs(dpbDiffT) + Math.abs(dpbDiffB)) {
                                    selection.r.left -= toUnscaled(dpbDiffL);
                                    selection.r.right += toUnscaled(dpbDiffR);
                                    final float width = selection.r.width(), height = width / aspectRatio;
                                    selection.r.top = (int) (centerY - height / 2.0f);
                                    selection.r.bottom = (int) (centerY + height / 2.0f);
                                    marqTOnView = toViewY(selection.r.top);
                                    marqBOnView = toViewY(selection.r.bottom);
                                    dlpbTop = Math.min(y0 - marqTOnView, y1 - marqTOnView);
                                    dlpbBottom = Math.min(marqBOnView - y0, marqBOnView - y1);
                                } else {
                                    selection.r.top -= toUnscaled(dpbDiffT);
                                    selection.r.bottom += toUnscaled(dpbDiffB);
                                    final float height = selection.r.height(), width = height * aspectRatio;
                                    selection.r.left = (int) (centerX - width / 2.0f);
                                    selection.r.right = (int) (centerX + width / 2.0f);
                                    marqLOnView = toViewX(selection.r.left);
                                    marqROnView = toViewX(selection.r.right);
                                    dlpbLeft = Math.min(x0 - marqLOnView, x1 - marqLOnView);
                                    dlpbRight = Math.min(marqROnView - x0, marqROnView - x1);
                                }
                            } else {
                                selection.r.left -= toUnscaled(dpbDiffL);
                                selection.r.top -= toUnscaled(dpbDiffT);
                                selection.r.right += toUnscaled(dpbDiffR);
                                selection.r.bottom += toUnscaled(dpbDiffB);
                            }
                            drawSelectionOntoView();
                            activityMain.tvStatus.setText(getString(R.string.state_size,
                                    selection.r.width(), selection.r.height()));
                        }
                        case MotionEvent.ACTION_POINTER_DOWN -> {
                            selection.marqBoundBeingDragged = null;
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            final RectF viewSelection = new RectF(
                                    toViewX(selection.r.left), toViewY(selection.r.top),
                                    toViewX(selection.r.right), toViewY(selection.r.bottom));
                            dlpbLeft = Math.min(x0 - viewSelection.left, x1 - viewSelection.left);
                            dlpbTop = Math.min(y0 - viewSelection.top, y1 - viewSelection.top);
                            dlpbRight = Math.min(viewSelection.right - x0, viewSelection.right - x1);
                            dlpbBottom = Math.min(viewSelection.bottom - y0, viewSelection.bottom - y1);
                            if (activityMain.optionsTransformer.cbLar.isChecked()) {
                                aspectRatio = (float) selection.r.width() / (float) selection.r.height();
                                centerX = selection.r.exactCenterX();
                                centerY = selection.r.exactCenterY();
                            }
                            activityMain.tvStatus.setText(getString(R.string.state_size,
                                    selection.r.width(), selection.r.height()));
                        }
                        case MotionEvent.ACTION_POINTER_UP -> {
                            transformer.stretch(activityMain.optionsTransformer.cbFilter.isChecked(), antiAlias);
                            selection.r.sort();
                            drawBitmapOntoView(true);
                            drawSelectionOntoView();
                        }
                    }
                }
            }
        }

        private boolean stretchByDraggedMarqueeBound(float viewX, float viewY) {
            final boolean hasDragged = selection.dragMarqueeBound(viewX, viewY, scale);
            if (activityMain.optionsTransformer.cbLar.isChecked()) {
                if (selection.marqBoundBeingDragged == SelectionTool.Position.LEFT || selection.marqBoundBeingDragged == SelectionTool.Position.RIGHT) {
                    final float halfHeight = selection.r.width() / aspectRatio / 2.0f;
                    selection.r.top = (int) (centerY - halfHeight);
                    selection.r.bottom = (int) (centerY + halfHeight);
                } else if (selection.marqBoundBeingDragged == SelectionTool.Position.TOP || selection.marqBoundBeingDragged == SelectionTool.Position.BOTTOM) {
                    final float halfWidth = selection.r.height() * aspectRatio / 2.0f;
                    selection.r.left = (int) (centerX - halfWidth);
                    selection.r.right = (int) (centerX + halfWidth);
                }
            }
            drawSelectionOntoView(true);
            return hasDragged;
        }
    };

    /**
     * Callback to call on touch image view with translation transformer
     */
    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithTTListener = new OnIVTouchListener() {
        private float dx, dy;

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    final float x = event.getX(), y = event.getY();
                    if (hasSelection) {
                        if (transformer.isRecycled()) {
                            createTransformer();
                        }
                        drawSelectionOntoView(false);
                        activityMain.tvStatus.setText(getString(R.string.state_left_top,
                                selection.r.left, selection.r.top));
                        dx = x - toViewX(selection.r.left);
                        dy = y - toViewY(selection.r.top);
                    } else {
                        activityMain.tvStatus.setText(getString(R.string.state_left_top,
                                layer.left, layer.top));
                        dx = x - toViewX(0);
                        dy = y - toViewY(0);
                    }
                }
                case MotionEvent.ACTION_MOVE -> {
                    final float x = event.getX(), y = event.getY();
                    if (!transformer.isRecycled()) {
                        selection.r.offsetTo(toBitmapX(x - dx), toBitmapY(y - dy));
                        drawSelectionOntoView(true);
                        activityMain.tvStatus.setText(getString(R.string.state_left_top,
                                selection.r.left, selection.r.top));
                    } else {
                        layer.left = toBitmapXAbs(x - dx);
                        layer.top = toBitmapYAbs(y - dy);
                        drawChessboardOntoView();
                        drawGridOntoView();
                        activityMain.tvStatus.setText(getString(R.string.state_left_top,
                                layer.left, layer.top));
                    }
                    drawBitmapOntoView();
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!transformer.isRecycled()) {
                        drawSelectionOntoView(false);
                    }
                    drawBitmapOntoView(true);
                }
            }
        }
    };

    private View.OnTouchListener onIVTouchWithTransformerListener = onIVTouchWithTTListener;

    @SuppressLint({"ClickableViewAccessibility"})
    private final OnIVTouchListener onIVTouchWithZoomToolListener = new OnIVTouchListener() {
        private float dx, dy;
        private float lastPivotX, lastPivotY;
        private double lastDiagonal;

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            switch (event.getPointerCount()) {
                case 1 -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN -> {
                            final float x = event.getX(), y = event.getY();
                            activityMain.tvStatus.setText(getString(R.string.coordinates,
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
                            drawAfterTransformingView(true);
                            dx = x;
                            dy = y;
                        }
                        case MotionEvent.ACTION_UP -> {
                            drawBitmapLastOntoView(true);
                            clearStatus();
                        }
                    }
                }
                case 2 -> {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_MOVE -> {
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            final double diagonal = Math.hypot(x0 - x1, y0 - y1);
                            final double diagonalRatio = diagonal / lastDiagonal;
                            scale = (float) (scale * diagonalRatio);
                            calculateBackgroundSizeOnView();
                            final float pivotX = (x0 + x1) / 2.0f, pivotY = (y0 + y1) / 2.0f;
                            translationX = (float) (pivotX - (lastPivotX - translationX) * diagonalRatio);
                            translationY = (float) (pivotY - (lastPivotY - translationY) * diagonalRatio);
                            drawAfterTransformingView(true);
                            lastPivotX = pivotX;
                            lastPivotY = pivotY;
                            lastDiagonal = diagonal;
                        }
                        case MotionEvent.ACTION_POINTER_DOWN -> {
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            lastPivotX = (x0 + x1) / 2.0f;
                            lastPivotY = (y0 + y1) / 2.0f;
                            lastDiagonal = Math.hypot(x0 - x1, y0 - y1);
                            clearStatus();
                        }
                        case MotionEvent.ACTION_POINTER_UP -> {
                            final int index = 1 - event.getActionIndex();
                            final float x = event.getX(index);
                            final float y = event.getY(index);
                            activityMain.tvStatus.setText(getString(R.string.coordinates,
                                    toBitmapX(x), toBitmapY(y)));
                            dx = x;
                            dy = y;
                        }
                    }
                }
            }
        }
    };

    private View.OnTouchListener onIVTouchListener = onIVTouchWithPencilListener;

    @SuppressLint("NonConstantResourceId")
    private final MaterialButtonToggleGroup.OnButtonCheckedListener onToolButtonCheckedListener = (group, checkedId, isChecked) -> {
        switch (checkedId) {
            case R.id.b_brush -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithBrushListener, activityMain.svOptionsBrush);
                    paint.setAntiAlias(true);
                    activityMain.optionsBrush.tietSoftness.setText(String.valueOf(softness));
                    activityMain.optionsBrush.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                    activityMain.svOptionsBrush.setVisibility(View.VISIBLE);
                    updateBrush();
                }
            }
            case R.id.b_bucket_fill -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithBucketListener, activityMain.svOptionsBucketFill);
                    threshold = 0x0;
                }
            }
            case R.id.b_clone_stamp -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithCloneStampListener, activityMain.svOptionsCloneStamp);
                    activityMain.optionsCloneStamp.cbAntiAlias.setChecked(antiAlias);
                    activityMain.optionsCloneStamp.tietBlurRadius.setText(String.valueOf(blurRadius));
                    activityMain.optionsCloneStamp.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                } else {
                    cloneStampSrc = null;
                }
            }
            case R.id.b_eraser -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithEraserListener, activityMain.svOptionsEraser);
                }
            }
            case R.id.b_eyedropper -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithEyedropperListener, activityMain.svOptionsEyedropper);
                }
            }
            case R.id.b_gradient_line -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithGradientListener, activityMain.svOptionsGradientLine);
                    paint.setBlendMode(BlendMode.SRC);
                    activityMain.optionsGradientLine.cbAntiAlias.setChecked(antiAlias);
                    activityMain.optionsGradientLine.tietBlurRadius.setText(String.valueOf(blurRadius));
                    activityMain.optionsGradientLine.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                    dpPreview.setBitmap(bitmap.getWidth(), bitmap.getHeight());
                } else {
                    paint.setBlendMode(BlendMode.SRC_OVER);
                }
            }
            case R.id.b_magic_eraser -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithMagicEraserListener, activityMain.svOptionsMagicEraser);
                    updateReference();
                    paint.setAntiAlias(false);
                    paint.setMaskFilter(null);
                    paint.setStrokeCap(Paint.Cap.BUTT);
                    activityMain.optionsMagicEraser.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                } else {
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    magEr.b = null;
                    magEr.f = null;
                }
            }
            case R.id.b_magic_paint -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithMagicPaintListener, activityMain.svOptionsMagicPaint);
                    updateReference();
                    threshold = 0xFF;
                    activityMain.optionsMagicPaint.cbAntiAlias.setChecked(antiAlias);
                    activityMain.optionsMagicPaint.tietBlurRadius.setText(String.valueOf(blurRadius));
                    activityMain.optionsMagicPaint.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                }
            }
            case R.id.b_patcher -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithPatcherListener, activityMain.svOptionsPatcher);
                    activityMain.optionsPatcher.cbAntiAlias.setChecked(antiAlias);
                    activityMain.optionsPatcher.tietBlurRadius.setText(String.valueOf(blurRadius));
                    activityMain.optionsPatcher.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                }
            }
            case R.id.b_path -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithPathListener, activityMain.svOptionsPath);
                    activityMain.optionsPath.cbAntiAlias.setChecked(antiAlias);
                    activityMain.optionsPath.cbFill.setChecked(isPaintStyleFill());
                    activityMain.optionsPath.tietBlurRadius.setText(String.valueOf(blurRadius));
                    activityMain.optionsPath.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                }
            }
            case R.id.b_pencil -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithPencilListener, activityMain.svOptionsPencil);
                    activityMain.optionsPencil.cbAntiAlias.setChecked(antiAlias);
                    activityMain.optionsPencil.tietBlurRadius.setText(String.valueOf(blurRadius));
                    activityMain.optionsPencil.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                }
            }
            case R.id.b_ruler -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithRulerListener);
                } else {
                    ruler.enabled = false;
                }
            }
            case R.id.b_selector -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithMarqueeListener);
                }
            }
            case R.id.b_circle, R.id.b_line, R.id.b_oval, R.id.b_rect -> {
                if (isChecked) {
                    if (shape == null) {
                        onToolChanged(onIVTouchWithShapeListener, activityMain.svOptionsShape);
                        activityMain.optionsShape.cbAntiAlias.setChecked(antiAlias);
                        activityMain.optionsShape.cbFill.setChecked(isPaintStyleFill());
                        activityMain.optionsShape.tietBlurRadius.setText(String.valueOf(blurRadius));
                        activityMain.optionsShape.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                        dpPreview.setBitmap(bitmap.getWidth(), bitmap.getHeight());
                    }
                    shape = switch (checkedId) {
                        case R.id.b_line -> line;
                        case R.id.b_rect -> rect;
                        case R.id.b_oval -> oval;
                        case R.id.b_circle -> circle;
                        default -> null;
                    };
                }
            }
            case R.id.b_text -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithTextListener);
                    activityMain.optionsText.cbFill.setChecked(isPaintStyleFill());
                    dpPreview.setBitmap(bitmap.getWidth(), bitmap.getHeight());
                } else {
                    drawTextIntoImage(false);
                }
            }
            case R.id.b_transformer -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithTransformerListener, activityMain.svOptionsTransformer);
                    selector.setColor(Color.BLUE);
                    if (hasSelection && activityMain.optionsTransformer.btgTransformer.getCheckedButtonId() == R.id.b_mesh) {
                        createTransformerMesh();
                    }
                    drawSelectionOntoView();
                } else {
                    drawTransformerIntoImage();
                    transformer.mesh = null;
                    selection.marqBoundBeingDragged = null;
                    selector.setColor(Color.DKGRAY);
                    drawSelectionOntoView();
                }
            }
        }
    };

    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    private final MaterialButtonToggleGroup.OnButtonCheckedListener onTransformerButtonCheckedListener = (OnButtonCheckedListener) (group, checkedId) -> {
        activityMain.optionsTransformer.cbFilter.setVisibility(checkedId != R.id.b_translation ? View.VISIBLE : View.GONE);
        activityMain.optionsTransformer.cbLar.setVisibility(checkedId == R.id.b_scale ? View.VISIBLE : View.GONE);
        activityMain.optionsTransformer.llMesh.setVisibility(checkedId == R.id.b_mesh ? View.VISIBLE : View.GONE);
        if (hasSelection) {
            transformer.apply();
            if (checkedId == R.id.b_mesh) {
                createTransformerMesh();
            } else {
                transformer.mesh = null;
            }
            drawSelectionOntoView();
        }
        onIVTouchWithTransformerListener = switch (checkedId) {
            case R.id.b_translation -> onIVTouchWithTTListener;
            case R.id.b_scale -> onIVTouchWithSTListener;
            case R.id.b_rotation -> onIVTouchWithRTListener;
            case R.id.b_poly -> onIVTouchWithPTListener;
            case R.id.b_mesh -> onIVTouchWithMTListener;
            default -> null;
        };
        activityMain.canvas.flIv.setOnTouchListener(onIVTouchWithTransformerListener);
    };

    @SuppressLint("ClickableViewAccessibility")
    private final MaterialButtonToggleGroup.OnButtonCheckedListener onZoomToolButtonCheckedListener = (group, checkedId, isChecked) -> {
        isZoomingEnabled = isChecked;
    };

    private final CompoundButton.OnCheckedChangeListener onMagicEraserStyleCBCheckedChangeListener = (buttonView, isChecked) -> {
        activityMain.optionsMagicEraser.btgSides.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        activityMain.optionsMagicEraser.cbAccEnabled.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        onIVTouchWithMagicEraserListener = isChecked
                ? onIVTouchWithPreciseMagicEraserListener
                : onIVTouchWithImpreciseMagicEraserListener;
        activityMain.canvas.flIv.setOnTouchListener(onIVTouchWithMagicEraserListener);
        if (!isChecked) {
            magEr.b = null;
            magEr.f = null;
            eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
        }
    };

    private final MessageQueue.IdleHandler onUiThreadWaitForMsgSinceMAHasBeenCreatedHandler = () -> {
        load();
        return false;
    };

    private void addDefaultTab() {
        addProject(Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888), 0);
    }

    private Frame addFrame(Project project, Bitmap bitmap, int position, int delay, boolean setSelected) {
        final Frame frame = new Frame();
        frame.delay = delay;
        addFrame(project, frame, bitmap, position, setSelected);
        return frame;
    }

    private Frame addFrame(Project project, Frame frame, Bitmap bitmap, int position, boolean setSelected) {
        project.frames.add(position, frame);
        frame.layerAdapter.setOnItemSelectedListener(onLayerItemSelectedListener, onLayerItemReselectedListener);
        frame.layerAdapter.setOnLayerVisibleChangedListener((buttonView, isChecked) -> drawBitmapOntoView(true));
        if (bitmap != null) {
            addLayer(project, frame, bitmap, getString(R.string.background), false);
        }
        if (setSelected) {
            selectFrame(project, position);
            if (bsdFrameList != null) {
                frameList.rvFrameList.post(() -> {
                    if (project.selectedFrameIndex > 0) {
                        project.frameAdapter.notifyItemChanged(project.selectedFrameIndex - 1, FrameAdapter.Payload.SELECTED);
                    }
                    project.frameAdapter.notifyItemInserted(position);
                    project.frameAdapter.notifyItemRangeChanged(position + 1, project.frames.size() - position);
                });
            }
        }
        return frame;
    }

    private Layer addLayer(Project project, Frame frame, Bitmap bitmap, String name, boolean setSelected) {
        return addLayer(project, frame, bitmap, 0, 0, name, setSelected);
    }

    private Layer addLayer(Project project, Frame frame, Bitmap bitmap, int position, int level, String name, boolean setSelected) {
        return addLayer(project, frame, bitmap, position, level, 0, 0, true, name, setSelected);
    }

    private Layer addLayer(Project project, Frame frame, Bitmap bitmap, int position, int level, int left, int top,
                           boolean visible, String name, boolean setSelected) {
        final Layer layer = new Layer();
        layer.bitmap = bitmap != null ? bitmap : Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
        layer.setLevel(level);
        layer.moveTo(left, top);
        layer.paint.setBlendMode(BlendMode.SRC_OVER);
        layer.visible = visible;
        layer.name = name;
        fitOnScreen(project, layer);
        return addLayer(frame, layer, position, setSelected);
    }

    private Layer addLayer(Frame frame, Layer layer, int position, boolean setSelected) {
        frame.layers.add(position, layer);
        addToHistory(layer);
        frame.computeLayerTree();
        if (setSelected) {
            hasSelection = false;
            selectLayer(position);
            if (ssdLayerList != null) {
                layerList.rvLayerList.post(() -> {
                    if (frame.selectedLayerIndex < frame.layers.size() - 1) {
                        frame.layerAdapter.notifyItemChanged(frame.selectedLayerIndex, LayerAdapter.Payload.SELECTED);
                    }
                    frame.layerAdapter.notifyItemInserted(position);
                    frame.layerAdapter.notifyItemRangeChanged(position + 1, frame.layers.size() - position);
                    layerList.rvLayerList.post(frame.layerAdapter::notifyLayerTreeChanged);
                });
            }
        }
        return layer;
    }

    private Project addProject(Bitmap bitmap, int position) {
        return addProject(bitmap, position, getString(R.string.untitled));
    }

    private Project addProject(Bitmap bitmap, int position, String title) {
        return addProject(bitmap, position, title, null, null, null);
    }

    private Project addProject(Bitmap bitmap, int position,
                               String title, String path, Project.FileType type, Bitmap.CompressFormat compressFormat) {
        return addProject(bitmap, position, title, path, type, compressFormat, true);
    }

    private Project addProject(Bitmap bitmap, int position,
                               String title, String path, Project.FileType type, Bitmap.CompressFormat compressFormat,
                               boolean setSelected) {
        final Project project = new Project();
        project.filePath = path;
        project.fileType = type;
        project.compressFormat = compressFormat;
        project.setTitle(title);
        addProject(project, bitmap, position, setSelected);
        return project;
    }

    private void addProject(Project project, Bitmap bitmap, int position, boolean setSelected) {
        projects.add(position, project);
        loadTab(project, position);
        project.frameAdapter.setOnItemSelectedListener(onFrameItemSelectedListener, onFrameItemReselectedListener);
        if (bitmap != null) {
            addFrame(project, bitmap, 0, 0, false);
        }
        if (setSelected) {
            selectProject(position);
        }
    }

    private void addToHistory() {
        addToHistory(layer);
    }

    private void addToHistory(Layer layer) {
        layer.history.add(layer.bitmap);
    }

    private void calculateBackgroundSizeOnView() {
        final Bitmap background = frame.getBackgroundLayer().bitmap;
        backgroundScaledW = toScaled(background.getWidth());
        backgroundScaledH = toScaled(background.getHeight());
    }

    @SuppressLint("NonConstantResourceId")
    private boolean checkIfRequireRef() {
        return switch (activityMain.tools.btgTools.getCheckedButtonId()) {
            case R.id.b_brush -> brush.tipShape == BrushTool.TipShape.REF;
            case R.id.b_magic_eraser, R.id.b_magic_paint -> true;
            default -> false;
        };
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
        activityMain.tvStatus.setText("");
    }

    private void closeFrame(int position) {
        if (project.frames.size() > 1) {
            deleteFrame(position);
            final int posToSelect = Math.min(project.selectedFrameIndex, project.frames.size() - 1);
            selectFrame(posToSelect);
            if (bsdFrameList != null) {
                frameList.rvFrameList.post(() -> {
                    project.frameAdapter.notifyItemRemoved(position);
                    project.frameAdapter.notifyItemRangeChanged(posToSelect, project.frames.size() - posToSelect);
                });
            }
        } else {
            closeProject(activityMain.tlProjectList.getSelectedTabPosition());
        }
    }

    private void closeLayer(int position) {
        if (frame.layers.size() > 1) {
            deleteLayer(position);
            frame.computeLayerTree();
            final int posToSelect = Math.min(frame.selectedLayerIndex, frame.layers.size() - 1);
            selectLayer(posToSelect);
            layerList.rvLayerList.post(() -> {
                frame.layerAdapter.notifyItemRemoved(position);
                frame.layerAdapter.notifyItemRangeChanged(posToSelect, frame.layers.size() - posToSelect);
                layerList.rvLayerList.post(frame.layerAdapter::notifyLayerTreeChanged);
            });
        } else {
            closeFrame(project.selectedFrameIndex);
        }
    }

    private void closeProject(int position) {
        if (projects.size() > 1) {
            deleteProject(position);
            selectProject(activityMain.tlProjectList.getSelectedTabPosition());
        } else {
            deleteProject(0);
            addDefaultTab();
        }
    }

    private void createFrame() {
        final Frame firstFrame = project.getFirstFrame();
        final Bitmap ffblb = firstFrame.getBackgroundLayer().bitmap;
        final Bitmap bm = Bitmap.createBitmap(ffblb.getWidth(), ffblb.getHeight(),
                ffblb.getConfig(), ffblb.hasAlpha(), ffblb.getColorSpace());
        addFrame(project, bm, project.frames.size(), firstFrame.delay, true);
    }

    private void createImage(int width, int height) {
        final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        addProject(bm, projects.size());
    }

    private void createLayer(int width, int height,
                             Bitmap.Config config, ColorSpace colorSpace,
                             int level, int left, int top, int position) {
        final Bitmap bm = Bitmap.createBitmap(width, height, config, true, colorSpace);
        addLayer(project, frame, bm, position, level, left, top, true, getString(R.string.layer), true);
    }

    private void createEditPreview() {
        if (editPreview != null) {
            editPreview.recycle();
        }
        if (!hasSelection) {
            selectAll();
        }
        editPreview = new EditPreview(bitmap, selection.r);
    }

    private void createTransformer() {
        final Bitmap bm = Bitmap.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height());
        canvas.drawRect(selection.r.left, selection.r.top, selection.r.right, selection.r.bottom, eraser);
        createTransformer(bm);
    }

    private void createTransformer(Bitmap bitmap) {
        transformer.setBitmap(bitmap, selection.r);
        transformerActionMode = startSupportActionMode(onTransformerActionModeCallback);
        transformerActionMode.setTitle(R.string.transform);
    }

    private void createTransformerMesh() {
        final int w, h;
        try {
            w = Integer.parseUnsignedInt(activityMain.optionsTransformer.tietMeshWidth.getText().toString());
            h = Integer.parseUnsignedInt(activityMain.optionsTransformer.tietMeshHeight.getText().toString());
        } catch (NumberFormatException e) {
            return;
        }
        transformer.rect = selection.r;
        transformer.createMesh(w, h);
    }

    private void deleteFrame(int position) {
        deleteFrame(project, position);
    }

    private void deleteFrame(Project project, int position) {
        final Frame frame = project.frames.get(position);
        for (int i = frame.layers.size() - 1; i >= 0; --i) {
            deleteLayer(frame, i);
        }
        frame.recycleThumbnail();
        project.frames.remove(position);
    }

    private void deleteLayer(int position) {
        deleteLayer(frame, position);
    }

    private void deleteLayer(Frame frame, int position) {
        final Layer layer = frame.layers.get(position);
        if (layer == this.layer) {
            recycleTransformer();
        }
        layer.bitmap.recycle();
        layer.history.clear();
        frame.layers.remove(position);
    }

    private void deleteProject(int position) {
        final Project project = projects.get(position);
        for (int i = project.frames.size() - 1; i >= 0; --i) {
            deleteFrame(project, i);
        }
        activityMain.tlProjectList.removeOnTabSelectedListener(onProjTabSelectedListener);
        activityMain.tlProjectList.removeTabAt(position);
        activityMain.tlProjectList.addOnTabSelectedListener(onProjTabSelectedListener);
        projects.remove(position);
    }

    private Bitmap drawableToBitmap(@DrawableRes int id) {
        final Drawable drawable = ContextCompat.getDrawable(this, id);
        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void drawAfterTransformingView(boolean doNotMerge) {
        if (doNotMerge) {
            drawBitmapLastOntoView(false);
        } else {
            drawBitmapOntoView(true, false);
        }
        if (isEditingText) {
            drawTextGuideOntoView();
        } else if (cloneStampSrc != null) {
            drawCrossOntoView(cloneStampSrc.x, cloneStampSrc.y);
        } else if (ruler.enabled) {
            drawRulerOntoView();
        } else if (magEr.b != null && magEr.f != null) {
            drawCrossOntoView(magEr.b.x, magEr.b.y, true);
            drawCrossOntoView(magEr.f.x, magEr.f.y, false);
        }
        drawChessboardOntoView();
        drawGridOntoView();
        drawSelectionOntoView();
    }

    private void drawBitmapOntoCanvas(Canvas canvas, Bitmap bitmap, float translX, float translY) {
        final Rect vs = getVisibleSubset(translX, translY, bitmap.getWidth(), bitmap.getHeight());
        drawBitmapOntoCanvas(canvas, bitmap, translX, translY, vs);
    }

    private void drawBitmapOntoCanvas(Canvas canvas, Bitmap bitmap, float translX,
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

    private void drawBitmapOntoView() {
        drawBitmapOntoView(false);
    }

    private void drawBitmapOntoView(final boolean wait) {
        drawBitmapOntoView(false, wait);
    }

    private void drawBitmapOntoView(final boolean eraseVisible, final boolean wait) {
        drawBitmapOntoView(bitmap, eraseVisible, wait);
    }

    private void drawBitmapOntoView(final Bitmap bitmap, final boolean eraseVisible, final boolean wait) {
        runOrStart(() -> drawBitmapVisibleOntoView(bitmap, eraseVisible), wait);
    }

    private void drawBitmapOntoView(final Rect rect) {
        drawBitmapOntoView(rect, false);
    }

    private void drawBitmapOntoView(final Rect rect, final boolean wait) {
        drawBitmapOntoView(bitmap, rect, wait);
    }

    private void drawBitmapOntoView(final Bitmap bitmap, final Rect rect) {
        drawBitmapOntoView(bitmap, rect, false);
    }

    private void drawBitmapOntoView(final Bitmap bitmap, final boolean wait) {
        drawBitmapOntoView(bitmap, false, wait);
    }

    private void drawBitmapOntoView(final Bitmap bitmap, final Rect rect, final boolean wait) {
        drawBitmapOntoView(bitmap, rect.left, rect.top, rect.right, rect.bottom, wait);
    }

    private void drawBitmapOntoView(final int left, final int top, final int right,
                                    final int bottom) {
        drawBitmapOntoView(bitmap, left, top, right, bottom, false);
    }

    private void drawBitmapOntoView(final Bitmap bitmap,
                                    final int left, final int top, final int right, final int bottom,
                                    final boolean wait) {
        runOrStart(() -> drawBitmapSubsetOntoView(bitmap,
                layer.left + left, layer.top + top, layer.left + right, layer.top + bottom), wait);
    }

    private void drawBitmapOntoView(final int x0, final int y0, final int x1, final int y1,
                                    final float radius) {
        final boolean x = x0 <= x1, y = y0 <= y1;
        final int rad = (int) Math.ceil(radius);
        final int left = layer.left + (x ? x0 : x1) - rad, top = layer.top + (y ? y0 : y1) - rad,
                right = layer.left + (x ? x1 : x0) + rad + 1, bottom = layer.top + (y ? y1 : y0) + rad + 1;
        runOrStart(() ->
                drawBitmapSubsetOntoView(bitmap, left, top, right, bottom));
    }

    private final Runnable mergeLayersEntireRunner = () -> {
        final Bitmap background = frame.getBackgroundLayer().bitmap;
        final Rect vs = new Rect(0, 0, background.getWidth(), background.getHeight());

        final Bitmap merged = Layers.mergeLayers(frame.layerTree, vs, layer, bitmap, getCurrentFloatingLayer());
        BitmapUtils.recycle(lastMerged);
        lastMerged = merged;
    };

    private Rect mapRect(int x0, int y0, int x1, int y1, float radius) {
        final boolean x = x0 <= x1, y = y0 <= y1;
        final int rad = (int) Math.ceil(radius);
        final int left = layer.left + (x ? x0 : x1) - rad, top = layer.top + (y ? y0 : y1) - rad,
                right = layer.left + (x ? x1 : x0) + rad + 1, bottom = layer.top + (y ? y1 : y0) + rad + 1;
        return new Rect(left, top, right, bottom);
    }

    private void mergeLayersEntire() {
        runOrStart(mergeLayersEntireRunner, true);
    }

    private void drawBitmapEntireOntoView(final boolean wait) {
        mergeLayersEntire();
        drawBitmapLastOntoView(wait);
    }

    private void drawBitmapLastOntoView(final boolean wait) {
        runOrStart(this::drawBitmapLastOntoView, wait);
    }

    private void drawBitmapLastOntoView() {
        final Bitmap background = frame.getBackgroundLayer().bitmap;
        final Rect vs = getVisibleSubset(translationX, translationY,
                background.getWidth(), background.getHeight());

        runOnUiThread(() -> {
            eraseBitmap(viewBitmap);
            if (!vs.isEmpty()) {
                drawBitmapOntoCanvas(viewCanvas, lastMerged, translationX, translationY, vs);
            }
            activityMain.canvas.iv.invalidate();
        });
    }

    private void drawBitmapSubsetOntoView(final Bitmap bitmap,
                                          int left, int top, int right, int bottom) {
        final Bitmap background = frame.getBackgroundLayer().bitmap;
        final int width = background.getWidth(), height = background.getHeight();
        left = Math.max(left, 0);
        top = Math.max(top, 0);
        right = Math.min(right, width);
        bottom = Math.min(bottom, height);
        if (left >= right || top >= bottom) {
            return;
        }
        final Rect vs = getVisibleSubset(translationX, translationY, width, height);
        if (vs.isEmpty()) {
            runOnUiThread(() -> {
                eraseBitmap(viewBitmap);
                activityMain.canvas.iv.invalidate();
            });
            return;
        }
        if (!vs.intersect(left, top, right, bottom)) {
            return;
        }

        final Bitmap merged = Layers.mergeLayers(frame.layerTree, vs, layer, bitmap, getCurrentFloatingLayer());
        if (onIVTouchListener instanceof final OnIVMultiTouchListener oml && oml.isMultiTouch()) {
            merged.recycle();
            return;
        }
        BitmapUtils.recycle(lastMerged);
        lastMerged = null;
        final float translLeft = toViewXRel(left), translTop = toViewYRel(top);
        final Rect relative = new Rect(0, 0, vs.width(), vs.height());
        runOnUiThread(() -> {
            drawBitmapOntoCanvas(viewCanvas, merged,
                    translLeft > -scale ? translLeft : translLeft % scale,
                    translTop > -scale ? translTop : translTop % scale,
                    relative);
            merged.recycle();
            activityMain.canvas.iv.invalidate();
        });
    }

    private void drawBitmapVisibleOntoView(final Bitmap bitmap, final boolean eraseVisible) {
        final Bitmap background = frame.getBackgroundLayer().bitmap;
        final Rect vs = getVisibleSubset(translationX, translationY, background.getWidth(), background.getHeight());
        if (vs.isEmpty()) {
            runOnUiThread(() -> {
                eraseBitmap(viewBitmap);
                activityMain.canvas.iv.invalidate();
            });
            return;
        }

        final Bitmap merged = Layers.mergeLayers(frame.layerTree, vs, layer, bitmap, getCurrentFloatingLayer());
        if (onIVTouchListener instanceof final OnIVMultiTouchListener oml && oml.isMultiTouch()) {
            merged.recycle();
            return;
        }
        BitmapUtils.recycle(lastMerged);
        lastMerged = null;
        final Rect relative = new Rect(0, 0, vs.width(), vs.height());
        runOnUiThread(() -> {
            if (eraseVisible) {
                eraseBitmap(viewBitmap);
            }
            drawBitmapOntoCanvas(viewCanvas, merged,
                    translationX > -scale ? translationX : translationX % scale,
                    translationY > -scale ? translationY : translationY % scale,
                    relative);
            merged.recycle();
            activityMain.canvas.iv.invalidate();
        });
    }

    private void drawChessboardOntoView() {
        eraseBitmap(chessboardBitmap);

        final float left = Math.max(0.0f, translationX);
        final float top = Math.max(0.0f, translationY);
        final float right = Math.min(translationX + backgroundScaledW, viewWidth);
        final float bottom = Math.min(translationY + backgroundScaledH, viewHeight);

        chessboardCanvas.drawRect(left, top, right, bottom, chessboardPaint);
        activityMain.canvas.ivChessboard.invalidate();
        drawRuler();
    }

    private void drawCrossOntoView(float x, float y) {
        drawCrossOntoView(x, y, true);
    }

    private void drawCrossOntoView(float x, float y, boolean isFirst) {
        if (isFirst) {
            eraseBitmap(previewBitmap);
        }
        final float viewX = toViewX(x), viewY = toViewY(y);
        previewCanvas.drawLine(viewX - 50.0f, viewY, viewX + 50.0f, viewY, selector);
        previewCanvas.drawLine(viewX, viewY - 50.0f, viewX, viewY + 50.0f, selector);
        activityMain.canvas.ivPreview.invalidate();
    }

    private void drawEditPreviewIntoImage() {
        canvas.drawBitmap(editPreview.getEntire(), 0.0f, 0.0f, PAINT_BITMAP);
        drawBitmapOntoView(true);
    }

    private void drawEditPreviewOntoView() {
        drawEditPreviewOntoView(false);
    }

    private void drawEditPreviewOntoView(final boolean wait) {
        drawBitmapOntoView(editPreview.getEntire(), selection.r, wait);
    }

    private void drawFloatingLayersIntoImage() {
        drawTransformerIntoImage();
        drawTextIntoImage();
    }

    private void drawGridOntoView() {
        eraseBitmap(gridBitmap);

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

        final CellGrid cellGrid = layer.cellGrid;
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

        for (final Guide guide : layer.guides) {
            if (guide.orientation) {
                final float x = toViewX(guide.position);
                gridCanvas.drawLine(x, 0.0f, x, viewHeight, PAINT_GUIDE);
            } else {
                final float y = toViewY(guide.position);
                gridCanvas.drawLine(0.0f, y, viewWidth, y, PAINT_GUIDE);
            }
        }

        activityMain.canvas.ivGrid.invalidate();
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

        activityMain.canvas.ivRulerH.invalidate();
        activityMain.canvas.ivRulerV.invalidate();
    }

    private void drawPointOntoView(int x, int y) {
        fillPaint.setColor(paint.getColorLong());
        eraseBitmap(previewBitmap);
        final float left = toViewX(x), top = toViewY(y), right = left + scale, bottom = top + scale;
        previewCanvas.drawRect(left, top, right, bottom, fillPaint);
        activityMain.canvas.ivPreview.invalidate();
    }

    private void drawRulerOntoView() {
        eraseBitmap(previewBitmap);
        previewCanvas.drawLine(
                toViewX(ruler.startX), toViewY(ruler.startY),
                toViewX(ruler.stopX), toViewY(ruler.stopY),
                PAINT_CELL_GRID);
        activityMain.canvas.ivPreview.invalidate();
    }

    private void drawSelectionOntoView() {
        drawSelectionOntoView(false);
    }

    private void drawSelectionOntoView(boolean showMargins) {
        eraseBitmap(selectionBitmap);
        if (hasSelection) {
            final float left = Math.max(0.0f, toViewX(selection.r.left)),
                    top = Math.max(0.0f, toViewY(selection.r.top)),
                    right = Math.min(viewWidth, toViewX(selection.r.right)),
                    bottom = Math.min(viewHeight, toViewY(selection.r.bottom));
            selectionCanvas.drawRect(left, top, right, bottom, selector);
            if (showMargins) {
                final float viewImLeft = Math.max(0.0f, translationX),
                        viewImTop = Math.max(0.0f, translationY),
                        viewImRight = Math.min(viewWidth, translationX + backgroundScaledW),
                        viewImBottom = Math.min(viewHeight, translationY + backgroundScaledH);
                final float centerHorizontal = (left + right) / 2.0f,
                        centerVertical = (top + bottom) / 2.0f;
                if (Math.max(left, viewImLeft) > 0.0f) {
                    selectionCanvas.drawLine(left, centerVertical, viewImLeft, centerVertical, marginPaint);
                    selectionCanvas.drawText(String.valueOf(layer.left + selection.r.left), (viewImLeft + left) / 2.0f, centerVertical, marginPaint);
                }
                if (Math.max(top, viewImTop) > 0.0f) {
                    selectionCanvas.drawLine(centerHorizontal, top, centerHorizontal, viewImTop, marginPaint);
                    selectionCanvas.drawText(String.valueOf(layer.top + selection.r.top), centerHorizontal, (viewImTop + top) / 2.0f, marginPaint);
                }
                if (Math.min(right, viewImRight) < viewWidth) {
                    selectionCanvas.drawLine(right, centerVertical, viewImRight, centerVertical, marginPaint);
                    selectionCanvas.drawText(String.valueOf(layer.left + bitmap.getWidth() - selection.r.right), (viewImRight + right) / 2.0f, centerVertical, marginPaint);
                }
                if (Math.min(bottom, viewImBottom) < viewHeight) {
                    selectionCanvas.drawLine(centerHorizontal, bottom, centerHorizontal, viewImBottom, marginPaint);
                    selectionCanvas.drawText(String.valueOf(layer.top + bitmap.getHeight() - selection.r.bottom), centerHorizontal, (viewImBottom + bottom) / 2.0f, marginPaint);
                }
            }
            if (transformer.mesh != null) {
                final Transformer.Mesh mesh = transformer.mesh;
                for (int i = 0, r = 0; r <= mesh.height; ++r) {
                    for (int c = 0; c <= mesh.width; ++c, i += 2) {
                        final float x = toViewX(selection.r.left + mesh.verts[i]), y = toViewY(selection.r.top + mesh.verts[i + 1]);
                        if (c < mesh.width) {
                            selectionCanvas.drawLine(x, y,
                                    toViewX(selection.r.left + mesh.verts[(r * (mesh.width + 1) + (c + 1)) * 2]),
                                    toViewY(selection.r.top + mesh.verts[(r * (mesh.width + 1) + (c + 1)) * 2 + 1]),
                                    selector);
                        }
                        if (r < mesh.height) {
                            selectionCanvas.drawLine(x, y,
                                    toViewX(selection.r.left + mesh.verts[((r + 1) * (mesh.width + 1) + c) * 2]),
                                    toViewY(selection.r.top + mesh.verts[((r + 1) * (mesh.width + 1) + c) * 2 + 1]),
                                    selector);
                        }
                    }
                }
            }
        }
        activityMain.canvas.ivSelection.invalidate();
    }

    private void drawTextIntoImage() {
        drawTextIntoImage(true);
    }

    private void drawTextIntoImage(boolean hideOptions) {
        if (!isEditingText) {
            return;
        }
        isEditingText = false;
        if (textActionMode != null) {
            textActionMode.finish();
            textActionMode = null;
        }
        canvas.drawText(text.s, text.x, text.y, paint);
        dpPreview.erase();
        drawBitmapOntoView(true);
        eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
        hideSoftInputFromWindow();
        if (hideOptions) {
            activityMain.llOptionsText.setVisibility(View.INVISIBLE);
        }
        addToHistory();
    }

    private void drawTextGuideOntoView() {
        eraseBitmap(previewBitmap);
        final float x = toViewX(text.x), y = toViewY(text.y);
        final Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        final float centerVertical = y + toScaled(fontMetrics.ascent) / 2.0f;
        previewCanvas.drawLine(x, 0.0f, x, viewHeight, PAINT_CELL_GRID);
        previewCanvas.drawLine(0.0f, y, viewWidth, y, PAINT_TEXT_LINE);
        previewCanvas.drawLine(0.0f, centerVertical, viewWidth, centerVertical, PAINT_CELL_GRID);
        activityMain.canvas.ivPreview.invalidate();
    }

    private void drawTextOntoView(String s) {
        text.s = s;
        drawTextOntoView();
    }

    private void drawTextOntoView() {
        if (!isEditingText) {
            return;
        }
        dpPreview.erase();
        dpPreview.getCanvas().drawText(text.s, text.x, text.y, paint);
        drawBitmapOntoView();
        drawTextGuideOntoView();
    }

    private void drawTransformerIntoImage() {
        if (transformer.isRecycled() || !hasSelection) {
            return;
        }
        canvas.drawBitmap(transformer.getBitmap(), selection.r.left, selection.r.top, PAINT_SRC_OVER);
        recycleTransformer();
        drawBitmapOntoView(selection.r);
        optimizeSelection();
        if (activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_transformer
                && activityMain.optionsTransformer.btgTransformer.getCheckedButtonId() == R.id.b_mesh) {
            createTransformerMesh();
        }
        drawSelectionOntoView();
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

    private void export(Project project) {
        if (project == null || project.filePath == null) {
            if (!checkOrRequestPermission()) {
                return;
            }
            exportAs();
            return;
        }
        int quality = 100;
        if (project.fileType == Project.FileType.GIF) {
            if (project.gifEncodingType == null) {
                exportInQuality(project);
                return;
            }
        } else if (project.fileType != Project.FileType.PNG) {
            quality = project.quality;
            if (quality < 0) {
                exportInQuality(project);
                return;
            }
        }
        drawFloatingLayersIntoImage();
        FileUtils.export(this, project, quality);
    }

    private void exportAs() {
        dirSelector.open(project, new Project(), this::exportInQuality);
    }

    private void exportInQuality(Project project) {
        setQuality(project, this::export);
    }

    private void fitOnScreen() {
        fitOnScreen(project, layer);
    }

    private void fitOnScreen(Project project, Layer layer) {
        final float width = layer.bitmap.getWidth(), height = layer.bitmap.getHeight();
        final float scaleW = (float) viewWidth / width, scaleH = (float) viewHeight / height;
        if (scaleW <= scaleH) {
            project.scale = scaleW;
            project.translationX = 0.0f;
            project.translationY = (viewHeight >> 1) - height * scaleW / 2.0f;
        } else {
            project.scale = scaleH;
            project.translationX = (viewWidth >> 1) - width * scaleH / 2.0f;
            project.translationY = 0.0f;
        }
    }

    ActivityMainBinding getBinding() {
        return activityMain;
    }

    private FloatingLayer getCurrentFloatingLayer() {
        FloatingLayer floatingLayer = null;
        if (!transformer.isRecycled()) {
            floatingLayer = transformer;
        } else if (!dpPreview.isRecycled()) {
            floatingLayer = dpPreview;
        }
        return floatingLayer;
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
        for (int i = 0; i < activityMain.flToolOptions.getChildCount(); ++i) {
            activityMain.flToolOptions.getChildAt(i).setVisibility(View.INVISIBLE);
        }
    }

    private void initColorAdapter() {
        colorAdapter.setOnItemClickListener((view, color) -> {
            paint.setColor(color);
            activityMain.vForegroundColor.setBackgroundColor(Color.toArgb(color));
            onPaintColorChanged();
        });
        colorAdapter.setOnItemLongClickListener((view, color) -> {
            RgbColorPicker.make(this, R.string.swatch,
                            (oldColor, newColor) -> {
                                final int index = palette.indexOf(oldColor);
                                if (newColor != null) {
                                    palette.set(index, newColor);
                                    colorAdapter.notifyItemChanged(index);
                                } else {
                                    palette.remove(index);
                                    colorAdapter.notifyItemRemoved(index);
                                }
                            },
                            color, R.string.delete)
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
        viewWidth = activityMain.canvas.iv.getWidth();
        viewHeight = activityMain.canvas.iv.getHeight();
        rulerHHeight = activityMain.canvas.ivRulerH.getHeight();
        rulerVWidth = activityMain.canvas.ivRulerV.getWidth();

        viewBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        activityMain.canvas.iv.setImageBitmap(viewBitmap);

        rulerHBitmap = Bitmap.createBitmap(viewWidth, activityMain.canvas.ivRulerH.getHeight(), Bitmap.Config.ARGB_4444);
        rulerHCanvas = new Canvas(rulerHBitmap);
        activityMain.canvas.ivRulerH.setImageBitmap(rulerHBitmap);
        rulerVBitmap = Bitmap.createBitmap(activityMain.canvas.ivRulerV.getWidth(), viewHeight, Bitmap.Config.ARGB_4444);
        rulerVCanvas = new Canvas(rulerVBitmap);
        activityMain.canvas.ivRulerV.setImageBitmap(rulerVBitmap);

        chessboardBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        chessboardCanvas = new Canvas(chessboardBitmap);
        activityMain.canvas.ivChessboard.setImageBitmap(chessboardBitmap);

        gridBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        gridCanvas = new Canvas(gridBitmap);
        activityMain.canvas.ivGrid.setImageBitmap(gridBitmap);

        previewBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        previewCanvas = new Canvas(previewBitmap);
        activityMain.canvas.ivPreview.setImageBitmap(previewBitmap);

        selectionBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        selectionCanvas = new Canvas(selectionBitmap);
        activityMain.canvas.ivSelection.setImageBitmap(selectionBitmap);

        if (projects.isEmpty()) {
            if (fileToOpen != null) {
                openFile(fileToOpen);
            } else {
                addDefaultTab();
            }
            activityMain.tools.btgTools.check(R.id.b_pencil);
        } else {
            for (int i = 0; i < projects.size(); ++i) {
                final Project p = projects.get(i);
                loadTab(p, i);
                p.frameAdapter.setOnItemSelectedListener(onFrameItemSelectedListener, onFrameItemReselectedListener);
                for (final Frame f : p.frames) {
                    f.layerAdapter.setOnItemSelectedListener(onLayerItemSelectedListener, onLayerItemReselectedListener);
                }
            }
            selectProject(0);
        }

        {
            final ViewGroup.LayoutParams lpNeg = activityMain.vBlockerNeg.getLayoutParams(), lpPos = activityMain.vBlockerPos.getLayoutParams();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                lpNeg.width = activityMain.llCanvas.getLeft();
                lpPos.width = activityMain.llTl.getHeight();
            } else {
                final View canvasView = activityMain.canvas.getRoot();
                lpNeg.height = canvasView.getTop();
                lpPos.height = activityMain.getRoot().getHeight() - lpNeg.height - canvasView.getHeight();
            }
            activityMain.vBlockerNeg.setLayoutParams(lpNeg);
            activityMain.vBlockerPos.setLayoutParams(lpPos);
        }
    }

    private void loadTab(Project project, int position) {
        final TabLayout.Tab t = activityMain.tlProjectList.newTab().setText(project.getTitle()).setTag(project);
        project.tab = t;
        activityMain.tlProjectList.addTab(t, position, false);
    }

    @Override
    @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Preferences
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Settings.INST.mainActivity = this;
        Settings.INST.update(preferences);

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

        // Content view
        final LayoutInflater layoutInflater = getLayoutInflater();
        activityMain = ActivityMainBinding.inflate(layoutInflater);
        setContentView(activityMain.getRoot());

        frameList = FrameListBinding.bind(layoutInflater.inflate(R.layout.frame_list, null));

        layerList = LayerListBinding.bind(layoutInflater.inflate(R.layout.layer_list, null));
        layerList.bNew.setOnClickListener(v -> onLayerOptionsItemSelected(null, R.id.i_layer_new));
        layerList.bDuplicate.setOnClickListener(v -> onLayerOptionsItemSelected(null, R.id.i_layer_duplicate));
        layerList.bDelete.setOnClickListener(v -> onLayerOptionsItemSelected(null, R.id.i_layer_delete));

        vContent = findViewById(android.R.id.content);
        final ViewModel viewModel = new ViewModelProvider(this).get(ViewModel.class);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        activityMain.tools.btgTools.addOnButtonCheckedListener(onToolButtonCheckedListener);
        activityMain.btgZoom.addOnButtonCheckedListener(onZoomToolButtonCheckedListener);
        activityMain.optionsBrush.bTipShape.setOnClickListener(onBrushTipShapeButtonClickListener);
        activityMain.optionsBucketFill.bTolerance.setOnClickListener(onToleranceButtonClickListener);
        activityMain.optionsCloneStamp.bSrc.setOnClickListener(onCloneStampSrcButtonClickListener);
        activityMain.optionsMagicPaint.bTolerance.setOnClickListener(onToleranceButtonClickListener);
        activityMain.bSwatchesAdd.setOnClickListener(onAddSwatchButtonClickListener);
        activityMain.optionsText.bDraw.setOnClickListener(v -> drawTextIntoImage());
        activityMain.optionsTransformer.btgTransformer.addOnButtonCheckedListener(onTransformerButtonCheckedListener);
        activityMain.optionsCloneStamp.cbAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        activityMain.optionsEraser.cbAntiAlias.setOnCheckedChangeListener((buttonView, isChecked) -> eraser.setAntiAlias(isChecked));
        activityMain.optionsGradientLine.cbAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        activityMain.optionsMagicEraser.cbStyle.setOnCheckedChangeListener(onMagicEraserStyleCBCheckedChangeListener);
        activityMain.optionsMagicPaint.cbAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        activityMain.optionsMagicPaint.cbClear.setOnCheckedChangeListener(((buttonView, isChecked) -> magicPaint.setBlendMode(isChecked ? BlendMode.DST_OUT : null)));
        activityMain.optionsPatcher.cbAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        activityMain.optionsPath.cbAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        activityMain.optionsPath.cbFill.setOnCheckedChangeListener(onFillCBCheckedChangeListener);
        activityMain.optionsPencil.cbAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        activityMain.optionsShape.cbAntiAlias.setOnCheckedChangeListener(onAntiAliasCBCheckedChangeListener);
        activityMain.optionsShape.cbFill.setOnCheckedChangeListener(onFillCBCheckedChangeListener);
        activityMain.optionsText.cbFill.setOnCheckedChangeListener(onTextFillCBCheckedChangeListener);
        activityMain.optionsTransformer.cbFilter.setChecked(true);
        activityMain.optionsTransformer.cbFilter.setOnCheckedChangeListener(onTransformerFilterCheckedChangeListener);
        activityMain.canvas.flIv.setOnTouchListener(onIVTouchWithPencilListener);
        activityMain.canvas.ivRulerH.setOnTouchListener(onTouchRulerHListener);
        activityMain.canvas.ivRulerV.setOnTouchListener(onTouchRulerVListener);
        activityMain.rvSwatches.setItemAnimator(new DefaultItemAnimator());
        activityMain.tlProjectList.addOnTabSelectedListener(onProjTabSelectedListener);
        activityMain.optionsBrush.tietSoftness.addTextChangedListener(onSoftnessETTextChangedListener);
        activityMain.optionsBrush.tietStrokeWidth.addTextChangedListener(onSoftStrokeWidthETTextChangedListener);
        activityMain.optionsCloneStamp.tietBlurRadius.addTextChangedListener(onBlurRadiusETTextChangedListener);
        activityMain.optionsCloneStamp.tietStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        activityMain.optionsEraser.tietBlurRadius.addTextChangedListener(onEraserBlurRadiusETTextChangedListener);
        activityMain.optionsEraser.tietStrokeWidth.addTextChangedListener(onEraserStrokeWidthETTextChangedListener);
        activityMain.optionsMagicEraser.tietStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        activityMain.optionsMagicPaint.tietBlurRadius.addTextChangedListener(onBlurRadiusETTextChangedListener);
        activityMain.optionsMagicPaint.tietStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        activityMain.optionsGradientLine.tietBlurRadius.addTextChangedListener(onBlurRadiusETTextChangedListener);
        activityMain.optionsGradientLine.tietStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        activityMain.optionsPatcher.tietBlurRadius.addTextChangedListener(onBlurRadiusETTextChangedListener);
        activityMain.optionsPatcher.tietStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        activityMain.optionsPath.tietBlurRadius.addTextChangedListener(onBlurRadiusETTextChangedListener);
        activityMain.optionsPath.tietStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        activityMain.optionsPencil.tietBlurRadius.addTextChangedListener(onBlurRadiusETTextChangedListener);
        activityMain.optionsPencil.tietStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        activityMain.optionsShape.tietBlurRadius.addTextChangedListener(onBlurRadiusETTextChangedListener);
        activityMain.optionsShape.tietStrokeWidth.addTextChangedListener(onStrokeWidthETTextChangedListener);
        activityMain.optionsText.tietText.addTextChangedListener((AfterTextChangedListener) this::drawTextOntoView);
        activityMain.optionsText.tietTextSize.addTextChangedListener(onTextSizeETTextChangedListener);
        activityMain.optionsTransformer.tietMeshWidth.addTextChangedListener(onTransformerMeshSizeETTextChangedListener);
        activityMain.optionsTransformer.tietMeshHeight.addTextChangedListener(onTransformerMeshSizeETTextChangedListener);
        activityMain.vBackgroundColor.setOnClickListener(onBackgroundColorClickListener);
        activityMain.vForegroundColor.setOnClickListener(onForegroundColorClickListener);

        if (!isLandscape) {
            setSupportActionBar(activityMain.topAppBar.toolBar);
        }

        {
            final View.OnLongClickListener onToolLongClickListener = v -> {
                ToolSelector.show(this, (TextView) v);
                return true;
            };
            activityMain.tools.bBrush.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bCircle.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bEraser.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bEyedropper.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bGradientLine.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bLine.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bMagicEraser.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bMagicPaint.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bOval.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bPencil.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bRect.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bRuler.setOnLongClickListener(onToolLongClickListener);
        }

        frameList.rvFrameList.setItemAnimator(new DefaultItemAnimator());
        ItemMovableAdapter.createItemMoveHelper(onFrameItemMoveListener).attachToRecyclerView(frameList.rvFrameList);

        layerList.rvLayerList.setItemAnimator(new DefaultItemAnimator());
        ItemMovableAdapter.createItemMoveHelper(onLayerItemMoveListener).attachToRecyclerView(layerList.rvLayerList);

        activityMain.optionsBrush.tietSoftness.setText(String.valueOf(softness));
        activityMain.optionsEraser.tietBlurRadius.setText(String.valueOf(0.0f));
        activityMain.optionsEraser.tietStrokeWidth.setText(String.valueOf(eraser.getStrokeWidth()));
        activityMain.optionsPencil.tietBlurRadius.setText(String.valueOf(0.0f));
        activityMain.optionsPencil.tietStrokeWidth.setText(String.valueOf(paint.getStrokeWidth()));
        activityMain.optionsText.tietTextSize.setText(String.valueOf(paint.getTextSize()));
        activityMain.optionsTransformer.tietMeshWidth.setText(String.valueOf(2));
        activityMain.optionsTransformer.tietMeshHeight.setText(String.valueOf(2));

        final Resources res = getResources();

        chessboard = BitmapFactory.decodeResource(res, R.mipmap.chessboard);
        chessboardPaint.setShader(new BitmapShader(chessboard, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        fileToOpen = getIntent().getData();
        projects = viewModel.getProjects();

        palette = viewModel.getPalette();
        colorAdapter = new ColorAdapter(palette);
        initColorAdapter();
        activityMain.rvSwatches.setAdapter(colorAdapter);
        ItemMovableAdapter.createItemMoveHelper(null).attachToRecyclerView(activityMain.rvSwatches);

        brush.setBrush(drawableToBitmap(R.drawable.brush_tip_shape));

        if (isLandscape) {
            final LinearLayout ll = activityMain.llTl;
            OneShotPreDrawListener.add(ll, () -> {
                final int width = ll.getMeasuredHeight(), height = activityMain.tlProjectList.getMeasuredHeight();
                final ViewGroup.LayoutParams lp = ll.getLayoutParams();
                lp.width = width;
                lp.height = height;
                ll.setLayoutParams(lp);
                final float radius = height >> 1;
                ll.setPivotX(width - radius);
                ll.setPivotY(radius);
                ll.setRotation(90.0f);
            });
            Toast.makeText(this, R.string.please_switch_orientation_to_vertical_to_get_all_functions, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.setGroupDividerEnabled(true);
        miFrameList = menu.findItem(R.id.i_frame_list);
        miHasAlpha = menu.findItem(R.id.i_image_has_alpha);
        Settings.INST.update(PreferenceManager.getDefaultSharedPreferences(this), Settings.KEY_FL);
        return true;
    }

    @Override
    protected void onDestroy() {
        recycleAllBitmaps();
        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    private boolean onFrameOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default -> {
                return false;
            }
            case R.id.i_frame_clip -> {
                new AnimationClipper(this, project, onClipConfirmListener).show();
            }
            case R.id.i_frame_delay -> {
                new EditNumberDialog(this)
                        .setIcon(item.getIcon()).setTitle(R.string.delay)
                        .setOnApplyListener(number -> {
                            frame.delay = number;
                            frameList.rvFrameList.post(() ->
                                    project.frameAdapter.notifyItemChanged(project.selectedFrameIndex, FrameAdapter.Payload.DELAY));
                        })
                        .show(frame.delay, "ms");
            }
            case R.id.i_frame_delete -> closeFrame(project.selectedFrameIndex);
            case R.id.i_frame_duplicate -> {
                final int unselectedPos = project.selectedFrameIndex, pos = unselectedPos + 1;
                final Frame src = frame;
                final Frame dst = addFrame(project, null, pos, src.delay, false);
                for (int i = 0; i < src.layers.size(); ++i) {
                    final Layer l = src.layers.get(i);
                    addLayer(dst, new Layer(l), i, false);
                }
                frameList.rvFrameList.post(() -> {
                    project.frameAdapter.notifyItemChanged(unselectedPos, FrameAdapter.Payload.SELECTED);
                    project.frameAdapter.notifyItemInserted(pos);
                    project.frameAdapter.notifyItemRangeChanged(pos + 1, project.frames.size() - pos - 2);
                });
                selectFrame(pos);
            }
            case R.id.i_frame_new -> createFrame();
            case R.id.i_frame_unify_delays -> {
                new EditNumberDialog(this)
                        .setIcon(R.drawable.ic_access_time).setTitle(R.string.delay)
                        .setOnApplyListener(number -> {
                            project.frames.forEach(f -> f.delay = number);
                            frameList.rvFrameList.post(() ->
                                    project.frameAdapter.notifyItemRangeChanged(0, project.frames.size(), FrameAdapter.Payload.DELAY));
                        })
                        .show(frame.delay, "ms");
            }
        }
        return true;
    }

    private boolean onLayerOptionsItemSelected(MenuItem item) {
        return onLayerOptionsItemSelected(item, item.getItemId());
    }

    @SuppressLint({"NonConstantResourceId", "StringFormatMatches"})
    private boolean onLayerOptionsItemSelected(MenuItem item, int itemId) {
        switch (itemId) {
            default -> {
                return false;
            }
            case R.id.i_layer_add_layer_mask -> {
                final Layer l = new Layer();
                l.bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                        bitmap.getConfig(), true, bitmap.getColorSpace());
                l.name = getString(R.string.mask);
                l.setLevel(layer.getLevel() + 1);
                l.moveTo(layer.left, layer.top);
                l.paint.setBlendMode(BlendMode.DST_OUT);
                if (hasSelection) {
                    final Canvas cv = new Canvas(l.bitmap);
                    l.bitmap.eraseColor(Color.BLACK);
                    cv.drawRect(selection.r, PAINT_DST_OUT);
                }
                addLayer(frame, l, frame.selectedLayerIndex, true);
            }
            case R.id.i_layer_add_filter_layer -> {
                final Layer l = new Layer();
                l.bitmap = Bitmap.createBitmap(
                        hasSelection ? selection.r.width() : bitmap.getWidth(),
                        hasSelection ? selection.r.height() : bitmap.getHeight(),
                        bitmap.getConfig(), true, bitmap.getColorSpace());
                l.name = getString(R.string.filter_noun);
                l.setLevel(Settings.INST.newLayerLevel() ? layer.getLevel() : 0);
                l.paint.setBlendMode(BlendMode.SRC_OVER);
                l.passBelow = true;
                if (Settings.INST.newLayerLevel() || hasSelection) {
                    l.moveBy(layer.left, layer.top);
                    if (hasSelection) {
                        l.moveBy(selection.r.left, selection.r.top);
                    }
                }
                addLayer(frame, l, frame.selectedLayerIndex, true);
            }
            case R.id.i_layer_alpha -> {
                if (ssdLayerList != null) {
                    ssdLayerList.dismiss();
                }
                new SliderDialog(this)
                        .setIcon(item.getIcon()).setTitle(R.string.alpha)
                        .setValueFrom(0x00).setValueTo(0xFF).setValue(layer.paint.getAlpha())
                        .setStepSize(1.0f)
                        .setOnChangeListener(onLayerAlphaSliderChangeListener)
                        .setOnApplyListener((dialog, which) -> clearStatus())
                        .setOnCancelListener(dialog -> clearStatus(), false)
                        .show();
                activityMain.tvStatus.setText(String.format(
                        getString(R.string.state_alpha, Settings.INST.argbCompFormat()),
                        layer.paint.getAlpha()));
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
                layer.paint.setBlendMode(switch (itemId) {
                    case R.id.i_layer_blend_mode_clear -> BlendMode.CLEAR;
                    case R.id.i_layer_blend_mode_src -> BlendMode.SRC;
                    case R.id.i_layer_blend_mode_dst -> BlendMode.DST;
                    case R.id.i_layer_blend_mode_src_over -> BlendMode.SRC_OVER;
                    case R.id.i_layer_blend_mode_dst_over -> BlendMode.DST_OVER;
                    case R.id.i_layer_blend_mode_src_in -> BlendMode.SRC_IN;
                    case R.id.i_layer_blend_mode_dst_in -> BlendMode.DST_IN;
                    case R.id.i_layer_blend_mode_src_out -> BlendMode.SRC_OUT;
                    case R.id.i_layer_blend_mode_dst_out -> BlendMode.DST_OUT;
                    case R.id.i_layer_blend_mode_src_atop -> BlendMode.SRC_ATOP;
                    case R.id.i_layer_blend_mode_dst_atop -> BlendMode.DST_ATOP;
                    case R.id.i_layer_blend_mode_xor -> BlendMode.XOR;
                    case R.id.i_layer_blend_mode_plus -> BlendMode.PLUS;
                    case R.id.i_layer_blend_mode_modulate -> BlendMode.MODULATE;
                    case R.id.i_layer_blend_mode_screen -> BlendMode.SCREEN;
                    case R.id.i_layer_blend_mode_overlay -> BlendMode.OVERLAY;
                    case R.id.i_layer_blend_mode_darken -> BlendMode.DARKEN;
                    case R.id.i_layer_blend_mode_lighten -> BlendMode.LIGHTEN;
                    case R.id.i_layer_blend_mode_color_dodge -> BlendMode.COLOR_DODGE;
                    case R.id.i_layer_blend_mode_color_burn -> BlendMode.COLOR_BURN;
                    case R.id.i_layer_blend_mode_hard_light -> BlendMode.HARD_LIGHT;
                    case R.id.i_layer_blend_mode_soft_light -> BlendMode.SOFT_LIGHT;
                    case R.id.i_layer_blend_mode_difference -> BlendMode.DIFFERENCE;
                    case R.id.i_layer_blend_mode_exclusion -> BlendMode.EXCLUSION;
                    case R.id.i_layer_blend_mode_multiply -> BlendMode.MULTIPLY;
                    case R.id.i_layer_blend_mode_hue -> BlendMode.HUE;
                    case R.id.i_layer_blend_mode_saturation -> BlendMode.SATURATION;
                    case R.id.i_layer_blend_mode_color -> BlendMode.COLOR;
                    case R.id.i_layer_blend_mode_luminosity -> BlendMode.LUMINOSITY;
                    default -> null;
                });
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_clipping -> {
                layer.clipToBelow = !layer.clipToBelow;
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_create_clipping_mask -> {
                switch (layer.paint.getBlendMode()) {
                    case SRC_OVER, SRC_ATOP ->
                            layer.paint.setBlendMode(layer.passBelow ? BlendMode.SRC_OVER : BlendMode.SRC_ATOP);
                    default -> layer.clipToBelow = true;
                }
                Layers.levelDown(frame.layers, frame.selectedLayerIndex);
                frame.computeLayerTree();
                frame.layerAdapter.notifyLayerTreeChanged();
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_create_group -> {
                if (!layer.visible) {
                    break;
                }
                final int pos = frame.group();
                final Bitmap bg = frame.getBackgroundLayer().bitmap;
                final Bitmap bm = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), bg.getConfig(), bg.hasAlpha(), bg.getColorSpace());
                addLayer(project, frame, bm, pos, frame.layers.get(pos - 1).getLevel() - 1, getString(R.string.group), false);
                frame.computeLayerTree();
                layerList.rvLayerList.post(() -> {
                    frame.layerAdapter.notifyItemInserted(pos);
                    frame.layerAdapter.notifyItemRangeChanged(pos + 1, frame.layers.size() - pos - 1);
                    layerList.rvLayerList.post(frame.layerAdapter::notifyLayerTreeChanged);
                });
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_delete -> closeLayer(frame.selectedLayerIndex);
            case R.id.i_layer_delete_invisible -> {
                for (int i = frame.layers.size() - 1; i >= 0; --i) {
                    final Layer l = frame.layers.get(i);
                    if (!l.visible && l != layer) {
                        deleteLayer(i);
                        if (i < frame.selectedLayerIndex) {
                            --frame.selectedLayerIndex;
                        }
                    }
                }
                if (!layer.visible) {
                    layerList.rvLayerList.post(() -> frame.layerAdapter.notifyDataSetChanged());
                    closeLayer(frame.selectedLayerIndex);
                } else {
                    frame.computeLayerTree();
                    selectLayer(frame.selectedLayerIndex);
                    layerList.rvLayerList.post(() -> {
                        frame.layerAdapter.notifyDataSetChanged();
                        layerList.rvLayerList.post(frame.layerAdapter::notifyLayerTreeChanged);
                    });
                }
            }
            case R.id.i_layer_duplicate -> {
                drawFloatingLayersIntoImage();
                final Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                        bitmap.getConfig(), true, bitmap.getColorSpace());
                final Canvas cv = new Canvas(bm);
                if (hasSelection) {
                    cv.drawBitmap(bitmap, selection.r, selection.r, PAINT_SRC);
                } else {
                    cv.drawBitmap(bitmap, 0.0f, 0.0f, PAINT_SRC);
                }
                addLayer(frame,
                        new Layer(layer, bm, getString(R.string.copy_noun)),
                        frame.selectedLayerIndex, true);
            }
            case R.id.i_layer_filter_color_balance -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.lighting == null) layer.initLighting();
                    layer.resetLighting();
                    layer.filter = Layer.Filter.COLOR_BALANCE;
                    drawBitmapOntoView(true);
                }
                new ColorBalanceDialog(this, layer.lighting)
                        .setOnColorBalanceChangeListener((lighting, stopped) -> drawBitmapOntoView(stopped))
                        .setOnPositiveButtonClickListener(null)
                        .show();
                clearStatus();
            }
            case R.id.i_layer_filter_color_matrix -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.colorMatrix == null) layer.initColorMatrix();
                    layer.filter = Layer.Filter.COLOR_MATRIX;
                    drawBitmapOntoView(true);
                }
                new ColorMatrixManager(this, matrix -> drawBitmapOntoView(true), layer.colorMatrix.getArray())
                        .show();
                clearStatus();
            }
            case R.id.i_layer_filter_contrast -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.lighting == null) layer.initLighting();
                    layer.resetLighting();
                    layer.filter = Layer.Filter.CONTRAST;
                    drawBitmapOntoView(true);
                }
                new SliderDialog(this)
                        .setIcon(R.drawable.ic_contrast).setTitle(R.string.contrast)
                        .setValueFrom(-1.0f).setValueTo(10.0f).setValue(layer.lighting[0])
                        .setOnChangeListener(onLayerContrastSliderChangeListener)
                        .setOnApplyListener(null)
                        .show();
                activityMain.tvStatus.setText(getString(R.string.state_contrast, layer.lighting[0]));
            }
            case R.id.i_layer_filter_curves -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.curves == null) layer.initCurves();
                    layer.filter = Layer.Filter.CURVES;
                    drawBitmapOntoView(true);
                }
                new CurvesDialog(this)
                        .setSource(bitmap)
                        .setDefaultCurves(layer.curves)
                        .setOnCurvesChangeListener((curves, stopped) -> drawBitmapOntoView(stopped))
                        .setOnPositiveButtonClickListener(null)
                        .show();
                clearStatus();
            }
            case R.id.i_layer_filter_hsv -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.deltaHsv == null) layer.initDeltaHsv();
                    layer.filter = Layer.Filter.HSV;
                    drawBitmapOntoView(true);
                }
                new HsvDialog(this, layer.deltaHsv)
                        .setOnHsvChangeListener(onLayerHsvChangedListener)
                        .setOnPositiveButtonClickListener(null)
                        .show();
                activityMain.tvStatus.setText(getString(R.string.state_hsv,
                        layer.deltaHsv[0], layer.deltaHsv[1], layer.deltaHsv[2]));
            }
            case R.id.i_layer_filter_levels -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.lighting == null) layer.initLighting();
                    layer.resetLighting();
                    layer.filter = Layer.Filter.LEVELS;
                    drawBitmapOntoView(true);
                }
                new LevelsDialog(this)
                        .set(layer.lighting[0], layer.lighting[1])
                        .setOnLevelsChangeListener(onLayerLevelsChangedListener)
                        .setOnPositiveButtonClickListener(null)
                        .show()
                        .drawHistogram(BitmapUtils.getPixels(bitmap));
                clearStatus();
            }
            case R.id.i_layer_filter_lighting -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.lighting == null) layer.initLighting();
                    layer.filter = Layer.Filter.LIGHTING;
                    drawBitmapOntoView(true);
                }
                new LightingDialog(this, layer.lighting)
                        .setOnLightingChangeListener((lighting, stopped) -> drawBitmapOntoView(stopped))
                        .setOnPositiveButtonClickListener(null)
                        .show();
                clearStatus();
            }
            case R.id.i_layer_filter_lightness -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.lighting == null) layer.initLighting();
                    layer.resetLighting();
                    layer.filter = Layer.Filter.LIGHTNESS;
                    drawBitmapOntoView(true);
                }
                new SliderDialog(this)
                        .setIcon(R.drawable.ic_brightness_5).setTitle(R.string.lightness)
                        .setValueFrom(-0xFF).setValueTo(0xFF).setValue(layer.lighting[1]).setStepSize(1.0f)
                        .setOnChangeListener(onLayerLightnessSliderChangeListener)
                        .setOnApplyListener(null)
                        .show();
                activityMain.tvStatus.setText(getString(R.string.state_lightness, (int) layer.lighting[1]));
            }
            case R.id.i_layer_filter_none -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    layer.filter = null;
                    drawBitmapOntoView(true);
                }
                clearStatus();
            }
            case R.id.i_layer_filter_saturation -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.colorMatrix == null) layer.initColorMatrix();
                    layer.colorMatrix.reset();
                    layer.filter = Layer.Filter.SATURATION;
                    drawBitmapOntoView(true);
                }
                final float sat = (layer.colorMatrix.getArray()[0] - 0.213f) / (1.0f - 0.213f);
                new SliderDialog(this).setTitle(R.string.saturation)
                        .setValueFrom(-1.0f).setValueTo(10.0f)
                        .setValue(sat)
                        .setOnChangeListener(onLayerSaturationSliderChangeListener)
                        .setOnApplyListener(null)
                        .show();
                activityMain.tvStatus.setText(getString(R.string.state_saturation, sat));
            }
            case R.id.i_layer_filter_threshold -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.colorMatrix == null) layer.initColorMatrix();
                    layer.colorMatrix.reset();
                    layer.filter = Layer.Filter.SATURATION;
                    final float[] cm = layer.colorMatrix.getArray();
                    cm[0] = cm[5] = cm[10] = 0.213f * 0x100;
                    cm[1] = cm[6] = cm[11] = 0.715f * 0x100;
                    cm[2] = cm[7] = cm[12] = 0.072f * 0x100;
                    cm[3] = cm[8] = cm[13] = cm[15] = cm[16] = cm[17] = cm[19] = 0.0f;
                    cm[4] = cm[9] = cm[14] = -0x100 * 0x80;
                    cm[18] = 1.0f;
                    layer.filter = Layer.Filter.THRESHOLD;
                    drawBitmapOntoView(true);
                }
                final float threshold = layer.colorMatrix.getArray()[4] / -0x100;
                new SliderDialog(this)
                        .setIcon(R.drawable.ic_filter_b_and_w).setTitle(R.string.threshold)
                        .setValueFrom(0x00).setValueTo(0xFF).setValue(threshold).setStepSize(1.0f)
                        .setOnChangeListener(onLayerThresholdSliderChangeListener)
                        .setOnApplyListener(null)
                        .show();
                activityMain.tvStatus.setText(getString(R.string.state_threshold, (int) threshold));
            }
            case R.id.i_layer_level_down -> {
                layer.levelDown();
                frame.computeLayerTree();
                frame.layerAdapter.notifyLayerTreeChanged();
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_level_up -> {
                layer.levelUp();
                frame.computeLayerTree();
                frame.layerAdapter.notifyLayerTreeChanged();
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_merge_alpha -> {
                final int pos = frame.selectedLayerIndex, posBelow = pos + 1;
                if (posBelow >= frame.layers.size()) break;

                drawFloatingLayersIntoImage();
                final Layer layerBelow = frame.layers.get(posBelow);
                BitmapUtils.mergeAlpha(layer.bitmap, layerBelow.bitmap);
                layer.visible = false;
                selectLayer(posBelow);
                addToHistory();
            }
            case R.id.i_layer_merge_as_hidden -> {
                final int j = activityMain.tlProjectList.getSelectedTabPosition() + 1;
                if (j >= projects.size()) break;
                if (ssdLayerList != null) ssdLayerList.dismiss();

                drawFloatingLayersIntoImage();
                HiddenImageMaker.merge(this,
                        new Bitmap[]{bitmap, projects.get(j).getFirstFrame().getBackgroundLayer().bitmap},
                        onHiddenImageMakeListener);
            }
            case R.id.i_layer_merge_down -> {
                final int pos = frame.selectedLayerIndex, posBelow = pos + 1;
                if (posBelow >= frame.layers.size()) break;

                drawFloatingLayersIntoImage();
                final Layer layerBelow = frame.layers.get(posBelow);
                Layers.mergeLayers(layer, layerBelow);
                deleteLayer(pos);
                frame.computeLayerTree();
                selectLayer(pos);
                layerList.rvLayerList.post(() -> {
                    frame.layerAdapter.notifyItemRemoved(pos);
                    frame.layerAdapter.notifyItemRangeChanged(pos, frame.layers.size() - pos);
                    layerList.rvLayerList.post(frame.layerAdapter::notifyLayerTreeChanged);
                });
                addToHistory();
            }
            case R.id.i_layer_merge_visible -> {
                drawFloatingLayersIntoImage();
                final Bitmap bm = Layers.mergeLayers(frame.layerTree);
                for (final Layer l : frame.layers) {
                    l.visible = false;
                }
                addLayer(project, frame, bm, getString(R.string.layer), true);
            }
            case R.id.i_layer_new -> {
                if (Settings.INST.newLayerLevel()) {
                    createLayer(bitmap.getWidth(), bitmap.getHeight(),
                            bitmap.getConfig(), bitmap.getColorSpace(),
                            layer.getLevel(), layer.left, layer.top, frame.selectedLayerIndex);
                } else {
                    final Bitmap bg = frame.getBackgroundLayer().bitmap;
                    createLayer(bg.getWidth(), bg.getHeight(), bg.getConfig(), bg.getColorSpace(),
                            0, 0, 0, frame.selectedLayerIndex);
                }
            }
            case R.id.i_layer_pass_below -> {
                final boolean checked = !item.isChecked();
                item.setChecked(checked);
                layer.passBelow = checked;
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_reference -> {
                final boolean checked = !item.isChecked();
                layer.reference = checked;
                item.setChecked(checked);
                updateReference();
            }
            case R.id.i_layer_rename -> {
                final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, onLayerNameApplyListener)
                        .setTitle(R.string.rename)
                        .setView(R.layout.file_name)
                        .show();

                final TextInputLayout til = dialog.findViewById(R.id.til_file_name);
                final TextInputEditText tiet = (TextInputEditText) til.getEditText();

                tiet.setFilters(DirectorySelector.FileNameHelper.FILTERS);
                tiet.setText(layer.name);
                til.setHint(R.string.layer_name);
                dialog.findViewById(R.id.s_file_type).setVisibility(View.GONE);
            }
            case R.id.i_layer_select_by_color_range -> {
                if (ssdLayerList != null) {
                    ssdLayerList.dismiss();
                }
                new ColorRangeDialog(this, layer.colorRange)
                        .setOnColorRangeChangeListener(onLayerColorRangeChangedListener)
                        .show();
            }
        }
        return true;
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            default -> {
                return false;
            }
            case R.id.i_cell_grid ->
                    new CellGridManager(this, layer.cellGrid, onCellGridApplyListener).show();
            case R.id.i_clone -> {
                if (transformer.isRecycled()) {
                    drawFloatingLayersIntoImage();
                    final Bitmap bm = hasSelection
                            ? Bitmap.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height())
                            : Bitmap.createBitmap(bitmap);
                    createTransformer(bm);
                    activityMain.tools.btgTools.check(R.id.b_transformer);
                    drawSelectionOntoView();
                } else {
                    canvas.drawBitmap(transformer.getBitmap(),
                            selection.r.left, selection.r.top,
                            PAINT_SRC_OVER);
                    addToHistory();
                }
                if (hasSelection) {
                    drawBitmapOntoView(selection.r, true);
                } else {
                    drawBitmapOntoView(true);
                }
            }
            case R.id.i_clone_as_new -> {
                final Bitmap bm = hasSelection ?
                        transformer.isRecycled() ?
                                Bitmap.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height()) :
                                Bitmap.createBitmap(transformer.getBitmap()) :
                        Bitmap.createBitmap(bitmap);
                addProject(bm, activityMain.tlProjectList.getSelectedTabPosition() + 1, getString(R.string.copy_noun));
            }
            case R.id.i_copy -> {
                if (transformer.isRecycled()) {
                    if (clipboard != null) {
                        clipboard.recycle();
                    }
                    clipboard = hasSelection
                            ? Bitmap.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height())
                            : Bitmap.createBitmap(bitmap);
                } else {
                    clipboard = Bitmap.createBitmap(transformer.getBitmap());
                }
            }
            case R.id.i_crop -> {
                if (!hasSelection) {
                    break;
                }
                drawFloatingLayersIntoImage();
                final int width = selection.r.width(), height = selection.r.height();
                if (layer == frame.getBackgroundLayer()) {
                    for (final Frame f : project.frames) {
                        final Layer bl = f.getBackgroundLayer();
                        final Bitmap bm = Bitmap.createBitmap(bl.bitmap, selection.r.left, selection.r.top, width, height);
                        resizeImage(f, bl, width, height,
                                ImageSizeManager.ScaleType.CROP, bm,
                                selection.r.left, selection.r.top);
                        bm.recycle();
                    }
                } else {
                    final Bitmap bm = Bitmap.createBitmap(bitmap, selection.r.left, selection.r.top, width, height);
                    resizeImage(frame, layer, width, height,
                            ImageSizeManager.ScaleType.CROP, bm,
                            selection.r.left, selection.r.top);
                    bm.recycle();
                }
            }
            case R.id.i_cut -> {
                if (transformer.isRecycled()) {
                    if (clipboard != null) {
                        clipboard.recycle();
                    }
                    if (hasSelection) {
                        clipboard = Bitmap.createBitmap(bitmap, selection.r.left, selection.r.top,
                                selection.r.width(), selection.r.height());
                        canvas.drawRect(selection.r.left, selection.r.top, selection.r.right, selection.r.bottom, eraser);
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
                    drawBitmapOntoView(selection.r, true);
                } else {
                    drawBitmapOntoView(true);
                }
            }
            case R.id.i_delete -> {
                if (transformer.isRecycled()) {
                    if (hasSelection) {
                        canvas.drawRect(selection.r.left, selection.r.top, selection.r.right, selection.r.bottom, eraser);
                    } else {
                        canvas.drawColor(eraser.getColorLong(), BlendMode.SRC);
                    }
                    addToHistory();
                } else {
                    recycleTransformer();
                }
                if (hasSelection) {
                    drawBitmapOntoView(selection.r, true);
                } else {
                    drawBitmapOntoView(true);
                }
                clearStatus();
            }
            case R.id.i_deselect -> {
                drawFloatingLayersIntoImage();
                hasSelection = false;
                switch (activityMain.tools.btgTools.getCheckedButtonId()) {
                    case R.id.b_transformer -> {
                        if (activityMain.optionsTransformer.btgTransformer.getCheckedButtonId() == R.id.b_mesh) {
                            transformer.mesh = null;
                        }
                    }
                }
                eraseBitmapAndInvalidateView(selectionBitmap, activityMain.canvas.ivSelection);
                clearStatus();
            }
            case R.id.i_draw_color -> {
                if (transformer.isRecycled()) {
                    if (hasSelection) {
                        canvas.drawRect(selection.r.left, selection.r.top, selection.r.right, selection.r.bottom, paint);
                    } else {
                        canvas.drawColor(paint.getColorLong());
                    }
                    addToHistory();
                } else {
                    transformer.getBitmap().eraseColor(paint.getColorLong());
                }
                if (hasSelection) {
                    drawBitmapOntoView(selection.r, true);
                } else {
                    drawBitmapOntoView(true);
                }
                clearStatus();
            }
            case R.id.i_file_close ->
                    closeProject(activityMain.tlProjectList.getSelectedTabPosition());
            case R.id.i_file_export -> export(new Project());
            case R.id.i_file_new -> {
                new NewImageDialog(this)
                        .setOnFinishSettingListener(onNewImagePropertiesApplyListener)
                        .show();
            }
            case R.id.i_file_open -> pickMedia();
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
                final String filePath = project.filePath;
                if (filePath == null) {
                    Snackbar.make(vContent, R.string.please_save_first, Snackbar.LENGTH_LONG)
                            .setAction(R.string.save, v -> save())
                            .show();
                    break;
                }
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                        .setPrimaryClip(ClipData.newUri(getContentResolver(), "Image",
                                FileProvider.getUriForFile(this,
                                        getApplicationContext().getPackageName() + ".provider",
                                        new File(filePath))));
            }
            case R.id.i_file_save, R.id.i_save -> save();
            case R.id.i_file_save_as -> saveAs();
            case R.id.i_fill_with_ref -> {
                if (ref.recycled()) {
                    Snackbar.make(vContent, R.string.no_reference, BaseTransientBottomBar.LENGTH_LONG).show();
                    break;
                }
                drawFloatingLayersIntoImage();
                createEditPreview();
                new FillWithRefDialog(this, ref.bm())
                        .setOnChangeListener(onFillWithRefTileModeChangeListener)
                        .setOnPositiveButtonClickListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .setOnDismissListener(dialog -> paint.setShader(null))
                        .show();
                clearStatus();
            }
            case R.id.i_filter_color_balance -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new ColorBalanceDialog(this)
                        .setOnColorBalanceChangeListener(onFilterLightingChangedListener)
                        .setOnPositiveButtonClickListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_color_matrix -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new ColorMatrixManager(this,
                        onFilterColorMatrixChangedListener,
                        onEditPreviewPBClickListener,
                        onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_contrast -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new SliderDialog(this)
                        .setIcon(item.getIcon()).setTitle(R.string.contrast)
                        .setValueFrom(-1.0f).setValueTo(10.0f).setValue(1.0f)
                        .setOnChangeListener(onFilterContrastSliderChangeListener)
                        .setOnApplyListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_curves -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new CurvesDialog(this)
                        .setSource(editPreview.getPixels())
                        .setOnCurvesChangeListener(onFilterCurvesChangedListener)
                        .setOnPositiveButtonClickListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_hsv -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new HsvDialog(this)
                        .setOnHsvChangeListener(onFilterHsvChangedListener)
                        .setOnPositiveButtonClickListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_hue_to_alpha -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new SliderDialog(this).setTitle(R.string.hue).setValueFrom(0.0f).setValueTo(360.0f).setValue(0.0f)
                        .setOnChangeListener(onFilterHToASliderChangeListener)
                        .setOnApplyListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                onFilterHToASliderChangeListener.onChange(null, 0, true);
            }
            case R.id.i_filter_levels -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new LevelsDialog(this)
                        .setOnLevelsChangeListener(onFilterLevelsChangedListener)
                        .setOnPositiveButtonClickListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show()
                        .drawHistogram(editPreview.getPixels());
                clearStatus();
            }
            case R.id.i_filter_lighting -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new LightingDialog(this)
                        .setOnLightingChangeListener(onFilterLightingChangedListener)
                        .setOnPositiveButtonClickListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_lightness -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new SliderDialog(this)
                        .setIcon(item.getIcon()).setTitle(R.string.lightness)
                        .setValueFrom(-0xFF).setValueTo(0xFF).setValue(0).setStepSize(1.0f)
                        .setOnChangeListener(onFilterLightnessSliderChangeListener)
                        .setOnApplyListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_posterize -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new SliderDialog(this).setTitle(R.string.posterize)
                        .setValueFrom(0x02).setValueTo(0xFF).setValue(0xFF).setStepSize(1.0f)
                        .setOnChangeListener(onFilterPosterizationSliderChangeListener)
                        .setOnApplyListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_saturation -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new SliderDialog(this).setTitle(R.string.saturation).setValueFrom(-1.0f).setValueTo(10.0f).setValue(1.0f)
                        .setOnChangeListener(onFilterSaturationSliderChangeListener)
                        .setOnApplyListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_threshold -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new SliderDialog(this)
                        .setIcon(item.getIcon()).setTitle(R.string.threshold)
                        .setValueFrom(0x00).setValueTo(0xFF).setValue(0x80).setStepSize(1.0f)
                        .setOnChangeListener(onFilterThresholdSliderChangeListener)
                        .setOnApplyListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                onFilterThresholdSliderChangeListener.onChange(null, 0x80, true);
                clearStatus();
            }
            case R.id.i_flip_horizontally -> scale(-1.0f, 1.0f);
            case R.id.i_flip_vertically -> scale(1.0f, -1.0f);
            case R.id.i_frame_list -> {
                project.frameAdapter.notifyDataSetChanged();
                project.frames.forEach(Frame::createThumbnail);

                bsdFrameList = new BottomSheetDialog(this);
                bsdFrameList.setTitle(R.string.frames);
                bsdFrameList.setContentView(frameList.getRoot());
                bsdFrameList.setOnDismissListener(dialog -> {
                    ((ViewGroup) frameList.getRoot().getParent()).removeAllViews();
                    project.frames.forEach(Frame::recycleThumbnail);
                    bsdFrameList = null;
                });
                bsdFrameList.show();
            }
            case R.id.i_generate_noise -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new NoiseGenerator(this)
                        .setOnPropChangedListener(onNoisePropChangedListener)
                        .setOnConfirmListener(onEditPreviewPBClickListener)
                        .setOnCancelListener(onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_guides_clear -> {
                layer.guides.clear();
                drawGridOntoView();
            }
            case R.id.i_guides_new -> {
                final Guide guide = new Guide();
                layer.guides.offerFirst(guide); // Add at the front for faster removal if necessary later
                new GuideEditor(this, guide, bitmap.getWidth(), bitmap.getHeight(),
                        g -> drawGridOntoView(),
                        dialog -> {
                            layer.guides.remove(guide);
                            drawGridOntoView();
                        })
                        .show();
                drawGridOntoView();
            }
            case R.id.i_image_color_space -> {
            }
            case R.id.i_image_config -> {
                BitmapConfigModifier.showDialog(this, bitmap, onNewConfigBitmapCreatedListener);
            }
            case R.id.i_image_has_alpha -> {
                final boolean checked = !item.isChecked();
                item.setChecked(checked);
                bitmap.setHasAlpha(checked);
                drawBitmapOntoView(true);
                addToHistory();
            }
            case R.id.i_information -> {
                final StringBuilder message = new StringBuilder()
                        .append(getString(R.string.config)).append('\n').append(bitmap.getConfig()).append("\n\n")
                        .append(getString(R.string.has_alpha)).append('\n').append(bitmap.hasAlpha()).append("\n\n")
                        .append(getString(R.string.color_space)).append('\n').append(bitmap.getColorSpace());
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.information)
                        .setMessage(message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
            case R.id.i_layer_list -> {
                frame.layerAdapter.notifyDataSetChanged();
                layerList.rvLayerList.post(frame.layerAdapter::notifyLayerTreeChanged);

                ssdLayerList = new SideSheetDialog(this);
                ssdLayerList.setTitle(R.string.layers);
                ssdLayerList.setContentView(layerList.getRoot());
                ssdLayerList.setOnDismissListener(dialog -> {
                    ((ViewGroup) layerList.getRoot().getParent()).removeAllViews();
                    ssdLayerList = null;
                });
                ssdLayerList.show();
            }
            case R.id.i_paste -> {
                if (clipboard == null) {
                    break;
                }
                drawFloatingLayersIntoImage();

                boolean si = !hasSelection; // Is selection.r invisible
                if (hasSelection) {
                    final Rect vs = getVisibleSubset();
                    si = !Rect.intersects(selection.r, vs);
                }
                if (si) {
                    hasSelection = true;
                    selection.r.left = translationX >= 0.0f ? 0 : toUnscaled(-translationX) + 1;
                    selection.r.top = translationY >= 0.0f ? 0 : toUnscaled(-translationY) + 1;
                }
                selection.r.right = selection.r.left + clipboard.getWidth();
                selection.r.bottom = selection.r.top + clipboard.getHeight();
                createTransformer(Bitmap.createBitmap(clipboard));
                activityMain.tools.btgTools.check(R.id.b_transformer);
                drawBitmapOntoView(selection.r);
                drawSelectionOntoView();
            }
            case R.id.i_redo -> {
                if (layer.history.canRedo()) {
                    undoOrRedo(layer.history.redo());
                }
            }
            case R.id.i_rotate_90 -> rotate(90.0f);
            case R.id.i_rotate_180 -> rotate(180.0f);
            case R.id.i_rotate_270 -> rotate(270.0f);
            case R.id.i_select_all -> {
                selectAll();
                hasSelection = true;
                if (activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_transformer
                        && activityMain.optionsTransformer.btgTransformer.getCheckedButtonId() == R.id.b_mesh) {
                    createTransformerMesh();
                }
                drawSelectionOntoView();
                clearStatus();
            }
            case R.id.i_settings -> startActivity(new Intent(this, SettingsActivity.class));
            case R.id.i_size -> new ImageSizeManager(this, bitmap, onImageSizeApplyListener).show();
            case R.id.i_transform -> {
                drawFloatingLayersIntoImage();
                createEditPreview();
                new MatrixManager(this,
                        onMatrixChangedListener,
                        onEditPreviewPBClickListener,
                        dialog -> {
                            drawBitmapOntoView(true);
                            editPreview.recycle();
                            editPreview = null;
                            clearStatus();
                        })
                        .show();
                clearStatus();
            }
            case R.id.i_undo -> {
                if (!isShapeStopped) {
                    isShapeStopped = true;
                    if (!dpPreview.isRecycled()) {
                        dpPreview.erase();
                        drawBitmapOntoView(true);
                    }
                } else if (layer.history.canUndo()) {
                    undoOrRedo(layer.history.undo());
                }
            }
            case R.id.i_view_actual_pixels -> {
                translationX = project.translationX = 0.0f;
                translationY = project.translationY = 0.0f;
                scale = project.scale = 1.0f;
                calculateBackgroundSizeOnView();
                drawAfterTransformingView(false);
            }
            case R.id.i_view_fit_on_screen -> {
                fitOnScreen();
                translationX = project.translationX;
                translationY = project.translationY;
                scale = project.scale;
                calculateBackgroundSizeOnView();
                drawAfterTransformingView(false);
            }
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    private boolean onProjTabOptionsItemSelected(MenuItem item) {
        final int position = activityMain.tlProjectList.getSelectedTabPosition();
        switch (item.getItemId()) {
            case R.id.i_tab_close -> closeProject(position);
            case R.id.i_tab_close_others -> {
                for (int i = projects.size() - 1; projects.size() > 1; --i) {
                    if (i != position) deleteProject(i);
                }
            }
            case R.id.i_tab_move_to_first -> {
                if (position == 0) break;

                // Move without shifting the elements to the end since the list is an ArrayList
                for (int i = position; i > 0; --i) {
                    projects.set(i, projects.get(i - 1));
                }
                projects.set(0, project);

                activityMain.tlProjectList.removeOnTabSelectedListener(onProjTabSelectedListener);
                activityMain.tlProjectList.removeTabAt(position);
                loadTab(project, 0);
                activityMain.tlProjectList.getTabAt(0).select();
                activityMain.tlProjectList.addOnTabSelectedListener(onProjTabSelectedListener);
            }
        }
        return true;
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
    private void onToolChanged(View.OnTouchListener onIVTouchListener) {
        if (hasNotLoaded) {
            return;
        }
        activityMain.btgZoom.uncheck(R.id.b_zoom);
        activityMain.canvas.flIv.setOnTouchListener(onIVTouchListener);
        this.onIVTouchListener = onIVTouchListener;
        hideToolOptions();
        isShapeStopped = true;
        shape = null;
        if (!dpPreview.isRecycled()) {
            dpPreview.recycle();
            drawBitmapOntoView(true);
        }
        eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
        paint.setAntiAlias(antiAlias);
        setBlurRadius(paint, blurRadius);
        paint.setStyle(style);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChanged(View.OnTouchListener onTouchIVListener, View toolOption) {
        onToolChanged(onTouchIVListener);
        if (toolOption != null) {
            toolOption.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NonConstantResourceId")
    private boolean onTransformerActionItemClick(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            default -> {
                return false;
            }
            case R.id.i_flip_horizontally -> scale(-1.0f, 1.0f);
            case R.id.i_flip_vertically -> scale(1.0f, -1.0f);
            case R.id.i_rotate_90 -> rotate(90.0f);
            case R.id.i_rotate_180 -> rotate(180.0f);
            case R.id.i_rotate_270 -> rotate(270.0f);
            case R.id.i_ok -> drawTransformerIntoImage();
        }
        return true;
    }

    private void openFile(Uri uri) {
        if (uri == null) {
            return;
        }
        try (final InputStream inputStream = getContentResolver().openInputStream(uri)) {
            final Bitmap bm = BitmapFactory.decodeStream(inputStream);
            if (bm == null) {
                Snackbar.make(vContent, R.string.image_is_invalid, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open, v -> pickMedia())
                        .show();
                return;
            }
            openImage(bm, uri);
            bm.recycle();
        } catch (IOException e) {
        }
    }

    private void openImage(Bitmap bitmap, Uri uri) {
        final Bitmap bm = bitmap.copy(bitmap.getConfig(), true);
        bitmap.recycle();
        if (Settings.INST.autoSetHasAlpha()) {
            bm.setHasAlpha(true);
        }
        final DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        final String name = documentFile.getName(), mimeType = documentFile.getType();
        if (mimeType != null) {
            final String path;
            final Project.FileType type = switch (mimeType) {
                case "image/jpeg" -> Project.FileType.JPEG;
                case "image/png" -> Project.FileType.PNG;
                case "image/gif" -> Project.FileType.GIF;
                case "image/webp" -> Project.FileType.WEBP;
                default -> null;
            };
            final Bitmap.CompressFormat compressFormat = switch (mimeType) {
                case "image/jpeg" -> Bitmap.CompressFormat.JPEG;
                case "image/png" -> Bitmap.CompressFormat.PNG;
                case "image/webp" -> Bitmap.CompressFormat.WEBP_LOSSLESS;
                default -> null;
            };
            if (compressFormat != null) {
                path = FileUtils.getRealPath(this, uri);
                addProject(bm, projects.size(), name, path, type, compressFormat);
            } else if (type == Project.FileType.GIF) {
                path = FileUtils.getRealPath(this, uri);
                final GifDecoder gifDecoder = new GifDecoder();
                if (path != null && gifDecoder.load(path)) {
                    final Project proj = addProject(null, projects.size(), name, path, type, null, false);
                    final Frame[] frames = new Frame[gifDecoder.frameNum()];
                    for (int i = 0; i < gifDecoder.frameNum(); ++i) {
                        final Frame newFrame = addFrame(proj, gifDecoder.frame(i),
                                i, gifDecoder.delay(i), false);
                        frames[i] = newFrame;
                        frames[i].computeLayerTree();
                    }
                    selectProject(projects.size() - 1);
                } else {
                    addProject(bm, projects.size(), name, path, type, null);
                }
            } else {
                addProject(bm, projects.size(), name);
                Snackbar.make(vContent, R.string.not_supported_file_type, Snackbar.LENGTH_LONG)
                        .setAction(R.string.save_as, v -> save())
                        .show();
            }
        } else {
            addProject(bm, projects.size());
        }
    }

    private void optimizeSelection() {
        final int imageWidth = bitmap.getWidth(), imageHeight = bitmap.getHeight();
        selection.r.sort();
        if (!selection.r.isEmpty()
                && selection.r.left < imageWidth && selection.r.top < imageHeight
                && selection.r.right > 0 && selection.r.bottom > 0) {
            selection.r.set(Math.max(0, selection.r.left), Math.max(0, selection.r.top),
                    Math.min(imageWidth, selection.r.right), Math.min(imageHeight, selection.r.bottom));
        } else {
            hasSelection = false;
        }
    }

    private void pickMedia() {
        pickMultipleMedia.launch(pickVisualMediaRequest);
    }

    private void recycleAllBitmaps() {
        dpPreview.recycle();
        transformer.recycle();
        ref.recycle();
        brush.recycleAll();
        if (editPreview != null) editPreview.recycle();
        BitmapUtils.recycle(chessboard);
        BitmapUtils.recycle(chessboardBitmap);
        BitmapUtils.recycle(clipboard);
        BitmapUtils.recycle(gridBitmap);
        BitmapUtils.recycle(lastMerged);
        BitmapUtils.recycle(previewBitmap);
        BitmapUtils.recycle(rulerHBitmap);
        BitmapUtils.recycle(rulerVBitmap);
        BitmapUtils.recycle(selectionBitmap);
        BitmapUtils.recycle(viewBitmap);
    }

    private void recycleTransformer() {
        if (transformer.isRecycled()) return;
        transformer.recycle();
        if (transformerActionMode != null) {
            transformerActionMode.finish();
            transformerActionMode = null;
        }
    }

    private void resizeImage(Frame frame, Layer layer, int width, int height,
                             ImageSizeManager.ScaleType scaleType, @Nullable Bitmap newImage,
                             int offsetX, int offsetY) {
        final Bitmap bm = Bitmap.createBitmap(width, height,
                layer.bitmap.getConfig(), layer.bitmap.hasAlpha(), layer.bitmap.getColorSpace());
        final Canvas cv = new Canvas(bm);
        if (scaleType != null) {
            if (newImage == null) newImage = layer.bitmap;
            switch (scaleType) {
                case STRETCH -> cv.drawBitmap(newImage,
                        new Rect(0, 0, layer.bitmap.getWidth(), layer.bitmap.getHeight()),
                        new RectF(0.0f, 0.0f, width, height),
                        PAINT_BITMAP);
                case STRETCH_FILTER -> cv.drawBitmap(newImage,
                        new Rect(0, 0, layer.bitmap.getWidth(), layer.bitmap.getHeight()),
                        new RectF(0.0f, 0.0f, width, height),
                        PAINT_SRC);
                case CROP -> cv.drawBitmap(newImage, 0.0f, 0.0f, PAINT_BITMAP);
            }
        }
        if (layer == frame.getBackgroundLayer()) {
            for (int i = 0; i < frame.layers.size() - 1; ++i) {
                frame.layers.get(i).moveBy(-offsetX, -offsetY);
            }
        } else {
            layer.moveBy(offsetX, offsetY);
        }
        layer.bitmap.recycle();
        layer.bitmap = bm;
        addToHistory(layer);

        if (layer == this.layer) {
            bitmap = bm;
            canvas = cv;
            calculateBackgroundSizeOnView();

            recycleTransformer();
            hasSelection = false;

            drawBitmapOntoView(true, true);
            drawChessboardOntoView();
            drawGridOntoView();
            drawSelectionOntoView();

            clearStatus(); // Prevent from displaying the old size
        }
    }

    private void rotate(float degrees) {
        if (!hasSelection && (degrees == 90.0f || degrees == 270.0f)) {
            final int width = bitmap.getWidth(), height = bitmap.getHeight();
            final Bitmap bm = Bitmap.createBitmap(height, width, bitmap.getConfig(), bitmap.hasAlpha(), bitmap.getColorSpace());
            canvas = new Canvas(bm);
            final Matrix matrix = new Matrix();
            final float pivot = switch ((int) degrees) {
                case 90 -> height / 2.0f;
                case 270 -> width / 2.0f;
                default -> 0.0f;
            };
            matrix.setRotate(degrees, pivot, pivot);
            canvas.drawBitmap(bitmap, matrix, PAINT_BITMAP);
            bitmap.recycle();
            bitmap = bm;
            layer.bitmap = bm;
            addToHistory();
            calculateBackgroundSizeOnView();
            drawBitmapOntoView(true, true);
            drawChessboardOntoView();
            drawGridOntoView();
        } else {
            if (transformer.isRecycled()) {
                if (!hasSelection) selectAll();
                final int left = selection.r.left, top = selection.r.top, width = selection.r.width(), height = selection.r.height();
                final Bitmap bm = Bitmap.createBitmap(bitmap, left, top, width, height);
                final Matrix matrix = new Matrix();
                matrix.setRotate(degrees, width / 2.0f, height / 2.0f);
                matrix.postTranslate(left, top);
                canvas.drawRect(selection.r, eraser);
                canvas.drawBitmap(bm, matrix, PAINT_BITMAP_OVER);
                bm.recycle();
                addToHistory();
            } else {
                final int w = transformer.getWidth(), h = transformer.getHeight();
                transformer.rotate(degrees, false, false);
                final int w_ = transformer.getWidth(), h_ = transformer.getHeight();
                selection.r.left += w - w_ >> 1;
                selection.r.top += h - h_ >> 1;
                selection.r.right = selection.r.left + w_;
                selection.r.bottom = selection.r.top + h_;
                drawSelectionOntoView();
            }
            final Matrix matrix = new Matrix();
            matrix.setRotate(degrees, selection.r.exactCenterX(), selection.r.exactCenterY());
            final RectF rf = new RectF(selection.r);
            matrix.mapRect(rf);
            final Rect r = new Rect();
            rf.roundOut(r);
            r.union(selection.r);
            drawBitmapOntoView(r, true);
        }
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

    @SuppressLint("StringFormatMatches")
    private void save() {
        if (project.filePath == null) {
            if (!checkOrRequestPermission()) {
                return;
            }
            saveAs();
            return;
        }
        int quality = 100;
        if (project.fileType == Project.FileType.GIF) {
            if (project.gifEncodingType == null) {
                saveInQuality();
                return;
            }
        } else if (project.fileType != Project.FileType.PNG) {
            quality = project.quality;
            if (quality < 0) {
                saveInQuality();
                return;
            }
        }
        drawFloatingLayersIntoImage();
        FileUtils.save(this, project, quality);
    }

    private void saveAs() {
        dirSelector.open(project, project, project -> saveInQuality());
    }

    private void saveInQuality() {
        setQuality(project, project -> save());
    }

    private void scale(float sx, float sy) {
        if (transformer.isRecycled()) {
            if (!hasSelection) selectAll();
            final int left = selection.r.left, top = selection.r.top, width = selection.r.width(), height = selection.r.height();
            final Bitmap bm = Bitmap.createBitmap(bitmap, left, top, width, height);
            final Matrix matrix = new Matrix();
            matrix.setScale(sx, sy, width / 2.0f, height / 2.0f);
            matrix.postTranslate(left, top);
            canvas.drawRect(selection.r, eraser);
            canvas.drawBitmap(bm, matrix, PAINT_BITMAP_OVER);
            bm.recycle();
            addToHistory();
        } else {
            final int w = transformer.getWidth(), h = transformer.getHeight();
            transformer.scale(sx, sy, false, false);
            final int w_ = transformer.getWidth(), h_ = transformer.getHeight();
            selection.r.left += w - w_ >> 1;
            selection.r.top += h - h_ >> 1;
            selection.r.right = selection.r.left + w_;
            selection.r.bottom = selection.r.top + h_;
            drawSelectionOntoView();
        }
        final Matrix matrix = new Matrix();
        matrix.setScale(sx, sy, selection.r.exactCenterX(), selection.r.exactCenterY());
        final RectF rf = new RectF(selection.r);
        matrix.mapRect(rf);
        final Rect r = new Rect();
        rf.roundOut(r);
        r.union(selection.r);
        drawBitmapOntoView(r, true);
    }

    private void selectAll() {
        selection.r.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    private void selectFrame(int position) {
        selectFrame(project, position);
    }

    private void selectFrame(Project project, int position) {
        frame = project.frames.get(position);
        project.selectedFrameIndex = position;
        layerList.rvLayerList.setAdapter(frame.layerAdapter);

        selectLayer(frame.selectedLayerIndex);
        frame.layerAdapter.notifyDataSetChanged();
        layerList.rvLayerList.post(frame.layerAdapter::notifyLayerTreeChanged);
    }

    @SuppressLint("NonConstantResourceId")
    private void selectLayer(int position) {
        drawFloatingLayersIntoImage();

        final Layer unselectedLayer = layer;
        layer = frame.layers.get(position);
        frame.selectedLayerIndex = position;
        bitmap = layer.bitmap;
        canvas = new Canvas(bitmap);

        calculateBackgroundSizeOnView();

        if (hasSelection) {
            selection.r.offset(unselectedLayer.left - layer.left, unselectedLayer.top - layer.top);
        }

        recycleTransformer();
        transformer.mesh = null;
        optimizeSelection();

        switch (activityMain.tools.btgTools.getCheckedButtonId()) {
            case R.id.b_clone_stamp -> cloneStampSrc = null;
        }

        updateReference();

        if (activityMain.topAppBar != null) {
            miHasAlpha.setChecked(bitmap.hasAlpha());
        }

        if (!dpPreview.isRecycled()) {
            dpPreview.erase();
        }
        drawBitmapOntoView(true, true);
        drawChessboardOntoView();
        drawGridOntoView();
        drawSelectionOntoView();
        eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);

        activityMain.tvStatus.setText(getString(R.string.state_size, bitmap.getWidth(), bitmap.getHeight()));
    }

    private void selectProject(int position) {
        final TabLayout.Tab tab = activityMain.tlProjectList.getTabAt(position);
        if (tab.isSelected()) {
            onProjTabSelectedListener.onTabSelected(tab);
        } else {
            tab.select();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setArgbColorType() {
        onIVTouchWithEyedropperListener = Settings.INST.argbColorType()
                ? onIVTouchWithPreciseEyedropperListener : onIVTouchWithImpreciseEyedropperListener;
        if (activityMain != null && activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_eyedropper) {
            activityMain.canvas.flIv.setOnTouchListener(onIVTouchWithEyedropperListener);
        }
    }

    private void setBlurRadius(Paint paint, float f) {
        paint.setMaskFilter(f > 0.0f ? new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL) : null);
    }

    public void setFilterBitmap(boolean filterBitmap) {
        bitmapPaint.setFilterBitmap(filterBitmap);
        if (!hasNotLoaded) {
            drawBitmapOntoView(true);
        }
    }

    public void setFrameListMenuItemVisible(boolean visible) {
        if (activityMain.topAppBar != null) {
            miFrameList.setVisible(visible);
            activityMain.topAppBar.toolBar.setTitle(visible ? R.string.app_name_abbrev : R.string.app_name);
        }
    }

    private void setPaintColor(long foregroundColor, long backgroundColor) {
        paint.setColor(foregroundColor);
        eraser.setColor(backgroundColor);
        activityMain.vForegroundColor.setBackgroundColor(Color.toArgb(foregroundColor));
        activityMain.vBackgroundColor.setBackgroundColor(Color.toArgb(backgroundColor));
        onPaintColorChanged();
    }

    private void onPaintColorChanged() {
        if (activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_brush) {
            brush.set(paint.getColorLong());
        } else if (isEditingText) {
            drawTextOntoView();
        }
    }

    private void setQuality(Project project, DirectorySelector.OnApplyFileNameCallback callback) {
        switch (project.fileType) {
            case PNG -> callback.onApply(project);
            case GIF -> {
                new QualityManager(this,
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
                new QualityManager(this,
                        project.quality < 0 ? 100 : project.quality,
                        project.compressFormat,
                        (quality, format) -> {
                            project.quality = quality;
                            project.compressFormat = format;
                            if (callback != null) {
                                callback.onApply(project);
                            }
                        })
                        .show();
            }
        }
    }

    public void setRunnableRunner(boolean multithreaded) {
        runnableRunner = multithreaded ? runnableStartingRunner : runnableRunningRunner;
    }

    private void setSelection(int fromX, int fromY, int toX, int toY) {
        if (fromX < toX) {
            selection.r.left = fromX;
            selection.r.right = toX;
        } else {
            selection.r.left = toX - 1;
            selection.r.right = fromX + 1;
        }
        if (fromY < toY) {
            selection.r.top = fromY;
            selection.r.bottom = toY;
        } else {
            selection.r.top = toY - 1;
            selection.r.bottom = fromY + 1;
        }
        drawSelectionOntoView();
    }

    private void spotPoint(float x, float y, String text) {
        canvas.drawLine(x - 100.0f, y, x + 100.0f, y, PAINT_POINT);
        canvas.drawLine(x, y - 100.0f, x, y + 100.0f, PAINT_POINT);
        canvas.drawText(text, x, y, PAINT_POINT);
        activityMain.canvas.iv.invalidate();
    }

    /**
     * @return The x coordinate on bitmap.
     */
    public int toBitmapX(float x) {
        return (int) ((x - translationX) / scale) - layer.left;
    }

    private int toBitmapXAbs(float x) {
        return (int) ((x - translationX) / scale);
    }

    private float toBitmapXExact(float x) {
        return ((x - translationX) / scale) - layer.left;
    }

    /**
     * @return The y coordinate on bitmap.
     */
    public int toBitmapY(float y) {
        return (int) ((y - translationY) / scale) - layer.top;
    }

    private int toBitmapYAbs(float y) {
        return (int) ((y - translationY) / scale);
    }

    private float toBitmapYExact(float y) {
        return ((y - translationY) / scale) - layer.top;
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
    public float toViewX(int x) {
        return translationX + (x + layer.left) * scale;
    }

    /**
     * @return The x coordinate on view.
     */
    private float toViewX(float x) {
        return translationX + (x + layer.left) * scale;
    }

    private float toViewXRel(int x) {
        return translationX + x * scale;
    }

    /**
     * @return The y coordinate on view.
     */
    public float toViewY(int y) {
        return translationY + (y + layer.top) * scale;
    }

    /**
     * @return The y coordinate on view.
     */
    private float toViewY(float y) {
        return translationY + (y + layer.top) * scale;
    }

    private float toViewYRel(int y) {
        return translationY + y * scale;
    }

    private void undoOrRedo(Bitmap bitmap) {
        this.bitmap.recycle();
        this.bitmap = bitmap;
        layer.bitmap = this.bitmap;
        canvas = new Canvas(this.bitmap);

        calculateBackgroundSizeOnView();

        recycleTransformer();
        if (hasSelection
                && activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_transformer
                && activityMain.optionsTransformer.btgTransformer.getCheckedButtonId() == R.id.b_mesh) {
            createTransformerMesh();
        }

        if (activityMain.topAppBar != null) {
            miHasAlpha.setChecked(this.bitmap.hasAlpha());
        }

        optimizeSelection();
        isShapeStopped = true;
        selection.marqBoundBeingDragged = null;
        if (magEr.b != null && magEr.f != null) {
            drawCrossOntoView(magEr.b.x, magEr.b.y, true);
            drawCrossOntoView(magEr.f.x, magEr.f.y, false);
        } else {
            eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
            if (!dpPreview.isRecycled()) {
                dpPreview.erase();
            }
        }

        drawBitmapOntoView(true, true);
        drawChessboardOntoView();
        drawGridOntoView();
        drawSelectionOntoView();

        if (cloneStampSrc != null) {
            drawCrossOntoView(cloneStampSrc.x, cloneStampSrc.y);
        }

        clearStatus();
    }

    private void updateBrush() {
        switch (brush.tipShape) {
            case BRUSH -> brush.setToBrush(paint.getColorLong());
            case REF -> updateReference();
        }
    }

    private void updateReference() {
        final Bitmap rb = frame.mergeReferenceLayers();
        ref.set(rb != null ? rb : checkIfRequireRef() ? Bitmap.createBitmap(bitmap) : null);
        if (activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_brush && brush.tipShape == BrushTool.TipShape.REF) {
            brush.setToRef(ref.bm(), paint.getColorLong());
        }
    }
}