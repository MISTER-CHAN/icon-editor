package com.misterchan.iconeditor;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.util.TypedValue;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private final class FloodFill extends Thread {

        int x, y, color;

        private FloodFill(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        @Override
        public void run() {
            int pixel = bitmap.getPixel(x, y);
            if (pixel == color) {
                return;
            }
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
            Timer timer = new Timer();
            timer.schedule(new ViewBitmapTask(), 50L, 50L);
            Queue<Point> pointsToBeSet = new LinkedList<>();
            boolean[][] havePointsBeenSet = new boolean[right - left + 1][bottom - top + 1];
            pointsToBeSet.offer(new Point(x, y));
            Point point;
            while ((point = pointsToBeSet.poll()) != null) {
                int px = bitmap.getPixel(point.x, point.y);
                int xr = point.x - left, yr = point.y - top; // r - relative
                havePointsBeenSet[xr][yr] = true;
                if (px == pixel && px != color) {
                    bitmap.setPixel(point.x, point.y, color);
                    int xn = point.x - 1, xp = point.x + 1, yn = point.y - 1, yp = point.y + 1; // n - negative, p - positive
                    if (left <= xn && !havePointsBeenSet[xn - left][yr])
                        pointsToBeSet.offer(new Point(xn, point.y));
                    if (xp <= right && !havePointsBeenSet[xp - left][yr])
                        pointsToBeSet.offer(new Point(xp, point.y));
                    if (top <= yn && !havePointsBeenSet[xr][yn - top])
                        pointsToBeSet.offer(new Point(point.x, yn));
                    if (yp <= bottom && !havePointsBeenSet[xr][yp - top])
                        pointsToBeSet.offer(new Point(point.x, yp));
                }
            }
            timer.cancel();
            timer.purge();
            drawBitmapOnView();
            history.offer(bitmap);
        }
    }

    private final class ViewBitmapTask extends TimerTask {
        @Override
        public void run() {
            drawBitmapOnCanvas(bitmap, window.translationX, window.translationY, viewCanvas);
            imageView.invalidate();
        }
    }

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

    private AfterTextChangedListener onImgSizeXTextChangedListener, onImgSizeYTextChangedListener;
    private Bitmap bitmap;
    private Bitmap chessboard;
    private Bitmap chessboardBitmap;
    private Bitmap clipboard;
    private Bitmap gridBitmap;
    private Bitmap previewBitmap;
    private Bitmap rulerHBitmap, rulerVBitmap;
    private Bitmap transformeeBitmap;
    private Bitmap selectionBitmap;
    private Bitmap viewBitmap;
    private BitmapHistory history;
    private boolean hasNotLoaded = true;
    private boolean hasSelection = false;
    private boolean hasStretched = false;
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
    private CheckBox cbCellGridEnabled;
    private CheckBox cbImgLar;
    private CheckBox cbTransformerLar;
    private CheckBox cbZoom;
    private ColorAdapter colorAdapter;
    private double prevDiagonal;
    private double transformeeAspectRatio;
    private EditText etCellGridOffsetX, etCellGridOffsetY;
    private EditText etCellGridSizeX, etCellGridSizeY;
    private EditText etCellGridSpacingX, etCellGridSpacingY;
    private EditText etEraserStrokeWidth;
    private EditText etFileName;
    private EditText etImgSizeX, etImgSizeY;
    private EditText etNewGraphicSizeX, etNewGraphicSizeY;
    private EditText etPencilStrokeWidth;
    private EditText etText;
    private EditText etTextSize;
    private float pivotX, pivotY;
    private float prevX, prevY;
    private float transformeeCenterHorizontal, transformeeCenterVertical;
    private float transformeeTranslationX, transformeeTranslationY;
    private FrameLayout flImageView;
    private ImageView imageView;
    private ImageView ivChessboard;
    private ImageView ivGrid;
    private ImageView ivPreview;
    private ImageView ivRulerH, ivRulerV;
    private ImageView ivSelection;
    private InputMethodManager inputMethodManager;
    private int currentBitmapIndex;
    private int imageWidth, imageHeight;
    private int selectionStartX, selectionStartY;
    private int selectionEndX, selectionEndY;
    private int shapeStartX, shapeStartY;
    private int textX, textY;
    private int viewWidth, viewHeight;
    private LinearLayout llBehaviorBucketFill;
    private LinearLayout llBehaviorEraser;
    private LinearLayout llBehaviorPencil;
    private LinearLayout llBehaviorShape;
    private LinearLayout llBehaviorText;
    private LinearLayout llBehaviorTransformer;
    private LinkedList<Integer> swatches;
    private List<Window> windows = new ArrayList<>();
    private Position stretchingBound = Position.NULL;
    private Positions selection = new Positions();
    private PositionsF transfromeeDpb = new PositionsF(); // DPB - Distance from point to bounds
    private RadioButton rbImgCrop, rbImgStretch;
    private RadioButton rbTransformer;
    private RecyclerView rvSwatches;
    private Spinner sFileType;
    private String tree = "";
    private TabLayout tabLayout;
    private TextView tvStatus;
    private Uri fileToBeOpened;
    private View vBackgroundColor;
    private View vForegroundColor;
    private Window window;

    private final Paint cellGridPaint = new Paint() {
        {
            setColor(Color.RED);
            setStrokeWidth(2.0f);
        }
    };

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
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }
    };

    private final Paint paint = new Paint() {
        {
            setAntiAlias(false);
            setColor(Color.BLACK);
            setDither(false);
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

    private final CompoundButton.OnCheckedChangeListener onImgSizeLarCheckBoxCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            etImgSizeX.addTextChangedListener(onImgSizeXTextChangedListener);
            etImgSizeY.addTextChangedListener(onImgSizeYTextChangedListener);
        } else {
            etImgSizeX.removeTextChangedListener(onImgSizeXTextChangedListener);
            etImgSizeY.removeTextChangedListener(onImgSizeYTextChangedListener);
        }
    };

    private final DialogInterface.OnClickListener onCellGridDialogPosButtonClickListener = (dialog, which) -> {
        try {
            int sizeX = Integer.parseInt(etCellGridSizeX.getText().toString()),
                    sizeY = Integer.parseInt(etCellGridSizeY.getText().toString()),
                    spacingX = Integer.parseInt(etCellGridSpacingX.getText().toString()),
                    spacingY = Integer.parseInt(etCellGridSpacingY.getText().toString()),
                    offsetX = Integer.parseInt(etCellGridOffsetX.getText().toString()),
                    offsetY = Integer.parseInt(etCellGridOffsetY.getText().toString());
            cellGrid.enabled = cbCellGridEnabled.isChecked();
            cellGrid.sizeX = sizeX;
            cellGrid.sizeY = sizeY;
            cellGrid.spacingX = spacingX;
            cellGrid.spacingY = spacingY;
            cellGrid.offsetX = offsetX;
            cellGrid.offsetY = offsetY;
        } catch (NumberFormatException e) {
        }
        drawGridOnView();
    };

    private final DialogInterface.OnClickListener onFileNameDialogPosButtonClickListener = (dialog, which) -> {
        String fileName = etFileName.getText().toString();
        if ("".equals(fileName)) {
            return;
        }
        fileName += sFileType.getSelectedItem().toString();
        window.path = Environment.getExternalStorageDirectory().getPath() + File.separator + tree + File.separator + fileName;
        window.compressFormat = COMPRESS_FORMATS[sFileType.getSelectedItemPosition()];
        save(window.path);
        tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).setText(fileName);
    };

    private final DialogInterface.OnClickListener onImgSizeDialogPosButtonClickListener = (dialog, which) -> {
        try {
            int width = Integer.parseUnsignedInt(etImgSizeX.getText().toString());
            int height = Integer.parseUnsignedInt(etImgSizeY.getText().toString());
            boolean stretch = rbImgStretch.isChecked();
            resizeBitmap(width, height, stretch);
            drawBitmapOnView();
            history.offer(bitmap);
        } catch (NumberFormatException e) {
        }
    };

    private final DialogInterface.OnClickListener onNewGraphicDialogPosButtonClickListener = (dialog, which) -> {
        try {
            int width = Integer.parseUnsignedInt(etNewGraphicSizeX.getText().toString());
            int height = Integer.parseUnsignedInt(etNewGraphicSizeY.getText().toString());
            createGraphic(width, height);
        } catch (NumberFormatException e) {
        }
    };

    private final View.OnClickListener onAddColorViewClickListener = v -> new ColorPicker().show(
            MainActivity.this,
            (oldColor, newColor) -> {
                swatches.offerFirst(newColor);
                colorAdapter.notifyDataSetChanged();
            });

    private final View.OnClickListener onBackgroundColorClickListener = v -> new ColorPicker().show(
            MainActivity.this,
            (oldColor, newColor) -> {
                eraser.setColor(newColor);
                vBackgroundColor.setBackgroundColor(newColor);
            },
            eraser.getColor());

    private final View.OnClickListener onForegroundColorClickListener = v -> new ColorPicker().show(
            MainActivity.this,
            (oldColor, newColor) -> {
                paint.setColor(newColor);
                vForegroundColor.setBackgroundColor(newColor);
                if (llBehaviorText.getVisibility() == View.VISIBLE) {
                    drawTextOnView();
                }
            },
            paint.getColor());

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

    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            drawTransformeeOnCanvas();
            drawTextOnCanvas();

            currentBitmapIndex = tab.getPosition();
            window = windows.get(currentBitmapIndex);
            bitmap = window.bitmap;
            canvas = new Canvas(bitmap);
            history = window.history;
            cellGrid = window.cellGrid;

            int width = bitmap.getWidth(), height = bitmap.getHeight();
            imageWidth = (int) toScaled(width);
            imageHeight = (int) toScaled(height);

            recycleBitmapIfIsNotNull(transformeeBitmap);
            transformeeBitmap = null;
            hasSelection = false;

            drawChessboardOnView();
            drawBitmapOnView();
            drawGridOnView();
            drawSelectionOnView();
            clearCanvasAndInvalidateView(previewCanvas, ivPreview);
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
                int originalX = toOriginal(x - window.translationX), originalY = toOriginal(y - window.translationY);
                if (!(0 <= originalX && originalX < bitmap.getWidth() && 0 <= originalY && originalY < bitmap.getHeight())) {
                    break;
                }
                if (cbBucketFillContiguous.isChecked()) {
                    floodFill(originalX, originalY, paint.getColor());
                } else {
                    bucketFill(originalX, originalY, paint.getColor());
                }
                tvStatus.setText("");
                break;
            }
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithEraserListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                int originalX = toOriginal(x - window.translationX), originalY = toOriginal(y - window.translationY);
                canvas.drawPoint(originalX, originalY, eraser);
                drawBitmapOnView();
                tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                prevX = x;
                prevY = y;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                int originalX = toOriginal(x - window.translationX), originalY = toOriginal(y - window.translationY);
                drawLineOnCanvas(
                        toOriginal(prevX - window.translationX),
                        toOriginal(prevY - window.translationY),
                        originalX,
                        originalY,
                        eraser);
                drawBitmapOnView();
                tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                prevX = x;
                prevY = y;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                history.offer(bitmap);
                tvStatus.setText("");
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithEyedropperListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                int originalX = toOriginal(x - window.translationX), originalY = toOriginal(y - window.translationY);
                int color = bitmap.getPixel(originalX, originalY);
                paint.setColor(color);
                vForegroundColor.setBackgroundColor(color);
                tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                tvStatus.setText("");
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithPencilListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                int originalX = toOriginal(x - window.translationX), originalY = toOriginal(y - window.translationY);
                canvas.drawPoint(originalX, originalY, paint);
                drawBitmapOnView();
                tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                prevX = x;
                prevY = y;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                int originalX = toOriginal(x - window.translationX), originalY = toOriginal(y - window.translationY);
                drawLineOnCanvas(
                        toOriginal(prevX - window.translationX),
                        toOriginal(prevY - window.translationY),
                        originalX,
                        originalY,
                        paint);
                drawBitmapOnView();
                tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                prevX = x;
                prevY = y;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                history.offer(bitmap);
                tvStatus.setText("");
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithSelectorListener = (v, event) -> {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                float x = event.getX(), y = event.getY();
                if (hasSelection && selectionStartX == selectionEndX && selectionStartY == selectionEndY) {
                    selectionEndX = toOriginal(x - window.translationX);
                    selectionEndY = toOriginal(y - window.translationY);
                } else {
                    hasSelection = true;
                    selectionStartX = toOriginal(x - window.translationX);
                    selectionStartY = toOriginal(y - window.translationY);
                    selectionEndX = selectionStartX;
                    selectionEndY = selectionStartY;
                }
                drawSelectionOnViewByStartsAndEnds();
                tvStatus.setText(String.format("Start: (%d, %d), End: (%d, %d), Area: 1 × 1",
                        selectionStartX, selectionStartY, selectionStartX, selectionStartY));
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float x = event.getX(), y = event.getY();
                selectionEndX = toOriginal(x - window.translationX);
                selectionEndY = toOriginal(y - window.translationY);
                drawSelectionOnViewByStartsAndEnds();
                tvStatus.setText(String.format("Start: (%d, %d), End: (%d, %d), Area: %d × %d",
                        selectionStartX, selectionStartY, selectionEndX, selectionEndY,
                        Math.abs(selectionEndX - selectionStartX) + 1, Math.abs(selectionEndY - selectionStartY) + 1));
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                optimizeSelection();
                drawSelectionOnView();
                tvStatus.setText(hasSelection
                        ? String.format("Start: (%d, %d), End: (%d, %d), Area: %d × %d",
                        selection.left, selection.top, selection.right, selection.bottom,
                        selection.right - selection.left + 1, selection.bottom - selection.top + 1)
                        : "");
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithShapeListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        int originalX = toOriginal(x - window.translationX), originalY = toOriginal(y - window.translationY);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: {
                paint.setStrokeWidth(toScaled((int) paint.getStrokeWidth()));
                if (isShapeStopped) {
                    isShapeStopped = false;
                    drawPointOnView(originalX, originalY);
                    shapeStartX = originalX;
                    shapeStartY = originalY;
                    tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                    break;
                }
            }

            case MotionEvent.ACTION_MOVE: {
                String result = drawShapeOnView(shapeStartX, shapeStartY, originalX, originalY);
                tvStatus.setText(
                        String.format("Start: (%d, %d), Stop: (%d, %d), ", shapeStartX, shapeStartY, originalX, originalY)
                                + result);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setStrokeWidth(etPencilStrokeWidth.getText().toString());
                if (originalX != shapeStartX || originalY != shapeStartY) {
                    drawShapeOnCanvas(shapeStartX, shapeStartY, originalX, originalY);
                    isShapeStopped = true;
                    drawBitmapOnView();
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                    history.offer(bitmap);
                    tvStatus.setText("");
                }
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener onImageViewTouchWithTextListener = (v, event) -> {
        switch (llBehaviorText.getVisibility()) {

            case View.VISIBLE: {
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
                break;
            }

            case View.INVISIBLE:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        textX = toOriginal(event.getX() - window.translationX);
                        textY = toOriginal(event.getY() - window.translationY);
                        llBehaviorText.setVisibility(View.VISIBLE);
                        scaleTextSizeAndDrawTextOnView();
                        prevX = window.translationX;
                        prevY = window.translationY;
                        break;
                }
        }
        return true;
    };

    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    private final View.OnTouchListener onImageViewTouchWithTransformerListener = (v, event) -> {
        if (!hasSelection) {
            return true;
        }

        switch (event.getPointerCount()) {

            case 1:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        float x = event.getX(), y = event.getY();
                        int width = selection.right - selection.left + 1, height = selection.bottom - selection.top + 1;
                        if (width > 0 && height > 0) {
                            if (transformeeBitmap == null) {
                                transformeeTranslationX = window.translationX + toScaled(selection.left);
                                transformeeTranslationY = window.translationY + toScaled(selection.top);
                                transformeeBitmap = Bitmap.createBitmap(bitmap,
                                        selection.left, selection.top, width, height);
                                canvas.drawRect(selection.left, selection.top, selection.right + 1, selection.bottom + 1, eraser);
                            }
                            drawBitmapOnView();
                            drawTransformeeAndSelectionOnViewByTranslation(false);
                            if (stretchingBound == Position.NULL) {
                                PositionsF selectionBounds = new PositionsF(
                                        window.translationX + toScaled(selection.left),
                                        window.translationY + toScaled(selection.top),
                                        window.translationX + toScaled(selection.right + 1),
                                        window.translationY + toScaled(selection.bottom + 1));
                                if (selectionBounds.left - 50.0f <= x && x < selectionBounds.left + 50.0f) {
                                    stretchingBound = Position.LEFT;
                                } else if (selectionBounds.top - 50.0f <= y && y < selectionBounds.top + 50.0f) {
                                    stretchingBound = Position.TOP;
                                } else if (selectionBounds.right - 50.0f <= x && x < selectionBounds.right + 50.0f) {
                                    stretchingBound = Position.RIGHT;
                                } else if (selectionBounds.bottom - 50.0f <= y && y < selectionBounds.bottom + 50.0f) {
                                    stretchingBound = Position.BOTTOM;
                                }
                                if (stretchingBound != Position.NULL) {
                                    if (cbTransformerLar.isChecked()) {
                                        transformeeAspectRatio =
                                                (double) (selection.right - selection.left + 1) / (double) (selection.bottom - selection.top + 1);
                                        transformeeCenterHorizontal = (selection.left + selection.right + 1) / 2.0f;
                                        transformeeCenterVertical = (selection.top + selection.bottom + 1) / 2.0f;
                                    }
                                    tvStatus.setText(String.format("Selected bound: %s", stretchingBound.name));
                                } else {
                                    tvStatus.setText(String.format("Left: %d, Top: %d",
                                            selection.left, selection.top));
                                }
                            } else {
                                stretchByBound(x, y);
                                tvStatus.setText(String.format("Left: %d, Top: %d",
                                        selection.left, selection.top));
                            }
                        }
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        if (transformeeBitmap == null) {
                            break;
                        }
                        float x = event.getX(), y = event.getY();
                        if (stretchingBound == Position.NULL) {
                            transformeeTranslationX += x - prevX;
                            transformeeTranslationY += y - prevY;
                            drawTransformeeAndSelectionOnViewByTranslation(true);
                            tvStatus.setText(String.format("Left: %d, Top: %d",
                                    selection.left, selection.top));
                        } else {
                            stretchByBound(x, y);
                            tvStatus.setText(String.format("Area: %d × %d",
                                    selection.right - selection.left + 1, selection.bottom - selection.top + 1));
                        }
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (stretchingBound != Position.NULL) {
                            if (hasStretched) {
                                stretchingBound = Position.NULL;
                                hasStretched = false;
                                int width = selection.right - selection.left + 1, height = selection.bottom - selection.top + 1;
                                if (width > 0 && height > 0) {
                                    Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                                    new Canvas(bm).drawBitmap(transformeeBitmap,
                                            new Rect(0, 0, transformeeBitmap.getWidth(), transformeeBitmap.getHeight()),
                                            new Rect(0, 0, width, height),
                                            opaquePaint);
                                    transformeeBitmap.recycle();
                                    transformeeBitmap = bm;
                                    transformeeTranslationX = window.translationX + toScaled(selection.left);
                                    transformeeTranslationY = window.translationY + toScaled(selection.top);
                                } else if (transformeeBitmap != null) {
                                    transformeeBitmap.recycle();
                                    transformeeBitmap = null;
                                }
                                drawTransformeeAndSelectionOnViewByTranslation(false);
                                tvStatus.setText("");
                            }
                        } else {
                            drawSelectionOnView(false);
                        }
                        break;
                }
                break;

            case 2:
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (cbTransformerLar.isChecked()) {
                            transformeeCenterHorizontal = (selection.right + selection.left + 1) / 2.0f;
                            transformeeCenterVertical = (selection.top + selection.bottom + 1) / 2.0f;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        PositionsF scaledSelection = new PositionsF(
                                window.translationX + toScaled(selection.left),
                                window.translationY + toScaled(selection.top),
                                window.translationX + toScaled(selection.right),
                                window.translationY + toScaled(selection.bottom));
                        PositionsF dpb = new PositionsF(
                                Math.min(x0 - scaledSelection.left, x1 - scaledSelection.left),
                                Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top),
                                Math.min(scaledSelection.right - x0, scaledSelection.right - x1),
                                Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1));
                        if (cbTransformerLar.isChecked()) {
                            PositionsF dpbDiff = new PositionsF();
                            dpbDiff.left = transfromeeDpb.left - dpb.left;
                            dpbDiff.top = transfromeeDpb.top - dpb.top;
                            dpbDiff.right = transfromeeDpb.right - dpb.right;
                            dpbDiff.bottom = transfromeeDpb.bottom - dpb.bottom;
                            if (Math.abs(dpbDiff.left) + Math.abs(dpbDiff.right) >= Math.abs(dpbDiff.top) + Math.abs(dpbDiff.bottom)) {
                                selection.left -= toOriginal(transfromeeDpb.left - dpb.left);
                                selection.right += toOriginal(transfromeeDpb.right - dpb.right);
                                double width = selection.right - selection.left + 1, height = width / transformeeAspectRatio;
                                selection.top = (int) (transformeeCenterVertical - height / 2.0);
                                selection.bottom = (int) (transformeeCenterVertical + height / 2.0);
                                scaledSelection.top = window.translationY + toScaled(selection.top);
                                scaledSelection.bottom = window.translationY + toScaled(selection.bottom);
                                transfromeeDpb.top = Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top);
                                transfromeeDpb.bottom = Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1);
                            } else {
                                selection.top -= toOriginal(transfromeeDpb.top - dpb.top);
                                selection.bottom += toOriginal(transfromeeDpb.bottom - dpb.bottom);
                                double height = selection.bottom - selection.top + 1, width = height * transformeeAspectRatio;
                                selection.left = (int) (transformeeCenterHorizontal - width / 2.0);
                                selection.right = (int) (transformeeCenterHorizontal + width / 2.0);
                                scaledSelection.left = window.translationX + toScaled(selection.left);
                                scaledSelection.right = window.translationX + toScaled(selection.right);
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
                        tvStatus.setText(String.format("Area: %d × %d",
                                selection.right - selection.left + 1, selection.bottom - selection.top + 1));
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_DOWN: {
                        stretchingBound = Position.NULL;
                        float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        PositionsF scaledSelection = new PositionsF();
                        scaledSelection.left = window.translationX + toScaled(selection.left);
                        scaledSelection.top = window.translationY + toScaled(selection.top);
                        scaledSelection.right = window.translationX + toScaled(selection.right);
                        scaledSelection.bottom = window.translationY + toScaled(selection.bottom);
                        transfromeeDpb.left = Math.min(x0 - scaledSelection.left, x1 - scaledSelection.left);
                        transfromeeDpb.top = Math.min(y0 - scaledSelection.top, y1 - scaledSelection.top);
                        transfromeeDpb.right = Math.min(scaledSelection.right - x0, scaledSelection.right - x1);
                        transfromeeDpb.bottom = Math.min(scaledSelection.bottom - y0, scaledSelection.bottom - y1);
                        if (cbTransformerLar.isChecked()) {
                            transformeeAspectRatio = (double) (selection.right - selection.left + 1) / (double) (selection.bottom - selection.top + 1);
                        }
                        tvStatus.setText(String.format("Area: %d × %d",
                                selection.right - selection.left + 1, selection.bottom - selection.top + 1));
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        int width = selection.right - selection.left + 1, height = selection.bottom - selection.top + 1;
                        if (width > 0 && height > 0) {
                            Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                            new Canvas(bm).drawBitmap(transformeeBitmap,
                                    new Rect(0, 0, transformeeBitmap.getWidth(), transformeeBitmap.getHeight()),
                                    new Rect(0, 0, width, height),
                                    opaquePaint);
                            transformeeBitmap.recycle();
                            transformeeBitmap = bm;
                            transformeeTranslationX = window.translationX + toScaled(selection.left);
                            transformeeTranslationY = window.translationY + toScaled(selection.top);
                        } else if (transformeeBitmap != null) {
                            transformeeBitmap.recycle();
                            transformeeBitmap = null;
                        }
                        drawTransformeeAndSelectionOnViewByTranslation();
                        prevX = event.getX(1 - event.getActionIndex());
                        prevY = event.getY(1 - event.getActionIndex());
                        tvStatus.setText("");
                        break;
                    }
                }
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithZoomToolListener = (v, event) -> {
        switch (event.getPointerCount()) {

            case 1: {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        float x = event.getX(), y = event.getY();
                        tvStatus.setText(String.format("(%d, %d)",
                                toOriginal(x - window.translationX), toOriginal(y - window.translationY)));
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float x = event.getX(), y = event.getY();
                        float deltaX = x - prevX, deltaY = y - prevY;
                        window.translationX += deltaX;
                        window.translationY += deltaY;
                        drawChessboardOnView();
                        drawBitmapOnView();
                        drawGridOnView();
                        if (transformeeBitmap != null) {
                            drawTransformeeOnViewBySelection();
                        } else if (llBehaviorText.getVisibility() == View.VISIBLE) {
                            drawTextOnView();
                        } else if (!isShapeStopped) {
                            drawPointOnView(shapeStartX, shapeStartY);
                        }
                        drawSelectionOnView();
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
                        float x0 = event.getX(0), y0 = event.getY(0),
                                x1 = event.getX(1), y1 = event.getY(1);
                        double diagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        double diagonalRatio = diagonal / prevDiagonal;
                        float scale = (float) (window.scale * diagonalRatio);
                        int scaledWidth = (int) (bitmap.getWidth() * scale), scaledHeight = (int) (bitmap.getHeight() * scale);
                        window.scale = scale;
                        imageWidth = scaledWidth;
                        imageHeight = scaledHeight;
                        float pivotX = (float) (this.pivotX * diagonalRatio), pivotY = (float) (this.pivotY * diagonalRatio);
                        window.translationX = window.translationX - pivotX + this.pivotX;
                        window.translationY = window.translationY - pivotY + this.pivotY;
                        drawChessboardOnView();
                        drawBitmapOnView();
                        drawGridOnView();
                        if (transformeeBitmap != null) {
                            drawTransformeeOnViewBySelection();
                        } else if (llBehaviorText.getVisibility() == View.VISIBLE) {
                            scaleTextSizeAndDrawTextOnView();
                        } else if (!isShapeStopped) {
                            drawPointOnView(shapeStartX, shapeStartY);
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
                        this.pivotX = (x0 + x1) / 2.0f - window.translationX;
                        this.pivotY = (y0 + y1) / 2.0f - window.translationY;
                        prevDiagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        tvStatus.setText("");
                        break;
                    }
                    case MotionEvent.ACTION_POINTER_UP: {
                        float x = event.getX(1 - event.getActionIndex());
                        float y = event.getY(1 - event.getActionIndex());
                        tvStatus.setText(String.format("(%d, %d)",
                                toOriginal(x - window.translationX), toOriginal(y - window.translationY)));
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
    private final CompoundButton.OnCheckedChangeListener onTransformerRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            cbZoom.setChecked(false);
            cbZoom.setTag(onImageViewTouchWithTransformerListener);
            flImageView.setOnTouchListener(onImageViewTouchWithTransformerListener);
            llBehaviorTransformer.setVisibility(View.VISIBLE);
            selector.setColor(Color.BLUE);
            drawSelectionOnView();
        } else {
            drawTransformeeOnCanvas();
            llBehaviorTransformer.setVisibility(View.GONE);
            stretchingBound = Position.NULL;
            selector.setColor(Color.DKGRAY);
            drawSelectionOnView();
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final CompoundButton.OnCheckedChangeListener onTextRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            cbZoom.setChecked(false);
            cbZoom.setTag(onImageViewTouchWithTextListener);
            flImageView.setOnTouchListener(onImageViewTouchWithTextListener);
            paint.setAntiAlias(true);
        } else {
            drawTextOnCanvas();
            paint.setAntiAlias(false);
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
                    window.translationX + toScaled(x0 + 0.5f),
                    window.translationY + toScaled(y0 + 0.5f),
                    toScaled(radius),
                    paint);
            return String.format("Radius: %.1f", radius + 0.5f);
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
                    window.translationX + toScaled(x0 + 0.5f),
                    window.translationY + toScaled(y0 + 0.5f),
                    window.translationX + toScaled(x1 + 0.5f),
                    window.translationY + toScaled(y1 + 0.5f),
                    paint);
            return String.format("Length: %f", Math.sqrt(Math.pow(x1 - x0, 2.0) + Math.pow(y1 - y0, 2.0)) + 1);
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
                    window.translationX + toScaled(x0 + 0.5f),
                    window.translationY + toScaled(y0 + 0.5f),
                    window.translationX + toScaled(x1 + 0.5f),
                    window.translationY + toScaled(y1 + 0.5f),
                    paint);
            return String.format("Axes: %d, %d", Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1);
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
                    window.translationX + toScaled(x0 + 0.5f),
                    window.translationY + toScaled(y0 + 0.5f),
                    window.translationX + toScaled(x1 + 0.5f),
                    window.translationY + toScaled(y1 + 0.5f),
                    paint);
            return String.format("Area: %d × %d", Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1);
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
        window = new Window();
        windows.add(window);
        window.bitmap = bitmap;
        currentBitmapIndex = windows.size() - 1;
        history = new BitmapHistory();
        window.history = history;
        history.offer(bitmap);
        window.path = path;
        window.compressFormat = compressFormat;
        cellGrid = new CellGrid();
        window.cellGrid = cellGrid;

        window.scale = (float) ((double) viewWidth / (double) width);
        imageWidth = (int) toScaled(width);
        imageHeight = (int) toScaled(height);
        window.translationX = 0.0f;
        window.translationY = 0.0f;

        recycleBitmapIfIsNotNull(transformeeBitmap);
        transformeeBitmap = null;
        hasSelection = false;

        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
        drawSelectionOnView();

        tabLayout.addTab(tabLayout.newTab().setText(title).setTag(bitmap));
        tabLayout.getTabAt(currentBitmapIndex).select();
    }

    private void bucketFill(int x, int y, int color) {
        int pixel = bitmap.getPixel(x, y);
        if (pixel == color) {
            return;
        }
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
        for (y = top; y <= bottom; ++y) {
            for (x = left; x <= right; ++x) {
                if (bitmap.getPixel(x, y) == pixel) {
                    bitmap.setPixel(x, y, color);
                }
            }
        }
        drawBitmapOnView();
        history.offer(bitmap);
    }

    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private void clearCanvasAndInvalidateView(Canvas canvas, ImageView imageView) {
        clearCanvas(canvas);
        imageView.invalidate();
    }

    private void createGraphic(int width, int height) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        addBitmap(bitmap, width, height);
    }

    private void drawBitmapOnCanvas(Bitmap bm, float translX, float translY, Canvas cv) {
        int startX = translX >= 0.0f ? 0 : toOriginal(-translX);
        int startY = translY >= 0.0f ? 0 : toOriginal(-translY);
        int bitmapWidth = bm.getWidth(), bitmapHeight = bm.getHeight();
        int scaledBmpWidth = (int) toScaled(bitmapWidth), scaledBmpHeight = (int) toScaled(bitmapHeight);
        int endX = Math.min(toOriginal(translX + scaledBmpWidth <= viewWidth ? scaledBmpWidth : viewWidth - translX) + 1, bitmapWidth);
        int endY = Math.min(toOriginal(translY + scaledBmpHeight <= viewHeight ? scaledBmpHeight : viewHeight - translY) + 1, bitmapHeight);
        float left = translX >= 0.0f ? translX : translX % window.scale;
        float top = translY >= 0.0f ? translY : translY % window.scale;
        if (isScaledMuch()) {
            float t = top, b = t + window.scale;
            for (int y = startY; y < endY; ++y, t += window.scale, b += window.scale) {
                float l = left;
                for (int x = startX; x < endX; ++x) {
                    colorPaint.setColor(bm.getPixel(x, y));
                    cv.drawRect(l, t, l += window.scale, b, colorPaint);
                }
            }
        } else {
            float right = Math.min(translX + scaledBmpWidth, viewWidth);
            float bottom = Math.min(translY + scaledBmpHeight, viewHeight);
            cv.drawBitmap(bm,
                    new Rect(startX, startY, endX, endY),
                    new RectF(left, top, right, bottom),
                    opaquePaint);
        }
    }

    private void drawBitmapOnView() {
        clearCanvas(viewCanvas);
        drawBitmapOnCanvas(bitmap, window.translationX, window.translationY, viewCanvas);
        imageView.invalidate();
    }

    private void drawChessboardOnView() {
        clearCanvas(chessboardCanvas);
        float left = Math.max(0.0f, window.translationX);
        float top = Math.max(0.0f, window.translationY);
        float right = Math.min(window.translationX + imageWidth, viewWidth);
        float bottom = Math.min(window.translationY + imageHeight, viewHeight);

        chessboardCanvas.drawBitmap(chessboard,
                new Rect((int) left, (int) top, (int) right, (int) bottom),
                new RectF(left, top, right, bottom),
                opaquePaint);

        chessboardCanvas.drawLine(left, top, left - 100.0f, top, imageBound);
        chessboardCanvas.drawLine(right, top, right + 100.0f, top, imageBound);
        chessboardCanvas.drawLine(right, top - 100.0f, right, top, imageBound);
        chessboardCanvas.drawLine(right, bottom, right, bottom + 100.0f, imageBound);
        chessboardCanvas.drawLine(right + 100.0f, bottom, right, bottom, imageBound);
        chessboardCanvas.drawLine(left, bottom, left - 100.0f, bottom, imageBound);
        chessboardCanvas.drawLine(left, bottom + 100.0f, left, bottom, imageBound);
        chessboardCanvas.drawLine(left, top, left, top - 100.0f, imageBound);

        ivChessboard.invalidate();

        drawRuler();
    }

    private void drawGridOnView() {
        clearCanvas(gridCanvas);
        float startX = window.translationX >= 0.0f ? window.translationX : window.translationX % window.scale,
                startY = window.translationY >= 0.0f ? window.translationY : window.translationY % window.scale,
                endX = Math.min(window.translationX + imageWidth, viewWidth),
                endY = Math.min(window.translationY + imageHeight, viewHeight);
        if (isScaledMuch()) {
            for (float x = startX; x < endX; x += window.scale) {
                gridCanvas.drawLine(x, startY, x, endY, gridPaint);
            }
            for (float y = startY; y < endY; y += window.scale) {
                gridCanvas.drawLine(startX, y, endX, y, gridPaint);
            }
        }

        if (cellGrid.enabled) {
            if (cellGrid.sizeX > 1) {
                float scaledSizeX = toScaled(cellGrid.sizeX),
                        scaledSpacingX = toScaled(cellGrid.spacingX);
                startX = (window.translationX >= 0.0f ? window.translationX : window.translationX % (scaledSizeX + scaledSpacingX)) + toScaled(cellGrid.offsetX);
                startY = Math.max(0.0f, window.translationY);
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
                startY = (window.translationY >= 0.0f ? window.translationY : window.translationY % (scaledSizeY + scaledSpacingY)) + toScaled(cellGrid.offsetY);
                startX = Math.max(0.0f, window.translationX);
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
                window.translationX + toScaled(x),
                window.translationY + toScaled(y),
                window.translationX + toScaled(x + 1),
                window.translationY + toScaled(y + 1),
                fillPaint);
        ivPreview.invalidate();
    }

    private void drawRuler() {
        clearCanvas(rulerHCanvas);
        clearCanvas(rulerVCanvas);
        final int multiplier = (int) Math.ceil(96.0 / window.scale);
        final float scaledMultiplier = toScaled(multiplier);
        float x = window.translationX % scaledMultiplier, height = rulerHBitmap.getHeight();
        int originalX = (int) (-window.translationX / scaledMultiplier) * multiplier;
        for (;
             x < viewWidth;
             x += scaledMultiplier, originalX += multiplier) {
            rulerHCanvas.drawLine(x, 0.0f, x, height, rulerPaint);
            rulerHCanvas.drawText(String.valueOf(originalX), x, height, rulerPaint);
        }
        float y = window.translationY % scaledMultiplier, width = rulerVBitmap.getWidth();
        int originalY = (int) (-window.translationY / scaledMultiplier) * multiplier;
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
            float left = Math.max(0.0f, window.translationX + toScaled(selection.left)),
                    top = Math.max(0.0f, window.translationY + toScaled(selection.top)),
                    right = Math.min(viewWidth, window.translationX + toScaled(selection.right + 1)),
                    bottom = Math.min(viewHeight, window.translationY + toScaled(selection.bottom + 1));
            selectionCanvas.drawRect(left, top, right, bottom, selector);
            if (showMargins) {
                float imageLeft = Math.max(0.0f, window.translationX),
                        imageTop = Math.max(0.0f, window.translationY),
                        imageRight = Math.min(viewWidth, window.translationX + imageWidth),
                        imageBottom = Math.min(viewHeight, window.translationY + imageHeight);
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
                    window.translationX + toScaled(selection.left),
                    window.translationY + toScaled(selection.top),
                    window.translationX + toScaled(selection.right + 1),
                    window.translationY + toScaled(selection.bottom + 1),
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
        if (llBehaviorText.getVisibility() != View.VISIBLE) {
            return;
        }
        try {
            paint.setTextSize(Float.parseFloat(etTextSize.getText().toString()));
        } catch (NumberFormatException e) {
        }
        canvas.drawText(etText.getText().toString(), textX, textY, paint);
        drawBitmapOnView();
        clearCanvasAndInvalidateView(previewCanvas, ivPreview);
        inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        llBehaviorText.setVisibility(View.INVISIBLE);
        history.offer(bitmap);
    }

    private void drawTextOnView() {
        clearCanvas(previewCanvas);
        float x = window.translationX + toScaled(textX), y = window.translationY + toScaled(textY);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float centerVertical = y + fontMetrics.ascent / 2.0f;
        previewCanvas.drawText(etText.getText().toString(), x, y, paint);
        previewCanvas.drawLine(x, 0.0f, x, viewHeight, cellGridPaint);
        previewCanvas.drawLine(0.0f, y, viewWidth, y, textLine);
        previewCanvas.drawLine(0.0f, centerVertical, viewWidth, centerVertical, cellGridPaint);
        ivPreview.invalidate();
    }

    private void drawTransformeeOnCanvas() {
        if (transformeeBitmap != null) {
            if (hasSelection) {
                canvas.drawBitmap(transformeeBitmap, selection.left, selection.top, paint);
                optimizeSelection();
                drawSelectionOnView();
                drawBitmapOnView();
                history.offer(bitmap);
                tvStatus.setText("");
            }
            transformeeBitmap.recycle();
            transformeeBitmap = null;
        }
        clearCanvasAndInvalidateView(previewCanvas, ivPreview);
    }

    private void drawTransformeeAndSelectionOnViewByTranslation() {
        drawTransformeeAndSelectionOnViewByTranslation(false);
    }

    private void drawTransformeeAndSelectionOnViewByTranslation(boolean showMargins) {
        clearCanvas(previewCanvas);
        if (hasSelection && transformeeBitmap != null) {
            selection.left = toOriginal(transformeeTranslationX - window.translationX);
            selection.top = toOriginal(transformeeTranslationY - window.translationY);
            selection.right = selection.left + transformeeBitmap.getWidth() - 1;
            selection.bottom = selection.top + transformeeBitmap.getHeight() - 1;
            float ttx = toScaled(selection.left) + window.translationX;
            float tty = toScaled(selection.top) + window.translationY;
            drawBitmapOnCanvas(transformeeBitmap, ttx, tty, previewCanvas);
        }
        ivPreview.invalidate();
        drawSelectionOnView(showMargins);
    }

    private void drawTransformeeOnViewBySelection() {
        clearCanvas(previewCanvas);
        if (hasSelection && transformeeBitmap != null) {
            float ttx = toScaled(selection.left) + window.translationX;
            float tty = toScaled(selection.top) + window.translationY;
            drawBitmapOnCanvas(transformeeBitmap, ttx, tty, previewCanvas);
            transformeeTranslationX = ttx;
            transformeeTranslationY = tty;
        }
        ivPreview.invalidate();
    }

    private void floodFill(int x, int y, int color) {
        new FloodFill(x, y, color).start();
    }

    private boolean isScaledMuch() {
        return window.scale >= 16.0f;
    }

    private void load() {
        viewWidth = imageView.getWidth();
        viewHeight = imageView.getHeight();

        window = new Window();
        windows.add(window);
        bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
        window.bitmap = bitmap;
        currentBitmapIndex = 0;
        canvas = new Canvas(bitmap);
        history = new BitmapHistory();
        window.history = history;
        history.offer(bitmap);
        window.path = null;
        cellGrid = new CellGrid();
        window.cellGrid = cellGrid;

        window.scale = 20.0f;
        imageWidth = 960;
        imageHeight = 960;
        window.translationX = 0.0f;
        window.translationY = 0.0f;

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

        etEraserStrokeWidth.setText(String.valueOf(eraser.getStrokeWidth()));
        etPencilStrokeWidth.setText(String.valueOf(paint.getStrokeWidth()));
        etTextSize.setText(String.valueOf(paint.getTextSize()));

        clearCanvasAndInvalidateView(previewCanvas, ivPreview);

        if (fileToBeOpened != null) {
            openFile(fileToBeOpened);
            windows.remove(0);
            tabLayout.removeTabAt(0);
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cbBucketFillContiguous = findViewById(R.id.cb_bucket_fill_contiguous);
        cbTransformerLar = findViewById(R.id.cb_transformer_lar);
        cbZoom = findViewById(R.id.cb_zoom);
        etEraserStrokeWidth = findViewById(R.id.et_eraser_stroke_width);
        etPencilStrokeWidth = findViewById(R.id.et_pencil_stroke_width);
        etText = findViewById(R.id.et_text);
        etTextSize = findViewById(R.id.et_text_size);
        flImageView = findViewById(R.id.fl_iv);
        imageView = findViewById(R.id.iv);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        ivChessboard = findViewById(R.id.iv_chessboard);
        ivGrid = findViewById(R.id.iv_grid);
        ivPreview = findViewById(R.id.iv_preview);
        ivRulerH = findViewById(R.id.iv_ruler_horizontal);
        ivRulerV = findViewById(R.id.iv_ruler_vertical);
        ivSelection = findViewById(R.id.iv_selection);
        llBehaviorBucketFill = findViewById(R.id.ll_behavior_bucket_fill);
        llBehaviorEraser = findViewById(R.id.ll_behavior_eraser);
        llBehaviorPencil = findViewById(R.id.ll_behavior_pencil);
        llBehaviorShape = findViewById(R.id.ll_behavior_shape);
        llBehaviorText = findViewById(R.id.ll_behavior_text);
        llBehaviorTransformer = findViewById(R.id.ll_behavior_transformer);
        RadioButton rbPencil = findViewById(R.id.rb_pencil);
        rvSwatches = findViewById(R.id.rv_swatches);
        rbTransformer = findViewById(R.id.rb_transformer);
        tabLayout = findViewById(R.id.tl);
        tvStatus = findViewById(R.id.tv_status);
        vBackgroundColor = findViewById(R.id.v_background_color);
        vForegroundColor = findViewById(R.id.v_foreground_color);

        onImgSizeXTextChangedListener = s -> {
            try {
                int i = Integer.parseUnsignedInt(s);
                etImgSizeY.removeTextChangedListener(onImgSizeYTextChangedListener);
                etImgSizeY.setText(String.valueOf(i * bitmap.getHeight() / bitmap.getWidth()));
                etImgSizeY.addTextChangedListener(onImgSizeYTextChangedListener);
            } catch (NumberFormatException e) {
            }
        };

        onImgSizeYTextChangedListener = s -> {
            try {
                int i = Integer.parseUnsignedInt(s);
                etImgSizeX.removeTextChangedListener(onImgSizeXTextChangedListener);
                etImgSizeX.setText(String.valueOf(i * bitmap.getWidth() / bitmap.getHeight()));
                etImgSizeX.addTextChangedListener(onImgSizeXTextChangedListener);
            } catch (NumberFormatException e) {
            }
        };

        findViewById(R.id.b_text_draw).setOnClickListener(v -> drawTextOnCanvas());
        cbZoom.setOnCheckedChangeListener(onZoomToolCheckBoxCheckedChangeListener);
        cbZoom.setTag(onImageViewTouchWithPencilListener);
        etPencilStrokeWidth.addTextChangedListener((AfterTextChangedListener) this::setStrokeWidth);
        etText.addTextChangedListener((AfterTextChangedListener) s -> drawTextOnView());
        etTextSize.addTextChangedListener((AfterTextChangedListener) s -> scaleTextSizeAndDrawTextOnView());
        flImageView.setOnTouchListener(onImageViewTouchWithPencilListener);
        ((RadioButton) findViewById(R.id.rb_bucket_fill)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(buttonView, isChecked, onImageViewTouchWithBucketListener, llBehaviorBucketFill));
        ((RadioButton) findViewById(R.id.rb_circle)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = circle);
        ((RadioButton) findViewById(R.id.rb_eraser)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(buttonView, isChecked, onImageViewTouchWithEraserListener, llBehaviorEraser));
        ((RadioButton) findViewById(R.id.rb_eyedropper)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(buttonView, isChecked, onImageViewTouchWithEyedropperListener, null));
        ((RadioButton) findViewById(R.id.rb_line)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = line);
        ((RadioButton) findViewById(R.id.rb_oval)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = oval);
        rbPencil.setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(buttonView, isChecked, onImageViewTouchWithPencilListener, llBehaviorPencil));
        ((RadioButton) findViewById(R.id.rb_rect)).setOnCheckedChangeListener((OnCheckedListener) () -> shape = rect);
        ((RadioButton) findViewById(R.id.rb_selector)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(buttonView, isChecked, onImageViewTouchWithSelectorListener, null));
        ((RadioButton) findViewById(R.id.rb_shape)).setOnCheckedChangeListener((buttonView, isChecked) -> onToolChange(buttonView, isChecked, onImageViewTouchWithShapeListener, llBehaviorShape));
        ((RadioButton) findViewById(R.id.rb_text)).setOnCheckedChangeListener(onTextRadioButtonCheckedChangeListener);
        rbTransformer.setOnCheckedChangeListener(onTransformerRadioButtonCheckedChangeListener);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        findViewById(R.id.tv_color_add).setOnClickListener(onAddColorViewClickListener);
        vBackgroundColor.setOnClickListener(onBackgroundColorClickListener);
        vForegroundColor.setOnClickListener(onForegroundColorClickListener);

        ((CheckBox) findViewById(R.id.cb_style_fill)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            Paint.Style style = isChecked ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE;
            paint.setStyle(style);
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
        swatches = new LinkedList<Integer>() {
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
        colorAdapter = new ColorAdapter(swatches) {
            {
                setOnItemClickListener(view -> {
                    int color = ((ColorDrawable) view.getBackground()).getColor();
                    paint.setColor(color);
                    vForegroundColor.setBackgroundColor(color);
                    if (llBehaviorText.getVisibility() == View.VISIBLE) {
                        drawTextOnView();
                    }
                });
                setOnItemLongClickListener(view -> {
                    new ColorPicker().show(
                            MainActivity.this,
                            (oldColor, newColor) -> {
                                if (newColor != null) {
                                    swatches.set(swatches.indexOf(oldColor), newColor);
                                } else {
                                    swatches.remove(oldColor);
                                }
                                colorAdapter.notifyDataSetChanged();
                            },
                            (Integer) view.getTag(),
                            true);
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
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {

        canvas = null;
        bitmap.recycle();
        bitmap = null;
        chessboard.recycle();
        chessboard = null;
        chessboardCanvas = null;
        chessboardBitmap.recycle();
        chessboardBitmap = null;
        clipboard.recycle();
        clipboard = null;
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
        transformeeBitmap.recycle();
        transformeeBitmap = null;
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
    private void onToolChange(CompoundButton buttonView, boolean isChecked, View.OnTouchListener onImageViewTouchListener, View toolBehavior) {
        if (isChecked) {
            cbZoom.setChecked(false);
            cbZoom.setTag(onImageViewTouchListener);
            flImageView.setOnTouchListener(onImageViewTouchListener);
        }
        if (toolBehavior != null) {
            toolBehavior.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.i_cell_grid:
                AlertDialog cellGridDialog = new AlertDialog.Builder(this)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, onCellGridDialogPosButtonClickListener)
                        .setTitle(R.string.cell_grid)
                        .setView(R.layout.cell_grid)
                        .show();

                cbCellGridEnabled = cellGridDialog.findViewById(R.id.cb_cg_enabled);
                etCellGridSizeX = cellGridDialog.findViewById(R.id.et_cg_size_x);
                etCellGridSizeY = cellGridDialog.findViewById(R.id.et_cg_size_y);
                etCellGridSpacingX = cellGridDialog.findViewById(R.id.et_cg_spacing_x);
                etCellGridSpacingY = cellGridDialog.findViewById(R.id.et_cg_spacing_y);
                etCellGridOffsetX = cellGridDialog.findViewById(R.id.et_cg_offset_x);
                etCellGridOffsetY = cellGridDialog.findViewById(R.id.et_cg_offset_y);

                cbCellGridEnabled.setChecked(cellGrid.enabled);
                etCellGridSizeX.setText(String.valueOf(cellGrid.sizeX));
                etCellGridSizeY.setText(String.valueOf(cellGrid.sizeY));
                etCellGridSpacingX.setText(String.valueOf(cellGrid.spacingX));
                etCellGridSpacingY.setText(String.valueOf(cellGrid.spacingY));
                etCellGridOffsetX.setText(String.valueOf(cellGrid.offsetX));
                etCellGridOffsetY.setText(String.valueOf(cellGrid.offsetY));
                break;

            case R.id.i_close:
                if (windows.size() == 1) {
                    break;
                }
                recycleBitmapIfIsNotNull(transformeeBitmap);
                transformeeBitmap = null;
                windows.remove(currentBitmapIndex);
                tabLayout.removeTabAt(currentBitmapIndex);
                break;

            case R.id.i_copy:
                if (!hasSelection) {
                    break;
                }
                if (transformeeBitmap == null) {
                    if (clipboard != null) {
                        clipboard.recycle();
                    }
                    clipboard = Bitmap.createBitmap(bitmap, selection.left, selection.top,
                            selection.right - selection.left + 1, selection.bottom - selection.top + 1);
                } else {
                    clipboard = Bitmap.createBitmap(transformeeBitmap);
                }
                break;

            case R.id.i_crop: {
                if (!hasSelection) {
                    break;
                }
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
                int width = selection.right - selection.left + 1, height = selection.bottom - selection.top + 1;
                Bitmap bm = Bitmap.createBitmap(bitmap, selection.left, selection.top, width, height);
                resizeBitmap(width, height, false);
                canvas.drawBitmap(bm, 0.0f, 0.0f, opaquePaint);
                drawBitmapOnView();
                history.offer(bitmap);
                break;
            }

            case R.id.i_cut:
                if (!hasSelection) {
                    break;
                }
                if (transformeeBitmap == null) {
                    if (clipboard != null) {
                        clipboard.recycle();
                    }
                    clipboard = Bitmap.createBitmap(bitmap, selection.left, selection.top,
                            selection.right - selection.left + 1, selection.bottom - selection.top + 1);
                    canvas.drawRect(selection.left, selection.top, selection.right + 1, selection.bottom + 1, eraser);
                    drawBitmapOnView();
                    history.offer(bitmap);
                } else {
                    clipboard = Bitmap.createBitmap(transformeeBitmap);
                    transformeeBitmap.recycle();
                    transformeeBitmap = null;
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                }
                break;

            case R.id.i_delete:
                if (!hasSelection) {
                    break;
                }
                if (transformeeBitmap == null) {
                    canvas.drawRect(selection.left, selection.top, selection.right + 1, selection.bottom + 1, eraser);
                    drawBitmapOnView();
                    history.offer(bitmap);
                } else {
                    transformeeBitmap.recycle();
                    transformeeBitmap = null;
                    clearCanvasAndInvalidateView(previewCanvas, ivPreview);
                }
                break;

            case R.id.i_deselect:
                drawTransformeeOnCanvas();
                drawTextOnCanvas();
                hasSelection = false;
                clearCanvasAndInvalidateView(selectionCanvas, ivSelection);
                tvStatus.setText("");
                break;

            case R.id.i_new:
                drawTransformeeOnCanvas();
                drawTextOnCanvas();

                AlertDialog newGraphicDialog = new AlertDialog.Builder(this)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, onNewGraphicDialogPosButtonClickListener)
                        .setTitle(R.string.new_)
                        .setView(R.layout.new_graphic)
                        .show();

                etNewGraphicSizeX = newGraphicDialog.findViewById(R.id.et_new_size_x);
                etNewGraphicSizeY = newGraphicDialog.findViewById(R.id.et_new_size_y);
                break;

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

                transformeeBitmap = Bitmap.createBitmap(clipboard);
                selection.left = window.translationX >= 0.0f ? 0 : toOriginal(-window.translationX) + 1;
                selection.top = window.translationY >= 0.0f ? 0 : toOriginal(-window.translationY) + 1;
                selection.right = selection.left + Math.min(transformeeBitmap.getWidth(), bitmap.getWidth());
                selection.bottom = selection.top + Math.min(transformeeBitmap.getHeight(), bitmap.getHeight());
                transformeeTranslationX = window.translationX + toScaled(selection.left);
                transformeeTranslationY = window.translationY + toScaled(selection.top);
                hasSelection = true;
                rbTransformer.setChecked(true);
                drawTransformeeAndSelectionOnViewByTranslation();
                break;

            case R.id.i_size:
                drawTransformeeOnCanvas();
                drawTextOnCanvas();

                AlertDialog imageSizeDialog = new AlertDialog.Builder(this)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, onImgSizeDialogPosButtonClickListener)
                        .setTitle(R.string.image_size)
                        .setView(R.layout.image_size)
                        .show();

                cbImgLar = imageSizeDialog.findViewById(R.id.cb_img_lar);
                etImgSizeX = imageSizeDialog.findViewById(R.id.et_img_size_x);
                etImgSizeY = imageSizeDialog.findViewById(R.id.et_img_size_y);
                rbImgStretch = imageSizeDialog.findViewById(R.id.rb_img_stretch);
                rbImgCrop = imageSizeDialog.findViewById(R.id.rb_img_crop);

                cbImgLar.setOnCheckedChangeListener(onImgSizeLarCheckBoxCheckedChangeListener);
                etImgSizeX.setText(String.valueOf(bitmap.getWidth()));
                etImgSizeY.setText(String.valueOf(bitmap.getHeight()));
                rbImgStretch.setChecked(true);
                break;

            case R.id.i_redo: {
                if (history.canRedo()) {
                    undoOrRedo(history.redo());
                }
                break;
            }

            case R.id.i_save:
                save();
                break;

            case R.id.i_save_as:
                saveAs();
                break;

            case R.id.i_select_all:
                selection.left = 0;
                selection.top = 0;
                selection.right = bitmap.getWidth() - 1;
                selection.bottom = bitmap.getHeight() - 1;
                hasSelection = true;
                drawSelectionOnView();
                selectionStartX = selection.left;
                selectionStartY = selection.top;
                selectionEndX = selection.right;
                selectionEndY = selection.bottom;
                break;

            case R.id.i_undo: {
                if (transformeeBitmap != null) {
                    undoOrRedo(history.getCurrent());
                } else if (history.canUndo()) {
                    undoOrRedo(history.undo());
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void recycleBitmapIfIsNotNull(Bitmap bm) {
        if (bm != null) {
            bm.recycle();
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
        canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
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
        } else {
            hasSelection = false;
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
            cv.drawBitmap(bitmap, 0.0f, 0.0f, opaquePaint);
        }
        bitmap.recycle();
        bitmap = bm;
        window.bitmap = bitmap;
        canvas = cv;
        imageWidth = (int) toScaled(width);
        imageHeight = (int) toScaled(height);

        recycleBitmapIfIsNotNull(transformeeBitmap);
        transformeeBitmap = null;
        hasSelection = false;

        drawChessboardOnView();
        drawGridOnView();
        drawSelectionOnView();
    }

    private void save() {
        save(window.path);
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
            bitmap.compress(window.compressFormat, 100, fos);
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

    private void scaleTextSizeAndDrawTextOnView() {
        try {
            paint.setTextSize(toScaled((int) Float.parseFloat(etTextSize.getText().toString())));
        } catch (NumberFormatException e) {
        }
        drawTextOnView();
    }

    private void setStrokeWidth(String s) {
        try {
            float f = Float.parseFloat(s);
            paint.setStrokeWidth(f);
        } catch (NumberFormatException e) {
        }
    }

    private void stretchByBound(float viewX, float viewY) {
        float halfScale = window.scale / 2.0f;
        switch (stretchingBound) {
            case LEFT: {
                int left = toOriginal(viewX - window.translationX + halfScale);
                if (left != selection.left) selection.left = left;
                else return;
                break;
            }
            case TOP: {
                int top = toOriginal(viewY - window.translationY + halfScale);
                if (top != selection.top) selection.top = top;
                else return;
                break;
            }
            case RIGHT: {
                int right = toOriginal(viewX - window.translationX + halfScale) - 1;
                if (right != selection.right) selection.right = right;
                else return;
                break;
            }
            case BOTTOM: {
                int bottom = toOriginal(viewY - window.translationY + halfScale) - 1;
                if (bottom != selection.bottom) selection.bottom = bottom;
                else return;
                break;
            }
            case NULL:
                return;
        }
        if (cbTransformerLar.isChecked()) {
            if (stretchingBound == Position.LEFT || stretchingBound == Position.RIGHT) {
                double width = selection.right - selection.left + 1, height = width / transformeeAspectRatio;
                selection.top = (int) (transformeeCenterVertical - height / 2.0);
                selection.bottom = (int) (transformeeCenterVertical + height / 2.0);
            } else if (stretchingBound == Position.TOP || stretchingBound == Position.BOTTOM) {
                double height = selection.bottom - selection.top + 1, width = height * transformeeAspectRatio;
                selection.left = (int) (transformeeCenterHorizontal - width / 2.0);
                selection.right = (int) (transformeeCenterHorizontal + width / 2.0);
            }
        }
        hasStretched = true;
        drawSelectionOnView(true);
    }

    private int toOriginal(float scaled) {
        return (int) (scaled / window.scale);
    }

    private float toScaled(int original) {
        return original * window.scale;
    }

    private float toScaled(float original) {
        return original * window.scale;
    }

    private void undoOrRedo(Bitmap bm) {
        optimizeSelection();
        bitmap.recycle();
        bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        window.bitmap = bitmap;
        canvas = new Canvas(bitmap);
        canvas.drawBitmap(bm, 0.0f, 0.0f, opaquePaint);

        imageWidth = (int) toScaled(bitmap.getWidth());
        imageHeight = (int) toScaled(bitmap.getHeight());

        recycleBitmapIfIsNotNull(transformeeBitmap);
        transformeeBitmap = null;
        clearCanvasAndInvalidateView(previewCanvas, ivPreview);

        optimizeSelection();
        isShapeStopped = true;
        hasStretched = false;

        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
        drawSelectionOnView();
    }
}