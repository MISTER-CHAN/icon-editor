package com.misterchan.iconeditor.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.graphics.Color;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorLong;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.misterchan.iconeditor.BuildConfig;
import com.misterchan.iconeditor.CellGrid;
import com.misterchan.iconeditor.DrawingPrimitivePreview;
import com.misterchan.iconeditor.EditPreview;
import com.misterchan.iconeditor.FloatingLayer;
import com.misterchan.iconeditor.Frame;
import com.misterchan.iconeditor.Guide;
import com.misterchan.iconeditor.History;
import com.misterchan.iconeditor.ImageStateAccumulator;
import com.misterchan.iconeditor.Layer;
import com.misterchan.iconeditor.Layers;
import com.misterchan.iconeditor.Project;
import com.misterchan.iconeditor.R;
import com.misterchan.iconeditor.Reference;
import com.misterchan.iconeditor.Settings;
import com.misterchan.iconeditor.databinding.ActivityMainBinding;
import com.misterchan.iconeditor.databinding.FrameListBinding;
import com.misterchan.iconeditor.databinding.LayerListBinding;
import com.misterchan.iconeditor.dialog.AnimationClipper;
import com.misterchan.iconeditor.dialog.BitmapConfigModifier;
import com.misterchan.iconeditor.dialog.CellGridManager;
import com.misterchan.iconeditor.dialog.ColorBalanceDialog;
import com.misterchan.iconeditor.dialog.ColorMatrixManager;
import com.misterchan.iconeditor.dialog.ColorPickerDialog;
import com.misterchan.iconeditor.dialog.ColorRangeDialog;
import com.misterchan.iconeditor.dialog.CurvesDialog;
import com.misterchan.iconeditor.dialog.DirectorySelector;
import com.misterchan.iconeditor.dialog.EditNumberDialog;
import com.misterchan.iconeditor.dialog.FillWithRefDialog;
import com.misterchan.iconeditor.dialog.GuideEditor;
import com.misterchan.iconeditor.dialog.HiddenImageMaker;
import com.misterchan.iconeditor.dialog.HsDialog;
import com.misterchan.iconeditor.dialog.ImageSizeManager;
import com.misterchan.iconeditor.dialog.LevelsDialog;
import com.misterchan.iconeditor.dialog.LightingDialog;
import com.misterchan.iconeditor.dialog.MatrixManager;
import com.misterchan.iconeditor.dialog.NewImageDialog;
import com.misterchan.iconeditor.dialog.NoiseGenerator;
import com.misterchan.iconeditor.dialog.QualityManager;
import com.misterchan.iconeditor.dialog.SliderDialog;
import com.misterchan.iconeditor.listener.AfterTextChangedListener;
import com.misterchan.iconeditor.listener.OnAdapterViewItemSelectedListener;
import com.misterchan.iconeditor.listener.OnSliderChangeListener;
import com.misterchan.iconeditor.tool.BrushTool;
import com.misterchan.iconeditor.tool.Gradient;
import com.misterchan.iconeditor.tool.MagicEraser;
import com.misterchan.iconeditor.tool.Ruler;
import com.misterchan.iconeditor.tool.SelectionTool;
import com.misterchan.iconeditor.tool.Shape;
import com.misterchan.iconeditor.tool.TextTool;
import com.misterchan.iconeditor.tool.Transformer;
import com.misterchan.iconeditor.util.BitmapUtils;
import com.misterchan.iconeditor.util.CanvasUtils;
import com.misterchan.iconeditor.util.FileUtils;
import com.misterchan.iconeditor.util.RunnableRunnable;
import com.waynejo.androidndkgif.GifDecoder;
import com.waynejo.androidndkgif.GifEncoder;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements CoordinateConversions {

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
    private float blurRadius = 0.0f, eraserBlurDiameter = 0.0f;
    private float scale;
    private float softness = 0.5f;
    private float strokeWidth = 1.0f, eraserStrokeHalfWidth = 0.5f;
    private float textSize = 12.0f;
    private float translationX, translationY;
    private Frame frame;
    private FrameListBinding frameList;
    private final Gradient gradient = new Gradient();
    private InputMethodManager inputMethodManager;
    private int rulerHHeight, rulerVWidth;
    private int threshold;
    private int viewWidth, viewHeight;
    private Layer layer;
    private LayerListBinding layerList;
    private List<Long> palette;
    private List<Project> projects;
    private final MagicEraser magEr = new MagicEraser();
    private MenuItem miFrameList;
    private MenuItem miHasAlpha;
    private final ImageStateAccumulator isa = new ImageStateAccumulator();
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
            setTextAlign(Align.CENTER);
            setTextSize(24.0f);
        }
    };

    private final Paint onionSkinPaint = new Paint() {
        {
            setAntiAlias(false);
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

    private final ActivityResultCallback<List<Uri>> onImagesPickedCallback = result -> {
        if (result.size() == 1) {
            @StringRes final int r = openFile(result.get(0));
            if (r != 0)
                Snackbar.make(vContent, r, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open, v -> pickMedia())
                        .show();
        } else for (final Uri uri : result) {
            openFile(uri);
        }
    };

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia = registerForActivityResult(
            new ActivityResultContracts.PickMultipleVisualMedia(
                    SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2 ? MediaStore.getPickImagesMaxLimit() : 100),
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
        eraserBlurDiameter = f * 2.0f;
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

    private final AfterTextChangedListener onTextETTextChangedListener = s -> {
        text.s = s;
        drawTextOntoView(true);
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
        drawTextOntoView(true);
    };

    private final AfterTextChangedListener onTransformerMeshSizeETTextChangedListener = s -> {
        if (!hasSelection) return;
        if (!transformer.isRecycled()) transformer.apply();
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
            final Typeface typeface = paint.getTypeface();

            menu.findItem(switch (paint.getTextAlign()) {
                case LEFT -> R.id.i_align_left;
                case CENTER -> R.id.i_align_center;
                case RIGHT -> R.id.i_align_right;
            }).setChecked(true);

            miTypefaceBold.setChecked(typeface != null && typeface.isBold());
            miTypefaceItalic.setChecked(typeface != null && typeface.isItalic());
            miUnderlined.setChecked(paint.isUnderlineText());
            miStrikeThru.setChecked(paint.isStrikeThruText());

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
                    drawTextOntoView(true);
                }
                case R.id.i_typeface_italic -> {
                    final boolean checked = !item.isChecked();
                    item.setChecked(checked);
                    final Typeface typeface = paint.getTypeface() != null ? paint.getTypeface() : Typeface.DEFAULT;
                    final int oldStyle = typeface.getStyle();
                    final int style = checked ? oldStyle | Typeface.ITALIC : oldStyle & ~Typeface.ITALIC;
                    paint.setTypeface(Typeface.create(typeface, style));
                    drawTextOntoView(true);
                }
                case R.id.i_underlined -> {
                    final boolean checked = !item.isChecked();
                    item.setChecked(checked);
                    paint.setUnderlineText(checked);
                    drawTextOntoView(true);
                }
                case R.id.i_strike_thru -> {
                    final boolean checked = !item.isChecked();
                    item.setChecked(checked);
                    paint.setStrikeThruText(checked);
                    drawTextOntoView(true);
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
            drawTextOntoView(true);
        }

        private void setTypeface(Typeface typeface) {
            paint.setTypeface(Typeface.create(typeface,
                    paint.getTypeface() != null ? paint.getTypeface().getStyle() : Typeface.NORMAL));
            drawTextOntoView(true);
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
            if (transformer.isRecycled()) return;
            final Rect bounds = new Rect(selection.r);
            if (transformer.undoAction != null) {
                canvas.drawBitmap(transformer.undoAction.bm(), null, transformer.undoAction.rect(), PAINT_BITMAP);
                bounds.union(transformer.undoAction.rect());
            }
            recycleTransformer();
            if (transformer.mesh != null) transformer.resetMesh();
            optimizeSelection();
            drawBitmapOntoView(bounds, true);
            drawSelectionOntoView();
        }
    };

    private final BitmapConfigModifier.OnChangedListener onNewConfigBitmapCreatedListener = bitmap -> {
        if (bitmap == null) {
            Snackbar.make(vContent, R.string.this_config_is_not_supported_yet, Snackbar.LENGTH_LONG).show();
            return;
        }
        this.bitmap.recycle();
        this.bitmap = layer.bitmap = bitmap;
        canvas = layer.canvas = new Canvas(bitmap);
        drawBitmapOntoView(true);
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
        drawTextOntoView(text.rect(), true);
    };

    private final CompoundButton.OnCheckedChangeListener onTransformerFilterCheckedChangeListener = (buttonView, isChecked) -> {
        if (!transformer.isRecycled() && transformer.mesh != null) {
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

    private final View.OnClickListener onAddPaletteColorButtonClickListener = v ->
            new ColorPickerDialog(this, R.string.add,
                    (oldColor, newColor) -> {
                        palette.add(0, newColor);
                        colorAdapter.notifyItemInserted(0);
                        activityMain.rvPalette.scrollToPosition(0);
                        Settings.INST.savePalette(palette);
                    },
                    paint.getColorLong())
                    .show();

    @SuppressLint("NonConstantResourceId")
    private final PopupMenu.OnMenuItemClickListener onPaletteOptionsItemSelectedListener = item -> {
        switch (item.getItemId()) {
            default -> {
                return false;
            }
            case R.id.i_add ->
                    onAddPaletteColorButtonClickListener.onClick(activityMain.bPaletteAdd);
            case R.id.i_add_all -> {
                if (!hasSelection) selectAll();
                if (selection.r.width() * selection.r.height() >= 0x1000000) {
                    Snackbar.make(vContent, R.string.selection_too_large, Snackbar.LENGTH_LONG).show();
                    break;
                }
                final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.sampling)
                        .setView(R.layout.progress_indicator)
                        .setCancelable(false)
                        .show();
                final LinearProgressIndicator pi = dialog.findViewById(R.id.progress_indicator);
                new Thread(() -> {
                    final int[] pixels = BitmapUtils.getPixels(bitmap, selection.r);
                    runOnUiThread(() -> pi.setMax(pixels.length));
                    final Collection<Long> colorSet = BitmapUtils.getColorSet(pixels,
                            progress -> runOnUiThread(() -> pi.setProgressCompat(progress, true)));
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        new MaterialAlertDialogBuilder(this)
                                .setTitle(R.string.add_all_colors_in_layer)
                                .setMessage(getString(R.string.there_are_colors, colorSet.size()))
                                .setPositiveButton(R.string.add_them_all, (d, which) -> {
                                    palette.addAll(0, colorSet);
                                    Settings.INST.savePalette(palette);
                                    colorAdapter.notifyItemRangeRemoved(0, palette.size());
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    });
                }).start();
            }
            case R.id.i_clear -> {
                final int count = palette.size();
                palette.clear();
                colorAdapter.notifyItemRangeRemoved(0, count);
                Settings.INST.savePalette(palette);
            }
        }
        return true;
    };

    private final View.OnLongClickListener onAddPaletteColorButtonLongClickListener = v -> {
        final PopupMenu popupMenu = new PopupMenu(this, v);
        final Menu menu = popupMenu.getMenu();
        MenuCompat.setGroupDividerEnabled(menu, true);
        popupMenu.getMenuInflater().inflate(R.menu.palette, menu);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(onPaletteOptionsItemSelectedListener);
        popupMenu.show();
        return true;
    };

    private final View.OnClickListener onBackgroundColorClickListener = v ->
            new ColorPickerDialog(this, R.string.background_color,
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
                    updateBrush(switch (which) {
                        default -> BrushTool.TipShape.PRESET_BRUSH;
                        case 1 -> BrushTool.TipShape.REF;
                    });
                })
                .setPositiveButton(R.string.ok, null)
                .show();
    };

    private final View.OnClickListener onCloneStampSrcButtonClickListener = v -> {
        cloneStampSrc = null;
        eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
    };

    private final View.OnClickListener onForegroundColorClickListener = v ->
            new ColorPickerDialog(this, R.string.foreground_color,
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
        editPreview.edit((src, dst) -> BitmapUtils.addColorMatrixColorFilter(src, dst, matrix));
        drawEditPreviewOntoView();
    }, true);

    private final ColorRangeDialog.OnChangedListener onLayerColorRangeChangedListener = (colorRange, stopped) -> {
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(getString(R.string.state_color_range,
                colorRange.cuboid[0], colorRange.cuboid[3], colorRange.cuboid[1] * 100.0f, colorRange.cuboid[4] * 100.0f, colorRange.cuboid[2] * 100.0f, colorRange.cuboid[5] * 100.0f));
    };

    private final CurvesDialog.OnCurvesChangedListener onFilterCurvesChangedListener = (curves, stopped) -> runOrStart(() -> {
        if (!editPreview.visible()) return;
        editPreview.edit((src, dst) -> BitmapUtils.applyCurves(src, dst, curves));
        drawEditPreviewOntoView();
    }, stopped);

    private final Runnable onEditPreviewCancelListener = () -> {
        drawBitmapOntoView(selection.r, true);
        recycleEditPreview();
        clearStatus();
    };

    private final Runnable onEditPreviewPBClickListener = () -> editPreview.prepareToCommit();

    private void onEditPreviewCommit(boolean entire) {
        saveStepBackToHistory(entire ? null : editPreview.getRect());
        drawEditPreviewIntoImage(entire);
        saveStepForwardToHistory();
        runOnUiThread(this::clearStatus);
    }

    private final FillWithRefDialog.OnChangeListener onFillWithRefTileModeChangeListener = (bitmap, tileMode, stopped) -> runOrStart(() -> {
        paint.setShader(new BitmapShader(bitmap, tileMode, tileMode));
        editPreview.revert();
        editPreview.getCanvas().drawRect(editPreview.getRect(), paint);
        drawEditPreviewOntoView();
    }, stopped);

    private final HiddenImageMaker.OnMakeListener onHiddenImageMakeListener = bitmap -> {
        final Bitmap bm = BitmapUtils.createBitmap(bitmap);
        addProject(bm, activityMain.tlProjectList.getSelectedTabPosition() + 2);
        bitmap.recycle();
    };

    private final HsDialog.OnChangedListener onFilterHsChangedListener = (deltaHs, stopped) -> {
        runOrStart(() -> {
            if (!editPreview.visible()) return;
            if (deltaHs[0] == 0.0f && deltaHs[1] == 0.0f && deltaHs[2] == 0.0f) {
                editPreview.revert();
            } else {
                editPreview.edit((src, dst) -> BitmapUtils.shiftHs(src, dst, deltaHs));
            }
            drawEditPreviewOntoView();
        }, stopped);
        showStateOfHs(deltaHs);
    };

    private final HsDialog.OnChangedListener onLayerHsChangedListener = (deltaHs, stopped) -> {
        layer.deltaHs = deltaHs;
        drawBitmapOntoView(stopped);
        showStateOfHs(deltaHs);
    };

    private final AdapterView.OnItemSelectedListener onGradientColorsSpinnerItemSelectedListener = (OnAdapterViewItemSelectedListener) (parent, view, position, id) -> {
        gradient.colors = switch (position) {
            default -> Gradient.Colors.PAINTS;
            case 1 -> Gradient.Colors.PALETTE;
        };
    };

    private final AdapterView.OnItemSelectedListener onGradientTypeSpinnerItemSelectedListener = (OnAdapterViewItemSelectedListener) (parent, view, position, id) -> {
        gradient.type = switch (position) {
            default -> Gradient.Type.LINEAR;
            case 1 -> Gradient.Type.RADIAL;
            case 2 -> Gradient.Type.SWEEP;
        };
    };

    private final LevelsDialog.OnLevelsChangedListener onFilterLevelsChangedListener = (inputShadows, inputHighlights, outputShadows, outputHighlights, stopped) -> {
        final float ratio = (outputHighlights - outputShadows) / (inputHighlights - inputShadows);
        runOrStart(() -> {
            if (!editPreview.visible()) return;
            editPreview.edit((src, dst) ->
                    BitmapUtils.addLightingColorFilter(src, dst, ratio, -inputShadows * ratio + outputShadows));
            drawEditPreviewOntoView();
        }, stopped);
    };

    private final LevelsDialog.OnLevelsChangedListener onLayerLevelsChangedListener = (inputShadows, inputHighlights, outputShadows, outputHighlights, stopped) -> {
        final float ratio = (outputHighlights - outputShadows) / (inputHighlights - inputShadows);
        layer.lighting[0] = layer.lighting[2] = layer.lighting[4] = ratio;
        layer.lighting[1] = layer.lighting[3] = layer.lighting[5] = -inputShadows * ratio + outputShadows;
        drawBitmapOntoView(stopped);
    };

    private final LightingDialog.OnLightingChangedListener onFilterLightingChangedListener = (lighting, stopped) -> runOrStart(() -> {
        editPreview.edit((src, dst) -> BitmapUtils.addLightingColorFilter(src, dst, lighting));
        drawEditPreviewOntoView();
    }, true);

    private final MatrixManager.OnMatrixElementsChangedListener onMatrixChangedListener = matrix -> runOrStart(() -> {
        if (editPreview.committed()) {
            onEditPreviewCommit(true);
            return;
        }
        editPreview.transform(matrix);
        drawBitmapOntoView(editPreview.getEntire(), true);
    }, true);

    private final NewImageDialog.OnApplyListener onNewImagePropertiesApplyListener = this::createImage;

    private final NoiseGenerator.OnPropChangedListener onNoisePropChangedListener = (properties, stopped) -> {
        runOrStart(() -> {
            if (editPreview.committed()) {
                onEditPreviewCommit(false);
                return;
            }
            if (properties.noisiness() == 0.0f) {
                editPreview.revert();
            } else switch (properties.drawingPrimitive()) {
                case PIXEL -> {
                    if (properties.noisiness() == 1.0f && properties.noRepeats()) {
                        editPreview.drawColor(paint.getColor(), BlendMode.SRC);
                        break;
                    }
                    editPreview.revert();
                    editPreview.edit((src, dst) -> BitmapUtils.generateNoise(dst, paint.getColor(),
                            properties.noisiness(), properties.seed(), properties.noRepeats()));
                }
                case POINT -> {
                    if (properties.noisiness() == 1.0f && properties.noRepeats()) {
                        editPreview.drawColor(paint.getColor(), BlendMode.SRC);
                        break;
                    }
                    editPreview.revert();
                    BitmapUtils.generateNoise(editPreview.getCanvas(), editPreview.getRect(), paint,
                            properties.noisiness(), properties.seed(), properties.noRepeats());
                }
                case REF -> {
                    editPreview.revert();
                    BitmapUtils.generateNoise(editPreview.getCanvas(), editPreview.getRect(),
                            !ref.recycled() ? ref.bm() : editPreview.getOriginal(), paint,
                            properties.noisiness(), properties.seed(), properties.noRepeats());
                }
            }
            drawEditPreviewOntoView();
        }, stopped);
        clearStatus();
    };

    private final OnSliderChangeListener onFilterContrastSliderChangeListener = (slider, value, stopped) -> {
        final float mul = value, add = 0xFF / 2.0f * (1.0f - mul);
        runOrStart(() -> {
            if (!editPreview.visible()) return;
            editPreview.edit((src, dst) -> BitmapUtils.addLightingColorFilter(src, dst, mul, add));
            drawEditPreviewOntoView();
        }, stopped);
        showStateForEditPreview(getString(R.string.state_contrast, mul));
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
            if (!editPreview.visible()) return;
            editPreview.edit((src, dst) -> BitmapUtils.setAlphaByHue(src, dst, value));
            drawEditPreviewOntoView();
        }, stopped);
        showStateForEditPreview(getString(R.string.state_hue, value));
    };

    private final OnSliderChangeListener onFilterLightnessSliderChangeListener = (slider, value, stopped) -> {
        runOrStart(() -> {
            if (!editPreview.visible()) return;
            editPreview.edit((src, dst) -> BitmapUtils.addLightingColorFilter(src, dst, 1.0f, value));
            drawEditPreviewOntoView();
        }, stopped);
        showStateForEditPreview(getString(R.string.state_lightness, (int) value));
    };

    private final OnSliderChangeListener onLayerLightnessSliderChangeListener = (slider, value, stopped) -> {
        layer.lighting[1] = layer.lighting[3] = layer.lighting[5] = value;
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(getString(R.string.state_lightness, (int) value));
    };

    @SuppressLint("StringFormatMatches")
    private final OnSliderChangeListener onFilterPosterizationSliderChangeListener = (slider, value, stopped) -> {
        runOrStart(() -> {
            if (!editPreview.visible()) return;
            editPreview.edit((src, dst) -> BitmapUtils.posterize(src, dst, (int) value));
            drawEditPreviewOntoView();
        }, stopped);
        showStateForEditPreview(String.format(
                getString(R.string.state_posterization, Settings.INST.colorIntCompFormat()),
                (int) value));
    };

    private final OnSliderChangeListener onFilterSaturationSliderChangeListener = (slider, value, stopped) -> {
        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(value);
        runOrStart(() -> {
            editPreview.edit((src, dst) -> BitmapUtils.addColorMatrixColorFilter(src, dst, colorMatrix.getArray()));
            drawEditPreviewOntoView();
        }, stopped);
        showStateForEditPreview(getString(R.string.state_saturation, value));
    };

    private final OnSliderChangeListener onLayerSaturationSliderChangeListener = (slider, value, stopped) -> {
        layer.colorMatrix.setSaturation(value);
        drawBitmapOntoView(stopped);
        activityMain.tvStatus.setText(getString(R.string.state_saturation, value));
    };

    private final OnSliderChangeListener onFilterThresholdSliderChangeListener = (slider, value, stopped) -> {
        final float f = -0x100 * value;
        runOrStart(() -> {
            editPreview.edit((src, dst) -> BitmapUtils.addColorMatrixColorFilter(src, dst, new float[]{
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.213f * 0x100, 0.715f * 0x100, 0.072f * 0x100, 0.0f, f,
                    0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            }));
            drawEditPreviewOntoView();
        }, stopped);
        showStateForEditPreview(getString(R.string.state_threshold, (int) value));
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
                getString(R.string.state_alpha, Settings.INST.colorIntCompFormat()),
                (int) value));
    };

    private final OnSliderChangeListener onThresholdChangeListener = (slider, value, stopped) -> {
        threshold = (int) value;
        runOrStart(() -> {
            if (!editPreview.visible()) return;
            if (threshold == 0xFF) {
                editPreview.drawColor(Color.BLACK, BlendMode.SRC_IN);
            } else if (threshold == 0x00) {
                editPreview.revert();
            } else {
                editPreview.edit((src, dst) -> BitmapUtils.posterize(src, dst, 0xFF - threshold));
            }
            drawEditPreviewOntoView();
        }, stopped);
        activityMain.tvStatus.setText(getString(R.string.state_threshold, threshold));
    };

    private final View.OnClickListener onToleranceButtonClickListener = v -> {
        createEditPreview(true, true);
        new SliderDialog(this).setTitle(R.string.tolerance).setValueFrom(0x00).setValueTo(0xFF).setValue(threshold)
                .setStepSize(1.0f)
                .setOnChangeListener(onThresholdChangeListener)
                .setOnActionListener(onEditPreviewCancelListener)
                .show();
        onThresholdChangeListener.onChange(null, threshold, true);
    };

    private final MovableItemAdapter.OnItemMoveListener onFrameItemMoveListener = (fromPos, toPos) -> {
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

    private final MovableItemAdapter.OnItemMoveListener onLayerItemMoveListener = (fromPos, toPos) -> {
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
            final int start = Math.min(fromPos, toPos), count = Math.abs(toPos - fromPos) + 1, stop = start + count, size = frame.layers.size();
            frame.layerAdapter.notifyItemRangeChanged(start, count);
            if (start > 0)
                frame.layerAdapter.notifyItemRangeChanged(0, start, LayerAdapter.Payload.LEVEL);
            if (stop < size)
                frame.layerAdapter.notifyItemRangeChanged(stop, size - stop, LayerAdapter.Payload.LEVEL);
        });
    };

    private final MovableItemAdapter.OnItemSelectedListener onFrameItemSelectedListener = (view, position) -> {
        final int unselectedPos = project.selectedFrameIndex;
        project.frames.get(unselectedPos).updateThumbnail();
        selectFrame(position);
        frameList.rvFrameList.post(() -> {
            project.frameAdapter.notifyItemChanged(unselectedPos, FrameAdapter.Payload.SELECTED);
            project.frameAdapter.notifyItemChanged(position, FrameAdapter.Payload.SELECTED);
        });
    };

    private final MovableItemAdapter.OnItemSelectedListener onFrameItemReselectedListener = (view, position) -> {
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
    private final MovableItemAdapter.OnItemSelectedListener onLayerItemSelectedListener = (view, position) -> {
        final int unselectedPos = frame.selectedLayerIndex;
        selectLayer(position);
        layerList.rvLayerList.post(() -> {
            frame.layerAdapter.notifyItemChanged(unselectedPos, LayerAdapter.Payload.SELECTED);
            frame.layerAdapter.notifyItemChanged(position, LayerAdapter.Payload.SELECTED);
        });
    };

    private final MovableItemAdapter.OnItemSelectedListener onLayerItemReselectedListener = (view, position) -> {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        final Menu menu = popupMenu.getMenu();
        MenuCompat.setGroupDividerEnabled(menu, true);
        popupMenu.getMenuInflater().inflate(R.menu.layer, menu);
        popupMenu.setForceShowIcon(true);
        popupMenu.setOnMenuItemClickListener(this::onLayerOptionsItemSelected);
        popupMenu.show();

        menu.findItem(R.id.i_layer_clipped).setChecked(layer.clipped);
        menu.findItem(R.id.i_layer_level_up).setEnabled(layer.getLevel() > 0);
        menu.findItem(R.id.i_layer_reference).setChecked(layer.reference);

        menu.findItem(R.id.i_layer_blend_mode).getSubMenu()
                .getItem(layer.paint.getBlendMode().ordinal()).setChecked(true);

        menu.findItem(layer.filter == null ? R.id.i_layer_filter_none : switch (layer.filter) {
            case COLOR_BALANCE -> R.id.i_layer_filter_color_balance;
            case COLOR_MATRIX -> R.id.i_layer_filter_color_matrix;
            case CONTRAST -> R.id.i_layer_filter_contrast;
            case CURVES -> R.id.i_layer_filter_curves;
            case HS -> R.id.i_layer_filter_hs;
            case LEVELS -> R.id.i_layer_filter_levels;
            case LIGHTING -> R.id.i_layer_filter_lighting;
            case LIGHTNESS -> R.id.i_layer_filter_lightness;
            case SATURATION -> R.id.i_layer_filter_saturation;
            case SELECTED_BY_CR -> R.id.i_layer_filter_selected_by_cr;
            case THRESHOLD -> R.id.i_layer_filter_threshold;
        }).setChecked(true);
    };

    private final TabLayout.OnTabSelectedListener onProjTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            drawFloatingLayersIntoImage();

            final int position = tab.getPosition();
            project = projects.get(position);
            frameList.rvFrameList.setAdapter(project.frameAdapter);

            translationX = project.translationX;
            translationY = project.translationY;
            scale = project.scale;

            if (hasSelection && frame.layers.size() > 0) {
                final Bitmap unselectedBm = frame.getBackgroundLayer().bitmap;
                final Bitmap selectedBm = project.getFirstFrame().getBackgroundLayer().bitmap;
                if (selectedBm.getWidth() != unselectedBm.getWidth() || selectedBm.getHeight() != unselectedBm.getHeight()) {
                    hasSelection = false;
                }
            }

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
    private final View.OnTouchListener onRulerHTouchListener = new View.OnTouchListener() {
        private Guide guide;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    guide = new Guide();
                    guide.orientation = Guide.ORIENTATION_HORIZONTAL;
                    project.guides.offerFirst(guide); // Add at the front for faster removal if necessary later
                }
                case MotionEvent.ACTION_MOVE -> {
                    guide.position = toBitmapYAbs(event.getY() - rulerHHeight);
                    drawGridOntoView();
                    activityMain.tvStatus.setText(getString(R.string.position_, guide.position));
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    final float y = event.getY();
                    if (!(rulerHHeight <= y && y < rulerHHeight + viewHeight)) {
                        project.guides.remove(guide);
                        drawGridOntoView();
                        clearStatus();
                    }
                    guide = null;
                }
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onRulerVTouchListener = new View.OnTouchListener() {
        private Guide guide;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    guide = new Guide();
                    guide.orientation = Guide.ORIENTATION_VERTICAL;
                    project.guides.offerFirst(guide); // Add at the front for faster removal if necessary later
                }
                case MotionEvent.ACTION_MOVE -> {
                    guide.position = toBitmapXAbs(event.getX() - rulerVWidth);
                    drawGridOntoView();
                    activityMain.tvStatus.setText(getString(R.string.position_, guide.position));
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    final float x = event.getX();
                    if (!(rulerVWidth <= x && x < rulerVWidth + viewWidth)) {
                        project.guides.remove(guide);
                        drawGridOntoView();
                        clearStatus();
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
                if (i < project.selectedFrameIndex) --project.selectedFrameIndex;
            }
        }
        frameList.rvFrameList.post(() -> project.frameAdapter.notifyDataSetChanged());
        selectFrame(Math.min(project.selectedFrameIndex, project.frames.size() - 1));
        project.history.optimize();
    };

    private final CellGridManager.OnApplyListener onCellGridApplyListener = this::drawGridOntoView;

    private final ImageSizeManager.OnApplyListener onImageSizeApplyListener = (width, height, transform) -> {
        drawFloatingLayersIntoImage();
        if (layer == frame.getBackgroundLayer()) {
            for (final Frame f : project.frames) {
                final Layer bl = f.getBackgroundLayer();
                if (f.layers.size() > 1 && transform != ImageSizeManager.ScaleType.CROP) {
                    final Matrix matrix = new Matrix();
                    matrix.setRectToRect(
                            new RectF(bl.left, bl.top, bl.left + bl.bitmap.getWidth(), bl.top + bl.bitmap.getHeight()),
                            new RectF(bl.left, bl.top, bl.left + width, bl.top + height),
                            Matrix.ScaleToFit.FILL);
                    for (int i = 0; i < f.layers.size() - 1; ++i) {
                        final Layer l = f.layers.get(i);
                        final RectF rf = new RectF(l.left, l.top, l.left + l.bitmap.getWidth(), l.top + l.bitmap.getHeight());
                        matrix.mapRect(rf);
                        final Rect r = new Rect();
                        rf.round(r);
                        l.moveTo(r.left, r.top);
                        resizeImage(l, r.width(), r.height(), transform, null);
                    }
                }
                resizeImage(bl, width, height, transform, null);
                f.updateThumbnail();
            }
        } else {
            resizeImage(layer, width, height, transform, null);
        }
        drawBitmapOntoView(true, true);
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
                    calcPaintStrokeRad());
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
            return MainActivity.this.mapRect(x0, y0, x1, y1, calcPaintStrokeRad());
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
            return MainActivity.this.mapRect(x0, y0, x1, y1, calcPaintStrokeRad());
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
            return MainActivity.this.mapRect(x0, y0, x1, y1, calcPaintStrokeRad());
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

        public boolean isMultiTouch() {
            return multiTouch;
        }

        public abstract void onIVSingleTouch(View v, MotionEvent event);

        @Override
        public final void onIVTouch(View v, MotionEvent event) {
            final int pointerCount = event.getPointerCount(), action = event.getAction();
            if (pointerCount == 1 && !multiTouch) {
                onIVSingleTouch(v, event);
            } else if (pointerCount <= 2) {
                if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && !multiTouch) {
                    multiTouch = true;
                    onMultiTouchBegin();
                    isShapeStopped = true;
                    undo();
                    if (lastMerged == null || lastMerged.isRecycled()) mergeLayersEntire();
                }
                onIVTouchWithZoomToolListener.onIVTouch(v, event);
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (multiTouch) multiTouch = false;
                else onTouchEnd();
            }
        }

        protected void onMultiTouchBegin() {
        }

        protected void onTouchEnd() {
            BitmapUtils.recycle(lastMerged);
        }

        protected void undo() {
            if (!isa.isRecycled()) isa.drawOnto(canvas);
        }
    }

    private final View.OnTouchListener onIVTouchWithBrushListener = new OnIVMultiTouchListener() {
        private float lastRad;
        private float maxRad;
        private int lastBX, lastBY;
        private VelocityTracker velocityTracker;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            if (brush.recycled()) return;
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
                    if (vel == 0.0f) break;
                    final float rad = Math.min(maxRad / (vel + 1.0f / softness) / softness, maxRad);
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
                            for (float r = lastRad, bx = lastBX, by = lastBY, s = 0.0f; s < stepCount && !isMultiTouch(); r += stepRad, bx += stepBX, by += stepBY, ++s) {
                                canvas.drawBitmap(brush.bm(), null, new RectF(bx - r, by - r, bx + r, by + r), PAINT_SRC_OVER);
                            }
                            runOnUiThread(() -> drawBitmapOntoView(lastBX, lastBY, currBX, currBY, maxRad + blurRadius * 2.0f + 1.0f));
                        }).start();
                    }
                    isa.unionBounds(lastBX, lastBY, currBX, currBY, maxRad + blurRadius * 2.0f + 1.0f);
                    lastRad = rad;
                    lastBX = currBX;
                    lastBY = currBY;
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    velocityTracker.recycle();
                    saveStateToHistory();
                    clearStatus();
                }
            }
        }

        @Override
        protected void onMultiTouchBegin() {
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
                    final Rect dstRect = hasSelection ? selection.r : null;
                    final Rect srcRect = src == bitmap ? dstRect
                            : hasSelection ? new Rect(selection.r) : new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    if (src != bitmap) {
                        srcRect.offset(layer.left, layer.top);
                        if (!srcRect.intersect(0, 0, src.getWidth(), src.getHeight())) break;
                    }
                    runOrStart(() -> {
                        final Rect bounds = new Rect(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
                        if (activityMain.optionsBucketFill.cbContiguous.isChecked()) {
                            BitmapUtils.floodFill(src, srcRect, bitmap, dstRect, bx, by, paint.getColor(),
                                    activityMain.optionsBucketFill.cbIgnoreAlpha.isChecked(), threshold, bounds);
                        } else {
                            BitmapUtils.bucketFill(src, srcRect, bitmap, dstRect, bx, by, paint.getColor(),
                                    activityMain.optionsBucketFill.cbIgnoreAlpha.isChecked(), threshold, bounds);
                        }
                        if (dstRect == null) drawBitmapOntoView(true);
                        else drawBitmapOntoView(dstRect, true);
                        isa.unionBounds(bounds);
                        saveStateToHistory();
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
                    if (cloneStampSrc == null) break;
                    dx = cloneStampSrc.x - bx;
                    dy = cloneStampSrc.y - by;
                    lastBX = bx;
                    lastBY = by;

                case MotionEvent.ACTION_MOVE: {
                    if (cloneStampSrc == null) break;

                    final int width = (int) (Math.abs(bx - lastBX) + strokeWidth + blurRadius * 4.0f),
                            height = (int) (Math.abs(by - lastBY) + strokeWidth + blurRadius * 4.0f);
                    final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    final float rad = calcPaintStrokeRad();
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
                    isa.unionBounds((int) left, (int) top, (int) (left + width), (int) (top + height));
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
                        saveStateToHistory();
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
                    drawBitmapStateOntoView(lastBX, lastBY, bx, by, eraserStrokeHalfWidth + eraserBlurDiameter + 1.0f);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    lastBX = bx;
                    lastBY = by;
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    saveStateToHistory();
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
                            getString(R.string.state_eyedropper_imprecise, Settings.INST.colorIntCompFormat()),
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
                    final Color color = activityMain.optionsEyedropper.btgSrc.getCheckedButtonId() == R.id.b_all_layers
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

    private final View.OnTouchListener onIVTouchWithGradientListener = new OnIVMultiTouchListener() {
        private int shapeStartX, shapeStartY;

        @ColorLong
        private long[] lastCopiedPalette;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            final float x = event.getX(), y = event.getY();
            final int bx = toBitmapX(x), by = toBitmapY(y);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isShapeStopped) {
                        isShapeStopped = false;
                        dpPreview.getCanvas().drawPoint(bx + 0.5f, by + 0.5f, paint);
                        drawBitmapOntoView(bx, by, bx + 1, by + 1);
                        if (gradient.colors == Gradient.Colors.PALETTE) {
                            lastCopiedPalette = new long[palette.size()];
                            for (int i = 0; i < palette.size(); ++i) {
                                lastCopiedPalette[i] = palette.get(i);
                            }
                        }
                        shapeStartX = bx;
                        shapeStartY = by;
                        activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                        break;
                    }
                case MotionEvent.ACTION_MOVE: {
                    if (bx == shapeStartX && by == shapeStartY) break;
                    paint.setShader(gradient.createShader(shapeStartX, shapeStartY, bx, by, switch (gradient.colors) {
                        case PAINTS -> new long[]{paint.getColorLong(), eraser.getColorLong()};
                        case PALETTE -> lastCopiedPalette;
                    }));
                    dpPreview.erase();
                    dpPreview.getCanvas().drawPaint(paint);
                    drawBitmapOntoView();
                    activityMain.tvStatus.setText(getString(R.string.state_start_stop,
                            shapeStartX, shapeStartY, bx, by));
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (bx == shapeStartX && by == shapeStartY) break;
                    isShapeStopped = true;
                    saveStepBackToHistory(null);
                    canvas.drawPaint(paint);
                    dpPreview.erase();
                    drawBitmapOntoView(true);
                    saveStepForwardToHistory();
                    clearStatus();
                    break;
            }
        }

        @Override
        public void onMultiTouchBegin() {
            paint.setShader(null);
        }

        @Override
        protected void undo() {
            if (!dpPreview.isRecycled()) dpPreview.erase();
        }
    };

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private final View.OnTouchListener onIVTouchWithGradientLineListener = new OnIVMultiTouchListener() {
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
                case MotionEvent.ACTION_DOWN:
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
                case MotionEvent.ACTION_MOVE: {
                    paint.setShader(new LinearGradient(shapeStartX, shapeStartY, bx, by,
                            color0,
                            src.getColor(satX(src, bx), satY(src, by)).pack(),
                            Shader.TileMode.CLAMP));
                    dpPreview.erase(lastRect);
                    CanvasUtils.drawInclusiveLine(dpPreview.getCanvas(), shapeStartX, shapeStartY, bx, by, paint);
                    final Rect rect = mapRect(shapeStartX, shapeStartY, bx, by, calcPaintStrokeRad());
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
                        final Rect rect = mapRect(shapeStartX, shapeStartY, bx, by, calcPaintStrokeRad());
                        lastRect.union(rect);
                        saveStepBackToHistory(lastRect);
                        CanvasUtils.drawInclusiveLine(canvas, shapeStartX, shapeStartY, bx, by, paint);
                        dpPreview.erase(lastRect);
                        drawBitmapOntoView(lastRect, true);
                        lastRect = null;
                        saveStepForwardToHistory();
                        clearStatus();
                    }
                    break;
            }
        }

        @Override
        public void onMultiTouchBegin() {
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
                    final int rad = (int) (calcPaintStrokeRad());
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
                    cLine.drawLine(lastBX - left, lastBY - top, bx - left, by - top, paint);
                    canvas.drawBitmap(bLine, left, top, PAINT_DST_OUT);
                    final Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    new Canvas(bm).drawBitmap(ref.bm(),
                            new Rect(left, top, right, bottom), new Rect(0, 0, width, height), PAINT_SRC);
                    BitmapUtils.removeBackground(bm, foregroundColor, backgroundColor);
                    cLine.drawBitmap(bm, 0.0f, 0.0f, PAINT_SRC_IN);
                    bm.recycle();
                    canvas.drawBitmap(bLine, left, top, PAINT_SRC_OVER);
                    bLine.recycle();

                    drawBitmapOntoView(left, top, right, bottom);
                    isa.unionBounds(left, top, right, bottom);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    lastX = x;
                    lastY = y;
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    saveStateToHistory();
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
                        case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            saveStateToHistory();
                        }
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

                            final int rad = (int) (calcPaintStrokeRad());
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
                            isa.unionBounds(left, top, right, bottom);
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

                    final int rad = (int) (calcPaintStrokeRad());
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
                        BitmapUtils.floodFill(bm, null, bThr, null,
                                relativeX, relativeY, Color.BLACK, true, threshold);
                        bm.recycle();
                        cLine.drawBitmap(bThr, 0.0f, 0.0f, PAINT_DST_IN);
                        bThr.recycle();
                    }
                    canvas.drawBitmap(bLine, left, top, magicPaint);
                    bLine.recycle();

                    drawBitmapOntoView(left, top, right, bottom);
                    isa.unionBounds(left, top, right, bottom);
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    lastBX = bx;
                    lastBY = by;
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    saveStateToHistory();
                    clearStatus();
                    break;
            }
        }
    };

    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithMarqueeListener = new OnIVMultiTouchListener() {
        private boolean hasDraggedBound = false;
        private int startX, startY;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
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

        @Override
        protected void onTouchEnd() {
        }

        @Override
        protected void undo() {
            hasSelection = false;
            drawSelectionOntoView();
            selection.marqBoundBeingDragged = null;
            hasDraggedBound = false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onIVTouchWithPatcherListener = new OnIVMultiTouchListener() {
        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            if (!hasSelection) return;
            final float radius = calcPaintStrokeRad();
            if (selection.r.left + radius * 2.0f >= selection.r.right || selection.r.top + radius * 2.0f >= selection.r.bottom) {
                return;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    createEditPreview(false, false);

                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX(), y = event.getY();
                    final int bx = toBitmapX(x), by = toBitmapY(y);
                    final int w = selection.r.width(), h = selection.r.height();
                    final int wh = w >> 1, hh = h >> 1; // h - Half
                    editPreview.revert();
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
                    onEditPreviewCommit(false);
                    break;
            }
        }

        @Override
        protected void undo() {
            recycleEditPreview();
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
                    final RectF boundsF = new RectF();
                    path.computeBounds(boundsF, false);
                    @IdRes final int drawingPrimitive = activityMain.optionsPath.btgDrawingPrimitives.getCheckedButtonId();
                    final float rad = calcPaintStrokeRad() + (drawingPrimitive == R.id.b_text ? textSize : 0);
                    boundsF.inset(-rad, -rad);
                    final Rect bounds = new Rect();
                    boundsF.roundOut(bounds);
                    saveStepBackToHistory(bitmap, bounds);
                    switch (drawingPrimitive) {
                        case R.id.b_path -> canvas.drawPath(path, paint);
                        case R.id.b_text ->
                                canvas.drawTextOnPath(activityMain.optionsText.tietText.getText().toString(), path, 0.0f, 0.0f, paint);
                    }
                    drawBitmapOntoView(bounds.left, bounds.top, bounds.right, bounds.bottom);
                    eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
                    saveStepForwardToHistory();
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
                    drawBitmapStateOntoView(lastBX, lastBY, bx, by, calcPaintStrokeRad());
                    activityMain.tvStatus.setText(getString(R.string.coordinates, bx, by));
                    lastBX = bx;
                    lastBY = by;
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    saveStateToHistory();
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
                        final Rect rect = shape.mapRect(shapeStartX, shapeStartY, bx, by);
                        lastRect.union(rect);
                        saveStepBackToHistory(lastRect);
                        shape.drawShapeOntoCanvas(canvas, shapeStartX, shapeStartY, bx, by);
                        dpPreview.erase(lastRect);
                        drawBitmapOntoView(lastRect, true);
                        lastRect = null;
                        saveStepForwardToHistory();
                        clearStatus();
                    }
                    break;
            }
        }

        @Override
        protected void undo() {
            if (!dpPreview.isRecycled()) dpPreview.erase();
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
                    }
                    case MotionEvent.ACTION_MOVE -> {
                        final float x = event.getX(), y = event.getY();
                        text.x = toUnscaled(x - dx);
                        text.y = toUnscaled(y - dy);
                        drawTextOntoView(text.getMeasuredBounds(paint, calcPaintStrokeRad()));
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
                        drawTextOntoView(text.measure(paint, calcPaintStrokeRad()));
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
        private float dx, dy;
        private int lastVertIndex = -1;

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            if (!hasSelection) return;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    if (selection.r.isEmpty() || transformer.mesh == null) break;
                    if (transformer.isRecycled()) createTransformer();
                    final float rx = toBitmapXExact(event.getX()) - selection.r.left, ry = toBitmapYExact(event.getY()) - selection.r.top;
                    lastVertIndex = Math.round(ry / selection.r.height() * transformer.mesh.height) * (transformer.mesh.width + 1)
                            + Math.round(rx / selection.r.width() * transformer.mesh.width);
                    if (0 > lastVertIndex || lastVertIndex << 1 >= transformer.mesh.verts.length) {
                        lastVertIndex = -1;
                        break;
                    }
                    dx = rx - transformer.mesh.verts[lastVertIndex << 1];
                    dy = ry - transformer.mesh.verts[lastVertIndex << 1 | 1];
                }
                case MotionEvent.ACTION_MOVE -> {
                    if (transformer.isRecycled() || transformer.mesh == null || lastVertIndex < 0)
                        break;
                    final float rx = toBitmapXExact(event.getX()) - selection.r.left, ry = toBitmapYExact(event.getY()) - selection.r.top;
                    transformer.mesh.verts[lastVertIndex << 1] = rx - dx;
                    transformer.mesh.verts[lastVertIndex << 1 | 1] = ry - dy;
                    drawSelectionOntoView();
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (transformer.isRecycled() || transformer.mesh == null || lastVertIndex < 0)
                        break;
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
        private float[] src, dst;
        private int pointCount = 0;
        private Matrix matrix;

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            if (!hasSelection || selection.r.isEmpty()) return;
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN -> {
                    if (transformer.isRecycled()) createTransformer();
                    src = new float[10];
                    dst = new float[10];
                    pointCount = 1;
                    final float x = event.getX(), y = event.getY();
                    src[0] = x;
                    src[1] = y;
                    matrix = new Matrix();
                    clearStatus();
                }
                case MotionEvent.ACTION_POINTER_DOWN -> {
                    if (transformer.isRecycled()) break;
                    final int pointerCount = event.getPointerCount();
                    if (pointerCount > 4) break;
                    pointCount = Math.max(pointCount, pointerCount);
                    final int index = event.getActionIndex();
                    for (int i = pointCount - 1; i > index; --i) setPointFrom(i, i - 1);
                    final float x = event.getX(index), y = event.getY(index);
                    src[index << 1] = x;
                    src[index << 1 | 1] = y;
                }
                case MotionEvent.ACTION_MOVE -> {
                    if (transformer.isRecycled()) break;
                    for (int i = 0, pointerCount = Math.min(event.getPointerCount(), 4); i < pointerCount; ++i) {
                        final float x = event.getX(i), y = event.getY(i);
                        dst[i << 1] = x;
                        dst[i << 1 | 1] = y;
                    }
                    matrix.setPolyToPoly(src, 0, dst, 0, pointCount);
                    activityMain.canvas.ivSelection.setImageMatrix(matrix);
                    drawSelectionOntoView(false);
                }
                case MotionEvent.ACTION_POINTER_UP -> {
                    if (transformer.isRecycled()) break;
                    final int index = event.getActionIndex();
                    setPointFrom(pointCount, index);
                    for (int i = index; i < pointCount; ++i) setPointFrom(i, i + 1);
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (transformer.isRecycled()) break;
                    matrix = null;
                    activityMain.canvas.ivSelection.setImageMatrix(null);
                    final Matrix bmMatrix = new Matrix();
                    final float[] bmSrc = new float[pointCount << 1], bmDst = new float[pointCount << 1];
                    for (int i = 0; i < pointCount; ++i) {
                        bmSrc[i << 1] = toBitmapX(src[i << 1]) - selection.r.left;
                        bmSrc[i << 1 | 1] = toBitmapY(src[i << 1 | 1]) - selection.r.top;
                        bmDst[i << 1] = toBitmapX(dst[i << 1]) - selection.r.left;
                        bmDst[i << 1 | 1] = toBitmapY(dst[i << 1 | 1]) - selection.r.top;
                    }
                    src = dst = null;
                    bmMatrix.setPolyToPoly(bmSrc, 0, bmDst, 0, pointCount);
                    pointCount = 0;
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

        private void setPointFrom(int index, int fromIndex) {
            src[index << 1] = src[fromIndex << 1];
            src[index << 1 | 1] = src[fromIndex << 1 | 1];
            dst[index << 1] = dst[fromIndex << 1];
            dst[index << 1 | 1] = dst[fromIndex << 1 | 1];
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
            if (!hasSelection) return;
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
        private int width, height;
        private float aspectRatio;
        private float centerX, centerY;
        private float lastPivotX, lastPivotY;
        private float scale;
        private float translationX, translationY;
        private double lastDiagonal;

        private boolean dragMarqueeBound(float viewX, float viewY) {
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

        @Override
        public void onIVTouch(View v, MotionEvent event) {
            if (!hasSelection) return;
            switch (event.getPointerCount()) {
                case 1 -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN -> {
                            if (selection.r.isEmpty()) break;
                            final float x = event.getX(), y = event.getY();
                            if (transformer.isRecycled()) createTransformer();
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
                                hasDraggedBound |= dragMarqueeBound(x, y);
                                activityMain.tvStatus.setText(getString(R.string.state_left_top,
                                        selection.r.left, selection.r.top));
                            }
                        }
                        case MotionEvent.ACTION_MOVE -> {
                            if (transformer.isRecycled()) break;
                            final float x = event.getX(), y = event.getY();
                            if (selection.marqBoundBeingDragged != null) {
                                hasDraggedBound |= dragMarqueeBound(x, y);
                                activityMain.tvStatus.setText(getString(R.string.state_size,
                                        selection.r.width(), selection.r.height()));
                            }
                        }
                        case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (transformer.isRecycled()) break;
                            if (selection.marqBoundBeingDragged != null && hasDraggedBound) {
                                selection.marqBoundBeingDragged = null;
                                hasDraggedBound = false;
                                stretch();
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
                            final double diagonal = Math.hypot(x0 - x1, y0 - y1);
                            final double diagonalRatio = diagonal / lastDiagonal;
                            scale = (float) (scale * diagonalRatio);
                            final float pivotX = (x0 + x1) / 2.0f, pivotY = (y0 + y1) / 2.0f;
                            translationX = (float) (pivotX - (lastPivotX - translationX) * diagonalRatio);
                            translationY = (float) (pivotY - (lastPivotY - translationY) * diagonalRatio);
                            selection.r.left = toUnscaled(translationX);
                            selection.r.top = toUnscaled(translationY);
                            selection.r.right = selection.r.left + (int) (width * scale);
                            selection.r.bottom = selection.r.top + (int) (height * scale);
                            drawSelectionOntoView();
                            activityMain.tvStatus.setText(getString(R.string.state_size,
                                    selection.r.width(), selection.r.height()));
                            lastDiagonal = diagonal;
                            lastPivotX = pivotX;
                            lastPivotY = pivotY;
                        }
                        case MotionEvent.ACTION_POINTER_DOWN -> {
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            width = selection.r.width();
                            height = selection.r.height();
                            lastDiagonal = Math.hypot(x0 - x1, y0 - y1);
                            scale = 1.0f;
                            lastPivotX = (x0 + x1) / 2.0f;
                            lastPivotY = (y0 + y1) / 2.0f;
                            translationX = toScaled(selection.r.left);
                            translationY = toScaled(selection.r.top);
                            clearStatus();
                        }
                        case MotionEvent.ACTION_POINTER_UP -> {
                            stretch();
                            drawBitmapOntoView(true);
                            drawSelectionOntoView();
                        }
                    }
                }
            }
        }

        private void stretch() {
            if (selection.r.left != selection.r.right && selection.r.top != selection.r.bottom) {
                transformer.stretch(activityMain.optionsTransformer.cbFilter.isChecked(), antiAlias);
                selection.r.sort();
            } else {
                final int w = transformer.getWidth(), h = transformer.getHeight();
                selection.r.left = selection.r.centerX() - (w >> 1);
                selection.r.top = selection.r.centerY() - (h >> 1);
                selection.r.right = selection.r.left + w;
                selection.r.bottom = selection.r.top + h;
            }
        }
    };

    /**
     * Callback to call on touch image view with translation transformer
     */
    @SuppressLint({"ClickableViewAccessibility"})
    private final View.OnTouchListener onIVTouchWithTTListener = new OnIVMultiTouchListener() {
        private float dx, dy;
        private Rect lastRect;

        @Override
        public void onIVSingleTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    final float x = event.getX(), y = event.getY();
                    if (hasSelection) {
                        if (transformer.isRecycled()) createTransformer();
                        drawSelectionOntoView(false);
                        if (transformer.undoAction != null)
                            drawBitmapOntoView(transformer.undoAction.rect(), true);
                        activityMain.tvStatus.setText(getString(R.string.state_left_top,
                                selection.r.left, selection.r.top));
                        dx = x - toViewX(selection.r.left);
                        dy = y - toViewY(selection.r.top);
                        lastRect = new Rect(selection.r);
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
                        lastRect.union(selection.r);
                        drawBitmapOntoView(lastRect);
                        lastRect = new Rect(selection.r);
                        activityMain.tvStatus.setText(getString(R.string.state_left_top,
                                selection.r.left, selection.r.top));
                    } else {
                        layer.left = toBitmapXAbs(x - dx);
                        layer.top = toBitmapYAbs(y - dy);
                        drawBitmapOntoView();
                        drawChessboardOntoView();
                        drawGridOntoView();
                        activityMain.tvStatus.setText(getString(R.string.state_left_top,
                                layer.left, layer.top));
                    }
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (hasSelection) {
                        drawBitmapOntoView(lastRect, true);
                        if (!transformer.isRecycled()) drawSelectionOntoView(false);
                        lastRect = null;
                    }
                }
            }
        }

        @Override
        protected void undo() {
            if (!transformer.isRecycled()) drawSelectionOntoView(false);
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
                            if (lastMerged == null || lastMerged.isRecycled()) mergeLayersEntire();
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
                        case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
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
                            lastDiagonal = diagonal;
                            lastPivotX = pivotX;
                            lastPivotY = pivotY;
                        }
                        case MotionEvent.ACTION_POINTER_DOWN -> {
                            final float x0 = event.getX(0), y0 = event.getY(0),
                                    x1 = event.getX(1), y1 = event.getY(1);
                            lastDiagonal = Math.hypot(x0 - x1, y0 - y1);
                            lastPivotX = (x0 + x1) / 2.0f;
                            lastPivotY = (y0 + y1) / 2.0f;
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

    @SuppressLint({"NonConstantResourceId", "ClickableViewAccessibility"})
    private final MaterialButtonToggleGroup.OnButtonCheckedListener onToolButtonCheckedListener = (group, checkedId, isChecked) -> {
        switch (checkedId) {
            case R.id.b_brush -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithBrushListener, activityMain.svOptionsBrush);
                    updateBrush(null);
                    isa.set(bitmap);
                    paint.setAntiAlias(true);
                    activityMain.optionsBrush.tietSoftness.setText(String.valueOf(softness));
                    activityMain.optionsBrush.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                }
            }
            case R.id.b_bucket_fill -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithBucketListener, activityMain.svOptionsBucketFill);
                    isa.set(bitmap);
                    threshold = 0x0;
                }
            }
            case R.id.b_clone_stamp -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithCloneStampListener, activityMain.svOptionsCloneStamp);
                    isa.set(bitmap);
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
                    isa.set(bitmap);
                }
            }
            case R.id.b_eyedropper -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithEyedropperListener, activityMain.svOptionsEyedropper);
                }
            }
            case R.id.b_gradient -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithGradientListener, activityMain.svOptionsGradient);
                    dpPreview.setBitmap(bitmap.getWidth(), bitmap.getHeight());
                }
            }
            case R.id.b_gradient_line -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithGradientLineListener, activityMain.svOptionsGradientLine);
                    dpPreview.setBitmap(bitmap.getWidth(), bitmap.getHeight());
                    activityMain.optionsGradientLine.cbAntiAlias.setChecked(antiAlias);
                    activityMain.optionsGradientLine.tietBlurRadius.setText(String.valueOf(blurRadius));
                    activityMain.optionsGradientLine.tietStrokeWidth.setText(String.valueOf(strokeWidth));
                }
            }
            case R.id.b_magic_eraser -> {
                if (isChecked) {
                    onToolChanged(onIVTouchWithMagicEraserListener, activityMain.svOptionsMagicEraser);
                    updateReference(true);
                    isa.set(bitmap);
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
                    updateReference(true);
                    isa.set(bitmap);
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
                    isa.set(bitmap);
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
                        dpPreview.setBitmap(bitmap.getWidth(), bitmap.getHeight());
                        activityMain.optionsShape.cbAntiAlias.setChecked(antiAlias);
                        activityMain.optionsShape.cbFill.setChecked(isPaintStyleFill());
                        activityMain.optionsShape.tietBlurRadius.setText(String.valueOf(blurRadius));
                        activityMain.optionsShape.tietStrokeWidth.setText(String.valueOf(strokeWidth));
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
                    dpPreview.setBitmap(bitmap.getWidth(), bitmap.getHeight());
                    activityMain.optionsText.cbFill.setChecked(isPaintStyleFill());
                } else {
                    drawTextIntoImage(false);
                }
            }
            case R.id.b_translation, R.id.b_scale, R.id.b_rotation, R.id.b_poly, R.id.b_mesh -> {
                final boolean isTransformerButtonChecked = switch (activityMain.tools.btgTools.getCheckedButtonId()) {
                    case R.id.b_translation, R.id.b_scale, R.id.b_rotation, R.id.b_poly, R.id.b_mesh ->
                            true;
                    default -> false;
                };
                if (isChecked) {
                    onIVTouchWithTransformerListener = switch (checkedId) {
                        case R.id.b_translation -> onIVTouchWithTTListener;
                        case R.id.b_scale -> onIVTouchWithSTListener;
                        case R.id.b_rotation -> onIVTouchWithRTListener;
                        case R.id.b_poly -> onIVTouchWithPTListener;
                        case R.id.b_mesh -> onIVTouchWithMTListener;
                        default -> null;
                    };
                    activityMain.optionsTransformer.cbFilter.setVisibility(checkedId != R.id.b_translation ? View.VISIBLE : View.GONE);
                    activityMain.optionsTransformer.cbLar.setVisibility(checkedId == R.id.b_scale ? View.VISIBLE : View.GONE);
                    activityMain.optionsTransformer.llMesh.setVisibility(checkedId == R.id.b_mesh ? View.VISIBLE : View.GONE);
                }
                if (isTransformerButtonChecked && activityMain.svOptionsTransformer.getVisibility() == View.VISIBLE) {
                    if (isChecked) {
                        activityMain.canvas.flIv.setOnTouchListener(onIVTouchWithTransformerListener);
                        if (hasSelection) {
                            transformer.apply();
                            if (checkedId == R.id.b_mesh) transformer.resetMesh();
                            else transformer.mesh = null;
                            drawSelectionOntoView();
                        }
                    }
                } else {
                    if (isChecked) {
                        onToolChanged(onIVTouchWithTransformerListener, activityMain.svOptionsTransformer);
                        selector.setColor(Color.BLUE);
                        if (hasSelection && checkedId == R.id.b_mesh) createTransformerMesh();
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
        }
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
        layer.canvas = new Canvas(layer.bitmap);
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
        frame.computeLayerTree();
        if (setSelected) {
            selectLayer(position);
            if (ssdLayerList != null) {
                layerList.rvLayerList.post(() -> {
                    if (frame.selectedLayerIndex < frame.layers.size() - 1)
                        frame.layerAdapter.notifyItemChanged(frame.selectedLayerIndex, LayerAdapter.Payload.SELECTED);
                    frame.layerAdapter.notifyItemInserted(position);
                    frame.layerAdapter.notifyItemRangeChanged(position + 1, frame.layers.size() - position);
                    if (position > 0)
                        frame.layerAdapter.notifyItemRangeChanged(0, position, LayerAdapter.Payload.LEVEL);
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
        project.history.init();
        loadTab(project, position);
        project.frameAdapter.setOnItemSelectedListener(onFrameItemSelectedListener, onFrameItemReselectedListener);
        if (bitmap != null) addFrame(project, bitmap, 0, 0, false);
        if (setSelected) selectProject(position);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // Locale
        final String loc = PreferenceManager.getDefaultSharedPreferences(newBase).getString(Settings.KEY_LOC, "d");
        if (!"d".equals(loc)) {
            final int i = loc.indexOf('_');
            final Locale locale = i == -1
                    ? new Locale(loc)
                    : new Locale(loc.substring(0, i), loc.substring(i + 1));
            final Configuration configuration = newBase.getResources().getConfiguration();
            configuration.setLocale(locale);
            Locale.setDefault(locale);
            newBase.createConfigurationContext(configuration);
        }
        super.attachBaseContext(newBase);
    }

    private float calcPaintStrokeRad() {
        return strokeWidth / 2.0f + blurRadius * 2.0f + 1.0f;
    }

    private void calculateBackgroundSizeOnView() {
        final Bitmap background = frame.getBackgroundLayer().bitmap;
        backgroundScaledW = toScaled(background.getWidth());
        backgroundScaledH = toScaled(background.getHeight());
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

    @SuppressLint("NonConstantResourceId")
    private boolean checkRefNecessity() {
        return switch (activityMain.tools.btgTools.getCheckedButtonId()) {
            case R.id.b_brush -> brush.tipShape == BrushTool.TipShape.REF;
            case R.id.b_magic_eraser, R.id.b_magic_paint -> true;
            default -> false;
        };
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
            project.history.optimize();
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
                if (position > 0)
                    frame.layerAdapter.notifyItemRangeChanged(0, position, LayerAdapter.Payload.LEVEL);
            });
            project.history.optimize();
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
        project.history.optimize();
    }

    private Rect computeRectBoundsDrawn(Rect rect, Paint paint) {
        final Rect bounds = new Rect(rect);
        final int outset;
        if (paint == this.paint) {
            outset = (int) Math.ceil(calcPaintStrokeRad());
        } else if (paint == eraser) {
            outset = (int) Math.ceil(eraserStrokeHalfWidth + eraserBlurDiameter + 1.0f);
        } else {
            return null;
        }
        bounds.inset(-outset, -outset);
        return bounds;
    }

    private Rect computeSelectionBoundsDrawnBy(Paint paint) {
        if (!hasSelection) return null;
        return computeRectBoundsDrawn(selection.r, paint);
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

    private void createEditPreview(boolean cacheBitmap) {
        createEditPreview(cacheBitmap, false, false);
    }

    private void createEditPreview(boolean cachePixels, boolean onlyForVisible) {
        createEditPreview(false, cachePixels, onlyForVisible);
    }

    private void createEditPreview(boolean cacheBitmap, boolean cachePixels, boolean onlyForVisible) {
        if (editPreview != null) editPreview.recycle();
        if (!hasSelection) selectAll();
        editPreview = new EditPreview(bitmap, selection.r, cacheBitmap, cachePixels,
                onlyForVisible ? getVisibleSubset() : null);
    }

    private void createTransformer() {
        final Bitmap bm = BitmapUtils.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height());
        createTransformer(bm);
        final Rect r = computeSelectionBoundsDrawnBy(eraser);
        r.intersect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        transformer.undoAction = new History.Action(null, r, Bitmap.createBitmap(bitmap, r.left, r.top, r.width(), r.height()));
        canvas.drawRect(selection.r.left, selection.r.top, selection.r.right, selection.r.bottom, eraser);
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
        if (layer == this.layer) recycleTransformer();
        layer.bitmap.recycle();
        frame.layers.remove(position);
    }

    private void deleteProject(int position) {
        activityMain.tlProjectList.removeOnTabSelectedListener(onProjTabSelectedListener);
        activityMain.tlProjectList.removeTabAt(position);
        activityMain.tlProjectList.addOnTabSelectedListener(onProjTabSelectedListener);
        final Project project = projects.get(position);
        projects.remove(position);
        project.history.clear();
        for (int i = project.frames.size() - 1; i >= 0; --i) {
            deleteFrame(project, i);
        }
    }

    private void drawAfterTransformingView(boolean doNotMerge) {
        if (doNotMerge) drawBitmapLastOntoView(false);
        else drawBitmapOntoView(true, false);
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

    private void drawBitmapOntoCanvas(Canvas canvas, Bitmap bitmap, float translX, float translY, Rect vs) {
        if (vs.isEmpty()) return;
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
        if (wait) BitmapUtils.recycle(lastMerged);
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

    private void drawBitmapOntoView(final int left, final int top, final int right, final int bottom) {
        drawBitmapOntoView(bitmap, left, top, right, bottom, false);
    }

    private void drawBitmapOntoView(final Bitmap bitmap,
                                    final int left, final int top, final int right, final int bottom,
                                    final boolean wait) {
        runOrStart(() -> drawBitmapSubsetOntoView(bitmap,
                layer.left + left, layer.top + top, layer.left + right, layer.top + bottom), wait);
        if (wait) BitmapUtils.recycle(lastMerged);
    }

    private void drawBitmapOntoView(final int x0, final int y0, final int x1, final int y1, final float radius) {
        final boolean x = x0 <= x1, y = y0 <= y1;
        final int rad = (int) Math.ceil(radius + 1.0f);
        final int left = layer.left + (x ? x0 : x1) - rad, top = layer.top + (y ? y0 : y1) - rad,
                right = layer.left + (x ? x1 : x0) + rad + 1, bottom = layer.top + (y ? y1 : y0) + rad + 1;
        runOrStart(() -> drawBitmapSubsetOntoView(bitmap, left, top, right, bottom));
    }

    private void drawBitmapStateOntoView(final int x0, final int y0, final int x1, final int y1, final float radius) {
        drawBitmapOntoView(x0, y0, x1, y1, radius);
        isa.unionBounds(x0, y0, x1, y1, radius);
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
        final Rect vs = getVisibleSubsetOfBackground();

        runOnUiThread(() -> {
            if (lastMerged == null || lastMerged.isRecycled()) return;
            eraseBitmap(viewBitmap);
            if (!vs.isEmpty())
                drawBitmapOntoCanvas(viewCanvas, lastMerged, translationX, translationY, vs);
            activityMain.canvas.iv.invalidate();
        });
    }

    private void drawBitmapSubsetOntoView(final Bitmap bitmap,
                                          int left, int top, int right, int bottom) {
        final Bitmap background = frame.getBackgroundLayer().bitmap;
        left = Math.max(left, 0);
        top = Math.max(top, 0);
        right = Math.min(right, background.getWidth());
        bottom = Math.min(bottom, background.getHeight());
        if (left >= right || top >= bottom) {
            return;
        }
        final Rect vs = getVisibleSubsetOfBackground();
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
        final Rect vs = getVisibleSubsetOfBackground();
        if (vs.isEmpty()) {
            runOnUiThread(() -> {
                eraseBitmap(viewBitmap);
                activityMain.canvas.iv.invalidate();
            });
            return;
        }

        final Bitmap merged = Layers.mergeLayers(frame.layerTree, vs, layer, bitmap, getCurrentFloatingLayer());
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

        if (project.frames.size() > 1 && project.onionSkins > 0) onionSkins:{
            final Rect vs = getVisibleSubsetOfBackground();
            if (vs.isEmpty()) break onionSkins;
            final RectF svs = getVisibleSubsetOfView(vs, translationX, translationY);
            for (int i = 1; i <= project.onionSkins; ++i) {
                onionSkinPaint.setAlpha(Math.round((float) Math.pow(2.0, -i) * 0xFF));
                if (project.selectedFrameIndex >= i) {
                    chessboardCanvas.drawBitmap(project.frames.get(project.selectedFrameIndex - i).getThumbnail(),
                            vs, svs, onionSkinPaint);
                }
                if (project.selectedFrameIndex < project.frames.size() - i) {
                    chessboardCanvas.drawBitmap(project.frames.get(project.selectedFrameIndex + i).getThumbnail(),
                            vs, svs, onionSkinPaint);
                }
            }
        }

        activityMain.canvas.ivChessboard.invalidate();
        drawRuler();
    }

    private void drawCrossOntoView(float x, float y) {
        drawCrossOntoView(x, y, true);
    }

    private void drawCrossOntoView(float x, float y, boolean isFirst) {
        if (isFirst) eraseBitmap(previewBitmap);
        final float viewX = toViewX(x), viewY = toViewY(y);
        previewCanvas.drawLine(viewX - 50.0f, viewY, viewX + 50.0f, viewY, selector);
        previewCanvas.drawLine(viewX, viewY - 50.0f, viewX, viewY + 50.0f, selector);
        activityMain.canvas.ivPreview.invalidate();
    }

    private void drawEditPreviewIntoImage(boolean entire) {
        if (entire) {
            canvas.drawBitmap(editPreview.getEntire(), 0.0f, 0.0f, PAINT_BITMAP);
        } else {
            canvas.drawBitmap(editPreview.getEntire(), editPreview.getRect(), editPreview.getRect(), PAINT_BITMAP);
        }
        recycleEditPreview();
        drawBitmapOntoView(true);
    }

    private void drawEditPreviewOntoView() {
        if (editPreview.committed()) {
            onEditPreviewCommit(false);
            return;
        }
        drawBitmapOntoView(editPreview.getEntire(), selection.r, true);
    }

    private void drawFloatingLayersIntoImage() {
        drawTransformerIntoImage();
        drawTextIntoImage();
    }

    private void drawGridOntoView() {
        eraseBitmap(gridBitmap);

        {
            final float left = toViewX(0), top = toViewY(0);
            final float l = left >= 0.0f ? left : left % scale, t = top >= 0.0f ? top : top % scale,
                    r = Math.min(left + toScaled(bitmap.getWidth()), viewWidth), b = Math.min(top + toScaled(bitmap.getHeight()), viewHeight);
            if (isScaledMuch()) {
                for (float x = l; x <= r; x += scale)
                    gridCanvas.drawLine(x, t, x, b, PAINT_GRID);
                for (float y = t; y <= b; y += scale)
                    gridCanvas.drawLine(l, y, r, y, PAINT_GRID);
            }
            gridCanvas.drawLine(l, t, l - 100.0f, t, PAINT_IMAGE_BOUND);
            gridCanvas.drawLine(r, t, r + 100.0f, t, PAINT_IMAGE_BOUND);
            gridCanvas.drawLine(r, t - 100.0f, r, t, PAINT_IMAGE_BOUND);
            gridCanvas.drawLine(r, b, r, b + 100.0f, PAINT_IMAGE_BOUND);
            gridCanvas.drawLine(r + 100.0f, b, r, b, PAINT_IMAGE_BOUND);
            gridCanvas.drawLine(l, b, l - 100.0f, b, PAINT_IMAGE_BOUND);
            gridCanvas.drawLine(l, b + 100.0f, l, b, PAINT_IMAGE_BOUND);
            gridCanvas.drawLine(l, t, l, t - 100.0f, PAINT_IMAGE_BOUND);
        }

        final CellGrid cellGrid = project.cellGrid;
        if (cellGrid.enabled) {
            final Bitmap background = frame.getBackgroundLayer().bitmap;
            final float r = Math.min(translationX + toScaled(background.getWidth()), viewWidth), b = Math.min(translationY + toScaled(background.getHeight()), viewHeight);
            if (cellGrid.sizeX > 1) {
                final float t = translationY >= 0.0f ? translationY : translationY % scale;
                final float scaledSizeX = toScaled(cellGrid.sizeX), scaledSpacingX = toScaled(cellGrid.spacingX);
                float x = (translationX >= 0.0f ? translationX : translationX % (scaledSizeX + scaledSpacingX))
                        + toScaled(cellGrid.offsetX % (cellGrid.sizeX + cellGrid.spacingX));
                if (x < translationX) x += scaledSizeX + scaledSpacingX;
                if (cellGrid.spacingX <= 0) {
                    do gridCanvas.drawLine(x, t, x, b, PAINT_CELL_GRID);
                    while ((x += scaledSizeX) <= r);
                } else {
                    do {
                        gridCanvas.drawLine(x, t, x, b, PAINT_CELL_GRID);
                        if ((x += scaledSizeX) > r) break;
                        gridCanvas.drawLine(x, t, x, b, PAINT_CELL_GRID);
                        if ((x += scaledSpacingX) > r) break;
                    } while (true);
                }
            }
            if (cellGrid.sizeY > 1) {
                final float l = translationX >= 0.0f ? translationX : translationX % scale;
                final float scaledSizeY = toScaled(cellGrid.sizeY), scaledSpacingY = toScaled(cellGrid.spacingY);
                float y = (translationY >= 0.0f ? translationY : translationY % (scaledSizeY + scaledSpacingY))
                        + toScaled(cellGrid.offsetY % (cellGrid.sizeY + cellGrid.spacingY));
                if (y < translationY) y += scaledSizeY + scaledSpacingY;
                if (cellGrid.spacingY <= 0) {
                    do gridCanvas.drawLine(l, y, r, y, PAINT_CELL_GRID);
                    while ((y += scaledSizeY) <= b);
                } else {
                    do {
                        gridCanvas.drawLine(l, y, r, y, PAINT_CELL_GRID);
                        if ((y += scaledSizeY) > b) break;
                        gridCanvas.drawLine(l, y, r, y, PAINT_CELL_GRID);
                        if ((y += scaledSpacingY) > b) break;
                    } while (true);
                }
            }
        }

        for (final Guide guide : project.guides) {
            if (guide.orientation) {
                final float x = toViewXRel(guide.position);
                gridCanvas.drawLine(x, 0.0f, x, viewHeight, PAINT_GUIDE);
            } else {
                final float y = toViewYRel(guide.position);
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
            final float left = Math.max(0.0f, toViewX(selection.r.left)), top = Math.max(0.0f, toViewY(selection.r.top)),
                    right = Math.min(viewWidth, toViewX(selection.r.right)), bottom = Math.min(viewHeight, toViewY(selection.r.bottom));
            selectionCanvas.drawRect(left, top, right, bottom, selector);

            if (showMargins) {
                SelectionTool.drawMargins(selectionCanvas,
                        left, top, right, bottom,
                        Math.max(0.0f, translationX), Math.max(0.0f, translationY),
                        Math.min(viewWidth, translationX + backgroundScaledW), Math.min(viewHeight, translationY + backgroundScaledH),
                        viewWidth, viewHeight,
                        String.valueOf(layer.left + selection.r.left), String.valueOf(layer.top + selection.r.top),
                        String.valueOf(layer.left + bitmap.getWidth() - selection.r.right), String.valueOf(layer.top + bitmap.getHeight() - selection.r.bottom),
                        marginPaint);
            }

            transformer.drawMesh(this, selectionCanvas, selector);
        }
        activityMain.canvas.ivSelection.invalidate();
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

    private void drawTextIntoImage() {
        drawTextIntoImage(true);
    }

    private void drawTextIntoImage(boolean hideOptions) {
        if (!isEditingText) return;
        isEditingText = false;
        if (textActionMode != null) {
            textActionMode.finish();
            textActionMode = null;
        }
        saveStepBackToHistory(text.rect());
        canvas.drawText(text.s, text.x, text.y, paint);
        dpPreview.erase();
        drawBitmapOntoView(text.rect(), true);
        eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
        hideSoftInputFromWindow();
        if (hideOptions) activityMain.llOptionsText.setVisibility(View.INVISIBLE);
        saveStepForwardToHistory();
    }

    private void drawTextOntoView(Rect rect) {
        drawTextOntoView(rect, false);
    }

    private void drawTextOntoView(boolean stopped) {
        drawTextOntoView(text.measure(paint, calcPaintStrokeRad()), stopped);
    }

    private void drawTextOntoView(Rect rect, boolean stopped) {
        if (!isEditingText) return;
        dpPreview.erase();
        dpPreview.getCanvas().drawText(text.s, text.x, text.y, paint);
        drawBitmapOntoView(rect, stopped);
        drawTextGuideOntoView();
    }

    private void drawTransformerIntoImage() {
        if (transformer.isRecycled() || !hasSelection) return;
        if (transformer.undoAction != null) {
            canvas.drawBitmap(transformer.undoAction.bm(), null, transformer.undoAction.rect(), PAINT_BITMAP);
            final Rect bounds = new Rect(selection.r);
            bounds.union(transformer.undoAction.rect());
            saveStepBackToHistory(bounds);
            canvas.drawRect(transformer.getOrigRect(), eraser);
        } else {
            saveStepBackToHistory(selection.r);
        }
        canvas.drawBitmap(transformer.getBitmap(), selection.r.left, selection.r.top, PAINT_SRC_OVER);
        recycleTransformer();
        drawBitmapOntoView(selection.r, true);
        optimizeSelection();
        if (transformer.mesh != null) transformer.resetMesh();
        drawSelectionOntoView();
        saveStepForwardToHistory();
        clearStatus();
    }

    private static void eraseBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
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
        if (!transformer.isRecycled()) {
            return transformer;
        } else if (!dpPreview.isRecycled()) {
            return dpPreview;
        }
        return null;
    }

    private Rect getVisibleSubset() {
        return getVisibleSubset(toViewX(0), toViewY(0), bitmap.getWidth(), bitmap.getHeight());
    }

    private Rect getVisibleSubset(float translX, float translY, int width, int height) {
        final int left = translX >= 0.0f ? 0 : toUnscaled(-translX);
        final int top = translY >= 0.0f ? 0 : toUnscaled(-translY);
        final int right = Math.min(toUnscaled(translX + backgroundScaledW <= viewWidth ? backgroundScaledW : viewWidth - translX) + 1, width);
        final int bottom = Math.min(toUnscaled(translY + backgroundScaledH <= viewHeight ? backgroundScaledH : viewHeight - translY) + 1, height);
        return new Rect(left, top, right, bottom);
    }

    private Rect getVisibleSubsetOfBackground() {
        return getVisibleSubset(translationX, translationY,
                frame.getBackgroundLayer().bitmap.getWidth(), frame.getBackgroundLayer().bitmap.getHeight());
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
            new ColorPickerDialog(this, R.string.color,
                    (oldColor, newColor) -> {
                        final int index = palette.indexOf(oldColor);
                        if (newColor != null) {
                            palette.set(index, newColor);
                            colorAdapter.notifyItemChanged(index);
                        } else {
                            palette.remove(index);
                            colorAdapter.notifyItemRemoved(index);
                        }
                        Settings.INST.savePalette(palette);
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
        activityMain.optionsBrush.tietSoftness.setText(String.valueOf(softness));
        activityMain.optionsEraser.tietBlurRadius.setText(String.valueOf(0.0f));
        activityMain.optionsEraser.tietStrokeWidth.setText(String.valueOf(eraser.getStrokeWidth()));
        activityMain.optionsPencil.tietBlurRadius.setText(String.valueOf(0.0f));
        activityMain.optionsPencil.tietStrokeWidth.setText(String.valueOf(paint.getStrokeWidth()));
        activityMain.optionsText.tietTextSize.setText(String.valueOf(paint.getTextSize()));
        activityMain.optionsTransformer.tietMeshWidth.setText(String.valueOf(2));
        activityMain.optionsTransformer.tietMeshHeight.setText(String.valueOf(2));

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

        {
            final ViewGroup.LayoutParams lpNeg = activityMain.vBlockerNeg.getLayoutParams(), lpPos = activityMain.vBlockerPos.getLayoutParams();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                lpNeg.width = activityMain.canvas.getRoot().getLeft();
                lpPos.width = activityMain.tlProjectList.getHeight();
            } else {
                final View canvasView = activityMain.canvas.getRoot();
                lpNeg.height = canvasView.getTop();
                lpPos.height = activityMain.getRoot().getHeight() - lpNeg.height - canvasView.getHeight();
            }
            activityMain.vBlockerNeg.setLayoutParams(lpNeg);
            activityMain.vBlockerPos.setLayoutParams(lpPos);
        }

        if (projects.isEmpty()) {
            final Uri uri = getIntent().getData();
            if (uri != null) {
                @StringRes final int r = openFile(uri);
                if (r != 0) {
                    if (projects.isEmpty()) addDefaultTab();
                    Snackbar.make(vContent, r, Snackbar.LENGTH_LONG).show();
                }
            } else {
                addDefaultTab();
            }
        } else {
            for (int i = 0; i < projects.size(); ++i) {
                final Project p = projects.get(i);
                loadTab(p, i);
                p.frameAdapter.setOnItemSelectedListener(onFrameItemSelectedListener, onFrameItemReselectedListener);
                for (final Frame f : p.frames) {
                    f.layerAdapter.setOnItemSelectedListener(onLayerItemSelectedListener, onLayerItemReselectedListener);
                    f.layerAdapter.setOnLayerVisibleChangedListener((buttonView, isChecked) -> drawBitmapOntoView(true));
                }
            }
            selectProject(0);
        }

        activityMain.tools.btgTools.addOnButtonCheckedListener(onToolButtonCheckedListener);
        if (activityMain.tools.btgTools.getCheckedButtonId() != R.id.b_pencil) {
            activityMain.tools.btgTools.check(R.id.b_pencil);
        } else {
            onToolButtonCheckedListener.onButtonChecked(null, R.id.b_pencil, true);
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
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if ("s".equals(preferences.getString(Settings.KEY_THEME, "d"))) {
            setTheme(R.style.Theme_IconEditor_SquareCorner_NoActionBar);
        }
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) FileUtils.prepareToLogExceptionMsg();

        // Preferences
        Settings.INST.mainActivity = this;
        Settings.INST.update(preferences);

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

        activityMain.btgZoom.addOnButtonCheckedListener(onZoomToolButtonCheckedListener);
        activityMain.optionsBrush.bTipShape.setOnClickListener(onBrushTipShapeButtonClickListener);
        activityMain.optionsBucketFill.bTolerance.setOnClickListener(onToleranceButtonClickListener);
        activityMain.optionsCloneStamp.bSrc.setOnClickListener(onCloneStampSrcButtonClickListener);
        activityMain.optionsMagicPaint.bTolerance.setOnClickListener(onToleranceButtonClickListener);
        activityMain.bPaletteAdd.setOnClickListener(onAddPaletteColorButtonClickListener);
        activityMain.bPaletteAdd.setOnLongClickListener(onAddPaletteColorButtonLongClickListener);
        activityMain.optionsText.bDraw.setOnClickListener(v -> drawTextIntoImage());
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
        activityMain.canvas.ivRulerH.setOnTouchListener(onRulerHTouchListener);
        activityMain.canvas.ivRulerV.setOnTouchListener(onRulerVTouchListener);
        activityMain.rvPalette.setItemAnimator(new DefaultItemAnimator());
        activityMain.optionsGradient.sColors.setOnItemSelectedListener(onGradientColorsSpinnerItemSelectedListener);
        activityMain.optionsGradient.sType.setOnItemSelectedListener(onGradientTypeSpinnerItemSelectedListener);
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
        activityMain.optionsText.tietText.addTextChangedListener(onTextETTextChangedListener);
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
            activityMain.tools.bBucketFill.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bCircle.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bEraser.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bEyedropper.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bGradient.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bGradientLine.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bLine.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bMagicEraser.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bMagicPaint.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bMesh.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bOval.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bPencil.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bPoly.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bRect.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bRotation.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bRuler.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bScale.setOnLongClickListener(onToolLongClickListener);
            activityMain.tools.bTranslation.setOnLongClickListener(onToolLongClickListener);
        }

        frameList.rvFrameList.setItemAnimator(new DefaultItemAnimator());
        MovableItemAdapter.createItemMoveHelper(onFrameItemMoveListener).attachToRecyclerView(frameList.rvFrameList);

        layerList.rvLayerList.setItemAnimator(new DefaultItemAnimator());
        MovableItemAdapter.createItemMoveHelper(onLayerItemMoveListener).attachToRecyclerView(layerList.rvLayerList);

        final Resources res = getResources();

        chessboard = BitmapFactory.decodeResource(res, R.mipmap.chessboard);
        chessboardPaint.setShader(new BitmapShader(chessboard, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        projects = viewModel.getProjects();

        palette = Settings.INST.palette();
        colorAdapter = new ColorAdapter(palette);
        initColorAdapter();
        activityMain.rvPalette.setAdapter(colorAdapter);
        MovableItemAdapter
                .createItemMoveHelper((fromPos, toPos) -> Settings.INST.savePalette(palette))
                .attachToRecyclerView(activityMain.rvPalette);

        brush.setBrush(BitmapUtils.drawableToBitmap(this, R.drawable.brush_tip_shape));

        if (isLandscape) {
            final var tl = activityMain.tlProjectList;
            OneShotPreDrawListener.add(tl, () -> {
                final int width = activityMain.getRoot().getMeasuredHeight(), height = activityMain.tlProjectList.getMeasuredHeight();
                final ViewGroup.LayoutParams lp = tl.getLayoutParams();
                lp.width = width;
                tl.setLayoutParams(lp);
                final boolean ltr = getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
                final float radius = height >> 1;
                tl.setPivotX(ltr ? width - radius : radius);
                tl.setPivotY(radius);
                tl.setRotation(ltr ? 90.0f : -90.0f);
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
        Settings.INST.update(Settings.KEY_FL);
        return true;
    }

    @Override
    protected void onDestroy() {
        recycleAllBitmaps();
        super.onDestroy();
    }

    private void onExecuteHistoricalCommand(History.Action action) {
        if (action.layer() == layer) {
            if (!isa.isRecycled()) {
                if (action.rect() != null) isa.draw(action.bm(), action.rect());
                else if (action.bm().getWidth() == bitmap.getWidth() && action.bm().getHeight() == bitmap.getHeight())
                    isa.erase(action.bm(), null);
                else isa.set(action.bm());
            }
            if (!dpPreview.isRecycled()) {
                if (action.rect() != null || action.bm().getWidth() == bitmap.getWidth() && action.bm().getHeight() == bitmap.getHeight())
                    dpPreview.erase();
                else dpPreview.setBitmap(action.bm().getWidth(), action.bm().getHeight());
            }
            bitmap = action.layer().bitmap;
            canvas = action.layer().canvas;

            miHasAlpha.setChecked(bitmap.hasAlpha());
            recycleTransformer();
            if (transformer.mesh != null) {
                transformer.resetMesh();
            } else if (magEr.b != null && magEr.f != null) {
                drawCrossOntoView(magEr.b.x, magEr.b.y, true);
                drawCrossOntoView(magEr.f.x, magEr.f.y, false);
            } else if (cloneStampSrc != null) {
                drawCrossOntoView(cloneStampSrc.x, cloneStampSrc.y);
            } else {
                eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
            }
            optimizeSelection();
            isShapeStopped = true;
            selection.marqBoundBeingDragged = null;
        }

        if (action.layer() == frame.getBackgroundLayer()) {
            calculateBackgroundSizeOnView();
            drawChessboardOntoView();
        } else if (project.onionSkins > 0) {
            drawChessboardOntoView();
        }
        for (int i = 1; i <= project.onionSkins; ++i) {
            if (project.selectedFrameIndex >= i)
                project.frames.get(project.selectedFrameIndex - i).updateThumbnail();
            if (project.selectedFrameIndex < project.frames.size() - i)
                project.frames.get(project.selectedFrameIndex + i).updateThumbnail();
        }
        drawBitmapOntoView(true, true);
        drawGridOntoView();
        drawSelectionOntoView();
        clearStatus();
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
                        .setNeutralButton(R.string.apply_to_each_frame, number -> {
                            project.frames.forEach(f -> f.delay = number);
                            frameList.rvFrameList.post(() ->
                                    project.frameAdapter.notifyItemRangeChanged(0, project.frames.size(), FrameAdapter.Payload.DELAY));
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
            case R.id.i_frame_onion_skins -> {
                new EditNumberDialog(this)
                        .setTitle(R.string.onion_skins)
                        .setOnApplyListener(number -> {
                            project.onionSkins = Math.min(number, 8);
                            drawChessboardOntoView();
                        })
                        .show(project.onionSkins);
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
                l.canvas = new Canvas(l.bitmap);
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
            case R.id.i_layer_alpha -> {
                if (ssdLayerList != null) ssdLayerList.dismiss();
                new SliderDialog(this)
                        .setIcon(item.getIcon()).setTitle(R.string.alpha)
                        .setValueFrom(0x00).setValueTo(0xFF).setValue(layer.paint.getAlpha())
                        .setStepSize(1.0f)
                        .setOnChangeListener(onLayerAlphaSliderChangeListener)
                        .setOnActionListener(this::clearStatus)
                        .show();
                activityMain.tvStatus.setText(String.format(
                        getString(R.string.state_alpha, Settings.INST.colorIntCompFormat()),
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
            case R.id.i_layer_clipped -> {
                layer.clipped = !layer.clipped;
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_create_clipping_mask -> {
                switch (layer.paint.getBlendMode()) {
                    case SRC_OVER, SRC_ATOP -> layer.paint.setBlendMode(BlendMode.SRC_ATOP);
                    default -> layer.clipped = true;
                }
                Layers.levelDown(frame.layers, frame.selectedLayerIndex);
                frame.computeLayerTree();
                layerList.rvLayerList.post(() -> {
                    frame.layerAdapter.notifyItemRangeChanged(0, frame.layers.size(), LayerAdapter.Payload.LEVEL);
                });
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_delete -> closeLayer(frame.selectedLayerIndex);
            case R.id.i_layer_delete_invisible -> {
                for (int i = frame.layers.size() - 1; i >= 0; --i) {
                    final Layer l = frame.layers.get(i);
                    if (!l.visible && l != layer) {
                        deleteLayer(i);
                        if (i < frame.selectedLayerIndex) --frame.selectedLayerIndex;
                    }
                }
                if (!layer.visible) {
                    layerList.rvLayerList.post(frame.layerAdapter::notifyDataSetChanged);
                    closeLayer(frame.selectedLayerIndex);
                } else {
                    frame.computeLayerTree();
                    selectLayer(frame.selectedLayerIndex);
                    layerList.rvLayerList.post(frame.layerAdapter::notifyDataSetChanged);
                    project.history.optimize();
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
                new ColorBalanceDialog(this, layer.lighting,
                        (lighting, stopped) -> drawBitmapOntoView(stopped))
                        .setOkButton()
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
                new ColorMatrixManager(this, layer.colorMatrix.getArray(),
                        matrix -> drawBitmapOntoView(true))
                        .setOkButton()
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
                        .setOkButton()
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
                        .setOnCurvesChangedListener((curves, stopped) -> drawBitmapOntoView(stopped))
                        .setOkButton()
                        .show();
                clearStatus();
            }
            case R.id.i_layer_filter_hs -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.deltaHs == null) layer.initDeltaHs();
                    layer.filter = Layer.Filter.HS;
                    drawBitmapOntoView(true);
                }
                new HsDialog(this, layer.deltaHs,
                        onLayerHsChangedListener)
                        .setOkButton()
                        .show();
                showStateOfHs(layer.deltaHs);
            }
            case R.id.i_layer_filter_levels -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.lighting == null) layer.initLighting();
                    layer.resetLighting();
                    layer.filter = Layer.Filter.LEVELS;
                    drawBitmapOntoView(true);
                }
                new LevelsDialog(this, onLayerLevelsChangedListener)
                        .set(layer.lighting[0], layer.lighting[1])
                        .drawHistogram(BitmapUtils.getPixels(bitmap))
                        .setOkButton()
                        .show();
                clearStatus();
            }
            case R.id.i_layer_filter_lighting -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    if (layer.lighting == null) layer.initLighting();
                    layer.filter = Layer.Filter.LIGHTING;
                    drawBitmapOntoView(true);
                }
                new LightingDialog(this, layer.lighting,
                        (lighting, stopped) -> drawBitmapOntoView(true))
                        .setOkButton()
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
                        .setOkButton()
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
                        .setValueFrom(-1.0f).setValueTo(10.0f).setValue(sat)
                        .setOnChangeListener(onLayerSaturationSliderChangeListener)
                        .setOkButton()
                        .show();
                activityMain.tvStatus.setText(getString(R.string.state_saturation, sat));
            }
            case R.id.i_layer_filter_selected_by_cr -> {
                ssdLayerList.dismiss();
                if (!item.isChecked()) {
                    layer.filter = Layer.Filter.SELECTED_BY_CR;
                    drawBitmapOntoView(true);
                }
                new ColorRangeDialog(this, layer.colorRange,
                        onLayerColorRangeChangedListener)
                        .setOkButton()
                        .show();
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
                        .setOkButton()
                        .show();
                activityMain.tvStatus.setText(getString(R.string.state_threshold, (int) threshold));
            }
            case R.id.i_layer_group -> {
                if (!layer.visible) break;
                final int pos = frame.group();
                final Bitmap bg = frame.getBackgroundLayer().bitmap;
                final Bitmap bm = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), bg.getConfig(), bg.hasAlpha(), bg.getColorSpace());
                addLayer(project, frame, bm, pos, frame.layers.get(pos - 1).getLevel() - 1, getString(R.string.group), false);
                frame.computeLayerTree();
                layerList.rvLayerList.post(() -> {
                    frame.layerAdapter.notifyItemInserted(pos);
                    frame.layerAdapter.notifyItemRangeChanged(pos + 1, frame.layers.size() - pos - 1);
                    if (pos > 0)
                        frame.layerAdapter.notifyItemRangeChanged(0, pos, LayerAdapter.Payload.LEVEL);
                });
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_level_down -> {
                layer.levelDown();
                frame.computeLayerTree();
                layerList.rvLayerList.post(() -> {
                    frame.layerAdapter.notifyItemRangeChanged(0, frame.layers.size(), LayerAdapter.Payload.LEVEL);
                });
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_level_up -> {
                layer.levelUp();
                frame.computeLayerTree();
                layerList.rvLayerList.post(() -> {
                    frame.layerAdapter.notifyItemRangeChanged(0, frame.layers.size(), LayerAdapter.Payload.LEVEL);
                });
                drawBitmapOntoView(true);
            }
            case R.id.i_layer_merge_alpha -> {
                final int pos = frame.selectedLayerIndex, posBelow = pos + 1;
                if (posBelow >= frame.layers.size()) break;

                drawFloatingLayersIntoImage();
                saveStepBackToHistory(null);
                final Layer layerBelow = frame.layers.get(posBelow);
                BitmapUtils.mergeAlpha(layer.bitmap, layerBelow.bitmap);
                layer.visible = false;
                selectLayer(posBelow);
                saveStepForwardToHistory();
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
                project.history.addUndoAction(layerBelow, layerBelow.bitmap, null);
                Layers.mergeLayers(layer, layerBelow);
                deleteLayer(pos);
                frame.computeLayerTree();
                selectLayer(pos);
                layerList.rvLayerList.post(() -> {
                    frame.layerAdapter.notifyItemRemoved(pos);
                    frame.layerAdapter.notifyItemRangeChanged(pos, frame.layers.size() - pos);
                    if (pos > 0)
                        frame.layerAdapter.notifyItemRangeChanged(0, pos, LayerAdapter.Payload.LEVEL);
                });
                saveStepForwardToHistory();
                project.history.optimize();
            }
            case R.id.i_layer_merge_visible -> {
                drawFloatingLayersIntoImage();
                final Bitmap bm = Layers.mergeLayers(frame.layerTree);
                for (final Layer l : frame.layers) l.visible = false;
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
            case R.id.i_layer_reference -> {
                final boolean checked = !item.isChecked();
                layer.reference = checked;
                item.setChecked(checked);
                updateReference();
            }
            case R.id.i_layer_rename -> {
                final AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                        .setIcon(R.drawable.ic_drive_file_rename_outline)
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
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final Uri uri = intent.getData();
        if (uri == null) return;
        @StringRes final int r = openFile(uri);
        if (r != 0) Snackbar.make(vContent, r, Snackbar.LENGTH_LONG).show();
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            default -> {
                return false;
            }
            case R.id.i_cell_grid ->
                    new CellGridManager(this, project.cellGrid, onCellGridApplyListener).show();
            case R.id.i_copy -> {
                if (transformer.isRecycled()) {
                    if (clipboard != null) clipboard.recycle();
                    clipboard = hasSelection
                            ? BitmapUtils.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height())
                            : BitmapUtils.createBitmap(bitmap);
                } else {
                    clipboard = BitmapUtils.createBitmap(transformer.getBitmap());
                }
            }
            case R.id.i_crop -> {
                if (!hasSelection) break;
                drawFloatingLayersIntoImage();
                final int width = selection.r.width(), height = selection.r.height();
                final int offsetX = selection.r.left, offsetY = selection.r.top;
                if (layer == frame.getBackgroundLayer()) {
                    translationX += toScaled(offsetX);
                    translationY += toScaled(offsetY);
                    for (final Frame f : project.frames) {
                        final Layer bl = f.getBackgroundLayer();
                        final Bitmap bm = Bitmap.createBitmap(bl.bitmap, offsetX, offsetY, width, height);
                        for (int i = 0; i < frame.layers.size() - 1; ++i)
                            frame.layers.get(i).moveBy(-offsetX, -offsetY);
                        resizeImage(bl, width, height, ImageSizeManager.ScaleType.CROP, bm);
                        bm.recycle();
                    }
                } else {
                    layer.moveBy(offsetX, offsetY);
                    final Bitmap bm = Bitmap.createBitmap(layer.bitmap, offsetX, offsetY, width, height);
                    resizeImage(layer, width, height, ImageSizeManager.ScaleType.CROP, bm);
                    bm.recycle();
                }
                drawBitmapOntoView(true, true);
            }
            case R.id.i_cut -> {
                final Rect bounds = computeSelectionBoundsDrawnBy(eraser);
                if (transformer.isRecycled()) {
                    if (clipboard != null) clipboard.recycle();
                    if (hasSelection) {
                        clipboard = BitmapUtils.createBitmap(bitmap,
                                selection.r.left, selection.r.top, selection.r.width(), selection.r.height());
                        saveStepBackToHistory(bounds);
                        canvas.drawRect(selection.r, eraser);
                    } else {
                        clipboard = BitmapUtils.createBitmap(bitmap);
                        saveStepBackToHistory(null);
                        canvas.drawColor(eraser.getColorLong(), BlendMode.SRC);
                    }
                    saveStepForwardToHistory();
                } else {
                    clipboard = BitmapUtils.createBitmap(transformer.getBitmap());
                    recycleTransformer();
                }
                if (hasSelection) drawBitmapOntoView(bounds, true);
                else drawBitmapOntoView(true);
            }
            case R.id.i_delete -> {
                final Rect bounds = computeSelectionBoundsDrawnBy(eraser);
                if (transformer.isRecycled()) {
                    if (hasSelection) {
                        saveStepBackToHistory(bounds);
                        canvas.drawRect(selection.r, eraser);
                    } else {
                        saveStepBackToHistory(null);
                        canvas.drawColor(eraser.getColorLong(), BlendMode.SRC);
                    }
                    saveStepForwardToHistory();
                } else {
                    recycleTransformer();
                }
                if (hasSelection) drawBitmapOntoView(bounds, true);
                else drawBitmapOntoView(true);
                clearStatus();
            }
            case R.id.i_deselect -> {
                drawFloatingLayersIntoImage();
                hasSelection = false;
                if (transformer.mesh != null) transformer.mesh = null;
                eraseBitmapAndInvalidateView(selectionBitmap, activityMain.canvas.ivSelection);
                clearStatus();
            }
            case R.id.i_draw_color -> {
                final Rect bounds = computeSelectionBoundsDrawnBy(paint);
                if (transformer.isRecycled()) {
                    if (hasSelection) {
                        saveStepBackToHistory(bounds);
                        canvas.drawRect(selection.r, paint);
                    } else {
                        saveStepBackToHistory(null);
                        canvas.drawColor(paint.getColorLong());
                    }
                    saveStepForwardToHistory();
                } else {
                    transformer.getBitmap().eraseColor(paint.getColorLong());
                }
                if (hasSelection) drawBitmapOntoView(bounds, true);
                else drawBitmapOntoView(true);
                clearStatus();
            }
            case R.id.i_duplicate -> {
                if (transformer.isRecycled()) {
                    drawFloatingLayersIntoImage();
                    final Bitmap bm = hasSelection
                            ? BitmapUtils.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height())
                            : BitmapUtils.createBitmap(bitmap);
                    createTransformer(bm);
                    ToolSelector.selectTransformer(activityMain.tools);
                    drawSelectionOntoView();
                } else {
                    saveStepBackToHistory(selection.r);
                    canvas.drawBitmap(transformer.getBitmap(), selection.r.left, selection.r.top,
                            PAINT_SRC_OVER);
                    saveStepForwardToHistory();
                }
                if (hasSelection) drawBitmapOntoView(selection.r, true);
                else drawBitmapOntoView(true);
            }
            case R.id.i_duplicate_into_new -> {
                final Bitmap bm = hasSelection ?
                        transformer.isRecycled() ?
                                Bitmap.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height()) :
                                BitmapUtils.createBitmap(transformer.getBitmap()) :
                        Bitmap.createBitmap(bitmap);
                addProject(bm, activityMain.tlProjectList.getSelectedTabPosition() + 1, getString(R.string.copy_noun));
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
                if (!clipboardManager.hasPrimaryClip()) break;
                final ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData == null || clipData.getItemCount() < 1) break;
                @StringRes final int r = openFile(clipData.getItemAt(0).getUri());
                if (r != 0) Snackbar.make(vContent, r, Snackbar.LENGTH_LONG).show();
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
                    Snackbar.make(vContent, R.string.no_reference, Snackbar.LENGTH_LONG).show();
                    break;
                }
                drawFloatingLayersIntoImage();
                createEditPreview(false, false);
                new FillWithRefDialog(this, ref.bm(), onFillWithRefTileModeChangeListener)
                        .setOnDismissListener(dialog -> paint.setShader(null))
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_color_balance -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new ColorBalanceDialog(this, onFilterLightingChangedListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_color_matrix -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new ColorMatrixManager(this, onFilterColorMatrixChangedListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_contrast -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new SliderDialog(this)
                        .setIcon(item.getIcon()).setTitle(R.string.contrast)
                        .setValueFrom(-1.0f).setValueTo(10.0f).setValue(1.0f)
                        .setOnChangeListener(onFilterContrastSliderChangeListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_curves -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new CurvesDialog(this)
                        .setSource(editPreview.getOriginalPixels())
                        .setOnCurvesChangedListener(onFilterCurvesChangedListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_hs -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new HsDialog(this, onFilterHsChangedListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_hue_to_alpha -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new SliderDialog(this).setTitle(R.string.hue).setValueFrom(0.0f).setValueTo(360.0f).setValue(0.0f)
                        .setOnChangeListener(onFilterHToASliderChangeListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                onFilterHToASliderChangeListener.onChange(null, 0, true);
            }
            case R.id.i_filter_levels -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new LevelsDialog(this, onFilterLevelsChangedListener)
                        .drawHistogram(editPreview.getOriginalPixels())
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_lighting -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new LightingDialog(this, onFilterLightingChangedListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_lightness -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new SliderDialog(this)
                        .setIcon(item.getIcon()).setTitle(R.string.lightness)
                        .setValueFrom(-0xFF).setValueTo(0xFF).setValue(0).setStepSize(1.0f)
                        .setOnChangeListener(onFilterLightnessSliderChangeListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_posterize -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new SliderDialog(this).setTitle(R.string.posterize)
                        .setValueFrom(0x02).setValueTo(0xFF).setValue(0xFF).setStepSize(1.0f)
                        .setOnChangeListener(onFilterPosterizationSliderChangeListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_saturation -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new SliderDialog(this).setTitle(R.string.saturation).setValueFrom(-1.0f).setValueTo(10.0f).setValue(1.0f)
                        .setOnChangeListener(onFilterSaturationSliderChangeListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_filter_threshold -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true, true);
                new SliderDialog(this)
                        .setIcon(item.getIcon()).setTitle(R.string.threshold)
                        .setValueFrom(0x00).setValueTo(0xFF).setValue(0x80).setStepSize(1.0f)
                        .setOnChangeListener(onFilterThresholdSliderChangeListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                onFilterThresholdSliderChangeListener.onChange(null, 0x80, true);
                clearStatus();
            }
            case R.id.i_flip_horizontally -> scale(-1.0f, 1.0f);
            case R.id.i_flip_vertically -> scale(1.0f, -1.0f);
            case R.id.i_frame_list -> {
                project.frames.forEach(Frame::createThumbnail);
                project.frameAdapter.notifyDataSetChanged();

                bsdFrameList = new BottomSheetDialog(this);
                bsdFrameList.setTitle(R.string.frames);
                bsdFrameList.setContentView(frameList.getRoot());
                bsdFrameList.setOnDismissListener(dialog -> {
                    ((ViewGroup) frameList.getRoot().getParent()).removeAllViews();
                    bsdFrameList = null;
                });
                bsdFrameList.show();
            }
            case R.id.i_generate_noise -> {
                drawFloatingLayersIntoImage();
                createEditPreview(false, false);
                new NoiseGenerator(this, onNoisePropChangedListener)
                        .setOnActionListener(onEditPreviewPBClickListener, onEditPreviewCancelListener)
                        .show();
                clearStatus();
            }
            case R.id.i_guides_clear -> {
                project.guides.clear();
                drawGridOntoView();
                clearStatus();
            }
            case R.id.i_guides_new -> {
                final Guide guide = new Guide();
                project.guides.offerFirst(guide); // Add at the front for faster removal if necessary later
                final Bitmap background = frame.getBackgroundLayer().bitmap;
                new GuideEditor(this, guide, background.getWidth(), background.getHeight(),
                        g -> drawGridOntoView(),
                        dialog -> {
                            project.guides.remove(guide);
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
            }
            case R.id.i_information -> {
                final StringBuilder message = new StringBuilder()
                        .append(getString(R.string.size)).append('\n').append(bitmap.getWidth()).append(" × ").append(bitmap.getHeight()).append("\n\n")
                        .append(getString(R.string.configuration)).append('\n').append(bitmap.getConfig()).append("\n\n")
                        .append(getString(R.string.has_alpha)).append('\n').append(bitmap.hasAlpha()).append("\n\n")
                        .append(getString(R.string.color_space)).append('\n').append(bitmap.getColorSpace());
                new MaterialAlertDialogBuilder(this)
                        .setIcon(R.drawable.ic_info)
                        .setTitle(R.string.information)
                        .setMessage(message)
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }
            case R.id.i_layer_list -> {
                frame.layerAdapter.notifyDataSetChanged();

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
                if (clipboard == null) break;
                drawFloatingLayersIntoImage();

                boolean si = !hasSelection; // Is selection invisible
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
                createTransformer(BitmapUtils.createBitmap(clipboard));
                ToolSelector.selectTransformer(activityMain.tools);
                drawBitmapOntoView(selection.r, true);
                drawSelectionOntoView();
            }
            case R.id.i_redo -> {
                if (project.history.canRedo()) onExecuteHistoricalCommand(project.history.redo());
            }
            case R.id.i_rotate_90 -> rotate(90.0f);
            case R.id.i_rotate_180 -> rotate(180.0f);
            case R.id.i_rotate_270 -> rotate(270.0f);
            case R.id.i_select_all -> {
                selectAll();
                hasSelection = true;
                if (transformer.mesh != null) transformer.resetMesh();
                drawSelectionOntoView();
                clearStatus();
            }
            case R.id.i_settings -> startActivity(new Intent(this, SettingsActivity.class));
            case R.id.i_size -> new ImageSizeManager(this, bitmap, onImageSizeApplyListener).show();
            case R.id.i_transform -> {
                drawFloatingLayersIntoImage();
                createEditPreview(true);
                new MatrixManager(this,
                        onMatrixChangedListener)
                        .setOnActionListener(onEditPreviewPBClickListener, () -> {
                            drawBitmapOntoView(true);
                            recycleEditPreview();
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
                } else if (project.history.canUndo()) {
                    onExecuteHistoricalCommand(project.history.undo());
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

    private void onPaintColorChanged() {
        if (activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_brush) {
            brush.set(paint.getColorLong());
        } else if (isEditingText) {
            drawTextOntoView(text.rect(), true);
        }
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
                project.history.optimize();
            }
            case R.id.i_tab_move_to_first -> {
                if (position == 0) break;

                // Move without shifting the elements to the end since the list is an ArrayList
                for (int i = position; i > 0; --i) projects.set(i, projects.get(i - 1));
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
        if (hasNotLoaded) return;
        activityMain.btgZoom.uncheck(R.id.b_zoom);
        activityMain.canvas.flIv.setOnTouchListener(onIVTouchListener);
        hideToolOptions();
        isShapeStopped = true;
        shape = null;
        if (!dpPreview.isRecycled()) {
            dpPreview.recycle();
            drawBitmapOntoView(true);
        }
        if (!isa.isRecycled()) isa.recycle();
        eraseBitmapAndInvalidateView(previewBitmap, activityMain.canvas.ivPreview);
        paint.setAntiAlias(antiAlias);
        setBlurRadius(paint, blurRadius);
        paint.setShader(null);
        paint.setStyle(style);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onToolChanged(View.OnTouchListener onTouchIVListener, View toolOption) {
        onToolChanged(onTouchIVListener);
        if (toolOption != null) toolOption.setVisibility(View.VISIBLE);
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

    @StringRes
    private int openFile(Uri uri) {
        if (uri == null) return R.string.uri_is_invalid;
        final Bitmap bm = FileUtils.openFile(getContentResolver(), uri);
        if (bm == null) return R.string.image_is_invalid;
        return openImage(bm, uri) ? 0 : R.string.not_supported_file_type;
    }

    private boolean openImage(Bitmap bitmap, Uri uri) {
        final DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        final String name = documentFile.getName(), mimeType = documentFile.getType();
        if (mimeType != null) {
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
                addProject(BitmapUtils.mutable(bitmap), projects.size(),
                        name, FileUtils.getRealPath(this, uri), type, compressFormat);
            } else if (type == Project.FileType.GIF) {
                final String path = FileUtils.getRealPath(this, uri);
                final GifDecoder gifDecoder = new GifDecoder();
                if (path != null && gifDecoder.load(path)) {
                    bitmap.recycle();
                    final Project proj = addProject(null, projects.size(), name, path, type, null, false);
                    final Frame[] frames = new Frame[gifDecoder.frameNum()];
                    for (int i = 0; i < gifDecoder.frameNum(); ++i) {
                        final Bitmap bm = gifDecoder.frame(i);
                        if (Settings.INST.autoSetHasAlpha()) bm.setHasAlpha(true);
                        frames[i] = addFrame(proj, bm, i, gifDecoder.delay(i), false);
                        frames[i].computeLayerTree();
                    }
                    proj.onionSkins = 0;
                    selectProject(projects.size() - 1);
                } else {
                    addProject(BitmapUtils.mutable(bitmap), projects.size(), name, path, type, null);
                }
            } else {
                addProject(BitmapUtils.mutable(bitmap), projects.size(), name);
                return false;
            }
        } else {
            addProject(BitmapUtils.mutable(bitmap), projects.size());
            return false;
        }
        return true;
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

    private void recycleEditPreview() {
        editPreview.recycle();
        editPreview = null;
    }

    private void recycleTransformer() {
        if (transformer.isRecycled()) return;
        transformer.recycle();
        if (transformerActionMode != null) {
            transformerActionMode.finish();
            transformerActionMode = null;
        }
    }

    private void resizeImage(Layer layer, int width, int height,
                             ImageSizeManager.ScaleType scaleType, @Nullable Bitmap newImage) {
        final Bitmap bm = Bitmap.createBitmap(width, height,
                layer.bitmap.getConfig(), layer.bitmap.hasAlpha(), layer.bitmap.getColorSpace());
        final Canvas cv = new Canvas(bm);
        if (scaleType != null) {
            if (newImage == null) newImage = layer.bitmap;
            if (scaleType == ImageSizeManager.ScaleType.CROP) {
                cv.drawBitmap(newImage, 0.0f, 0.0f, PAINT_BITMAP);
            } else {
                cv.drawBitmap(newImage,
                        null, new RectF(0.0f, 0.0f, width, height),
                        scaleType == ImageSizeManager.ScaleType.STRETCH_FILTER ? PAINT_SRC : PAINT_BITMAP);
            }
        }
        project.history.addUndoAction(layer, layer.bitmap, null);
        layer.bitmap.recycle();
        layer.bitmap = bm;
        layer.canvas = cv;
        project.history.addRedoAction(layer, layer.bitmap);

        if (layer == frame.getBackgroundLayer()) {
            calculateBackgroundSizeOnView();
            drawChessboardOntoView();
        }
        if (layer == this.layer) {
            bitmap = bm;
            canvas = cv;

            hasSelection = false;
            if (!dpPreview.isRecycled()) dpPreview.setBitmap(bitmap.getWidth(), bitmap.getHeight());
            if (!isa.isRecycled()) isa.set(bitmap);

            drawGridOntoView();
            drawSelectionOntoView();
            clearStatus(); // Prevent from displaying the old size
        }
    }

    private void rotate(float degrees) {
        if (!hasSelection && (degrees == 90.0f || degrees == 270.0f)) {
            final int width = bitmap.getWidth(), height = bitmap.getHeight();
            final Bitmap bm = Bitmap.createBitmap(height, width, bitmap.getConfig(), bitmap.hasAlpha(), bitmap.getColorSpace());
            canvas = layer.canvas = new Canvas(bm);
            final Matrix matrix = new Matrix();
            final float pivot = switch ((int) degrees) {
                case 90 -> height / 2.0f;
                case 270 -> width / 2.0f;
                default -> 0.0f;
            };
            matrix.setRotate(degrees, pivot, pivot);
            canvas.drawBitmap(bitmap, matrix, PAINT_BITMAP);
            saveStepBackToHistory(null);
            bitmap.recycle();
            bitmap = layer.bitmap = bm;
            saveStepForwardToHistory();
            if (layer == frame.getBackgroundLayer()) {
                calculateBackgroundSizeOnView();
                drawChessboardOntoView();
            }
            drawBitmapOntoView(true, true);
            drawGridOntoView();
        } else {
            final Matrix matrix = new Matrix();
            matrix.setRotate(degrees, selection.r.exactCenterX(), selection.r.exactCenterY());
            matrix.postTranslate(selection.r.left, selection.r.top);
            final RectF rf = new RectF(selection.r);
            matrix.mapRect(rf);
            final Rect r = new Rect();
            rf.roundOut(r);
            r.union(selection.r);
            if (transformer.isRecycled()) {
                if (!hasSelection) selectAll();
                saveStepBackToHistory(r);
                final Bitmap bm = Bitmap.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height());
                canvas.drawRect(selection.r, eraser);
                canvas.drawBitmap(bm, matrix, PAINT_BITMAP_OVER);
                bm.recycle();
                saveStepForwardToHistory();
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

    private void saveStateToHistory() {
        if (isa.doStateBoundsIntersect()) {
            saveStepBackToHistory(isa.bitmap(), isa.stateBounds);
            saveStepForwardToHistory();
        } else {
            isa.resetBounds();
        }
    }

    private void saveStepForwardToHistory() {
        final Rect r = project.history.addRedoAction(layer, layer.bitmap);
        if (!isa.isRecycled()) {
            if (r == null || isa.areStateBoundsEmpty()) isa.erase(layer.bitmap, r);
            else isa.post(layer.bitmap, r);
        }
    }

    private void saveStepBackToHistory(Rect rect) {
        saveStepBackToHistory(layer.bitmap, rect);
    }

    private void saveStepBackToHistory(Bitmap src, Rect rect) {
        project.history.addUndoAction(layer, src, rect);
    }

    private void scale(float sx, float sy) {
        final Matrix matrix = new Matrix();
        matrix.setScale(sx, sy, selection.r.exactCenterX(), selection.r.exactCenterY());
        matrix.postTranslate(selection.r.left, selection.r.top);
        final RectF rf = new RectF(selection.r);
        matrix.mapRect(rf);
        final Rect r = new Rect();
        rf.roundOut(r);
        r.union(selection.r);
        if (transformer.isRecycled()) {
            if (!hasSelection) selectAll();
            saveStepBackToHistory(r);
            final Bitmap bm = Bitmap.createBitmap(bitmap, selection.r.left, selection.r.top, selection.r.width(), selection.r.height());
            canvas.drawRect(selection.r, eraser);
            canvas.drawBitmap(bm, matrix, PAINT_BITMAP_OVER);
            bm.recycle();
            saveStepForwardToHistory();
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
        drawBitmapOntoView(r, true);
    }

    private void selectAll() {
        selection.r.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    private void selectFrame(int position) {
        selectFrame(project, position);
    }

    private void selectFrame(Project project, int position) {
        drawFloatingLayersIntoImage();

        frame = project.frames.get(position);
        project.selectedFrameIndex = position;
        layerList.rvLayerList.setAdapter(frame.layerAdapter);

        calculateBackgroundSizeOnView();
        selectLayer(frame.selectedLayerIndex);
        frame.layerAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NonConstantResourceId")
    private void selectLayer(int position) {
        drawFloatingLayersIntoImage();

        final Layer unselectedLayer = layer;
        layer = frame.layers.get(position);
        frame.selectedLayerIndex = position;
        bitmap = layer.bitmap;
        canvas = layer.canvas;

        if (hasSelection) {
            selection.r.offset(unselectedLayer.left - layer.left, unselectedLayer.top - layer.top);
            optimizeSelection();
            if (transformer.mesh != null) transformer.resetMesh();
        }
        if (!dpPreview.isRecycled()) dpPreview.erase();
        if (!isa.isRecycled()) isa.set(bitmap);

        if (activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_clone_stamp) {
            cloneStampSrc = null;
        }
        updateReference();

        if (activityMain.topAppBar != null) miHasAlpha.setChecked(bitmap.hasAlpha());

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

    private void setBlurRadius(Paint paint, float f) {
        paint.setMaskFilter(f > 0.0f ? new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL) : null);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setColorRep() {
        onIVTouchWithEyedropperListener = Settings.INST.colorRep()
                ? onIVTouchWithPreciseEyedropperListener : onIVTouchWithImpreciseEyedropperListener;
        if (activityMain != null && activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_eyedropper) {
            activityMain.canvas.flIv.setOnTouchListener(onIVTouchWithEyedropperListener);
        }
    }

    public void setFilterBitmap(boolean filterBitmap) {
        bitmapPaint.setFilterBitmap(filterBitmap);
        onionSkinPaint.setFilterBitmap(filterBitmap);
        if (!hasNotLoaded) {
            drawBitmapOntoView(true);
            if (project.onionSkins > 0) drawChessboardOntoView();
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

    private void setQuality(Project project, DirectorySelector.OnFileNameApplyCallback callback) {
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
                            if (callback != null) callback.onApply(project);
                        })
                        .show();
            }
        }
    }

    public void setRunnableRunner(boolean multithreaded) {
        runnableRunner = multithreaded ? runnableStartingRunner : runnableRunningRunner;
    }

    private void setSelection(int fromX, int fromY, int toX, int toY) {
        selection.set(fromX, fromY, toX, toY);
        drawSelectionOntoView();
    }

    private void showStateForEditPreview(CharSequence text) {
        if (editPreview == null || editPreview.committed()) return;
        activityMain.tvStatus.setText(text);
    }

    @SuppressLint("StringFormatMatches")
    private void showStateOfHs(@Size(4) float[] deltaHs) {
        if (editPreview != null && editPreview.committed()) return;
        activityMain.tvStatus.setText(getString(R.string.state_hs, deltaHs[0], deltaHs[1], switch ((int) deltaHs[3]) {
            default -> 'V';
            case 1 -> 'L';
        }, deltaHs[2]));
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
        return (x - translationX) / scale - layer.left;
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
        return (y - translationY) / scale - layer.top;
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

    public float toViewX(float x) {
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

    public float toViewY(float y) {
        return translationY + (y + layer.top) * scale;
    }

    private float toViewYRel(int y) {
        return translationY + y * scale;
    }

    private void updateBrush(BrushTool.TipShape tipShape) {
        if (tipShape == null) tipShape = brush.tipShape;
        switch (tipShape) {
            case PRESET_BRUSH -> brush.setToBrush(paint.getColorLong());
            case REF -> {
                updateReference(true);
                if (brush.tipShape == BrushTool.TipShape.PRESET_BRUSH)
                    brush.setToRef(ref.bm(), paint.getColorLong());
            }
        }
    }

    private void updateReference() {
        updateReference(false);
    }

    private void updateReference(boolean nonNull) {
        final Bitmap rb = frame.mergeReferenceLayers();
        ref.set(rb != null ? rb : nonNull || checkRefNecessity() ? BitmapUtils.createBitmap(bitmap) : null);
        if (activityMain.tools.btgTools.getCheckedButtonId() == R.id.b_brush && brush.tipShape == BrushTool.TipShape.REF) {
            brush.setToRef(ref.bm(), paint.getColorLong());
        }
    }
}
