package com.misterchan.iconeditor;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private Bitmap chessboard;
    private Bitmap chessboardBitmap;
    private Bitmap clipboard;
    private Bitmap gridBitmap;
    private Bitmap previewBitmap;
    private Bitmap transformeeBitmap;
    private Bitmap viewBitmap;
    private BitmapHistory history;
    private boolean hasNotLoaded = true;
    private boolean hasSelection = false;
    private Canvas canvas;
    private Canvas chessboardCanvas;
    private Canvas gridCanvas;
    private Canvas previewCanvas;
    private Canvas viewCanvas;
    private final CellGrid cellGrid = new CellGrid();
    private CheckBox cbCellGridEnabled;
    private Bitmap.CompressFormat compressFormat = null;
    private double prevDiagonal;
    private EditText etCellGridOffsetX, etCellGridOffsetY;
    private EditText etCellGridSizeX, etCellGridSizeY;
    private EditText etCellGridSpacingX, etCellGridSpacingY;
    private EditText etNewGraphicSizeX, etNewGraphicSizeY;
    private EditText etPropSizeX, etPropSizeY;
    private EditText etRed, etGreen, etBlue, etAlpha;
    private float pivotX, pivotY;
    private float prevX, prevY;
    private float scale;
    private float transformeeTranslationX, transformeeTranslationY;
    private float translationX, translationY;
    private FrameLayout flImageView;
    private ImageView imageView;
    private ImageView ivChessboard;
    private ImageView ivGrid;
    private ImageView ivPreview;
    private int currentBitmapIndex;
    private int imageWidth, imageHeight;
    private int selectionStartX, selectionStartY;
    private int selectionEndX, selectionEndY;
    private int viewWidth, viewHeight;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private List<BitmapHistory> histories = new ArrayList<>();
    private RadioButton rbColor;
    private RadioButton rbBackgroundColor;
    private RadioButton rbForegroundColor;
    private RadioButton rbPropStretch, rbPropCrop;
    private SeekBar sbRed, sbGreen, sbBlue, sbAlpha;
    private SelectionBounds selection = new SelectionBounds();
    private TabLayout tabLayout;
    private TextView tvStatus;

    private final Map<Bitmap, String> paths = new HashMap<>();

    private final Paint backgroundPaint = new Paint() {

        {
            setAntiAlias(false);
            setColor(Color.WHITE);
            setDither(false);
        }
    };

    private final Paint cellGridPaint = new Paint() {

        {
            setColor(Color.RED);
        }
    };

    private final Paint eraser = new Paint() {

        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
    };

    private final Paint foregroundPaint = new Paint() {

        {
            setAntiAlias(false);
            setColor(Color.BLACK);
            setDither(false);
        }
    };

    private final Paint gridPaint = new Paint() {

        {
            setColor(Color.GRAY);
        }
    };

    private final Paint opaquePaint = new Paint();

    private final Paint pointPaint = new Paint() {

        {
            setColor(Color.RED);
            setStrokeWidth(4.0f);
            setTextSize(32.0f);
        }
    };

    private final Paint selector = new Paint() {

        {
            setColor(Color.DKGRAY);
            setStrokeWidth(4.0f);
            setStyle(Style.STROKE);
        }
    };

    private Paint paint = foregroundPaint;

    private final ActivityResultCallback<Uri> imageActivityResultCallback = result -> {
        if (result == null) {
            return;
        }
        try (InputStream inputStream = getContentResolver().openInputStream(result)) {
            Bitmap bm = BitmapFactory.decodeStream(inputStream);
            openFile(bm, result);
            bm.recycle();

        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    private final ActivityResultLauncher<String> imageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), imageActivityResultCallback);

    private final CompoundButton.OnCheckedChangeListener onBackgroundColorRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            paint = backgroundPaint;
            rbColor = rbBackgroundColor;
            showPaintColorOnSeekBars();
        }
    };

    private final CompoundButton.OnCheckedChangeListener onForegroundColorRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            paint = foregroundPaint;
            rbColor = rbForegroundColor;
            showPaintColorOnSeekBars();
        }
    };

    private final DialogInterface.OnClickListener onCellGridDialogPosButtonClickListener = (dialog, which) -> {
        try {
            cellGrid.enabled = cbCellGridEnabled.isChecked();
            cellGrid.sizeX = Integer.parseInt(etCellGridSizeX.getText().toString());
            cellGrid.sizeY = Integer.parseInt(etCellGridSizeY.getText().toString());
        } catch (NumberFormatException e) {
        }
        drawGridOnView();
    };

    private final DialogInterface.OnClickListener onNewGraphicDialogPosButtonClickListener = (dialog, which) -> {
        try {
            int width = Integer.parseInt(etNewGraphicSizeX.getText().toString());
            int height = Integer.parseInt(etNewGraphicSizeY.getText().toString());
            createGraphic(width, height);
        } catch (NumberFormatException e) {
        }
    };

    private final DialogInterface.OnClickListener onPropDialogPosButtonClickListener = (dialog, which) -> {
        try {
            int width = Integer.parseInt(etPropSizeX.getText().toString());
            int height = Integer.parseInt(etPropSizeY.getText().toString());
            boolean stretch = rbPropStretch.isChecked();
            resizeBitmap(width, height, stretch);
        } catch (NumberFormatException e) {
        }
    };

    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            currentBitmapIndex = tab.getPosition();
            bitmap = bitmaps.get(currentBitmapIndex);
            canvas = new Canvas(bitmap);
            history = histories.get(currentBitmapIndex);

            int width = bitmap.getWidth(), height = bitmap.getHeight();
            scale = (float) ((double) viewWidth / (double) width);
            imageWidth = (int) toScaled(width);
            imageHeight = (int) toScaled(height);
            translationX = 0.0f;
            translationY = 0.0f;

            transformeeBitmap = null;
            hasSelection = false;

            drawChessboardOnView();
            drawBitmapOnView();
            drawGridOnView();
            drawSelectionOnView();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithEraserListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int originalX = toOriginal(x - translationX), originalY = toOriginal(y - translationY);
                canvas.drawPoint(originalX + 0.5f, originalY + 0.5f, eraser);
                drawBitmapOnView();
                tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int originalX = toOriginal(x - translationX), originalY = toOriginal(y - translationY);
                canvas.drawLine(
                        toOriginal(prevX - translationX) + 0.5f,
                        toOriginal(prevY - translationY) + 0.5f,
                        originalX + 0.5f,
                        originalY + 0.5f,
                        eraser);
                drawBitmapOnView();
                tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                prevX = x;
                prevY = y;
            }
            break;
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
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int originalX = toOriginal(x - translationX), originalY = toOriginal(y - translationY);
                paint.setColor(bitmap.getPixel(originalX, originalY));
                showPaintColorOnSeekBars();
                tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                break;
            case MotionEvent.ACTION_UP:
                tvStatus.setText("");
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithPencilListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int originalX = toOriginal(x - translationX), originalY = toOriginal(y - translationY);
                canvas.drawPoint(originalX, originalY, paint);
                drawBitmapOnView();
                tvStatus.setText(String.format("(%d, %d)", originalX, originalY));
                prevX = x;
                prevY = y;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int originalX = toOriginal(x - translationX), originalY = toOriginal(y - translationY);
                canvas.drawLine(
                        toOriginal(prevX - translationX),
                        toOriginal(prevY - translationY),
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
    private final View.OnTouchListener onImageViewTouchWithScalerListener = (v, event) -> {
        switch (event.getPointerCount()) {

            case 1:
                float x = event.getX(), y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        prevX = x;
                        prevY = y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        translationX += x - prevX;
                        translationY += y - prevY;
                        drawChessboardOnView();
                        drawBitmapOnView();
                        drawGridOnView();
                        drawSelectionOnView();
                        prevX = x;
                        prevY = y;
                        break;
                }
                break;

            case 2:
                float x0 = event.getX(0), y0 = event.getY(0),
                        x1 = event.getX(1), y1 = event.getY(1);
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_MOVE:
                        double diagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        double diagonalRatio = diagonal / prevDiagonal;
                        float scale = (float) (this.scale * diagonalRatio);
                        int scaledWidth = (int) (bitmap.getWidth() * scale), scaledHeight = (int) (bitmap.getHeight() * scale);
                        this.scale = scale;
                        imageWidth = scaledWidth;
                        imageHeight = scaledHeight;
                        float pivotX = (float) (this.pivotX * diagonalRatio), pivotY = (float) (this.pivotY * diagonalRatio);
                        translationX = translationX - pivotX + this.pivotX;
                        translationY = translationY - pivotY + this.pivotY;
                        drawChessboardOnView();
                        drawBitmapOnView();
                        drawGridOnView();
                        drawSelectionOnView();
                        this.pivotX = pivotX;
                        this.pivotY = pivotY;
                        prevDiagonal = diagonal;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        this.pivotX = (x0 + x1) / 2.0f - translationX;
                        this.pivotY = (y0 + y1) / 2.0f - translationY;
                        prevDiagonal = Math.sqrt(Math.pow(x0 - x1, 2.0) + Math.pow(y0 - y1, 2.0));
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        prevX = event.getX(1 - event.getActionIndex());
                        prevY = event.getY(1 - event.getActionIndex());
                        break;
                }
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onImageViewTouchWithSelectorListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hasSelection = true;
                selectionStartX = toOriginal(x - translationX);
                selectionStartY = toOriginal(y - translationY);
                selectionEndX = selectionStartX;
                selectionEndY = selectionStartY;
                drawSelectionOnViewByStartsAndEnds();
                tvStatus.setText(String.format("Start: (%d, %d), End: (%d, %d), Area: 1 × 1", selectionStartX, selectionStartY, selectionStartX, selectionStartY));
                break;

            case MotionEvent.ACTION_MOVE:
                selectionEndX = toOriginal(x - translationX);
                selectionEndY = toOriginal(y - translationY);
                drawSelectionOnViewByStartsAndEnds();
                tvStatus.setText(String.format("Start: (%d, %d), End: (%d, %d), Area: %d × %d",
                        selectionStartX, selectionStartY, selectionEndX, selectionEndY,
                        Math.abs(selectionEndX - selectionStartX) + 1, Math.abs(selectionEndY - selectionStartY) + 1));
                break;

            case MotionEvent.ACTION_UP:
                optimizeSelection();
                drawSelectionOnView();
                tvStatus.setText(String.format("Start: (%d, %d), End: (%d, %d), Area: %d × %d",
                        selection.left, selection.top, selection.right, selection.bottom,
                        selection.right - selection.left + 1, selection.bottom - selection.top + 1));
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    View.OnTouchListener onImageViewTouchWithTransformerListener = (v, event) -> {
        float x = event.getX(), y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (transformeeBitmap == null) {
                    transformeeTranslationX = translationX + toScaled(selection.left);
                    transformeeTranslationY = translationY + toScaled(selection.top);
                    transformeeBitmap = Bitmap.createBitmap(bitmap, selection.left, selection.top,
                            selection.right - selection.left + 1, selection.bottom - selection.top + 1);
                    canvas.drawRect(selection.left, selection.top, selection.right + 1, selection.bottom + 1, eraser);
                }
                drawBitmapOnView();
                drawTransformeeOnView();
                tvStatus.setText(String.format("(%d, %d), (%d, %d)", selection.left, selection.top, selection.right, selection.bottom));
                prevX = x;
                prevY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                transformeeTranslationX += x - prevX;
                transformeeTranslationY += y - prevY;
                drawTransformeeOnView();
                tvStatus.setText(String.format("(%d, %d), (%d, %d)", selection.left, selection.top, selection.right, selection.bottom));
                prevX = x;
                prevY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    CompoundButton.OnCheckedChangeListener onTransformerRadioButtonCheckedChangeListener = (buttonView, isChecked) -> {
        if (isChecked) {
            flImageView.setOnTouchListener(onImageViewTouchWithTransformerListener);
        } else {
            if (hasSelection && transformeeBitmap != null) {
                canvas.drawBitmap(transformeeBitmap, selection.left, selection.top, opaquePaint);
                transformeeBitmap.recycle();
                transformeeBitmap = null;
                drawBitmapOnView();
                drawSelectionOnView();
                history.offer(bitmap);
                tvStatus.setText("");
            }
        }
    };

    private void clearCanvas(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    private void createGraphic(int width, int height) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        addBitmap(bitmap, width, height);
    }

    private void drawPoint(Canvas canvas, float x, float y, String text) {
        canvas.drawLine(x - 100.0f, y, x + 100.0f, y, pointPaint);
        canvas.drawLine(x, y - 100.0f, x, y + 100.0f, pointPaint);
        canvas.drawText(text, x, y, pointPaint);
        imageView.invalidate();
    }

    private void drawBitmapOnCanvas(Bitmap bm, float translX, float translY, Canvas cv) {
        int startX = translX >= 0.0f ? 0 : toOriginal(-translX);
        int startY = translY >= 0.0f ? 0 : toOriginal(-translY);
        int bitmapWidth = bm.getWidth(), bitmapHeight = bm.getHeight();
        int scaledBmpWidth = (int) toScaled(bitmapWidth), scaledBmpHeight = (int) toScaled(bitmapHeight);
        int endX = Math.min(toOriginal(translX + scaledBmpWidth <= viewWidth ? scaledBmpWidth : viewWidth - translX) + 1, bitmapWidth);
        int endY = Math.min(toOriginal(translY + scaledBmpHeight <= viewHeight ? scaledBmpHeight : viewHeight - translY) + 1, bitmapHeight);
        float left = translX >= 0.0f ? translX : translX % scale;
        float top = translY >= 0.0f ? translY : translY % scale;
        if (isScaledMuch()) {
            float t = top, b = t + scale;
            Paint paint = new Paint();
            for (int y = startY; y < endY; ++y, t += scale, b += scale) {
                float l = left;
                for (int x = startX; x < endX; ++x) {
                    paint.setColor(bm.getPixel(x, y));
                    cv.drawRect(l, t, l += scale, b, paint);
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
        drawBitmapOnCanvas(bitmap, translationX, translationY, viewCanvas);
        imageView.invalidate();
    }

    private void drawChessboardOnView() {
        clearCanvas(chessboardCanvas);
        float left = Math.max(0.0f, translationX);
        float top = Math.max(0.0f, translationY);
        float right = Math.min(translationX + imageWidth, viewWidth);
        float bottom = Math.min(translationY + imageHeight, viewHeight);
        chessboardCanvas.drawBitmap(chessboard,
                new Rect((int) left, (int) top, (int) right, (int) bottom),
                new RectF(left, top, right, bottom),
                opaquePaint);
        ivChessboard.invalidate();
    }

    private void drawGridOnView() {
        clearCanvas(gridCanvas);
        float startX = translationX >= 0.0f ? translationX : translationX % scale;
        float startY = translationY >= 0.0f ? translationY : translationY % scale;
        float endX = Math.min(translationX + imageWidth, viewWidth);
        float endY = Math.min(translationY + imageHeight, viewHeight);
        if (isScaledMuch()) {
            for (float x = startX; x < endX; x += scale) {
                gridCanvas.drawLine(x, startY, x, endY, gridPaint);
            }
            for (float y = startY; y < endY; y += scale) {
                gridCanvas.drawLine(startX, y, endX, y, gridPaint);
            }
        }
        if (cellGrid.enabled) {
            if (cellGrid.sizeX > 0) {
                float scaledSizeX = toScaled(cellGrid.sizeX);
                startX = translationX >= 0.0f ? translationX : translationX % scaledSizeX + toScaled(cellGrid.offsetX);
                for (float x = startX; x < endX; x += scaledSizeX) {
                    gridCanvas.drawLine(x, startY, x, endY, cellGridPaint);
                }
            }
            if (cellGrid.sizeY > 0) {
                float scaledSizeY = toScaled(cellGrid.sizeY);
                startY = translationY >= 0.0f ? translationY : translationY % scaledSizeY + toScaled(cellGrid.offsetY);
                for (float y = startY; y < endY; y += scaledSizeY) {
                    gridCanvas.drawLine(startX, y, endX, y, cellGridPaint);
                }
            }
        }
        ivGrid.invalidate();
    }

    private void drawSelectionOnViewByStartsAndEnds() {
        clearCanvas(previewCanvas);
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
            previewCanvas.drawRect(
                    translationX + toScaled(selection.left),
                    translationY + toScaled(selection.top),
                    translationX + toScaled(selection.right + 1),
                    translationY + toScaled(selection.bottom + 1),
                    selector);
        }
        ivPreview.invalidate();
    }

    private void drawSelectionOnView() {
        clearCanvas(previewCanvas);
        if (hasSelection) {
            previewCanvas.drawRect(
                    translationX + toScaled(selection.left),
                    translationY + toScaled(selection.top),
                    translationX + toScaled(selection.right + 1),
                    translationY + toScaled(selection.bottom + 1),
                    selector);
        }
        ivPreview.invalidate();
    }

    private void drawTransformeeOnView() {
        clearCanvas(previewCanvas);
        selection.left = toOriginal(transformeeTranslationX - translationX);
        selection.top = toOriginal(transformeeTranslationY - translationY);
        selection.right = selection.left + transformeeBitmap.getWidth() - 1;
        selection.bottom = selection.top + transformeeBitmap.getHeight() - 1;
        float ttx = toScaled(selection.left) + translationX;
        float tty = toScaled(selection.top) + translationY;
        drawBitmapOnCanvas(transformeeBitmap, ttx, tty, previewCanvas);
        optimizeSelection();
        if (hasSelection) {
            previewCanvas.drawRect(
                    translationX + toScaled(selection.left),
                    translationY + toScaled(selection.top),
                    translationX + toScaled(selection.right + 1),
                    translationY + toScaled(selection.bottom + 1),
                    selector);
        }
        ivPreview.invalidate();
    }

    private boolean isScaledMuch() {
        return scale >= 16.0f;
    }

    private void load() {
        viewWidth = imageView.getWidth();
        viewHeight = imageView.getHeight();

        bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
        bitmaps.add(bitmap);
        currentBitmapIndex = 0;
        canvas = new Canvas(bitmap);
        history = new BitmapHistory();
        histories.add(history);
        history.offer(bitmap);

        scale = 20.0f;
        imageWidth = 960;
        imageHeight = 960;
        translationX = 0.0f;
        translationY = 0.0f;

        viewBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        viewCanvas = new Canvas(viewBitmap);
        imageView.setImageBitmap(viewBitmap);

        gridBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        gridCanvas = new Canvas(gridBitmap);
        ivGrid.setImageBitmap(gridBitmap);
        drawGridOnView();

        chessboardBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        chessboardCanvas = new Canvas(chessboardBitmap);
        ivChessboard.setImageBitmap(chessboardBitmap);
        drawChessboardOnView();

        previewBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        previewCanvas = new Canvas(previewBitmap);
        ivPreview.setImageBitmap(previewBitmap);
        drawSelectionOnView();

        tabLayout.addTab(tabLayout.newTab().setText("Untitled").setTag(bitmap));
    }

    private void addBitmap(Bitmap bitmap, int width, int height) {
        addBitmap(bitmap, width, height, "Untitled");
    }

    private void addBitmap(Bitmap bitmap, int width, int height, String title) {
        bitmaps.add(bitmap);
        currentBitmapIndex = bitmaps.size() - 1;
        history = new BitmapHistory();
        histories.add(history);
        history.offer(bitmap);

        scale = (float) ((double) viewWidth / (double) width);
        imageWidth = (int) toScaled(width);
        imageHeight = (int) toScaled(height);
        translationX = 0.0f;
        translationY = 0.0f;

        hasSelection = false;

        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
        drawSelectionOnView();

        tabLayout.addTab(tabLayout.newTab().setText(title).setTag(bitmap));
        tabLayout.getTabAt(currentBitmapIndex).select();
    }

    private void onChannelChanged(String hex, SeekBar seekBar) {
        try {
            seekBar.setProgress(Integer.parseUnsignedInt(hex, 16));
            int color = Color.argb(
                    sbAlpha.getProgress(),
                    sbRed.getProgress(),
                    sbGreen.getProgress(),
                    sbBlue.getProgress());
            paint.setColor(color);
            rbColor.setTextColor(color);

        } catch (Exception e) {
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etAlpha = findViewById(R.id.et_alpha);
        etBlue = findViewById(R.id.et_blue);
        etGreen = findViewById(R.id.et_green);
        etRed = findViewById(R.id.et_red);
        flImageView = findViewById(R.id.fl_iv);
        imageView = findViewById(R.id.iv);
        ivChessboard = findViewById(R.id.iv_chessboard);
        ivGrid = findViewById(R.id.iv_grid);
        ivPreview = findViewById(R.id.iv_preview);
        rbBackgroundColor = findViewById(R.id.rb_background_color);
        rbForegroundColor = findViewById(R.id.rb_foreground_color);
        rbColor = rbForegroundColor;
        sbAlpha = findViewById(R.id.sb_alpha);
        sbBlue = findViewById(R.id.sb_blue);
        sbGreen = findViewById(R.id.sb_green);
        sbRed = findViewById(R.id.sb_red);
        tabLayout = findViewById(R.id.tl);
        tvStatus = findViewById(R.id.tv_status);

        etAlpha.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbAlpha));
        etBlue.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbBlue));
        etGreen.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbGreen));
        etRed.addTextChangedListener((AfterTextChangedListener) s -> onChannelChanged(s, sbRed));
        flImageView.setOnTouchListener(onImageViewTouchWithPencilListener);
        rbBackgroundColor.setOnCheckedChangeListener(onBackgroundColorRadioButtonCheckedChangeListener);
        rbForegroundColor.setOnCheckedChangeListener(onForegroundColorRadioButtonCheckedChangeListener);
        ((RadioButton) findViewById(R.id.rb_cropper)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(null));
        ((RadioButton) findViewById(R.id.rb_eraser)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(onImageViewTouchWithEraserListener));
        ((RadioButton) findViewById(R.id.rb_eyedropper)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(onImageViewTouchWithEyedropperListener));
        ((RadioButton) findViewById(R.id.rb_pencil)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(onImageViewTouchWithPencilListener));
        ((RadioButton) findViewById(R.id.rb_scaler)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(onImageViewTouchWithScalerListener));
        ((RadioButton) findViewById(R.id.rb_selector)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(onImageViewTouchWithSelectorListener));
        ((RadioButton) findViewById(R.id.rb_text)).setOnCheckedChangeListener((OnCheckListener) () -> flImageView.setOnTouchListener(null));
        ((RadioButton) findViewById(R.id.rb_transformer)).setOnCheckedChangeListener(onTransformerRadioButtonCheckedChangeListener);
        sbAlpha.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etAlpha.setText(String.format("%02X", progress)));
        sbBlue.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etBlue.setText(String.format("%02X", progress)));
        sbGreen.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etGreen.setText(String.format("%02X", progress)));
        sbRed.setOnSeekBarChangeListener((OnProgressChangeListener) progress -> etRed.setText(String.format("%02X", progress)));
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        chessboard = BitmapFactory.decodeResource(getResources(), R.mipmap.chessboard);
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

        viewCanvas = null;
        viewBitmap.recycle();
        viewBitmap = null;

        gridCanvas = null;
        gridBitmap.recycle();
        gridBitmap = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.i_cell_grid:
                AlertDialog cellGridDialog = new AlertDialog.Builder(this)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("OK", onCellGridDialogPosButtonClickListener)
                        .setTitle("Cell Grid")
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
                if (bitmaps.size() == 1) {
                    break;
                }
                bitmaps.remove(currentBitmapIndex);
                histories.remove(currentBitmapIndex);
                tabLayout.removeTabAt(currentBitmapIndex);
                break;

            case R.id.i_copy:
                if (clipboard != null) {
                    clipboard.recycle();
                }
                clipboard = Bitmap.createBitmap(bitmap, selection.left, selection.top,
                        selection.right - selection.left + 1, selection.bottom - selection.top + 1);
                break;

            case R.id.i_delete:
                if (hasSelection) {
                    canvas.drawRect(selection.left, selection.top, selection.right + 1, selection.bottom + 1, eraser);
                }
                break;

            case R.id.i_new:
                AlertDialog newGraphicDialog = new AlertDialog.Builder(this)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("OK", onNewGraphicDialogPosButtonClickListener)
                        .setTitle("New")
                        .setView(R.layout.new_graphic)
                        .show();

                etNewGraphicSizeX = newGraphicDialog.findViewById(R.id.et_new_size_x);
                etNewGraphicSizeY = newGraphicDialog.findViewById(R.id.et_new_size_y);
                break;

            case R.id.i_open:
                imageActivityResultLauncher.launch("image/*");
                break;

            case R.id.i_paste:
                if (clipboard == null) {
                    break;
                }
                transformeeBitmap = Bitmap.createBitmap(clipboard);
                transformeeTranslationX = translationX;
                transformeeTranslationY = translationY;
                selection.left = 0;
                selection.top = 0;
                selection.right = Math.min(transformeeBitmap.getWidth(), bitmap.getWidth());
                selection.bottom = Math.min(transformeeBitmap.getHeight(), bitmap.getHeight());
                hasSelection = true;
                drawTransformeeOnView();
                break;

            case R.id.i_properties:
                AlertDialog propertiesDialog = new AlertDialog.Builder(this)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("OK", onPropDialogPosButtonClickListener)
                        .setTitle("Properties")
                        .setView(R.layout.properties)
                        .show();

                etPropSizeX = propertiesDialog.findViewById(R.id.et_prop_size_x);
                etPropSizeY = propertiesDialog.findViewById(R.id.et_prop_size_y);
                rbPropStretch = propertiesDialog.findViewById(R.id.rb_prop_stretch);
                rbPropCrop = propertiesDialog.findViewById(R.id.rb_prop_crop);

                etPropSizeX.setText(String.valueOf(bitmap.getWidth()));
                etPropSizeY.setText(String.valueOf(bitmap.getHeight()));
                rbPropStretch.setChecked(true);
                break;

            case R.id.i_redo: {
                if (history.canRedo()) {
                    optimizeSelection();
                    Bitmap next = history.redo();
                    bitmap.recycle();
                    bitmap = Bitmap.createBitmap(next.getWidth(), next.getHeight(), Bitmap.Config.ARGB_8888);
                    bitmaps.set(currentBitmapIndex, bitmap);
                    canvas = new Canvas(bitmap);
                    canvas.drawBitmap(next, 0.0f, 0.0f, opaquePaint);
                    drawBitmapOnView();
                }
                break;
            }

            case R.id.i_save:
                save();
                break;

            case R.id.i_undo: {
                if (history.canUndo()) {
                    optimizeSelection();
                    Bitmap prev = history.undo();
                    bitmap.recycle();
                    bitmap = Bitmap.createBitmap(prev.getWidth(), prev.getHeight(), Bitmap.Config.ARGB_8888);
                    bitmaps.set(currentBitmapIndex, bitmap);
                    canvas = new Canvas(bitmap);
                    canvas.drawBitmap(prev, 0.0f, 0.0f, opaquePaint);
                    drawBitmapOnView();
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

    private void openFile(Bitmap bm, Uri uri) {
        int width = bm.getWidth(), height = bm.getHeight();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawBitmap(bm, 0.0f, 0.0f, paint);
        DocumentFile documentFile = DocumentFile.fromSingleUri(this, uri);
        addBitmap(bitmap, width, height, documentFile.getName());

        paths.put(bitmap, UriToPathUtil.getRealFilePath(this, uri));

        switch (documentFile.getType()) {
            case "image/jpg":
                compressFormat = Bitmap.CompressFormat.JPEG;
                break;
            case "image/png":
                compressFormat = Bitmap.CompressFormat.PNG;
                break;
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
        bitmaps.set(currentBitmapIndex, bitmap);
        canvas = cv;
        history.offer(bitmap);
        imageWidth = (int) toScaled(width);
        imageHeight = (int) toScaled(height);

        hasSelection = false;

        drawChessboardOnView();
        drawBitmapOnView();
        drawGridOnView();
        drawSelectionOnView();
    }

    private void save() {
        save(paths.get(bitmap));
    }

    private void save(String path) {
        if (path == null) {
            return;
        }

        File file = new File(path);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(compressFormat, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }

    private void saveAs() {
        String path = null;
        save(path);
    }

    private void showPaintColorOnSeekBars() {
        int color = paint.getColor();
        int red = Color.red(color),
                green = Color.green(color),
                blue = Color.blue(color),
                alpha = Color.alpha(color);

        sbRed.setProgress(red);
        etRed.setText(String.format("%02X", red));

        sbGreen.setProgress(green);
        etGreen.setText(String.format("%02X", green));

        sbBlue.setProgress(blue);
        etBlue.setText(String.format("%02X", blue));

        sbAlpha.setProgress(alpha);
        etAlpha.setText(String.format("%02X", alpha));
    }

    private int toOriginal(float scaled) {
        return (int) (scaled / scale);
    }

    private float toScaled(int original) {
        return original * scale;
    }
}